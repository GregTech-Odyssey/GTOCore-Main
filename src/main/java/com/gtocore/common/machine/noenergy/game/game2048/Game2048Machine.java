package com.gtocore.common.machine.noenergy.game.game2048;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.lwjgl.glfw.GLFW;

/**
 * 「2048」游戏机：4×4 盘面的推格合并玩法。
 *
 * <p>
 * 一局攒积分，盘面再也推不动时这一局结束、按兑现比例换成游戏币；按「重新开始」提前兑现重开。余额、取出与快照通道
 * 来自 {@link AbstractGameMachine}，本局积分、盘面与界面都在这个类里。
 *
 * <p>
 * 盘面用键盘操作：W A S D 与四个方向键各对应一次移动（见 {@link BoardInput#keyPressed}），请求发到服务端由服务端改
 * 盘面，客户端把下发来的快照画进固定大小的盘面区。
 */
@DataGeneratorScanned
public class Game2048Machine extends AbstractGameMachine<Game2048Machine.BoardSnapshot> {

    @RegisterLanguage(cn = "本局积分", en = "Current score")
    private static final String LINE_SCORE = "gtocore.machine.game.line.score";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.game.button.restart";

    /** 盘面区宽度：顶部图例 15 档 × {@link #LEGEND_ICON_SIZE} 正好铺满，4×4 的格子按它摊满整框。 */
    private static final int FRAME_SIZE = 156;

    /**
     * 额外分：一局里 {@link #SCORE_BONUS_MIN_VALUE} 及以上的每个数字<b>首次</b>被合出时各给一次，金额随数字递增；
     * 水位就是当时盘面上的最大数字，合并只会把它顶得更高，所以直接和上一步的盘面比。
     */
    private static final int SCORE_BONUS_MIN_VALUE = 1024;
    private static final int SCORE_BONUS_BASE = 1000;
    /** 兑现基线：超过它的部分才折算游戏币，折算比例见 {@link Game2048Rules#POINTS_PER_COIN}。 */
    public static final int REDEMPTION_BASELINE = 2000;

    private static final int NO_DIRECTION = -1;
    /** 「没有盘面」用空数组而不是 null；首次打开界面时会立刻换成新发的一局。 */
    private static final int[] NO_BOARD = new int[0];

    /** 图例里每个电路的固定边长：15 档 × 10.4px 排进 156 宽的盘面区。 */
    private static final float LEGEND_ICON_SIZE = 10.4f;
    /** 盘面区顶部那条图例的高度：一排电压等级图标 + 底部 1px 分隔线。 */
    private static final int LEGEND_HEIGHT = 15;

    /**
     * 数字用通用电路显示：数组下标就是电路档位（{@code UNIVERSAL_CIRCUIT} 从最低到最高共 15 档），
     * 格子越大用越高电压等级的电路，每格始终只有 1 个。
     */
    private static final ItemStack[] TILE_STACKS = new ItemStack[GTOItems.UNIVERSAL_CIRCUIT.length];

    static {
        for (int tier = 0; tier < TILE_STACKS.length; tier++) {
            TILE_STACKS[tier] = new ItemStack(GTOItems.UNIVERSAL_CIRCUIT[tier].get());
        }
    }

    /** 本局累计积分。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int score;
    /** 局面阶段；只有服务端看它，界面按盘面自己判断能不能动。 */
    @SaveToDisk
    private int gameStatus = Status.IDLE.ordinal();
    /** 本局是否已经结过账。不存盘：读档时按局面阶段还原（已结束的那一局在结束时就结过账了）。 */
    private boolean settled;

    /** 局面阶段：还没开局 / 进行中 / 已结束（结束的那一局已经兑现过）。 */
    private enum Status {
        IDLE,
        PLAYING,
        GAME_OVER
    }

    /** 规则实例；临时容器跟着它复用。 */
    private final Game2048Rules rules = Game2048Rules.create();
    /** 服务端权威盘面，按行优先存 {@code SIZE * SIZE} 格，空格为 0。 */
    @SaveToDisk
    private int[] board = NO_BOARD;

    public Game2048Machine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** 通道里下发的盘面；相等性只看版本号，省掉逐元素比整盘数字。 */
    public record BoardSnapshot(int revision, int[] board) {

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, NO_BOARD);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::board,
                BoardSnapshot::new);

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof BoardSnapshot snapshot && revision == snapshot.revision;
        }

        @Override
        public int hashCode() {
            return revision;
        }
    }

    // ==================== 局面 ====================

    /** 读档后的收尾：盘面尺寸与存档对不上就作废重来。只在服务端调用。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    /**
     * 先把局面阶段夹回合法值，再整理盘面；长度对不上（旧档）会被整成空盘，进而当成「还没开这一局」，等下次打开界面
     * 重发——那种情况下残留的积分不属于任何一局，一并清掉，免得被当成有效成绩兑现。
     */
    private void sanitizeState() {
        if (gameStatus < 0 || gameStatus >= Status.values().length) setIdle();
        board = rules.normalizeBoard(board);
        // 已结束的那一局在结束时就已经兑现过，读档后不能再结一次账
        settled = isGameOver();
        if (!Game2048Rules.isEmpty(board)) return;
        score = 0;
        setIdle();
    }

    @Override
    protected BoardSnapshot createView() {
        return new BoardSnapshot(revision(), board);
    }

    private boolean isPlaying() {
        return gameStatus == Status.PLAYING.ordinal();
    }

    private boolean isGameOver() {
        return gameStatus == Status.GAME_OVER.ordinal();
    }

    private void setIdle() {
        gameStatus = Status.IDLE.ordinal();
    }

    /** 发一局新牌：按当前规则把盘面铺出来。 */
    private void dealBoard() {
        board = rules.createBoard(random());
    }

    /** 服务端：清零本局成绩、进入进行中，并重新打开「可以结账」的开关。 */
    private void startRound() {
        dealBoard();
        score = 0;
        settled = false;
        gameStatus = Status.PLAYING.ordinal();
    }

    /**
     * 把本局积分折成游戏币入余额；同一局只结一次账（{@link #settled} 一置位，结束、重新开始、拆机器都不会再给第二次）。
     * 分数没超过兑现基线时同样算结过账，只是这次给 0 枚。
     */
    @Override
    protected void cashOut() {
        if (settled) return;
        settled = true;
        long reward = (score - REDEMPTION_BASELINE) / Game2048Rules.POINTS_PER_COIN;
        if (reward > 0) addBalance(reward);
    }

    /** 玩不动了：把这局积分兑现掉，再记下结束。先结账后改状态，任何顺序都不会漏给或重复给。 */
    private void endRound() {
        cashOut();
        gameStatus = Status.GAME_OVER.ordinal();
    }

    /** 服务端：结掉当前这一局——兑现本局积分并回到「没开局」，成绩清零。 */
    private void settleRound() {
        cashOut();
        score = 0;
        setIdle();
    }

    /** 服务端：「重新开始」按钮：结掉当前这一局再开一局。没有进行中的一局时按下也无副作用。 */
    private void restartRoundOnServer() {
        if (isRemote()) return;
        settleRound();
        startRound();
        syncAndMarkChanged();
    }

    /** 首次打开界面时把没开的那一局开起来。 */
    private void ensureStarted() {
        if (gameStatus == Status.IDLE.ordinal()) startRound();
    }

    // ==================== 服务端动作 ====================

    /**
     * 服务端：朝 {@code directionOrdinal} 推一次（下标对应 {@link Game2048Rules.Direction}，网络来的值一律校验）。
     *
     * <p>
     * 推不动时 {@link Game2048Rules#move} 返回 {@code null}，状态一律不变。合并得分之外，本局首次合出的
     * {@link #SCORE_BONUS_MIN_VALUE} 及以上数字各再给一次 {@code (数字 / 1024) × SCORE_BONUS_BASE}。
     */
    private void moveOnServer(int directionOrdinal) {
        if (isRemote() || !isPlaying()) return;
        var direction = Game2048Rules.Direction.of(directionOrdinal);
        if (direction == null) return;
        var level = getLevel();
        if (level == null) return;
        int previousMax = Game2048Rules.maxValue(board);
        var move = rules.move(board, direction, level.getRandom());
        if (move == null) return;
        board = move.board();
        int gained = move.gained();
        int reachedValue = Math.min(Game2048Rules.VALUE_MAX, Game2048Rules.maxValue(board));
        if (reachedValue > previousMax) {
            for (int value = Math.max(previousMax * 2, SCORE_BONUS_MIN_VALUE); value <= reachedValue; value *= 2) {
                gained += value / SCORE_BONUS_MIN_VALUE * SCORE_BONUS_BASE;
            }
        }
        score = (int) Math.min(Integer.MAX_VALUE, (long) score + gained);
        if (move.gameOver()) endRound();
        syncAndMarkChanged();
    }

    /** 键盘到方向的映射：W A S D 与四个方向键都收，其余键返回 {@link #NO_DIRECTION}。 */
    private static int directionOf(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT -> Game2048Rules.Direction.LEFT.ordinal();
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP -> Game2048Rules.Direction.UP.ordinal();
            case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT -> Game2048Rules.Direction.RIGHT.ordinal();
            case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN -> Game2048Rules.Direction.DOWN.ordinal();
            default -> NO_DIRECTION;
        };
    }

    // ==================== 界面 ====================

    /** 主页 = 盘面区 + 状态面板；盘面在左、状态在右的顺序两端必须一致，通道里的条目编号才对得上。 */
    @Override
    public Widget createUIWidget() {
        if (!isRemote()) ensureStarted();

        var page = Form.page();
        page.addChildren(createGameArea(), createStatusPanel());
        return page;
    }

    /** 状态面板：余额 + 本局积分 + 重新开始/取出全部。 */
    private StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance()));
        status.addLine(LINE_SCORE, () -> number(score));
        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(
                Button.translatable(77, BUTTON_RESTART).setOnServerClick(this::restartRoundOnServer),
                createWithdrawButton(77)));
        return status;
    }

    /** 带底图的面板，行距统一 2px；状态面板与游戏区都用它。 */
    private static StatusPanel panel() {
        var panel = new StatusPanel();
        panel.getLayoutStyle().gapAll(2);
        return panel;
    }

    /** 盘面区：外层面板当底并把盘面居中，盘面本体是 {@link BoardInput}。 */
    private UIElement createGameArea() {
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChild(new BoardInput());
    }

    /**
     * 盘面本体：一次画完整盘，同时是界面的键盘入口。
     *
     * <p>
     * 控件按 {@link #FRAME_SIZE} 定尺寸（顶部图例铺满一行，4×4 的棋盘把整框填满）。
     *
     * <p>
     * 键盘事件由 LDLib 从界面根控件逐层向下派发（{@code WidgetGroup.keyPressed}），本元素在派发链上，所以不需要
     * 焦点；收到的移动请求经 {@link RPC} 上行，服务端照旧做一次校验再改盘面。盘面值只在绘制时读同步快照，
     * 两端的控件树完全一致，通道里的条目顺序（先盘面、后请求）也因此一致。
     */
    private final class BoardInput extends UIElement {

        private final RPC<Integer> move;

        private BoardInput() {
            layout(style -> style.size(FRAME_SIZE, LEGEND_HEIGHT + FRAME_SIZE));
            registerView(this, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
            move = addRPC(ByteBufCodecs.VAR_INT, (player, direction) -> moveOnServer(direction));
        }

        /** 顶部图例：全部电压等级的电路按固定尺寸横排，底部一条横线跟盘面分开。 */
        @OnlyIn(Dist.CLIENT)
        private void drawLegend(GuiGraphics graphics, int left, int top, int width) {
            int count = TILE_STACKS.length;
            float step = Math.max(LEGEND_ICON_SIZE, (float) width / count);
            float offsetY = (LEGEND_HEIGHT - 1 - LEGEND_ICON_SIZE) / 2;
            for (int tier = 0; tier < count; tier++) {
                var pose = graphics.pose();
                pose.pushPose();
                pose.translate(left + tier * step + (step - LEGEND_ICON_SIZE) / 2, top + offsetY, 0);
                float scale = LEGEND_ICON_SIZE / 16.0F;
                pose.scale(scale, scale, 1.0F);
                graphics.renderItem(TILE_STACKS[tier], 0, 0);
                pose.popPose();
            }
            graphics.fill(left, top + LEGEND_HEIGHT - 1, left + width, top + LEGEND_HEIGHT, 0xFF6A7280);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int direction = directionOf(keyCode);
            if (direction == NO_DIRECTION) return super.keyPressed(keyCode, scanCode, modifiers);
            move.send(direction);
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            int[] current = view().board();
            // 4×4 的格子按整框摊满：156 / 4 = 39 一格，和旧版 4×4 的大小一致
            int cell = FRAME_SIZE / Game2048Rules.SIZE;
            int board = cell * Game2048Rules.SIZE;
            int left = getPositionX() + (FRAME_SIZE - board) / 2;
            int legendTop = getPositionY();
            drawLegend(graphics, getPositionX(), legendTop, FRAME_SIZE);
            int top = legendTop + LEGEND_HEIGHT + (FRAME_SIZE - board) / 2;
            for (int row = 0; row < Game2048Rules.SIZE; row++) {
                for (int col = 0; col < Game2048Rules.SIZE; col++) {
                    int index = row * Game2048Rules.SIZE + col;
                    int value = index < current.length ? current[index] : Game2048Rules.EMPTY;
                    if (value < Game2048Rules.VALUE_MIN || value > Game2048Rules.VALUE_MAX) continue;
                    int exponent = Integer.numberOfTrailingZeros(value);
                    var stack = TILE_STACKS[Math.min(exponent - 1, TILE_STACKS.length - 1)];
                    var pose = graphics.pose();
                    pose.pushPose();
                    pose.translate(left + col * cell, top + row * cell, 0);
                    float scale = cell / 16.0F;
                    pose.scale(scale, scale, 1.0F);
                    graphics.renderItem(stack, 0, 0);
                    pose.popPose();
                }
            }
        }
    }
}

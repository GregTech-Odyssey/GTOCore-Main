package com.gtocore.common.machine.noenergy.game.justGet10;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.render.UIDraw;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * 「合成十」游戏机：5×5 盘面的合并玩法。
 *
 * <p>
 * 一局攒积分，盘面无相邻同数字时这一局结束、按兑现比例换成游戏币；按「重新开始」提前兑现重开。余额、取出与快照通道
 * 来自 {@link AbstractGameMachine}，本局积分、盘面与界面都在这个类里。
 *
 * <p>
 * 每个数字格是一个 {@link Cell}（外裹一层转发坐标，里面是 {@link CellButton}），盘面走界面通道，只在盘面或提起位变了
 * 的那一 tick 下发。
 */
@DataGeneratorScanned
public class JustGet10Machine extends AbstractGameMachine<JustGet10Machine.BoardSnapshot> {

    @RegisterLanguage(cn = "本局积分", en = "Current score")
    private static final String LINE_SCORE = "gtocore.machine.game.line.score";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.game.button.restart";

    /** 格子材质原尺寸（和数字材质一致）。 */
    private static final int CELL_SIZE = 24;

    /**
     * 额外分：一局里 {@link #SCORE_BONUS_MIN_VALUE}~{@link JustGet10Rules#VALUE_MAX} 的每个数字<b>首次</b>被合出时各给
     * 一次，金额随数字递增；水位记在 {@link #maxValue} 上。
     */
    private static final int SCORE_BONUS_MIN_VALUE = 10;
    private static final int SCORE_BONUS_BASE = 1000;
    private static final int SCORE_BONUS_PER_VALUE = 500;
    /**
     * 兑现门槛：一局积分超过它的部分才按比例折成游戏币，达不到就一枚不给。公开给工具提示复用，免得两处各写一个数。
     */
    public static final int REDEMPTION_BASELINE = 1000;

    /** 提起位图：一个 {@code long} 装 {@link Long#SIZE} 格，25 格一个字够用。 */
    private static final int SELECTION_WORDS = (JustGet10Rules.CELL_COUNT + Long.SIZE - 1) / Long.SIZE;
    private static final long[] NO_SELECTION = new long[0];
    /** 「没有盘面」用空数组而不是 null；首次打开界面时会立刻换成随机盘面。 */
    private static final int[] NO_BOARD = new int[0];

    /** 数字材质：下标是数字，1~20 各有一张普通与一张「已提起」的图。 */
    private static final IGuiTexture[] NUMBER_TEXTURES = new IGuiTexture[JustGet10Rules.VALUE_MAX + 1];
    private static final IGuiTexture[] NUMBER_TEXTURES_PRESSED = new IGuiTexture[JustGet10Rules.VALUE_MAX + 1];

    static {
        String path = "gtocore:textures/gui/game/just_get_10/just_get_10_display_";
        for (int value = JustGet10Rules.VALUE_MIN; value <= JustGet10Rules.VALUE_MAX; value++) {
            NUMBER_TEXTURES[value] = new ResourceTexture(path + value + ".png");
            NUMBER_TEXTURES_PRESSED[value] = new ResourceTexture(path + value + "_highlighted.png");
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
    private final JustGet10Rules rules = JustGet10Rules.create();

    @SaveToDisk(defaultValue = "true")
    @SyncToClient(scheduleUpdate = true)
    private boolean enabled = true;
    /** 服务端权威盘面，按行优先存 {@code size * size} 格，空格为 -1。 */
    @SaveToDisk
    private int[] board = NO_BOARD;
    /** 本局出现过的最大数字 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int maxValue = JustGet10Rules.VALUE_MIN;
    /** 已提起的格子位图；提到的是「点中那格所在的整个连通组」，格数超过 64，所以用位图而不是位掩码。 */
    @SaveToDisk
    private long[] selection = NO_SELECTION;

    public JustGet10Machine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** 通道里下发的盘面；相等性只看版本号，省掉逐元素比格子。 */
    public record BoardSnapshot(int revision, int[] board, long[] selection) {

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, NO_BOARD, NO_SELECTION);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::board,
                ByteBufCodecs.VAR_LONG_ARRAY, BoardSnapshot::selection,
                BoardSnapshot::new);

        /** 第 {@code cell} 格是否已被提起。 */
        boolean isSelected(int cell) {
            return JustGet10Machine.isSelected(selection, cell);
        }

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

    /** 读档后的收尾：盘面尺寸与存档对不上就作废重来，其余数值夹回合法范围。只在服务端调用。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    /**
     * 先把局面阶段夹回合法值，再整理盘面；盘面尺寸对不上（旧档）本局作废，等下次打开界面重发，其余数值夹回合法范围。
     */
    private void sanitizeState() {
        if (gameStatus < 0 || gameStatus >= Status.values().length) setIdle();
        // 已结束的那一局在结束时就已经兑现过，读档后不能再结一次账
        settled = isGameOver();
        maxValue = Math.clamp(maxValue, JustGet10Rules.VALUE_MIN, JustGet10Rules.VALUE_MAX);
        if (selection.length != SELECTION_WORDS) selection = NO_SELECTION;
        if (board.length != JustGet10Rules.CELL_COUNT) {
            board = NO_BOARD;
            score = 0;
            maxValue = JustGet10Rules.VALUE_MIN;
            selection = NO_SELECTION;
            setIdle();
            return;
        }
        for (int cell = 0; cell < board.length; cell++) {
            board[cell] = Math.clamp(board[cell], JustGet10Rules.VALUE_MIN, JustGet10Rules.VALUE_MAX);
        }
    }

    @Override
    protected BoardSnapshot createView() {
        return new BoardSnapshot(revision(), board, selection);
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
        maxValue = JustGet10Rules.maxValue(board);
        selection = NO_SELECTION;
    }

    /** 服务端：清零本局成绩、进入进行中，并重新打开「可以结账」的开关。 */
    private void startRound() {
        dealBoard();
        score = 0;
        settled = false;
        gameStatus = Status.PLAYING.ordinal();
    }

    /**
     * 把本局积分折成游戏币入余额；同一局只结一次账（{@link #settled} 一置位，结束、重新开始、换档位、拆机器都不会
     * 再给第二次）。分数没超过兑现基线时同样算结过账，只是这次给 0 枚。
     */
    @Override
    protected void cashOut() {
        if (settled) return;
        settled = true;
        long reward = (score - REDEMPTION_BASELINE) / JustGet10Rules.POINTS_PER_COIN;
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

    /** 服务端：「重新开始」按钮：结掉当前这一局再开一局。没有进行中的一局时按下也无副作用；停用时不响应。 */
    private void restartRoundOnServer() {
        if (isRemote() || !enabled) return;
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
     * 落子（下标就是按钮编号）：
     *
     * <p>
     * 未提起的格子先提起整组——把 {@code index} 所在、数字相同且四向连通的一一整组标进 {@link #selection}；不成组
     * （单格、空格、越界）就清空提起。已提起的格子则合并：整组清空、被点中的那格写入「原数字 + 1」、逐列下坠补格，
     * 然后按新盘面结算分数与「首次合出」里程碑。合并会清掉<b>所有</b>提起（盘面换了位置，留着旧高亮会指错格子）；
     * 不可合并时直接返回、状态不变。
     */
    private void clickCell(int index) {
        if (isRemote() || !enabled || !isPlaying()) return;
        int size = rules.size();
        if (index < 0 || index >= board.length) {
            selection = NO_SELECTION;
            syncAndMarkChanged();
            return;
        }
        if (!isSelected(selection, index)) {
            var group = rules.groupAt(board, index / size, index % size);
            selection = group == null ? NO_SELECTION : maskOf(group);
        } else {
            int row = index / size;
            int col = index % size;
            var group = rules.groupAt(board, row, col);
            if (group == null) return;
            var level = getLevel();
            if (level == null) return;
            var merge = rules.merge(board, group, row, col, level.getRandom(), score);
            if (merge == null) return;
            int mergedValue = merge.mergedValue();
            int gained = mergedValue * group.size();
            board = merge.board();
            int reachedValue = Math.min(JustGet10Rules.VALUE_MAX, JustGet10Rules.maxValue(board));
            if (reachedValue > maxValue) {
                for (int value = Math.max(maxValue + 1, SCORE_BONUS_MIN_VALUE); value <= reachedValue; value++) {
                    gained += SCORE_BONUS_BASE + (value - SCORE_BONUS_MIN_VALUE) * SCORE_BONUS_PER_VALUE;
                }
                maxValue = reachedValue;
            }
            score = (int) Math.min(Integer.MAX_VALUE, (long) score + gained);
            selection = NO_SELECTION;
            if (rules.isGameOver(board)) endRound();
        }
        syncAndMarkChanged();
    }

    /** 提起位图：字 = 格号 / {@link Long#SIZE}，位 = 格号 % 64；越界或没这两个字时算未提起。 */
    private static boolean isSelected(long[] selection, int cell) {
        int word = cell >>> 6;
        return word < selection.length && (selection[word] & (1L << (cell & 63))) != 0;
    }

    /** 把一整组格号打成位图。 */
    private static long[] maskOf(IntArrayList group) {
        long[] mask = new long[SELECTION_WORDS];
        for (int index = 0; index < group.size(); index++) {
            int cell = group.getInt(index);
            mask[cell >>> 6] |= 1L << (cell & 63);
        }
        return mask;
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

    /** 盘面区：外层面板当底并把盘面居中，盘面本体是 {@link #createBoard()}。 */
    private UIElement createGameArea() {
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChild(createBoard());
    }

    /** 按 {@link JustGet10Rules#SIZE} 铺格子，盘面快照挂在这一层上。 */
    private UIElement createBoard() {
        int width = CELL_SIZE * JustGet10Rules.SIZE;
        var boardRows = new UIElement().layout(style -> style.alignCenter().width(width));
        registerView(boardRows, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
        for (int row = 0; row < JustGet10Rules.SIZE; row++) {
            var cells = UIElement.row(CELL_SIZE).layout(style -> style.width(width));
            for (int col = 0; col < JustGet10Rules.SIZE; col++) {
                cells.addChild(new Cell(CELL_SIZE, row * JustGet10Rules.SIZE + col));
            }
            boardRows.addChild(cells);
        }
        return boardRows;
    }

    /**
     * 数字格：外裹定尺寸的 {@link UIElement}，里面才是按钮。这层不能省——LDLib 的
     * {@code WidgetGroup.mouseClicked} 会把父控件坐标原样转发给每个子控件，只有 {@link UIElement} 会在自己的坐标
     * 空间里再转发一次，少了它整盘按钮会同时收到同一次点击。
     */
    private final class Cell extends UIElement {

        private Cell(int cellSize, int index) {
            layout(style -> style.size(cellSize, cellSize));
            addChild(new CellButton(cellSize, index));
        }
    }

    /**
     * 按钮本体：整格就是一张数字材质，提起时换成高亮的那套；不画底图、文字或禁用态。
     *
     * <p>
     * 一律不禁用：格值每帧现读同步快照，有数字就画；能不能动由服务端在处理点击时判定，界面不会出现斜纹或
     * 「禁止操作」。
     */
    private final class CellButton extends Button {

        private final int cellSize;
        private final int index;

        private CellButton(int cellSize, int index) {
            super(cellSize, cellSize, null, null);
            this.cellSize = cellSize;
            this.index = index;
            setOnServerClick(() -> clickCell(index));
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var snapshot = view();
            int[] current = snapshot.board();
            int value = index < current.length ? current[index] : -1;
            if (value < JustGet10Rules.VALUE_MIN) return;
            int x = getPositionX();
            int y = getPositionY();
            var textures = snapshot.isSelected(index) ? NUMBER_TEXTURES_PRESSED : NUMBER_TEXTURES;
            textures[Math.min(value, JustGet10Rules.VALUE_MAX)].draw(graphics, mouseX, mouseY, x, y, cellSize, cellSize);
            if (isMouseOverElement(mouseX, mouseY)) UIDraw.hoverOverlay(graphics, x, y, cellSize, cellSize);
        }
    }
}

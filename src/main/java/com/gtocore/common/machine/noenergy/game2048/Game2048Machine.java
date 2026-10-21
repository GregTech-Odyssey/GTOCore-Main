package com.gtocore.common.machine.noenergy.game2048;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.machine.noenergy.game.AbstractRoundGameMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.RPC;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import org.lwjgl.glfw.GLFW;

/**
 * 「2048」游戏机：{@link Game2048Rules.BoardSize} 档位的推格合并玩法。
 *
 * <p>
 * 一局攒积分，盘面再也推不动时这一局结束、按档位比例兑现成游戏币；按「重新开始」提前兑现重开。余额、兑现与界面
 * 骨架都在 {@link AbstractRoundGameMachine} / {@code AbstractGameMachine} 里，这里只管自己的盘面规则与绘制。
 *
 * <p>
 * 盘面用键盘操作：W A S D 与四个方向键各对应一次移动（见 {@link BoardInput#keyPressed}），请求发到服务端由服务端改
 * 盘面，客户端按当前档位把下发来的快照画进固定大小的盘面区。档位是基类的持久化字段，换档位由基类的配置页调
 * {@code switchBoardSize}。
 */
@DataGeneratorScanned
public class Game2048Machine extends AbstractRoundGameMachine<Game2048Machine.BoardSnapshot> {

    @RegisterLanguage(cn = "棋盘大小", en = "Board size")
    private static final String SECTION_BOARD_SIZE = "gtocore.machine.game_2048.section.board_size";
    @RegisterLanguage(cn = "小 4×4", en = "Small 4×4")
    private static final String SIZE_SMALL = "gtocore.machine.game_2048.size.small";
    @RegisterLanguage(cn = "中 6×6", en = "Medium 6×6")
    private static final String SIZE_MEDIUM = "gtocore.machine.game_2048.size.medium";
    @RegisterLanguage(cn = "大 8×8", en = "Large 8×8")
    private static final String SIZE_LARGE = "gtocore.machine.game_2048.size.large";

    /** 档位按钮上的名字，下标与 {@link Game2048Rules.BoardSize} 的序数一一对应。 */
    private static final String[] SIZE_NAMES = { SIZE_SMALL, SIZE_MEDIUM, SIZE_LARGE };

    /** 格子材质原尺寸：小盘就用它。 */
    private static final int CELL_SIZE_MAX = 28;

    /**
     * 额外分：一局里 {@link #SCORE_BONUS_MIN_VALUE} 及以上的每个数字<b>首次</b>被合出时各给一次，金额随数字递增；
     * 水位就是当时盘面上的最大数字，合并只会把它顶得更高，所以直接和上一步的盘面比。
     */
    private static final int SCORE_BONUS_MIN_VALUE = 1024;
    private static final int SCORE_BONUS_BASE = 1000;
    /** 兑现基线：超过它的部分才折算游戏币，折算比例随档位变（见 {@link Game2048Rules.BoardSize#pointsPerCoin()}）。 */
    public static final int REDEMPTION_BASELINE = 2000;

    private static final int NO_DIRECTION = -1;
    /** 「没有盘面」用空数组而不是 null；首次打开界面时会立刻换成新发的一局。 */
    private static final int[] NO_BOARD = new int[0];

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
    /** 按当前档位建的规则实例；读档与换档位时重建（临时容器跟着盘面尺寸分配）。 */
    private Game2048Rules rules;

    /** 服务端权威盘面，按行优先存 {@code size * size} 格，空格为 0。 */
    @SaveToDisk
    private int[] board = NO_BOARD;

    public Game2048Machine(MetaMachineBlockEntity holder) {
        super(holder);
        rules = Game2048Rules.create(Game2048Rules.BoardSize.SMALL);
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

    // ==================== 基类钩子 ====================

    @Override
    public int redemptionBaseline() {
        return REDEMPTION_BASELINE;
    }

    @Override
    public int pointsPerCoin(int ordinal) {
        return Game2048Rules.BoardSize.ALL[ordinal].pointsPerCoin();
    }

    /** 重新发牌；清零成绩与进入进行中由基类的 {@code startRound()} 统一做。 */
    @Override
    protected void dealBoard() {
        board = rules.createBoard(random());
    }

    /**
     * 读档后的收尾：档位在基类已经校验过，这里按它重建规则；盘面尺寸对不上（旧档或换过档位）会被整成空盘，进而当成
     * 「还没开这一局」，等下次打开界面重发——那种情况下残留的积分不属于任何一局，一并清掉，免得被当成有效成绩兑现。
     */
    @Override
    protected void sanitizeRound() {
        rules = Game2048Rules.create(Game2048Rules.BoardSize.ALL[boardSize()]);
        board = rules.normalizeBoard(board);
        if (!Game2048Rules.isEmpty(board)) return;
        resetScore();
        setIdle();
    }

    @Override
    protected BoardSnapshot createView() {
        return new BoardSnapshot(revision(), board);
    }

    @Override
    protected int cellSizeMax() {
        return CELL_SIZE_MAX;
    }

    /** 图例里每个电路的固定边长：15 档 × 10px = 150，正好排进 156 宽的盘面区。 */
    private static final float LEGEND_ICON_SIZE = 10.4f;
    /** 盘面区顶部那条图例的高度：一排电压等级图标 + 底部 1px 分隔线。 */
    private static final int LEGEND_HEIGHT = 15;

    /** 盘面是同一个自绘控件按档位画格子，控件树不随档位变。 */
    @Override
    protected UIElement createBoardArea() {
        return new BoardInput();
    }

    @Override
    protected void applyBoardSize(int ordinal) {
        rules = Game2048Rules.create(Game2048Rules.BoardSize.ALL[ordinal]);
    }

    @Override
    protected boolean isValidBoardSize(int ordinal) {
        return Game2048Rules.BoardSize.of(ordinal) != null;
    }

    @Override
    protected String[] boardSizeNames() {
        return SIZE_NAMES;
    }

    @Override
    protected String boardSizeSectionTitle() {
        return SECTION_BOARD_SIZE;
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
        addScore(gained);
        if (move.gameOver()) endRound();
        syncAndMarkChanged();
    }

    /** 当前档位；存档/同步值异常时回落小盘。 */
    private Game2048Rules.BoardSize currentSize() {
        var size = Game2048Rules.BoardSize.of(boardSize());
        return size == null ? Game2048Rules.BoardSize.SMALL : size;
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

    /**
     * 盘面本体：一次画完整盘，同时是界面的键盘入口。
     *
     * <p>
     * 控件尺寸固定按最大档（{@link Game2048Rules.BoardSize#LARGE}）算，实际棋盘按当前档位算好边长后在框里居中，
     * 所以换档位只改绘制、不改布局，两端不会出现尺寸不一致。
     *
     * <p>
     * 键盘事件由 LDLib 从界面根控件逐层向下派发（{@code WidgetGroup.keyPressed}），本元素在派发链上，所以不需要
     * 焦点；收到的移动请求经 {@link RPC} 上行，服务端照旧做一次校验再改盘面。盘面值只在绘制时读同步快照，
     * 两端的控件树完全一致，通道里的条目顺序（先盘面、后请求）也因此一致。
     */
    private final class BoardInput extends UIElement {

        private final RPC<Integer> move;
        private final int frameSize;

        private BoardInput() {
            int frame = 0;
            for (var preset : Game2048Rules.BoardSize.ALL)
                frame = Math.max(frame, cellSize(preset.size()) * preset.size());
            frameSize = frame;
            layout(style -> style.size(frameSize, LEGEND_HEIGHT + frameSize));
            registerView(this, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
            move = addRPC(ByteBufCodecs.VAR_INT, (player, direction) -> moveOnServer(direction));
        }

        /** 顶部图例：全部电压等级的电路按固定尺寸横排（不随窗口或档位变），底部一条横线跟盘面分开。 */
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
            int side = current.length > 0 ? (int) Math.round(Math.sqrt(current.length)) : currentSize().size();
            int cell = frameSize / side;
            int board = cell * side;
            int left = getPositionX() + (frameSize - board) / 2;
            int legendTop = getPositionY();
            drawLegend(graphics, getPositionX(), legendTop, frameSize);
            int top = legendTop + LEGEND_HEIGHT + (frameSize - board) / 2;
            for (int row = 0; row < side; row++) {
                for (int col = 0; col < side; col++) {
                    int index = row * side + col;
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

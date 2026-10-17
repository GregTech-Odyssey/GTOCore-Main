package com.gtocore.common.machine.noenergy.justGet10;

import com.gtocore.common.machine.noenergy.game.AbstractRoundGameMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.SwitchedContent;
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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

/**
 * 「合成十」游戏机：按 {@link JustGet10Rules.BoardSize} 铺盘面的合并玩法。
 *
 * <p>
 * 一局攒积分，盘面无相邻同数字时这一局结束、按档位比例兑现成游戏币；按「重新开始」提前兑现重开。余额、兑现与界面
 * 骨架都在 {@link AbstractRoundGameMachine} / {@code AbstractGameMachine} 里，这里只管自己的盘面规则与绘制。
 *
 * <p>
 * 每个数字格是一个 {@link Cell}（外裹一层转发坐标，里面是 {@link CellButton}），盘面走界面通道，只在盘面或提起位变了
 * 的那一 tick 下发。档位是基类的持久化字段，换档位由基类的配置页调 {@code switchBoardSize}；盘面格子数随档位增减、
 * 控件树要跟着改，所以盘面放在 {@link SwitchedContent} 里由服务端定键重建。
 */
@DataGeneratorScanned
public class JustGet10Machine extends AbstractRoundGameMachine<JustGet10Machine.BoardSnapshot> {

    @RegisterLanguage(cn = "棋盘大小", en = "Board size")
    private static final String SECTION_BOARD_SIZE = "gtocore.machine.just_get_10.section.board_size";
    @RegisterLanguage(cn = "小 5×5", en = "Small 5×5")
    private static final String SIZE_SMALL = "gtocore.machine.just_get_10.size.small";
    @RegisterLanguage(cn = "中 7×7", en = "Medium 7×7")
    private static final String SIZE_MEDIUM = "gtocore.machine.just_get_10.size.medium";
    @RegisterLanguage(cn = "大 9×9", en = "Large 9×9")
    private static final String SIZE_LARGE = "gtocore.machine.just_get_10.size.large";
    @RegisterLanguage(cn = "特大 11×11", en = "Huge 11×11")
    private static final String SIZE_HUGE = "gtocore.machine.just_get_10.size.huge";

    /** 档位按钮上的名字，下标与 {@link JustGet10Rules.BoardSize} 的序数一一对应。 */
    private static final String[] SIZE_NAMES = { SIZE_SMALL, SIZE_MEDIUM, SIZE_LARGE, SIZE_HUGE };

    /** 格子材质原尺寸（小盘就用它，和数字材质一致）。 */
    private static final int CELL_SIZE_MAX = 24;

    /**
     * 额外分：一局里 {@link #SCORE_BONUS_MIN_VALUE}~{@link JustGet10Rules#VALUE_MAX} 的每个数字<b>首次</b>被合出时各给
     * 一次，金额随数字递增；水位记在 {@link #maxValue} 上。
     */
    private static final int SCORE_BONUS_MIN_VALUE = 10;
    private static final int SCORE_BONUS_BASE = 1000;
    private static final int SCORE_BONUS_PER_VALUE = 500;
    /**
     * 兑现门槛：一局积分超过它的部分才按档位比例折成游戏币，达不到就一枚不给。公开给工具提示复用，免得两处各写一个数。
     */
    public static final int REDEMPTION_BASELINE = 1000;

    /** 提起位图：一个 {@code long} 装 {@link Long#SIZE} 格，最大档 121 格两个字够用。 */
    private static final int SELECTION_WORDS = (JustGet10Rules.BoardSize.HUGE.cellCount() + Long.SIZE - 1) / Long.SIZE;
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

    /** 按当前档位建的规则实例；读档与换档位时重建（临时容器跟着盘面尺寸分配）。 */
    private JustGet10Rules rules;

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
        rules = JustGet10Rules.create(JustGet10Rules.BoardSize.SMALL);
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

    // ==================== 基类钩子 ====================

    @Override
    public int redemptionBaseline() {
        return REDEMPTION_BASELINE;
    }

    @Override
    public int pointsPerCoin(int ordinal) {
        return JustGet10Rules.BoardSize.ALL[ordinal].pointsPerCoin();
    }

    /** 重新发牌；清零成绩与进入进行中由基类的 {@code startRound()} 统一做。 */
    @Override
    protected void dealBoard() {
        board = rules.createBoard(random());
        maxValue = JustGet10Rules.maxValue(board);
        selection = NO_SELECTION;
    }

    /**
     * 读档后的收尾：档位在基类已经校验过，这里按它重建规则；盘面尺寸对不上（旧档或换过档位）本局作废，等下次打开
     * 界面重发，其余数值夹回合法范围。
     */
    @Override
    protected void sanitizeRound() {
        var size = JustGet10Rules.BoardSize.ALL[boardSize()];
        rules = JustGet10Rules.create(size);
        maxValue = Math.clamp(maxValue, JustGet10Rules.VALUE_MIN, JustGet10Rules.VALUE_MAX);
        if (selection.length != SELECTION_WORDS) selection = NO_SELECTION;
        if (board.length != size.cellCount()) {
            board = NO_BOARD;
            resetScore();
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

    @Override
    protected int cellSizeMax() {
        return CELL_SIZE_MAX;
    }

    /**
     * 盘面区：格子数随档位增减、控件树要跟着改，所以盘面放在 {@link SwitchedContent} 里，由服务端按 {@link #boardSize()}
     * 这个键决定何时重建，两端才能同序执行。
     */
    @Override
    protected UIElement createBoardArea() {
        return new SwitchedContent(this::boardSize, (key, remote) -> createBoard(key));
    }

    @Override
    protected void applyBoardSize(int ordinal) {
        rules = JustGet10Rules.create(JustGet10Rules.BoardSize.ALL[ordinal]);
    }

    @Override
    protected boolean isValidBoardSize(int ordinal) {
        return JustGet10Rules.BoardSize.of(ordinal) != null;
    }

    @Override
    protected String[] boardSizeNames() {
        return SIZE_NAMES;
    }

    @Override
    protected String boardSizeSectionTitle() {
        return SECTION_BOARD_SIZE;
    }

    /** 停用时不响应「重新开始」；点格子那条在 {@link #clickCell} 里自己判。 */
    @Override
    protected void restartRoundOnServer() {
        if (!enabled) return;
        super.restartRoundOnServer();
    }

    // ==================== 服务端动作 ====================

    /**
     * 落子（下标就是按钮编号）：
     *
     * <p>
     * 未提起的格子先提起整组——把 {@code index} 所在、数字相同且四向连通的一整组标进 {@link #selection}；不成组
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
            var merge = rules.merge(board, group, row, col, level.getRandom(), score());
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
            addScore(gained);
            selection = NO_SELECTION;
            if (rules.isGameOver(board)) endRound();
        }
        syncAndMarkChanged();
    }

    /**
     * 按档位铺 {@code size × size} 个格子：边长随档位缩，保证 11×11 也放得进页面；盘面快照挂在这一层上。
     */
    private @Nullable UIElement createBoard(int sizeKey) {
        var size = JustGet10Rules.BoardSize.of(sizeKey);
        if (size == null) return null;
        int cell = cellSize(size.size());
        int width = cell * size.size();
        var boardRows = new UIElement().layout(style -> style.alignCenter().width(width));
        registerView(boardRows, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
        for (int row = 0; row < size.size(); row++) {
            var cells = UIElement.row(cell).layout(style -> style.width(width));
            for (int col = 0; col < size.size(); col++) {
                cells.addChild(new Cell(cell, row * size.size() + col));
            }
            boardRows.addChild(cells);
        }
        return boardRows;
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

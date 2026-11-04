package com.gtocore.common.machine.noenergy.game.sudoku;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
import com.gtocore.common.machine.noenergy.game.GameBoardCatalog;
import com.gtocore.common.machine.noenergy.game.LiveDurationLine;
import com.gtocore.common.machine.noenergy.game.milestone.SudokuMilestones;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.SwitchedContent;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * 数独游戏机：9×9 盘面，六档难度，每局可选一条变体规则；完成按档位发游戏币，并把用时记入「难度 × 变体」榜。
 *
 * <p>
 * 盘面是一整块自绘控件，由下到上：纸面底色 → 变体底色（彩色分区、对角线/中心点/窗口浅绿）→ 数字橙色高亮 →
 * 选中格天青色边框 → 格线（细线在下、粗线在上）→ 变体层（锯齿边界、连续标记、杀手笼边界）→ 数字 →
 * 杀手笼角标。提示数字深蓝近黑，玩家数字亮粉，候选数灰色小字；高亮只加橙色底色，不改变数字颜色。
 *
 * <p>
 * 盘面下是 5×2 的十个按钮：1~9 与「标记」。普通模式点数字填写、再点同一数字清除；标记模式点数字只切换该空格的候选数。
 * 落子全部由服务端校验并下发快照；判定同时检查全异单元、连续标记与杀手笼和。
 */
@DataGeneratorScanned
public class SudokuMachine extends AbstractGameMachine<SudokuMachine.BoardSnapshot> {

    @RegisterLanguage(cn = "入门", en = "Beginner")
    private static final Component DIFFICULTY_BEGINNER = Component.translatable("gtocore.machine.sudoku.difficulty.beginner");
    @RegisterLanguage(cn = "简单", en = "Easy")
    private static final Component DIFFICULTY_EASY = Component.translatable("gtocore.machine.sudoku.difficulty.easy");
    @RegisterLanguage(cn = "普通", en = "Normal")
    private static final Component DIFFICULTY_NORMAL = Component.translatable("gtocore.machine.sudoku.difficulty.normal");
    @RegisterLanguage(cn = "困难", en = "Hard")
    private static final Component DIFFICULTY_HARD = Component.translatable("gtocore.machine.sudoku.difficulty.hard");
    @RegisterLanguage(cn = "专家", en = "Expert")
    private static final Component DIFFICULTY_EXPERT = Component.translatable("gtocore.machine.sudoku.difficulty.expert");
    @RegisterLanguage(cn = "大师", en = "Master")
    private static final Component DIFFICULTY_MASTER = Component.translatable("gtocore.machine.sudoku.difficulty.master");
    /** 难度名字，下标与 {@link SudokuRules.Difficulty} 的序数一一对应。 */
    public static final Component[] DIFFICULTY_NAMES = { DIFFICULTY_BEGINNER, DIFFICULTY_EASY, DIFFICULTY_NORMAL,
            DIFFICULTY_HARD, DIFFICULTY_EXPERT, DIFFICULTY_MASTER };

    @RegisterLanguage(cn = "标准", en = "Standard")
    private static final Component VARIANT_STANDARD = Component.translatable("gtocore.machine.sudoku.variant.standard");
    @RegisterLanguage(cn = "对角线 X", en = "Diagonal X")
    private static final Component VARIANT_DIAGONAL_X = Component.translatable("gtocore.machine.sudoku.variant.diagonal_x");
    @RegisterLanguage(cn = "中心点", en = "Center dots")
    private static final Component VARIANT_CENTER_DOTS = Component.translatable("gtocore.machine.sudoku.variant.center_dots");
    @RegisterLanguage(cn = "窗口", en = "Window")
    private static final Component VARIANT_WINDOW = Component.translatable("gtocore.machine.sudoku.variant.window");
    @RegisterLanguage(cn = "彩色", en = "Color")
    private static final Component VARIANT_COLOR = Component.translatable("gtocore.machine.sudoku.variant.color");
    @RegisterLanguage(cn = "锯齿", en = "Jigsaw")
    private static final Component VARIANT_JIGSAW = Component.translatable("gtocore.machine.sudoku.variant.jigsaw");
    @RegisterLanguage(cn = "连续", en = "Consecutive")
    private static final Component VARIANT_CONSECUTIVE = Component.translatable("gtocore.machine.sudoku.variant.consecutive");
    @RegisterLanguage(cn = "杀手", en = "Killer")
    private static final Component VARIANT_KILLER = Component.translatable("gtocore.machine.sudoku.variant.killer");
    /** 变体名字，下标与 {@link SudokuVariant} 的序数一一对应。 */
    public static final Component[] VARIANT_NAMES = { VARIANT_STANDARD, VARIANT_DIAGONAL_X, VARIANT_CENTER_DOTS,
            VARIANT_WINDOW, VARIANT_COLOR, VARIANT_JIGSAW, VARIANT_CONSECUTIVE, VARIANT_KILLER };

    @RegisterLanguage(cn = "难度", en = "Difficulty")
    private static final Component SECTION_DIFFICULTY = Component.translatable("gtocore.machine.sudoku.section.difficulty");
    @RegisterLanguage(cn = "变体", en = "Variant")
    public static final Component PAGE_VARIANT = Component.translatable("gtocore.machine.sudoku.page.variant");
    @RegisterLanguage(cn = "为这一局叠加一条附加规则", en = "Stack one extra rule on this round")
    private static final Component PAGE_VARIANT_TOOLTIP = Component.translatable("gtocore.machine.sudoku.page.variant.tooltip");
    @RegisterLanguage(cn = "变体", en = "Variant")
    private static final Component LINE_VARIANT = Component.translatable("gtocore.machine.sudoku.line.variant");
    @RegisterLanguage(cn = "难度", en = "Difficulty")
    private static final Component LINE_DIFFICULTY = Component.translatable("gtocore.machine.sudoku.line.difficulty");
    @RegisterLanguage(cn = "用时", en = "Time")
    private static final Component LINE_TIME = Component.translatable("gtocore.machine.sudoku.line.time");
    @RegisterLanguage(cn = "标记", en = "Mark")
    private static final Component BUTTON_MARK = Component.translatable("gtocore.machine.sudoku.button.mark");
    @RegisterLanguage(cn = "%s · 完成奖励 %s 游戏币", en = "%s · Clear reward %s Game Coins")
    private static final String TEXT_DIFFICULTY = "gtocore.machine.sudoku.text.difficulty";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final Component BUTTON_RESTART = Component.translatable("gtocore.machine.sudoku.button.restart");
    @RegisterLanguage(cn = "这一局已经完成了", en = "This puzzle is already solved")
    private static final Component REASON_SOLVED = Component.translatable("gtocore.machine.sudoku.reason.solved");

    private static final MilestoneColumn[] LEADERBOARD_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 64),
            MilestoneColumn.center(COLUMN_DURATION, 54) };

    /** 「全部榜单」页里的数独榜（一级维度难度、二级维度变体）：列与行和本机排行榜页完全一致。 */
    public static GameBoardCatalog.Board leaderboardBoard() {
        return GameBoardCatalog.board(LEADERBOARD_COLUMNS, server -> SudokuMilestones.get(server).modCount(),
                (server, difficulty, variant) -> buildLeaderboardRows(SudokuMilestones.get(server), server,
                        difficulty, variant));
    }

    private static final int CELL_SIZE = 15;
    /** 线槽宽度；粗线填满线槽，细线只填中间 1 像素。 */
    private static final int THICK_GRID = 3;
    private static final int CELL_PITCH = CELL_SIZE + THICK_GRID;
    private static final int BOARD_PIXELS = CELL_SIZE * SudokuRules.SIZE + THICK_GRID * (SudokuRules.SIZE + 1);
    /** 数字键的宽度：一行五个加四个间隙略窄于内容区，整体居中。 */
    private static final int KEY_WIDTH = 30;
    private static final int NOTE_SLOT = CELL_SIZE / SudokuRules.BOX;
    private static final float NOTE_SCALE = 0.5F;

    private static final int COLOR_PAPER = 0xEFFBF7E8;
    private static final int COLOR_GRID = 0xFFB4AA92;
    private static final int COLOR_BOX = 0xFF4C4738;
    /** 提示数字：深蓝近黑。 */
    private static final int COLOR_GIVEN = 0xFF141C4A;
    /** 玩家填写：亮粉。 */
    private static final int COLOR_ENTERED = 0xFFFF4FC3;
    /** 候选数：灰色小字。 */
    private static final int COLOR_NOTE = 0xFF9A9A9A;
    /** 选中格子的天青色边框。 */
    private static final int COLOR_SELECTED = 0xFF4FD8FF;
    /** 与选中格同数字/同候选的橙色底色高亮。 */
    private static final int COLOR_MATCH_FILL = 0x55FF9A2E;
    private static final int COLOR_NOTE_MATCH_FILL = 0x66FF9A2E;
    /** 对角线、中心点与窗口变体的区域浅绿底色。 */
    private static final int COLOR_REGION_TINT = 0x3322C55E;
    /** 连续变体的亮紫色短线。 */
    private static final int COLOR_CONSECUTIVE = 0xFFB44DFF;
    /** 杀手笼边界的正红色。 */
    private static final int COLOR_CAGE_BOUNDARY = 0xFFFF0000;
    /** 杀手笼角标的颜色。 */
    private static final int COLOR_CAGE_TOTAL = 0xFFB85C00;
    /** 某个数字已填满 9 个时，按键数字显示的灰色。 */
    private static final int COLOR_DIGIT_DONE = 0xFF8C8C8C;
    /** 某个数字超过 9 个时，按键数字显示的错误红色。 */
    private static final int COLOR_DIGIT_ERROR = 0xFFE53935;
    /** 彩色变体 9 个分区的半透明底色，按分区编号取色。 */
    private static final int[] COLOR_REGION_FILLS = {
            0x33FF6B6B, 0x33FFD166, 0x3376D7B8, 0x334D9FFF, 0x33B388FF,
            0x33FF8FD8, 0x33FFA94D, 0x338CE99A, 0x33D6C6FF };

    /** 没有盘面时给客户端的全零占位；机器自己的数组不会被它共享。 */
    private static final int[] EMPTY_CELLS = new int[SudokuRules.CELLS];

    private enum Status {
        IDLE,
        PLAYING,
        SOLVED
    }

    /** 当前难度（{@link SudokuRules.Difficulty} 的序数）。 */
    @SaveToDisk(defaultValue = "0")
    private int difficulty = SudokuRules.Difficulty.BEGINNER.ordinal();
    /** 当前附加变体（{@link SudokuVariant} 的序数）；0 是标准规则。 */
    @SaveToDisk(defaultValue = "0")
    private int variant = SudokuVariant.STANDARD.ordinal();
    /** 提示盘：0 为空格，非 0 的格子玩家不能修改。 */
    @SaveToDisk
    private int[] givens = new int[SudokuRules.CELLS];
    /** 玩家填写的数字，0 为未填。 */
    @SaveToDisk
    private int[] values = new int[SudokuRules.CELLS];
    /** 每格的候选数位掩码（第 n 位表示数字 n + 1）。 */
    @SaveToDisk
    private int[] notes = new int[SudokuRules.CELLS];
    /** 锯齿变体这一局的区域编号（0~8）；其它变体不用。 */
    @SaveToDisk
    private int[] jigsawMap = new int[SudokuRules.CELLS];
    /** 杀手变体的每格笼编号（0 表示无笼）；其它变体不用。 */
    @SaveToDisk
    private int[] cageOf = new int[SudokuRules.CELLS];
    /** 杀手变体每个笼的目标和（下标 = 笼编号 - 1）；其它变体为空。 */
    @SaveToDisk
    private int[] cageTotals = new int[0];
    /** 连续变体每格的标记方位掩码；其它变体不用。 */
    @SaveToDisk
    private int[] marks = new int[SudokuRules.CELLS];
    /** 当前选中的格子，-1 表示没有选中。 */
    @SaveToDisk(defaultValue = "-1")
    private int selected = -1;
    /** 标记模式：点数字只改候选数。 */
    @SaveToDisk(defaultValue = "false")
    private boolean notesMode;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient(scheduleUpdate = true)
    private long roundStart;
    @SaveToDisk(defaultValue = "0")
    private long lastElapsed;
    @SaveToDisk(defaultValue = "0")
    private int status = Status.IDLE.ordinal();

    public SudokuMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /**
     * 通道下发的盘面快照：难度、变体、提示盘、玩家填写、候选数、选中格、标志位、用时、阶段，以及锯齿/杀手/连续数据。
     * 相等性只看版本号，省掉逐数组比较。
     */
    public record BoardSnapshot(int revision, int difficulty, int variant, int[] givens, int[] values, int[] notes,
                                int selected, int flags, long elapsedTicks, int state, int[] jigsawMap, int[] cageOf,
                                int[] cageTotals, int[] marks) {

        private static final int FLAG_SOLVED = 1;
        private static final int FLAG_NOTES_MODE = 1 << 1;
        private static final int[] EMPTY_TOTALS = new int[0];
        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, 0, 0, EMPTY_CELLS, EMPTY_CELLS, EMPTY_CELLS,
                -1, 0, 0L, Status.IDLE.ordinal(), EMPTY_CELLS, EMPTY_CELLS, EMPTY_TOTALS, EMPTY_CELLS);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT, BoardSnapshot::difficulty,
                ByteBufCodecs.VAR_INT, BoardSnapshot::variant,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::givens,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::values,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::notes,
                ByteBufCodecs.VAR_INT, BoardSnapshot::selected,
                ByteBufCodecs.VAR_INT, BoardSnapshot::flags,
                ByteBufCodecs.VAR_LONG, BoardSnapshot::elapsedTicks,
                ByteBufCodecs.VAR_INT, BoardSnapshot::state,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::jigsawMap,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::cageOf,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::cageTotals,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::marks,
                BoardSnapshot::new);

        boolean solved() {
            return (flags & FLAG_SOLVED) != 0;
        }

        boolean notesMode() {
            return (flags & FLAG_NOTES_MODE) != 0;
        }

        /** 第 {@code cell} 格最终显示的数字：提示优先，其次玩家填写。 */
        int valueAt(int cell) {
            return SudokuRules.displayedValue(givens, values, cell);
        }

        /**
         * 要高亮的数字位掩码：选中格已有数字时取该数字；选中空格（即使有候选数）返回 0。
         */
        int focusMask() {
            if (selected < 0 || selected >= SudokuRules.CELLS) return 0;
            int value = valueAt(selected);
            return value != 0 ? 1 << (value - SudokuRules.VALUE_MIN) : 0;
        }

        /** 盘面上已经显示了多少个该数字；提示数与玩家填写的都算。 */
        int digitCount(int digit) {
            if (digit < SudokuRules.VALUE_MIN || digit > SudokuRules.VALUE_MAX) return 0;
            int count = 0;
            for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
                if (valueAt(cell) == digit) count++;
            }
            return count;
        }

        /** 当前难度与变体组合的奖励；网络来的序数一律夹回合法范围。 */
        long reward() {
            var all = SudokuRules.Difficulty.ALL;
            var variants = SudokuVariant.ALL;
            return SudokuRules.reward(all[Math.clamp(difficulty, 0, all.length - 1)],
                    variants[Math.clamp(variant, 0, variants.length - 1)]);
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

    /** 读档收尾：状态夹回合法值；坏盘按没开局处理，等下次打开界面重开。仅服务端调用。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    private void sanitizeState() {
        difficulty = Math.clamp(difficulty, 0, SudokuRules.Difficulty.ALL.length - 1);
        variant = Math.clamp(variant, 0, SudokuVariant.ALL.length - 1);
        jigsawMap = normalizeCells(jigsawMap);
        cageOf = normalizeCells(cageOf);
        marks = normalizeCells(marks);
        if (cageTotals == null) cageTotals = new int[0];
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            marks[cell] &= SudokuConstraints.ALL_DIRECTIONS;
        }
        // 数组缺失或长度不对说明存档写坏：归一化成空盘后整局作废。
        boolean boardMalformed = !isCellArray(givens) || !isCellArray(values) || !isCellArray(notes);
        givens = normalizeCells(givens);
        values = normalizeCells(values);
        notes = normalizeCells(notes);
        if (boardMalformed) {
            resetBoard();
            return;
        }
        int clues = 0;
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            if (givens[cell] < SudokuRules.VALUE_MIN || givens[cell] > SudokuRules.VALUE_MAX) givens[cell] = 0;
            if (values[cell] < SudokuRules.VALUE_MIN || values[cell] > SudokuRules.VALUE_MAX) values[cell] = 0;
            if (givens[cell] != 0) {
                values[cell] = 0;
                notes[cell] = 0;
                clues++;
            } else if (values[cell] != 0) {
                notes[cell] = 0;
            }
            notes[cell] &= SudokuRules.ALL_VALUES;
        }
        selected = selected >= 0 && selected < SudokuRules.CELLS ? selected : -1;
        if (status < 0 || status >= Status.values().length) status = Status.IDLE.ordinal();
        // 杀手本就没有提示数字，不能把提示数 0 当成坏档；有效性由笼数据决定。
        boolean emptyPuzzle = clues == 0 && currentVariant() != SudokuVariant.KILLER;
        if (status != Status.IDLE.ordinal() && (emptyPuzzle || !variantDataValid())) {
            resetBoard();
            return;
        }
        if (status == Status.PLAYING.ordinal() &&
                (roundStart <= 0L || roundStart > gameTime())) {
            roundStart = gameTime();
        }
        if (status == Status.SOLVED.ordinal()) lastElapsed = Math.max(1L, lastElapsed);
    }

    /** 本局锯齿/杀手附加数据是否合法；不合法时由调用方作废这一局。 */
    private boolean variantDataValid() {
        var current = currentVariant();
        if (current == SudokuVariant.JIGSAW) {
            return JigsawLayout.isValid(jigsawMap);
        } else if (current == SudokuVariant.KILLER) {
            if (cageTotals.length == 0) return false;
            int[] counts = new int[cageTotals.length + 1];
            for (int cage : cageOf) {
                if (cage < 1 || cage > cageTotals.length) return false;
                counts[cage]++;
            }
            for (int cage = 1; cage <= cageTotals.length; cage++) {
                if (counts[cage] < 2) return false;
                if (cageTotals[cage - 1] < SudokuRules.VALUE_MIN ||
                        cageTotals[cage - 1] > SudokuRules.MAX_CAGE_TOTAL) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int[] normalizeCells(int[] array) {
        return array != null && array.length == SudokuRules.CELLS ? array : new int[SudokuRules.CELLS];
    }

    private static boolean isCellArray(int[] array) {
        return array != null && array.length == SudokuRules.CELLS;
    }

    private void resetBoard() {
        Arrays.fill(givens, 0);
        Arrays.fill(values, 0);
        Arrays.fill(notes, 0);
        Arrays.fill(jigsawMap, 0);
        Arrays.fill(cageOf, 0);
        Arrays.fill(marks, 0);
        cageTotals = new int[0];
        selected = -1;
        roundStart = 0L;
        lastElapsed = 0L;
        notesMode = false;
        status = Status.IDLE.ordinal();
    }

    @Override
    protected BoardSnapshot createView() {
        int flags = (status == Status.SOLVED.ordinal() ? BoardSnapshot.FLAG_SOLVED : 0) |
                (notesMode ? BoardSnapshot.FLAG_NOTES_MODE : 0);
        return new BoardSnapshot(revision(), difficulty, variant, givens, values, notes, selected, flags,
                elapsedTicks(), status, jigsawMap, cageOf, cageTotals, marks);
    }

    private SudokuRules.Difficulty currentDifficulty() {
        var difficulty = SudokuRules.Difficulty.of(this.difficulty);
        return difficulty == null ? SudokuRules.Difficulty.BEGINNER : difficulty;
    }

    /** 数独独立的里程碑存储；客户端或还没有世界时为 {@code null}。 */
    private @Nullable SudokuMilestones sudokuMilestones() {
        return getLevel() instanceof ServerLevel level ? SudokuMilestones.get(level) : null;
    }

    /** 当前附加变体；越界序数按标准规则处理。 */
    private SudokuVariant currentVariant() {
        var variant = SudokuVariant.of(this.variant);
        return variant == null ? SudokuVariant.STANDARD : variant;
    }

    /** 当前这一局的完整约束（含本局锯齿区域、笼与标记）。 */
    private SudokuConstraints currentConstraints() {
        return SudokuConstraints.of(currentVariant(), jigsawMap, cageOf, cageTotals, marks);
    }

    private boolean isPlaying() {
        return status == Status.PLAYING.ordinal();
    }

    private long elapsedTicks() {
        if (status == Status.SOLVED.ordinal()) return lastElapsed;
        if (status != Status.PLAYING.ordinal() || roundStart <= 0L) return 0L;
        return Math.max(0L, gameTime() - roundStart);
    }

    /** 状态栏计时行：进行中按本地世界刻现算，已完成用快照里的最终用时。 */
    private long displayElapsedTicks() {
        var snapshot = view();
        if (snapshot.solved()) return snapshot.elapsedTicks();
        if (snapshot.state() != Status.PLAYING.ordinal() || roundStart <= 0L) return 0L;
        return Math.max(0L, gameTime() - roundStart);
    }

    /** 首次打开界面时把没开的一局开起来。 */
    private void ensureStarted() {
        if (status == Status.IDLE.ordinal()) {
            startRound(null);
            // 开局改了盘面与起点，必须顶版本号，避免复用上一份缓存快照。
            syncAndMarkChanged();
        }
    }

    private void startRound(@Nullable ServerPlayer starter) {
        var puzzle = SudokuRules.createPuzzle(random(), currentDifficulty(), currentVariant());
        givens = puzzle.givens();
        jigsawMap = puzzle.jigsawMap();
        cageOf = puzzle.cageOf();
        cageTotals = puzzle.cageTotals();
        marks = puzzle.marks();
        Arrays.fill(values, 0);
        Arrays.fill(notes, 0);
        selected = -1;
        notesMode = false;
        lastElapsed = 0L;
        roundStart = gameTime();
        status = Status.PLAYING.ordinal();
        beginSession(starter);
        // 开局时同步一次 roundStart，客户端计时行才能在本地读秒。
        requestSync();
    }

    /** 完成一局：按「难度基础币值 × 变体倍率」发奖励；单人局再记一次「难度 × 变体」的最快用时。 */
    private void finishRound() {
        var difficulty = currentDifficulty();
        var playedVariant = currentVariant();
        status = Status.SOLVED.ordinal();
        lastElapsed = Math.max(1L, gameTime() - roundStart);
        addBalance(SudokuRules.reward(difficulty, playedVariant));
        if (!sessionCounts()) return;
        var store = sudokuMilestones();
        if (store != null) {
            // 成绩按「难度 × 变体」记录，每个组合各一条。
            store.record(sessionStarter(), difficulty.ordinal(), playedVariant.ordinal(), lastElapsed, gameTime());
        }
    }

    // ==================== 服务端动作 ====================

    private void selectCell(int cell, @Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        if (cell < 0 || cell >= SudokuRules.CELLS) return;
        selected = cell;
        syncAndMarkChanged();
    }

    /** 数字键：普通模式填写/清除，标记模式切换候选数（已填格不可标记）。 */
    private void enterDigit(int digit, @Nullable ServerPlayer player) {
        if (isRemote() || !isPlaying() || !guard(player)) return;
        if (digit < SudokuRules.VALUE_MIN || digit > SudokuRules.VALUE_MAX) return;
        if (selected < 0 || selected >= SudokuRules.CELLS || givens[selected] != 0) return;
        int bit = 1 << (digit - SudokuRules.VALUE_MIN);
        if (notesMode) {
            if (values[selected] != 0) return;
            notes[selected] ^= bit;
            syncAndMarkChanged();
            return;
        }
        values[selected] = values[selected] == digit ? 0 : digit;
        notes[selected] = 0;
        if (SudokuSolver.isComplete(currentConstraints(), givens, values)) finishRound();
        syncAndMarkChanged();
    }

    private void toggleMark(@Nullable ServerPlayer player) {
        if (isRemote() || !isPlaying() || !guard(player)) return;
        notesMode = !notesMode;
        syncAndMarkChanged();
    }

    private void restartRound(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        startRound(player);
        syncAndMarkChanged();
    }

    /** 换难度并立即开新局。 */
    private void selectDifficulty(int ordinal, @Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        var difficulty = SudokuRules.Difficulty.of(ordinal);
        if (difficulty == null || difficulty.ordinal() == this.difficulty) return;
        this.difficulty = difficulty.ordinal();
        startRound(player);
        syncAndMarkChanged();
    }

    /** 换变体并立即开新局；一次只能选一条。 */
    private void selectVariant(int ordinal, @Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        var variant = SudokuVariant.of(ordinal);
        if (variant == null || variant.ordinal() == this.variant) return;
        this.variant = variant.ordinal();
        startRound(player);
        syncAndMarkChanged();
    }

    // ==================== 界面 ====================

    /** 主页 = 盘面 + 十个按键 + 状态面板；盘面必须先建，快照同步项要排在按键之前，两端条目编号才对得上。 */
    @Override
    public Widget createUIWidget() {
        if (!isRemote()) ensureStarted();
        var page = Form.page();
        page.addChildren(createGameArea(), createKeypad(), createStatusPanel());
        return page;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new DifficultyPage());
        sideTabs.attachSubTab(new VariantPage());
        sideTabs.attachSubTab(new LeaderboardPage());
    }

    private UIElement createGameArea() {
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChild(new BoardView());
    }

    /** 9×9 盘面下 5×2 的按键：第一行 1~5，第二行 6~9 与标记。 */
    private UIElement createKeypad() {
        var keypad = panel();
        keypad.addChild(keyRow(SudokuRules.VALUE_MIN, SudokuRules.VALUE_MIN + 4));
        var second = keyRow(SudokuRules.VALUE_MIN + 5, SudokuRules.VALUE_MAX);
        second.addChild(createMarkButton());
        keypad.addChild(second);
        return keypad;
    }

    private UIElement keyRow(int from, int to) {
        var row = UIElement.row(Button.HEIGHT).layout(style -> style.justifyContent(AlignContent.CENTER).gapAll(2));
        for (int digit = from; digit <= to; digit++) {
            row.addChild(createDigitButton(digit));
        }
        return row;
    }

    private Button createDigitButton(int digit) {
        var button = new DigitButton(KEY_WIDTH, Component.literal(Integer.toString(digit)),
                () -> view().digitCount(digit));
        button.clientDisabled(() -> view().solved(), REASON_SOLVED);
        button.setOnServerClick(() -> enterDigit(digit, playerOf(button)));
        return button;
    }

    /** 数字键：该数字填满 9 个变灰、超过 9 个变红；按键仍可用。 */
    private static final class DigitButton extends Button {

        private final IntSupplier count;

        private DigitButton(int width, Component text, IntSupplier count) {
            super(width, Button.HEIGHT, text, null);
            this.count = count;
        }

        @Override
        protected int getTextColor(boolean disabled, boolean hovered) {
            int amount = count.getAsInt();
            if (amount > SudokuRules.VALUE_MAX) return COLOR_DIGIT_ERROR;
            if (amount == SudokuRules.VALUE_MAX) return COLOR_DIGIT_DONE;
            return super.getTextColor(disabled, hovered);
        }
    }

    private Button createMarkButton() {
        var button = Button.text(KEY_WIDTH, Component.literal("✏️"));
        button.setHoverTooltips(BUTTON_MARK);
        button.setSelected(() -> view().notesMode());
        button.clientDisabled(() -> view().solved(), REASON_SOLVED);
        button.setOnServerClick(() -> toggleMark(playerOf(button)));
        return button;
    }

    private StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance()));
        status.addLine(LINE_DIFFICULTY, () -> difficultyName(view().difficulty()));
        status.addLine(LINE_VARIANT, () -> variantName(view().variant()));
        status.addChild(new LiveDurationLine(LINE_TIME, this::displayElapsedTicks));
        var restart = Button.text(77, BUTTON_RESTART);
        restart.setOnServerClick(() -> restartRound(playerOf(restart)));
        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, createWithdrawButton(77)));
        return status;
    }

    /** 难度名字的翻译键；越界按入门处理。 */
    private static Component difficultyName(int ordinal) {
        return DIFFICULTY_NAMES[Math.clamp(ordinal, 0, DIFFICULTY_NAMES.length - 1)];
    }

    /** 变体名字的翻译键；越界按标准处理。 */
    private static Component variantName(int ordinal) {
        return VARIANT_NAMES[Math.clamp(ordinal, 0, VARIANT_NAMES.length - 1)];
    }

    /** 变体奖励倍率的显示文本：100 → "1"，110 → "1.1"，200 → "2"；倍率都按 10% 的整数倍配置。 */
    private static String multiplierText(SudokuVariant variant) {
        int percent = variant.rewardPercent();
        int whole = percent / 100;
        int tenth = percent % 100 / 10;
        return tenth == 0 ? Integer.toString(whole) : whole + "." + tenth;
    }

    /** 盘面控件：整块自绘并接收格子点击。 */
    private final class BoardView extends UIElement {

        private final RPC<Integer> click;
        /** 区域底色去重：对角线两条线共享中心格，避免同一格叠两层底色。 */
        private final boolean[] tinted = new boolean[SudokuRules.CELLS];
        /** 杀手笼框的像素段缓存：笼表不变时不需要每帧重新算轮廓。 */
        private int[] cageRunsSource;
        private int[][] cageRuns;

        private BoardView() {
            layout(style -> style.size(BOARD_PIXELS, BOARD_PIXELS));
            registerView(this, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
            click = addRPC(ByteBufCodecs.VAR_INT,
                    (player, cell) -> selectCell(cell,
                            player instanceof ServerPlayer serverPlayer ? serverPlayer : null));
        }

        /** 第 {@code column} 格的左边缘（跳过最左边的线槽）。 */
        private int cellX(int column) {
            return getPositionX() + THICK_GRID + column * CELL_PITCH;
        }

        /** 第 {@code row} 格的上边缘（跳过最上面的线槽）。 */
        private int cellY(int row) {
            return getPositionY() + THICK_GRID + row * CELL_PITCH;
        }

        /** 鼠标下的格子下标；落在格线槽里或盘面外为 -1。 */
        private int cellAt(double mouseX, double mouseY) {
            if (!isMouseOverElement(mouseX, mouseY)) return -1;
            int localX = (int) (mouseX - getPositionX()) - THICK_GRID;
            int localY = (int) (mouseY - getPositionY()) - THICK_GRID;
            if (localX < 0 || localY < 0) return -1;
            int column = localX / CELL_PITCH;
            int row = localY / CELL_PITCH;
            if (column >= SudokuRules.SIZE || row >= SudokuRules.SIZE) return -1;
            if (localX % CELL_PITCH >= CELL_SIZE || localY % CELL_PITCH >= CELL_SIZE) return -1;
            return row * SudokuRules.SIZE + column;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int cell = cellAt(mouseX, mouseY);
            if (cell < 0 || button != 0) return super.mouseClicked(mouseX, mouseY, button);
            click.send(cell);
            playButtonClickSound();
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            drawPaperLayer(graphics);
            drawVariantBackgroundLayer(graphics);
            drawHighlightLayer(graphics);
            drawSelectionLayer(graphics);
            drawGridLayer(graphics);
            drawVariantLayer(graphics);
            drawDigitLayer(graphics);
            drawVariantTopLayer(graphics);
            int hovered = cellAt(mouseX, mouseY);
            if (hovered >= 0 && hovered != view().selected()) {
                UIDraw.hoverOverlay(graphics, cellX(hovered % SudokuRules.SIZE), cellY(hovered / SudokuRules.SIZE),
                        CELL_SIZE, CELL_SIZE);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawPaperLayer(GuiGraphics graphics) {
            int x0 = getPositionX();
            int y0 = getPositionY();
            UITheme.PANEL.draw(graphics, 0, 0, x0, y0, BOARD_PIXELS, BOARD_PIXELS);
            graphics.fill(x0, y0, x0 + BOARD_PIXELS, y0 + BOARD_PIXELS, COLOR_PAPER);
        }

        /** 变体底色层：彩色按分区取色，对角线/中心点/窗口用浅绿；统一外扩 1 像素。 */
        @OnlyIn(Dist.CLIENT)
        private void drawVariantBackgroundLayer(GuiGraphics graphics) {
            var variant = SudokuVariant.of(view().variant());
            if (variant == null) return;
            if (variant == SudokuVariant.COLOR) {
                int[] colorMap = variant.regionMap();
                if (colorMap == null || colorMap.length != SudokuRules.CELLS) return;
                for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
                    int region = colorMap[cell];
                    if (region < 0 || region >= COLOR_REGION_FILLS.length) continue;
                    overlayCell(graphics, cell, COLOR_REGION_FILLS[region]);
                }
                return;
            }
            switch (variant) {
                case DIAGONAL_X, CENTER_DOTS, WINDOW -> tintUnits(graphics, variant.extraUnits());
                default -> {}
            }
        }

        /** 逐格铺浅绿底色；同一格只铺一次，避免两条对角线在中心格叠色。 */
        @OnlyIn(Dist.CLIENT)
        private void tintUnits(GuiGraphics graphics, int[][] units) {
            Arrays.fill(tinted, false);
            for (int[] unit : units) {
                for (int cell : unit) {
                    if (tinted[cell]) continue;
                    tinted[cell] = true;
                    overlayCell(graphics, cell, COLOR_REGION_TINT);
                }
            }
        }

        /** 底色与整格高亮共用同一套叠加逻辑：向外扩 1 像素，3 像素线槽里仍留 1 像素，相邻格不会叠色。 */
        @OnlyIn(Dist.CLIENT)
        private void overlayCell(GuiGraphics graphics, int cell, int color) {
            int x = cellX(cell % SudokuRules.SIZE);
            int y = cellY(cell / SudokuRules.SIZE);
            graphics.fill(x - 1, y - 1, x + CELL_SIZE + 1, y + CELL_SIZE + 1, color);
        }

        /** 橙色高亮层：同数字整格用 overlayCell；含该数字的候选只给对应 5×5 小格加底色。 */
        @OnlyIn(Dist.CLIENT)
        private void drawHighlightLayer(GuiGraphics graphics) {
            var snapshot = view();
            int focus = snapshot.focusMask();
            int[] notes = snapshot.notes();
            for (int row = 0; row < SudokuRules.SIZE; row++) {
                for (int column = 0; column < SudokuRules.SIZE; column++) {
                    int cell = row * SudokuRules.SIZE + column;
                    int x = cellX(column);
                    int y = cellY(row);
                    int value = snapshot.valueAt(cell);
                    if (focus != 0 && value != 0) {
                        int bit = 1 << (value - SudokuRules.VALUE_MIN);
                        if ((focus & bit) != 0) {
                            overlayCell(graphics, cell, COLOR_MATCH_FILL);
                        }
                        continue;
                    }
                    if (focus == 0 || cell >= notes.length) continue;
                    int matched = notes[cell] & focus;
                    if (matched == 0) continue;
                    for (int digit = SudokuRules.VALUE_MIN; digit <= SudokuRules.VALUE_MAX; digit++) {
                        int bit = 1 << (digit - SudokuRules.VALUE_MIN);
                        if ((matched & bit) == 0) continue;
                        int noteColumn = (digit - SudokuRules.VALUE_MIN) % SudokuRules.BOX;
                        int noteRow = (digit - SudokuRules.VALUE_MIN) / SudokuRules.BOX;
                        graphics.fill(x + noteColumn * NOTE_SLOT, y + noteRow * NOTE_SLOT,
                                x + (noteColumn + 1) * NOTE_SLOT, y + (noteRow + 1) * NOTE_SLOT,
                                COLOR_NOTE_MATCH_FILL);
                    }
                }
            }
        }

        /** 格线层：细格线先画、粗格线后画，交叉处由粗格线压住；每条线槽 3 像素，细线只填中间 1 像素。 */
        @OnlyIn(Dist.CLIENT)
        private void drawGridLayer(GuiGraphics graphics) {
            int x0 = getPositionX();
            int y0 = getPositionY();
            int middle = THICK_GRID / 2;
            var variant = SudokuVariant.of(view().variant());
            boolean jigsaw = variant != null && variant.replacesBoxes();
            for (int index = 0; index <= SudokuRules.SIZE; index++) {
                // 锯齿模式没有 3×3 宫：所有内部线槽都画细线，粗线交给变体层按区域边界补
                if (!jigsaw && index % SudokuRules.BOX == 0) continue;
                int vertical = x0 + index * CELL_PITCH;
                int horizontal = y0 + index * CELL_PITCH;
                graphics.fill(vertical + middle, y0, vertical + middle + 1, y0 + BOARD_PIXELS, COLOR_GRID);
                graphics.fill(x0, horizontal + middle, x0 + BOARD_PIXELS, horizontal + middle + 1, COLOR_GRID);
            }
            if (jigsaw) return;
            for (int index = 0; index <= SudokuRules.SIZE; index++) {
                if (index % SudokuRules.BOX != 0) continue;
                int vertical = x0 + index * CELL_PITCH;
                int horizontal = y0 + index * CELL_PITCH;
                graphics.fill(vertical, y0, vertical + THICK_GRID, y0 + BOARD_PIXELS, COLOR_BOX);
                graphics.fill(x0, horizontal, x0 + BOARD_PIXELS, horizontal + THICK_GRID, COLOR_BOX);
            }
        }

        /** 变体层：锯齿边界、连续标记与杀手笼边界，压在格线之上、数字之下。 */
        @OnlyIn(Dist.CLIENT)
        private void drawVariantLayer(GuiGraphics graphics) {
            var variant = SudokuVariant.of(view().variant());
            if (variant == null) return;
            switch (variant) {
                case STANDARD, COLOR, DIAGONAL_X, CENTER_DOTS, WINDOW -> {}
                case JIGSAW -> drawJigsawBounds(graphics, view().jigsawMap());
                case CONSECUTIVE -> drawConsecutiveMarks(graphics);
                case KILLER -> drawCageBounds(graphics);
            }
        }

        /** 数字之上的变体层：杀手笼角标压在数字上，角落也看得清。 */
        @OnlyIn(Dist.CLIENT)
        private void drawVariantTopLayer(GuiGraphics graphics) {
            var variant = SudokuVariant.of(view().variant());
            if (variant == SudokuVariant.KILLER) drawCageTotals(graphics);
        }

        /** 锯齿模式：在区域交界处画 3 像素粗线，并补回被细线替代的外框。 */
        @OnlyIn(Dist.CLIENT)
        private void drawJigsawBounds(GuiGraphics graphics, int[] map) {
            if (map == null || map.length != SudokuRules.CELLS) return;
            int x0 = getPositionX();
            int y0 = getPositionY();
            for (int row = 0; row < SudokuRules.SIZE; row++) {
                for (int column = 0; column < SudokuRules.SIZE; column++) {
                    int cell = row * SudokuRules.SIZE + column;
                    if (column < SudokuRules.SIZE - 1 && map[cell] != map[cell + 1]) {
                        int slot = x0 + (column + 1) * CELL_PITCH;
                        graphics.fill(slot, cellY(row), slot + THICK_GRID, cellY(row) + CELL_PITCH, COLOR_BOX);
                    }
                    if (row < SudokuRules.SIZE - 1 && map[cell] != map[cell + SudokuRules.SIZE]) {
                        int slot = y0 + (row + 1) * CELL_PITCH;
                        graphics.fill(cellX(column), slot, cellX(column) + CELL_PITCH, slot + THICK_GRID, COLOR_BOX);
                    }
                }
            }
            // 粗线在格子左上角交会时，横竖两段都从边槽之后开始，会留下 3×3 顶点空缺；逐格补满。
            for (int row = 0; row < SudokuRules.SIZE; row++) {
                for (int column = 0; column < SudokuRules.SIZE; column++) {
                    int cell = row * SudokuRules.SIZE + column;
                    boolean left = column == 0 || map[cell - 1] != map[cell];
                    boolean top = row == 0 || map[cell - SudokuRules.SIZE] != map[cell];
                    if (left && top) {
                        graphics.fill(cellX(column) - THICK_GRID, cellY(row) - THICK_GRID, cellX(column),
                                cellY(row), COLOR_BOX);
                    }
                }
            }
            int right = x0 + BOARD_PIXELS;
            int bottom = y0 + BOARD_PIXELS;
            graphics.fill(x0, y0, right, y0 + THICK_GRID, COLOR_BOX);
            graphics.fill(x0, bottom - THICK_GRID, right, bottom, COLOR_BOX);
            graphics.fill(x0, y0, x0 + THICK_GRID, bottom, COLOR_BOX);
            graphics.fill(right - THICK_GRID, y0, right, bottom, COLOR_BOX);
        }

        /**
         * 连续变体：在相邻两格间的线槽中心画一条垂直于线槽、长 9 像素的亮紫短线（向两侧各伸 3 像素）；
         * 只画右与下，避免同一条边重复绘制。
         */
        @OnlyIn(Dist.CLIENT)
        private void drawConsecutiveMarks(GuiGraphics graphics) {
            int[] marks = view().marks();
            if (marks == null || marks.length != SudokuRules.CELLS) return;
            int half = CELL_SIZE / 2;
            for (int row = 0; row < SudokuRules.SIZE; row++) {
                for (int column = 0; column < SudokuRules.SIZE; column++) {
                    int cell = row * SudokuRules.SIZE + column;
                    int x = cellX(column);
                    int y = cellY(row);
                    if ((marks[cell] & SudokuConstraints.DIR_RIGHT) != 0) {
                        int slotX = x + CELL_SIZE;
                        int midY = y + half;
                        graphics.fill(slotX - THICK_GRID, midY, slotX + 2 * THICK_GRID, midY + 1,
                                COLOR_CONSECUTIVE);
                    }
                    if ((marks[cell] & SudokuConstraints.DIR_DOWN) != 0) {
                        int slotY = y + CELL_SIZE;
                        int midX = x + half;
                        graphics.fill(midX, slotY - THICK_GRID, midX + 1, slotY + 2 * THICK_GRID,
                                COLOR_CONSECUTIVE);
                    }
                }
            }
        }

        /**
         * 杀手变体：每个笼画一圈完整连续的正红细框。把笼内格子向四周扩 1 像素得到轮廓：与相邻笼共享的线槽
         * 两侧各留一条红框、中间 1 像素给格线；同笼相邻格的空隙再补齐，笼内不会出现分隔线。
         * 轮廓按行合并成矩形段缓存，笼表不变时不重算。
         */
        @OnlyIn(Dist.CLIENT)
        private void drawCageBounds(GuiGraphics graphics) {
            int[] cageOf = view().cageOf();
            if (cageOf == null || cageOf.length != SudokuRules.CELLS) {
                cageRunsSource = null;
                cageRuns = null;
                return;
            }
            // 客户端每次收到新快照都是新数组，按内容比较；笼表没变就不重算。
            if (cageRuns == null || cageRunsSource == null || !Arrays.equals(cageRunsSource, cageOf)) {
                cageRunsSource = cageOf;
                cageRuns = buildCageRuns(cageOf);
            }
            int x0 = getPositionX();
            int y0 = getPositionY();
            for (int[] run : cageRuns) {
                graphics.fill(x0 + run[0], y0 + run[1], x0 + run[2], y0 + run[3], COLOR_CAGE_BOUNDARY);
            }
        }

        /** 逐笼构造扩展后的像素轮廓，把同起止的相邻行合并成矩形段（盘面内局部坐标）。 */
        private int[][] buildCageRuns(int[] cageOf) {
            int maxCage = 0;
            for (int cage : cageOf) {
                maxCage = Math.max(maxCage, cage);
            }
            int width = BOARD_PIXELS;
            int height = BOARD_PIXELS;
            boolean[] mask = new boolean[width * height];
            List<int[]> runs = new ArrayList<>();
            int[] rowStarts = new int[width + 1];
            int[] rowEnds = new int[width + 1];
            for (int cage = 1; cage <= maxCage; cage++) {
                Arrays.fill(mask, false);
                for (int row = 0; row < SudokuRules.SIZE; row++) {
                    for (int column = 0; column < SudokuRules.SIZE; column++) {
                        int cell = row * SudokuRules.SIZE + column;
                        if (cageOf[cell] != cage) continue;
                        int x = THICK_GRID + column * CELL_PITCH;
                        int y = THICK_GRID + row * CELL_PITCH;
                        // 边界统一向线槽扩 1 像素，盘面最外圈也不例外：红框贴着格子外侧，
                        // 不再占掉格子本身的第一像素；同笼相邻格间的 1 像素空隙随后补上。
                        int x1 = x - 1;
                        int x2 = x + CELL_SIZE + 1;
                        int y1 = y - 1;
                        int y2 = y + CELL_SIZE + 1;
                        for (int py = y1; py < y2; py++) {
                            int base = py * width;
                            for (int px = x1; px < x2; px++) {
                                mask[base + px] = true;
                            }
                        }
                    }
                }
                // 同笼相邻格在线槽里各扩 1 像素后还留 1 像素空隙，补上让笼内连成一片。
                for (int row = 0; row < SudokuRules.SIZE; row++) {
                    for (int column = 0; column < SudokuRules.SIZE; column++) {
                        int cell = row * SudokuRules.SIZE + column;
                        if (cageOf[cell] != cage) continue;
                        int x = THICK_GRID + column * CELL_PITCH;
                        int y = THICK_GRID + row * CELL_PITCH;
                        if (column < SudokuRules.SIZE - 1 && cageOf[cell + 1] == cage) {
                            int gapX = x + CELL_SIZE + 1;
                            int y1 = y - 1;
                            int y2 = y + CELL_SIZE + 1;
                            for (int py = y1; py < y2; py++) {
                                mask[py * width + gapX] = true;
                            }
                        }
                        if (row < SudokuRules.SIZE - 1 && cageOf[cell + SudokuRules.SIZE] == cage) {
                            int gapY = y + CELL_SIZE + 1;
                            int x1 = x - 1;
                            int x2 = x + CELL_SIZE + 1;
                            for (int px = x1; px < x2; px++) {
                                mask[gapY * width + px] = true;
                            }
                        }
                        // 2×2 同笼时，横竖空隙的交叉点也要补，避免 1 像素洞。
                        if (column < SudokuRules.SIZE - 1 && row < SudokuRules.SIZE - 1 &&
                                cageOf[cell + 1] == cage && cageOf[cell + SudokuRules.SIZE] == cage &&
                                cageOf[cell + SudokuRules.SIZE + 1] == cage) {
                            mask[(y + CELL_SIZE + 1) * width + x + CELL_SIZE + 1] = true;
                        }
                    }
                }
                // 逐行找掩码边界，并把起止相同的相邻行合并成竖长矩形。
                List<int[]> active = new ArrayList<>();
                for (int py = 0; py < height; py++) {
                    int count = 0;
                    int px = 0;
                    while (px < width) {
                        while (px < width && !isCageBoundary(mask, px, py, width, height)) px++;
                        if (px >= width) break;
                        int start = px;
                        while (px < width && isCageBoundary(mask, px, py, width, height)) px++;
                        rowStarts[count] = start;
                        rowEnds[count] = px;
                        count++;
                    }
                    boolean[] matched = new boolean[active.size()];
                    List<int[]> next = new ArrayList<>(count);
                    for (int index = 0; index < count; index++) {
                        int begin = py;
                        for (int old = 0; old < active.size(); old++) {
                            if (matched[old]) continue;
                            int[] previous = active.get(old);
                            if (previous[0] == rowStarts[index] && previous[1] == rowEnds[index]) {
                                begin = previous[2];
                                matched[old] = true;
                                break;
                            }
                        }
                        next.add(new int[] { rowStarts[index], rowEnds[index], begin });
                    }
                    for (int old = 0; old < active.size(); old++) {
                        if (matched[old]) continue;
                        int[] previous = active.get(old);
                        runs.add(new int[] { previous[0], previous[2], previous[1], py });
                    }
                    active = next;
                }
                for (int[] previous : active) {
                    runs.add(new int[] { previous[0], previous[2], previous[1], height });
                }
            }
            return runs.toArray(new int[0][]);
        }

        /**
         * 掩码像素是否在轮廓上：除四邻域外还看四个对角。笼在凹角处横竖框线只差一个对角像素，
         * 漏了它会留下 1 像素缺口、框线在尖端断开；不同笼之间的 1 像素间隔不受影响。
         */
        private boolean isCageBoundary(boolean[] mask, int x, int y, int width, int height) {
            int index = y * width + x;
            if (!mask[index]) return false;
            if (x == 0 || x == width - 1 || y == 0 || y == height - 1) return true;
            if (!mask[index - 1] || !mask[index + 1] || !mask[index - width] || !mask[index + width]) return true;
            return !mask[index - width - 1] || !mask[index - width + 1] ||
                    !mask[index + width - 1] || !mask[index + width + 1];
        }

        /** 杀手变体：每个笼在行优先第一个格子的左上角写目标和的半字号小字。 */
        @OnlyIn(Dist.CLIENT)
        private void drawCageTotals(GuiGraphics graphics) {
            int[] totals = view().cageTotals();
            int[] cageOf = view().cageOf();
            if (totals == null || totals.length == 0 || cageOf == null || cageOf.length != SudokuRules.CELLS) return;
            var font = UIText.font();
            var pose = graphics.pose();
            for (int cage = 1; cage <= totals.length; cage++) {
                int anchor = -1;
                for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
                    if (cageOf[cell] == cage) {
                        anchor = cell;
                        break;
                    }
                }
                if (anchor < 0) continue;
                String text = Integer.toString(totals[cage - 1]);
                int x = cellX(anchor % SudokuRules.SIZE) + 1;
                int y = cellY(anchor / SudokuRules.SIZE);
                pose.pushPose();
                pose.translate(x, y, 0);
                pose.scale(NOTE_SCALE, NOTE_SCALE, 1.0F);
                graphics.drawString(font, text, 0, 0, COLOR_CAGE_TOTAL, false);
                pose.popPose();
            }
        }

        /**
         * 选中格的天青色选择高亮：贴住 15×15 格边、线宽 1 像素，不向外扩。
         */
        @OnlyIn(Dist.CLIENT)
        private void drawSelectionLayer(GuiGraphics graphics) {
            int selected = view().selected();
            if (selected < 0 || selected >= SudokuRules.CELLS) return;
            int left = cellX(selected % SudokuRules.SIZE);
            int top = cellY(selected / SudokuRules.SIZE);
            int right = left + CELL_SIZE;
            int bottom = top + CELL_SIZE;
            graphics.fill(left, top, right, top + 1, COLOR_SELECTED);
            graphics.fill(left, bottom - 1, right, bottom, COLOR_SELECTED);
            graphics.fill(left, top + 1, left + 1, bottom - 1, COLOR_SELECTED);
            graphics.fill(right - 1, top + 1, right, bottom - 1, COLOR_SELECTED);
        }

        /** 数字层：数字在格内居中，候选数按 3×3 小格显示，颜色由来源决定。 */
        @OnlyIn(Dist.CLIENT)
        private void drawDigitLayer(GuiGraphics graphics) {
            var snapshot = view();
            int[] givens = snapshot.givens();
            int[] values = snapshot.values();
            int[] notes = snapshot.notes();
            for (int row = 0; row < SudokuRules.SIZE; row++) {
                for (int column = 0; column < SudokuRules.SIZE; column++) {
                    int cell = row * SudokuRules.SIZE + column;
                    int x = cellX(column);
                    int y = cellY(row);
                    int given = cell < givens.length ? givens[cell] : 0;
                    if (given != 0) {
                        drawDigit(graphics, given, x, y, COLOR_GIVEN);
                        continue;
                    }
                    int value = cell < values.length ? values[cell] : 0;
                    if (value != 0) {
                        drawDigit(graphics, value, x, y, COLOR_ENTERED);
                        continue;
                    }
                    int note = cell < notes.length ? notes[cell] : 0;
                    if (note != 0) drawNotes(graphics, note, x, y);
                }
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawDigit(GuiGraphics graphics, int digit, int cellX, int cellY, int color) {
            var font = UIText.font();
            String text = Integer.toString(digit);
            graphics.drawString(font, text, cellX + (CELL_SIZE - font.width(text)) / 2,
                    UIText.centerY(cellY, CELL_SIZE), color, false);
        }

        /** 候选数按 3×3 摊在 15×15 的格内，字号缩到一半，一律用灰色。 */
        @OnlyIn(Dist.CLIENT)
        private void drawNotes(GuiGraphics graphics, int note, int cellX, int cellY) {
            var font = UIText.font();
            var pose = graphics.pose();
            for (int digit = SudokuRules.VALUE_MIN; digit <= SudokuRules.VALUE_MAX; digit++) {
                int bit = 1 << (digit - SudokuRules.VALUE_MIN);
                if ((note & bit) == 0) continue;
                String text = Integer.toString(digit);
                int column = (digit - SudokuRules.VALUE_MIN) % SudokuRules.BOX;
                int row = (digit - SudokuRules.VALUE_MIN) / SudokuRules.BOX;
                float offsetX = cellX + column * NOTE_SLOT + (NOTE_SLOT - font.width(text) * NOTE_SCALE) / 2;
                float offsetY = cellY + row * NOTE_SLOT + (NOTE_SLOT - UIText.GLYPH_HEIGHT * NOTE_SCALE) / 2;
                pose.pushPose();
                pose.translate(offsetX, offsetY, 0);
                pose.scale(NOTE_SCALE, NOTE_SCALE, 1.0F);
                graphics.drawString(font, text, 0, 0, COLOR_NOTE, false);
                pose.popPose();
            }
        }
    }

    /** 难度页：六个档位纵向排列，选中项两端带绿色 > <，切换后立即开新局。 */
    private final class DifficultyPage extends GamePage {

        private DifficultyPage() {
            super(WidgetIcons.SETTINGS, PAGE_DIFFICULTY, PAGE_DIFFICULTY_TOOLTIP);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(allowOthersRow());

            var section = Form.section(SECTION_DIFFICULTY);
            for (int ordinal = 0; ordinal < SudokuRules.Difficulty.ALL.length; ordinal++) {
                // 循环变量不是有效 final，拷一份给 lambda。
                int index = ordinal;
                var difficulty = SudokuRules.Difficulty.ALL[index];
                var button = choiceButton(LayoutStyle.AUTO, DIFFICULTY_NAMES[index],
                        () -> difficulty.ordinal() == view().difficulty());
                button.bindTooltip(() -> {
                    var current = SudokuVariant.ALL[Math.clamp(view().variant(), 0, SudokuVariant.ALL.length - 1)];
                    return Component.translatable(TEXT_DIFFICULTY,
                            DIFFICULTY_NAMES[index],
                            number(SudokuRules.reward(difficulty, current)))
                            .append(Component.literal(" ×" + multiplierText(current)));
                });
                button.setOnServerClick(() -> selectDifficulty(index, playerOf(button)));
                section.addChild(button);
            }
            page.addChild(section);
            page.addChild(balancePanel());
            return page;
        }
    }

    /** 变体页：标准与各变体两两一行排列，切换后立即开新局；一次只能选一条。 */
    private final class VariantPage extends GamePage {

        private VariantPage() {
            super(WidgetIcons.FILTER, PAGE_VARIANT, PAGE_VARIANT_TOOLTIP);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(allowOthersRow());

            var section = Form.section(PAGE_VARIANT);
            int buttonWidth = (UISizes.CONTENT_WIDTH - 2 * UISizes.PANEL_PADDING - 2) / 2;
            for (int start = 0; start < SudokuVariant.ALL.length; start += 2) {
                var row = UIElement.row(Button.HEIGHT).layout(style -> style.justifyContent(AlignContent.CENTER)
                        .gapAll(2));
                for (int offset = 0; offset < 2 && start + offset < SudokuVariant.ALL.length; offset++) {
                    int index = start + offset;
                    var button = choiceButton(buttonWidth, VARIANT_NAMES[index],
                            () -> index == view().variant());
                    button.setOnServerClick(() -> selectVariant(index, playerOf(button)));
                    row.addChild(button);
                }
                section.addChild(row);
            }
            page.addChild(section);
            page.addChild(balancePanel());
            return page;
        }
    }

    /**
     * 排行榜页：两条横向滚轴，上面选难度、下面选变体，再下面是该组合的榜。成绩按「难度 × 变体」二维存放，
     * 基类 {@code LeaderboardTabPage} 只支持单条滚轴，故这里自己拼两条并复用 optionStrip/optionRow/milestoneTable。
     *
     * <p>
     * 两个选择项拼成一个整数作为 {@link SwitchedContent} 的键，任一维变化都会重建表格；同一行的数值变化由格子的同步值刷新。
     */
    private final class LeaderboardPage extends GamePage {

        /** 当前选中的难度序数；服务端改选后作为 SwitchedContent 的键下发。 */
        private int difficulty;
        /** 当前选中的变体序数。 */
        private int variant;

        private LeaderboardPage() {
            super(LEADERBOARD_ICON, PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(optionRow(optionStrip(SudokuRules.Difficulty.ALL.length, this::difficultyLabel,
                    () -> difficulty, index -> difficulty = index)));
            page.addChild(optionRow(optionStrip(SudokuVariant.ALL.length, this::variantLabel,
                    () -> variant, index -> variant = index)));
            // 把难度×变体压成一个整数作为 SwitchedContent 的键
            page.addChild(new SwitchedContent(() -> difficulty * SudokuVariant.ALL.length + variant,
                    (key, remote) -> boardSection()));
            page.addChild(balancePanel());
            return page;
        }

        /** 难度名字的翻译键；由控件在客户端解析当前语言。 */
        private Component difficultyLabel(int index) {
            return DIFFICULTY_NAMES[Math.clamp(index, 0, DIFFICULTY_NAMES.length - 1)];
        }

        /** 变体名字的翻译键。 */
        private Component variantLabel(int index) {
            return VARIANT_NAMES[Math.clamp(index, 0, VARIANT_NAMES.length - 1)];
        }

        /**
         * 当前组合的榜单区块。标题只用变体名：{@code Form.section} 只接受单个翻译键、没有参数化重载，
         * 难度与变体由上方两条滚轴的绿色选中标记表达。
         */
        private Widget boardSection() {
            int selectedDifficulty = Math.clamp(difficulty, 0, SudokuRules.Difficulty.ALL.length - 1);
            int selectedVariant = Math.clamp(variant, 0, SudokuVariant.ALL.length - 1);
            var section = Form.section(VARIANT_NAMES[selectedVariant]);
            // 表格 id 带两个序号，换组合时 ScrollerView 的拖动/缩放记忆不会串榜。
            section.addChild(milestoneTable("game_milestones_sudoku_" + selectedDifficulty + "_" + selectedVariant,
                    LEADERBOARD_COLUMNS, this::milestoneVersion, () -> leaderboardRows(selectedDifficulty, selectedVariant)));
            return section;
        }

        /** 数独里程碑的数据版本，供 {@link AbstractGameMachine#milestoneTable} 的通用重载使用。 */
        private int milestoneVersion() {
            var store = sudokuMilestones();
            return store == null ? Integer.MIN_VALUE : store.modCount();
        }

        /** 单个「难度 × 变体」组合的榜单：玩家 + 最快完成用时，用时悬停显示达成时刻。 */
        private List<MilestoneCell[]> leaderboardRows(int difficultyOrdinal, int variantOrdinal) {
            var store = sudokuMilestones();
            var server = server();
            return store == null || server == null ? List.of() :
                    buildLeaderboardRows(store, server, difficultyOrdinal, variantOrdinal);
        }
    }

    /** 单个「难度 × 变体」组合的榜单行（服务端构建），排行榜页与「全部榜单」页共用。 */
    private static List<MilestoneCell[]> buildLeaderboardRows(SudokuMilestones store, MinecraftServer server,
                                                              int difficulty, int variant) {
        var stats = store.board(difficulty, variant);
        var rows = new ObjectArrayList<MilestoneCell[]>(stats.size());
        for (var stat : stats) {
            rows.add(new MilestoneCell[] {
                    MilestoneCell.of(Component.literal(SudokuMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.achieved(duration(stat.ticks()), stat.completedGameTime()) });
        }
        return rows;
    }
}

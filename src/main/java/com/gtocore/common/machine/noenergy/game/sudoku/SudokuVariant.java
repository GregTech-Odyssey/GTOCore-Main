package com.gtocore.common.machine.noenergy.game.sudoku;

import org.jetbrains.annotations.Nullable;

/**
 * 数独的附加变体，分三类：固定几何型（对角线、中心点、窗口、彩色）直接给出格集合；每局生成型（锯齿）
 * 由 {@link JigsawLayout} 每局生长；推导型（连续、杀手）依赖本局完整解反推。
 * 求解、出题、评级与判题都只认 {@link SudokuConstraints}，新增变体不用改求解器；当前界面只能选一条，{@link #mask()} 按位预留组合。
 */
public enum SudokuVariant {

    /** 标准数独：只有行、列、3×3 宫。 */
    STANDARD(100),
    /** DX 对角线：两条对角线各自 1~9 不重复。 */
    DIAGONAL_X(200),
    /** CD 中心点：9 个宫的中心格组成一组 1~9。 */
    CENTER_DOTS(200),
    /** HY 窗口：4 个错位的 3×3 窗口，每窗 1~9。 */
    WINDOW(250),
    /** CO 彩色：9 个固定分区，每组取各宫同一偏移位置的 9 格。 */
    COLOR(300),
    /** JG 锯齿：用 9 个 180° 对称的不规则区域替换标准 3×3 宫。 */
    JIGSAW(400),
    /** CS 连续：相邻格之间的标记边两侧数字必须相差 1。 */
    CONSECUTIVE(500),
    /** KL 杀手：笼内数字之和等于角标，且笼内不重复。 */
    KILLER(800);

    public static final SudokuVariant[] ALL = values();

    /** 完成奖励倍率，百分数：100 表示 ×1，110 表示 ×1.1；越难的变体倍率越高。 */
    private final int rewardPercent;

    SudokuVariant(int rewardPercent) {
        this.rewardPercent = rewardPercent;
    }

    private static final int SIZE = SudokuRules.SIZE;
    private static final int[][] NONE = new int[0][];

    /** DX：主对角线、副对角线。 */
    private static final int[][] DIAGONALS = {
            { 0, 10, 20, 30, 40, 50, 60, 70, 80 },
            { 8, 16, 24, 32, 40, 48, 56, 64, 72 } };

    /** CD：行、列都取 1/4/7（0 起算）交叉出的 9 个中心格。 */
    private static final int[][] CENTER_DOTS_UNITS = { { 10, 13, 16, 37, 40, 43, 64, 67, 70 } };

    private static final int[][] WINDOW_UNITS = {
            { 10, 11, 12, 19, 20, 21, 28, 29, 30 },
            { 14, 15, 16, 23, 24, 25, 32, 33, 34 },
            { 46, 47, 48, 55, 56, 57, 64, 65, 66 },
            { 50, 51, 52, 59, 60, 61, 68, 69, 70 } };

    /** CO 的固定分区：偏移 0~8 对应宫的九个相对位置，每组覆盖 9 个宫的同一位置。 */
    private static final int[] COLOR_MAP = buildColorMap();
    private static final int[][] COLOR_UNITS = regionsFromMap(COLOR_MAP);

    private static int[] buildColorMap() {
        int[] map = new int[SIZE * SIZE];
        for (int boxRow = 0; boxRow < SudokuRules.BOX; boxRow++) {
            for (int boxColumn = 0; boxColumn < SudokuRules.BOX; boxColumn++) {
                for (int rowOffset = 0; rowOffset < SudokuRules.BOX; rowOffset++) {
                    for (int columnOffset = 0; columnOffset < SudokuRules.BOX; columnOffset++) {
                        int row = boxRow * SudokuRules.BOX + rowOffset;
                        int column = boxColumn * SudokuRules.BOX + columnOffset;
                        map[row * SIZE + column] = rowOffset * SudokuRules.BOX + columnOffset;
                    }
                }
            }
        }
        return map;
    }

    /** 把“每格属于哪个区域”的映射收集成若干组等长全异单元。 */
    private static int[][] regionsFromMap(int[] map) {
        int[] counts = new int[SIZE];
        for (int region : map) {
            counts[region]++;
        }
        int[][] regions = new int[SIZE][];
        int[] filled = new int[SIZE];
        for (int region = 0; region < SIZE; region++) {
            regions[region] = new int[counts[region]];
        }
        for (int cell = 0; cell < map.length; cell++) {
            int region = map[cell];
            regions[region][filled[region]++] = cell;
        }
        return regions;
    }

    /** 规则位掩码：标准为 0，其余按序数左移（ordinal - 1）；多规则按位或。 */
    public int mask() {
        return this == STANDARD ? 0 : 1 << (ordinal() - 1);
    }

    /** 完成奖励的变体倍率（百分数）：100 为 ×1，110 为 ×1.1。 */
    public int rewardPercent() {
        return rewardPercent;
    }

    /** 是否用不规则区域替换标准 3×3 宫；仅锯齿为 true。 */
    public boolean replacesBoxes() {
        return this == JIGSAW;
    }

    /** 固定几何变体追加的全异单元；推导型（连续、杀手）由出题时动态生成。 */
    public int[][] extraUnits() {
        return switch (this) {
            case DIAGONAL_X -> DIAGONALS;
            case CENTER_DOTS -> CENTER_DOTS_UNITS;
            case WINDOW -> WINDOW_UNITS;
            case COLOR -> COLOR_UNITS;
            case STANDARD, JIGSAW, CONSECUTIVE, KILLER -> NONE;
        };
    }

    /** 彩色变体的固定每格区域编号；锯齿区域每局由 {@link JigsawLayout} 生成，经约束单独传入。 */
    public @Nullable int[] regionMap() {
        return this == COLOR ? COLOR_MAP : null;
    }

    /** 恒为空；锯齿区域每局生成，由 {@link SudokuConstraints} 经 {@link JigsawLayout#regions(int[])} 传入。 */
    public int[][] regions() {
        return NONE;
    }

    /** 按序数取变体，越界返回 {@code null}；存档与网络来的值均经此校验。 */
    public static @Nullable SudokuVariant of(int ordinal) {
        return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
    }
}

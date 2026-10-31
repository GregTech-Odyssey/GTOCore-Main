package com.gtocore.common.machine.noenergy.game.sudoku;

import org.jetbrains.annotations.Nullable;

/**
 * 一局数独的完整约束：全异单元 + 连续标记边 + 杀手笼。
 *
 * <p>
 * 固定变体与锯齿经 {@link SudokuRuleSet} 组装；杀手笼作为额外全异单元挂入，连续标记存成每格方位掩码。
 * 求解器只从这里取约束，叠加规则无需另写求解逻辑；界面目前只传单条规则。
 */
public final class SudokuConstraints {

    /** 相邻格方位：格号按行优先，上下左右各占一位。 */
    public static final int DIR_UP = 1;
    public static final int DIR_RIGHT = 1 << 1;
    public static final int DIR_DOWN = 1 << 2;
    public static final int DIR_LEFT = 1 << 3;
    public static final int ALL_DIRECTIONS = DIR_UP | DIR_RIGHT | DIR_DOWN | DIR_LEFT;
    /** 四个方位按上右下左排列；遍历邻居处复用。 */
    static final int[] ALL_DIRECTIONS_BITS = { DIR_UP, DIR_RIGHT, DIR_DOWN, DIR_LEFT };

    private static final int[][] NO_UNITS = new int[0][];

    private final int variantMask;
    private final SudokuRuleSet rules;
    private final int[] cageOf;
    private final int[] cageTotals;
    private final int[] marks;

    private SudokuConstraints(int variantMask, SudokuRuleSet rules, int[] cageOf, int[] cageTotals,
                              int[] marks) {
        this.variantMask = variantMask;
        this.rules = rules;
        this.cageOf = cageOf;
        this.cageTotals = cageTotals;
        this.marks = marks;
    }

    /** 只有固定几何的约束；锯齿需要本局区域表，否则退化成标准宫。 */
    public static SudokuConstraints of(SudokuVariant variant) {
        return of(variant.mask(), null, null, null, null);
    }

    /** 只有固定几何的多规则约束；连续、杀手与锯齿需要另外传数据数组。 */
    public static SudokuConstraints of(int variantMask) {
        return of(variantMask, null, null, null, null);
    }

    /** 单条变体加本局数据的约束。 */
    public static SudokuConstraints of(SudokuVariant variant, @Nullable int[] jigsawMap, @Nullable int[] cageOf,
                                       @Nullable int[] cageTotals, @Nullable int[] marks) {
        return of(variant.mask(), jigsawMap, cageOf, cageTotals, marks);
    }

    /**
     * 按变体位掩码组装约束：锯齿用本局区域替换标准宫，杀手笼追加为全异单元，连续标记交给求解器过滤候选。
     *
     * @param variantMask {@link SudokuVariant#mask()} 的位或；0 表示标准规则
     * @param jigsawMap   每格所属锯齿区域（0~8），仅在选中锯齿时使用
     * @param cageOf      每格所属笼编号（0 表示无笼），仅在选中杀手时使用
     * @param cageTotals  每个笼的目标和（下标 = 笼编号 - 1），仅在选中杀手时使用
     * @param marks       每格连续标记方位掩码，仅在选中连续时使用
     */
    public static SudokuConstraints of(int variantMask, @Nullable int[] jigsawMap, @Nullable int[] cageOf,
                                       @Nullable int[] cageTotals, @Nullable int[] marks) {
        int mask = SudokuRuleSet.normalizeMask(variantMask);
        int[] cells = new int[SudokuRules.CELLS];
        int[] totals = new int[0];
        int[] marksMap = new int[SudokuRules.CELLS];
        int[][] cageUnits = NO_UNITS;
        SudokuRuleSet rules = SudokuRuleSet.of(mask);
        if ((mask & SudokuVariant.JIGSAW.mask()) != 0 && jigsawMap != null &&
                jigsawMap.length == SudokuRules.CELLS) {
            rules = rules.withBoxRegions(JigsawLayout.regions(jigsawMap));
        }
        if ((mask & SudokuVariant.KILLER.mask()) != 0 && cageOf != null &&
                cageOf.length == SudokuRules.CELLS && cageTotals != null && cageTotals.length > 0) {
            cells = cageOf;
            totals = cageTotals;
            cageUnits = cageUnits(cageOf, cageTotals);
        }
        if ((mask & SudokuVariant.CONSECUTIVE.mask()) != 0 && marks != null &&
                marks.length == SudokuRules.CELLS) {
            marksMap = marks;
        }
        rules = rules.withExtraUnits(cageUnits);
        return new SudokuConstraints(mask, rules, cells, totals, marksMap);
    }

    /** 杀手笼：尺寸大于 1 的笼同时是一个全异单元，尺寸 1 的笼只靠和约束。 */
    private static int[][] cageUnits(int[] cageOf, int[] cageTotals) {
        int length = cageTotals.length;
        int[] magnitudes = new int[length];
        for (int cage : cageOf) {
            if (cage >= 1 && cage <= length) magnitudes[cage - 1]++;
        }
        int multi = 0;
        for (int count : magnitudes) {
            if (count > 1) multi++;
        }
        if (multi == 0) return NO_UNITS;
        int[][] units = new int[multi][];
        int[] filled = new int[length];
        int next = 0;
        for (int cage = 1; cage <= length; cage++) {
            if (magnitudes[cage - 1] > 1) {
                units[next++] = new int[magnitudes[cage - 1]];
            }
        }
        for (int cell = 0; cell < cageOf.length; cell++) {
            int cage = cageOf[cell];
            if (cage >= 1 && cage <= length && magnitudes[cage - 1] > 1) {
                int index = rank(magnitudes, cage);
                units[index][filled[cage - 1]++] = cell;
            }
        }
        return units;
    }

    /** 笼编号在多格笼里的下标：按笼编号升序数出第几个。 */
    private static int rank(int[] magnitudes, int cage) {
        int rank = 0;
        for (int index = 1; index < cage; index++) {
            if (magnitudes[index - 1] > 1) rank++;
        }
        return rank;
    }

    /** 从 {@code cell} 沿 {@code direction} 走一格；越界返回 -1。 */
    public static int neighbor(int cell, int direction) {
        int row = cell / SudokuRules.SIZE;
        int column = cell % SudokuRules.SIZE;
        if ((direction & DIR_UP) != 0) return row > 0 ? cell - SudokuRules.SIZE : -1;
        if ((direction & DIR_RIGHT) != 0) {
            return column < SudokuRules.SIZE - 1 ? cell + 1 : -1;
        }
        if ((direction & DIR_DOWN) != 0) {
            return row < SudokuRules.SIZE - 1 ? cell + SudokuRules.SIZE : -1;
        }
        if ((direction & DIR_LEFT) != 0) return column > 0 ? cell - 1 : -1;
        return -1;
    }

    public int variantMask() {
        return variantMask;
    }

    public SudokuRuleSet rules() {
        return rules;
    }

    /** 每格所属笼编号；0 表示没有笼。 */
    public int[] cageOf() {
        return cageOf;
    }

    public int[] cageTotals() {
        return cageTotals;
    }

    /** 每格连续标记方位掩码；0 表示没有标记。 */
    public int[] marks() {
        return marks;
    }

    public boolean hasCages() {
        return cageTotals.length > 0;
    }

    public boolean hasMarks() {
        for (int mark : marks) {
            if (mark != 0) return true;
        }
        return false;
    }
}

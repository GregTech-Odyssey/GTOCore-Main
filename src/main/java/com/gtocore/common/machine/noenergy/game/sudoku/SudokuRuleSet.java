package com.gtocore.common.machine.noenergy.game.sudoku;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * 一局实际生效的全异单元：9 行 + 9 列 +（标准 9 宫或锯齿 9 区）+ 各变体追加的单元。
 *
 * <p>
 * 求解器只读 {@link #units()} 与 {@link #unitsOf(int)}，因此任何「同组数字互异」的约束都能直接叠加；杀手笼等动态单元由
 * {@link #withExtraUnits(int[][])} 追加，固定几何走缓存。{@link #of(int)} 接受 {@link SudokuVariant#mask()} 的位或，
 * 界面目前只传单条掩码，多规则叠加在求解与判题层已支持。
 */
public final class SudokuRuleSet {

    private static final int MASK_LIMIT = 1 << (SudokuVariant.ALL.length - 1);
    private static final SudokuRuleSet[] CACHE = new SudokuRuleSet[MASK_LIMIT];

    private final int variantMask;
    private final int[][] units;
    private final int[][] unitsOf;

    private SudokuRuleSet(int variantMask, int[][] units) {
        this.variantMask = variantMask;
        this.units = units;
        this.unitsOf = buildUnitsOf(units);
    }

    private static int[][] buildUnitsOf(int[][] units) {
        int[] counts = new int[SudokuRules.CELLS];
        for (int[] unit : units) {
            for (int cell : unit) {
                counts[cell]++;
            }
        }
        int[][] unitsOf = new int[SudokuRules.CELLS][];
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            unitsOf[cell] = new int[counts[cell]];
        }
        int[] filled = new int[SudokuRules.CELLS];
        for (int unitIndex = 0; unitIndex < units.length; unitIndex++) {
            for (int cell : units[unitIndex]) {
                unitsOf[cell][filled[cell]++] = unitIndex;
            }
        }
        return unitsOf;
    }

    private static int[][] buildBaseUnits(int variantMask) {
        var collected = new ObjectArrayList<int[]>(SudokuRules.SIZE * 3 + 9);
        addRowsAndColumns(collected);
        for (int box = 0; box < SudokuRules.SIZE; box++) {
            collected.add(boxCells(box));
        }
        addVariantUnits(collected, variantMask);
        return collected.toArray(new int[0][]);
    }

    private static void addRowsAndColumns(ObjectArrayList<int[]> collected) {
        for (int row = 0; row < SudokuRules.SIZE; row++) {
            int[] unit = new int[SudokuRules.SIZE];
            for (int column = 0; column < SudokuRules.SIZE; column++) {
                unit[column] = row * SudokuRules.SIZE + column;
            }
            collected.add(unit);
        }
        for (int column = 0; column < SudokuRules.SIZE; column++) {
            int[] unit = new int[SudokuRules.SIZE];
            for (int row = 0; row < SudokuRules.SIZE; row++) {
                unit[row] = row * SudokuRules.SIZE + column;
            }
            collected.add(unit);
        }
    }

    private static void addVariantUnits(ObjectArrayList<int[]> collected, int variantMask) {
        for (var variant : SudokuVariant.ALL) {
            if ((variantMask & variant.mask()) == 0) continue;
            for (int[] unit : variant.extraUnits()) {
                collected.add(unit);
            }
        }
    }

    private static int[] boxCells(int box) {
        int startRow = box / SudokuRules.BOX * SudokuRules.BOX;
        int startColumn = box % SudokuRules.BOX * SudokuRules.BOX;
        int[] cells = new int[SudokuRules.SIZE];
        int index = 0;
        for (int row = startRow; row < startRow + SudokuRules.BOX; row++) {
            for (int column = startColumn; column < startColumn + SudokuRules.BOX; column++) {
                cells[index++] = row * SudokuRules.SIZE + column;
            }
        }
        return cells;
    }

    /** 单条变体的固定规则集；锯齿的每局区域需再经 {@link #withBoxRegions(int[][])} 替换宫。 */
    public static SudokuRuleSet of(SudokuVariant variant) {
        return of(variant.mask());
    }

    /** 裁掉未定义的位。 */
    static int normalizeMask(int variantMask) {
        return variantMask & (MASK_LIMIT - 1);
    }

    /** 按位或组合的规则集；未定义的位会被忽略。 */
    public static SudokuRuleSet of(int variantMask) {
        int mask = normalizeMask(variantMask);
        var cached = CACHE[mask];
        if (cached != null) return cached;
        var created = new SudokuRuleSet(mask, buildBaseUnits(mask));
        CACHE[mask] = created;
        return created;
    }

    /**
     * 用本局锯齿区域替换标准 3×3 宫，行列与其它变体单元不变；区域每局不同，不进缓存。
     * {@code regions} 不是 9 组时原样返回。
     */
    public SudokuRuleSet withBoxRegions(int[][] regions) {
        if (regions.length != SudokuRules.SIZE) return this;
        var collected = new ObjectArrayList<int[]>(SudokuRules.SIZE * 3 + SudokuRules.CELLS);
        addRowsAndColumns(collected);
        for (int[] region : regions) {
            collected.add(region);
        }
        addVariantUnits(collected, variantMask);
        return new SudokuRuleSet(variantMask, collected.toArray(new int[0][]));
    }

    /**
     * 在固定单元之上追加动态单元（杀手笼等）；追加为空时返回当前实例。动态规则集不进缓存，每局重建。
     */
    public SudokuRuleSet withExtraUnits(int[][] extraUnits) {
        if (extraUnits.length == 0) return this;
        int[][] combined = new int[units.length + extraUnits.length][];
        System.arraycopy(units, 0, combined, 0, units.length);
        System.arraycopy(extraUnits, 0, combined, units.length, extraUnits.length);
        return new SudokuRuleSet(variantMask, combined);
    }

    public int variantMask() {
        return variantMask;
    }

    public int unitCount() {
        return units.length;
    }

    public int[][] units() {
        return units;
    }

    public int[] unitsOf(int cell) {
        return unitsOf[cell];
    }
}

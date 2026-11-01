package com.gtocore.common.machine.noenergy.game.sudoku;

/**
 * 一局生成好的盘面数据：提示盘 + 锯齿区域 + 推导型变体的附加数据。
 * 锯齿区域每局重新生成，随本局存盘并下发；连续带标记边、杀手带笼数据，判定时经 {@link #constraints(int)} 还原约束。
 *
 * @param givens     提示盘，0 为空格
 * @param jigsawMap  每格所属锯齿区域（0~8），仅锯齿变体有效
 * @param cageOf     每格所属笼编号（0 表示无笼），仅杀手变体非空
 * @param cageTotals 每个笼的目标和（下标 = 笼编号 - 1），仅杀手变体非空
 * @param marks      每格连续标记方位掩码，仅连续变体非空
 */
record SudokuPuzzle(int[] givens, int[] jigsawMap, int[] cageOf, int[] cageTotals, int[] marks) {

    public SudokuConstraints constraints(SudokuVariant variant) {
        return constraints(variant.mask());
    }

    /** 按变体位掩码还原约束；求解与判题共用。 */
    public SudokuConstraints constraints(int variantMask) {
        return SudokuConstraints.of(variantMask, jigsawMap, cageOf, cageTotals, marks);
    }
}

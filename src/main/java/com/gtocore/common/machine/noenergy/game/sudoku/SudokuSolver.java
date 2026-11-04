package com.gtocore.common.machine.noenergy.game.sudoku;

import net.minecraft.util.RandomSource;

/**
 * 数独求解器：只认 {@link SudokuConstraints}，不关心当前是哪条变体。
 *
 * <p>
 * 候选数 = 所属全异单元的已用数字取并集排除，再按连续标记剔除与已填邻居不相差 1 的数字；杀手笼用
 * 「已填和 + 剩余格最小/最大候选和」做区间剪枝。出题、评级与判题共用这一套。
 * 非杀手出题按唯一解挖空，判题只查填满 + 约束合法（杀手允许多解）。
 */
final class SudokuSolver {

    private SudokuSolver() {}

    // ==================== 候选与放置 ====================

    static int candidates(SudokuConstraints constraints, int[] board, int[] unitUsed, int cell) {
        int used = 0;
        for (int unit : constraints.rules().unitsOf(cell)) {
            used |= unitUsed[unit];
        }
        int mask = SudokuRules.ALL_VALUES & ~used;
        int marks = constraints.marks()[cell];
        if (marks != 0) {
            mask = applyMarks(board, cell, marks, mask);
        }
        return mask;
    }

    /** 连续标记：相邻格已填时，本格只能取比它大 1 或小 1 的数字。 */
    private static int applyMarks(int[] board, int cell, int marks, int mask) {
        int allowed = SudokuRules.ALL_VALUES;
        if ((marks & SudokuConstraints.DIR_UP) != 0) {
            allowed &= neighborAllowed(board, cell, SudokuConstraints.DIR_UP);
        }
        if ((marks & SudokuConstraints.DIR_RIGHT) != 0) {
            allowed &= neighborAllowed(board, cell, SudokuConstraints.DIR_RIGHT);
        }
        if ((marks & SudokuConstraints.DIR_DOWN) != 0) {
            allowed &= neighborAllowed(board, cell, SudokuConstraints.DIR_DOWN);
        }
        if ((marks & SudokuConstraints.DIR_LEFT) != 0) {
            allowed &= neighborAllowed(board, cell, SudokuConstraints.DIR_LEFT);
        }
        return mask & allowed;
    }

    private static int neighborAllowed(int[] board, int cell, int direction) {
        int neighbor = SudokuConstraints.neighbor(cell, direction);
        if (neighbor < 0) return SudokuRules.ALL_VALUES;
        int value = board[neighbor];
        if (value == 0) return SudokuRules.ALL_VALUES;
        int allowed = 0;
        if (value > SudokuRules.VALUE_MIN) {
            allowed |= 1 << (value - 1 - SudokuRules.VALUE_MIN);
        }
        if (value < SudokuRules.VALUE_MAX) {
            allowed |= 1 << (value + 1 - SudokuRules.VALUE_MIN);
        }
        return allowed;
    }

    static void assign(int[] board, SudokuConstraints constraints, int[] unitUsed, int cell, int digit) {
        int bit = 1 << (digit - SudokuRules.VALUE_MIN);
        for (int unit : constraints.rules().unitsOf(cell)) {
            unitUsed[unit] |= bit;
        }
        board[cell] = digit;
    }

    static void unassign(int[] board, SudokuConstraints constraints, int[] unitUsed, int cell, int digit) {
        int bit = 1 << (digit - SudokuRules.VALUE_MIN);
        board[cell] = 0;
        for (int unit : constraints.rules().unitsOf(cell)) {
            unitUsed[unit] &= ~bit;
        }
    }

    /** 放置后这只笼的和仍然可能成立。 */
    private static boolean placementKeepsCage(SudokuConstraints constraints, int[] board, int[] unitUsed, int cell) {
        int cage = constraints.cageOf()[cell];
        return cage == 0 || cageFeasible(constraints, board, unitUsed, cage);
    }

    /** 笼和剪枝：已填和加剩余格最小/最大候选和须覆盖目标值，笼满时必须恰好相等。 */
    private static boolean cageFeasible(SudokuConstraints constraints, int[] board, int[] unitUsed, int cage) {
        int[] totals = constraints.cageTotals();
        if (cage < 1 || cage > totals.length) return true;
        int total = totals[cage - 1];
        int sum = 0;
        int min = 0;
        int max = 0;
        int remaining = 0;
        int[] cageOf = constraints.cageOf();
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            if (cageOf[cell] != cage) continue;
            int value = board[cell];
            if (value != 0) {
                sum += value;
                continue;
            }
            remaining++;
            int candidates = candidates(constraints, board, unitUsed, cell);
            if (candidates == 0) return false;
            min += Integer.numberOfTrailingZeros(candidates) + SudokuRules.VALUE_MIN;
            max += 31 - Integer.numberOfLeadingZeros(candidates) + SudokuRules.VALUE_MIN;
        }
        if (remaining == 0) return sum == total;
        return sum + min <= total && total <= sum + max;
    }

    private static boolean allCagesFeasible(SudokuConstraints constraints, int[] board, int[] unitUsed) {
        int[] totals = constraints.cageTotals();
        for (int cage = 1; cage <= totals.length; cage++) {
            if (!cageFeasible(constraints, board, unitUsed, cage)) return false;
        }
        return true;
    }

    // ==================== 填盘与数解 ====================

    /**
     * 随机回溯填满整盘，只用于出题求完整解。先做显性/隐性唯一数传播，再按最小候选分支回落；
     * 超过节点预算即放弃，由调用方换区域重试。
     */
    static boolean fillBoard(int[] board, SudokuConstraints constraints, RandomSource random) {
        var state = new FillState(constraints, random);
        if (!state.search()) return false;
        System.arraycopy(state.board, 0, board, 0, SudokuRules.CELLS);
        return true;
    }

    /** 单次填盘的节点上限；耗尽后与其继续搜，不如换随机顺序或布局重试。 */
    private static final int FILL_NODE_BUDGET = 20_000;

    /** 带传播的填盘状态：所有回溯改动都通过 trail 撤销。 */
    private static final class FillState {

        private final SudokuConstraints constraints;
        private final RandomSource random;
        private final int[] board = new int[SudokuRules.CELLS];
        private final int[] unitUsed;
        private final int[] candidates = new int[SudokuRules.CELLS];
        private final int[] trailCell = new int[SudokuRules.CELLS];
        private final int[] trailDigit = new int[SudokuRules.CELLS];
        private int trailSize;
        private int nodes;
        private boolean aborted;

        private FillState(SudokuConstraints constraints, RandomSource random) {
            this.constraints = constraints;
            this.random = random;
            this.unitUsed = new int[constraints.rules().unitCount()];
            refreshCandidates();
        }

        private int candidatesAt(int cell) {
            int used = 0;
            for (int unit : constraints.rules().unitsOf(cell)) {
                used |= unitUsed[unit];
            }
            return SudokuRules.ALL_VALUES & ~used;
        }

        private void refreshCandidates() {
            for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
                candidates[cell] = board[cell] == 0 ? candidatesAt(cell) : 0;
            }
        }

        private void assignWithTrail(int cell, int digit) {
            assign(board, constraints, unitUsed, cell, digit);
            trailCell[trailSize] = cell;
            trailDigit[trailSize] = digit;
            trailSize++;
            refreshCandidates();
        }

        private void undoTo(int mark) {
            while (trailSize > mark) {
                trailSize--;
                unassign(board, constraints, unitUsed, trailCell[trailSize], trailDigit[trailSize]);
            }
            refreshCandidates();
        }

        /** 反复落显性唯一数与隐性唯一数，直到没有新的单数为止。 */
        private boolean propagate() {
            while (true) {
                if (++nodes > FILL_NODE_BUDGET) {
                    aborted = true;
                    return false;
                }
                boolean progress = false;
                for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
                    if (board[cell] != 0) continue;
                    int mask = candidates[cell];
                    if (mask == 0) return false;
                    if (Integer.bitCount(mask) != 1) continue;
                    int digit = Integer.numberOfTrailingZeros(mask) + SudokuRules.VALUE_MIN;
                    assignWithTrail(cell, digit);
                    progress = true;
                }
                if (progress) continue;
                outer:
                for (int unitIndex = 0; unitIndex < constraints.rules().unitCount(); unitIndex++) {
                    int[] unit = constraints.rules().units()[unitIndex];
                    for (int digit = SudokuRules.VALUE_MIN; digit <= SudokuRules.VALUE_MAX; digit++) {
                        int bit = 1 << (digit - SudokuRules.VALUE_MIN);
                        int only = -1;
                        int count = 0;
                        for (int index = 0; index < unit.length; index++) {
                            int cell = unit[index];
                            if (board[cell] == 0 && (candidates[cell] & bit) != 0) {
                                count++;
                                only = cell;
                                if (count > 1) break;
                            }
                        }
                        if (count != 1) continue;
                        assignWithTrail(only, digit);
                        progress = true;
                        continue outer;
                    }
                }
                if (!progress) return true;
            }
        }

        private boolean search() {
            if (aborted) return false;
            if (++nodes > FILL_NODE_BUDGET) {
                aborted = true;
                return false;
            }
            if (!propagate()) return false;
            int bestCell = -1;
            int bestCandidates = 0;
            int bestCount = SudokuRules.VALUE_MAX + 1;
            for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
                if (board[cell] != 0) continue;
                int mask = candidates[cell];
                if (mask == 0) return false;
                int count = Integer.bitCount(mask);
                if (count < bestCount) {
                    bestCell = cell;
                    bestCandidates = mask;
                    bestCount = count;
                    if (count == 1) break;
                }
            }
            if (bestCell < 0) return true;
            int mark = trailSize;
            int start = random.nextInt(SudokuRules.VALUE_MAX);
            for (int offset = 0; offset < SudokuRules.VALUE_MAX; offset++) {
                int digit = (start + offset) % SudokuRules.VALUE_MAX + SudokuRules.VALUE_MIN;
                int bit = 1 << (digit - SudokuRules.VALUE_MIN);
                if ((bestCandidates & bit) == 0) continue;
                assignWithTrail(bestCell, digit);
                if (search()) return true;
                undoTo(mark);
                if (aborted) return false;
            }
            return false;
        }
    }

    /**
     * 数解，最多数到 {@code limit}。每层挑候选最少的空格，候选为 0 或笼和不可行立刻回溯。
     * 只在出题挖空做唯一性校验时调用，属冷路径。
     */
    static int countSolutions(int[] puzzle, SudokuConstraints constraints, int limit) {
        int[] board = puzzle.clone();
        int[] unitUsed = new int[constraints.rules().unitCount()];
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            int digit = board[cell];
            if (digit != 0) assign(board, constraints, unitUsed, cell, digit);
        }
        if (!allCagesFeasible(constraints, board, unitUsed)) return 0;
        return count(board, constraints, unitUsed, limit, 0);
    }

    private static int count(int[] board, SudokuConstraints constraints, int[] unitUsed, int limit, int found) {
        int bestCell = -1;
        int bestCandidates = 0;
        int bestCount = SudokuRules.VALUE_MAX + 1;
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            if (board[cell] != 0) continue;
            int candidates = candidates(constraints, board, unitUsed, cell);
            int count = Integer.bitCount(candidates);
            if (count == 0) return found;
            if (count < bestCount) {
                bestCell = cell;
                bestCandidates = candidates;
                bestCount = count;
                if (count == 1) break;
            }
        }
        if (bestCell < 0) return found + 1;
        for (int digit = SudokuRules.VALUE_MIN; digit <= SudokuRules.VALUE_MAX && found < limit; digit++) {
            int bit = 1 << (digit - SudokuRules.VALUE_MIN);
            if ((bestCandidates & bit) == 0) continue;
            assign(board, constraints, unitUsed, bestCell, digit);
            if (placementKeepsCage(constraints, board, unitUsed, bestCell)) {
                found = count(board, constraints, unitUsed, limit, found);
            }
            unassign(board, constraints, unitUsed, bestCell, digit);
        }
        return found;
    }

    // ==================== 难度评级 ====================

    /**
     * 逻辑难度评级（0~5），只看推理强度不看提示数：
     *
     * <ul>
     * <li>0/1：只用唯一数类技巧即可填满，0 级要求隐性唯一数不超过 {@link SudokuRules#SINGLES_EASY_LIMIT} 次；</li>
     * <li>2~5：单数推进后卡住，按试探求解的尝试次数（≤3/≤7/≤20）与最大深度（≤2/≤4/≤7）分档，超出为 5。</li>
     * </ul>
     */
    public static int rate(int[] puzzle, SudokuConstraints constraints) {
        SinglesResult singles = solveSingles(puzzle, constraints);
        if (singles.filled() == SudokuRules.CELLS) {
            return singles.hiddenSingles() <= SudokuRules.SINGLES_EASY_LIMIT ? 0 : 1;
        }
        GuessStats stats = solveByGuessing(singles.board(), constraints);
        if (stats.attempts() <= 3 && stats.depth() <= 2) return 2;
        if (stats.attempts() <= 7 && stats.depth() <= 4) return 3;
        if (stats.attempts() <= 20 && stats.depth() <= 7) return 4;
        return 5;
    }

    /** 单数推进结果：已填格数、隐性唯一数次数，以及卡住时的盘面。 */
    private record SinglesResult(int filled, int hiddenSingles, int[] board) {}

    /** 试探求解的统计：分支候选的尝试次数与最大嵌套深度。 */
    private record GuessStats(int attempts, int depth) {}

    /**
     * 反复应用显性/隐性唯一数直到填满或卡住；笼和不可行的单数不落子，交给试探求解器。
     */
    private static SinglesResult solveSingles(int[] puzzle, SudokuConstraints constraints) {
        int[] board = puzzle.clone();
        int[] unitUsed = new int[constraints.rules().unitCount()];
        int[] candidates = new int[SudokuRules.CELLS];
        int filled = 0;
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            int digit = board[cell];
            if (digit == 0) continue;
            assign(board, constraints, unitUsed, cell, digit);
            filled++;
        }
        refreshCandidates(constraints, board, unitUsed, candidates);
        int hiddenSingles = 0;
        boolean progress = true;
        while (progress) {
            progress = false;
            for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
                if (board[cell] != 0) continue;
                int candidatesHere = candidates[cell];
                if (candidatesHere == 0) return new SinglesResult(filled, hiddenSingles, board);
                if (Integer.bitCount(candidatesHere) != 1) continue;
                int digit = Integer.numberOfTrailingZeros(candidatesHere) + SudokuRules.VALUE_MIN;
                assign(board, constraints, unitUsed, cell, digit);
                if (!placementKeepsCage(constraints, board, unitUsed, cell)) {
                    unassign(board, constraints, unitUsed, cell, digit);
                    return new SinglesResult(filled, hiddenSingles, board);
                }
                filled++;
                progress = true;
                refreshCandidates(constraints, board, unitUsed, candidates);
            }
            if (progress) continue;
            outer:
            for (int unitIndex = 0; unitIndex < constraints.rules().unitCount(); unitIndex++) {
                int[] unit = constraints.rules().units()[unitIndex];
                for (int digit = SudokuRules.VALUE_MIN; digit <= SudokuRules.VALUE_MAX; digit++) {
                    int bit = 1 << (digit - SudokuRules.VALUE_MIN);
                    int only = -1;
                    int count = 0;
                    for (int index = 0; index < unit.length; index++) {
                        int cell = unit[index];
                        if (board[cell] == 0 && (candidates[cell] & bit) != 0) {
                            count++;
                            only = cell;
                            if (count > 1) break;
                        }
                    }
                    if (count != 1) continue;
                    assign(board, constraints, unitUsed, only, digit);
                    if (!placementKeepsCage(constraints, board, unitUsed, only)) {
                        unassign(board, constraints, unitUsed, only, digit);
                        return new SinglesResult(filled, hiddenSingles, board);
                    }
                    hiddenSingles++;
                    filled++;
                    progress = true;
                    refreshCandidates(constraints, board, unitUsed, candidates);
                    continue outer;
                }
            }
        }
        return new SinglesResult(filled, hiddenSingles, board);
    }

    private static void refreshCandidates(SudokuConstraints constraints, int[] board, int[] unitUsed,
                                          int[] candidates) {
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            candidates[cell] = board[cell] == 0 ? candidates(constraints, board, unitUsed, cell) : 0;
        }
    }

    /** 从单数推进卡住的局面继续试探，统计分支尝试次数与最大深度。 */
    private static GuessStats solveByGuessing(int[] board, SudokuConstraints constraints) {
        int[] work = board.clone();
        int[] unitUsed = new int[constraints.rules().unitCount()];
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            int digit = work[cell];
            if (digit != 0) assign(work, constraints, unitUsed, cell, digit);
        }
        int[] counters = new int[2];
        guess(work, constraints, unitUsed, 0, counters);
        return new GuessStats(counters[0], counters[1]);
    }

    /**
     * 最小候选优先的试探求解；{@code counters[0]} 累计分支尝试次数，{@code counters[1]} 记最大分支深度。
     * 找到一份解就返回，不数其它解。
     */
    private static boolean guess(int[] board, SudokuConstraints constraints, int[] unitUsed, int depth,
                                 int[] counters) {
        int bestCell = -1;
        int bestCandidates = 0;
        int bestCount = SudokuRules.VALUE_MAX + 1;
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            if (board[cell] != 0) continue;
            int candidates = candidates(constraints, board, unitUsed, cell);
            int count = Integer.bitCount(candidates);
            if (count == 0) return false;
            if (count < bestCount) {
                bestCell = cell;
                bestCandidates = candidates;
                bestCount = count;
                if (count == 1) break;
            }
        }
        if (bestCell < 0) return true;
        int nextDepth = bestCount > 1 ? depth + 1 : depth;
        if (bestCount > 1 && nextDepth > counters[1]) counters[1] = nextDepth;
        for (int digit = SudokuRules.VALUE_MIN; digit <= SudokuRules.VALUE_MAX; digit++) {
            int bit = 1 << (digit - SudokuRules.VALUE_MIN);
            if ((bestCandidates & bit) == 0) continue;
            if (bestCount > 1) counters[0]++;
            assign(board, constraints, unitUsed, bestCell, digit);
            if (placementKeepsCage(constraints, board, unitUsed, bestCell) &&
                    guess(board, constraints, unitUsed, nextDepth, counters)) {
                return true;
            }
            unassign(board, constraints, unitUsed, bestCell, digit);
        }
        return false;
    }

    // ==================== 完成判定 ====================

    /**
     * 盘面是否完成：每格都有数（提示或玩家填写），且满足全部约束——全异单元不重复、连续标记两侧相差 1、
     * 每个杀手笼的和等于角标。只认「填满 + 合法」，不要求唯一解：杀手同一组笼和可能有多份合法解。
     * 提示格以提示数字为准，与界面显示一致。
     */
    public static boolean isComplete(SudokuConstraints constraints, int[] givens, int[] values) {
        if (givens.length != SudokuRules.CELLS || values.length != SudokuRules.CELLS) return false;
        int[] digits = new int[SudokuRules.CELLS];
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            int digit = givens[cell] != 0 ? givens[cell] : values[cell];
            if (digit < SudokuRules.VALUE_MIN || digit > SudokuRules.VALUE_MAX) return false;
            digits[cell] = digit;
        }
        int[] unitUsed = new int[constraints.rules().unitCount()];
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            int bit = 1 << (digits[cell] - SudokuRules.VALUE_MIN);
            for (int unit : constraints.rules().unitsOf(cell)) {
                if ((unitUsed[unit] & bit) != 0) return false;
                unitUsed[unit] |= bit;
            }
        }
        int[] marks = constraints.marks();
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            int mark = marks[cell];
            if (mark == 0) continue;
            // 标记数据两侧都会写，这里仍按四个方位全查，避免漏掉不对称标记
            for (int direction : SudokuConstraints.ALL_DIRECTIONS_BITS) {
                if ((mark & direction) == 0) continue;
                int neighbor = SudokuConstraints.neighbor(cell, direction);
                if (neighbor < 0) continue;
                if (Math.abs(digits[cell] - digits[neighbor]) != 1) return false;
            }
        }
        int[] cageOf = constraints.cageOf();
        int[] totals = constraints.cageTotals();
        if (totals.length > 0) {
            int[] sums = new int[totals.length + 1];
            for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
                int cage = cageOf[cell];
                // 每格都必须属于一个笼，否则笼和不完整
                if (cage < 1 || cage > totals.length) return false;
                sums[cage] += digits[cell];
            }
            for (int cage = 1; cage <= totals.length; cage++) {
                if (sums[cage] != totals[cage - 1]) return false;
            }
        }
        return true;
    }
}

package com.gtocore.common.machine.noenergy.game.sudoku;

import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 数独的规则与出题：标准 9×9 盘面，每行、每列、每个 3×3 宫恰好填 1~9 一次。
 *
 * <p>
 * 出题先用随机回溯填出一份完整解，再随机抽一个 {@link #MIN_CLUES}~{@link #MAX_CLUES} 的提示数并挖去格子；每挖一格
 * 都检查「解是否仍然唯一」，不唯一就放回去。难度完全由 {@link #rate(int[])} 的逻辑评级决定，提示数只作为随机范围。
 * 因此玩家填满且行列宫都合法时，答案必然就是那份唯一的解，不需要再和标准答案逐个比对。
 *
 * <p>
 * 纯逻辑，不持有机器状态；位掩码只在小整数上运算，临时容器按实例复用。只在服务端线程调用，不要求线程安全。
 */
public final class SudokuRules {

    public static final int SIZE = 9;
    public static final int BOX = 3;
    public static final int CELLS = SIZE * SIZE;
    public static final int VALUE_MIN = 1;
    public static final int VALUE_MAX = 9;
    /** 1~9 全在的低九位。 */
    public static final int ALL_VALUES = (1 << VALUE_MAX) - 1;
    /** 每局提示数的随机范围（含两端），与难度档位无关；行、列、宫都可以出现空置。 */
    public static final int MIN_CLUES = 24;
    public static final int MAX_CLUES = 28;
    /** 单数可解的题里，隐性唯一数不超过这个次数算 0 级（扫描与少量隐性唯一数即可完成）。 */
    private static final int SINGLES_EASY_LIMIT = 8;

    /**
     * 六档难度完全由 {@link SudokuRules#rate(int[])} 的逻辑评级决定，提示数只在
     * {@link #MIN_CLUES}~{@link #MAX_CLUES} 内随机抽取并微调，不参与分档。
     *
     * <ul>
     * <li>{@link #targetRate()}：这一档要求的逻辑评级（0~5）；</li>
     * <li>{@link #attempts()}：最多重试几次，用来命中评级；</li>
     * <li>{@link #ratingLevel()}：界面上显示的 1~6 级。</li>
     * </ul>
     *
     * 奖励按档位固定，与这一局实际抽到多少提示无关。
     */
    public enum Difficulty {

        BEGINNER(2, 0, 32),
        EASY(5, 1, 16),
        NORMAL(9, 2, 48),
        HARD(16, 3, 64),
        EXPERT(25, 4, 48),
        MASTER(40, 5, 48);

        public static final Difficulty[] ALL = values();

        private final long reward;
        private final int targetRate;
        private final int attempts;

        Difficulty(long reward, int targetRate, int attempts) {
            this.reward = reward;
            this.targetRate = targetRate;
            this.attempts = attempts;
        }

        /** 完成一局发多少游戏币。 */
        public long reward() {
            return reward;
        }

        /** 这一档要求的逻辑评级（0~5）。 */
        public int targetRate() {
            return targetRate;
        }

        /** 界面上显示的 1~6 级。 */
        public int ratingLevel() {
            return targetRate + 1;
        }

        /** 生成一局最多尝试几次，用来命中逻辑评级。 */
        public int attempts() {
            return attempts;
        }

        /** 距目标评级差多少：0 表示命中，数值越小越接近。 */
        int rateDistance(int rate) {
            return Math.abs(rate - targetRate);
        }

        /** 按序数取档位，越界返回 {@code null}（存档与网络来的值一律校验）。 */
        public static @Nullable Difficulty of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    /** 判别完成时复用的掩码（每个机器一份实例，服务端单线程使用）。 */
    private final int[] rowUsed = new int[SIZE];
    private final int[] columnUsed = new int[SIZE];
    private final int[] boxUsed = new int[SIZE];

    private SudokuRules() {}

    /** 机器持有一份实例，临时容器跟着它复用。 */
    public static SudokuRules create() {
        return new SudokuRules();
    }

    /** 第 {@code cell} 格的宫序号（行、列各 0~8）。 */
    public static int boxOf(int cell) {
        return cell / SIZE / BOX * BOX + cell % SIZE / BOX;
    }

    /**
     * 出一局新题：返回 {@link #CELLS} 格的提示盘，0 表示空格。
     *
     * <p>
     * 每一局先从 {@link #MIN_CLUES}~{@link #MAX_CLUES} 随机抽一个提示数，再按唯一解挖格，最后只用逻辑评级筛选；
     * 没命中时在随机范围内朝更难/更简单方向移动提示数再试，最多尝试 {@link Difficulty#attempts()} 次，
     * 仍未命中则返回最接近的一局。
     *
     * @param difficulty 目标逻辑评级所属的档位
     */
    public int[] createPuzzle(RandomSource random, Difficulty difficulty) {
        int targetRate = difficulty.targetRate();
        int clueTarget = MIN_CLUES + random.nextInt(MAX_CLUES - MIN_CLUES + 1);
        int[] best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int attempt = 0; attempt < difficulty.attempts(); attempt++) {
            int[] puzzle = createCandidate(random, clueTarget);
            int rate = rate(puzzle);
            int distance = Math.abs(rate - targetRate);
            if (distance < bestDistance) {
                best = puzzle;
                bestDistance = distance;
            }
            if (distance == 0) return puzzle;
            // 难度不够就往更少提示再试，太难就往更多提示再试；提示数始终留在随机范围内
            if (rate < targetRate && clueTarget > MIN_CLUES) {
                clueTarget--;
            } else if (rate > targetRate && clueTarget < MAX_CLUES) {
                clueTarget++;
            }
        }
        return best == null ? createCandidate(random, clueTarget) : best;
    }

    /** 挖一局候选题：随机挖格，直到提示数达到目标；每挖一格都只要求解仍唯一，允许出现空行、空列或空宫。 */
    private int[] createCandidate(RandomSource random, int clueTarget) {
        int[] board = new int[CELLS];
        int[] rows = new int[SIZE];
        int[] columns = new int[SIZE];
        int[] boxes = new int[SIZE];
        fillBoard(board, rows, columns, boxes, 0, random);
        int[] puzzle = board.clone();
        int[] order = shuffledCells(random);
        int remaining = CELLS;
        for (int index = 0; index < order.length && remaining > clueTarget; index++) {
            int cell = order[index];
            int value = puzzle[cell];
            puzzle[cell] = 0;
            if (countSolutions(puzzle, 2) == 1) {
                remaining--;
            } else {
                puzzle[cell] = value;
            }
        }
        return puzzle;
    }

    /** 按行优先的格号随机打乱。 */
    private static int[] shuffledCells(RandomSource random) {
        int[] order = new int[CELLS];
        for (int cell = 0; cell < CELLS; cell++) {
            order[cell] = cell;
        }
        for (int index = CELLS - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            int value = order[index];
            order[index] = order[swap];
            order[swap] = value;
        }
        return order;
    }

    /** 随机回溯填满整盘；顺序按格号走，候选数字起点随机。 */
    private static boolean fillBoard(int[] board, int[] rows, int[] columns, int[] boxes, int cell,
                                     RandomSource random) {
        if (cell == CELLS) return true;
        int row = cell / SIZE;
        int column = cell % SIZE;
        int box = boxOf(cell);
        int free = ALL_VALUES & ~(rows[row] | columns[column] | boxes[box]);
        if (free == 0) return false;
        int start = random.nextInt(VALUE_MAX);
        for (int offset = 0; offset < VALUE_MAX; offset++) {
            int digit = (start + offset) % VALUE_MAX + VALUE_MIN;
            int bit = 1 << (digit - VALUE_MIN);
            if ((free & bit) == 0) continue;
            board[cell] = digit;
            rows[row] |= bit;
            columns[column] |= bit;
            boxes[box] |= bit;
            if (fillBoard(board, rows, columns, boxes, cell + 1, random)) return true;
            board[cell] = 0;
            rows[row] &= ~bit;
            columns[column] &= ~bit;
            boxes[box] &= ~bit;
        }
        return false;
    }

    /**
     * 数一数这盘有几种解，最多数到 {@code limit} 种。
     *
     * <p>
     * 每层挑候选最少的空格（MRV），候选为 0 立刻回溯；填满即记一种解。只在出题时调用，属于冷路径。
     */
    private static int countSolutions(int[] puzzle, int limit) {
        int[] board = puzzle.clone();
        int[] rows = new int[SIZE];
        int[] columns = new int[SIZE];
        int[] boxes = new int[SIZE];
        for (int cell = 0; cell < CELLS; cell++) {
            int digit = board[cell];
            if (digit == 0) continue;
            int bit = 1 << (digit - VALUE_MIN);
            rows[cell / SIZE] |= bit;
            columns[cell % SIZE] |= bit;
            boxes[boxOf(cell)] |= bit;
        }
        return count(board, rows, columns, boxes, limit, 0);
    }

    private static int count(int[] board, int[] rows, int[] columns, int[] boxes, int limit, int found) {
        int bestCell = -1;
        int bestCandidates = 0;
        int bestCount = VALUE_MAX + 1;
        for (int cell = 0; cell < CELLS; cell++) {
            if (board[cell] != 0) continue;
            int row = cell / SIZE;
            int column = cell % SIZE;
            int box = boxOf(cell);
            int candidates = ALL_VALUES & ~(rows[row] | columns[column] | boxes[box]);
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
        int row = bestCell / SIZE;
        int column = bestCell % SIZE;
        int box = boxOf(bestCell);
        for (int digit = VALUE_MIN; digit <= VALUE_MAX && found < limit; digit++) {
            int bit = 1 << (digit - VALUE_MIN);
            if ((bestCandidates & bit) == 0) continue;
            board[bestCell] = digit;
            rows[row] |= bit;
            columns[column] |= bit;
            boxes[box] |= bit;
            found = count(board, rows, columns, boxes, limit, found);
            board[bestCell] = 0;
            rows[row] &= ~bit;
            columns[column] &= ~bit;
            boxes[box] &= ~bit;
        }
        return found;
    }

    // ==================== 难度评级 ====================

    /**
     * 逻辑难度评级（0~5），只看「必须动用的推理强度」而不看提示数：
     *
     * <ul>
     * <li>0：只靠唯一数类技巧就能填满，且隐性唯一数不超过 {@link #SINGLES_EASY_LIMIT} 次；</li>
     * <li>1：同样只靠唯一数类技巧，但需要的隐性唯一数更多；</li>
     * <li>2~5：单数推进后会卡住，按最小候选优先求解时的分支尝试次数与最大嵌套深度递增。
     * </li>
     * </ul>
     *
     * 单数推进用严格的候选数传播；卡住后交给最小候选优先的试探求解器。
     */
    public int rate(int[] puzzle) {
        SinglesResult singles = solveSingles(puzzle);
        if (singles.filled() == CELLS) return singles.hiddenSingles() <= SINGLES_EASY_LIMIT ? 0 : 1;
        GuessStats stats = solveByGuessing(singles.board());
        if (stats.attempts() <= 3 && stats.depth() <= 2) return 2;
        if (stats.attempts() <= 7 && stats.depth() <= 4) return 3;
        if (stats.attempts() <= 20 && stats.depth() <= 7) return 4;
        return 5;
    }

    /** 单数推进结果：填了多少格、用了多少次隐性唯一数，以及卡住时的盘面（交给试探求解器继续）。 */
    private record SinglesResult(int filled, int hiddenSingles, int[] board) {}

    /** 试探求解的统计：分支候选的尝试次数与最大嵌套深度。 */
    private record GuessStats(int attempts, int depth) {}

    /**
     * 反复应用显性唯一数与隐性唯一数，直到填满或卡住；返回填了多少格与卡住时的盘面。
     * 候选数用位掩码维护，放置一个数字时同步清掉同行、同列、同宫的该候选。
     */
    private static SinglesResult solveSingles(int[] puzzle) {
        int[] board = puzzle.clone();
        int[] candidates = new int[CELLS];
        int[] rows = new int[SIZE];
        int[] columns = new int[SIZE];
        int[] boxes = new int[SIZE];
        int filled = 0;
        for (int cell = 0; cell < CELLS; cell++) {
            int digit = board[cell];
            if (digit == 0) continue;
            int bit = 1 << (digit - VALUE_MIN);
            rows[cell / SIZE] |= bit;
            columns[cell % SIZE] |= bit;
            boxes[boxOf(cell)] |= bit;
            filled++;
        }
        for (int cell = 0; cell < CELLS; cell++) {
            if (board[cell] != 0) continue;
            candidates[cell] = ALL_VALUES & ~(rows[cell / SIZE] | columns[cell % SIZE] | boxes[boxOf(cell)]);
        }
        int hiddenSingles = 0;
        boolean progress = true;
        while (progress) {
            progress = false;
            for (int cell = 0; cell < CELLS; cell++) {
                if (board[cell] != 0) continue;
                int candidatesHere = candidates[cell];
                if (candidatesHere == 0) return new SinglesResult(filled, hiddenSingles, board);
                if (Integer.bitCount(candidatesHere) != 1) continue;
                int digit = Integer.numberOfTrailingZeros(candidatesHere) + VALUE_MIN;
                placeDigit(board, candidates, rows, columns, boxes, cell, digit);
                filled++;
                progress = true;
            }
            if (progress) continue;
            outer:
            for (int digit = VALUE_MIN; digit <= VALUE_MAX; digit++) {
                int bit = 1 << (digit - VALUE_MIN);
                for (int unit = 0; unit < SIZE; unit++) {
                    int only = -1;
                    int count = 0;
                    for (int index = 0; index < SIZE; index++) {
                        int cell = unit * SIZE + index;
                        if (board[cell] == 0 && (candidates[cell] & bit) != 0) {
                            count++;
                            only = cell;
                            if (count > 1) break;
                        }
                    }
                    if (count == 1) {
                        hiddenSingles++;
                        placeDigit(board, candidates, rows, columns, boxes, only, digit);
                        filled++;
                        progress = true;
                        continue outer;
                    }
                    count = 0;
                    only = -1;
                    for (int index = 0; index < SIZE; index++) {
                        int cell = index * SIZE + unit;
                        if (board[cell] == 0 && (candidates[cell] & bit) != 0) {
                            count++;
                            only = cell;
                            if (count > 1) break;
                        }
                    }
                    if (count == 1) {
                        hiddenSingles++;
                        placeDigit(board, candidates, rows, columns, boxes, only, digit);
                        filled++;
                        progress = true;
                        continue outer;
                    }
                    int startRow = unit / BOX * BOX;
                    int startColumn = unit % BOX * BOX;
                    count = 0;
                    only = -1;
                    for (int row = 0; row < BOX; row++) {
                        for (int column = 0; column < BOX; column++) {
                            int cell = (startRow + row) * SIZE + startColumn + column;
                            if (board[cell] == 0 && (candidates[cell] & bit) != 0) {
                                count++;
                                only = cell;
                                if (count > 1) break;
                            }
                        }
                        if (count > 1) break;
                    }
                    if (count == 1) {
                        hiddenSingles++;
                        placeDigit(board, candidates, rows, columns, boxes, only, digit);
                        filled++;
                        progress = true;
                        continue outer;
                    }
                }
            }
        }
        return new SinglesResult(filled, hiddenSingles, board);
    }

    /** 放置一个数字：写盘面、清本格候选，并清掉同行、同列、同宫其它格子的该候选。 */
    private static void placeDigit(int[] board, int[] candidates, int[] rows, int[] columns, int[] boxes,
                                   int cell, int digit) {
        int bit = 1 << (digit - VALUE_MIN);
        int row = cell / SIZE;
        int column = cell % SIZE;
        int box = boxOf(cell);
        board[cell] = digit;
        candidates[cell] = 0;
        rows[row] |= bit;
        columns[column] |= bit;
        boxes[box] |= bit;
        int startRow = row / BOX * BOX;
        int startColumn = column / BOX * BOX;
        for (int peerRow = startRow; peerRow < startRow + BOX; peerRow++) {
            for (int peerColumn = startColumn; peerColumn < startColumn + BOX; peerColumn++) {
                int peer = peerRow * SIZE + peerColumn;
                if (peer != cell) candidates[peer] &= ~bit;
            }
        }
        for (int peerColumn = 0; peerColumn < SIZE; peerColumn++) {
            int peer = row * SIZE + peerColumn;
            if (peer != cell) candidates[peer] &= ~bit;
        }
        for (int peerRow = 0; peerRow < SIZE; peerRow++) {
            int peer = peerRow * SIZE + column;
            if (peer != cell) candidates[peer] &= ~bit;
        }
    }

    /** 从单数推进卡住的局面继续试探，统计分支尝试次数与最大深度。 */
    private static GuessStats solveByGuessing(int[] board) {
        int[] work = board.clone();
        int[] rows = new int[SIZE];
        int[] columns = new int[SIZE];
        int[] boxes = new int[SIZE];
        for (int cell = 0; cell < CELLS; cell++) {
            int digit = work[cell];
            if (digit == 0) continue;
            int bit = 1 << (digit - VALUE_MIN);
            rows[cell / SIZE] |= bit;
            columns[cell % SIZE] |= bit;
            boxes[boxOf(cell)] |= bit;
        }
        int[] counters = new int[2];
        guess(work, rows, columns, boxes, 0, counters);
        return new GuessStats(counters[0], counters[1]);
    }

    /**
     * 最小候选优先的试探求解；{@code counters[0]} 累计分支候选的尝试次数，{@code counters[1]} 记最大分支深度。
     * 找到一份解就立刻返回，不再数其它解（出题阶段已经保证唯一）。
     */
    private static boolean guess(int[] board, int[] rows, int[] columns, int[] boxes, int depth, int[] counters) {
        int bestCell = -1;
        int bestCandidates = 0;
        int bestCount = VALUE_MAX + 1;
        for (int cell = 0; cell < CELLS; cell++) {
            if (board[cell] != 0) continue;
            int candidates = ALL_VALUES & ~(rows[cell / SIZE] | columns[cell % SIZE] | boxes[boxOf(cell)]);
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
        int row = bestCell / SIZE;
        int column = bestCell % SIZE;
        int box = boxOf(bestCell);
        for (int digit = VALUE_MIN; digit <= VALUE_MAX; digit++) {
            int bit = 1 << (digit - VALUE_MIN);
            if ((bestCandidates & bit) == 0) continue;
            if (bestCount > 1) counters[0]++;
            board[bestCell] = digit;
            rows[row] |= bit;
            columns[column] |= bit;
            boxes[box] |= bit;
            if (guess(board, rows, columns, boxes, nextDepth, counters)) return true;
            board[bestCell] = 0;
            rows[row] &= ~bit;
            columns[column] &= ~bit;
            boxes[box] &= ~bit;
        }
        return false;
    }

    /**
     * 盘面是否已经填满并且合法：每格都有数（提示或玩家填写），每行、每列、每宫都没有重复。
     * 出题保证唯一解，所以合法即等于答案。
     */
    public boolean isComplete(int[] givens, int[] values) {
        if (givens.length != CELLS || values.length != CELLS) return false;
        Arrays.fill(rowUsed, 0);
        Arrays.fill(columnUsed, 0);
        Arrays.fill(boxUsed, 0);
        for (int cell = 0; cell < CELLS; cell++) {
            int digit = values[cell] != 0 ? values[cell] : givens[cell];
            if (digit < VALUE_MIN || digit > VALUE_MAX) return false;
            int bit = 1 << (digit - VALUE_MIN);
            int row = cell / SIZE;
            int column = cell % SIZE;
            int box = boxOf(cell);
            if ((rowUsed[row] & bit) != 0 || (columnUsed[column] & bit) != 0 || (boxUsed[box] & bit) != 0) {
                return false;
            }
            rowUsed[row] |= bit;
            columnUsed[column] |= bit;
            boxUsed[box] |= bit;
        }
        return true;
    }

    /** 格子最终显示的数字：提示优先，其次玩家填写，空为 0。数组长度异常时按空处理。 */
    public static int displayedValue(int[] givens, int[] values, int cell) {
        if (cell < 0 || cell >= CELLS) return 0;
        int given = cell < givens.length ? givens[cell] : 0;
        if (given != 0) return given;
        return cell < values.length ? values[cell] : 0;
    }
}

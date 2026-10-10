package com.gtocore.common.machine.noenergy.justGet10;

import net.minecraft.util.RandomSource;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 「合成十」的盘面规则：{@link #SIZE}×{@link #SIZE} 方格，每格一个 {@link #VALUE_MIN}~{@link #VALUE_MAX} 的整数，按行
 * 优先存进 {@link #CELL_COUNT} 长的 {@code int[]}（第 0 行在最上面）。点一格把与它四向连通、数字相同的一整组合并成
 * 「该数字 + 1」，合并后整列下坠、顶部补随机数字。
 *
 * <p>
 * 纯逻辑，不持有机器状态；只在机器所在的服务端线程调用，不要求线程安全，临时容器按实例复用。
 */
public final class JustGet10Rules {

    /** 盘面边长。改动会让旧档长度对不上，读档时按新尺寸截断或补 1。 */
    public static final int SIZE = 5;
    public static final int CELL_COUNT = SIZE * SIZE;
    public static final int VALUE_MIN = 1;
    /** 合并上限：已经到顶的组只能清掉、不再升级。 */
    public static final int VALUE_MAX = 20;
    public static final int SCORE_MAX = Integer.MAX_VALUE;
    /** 空格标记，只在合并中途占位。 */
    public static final int EMPTY = -1;

    /** 难度区间：分数到 DIFFICULTY_START 之前新格等概率，到 DIFFICULTY_END 完全按高难度权重。 */
    private static final int DIFFICULTY_START = 500;
    private static final int DIFFICULTY_END = 4000;
    /** 补格数字的权重（下标 0 对应数字 1）：低难度等概率，高难度偏大数字但四种都会出。 */
    private static final double[] EASY_VALUE_WEIGHTS = { 1, 1, 1, 1 };
    private static final double[] HARD_VALUE_WEIGHTS = { 0.2, 0.6, 1, 2.2 };

    /** 四向邻居的行/列偏移。 */
    private static final int[] NEIGHBOR_ROW = { -1, 1, 0, 0 };
    private static final int[] NEIGHBOR_COL = { 0, 0, -1, 1 };

    /** {@link #groupAt} 的上一次结果，下一次调用前有效；只有一个调用方，直接复用。 */
    private final IntArrayList group = new IntArrayList(CELL_COUNT);
    private final boolean[] visited = new boolean[CELL_COUNT];
    private final int[] queue = new int[CELL_COUNT];
    /** 下坠时按列暂存数字。 */
    private final int[] columnScratch = new int[SIZE];

    private JustGet10Rules() {}

    /** 机器持有一份实例，临时容器跟着它复用。 */
    public static JustGet10Rules create() {
        return new JustGet10Rules();
    }

    /** 开局：整盘按 0 分难度补满随机数字。 */
    public static int[] createBoard(RandomSource random) {
        int[] board = new int[CELL_COUNT];
        for (int cell = 0; cell < CELL_COUNT; cell++) {
            board[cell] = randomValue(random, 0);
        }
        return board;
    }

    /** 存档里的盘面：长度不对（旧档或损坏）时按 1 补齐、多余的截断。 */
    public static int[] normalizeBoard(int @Nullable [] board) {
        if (board == null) return createBoard(RandomSource.create(0));
        if (board.length == CELL_COUNT) return board;
        int[] normalized = new int[CELL_COUNT];
        for (int cell = 0; cell < CELL_COUNT; cell++) {
            normalized[cell] = cell < board.length ? Math.clamp(board[cell], VALUE_MIN, VALUE_MAX) : VALUE_MIN;
        }
        return normalized;
    }

    /**
     * {@code (row, col)} 所在、四向连通且数字相同的整组；不是连通组（越界、空格、只有自己一格）时返回 {@code null}。
     *
     * <p>
     * 返回的列表在下一次调用本方法前有效，调用方不必判空遍历。
     */
    public @Nullable IntArrayList groupAt(int[] board, int row, int col) {
        group.clear();
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return null;
        int value = board[row * SIZE + col];
        if (value < VALUE_MIN) return null;
        Arrays.fill(visited, false);
        int head = 0;
        int tail = 0;
        int start = row * SIZE + col;
        queue[tail++] = start;
        visited[start] = true;
        while (head < tail) {
            int cell = queue[head++];
            group.add(cell);
            int cellRow = cell / SIZE;
            int cellCol = cell % SIZE;
            for (int direction = 0; direction < NEIGHBOR_ROW.length; direction++) {
                int nextRow = cellRow + NEIGHBOR_ROW[direction];
                int nextCol = cellCol + NEIGHBOR_COL[direction];
                if (nextRow < 0 || nextRow >= SIZE || nextCol < 0 || nextCol >= SIZE) continue;
                int nextCell = nextRow * SIZE + nextCol;
                if (visited[nextCell] || board[nextCell] != value) continue;
                visited[nextCell] = true;
                queue[tail++] = nextCell;
            }
        }
        return group.size() < 2 ? null : group;
    }

    /**
     * 合并 {@code group}：整组清空、在被点中的那一格写入「原数字 + 1」，然后逐列下坠补格。
     *
     * <p>
     * 落点就是玩家点的格子，下坠后跟着该列重力到最低空位。每次都新建数组，机器才能靠字段变化看出「盘面变了」；
     * 返回 {@code null} 表示这一组不该合并。
     */
    public @Nullable Merge merge(int[] board, IntArrayList group, int row, int col, RandomSource random, int score) {
        if (group.isEmpty()) return null;
        int[] merged = board.clone();
        int cell = row * SIZE + col;
        int mergedValue = Math.min(VALUE_MAX, board[cell] + 1);
        for (int index = 0; index < group.size(); index++) {
            merged[group.getInt(index)] = EMPTY;
        }
        merged[cell] = mergedValue;
        collapse(merged, random, score);
        return new Merge(merged, mergedValue);
    }

    /** 逐列重力：该列数字按原顺序压到底，空出的顶部补随机数字。 */
    private void collapse(int[] board, RandomSource random, int score) {
        for (int col = 0; col < SIZE; col++) {
            int count = 0;
            for (int row = 0; row < SIZE; row++) {
                int cell = row * SIZE + col;
                if (board[cell] >= VALUE_MIN) columnScratch[count++] = board[cell];
            }
            for (int row = SIZE - 1, index = count - 1; row >= 0; row--, index--) {
                board[row * SIZE + col] = index >= 0 ? columnScratch[index] : randomValue(random, score);
            }
        }
    }

    /**
     * 一次合并的结果。
     *
     * @param board       下坠补格之后的新盘面
     * @param mergedValue 本次合并出的数字（下坠前写在点击位置上的值）
     */
    public record Merge(int[] board, int mergedValue) {}

    /** 盘面无任何相邻同数字：本局到此为止。 */
    public static boolean isGameOver(int[] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int cell = row * SIZE + col;
                int value = board[cell];
                if (value < VALUE_MIN) continue;
                if (col + 1 < SIZE && board[cell + 1] == value) return false;
                if (row + 1 < SIZE && board[cell + SIZE] == value) return false;
            }
        }
        return true;
    }

    /** 盘面里最大的数字。 */
    public static int maxValue(int[] board) {
        int max = VALUE_MIN;
        for (int value : board) {
            if (value > max) max = value;
        }
        return max;
    }

    /**
     * 补格用的新数字：按难度在两组权重间插值，再按权重抽 1~4。
     *
     * <p>
     * 权重现算两遍（求和、抽签各一遍）而不落静态暂存数组：{@link #EASY_VALUE_WEIGHTS} 与 {@link #HARD_VALUE_WEIGHTS}
     * 是只读常量，一次插值只是几个乘加，没必要为它维护一份所有规则实例共享的可写数组。
     */
    private static int randomValue(RandomSource random, int score) {
        double difficulty = difficultyAt(score);
        double total = 0;
        for (int value = 0; value < EASY_VALUE_WEIGHTS.length; value++) {
            total += valueWeight(value, difficulty);
        }
        double roll = random.nextDouble() * total;
        for (int value = 0; value < EASY_VALUE_WEIGHTS.length; value++) {
            roll -= valueWeight(value, difficulty);
            if (roll < 0) return VALUE_MIN + value;
        }
        return VALUE_MIN;
    }

    /** 数字 {@code VALUE_MIN + value} 在 {@code difficulty} 难度下的补格权重。 */
    private static double valueWeight(int value, double difficulty) {
        return EASY_VALUE_WEIGHTS[value] + (HARD_VALUE_WEIGHTS[value] - EASY_VALUE_WEIGHTS[value]) * difficulty;
    }

    private static double difficultyAt(int score) {
        if (score <= DIFFICULTY_START) return 0.0;
        if (score >= DIFFICULTY_END) return 1.0;
        return (score - DIFFICULTY_START) / (double) (DIFFICULTY_END - DIFFICULTY_START);
    }
}

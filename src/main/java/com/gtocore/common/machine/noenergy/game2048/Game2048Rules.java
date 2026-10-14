package com.gtocore.common.machine.noenergy.game2048;

import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.Nullable;

/**
 * 「2048」的盘面规则：{@link #SIZE}×{@link #SIZE} 方格，每格是 {@link #EMPTY}（0）或 2 的幂，按行优先存进
 * {@link #CELL_COUNT} 长的 {@code int[]}（第 0 行在最上面）。
 *
 * <p>
 * 一次移动把整盘数字朝该方向推到底，相邻同值两两合并（同一回合里每个数字只参与一次合并）；只要有格子挪动或合并，
 * 就在随机一个空格补上新数字（九成是 {@link #VALUE_MIN}、一成是它的两倍）。推不动的方向返回 {@code null}：不补新数字，
 * 机器也就不改任何状态。
 *
 * <p>
 * 纯逻辑，不持有机器状态；只在机器所在的服务端线程调用，不要求线程安全，临时容器按实例复用。
 */
public final class Game2048Rules {

    /** 盘面边长。 */
    public static final int SIZE = 4;
    public static final int CELL_COUNT = SIZE * SIZE;
    /** 空格。 */
    public static final int EMPTY = 0;
    /** 补格与合并出的最小数字；{@link #VALUE_MAX} 是合并上限，两格到顶的数字合起来仍是它自己。 */
    public static final int VALUE_MIN = 2;
    public static final int VALUE_MAX = 65536;
    public static final int SCORE_MAX = Integer.MAX_VALUE;

    /** 新格出现 {@link #VALUE_MIN} 两倍数字的概率，其余出 {@link #VALUE_MIN}。 */
    private static final double SPAWN_DOUBLE_CHANCE = 0.1;

    /** 移动方向；{@link #rotations()} 是左旋几次后该方向就变成「往左推」，与合并逻辑一致。 */
    public enum Direction {

        LEFT,
        UP,
        RIGHT,
        DOWN;

        private static final Direction[] ALL = values();

        int rotations() {
            return ordinal();
        }

        /** 按序数取方向，网络来的下标越界时返回 {@code null}。 */
        public static @Nullable Direction of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    /**
     * 一次移动的结果。
     *
     * @param board    补上新数字之后的盘面；每次都是新数组，机器靠字段换了引用来确认盘面变了
     * @param gained   本次合并得到的分数（合并出的数字之和）
     * @param gameOver 新盘面是否已经推不动了
     */
    public record Move(int[] board, int gained, boolean gameOver) {}

    /** 左旋后的工作盘面，合并就地写回这里。 */
    private final int[] working = new int[CELL_COUNT];
    /** 单次 90° 左旋的中转盘面。 */
    private final int[] rotated = new int[CELL_COUNT];
    /** 一行去掉空格后的数字，最多 {@link #SIZE} 个。 */
    private final int[] filtered = new int[SIZE];
    /** 一行合并后的结果。 */
    private final int[] line = new int[SIZE];

    private Game2048Rules() {}

    /** 机器持有一份实例，临时容器跟着它复用。 */
    public static Game2048Rules create() {
        return new Game2048Rules();
    }

    /** 开局：空盘上先补两个随机数字。 */
    public static int[] createBoard(RandomSource random) {
        int[] board = new int[CELL_COUNT];
        spawn(board, random);
        spawn(board, random);
        return board;
    }

    /**
     * 存档里的盘面：长度不对（旧档或损坏）时重建成空盘，非法数字（既不是 {@link #EMPTY}、也不是
     * {@link #VALUE_MIN}~{@link #VALUE_MAX} 的 2 的幂）当空格。空盘由机器当成「还没开这一局」，不是可玩的局面。
     */
    public static int[] normalizeBoard(int @Nullable [] board) {
        if (board == null || board.length != CELL_COUNT) return new int[CELL_COUNT];
        for (int cell = 0; cell < CELL_COUNT; cell++) {
            if (!isTileValue(board[cell])) board[cell] = EMPTY;
        }
        return board;
    }

    /** 是否是盘面上合法的数字：{@link #EMPTY} 或 {@link #VALUE_MIN}~{@link #VALUE_MAX} 的 2 的幂。 */
    public static boolean isTileValue(int value) {
        return value == EMPTY || value >= VALUE_MIN && value <= VALUE_MAX && (value & (value - 1)) == 0;
    }

    /** 盘面上有没有数字；空盘说明这一局还没开始。 */
    public static boolean isEmpty(int[] board) {
        for (int value : board) {
            if (value != EMPTY) return false;
        }
        return true;
    }

    /**
     * 朝 {@code direction} 推一次；返回 {@code null} 表示这一下推不动，机器什么都不用改。
     *
     * <p>
     * 先把整盘左旋成「往左推」，逐行合并后再旋回来，这样四个方向共用同一套合并逻辑。
     */
    public @Nullable Move move(int[] board, Direction direction, RandomSource random) {
        rotateInto(board, direction.rotations());
        boolean moved = false;
        int gained = 0;
        for (int row = 0; row < SIZE; row++) {
            int base = row * SIZE;
            int count = 0;
            for (int col = 0; col < SIZE; col++) {
                int value = working[base + col];
                if (value != EMPTY) filtered[count++] = value;
            }
            int index = 0;
            for (int i = 0; i < count; i++) {
                if (i + 1 < count && filtered[i] == filtered[i + 1]) {
                    int value = Math.min(VALUE_MAX, filtered[i] * 2);
                    line[index++] = value;
                    gained += value;
                    i++;
                } else {
                    line[index++] = filtered[i];
                }
            }
            while (index < SIZE) line[index++] = EMPTY;
            for (int col = 0; col < SIZE; col++) {
                if (working[base + col] != line[col]) moved = true;
                working[base + col] = line[col];
            }
        }
        if (!moved) return null;
        rotateInto(working, (SIZE - direction.rotations()) % SIZE);
        int[] result = working.clone();
        spawn(result, random);
        return new Move(result, gained, isGameOver(result));
    }

    /** 把 {@code board} 左旋 {@code times} 次写进 {@link #working}；{@code board} 就是它自己时只做旋转。 */
    private void rotateInto(int[] board, int times) {
        if (board != working) System.arraycopy(board, 0, working, 0, CELL_COUNT);
        for (int pass = 0; pass < times; pass++) {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    rotated[(SIZE - 1 - col) * SIZE + row] = working[row * SIZE + col];
                }
            }
            System.arraycopy(rotated, 0, working, 0, CELL_COUNT);
        }
    }

    /** 在随机一个空格上补新数字：九成 {@link #VALUE_MIN}、一成它的两倍；没有空格就什么也不做。 */
    public static void spawn(int[] board, RandomSource random) {
        int emptyCount = 0;
        for (int value : board) {
            if (value == EMPTY) emptyCount++;
        }
        if (emptyCount == 0) return;
        int target = random.nextInt(emptyCount);
        for (int cell = 0; cell < CELL_COUNT; cell++) {
            if (board[cell] != EMPTY) continue;
            if (target == 0) {
                board[cell] = random.nextDouble() < SPAWN_DOUBLE_CHANCE ? VALUE_MIN * 2 : VALUE_MIN;
                return;
            }
            target--;
        }
    }

    /** 盘面没有空格、也没有相邻同数字：这一局到此为止。 */
    public static boolean isGameOver(int[] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int cell = row * SIZE + col;
                int value = board[cell];
                if (value == EMPTY) return false;
                if (col + 1 < SIZE && board[cell + 1] == value) return false;
                if (row + 1 < SIZE && board[cell + SIZE] == value) return false;
            }
        }
        return true;
    }

    /** 盘面里最大的数字，空盘为 {@link #EMPTY}。 */
    public static int maxValue(int[] board) {
        int max = EMPTY;
        for (int value : board) {
            if (value > max) max = value;
        }
        return max;
    }
}

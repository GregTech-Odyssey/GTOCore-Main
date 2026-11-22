package com.gtocore.common.machine.noenergy.game.slidey;

import net.minecraft.util.RandomSource;

import java.util.Arrays;

/**
 * 土耳其方块规则：9×10 盘面，每一块是 1~4 格的横向条状方块，只能在本行内左右滑动，
 * 整块一起下落，填满整行消除。
 *
 * <p>
 * 盘面用定长 {@code int[]} 存方块列表，每块占 {@link #STRIDE} 个位置：
 * 行、起始列、长度、颜色。相邻的两块即使贴在一起也是两块，不会合并；
 * 颜色只影响外观，消除与颜色无关。纯逻辑，不持有机器状态，只在服务端线程调用。
 *
 * <p>
 * 涨行规则参照 Turkey Blocks 原版：新行留 {@link #EMPTY_MIN}~{@link #EMPTY_MAX} 个空格，
 * 方块长度按难度权重表抽取，难度每涨 {@link #RAMP_EVERY} 行提升一档，越到后面长条越多、空格越少。
 */
public final class SlideyRules {

    /** 盘面宽（格）。 */
    public static final int COLS = 9;
    /** 盘面高（格）。 */
    public static final int ROWS = 10;
    /** 方块列表的容量。 */
    public static final int MAX_BLOCKS = 64;

    /** 每块在 {@code int[]} 里的字段数与字段下标。 */
    public static final int STRIDE = 4;
    public static final int FIELD_ROW = 0;
    public static final int FIELD_COL = 1;
    public static final int FIELD_LENGTH = 2;
    public static final int FIELD_COLOR = 3;

    /** 方块颜色档数；格子值 1~COLOR_COUNT 有方块。 */
    public static final int COLOR_COUNT = 6;
    /** 单块最长格数。 */
    public static final int MAX_BLOCK_LENGTH = 4;
    /** 新行至少留的空格数。 */
    public static final int EMPTY_MIN = 2;
    /** 新行最多留的空格数。 */
    public static final int EMPTY_MAX = 5;
    /** 每涨多少行难度提升一档。 */
    public static final int RAMP_EVERY = 20;
    /** 场上至少有这么多行有方块，不足就从底部补行。 */
    public static final int MIN_ROWS = 2;
    /** 难度对空格数的倾斜：档位越高，新行越偏向空格更少。 */
    private static final double EMPTY_TILT = 0.3;
    /** 两段空格同时出现的概率（空格足够多时）。 */
    private static final float DOUBLE_GAP_CHANCE = 0.35F;

    /** 各难度档的 [1 格, 2 格, 3 格, 4 格] 权重；超出档数沿用最后一档。 */
    private static final int[][] LEVEL_WEIGHTS = {
            { 30, 30, 15, 10 },
            { 25, 30, 20, 15 },
            { 20, 25, 25, 20 },
            { 15, 25, 30, 25 },
            { 10, 20, 35, 30 },
            { 5, 20, 40, 35 } };

    private SlideyRules() {}

    public static int blockRow(int[] blocks, int index) {
        return blocks[index * STRIDE + FIELD_ROW];
    }

    public static int blockColumn(int[] blocks, int index) {
        return blocks[index * STRIDE + FIELD_COL];
    }

    public static int blockLength(int[] blocks, int index) {
        return blocks[index * STRIDE + FIELD_LENGTH];
    }

    public static int blockColor(int[] blocks, int index) {
        return blocks[index * STRIDE + FIELD_COLOR];
    }

    /** 某一格被哪一块占着；空格返回 -1。 */
    public static int blockAt(int[] blocks, int count, int row, int column) {
        for (int index = 0; index < count; index++) {
            int base = index * STRIDE;
            if (blocks[base + FIELD_ROW] != row) continue;
            int start = blocks[base + FIELD_COL];
            if (column >= start && column < start + blocks[base + FIELD_LENGTH]) return index;
        }
        return -1;
    }

    /** 某一格是否被占；{@code ignore} 是忽略的方块下标（判断自身移动时用）。 */
    public static boolean occupied(int[] blocks, int count, int row, int column, int ignore) {
        for (int index = 0; index < count; index++) {
            if (index == ignore) continue;
            int base = index * STRIDE;
            if (blocks[base + FIELD_ROW] != row) continue;
            int start = blocks[base + FIELD_COL];
            if (column >= start && column < start + blocks[base + FIELD_LENGTH]) return true;
        }
        return false;
    }

    /** 这一块最多还能往某个方向滑几格：一路都不能碰到别的方块，也不能出界。 */
    public static int moveRoom(int[] blocks, int count, int index, int direction) {
        int row = blockRow(blocks, index);
        int start = blockColumn(blocks, index);
        int length = blockLength(blocks, index);
        int room = 0;
        while (true) {
            int target = start + direction * (room + 1);
            if (target < 0 || target + length > COLS) return room;
            int edge = direction > 0 ? target + length - 1 : target;
            if (occupied(blocks, count, row, edge, index)) return room;
            room++;
        }
    }

    /** 这一块能不能横移 {@code delta} 格。 */
    public static boolean canMove(int[] blocks, int count, int index, int delta) {
        if (delta == 0) return false;
        return delta < 0 ? moveRoom(blocks, count, index, -1) >= -delta :
                moveRoom(blocks, count, index, 1) >= delta;
    }

    /** 横移一块；调用前先确认 {@link #canMove}。 */
    public static void move(int[] blocks, int index, int delta) {
        blocks[index * STRIDE + FIELD_COL] += delta;
    }

    /** 这一块能不能整块下落一格：下面一行整段都要空着。 */
    public static boolean canFall(int[] blocks, int count, int index) {
        int row = blockRow(blocks, index) + 1;
        if (row >= ROWS) return false;
        int start = blockColumn(blocks, index);
        int length = blockLength(blocks, index);
        for (int column = start; column < start + length; column++) {
            if (occupied(blocks, count, row, column, index)) return false;
        }
        return true;
    }

    /** 还有方块能继续下落。 */
    public static boolean hasFloating(int[] blocks, int count) {
        for (int index = 0; index < count; index++) {
            if (canFall(blocks, count, index)) return true;
        }
        return false;
    }

    /**
     * 重力走一步：能整块下落的方块各下落一格；返回这一步有没有方块移动。
     * 自底向上处理，同一 tick 内一列上的方块整体下移一格。
     */
    public static boolean stepGravity(int[] blocks, int count) {
        boolean moved = false;
        for (int row = ROWS - 2; row >= 0; row--) {
            for (int index = 0; index < count; index++) {
                if (blockRow(blocks, index) != row) continue;
                if (!canFall(blocks, count, index)) continue;
                blocks[index * STRIDE + FIELD_ROW] = row + 1;
                moved = true;
            }
        }
        return moved;
    }

    /** 某一行是不是被方块完全填满。 */
    public static boolean rowFull(int[] blocks, int count, int row) {
        for (int column = 0; column < COLS; column++) {
            if (!occupied(blocks, count, row, column, -1)) return false;
        }
        return true;
    }

    /** 有方块的行数（判断是否需要补行）。 */
    public static int occupiedRows(int[] blocks, int count) {
        int mask = 0;
        for (int index = 0; index < count; index++) {
            mask |= 1 << blockRow(blocks, index);
        }
        return Integer.bitCount(mask);
    }

    /**
     * 消掉所有满行：满行上的方块整块移除，上方方块整体下移。
     *
     * @return {@code {新的方块数, 消掉的行数}}
     */
    public static int[] clearFullRows(int[] blocks, int count) {
        int cleared = 0;
        boolean again = true;
        while (again) {
            again = false;
            for (int row = ROWS - 1; row >= 0; row--) {
                if (!rowFull(blocks, count, row)) continue;
                count = removeRowAndShift(blocks, count, row);
                cleared++;
                again = true;
                break;
            }
        }
        return new int[] { count, cleared };
    }

    /** 移除某一行的全部方块，其余上方的方块下移一格；返回新的方块数。 */
    private static int removeRowAndShift(int[] blocks, int count, int row) {
        int write = 0;
        for (int index = 0; index < count; index++) {
            int base = index * STRIDE;
            int blockRow = blocks[base + FIELD_ROW];
            if (blockRow == row) continue;
            if (write != index) {
                System.arraycopy(blocks, base, blocks, write * STRIDE, STRIDE);
            }
            if (blockRow < row) {
                blocks[write * STRIDE + FIELD_ROW] = blockRow + 1;
            }
            write++;
        }
        return write;
    }

    /**
     * 底部补一行、其余整体上移一格；最上一行有方块就放不下，返回 -1。
     * 调用方拿到 -1 时应当结束对局。
     *
     * @param tideCount 已经涨过多少行，决定这一行的难度档位
     * @return 新的方块数
     */
    public static int insertGeneratedRow(int[] blocks, int count, RandomSource random, int tideCount) {
        for (int index = 0; index < count; index++) {
            if (blockRow(blocks, index) == 0) return -1;
        }
        for (int index = 0; index < count; index++) {
            blocks[index * STRIDE + FIELD_ROW]--;
        }
        return appendGeneratedRow(blocks, count, ROWS - 1, random, tideCount);
    }

    /** 有没有任意一块还能左右滑动（判断死局）。 */
    public static boolean hasAnyMove(int[] blocks, int count) {
        for (int index = 0; index < count; index++) {
            if (moveRoom(blocks, count, index, -1) > 0) return true;
            if (moveRoom(blocks, count, index, 1) > 0) return true;
        }
        return false;
    }

    /** 难度档位：每涨 {@link #RAMP_EVERY} 行提升一档。 */
    public static int level(int tideCount) {
        return Math.max(0, tideCount) / RAMP_EVERY;
    }

    /**
     * 开局盘面：底行按正常规则生成；倒数第二行每条方块都要压住底行至少一格，
     * 这样整块刚体落不下去，开局不会出现悬空。返回方块数。
     */
    public static int fillStartRows(int[] blocks, RandomSource random) {
        Arrays.fill(blocks, 0);
        int count = appendGeneratedRow(blocks, 0, ROWS - 1, random, 0);

        boolean[] support = new boolean[COLS];
        int supportCount = 0;
        for (int index = 0; index < count; index++) {
            int start = blockColumn(blocks, index);
            for (int column = start; column < start + blockLength(blocks, index); column++) {
                if (support[column]) continue;
                support[column] = true;
                supportCount++;
            }
        }

        // 方块数多于支撑列时一定摆不下，直接换一组长度再试
        boolean[] used = new boolean[COLS];
        for (int attempt = 0; attempt < 200; attempt++) {
            int fill = COLS - pickEmpty(random, 0);
            int[] lens = makeLens(random, fill, 0);
            if (lens.length > supportCount) continue;
            int[] starts = new int[lens.length];
            Arrays.fill(used, false);
            if (!placeOverSupport(lens, 0, support, used, starts, random)) continue;
            int previousColor = 0;
            for (int index = 0; index < lens.length; index++) {
                int color = pickColor(random, previousColor);
                count = appendBlock(blocks, count, ROWS - 2, starts[index], lens[index], color);
                previousColor = color;
            }
            return count;
        }
        return appendFallbackUpper(blocks, count, support, random);
    }

    /** 回溯摆放：方块互不重叠，且每条至少压住一个支撑列。 */
    private static boolean placeOverSupport(int[] lens, int index, boolean[] support, boolean[] used, int[] starts,
                                            RandomSource random) {
        if (index >= lens.length) return true;
        int length = lens[index];
        int maxStart = COLS - length;
        int[] order = new int[maxStart + 1];
        for (int start = 0; start <= maxStart; start++) {
            order[start] = start;
        }
        for (int slot = order.length - 1; slot > 0; slot--) {
            int other = random.nextInt(slot + 1);
            int temp = order[slot];
            order[slot] = order[other];
            order[other] = temp;
        }
        for (int candidate : order) {
            boolean free = true;
            boolean supported = false;
            for (int column = candidate; column < candidate + length; column++) {
                if (used[column]) {
                    free = false;
                    break;
                }
                if (support[column]) supported = true;
            }
            if (!free || !supported) continue;
            for (int column = candidate; column < candidate + length; column++) {
                used[column] = true;
            }
            starts[index] = candidate;
            if (placeOverSupport(lens, index + 1, support, used, starts, random)) return true;
            for (int column = candidate; column < candidate + length; column++) {
                used[column] = false;
            }
        }
        return false;
    }

    /** 兜底：从支撑列里随机取几列，连成尽量长的条直接铺在倒数第二行上。 */
    private static int appendFallbackUpper(int[] blocks, int count, boolean[] support, RandomSource random) {
        int[] columns = new int[COLS];
        int size = 0;
        for (int column = 0; column < COLS; column++) {
            if (support[column]) columns[size++] = column;
        }
        if (size == 0) return count;
        for (int slot = size - 1; slot > 0; slot--) {
            int other = random.nextInt(slot + 1);
            int temp = columns[slot];
            columns[slot] = columns[other];
            columns[other] = temp;
        }
        int lo = Math.min(3, size);
        int hi = Math.min(7, size);
        int take = lo + random.nextInt(hi - lo + 1);
        Arrays.sort(columns, 0, take);

        int previousColor = 0;
        int index = 0;
        while (index < take) {
            int runStart = index;
            while (index + 1 < take && columns[index + 1] == columns[index] + 1) {
                index++;
            }
            int runLength = index - runStart + 1;
            int offset = 0;
            while (offset < runLength) {
                int length = Math.min(MAX_BLOCK_LENGTH, runLength - offset);
                int color = pickColor(random, previousColor);
                count = appendBlock(blocks, count, ROWS - 2, columns[runStart + offset], length, color);
                previousColor = color;
                offset += length;
            }
            index++;
        }
        return count;
    }

    /** 随机一个颜色，避免与前一块相同。 */
    private static int pickColor(RandomSource random, int previousColor) {
        int color = 1 + random.nextInt(COLOR_COUNT);
        return color == previousColor ? color % COLOR_COUNT + 1 : color;
    }

    /**
     * 生成一整行并追加到方块列表：空格数 {@link #EMPTY_MIN}~{@link #EMPTY_MAX}，
     * 方块长度按难度权重抽取，空格聚成 1~2 段。
     *
     * @return 新的方块数
     */
    public static int appendGeneratedRow(int[] blocks, int count, int row, RandomSource random, int tideCount) {
        int level = level(tideCount);
        int empty = pickEmpty(random, level);
        int fill = COLS - empty;
        int[] lens = makeLens(random, fill, level);

        // 相邻方块之间至少要能放下空格：块数太多就合并相邻两块（合并后仍不超过最长格数）
        int blockTotal = lens.length;
        while (blockTotal - 1 > empty) {
            boolean merged = false;
            for (int index = 0; index < blockTotal - 1; index++) {
                if (lens[index] + lens[index + 1] > MAX_BLOCK_LENGTH) continue;
                lens[index] += lens[index + 1];
                System.arraycopy(lens, index + 2, lens, index + 1, blockTotal - index - 2);
                blockTotal--;
                merged = true;
                break;
            }
            if (!merged) break;
        }

        // 空格聚成 1~2 段，随机放在方块之间的槽位里
        int[] gaps = new int[blockTotal + 1];
        int[] slots = new int[blockTotal + 1];
        for (int index = 0; index <= blockTotal; index++) {
            slots[index] = index;
        }
        for (int index = slots.length - 1; index > 0; index--) {
            int other = random.nextInt(index + 1);
            int temp = slots[index];
            slots[index] = slots[other];
            slots[other] = temp;
        }
        int remaining = empty;
        int runs = remaining >= 4 && random.nextFloat() < DOUBLE_GAP_CHANCE ? 2 : 1;
        if (runs > slots.length) runs = slots.length;
        for (int run = 0; run < runs; run++) {
            int take = run == runs - 1 ? remaining : 1 + random.nextInt(Math.max(1, remaining - (runs - 1 - run)));
            gaps[slots[run]] = take;
            remaining -= take;
        }
        if (remaining > 0) gaps[slots[0]] += remaining;

        int column = gaps[0];
        int previousColor = 0;
        for (int index = 0; index < blockTotal; index++) {
            int length = lens[index];
            if (column + length > COLS) break;
            int color = pickColor(random, previousColor);
            count = appendBlock(blocks, count, row, column, length, color);
            previousColor = color;
            column += length + gaps[index + 1];
        }
        return count;
    }

    /** 新行空格数：范围固定，难度越高越偏向空格更少的一端。 */
    private static int pickEmpty(RandomSource random, int level) {
        int total = 0;
        double[] weights = new double[EMPTY_MAX - EMPTY_MIN + 1];
        for (int empty = EMPTY_MIN; empty <= EMPTY_MAX; empty++) {
            weights[empty - EMPTY_MIN] = 1 + level * (EMPTY_MAX - empty) * EMPTY_TILT;
            total += weights[empty - EMPTY_MIN];
        }
        double roll = random.nextDouble() * total;
        for (int index = 0; index < weights.length; index++) {
            roll -= weights[index];
            if (roll <= 0) return EMPTY_MIN + index;
        }
        return EMPTY_MAX;
    }

    /** 按难度权重把 {@code fill} 格切成若干条，总长正好为 {@code fill}。 */
    private static int[] makeLens(RandomSource random, int fill, int level) {
        int[] weights = LEVEL_WEIGHTS[Math.min(level, LEVEL_WEIGHTS.length - 1)];
        int[] lens = new int[fill];
        int size = 0;
        int sum = 0;
        int guard = 0;
        while (sum < fill && guard++ < 60) {
            int length = pickLength(random, fill - sum, weights);
            if (length <= 0) break;
            lens[size++] = length;
            sum += length;
        }
        if (sum < fill && size < lens.length) lens[size++] = fill - sum;
        return Arrays.copyOf(lens, size);
    }

    /** 在剩余空间内按权重抽一个块长。 */
    private static int pickLength(RandomSource random, int room, int[] weights) {
        int limit = Math.min(room, MAX_BLOCK_LENGTH);
        int total = 0;
        for (int length = 1; length <= limit; length++) {
            total += weights[length - 1];
        }
        if (total <= 0) return Math.min(room, 1);
        int roll = random.nextInt(total);
        for (int length = 1; length <= limit; length++) {
            roll -= weights[length - 1];
            if (roll < 0) return length;
        }
        return 1;
    }

    /** 往列表末尾追加一块；容量不够就丢弃。 */
    private static int appendBlock(int[] blocks, int count, int row, int column, int length, int color) {
        if (count >= MAX_BLOCKS) return count;
        int base = count * STRIDE;
        blocks[base + FIELD_ROW] = row;
        blocks[base + FIELD_COL] = column;
        blocks[base + FIELD_LENGTH] = length;
        blocks[base + FIELD_COLOR] = color;
        return count + 1;
    }
}

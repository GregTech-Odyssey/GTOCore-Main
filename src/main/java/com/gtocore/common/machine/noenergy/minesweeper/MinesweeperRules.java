package com.gtocore.common.machine.noenergy.minesweeper;

import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.Nullable;

/**
 * 「扫雷」的盘面规则：{@link Difficulty} 档位的 {@code width × height} 格，每格是未翻开、翻开后的空/数字 1~8、插旗或雷。
 *
 * <p>
 * 只算规则、不持盘面：雷盘（{@code boolean[]}）与显示盘（{@code int[]}）由调用方持有并按行优先存（第 0 行在最上面），
 * 格子取值见 {@link #UNOPENED} 等常量——未翻开 0、翻开空 -1、翻开数字 1~8、插旗 -2、雷 -3、踩中的雷 -4。顺带的状态
 * （旗数、找到几颗雷、是否结束）都从数组现推，调用方不必另存计数。
 *
 * <p>
 * 首点安全：第一下翻开的格子连同它周围的 3×3 都保证不是雷，雷盘在第一次翻开时才布下（{@link #open} 内部处理）。
 * 连锁翻开用可复用的 int 队列，布雷用可复用的位置表，都不额外分配。
 *
 * <p>
 * 纯逻辑；只在服务端线程调用，不要求线程安全，临时容器按实例复用。实例按档位建（尺寸相关的临时容器跟着走）。
 */
public final class MinesweeperRules {

    /** 未翻开。 */
    public static final int UNOPENED = 0;
    /** 翻开后是空格（周围没有雷）。 */
    public static final int OPEN_EMPTY = -1;
    /** 插旗。 */
    public static final int FLAGGED = -2;
    /** 结束时亮出来的雷（不是踩中的那颗）。 */
    public static final int BOMB = -3;
    /** 踩中的那颗雷。 */
    public static final int DEATH_BOMB = -4;
    /** 数字格就是 1~8 本身。 */
    public static final int NUMBER_MIN = 1;

    /** 八向邻居的个数：配合 {@link #neighbour} 用。 */
    public static final int DIRECTIONS = 8;

    /** 八向邻居的行/列偏移。 */
    private static final int[] NEIGHBOR_COL = { -1, 0, 1, -1, 1, -1, 0, 1 };
    private static final int[] NEIGHBOR_ROW = { -1, -1, -1, 0, 0, 1, 1, 1 };

    /**
     * 难度档位：盘面尺寸、雷数、全找到时的满额奖励（越难给得越多）。尺寸与雷数照搬参考实现的四个预设（不含自定义档），
     * 雷密度逐档变高：12.3% / 15.6% / 20.6% / 23.4%。
     */
    public enum Difficulty {

        EASY(9, 9, 10, 3),
        NORMAL(16, 16, 40, 8),
        HARD(16, 30, 99, 20),
        EXPERT(30, 64, 450, 50);

        public static final Difficulty[] ALL = values();

        private final int width;
        private final int height;
        private final int mineCount;
        private final long maxReward;

        Difficulty(int width, int height, int mineCount, long maxReward) {
            this.width = width;
            this.height = height;
            this.mineCount = mineCount;
            this.maxReward = maxReward;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public int cellCount() {
            return width * height;
        }

        public int mineCount() {
            return mineCount;
        }

        /** 全部找到（或整局通关）时给多少游戏币。 */
        public long maxReward() {
            return maxReward;
        }

        /** 按序数取档位，越界返回 {@code null}（存档与网络来的值一律校验）。 */
        public static @Nullable Difficulty of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    private final Difficulty difficulty;
    private final int width;
    private final int height;
    private final int cellCount;
    private final int mineCount;

    /** 连锁翻开的队列。 */
    private final int[] queue;
    /** 布雷时打乱用的位置表。 */
    private final int[] positions;

    private MinesweeperRules(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.width = difficulty.width();
        this.height = difficulty.height();
        this.cellCount = difficulty.cellCount();
        this.mineCount = difficulty.mineCount();
        this.queue = new int[cellCount];
        this.positions = new int[cellCount];
    }

    /** 调用方按档位持有一份实例；换档位时重建，临时容器跟着盘面尺寸分配。 */
    public static MinesweeperRules create(Difficulty difficulty) {
        return new MinesweeperRules(difficulty);
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public int cellCount() {
        return cellCount;
    }

    public int mineCount() {
        return mineCount;
    }

    /** 一片空雷盘。 */
    public boolean[] newMines() {
        return new boolean[cellCount];
    }

    /** 一整片未翻开的显示盘。 */
    public int[] newCells() {
        return new int[cellCount];
    }

    // ==================== 落子 ====================

    /**
     * 翻开一格：已翻开或插了旗的格子不动；第一次翻开时先布雷（避开该格周围 3×3）。
     *
     * @return 盘面是否真的变了
     */
    public boolean open(boolean[] mines, int[] cells, int cell, RandomSource random) {
        if (cell < 0 || cell >= cellCount) return false;
        if (cells[cell] != UNOPENED) return false;
        if (!isPlaced(mines)) placeMines(mines, cell, random);
        if (mines[cell]) {
            revealMines(mines, cells, cell);
            return true;
        }
        floodOpen(mines, cells, cell);
        if (isWon(mines, cells)) flagAllMines(mines, cells);
        return true;
    }

    /**
     * 插旗/拔旗：旗子最多插到雷数那么多（插满就不能再插，免得靠满屏旗子碰运气）。
     *
     * @return 盘面是否真的变了
     */
    public boolean toggleFlag(int[] cells, int cell) {
        if (cell < 0 || cell >= cellCount) return false;
        if (cells[cell] == FLAGGED) {
            cells[cell] = UNOPENED;
            return true;
        }
        if (cells[cell] != UNOPENED || flags(cells) >= mineCount) return false;
        cells[cell] = FLAGGED;
        return true;
    }

    /** 雷盘是否已经布好：只要有一颗雷就当作布好了。 */
    public boolean isPlaced(boolean[] mines) {
        for (boolean mine : mines) {
            if (mine) return true;
        }
        return false;
    }

    // ==================== 局面查询 ====================

    /** 已插旗数。 */
    public int flags(int[] cells) {
        int count = 0;
        for (int state : cells) {
            if (state == FLAGGED) count++;
        }
        return count;
    }

    /** 插对的旗（插在雷上的旗）数，也就是「找到的雷」。 */
    public int foundMines(boolean[] mines, int[] cells) {
        int count = 0;
        for (int cell = 0; cell < cellCount; cell++) {
            if (mines[cell] && cells[cell] == FLAGGED) count++;
        }
        return count;
    }

    /** 踩雷了。 */
    public boolean isLost(int[] cells) {
        for (int state : cells) {
            if (state == DEATH_BOMB) return true;
        }
        return false;
    }

    /** 所有非雷格都翻开了（插错旗的格子不算翻开）。 */
    public boolean isWon(boolean[] mines, int[] cells) {
        for (int cell = 0; cell < cellCount; cell++) {
            if (mines[cell]) continue;
            int state = cells[cell];
            if (state != OPEN_EMPTY && state < NUMBER_MIN) return false;
        }
        return true;
    }

    /** 这一局结束了吗（踩雷或翻完）。 */
    public boolean isOver(boolean[] mines, int[] cells) {
        return isLost(cells) || isWon(mines, cells);
    }

    /**
     * 本局能兑现多少币：只有<b>全部完成</b>才给满额——雷全部找到，或者整局通关（所有非雷格翻开）。
     * 没走完的一律不给，所以中途重开、提前收摊都拿不到币。
     */
    public static long payout(long maxReward, int foundMines, int mineCount, boolean won) {
        if (mineCount <= 0) return 0;
        return won || foundMines >= mineCount ? maxReward : 0;
    }

    // ==================== 内部实现 ====================

    /** 布雷：先把除 3×3 安全区外的格子收进位置表，再部分洗牌取前 {@link #mineCount} 个。 */
    private void placeMines(boolean[] mines, int safeCell, RandomSource random) {
        int safeCol = safeCell % width;
        int safeRow = safeCell / width;
        int available = 0;
        for (int cell = 0; cell < cellCount; cell++) {
            int col = cell % width;
            int row = cell / width;
            if (Math.abs(col - safeCol) <= 1 && Math.abs(row - safeRow) <= 1) continue;
            positions[available++] = cell;
        }
        int count = Math.min(mineCount, available);
        for (int index = 0; index < count; index++) {
            int pick = index + random.nextInt(available - index);
            int cell = positions[pick];
            positions[pick] = positions[index];
            positions[index] = cell;
            mines[cell] = true;
        }
    }

    /** 从 {@code start} 开始连锁翻开：空格继续扩散，数字格亮出数字为止。 */
    private void floodOpen(boolean[] mines, int[] cells, int start) {
        int head = 0;
        int tail = 0;
        cells[start] = OPEN_EMPTY;
        queue[tail++] = start;
        while (head < tail) {
            int cell = queue[head++];
            int adjacent = adjacentMines(mines, cell);
            if (adjacent > 0) {
                cells[cell] = adjacent;
                continue;
            }
            for (int direction = 0; direction < DIRECTIONS; direction++) {
                int next = neighbour(cell, direction);
                if (next < 0 || mines[next] || cells[next] != UNOPENED) continue;
                cells[next] = OPEN_EMPTY;
                queue[tail++] = next;
            }
        }
    }

    /** 踩雷了：所有雷亮出来（踩中的那颗单独一张图），插错的旗换成它本来的数字。 */
    private void revealMines(boolean[] mines, int[] cells, int deathCell) {
        for (int cell = 0; cell < cellCount; cell++) {
            if (mines[cell]) {
                cells[cell] = cell == deathCell ? DEATH_BOMB : BOMB;
            } else if (cells[cell] == FLAGGED) {
                int adjacent = adjacentMines(mines, cell);
                cells[cell] = adjacent > 0 ? adjacent : OPEN_EMPTY;
            }
        }
    }

    /** 通关：把还没插旗的雷都插上旗，界面上一眼看得出还剩几颗雷。 */
    private void flagAllMines(boolean[] mines, int[] cells) {
        for (int cell = 0; cell < cellCount; cell++) {
            if (mines[cell] && cells[cell] == UNOPENED) cells[cell] = FLAGGED;
        }
    }

    /** 一格周围八向的雷数。 */
    private int adjacentMines(boolean[] mines, int cell) {
        int count = 0;
        for (int direction = 0; direction < DIRECTIONS; direction++) {
            int next = neighbour(cell, direction);
            if (next >= 0 && mines[next]) count++;
        }
        return count;
    }

    /** 八向邻居的格号（{@code direction} 取 0~{@link #DIRECTIONS}-1）；越界返回 -1。 */
    public int neighbour(int cell, int direction) {
        int col = cell % width + NEIGHBOR_COL[direction];
        int row = cell / width + NEIGHBOR_ROW[direction];
        if (col < 0 || col >= width || row < 0 || row >= height) return -1;
        return row * width + col;
    }
}

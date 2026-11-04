package com.gtocore.common.machine.noenergy.game.minesweeper;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

/**
 * 扫雷盘面规则：只持有模式与尺寸/雷数，雷盘与显示盘由调用方按行优先保存。
 * 格子值：未翻开 0、空 -1、数字 1~8、旗 -2、雷 -3、踩中的雷 -4；旗数、踩雷、通关都从数组推导。
 * 数组可长于 {@link #cellCount()}，查询与落子只看前 {@code cellCount} 格；改动格记进 {@link IntArrayList} 供调用方刷新。
 * 首点 3×3 由生成器保证无雷，{@link #open} 不负责布雷；纯逻辑无同步，调用方须保证单线程。
 */
public final class MinesweeperRules {

    public static final int UNOPENED = 0;
    /** 翻开后的空格（周围无雷）。 */
    public static final int OPEN_EMPTY = -1;
    public static final int FLAGGED = -2;
    /** 结束时亮出的雷（不含踩中的那颗）。 */
    public static final int BOMB = -3;
    /** 踩中的那颗雷。 */
    public static final int DEATH_BOMB = -4;
    /** 数字格就是 1~8 本身。 */
    public static final int NUMBER_MIN = 1;

    public static final int DIRECTIONS = 8;

    public static final int CUSTOM_MIN_SIDE = 5;
    public static final int CUSTOM_MAX_SIDE = 64;

    /** 八向邻居的列/行偏移，两组顺序一一对应。 */
    private static final int[] NEIGHBOR_COL = { -1, 0, 1, -1, 1, -1, 0, 1 };
    private static final int[] NEIGHBOR_ROW = { -1, -1, -1, 0, 0, 1, 1, 1 };

    /** 这张规则表属于哪种模式：基础预设、战役关卡或自定义盘。 */
    public enum Mode {

        PRESET,
        CAMPAIGN,
        CUSTOM;

        public static final Mode[] ALL = values();

        public static @Nullable Mode of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    /** 难度档位：尺寸、雷数与奖励；{@link #CUSTOM} 由玩家给尺寸/雷数且不计奖励，前五档密度逐档提高（约 12%→28%）。 */
    public enum Difficulty {

        EASY(9, 9, 10, 3),
        NORMAL(16, 16, 40, 8),
        HARD(16, 30, 99, 20),
        EXPERT(24, 30, 179, 33),
        HELL(30, 30, 249, 45),
        CUSTOM(0, 0, 0, 0);

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

        /** 全部完成时的奖励额；{@link #CUSTOM} 恒为 0。 */
        public long maxReward() {
            return maxReward;
        }

        public boolean custom() {
            return this == CUSTOM;
        }

        /** 按序数取档位，越界返回 {@code null}（存档与网络来的值一律校验）。 */
        public static @Nullable Difficulty of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    private final Mode mode;
    /** 预设档位；战役/自定义为 {@code null}。 */
    @Nullable
    private final Difficulty difficulty;
    /** 战役关卡；基础/自定义为 {@code null}。 */
    @Nullable
    private final MinesweeperCampaign.Level campaignLevel;
    private final int width;
    private final int height;
    private final int cellCount;
    private final int mineCount;
    /** 全部完成时的奖励；战役与自定义档为 0。 */
    private final long maxReward;

    /** 连锁翻开的队列。 */
    private final int[] queue;

    private MinesweeperRules(Mode mode, @Nullable Difficulty difficulty,
                             @Nullable MinesweeperCampaign.Level campaignLevel,
                             int width, int height, int mineCount, long maxReward) {
        this.mode = mode;
        this.difficulty = difficulty;
        this.campaignLevel = campaignLevel;
        this.width = width;
        this.height = height;
        this.cellCount = width * height;
        this.mineCount = mineCount;
        this.maxReward = maxReward;
        this.queue = new int[cellCount];
    }

    /** 机器按当前档位持有一份实例，换档位时重建（队列按盘面尺寸分配）。 */
    public static MinesweeperRules create(Difficulty preset) {
        return new MinesweeperRules(Mode.PRESET, preset, null,
                preset.width(), preset.height(), preset.mineCount(), preset.maxReward());
    }

    /** 战役关卡：尺寸/雷数取自静态关卡表；首通奖励由里程碑记录另发，本规则不产生奖励。 */
    public static MinesweeperRules createCampaign(MinesweeperCampaign.Level level) {
        return new MinesweeperRules(Mode.CAMPAIGN, null, level,
                level.width(), level.height(), level.minesCount(), 0L);
    }

    /**
     * 自定义档：宽高夹到 {@link #CUSTOM_MIN_SIDE}~{@link #CUSTOM_MAX_SIDE}、雷数夹到 {@link #clampMines}，不计奖励；
     * 调用方要把夹好的值写回字段，界面与规则才不会各说一套。
     */
    public static MinesweeperRules createCustom(int width, int height, int mineCount) {
        int clampedWidth = Math.clamp(width, CUSTOM_MIN_SIDE, CUSTOM_MAX_SIDE);
        int clampedHeight = Math.clamp(height, CUSTOM_MIN_SIDE, CUSTOM_MAX_SIDE);
        return new MinesweeperRules(Mode.CUSTOM, null, null, clampedWidth, clampedHeight,
                clampMines(clampedWidth, clampedHeight, mineCount), 0);
    }

    /** 把雷数夹到 1 ~ 格数-9：上限给首点 3×3 留出安全区。 */
    public static int clampMines(int width, int height, int mineCount) {
        int max = Math.max(1, width * height - 9);
        return Math.clamp(mineCount, 1, max);
    }

    public Mode mode() {
        return mode;
    }

    public @Nullable Difficulty difficulty() {
        return difficulty;
    }

    public @Nullable MinesweeperCampaign.Level campaignLevel() {
        return campaignLevel;
    }

    public boolean campaign() {
        return mode == Mode.CAMPAIGN;
    }

    public boolean custom() {
        return mode == Mode.CUSTOM;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int cellCount() {
        return cellCount;
    }

    public int mineCount() {
        return mineCount;
    }

    public long maxReward() {
        return maxReward;
    }

    // ==================== 落子 ====================

    /**
     * 翻开一格：已翻开或插旗的格子不动；雷盘必须由调用方先布好。
     *
     * @param touched 改动格都记进来，可能重复；调用方先清空
     * @return 盘面是否真的变了
     */
    public boolean open(boolean[] mines, int[] cells, int cell, IntArrayList touched) {
        if (cell < 0 || cell >= cellCount) return false;
        if (cells[cell] != UNOPENED) return false;
        if (!isPlaced(mines)) return false;
        if (mines[cell]) {
            revealMines(mines, cells, cell, touched);
            return true;
        }
        floodOpen(mines, cells, cell, touched);
        if (isWon(mines, cells)) flagAllMines(mines, cells, touched);
        return true;
    }

    /**
     * 插旗/拔旗：旗数上限为雷数，插满后不能再插（免得靠满屏旗子碰运气）。
     *
     * @param touched 改动格都记进来；调用方先清空
     * @return 盘面是否真的变了
     */
    public boolean toggleFlag(int[] cells, int cell, IntArrayList touched) {
        if (cell < 0 || cell >= cellCount) return false;
        if (cells[cell] == FLAGGED) {
            cells[cell] = UNOPENED;
            touched.add(cell);
            return true;
        }
        if (cells[cell] != UNOPENED || flags(cells) >= mineCount) return false;
        cells[cell] = FLAGGED;
        touched.add(cell);
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

    /** 已插旗数（含插错的旗）。 */
    public int flags(int[] cells) {
        int count = 0;
        for (int state : cells) {
            if (state == FLAGGED) count++;
        }
        return count;
    }

    /** 插对的旗数（旗落在雷上），即「找到的雷」。 */
    public int foundMines(boolean[] mines, int[] cells) {
        int count = 0;
        for (int cell = 0; cell < cellCount; cell++) {
            if (mines[cell] && cells[cell] == FLAGGED) count++;
        }
        return count;
    }

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

    public boolean isOver(boolean[] mines, int[] cells) {
        return isLost(cells) || isWon(mines, cells);
    }

    /** 本局能兑现多少币：只有<b>全部完成</b>（雷全找到，或所有非雷格翻开）才给满额，其余一律 0；自定义档恒为 0。 */
    public long payout(int foundMines, boolean won) {
        if (maxReward <= 0 || mineCount <= 0) return 0;
        return won || foundMines >= mineCount ? maxReward : 0;
    }

    // ==================== 内部实现 ====================

    /** 从 {@code start} 开始连锁翻开：空格继续扩散，数字格亮出数字为止。 */
    private void floodOpen(boolean[] mines, int[] cells, int start, IntArrayList touched) {
        int head = 0;
        int tail = 0;
        cells[start] = OPEN_EMPTY;
        touched.add(start);
        queue[tail++] = start;
        while (head < tail) {
            int cell = queue[head++];
            int adjacent = adjacentMines(mines, cell);
            if (adjacent > 0) {
                cells[cell] = adjacent;
                touched.add(cell);
                continue;
            }
            for (int direction = 0; direction < DIRECTIONS; direction++) {
                int next = neighbour(cell, direction);
                if (next < 0 || mines[next] || cells[next] != UNOPENED) continue;
                cells[next] = OPEN_EMPTY;
                touched.add(next);
                queue[tail++] = next;
            }
        }
    }

    /** 踩雷：亮出所有雷（踩中的那颗单独一张图），插错的旗换回本来的数字。 */
    private void revealMines(boolean[] mines, int[] cells, int deathCell, IntArrayList touched) {
        for (int cell = 0; cell < cellCount; cell++) {
            if (mines[cell]) {
                cells[cell] = cell == deathCell ? DEATH_BOMB : BOMB;
                touched.add(cell);
            } else if (cells[cell] == FLAGGED) {
                int adjacent = adjacentMines(mines, cell);
                cells[cell] = adjacent > 0 ? adjacent : OPEN_EMPTY;
                touched.add(cell);
            }
        }
    }

    /** 通关：给还没插旗的雷补上旗，界面上一眼看得出雷已找齐。 */
    private void flagAllMines(boolean[] mines, int[] cells, IntArrayList touched) {
        for (int cell = 0; cell < cellCount; cell++) {
            if (mines[cell] && cells[cell] == UNOPENED) {
                cells[cell] = FLAGGED;
                touched.add(cell);
            }
        }
    }

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

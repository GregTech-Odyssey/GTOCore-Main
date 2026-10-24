package com.gtocore.common.machine.noenergy.game.minesweeper;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

/**
 * 扫雷盘面规则：只持有难度，雷盘与显示盘由调用方按行优先保存。
 * 格子值：未翻开 0、空 -1、数字 1~8、旗 -2、雷 -3、踩中的雷 -4；旗数、踩雷、通关都从数组推导。
 *
 * <p>
 * 数组可以长于 {@link #cellCount()}，查询与落子只看前 {@code cellCount} 格。
 * 每次落子把改动格写入 {@link IntArrayList}，调用方只刷新这些显示方块。
 * 首点 3×3 由生成器保证无雷，{@link #open} 不负责布雷；纯逻辑，只在服务端线程调用。
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

    /** 自定义盘面的边长下限。 */
    public static final int CUSTOM_MIN_SIDE = 5;
    /** 自定义盘面的边长上限（宽高各自）。 */
    public static final int CUSTOM_MAX_SIDE = 64;

    /** 八向邻居的行/列偏移。 */
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

    /**
     * 难度档位：尺寸、雷数与通关奖励；前五档固定，{@link #CUSTOM} 由玩家给尺寸/雷数且不计奖励。
     * 密度逐档提高：12.3% / 15.6% / 20.6% / 24.9% / 27.7%。
     */
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

        /** 全部完成（或整局通关）时给多少游戏币；{@link #CUSTOM} 不计奖励，返回 0。 */
        public long maxReward() {
            return maxReward;
        }

        /** 尺寸与雷数是否由玩家给。 */
        public boolean custom() {
            return this == CUSTOM;
        }

        /** 按序数取档位，越界返回 {@code null}（存档与网络来的值一律校验）。 */
        public static @Nullable Difficulty of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    /** 模式。 */
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

    /** 调用方按档位持有一份实例；换档位时重建，临时容器跟着盘面尺寸分配。 */
    public static MinesweeperRules create(Difficulty preset) {
        return new MinesweeperRules(Mode.PRESET, preset, null,
                preset.width(), preset.height(), preset.mineCount(), preset.maxReward());
    }

    /** 战役关卡：尺寸/雷数来自静态关卡表，按里程碑记录单独发首通奖励，本规则不产生基础奖励。 */
    public static MinesweeperRules createCampaign(MinesweeperCampaign.Level level) {
        return new MinesweeperRules(Mode.CAMPAIGN, null, level,
                level.width(), level.height(), level.minesCount(), 0L);
    }

    /**
     * 自定义档：尺寸与雷数由玩家给，宽高夹到 {@link #CUSTOM_MIN_SIDE}~{@link #CUSTOM_MAX_SIDE}、雷数夹到
     * {@link #clampMines} 的范围，一律不计奖励。调用方把夹好的值写回自己的字段，界面与规则才不会各说一套。
     */
    public static MinesweeperRules createCustom(int width, int height, int mineCount) {
        int clampedWidth = Math.clamp(width, CUSTOM_MIN_SIDE, CUSTOM_MAX_SIDE);
        int clampedHeight = Math.clamp(height, CUSTOM_MIN_SIDE, CUSTOM_MAX_SIDE);
        return new MinesweeperRules(Mode.CUSTOM, null, null, clampedWidth, clampedHeight,
                clampMines(clampedWidth, clampedHeight, mineCount), 0);
    }

    /** 雷数上限：留出首点 3×3 的安全区，至少 1 颗。 */
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

    /** 全部完成时的奖励；自定义档为 0。 */
    public long maxReward() {
        return maxReward;
    }

    // ==================== 落子 ====================

    /**
     * 翻开一格：已翻开或插了旗的格子不动；雷盘必须由调用方提前生成好。
     *
     * @param touched 改动过的格子都记进来（调用方先清空），可能重复
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
     * 插旗/拔旗：旗子最多插到雷数那么多（插满就不能再插，免得靠满屏旗子碰运气）。
     *
     * @param touched 改动过的格子都记进来（调用方先清空）
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
     * 没走完的一律不给，所以中途重开、提前收摊都拿不到币；自定义档 {@link #maxReward()} 为 0，同样一分不给。
     */
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

    /** 踩雷了：所有雷亮出来（踩中的那颗单独一张图），插错的旗换成它本来的数字。 */
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

    /** 通关：把还没插旗的雷都插上旗，界面上一眼看得出还剩几颗雷。 */
    private void flagAllMines(boolean[] mines, int[] cells, IntArrayList touched) {
        for (int cell = 0; cell < cellCount; cell++) {
            if (mines[cell] && cells[cell] == UNOPENED) {
                cells[cell] = FLAGGED;
                touched.add(cell);
            }
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

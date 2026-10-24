package com.gtocore.common.machine.noenergy.game.minesweeper;

/**
 * 战役的 60 关：新手 10 关、老兵 20 关、精锐 30 关，按全局关卡 0..59 连续排列。
 * 每关记录章节、章内序号、宽高与雷数；进度用每玩家一个 60 位 {@code long} 表示，前一位完成才解锁后一位。
 */
public final class MinesweeperCampaign {

    public static final int LEVEL_COUNT = 60;
    public static final int TIER_COUNT = 3;
    public static final int TIER_NOVICE = 0;
    public static final int TIER_VETERAN = 1;
    public static final int TIER_ELITE = 2;
    public static final long ALL_LEVELS_MASK = (1L << LEVEL_COUNT) - 1L;

    private static final int[] TIER_STARTS = { 0, 10, 30 };
    private static final int[] TIER_SIZES = { 10, 20, 30 };

    /**
     * 一关的静态数据。
     *
     * @param tier       0 新手、1 老兵、2 精锐
     * @param index      章内序号，从 1 开始
     * @param width      宽（列）
     * @param height     高（行）
     * @param minesCount 雷数
     */
    public record Level(int tier, int index, int width, int height, int minesCount) {}

    private static final Level[] LEVELS = {
            new Level(0, 1, 10, 10, 15),
            new Level(0, 2, 11, 12, 20),
            new Level(0, 3, 13, 15, 30),
            new Level(0, 4, 14, 17, 37),
            new Level(0, 5, 16, 20, 50),
            new Level(0, 6, 17, 22, 60),
            new Level(0, 7, 19, 25, 80),
            new Level(0, 8, 20, 27, 95),
            new Level(0, 9, 22, 30, 120),
            new Level(0, 10, 25, 35, 175),
            new Level(1, 1, 10, 10, 18),
            new Level(1, 2, 11, 11, 22),
            new Level(1, 3, 12, 12, 28),
            new Level(1, 4, 13, 14, 35),
            new Level(1, 5, 14, 15, 40),
            new Level(1, 6, 15, 16, 48),
            new Level(1, 7, 16, 17, 55),
            new Level(1, 8, 17, 18, 62),
            new Level(1, 9, 18, 20, 74),
            new Level(1, 10, 19, 21, 85),
            new Level(1, 11, 20, 22, 92),
            new Level(1, 12, 21, 23, 105),
            new Level(1, 13, 22, 24, 115),
            new Level(1, 14, 23, 25, 128),
            new Level(1, 15, 24, 26, 146),
            new Level(1, 16, 25, 27, 158),
            new Level(1, 17, 26, 29, 175),
            new Level(1, 18, 27, 30, 195),
            new Level(1, 19, 29, 31, 220),
            new Level(1, 20, 30, 32, 230),
            new Level(2, 1, 10, 10, 21),
            new Level(2, 2, 11, 11, 25),
            new Level(2, 3, 12, 12, 30),
            new Level(2, 4, 12, 13, 33),
            new Level(2, 5, 13, 13, 38),
            new Level(2, 6, 14, 14, 44),
            new Level(2, 7, 15, 15, 50),
            new Level(2, 8, 16, 16, 56),
            new Level(2, 9, 17, 17, 66),
            new Level(2, 10, 18, 18, 74),
            new Level(2, 11, 19, 19, 82),
            new Level(2, 12, 20, 20, 90),
            new Level(2, 13, 20, 21, 100),
            new Level(2, 14, 21, 21, 106),
            new Level(2, 15, 22, 22, 118),
            new Level(2, 16, 23, 23, 128),
            new Level(2, 17, 24, 24, 140),
            new Level(2, 18, 25, 25, 158),
            new Level(2, 19, 26, 26, 170),
            new Level(2, 20, 27, 27, 180),
            new Level(2, 21, 28, 28, 196),
            new Level(2, 22, 29, 29, 220),
            new Level(2, 23, 30, 30, 230),
            new Level(2, 24, 30, 31, 240),
            new Level(2, 25, 31, 31, 252),
            new Level(2, 26, 32, 32, 270),
            new Level(2, 27, 35, 37, 360),
            new Level(2, 28, 36, 36, 362),
            new Level(2, 29, 35, 38, 380),
            new Level(2, 30, 40, 42, 504)
    };

    private MinesweeperCampaign() {}

    /** 全局关卡 0..59；越界夹到最近的有效关。 */
    public static Level level(int globalIndex) {
        return LEVELS[Math.clamp(globalIndex, 0, LEVEL_COUNT - 1)];
    }

    public static int tierStart(int tier) {
        return TIER_STARTS[Math.clamp(tier, 0, TIER_COUNT - 1)];
    }

    public static int tierSize(int tier) {
        return TIER_SIZES[Math.clamp(tier, 0, TIER_COUNT - 1)];
    }

    /** 关卡所在的章节；越界夹到有效范围。 */
    public static int tierOf(int globalIndex) {
        int index = Math.clamp(globalIndex, 0, LEVEL_COUNT - 1);
        return index < TIER_STARTS[1] ? TIER_NOVICE : index < TIER_STARTS[2] ? TIER_VETERAN : TIER_ELITE;
    }

    /** 章内序号，从 1 开始。 */
    public static int indexInTier(int globalIndex) {
        int index = Math.clamp(globalIndex, 0, LEVEL_COUNT - 1);
        return index - TIER_STARTS[tierOf(index)] + 1;
    }

    /** 第一关永远解锁；第 n 关要求第 n-1 关的位已置。 */
    public static boolean isUnlocked(long cleared, int globalIndex) {
        if (globalIndex <= 0) return true;
        if (globalIndex >= LEVEL_COUNT) return false;
        return (cleared & (1L << (globalIndex - 1))) != 0L;
    }

    /** 第一个已解锁但还没通关的关卡；全部通关时返回最后一关。 */
    public static int firstUnlocked(long cleared) {
        for (int index = 0; index < LEVEL_COUNT; index++) {
            if (isUnlocked(cleared, index) && (cleared & (1L << index)) == 0L) return index;
        }
        return LEVEL_COUNT - 1;
    }

    public static int clearedCount(long cleared) {
        return Long.bitCount(cleared & ALL_LEVELS_MASK);
    }

    /** 章节首通奖励 */
    public static long rewardForTier(int tier) {
        return switch (tier) {
            case TIER_NOVICE -> 15L;
            case TIER_VETERAN -> 30L;
            case TIER_ELITE -> 60L;
            default -> 0L;
        };
    }

    public static long rewardForLevel(int globalIndex) {
        return rewardForTier(tierOf(globalIndex));
    }
}

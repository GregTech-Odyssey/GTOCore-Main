package com.gtocore.common.machine.noenergy.slotMachine;

import com.gtocore.common.machine.noenergy.slotMachine.SlotMachineResult.WinningLine;

import net.minecraft.util.RandomSource;

import java.util.Arrays;
import java.util.Objects;

/** 不可变的老虎机规则，统一管理档位、固定卷轴、中奖线和开奖判定。 */
public final class SlotMachineRules {

    public static final int REEL_COUNT = 3;
    public static final int VISIBLE_ROWS = 3;
    /** 盘面按行存储：上、中、下各三个符号。 */
    public static final int SYMBOL_COUNT = REEL_COUNT * VISIBLE_ROWS;
    public static final int DEFAULT_BET = 5;
    public static final int DEFAULT_MAX_BET = 78125;
    /** 正常速度下滚动一秒。 */
    public static final int DEFAULT_SPIN_TICKS = 20;
    /**
     * 符号比例档位（1~5），档位越高长期回报率越高；各档赔率相同，已保存的待结算结果不受影响，
     * 改这里需要重新编译。
     */
    public static final int DEFAULT_RETURN_TIER = 3;

    /**
     * 每档左卷轴的符号次数，按稳定 id 排列：甜浆果、紫颂果、西瓜片、苹果、金胡萝卜、金苹果、绿宝石、下界之星、蜂蜜。
     * 每行共 99 格；中、右卷轴分别交换甜浆果与紫颂果、西瓜片的次数。
     * 高档减弱三列之间的比例差异，中高奖符号和百搭的次数保持固定。
     * 档位后的百分比是按当前五条线、固定赔率实测的长期回报率，只作记录，不参与开奖。
     */
    private static final int[][] TIER_SYMBOL_COUNTS = {
            { 62, 2, 2, 14, 9, 5, 3, 1, 1 }, // 第 1 档：约 71.125%
            { 52, 7, 7, 14, 9, 5, 3, 1, 1 }, // 第 2 档：约 84.227%
            { 46, 10, 10, 14, 9, 5, 3, 1, 1 }, // 第 3 档：约 94.870%
            { 40, 13, 13, 14, 9, 5, 3, 1, 1 }, // 第 4 档：约 105.764%
            { 30, 18, 18, 14, 9, 5, 3, 1, 1 }, // 第 5 档：约 120.396%
    };
    private static final Payline[] PAYLINES = {
            Payline.MIDDLE, Payline.TOP, Payline.BOTTOM, Payline.DIAGONAL_DOWN, Payline.DIAGONAL_UP
    };

    public static final SlotMachineRules DEFAULT = forTier(DEFAULT_RETURN_TIER);

    /** 不向外暴露可变数组，开奖只经 {@link #spin} 读取。 */
    private final SlotSymbol[][] reelStrips;

    private SlotMachineRules(SlotSymbol[][] reelStrips) {
        if (DEFAULT_BET < 1 || DEFAULT_MAX_BET < DEFAULT_BET) throw new IllegalArgumentException("Invalid bet range");
        if (DEFAULT_SPIN_TICKS < 1) throw new IllegalArgumentException("Spin duration must be positive");
        this.reelStrips = reelStrips;
        int maxMultiplier = 0;
        for (int i = 0; i < SlotSymbol.count(); i++) {
            SlotSymbol symbol = SlotSymbol.byId(i);
            maxMultiplier = Math.max(maxMultiplier, Math.max(symbol.twoMatchMultiplier(), symbol.threeMatchMultiplier()));
        }
        if ((long) DEFAULT_MAX_BET * maxMultiplier > Long.MAX_VALUE / PAYLINES.length) {
            throw new IllegalArgumentException("Maximum bet and payline count would overflow total rewards");
        }
    }

    /** 三根固定卷轴只在这里生成一次，之后每局复用，不在开奖时重建。 */
    public static SlotMachineRules forTier(int tier) {
        if (tier < 1 || tier > TIER_SYMBOL_COUNTS.length) throw new IllegalArgumentException("Return tier must be between 1 and 5");
        int[] leftCounts = TIER_SYMBOL_COUNTS[tier - 1];
        SlotSymbol[][] reels = new SlotSymbol[REEL_COUNT][];
        for (int reel = 0; reel < REEL_COUNT; reel++) {
            int[] counts = Arrays.copyOf(leftCounts, leftCounts.length);
            counts[0] = leftCounts[reel];
            counts[reel] = leftCounts[0];
            reels[reel] = createReel(counts, reel);
        }
        return new SlotMachineRules(reels);
    }

    static int clampBet(int bet) {
        return Math.clamp(bet, DEFAULT_BET, DEFAULT_MAX_BET);
    }

    static int symbolIndex(int reel, int row) {
        if (reel < 0 || reel >= REEL_COUNT) throw new IndexOutOfBoundsException(reel);
        if (row < 0 || row >= VISIBLE_ROWS) throw new IndexOutOfBoundsException(row);
        return row * REEL_COUNT + reel;
    }

    /** 每列只随机一次停点，连续三格组成可见窗口，再按五条中奖线汇总奖励。 */
    SlotMachineResult spin(RandomSource random, int bet) {
        Objects.requireNonNull(random, "random");
        int clampedBet = clampBet(bet);
        SlotSymbol[] symbols = new SlotSymbol[SYMBOL_COUNT];
        for (int reel = 0; reel < REEL_COUNT; reel++) {
            SlotSymbol[] strip = reelStrips[reel];
            int stop = random.nextInt(strip.length);
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                symbols[symbolIndex(reel, row)] = strip[(stop + row) % strip.length];
            }
        }

        WinningLine[] lines = new WinningLine[PAYLINES.length];
        int lineCount = 0;
        long reward = 0;
        for (Payline payline : PAYLINES) {
            SlotSymbol first = symbols[symbolIndex(0, payline.row(0))];
            SlotSymbol second = symbols[symbolIndex(1, payline.row(1))];
            SlotSymbol third = symbols[symbolIndex(2, payline.row(2))];
            SlotSymbol target = chooseTarget(first, second, third);
            int matchCount = matchCount(first, second, third, target);
            int multiplier = multiplier(target, matchCount);
            if (multiplier <= 0) continue;
            long lineReward = (long) clampedBet * multiplier;
            lines[lineCount++] = new WinningLine(payline, target, matchCount, multiplier, lineReward);
            reward += lineReward;
        }
        return new SlotMachineResult(clampedBet, reward, symbols, lines, lineCount);
    }

    /** 目标符号取最左侧的非百搭符号；全百搭按下界之星三连结算。 */
    private static SlotSymbol chooseTarget(SlotSymbol first, SlotSymbol second, SlotSymbol third) {
        if (!first.wild()) return first;
        if (!second.wild()) return second;
        return third.wild() ? SlotSymbol.NETHER_STAR : third;
    }

    /** 只认从左开始的连续匹配：前两格必须是目标或百搭，第三格再决定是三连还是二连。 */
    private static int matchCount(SlotSymbol first, SlotSymbol second, SlotSymbol third, SlotSymbol target) {
        if ((!first.wild() && first != target) || (!second.wild() && second != target)) return 0;
        return third.wild() || third == target ? 3 : 2;
    }

    private static int multiplier(SlotSymbol target, int matchCount) {
        return switch (matchCount) {
            case 3 -> target.threeMatchMultiplier();
            case 2 -> target.twoMatchMultiplier();
            default -> 0;
        };
    }

    /**
     * 按次数生成固定、分散的卷轴：各符号等间距放置后按相位交错合并，占比过高的符号再均匀插入其他符号之间。
     * 种子只决定排列，卷轴本身不随后续开奖变化。
     */
    private static SlotSymbol[] createReel(int[] symbolCounts, long layoutSeed) {
        int size = 0;
        int symbolCount = SlotSymbol.count();
        if (symbolCounts.length != symbolCount) throw new IllegalArgumentException("A count is required for every symbol");
        for (int count : symbolCounts) {
            if (count < 0) throw new IllegalArgumentException("Symbol counts must not be negative");
            size = Math.addExact(size, count);
        }
        if (size < VISIBLE_ROWS) throw new IllegalArgumentException("A reel strip must contain at least 3 symbols");
        int[] placed = new int[symbolCount];
        double[] phases = new double[symbolCount];
        RandomSource layoutRandom = RandomSource.create(layoutSeed);
        int dominantId = 0;
        for (int i = 0; i < symbolCount; i++) {
            phases[i] = layoutRandom.nextDouble();
            if (symbolCounts[i] > symbolCounts[dominantId]) dominantId = i;
        }
        SlotSymbol[] strip = new SlotSymbol[size];
        for (int index = 0; index < size; index++) {
            int selected = -1;
            double earliestPosition = Double.POSITIVE_INFINITY;
            for (int i = 0; i < symbolCount; i++) {
                if (placed[i] >= symbolCounts[i]) continue;
                double position = (placed[i] + phases[i]) / symbolCounts[i];
                if (position < earliestPosition) {
                    selected = i;
                    earliestPosition = position;
                }
            }
            strip[index] = SlotSymbol.byId(selected);
            placed[selected]++;
        }
        int dominantCount = symbolCounts[dominantId];
        int otherCount = size - dominantCount;
        if (3L * dominantCount > size && dominantCount <= 2L * otherCount) {
            // 保留其他符号的相对顺序，每个环形间隔至多插入两格高频符号。
            SlotSymbol dominant = SlotSymbol.byId(dominantId);
            SlotSymbol[] dispersed = new SlotSymbol[size];
            int index = 0;
            int otherPlaced = 0;
            int dominantPlaced = 0;
            for (SlotSymbol symbol : strip) {
                if (symbol == dominant) continue;
                dispersed[index++] = symbol;
                int target = (int) ((long) ++otherPlaced * dominantCount / otherCount);
                while (dominantPlaced < target) {
                    dispersed[index++] = dominant;
                    dominantPlaced++;
                }
            }
            strip = dispersed;
        }
        return strip;
    }

    /** 中奖线的稳定存档 id 与每列行号，0/1/2 分别表示上/中/下行。 */
    enum Payline {

        TOP(0, 0, 0, 0),
        MIDDLE(1, 1, 1, 1),
        BOTTOM(2, 2, 2, 2),
        DIAGONAL_DOWN(3, 0, 1, 2),
        DIAGONAL_UP(4, 2, 1, 0);

        private static final Payline[] VALUES = values();

        private final int id;
        private final int row0;
        private final int row1;
        private final int row2;

        Payline(int id, int row0, int row1, int row2) {
            this.id = id;
            this.row0 = row0;
            this.row1 = row1;
            this.row2 = row2;
        }

        int id() {
            return id;
        }

        int row(int reel) {
            return switch (reel) {
                case 0 -> row0;
                case 1 -> row1;
                case 2 -> row2;
                default -> throw new IndexOutOfBoundsException(reel);
            };
        }

        static Payline byId(int id) {
            // 非法 id 回退到中线，保证读档不失败
            return id < 0 || id >= VALUES.length ? MIDDLE : VALUES[id];
        }
    }
}

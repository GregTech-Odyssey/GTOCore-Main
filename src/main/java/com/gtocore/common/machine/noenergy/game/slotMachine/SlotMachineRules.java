package com.gtocore.common.machine.noenergy.game.slotMachine;

import com.gtocore.common.machine.noenergy.game.slotMachine.SlotMachineLayout.Payline;
import com.gtocore.common.machine.noenergy.game.slotMachine.SlotMachineResult.WinningLine;

import net.minecraft.util.RandomSource;

import java.util.Arrays;
import java.util.Objects;

/**
 * 一套不可变的老虎机规则：盘面布局、固定卷轴与按布局的赔付表；每台机器构造时注入一份，规则本身不参与存档。
 *
 * <p>
 * 单线奖励 = 下注 × 该线倍率，整局奖励是各中奖线之和。每条线只从每根卷轴取一格，卷轴停点均匀分布，
 * 因此各线期望相同：整机回报率 = 中奖线数 × 单线期望。按此精确求和，3 列为 94.87%、5 列为 95.00%，
 * 改卷轴次数或赔付表都会改变它。
 */
public final class SlotMachineRules {

    public static final int DEFAULT_BET = 5;
    public static final int DEFAULT_MAX_BET = 78125;

    /**
     * 3 列布局左卷轴的符号次数，下标即稳定 id（SYMBOL0~SYMBOL8），对应物品见 {@link SlotSymbol#symbolItem(SlotSymbol)}。
     * 每根卷轴共 99 格；中、右两列把 SYMBOL0 的次数与 SYMBOL1、SYMBOL2 对调，让最便宜的三个符号在各列错开。
     */
    private static final int[] TIER_SYMBOL_COUNTS = { 46, 10, 10, 14, 9, 5, 3, 1, 1 };

    /**
     * 5 列布局每根卷轴的符号次数（下标同上，每根合计 100）：最便宜的三个符号在两两错开的卷轴上轮流占主导，
     * 避免它们在前两列频繁对上。中高奖符号的次数与 3 列布局相同，百搭（SYMBOL8）每根 2 格，比 3 列的 1 格多。
     */
    private static final int[][] WIDE_SYMBOL_COUNTS = {
            { 42, 12, 12, 14, 9, 5, 3, 1, 2 },
            { 12, 42, 12, 14, 9, 5, 3, 1, 2 },
            { 12, 12, 42, 14, 9, 5, 3, 1, 2 },
            { 42, 12, 12, 14, 9, 5, 3, 1, 2 },
            { 12, 42, 12, 14, 9, 5, 3, 1, 2 },
    };

    /**
     * 5 列布局的赔付表：行是三连/四连/五连，列是符号 id（顺序同上；百搭 SYMBOL8 自身不赔付）。
     * 5 列只赔三连以上，连得越长赔得越多；配套回报率恰为 1899999999/2000000000，改动任意一格都会改变它。
     */
    private static final int[][] WIDE_PAYTABLE = {
            { 2, 2, 2, 3, 4, 6, 15, 25, 0 },
            { 4, 4, 4, 12, 20, 25, 60, 200, 0 },
            { 10, 10, 10, 30, 80, 150, 300, 800, 0 },
    };

    /** 经典三列三行，5 条中奖线。 */
    public static final SlotMachineRules CLASSIC = new SlotMachineRules(SlotMachineLayout.CLASSIC, classicReels(), classicPaytable());
    /** 宽幅五列三行，9 条中奖线，只赔三连以上。 */
    public static final SlotMachineRules WIDE = new SlotMachineRules(SlotMachineLayout.WIDE, wideReels(), widePaytable());

    /** 各列固定卷轴；开奖只经 {@link #spin} 读取，不对外暴露。 */
    private final SlotSymbol[][] reelStrips;
    private final SlotMachineLayout layout;
    /**
     * 赔付表：第一维是连中长度、第二维是符号 id，0 表示该长度不赔付。3 列只填 2/3 两行，5 列只填 3/4/5 三行，
     * 没填的组合自然不中奖；百搭按被替代符号查表。
     */
    private final int[][] paytable;
    /** 单条中奖线的最高倍率，附赠效果拿它当「满档」参照。 */
    private final int maxLineMultiplier;

    private SlotMachineRules(SlotMachineLayout layout, SlotSymbol[][] reelStrips, int[][] paytable) {
        if (DEFAULT_BET < 1 || DEFAULT_MAX_BET < DEFAULT_BET) throw new IllegalArgumentException("Invalid bet range");
        this.layout = layout;
        this.reelStrips = reelStrips;
        this.paytable = paytable;
        int maxMultiplier = 0;
        for (int[] multipliers : paytable) {
            for (int multiplier : multipliers) {
                maxMultiplier = Math.max(maxMultiplier, multiplier);
            }
        }
        this.maxLineMultiplier = maxMultiplier;
        if ((long) DEFAULT_MAX_BET * maxMultiplier > Long.MAX_VALUE / layout.paylines().length) {
            throw new IllegalArgumentException("Maximum bet and payline count would overflow total rewards");
        }
    }

    SlotMachineLayout layout() {
        return layout;
    }

    /** 单线最高倍率；多线相加的实际倍率还可能更高。 */
    int maxLineMultiplier() {
        return maxLineMultiplier;
    }

    int spinTicks() {
        return layout.spinTicks();
    }

    /** 符号在某个连中长度上的倍率；{@code 0} 表示该长度不赔付。 */
    public int multiplier(SlotSymbol symbol, int matchCount) {
        return paytable[matchCount][symbol.id()];
    }

    /** 3 列的固定卷轴只在这里生成一次，之后每局复用。 */
    private static SlotSymbol[][] classicReels() {
        int[] leftCounts = TIER_SYMBOL_COUNTS;
        SlotSymbol[][] reels = new SlotSymbol[SlotMachineLayout.CLASSIC.reelCount()][];
        for (int reel = 0; reel < reels.length; reel++) {
            int[] counts = Arrays.copyOf(leftCounts, leftCounts.length);
            counts[0] = leftCounts[reel];
            counts[reel] = leftCounts[0];
            reels[reel] = createReel(SlotMachineLayout.CLASSIC, counts, reel);
        }
        return reels;
    }

    private static SlotSymbol[][] wideReels() {
        SlotSymbol[][] reels = new SlotSymbol[WIDE_SYMBOL_COUNTS.length][];
        for (int reel = 0; reel < reels.length; reel++) {
            reels[reel] = createReel(SlotMachineLayout.WIDE, WIDE_SYMBOL_COUNTS[reel], reel);
        }
        return reels;
    }

    /** 3 列赔付表：2 连、3 连两行取自符号自身倍率，其余长度为 0。 */
    private static int[][] classicPaytable() {
        int[][] table = new int[SlotMachineLayout.MAX_REEL_COUNT + 1][SlotSymbol.count()];
        for (int i = 0; i < SlotSymbol.count(); i++) {
            SlotSymbol symbol = SlotSymbol.byId(i);
            table[2][i] = symbol.twoMatchMultiplier();
            table[3][i] = symbol.threeMatchMultiplier();
        }
        return table;
    }

    /** 5 列赔付表只有 3/4/5 连三行，直接搬 {@link #WIDE_PAYTABLE}。 */
    private static int[][] widePaytable() {
        int[][] table = new int[SlotMachineLayout.MAX_REEL_COUNT + 1][SlotSymbol.count()];
        for (int i = 0; i < WIDE_PAYTABLE.length; i++) {
            System.arraycopy(WIDE_PAYTABLE[i], 0, table[i + 3], 0, SlotSymbol.count());
        }
        return table;
    }

    static int clampBet(int bet) {
        return Math.clamp(bet, DEFAULT_BET, DEFAULT_MAX_BET);
    }

    /** 每列随机一个停点，取连续 visibleRows 格作窗口，再逐条中奖线汇总。 */
    SlotMachineResult spin(RandomSource random, int bet) {
        Objects.requireNonNull(random, "random");
        int clampedBet = clampBet(bet);
        SlotSymbol[] symbols = new SlotSymbol[layout.symbolCount()];
        for (int reel = 0; reel < layout.reelCount(); reel++) {
            SlotSymbol[] strip = reelStrips[reel];
            int stop = random.nextInt(strip.length);
            for (int row = 0; row < layout.visibleRows(); row++) {
                symbols[layout.symbolIndex(reel, row)] = strip[(stop + row) % strip.length];
            }
        }

        Payline[] paylines = layout.paylines();
        WinningLine[] lines = new WinningLine[paylines.length];
        int lineCount = 0;
        long reward = 0;
        for (Payline payline : paylines) {
            SlotSymbol target = chooseTarget(symbols, payline);
            int matchCount = matchCount(symbols, payline, target);
            int multiplier = multiplier(target, matchCount);
            if (multiplier <= 0) continue;
            long lineReward = (long) clampedBet * multiplier;
            lines[lineCount++] = new WinningLine(payline, target, matchCount, multiplier, lineReward);
            reward += lineReward;
        }
        return new SlotMachineResult(layout, clampedBet, reward, symbols, lines, lineCount);
    }

    /** 目标取最左侧的非百搭符号；整条线全百搭时按最高奖符号 SYMBOL7 结算。 */
    private SlotSymbol chooseTarget(SlotSymbol[] symbols, Payline payline) {
        for (int reel = 0; reel < layout.reelCount(); reel++) {
            SlotSymbol symbol = symbols[layout.symbolIndex(reel, payline.row(reel))];
            if (!symbol.wild()) return symbol;
        }
        return SlotSymbol.SYMBOL7;
    }

    /** 只认从左开始的连续匹配：百搭可替代目标，遇到别的符号即断，第一格就断则整线不中。 */
    private int matchCount(SlotSymbol[] symbols, Payline payline, SlotSymbol target) {
        int count = 0;
        for (int reel = 0; reel < layout.reelCount(); reel++) {
            SlotSymbol symbol = symbols[layout.symbolIndex(reel, payline.row(reel))];
            if (!symbol.wild() && symbol != target) break;
            count++;
        }
        return count;
    }

    /**
     * 按次数生成固定卷轴：各符号等间距放置后按相位交错合并，占比过高的符号再均匀插入其他符号之间。
     * 种子只决定排列，卷轴不随后续开奖变化。
     */
    private static SlotSymbol[] createReel(SlotMachineLayout layout, int[] symbolCounts, long layoutSeed) {
        int size = 0;
        int symbolCount = SlotSymbol.count();
        if (symbolCounts.length != symbolCount) throw new IllegalArgumentException("A count is required for every symbol");
        for (int count : symbolCounts) {
            if (count < 0) throw new IllegalArgumentException("Symbol counts must not be negative");
            size = Math.addExact(size, count);
        }
        if (size < layout.visibleRows()) throw new IllegalArgumentException("A reel strip must contain at least one visible window");
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
}

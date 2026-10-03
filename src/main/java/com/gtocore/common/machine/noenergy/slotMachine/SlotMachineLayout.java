package com.gtocore.common.machine.noenergy.slotMachine;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 老虎机的盘面布局：列数、显示行数、中奖线走向与滚动时长。
 *
 * <p>
 * 布局只决定盘面几何，不参与赔付：赔付倍率由 {@link SlotMachineRules} 按布局提供。{@link #id()} 会随开奖
 * 结果写进存档，所以布局只能追加，不能改动或删除已有 id 的含义。
 */
@Getter
@Accessors(fluent = true)
public enum SlotMachineLayout {

    /** 经典三列三行：三条横线与两条对角线。 */
    CLASSIC(0, 3, 3, 20,
            Payline.TOP, Payline.MIDDLE, Payline.BOTTOM, Payline.DIAGONAL_DOWN, Payline.DIAGONAL_UP),
    /** 宽幅五列三行：三条横线、两条折线与四条带缺口的折线。 */
    WIDE(1, 5, 3, 20,
            Payline.WIDE_MIDDLE, Payline.WIDE_TOP, Payline.WIDE_BOTTOM, Payline.WIDE_V, Payline.WIDE_LAMBDA,
            Payline.WIDE_TOP_NOTCH, Payline.WIDE_BOTTOM_NOTCH, Payline.WIDE_TOP_EDGES, Payline.WIDE_BOTTOM_EDGES);

    private static final SlotMachineLayout[] VALUES = values();
    /** 所有布局里最大的列数：开奖结果的连中长度按它校验，不需要为每种布局单独编解码。 */
    static final int MAX_REEL_COUNT;

    static {
        int max = 0;
        for (SlotMachineLayout layout : VALUES) {
            max = Math.max(max, layout.reelCount);
        }
        MAX_REEL_COUNT = max;
    }

    private final int id;
    private final int reelCount;
    private final int visibleRows;
    private final int spinTicks;
    private final Payline[] paylines;

    SlotMachineLayout(int id, int reelCount, int visibleRows, int spinTicks, Payline... paylines) {
        this.id = id;
        this.reelCount = reelCount;
        this.visibleRows = visibleRows;
        this.spinTicks = spinTicks;
        this.paylines = paylines;
    }

    /** 一局盘面的格子总数。 */
    int symbolCount() {
        return reelCount * visibleRows;
    }

    /** 盘面按行存储：第 {@code row} 行第 {@code reel} 列的格子下标。 */
    int symbolIndex(int reel, int row) {
        if (reel < 0 || reel >= reelCount) throw new IndexOutOfBoundsException(reel);
        if (row < 0 || row >= visibleRows) throw new IndexOutOfBoundsException(row);
        return row * reelCount + reel;
    }

    /** 从稳定 id 还原布局；非法 id（旧档或损坏数据）回退到经典盘面，读档不失败。 */
    static SlotMachineLayout byId(int id) {
        return id < 0 || id >= VALUES.length ? CLASSIC : VALUES[id];
    }

    /**
     * 中奖线几何：每条线记录自己在每根卷轴上取的行号（0 上、1 中、2 下）。
     *
     * <p>
     * {@link #id()} 是稳定存档 id，与布局 id 一起决定读档结果，只能追加新的线；未知 id 回退到中线。
     */
    enum Payline {

        TOP(0, 0, 0, 0),
        MIDDLE(1, 1, 1, 1),
        BOTTOM(2, 2, 2, 2),
        DIAGONAL_DOWN(3, 0, 1, 2),
        DIAGONAL_UP(4, 2, 1, 0),
        WIDE_MIDDLE(5, 1, 1, 1, 1, 1),
        WIDE_TOP(6, 0, 0, 0, 0, 0),
        WIDE_BOTTOM(7, 2, 2, 2, 2, 2),
        WIDE_V(8, 0, 1, 2, 1, 0),
        WIDE_LAMBDA(9, 2, 1, 0, 1, 2),
        WIDE_TOP_NOTCH(10, 0, 0, 1, 0, 0),
        WIDE_BOTTOM_NOTCH(11, 2, 2, 1, 2, 2),
        WIDE_TOP_EDGES(12, 1, 0, 0, 0, 1),
        WIDE_BOTTOM_EDGES(13, 1, 2, 2, 2, 1);

        private static final Payline[] VALUES = values();

        private final int id;
        private final int[] rows;

        Payline(int id, int... rows) {
            this.id = id;
            this.rows = rows;
        }

        int id() {
            return id;
        }

        int rowCount() {
            return rows.length;
        }

        /** 这条线在第 {@code reel} 根卷轴上取的行号。 */
        int row(int reel) {
            return rows[reel];
        }

        static Payline byId(int id) {
            // 非法 id 回退到中线，保证读档不失败
            return id < 0 || id >= VALUES.length ? MIDDLE : VALUES[id];
        }
    }
}

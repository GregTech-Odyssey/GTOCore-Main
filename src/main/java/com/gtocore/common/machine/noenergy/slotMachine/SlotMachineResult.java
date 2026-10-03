package com.gtocore.common.machine.noenergy.slotMachine;

import com.gtocore.common.machine.noenergy.slotMachine.SlotMachineLayout.Payline;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Objects;

/**
 * 一次老虎机开奖的完整结果。
 *
 * <p>
 * 不可变快照：构造时复制数组，只按格提供读取，避免机器或界面误改历史结果。{@link #bet}、{@link #reward}
 * 由 Lombok 生成同名取值方法，盘面与中奖线则刻意不生成，只能经 {@link #symbolAt(int, int)} /
 * {@link #isWinningCell(int, int)} 读取。
 *
 * <p>
 * 结果带着产生它的 {@link SlotMachineLayout}：盘面尺寸与中奖线走向都按布局读取。布局只以 id 写进载荷，
 * 因此读档时不需要机器实例就能还原盘面，换布局也不会让旧结果错位。
 */
public final class SlotMachineResult {

    /**
     * 盘面与线奖共用的编解码器；包内可见，{@link SlotMachineState} 在它外面再包一层空值表达。
     *
     * <p>
     * 布局 id 是最前面的一个分量：读档时没有机器实例，只能靠它还原盘面尺寸。分量顺序就是存档布局，
     * 再改一次就要连同旧档一起处理。
     */
    static final DataSyncCodec<SlotMachineResult> CODEC = CombinedCodec.composite(
            DataSyncCodec.INT_CODEC, result -> result.layout.id(),
            DataSyncCodec.INT_CODEC, SlotMachineResult::bet,
            DataSyncCodec.LONG_CODEC, SlotMachineResult::reward,
            DataSyncCodec.INTS_CODEC, result -> {
                int[] ids = new int[result.symbols.length];
                for (int i = 0; i < ids.length; i++) {
                    ids[i] = result.symbols[i].id();
                }
                return ids;
            },
            CombinedCodec.array(WinningLine.class, WinningLine.CODEC), result -> result.winningLines,
            SlotMachineResult::fromEncoded);

    @Getter
    @Accessors(fluent = true)
    private final int bet;
    @Getter
    @Accessors(fluent = true)
    private final long reward;
    private final SlotMachineLayout layout;
    private final SlotSymbol[] symbols;
    private final WinningLine[] winningLines;

    /**
     * {@code symbols} 必须刚好是布局的格子数（排列见 {@link SlotMachineLayout#symbolIndex(int, int)}）；
     * 中奖线只复制前 {@code lineCount} 项。
     */
    SlotMachineResult(SlotMachineLayout layout, int bet, long reward, SlotSymbol[] symbols, WinningLine[] winningLines, int lineCount) {
        if (bet < SlotMachineRules.DEFAULT_BET) throw new IllegalArgumentException("bet is below the minimum");
        if (reward < 0) throw new IllegalArgumentException("reward must not be negative");
        Objects.requireNonNull(symbols, "symbols");
        Objects.requireNonNull(winningLines, "winningLines");
        if (symbols.length != layout.symbolCount()) {
            throw new IllegalArgumentException("visible symbol count must match the layout");
        }
        this.bet = bet;
        this.reward = reward;
        this.layout = layout;
        this.symbols = Arrays.copyOf(symbols, symbols.length);
        for (SlotSymbol symbol : this.symbols) {
            Objects.requireNonNull(symbol, "symbol");
        }
        if (lineCount < 0 || lineCount > winningLines.length) throw new IllegalArgumentException("Invalid winning line count");
        this.winningLines = Arrays.copyOf(winningLines, lineCount);
        for (WinningLine line : this.winningLines) {
            Objects.requireNonNull(line, "line");
            if (line.payline().rowCount() != layout.reelCount()) {
                throw new IllegalArgumentException("winning line must match the layout");
            }
        }
    }

    SlotMachineLayout layout() {
        return layout;
    }

    public boolean hasWin() {
        return reward > 0;
    }

    public SlotSymbol symbolAt(int reel, int row) {
        return symbols[layout.symbolIndex(reel, row)];
    }

    /** 二连只点亮左侧两格；多条中奖线取并集。 */
    public boolean isWinningCell(int reel, int row) {
        for (WinningLine line : winningLines) {
            if (line.reward() > 0 && reel < line.matchCount() && line.payline().row(reel) == row) return true;
        }
        return false;
    }

    /** 与其他机器的自定义数据类型一起，在字段扫描前注册。 */
    public static void addCodec() {
        CODEC.register(SlotMachineResult.class);
    }

    /** 先还原布局，再按布局补齐盘面；跨布局或损坏的中奖线在补齐时剔除，不让它带进展示与结算。 */
    private static SlotMachineResult fromEncoded(int layoutId, int bet, long reward, int[] encodedSymbols, WinningLine[] lines) {
        SlotMachineLayout layout = SlotMachineLayout.byId(layoutId);
        SlotSymbol[] loadedSymbols = new SlotSymbol[layout.symbolCount()];
        for (int i = 0; i < loadedSymbols.length; i++) {
            loadedSymbols[i] = i < encodedSymbols.length ? SlotSymbol.byId(encodedSymbols[i]) : SlotSymbol.SYMBOL0;
        }
        int lineCount = 0;
        for (WinningLine line : lines) {
            if (line.payline().rowCount() == layout.reelCount()) lines[lineCount++] = line;
        }
        return new SlotMachineResult(layout, Math.max(SlotMachineRules.DEFAULT_BET, bet), Math.max(0L, reward), loadedSymbols, lines, lineCount);
    }

    /**
     * 单条中奖线的结算结果。
     *
     * @param symbol     本线按哪个符号结算；百搭会被换成它替代的目标符号
     * @param matchCount 匹配数量，从最左列数起；只有赔付表里不为 0 的长度才会生成中奖线
     * @param reward     本线奖励 = 下注 × 倍率；读档时不重算
     */
    record WinningLine(Payline payline, SlotSymbol symbol, int matchCount, int multiplier, long reward) {

        private static final DataSyncCodec<WinningLine> CODEC = CombinedCodec.composite(
                DataSyncCodec.INT_CODEC, line -> line.payline.id(),
                DataSyncCodec.INT_CODEC, line -> line.symbol.id(),
                DataSyncCodec.INT_CODEC, WinningLine::matchCount,
                DataSyncCodec.INT_CODEC, WinningLine::multiplier,
                DataSyncCodec.LONG_CODEC, WinningLine::reward,
                (payline, symbol, count, multiplier, reward) -> new WinningLine(
                        Payline.byId(payline), SlotSymbol.byId(symbol), count, Math.max(1, multiplier), Math.max(0L, reward)));

        /** 校验由规则或 codec 还原出的中奖线，避免非法数据继续参与展示或发奖。 */
        WinningLine {
            if (payline == null) throw new IllegalArgumentException("payline must not be null");
            if (symbol == null) throw new IllegalArgumentException("symbol must not be null");
            if (matchCount < 2 || matchCount > SlotMachineLayout.MAX_REEL_COUNT) {
                throw new IllegalArgumentException("matchCount must fit a layout");
            }
            if (multiplier <= 0) throw new IllegalArgumentException("multiplier must be positive");
            if (reward < 0) throw new IllegalArgumentException("reward must not be negative");
        }
    }
}

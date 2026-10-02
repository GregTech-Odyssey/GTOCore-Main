package com.gtocore.common.machine.noenergy.slotMachine;

import com.gtocore.common.machine.noenergy.slotMachine.SlotMachineRules.Payline;

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
 */
public final class SlotMachineResult {

    /** 盘面与线奖共用的编解码器；包内可见，{@link SlotMachineState} 在它外面再包一层空值表达。 */
    static final DataSyncCodec<SlotMachineResult> CODEC = CombinedCodec.composite(
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
            SlotMachineResult::fromEncodedSymbols);

    @Getter
    @Accessors(fluent = true)
    private final int bet;
    @Getter
    @Accessors(fluent = true)
    private final long reward;
    private final SlotSymbol[] symbols;
    private final WinningLine[] winningLines;

    /**
     * {@code symbols} 必须刚好 9 个（排列见 {@link SlotMachineRules#symbolIndex(int, int)}）；
     * 中奖线只复制前 {@code lineCount} 项。
     */
    SlotMachineResult(int bet, long reward, SlotSymbol[] symbols, WinningLine[] winningLines, int lineCount) {
        if (bet < SlotMachineRules.DEFAULT_BET) throw new IllegalArgumentException("bet is below the minimum");
        if (reward < 0) throw new IllegalArgumentException("reward must not be negative");
        Objects.requireNonNull(symbols, "symbols");
        Objects.requireNonNull(winningLines, "winningLines");
        if (symbols.length != SlotMachineRules.SYMBOL_COUNT) {
            throw new IllegalArgumentException("visible symbol count must be 9");
        }
        this.bet = bet;
        this.reward = reward;
        this.symbols = Arrays.copyOf(symbols, symbols.length);
        for (SlotSymbol symbol : this.symbols) {
            Objects.requireNonNull(symbol, "symbol");
        }
        if (lineCount < 0 || lineCount > winningLines.length) throw new IllegalArgumentException("Invalid winning line count");
        this.winningLines = Arrays.copyOf(winningLines, lineCount);
        for (WinningLine line : this.winningLines) {
            Objects.requireNonNull(line, "line");
        }
    }

    public boolean hasWin() {
        return reward > 0;
    }

    public SlotSymbol symbolAt(int reel, int row) {
        return symbols[SlotMachineRules.symbolIndex(reel, row)];
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

    private static SlotMachineResult fromEncodedSymbols(int bet, long reward, int[] encodedSymbols, WinningLine[] lines) {
        SlotSymbol[] loadedSymbols = new SlotSymbol[SlotMachineRules.SYMBOL_COUNT];
        for (int i = 0; i < loadedSymbols.length; i++) {
            loadedSymbols[i] = i < encodedSymbols.length ? SlotSymbol.byId(encodedSymbols[i]) : SlotSymbol.SWEET_BERRIES;
        }
        return new SlotMachineResult(Math.max(SlotMachineRules.DEFAULT_BET, bet), Math.max(0L, reward), loadedSymbols, lines, lines.length);
    }

    /**
     * 单条中奖线的结算结果。
     *
     * @param symbol     本线按哪个符号结算；百搭会被换成它替代的目标符号
     * @param matchCount 匹配数量，当前规则只会是 2 或 3
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
            if (matchCount < 2 || matchCount > SlotMachineRules.REEL_COUNT) {
                throw new IllegalArgumentException("matchCount must be 2 or 3");
            }
            if (multiplier <= 0) throw new IllegalArgumentException("multiplier must be positive");
            if (reward < 0) throw new IllegalArgumentException("reward must not be negative");
        }
    }
}

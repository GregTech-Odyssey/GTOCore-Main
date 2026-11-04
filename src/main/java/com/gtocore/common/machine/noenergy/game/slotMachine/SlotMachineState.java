package com.gtocore.common.machine.noenergy.game.slotMachine;

import net.minecraft.util.RandomSource;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * 单台老虎机的运行状态：下注、滚动倒计时、待结算结果与上一局结果；滚动中即 pendingResult != null。
 * 整份状态由 {@link #CODEC} 编成一个值，经 {@link #addCodec()} 在 {@code GTOCodecs.init()} 注册，
 * 机器只在 {@code state} 字段上写 {@code @SaveToDisk}，状态因此只有这一条存盘路径。
 * CODEC 首分量是旧存档余额，读档时由机器经 {@link #takeLegacyBalance()} 搬入基类余额，新存档写 0；余额本身不在这里。
 * 不要再改成嵌套 holder（编码规范第 30 条）：那样状态会自己维护一套字段管理器，多出一条持久化路径。
 * 开奖规则由机器注入（不参与存档），所以 3 列与 5 列共用同一个状态类，读档只依赖结果里自带的布局 id。
 */
@Getter
@Accessors(fluent = true)
public final class SlotMachineState {

    /**
     * 整份状态的编解码器；分量顺序即存档布局，改动要连旧档一起处理。
     * 两个结果字段用 {@link CombinedCodec#optional(CombinedCodec)} 表达可空——{@link SlotMachineResult#CODEC}
     * 收到 null 会在取字段时抛 {@code NullPointerException}，空值只能由这层承担。
     */
    private static final DataSyncCodec<SlotMachineState> CODEC = CombinedCodec.composite(
            DataSyncCodec.LONG_CODEC, SlotMachineState::legacyBalance,
            DataSyncCodec.INT_CODEC, SlotMachineState::bet,
            DataSyncCodec.INT_CODEC, SlotMachineState::spinTicksRemaining,
            SlotMachineResult.CODEC.optional(), SlotMachineState::pendingResult,
            SlotMachineResult.CODEC.optional(), SlotMachineState::lastResult,
            SlotMachineState::new);

    /** 旧存档里的余额：读档时由机器搬到基类余额上，新存档一律写 0。 */
    private long legacyBalance;
    private int bet;
    /** 只给 {@link #CODEC} 存取。 */
    @Getter(AccessLevel.PRIVATE)
    private int spinTicksRemaining;
    /** 只给 {@link #CODEC} 存取。 */
    @Getter(AccessLevel.PRIVATE)
    private SlotMachineResult pendingResult;
    private SlotMachineResult lastResult;

    public SlotMachineState() {
        this.bet = SlotMachineRules.DEFAULT_BET;
    }

    /** 解码专用构造：夹紧存下来的数值（没有待结算结果即不在滚动），已保存的盘面与奖励不重算。 */
    private SlotMachineState(long legacyBalance, int bet, int spinTicksRemaining, SlotMachineResult pendingResult,
                             SlotMachineResult lastResult) {
        this.legacyBalance = Math.max(0L, legacyBalance);
        this.bet = SlotMachineRules.clampBet(bet);
        this.pendingResult = pendingResult;
        this.spinTicksRemaining = pendingResult == null ? 0 : Math.max(0, spinTicksRemaining);
        this.lastResult = lastResult;
    }

    /** 与其它自定义类型一起，在字段扫描前注册。 */
    public static void addCodec() {
        CODEC.register(SlotMachineState.class);
    }

    public boolean isSpinning() {
        return pendingResult != null;
    }

    /** 取出旧存档余额并清零：余额已搬到机器上，留着会在下次读档时重复加上。 */
    public long takeLegacyBalance() {
        long legacy = legacyBalance;
        legacyBalance = 0L;
        return legacy;
    }

    /** 滚动中不接受改下注，避免结果已预生成后结算基准被改。 */
    public void setBet(int bet) {
        if (isSpinning()) return;
        this.bet = SlotMachineRules.clampBet(bet);
    }

    /** 现在能不能开一局：没在滚动，且余额够这一注（余额在机器上，由调用方传入）。 */
    public boolean canStart(long balance) {
        return !isSpinning() && balance >= bet;
    }

    /**
     * 按传入规则预生成待结算结果（有结果即滚动中）；结果先落地，动画中途卸载或存档也不会丢或重随机。
     * 下注由机器在调用前扣好，这里只管盘面与倒计时。
     */
    public boolean startSpin(RandomSource random, SlotMachineRules rules) {
        Objects.requireNonNull(random, "random");
        if (isSpinning()) return false;
        pendingResult = rules.spin(random, bet);
        spinTicksRemaining = rules.spinTicks();
        return true;
    }

    /** 每 tick 推进一次倒计时；返回本 tick 是否刚好结束并完成结算（奖励由机器按 {@link #lastResult()} 入账）。 */
    public boolean tickSpin() {
        if (!isSpinning()) return false;
        if (--spinTicksRemaining > 0) return false;
        lastResult = pendingResult;
        pendingResult = null;
        spinTicksRemaining = 0;
        return true;
    }
}

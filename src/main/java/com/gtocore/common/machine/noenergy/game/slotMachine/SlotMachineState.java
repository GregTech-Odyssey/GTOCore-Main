package com.gtocore.common.machine.noenergy.game.slotMachine;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;
import com.gto.datasynclib.datastream.codec.ValueOps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 单台老虎机的运行状态：下注、滚动倒计时、预生成的待结算结果和上一局结果。是否在滚动只看待结算结果是否存在。
 * 余额不在这里：它挂在 {@code AbstractGameMachine} 上，由机器负责加减（下注、中奖、投币入账）。
 *
 * <p>
 * 状态是自定义元素：{@link #CODEC} 把整份状态编解码成一个值，{@link #addCodec()} 在 {@code GTOCodecs.init()}
 * 里注册，机器侧只在 {@code state} 字段上写 {@code @SaveToDisk}，区块存盘因此只有这一条路径（机器物品不再保存状态，
 * 拆除时余额由机器换成硬币掉回世界）。
 * 不要再改成嵌套 holder（编码规范第 27 条）：那样状态会自己维护一套字段管理器，多出一条持久化路径，
 * 还得靠 {@code listener} 字符串回调。读回来的数值统一在解码构造里校验，不需要「读盘完成」回调。
 *
 * <p>
 * 余额搬走之后，{@code CODEC} 的第一个分量留着原来的位置不删：它是旧存档里的余额，读档时由机器用
 * {@link #takeLegacyBalance()} 搬到基类的余额字段上，新存档一律写 0。分量顺序就是存档布局，改动要连着旧档一起考虑。
 *
 * <p>
 * 开奖用的规则由机器在构造时注入（{@link #startSpin} 的 {@code rules} 参数，不参与存档）：卷轴带与赔付表
 * 由规则持有，状态只保存结果。因此 3 列与 5 列盘面共用同一个状态类，读档只依赖结果里自带的布局 id。
 * 取值方法由 Lombok 按 fluent 命名生成、与字段同名；{@code pendingResult}、{@code spinTicksRemaining}
 * 只给 {@link #CODEC} 存取。
 */
@Getter
@Accessors(fluent = true)
public final class SlotMachineState {

    /**
     * 可为空的开奖结果编解码器：网络侧先写一位存在标记，存盘侧用 {@code ops.createNull()} 当空值。
     *
     * <p>
     * {@link SlotMachineResult#CODEC} 描述的是完整一局，null 传进去会在取字段时抛
     * {@link NullPointerException}，所以「这一局不存在」必须在这里显式表达。
     */
    private static final CombinedCodec<SlotMachineResult> OPTIONAL_RESULT_CODEC = new CombinedCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, SlotMachineResult obj) {
            buf.writeBoolean(obj != null);
            if (obj != null) SlotMachineResult.CODEC.encode(buf, obj);
        }

        @Override
        public SlotMachineResult decode(FriendlyByteBuf buf) {
            return buf.readBoolean() ? SlotMachineResult.CODEC.decode(buf) : null;
        }

        @Override
        public @NotNull Object encode(ValueOps ops, SlotMachineResult obj) {
            return obj == null ? ops.createNull() : SlotMachineResult.CODEC.encode(ops, obj);
        }

        @Override
        public SlotMachineResult decode(ValueOps ops, @NotNull Object data) {
            return ops.isNull(data) ? null : SlotMachineResult.CODEC.decode(ops, data);
        }
    };

    /** 整份状态的编解码器；分量顺序就是存档布局，改动要连着旧档一起考虑。 */
    private static final DataSyncCodec<SlotMachineState> CODEC = CombinedCodec.composite(
            DataSyncCodec.LONG_CODEC, SlotMachineState::legacyBalance,
            DataSyncCodec.INT_CODEC, SlotMachineState::bet,
            DataSyncCodec.INT_CODEC, SlotMachineState::spinTicksRemaining,
            OPTIONAL_RESULT_CODEC, SlotMachineState::pendingResult,
            OPTIONAL_RESULT_CODEC, SlotMachineState::lastResult,
            SlotMachineState::new);

    /** 旧存档里的余额：读档时由机器搬到基类的余额字段上，新存档一律写 0。 */
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

    /** 解码专用构造：夹紧存下来的数值（没有待结算结果就没有在滚动），已保存的盘面与奖励不重算。 */
    private SlotMachineState(long legacyBalance, int bet, int spinTicksRemaining, SlotMachineResult pendingResult,
                             SlotMachineResult lastResult) {
        this.legacyBalance = Math.max(0L, legacyBalance);
        this.bet = SlotMachineRules.clampBet(bet);
        this.pendingResult = pendingResult;
        this.spinTicksRemaining = pendingResult == null ? 0 : Math.max(0, spinTicksRemaining);
        this.lastResult = lastResult;
    }

    /** 与其他机器的自定义数据类型一起，在字段扫描前注册。 */
    public static void addCodec() {
        CODEC.register(SlotMachineState.class);
    }

    public boolean isSpinning() {
        return pendingResult != null;
    }

    /** 取出旧存档里的余额并清零：余额已经搬到机器上，留着会在下次读档时被重复加上。 */
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

    /** 现在能不能开一局：没在滚动，且余额够这一注（余额在机器上，由调用方传进来判定）。 */
    public boolean canStart(long balance) {
        return !isSpinning() && balance >= bet;
    }

    /**
     * 按传入规则预生成待结算结果（有待结算结果即滚动中）；结果先落地，动画中途卸载或存档也不会丢或重随机。
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

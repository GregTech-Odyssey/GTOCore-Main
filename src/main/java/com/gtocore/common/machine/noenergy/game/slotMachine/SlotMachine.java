package com.gtocore.common.machine.noenergy.game.slotMachine;

import com.gtocore.common.data.GTOEffects;
import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
import com.gtocore.common.machine.noenergy.game.milestone.GameAccess;
import com.gtocore.common.machine.noenergy.game.milestone.GameMilestones;
import com.gtocore.config.GTOConfig;
import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.CoinSeries;
import com.gtocore.data.transaction.data.TradeLang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.animation.UIClock;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ItemView;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.texture.DynamicTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

/**
 * 基于 {@link SlotMachineState} 的老虎机机器，余额、取出、权限、会话与里程碑都来自 {@link AbstractGameMachine}。
 *
 * <p>
 * 本类只处理机器层职责：服务端状态、tick 订阅、DataSyncLib 字段存盘、拆除掉落与 Fancy UI；
 * 开奖、赔率与结果结构都在同包的底层逻辑类里。收付用的是技术员币（{@link #currency()}），往硬币槽投币即入账，
 * 拆机器时槽里的硬币原样掉出、余额按面额换成实体币一起掉回世界。
 *
 * <p>
 * 盘面由构造时注入的 {@link SlotMachineRules} 决定（列数、行数、卷轴与赔付表），界面按规则的布局铺格子，
 * 所以同一个类同时驱动 3 列与 5 列老虎机；规则不参与存档，读档时由方块的机器工厂重新注入。
 */
@DataGeneratorScanned
public class SlotMachine extends AbstractGameMachine<SlotMachine.Snapshot> {

    @RegisterLanguage(cn = "上一局奖励", en = "Last payout")
    private static final String LINE_LAST_REWARD = "gtocore.machine.slot_machine.line.last_reward";
    @RegisterLanguage(cn = "下注数量", en = "Bet amount")
    private static final String ROW_BET = "gtocore.machine.slot_machine.row.bet";
    @RegisterLanguage(cn = "下注会在开始滚动时扣除，滚动结束后按中奖线返还奖励", en = "The bet is consumed when a spin starts; payout is returned after the spin")
    private static final String ROW_BET_TOOLTIP = "gtocore.machine.slot_machine.row.bet.tooltip";
    @RegisterLanguage(cn = "开始", en = "Spin")
    private static final String BUTTON_SPIN = "gtocore.machine.slot_machine.button.spin";
    @RegisterLanguage(cn = "当前无法开始：机器关闭、正在滚动或余额不足", en = "Cannot start: disabled, spinning, or insufficient credits")
    private static final String REASON_CANNOT_START = "gtocore.machine.slot_machine.reason.cannot_start";
    @RegisterLanguage(cn = "正在滚动，暂不能取出余额", en = "Cannot withdraw while spinning")
    private static final String REASON_SPINNING = "gtocore.machine.slot_machine.reason.spinning";
    @RegisterLanguage(cn = "当前无法开始：服务器已禁用老虎机", en = "Cannot start: the slot machine is disabled on this server")
    private static final String REASON_DISABLED = "gtocore.machine.slot_machine.reason.disabled";
    @RegisterLanguage(cn = "老虎机已被禁用", en = "Slot machine disabled")
    private static final String NOTICE_DISABLED_TITLE = "gtocore.machine.slot_machine.notice.disabled.title";
    @RegisterLanguage(cn = "本服务器已关闭老虎机，禁止赌博；余额仍可取出", en = "This server has disabled the slot machine; credits can still be withdrawn")
    private static final String NOTICE_DISABLED_DETAIL = "gtocore.machine.slot_machine.notice.disabled.detail";
    @RegisterLanguage(cn = "无", en = "None")
    private static final String VALUE_NONE = "gtocore.machine.slot_machine.value.none";
    @RegisterLanguage(cn = "%s (倍率 ×%s)", en = "%s (multiplier ×%s)")
    private static final String VALUE_REWARD = "gtocore.machine.slot_machine.value.reward";
    @RegisterLanguage(cn = "中奖附赠：%s", en = "Bonus on win: %s")
    private static final String MESSAGE_BOOST_GRANTED = "gtocore.machine.slot_machine.message.boost_granted";
    @RegisterLanguage(cn = "次数", en = "Spins")
    private static final String COLUMN_SPINS = "gtocore.machine.slot_machine.column.spins";
    @RegisterLanguage(cn = "投入", en = "Staked")
    private static final String COLUMN_STAKED = "gtocore.machine.slot_machine.column.staked";
    @RegisterLanguage(cn = "获得", en = "Won")
    private static final String COLUMN_WON = "gtocore.machine.slot_machine.column.won";
    @RegisterLanguage(cn = "回报比", en = "Return")
    private static final String COLUMN_RATIO = "gtocore.machine.slot_machine.column.ratio";

    /** 里程碑表格的列（名次由表格自动加在最前）：玩家 + 次数、投入、获得与回报比。 */
    private static final MilestoneColumn[] MILESTONE_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 42),
            MilestoneColumn.right(COLUMN_SPINS, 40),
            MilestoneColumn.right(COLUMN_STAKED, 52),
            MilestoneColumn.right(COLUMN_WON, 52),
            MilestoneColumn.right(COLUMN_RATIO, 38) };

    /** 中奖后附赠「机械之神降临」的概率。 */
    public static final double BOOST_CHANCE = 0.3;
    /**
     * 下注在效果档位里的权重，剩下的给倍率。倍率决定大方向，投注额度把同一倍率的效果再拉开一截——
     * 小额小奖只给脚下那台机器加个速，豪赌的大奖才能顶到满级。
     */
    private static final double BOOST_BET_WEIGHT = 0.3;
    /** 效果上限：14 级（与满档监工可乐同档，能加速到 GT tier 14 的配方）、15 分钟；下限 1 级、半分钟。 */
    public static final int BOOST_MAX_AMPLIFIER = 14;
    private static final int BOOST_MIN_AMPLIFIER = 1;
    private static final int BOOST_MIN_DURATION = 20 * 30;
    public static final int BOOST_MAX_DURATION = 20 * 60 * 15;
    /** 档位过半时换成「大瓶」的范围版（5×5×5）；低倍率只给自己脚下那台机器加速。 */
    private static final double BOOST_LARGE_THRESHOLD = 0.5;
    /** 同一档位内的随机浮动：倍率相近的两局也可能差一档。 */
    private static final double BOOST_JITTER = 0.1;
    /**
     * 档位曲线：加权后的档位再取这个幂。指数越大，中低档被压得越狠，满档不受影响（{@code 1^n = 1}）。
     * 当前 2.5 档把奖励整体压下一大截：最低档（×2 + 最小注）落在 1 级、约半分钟，中档 2~6 级，
     * 只有满注 + 本机最高倍率才摸得到 14 级 / 15 分钟。
     */
    private static final double BOOST_CURVE = 2.5;

    private static final long[] CREDIT_STEPS = { 5, 25, 625 };
    private static final int REEL_ICON_SIZE = 24;
    private static final int REEL_CELL_PADDING = 2;
    private static final int REEL_CELL_SIZE = REEL_ICON_SIZE + REEL_CELL_PADDING * 2;
    // 卷轴格子共用一个同步值：低 8 位是符号 id，第 8 位是中奖标记；-1/-2 是空位与滚动哨兵。
    private static final int REEL_EMPTY = -1;
    private static final int REEL_SPINNING = -2;
    private static final int REEL_WINNING_FLAG = 1 << 8;
    private static final int WIN_SOUND_INTERVAL = 2;
    private static final float[] WIN_SOUND_PITCHES = { 1.0F, 1.25F, 1.5F, 1.25F, 1.5F, 1.75F, 1.5F, 2.0F };

    // 状态是自定义元素：SlotMachineState 为整份状态注册了自己的 codec，DataSyncLib 按字段类型找到它，
    // 字段不能是 final：final 字段只查 access 工厂（嵌套 holder、容器），不查 codec 工厂，会在字段扫描期
    // 直接抛异常；非 final 才允许读盘时整体替换实例。
    @SaveToDisk
    private SlotMachineState state = new SlotMachineState();
    // 硬币槽随机器存盘，槽里没换完的硬币不能丢。
    @SaveToDisk
    private final KeyInventory<AEItemKey> depositInventory = KeyInventory.items(1);
    private final Runnable machineTick = this::tickMachine;

    @Nullable
    private TickableSubscription tickSubs;
    private int winSoundTicksRemaining;
    /** 开这一局的人：中奖附赠的效果给他；不存盘，读档中断的一局就退回机器主人。 */
    @Nullable
    private UUID spinnerUUID;
    // 盘面规则由方块类型决定，不参与存档：读档时机器工厂会重新注入同一份规则。
    private final SlotMachineRules rules;

    public SlotMachine(MetaMachineBlockEntity holder, SlotMachineRules rules) {
        super(holder);
        this.rules = rules;
        depositInventory.setOnChanged(this::onDepositInventoryChanged);
    }

    /** 老虎机收付技术员币：投币入账、取出与拆除掉币都按它的面额来。 */
    @Override
    protected String currencyId() {
        return TradeLang.TECH_OPERATOR_COIN;
    }

    @Override
    protected CoinSeries currency() {
        return CoinExchange.techOperator();
    }

    /**
     * 通道里下发的界面快照：下注、上一局奖励与倍率；余额由基类的同步字段提供，不在这里重复。相等性只看版本号。
     */
    public record Snapshot(int revision, boolean hasResult, int bet, long lastReward, double lastRewardMultiplier) {

        private static final Snapshot EMPTY = new Snapshot(-1, false, SlotMachineRules.DEFAULT_BET, 0L, 0D);
        private static final StreamCodec<FriendlyByteBuf, Snapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Snapshot::revision,
                ByteBufCodecs.BOOL, Snapshot::hasResult,
                ByteBufCodecs.VAR_INT, Snapshot::bet,
                ByteBufCodecs.VAR_LONG, Snapshot::lastReward,
                ByteBufCodecs.DOUBLE, Snapshot::lastRewardMultiplier,
                Snapshot::new);

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof Snapshot snapshot && revision == snapshot.revision;
        }

        @Override
        public int hashCode() {
            return revision;
        }
    }

    @Override
    protected Snapshot createView() {
        var last = state.lastResult();
        return new Snapshot(revision(), last != null, state.bet(), last == null ? 0L : last.reward(),
                last == null ? 0D : last.reward() / (double) last.bet());
    }

    /** 状态一变就重排 tick 订阅：滚动中、还有中奖音效、或槽里有能入账的硬币时才需要每 tick 干活。 */
    @Override
    protected void onGameStateChanged() {
        if (!isRemote()) updateTickSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        migrateLegacyBalance();
        updateTickSubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        cancelTickSubscription();
        winSoundTicksRemaining = 0;
    }

    /** 拆除时硬币槽里的硬币原样掉出；余额由基类按面额换成实体币掉回世界。 */
    @Override
    public void onMachineRemoved() {
        clearInventory(depositInventory);
        super.onMachineRemoved();
    }

    // ==================== 余额迁移 ====================

    /** 旧存档的余额还压在 {@code state} 里：读档时搬到基类的余额字段上，并把那个位置清零免得下次重复搬。 */
    private void migrateLegacyBalance() {
        long legacy = state.takeLegacyBalance();
        if (legacy <= 0) return;
        addBalance(legacy);
        syncAndMarkChanged();
    }

    // ==================== tick ====================

    private void updateTickSubscription() {
        if (getLevel() == null || isRemote()) return;
        if (state.isSpinning() || winSoundTicksRemaining > 0 || canDeposit()) {
            tickSubs = subscribeServerTick(tickSubs, machineTick);
        } else {
            cancelTickSubscription();
        }
    }

    private void cancelTickSubscription() {
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    private void tickMachine() {
        depositCredits();
        if (state.tickSpin()) {
            SlotMachineResult result = state.lastResult();
            // 奖励先落进余额，再放音效与附赠效果：这一步之前余额里还没有这一局的钱
            if (result != null) addBalance(result.reward());
            if (result != null && result.hasWin()) {
                winSoundTicksRemaining = WIN_SOUND_PITCHES.length * WIN_SOUND_INTERVAL;
                grantBoost(result);
            }
            recordSpin(result);
            spinnerUUID = null;
            syncAndMarkChanged();
        } else if (state.isSpinning()) {
            // 倒计时也进入区块存盘，重载后从实际剩余时间继续。
            onChanged();
        }
        tickWinSound();
        updateTickSubscription();
    }

    /** 一次旋转结束：单人局才把次数、抽奖消耗（这一把的下注）与赢得记进里程碑。 */
    private void recordSpin(@Nullable SlotMachineResult result) {
        if (!sessionCounts()) return;
        var milestones = milestones();
        if (milestones == null) return;
        milestones.addSpin(sessionStarter(), result == null ? 0L : result.bet(),
                result == null ? 0L : result.reward());
    }

    private void tickWinSound() {
        if (winSoundTicksRemaining <= 0) return;
        int elapsed = WIN_SOUND_PITCHES.length * WIN_SOUND_INTERVAL - winSoundTicksRemaining;
        if (elapsed % WIN_SOUND_INTERVAL == 0 && getLevel() instanceof ServerLevel level) {
            level.playSound(null, getPos(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS,
                    1.0F, WIN_SOUND_PITCHES[elapsed / WIN_SOUND_INTERVAL]);
        }
        winSoundTicksRemaining--;
    }

    // ==================== 玩法 ====================

    /** 服务器可以在配置里关闭老虎机：关闭后只显示禁赌提示，且永远不能开始（余额与硬币槽照常可用）。 */
    private static boolean slotMachineEnabled() {
        return GTOConfig.INSTANCE.gamePlay.slotMachineEnabled;
    }

    /** 现在能不能开一局：没在滚动，且余额够这一注。 */
    private boolean canStart() {
        return state.canStart(balance());
    }

    private void startSpin(@Nullable ServerPlayer player) {
        var level = getLevel();
        if (level == null || isRemote() || !slotMachineEnabled() || !guard(player)) return;
        if (!canStart()) return;
        // 先预生成结果再扣注：canStart 已经拦过滚动中与余额不足，这里不会再失败
        if (!state.startSpin(level.getRandom(), rules)) return;
        removeBalance(state.bet());
        spinnerUUID = player == null ? null : player.getUUID();
        beginSession(player);
        syncAndMarkChanged();
    }

    /**
     * 中奖附赠「机械之神降临」：{@link #BOOST_CHANCE} 的概率，等级与时长由这一局的倍率和下注一起决定。
     *
     * <p>
     * 倍率与下注各按对数压到 0~1 再加权（倍率占七成，见 {@link #BOOST_BET_WEIGHT}）：倍率从「随便中一点」到单线满档、
     * 下注从最小到最大，都会线性地铺满整个区间，不会出现「小倍率永远 0 级、大倍率永远满级」。算完再抖一档，
     * 封顶 {@link #BOOST_MAX_AMPLIFIER} 级 / {@link #BOOST_MAX_DURATION} tick（15 分钟）。
     *
     * <p>
     * 档位过半给「大瓶」的范围版（周围 5×5×5 的单方块机器都能加速），低档只给「小瓶」，作用在玩家脚下那一台。
     */
    private void grantBoost(SlotMachineResult result) {
        if (!(getLevel() instanceof ServerLevel level)) return;
        var random = level.getRandom();
        if (random.nextDouble() >= BOOST_CHANCE) return;
        var player = resolveSpinner(level);
        if (player == null) return;
        double multiplier = result.reward() / (double) result.bet();
        double multiplierQuality = Math.log(Math.max(1.0, multiplier)) / Math.log(Math.max(2.0, rules.maxLineMultiplier()));
        double betQuality = Math.log10(Math.max(1, result.bet())) / Math.log10(SlotMachineRules.DEFAULT_MAX_BET);
        double quality = (1 - BOOST_BET_WEIGHT) * Math.clamp(multiplierQuality, 0.0, 1.0) +
                BOOST_BET_WEIGHT * Math.clamp(betQuality, 0.0, 1.0);
        quality = Math.clamp(quality + (random.nextDouble() * 2 - 1) * BOOST_JITTER, 0.0, 1.0);
        quality = Math.pow(quality, BOOST_CURVE);
        int amplifier = Math.clamp((int) Math.floor(quality * BOOST_MAX_AMPLIFIER), BOOST_MIN_AMPLIFIER,
                BOOST_MAX_AMPLIFIER);
        int duration = BOOST_MIN_DURATION + (int) ((BOOST_MAX_DURATION - BOOST_MIN_DURATION) * quality);
        var effect = quality >= BOOST_LARGE_THRESHOLD ? GTOEffects.LARGE_MYSTERIOUS_BOOST.get() : GTOEffects.MYSTERIOUS_BOOST.get();
        var instance = new MobEffectInstance(effect, duration, amplifier, false, true);
        player.addEffect(instance);
        player.displayClientMessage(Component.translatable(MESSAGE_BOOST_GRANTED,
                Component.translatable(effect.getDescriptionId())), true);
    }

    /** 开这一局的人优先（还在线的话），否则退回机器主人；都不在线就不给。 */
    @Nullable
    private ServerPlayer resolveSpinner(ServerLevel level) {
        var server = level.getServer();
        var players = server.getPlayerList();
        if (spinnerUUID != null) {
            var spinner = players.getPlayer(spinnerUUID);
            if (spinner != null) return spinner;
        }
        var owner = getOwnerUUID();
        return owner == null ? null : players.getPlayer(owner);
    }

    // ==================== 投币 ====================

    private void onDepositInventoryChanged() {
        if (getLevel() == null || isRemote()) return;
        // 这里只调度；槽位容量探测和 Shift 转移完成后，才在服务端 tick 中消耗物品。
        onChanged();
        updateTickSubscription();
    }

    private boolean canDeposit() {
        CoinSeries coins = currency();
        int tier = coins.tierOf(Keys.displayStack(depositInventory.keyAt(0)));
        return tier >= 0 && Long.MAX_VALUE - balance() >= coins.value(tier);
    }

    private void depositCredits() {
        var key = depositInventory.keyAt(0);
        CoinSeries coins = currency();
        int tier = coins.tierOf(Keys.displayStack(key));
        if (tier < 0) return;
        // 没权限的玩家开着界面时别动槽里的币（锁机后仍留着他之前塞进去的硬币）。
        var player = viewer();
        if (player != null && !GameAccess.canPlay(this, allowOthers(), player)) return;
        long value = coins.value(tier);
        // 只接收能完整入账的硬币，不能吞掉一枚高面值币后只加剩余额度。
        long amount = Math.min(depositInventory.amountAt(0), (Long.MAX_VALUE - balance()) / value);
        if (amount <= 0) return;
        long deposited = depositInventory.extract(0, key, amount, false);
        if (deposited > 0) {
            addBalance(deposited * value);
            syncAndMarkChanged();
        }
    }

    private void setBet(int bet) {
        if (isRemote() || !guard(viewer())) return;
        state.setBet(bet);
        syncAndMarkChanged();
    }

    // ==================== 界面 ====================

    /** 关闭老虎机时，卷轴位置改为禁赌提示；其余控件（余额、硬币槽、取出）保持不变。 */
    private UIElement createReels() {
        if (!slotMachineEnabled()) return createDisabledNotice();
        SlotMachineLayout layout = rules.layout();
        // 物品材质只在开界面时构建一次，渲染与滚动期间复用。
        var textures = new ItemStackTexture[SlotSymbol.count()];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = new ItemStackTexture(SlotSymbol.symbolItem(SlotSymbol.byId(i)));
        }
        var board = UIElement.column(LayoutStyle.AUTO).layout(style -> style.alignCenter().gapAll(UISizes.GAP));
        int width = layout.reelCount() * REEL_CELL_SIZE + (layout.reelCount() - 1) * UISizes.GAP;
        for (int row = 0; row < layout.visibleRows(); row++) {
            var cells = UIElement.centeredRow(REEL_CELL_SIZE).layout(style -> style.width(width));
            for (int reel = 0; reel < layout.reelCount(); reel++) {
                cells.addChild(createReelCell(layout, reel, row, textures));
            }
            board.addChild(cells);
        }
        return board;
    }

    /** 禁用时的禁赌提示：固定文字，两端显示相同内容，不需要下发。 */
    private static UIElement createDisabledNotice() {
        var notice = UIElement.column(LayoutStyle.AUTO).layout(style -> style.alignCenter().gapAll(UISizes.GAP));
        notice.addChild(TextLine.translatable(LayoutStyle.AUTO, NOTICE_DISABLED_TITLE)
                .styled().bindClientColor(() -> UITheme.STATUS_TEXT_ERROR));
        notice.addChild(TextLine.translatable(LayoutStyle.AUTO, NOTICE_DISABLED_DETAIL)
                .bindClientColor(() -> UITheme.TEXT_SECONDARY));
        return notice;
    }

    private UIElement createReelCell(SlotMachineLayout layout, int reel, int row, ItemStackTexture[] textures) {
        var cell = new UIElement().layout(style -> style.size(REEL_CELL_SIZE, REEL_CELL_SIZE).paddingAll(REEL_CELL_PADDING));
        cell.setBackground(UITheme.ITEM_SLOT);
        var display = cell.addSyncValue(SyncValue.ofInt(() -> reelDisplayValue(reel, row), REEL_EMPTY));
        cell.setSelected(() -> {
            int value = display.getValue();
            return value >= 0 && (value & REEL_WINNING_FLAG) != 0;
        });
        var icon = ItemView.of(REEL_ICON_SIZE, new DynamicTexture(() -> {
            int id = display.getValue();
            // 滚动只是客户端装饰，不读取也没提前同步尚未结算的结果。
            if (id == REEL_SPINNING) {
                id = (int) ((UIClock.millis() / 100 + reel * layout.visibleRows() + row) % textures.length);
            }
            return id < 0 ? IGuiTexture.EMPTY : textures[id & ~REEL_WINNING_FLAG];
        }));
        cell.addChild(icon);
        return cell;
    }

    @Override
    public Widget createUIWidget() {
        var reels = new ReelPanel();

        var status = new StatusPanel();
        status.addLine(LINE_BALANCE, () -> number(balance()));
        status.addLine(LINE_LAST_REWARD, this::lastRewardText);

        var spinButton = Button.translatable(LayoutStyle.AUTO, BUTTON_SPIN)
                .disabled(() -> !slotMachineEnabled() || !canStart(),
                        slotMachineEnabled() ? REASON_CANNOT_START : REASON_DISABLED);
        spinButton.setOnServerClick(() -> startSpin(playerOf(spinButton)));

        var credits = UIElement.centeredRow(UISizes.SLOT_SIZE).layout(layout -> layout.justifyContent(AlignContent.CENTER));
        var deposit = ItemSlot.of(depositInventory, 0, true, true);
        deposit.setGhosts(currency().itemStacks())
                .setChangeListener(this::onDepositInventoryChanged)
                .setHoverTooltips(currencyName());
        var withdraw = Button.translatable(136, BUTTON_WITHDRAW)
                .disabled(() -> state.isSpinning(), REASON_SPINNING);
        withdraw.setOnServerClick(() -> {
            // 滚动中不取币：界面上按钮是禁用的，服务端再挡一次，免得网络请求绕过禁用态
            if (state.isSpinning() || !guard(playerOf(withdraw))) return;
            withdrawAll(withdraw);
        });
        withdraw.setHoverTooltips(currencyName());
        credits.addChildren(deposit, withdraw);

        status.addChildren(
                NumberField.ofInt(LayoutStyle.AUTO, () -> view().bet(), this::setBet, SlotMachineRules.DEFAULT_BET, SlotMachineRules.DEFAULT_MAX_BET)
                        .setSteps(CREDIT_STEPS).tooltips(ROW_BET, ROW_BET_TOOLTIP),
                credits,
                spinButton);

        return Form.page().addChildren(reels, status);
    }

    /** 主页之外挂「配置」与「排行榜」两个子页。 */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new ConfigPage());
        sideTabs.attachSubTab(new LeaderboardPage());
    }

    /** 配置页：游玩权限开关 + 余额；排行榜单独一页。 */
    private final class ConfigPage extends GamePage {

        private ConfigPage() {
            super(WidgetIcons.SETTINGS, PAGE_SETTINGS, PAGE_SETTINGS_TOOLTIP);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(allowOthersRow());
            page.addChild(balancePanel());
            return page;
        }
    }

    /** 排行榜页：回报比榜（按回报比降序，文字由服务端整表下发）。 */
    private final class LeaderboardPage extends SingleLeaderboardPage {

        private LeaderboardPage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected Widget boardContent() {
            var section = Form.section(SECTION_MILESTONES);
            section.addChild(milestoneTable("game_milestones_slot_machine", MILESTONE_COLUMNS,
                    SlotMachine::buildMilestoneRows));
            return section;
        }
    }

    /** 里程碑表格的行（服务端构建）：回报比榜，一人一行，名次由表格补。 */
    private static List<MilestoneCell[]> buildMilestoneRows(GameMilestones store, MinecraftServer server) {
        var board = store.slotBoard();
        var rows = new ObjectArrayList<MilestoneCell[]>(board.size());
        for (var stat : board) {
            rows.add(new MilestoneCell[] { MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.of(number(stat.spins())), MilestoneCell.of(number(stat.staked())),
                    MilestoneCell.of(number(stat.won())),
                    MilestoneCell.of(Component.literal(Math.round(GameMilestones.slotRatio(stat) * 100) + "%")) });
        }
        return rows;
    }

    @Override
    public boolean hasPlayerInventory() {
        return true;
    }

    private Component lastRewardText() {
        var snapshot = view();
        if (!snapshot.hasResult()) return Component.translatable(VALUE_NONE);
        return Component.translatable(VALUE_REWARD, number(snapshot.lastReward()),
                Component.literal(FormattingUtil.formatNumber2Places(snapshot.lastRewardMultiplier()))
                        .withStyle(ChatFormatting.GOLD));
    }

    private int reelDisplayValue(int reel, int row) {
        if (state.isSpinning()) return REEL_SPINNING;
        SlotMachineResult result = state.lastResult();
        if (result == null) return REEL_EMPTY;
        // 符号与中奖标记打包在同一个同步值里，避免两者显示错位。
        return result.symbolAt(reel, row).id() | (result.isWinningCell(reel, row) ? REEL_WINNING_FLAG : 0);
    }

    /**
     * 卷轴面板：同时兼作键盘入口，按空格等同于点「开始」，快照也挂在这一层上。
     *
     * <p>
     * 键盘事件由 LDLib 从界面根控件逐层向下派发（{@code WidgetGroup.keyPressed}），本元素在派发链上，所以不需要焦点；
     * 请求经 RPC 上行，服务端在 {@link #startSpin} 里照旧自己校验能不能开（禁赌、滚动中、余额不足都拦得住），
     * 因此这里不做客户端的预判，界面上的禁用态仍由「开始」按钮自己显示。
     */
    private final class ReelPanel extends StatusPanel {

        private final RPC<Unit> spin;

        private ReelPanel() {
            registerView(this, Snapshot.EMPTY, Snapshot.CODEC);
            addChildren(createReels());
            spin = addRPC(player -> startSpin(player instanceof ServerPlayer serverPlayer ? serverPlayer : null));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode != GLFW.GLFW_KEY_SPACE) return super.keyPressed(keyCode, scanCode, modifiers);
            spin.send(Unit.INSTANCE);
            return true;
        }
    }
}

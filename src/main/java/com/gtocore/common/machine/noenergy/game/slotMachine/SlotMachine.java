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
 * 老虎机：余额、取出、权限、会话与里程碑由 {@link AbstractGameMachine} 提供，本类只管服务端状态、tick、存盘与 UI。
 * 开奖、赔率与结果结构在同包底层类里。收付技术员币（{@link #currency()}），投币入槽即入账，拆机时槽里的硬币原样掉出。
 * 盘面由构造时注入的 {@link SlotMachineRules} 决定，同一个类因此同时驱动 3 列与 5 列机器；规则不参与存档，读档时由机器工厂重新注入。
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

    /** 里程碑表格的列；名次列由表格自动加在最前。 */
    private static final MilestoneColumn[] MILESTONE_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 42),
            MilestoneColumn.right(COLUMN_SPINS, 40),
            MilestoneColumn.right(COLUMN_STAKED, 52),
            MilestoneColumn.right(COLUMN_WON, 52),
            MilestoneColumn.right(COLUMN_RATIO, 38) };

    /** 中奖后附赠「机械之神降临」的概率。 */
    public static final double BOOST_CHANCE = 0.3;
    /** 下注在档位权重里的占比，其余给倍率。 */
    private static final double BOOST_BET_WEIGHT = 0.3;
    /** 效果上限：14 级（放大器即配方可加速的电压档上限）、15 分钟；下限 1 级、30 秒。 */
    public static final int BOOST_MAX_AMPLIFIER = 14;
    private static final int BOOST_MIN_AMPLIFIER = 1;
    private static final int BOOST_MIN_DURATION = 20 * 30;
    public static final int BOOST_MAX_DURATION = 20 * 60 * 15;
    /** 曲线后的档位过半时换成「大瓶」范围版（周围 5×5×5）；低档只作用于脚下那台。 */
    private static final double BOOST_LARGE_THRESHOLD = 0.5;
    /** 档位权重的随机抖动（±0.1），避免相近的两局完全同档。 */
    private static final double BOOST_JITTER = 0.1;
    /** 档位曲线的指数：加权档位再取这个幂，中低档被压得更狠，满档不变（{@code 1^n = 1}）。 */
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

    // 状态是自定义元素：SlotMachineState 自带 codec，DataSyncLib 按字段类型找到它。
    // 字段不能是 final：final 只查 access 工厂、不查 codec 工厂，且读档要整体替换实例。
    @SaveToDisk
    private SlotMachineState state = new SlotMachineState();
    // 硬币槽随机器存盘：槽里没入账的硬币不能丢。
    @SaveToDisk
    private final KeyInventory<AEItemKey> depositInventory = KeyInventory.items(1);
    private final Runnable machineTick = this::tickMachine;

    @Nullable
    private TickableSubscription tickSubs;
    private int winSoundTicksRemaining;
    /** 开这一局的人：附赠效果给他；不存盘，读档后回退到机器主人。 */
    @Nullable
    private UUID spinnerUUID;
    // 盘面规则由方块类型决定，不参与存档：读档时机器工厂重新注入。
    private final SlotMachineRules rules;

    public SlotMachine(MetaMachineBlockEntity holder, SlotMachineRules rules) {
        super(holder);
        this.rules = rules;
        depositInventory.setOnChanged(this::onDepositInventoryChanged);
    }

    /** 老虎机收付技术员币：入账、取出与拆机掉币都按它的面额。 */
    @Override
    protected String currencyId() {
        return TradeLang.TECH_OPERATOR_COIN;
    }

    @Override
    protected CoinSeries currency() {
        return CoinExchange.techOperator();
    }

    /** 下发的界面快照：下注、上一局奖励与倍率；余额由基类同步，相等性只看版本号。 */
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

    /** 状态一变就重排 tick 订阅：滚动中、有中奖音效或槽里有币可入账时才需要每 tick 干活。 */
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

    /** 拆机时槽里的硬币原样掉出；余额由基类按面额换成实体币。 */
    @Override
    public void onMachineRemoved() {
        clearInventory(depositInventory);
        super.onMachineRemoved();
    }

    // ==================== 余额迁移 ====================

    /** 旧存档的余额还压在 {@code state} 里：搬到基类余额上并清零，避免下次读档重复加。 */
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
            if (result != null) addBalance(result.reward());
            if (result != null && result.hasWin()) {
                winSoundTicksRemaining = WIN_SOUND_PITCHES.length * WIN_SOUND_INTERVAL;
                grantBoost(result);
            }
            recordSpin(result);
            spinnerUUID = null;
            syncAndMarkChanged();
        } else if (state.isSpinning()) {
            // 倒计时也进存盘，重载后从剩余时间继续。
            onChanged();
        }
        tickWinSound();
        updateTickSubscription();
    }

    /** 旋转结束：单人局才把次数、本把下注与赢得记进里程碑。 */
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

    /** 服务器可在配置里关掉老虎机：关闭后不能开始，余额与硬币槽照常可用。 */
    private static boolean slotMachineEnabled() {
        return GTOConfig.INSTANCE.gamePlay.slotMachineEnabled;
    }

    private boolean canStart() {
        return state.canStart(balance());
    }

    private void startSpin(@Nullable ServerPlayer player) {
        var level = getLevel();
        if (level == null || isRemote() || !slotMachineEnabled() || !guard(player)) return;
        if (!canStart()) return;
        // 先预生成结果再扣注：canStart 已拦过滚动与余额不足，这里不会失败。
        if (!state.startSpin(level.getRandom(), rules)) return;
        removeBalance(state.bet());
        spinnerUUID = player == null ? null : player.getUUID();
        beginSession(player);
        syncAndMarkChanged();
    }

    /**
     * 中奖附赠「机械之神降临」：{@link #BOOST_CHANCE} 的概率，等级与时长由本局倍率和下注决定：两者各按对数归一
     * 后加权（倍率占七成，权重见 {@link #BOOST_BET_WEIGHT}），加抖动、取 {@link #BOOST_CURVE} 次幂，再映射到
     * 1~{@link #BOOST_MAX_AMPLIFIER} 级、30 秒~15 分钟（{@link #BOOST_MAX_DURATION} tick）；
     * 曲线后档位 ≥ {@link #BOOST_LARGE_THRESHOLD} 时换成大瓶范围版（5×5×5），否则只作用于玩家脚下那台。
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

    /** 开这一局的人优先（须在线），否则退回机器主人；都不在线就不给。 */
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
        // 只调度 tick：此时槽位可能处于 Shift 转移中途，物品留到服务端 tick 再消耗。
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
        // 查看者没权限时不动槽里的币（锁机后仍留着他之前塞进去的硬币）。
        var player = viewer();
        if (player != null && !GameAccess.canPlay(this, allowOthers(), player)) return;
        long value = coins.value(tier);
        // 只收能完整入账的硬币：不能吞掉高面值币却只加剩余额度。
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

    /** 关闭老虎机时卷轴位置换成禁赌提示；余额、硬币槽、取出等控件不变。 */
    private UIElement createReels() {
        if (!slotMachineEnabled()) return createDisabledNotice();
        SlotMachineLayout layout = rules.layout();
        // 物品材质只在开界面时构建一次，渲染与滚动复用。
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

    /** 禁赌提示：固定文本，两端各自翻译，不需要下发。 */
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
            // 滚动只是客户端动画，不读取待结算结果。
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
            // 界面已禁用，服务端再挡一次，防网络请求绕过。
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

    /** 挂「配置」与「排行榜」两个子页。 */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new ConfigPage());
        sideTabs.attachSubTab(new LeaderboardPage());
    }

    /** 配置页：游玩权限开关与余额。 */
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

    /** 排行榜页：回报比榜，按回报比降序。 */
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

    /** 服务端构建榜单行：一人一行，顺序由 {@code slotBoard} 排好。 */
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
        // 符号与中奖标记打包在同一个同步值里，避免显示错位。
        return result.symbolAt(reel, row).id() | (result.isWinningCell(reel, row) ? REEL_WINNING_FLAG : 0);
    }

    /**
     * 卷轴面板：兼作键盘入口，按空格等同于点「开始」，界面快照也挂在这一层。
     * 请求走 RPC，服务端在 {@link #startSpin} 里重新校验（禁赌、滚动中、余额不足）；客户端不做预判。
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

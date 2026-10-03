package com.gtocore.common.machine.noenergy.slotMachine;

import com.gtocore.config.GTOConfig;
import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.TradeLang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.animation.UIClock;
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
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.data.Data;
import com.lowdragmc.lowdraglib.gui.texture.DynamicTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;

/**
 * 基于 {@link SlotMachineState} 的老虎机机器。
 *
 * <p>
 * 本类只处理机器层职责：服务端状态、tick 订阅、DataSyncLib 字段存盘、掉落物保存与 Fancy UI；
 * 开奖、赔率与结果结构都在同包的底层逻辑类里。
 *
 * <p>
 * 盘面由构造时注入的 {@link SlotMachineRules} 决定（列数、行数、卷轴与赔付表），界面按规则的布局铺格子，
 * 所以同一个类同时驱动 3 列与 5 列老虎机；规则不参与存档，读档时由方块的机器工厂重新注入。
 */
@DataGeneratorScanned
public class SlotMachine extends MetaMachine implements IFancyUIMachine, IControllable, IDropSaveMachine {

    private static final String ITEM_DATA_TAG = "SlotMachineData";
    private static final String[] SAVED_FIELDS = { "state", "enabled", "depositInventory" };
    private static final String CURRENCY_NAME = "gtocore.currency." + TradeLang.TECH_OPERATOR_COIN;

    @RegisterLanguage(cn = "余额", en = "Credits")
    private static final String LINE_BALANCE = "gtocore.machine.slot_machine.line.balance";
    @RegisterLanguage(cn = "上一局奖励", en = "Last payout")
    private static final String LINE_LAST_REWARD = "gtocore.machine.slot_machine.line.last_reward";
    @RegisterLanguage(cn = "下注数量", en = "Bet amount")
    private static final String ROW_BET = "gtocore.machine.slot_machine.row.bet";
    @RegisterLanguage(cn = "下注会在开始滚动时扣除，滚动结束后按中奖线返还奖励", en = "The bet is consumed when a spin starts; payout is returned after the spin")
    private static final String ROW_BET_TOOLTIP = "gtocore.machine.slot_machine.row.bet.tooltip";
    @RegisterLanguage(cn = "开始", en = "Spin")
    private static final String BUTTON_SPIN = "gtocore.machine.slot_machine.button.spin";
    @RegisterLanguage(cn = "取出全部", en = "Withdraw all")
    private static final String BUTTON_WITHDRAW = "gtocore.machine.slot_machine.button.withdraw";
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
    // 区块存盘与物品掉落都走同一个 "state" 字段。
    // 字段不能是 final：final 字段只查 access 工厂（嵌套 holder、容器），不查 codec 工厂，会在字段扫描期
    // 直接抛异常；非 final 才允许读盘时整体替换实例。
    @SaveToDisk
    private SlotMachineState state = new SlotMachineState();
    // 硬币槽随机器存盘，槽里没换完的硬币不能丢。
    @SaveToDisk
    private final CustomItemStackHandler depositInventory = new CustomItemStackHandler();
    private final Runnable machineTick = this::tickMachine;

    @SaveToDisk(defaultValue = "true")
    @SyncToClient(scheduleUpdate = true)
    private boolean enabled = true;
    @SyncToClient(scheduleUpdate = true)
    private long balanceView;
    @SyncToClient(scheduleUpdate = true)
    private int betView = SlotMachineRules.DEFAULT_BET;
    @SyncToClient(scheduleUpdate = true)
    private long lastRewardView;
    @SyncToClient(scheduleUpdate = true)
    private double lastRewardMultiplierView;

    @Nullable
    private TickableSubscription tickSubs;
    private int winSoundTicksRemaining;
    // 盘面规则由方块类型决定，不参与存档：读档时机器工厂会重新注入同一份规则。
    private final SlotMachineRules rules;

    public SlotMachine(MetaMachineBlockEntity holder, SlotMachineRules rules) {
        super(holder);
        this.rules = rules;
        depositInventory.setOnContentsChanged(this::onDepositInventoryChanged);
        syncViewFromState();
    }

    /** 服务器可以在配置里关闭老虎机：关闭后只显示禁赌提示，且永远不能开始（余额与硬币槽照常可用）。 */
    private static boolean slotMachineEnabled() {
        return GTOConfig.INSTANCE.gamePlay.slotMachineEnabled;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            // 数值校验在解码时已完成，这里只把界面同步值刷新到读盘后的状态。
            syncViewFromState();
            updateTickSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        cancelTickSubscription();
        winSoundTicksRemaining = 0;
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        // 掉落物与区块存档共用同一套字段编解码。
        Data data = getFieldDataManager().writeFieldsToData(SAVED_FIELDS);
        if (data.isNull()) tag.remove(ITEM_DATA_TAG);
        else tag.put(ITEM_DATA_TAG, new ByteArrayTag(data.writeToBytes()));
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (tag.get(ITEM_DATA_TAG) instanceof ByteArrayTag saved) {
            getFieldDataManager().readFieldsFromData(Data.readData(saved.getAsByteArray()), 0, SAVED_FIELDS);
        }
        syncViewFromState();
        updateTickSubscription();
    }

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
            if (result != null && result.hasWin()) {
                winSoundTicksRemaining = WIN_SOUND_PITCHES.length * WIN_SOUND_INTERVAL;
            }
            syncAndMarkChanged();
        } else if (state.isSpinning()) {
            // 倒计时也进入区块存盘，重载后从实际剩余时间继续。
            onChanged();
        }
        tickWinSound();
        updateTickSubscription();
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

    private void startSpin() {
        var level = getLevel();
        if (level == null || isRemote() || !enabled || !slotMachineEnabled()) return;
        if (state.startSpin(level.getRandom(), rules)) {
            syncAndMarkChanged();
        }
    }

    private void onDepositInventoryChanged() {
        if (getLevel() == null || isRemote()) return;
        // 这里只调度；槽位容量探测和 Shift 转移完成后，才在服务端 tick 中消耗物品。
        onChanged();
        updateTickSubscription();
    }

    private boolean canDeposit() {
        int tier = CoinExchange.tierOf(depositInventory.getStackInSlot(0));
        return tier >= 0 && Long.MAX_VALUE - state.balance() >= CoinExchange.value(tier);
    }

    private void depositCredits() {
        ItemStack stack = depositInventory.getStackInSlot(0);
        int tier = CoinExchange.tierOf(stack);
        if (tier < 0) return;
        long value = CoinExchange.value(tier);
        // 只接收能完整入账的硬币，不能吞掉一枚高面值币后只加剩余额度。
        int amount = (int) Math.min(stack.getCount(), (Long.MAX_VALUE - state.balance()) / value);
        if (amount <= 0) return;
        int deposited = depositInventory.extract(0, stack, amount, false);
        if (deposited > 0) {
            state.addCredits(deposited * value);
            syncAndMarkChanged();
        }
    }

    private void withdrawAll(Widget source) {
        if (isRemote() || state.isSpinning()) return;
        var gui = source.getGui();
        if (gui == null || !(gui.entityPlayer instanceof ServerPlayer player)) return;
        var inventory = player.getInventory();
        long remaining = state.balance();
        if (remaining == 0) return;
        var destination = new PlayerMainInvWrapper(inventory);
        // 优先大面额，再用小币补零；每档最多尝试一个背包的容量，避免大余额下无限循环。
        for (int tier = CoinExchange.TYPE_COUNT - 1; tier >= 0 && remaining > 0; tier--) {
            long value = CoinExchange.value(tier);
            if (remaining < value) continue;
            ItemStack coins = new ItemStack(CoinExchange.item(tier));
            if (coins.isEmpty()) continue;
            int amount = (int) Math.min(remaining / value, (long) inventory.items.size() * coins.getMaxStackSize());
            if (amount == 0) continue;
            coins.setCount(amount);
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(destination, coins, false);
            remaining -= (amount - remainder.getCount()) * value;
        }
        long withdrawn = state.balance() - remaining;
        if (withdrawn == 0) return;
        state.removeCredits(withdrawn);
        syncAndMarkChanged();
    }

    private void setBet(int bet) {
        if (isRemote()) return;
        state.setBet(bet);
        syncAndMarkChanged();
    }

    private void syncAndMarkChanged() {
        syncViewFromState();
        onChanged();
        updateTickSubscription();
    }

    private void syncViewFromState() {
        balanceView = state.balance();
        betView = state.bet();
        SlotMachineResult lastResult = state.lastResult();
        if (lastResult == null) {
            lastRewardView = 0;
            lastRewardMultiplierView = 0;
        } else {
            lastRewardView = lastResult.reward();
            lastRewardMultiplierView = lastResult.reward() / (double) lastResult.bet();
        }
    }

    private Component lastRewardText() {
        if (state.lastResult() == null) return Component.translatable(VALUE_NONE);
        return Component.translatable(VALUE_REWARD, number(lastRewardView),
                Component.literal(FormattingUtil.formatNumber2Places(lastRewardMultiplierView)).withStyle(ChatFormatting.GOLD));
    }

    private int reelDisplayValue(int reel, int row) {
        if (state.isSpinning()) return REEL_SPINNING;
        SlotMachineResult result = state.lastResult();
        if (result == null) return REEL_EMPTY;
        // 符号与中奖标记打包在同一个同步值里，避免两者显示错位。
        return result.symbolAt(reel, row).id() | (result.isWinningCell(reel, row) ? REEL_WINNING_FLAG : 0);
    }

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
        var reels = new StatusPanel().addChildren(createReels());

        var status = new StatusPanel();
        status.addLine(LINE_BALANCE, () -> number(balanceView));
        status.addLine(LINE_LAST_REWARD, this::lastRewardText);

        var spin = Button.translatable(LayoutStyle.AUTO, BUTTON_SPIN)
                .setOnServerClick(this::startSpin)
                .disabled(() -> !slotMachineEnabled() || !enabled || !state.canStart(),
                        slotMachineEnabled() ? REASON_CANNOT_START : REASON_DISABLED);

        var credits = UIElement.centeredRow(UISizes.SLOT_SIZE).layout(layout -> layout.justifyContent(AlignContent.CENTER));
        var deposit = ItemSlot.of(depositInventory, 0, true, true);
        deposit.setGhosts(CoinExchange.itemStacks())
                .setChangeListener(this::onDepositInventoryChanged)
                .setHoverTooltips(CURRENCY_NAME);
        var withdraw = Button.translatable(136, BUTTON_WITHDRAW);
        withdraw.setOnServerClick(() -> withdrawAll(withdraw)).disabled(() -> state.isSpinning(), REASON_SPINNING)
                .setHoverTooltips(CURRENCY_NAME);
        credits.addChildren(deposit, withdraw);

        status.addChildren(
                NumberField.ofInt(LayoutStyle.AUTO, () -> betView, this::setBet, SlotMachineRules.DEFAULT_BET, SlotMachineRules.DEFAULT_MAX_BET)
                        .setSteps(CREDIT_STEPS).tooltips(ROW_BET, ROW_BET_TOOLTIP),
                credits,
                spin);

        return Form.page().addChildren(reels, status);
    }

    @Override
    public boolean hasPlayerInventory() {
        return true;
    }

    @Override
    public boolean isWorkingEnabled() {
        return enabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (enabled == isWorkingAllowed) return;
        enabled = isWorkingAllowed;
        syncAndMarkChanged();
    }

    private static MutableComponent number(long value) {
        return Component.literal(FormattingUtil.formatNumbers(value)).withStyle(ChatFormatting.AQUA);
    }
}

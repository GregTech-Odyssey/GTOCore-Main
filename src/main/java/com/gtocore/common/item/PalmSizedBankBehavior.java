package com.gtocore.common.item;

import com.gtocore.common.data.GTOItems;

import com.gtolib.utils.WalletUtils;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.RichText;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.gto.fastcollection.fastutil.O2LOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.gtocore.common.item.GregMembershipCardItem.createWithUuidAndSharedList;
import static com.gtocore.data.transaction.data.TradeLang.TECH_OPERATOR_COIN;
import static com.gtocore.data.transaction.data.trade.UnlockTrade.*;

/**
 * 掌上银行：物品界面（uipro / Ore UI 风格）。
 * <p>
 * <b>数据流（与数据中心一致）</b>：
 * <ul>
 * <li>界面上每个数值都在<b>逻辑服务端</b>由 {@link WalletUtils} 算出：{@code RichText} 的服务端 {@code textSupplier}、
 * {@code TextLine}/{@code StatusPanel} 的取值函数、{@code Button.disabled} 的判定、{@code NumberField} 的取值，全部只在服务端执行。</li>
 * <li>客户端<b>不读</b>钱包数据：{@code RichText} 的 {@code textSupplier} 在客户端传 {@code null}，
 * 其它元素读 {@link SyncValue} 下发的值；初始值随建页的初始数据一起到达（{@code writeInitialData}/{@code readInitialData}），
 * 之后由 {@code detectAndSendChanges} 在变化时增量下发。</li>
 * <li>所有改动数据的交互都经 LDLib 自己的客户端动作回到<b>服务端 UI</b>执行（{@code setOnServerClick}、
 * {@code RichText.clickHandler} 里判 {@code clickData.isRemote}），服务端改完再把新数据下发。</li>
 * </ul>
 * 因此本类里任何看起来像"读钱包"的代码都不会在客户端跑；页内选择状态（转账对象、货币、交易主键、标签键、共享名单）
 * 由服务端那一份页面实例持有，客户端只显示服务端算出来的文字。
 */
public class PalmSizedBankBehavior implements IItemUIFactory, IFancyUIProvider {

    public static final PalmSizedBankBehavior INSTANCE = new PalmSizedBankBehavior();

    private static final String TEXT_HEADER = "gtocore.palm_sized_bank.textList.";
    private static final String CURRENCY_HEADER = "gtocore.currency.";
    /// 页内"返回/取消选择"按钮的可点击数据
    private static final String CLEAR_SELECTION = "clear_selection";
    private static final String UNSET = "—";
    /// 左右两栏时每栏的宽度（内容宽 162 减去一个间距后对半分）
    private static final int HALF_WIDTH = (UISizes.CONTENT_WIDTH - UISizes.GAP) / 2;
    /// 列表滚动区的高度：首选高度与自动增高上限
    private static final int LIST_HEIGHT = UISizes.SLOT * 4;
    private static final int LIST_MAX_HEIGHT = UISizes.SLOT * 7;

    private static @NotNull MutableComponent trans(int id, Object... args) {
        if (args.length == 1 && args[0] instanceof Object[]) args = (Object[]) args[0];
        return Component.translatable(TEXT_HEADER + id, args);
    }

    /** 货币名的翻译键（不存在时原样显示 id）。 */
    private static @NotNull Component currencyName(@Nullable String currencyId) {
        return currencyId == null ? Component.literal(UNSET) : Component.translatable(CURRENCY_HEADER + currencyId);
    }

    private static @NotNull MutableComponent literal(Object value) {
        return Component.literal(String.valueOf(value));
    }

    /**
     * {@code disabled(...)} 的判定在服务端执行、经同步值下发，但 LDLib 在控件还没挂进界面时（建页过程中）
     * 会回退成"直接求值"，那一步在客户端也会发生 —— 所以条件必须两端都安全：
     * 客户端直接按"禁用"回答（真实状态由服务端下发），只有服务端才去读钱包。
     */
    private static boolean serverDisabled(Player player, BooleanSupplier serverCondition) {
        return player.level().isClientSide || serverCondition.getAsBoolean();
    }

    /**
     * 转账金额的上限：服务端取当前货币余额（没选货币时为 0，输入框随之禁用）。
     * 建页时两端都会取一次，客户端取不到余额，先返回无上限——真实上界由服务端经同步值下发。
     */
    private static long amountLimit(Player player, @Nullable String currency) {
        if (player.level().isClientSide) return Long.MAX_VALUE;
        return currency == null ? 0L : WalletUtils.getCurrencyAmount(player, currency);
    }

    // ==================== 物品行为 ====================

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        openUI(itemStack.getItem(), context.getLevel(), player, context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack heldItem = player.getItemInHand(usedHand);
        openUI(item, level, player, usedHand);
        return InteractionResultHolder.success(heldItem);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        return new ModularUI(176, 166, holder, player)
                .widget(new MachineWindow(this));
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTOItems.PALM_SIZED_BANK.asItem());
    }

    @Override
    public Component getTitle() {
        return GTOItems.PALM_SIZED_BANK.asStack().getDisplayName();
    }

    // ==================== 主页：钱包状态 ====================

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var page = BankTab.page();
        Player player = getPlayerFromWidget(widget);
        if (player == null) return page;
        boolean remote = player.level().isClientSide;

        // 钱包是否存在：服务端判定、下发；"钱包不存在"那行按同步值显隐（隐藏的元素自己收不到更新，同步值挂在页面上）
        var hasWallet = page.addSyncValue(SyncValue.of(() -> WalletUtils.hasWallet(player), SyncValue.BOOLEAN, false));

        page.addChild(BankTab.textPane("gtocore.bank.intro", UISizes.CONTENT_WIDTH, UISizes.TEXT_HEIGHT * 4, UISizes.TEXT_HEIGHT * 8, remote,
                list -> {
                    list.add(trans(1).withStyle(ChatFormatting.AQUA));
                    list.add(trans(2));
                    list.add(trans(3));
                    list.add(trans(4));
                }));

        var status = new StatusPanel();
        status.addSentence(() -> trans(6, player.getName().getString()));
        status.addSentence(() -> trans(7, player.getUUID().toString()));
        var missing = status.addSentence(() -> trans(9)).level(() -> StatusLine.Level.WARNING);
        // 服务端这份直接按权威值摆好（客户端那份等初始数据下发后由 onChanged 校正；
        // 服务端的 SyncValue.writeInitial 不触发 onChanged，所以这里要自己设一次）
        missing.setDisplay(!remote && !WalletUtils.hasWallet(player));
        hasWallet.onChanged(value -> missing.setDisplay(!value));
        page.addChild(status);

        page.addChild(Button.translatable(LayoutStyle.AUTO, text(8))
                .setVariant(UITheme.ButtonVariant.CONFIRM)
                .disabled(() -> serverDisabled(player, () -> WalletUtils.hasWallet(player)), null)
                .setOnServerClick(() -> {
                    if (!(player instanceof ServerPlayer serverPlayer)) return;
                    WalletUtils.createAndInitializeWallet(player.getUUID(), serverPlayer.serverLevel(), player.getName().getString());
                    initNewPlayerCurrencies(player.getUUID(), serverPlayer.serverLevel());
                }));
        return page;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
        sideTabs.attachSubTab(assetOverview());
        sideTabs.attachSubTab(transfer());
        sideTabs.attachSubTab(tradeRecords());
        sideTabs.attachSubTab(tagList());
        sideTabs.attachSubTab(generateCard());
    }

    // ==================== 页签：资产概览 ====================

    private @NotNull IFancyUIProvider assetOverview() {
        return new BankTab() {

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                var page = BankTab.page();
                Player player = getPlayerFromWidget(widget);
                if (player == null) return page;
                page.addChild(BankTab.textPane("gtocore.bank.assets", UISizes.CONTENT_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT,
                        player.level().isClientSide, list -> {
                            Object2LongMap<String> currencies = WalletUtils.getCurrencyMap(player);
                            list.add(trans(10).withStyle(ChatFormatting.AQUA));
                            list.add(trans(11).copy().append("   ").append(trans(12)).withStyle(ChatFormatting.DARK_GRAY));
                            if (currencies.isEmpty()) {
                                list.add(Component.literal(UNSET));
                                return;
                            }
                            for (var it = Object2LongMaps.fastIterator(currencies); it.hasNext();) {
                                var entry = it.next();
                                list.add(currencyName(entry.getKey()).copy()
                                        .append(Component.literal(": " + FormattingUtil.formatNumbers(entry.getLongValue()))));
                            }
                        }));
                return page;
            }
        };
    }

    // ==================== 页签：转账 ====================

    private @NotNull IFancyUIProvider transfer() {
        return new BankTab() {

            @Nullable
            private UUID target;
            @Nullable
            private String currency;
            private long amount;

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                var page = BankTab.page();
                Player player = getPlayerFromWidget(widget);
                if (player == null) return page;
                boolean remote = player.level().isClientSide;

                var status = new StatusPanel();
                status.addSentence(() -> trans(50).copy().append(": ").append(targetName(player)));
                status.addSentence(() -> trans(51).copy().append(": ").append(currencyName(currency)));
                status.addSentence(() -> trans(52).copy().append(": ").append(literal(FormattingUtil.formatNumbers(amount))));
                page.addChild(status);

                // 左：可选账户；右：可转货币。点击都只在服务端改页内状态，改完由服务端重新算文字下发
                page.addChild(UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                        BankTab.halfWidth(BankTab.textPane("gtocore.bank.transfer.targets", HALF_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT, remote, list -> {
                            Object2ObjectMap<UUID, String> wallets = WalletUtils.getAllWalletPlayers(player.level());
                            list.add(trans(50).withStyle(ChatFormatting.AQUA));
                            if (wallets.isEmpty()) {
                                list.add(Component.literal(UNSET));
                                return;
                            }
                            for (var it = Object2ObjectMaps.fastIterator(wallets); it.hasNext();) {
                                var entry = it.next();
                                var text = Component.literal(entry.getValue() + (entry.getKey().equals(target) ? " ◀" : ""));
                                list.add(ComponentPanelWidget.withHoverTextTranslate(
                                        ComponentPanelWidget.withButton(text, entry.getKey().toString()),
                                        literal(entry.getKey())));
                            }
                        })),
                        BankTab.halfWidth(BankTab.textPane("gtocore.bank.transfer.currencies", HALF_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT, remote, list -> {
                            list.add(trans(51).withStyle(ChatFormatting.AQUA));
                            Object2LongMap<String> currencies = WalletUtils.getCurrencyMap(player);
                            if (currencies.isEmpty()) {
                                list.add(Component.literal(UNSET));
                                return;
                            }
                            list.add(ComponentPanelWidget.withButton(Component.literal(UNSET), CLEAR_SELECTION));
                            for (var it = Object2LongMaps.fastIterator(currencies); it.hasNext();) {
                                var entry = it.next();
                                var text = currencyName(entry.getKey()).copy()
                                        .append(Component.literal("  " + FormattingUtil.formatNumbers(entry.getLongValue())));
                                if (entry.getKey().equals(currency)) text.append(Component.literal(" ◀"));
                                list.add(ComponentPanelWidget.withButton(text, entry.getKey()));
                            }
                        }))));

                // 金额与服务端对齐：上限是当前货币余额（服务端取值下发），确认转账也只在服务端执行。
                // 建页时两端都会取一次上下限：客户端取不到余额，先报"无上限"（真实上界随初始数据下发）；
                // 写入时服务端再按余额夹一次，客户端改不出超过余额的金额。
                var field = new NumberField(HALF_WIDTH, () -> amount,
                        value -> amount = Math.clamp(value, 0L, Math.max(0L, amountLimit(player, currency))),
                        () -> 0L, () -> amountLimit(player, currency), NumberField.DEFAULT_STEPS)
                        .layout(l -> l.flex(1));
                page.addChild(UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                        field,
                        Button.translatable(LayoutStyle.AUTO, text(54))
                                .setVariant(UITheme.ButtonVariant.CONFIRM)
                                .layout(l -> l.flex(1))
                                .disabled(() -> serverDisabled(player, () -> target == null || currency == null || amount <= 0 || WalletUtils.getCurrencyAmount(player, currency) < amount), null)
                                .setOnServerClick(() -> {
                                    if (!(player instanceof ServerPlayer serverPlayer)) return;
                                    if (target == null || currency == null || amount <= 0) return;
                                    ServerLevel level = serverPlayer.serverLevel();
                                    if (WalletUtils.getCurrencyAmount(player, currency) < amount) return;
                                    WalletUtils.subtractCurrency(player.getUUID(), level, currency, amount);
                                    WalletUtils.addCurrency(target, level, currency, amount);
                                    amount = 0;
                                })));
                return page;
            }

            private @NotNull Component targetName(Player player) {
                if (target == null) return Component.literal(UNSET);
                String name = WalletUtils.getAllWalletPlayers(player.level()).get(target);
                return name == null ? literal(target) : Component.literal(name);
            }
        };
    }

    // ==================== 页签：交易记录 ====================

    private @NotNull IFancyUIProvider tradeRecords() {
        return new BankTab() {

            @Nullable
            private String choose;

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                var page = BankTab.page();
                Player player = getPlayerFromWidget(widget);
                if (player == null) return page;
                boolean remote = player.level().isClientSide;

                var keys = BankTab.textPane("gtocore.bank.records.keys", HALF_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT, remote, list -> {
                    list.add(trans(20).withStyle(ChatFormatting.AQUA));
                    Set<String> transactionKeys = WalletUtils.getTransactionKeys(player);
                    if (choose != null) list.add(ComponentPanelWidget.withButton(Component.literal("↩"), CLEAR_SELECTION));
                    if (transactionKeys.isEmpty()) {
                        list.add(Component.literal(UNSET));
                        return;
                    }
                    for (String key : transactionKeys) {
                        list.add(ComponentPanelWidget.withButton(Component.literal(choose != null && choose.equals(key) ? key + " ◀" : key), key));
                    }
                }, data -> choose = CLEAR_SELECTION.equals(data) ? null : data);

                var details = BankTab.textPane("gtocore.bank.records.details", HALF_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT, remote, list -> {
                    if (choose == null) {
                        list.add(trans(21).withStyle(ChatFormatting.AQUA));
                        for (String key : WalletUtils.getTransactionKeys(player)) {
                            list.add(literal(key).copy()
                                    .append(Component.literal("  " + trans(22).getString() + ": "))
                                    .append(literal(WalletUtils.getTransactionTotalAmount(player.getUUID(), player.level(), key))));
                        }
                        list.add(trans(26).withStyle(ChatFormatting.DARK_GRAY));
                        return;
                    }
                    String txKey = choose;
                    list.add(literal(txKey).withStyle(ChatFormatting.AQUA));
                    list.add(trans(22).copy().append(": ").append(literal(WalletUtils.getTransactionTotalAmount(player.getUUID(), player.level(), txKey))));
                    list.add(trans(23).copy().append(": ").append(literal(WalletUtils.getTransactionType(player.getUUID(), player.level(), txKey))));
                    long minute = WalletUtils.getGameMinuteKey(player);
                    list.add(trans(24).copy().append(": ").append(literal(WalletUtils.getTransactionMinuteAmount(player.getUUID(), player.level(), txKey, minute))));
                    list.add(trans(25).copy().append(": ").append(literal(WalletUtils.getTransactionMinuteAmount(player.getUUID(), player.level(), txKey, minute - 1))));
                    list.add(trans(26).withStyle(ChatFormatting.DARK_GRAY));
                    Long2LongMap minutes = WalletUtils.getTransactionMinuteMap(player.getUUID(), player.level(), txKey);
                    List<Long> minuteKeys = new ArrayList<>(minutes.keySet());
                    minuteKeys.sort(Collections.reverseOrder());
                    for (Long key : minuteKeys) {
                        list.add(Component.empty().append(literal(key)).append(Component.literal(": ")).append(literal(minutes.get(key.longValue()))));
                    }
                });

                page.addChild(UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                        BankTab.halfWidth(keys),
                        BankTab.halfWidth(details)));
                return page;
            }
        };
    }

    // ==================== 页签：钱包标签表 ====================

    private @NotNull IFancyUIProvider tagList() {
        return new BankTab() {

            @Nullable
            private String choose;

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                var page = BankTab.page();
                Player player = getPlayerFromWidget(widget);
                if (player == null) return page;
                boolean remote = player.level().isClientSide;

                var keys = BankTab.textPane("gtocore.bank.tags.keys", HALF_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT, remote, list -> {
                    list.add(trans(30).withStyle(ChatFormatting.AQUA));
                    Set<String> tagKeys = WalletUtils.getAllTagKeysFromWallet(player.getUUID(), player.level());
                    if (choose != null) list.add(ComponentPanelWidget.withButton(Component.literal("↩"), CLEAR_SELECTION));
                    if (tagKeys.isEmpty()) {
                        list.add(Component.literal(UNSET));
                        return;
                    }
                    for (String key : tagKeys) {
                        list.add(ComponentPanelWidget.withButton(Component.translatable(key).copy().withStyle(ChatFormatting.AQUA), key));
                    }
                }, data -> choose = CLEAR_SELECTION.equals(data) ? null : data);

                var values = BankTab.textPane("gtocore.bank.tags.values", HALF_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT, remote, list -> {
                    list.add(trans(31).withStyle(ChatFormatting.AQUA));
                    if (choose != null) list.add(Component.translatable(choose).withStyle(ChatFormatting.WHITE));
                    for (String tag : WalletUtils.getTagsFromWallet(player.getUUID(), player.level(), choose)) {
                        list.add(literal(tag));
                    }
                });

                page.addChild(UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                        BankTab.halfWidth(keys),
                        BankTab.halfWidth(values)));
                return page;
            }
        };
    }

    // ==================== 页签：申请会员卡 ====================

    private @NotNull IFancyUIProvider generateCard() {
        return new BankTab() {

            /// 要写进卡的共享名单（服务端持有；点击加入/移除都只在服务端执行）
            private final Set<UUID> shared = new OpenCacheHashSet<>();

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                var page = BankTab.page();
                Player player = getPlayerFromWidget(widget);
                if (player == null) return page;
                boolean remote = player.level().isClientSide;

                page.addChild(Button.translatable(LayoutStyle.AUTO, text(41))
                        .setVariant(UITheme.ButtonVariant.CONFIRM)
                        .disabled(() -> serverDisabled(player, () -> WalletUtils.getCurrencyAmount(player, TECH_OPERATOR_COIN) < 15), null)
                        .bindTooltip(() -> trans(40, currencyName(TECH_OPERATOR_COIN), literal(15)))
                        .setOnServerClick(() -> {
                            if (WalletUtils.getCurrencyAmount(player, TECH_OPERATOR_COIN) < 15) return;
                            ItemEntity itemEntity = player.spawnAtLocation(createWithUuidAndSharedList(player.getUUID(), new ArrayList<>(shared)));
                            WalletUtils.subtractCurrency(player.getUUID(), player.level(), TECH_OPERATOR_COIN, 15);
                            if (itemEntity != null) itemEntity.setNoPickUpDelay();
                        }));

                var candidates = BankTab.textPane("gtocore.bank.card.candidates", HALF_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT, remote, list -> {
                    list.add(trans(42).withStyle(ChatFormatting.AQUA));
                    Object2ObjectMap<UUID, String> wallets = WalletUtils.getAllWalletPlayers(player.level());
                    if (wallets.isEmpty()) {
                        list.add(Component.literal(UNSET));
                        return;
                    }
                    for (var it = Object2ObjectMaps.fastIterator(wallets); it.hasNext();) {
                        var entry = it.next();
                        if (shared.contains(entry.getKey())) continue;
                        list.add(ComponentPanelWidget.withButton(Component.literal(entry.getValue()), entry.getKey().toString()));
                    }
                }, data -> shared.add(UUID.fromString(data)));

                var selected = BankTab.textPane("gtocore.bank.card.selected", HALF_WIDTH, LIST_HEIGHT, LIST_MAX_HEIGHT, remote, list -> {
                    list.add(trans(43).withStyle(ChatFormatting.AQUA));
                    Object2ObjectMap<UUID, String> wallets = WalletUtils.getAllWalletPlayers(player.level());
                    for (UUID uuid : shared) {
                        String name = wallets.get(uuid);
                        list.add(ComponentPanelWidget.withButton(Component.literal(name == null ? uuid.toString() : name), uuid.toString()));
                    }
                }, data -> shared.remove(UUID.fromString(data)));

                page.addChild(UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                        BankTab.halfWidth(candidates),
                        BankTab.halfWidth(selected)));
                return page;
            }
        };
    }

    // ==================== 页签公共部分 ====================

    /**
     * 页签公共外观与建页工具。所有工具都只搭控件，不碰数据：
     * 文字一律由服务端取值下发，客户端只收同步值。
     */
    private abstract static class BankTab implements IFancyUIProvider {

        @Override
        public IGuiTexture getTabIcon() {
            return GuiTextures.GREGTECH_LOGO;
        }

        @Override
        public Component getTitle() {
            return GTOItems.PALM_SIZED_BANK.asStack().getDisplayName();
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(getTitle());
        }

        /** 建页公共外壳：内容宽与玩家背包对齐，区块之间用 {@link UISizes#SECTION_GAP}。 */
        static UIElement page() {
            return UIElement.column(UISizes.CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));
        }

        /**
         * 滚动文本区：{@code text} 只在服务端执行（客户端传 {@code null}），
         * 客户端的文字来自建页初始数据与之后的增量同步。
         */
        static ScrollerView textPane(String id, int width, int height, int maxHeight, boolean remote,
                                     java.util.function.Consumer<List<Component>> text) {
            return textPane(id, width, height, maxHeight, remote, text, null);
        }

        /**
         * 同上，并挂上"可点击片段"的服务端处理：{@code serverClick} 只在服务端执行
         * （LDLib 在客户端点击时先本地调一次、再把请求发给服务端，这里把本地那次挡掉）。
         */
        static ScrollerView textPane(String id, int width, int height, int maxHeight, boolean remote,
                                     java.util.function.Consumer<List<Component>> text,
                                     @Nullable java.util.function.Consumer<String> serverClick) {
            var rich = new RichText();
            rich.textSupplier(remote ? null : text);
            if (serverClick != null) {
                rich.clickHandler((data, clickData) -> {
                    if (clickData.isRemote) return;
                    serverClick.accept(data);
                });
            }
            var scroller = new ScrollerView(id, width, height).adaptiveHeight(maxHeight);
            scroller.setBackground(UITheme.STATUS_PANEL);
            scroller.layoutContent(l -> l.paddingAll(UITheme.PANEL_PADDING));
            scroller.addScrollViewChild(rich);
            return scroller;
        }

        /** 两栏并排时各自吃掉一半宽度（滚动区不是 {@link UIElement}，只能改它自己的布局样式）。 */
        static ScrollerView halfWidth(ScrollerView scroller) {
            scroller.getLayoutStyle().flex(1);
            return scroller;
        }
    }

    private static @NotNull String text(int id) {
        return TEXT_HEADER + id;
    }

    private static void initNewPlayerCurrencies(UUID playerUUID, ServerLevel world) {
        O2LOpenCacheHashMap<String> initialCurrencies = new O2LOpenCacheHashMap<>();
        initialCurrencies.put(TECH_OPERATOR_COIN, 37);
        WalletUtils.setCurrencies(playerUUID, world, initialCurrencies);
        WalletUtils.addTagToWallet(playerUUID, world, UNLOCK_SHOP, UNLOCK_BASE);
        WalletUtils.addTagToWallet(playerUUID, world, UNLOCK_TRADE, UNLOCK_BASE);
    }

    // 辅助方法
    private void openUI(Item item, Level level, Player player, InteractionHand hand) {
        initializationParameters(player);
        IItemUIFactory.super.use(item, level, player, hand);
    }

    private void initializationParameters(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            WalletUtils.updatePlayerName(player.getUUID(), serverPlayer.serverLevel(), player.getName().getString());
        }
    }

    private static @Nullable Player getPlayerFromWidget(FancyMachineUIWidget widget) {
        ModularUI modularUI = widget.getGui();
        return (modularUI != null) ? modularUI.entityPlayer : null;
    }
}

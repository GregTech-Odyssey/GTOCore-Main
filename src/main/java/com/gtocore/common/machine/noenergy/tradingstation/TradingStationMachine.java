package com.gtocore.common.machine.noenergy.tradingstation;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.data.transaction.manager.TradeData;
import com.gtocore.data.transaction.manager.TradeEntry;
import com.gtocore.data.transaction.manager.TradingManager;
import com.gtocore.data.transaction.manager.UnlockManager;

import com.gtolib.utils.WalletUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputBoth;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.FluidSlot;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ItemView;
import com.gregtechceu.gtceu.uipro.elements.RichText;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import dev.vfyjxf.taffy.style.FlexWrap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import static com.gtocore.common.item.GregMembershipCardItem.getSharedUuids;
import static com.gtocore.common.item.GregMembershipCardItem.getSingleUuid;
import static com.gtocore.data.transaction.data.trade.UnlockTrade.UNLOCK_SHOP;
import static com.gtocore.data.transaction.data.trade.UnlockTrade.UNLOCK_TRADE;

/**
 * 泛银河系格雷科技贸易站。
 * <p>
 * 界面按 GTM 的 uipro（Ore UI）搭：主页、物品/流体存储、交易解锁各一页，每个商店一个页签，页面元素全部是 uipro 控件。
 * 数据流与 {@code DataCenter} 相同——<b>服务端是唯一权威</b>：
 * <ul>
 * <li>要显示的数值、文字都由服务端取值：{@link TextLine#of}、{@link StatusPanel} 的行、交易格的悬停说明、
 * {@link RichText} 的 {@code textSupplier}（客户端传 null，只等下发）都只在服务端执行，读机器字段、{@link TradingManager}、
 * {@link UnlockManager}、{@link WalletUtils} 这些客户端没有（或不可信）的数据源；</li>
 * <li>客户端渲染时只读同步下来的值（{@link SyncValue#getValue()}）与两端一致的静态注册数据（{@link TradingManager} 的名字、
 * 图标、条目数量）；</li>
 * <li>会改数据的点击走 {@code setOnServerClick}（如执行交易）；会改变控件树结构的点击走 {@link Button#setOnClick}
 * ——切换商店组、翻页、切换解锁项时两端各执行一次、各重建一份同样的结构（LDLib1 的控件更新按子控件下标路由，
 * 两端树必须一致），结构只依据静态注册数据决定，数值仍由服务端算。</li>
 * </ul>
 */
public class TradingStationMachine extends MetaMachine implements IFancyUIMachine, IAutoOutputBoth, IMachineLife, IControllable {

    /////////////////////////////////////
    // *********** 数据存储 *********** //
    /////////////////////////////////////

    /** 输入输出存储 */
    @Getter
    @SaveToDisk
    @SyncToClient
    private final NotifiableItemStackHandler inputItem;
    @Getter
    @SaveToDisk
    @SyncToClient
    private final NotifiableItemStackHandler outputItem;
    @Getter
    @SaveToDisk
    @SyncToClient
    private final NotifiableFluidTank inputFluid;
    @Getter
    @SaveToDisk
    @SyncToClient
    private final NotifiableFluidTank outputFluid;

    /** 其他位置存储 */
    @SaveToDisk
    private final CustomItemStackHandler cardHandler;

    /** 玩家信息（只在服务端有值：不做客户端同步，界面里一律经服务端 supplier 取值） */
    @Getter
    @SaveToDisk
    private UUID uuid;
    @Getter
    @SaveToDisk
    List<UUID> sharedUUIDs = new ArrayList<>();
    @Getter
    @SaveToDisk
    private UUID teamUUID;

    /** 交易信息 */
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int groupSelected = 0;
    private int shopSelected = -1;

    /////////////////////////////////////
    // ********* 生命周期管理 ********* //
    /////////////////////////////////////

    public TradingStationMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder);

        cardHandler = new CustomItemStackHandler();
        cardHandler.setFilter(i -> i.getItem() == GTOItems.GREG_MEMBERSHIP_CARD.asItem());
        cardHandler.setOnContentsChanged(() -> initializationInformation(cardHandler.getStackInSlot(0)));

        inputItem = new NotifiableItemStackHandler(this, 32 * tier, IO.IN, IO.BOTH);
        outputItem = new NotifiableItemStackHandler(this, 32 * tier, IO.OUT, IO.OUT);
        inputFluid = new NotifiableFluidTank(this, tier * 4, 1000 * (8000 << tier), IO.IN, IO.BOTH);
        outputFluid = new NotifiableFluidTank(this, tier * 4, 1000 * (8000 << tier), IO.OUT, IO.OUT);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        initializationInformation(cardHandler.getStackInSlot(0));
        if (!isRemote()) {
            outputItemChangeSub = outputItem.addChangedListener(this::updateAutoOutputSubscription);
            outputFluidChangeSub = outputFluid.addChangedListener(this::updateAutoOutputSubscription);
            updateAutoOutputSubscription();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
        if (outputItemChangeSub != null) {
            outputItemChangeSub.unsubscribe();
            outputItemChangeSub = null;
        }
        if (outputFluidChangeSub != null) {
            outputFluidChangeSub.unsubscribe();
            outputFluidChangeSub = null;
        }
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(cardHandler);
        clearInventory(inputItem.storage);
        clearInventory(outputItem.storage);
    }

    /////////////////////////////////////
    // ************ UI实现 ************ //
    /////////////////////////////////////

    private static final String TEXT_HEADER = "gtocore.trading_station.textList.";
    private static final String CURRENCY_HEADER = "gtocore.currency.";
    private static final String HELP_TITLE = "gtocore.trading_station.help";
    private static final String OPEN_HELP = "gtocore.trading_station.open_help";
    private static final String UNKNOWN_PLAYER = "Unknown";
    /// 没有值时的占位
    private static final Component NO_VALUE = Component.literal("—");
    /// 交易解锁页里解锁项列表的高度
    private static final int KEY_LIST_HEIGHT = 4 * UISizes.CONTROL_HEIGHT;
    /// 每行/每页的交易格数（与原实现一致：商店 4×8，解锁 4×8）
    private static final int SHOP_COLUMNS = 8;
    private static final int SHOP_PER_PAGE = 32;
    private static final int UNLOCK_COLUMNS = 8;
    private static final int UNLOCK_PER_PAGE = 32;
    /// 存储页每行几组（一组 = 左边输入、右边输出）
    private static final int STORAGE_PER_ROW = 4;

    private final HelpTab helpTab = new HelpTab();
    private List<Component> helpLines;

    // ==================== 主页 ====================

    /** 主页：会员卡、会员信息、商店组切换与使用说明入口。 */
    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var page = UIElement.column(UISizes.CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));
        page.addChild(memberSection());
        page.addChild(shopGroupSection());
        page.addChild(Button.translatable(LayoutStyle.AUTO, OPEN_HELP)
                .setOnClick(clickData -> openPage(widget, helpTab)));
        return page;
    }

    /** 会员卡槽 + 刷新按钮（贴图与名字两端相同），下面一行会员信息由服务端取值下发。 */
    private Widget memberSection() {
        var section = UIElement.section(UISizes.CONTENT_WIDTH);
        var card = new ItemSlot(cardHandler, 0, true, true);
        card.setHoverTooltips(trans(11));
        var refresh = Button.translatable(LayoutStyle.AUTO, "↻").layout(l -> l.flex(1));
        refresh.setHoverTooltips(trans(8));
        refresh.setOnServerClick(clickData -> refreshMembership(refresh));
        var row = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
        row.addChildren(card, refresh);
        section.addChild(row);

        // 会员信息：文字、等级、悬停说明都在服务端取值（客户端只显示下发的内容）
        var status = new StatusPanel();
        status.addSentence(this::memberText)
                .level(() -> uuid == null ? StatusLine.Level.WARNING : StatusLine.Level.GOOD)
                .detail(this::sharedText);
        section.addChild(status);
        return section;
    }

    /** 当前商店组的名称与图标 + 组切换图标；切组会换掉页签结构，由服务端重开界面。 */
    private Widget shopGroupSection() {
        var section = UIElement.section(UISizes.CONTENT_WIDTH);
        var group = currentGroup();
        var name = TextLine.of(LayoutStyle.AUTO, this::currentGroupName);
        name.layout(l -> l.flex(1));
        var header = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
        header.addChildren(new ItemView(group == null ? IGuiTexture.EMPTY : group.getTexture1(), UISizes.SLOT), name);
        section.addChild(header);
        section.addChild(shopGroupSwitcher());
        return section;
    }

    /** 商店组切换：格子数与图标取两端一致的注册数据，选中状态由服务端下发。 */
    private Widget shopGroupSwitcher() {
        // 宽度按区块内宽（区块左右各有内边距），一行 8 个图标
        var grid = new UIElement().layout(l -> l.row().flexWrap(FlexWrap.WRAP).gapAll(UISizes.GAP)
                .width(UISizes.CONTENT_WIDTH - 2 * UITheme.PANEL_PADDING));
        for (int index = 0; index < TradingManager.INSTANCE.getGroupCount(); index++) {
            grid.addChild(shopGroupButton(index));
        }
        return grid;
    }

    private Widget shopGroupButton(int index) {
        var group = TradingManager.INSTANCE.getShopGroup(index);
        var cell = new UIElement().layout(l -> l.size(Button.ICON_SIZE, Button.ICON_SIZE));
        if (group == null) return cell;
        var selected = cell.addSyncValue(SyncValue.of(() -> groupSelected == index, SyncValue.BOOLEAN, false));
        cell.addChild(Button.icon(group.getTexture2())
                .setVariant(() -> selected.getValue() ? UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT)
                .setOnClick(clickData -> selectShopGroup(cell, index))
                .bindTooltip(() -> Component.translatable(group.getName())));
        return cell;
    }

    /** 两端同步跳转到指定页，与点击顶部页签的行为一致。 */
    private static void openPage(FancyMachineUIWidget widget, IFancyUIProvider page) {
        var tabs = widget.getSideTabsWidget();
        tabs.selectTab(page);
        tabs.getOnTabClick().accept(page);
    }

    /** 服务端：使用说明（内容固定，取一次存下来，避免每刻重建整表文字）。 */
    private void helpText(List<Component> lines) {
        if (helpLines == null) helpLines = List.copyOf(GTOMachineTooltips.PanGalaxyGregTechTradingStationIntroduction.get());
        lines.addAll(helpLines);
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GuiTextures.GREGTECH_LOGO;
    }

    // ==================== 页签 ====================

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setId("fancy_side_tabs");
        sideTabs.clearSubTabs();
        sideTabs.setMainTab(this);

        // 固定页签（与原实现一致：只在第一个商店组里显示）
        List<IFancyUIProvider> fixedTabs = new ArrayList<>();
        if (groupSelected == 0) {
            fixedTabs.add(helpTab);
            fixedTabs.add(itemStorageTab());
            fixedTabs.add(fluidStorageTab());
            fixedTabs.add(new UnlockTab());
        }

        // 当前商店组的商店页签
        List<IFancyUIProvider> shopTabs = shopTabs();
        fixedTabs.forEach(sideTabs::attachSubTab);
        shopTabs.forEach(sideTabs::attachSubTab);
        sideTabs.attachSubTab(CombinedDirectionalFancyConfigurator.of(this, this));

        sideTabs.setOnTabSwitch((oldTab, newTab) -> {
            if (newTab instanceof ShopTab shopTab) {
                // 只允许选择当前组的商店页签
                if (shopTab.groupIndex == groupSelected) {
                    shopSelected = shopTab.shopIndex;
                } else {
                    shopSelected = -1;
                    sideTabs.selectTab(sideTabs.getMainTab());
                }
            } else {
                shopSelected = -1;
            }
            sideTabs.detectAndSendChanges();

            var modularUI = sideTabs.getGui();
            if (modularUI != null && modularUI.getModularUIGui() != null) modularUI.getModularUIGui().init();
        });

        if (shopSelected != -1 && shopSelected < shopTabs.size()) {
            sideTabs.selectTab(shopTabs.get(shopSelected));
        } else {
            sideTabs.selectTab(sideTabs.getMainTab());
        }
        sideTabs.detectAndSendChanges();
    }

    /** 当前组的商店页签；组索引无效时没有商店页签（不抛异常）。 */
    private List<IFancyUIProvider> shopTabs() {
        List<IFancyUIProvider> tabs = new ArrayList<>();
        var group = TradingManager.INSTANCE.getShopGroup(groupSelected);
        if (group == null) return tabs;
        for (int shop = 0; shop < group.getShopCount(); shop++) {
            var tradingShop = group.getShop(shop);
            if (tradingShop != null) tabs.add(new ShopTab(groupSelected, shop, tradingShop));
        }
        return tabs;
    }

    // ==================== 使用说明页 ====================

    private final class HelpTab implements IFancyUIProvider {

        private static final int SERVER_HEIGHT_LIMIT = Integer.MAX_VALUE / 4;

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var text = new RichText();
            text.textSupplier(isRemote() ? null : TradingStationMachine.this::helpText);
            var scroller = new ScrollerView("trading_station.help", UISizes.CONTENT_WIDTH, UISizes.SLOT)
                    .setResizable(false)
                    .layoutContent(l -> l.paddingAll(UITheme.PANEL_PADDING));
            scroller.addScrollViewChild(text);
            scroller.adaptiveHeight(widget.isRemote() ? MachineWindow.clientPageHeightLimit(false) : SERVER_HEIGHT_LIMIT);
            scroller.setBackground(UITheme.STATUS_PANEL);
            return UIElement.column(UISizes.CONTENT_WIDTH).addChild(scroller);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return WidgetIcons.INFO;
        }

        @Override
        public Component getTitle() {
            return Component.translatable(HELP_TITLE);
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(getTitle());
        }

        @Override
        public boolean hasPlayerInventory() {
            return false;
        }
    }

    // ==================== 物品/流体存储页 ====================

    /** 物品存储：每行 4 组（左输入、右输出），格数取机器的物品栏（两端相同）。 */
    private IFancyUIProvider itemStorageTab() {
        return new IFancyUIProvider() {

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                if (widget instanceof MachineWindow window) window.setInventoryGutter(ScrollerView.SCROLL_BAR_SPACE);
                var page = UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));
                page.addChild(TextLine.translatable(LayoutStyle.AUTO, "gtocore.trading_station.item_storage"));
                var scroller = new ScrollerView("trading_station.items", UISizes.SLOT_ROW_WIDTH + ScrollerView.SCROLL_BAR_SPACE, UISizes.MACHINE_PAGE_HEIGHT)
                        .adaptiveHeight(UISizes.MACHINE_PAGE_HEIGHT)
                        .verticalScrollDisplay(ScrollerView.ScrollDisplay.ALWAYS);
                scroller.addScrollViewChild(storageGrid(inputItem.getSlots(), outputItem.getSlots(),
                        index -> new ItemSlot(inputItem, index, true, true),
                        index -> new ItemSlot(outputItem, index, true, false)));
                page.addChild(scroller);
                return page;
            }

            @Override
            public IGuiTexture getTabIcon() {
                return new ItemStackTexture(Blocks.CHEST.asItem());
            }

            @Override
            public Component getTitle() {
                return Component.translatable("gtocore.trading_station.item_storage");
            }

            @Override
            public List<Component> getTabTooltips() {
                return List.of(Component.translatable("gtocore.trading_station.item_storage"));
            }
        };
    }

    /** 流体存储：布局同物品存储，用流体槽。 */
    private IFancyUIProvider fluidStorageTab() {
        return new IFancyUIProvider() {

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                if (widget instanceof MachineWindow window) window.setInventoryGutter(ScrollerView.SCROLL_BAR_SPACE);
                var page = UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));
                page.addChild(TextLine.translatable(LayoutStyle.AUTO, "gtocore.trading_station.fluid_storage"));
                var scroller = new ScrollerView("trading_station.fluids", UISizes.SLOT_ROW_WIDTH + ScrollerView.SCROLL_BAR_SPACE, UISizes.MACHINE_PAGE_HEIGHT)
                        .adaptiveHeight(UISizes.MACHINE_PAGE_HEIGHT)
                        .verticalScrollDisplay(ScrollerView.ScrollDisplay.ALWAYS);
                scroller.addScrollViewChild(storageGrid(inputFluid.getTanks(), outputFluid.getTanks(),
                        index -> new FluidSlot(inputFluid, index, true, true),
                        index -> new FluidSlot(outputFluid, index, true, true)));
                page.addChild(scroller);
                return page;
            }

            @Override
            public IGuiTexture getTabIcon() {
                return new ItemStackTexture(Items.BUCKET);
            }

            @Override
            public Component getTitle() {
                return Component.translatable("gtocore.trading_station.fluid_storage");
            }

            @Override
            public List<Component> getTabTooltips() {
                return List.of(Component.translatable("gtocore.trading_station.fluid_storage"));
            }
        };
    }

    /**
     * 一行 {@code perRow} 组、一组是"左边一个输入槽，右边一个输出槽"的网格，整行正好 9 格宽。
     * 槽数与槽的注册顺序只依赖两端一致的机器字段（两端的原生容器槽位才会一一对应）。
     */
    private static Widget storageGrid(int inputCount, int outputCount, IntFunction<Widget> inputSlot, IntFunction<Widget> outputSlot) {
        var grid = new UIElement().layout(l -> l.row().flexWrap(FlexWrap.WRAP).width(UISizes.SLOT_ROW_WIDTH));
        int rows = Math.max(rowsOf(inputCount), rowsOf(outputCount));
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < STORAGE_PER_ROW; col++) {
                int index = row * STORAGE_PER_ROW + col;
                grid.addChild(index < inputCount ? inputSlot.apply(index) : UIElement.spacer(UISizes.SLOT, UISizes.SLOT));
            }
            // 中间空一列，输入与输出分开
            grid.addChild(UIElement.spacer(UISizes.SLOT, UISizes.SLOT));
            for (int col = 0; col < STORAGE_PER_ROW; col++) {
                int index = row * STORAGE_PER_ROW + col;
                grid.addChild(index < outputCount ? outputSlot.apply(index) : UIElement.spacer(UISizes.SLOT, UISizes.SLOT));
            }
        }
        return grid;
    }

    private static int rowsOf(int count) {
        return (count + STORAGE_PER_ROW - 1) / STORAGE_PER_ROW;
    }

    // ==================== 交易解锁页 ====================

    /** 交易解锁：上面是解锁项列表，下面是选中解锁项的交易格。 */
    private final class UnlockTab implements IFancyUIProvider {

        private final TradeGrid grid;
        @Nullable
        private String selectedKey;

        private UnlockTab() {
            grid = new TradeGrid(-1, -1, UNLOCK_COLUMNS, UNLOCK_PER_PAGE,
                    () -> selectedKey == null ? 0 : UnlockManager.INSTANCE.getEntryTradeCount(selectedKey),
                    index -> selectedKey == null ? null : UnlockManager.INSTANCE.getTradeEntry(selectedKey, index));
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = UIElement.column(UISizes.CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));
            page.addChild(UIElement.section(UISizes.CONTENT_WIDTH).addChildren(
                    TextLine.translatable(LayoutStyle.AUTO, TEXT_HEADER + 21),
                    keyListView()));
            page.addChild(TextLine.of(LayoutStyle.AUTO, this::selectedKeyName));
            page.addChild(grid);
            page.addChild(grid.pageRow());
            return page;
        }

        /** 解锁项列表：条目名与数量取两端一致的注册数据，选中状态由服务端下发。 */
        private Widget keyListView() {
            var scroller = new ScrollerView("trading_station.unlock", UISizes.SLOT_ROW_WIDTH, KEY_LIST_HEIGHT)
                    .adaptiveHeight(KEY_LIST_HEIGHT)
                    .layoutContent(l -> l.gapAll(UISizes.GAP));
            var keys = UnlockManager.INSTANCE.getKeySet();
            if (keys != null) {
                for (String key : keys) scroller.addScrollViewChild(keyButton(key));
            }
            return scroller;
        }

        private Widget keyButton(String key) {
            var cell = new UIElement();
            var selected = cell.addSyncValue(SyncValue.of(() -> key.equals(selectedKey), SyncValue.BOOLEAN, false));
            cell.addChild(Button.translatable(LayoutStyle.AUTO, key)
                    .setVariant(() -> selected.getValue() ? UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT)
                    .setOnClick(clickData -> selectKey(key)));
            return cell;
        }

        /** 选中解锁项：换掉整片交易格（结构变化，两端各执行一次），页码回到第一页。 */
        private void selectKey(String key) {
            if (key.equals(selectedKey)) return;
            selectedKey = key;
            grid.reset();
        }

        /** 服务端：当前解锁项的名字（没选时占位）。 */
        private Component selectedKeyName() {
            return selectedKey == null ? NO_VALUE : Component.translatable(selectedKey);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return WidgetIcons.INPUT_LIMIT_ON;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("gtocore.trading_station.unlock_shop");
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable("gtocore.trading_station.unlock_shop"));
        }
    }

    // ==================== 商店页 ====================

    /** 一个商店：解锁状态与机器钱包余额在服务端算，交易格每页 16 个（2 行 × 8 列）。 */
    private final class ShopTab implements IFancyUIProvider {

        private final int groupIndex;
        @Getter
        private final int shopIndex;
        private final TradingManager.TradingShop shop;
        private final TradeGrid grid;

        private ShopTab(int groupIndex, int shopIndex, TradingManager.TradingShop shop) {
            this.groupIndex = groupIndex;
            this.shopIndex = shopIndex;
            this.shop = shop;
            this.grid = new TradeGrid(groupIndex, shopIndex, SHOP_COLUMNS, SHOP_PER_PAGE,
                    () -> TradingManager.INSTANCE.getTradeCount(groupIndex, shopIndex),
                    index -> TradingManager.INSTANCE.getTradeEntryByIndices(groupIndex, shopIndex, index));
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = UIElement.column(UISizes.CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));
            page.addChild(shopStatus());
            page.addChild(grid);
            page.addChild(grid.pageRow());
            return page;
        }

        /** 商店名、解锁状态与货币余额：全部由服务端取值下发。 */
        private Widget shopStatus() {
            var status = new StatusPanel();
            status.addSentence(this::shopName)
                    .level(() -> shopUnlocked(groupIndex, shopIndex) ? StatusLine.Level.GOOD : StatusLine.Level.ERROR)
                    .detail(this::shopDetail);
            var currencies = shop.getCurrencies();
            if (currencies != null && !currencies.isEmpty()) {
                // 每行一个货币 → 行的顺序就是子控件顺序，而 LDLib 的点击/初始数据都按"父控件的子控件下标"路由，
                // 两端必须完全一致。注册数据是 Set.of(...)，元素多于 2 个时 JDK 的不可变集合按 per-JVM 随机盐排布，
                // 顺序不保证两端相同；这里排序固定下来（建页路径，一次性的小数组）。
                String[] currencyIds = currencies.toArray(new String[0]);
                Arrays.sort(currencyIds);
                for (String currency : currencyIds) {
                    status.addLine(CURRENCY_HEADER + currency, () -> currencyAmount(currency));
                }
            }
            return status;
        }

        /** 服务端：商店名。 */
        private Component shopName() {
            return Component.translatable(shop.getName());
        }

        /** 服务端：未解锁时说明还差什么，解锁后说明解锁条件。 */
        private Component shopDetail() {
            var condition = unlockName(shop.getUnlockCondition());
            return shopUnlocked(groupIndex, shopIndex) ? condition : trans(20, condition);
        }

        /** 服务端：机器钱包里该货币的数量。 */
        private Component currencyAmount(String currency) {
            return Component.literal(FormattingUtil.formatNumbers(WalletUtils.getCurrencyAmount(uuid, getLevel(), currency)));
        }

        @Override
        public IGuiTexture getTabIcon() {
            return shop.getTexture();
        }

        @Override
        public Component getTitle() {
            return Component.translatable(shop.getName());
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable(shop.getName()));
        }
    }

    // ==================== 交易格网格 ====================

    /**
     * 交易格网格：每页 {@code perPage} 格（{@code perRow} 列 × 若干行）。
     * 换页、换解锁项会换掉整片格子（控件树结构变化），所以那些点击用 {@link Button#setOnClick}：两端各重建一次
     * 同样的结构（只依据静态注册数据），格子里显示的数值仍由服务端算。
     */
    private final class TradeGrid extends UIElement {

        /// {@code groupIndex < 0}：不检查商店解锁（交易解锁页）
        private final int groupIndex;
        private final int shopIndex;
        private final int perRow;
        private final int perPage;
        private final IntSupplier totalCount;
        private final IntFunction<TradeEntry> entryAt;
        private int pageSelected;

        private TradeGrid(int groupIndex, int shopIndex, int perRow, int perPage, IntSupplier totalCount, IntFunction<TradeEntry> entryAt) {
            this.groupIndex = groupIndex;
            this.shopIndex = shopIndex;
            this.perRow = perRow;
            this.perPage = perPage;
            this.totalCount = totalCount;
            this.entryAt = entryAt;
            layout(l -> l.column().gapAll(UISizes.GAP));
            rebuild();
        }

        /** 总页数（至少 1 页）。 */
        private int totalPages() {
            return Math.max(1, (totalCount.getAsInt() + perPage - 1) / perPage);
        }

        /** 翻页（两端各执行一次）。 */
        private void changePage(int delta) {
            int next = Mth.clamp(pageSelected + delta, 0, totalPages() - 1);
            if (next == pageSelected) return;
            pageSelected = next;
            rebuild();
        }

        /** 回到第一页并重建（换解锁项时两端各执行一次）。 */
        private void reset() {
            pageSelected = 0;
            rebuild();
        }

        /** 按当前页码重建格子：两端结构一致，服务端随后给新加的格子补发初始数据。 */
        private void rebuild() {
            clearAllWidgets();
            int start = pageSelected * perPage;
            int count = totalCount.getAsInt();
            for (int row = 0; row < Math.max(1, perPage / perRow); row++) {
                var line = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.GAP));
                for (int col = 0; col < perRow; col++) {
                    int index = start + row * perRow + col;
                    var entry = index < count ? entryAt.apply(index) : null;
                    line.addChild(entry == null ? UIElement.spacer(UISizes.SLOT, UISizes.SLOT) :
                            new TradeCell(groupIndex, shopIndex, entry));
                }
                addChild(line);
            }
        }

        /** 翻页行：[←] 页码 [→]，页码文字与两端禁用状态都由服务端算。 */
        private Widget pageRow() {
            var row = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
            var label = TextLine.of(LayoutStyle.AUTO, this::pageText);
            label.layout(l -> l.flex(1));
            row.addChildren(Button.icon(UITheme.ARROW_LEFT)
                    .setOnClick(clickData -> changePage(-1))
                    .disabled(() -> pageSelected <= 0, null),
                    label,
                    Button.icon(UITheme.ARROW_RIGHT)
                            .setOnClick(clickData -> changePage(1))
                            .disabled(() -> pageSelected + 1 >= totalPages(), null));
            return row;
        }

        /** 服务端：页码文字。 */
        private Component pageText() {
            return Component.literal(" " + (pageSelected + 1) + " / " + totalPages() + " ");
        }
    }

    // ==================== 单个交易格 ====================

    /**
     * 一个交易格：图标按钮 + 悬停说明。说明里的解锁状态、可交易次数、价格与产出都在服务端算好后下发
     * （不用 {@code bindTooltip}：它在建界面时两端都会取一次值，客户端取不了钱包和背包数据）。
     * 点击只在服务端执行，倍率取点击时的 Ctrl / Shift。
     * <p>
     * 悬停说明按<b>行</b>同步：LDLib 的悬停提示是"一个 {@link Component} 一行"，所以这里下发的是
     * {@code List<Component>}（自己写 {@link SyncValue.Codec}），服务端把每一行分开算好，
     * 客户端逐行交给按钮，不会挤在一行里。
     */
    private final class TradeCell extends UIElement {

        /// 悬停说明的重算间隔（tick）：里面的判定要读钱包与输入输出栏，不必每刻都算
        private static final int REFRESH_TICKS = 10;

        /// 逐行同步悬停说明：行数 + 每行的组件（提示接口要的是"每行一个组件"，不能拼成一条带换行符的文字）
        private static final SyncValue.Codec<List<Component>> TOOLTIP_LINES_CODEC = new SyncValue.Codec<>() {

            @Override
            public void write(FriendlyByteBuf buf, List<Component> lines) {
                buf.writeVarInt(lines.size());
                for (Component line : lines) buf.writeComponent(line);
            }

            @Override
            public List<Component> read(FriendlyByteBuf buf) {
                int size = buf.readVarInt();
                List<Component> lines = new ArrayList<>(size);
                for (int i = 0; i < size; i++) lines.add(buf.readComponent());
                return lines;
            }
        };

        private final int groupIndex;
        private final int shopIndex;
        private final TradeEntry entry;
        private List<Component> cachedTooltip = List.of();
        private boolean refreshed;
        private int refreshedAt;

        private TradeCell(int groupIndex, int shopIndex, TradeEntry entry) {
            this.groupIndex = groupIndex;
            this.shopIndex = shopIndex;
            this.entry = entry;
            layout(l -> l.size(UISizes.SLOT, UISizes.SLOT));
            var button = Button.icon(entry.texture(), UISizes.SLOT);
            button.setOnServerClick(this::executeTrade);
            button.disabled(this::locked, null);
            addChild(button);
            addSyncValue(SyncValue.of(this::tooltipLines, TOOLTIP_LINES_CODEC, List.of())
                    .onChanged(lines -> button.setHoverTooltips(lines.toArray(Component[]::new))));
        }

        /**
         * 服务端：商店未解锁、交易未解锁时这格不能交易（滚轮判定与点击时都会再判一次）。
         * 客户端一律按"不可交易"回答：能不能交易由服务端下发的禁用状态与点击时的服务端判定决定，
         * 客户端不会去读钱包和背包。
         */
        private boolean locked() {
            if (TradingStationMachine.this.isRemote()) return true;
            if (groupIndex >= 0 && !shopUnlocked(groupIndex, shopIndex)) return true;
            return !entryUnlocked(entry);
        }

        /** 服务端：执行交易（Ctrl 十倍、Ctrl + Shift 百倍），能否交易与倍率都在这里判定。 */
        private void executeTrade(ClickData clickData) {
            if (clickData.isRemote || locked()) return;
            int multiplier = clickData.isCtrlClick ? (clickData.isShiftClick ? 100 : 10) : 1;
            entry.executeTrade(tradeData(), multiplier);
        }

        /** 只在服务端执行：悬停说明逐行由服务端拼好下发，按 {@link #REFRESH_TICKS} 重算一次。 */
        private List<Component> tooltipLines() {
            int now = getOffsetTimer();
            if (refreshed && now >= refreshedAt && now - refreshedAt < REFRESH_TICKS) return cachedTooltip;
            refreshed = true;
            refreshedAt = now;
            cachedTooltip = buildTooltipLines();
            return cachedTooltip;
        }

        /** 服务端：一行一个组件——状态行、以及交易说明里的每一条输入/产出各占一行。 */
        private List<Component> buildTooltipLines() {
            var data = tradeData();
            List<Component> lines = new ArrayList<>(4);
            if (groupIndex >= 0 && !shopUnlocked(groupIndex, shopIndex)) {
                lines.add(trans(20, unlockName(entry.unlockCondition())).withStyle(ChatFormatting.RED));
            } else if (!entryUnlocked(entry)) {
                lines.add(Component.translatable("gtocore.trade_group.unlock", unlockName(entry.unlockCondition()))
                        .withStyle(ChatFormatting.DARK_RED));
            } else if (entry.canExecuteCount(data) == 0) {
                lines.add(Component.translatable("gtocore.trade_group.unsatisfied").withStyle(ChatFormatting.DARK_RED));
            } else {
                int amount = entry.check(data);
                lines.add(Component.translatable("gtocore.trade_group.amount", FormattingUtil.formatNumbers(amount))
                        .withStyle(ChatFormatting.GOLD));
                if (amount >= 10) lines.add(Component.translatable("gtocore.trade_group.repeatedly1"));
                if (amount >= 100) lines.add(Component.translatable("gtocore.trade_group.repeatedly2"));
            }
            lines.addAll(entry.getDescription());
            return lines;
        }
    }

    // ==================== 服务端数据与动作 ====================

    /** 交易数据（只在服务端构造：玩家 UUID、队伍只存在服务端）。 */
    private TradeData tradeData() {
        return new TradeData(getLevel(), getPos(), inputItem, outputItem, inputFluid, outputFluid, uuid, sharedUUIDs, teamUUID);
    }

    /** 服务端：商店是否已解锁。 */
    private boolean shopUnlocked(int groupIndex, int shopIndex) {
        var shop = TradingManager.INSTANCE.getShopByIndices(groupIndex, shopIndex);
        return WalletUtils.containsTagValueInWallet(uuid, getLevel(), UNLOCK_SHOP, shop.getUnlockCondition());
    }

    /** 服务端：交易是否已解锁。 */
    private boolean entryUnlocked(TradeEntry entry) {
        return WalletUtils.containsTagValueInWallet(uuid, getLevel(), UNLOCK_TRADE, entry.unlockCondition());
    }

    private static Component unlockName(@Nullable String key) {
        return key == null ? NO_VALUE : Component.translatable(key);
    }

    /**
     * 切换商店组（两端各执行一次）：两端都改成本地的组索引，客户端的控件树才不会和服务端走岔；
     * 服务端再重开界面，页签、窗口尺寸按新的组整体重建。
     */
    private void selectShopGroup(Widget source, int index) {
        if (groupSelected == index) return;
        groupSelected = index;
        shopSelected = -1;
        if (isRemote()) return;
        markAsDirty();
        reopenUI(source);
    }

    /** 刷新（服务端）：重新读会员卡上的玩家信息，再重开界面。 */
    private void refreshMembership(Widget source) {
        if (isRemote()) return;
        initializationInformation(cardHandler.getStackInSlot(0));
        reopenUI(source);
    }

    /** 服务端：重开界面，让两端按最新的数据重建控件树（页签结构会变，只能整棵重建）。 */
    private void reopenUI(Widget source) {
        if (isRemote()) return;
        var gui = source.getGui();
        Player player = gui == null ? null : gui.entityPlayer;
        if (player == null) return;
        if (shouldOpenUI(player, InteractionHand.MAIN_HAND, null)) {
            tryToOpenUI(player, InteractionHand.MAIN_HAND, null);
        }
    }

    /** 服务端：当前组的名字（组索引无效时占位）。 */
    private Component currentGroupName() {
        var group = currentGroup();
        return group == null ? NO_VALUE : Component.translatable(group.getName());
    }

    @Nullable
    private TradingManager.TradingShopGroup currentGroup() {
        var group = TradingManager.INSTANCE.getShopGroup(groupSelected);
        return group != null ? group : TradingManager.INSTANCE.getShopGroup(0);
    }

    /** 服务端：会员信息一行（没有卡时提示放入会员卡）。 */
    private Component memberText() {
        if (uuid == null) return trans(2);
        return trans(3, Component.literal(playerName(uuid)));
    }

    /** 服务端：共享名单（没有共享时为空，不显示悬停说明）。 */
    private Component sharedText() {
        if (uuid == null) return Component.empty();
        var players = WalletUtils.getAllWalletPlayers(getLevel());
        var shared = new StringBuilder();
        for (UUID id : sharedUUIDs) {
            if (shared.length() > 0) shared.append(", ");
            shared.append(players.getOrDefault(id, UNKNOWN_PLAYER));
        }
        var team = teamUUID != null && !teamUUID.equals(uuid) ? TeamUtil.getName(getLevel(), uuid) : null;
        if (shared.length() == 0 && team == null) return Component.empty();
        var text = trans(4).copy();
        if (shared.length() > 0) text.append(Component.literal(shared.toString()));
        if (team != null) text.append(team);
        return text;
    }

    /** 服务端：会员卡上记录的玩家名。 */
    private String playerName(UUID playerUUID) {
        return WalletUtils.getAllWalletPlayers(getLevel()).getOrDefault(playerUUID, UNKNOWN_PLAYER);
    }

    // ==================== 辅助类与方法 ====================

    // 机器基础翻译键
    private static @NotNull MutableComponent trans(int id, Object... args) {
        return Component.translatable(TEXT_HEADER + id, args);
    }

    // 玩家信息初始化
    private void initializationInformation(ItemStack card) {
        if (card.getItem() == GTOItems.GREG_MEMBERSHIP_CARD.asItem()) {
            this.uuid = getSingleUuid(card);
            this.sharedUUIDs = getSharedUuids(card);
            if (uuid != null) {
                this.teamUUID = TeamUtil.getTeamUUID(uuid);
            }
        } else {
            this.uuid = null;
            this.sharedUUIDs = new ArrayList<>();
            this.teamUUID = null;
        }
    }

    /////////////////////////////////////
    // ********* 自动输出实现 ********* //
    /////////////////////////////////////

    @SaveToDisk(defaultValue = "DOWN")
    @SyncToClient(scheduleUpdate = true)
    private Direction outputFacingItems = Direction.DOWN;
    @SaveToDisk(defaultValue = "DOWN")
    @SyncToClient(scheduleUpdate = true)
    private Direction outputFacingFluids = Direction.DOWN;
    @SaveToDisk(defaultValue = "false")
    @SyncToClient(scheduleUpdate = true)
    private boolean autoOutputItems = false;
    @SaveToDisk(defaultValue = "false")
    @SyncToClient(scheduleUpdate = true)
    private boolean autoOutputFluids = false;
    @Nullable
    private TickableSubscription autoOutputSubs;
    @Nullable
    private ISubscription outputItemChangeSub;
    @Nullable
    private ISubscription outputFluidChangeSub;
    private boolean allowInputFromOutputSideItems;
    private boolean allowInputFromOutputSideFluids;

    @Override
    public boolean hasAutoOutputItem() {
        return outputItem.getSlots() > 0;
    }

    @Override
    public boolean isAutoOutputItems() {
        return autoOutputItems;
    }

    @Override
    public void setAutoOutputItems(boolean autoOutputItems) {
        if (hasAutoOutputItem()) {
            this.autoOutputItems = autoOutputItems;
            updateAutoOutputSubscription();
        }
    }

    @Nullable
    @Override
    public Direction getOutputFacingItems() {
        return hasAutoOutputItem() ? outputFacingItems : null;
    }

    @Override
    public void setOutputFacingItems(@Nullable Direction direction) {
        if (hasAutoOutputItem() && direction != null) {
            this.outputFacingItems = direction;
            clearDirectionCache();
            updateAutoOutputSubscription();
        }
    }

    @Override
    public boolean hasAutoOutputFluid() {
        return outputFluid.getTanks() > 0;
    }

    @Override
    public boolean isAutoOutputFluids() {
        return autoOutputFluids;
    }

    @Override
    public void setAutoOutputFluids(boolean autoOutputFluids) {
        if (hasAutoOutputFluid()) {
            this.autoOutputFluids = autoOutputFluids;
            updateAutoOutputSubscription();
        }
    }

    @Nullable
    @Override
    public Direction getOutputFacingFluids() {
        return hasAutoOutputFluid() ? outputFacingFluids : null;
    }

    @Override
    public void setOutputFacingFluids(@Nullable Direction direction) {
        if (hasAutoOutputFluid() && direction != null) {
            this.outputFacingFluids = direction;
            clearDirectionCache();
            updateAutoOutputSubscription();
        }
    }

    private void updateAutoOutputSubscription() {
        if (getLevel() == null || isRemote()) return;
        if ((autoOutputItems && !outputItem.isEmpty() && getOutputFacingItems() != null && holder.blockEntityDirectionCache.hasAdjacentItemHandler(getLevel(), getPos(), getOutputFacingItems())) || (autoOutputFluids && !outputFluid.isEmpty() && getOutputFacingFluids() != null && holder.blockEntityDirectionCache.hasAdjacentFluidHandler(getLevel(), getPos(), getOutputFacingFluids()))) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput, 20);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    private void autoOutput() {
        if (autoOutputItems && getOutputFacingItems() != null) {
            outputItem.exportToNearby(getOutputFacingItems());
        }
        if (autoOutputFluids && getOutputFacingFluids() != null) {
            outputFluid.exportToNearby(getOutputFacingFluids());
        }
        updateAutoOutputSubscription();
    }

    @Override
    public void setAllowInputFromOutputSideItems(boolean allowInputFromOutputSideItems) {
        this.clearDirectionCache();
        this.allowInputFromOutputSideItems = allowInputFromOutputSideItems;
    }

    @Override
    public void setAllowInputFromOutputSideFluids(boolean allowInputFromOutputSideFluids) {
        this.clearDirectionCache();
        this.allowInputFromOutputSideFluids = allowInputFromOutputSideFluids;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return allowInputFromOutputSideItems;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return allowInputFromOutputSideFluids;
    }

    @Override
    public void onNeighborChanged(@NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    /////////////////////////////////////
    // ********** 是否运行中 ********** //
    /////////////////////////////////////

    @SaveToDisk(defaultValue = "false")
    private boolean working = false;

    @Override
    public boolean isWorkingEnabled() {
        return working;
    }

    @Override
    public void setWorkingEnabled(boolean var1) {
        this.working = var1;
    }
}

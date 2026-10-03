package com.gtocore.common.machine.noenergy;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.common.data.translation.GTOMachineTooltips;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTTickTimeMonitors;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.IconToggle;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.uiwidgets.side.SideOverviewTab;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gtocore.common.data.GTOItems.*;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VillageTradingStationMachine extends MetaMachine implements IAutoOutputItem, IFancyUIMachine, IMachineLife {

    // 定时任务订阅
    private TickableSubscription tickSubs;
    @Nullable
    private TickableSubscription autoOutputSubs;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor autoOutputMonitor = holder.monitorTick(GTTickTimeMonitors.AUTO_OUTPUT, this::autoOutput);
    @Nullable
    private ISubscription exportItemSubs;

    // 输入输出物品存储
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> input;
    @SaveToDisk
    private final NotifiableInventory<AEItemKey> output;

    // 村民存储与配置
    @SaveToDisk
    @SyncToClient
    private final VillageHolder villagers;
    @SaveToDisk
    @SyncToClient
    private final boolean[] isLocked = new boolean[10];
    @SaveToDisk
    @SyncToClient
    private final int[] selected = new int[10];
    @SaveToDisk
    @SyncToClient
    private final boolean[] startUp = new boolean[10];

    private final VillagerRecipe[][] villagersDataset = new VillagerRecipe[10][];
    private final ItemStackHandler RecipesHandler = new ItemStackHandler(3 * 10);

    // 升级物品
    @SaveToDisk
    private final KeyInventory<AEItemKey> upgrade;
    @SaveToDisk
    private final KeyInventory<AEItemKey> enhance;

    // 最大交易次数 32*
    private static final Item[] FIELD_GENERATOR = {
            FIELD_GENERATOR_LV.asItem(), FIELD_GENERATOR_MV.asItem(), FIELD_GENERATOR_HV.asItem(), FIELD_GENERATOR_EV.asItem(),
            FIELD_GENERATOR_IV.asItem(), FIELD_GENERATOR_LuV.asItem(), FIELD_GENERATOR_ZPM.asItem(), FIELD_GENERATOR_UV.asItem() };

    // 补货与交易参数
    @SaveToDisk(defaultValue = "2400")
    private int replenishmentInterval = 2400;
    @SaveToDisk(defaultValue = "1")
    private int tradingMultiple = 1;

    @SaveToDisk(defaultValue = "0")
    private int tire = 0;
    // 补货时间间隔 -225×，交易乘数 4×
    private static final Map<Item, Integer> ENHANCE_INDEX_MAP = Map.ofEntries(
            Map.entry(GTMachines.WORLD_ACCELERATOR[LV].asItem(), 1),
            Map.entry(GTMachines.WORLD_ACCELERATOR[MV].asItem(), 2),
            Map.entry(GTMachines.WORLD_ACCELERATOR[HV].asItem(), 3),
            Map.entry(GTMachines.WORLD_ACCELERATOR[EV].asItem(), 4),
            Map.entry(GTMachines.WORLD_ACCELERATOR[IV].asItem(), 5),
            Map.entry(GTMachines.WORLD_ACCELERATOR[LuV].asItem(), 6),
            Map.entry(GTMachines.WORLD_ACCELERATOR[ZPM].asItem(), 7),
            Map.entry(GTMachines.WORLD_ACCELERATOR[UV].asItem(), 8),
            Map.entry(INTEGRATED_CONTROL_CORE_UV.asItem(), 9),
            Map.entry(INTEGRATED_CONTROL_CORE_UHV.asItem(), 10),
            Map.entry(INTEGRATED_CONTROL_CORE_UEV.asItem(), 11),
            Map.entry(INTEGRATED_CONTROL_CORE_UIV.asItem(), 12));
    private static final Item[] ENHANCE_ITEMS = {
            Items.AIR,
            GTMachines.WORLD_ACCELERATOR[LV].asItem(), GTMachines.WORLD_ACCELERATOR[MV].asItem(), GTMachines.WORLD_ACCELERATOR[HV].asItem(), GTMachines.WORLD_ACCELERATOR[EV].asItem(),
            GTMachines.WORLD_ACCELERATOR[IV].asItem(), GTMachines.WORLD_ACCELERATOR[LuV].asItem(), GTMachines.WORLD_ACCELERATOR[ZPM].asItem(), GTMachines.WORLD_ACCELERATOR[UV].asItem(),
            INTEGRATED_CONTROL_CORE_UV.asItem(), INTEGRATED_CONTROL_CORE_UHV.asItem(), INTEGRATED_CONTROL_CORE_UEV.asItem(), INTEGRATED_CONTROL_CORE_UIV.asItem() };

    private static final Item VILLAGER_ITEM = RegistriesUtils.getItem("easy_villagers:villager");

    public VillageTradingStationMachine(MetaMachineBlockEntity holder) {
        super(holder);
        input = NotifiableInventory.items(this, 256, IO.IN, IO.IN);
        output = NotifiableInventory.items(this, 256, IO.OUT, IO.OUT);
        villagers = new VillageHolder(this);
        upgrade = KeyInventory.items(1);
        enhance = KeyInventory.items(1);
        enhance.setOnChanged(() -> {
            var key = enhance.keyAt(0);
            tire = ENHANCE_INDEX_MAP.getOrDefault(key == null ? Items.AIR : key.getItem(), 0);
            if (tire < 9) {
                replenishmentInterval = 2400 - 225 * tire;
                tradingMultiple = 1;
            } else {
                replenishmentInterval = 600;
                tradingMultiple = (tire - 8) * 4;
            }
        });
        outputFacingItems = hasFrontFacing() ? getFrontFacing().getOpposite() : Direction.DOWN;
    }

    /////////////////////////////////////
    // ********* 生命周期管理 ********* //
    /////////////////////////////////////

    @Override
    public void onLoad() {
        super.onLoad();
        input.notifyListeners();
        output.notifyListeners();
        if (!isRemote()) {
            tickSubs = subscribeServerTick(tickSubs, this::tickUpdate, 100);
            exportItemSubs = output.addChangedListener(this::updateAutoOutputSubscription);
        }
        for (int i = 0; i < 10; i++) {
            incomingVillagersDataset(i);
            selectedRecipes(i);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) tickSubs.unsubscribe();
        if (exportItemSubs != null) exportItemSubs.unsubscribe();
        if (autoOutputSubs != null) autoOutputSubs.unsubscribe();
    }

    /////////////////////////////////////
    // ********* 核心交易逻辑 ********* //
    /////////////////////////////////////

    private int replenishment;
    private int executeTrades;

    // 定时更新
    private void tickUpdate() {
        // 定时补货
        if (tickSubs.lastTick > replenishment) {
            villagersRestock();
            replenishment = tickSubs.lastTick + replenishmentInterval;
        }
        // 执行交易
        if (tickSubs.lastTick > executeTrades) {
            executeTrades = tickSubs.lastTick + 200;
            for (int slot = 0; slot < 9; slot++) {
                executeTrades(slot);
            }
        }
    }

    // 执行交易逻辑
    private void executeTrades(int slot) {
        if (!isLocked(slot) || !isStartUp(slot) || villagersDataset[slot] == null || villagersDataset[slot].length == 0) return;

        VillagerRecipe[] recipes = villagersDataset[slot];
        if (selected[slot] >= recipes.length) return;

        VillagerRecipe trade = recipes[selected[slot]];
        int remainingUses = trade.maxUses - trade.uses;
        if (remainingUses <= 0) return;

        int maxPossibleTrades = getMaxPossibleTrades(input.storage, trade.buyKey, trade.buy.getCount(), trade.buyBKey, trade.buyB.getCount());
        if (maxPossibleTrades <= 0) return;

        int actualTrades = Math.min((maxPossibleTrades / tradingMultiple), remainingUses) * tradingMultiple;
        if (actualTrades <= 0) return;

        deductItems(input.storage, trade.buyKey, trade.buy.getCount(), actualTrades);
        if (!trade.buyB.isEmpty()) {
            deductItems(input.storage, trade.buyBKey, trade.buyB.getCount(), actualTrades);
        }

        if (trade.sellKey != null) {
            output.storage.insert(trade.sellKey, (long) trade.sell.getCount() * actualTrades, false);
        }

        villagersDataset[slot][selected[slot]].uses += actualTrades / tradingMultiple;
        syncUsesAndMaxUsesToVillagerItem(slot);
    }

    // 计算两个输入物品堆能支持的最大交易次数
    private static int getMaxPossibleTrades(KeyInventory<AEItemKey> inventory, @Nullable AEItemKey buy, int buyCount, @Nullable AEItemKey buyB, int buyBCount) {
        long maxByBuy = Integer.MAX_VALUE;
        if (buyCount > 0) {
            maxByBuy = buy == null ? 0 : inventory.count(buy) / buyCount;
        }

        long maxByBuyB = Integer.MAX_VALUE;
        if (buyB != null && buyBCount > 0) {
            maxByBuyB = inventory.count(buyB) / buyBCount;
        }

        return (int) Math.min(Math.min(maxByBuy, maxByBuyB), 128);
    }

    // 从物品处理器中扣除指定数量的物品
    private static void deductItems(KeyInventory<AEItemKey> inventory, @Nullable AEItemKey target, int targetCount, int count) {
        if (target == null || targetCount <= 0 || count <= 0) return;
        inventory.extract(target, (long) targetCount * count, false);
    }

    /////////////////////////////////////
    // ********* 村民与配方管理 ********* //
    /////////////////////////////////////

    private boolean isLocked(int slot) {
        return isLocked[slot];
    }

    public void setLocked(int slot) {
        isLocked[slot] = !isLocked[slot];
        selected[slot] = 0;
        if (isLocked[slot]) {
            incomingVillagersDataset(slot);
        } else {
            syncUsesAndMaxUsesToVillagerItem(slot);
            villagersDataset[slot] = null;
            setStartUp(slot);
        }
        selectedRecipes(slot);
    }

    private void selectedNext(int slot) {
        if (!isLocked(slot) || isStartUp(slot)) return;
        VillagerRecipe[] recipes = villagersDataset[slot];
        if (recipes != null && recipes.length > 0) {
            selected[slot] = (selected[slot] + 1) % recipes.length;
            selectedRecipes(slot);
        } else {
            isLocked[slot] = false;
        }
    }

    private boolean isStartUp(int slot) {
        return startUp[slot];
    }

    private void setStartUp(int slot) {
        if (isLocked(slot)) {
            startUp[slot] = !startUp[slot];
            VillagerRecipe[] recipes = villagersDataset[slot];
            // 数组判断
            if (recipes == null || recipes.length == 0) {
                startUp[slot] = false;
            }
            if (startUp[slot]) executeTrades(slot);
        } else {
            startUp[slot] = false;
        }
    }

    // 加载村民的配方数据到数据集
    private void incomingVillagersDataset(int slot) {
        ItemStack villager = villagers.getStackInSlot(slot);
        if (!villager.isEmpty() && isLocked[slot]) {
            List<VillagerRecipe> recipeList = parseTradeData(villager.getOrCreateTag());
            CompoundTag villagerCoreNbt = villager.getOrCreateTag().getCompound("villager");
            CompoundTag offersTag = villagerCoreNbt.getCompound("Offers");
            ListTag nbtRecipes = offersTag.getList("Recipes", CompoundTag.TAG_COMPOUND);
            if (recipeList.size() != nbtRecipes.size()) {
                recipeList = recipeList.subList(0, Math.min(recipeList.size(), nbtRecipes.size()));
            }
            villagersDataset[slot] = recipeList.toArray(new VillagerRecipe[0]);
        } else {
            villagersDataset[slot] = null;
            isLocked[slot] = false;
        }
    }

    // 选中配方并更新UI显示
    private void selectedRecipes(int slot) {
        VillagerRecipe[] recipes = villagersDataset[slot];
        if (recipes != null && isLocked(slot) && recipes.length > 0 && selected[slot] < recipes.length) {
            VillagerRecipe recipe = recipes[selected[slot]];
            RecipesHandler.setStackInSlot(slot * 3, recipe.buy.copy());
            RecipesHandler.setStackInSlot(slot * 3 + 1, recipe.buyB.copy());
            RecipesHandler.setStackInSlot(slot * 3 + 2, recipe.sell.copy());
        } else {
            clearRecipeSlots(slot);
        }
    }

    private void clearRecipeSlots(int slot) {
        RecipesHandler.setStackInSlot(slot * 3, ItemStack.EMPTY);
        RecipesHandler.setStackInSlot(slot * 3 + 1, ItemStack.EMPTY);
        RecipesHandler.setStackInSlot(slot * 3 + 2, ItemStack.EMPTY);
    }

    /////////////////////////////////////
    // *********** UI实现 *********** //
    /////////////////////////////////////

    @RegisterLanguage(cn = "已锁定：村民不可取出，可选择交易并启动", en = "Locked: the villager cannot be removed; trades can be selected and started")
    private static final String LOCKED = "gtocore.machine.village_trading_station.ui.locked";
    @RegisterLanguage(cn = "未锁定：可放入或取出村民，锁定后读取其交易", en = "Unlocked: the villager can be inserted or removed; locking reads its trades")
    private static final String UNLOCKED = "gtocore.machine.village_trading_station.ui.unlocked";
    @RegisterLanguage(cn = "需要先放入村民", en = "Insert a villager first")
    private static final String NEED_VILLAGER = "gtocore.machine.village_trading_station.ui.need_villager";
    @RegisterLanguage(cn = "由当前选中的交易决定", en = "Determined by the selected trade")
    private static final String RECIPE_SLOT = "gtocore.machine.village_trading_station.ui.recipe_slot";
    @RegisterLanguage(cn = "切换到下一个交易", en = "Switch to the next trade")
    private static final String NEXT_TRADE = "gtocore.machine.village_trading_station.ui.next_trade";
    @RegisterLanguage(cn = "需要锁定村民且未启动交易", en = "Requires a locked villager with trading stopped")
    private static final String NEXT_UNAVAILABLE = "gtocore.machine.village_trading_station.ui.next_unavailable";
    @RegisterLanguage(cn = "交易已启动：每 10 秒使用输入物品自动交易", en = "Trading active: trades automatically with input items every 10 seconds")
    private static final String TRADING = "gtocore.machine.village_trading_station.ui.trading";
    @RegisterLanguage(cn = "交易未启动", en = "Trading stopped")
    private static final String NOT_TRADING = "gtocore.machine.village_trading_station.ui.not_trading";
    @RegisterLanguage(cn = "需要先锁定村民", en = "Lock the villager first")
    private static final String NEED_LOCK = "gtocore.machine.village_trading_station.ui.need_lock";
    @RegisterLanguage(cn = "已交易次数（补货时清零）", en = "Trades used (reset on restock)")
    private static final String USES = "gtocore.machine.village_trading_station.ui.uses";
    @RegisterLanguage(cn = "最大交易次数", en = "Maximum trades")
    private static final String MAX_USES = "gtocore.machine.village_trading_station.ui.max_uses";
    @RegisterLanguage(cn = "补货与交易强化", en = "Restock and Trade Enhancement")
    private static final String ENHANCE_SECTION = "gtocore.machine.village_trading_station.ui.enhance_section";
    @RegisterLanguage(cn = "交易次数上限", en = "Trade Limit")
    private static final String UPGRADE_SECTION = "gtocore.machine.village_trading_station.ui.upgrade_section";
    @RegisterLanguage(cn = "提升上限", en = "Raise Limit")
    private static final String UPGRADE = "gtocore.machine.village_trading_station.ui.upgrade";
    @RegisterLanguage(cn = "提升", en = "Raise")
    private static final String UPGRADE_BUTTON = "gtocore.machine.village_trading_station.ui.upgrade_button";
    @RegisterLanguage(cn = "消耗场发生器，提升左侧村民当前交易的最大交易次数，每个提升 1 次", en = "Consumes field generators to raise the maximum uses of the selected trade of the villager on the left, by 1 each")
    private static final String UPGRADE_TIP = "gtocore.machine.village_trading_station.ui.upgrade_tip";
    @RegisterLanguage(cn = "需要锁定村民并选择交易、未达上限且放入对应的场发生器", en = "Requires a locked villager with a selected trade below the limit, and the matching field generator")
    private static final String UPGRADE_UNAVAILABLE = "gtocore.machine.village_trading_station.ui.upgrade_unavailable";
    @RegisterLanguage(cn = "需要锁定村民并选择交易", en = "Lock the villager and select a trade")
    private static final String NO_TRADE = "gtocore.machine.village_trading_station.ui.no_trade";
    @RegisterLanguage(cn = "提升 %s 次后上限为 %s", en = "+%s, new limit %s")
    private static final String UPGRADE_PREVIEW = "gtocore.machine.village_trading_station.ui.upgrade_preview";

    private static final String ENHANCE = "gtocore.machine.village_trading_station.enhance";
    private static final String INCREASE = "gtocore.machine.village_trading_station.increase";
    private static final String UPPER_LIMIT = "gtocore.machine.village_trading_station.upper_limit";
    private static final String REPLENISHMENT_INTERVAL = "gtocore.machine.village_trading_station.replenishment_interval";
    private static final String TRADING_MULTIPLE = "gtocore.machine.village_trading_station.trading_multiple";

    private static final int UPGRADE_SLOT = 9;
    private static final int MAX_UPGRADED_USES = 256;
    private static final ItemStack[] FIELD_GENERATOR_STACKS = new ItemStack[FIELD_GENERATOR.length];

    static {
        for (int i = 0; i < FIELD_GENERATOR.length; i++) FIELD_GENERATOR_STACKS[i] = FIELD_GENERATOR[i].getDefaultInstance();
    }

    @Override
    public Widget createUIWidget() {
        return MachineDisplay.page(this, GTOMachineTooltips.VillageTradingStationIntroduction::apply);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);

        // 交易控制页
        sideTabs.attachSubTab(new IFancyUIProvider() {

            @Override
            public IGuiTexture getTabIcon() {
                return new ItemStackTexture(VILLAGER_ITEM);
            }

            @Override
            public Component getTitle() {
                return Component.translatable(getDefinition().getDescriptionId());
            }

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                var row = new UIElement().layout(l -> l.row());
                for (int i = 0; i < UPGRADE_SLOT; i++) row.addChild(villagerColumn(i));
                return MachineDisplay.column().addChild(row);
            }
        });

        // 升级控制页
        sideTabs.attachSubTab(new IFancyUIProvider() {

            @Override
            public IGuiTexture getTabIcon() {
                return new ItemStackTexture(VILLAGER_ITEM);
            }

            @Override
            public Component getTitle() {
                return Component.empty();
            }

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                var right = UIElement.column(LayoutStyle.AUTO).layout(l -> l.flex(1).gapAll(UISizes.SECTION_GAP))
                        .addChildren(createEnhanceSection(), createUpgradeSection());
                var row = new UIElement().layout(l -> l.row().gapAll(UISizes.SECTION_GAP)).addChildren(villagerColumn(UPGRADE_SLOT), right);
                return MachineDisplay.column().addChild(row);
            }
        });

        SideOverviewTab.attach(sideTabs, this);
    }

    private UIElement createEnhanceSection() {
        var controls = ControlPanel.of(this);
        controls.addSlot(ItemSlot.of(enhance, 0), ENHANCE_SECTION, MultiblockPage.cached(() -> tire, VillageTradingStationMachine::enhanceText));
        controls.add(sectionLine(() -> replenishmentInterval, value -> Component.translatable(REPLENISHMENT_INTERVAL, value)));
        controls.add(sectionLine(() -> tradingMultiple, value -> Component.translatable(TRADING_MULTIPLE, value)));
        return controls.build();
    }

    private UIElement createUpgradeSection() {
        var controls = ControlPanel.of(this);
        controls.addSlot(ItemSlot.of(upgrade, 0), UPGRADE_SECTION, MultiblockPage.cached(this::upgradeState, VillageTradingStationMachine::upgradeHintText));
        controls.add(sectionLine(this::upgradeState, VillageTradingStationMachine::upgradePreviewText));
        controls.addServerButton(UPGRADE, UPGRADE_BUTTON, this::upgradeMaxUses, UPGRADE_TIP).disabled(() -> !canUpgrade(), UPGRADE_UNAVAILABLE);
        return controls.build();
    }

    private static TextLine sectionLine(LongSupplier value, LongFunction<Component> format) {
        return TextLine.of(LayoutStyle.AUTO, MultiblockPage.cached(value, format)).bindClientColor(UITheme::panelText);
    }

    private UIElement villagerColumn(int slot) {
        var column = UIElement.column(UISizes.SLOT_SIZE).layout(l -> l.gapAll(UISizes.GAP).alignCenter());

        column.addChild(IconToggle.of(WidgetIcons.ACCESS_PRIVATE, () -> isLocked(slot), locked -> {
            if (locked == isLocked(slot)) return;
            setLocked(slot);
            onChanged();
        }).onOffTooltips(LOCKED, UNLOCKED).disabled(() -> !isLocked(slot) && villagers.getStackInSlot(slot).isEmpty(), NEED_VILLAGER));

        column.addChild(ItemSlot.of(villagers, slot));

        for (int j = 0; j < 3; j++) {
            column.addChild(ItemSlot.display(RecipesHandler, 3 * slot + j, RECIPE_SLOT)
                    .setBackgroundTexture(GTOGuiTextures.VILLAGER_RECIPE_SLOTS[j]));
        }

        var next = Button.glyph(">")
                .setOnServerClick(() -> {
                    selectedNext(slot);
                    onChanged();
                })
                .disabled(() -> !isLocked(slot) || isStartUp(slot), NEXT_UNAVAILABLE);
        next.tooltips(NEXT_TRADE);
        column.addChild(next);

        column.addChild(IconToggle.of(WidgetIcons.POWER_ON, () -> isStartUp(slot), on -> {
            if (on == isStartUp(slot)) return;
            setStartUp(slot);
            onChanged();
        }).onOffTooltips(TRADING, NOT_TRADING).disabled(() -> !isLocked(slot), NEED_LOCK));

        var uses = TextLine.of(UISizes.SLOT_SIZE, MultiblockPage.cached(() -> recipeUses(slot), VillageTradingStationMachine::countText)).setTextAlign(Horizontal.CENTER);
        uses.setHoverTooltips(USES);
        var maxUses = TextLine.of(UISizes.SLOT_SIZE, MultiblockPage.cached(() -> recipeMaxUses(slot), VillageTradingStationMachine::countText)).setTextAlign(Horizontal.CENTER);
        maxUses.setHoverTooltips(MAX_USES);
        return column.addChildren(uses, maxUses);
    }

    @Nullable
    private VillagerRecipe currentRecipe(int slot) {
        VillagerRecipe[] recipes = villagersDataset[slot];
        if (recipes != null && isLocked(slot) && recipes.length > 0 && selected[slot] < recipes.length) return recipes[selected[slot]];
        return null;
    }

    private long recipeUses(int slot) {
        VillagerRecipe recipe = currentRecipe(slot);
        return recipe == null ? 0 : recipe.uses;
    }

    private long recipeMaxUses(int slot) {
        VillagerRecipe recipe = currentRecipe(slot);
        return recipe == null ? 0 : recipe.maxUses;
    }

    private static Component countText(long value) {
        return Component.literal(String.valueOf(value));
    }

    private static Component enhanceText(long tier) {
        if (tier < ENHANCE_ITEMS.length - 1) return Component.translatable(ENHANCE, ENHANCE_ITEMS[(int) tier + 1].getDefaultInstance().getDisplayName());
        return Component.translatable(UPPER_LIMIT);
    }

    private long upgradeState() {
        VillagerRecipe recipe = currentRecipe(UPGRADE_SLOT);
        if (recipe == null) return -1;
        if (recipe.maxUses >= MAX_UPGRADED_USES) return -2;
        int tier = recipe.maxUses / 32;
        int count = Math.min(getMaxPossibleTrades(upgrade, fieldGeneratorKey(tier), 1, null, 0), (tier + 1) * 32 - recipe.maxUses);
        return (long) tier << 48 | (long) count << 24 | (recipe.maxUses + count);
    }

    private boolean canUpgrade() {
        long state = upgradeState();
        return state >= 0 && (state >>> 24 & 0xFFFFFF) > 0;
    }

    private static Component upgradeHintText(long state) {
        if (state == -1) return Component.translatable(NO_TRADE);
        if (state == -2) return Component.translatable(UPPER_LIMIT);
        return Component.translatable(INCREASE, FIELD_GENERATOR_STACKS[(int) (state >>> 48)].getDisplayName());
    }

    private static Component upgradePreviewText(long state) {
        if (state < 0) return Component.empty();
        return Component.translatable(UPGRADE_PREVIEW, state >>> 24 & 0xFFFFFF, state & 0xFFFFFF);
    }

    private void upgradeMaxUses() {
        VillagerRecipe[] recipes = villagersDataset[UPGRADE_SLOT];
        if (recipes != null && isLocked(UPGRADE_SLOT) && recipes.length > 0 && selected[UPGRADE_SLOT] < recipes.length) {
            int upGread = recipes[selected[UPGRADE_SLOT]].maxUses / 32;
            if (recipes[selected[UPGRADE_SLOT]].maxUses < MAX_UPGRADED_USES) {
                AEItemKey item = fieldGeneratorKey(upGread);
                int count = Math.min(getMaxPossibleTrades(upgrade, item, 1, null, 0), (upGread + 1) * 32 - recipes[selected[UPGRADE_SLOT]].maxUses);
                if (count > 0) {
                    deductItems(upgrade, item, 1, count);
                    villagersDataset[UPGRADE_SLOT][selected[UPGRADE_SLOT]].maxUses += count;
                    syncUsesAndMaxUsesToVillagerItem(UPGRADE_SLOT);
                }
            }
        }
    }

    /////////////////////////////////////
    // ********* 辅助类与方法 ********* //
    /////////////////////////////////////

    private static final AEItemKey[] FIELD_GENERATOR_KEYS = new AEItemKey[FIELD_GENERATOR.length];

    private static AEItemKey fieldGeneratorKey(int tier) {
        var key = FIELD_GENERATOR_KEYS[tier];
        if (key == null) {
            key = AEItemKey.of(FIELD_GENERATOR[tier]);
            FIELD_GENERATOR_KEYS[tier] = key;
        }
        return key;
    }

    private static class VillageHolder extends StackInventory {

        private final VillageTradingStationMachine machine;

        private VillageHolder(VillageTradingStationMachine machine) {
            super(10);
            this.machine = machine;
            this.setFilter(i -> i.getItem().equals(VILLAGER_ITEM));
        }

        @Override
        public int extract(int slot, ItemStack existing, int amount, boolean simulate) {
            if (machine.isLocked(slot)) return 0;
            return super.extract(slot, existing, amount, simulate);
        }

        @Override
        public int insert(int slot, @NotNull ItemStack stack, int amount, boolean simulate) {
            if (machine.isLocked(slot)) return 0;
            return super.insert(slot, stack, amount, simulate);
        }

        @Override
        public long insert(int slot, AEItemKey key, long amount, boolean simulate) {
            if (machine.isLocked(slot)) return 0;
            return super.insert(slot, key, amount, simulate);
        }
    }

    // 村民交易配方类
    private static class VillagerRecipe {

        private final ItemStack buy;
        private final ItemStack buyB;
        private final ItemStack sell;
        @Nullable
        private final AEItemKey buyKey;
        @Nullable
        private final AEItemKey buyBKey;
        @Nullable
        private final AEItemKey sellKey;
        private int maxUses;
        private int uses;

        private VillagerRecipe(ItemStack buy, ItemStack buyB, ItemStack sell, int maxUses, int uses) {
            this.buy = buy;
            this.buyB = buyB;
            this.sell = sell;
            this.buyKey = Keys.item(buy);
            this.buyBKey = Keys.item(buyB);
            this.sellKey = Keys.item(sell);
            this.maxUses = maxUses;
            this.uses = uses;
        }
    }

    // 解析村民NBT数据获取交易配方（仅返回配方列表）
    private List<VillagerRecipe> parseTradeData(CompoundTag originalOuterNbt) {
        List<VillagerRecipe> recipes = new ArrayList<>();
        if (!originalOuterNbt.contains("villager", CompoundTag.TAG_COMPOUND)) {
            return recipes;
        }
        CompoundTag villagerCoreNbt = originalOuterNbt.getCompound("villager");

        if (!villagerCoreNbt.contains("Offers", CompoundTag.TAG_COMPOUND)) {
            return recipes;
        }
        CompoundTag offersTag = villagerCoreNbt.getCompound("Offers");

        if (!offersTag.contains("Recipes", ListTag.TAG_LIST)) {
            return recipes;
        }
        ListTag recipesList = offersTag.getList("Recipes", CompoundTag.TAG_COMPOUND);

        for (int i = 0; i < recipesList.size(); i++) {
            CompoundTag recipeTag = recipesList.getCompound(i);
            ItemStack buy = parseItemStack(recipeTag.getCompound("buy"));
            ItemStack buyB = parseItemStack(recipeTag.getCompound("buyB"));
            ItemStack sell = parseItemStack(recipeTag.getCompound("sell"));
            int maxUses = recipeTag.getInt("maxUses");
            int uses = recipeTag.getInt("uses");
            recipes.add(new VillagerRecipe(buy, buyB, sell, maxUses, uses));
        }
        return recipes;
    }

    // 解析NBT为物品栈
    private static ItemStack parseItemStack(CompoundTag itemTag) {
        String itemId = itemTag.getString("id");
        ItemStack item = RegistriesUtils.getItemStack(itemId);
        if (item.getItem().equals(Items.AIR)) return ItemStack.EMPTY;
        int count = Math.max(1, itemTag.getByte("Count"));
        item.setCount(count);
        return item;
    }

    // 村民补货：直接重置所有配方的使用次数（无次数限制）
    private void villagersRestock() {
        for (int slot = 0; slot < 10; slot++) {
            VillagerRecipe[] recipes = villagersDataset[slot];
            if (recipes == null) continue;
            for (VillagerRecipe recipe : recipes) {
                if (recipe != null) recipe.uses = 0;
            }
        }
    }

    // 同步内存中的uses和maxUses到村民物品的NBT数据
    private void syncUsesAndMaxUsesToVillagerItem(int slot) {
        VillagerRecipe[] datasetRecipes = villagersDataset[slot];
        ItemStack villagerStack = villagers.getStackInSlot(slot);
        if (villagerStack.isEmpty() || !villagerStack.getItem().equals(VILLAGER_ITEM) || datasetRecipes == null || datasetRecipes.length == 0) {
            return;
        }
        CompoundTag outerNbt = getCompoundTag(villagerStack, datasetRecipes);
        villagerStack.setTag(outerNbt);
        boolean wasLocked = isLocked[slot];
        if (wasLocked) isLocked[slot] = false;
        villagers.setStackInSlot(slot, villagerStack);
        if (wasLocked) isLocked[slot] = true;
    }

    private static CompoundTag getCompoundTag(ItemStack villagerStack, VillagerRecipe[] datasetRecipes) {
        CompoundTag outerNbt = villagerStack.getOrCreateTag();
        CompoundTag villagerCoreNbt = outerNbt.getCompound("villager");
        CompoundTag offersTag = villagerCoreNbt.getCompound("Offers");
        ListTag recipesList = offersTag.getList("Recipes", CompoundTag.TAG_COMPOUND);
        int syncCount = Math.min(datasetRecipes.length, recipesList.size());
        for (int i = 0; i < syncCount; i++) {
            VillagerRecipe datasetRecipe = datasetRecipes[i];
            CompoundTag nbtRecipe = recipesList.getCompound(i);
            nbtRecipe.putInt("uses", datasetRecipe.uses);
            nbtRecipe.putInt("maxUses", datasetRecipe.maxUses);
            recipesList.set(i, nbtRecipe);
        }
        offersTag.put("Recipes", recipesList);
        villagerCoreNbt.put("Offers", offersTag);
        outerNbt.put("villager", villagerCoreNbt);
        return outerNbt;
    }

    /////////////////////////////////////
    // ********* 自动输出实现 ********* //
    /////////////////////////////////////

    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private Direction outputFacingItems;
    @SaveToDisk(defaultValue = "false")
    @SyncToClient(scheduleUpdate = true)
    private boolean autoOutputItems;
    @SaveToDisk(defaultValue = "false")
    private boolean allowInputFromOutputSideItems;

    @Override
    @Nullable
    public Direction getOutputFacingItems() {
        if (hasAutoOutputItem()) {
            return outputFacingItems;
        }
        return null;
    }

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {
        if (hasAutoOutputItem()) {
            clearDirectionCache();
            outputFacingItems = outputFacing;
            updateAutoOutputSubscription();
        }
    }

    @Override
    public void setAutoOutputItems(boolean allow) {
        if (hasAutoOutputItem()) {
            autoOutputItems = allow;
            updateAutoOutputSubscription();
        }
    }

    @Override
    public boolean isAutoOutputItems() {
        return this.autoOutputItems;
    }

    @Override
    public void setAllowInputFromOutputSideItems(final boolean allowInputFromOutputSideItems) {
        clearDirectionCache();
        this.allowInputFromOutputSideItems = allowInputFromOutputSideItems;
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return this.allowInputFromOutputSideItems;
    }

    private void updateAutoOutputSubscription() {
        if (getLevel() == null) return;
        Direction outputFacing = getOutputFacingItems();
        if (autoOutputItems && !output.isEmpty() && outputFacing != null && holder.blockEntityDirectionCache.hasAdjacentItemHandler(getLevel(), getPos(), outputFacing)) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, autoOutputMonitor, 20);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    private void autoOutput() {
        if (autoOutputItems && getOutputFacingItems() != null) {
            output.exportToNearby(getOutputFacingItems());
        }
        updateAutoOutputSubscription();
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(input.storage);
        clearInventory(output.storage);
        clearInventory(villagers);
        clearInventory(upgrade);
        clearInventory(enhance);
    }
}

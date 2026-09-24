package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.machine.multiblock.part.ae.widget.slot.MEPatternViewSlotWidget;
import com.gtocore.eio_travel.logic.TravelSavedData;
import com.gtocore.eio_travel.logic.TravelUtils;
import com.gtocore.integration.ae.PatternContainerGroupHelper;
import com.gtocore.integration.ae.hooks.IExtendedPatternContainer;
import com.gtocore.integration.ae.wireless.WirelessMachine;

import com.gtolib.api.ae2.MyPatternDetailsHelper;
import com.gtolib.api.ae2.pattern.IParallelPatternDetails;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.datasynclib.GTDataFixer;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gregtechceu.gtceu.utils.asm.EmptyMethodChecker;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.gto.datasynclib.AbstractDataSerializable;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.listener.IntNotifiableHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 装 AE 样板的 ME 部件基类：把样板提供给 AE 网络（{@link ICraftingProvider}），每个样板槽对应一个内部槽 {@code T}。
 * <p>
 * 存档键：{@link SaveToDisk} 字段都显式写明 {@code key}，取值与改写成 Java 前的 Kotlin 属性名一致，旧存档原样读取。
 * 拆下后放置用的物品 NBT 见 {@link #saveToItem}/{@link #loadFromItem}，兼容所有旧格式。
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@DataGeneratorScanned
public abstract class MEPatternPartMachine<T extends MEPatternPartMachine.AbstractInternalSlot> extends MEPartMachine
                                          implements ICraftingProvider, WirelessMachine, IInteractedMachine, IExtendedPatternContainer, IDropSaveMachine {

    private static final Logger LOGGER = LogUtils.getLogger();

    @RegisterLanguage(cn = "AE显示名称:", en = "AE Name:")
    public static final String AE_NAME = "gtceu.ae.pattern_part_machine.ae_name";
    @RegisterLanguage(cn = "仅在简单游戏难度下启用", en = "Enable only in Easy Game Mode")
    public static final String NOT_SIMPLE = "gtceu.ae.pattern_part_machine.not_simple";
    @RegisterLanguage(cn = "不在旅行网络中显示", en = "Do not show in Travel Network")
    public static final String NOT_SHOW_IN_TRAVEL = "gtceu.ae.pattern_part_machine.not_show_in_travel";
    @RegisterLanguage(cn = "在旅行网络中显示", en = "Show in Travel Network")
    public static final String SHOW_IN_TRAVEL = "gtceu.ae.pattern_part_machine.show_in_travel";
    @RegisterLanguage(cn = "样板 #%s 独立配置", en = "Pattern #%s Settings")
    public static final String SLOT_CONFIG_TITLE = "gtocore.pattern_part.slot_config.title";
    @RegisterLanguage(cn = "中键点击：打开此样板的独立配置", en = "Middle-click: open this pattern's settings")
    public static final String SLOT_CONFIG_HINT = "gtocore.pattern_part.slot_config.hint";
    @RegisterLanguage(cn = "点击箭头或滚动滚轮切换电路编号", en = "Click the arrows or scroll to change the circuit number")
    public static final String CIRCUIT_HINT = "gtocore.pattern_part.circuit_hint";
    @RegisterLanguage(cn = "-1 表示不放电路；0~32 为对应编号的编程电路", en = "-1 means no circuit; 0-32 select the programmed circuit with that number")
    public static final String CIRCUIT_NONE = "gtocore.pattern_part.circuit_none";
    @RegisterLanguage(cn = "电路由右侧的编号设置，不能直接放取", en = "The circuit is set by the number on the right; it cannot be placed or taken directly")
    public static final String CIRCUIT_READ_ONLY = "gtocore.pattern_part.circuit_read_only";
    @RegisterLanguage(cn = "重置缓存", en = "Recache")
    public static final String CLEAR_MACHINE_RECIPE_CACHE = "gtceu.ae.pattern_part_machine.clear_machine_recipe_cache";
    @RegisterLanguage(cn = "重置机器的所有配方缓存，不会改变样板的任何数据内容", en = "Clear all recipe cache of the machine, will not change any data content in pattern")
    public static final String CLEAR_MACHINE_RECIPE_CACHE_TOOLTIP = "gtceu.ae.pattern_part_machine.clear_machine_recipe_cache_tooltip";
    @RegisterLanguage(cn = "清除配方", en = "Clear")
    public static final String CLEAR_PATTERN_RECIPE_CACHE = "gtceu.ae.pattern_part_machine.clear_pattern_recipe_cache";
    @RegisterLanguage(cn = "重置样板内的配方缓存，会清除样板内的编写的配方（不会改变原料与产物内容）", en = "Clear recipe cache in pattern, will clear the recipe written in pattern (will not change input and output)")
    public static final String CLEAR_PATTERN_RECIPE_CACHE_TOOLTIP = "gtceu.ae.pattern_part_machine.clear_pattern_recipe_cache_tooltip";

    /// 单槽配置弹出面板的键，参数为样板槽号
    public static final String SLOT_CONFIG_POPUP = "pattern_slot_config";

    // 拆下后物品 NBT 的键：p 样板、n 显示名、i 内部槽（每项一个 ByteArrayTag，LDLib 时代为 CompoundTag）、
    // dv 写入时的 DataSyncLib 数据版本（旧物品没有，按当前版本读取）
    private static final String ITEM_PATTERNS = "p";
    private static final String ITEM_NAME = "n";
    private static final String ITEM_SLOTS = "i";
    private static final String ITEM_DATA_VERSION = "dv";

    // ==================== 持久化 ====================

    @SaveToDisk(key = "patternInventory")
    @SyncToClient
    private final CustomItemStackHandler patternInventory;

    @SaveToDisk(key = "internalInventory")
    private final AbstractInternalSlot[] internalInventory;

    @SyncToClient
    @SaveToDisk(key = "customName", defaultValue = "")
    private String customName = "";

    /// 默认值随机型变化（见 {@link #defaultShowInTravel()}），等于默认值时不写盘
    @SaveToDisk(key = "showInTravelNetwork", defaultValueGetter = "defaultShowInTravel")
    private boolean showInTravelNetwork;

    // ==================== 运行时 ====================

    private final int maxPatternCount;
    private final BiMap<IPatternDetails, T> detailsSlotMap;
    private final PatternInventory internalPatternInventory = new PatternInventory(this);
    private boolean detailsInit;
    private List<IPatternDetails> patterns = Collections.emptyList();
    private boolean needPatternSync;
    @Nullable
    private TickableSubscription updateSubs;
    @Nullable
    private Boolean hasClearButtons;

    /// 当前页码；翻页按钮在服务端改它，经机器同步驱动两端的 {@link com.gregtechceu.gtceu.uipro.elements.PageView} 重建
    @SyncToClient
    private final IntNotifiableHolder newPageField = IntNotifiableHolder.create();

    protected MEPatternPartMachine(MetaMachineBlockEntity holder, int maxPatternCount) {
        super(holder, IO.IN);
        if (maxPatternCount > 500) throw new IllegalArgumentException("maxPatternCount " + maxPatternCount + " > 500");
        this.maxPatternCount = maxPatternCount;
        this.detailsSlotMap = HashBiMap.create(maxPatternCount);
        this.patternInventory = new CustomItemStackHandler(maxPatternCount);
        this.patternInventory.setFilter(this::patternFilter);
        this.internalInventory = createInternalSlotArray();
        for (int i = 0; i < internalInventory.length; i++) {
            internalInventory[i] = createInternalSlot(i);
        }
        this.showInTravelNetwork = defaultShowInTravel();
        getMainNode().addService(ICraftingProvider.class, this);
    }

    // ==================== 子类实现 ====================

    public abstract AbstractInternalSlot[] createInternalSlotArray();

    public abstract boolean patternFilter(ItemStack stack);

    public abstract T createInternalSlot(int i);

    // ==================== 扩展钩子 ====================

    /** 样板槽悬浮提示的附加行。 */
    @Nullable
    public Component appendHoverTooltips(int index) {
        return null;
    }

    /** 联网后样板全部解码完成时回调。 */
    public void onDetailsPostInit() {}

    public boolean defaultShowInTravel() {
        return true;
    }

    public void clearPatternRecipeCache() {}

    public void clearMachineRecipeCache() {}

    // ==================== 样板 ====================

    @SuppressWarnings("unchecked")
    public T[] getInternalInventory() {
        return (T[]) internalInventory;
    }

    public void onPatternChange(int index) {
        if (isRemote()) return;
        onChanged();
        var internalSlot = getInternalInventory()[index];
        var newPatternDetails = decodePattern(patternInventory.getStackInSlot(index), index);
        var oldPatternDetails = detailsSlotMap.inverse().get(internalSlot);
        detailsSlotMap.forcePut(newPatternDetails, internalSlot);
        // 旧版写成 Kotlin 时这里的"是否变化"判断实际没有生效，一律通知内部槽；保持该行为
        internalSlot.onPatternChange();
        updatePatterns();
    }

    public IPatternDetails convertPattern(IPatternDetails pattern, int index) {
        return pattern;
    }

    @Nullable
    public IPatternDetails decodePattern(ItemStack stack, int index) {
        var pattern = MyPatternDetailsHelper.decodePattern(stack, holder, getGrid());
        if (pattern == null) return null;
        return IParallelPatternDetails.of(convertPattern(pattern, index), getLevel(), 1);
    }

    private void updatePatterns() {
        var list = new ArrayList<IPatternDetails>(detailsSlotMap.size());
        for (var details : detailsSlotMap.keySet()) {
            if (details != null) list.add(details);
        }
        patterns = list;
        needPatternSync = true;
        if (getMainNode().isOnline()) {
            updateSubs = subscribeServerTick(updateSubs, this::update);
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    private void update() {
        if (needPatternSync) {
            if (isOnline()) {
                ICraftingProvider.requestUpdate(getMainNode());
                needPatternSync = false;
            }
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    /** 放入样板前检查：同一台机器里不允许两张主产物相同的样板。 */
    public static boolean checkDuplicatedPattern(MEPatternPartMachine<?> machine, ItemStack stack) {
        var patternDetails = PatternDetailsHelper.decodePattern(stack, machine.getLevel());
        if (patternDetails == null) return false;
        var level = machine.getLevel();
        if (level != null && level.isClientSide) return true;
        if (machine.detailsSlotMap.isEmpty()) return true;
        var primaryOutput = patternDetails.getPrimaryOutput().what();
        for (var details : machine.patterns) {
            if (details.getPrimaryOutput().what().equals(primaryOutput)) return false;
        }
        return true;
    }

    // ==================== 生命周期 ====================

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!isRemote()) {
            // 重新下发当前页码，保证刚打开的界面落在服务端记录的那一页
            newPageField.set(newPageField.get());
            newPageField.markAsChanged();
            syncToClient();
        }
        return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        detailsInit = false;
        var level = getLevel();
        if (level != null) TravelUtils.removeAndReadd(level, this);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        detailsInit = false;
        var level = getLevel();
        if (level != null) TravelSavedData.getTravelData(level).removeTravelTargetAt(level, holder.getBlockPos());
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        TravelUtils.requireResync(Objects.requireNonNull(getLevel()));
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (!isOnline()) {
            detailsInit = false;
            return;
        }
        if (!detailsInit && getLevel() instanceof ServerLevel level) {
            TaskHandler.enqueueTask(level, () -> {
                var slots = getInternalInventory();
                for (int i = 0; i < patternInventory.getSlots(); i++) {
                    var patternDetails = decodePattern(patternInventory.getStackInSlot(i), i);
                    if (patternDetails != null) detailsSlotMap.forcePut(patternDetails, slots[i]);
                }
                updatePatterns();
                onDetailsPostInit();
                detailsInit = true;
            }, 10);
        }
    }

    // ==================== ICraftingProvider ====================

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return patterns;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!getMainNode().isActive()) return false;
        var slot = detailsSlotMap.get(patternDetails);
        return slot != null && slot.pushPattern(patternDetails, inputHolder);
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    // ==================== PatternContainer ====================

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return internalPatternInventory;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        if (isFormed()) return formedGroup(customName);
        var itemKey = AEItemKey.of(GTAEMachines.ME_PATTERN_BUFFER.asItem());
        var description = customName.isEmpty() ? GTAEMachines.ME_PATTERN_BUFFER.get().getDefinition().asItem().getDescription() :
                Component.literal(customName);
        return new PatternContainerGroup(itemKey, description, Collections.emptyList());
    }

    @Override
    public @Nullable AEKey gto$getProviderIcon() {
        return AEItemKey.of(getDefinition().asStack());
    }

    @Override
    public @Nullable Component gto$getPlainCustomName() {
        return PatternContainerGroupHelper.isPlainCustomName(customName) ? Component.literal(customName) : null;
    }

    /** 发送样板面板：忽略普通改名时的分组，名字回到所属多方块机器。 */
    @Override
    public PatternContainerGroup gto$getMachineGroup() {
        if (gto$getPlainCustomName() == null) return getTerminalGroup();
        if (isFormed()) return formedGroup("");
        return new PatternContainerGroup(AEItemKey.of(getDefinition().asStack()), getDefinition().asItem().getDescription(), Collections.emptyList());
    }

    /** 已成形时的分组：按所属多方块机器命名，{@code name} 为自定义名（普通改名或 "+" 后缀），空串表示不改名。 */
    private PatternContainerGroup formedGroup(String name) {
        var controller = getController();
        List<GTRecipeType> availableRecipeTypes = controller instanceof IRecipeLogicMachine recipeMachine ?
                List.of(recipeMachine.getAvailableRecipeTypes()) : Collections.emptyList();
        return PatternContainerGroupHelper.forPatternBuffer(controller.self(), this, name, groupRecipeType(), availableRecipeTypes);
    }

    /** 分组名中用于显示的已选配方类型；样板总成按自身设置覆写。 */
    @Nullable
    protected GTRecipeType groupRecipeType() {
        return null;
    }

    @Override
    public Component gto$getTerminalGroupSearchName() {
        if (!isFormed()) return getTerminalGroup().name();
        if (!customName.isEmpty() && !customName.startsWith("+")) return Component.literal(customName);
        var controller = getController();
        List<GTRecipeType> availableRecipeTypes = controller instanceof IRecipeLogicMachine recipeMachine ?
                List.of(recipeMachine.getAvailableRecipeTypes()) : Collections.emptyList();
        var extraSuffix = customName.startsWith("+") ? customName.substring(1).strip() : "";
        return PatternContainerGroupHelper.getSearchName(controller.self(), extraSuffix, null, availableRecipeTypes);
    }

    // ==================== 其他接口 ====================

    @Override
    public @Nullable IGrid getGrid() {
        return getMainNode().getGrid();
    }

    @Override
    public List<RecipeHandlerUnit> getRecipeHandlers() {
        return Collections.emptyList();
    }

    @Override
    public RecipeHandlerUnit getHandlerUnit() {
        return RecipeHandlerUnit.NO_DATA;
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean ignored) {}

    @Override
    public boolean isDistinct() {
        return true;
    }

    @Override
    public void setDistinct(boolean isDistinct) {}

    @Override
    public boolean savePickClone() {
        return false;
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        var toggle = new IFancyConfiguratorButton.Toggle(
                GTOGuiTextures.TRAVEL_OFF,
                GTOGuiTextures.TRAVEL_ON,
                () -> showInTravelNetwork,
                (clickData, show) -> {
                    showInTravelNetwork = show;
                    TravelUtils.requireResync(Objects.requireNonNull(getLevel()));
                })
                .setTooltipsSupplier(show -> List.of(Component.translatable(show ? SHOW_IN_TRAVEL : NOT_SHOW_IN_TRAVEL)));
        configuratorPanel.attachConfigurators(toggle);
    }

    // ==================== 界面 ====================

    /** 使用新式机器外壳（{@link MachineWindow}）。 */
    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(UISizes.WINDOW_WIDTH, UISizes.WINDOW_WIDTH, this, entityPlayer).widget(new MachineWindow(this));
    }

    /**
     * 放进 {@link MachineWindow} 时，网络状态和 AE 名称放在窗口标题栏里，省下一行高度；
     * 其他外壳里仍放在页面第一行。
     */
    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        if (widget instanceof MachineWindow window) {
            window.setTitleContent(width -> MEPatternPartUI.header(this, width));
            registerPopups(window);
            return createUIWidget();
        }
        return UIElement.column(UISizes.CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP))
                .addChildren(MEPatternPartUI.header(this, UISizes.CONTENT_WIDTH), createUIWidget());
    }

    /**
     * 注册本机器用到的弹出面板，由子类决定有几种（默认只有支持单槽配置时的一种）。
     * 覆写时先调用 super 保留父类的面板。
     */
    protected void registerPopups(MachineWindow window) {
        if (supportsSlotConfig()) window.registerPopup(SLOT_CONFIG_POPUP, this::slotConfigPopup);
    }

    @Nullable
    private Popup slotConfigPopup(int index) {
        if (index < 0 || index >= maxPatternCount) return null;
        return Popup.of(() -> Component.translatable(SLOT_CONFIG_TITLE, index + 1), column -> buildSlotConfig(column, index));
    }

    @Override
    public Widget createUIWidget() {
        var root = UIElement.column(UISizes.CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));
        buildUI(root);
        return root;
    }

    /** 页面主体：样板网格 / 底栏（见 {@link MEPatternPartUI}），宽 {@link UISizes#CONTENT_WIDTH}，高度随内容。 */
    protected void buildUI(UIElement root) {
        int width = root.getContentWidth();
        var pages = MEPatternPartUI.patternPages(this, width, this::buildGridHeader, gridHeaderHeight(), () -> Component.translatable(NOT_SIMPLE));
        root.addChild(pages);
        pages.refresh();

        Widget[] actions = hasClearButtons() ? new Widget[] {
                Button.translatable(UISizes.BUTTON_WIDTH, CLEAR_MACHINE_RECIPE_CACHE).setOnServerClick(this::clearMachineRecipeCache)
                        .setHoverTooltips(CLEAR_MACHINE_RECIPE_CACHE_TOOLTIP),
                Button.translatable(UISizes.BUTTON_WIDTH, CLEAR_PATTERN_RECIPE_CACHE).setOnServerClick(this::clearPatternRecipeCache)
                        .setVariant(UITheme.ButtonVariant.DANGER).setHoverTooltips(CLEAR_PATTERN_RECIPE_CACHE_TOOLTIP)
        } : new Widget[0];
        var footer = MEPatternPartUI.footer(this, width, actions);
        if (footer != null) root.addChild(footer);
    }

    /** 是否支持中键打开单槽配置弹出面板；支持时样板槽提示里会附上操作说明。 */
    protected boolean supportsSlotConfig() {
        return false;
    }

    /** 单槽配置弹出面板的内容：用 {@link MEPatternPartUI#section} 往 {@code column} 里加面板区块。 */
    protected void buildSlotConfig(UIElement column, int index) {}

    /** 样板网格每页上方的附加内容（可选），高度由 {@link #gridHeaderHeight()} 声明（含与网格的间距）。 */
    protected void buildGridHeader(UIElement page) {}

    protected int gridHeaderHeight() {
        return 0;
    }

    void selectPage(int page, int pageCount) {
        newPageField.set(Math.max(0, Math.min(page, pageCount - 1)));
        newPageField.markAsChanged();
        syncToClient();
    }

    /** 子类实现了两个清缓存方法时才显示清缓存按钮。 */
    private boolean hasClearButtons() {
        if (hasClearButtons == null) {
            try {
                hasClearButtons = EmptyMethodChecker.hasMethodBody(getClass().getMethod("clearMachineRecipeCache")) &&
                        EmptyMethodChecker.hasMethodBody(getClass().getMethod("clearPatternRecipeCache"));
            } catch (NoSuchMethodException e) {
                hasClearButtons = false;
            }
        }
        return hasClearButtons;
    }

    public MEPatternViewSlotWidget createPatternSlotWidget(int index) {
        return new MEPatternViewSlotWidget(index, patternInventory, supportsSlotConfig() ? SLOT_CONFIG_POPUP : null);
    }

    public MEPatternViewSlotWidget createPatternSlot(int index) {
        var slot = createPatternSlotWidget(index);
        slot.getInner().setChangeListener(() -> onPatternChange(index));
        slot.getInner().setOnAddedTooltips((s, tooltips) -> {
            var tooltip = appendHoverTooltips(index);
            if (tooltip != null) tooltips.add(tooltip);
            if (supportsSlotConfig() && !patternInventory.getStackInSlot(index).isEmpty()) tooltips.add(Component.translatable(SLOT_CONFIG_HINT).withStyle(ChatFormatting.GRAY));
        });
        return slot;
    }

    // ==================== 拆下保存 ====================

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.put(ITEM_PATTERNS, patternInventory.serializeNBT());
        tag.putString(ITEM_NAME, customName);
        var list = new ListTag();
        for (var slot : internalInventory) {
            list.add(new ByteArrayTag(slot.writeData().writeToBytes()));
        }
        tag.put(ITEM_SLOTS, list);
        tag.putInt(ITEM_DATA_VERSION, GTDataFixer.VERSION);
    }

    /**
     * 兼容三代物品格式：内部槽列表元素为 {@link ByteArrayTag}（DataSyncLib，带或不带 {@code dv}）
     * 或 {@link CompoundTag}（LDLib 时代）。单个槽读取失败只记日志并跳过，不影响放置。
     */
    @Override
    public void loadFromItem(CompoundTag tag) {
        var patterns = tag.get(ITEM_PATTERNS);
        if (patterns != null) patternInventory.deserializeNBT(patterns);
        customName = tag.getString(ITEM_NAME);
        if (!(tag.get(ITEM_SLOTS) instanceof ListTag list)) return;
        int dataVersion = tag.contains(ITEM_DATA_VERSION) ? tag.getInt(ITEM_DATA_VERSION) : GTDataFixer.VERSION;
        int count = Math.min(list.size(), internalInventory.length);
        for (int i = 0; i < count; i++) {
            try {
                if (list.get(i) instanceof ByteArrayTag bytes) {
                    internalInventory[i].readData(Data.readData(bytes.getAsByteArray()), dataVersion);
                } else if (list.get(i) instanceof CompoundTag compound) {
                    internalInventory[i].deserializeNBT(compound);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load pattern slot {} of {} from item", i, getDefinition().getId(), e);
            }
        }
    }

    // ==================== 访问器 ====================

    public int getMaxPatternCount() {
        return maxPatternCount;
    }

    public CustomItemStackHandler getPatternInventory() {
        return patternInventory;
    }

    public PatternInventory getInternalPatternInventory() {
        return internalPatternInventory;
    }

    public BiMap<IPatternDetails, T> getDetailsSlotMap() {
        return detailsSlotMap;
    }

    public List<IPatternDetails> getPatterns() {
        return patterns;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        if (this.customName.equals(customName)) return;
        this.customName = customName;
        onChanged();
    }

    public boolean getShowInTravelNetwork() {
        return showInTravelNetwork;
    }

    public void setShowInTravelNetwork(boolean showInTravelNetwork) {
        this.showInTravelNetwork = showInTravelNetwork;
    }

    public IntNotifiableHolder getNewPageField() {
        return newPageField;
    }

    // ==================== 内部类 ====================

    public abstract static class AbstractInternalSlot extends AbstractDataSerializable {

        public abstract boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder);

        public abstract void onPatternChange();

        /** 读取 LDLib 时代的 NBT 格式（{@code dataVersion < 2} 的存档与旧物品）。 */
        public abstract void deserializeNBT(CompoundTag compoundTag);
    }

    /** 给 AE 样板终端看的样板槽视图，写入时同时触发样板变化处理。 */
    public static final class PatternInventory implements InternalInventory {

        private final MEPatternPartMachine<?> machine;

        private PatternInventory(MEPatternPartMachine<?> machine) {
            this.machine = machine;
        }

        public MEPatternPartMachine<?> getMachine() {
            return machine;
        }

        @Override
        public int size() {
            return machine.maxPatternCount;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return machine.patternInventory.getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            machine.patternInventory.setStackInSlot(slotIndex, stack);
            machine.patternInventory.onContentsChanged(slotIndex);
            machine.onPatternChange(slotIndex);
        }
    }
}

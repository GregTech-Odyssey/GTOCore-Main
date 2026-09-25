package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.MEStorageHatch;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.block.DrawerBlock;
import com.buuz135.functionalstorage.block.FluidDrawerBlock;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;
import java.util.function.Supplier;

/**
 * 抽屉存储器：抽屉与升级都放进结构的输入总线，主机（控制器）自己的槽里放超级箱或超级缸。
 * <ul>
 * <li>主机槽（一格、最多 {@link #CONTROLLER_LIMIT} 个）决定能存哪一大类：超级箱只能存物品、超级缸只能存流体，
 * 并按「等级 × 数量」给容量额外加成；</li>
 * <li>可存种类数 = 与主机同类别的抽屉槽数 × 抽屉数量（1x=1、2x=2、4x=4）；</li>
 * <li>每种类容量 = 那类抽屉里**最小**的每槽容量（混放取小的）× 密封机械方块等级 × 均分后的升级倍率 × 主机加成，
 * 流体抽屉按桶算再 ×1000 折算成 mB；</li>
 * <li>升级倍率：所有升级的倍率相乘，再按同类抽屉数量均分（{@code 乘积^(1/抽屉数)}），
 * 也就是 4 个抽屉 8 个升级只乘 2 次、4 个抽屉 3 个升级乘 0.75 次；流体的升级收益再减半（不低于 1）。</li>
 * <li>显示窗里可以开关「溢出销毁」：开启后，超过每种类容量或种类已满时放不下的内容会被直接销毁，
 * 调用方不会把放不下的部分收回去。</li>
 * </ul>
 */
@DataGeneratorScanned
public final class DrawerStorageMachine extends MultiblockMEStorageMachine implements IStorageMultiblock, IFancyUIMachine, IDisplayUIMachine {

    /// 主机槽（超级箱/缸）的数量上限
    public static final int CONTROLLER_LIMIT = 64;
    /// 升级按抽屉均分后，平均每个抽屉最多算几个升级（原版抽屉只放得下 4 个）
    public static final int MAX_UPGRADES_PER_DRAWER = 4;

    @RegisterLanguage(cn = "种类：%s / %s", en = "Types: %s / %s")
    public static final String TYPES = "gtocore.machine.drawer_storage.types";
    @RegisterLanguage(cn = "主机：%s ×%s（等级 %s）", en = "Controller: %s x%s (tier %s)")
    public static final String CONTROLLER = "gtocore.machine.drawer_storage.controller";
    @RegisterLanguage(cn = "主机槽里放超级箱（只能存物品）或超级缸（只能存流体），最多 " + CONTROLLER_LIMIT + " 个", en = "Put a super chest (items only) or a super tank (fluids only) into the controller slot, up to " + CONTROLLER_LIMIT)
    public static final String NO_CONTROLLER = "gtocore.machine.drawer_storage.no_controller";
    @RegisterLanguage(cn = "容量加成：√(主机 %s × 密封 %s) = ×%s", en = "Capacity bonus: sqrt(controller %s x hermetic %s) = x%s")
    public static final String BONUS = "gtocore.machine.drawer_storage.bonus";
    @RegisterLanguage(cn = "升级 %s 个（每个抽屉最多算 " + MAX_UPGRADES_PER_DRAWER + " 个）：按 %s 个抽屉均分，倍率 ×%s", en = "Upgrades %s (at most " + MAX_UPGRADES_PER_DRAWER + " per drawer): split over %s drawers, multiplier x%s")
    public static final String UPGRADES = "gtocore.machine.drawer_storage.upgrades";
    @RegisterLanguage(cn = "物品抽屉 %s 种：每种类容量 %s", en = "Item drawers %s types: %s per type")
    public static final String ITEM_DRAWERS = "gtocore.machine.drawer_storage.item_drawers";
    @RegisterLanguage(cn = "流体抽屉 %s 种：每种类容量 %s", en = "Fluid drawers %s types: %s per type")
    public static final String FLUID_DRAWERS = "gtocore.machine.drawer_storage.fluid_drawers";
    @RegisterLanguage(cn = "输入总线里放与主机同类的抽屉：可存种类 = 每个抽屉的槽数 × 数量", en = "Put drawers of the controller's kind into the input buses: type count = each drawer's slots x its count")
    public static final String NO_DRAWER = "gtocore.machine.drawer_storage.no_drawer";
    @RegisterLanguage(cn = "溢出销毁：%s", en = "Overflow voiding: %s")
    public static final String OVERFLOW = "gtocore.machine.drawer_storage.overflow";
    @RegisterLanguage(cn = "开启后，放不下的内容（超过每种类容量，或种类已满放不进新种类）会被直接销毁，调用方不会收回", en = "When enabled, anything that does not fit (over the per-type capacity, or no free type for a new key) is voided instead of being returned")
    public static final String OVERFLOW_TOOLTIP = "gtocore.machine.drawer_storage.overflow.tooltip";

    /// 溢出销毁开关的按钮键
    private static final String OVERFLOW_BUTTON = "voidOverflow";

    /// 主机槽（超级箱/缸）：一格，最多 {@link #CONTROLLER_LIMIT} 个
    @SaveToDisk
    @Getter
    private final NotifiableItemStackHandler machineStorage;
    /// 可存种类数：同类抽屉的槽数 × 数量
    @Getter
    private int types;
    /// 每种类容量
    @Getter
    private long perTypeCapacity;
    /// 主机槽决定的大类：true = 只能存流体
    @Getter
    private boolean fluidKind;
    /// 同类抽屉的数量（升级按它均分）
    @Getter
    private int drawerCount;
    /// 总线里算进倍率的升级数量
    @Getter
    private int upgradeCount;
    /// 均分后的升级倍率（已含流体减半）
    @Getter
    private double upgradeMultiplier = 1;
    /// 主机等级 × 数量
    @Getter
    private long controllerMultiplier;
    @Getter
    private int controllerLevel;
    @Getter
    private int hermeticLevel = 1;
    /// 溢出销毁：放不下的内容直接销毁，不退回调用方
    @SaveToDisk(defaultValue = "false")
    @Getter
    private boolean voidOverflow;

    /// 输入总线：放抽屉与升级
    private final StorageBusListener drawerBuses = new StorageBusListener();

    public DrawerStorageMachine(MetaMachineBlockEntity holder) {
        super(holder, null);
        machineStorage = createMachineStorage(null);
        machineStorage.setCapabilityValidator(GTUtil.NEGATIVE);
    }

    /// 主机槽只收超级箱 / 超级缸
    @Override
    public boolean storageFilter(ItemStack stack) {
        return controllerDefinition(stack) != null;
    }

    @Override
    public int getSlotLimit() {
        return CONTROLLER_LIMIT;
    }

    @Override
    public Supplier<BlockPattern>[] getPattern() {
        return getDefinition().getPatternFactory();
    }

    /// 不设总容量：只按种类卡每一种的上限
    @Override
    protected long computeCapacity() {
        refreshDrawers();
        return Long.MAX_VALUE;
    }

    /// 重扫输入总线里的抽屉/升级与主机槽里的超级箱缸，算出种类数与每种类容量
    private void refreshDrawers() {
        hermeticLevel = getMultiblockState().getMatchContext().getOrDefault(GTORecipeDataKeys.HERMETIC_CASING_TIER, 0) + 1;
        var controller = getStorageStack();
        var definition = controllerDefinition(controller);
        fluidKind = definition != null && isSuperTank(definition);
        controllerLevel = definition == null ? 0 : Math.max(1, definition.getTier());
        controllerMultiplier = definition == null ? 0 : (long) controllerLevel * controller.getCount();

        long minSlotAmount = 0;
        int types = 0;
        int drawers = 0;
        int upgrades = 0;
        double upgradeProduct = 1;
        for (var bus : drawerBuses.buses()) {
            for (int i = 0, slots = bus.getSlots(); i < slots; i++) {
                var stack = bus.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                var drawerType = drawerType(stack);
                if (drawerType != null) {
                    // 只算与主机同类的抽屉：它们决定种类数、最小每槽容量与升级均分基数
                    if (isFluidDrawer(stack) != fluidKind) continue;
                    types += drawerType.getSlots() * stack.getCount();
                    drawers += stack.getCount();
                    // 流体抽屉的容量按桶算，折算成 mB
                    long slotAmount = fluidKind ? drawerType.getSlotAmount() * 1000L : drawerType.getSlotAmount();
                    minSlotAmount = minSlotAmount == 0 ? slotAmount : Math.min(minSlotAmount, slotAmount);
                    continue;
                }
                int perItem = upgradeMultiplier(stack);
                if (perItem < 2) continue;
                upgrades += stack.getCount();
                upgradeProduct *= Math.pow(perItem, stack.getCount());
                if (Double.isInfinite(upgradeProduct)) upgradeProduct = Double.MAX_VALUE;
            }
        }
        this.types = types;
        this.drawerCount = drawers;
        this.upgradeCount = upgrades;
        this.upgradeMultiplier = averageUpgrade(upgradeProduct, upgrades, drawers, fluidKind);
        this.perTypeCapacity = minSlotAmount < 1 ? 0 :
                capacityOf(minSlotAmount, hermeticLevel, upgradeMultiplier, controllerMultiplier);
    }

    /// 升级倍率：所有升级相乘后按同类抽屉数量均分（{@code 乘积^(1/抽屉数)}），
    /// 且平均到每个抽屉的升级数最多算 {@link #MAX_UPGRADES_PER_DRAWER} 个（原版抽屉就只放得下 4 个）；
    /// 流体只拿一半，不低于 1
    private static double averageUpgrade(double product, int upgradeCount, int drawerCount, boolean fluid) {
        if (product <= 1 || upgradeCount < 1 || drawerCount < 1) return 1;
        double counted = Math.min(upgradeCount, (double) MAX_UPGRADES_PER_DRAWER * drawerCount);
        double multiplier = Math.pow(product, counted / upgradeCount / drawerCount);
        return fluid ? Math.max(1, multiplier / 2) : multiplier;
    }

    @Nullable
    private static FunctionalStorage.DrawerType drawerType(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        var block = blockItem.getBlock();
        if (block instanceof DrawerBlock drawer) return drawer.getType();
        if (block instanceof FluidDrawerBlock drawer) return drawer.getType();
        return null;
    }

    private static boolean isFluidDrawer(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof FluidDrawerBlock;
    }

    /// 主机槽里超级箱 / 超级缸对应的定义；不是这两类返回 null
    @Nullable
    private static MachineDefinition controllerDefinition(ItemStack stack) {
        if (!(stack.getItem() instanceof MetaMachineItem item)) return null;
        var definition = item.getDefinition();
        if (contains(GTMachines.SUPER_CHEST, definition) || contains(GTMachines.SUPER_TANK, definition)) return definition;
        return null;
    }

    private static boolean contains(MachineDefinition[] definitions, MachineDefinition definition) {
        for (var candidate : definitions) {
            if (candidate != null && candidate == definition) return true;
        }
        return false;
    }

    private static boolean isSuperTank(MachineDefinition definition) {
        return contains(GTMachines.SUPER_TANK, definition);
    }

    /// 存储升级的倍率；创造（最大存储）升级返回 0，不算倍率
    private static int upgradeMultiplier(ItemStack stack) {
        if (!(stack.getItem() instanceof StorageUpgradeItem upgrade)) return 0;
        if (upgrade.getStorageTier() == StorageUpgradeItem.StorageTier.MAX_STORAGE) return 0;
        return upgrade.getStorageMultiplier();
    }

    /// 每种类容量：最小每槽容量 × √(密封等级 × 主机加成) × 均分后的升级倍率
    private static long capacityOf(long slotAmount, int hermeticLevel, double upgradeMultiplier, long controllerMultiplier) {
        // 超级箱/缸与密封外壳的总加成开根号，避免两个乘数叠得太狠
        double bonus = Math.sqrt(Math.max(0, hermeticLevel) * (double) Math.max(0, controllerMultiplier));
        double capacity = Math.max(1, slotAmount) * bonus * Math.max(1, upgradeMultiplier);
        if (!(capacity < Long.MAX_VALUE)) return Long.MAX_VALUE;
        return Math.max(1, (long) capacity);
    }

    /// 主机决定的大类：超级箱只收物品、超级缸只收流体
    private boolean accepts(AEKey what) {
        return fluidKind ? what instanceof AEFluidKey : what instanceof AEItemKey;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!isFormed || amount < 1 || types < 1 || perTypeCapacity < 1 || !accepts(what)) return 0;
        var map = keyMap;
        if (mode == Actionable.SIMULATE) {
            var stored = map.getAmount(what);
            // 种类满了就一个新种类也进不来；已经存过的种类只受每种类容量的限制
            if (stored == 0 && map.size() >= types) return voidOverflow ? amount : 0;
            return voidOverflow ? amount : Math.clamp(perTypeCapacity - stored, 0, amount);
        }
        // 实际插入前调用方都会先模拟一次，这里就不用再查种类了
        long inserted = map.insert(what, amount, perTypeCapacity);
        if (inserted > 0) saveChanges();
        // 溢出销毁：放不下的部分也照单收下（多的直接销毁），调用方不会再把它们退回来
        return voidOverflow ? amount : inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (mode != Actionable.MODULATE) return Math.min(amount, keyMap.getAmount(what));
        long extracted = keyMap.extract(what, amount);
        if (extracted > 0) saveChanges();
        return extracted;
    }

    @Override
    protected void saveChanges() {
        holder.setChanged();
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (!isFormed || types < 1 || perTypeCapacity < 1 || !accepts(what)) return false;
        var map = getKeyMap();
        long stored = map.getAmount(what);
        return stored < perTypeCapacity && (stored > 0 || map.size() < types);
    }

    @Override
    public void onStructureFormed() {
        // 抽屉/升级总线要先挂上，容量里要用
        drawerBuses.bind(getMultiblockState().getMatchContext(), this, this::onDrawerBusChanged);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        drawerBuses.unbind();
        types = 0;
        drawerCount = 0;
        perTypeCapacity = 0;
        upgradeMultiplier = 1;
        controllerMultiplier = 0;
        controllerLevel = 0;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        drawerBuses.unbind();
    }

    /// 主机槽里的超级箱/缸换了（{@link IStorageMultiblock} 的槽变更回调）
    @Override
    public void onMachineChanged() {
        if (isRemote() || !isFormed) return;
        refreshCapacity();
        refreshHatches();
        onChanged();
    }

    private void onDrawerBusChanged() {
        if (isRemote() || !isFormed) return;
        long oldCapacity = perTypeCapacity;
        int oldTypes = types;
        int oldDrawers = drawerCount;
        refreshCapacity();
        if (oldCapacity == perTypeCapacity && oldTypes == types && oldDrawers == drawerCount) return;
        refreshHatches();
        onChanged();
    }

    /// 容量变了，让结构里的保险库仓重新读一遍
    private void refreshHatches() {
        for (var part : getParts()) {
            if (part.self() instanceof MEStorageHatch hatch) hatch.refreshStorageBinding();
        }
    }

    // ==================== 界面 ====================

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return isFormed && IFancyUIMachine.super.shouldOpenUI(player, hand, hit);
    }

    /// 显示窗那一套：主页是机器的状态显示窗，主机槽（超级箱/缸）跟在下方（和通用工厂一样）
    @Override
    public Widget createUIWidget() {
        return IStorageMultiblock.super.createUIWidget(MachineDisplay.page(this));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new MachineWindow(this));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        var controller = getStorageStack();
        if (controller.isEmpty()) {
            textList.add(Component.translatable(NO_CONTROLLER).withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(Component.translatable(CONTROLLER, controller.getHoverName(),
                    FormattingUtil.formatNumbers(controller.getCount()),
                    FormattingUtil.formatNumbers(controllerLevel)).withStyle(ChatFormatting.GRAY));
            textList.add(Component.translatable(BONUS,
                    FormattingUtil.formatNumbers(controllerMultiplier),
                    FormattingUtil.formatNumbers(hermeticLevel),
                    NumberUtils.formatDouble(capacityBonus())).withStyle(ChatFormatting.GRAY));
        }
        textList.add(Component.translatable(UPGRADES, FormattingUtil.formatNumbers(upgradeCount),
                FormattingUtil.formatNumbers(drawerCount), NumberUtils.formatDouble(upgradeMultiplier))
                .withStyle(ChatFormatting.GRAY));
        if (types < 1) {
            textList.add(Component.translatable(NO_DRAWER).withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(Component.translatable(fluidKind ? FLUID_DRAWERS : ITEM_DRAWERS,
                    FormattingUtil.formatNumbers(types),
                    FormattingUtil.formatNumbers(perTypeCapacity)).withStyle(ChatFormatting.GRAY));
        }
        textList.add(Component.translatable(TYPES,
                FormattingUtil.formatNumbers(getKeyMap().size()),
                FormattingUtil.formatNumbers(types)).withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable(OVERFLOW, ComponentPanelWidget.withButton(
                Component.translatable(voidOverflow ? "gtocore.machine.on" : "gtocore.machine.off"), OVERFLOW_BUTTON))
                .withStyle(ChatFormatting.GRAY)
                .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable(OVERFLOW_TOOLTIP).withStyle(ChatFormatting.YELLOW)))));
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote && OVERFLOW_BUTTON.equals(componentData)) {
            voidOverflow = !voidOverflow;
            onChanged();
            return;
        }
        IDisplayUIMachine.super.handleDisplayClick(componentData, clickData);
    }

    /// 主机与密封的总加成开根号（显示用）
    private double capacityBonus() {
        return Math.sqrt(Math.max(0, hermeticLevel) * (double) Math.max(0, controllerMultiplier));
    }

    @Override
    public void appendWailaData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        compoundTag.putInt("types", types);
        compoundTag.putInt("usedTypes", getKeyMap().size());
        compoundTag.putLong("perTypeCapacity", perTypeCapacity);
        compoundTag.putInt("drawerCount", drawerCount);
        compoundTag.putInt("upgradeCount", upgradeCount);
        compoundTag.putDouble("upgradeMultiplier", upgradeMultiplier);
        compoundTag.putLong("controllerMultiplier", controllerMultiplier);
        compoundTag.putInt("hermeticLevel", hermeticLevel);
        compoundTag.putBoolean("fluidKind", fluidKind);
        compoundTag.putBoolean("voidOverflow", voidOverflow);
    }

    @Override
    public void appendWailaTooltip(CompoundTag compoundTag, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.add(Component.translatable(TYPES,
                FormattingUtil.formatNumbers(compoundTag.getInt("usedTypes")),
                FormattingUtil.formatNumbers(compoundTag.getInt("types"))));
        iTooltip.add(Component.translatable(compoundTag.getBoolean("fluidKind") ? FLUID_DRAWERS : ITEM_DRAWERS,
                FormattingUtil.formatNumbers(compoundTag.getInt("types")),
                FormattingUtil.formatNumbers(compoundTag.getLong("perTypeCapacity"))));
        iTooltip.add(Component.translatable(UPGRADES,
                FormattingUtil.formatNumbers(compoundTag.getInt("upgradeCount")),
                FormattingUtil.formatNumbers(compoundTag.getInt("drawerCount")),
                NumberUtils.formatDouble(compoundTag.getDouble("upgradeMultiplier"))));
        iTooltip.add(Component.translatable(BONUS,
                FormattingUtil.formatNumbers(compoundTag.getLong("controllerMultiplier")),
                FormattingUtil.formatNumbers(compoundTag.getInt("hermeticLevel")),
                NumberUtils.formatDouble(Math.sqrt(Math.max(0, compoundTag.getInt("hermeticLevel")) *
                        (double) Math.max(0, compoundTag.getLong("controllerMultiplier"))))));
        iTooltip.add(Component.translatable(OVERFLOW,
                Component.translatable(compoundTag.getBoolean("voidOverflow") ? "gtocore.machine.on" : "gtocore.machine.off")));
    }
}

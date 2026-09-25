package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.MEStorageHatch;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;
import java.util.function.Supplier;

/**
 * 抽屉存储器：抽屉放进结构的输入总线，升级插在主机（控制器）自己的槽里（一格、最多 16 个，防止乘算溢出）。
 * <ul>
 * <li>每种类容量 = 那一类抽屉里最好的每槽容量 × 升级倍率 × 密封机械方块等级（抽屉数量不乘容量）；
 * 流体抽屉按桶算再 ×1000 折算成 mB；</li>
 * <li>种类数 = 每个抽屉自己的槽数 × 抽屉数量（1x=1、2x=2、4x=4），物品与流体共用一个池子；</li>
 * <li>类型跟着抽屉走：没有物品抽屉就存不了物品，没有流体抽屉就存不了流体；</li>
 * <li>升级倍率是乘算（与功能存储一致）：每个升级按自己的倍率相乘。</li>
 * </ul>
 */
@DataGeneratorScanned
public final class DrawerStorageMachine extends MultiblockMEStorageMachine implements IStorageMultiblock, IFancyUIMachine, IDisplayUIMachine {

    /// 主机槽里的升级上限（乘算，多了会溢出）
    public static final int UPGRADE_LIMIT = 16;

    @RegisterLanguage(cn = "种类：%s / %s", en = "Types: %s / %s")
    public static final String TYPES = "gtocore.machine.drawer_storage.types";
    @RegisterLanguage(cn = "密封等级 %s × 升级倍率 %s", en = "Hermetic tier %s x upgrade multiplier %s")
    public static final String HERMETIC_UPGRADE = "gtocore.machine.drawer_storage.hermetic_upgrade";
    @RegisterLanguage(cn = "升级 %s / " + UPGRADE_LIMIT + " 个：倍率 ×%s", en = "Upgrades %s / " + UPGRADE_LIMIT + ": multiplier x%s")
    public static final String UPGRADES = "gtocore.machine.drawer_storage.upgrades";
    @RegisterLanguage(cn = "物品抽屉 %s 种：每种类容量 %s", en = "Item drawers %s types: %s per type")
    public static final String ITEM_DRAWERS = "gtocore.machine.drawer_storage.item_drawers";
    @RegisterLanguage(cn = "没有物品抽屉：装不了物品", en = "No item drawer: items cannot be stored")
    public static final String NO_ITEM_DRAWERS = "gtocore.machine.drawer_storage.no_item_drawers";
    @RegisterLanguage(cn = "流体抽屉 %s 种：每种类容量 %s", en = "Fluid drawers %s types: %s per type")
    public static final String FLUID_DRAWERS = "gtocore.machine.drawer_storage.fluid_drawers";
    @RegisterLanguage(cn = "没有流体抽屉：装不了流体", en = "No fluid drawer: fluids cannot be stored")
    public static final String NO_FLUID_DRAWERS = "gtocore.machine.drawer_storage.no_fluid_drawers";
    @RegisterLanguage(cn = "输入总线里放抽屉：没有物品抽屉就存不了物品，没有流体抽屉就存不了流体", en = "Put drawers into the input buses: without an item drawer nothing can be stored as items, without a fluid drawer nothing as fluids")
    public static final String NO_DRAWER = "gtocore.machine.drawer_storage.no_drawer";

    /// 主机槽（升级）：一格，最多 {@link #UPGRADE_LIMIT} 个
    @SaveToDisk
    @Getter
    private final NotifiableItemStackHandler machineStorage;
    /// 可存种类数：所有抽屉的槽数 × 数量（物品与流体共用）
    @Getter
    private int types;
    @Getter
    private int itemTypes;
    @Getter
    private int fluidTypes;
    /// 物品每种类容量（没有物品抽屉就是 0）
    @Getter
    private long itemPerTypeCapacity;
    /// 流体每种类容量（没有流体抽屉就是 0）
    @Getter
    private long fluidPerTypeCapacity;
    @Getter
    private long upgradeMultiplier = 1;
    @Getter
    private int hermeticLevel = 1;

    /// 输入总线：只用来放抽屉
    private final StorageBusListener drawerBuses = new StorageBusListener();

    public DrawerStorageMachine(MetaMachineBlockEntity holder) {
        super(holder, null);
        machineStorage = createMachineStorage(null);
    }

    /// 主机槽只收功能性存储的升级；抽屉放输入总线
    @Override
    public boolean storageFilter(ItemStack stack) {
        return stack.getItem() instanceof StorageUpgradeItem;
    }

    @Override
    public int getSlotLimit() {
        return UPGRADE_LIMIT;
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

    /// 重扫输入总线里的抽屉与主机槽里的升级，算出种类数与每种类容量
    private void refreshDrawers() {
        upgradeMultiplier = scanUpgrades();
        hermeticLevel = getMultiblockState().getMatchContext().getOrDefault(GTORecipeDataKeys.HERMETIC_CASING_TIER, 0) + 1;
        long itemSlotAmount = 0;
        long fluidSlotAmount = 0;
        itemTypes = 0;
        fluidTypes = 0;
        for (var bus : drawerBuses.buses()) {
            for (int i = 0, slots = bus.getSlots(); i < slots; i++) {
                var stack = bus.getStackInSlot(i);
                var drawerType = drawerType(stack);
                if (drawerType == null) continue;
                int types = drawerType.getSlots() * stack.getCount();
                if (isFluidDrawer(stack)) {
                    fluidTypes += types;
                    // 流体抽屉的容量按桶算，折算成 mB
                    fluidSlotAmount = Math.max(fluidSlotAmount, drawerType.getSlotAmount() * 1000L);
                } else {
                    itemTypes += types;
                    itemSlotAmount = Math.max(itemSlotAmount, drawerType.getSlotAmount());
                }
            }
        }
        this.types = itemTypes + fluidTypes;
        itemPerTypeCapacity = itemSlotAmount < 1 ? 0 : capacityOf(itemSlotAmount, hermeticLevel, upgradeMultiplier);
        fluidPerTypeCapacity = fluidSlotAmount < 1 ? 0 : capacityOf(fluidSlotAmount, hermeticLevel, upgradeMultiplier);
    }

    /// 升级倍率：和功能存储一样是乘算——主机槽里每个升级按自己的倍率相乘
    private long scanUpgrades() {
        long multiplier = 1;
        var stack = getStorageStack();
        int perItem = upgradeMultiplier(stack);
        if (perItem < 2) return multiplier;
        for (int count = Math.min(stack.getCount(), UPGRADE_LIMIT); count > 0; count--) {
            multiplier = multiply(multiplier, perItem);
            if (multiplier == Long.MAX_VALUE) return multiplier;
        }
        return multiplier;
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

    private static int upgradeMultiplier(ItemStack stack) {
        return stack.getItem() instanceof StorageUpgradeItem upgrade ? upgrade.getStorageMultiplier() : 0;
    }

    /// 每种类容量：抽屉的每槽容量 × 升级倍率 × 密封等级
    private static long capacityOf(long slotAmount, int hermeticLevel, long upgradeMultiplier) {
        long capacity = Math.max(1, slotAmount);
        capacity = multiply(capacity, Math.max(1, upgradeMultiplier));
        return multiply(capacity, Math.max(1, hermeticLevel));
    }

    private static long multiply(long a, long b) {
        return a > Long.MAX_VALUE / b ? Long.MAX_VALUE : a * b;
    }

    /// 这一类东西的每种类容量；0 表示没有这一类的抽屉，装不了
    private long capacityFor(AEKey what) {
        if (what instanceof AEItemKey) return itemPerTypeCapacity;
        if (what instanceof AEFluidKey) return fluidPerTypeCapacity;
        return 0;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        long capacity = capacityFor(what);
        if (!isFormed || amount < 1 || types < 1 || capacity < 1) return 0;
        var map = keyMap;
        var size = map.size();
        if (mode == Actionable.SIMULATE) {
            var stored = map.getAmount(what);
            if (stored == 0 && size >= types) return 0;
            return Math.min(amount, capacity - stored);
        }
        if (size > types) return 0;
        long inserted = map.insert(what, amount, capacity);
        if (inserted > 0) saveChanges();
        return inserted;
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
        long capacity = capacityFor(what);
        if (!isFormed || types < 1 || capacity < 1) return false;
        var map = getKeyMap();
        long stored = map.getAmount(what);
        return stored < capacity && (stored > 0 || map.size() < types);
    }

    @Override
    public void onStructureFormed() {
        // 抽屉总线要先挂上，容量里要用
        drawerBuses.bind(getMultiblockState().getMatchContext(), this, this::onDrawerBusChanged);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        drawerBuses.unbind();
        types = 0;
        itemTypes = 0;
        fluidTypes = 0;
        itemPerTypeCapacity = 0;
        fluidPerTypeCapacity = 0;
        upgradeMultiplier = 1;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        drawerBuses.unbind();
    }

    /// 主机槽里的升级换了（{@link IStorageMultiblock} 的槽变更回调）
    @Override
    public void onMachineChanged() {
        if (isRemote() || !isFormed) return;
        refreshCapacity();
        refreshHatches();
        onChanged();
    }

    private void onDrawerBusChanged() {
        if (isRemote() || !isFormed) return;
        long oldItem = itemPerTypeCapacity;
        long oldFluid = fluidPerTypeCapacity;
        int oldTypes = types;
        refreshCapacity();
        if (oldItem == itemPerTypeCapacity && oldFluid == fluidPerTypeCapacity && oldTypes == types) return;
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

    /// 显示窗那一套：主页是机器的状态显示窗，主机槽（升级）跟在下方（和通用工厂一样）
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
        textList.add(Component.translatable(UPGRADES,
                FormattingUtil.formatNumbers(getStorageStack().getCount()),
                FormattingUtil.formatNumbers(upgradeMultiplier)).withStyle(ChatFormatting.GRAY));
        if (itemTypes < 1) {
            textList.add(Component.translatable(NO_ITEM_DRAWERS).withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(Component.translatable(ITEM_DRAWERS, FormattingUtil.formatNumbers(itemTypes),
                    FormattingUtil.formatNumbers(itemPerTypeCapacity)).withStyle(ChatFormatting.GRAY));
        }
        if (fluidTypes < 1) {
            textList.add(Component.translatable(NO_FLUID_DRAWERS).withStyle(ChatFormatting.GRAY));
        } else {
            textList.add(Component.translatable(FLUID_DRAWERS, FormattingUtil.formatNumbers(fluidTypes),
                    FormattingUtil.formatNumbers(fluidPerTypeCapacity)).withStyle(ChatFormatting.GRAY));
        }
        if (types < 1) {
            textList.add(Component.translatable(NO_DRAWER).withStyle(ChatFormatting.GRAY));
        }
        textList.add(Component.translatable(TYPES,
                FormattingUtil.formatNumbers(getKeyMap().size()),
                FormattingUtil.formatNumbers(types)).withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable(HERMETIC_UPGRADE,
                FormattingUtil.formatNumbers(hermeticLevel),
                FormattingUtil.formatNumbers(upgradeMultiplier)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void appendWailaData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        compoundTag.putInt("types", types);
        compoundTag.putInt("usedTypes", getKeyMap().size());
        compoundTag.putInt("itemTypes", itemTypes);
        compoundTag.putInt("fluidTypes", fluidTypes);
        compoundTag.putLong("itemCapacity", itemPerTypeCapacity);
        compoundTag.putLong("fluidCapacity", fluidPerTypeCapacity);
        compoundTag.putLong("upgrades", getStorageStack().getCount());
        compoundTag.putLong("upgradeMultiplier", upgradeMultiplier);
        compoundTag.putInt("hermeticLevel", hermeticLevel);
    }

    @Override
    public void appendWailaTooltip(CompoundTag compoundTag, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.add(Component.translatable(TYPES,
                FormattingUtil.formatNumbers(compoundTag.getInt("usedTypes")),
                FormattingUtil.formatNumbers(compoundTag.getInt("types"))));
        iTooltip.add(compoundTag.getInt("itemTypes") < 1 ? Component.translatable(NO_ITEM_DRAWERS) :
                Component.translatable(ITEM_DRAWERS, FormattingUtil.formatNumbers(compoundTag.getInt("itemTypes")),
                        FormattingUtil.formatNumbers(compoundTag.getLong("itemCapacity"))));
        iTooltip.add(compoundTag.getInt("fluidTypes") < 1 ? Component.translatable(NO_FLUID_DRAWERS) :
                Component.translatable(FLUID_DRAWERS, FormattingUtil.formatNumbers(compoundTag.getInt("fluidTypes")),
                        FormattingUtil.formatNumbers(compoundTag.getLong("fluidCapacity"))));
        iTooltip.add(Component.translatable(UPGRADES, FormattingUtil.formatNumbers(compoundTag.getLong("upgrades")),
                FormattingUtil.formatNumbers(compoundTag.getLong("upgradeMultiplier"))));
    }
}

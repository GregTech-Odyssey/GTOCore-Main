package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.MEStorageHatch;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.api.machine.storage.DrawerStorageCapacity;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.StorageUpgradeItem;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.AmountFormat;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import lombok.Getter;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;

/**
 * 抽屉存储器：主机提供一个升级槽，输入总线中的超级箱和超级缸分别贡献物品与流体存储。
 * <ul>
 * <li>物品与流体的可存种类数分别累计对应箱缸的格数 × 数量（普通=1、1x2=2、2x2=4）；</li>
 * <li>每种类容量 = 对应类型箱缸的总容量 ÷ 总格数 × 密封等级 × 升级倍率。</li>
 * <li>显示窗里可以开关「溢出销毁」：开启后，超过每种类容量或种类已满时放不下的内容会被直接销毁，
 * 调用方不会把放不下的部分收回去。</li>
 * </ul>
 */
@DataGeneratorScanned
public final class DrawerStorageMachine extends MultiblockMEStorageMachine implements IStorageMultiblock, IFancyUIMachine, IDisplayUIMachine {

    @RegisterLanguage(cn = "超级箱 / 超级缸", en = "Super Chest / Super Tank")
    private static final Component SLOT_LABEL = Component.translatable("gtocore.machine.drawer_storage.slot");

    /// 保留旧提示文案的常量，文案由人工维护。
    public static final int CONTROLLER_LIMIT = 64;

    @RegisterLanguage(cn = "种类：%s / %s", en = "Types: %s / %s")
    public static final String TYPES = "gtocore.machine.drawer_storage.types";
    @RegisterLanguage(cn = "主机：%s ×%s（等级 %s）", en = "Controller: %s x%s (tier %s)")
    public static final String CONTROLLER = "gtocore.machine.drawer_storage.controller";
    @RegisterLanguage(cn = "主机槽里放超级箱（只能存物品）或超级缸（只能存流体），最多 " + CONTROLLER_LIMIT + " 个", en = "Put a super chest (items only) or a super tank (fluids only) into the controller slot, up to " + CONTROLLER_LIMIT)
    public static final String NO_CONTROLLER = "gtocore.machine.drawer_storage.no_controller";
    @RegisterLanguage(cn = "容量加成：√(主机 %s × 密封 %s) = ×%s", en = "Capacity bonus: sqrt(controller %s x hermetic %s) = x%s")
    public static final String BONUS = "gtocore.machine.drawer_storage.bonus";
    @RegisterLanguage(cn = "输入箱缸 %s 个：最大格数 %s，数量收益 ×%s", en = "Input storage units: %s, maximum slots %s, count bonus x%s")
    public static final String STORAGE_COUNT = "gtocore.machine.drawer_storage.storage_count";
    @RegisterLanguage(cn = "升级 %s 个：按 %s 个箱缸均分，每个最多计入 4 个，倍率 ×%s", en = "Upgrades: %s, shared over %s storage units, at most 4 per unit, multiplier x%s")
    public static final String UPGRADES = "gtocore.machine.drawer_storage.upgrades";
    @RegisterLanguage(cn = "超级箱 %s 种：每种类容量 %s", en = "Super chests %s types: %s per type")
    public static final String ITEM_DRAWERS = "gtocore.machine.drawer_storage.item_drawers";
    @RegisterLanguage(cn = "超级缸 %s 种：每种类容量 %s", en = "Super tanks %s types: %s per type")
    public static final String FLUID_DRAWERS = "gtocore.machine.drawer_storage.fluid_drawers";
    @RegisterLanguage(cn = "输入总线放与主机同类的超级箱/缸：普通、1x2、2x2 每个分别提供 1、2、4 种", en = "Put super chests/tanks matching the controller into the input buses: normal, 1x2 and 2x2 units provide 1, 2 and 4 types each")
    public static final String NO_DRAWER = "gtocore.machine.drawer_storage.no_drawer";
    @RegisterLanguage(cn = "溢出销毁：%s", en = "Overflow voiding: %s")
    public static final String OVERFLOW = "gtocore.machine.drawer_storage.overflow";
    @RegisterLanguage(cn = "开启后，放不下的内容（超过每种类容量，或种类已满放不进新种类）会被直接销毁，调用方不会收回", en = "When enabled, anything that does not fit (over the per-type capacity, or no free type for a new key) is voided instead of being returned")
    public static final Component OVERFLOW_TOOLTIP = Component.translatable("gtocore.machine.drawer_storage.overflow.tooltip");
    @RegisterLanguage(cn = "溢出销毁", en = "Overflow Voiding")
    private static final Component OVERFLOW_LABEL = Component.translatable("gtocore.machine.drawer_storage.overflow.label");

    @SaveToDisk
    @Getter
    private final NotifiableStackInventory machineStorage;
    private final DrawerStorageCapacity drawers = new DrawerStorageCapacity();
    @Getter
    private long itemPerTypeCapacity;
    @Getter
    private long fluidPerTypeCapacity;
    @Getter
    private int usedItemTypes;
    @Getter
    private int usedFluidTypes;
    @Getter
    private int hermeticLevel = 1;
    /// 溢出销毁：放不下的内容直接销毁，不退回调用方
    @SaveToDisk(defaultValue = "false")
    @Getter
    private boolean voidOverflow;

    /// 输入总线：放超级箱/缸。
    private final StorageBusListener drawerBuses = new StorageBusListener();

    public DrawerStorageMachine(MetaMachineBlockEntity holder) {
        super(holder, null, false);
        machineStorage = createMachineStorage(null);
        machineStorage.setCapabilityValidator(GTUtil.NEGATIVE);
    }

    @Override
    public boolean storageFilter(ItemStack stack) {
        return stack.getItem() instanceof StorageUpgradeItem;
    }

    @Override
    public int getSlotLimit() {
        return 1;
    }

    public int getUpgradeMultiplier() {
        var stack = getStorageStack();
        return !stack.isEmpty() && stack.getItem() instanceof StorageUpgradeItem upgrade ? upgrade.getCapacityMultiplier() : 1;
    }

    /// 不设总容量：只按种类卡每一种的上限
    @Override
    protected long computeCapacity() {
        refreshDrawers();
        return Long.MAX_VALUE;
    }

    /// 输入总线变化时重新累计箱缸；主机升级变化只重算倍率。
    private void refreshDrawers() {
        hermeticLevel = getMultiblockState().getMatchContext().getOrDefault(GTORecipeDataKeys.HERMETIC_CASING_TIER, 0) + 1;
        drawers.clear();
        for (var bus : drawerBuses.buses()) {
            var inv = bus.storage;
            for (int i = 0, slots = inv.size(); i < slots; i++) {
                var key = inv.keyAt(i);
                if (key == null) continue;
                long count = inv.amountAt(i);
                if (count < 1) continue;
                var storage = DrawerStorageCapacity.get(key.getItem());
                if (storage != null) drawers.add(storage, count);
            }
        }
        updatePerTypeCapacity();
    }

    private void updatePerTypeCapacity() {
        int multiplier = getUpgradeMultiplier();
        itemPerTypeCapacity = drawers.capacityOf(false, hermeticLevel, multiplier);
        fluidPerTypeCapacity = drawers.capacityOf(true, hermeticLevel, multiplier);
    }

    public int getItemTypes() {
        return drawers.getItemTypes();
    }

    public int getFluidTypes() {
        return drawers.getFluidTypes();
    }

    private int typeLimit(AEKey what) {
        return what instanceof AEItemKey ? getItemTypes() : what instanceof AEFluidKey ? getFluidTypes() : 0;
    }

    private long perTypeCapacity(AEKey what) {
        return what instanceof AEItemKey ? itemPerTypeCapacity : what instanceof AEFluidKey ? fluidPerTypeCapacity : 0;
    }

    private int usedTypes(AEKey what) {
        return what instanceof AEItemKey ? usedItemTypes : usedFluidTypes;
    }

    private void changeUsedTypes(AEKey what, int delta) {
        if (what instanceof AEItemKey) usedItemTypes += delta;
        else if (what instanceof AEFluidKey) usedFluidTypes += delta;
    }

    private void recountUsedTypes() {
        usedItemTypes = 0;
        usedFluidTypes = 0;
        keyMap.fastForEach((key, amount) -> {
            if (amount > 0) changeUsedTypes(key, 1);
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        recountUsedTypes();
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        super.loadFromItem(tag);
        recountUsedTypes();
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        int types = typeLimit(what);
        long perTypeCapacity = perTypeCapacity(what);
        if (!isFormed || amount < 1 || types < 1 || perTypeCapacity < 1) return 0;
        var map = keyMap;
        long stored = map.getAmount(what);
        int used = usedTypes(what);
        if (used > types) return 0;
        if (stored == 0 && used >= types) return voidOverflow ? amount : 0;
        if (mode == Actionable.SIMULATE) {
            return voidOverflow ? amount : Math.clamp(perTypeCapacity - stored, 0, amount);
        }
        long inserted = map.insert(what, amount, perTypeCapacity);
        if (inserted > 0) {
            if (stored == 0) changeUsedTypes(what, 1);
            saveChanges();
        }
        // 溢出销毁：放不下的部分也照单收下（多的直接销毁），调用方不会再把它们退回来
        return voidOverflow ? amount : inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (mode != Actionable.MODULATE) return Math.min(amount, keyMap.getAmount(what));
        long extracted = keyMap.extract(what, amount);
        if (extracted > 0) {
            if (!keyMap.containsKey(what)) changeUsedTypes(what, -1);
            saveChanges();
        }
        return extracted;
    }

    @Override
    protected void saveChanges() {
        holder.setChanged();
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        int types = typeLimit(what);
        long perTypeCapacity = perTypeCapacity(what);
        if (!isFormed || types < 1 || perTypeCapacity < 1) return false;
        var map = getKeyMap();
        long stored = map.getAmount(what);
        int used = usedTypes(what);
        return used <= types && stored < perTypeCapacity && (stored > 0 || used < types);
    }

    @Override
    public void onStructureFormed() {
        // 箱缸总线要先挂上，容量里要用
        drawerBuses.bind(getMultiblockState().getMatchContext(), this, this::onDrawerBusChanged);
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        drawerBuses.unbind();
        drawers.clear();
        itemPerTypeCapacity = 0;
        fluidPerTypeCapacity = 0;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        drawerBuses.unbind();
    }

    @Override
    public void onMachineChanged() {
        onChanged();
        if (isRemote() || !isFormed) return;
        updatePerTypeCapacity();
        refreshHatches();
    }

    private void onDrawerBusChanged() {
        if (isRemote() || !isFormed) return;
        long oldItemCapacity = itemPerTypeCapacity;
        long oldFluidCapacity = fluidPerTypeCapacity;
        int oldItemTypes = getItemTypes();
        int oldFluidTypes = getFluidTypes();
        long oldDrawers = drawers.getDrawerCount();
        refreshCapacity();
        if (oldItemCapacity == itemPerTypeCapacity && oldFluidCapacity == fluidPerTypeCapacity &&
                oldItemTypes == getItemTypes() && oldFluidTypes == getFluidTypes() && oldDrawers == drawers.getDrawerCount())
            return;
        refreshHatches();
        onChanged();
    }

    /// 容量变了，让结构里的保险库仓重新读一遍
    private void refreshHatches() {
        for (var part : getParts(MEStorageHatch.class)) {
            part.refreshStorageBinding();
        }
    }

    // ==================== 界面 ====================

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return isFormed && IFancyUIMachine.super.shouldOpenUI(player, hand, hit);
    }

    /// 主机只保留一个升级槽，每槽限放一个。
    @Override
    public UIElement createUIWidget() {
        var controls = ControlPanel.of(this);
        addStorageSlot(controls);
        controls.addToggle(OVERFLOW_LABEL, () -> voidOverflow, value -> voidOverflow = value, OVERFLOW_TOOLTIP);
        return MachineDisplay.page(this).addChild(controls.build());
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return MachineWindow.createUI(this, this, entityPlayer);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        IDisplayUIMachine.super.addDisplayText(textList);
        textList.add(Component.translatable(ITEM_DRAWERS, FormattingUtil.formatNumbers(getItemTypes()),
                formatCapacity(false, itemPerTypeCapacity)).withStyle(ChatFormatting.GRAY));
        addTypeUsage(textList, usedItemTypes, getItemTypes());
        textList.add(Component.translatable(FLUID_DRAWERS, FormattingUtil.formatNumbers(getFluidTypes()),
                formatCapacity(true, fluidPerTypeCapacity)).withStyle(ChatFormatting.GRAY));
        addTypeUsage(textList, usedFluidTypes, getFluidTypes());
    }

    private static void addTypeUsage(List<Component> textList, int used, int types) {
        textList.add(Component.translatable(TYPES,
                FormattingUtil.formatNumbers(used),
                FormattingUtil.formatNumbers(types)).withStyle(used > types ? ChatFormatting.RED : ChatFormatting.GRAY));
    }

    private static String formatCapacity(boolean fluid, long capacity) {
        return fluid ? AEKeyTypes.FLUIDS.formatAmount(capacity, AmountFormat.FULL) : FormattingUtil.formatNumbers(capacity);
    }

    @Override
    public void appendWailaData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        boolean fluidKind = getItemTypes() == 0 && getFluidTypes() > 0;
        compoundTag.putInt("types", Keys.saturatedInt((long) getItemTypes() + getFluidTypes()));
        compoundTag.putInt("usedTypes", getKeyMap().size());
        compoundTag.putLong("perTypeCapacity", fluidKind ? fluidPerTypeCapacity : itemPerTypeCapacity);
        compoundTag.putLong("drawerCount", drawers.getDrawerCount());
        compoundTag.putInt("drawerSlots", 1);
        compoundTag.putLong("upgradeCount", getStorageStack().isEmpty() ? 0 : 1);
        compoundTag.putDouble("upgradeMultiplier", getUpgradeMultiplier());
        compoundTag.putDouble("controllerMultiplier", 1);
        compoundTag.putInt("itemTypes", getItemTypes());
        compoundTag.putInt("fluidTypes", getFluidTypes());
        compoundTag.putInt("usedItemTypes", usedItemTypes);
        compoundTag.putInt("usedFluidTypes", usedFluidTypes);
        compoundTag.putLong("itemPerTypeCapacity", itemPerTypeCapacity);
        compoundTag.putLong("fluidPerTypeCapacity", fluidPerTypeCapacity);
        compoundTag.putInt("hermeticLevel", hermeticLevel);
        compoundTag.putBoolean("fluidKind", fluidKind);
        compoundTag.putBoolean("voidOverflow", voidOverflow);
    }

    @Override
    public void appendWailaTooltip(CompoundTag compoundTag, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        int usedTypes = compoundTag.getInt("usedTypes");
        int typeLimit = compoundTag.getInt("types");
        var typeLine = Component.translatable(TYPES, FormattingUtil.formatNumbers(usedTypes), FormattingUtil.formatNumbers(typeLimit));
        iTooltip.add(usedTypes > typeLimit ? typeLine.withStyle(ChatFormatting.RED) : typeLine);
        boolean fluid = compoundTag.getBoolean("fluidKind");
        iTooltip.add(Component.translatable(fluid ? FLUID_DRAWERS : ITEM_DRAWERS,
                FormattingUtil.formatNumbers(compoundTag.getInt("types")),
                formatCapacity(fluid, compoundTag.getLong("perTypeCapacity"))));
        long drawerCount = compoundTag.getLong("drawerCount");
        int drawerSlots = Math.max(1, compoundTag.getInt("drawerSlots"));
        iTooltip.add(Component.translatable(STORAGE_COUNT,
                FormattingUtil.formatNumbers(drawerCount),
                FormattingUtil.formatNumbers(drawerSlots), NumberUtils.formatDouble(Math.sqrt(drawerCount))));
        iTooltip.add(Component.translatable(UPGRADES, FormattingUtil.formatNumbers(compoundTag.getLong("upgradeCount")),
                FormattingUtil.formatNumbers(drawerCount), NumberUtils.formatDouble(compoundTag.getDouble("upgradeMultiplier"))));
        iTooltip.add(Component.translatable(BONUS,
                NumberUtils.formatDouble(compoundTag.getDouble("controllerMultiplier")),
                FormattingUtil.formatNumbers(compoundTag.getInt("hermeticLevel")),
                NumberUtils.formatDouble(Math.sqrt(Math.max(0, compoundTag.getInt("hermeticLevel")) *
                        Math.max(0, compoundTag.getDouble("controllerMultiplier"))))));
        iTooltip.add(Component.translatable(OVERFLOW,
                Component.translatable(compoundTag.getBoolean("voidOverflow") ? "gtocore.machine.on" : "gtocore.machine.off")));
    }
}

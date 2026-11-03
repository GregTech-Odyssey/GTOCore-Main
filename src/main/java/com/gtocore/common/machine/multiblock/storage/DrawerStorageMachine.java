package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.MEStorageHatch;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.item.DrawerStorageUpgradeInventory;
import com.gtolib.api.item.DrawerStorageUpgradeItem;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.api.machine.storage.DrawerStorageCapacity;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
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
 * 抽屉存储器：主机提供两个升级槽，输入总线中的超级箱和超级缸分别贡献物品与流体存储。
 * <ul>
 * <li>物品与流体的可存种类数分别累计对应箱缸的格数 × 数量（普通=1、1x2=2、2x2=4）；</li>
 * <li>每种类容量 = 对应类型箱缸的总容量 ÷ 总格数 × 密封等级 × 升级倍率。</li>
 * <li>聚合升级使每个总线槽仅按一件箱缸累计格数，总容量仍按堆叠数量累计；多个升级的倍率相乘。</li>
 * <li>显示窗里可以开关「溢出销毁」：开启后，超过每种类容量或种类已满时放不下的内容会被直接销毁，
 * 调用方不会把放不下的部分收回去。</li>
 * </ul>
 */
@DataGeneratorScanned
public final class DrawerStorageMachine extends MultiblockMEStorageMachine implements IStorageMultiblock, IFancyUIMachine, IDisplayUIMachine {

    @RegisterLanguage(cn = "存储升级", en = "Storage Upgrade")
    private static final Component SLOT_LABEL = Component.translatable("gtocore.machine.drawer_storage.slot");

    @RegisterLanguage(cn = "种类：%s / %s", en = "Types: %s / %s")
    public static final Component TYPES = Component.translatable("gtocore.machine.drawer_storage.types");
    @RegisterLanguage(cn = "密封等级：%s", en = "Hermetic level: %s")
    public static final Component BONUS = Component.translatable("gtocore.machine.drawer_storage.bonus");
    @RegisterLanguage(cn = "输入箱缸：%s 个", en = "Input storage units: %s")
    public static final Component STORAGE_COUNT = Component.translatable("gtocore.machine.drawer_storage.storage_count");
    @RegisterLanguage(cn = "升级 %s / 2 个：容量倍率 ×%s", en = "Upgrades: %s / 2, capacity multiplier x%s")
    public static final Component UPGRADES = Component.translatable("gtocore.machine.drawer_storage.upgrades");
    @RegisterLanguage(cn = "聚合：%s", en = "Aggregation: %s")
    private static final Component AGGREGATION = Component.translatable("gtocore.machine.drawer_storage.aggregation");
    @RegisterLanguage(cn = "超级箱 %s 种：每种类容量 %s", en = "Super chests %s types: %s per type")
    public static final Component ITEM_DRAWERS = Component.translatable("gtocore.machine.drawer_storage.item_drawers");
    @RegisterLanguage(cn = "超级缸 %s 种：每种类容量 %s", en = "Super tanks %s types: %s per type")
    public static final Component FLUID_DRAWERS = Component.translatable("gtocore.machine.drawer_storage.fluid_drawers");
    @RegisterLanguage(cn = "溢出销毁：%s", en = "Overflow voiding: %s")
    public static final Component OVERFLOW = Component.translatable("gtocore.machine.drawer_storage.overflow");
    @RegisterLanguage(cn = "开启后，存入溢出时，多余的量会被直接销毁", en = "When enabled, anything that does not fit (over the per-type capacity, or no free type for a new key) is voided instead of being returned")
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
        machineStorage = new NotifiableStackInventory(this, new DrawerStorageUpgradeInventory(), IO.NONE, IO.BOTH);
        machineStorage.addChangedListener(this::onMachineChanged);
        machineStorage.setCapabilityValidator(GTUtil.NEGATIVE);
    }

    @Override
    public boolean storageFilter(ItemStack stack) {
        return stack.getItem() instanceof DrawerStorageUpgradeItem;
    }

    @Override
    public int getSlotLimit() {
        return 1;
    }

    public int getUpgradeMultiplier() {
        return upgrades().getCapacityMultiplier();
    }

    private DrawerStorageUpgradeInventory upgrades() {
        return (DrawerStorageUpgradeInventory) machineStorage.storage;
    }

    /// 不设总容量：只按种类卡每一种的上限
    @Override
    protected long computeCapacity() {
        refreshDrawers();
        return Long.MAX_VALUE;
    }

    /// 输入总线或主机升级变化时，重新累计箱缸的种类数与容量。
    private void refreshDrawers() {
        hermeticLevel = getMultiblockState().getMatchContext().getOrDefault(GTORecipeDataKeys.HERMETIC_CASING_TIER, 0) + 1;
        drawers.clear();
        boolean aggregation = upgrades().isAggregationEnabled();
        for (var bus : drawerBuses.buses()) {
            var inv = bus.storage;
            for (int i = 0, slots = inv.size(); i < slots; i++) {
                var key = inv.keyAt(i);
                if (key == null) continue;
                long count = inv.amountAt(i);
                if (count < 1) continue;
                var storage = DrawerStorageCapacity.get(key.getItem());
                if (storage != null) drawers.add(storage, count, aggregation);
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
        refreshCapacity();
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

    @Override
    public Component getStorageSlotLabel() {
        return SLOT_LABEL;
    }

    @Override
    public void addStorageSlot(ControlPanel controls) {
        var storage = machineStorage.storage;
        for (int slot = 0; slot < storage.size(); slot++) {
            controls.addSlot(ItemSlot.of(storage, slot), SLOT_LABEL, ControlPanel.contentName(storage, slot));
        }
    }

    /// 主机提供两个升级槽，每槽限放一个。
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
        textList.add(Component.translatable(((TranslatableContents) ITEM_DRAWERS.getContents()).getKey(), FormattingUtil.formatNumbers(getItemTypes()),
                formatCapacity(false, itemPerTypeCapacity)).withStyle(ChatFormatting.GRAY));
        textList.add(typeUsage(usedItemTypes, getItemTypes()));
        textList.add(Component.translatable(((TranslatableContents) FLUID_DRAWERS.getContents()).getKey(), FormattingUtil.formatNumbers(getFluidTypes()),
                formatCapacity(true, fluidPerTypeCapacity)).withStyle(ChatFormatting.GRAY));
        textList.add(typeUsage(usedFluidTypes, getFluidTypes()));
        textList.add(Component.translatable(((TranslatableContents) UPGRADES.getContents()).getKey(), upgrades().getUpgradeCount(), getUpgradeMultiplier()).withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable(((TranslatableContents) AGGREGATION.getContents()).getKey(),
                Component.translatable(upgrades().isAggregationEnabled() ? "gtocore.machine.on" : "gtocore.machine.off"))
                .withStyle(ChatFormatting.GRAY));
    }

    private static Component typeUsage(int used, int types) {
        return Component.translatable(((TranslatableContents) TYPES.getContents()).getKey(),
                FormattingUtil.formatNumbers(used),
                FormattingUtil.formatNumbers(types)).withStyle(used > types ? ChatFormatting.RED : ChatFormatting.GRAY);
    }

    private static String formatCapacity(boolean fluid, long capacity) {
        return fluid ? AEKeyTypes.FLUIDS.formatAmount(capacity, AmountFormat.FULL) : FormattingUtil.formatNumbers(capacity);
    }

    @Override
    public void appendWailaData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        compoundTag.putLong("drawerCount", drawers.getDrawerCount());
        compoundTag.putInt("upgradeCount", upgrades().getUpgradeCount());
        compoundTag.putInt("upgradeMultiplier", getUpgradeMultiplier());
        compoundTag.putBoolean("aggregation", upgrades().isAggregationEnabled());
        compoundTag.putInt("itemTypes", getItemTypes());
        compoundTag.putInt("fluidTypes", getFluidTypes());
        compoundTag.putInt("usedItemTypes", usedItemTypes);
        compoundTag.putInt("usedFluidTypes", usedFluidTypes);
        compoundTag.putLong("itemPerTypeCapacity", itemPerTypeCapacity);
        compoundTag.putLong("fluidPerTypeCapacity", fluidPerTypeCapacity);
        compoundTag.putInt("hermeticLevel", hermeticLevel);
        compoundTag.putBoolean("voidOverflow", voidOverflow);
    }

    @Override
    public void appendWailaTooltip(CompoundTag compoundTag, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.add(Component.translatable(((TranslatableContents) ITEM_DRAWERS.getContents()).getKey(),
                FormattingUtil.formatNumbers(compoundTag.getInt("itemTypes")),
                formatCapacity(false, compoundTag.getLong("itemPerTypeCapacity"))));
        iTooltip.add(typeUsage(compoundTag.getInt("usedItemTypes"), compoundTag.getInt("itemTypes")));
        iTooltip.add(Component.translatable(((TranslatableContents) FLUID_DRAWERS.getContents()).getKey(),
                FormattingUtil.formatNumbers(compoundTag.getInt("fluidTypes")),
                formatCapacity(true, compoundTag.getLong("fluidPerTypeCapacity"))));
        iTooltip.add(typeUsage(compoundTag.getInt("usedFluidTypes"), compoundTag.getInt("fluidTypes")));
        iTooltip.add(Component.translatable(((TranslatableContents) STORAGE_COUNT.getContents()).getKey(),
                FormattingUtil.formatNumbers(compoundTag.getLong("drawerCount"))));
        iTooltip.add(Component.translatable(((TranslatableContents) UPGRADES.getContents()).getKey(), FormattingUtil.formatNumbers(compoundTag.getInt("upgradeCount")),
                FormattingUtil.formatNumbers(compoundTag.getInt("upgradeMultiplier"))));
        iTooltip.add(Component.translatable(((TranslatableContents) AGGREGATION.getContents()).getKey(),
                Component.translatable(compoundTag.getBoolean("aggregation") ? "gtocore.machine.on" : "gtocore.machine.off")));
        iTooltip.add(Component.translatable(((TranslatableContents) BONUS.getContents()).getKey(), FormattingUtil.formatNumbers(compoundTag.getInt("hermeticLevel"))));
        iTooltip.add(Component.translatable(((TranslatableContents) OVERFLOW.getContents()).getKey(),
                Component.translatable(compoundTag.getBoolean("voidOverflow") ? "gtocore.machine.on" : "gtocore.machine.off")));
    }
}

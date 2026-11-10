package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.MEStorageHatch;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.storage.DrawerStorageCapacity;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
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

/** Super Storage Integrator: independent type limits. */
@DataGeneratorScanned
public final class DrawerStorageMachine extends MultiblockMEStorageMachine implements IFancyUIMachine, IDisplayUIMachine {

    @RegisterLanguage(cn = "物品种类上限", en = "Item Type Limit")
    private static final Component ITEM_LIMIT = Component.translatable("gtocore.machine.drawer_storage.item_limit");
    @RegisterLanguage(cn = "流体种类上限", en = "Fluid Type Limit")
    private static final Component FLUID_LIMIT = Component.translatable("gtocore.machine.drawer_storage.fluid_limit");
    @RegisterLanguage(cn = "最多为箱缸提供的格数之和；种类越少每种类容量越大，不能低于已存种类数", en = "Up to the total compartments of the containers; fewer types give more capacity per type, but never fewer than the stored types")
    private static final Component TYPE_LIMIT_TIP = Component.translatable("gtocore.machine.drawer_storage.type_limit_tip");
    @RegisterLanguage(cn = "可设置种类：物品 %s，流体 %s", en = "Maximum configurable types: %s items, %s fluids")
    private static final Component MAX_TYPES = Component.translatable("gtocore.machine.drawer_storage.max_types");

    @RegisterLanguage(cn = "种类：%s / %s", en = "Types: %s / %s")
    public static final Component TYPES = Component.translatable("gtocore.machine.drawer_storage.types");
    @RegisterLanguage(cn = "密封等级：%s", en = "Hermetic level: %s")
    public static final Component BONUS = Component.translatable("gtocore.machine.drawer_storage.bonus");
    @RegisterLanguage(cn = "输入箱缸：%s 个", en = "Input storage units: %s")
    public static final Component STORAGE_COUNT = Component.translatable("gtocore.machine.drawer_storage.storage_count");
    @RegisterLanguage(cn = "物品上限 %s 种：每种容量 %s", en = "Items: limit %s types, %s per type")
    public static final Component ITEM_DRAWERS = Component.translatable("gtocore.machine.drawer_storage.item_drawers");
    @RegisterLanguage(cn = "流体上限 %s 种：每种容量 %s", en = "Fluids: limit %s types, %s per type")
    public static final Component FLUID_DRAWERS = Component.translatable("gtocore.machine.drawer_storage.fluid_drawers");
    @RegisterLanguage(cn = "溢出销毁：%s", en = "Overflow voiding: %s")
    public static final Component OVERFLOW = Component.translatable("gtocore.machine.drawer_storage.overflow");
    @RegisterLanguage(cn = "开启后，已有种类存满时多出的量会被直接销毁；种类已满时仍不接收新种类", en = "When enabled, amounts over the per-type capacity of stored types are voided; new types are still rejected when no type is free")
    public static final Component OVERFLOW_TOOLTIP = Component.translatable("gtocore.machine.drawer_storage.overflow.tooltip");
    @RegisterLanguage(cn = "溢出销毁", en = "Overflow Voiding")
    private static final Component OVERFLOW_LABEL = Component.translatable("gtocore.machine.drawer_storage.overflow.label");

    @SaveToDisk(defaultValue = "0")
    private int itemTypeLimit;
    @SaveToDisk(defaultValue = "0")
    private int fluidTypeLimit;
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
    }

    public void setItemTypeLimit(int types) {
        itemTypeLimit = typeLimit(types, usedItemTypes, drawers.getItemTypes());
        onTypeLimitChanged();
    }

    public void setFluidTypeLimit(int types) {
        fluidTypeLimit = typeLimit(types, usedFluidTypes, drawers.getFluidTypes());
        onTypeLimitChanged();
    }

    private static int typeLimit(int requested, int used, int max) {
        return requested >= max ? 0 : Math.max(Math.max(1, used), requested);
    }

    private static int effectiveTypes(int limit, int max) {
        return limit > 0 ? Math.min(limit, max) : max;
    }

    private void onTypeLimitChanged() {
        updatePerTypeCapacity();
        refreshHatches();
        onChanged();
    }

    /// 不设总容量：只按种类卡每一种的上限
    @Override
    protected long computeCapacity() {
        refreshDrawers();
        return Long.MAX_VALUE;
    }

    /// 输入总线变化时，重新累计各等级箱缸数量。
    private void refreshDrawers() {
        hermeticLevel = getAssembly().getOrDefault(GTORecipeDataKeys.HERMETIC_CASING_TIER, 0) + 1;
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
        itemPerTypeCapacity = drawers.capacityOf(false, hermeticLevel, getItemTypes());
        fluidPerTypeCapacity = drawers.capacityOf(true, hermeticLevel, getFluidTypes());
    }

    public int getItemTypes() {
        return effectiveTypes(itemTypeLimit, drawers.getItemTypes());
    }

    public int getFluidTypes() {
        return effectiveTypes(fluidTypeLimit, drawers.getFluidTypes());
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
        if (tag.contains("itemTypeLimit")) itemTypeLimit = tag.getInt("itemTypeLimit");
        if (tag.contains("fluidTypeLimit")) fluidTypeLimit = tag.getInt("fluidTypeLimit");
        voidOverflow = tag.getBoolean("voidOverflow");
        recountUsedTypes();
    }

    @Override
    public boolean saveBreak() {
        return super.saveBreak() || itemTypeLimit != 0 || fluidTypeLimit != 0 || voidOverflow;
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        super.saveToItem(tag);
        if (itemTypeLimit != 0) tag.putInt("itemTypeLimit", itemTypeLimit);
        if (fluidTypeLimit != 0) tag.putInt("fluidTypeLimit", fluidTypeLimit);
        if (voidOverflow) tag.putBoolean("voidOverflow", true);
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
        if (stored == 0 && used >= types) return 0;
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
    public void onStructureFormed() {
        // 箱缸总线要先挂上，容量里要用
        drawerBuses.bind(getAssembly(), this, this::onDrawerBusChanged);
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
    public UIElement createUIWidget() {
        var controls = ControlPanel.of(this);
        controls.addInt(ITEM_LIMIT, this::getItemTypes, this::setItemTypeLimit, () -> Math.min(Math.max(1, usedItemTypes), drawers.getItemTypes()), drawers::getItemTypes, TYPE_LIMIT_TIP);
        controls.addInt(FLUID_LIMIT, this::getFluidTypes, this::setFluidTypeLimit, () -> Math.min(Math.max(1, usedFluidTypes), drawers.getFluidTypes()), drawers::getFluidTypes, TYPE_LIMIT_TIP);
        controls.addToggle(OVERFLOW_LABEL, () -> voidOverflow, value -> voidOverflow = value, OVERFLOW_TOOLTIP);
        return Form.page().addChildren(MachineDisplay.page(this), controls.build());
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
        textList.add(Component.translatable(((TranslatableContents) MAX_TYPES.getContents()).getKey(),
                FormattingUtil.formatNumbers(drawers.getItemTypes()), FormattingUtil.formatNumbers(drawers.getFluidTypes())).withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable(((TranslatableContents) BONUS.getContents()).getKey(), hermeticLevel).withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable(((TranslatableContents) STORAGE_COUNT.getContents()).getKey(),
                FormattingUtil.formatNumbers(drawers.getDrawerCount())).withStyle(ChatFormatting.GRAY));
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
        iTooltip.add(Component.translatable(((TranslatableContents) BONUS.getContents()).getKey(), FormattingUtil.formatNumbers(compoundTag.getInt("hermeticLevel"))));
        iTooltip.add(Component.translatable(((TranslatableContents) OVERFLOW.getContents()).getKey(),
                Component.translatable(compoundTag.getBoolean("voidOverflow") ? "gtocore.machine.on" : "gtocore.machine.off")));
    }
}

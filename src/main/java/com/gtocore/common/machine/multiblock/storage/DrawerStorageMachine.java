package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.MEStorageHatch;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.gtolib.utils.FluidUtils;
import com.gtolib.utils.ItemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
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
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@DataGeneratorScanned
public final class DrawerStorageMachine extends MultiblockMEStorageMachine implements IUIMachine {

    @RegisterLanguage(cn = "种类：%s / %s", en = "Types: %s / %s")
    public static final String TYPES = "gtocore.machine.drawer_storage.types";
    @RegisterLanguage(cn = "每种类容量：%s", en = "Capacity per type: %s")
    public static final String CAPACITY_PER_TYPE = "gtocore.machine.drawer_storage.capacity_per_type";
    @RegisterLanguage(cn = "密封等级 %s × 升级倍率 %s", en = "Hermetic tier %s x upgrade multiplier %s")
    public static final String HERMETIC_UPGRADE = "gtocore.machine.drawer_storage.hermetic_upgrade";
    @RegisterLanguage(cn = "暂无存储内容", en = "Nothing stored")
    public static final String EMPTY = "gtocore.machine.drawer_storage.empty";

    private static final int WINDOW_WIDTH = 176;
    private static final int WINDOW_HEIGHT = 244;
    private static final int INVENTORY_Y = 162;

    @Getter
    private int types;
    @Getter
    private long perTypeCapacity;
    @Getter
    private int upgradeMultiplier = 1;
    @Getter
    private int hermeticLevel = 1;

    private final StorageBusListener drawerBuses = new StorageBusListener();

    private final List<AEKey> displayOrder = new ArrayList<>();
    private boolean displayOrderDirty = true;

    @Getter
    private int displayVersion;

    public DrawerStorageMachine(MetaMachineBlockEntity holder) {
        super(holder, null);
    }

    @Override
    public Supplier<BlockPattern>[] getPattern() {
        return getDefinition().getPatternFactory();
    }

    @Override
    protected long computeCapacity() {
        scanDrawers();
        return Long.MAX_VALUE;
    }

    private void scanDrawers() {
        int types = 0;
        int upgrades = 0;
        long drawerCapacity = 0;
        for (var bus : drawerBuses.buses()) {
            for (int i = 0, slots = bus.getSlots(); i < slots; i++) {
                var stack = bus.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                var drawerType = drawerType(stack);
                if (drawerType != null) {
                    types += drawerType.getSlots() * stack.getCount();
                    drawerCapacity = Math.max(drawerCapacity, drawerType.getSlotAmount());
                    continue;
                }
                upgrades += upgradeMultiplier(stack) * stack.getCount();
            }
        }
        this.types = types;
        this.upgradeMultiplier = Math.max(1, upgrades);
        this.hermeticLevel = readHermeticLevel();
        this.perTypeCapacity = capacityOf(drawerCapacity, this.hermeticLevel, this.upgradeMultiplier);
        displayVersion++;
    }

    @Nullable
    public AEKey displayKeyAt(int index) {
        if (displayOrderDirty) {
            displayOrderDirty = false;
            displayOrder.clear();
            for (var entry : keyMap) {
                displayOrder.add(entry.getKey());
            }
            displayOrder.sort(Comparator.comparing(DrawerStorageMachine::displaySortKey));
        }
        return index >= 0 && index < displayOrder.size() ? displayOrder.get(index) : null;
    }

    private static String displaySortKey(AEKey key) {
        if (key instanceof AEItemKey itemKey) return ItemUtils.getId(itemKey.getItem());
        if (key instanceof AEFluidKey fluidKey) return FluidUtils.getId(fluidKey.getFluid());
        return key.toString();
    }

    @Nullable
    private static FunctionalStorage.DrawerType drawerType(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        var block = blockItem.getBlock();
        if (block instanceof DrawerBlock drawer) return drawer.getType();
        if (block instanceof FluidDrawerBlock drawer) return drawer.getType();
        return null;
    }

    private static int upgradeMultiplier(ItemStack stack) {
        return stack.getItem() instanceof StorageUpgradeItem upgrade ? upgrade.getStorageMultiplier() : 0;
    }

    private int readHermeticLevel() {
        return getMultiblockState().getMatchContext().getOrDefault(GTORecipeDataKeys.HERMETIC_CASING_TIER, 0) + 1;
    }

    private static long capacityOf(long drawerCapacity, int hermeticLevel, int upgradeMultiplier) {
        long capacity = Math.max(1, drawerCapacity);
        capacity = multiply(capacity, upgradeMultiplier);
        return multiply(capacity, hermeticLevel);
    }

    private static long multiply(long a, long b) {
        return a > Long.MAX_VALUE / b ? Long.MAX_VALUE : a * b;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!isFormed || amount < 1 || types < 1 || perTypeCapacity < 1) return 0;
        var map = keyMap;
        var size = map.size();
        if (mode == Actionable.SIMULATE) {
            var stored = map.getAmount(what);
            if (stored == 0 && size >= types) return 0;
            return Math.min(amount, perTypeCapacity - stored);
        }
        if (size > types) return 0;
        long inserted = map.insert(what, amount, perTypeCapacity);
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
        displayOrderDirty = true;
        displayVersion++;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (!isFormed || types < 1 || perTypeCapacity < 1) return false;
        var map = getKeyMap();
        long stored = map.getAmount(what);
        return stored < perTypeCapacity && (stored > 0 || map.size() < types);
    }

    @Override
    public void onStructureFormed() {
        bindDrawerBuses();
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        unbindDrawerBuses();
        types = 0;
        perTypeCapacity = 0;
        upgradeMultiplier = 1;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unbindDrawerBuses();
    }

    private void bindDrawerBuses() {
        drawerBuses.bind(getMultiblockState().getMatchContext(), this, this::onDrawerBusChanged);
    }

    private void unbindDrawerBuses() {
        drawerBuses.unbind();
    }

    private void onDrawerBusChanged() {
        if (isRemote() || !isFormed) return;
        int oldTypes = types;
        int oldUpgrade = upgradeMultiplier;
        long oldCapacity = perTypeCapacity;
        refreshCapacity();
        if (oldTypes == types && oldUpgrade == upgradeMultiplier && oldCapacity == perTypeCapacity) return;
        for (var part : getParts()) {
            if (part.self() instanceof MEStorageHatch hatch) hatch.refreshStorageBinding();
        }
        onChanged();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return isFormed && IUIMachine.super.shouldOpenUI(player, hand, hit);
    }

    @Override
    public ModularUI createUI(Player player) {
        return new ModularUI(WINDOW_WIDTH, WINDOW_HEIGHT, this, player)
                .background(GuiTextures.BACKGROUND)
                .widget(DrawerStorageUI.create(this))
                .widget(UITemplate.bindPlayerInventory(player.getInventory(), GuiTextures.SLOT, DrawerStorageUI.X, INVENTORY_Y, true));
    }

    @Override
    public void appendWailaData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        compoundTag.putInt("types", types);
        compoundTag.putInt("usedTypes", getKeyMap().size());
        compoundTag.putLong("perTypeCapacity", perTypeCapacity);
        compoundTag.putInt("hermeticLevel", hermeticLevel);
        compoundTag.putInt("upgradeMultiplier", upgradeMultiplier);
    }

    @Override
    public void appendWailaTooltip(CompoundTag compoundTag, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.add(Component.translatable(TYPES,
                FormattingUtil.formatNumbers(compoundTag.getInt("usedTypes")),
                FormattingUtil.formatNumbers(compoundTag.getInt("types"))));
        iTooltip.add(Component.translatable(CAPACITY_PER_TYPE,
                FormattingUtil.formatNumbers(compoundTag.getLong("perTypeCapacity"))));
        iTooltip.add(Component.translatable(HERMETIC_UPGRADE,
                FormattingUtil.formatNumbers(compoundTag.getInt("hermeticLevel")),
                FormattingUtil.formatNumbers(compoundTag.getInt("upgradeMultiplier"))));
    }
}

package com.gtocore.common.machine.multiblock.part;

import com.gtocore.api.electrochemistry.IonDefinition;
import com.gtocore.api.electrochemistry.IonMembrane;
import com.gtocore.api.electrochemistry.MembraneType;

import com.gtolib.api.machine.part.ItemPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.LogicalSide;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ValueOps;
import com.gto.fastcollection.map.enums.Enum2IntMap;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.Arrays;
import java.util.function.Predicate;

public class SemipermeableMembraneHatch extends ItemPartMachine {

    private static final MembraneType[] MEMBRANE_TYPES = MembraneType.values();
    private final Enum2IntMap<MembraneType> membranesByType = new Enum2IntMap<>(MembraneType.class);
    private int firstMembraneSlot = -1;

    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean locked;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int selectedSlot;
    @SaveToDisk
    private final long[] reservedDamage = new long[membraneSlots()];

    public SemipermeableMembraneHatch(MetaMachineBlockEntity holder) {
        super(holder, 1, stack -> IonMembrane.of(stack) != null);
    }

    protected int membraneSlots() {
        return 1;
    }

    @Override
    protected NotifiableStackInventory createMachineStorage(Predicate<ItemStack> filter) {
        var storage = new StackInventory(membraneSlots()) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public int extract(int slot, ItemStack existing, int amount, boolean simulate) {
                return locked ? 0 : super.extract(slot, existing, amount, simulate);
            }

            @Override
            public void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                // Cross-recipe searches can select another membrane before deferred listeners run.
                rebuildMembraneMap();
            }

            @Override
            public void readBuffer(LogicalSide side, FriendlyByteBuf data) {
                super.readBuffer(side, data);
                rebuildMembraneMap();
            }

            @Override
            public void readValue(Object data, ValueOps ops) {
                super.readValue(data, ops);
                rebuildMembraneMap();
            }
        };
        var result = new NotifiableStackInventory(this, storage, IO.NONE, IO.BOTH);
        storage.setFilter(stack -> !locked && filter.test(stack));
        result.addChangedListener(this::onMachineChanged);
        return result;
    }

    @Override
    public Widget createUIWidget() {
        int slots = inventory.storage.getSlots();
        int columns = slots == 1 ? 1 : 3;
        int rows = (slots + columns - 1) / columns;
        var group = new WidgetGroup(0, 0, columns * 18 + 16, rows * 18 + 16);
        for (int slot = 0; slot < slots; slot++) {
            var membraneSlot = ItemSlot.of(inventory.storage, slot).disabled(() -> locked, null);
            membraneSlot.setSelfPosition(8 + slot % columns * 18, 8 + slot / columns * 18);
            group.addWidget(membraneSlot);
        }
        return group;
    }

    public void setLocked(boolean locked) {
        if (this.locked == locked) return;
        this.locked = locked;
        if (!locked) {
            Arrays.fill(reservedDamage, 0);
            rebuildMembraneMap();
        }
        onChanged();
        requestSync();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        setLocked(false);
    }

    public ItemStack membrane() {
        int slot = activeSlot();
        return slot < 0 ? ItemStack.EMPTY : inventory.storage.getStackInSlot(slot);
    }

    public ItemStack membrane(MembraneType type) {
        int slot = membranesByType.getInt(type) - 1;
        return slot < 0 ? ItemStack.EMPTY : inventory.storage.getStackInSlot(slot);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        rebuildMembraneMap();
    }

    private int activeSlot() {
        if (!inventory.storage.getStackInSlot(selectedSlot).isEmpty()) return selectedSlot;
        return firstMembraneSlot;
    }

    private void rebuildMembraneMap() {
        membranesByType.clear();
        firstMembraneSlot = -1;
        for (int slot = 0; slot < inventory.storage.getSlots(); slot++) {
            var type = IonMembrane.typeOf(inventory.storage.getStackInSlot(slot));
            if (type == null) continue;
            if (firstMembraneSlot < 0) firstMembraneSlot = slot;
            int previous = membranesByType.getInt(type) - 1;
            if (previous < 0 || (remainingUses(previous) <= 0 && remainingUses(slot) > 0)) {
                // Enum2IntMap reserves zero for absent keys.
                membranesByType.put(type, slot + 1);
            }
        }
    }

    public boolean selectMembrane(MembraneType type) {
        int slot = membranesByType.getInt(type) - 1;
        if (slot < 0 || remainingUses(slot) <= 0) return false;
        selectSlot(slot);
        return true;
    }

    private void selectSlot(int slot) {
        if (selectedSlot == slot) return;
        selectedSlot = slot;
        onChanged();
        requestSync();
    }

    public long remainingUses() {
        int slot = activeSlot();
        return slot < 0 ? 0 : remainingUses(slot);
    }

    public boolean hasAvailableUses() {
        for (var type : MEMBRANE_TYPES) {
            int slot = membranesByType.getInt(type) - 1;
            if (slot >= 0 && remainingUses(slot) > 0) return true;
        }
        return false;
    }

    private long remainingUses(int slot) {
        var stack = inventory.storage.getStackInSlot(slot);
        return stack.isEmpty() ? 0 : stack.getMaxDamage() - stack.getDamageValue() - reservedDamage[slot];
    }

    public void reserveUses(long amount) {
        int slot = activeSlot();
        reservedDamage[slot] += amount;
        if (remainingUses(slot) <= 0) rebuildMembraneMap();
        onChanged();
    }

    public boolean hasReservedUses() {
        for (long amount : reservedDamage) {
            if (amount > 0) return true;
        }
        return false;
    }

    public long durabilityCost(long parallels, boolean discharge) {
        return discharge ? parallels : (long) Math.ceil(Math.sqrt(parallels));
    }

    public long parallelLimit(long durability) {
        if (durability <= 0) return 0;
        return durability > ParallelLogic.MAX_PARALLEL / durability ? ParallelLogic.MAX_PARALLEL : durability * durability;
    }

    public boolean permits(IonDefinition ion) {
        return IonMembrane.permits(membrane(), ion);
    }

    public void consumeReservedUses() {
        for (int slot = 0; slot < reservedDamage.length; slot++) {
            long amount = reservedDamage[slot];
            if (amount <= 0) continue;
            reservedDamage[slot] = 0;
            damageSlot(slot, amount);
        }
    }

    public void consumeUses(long amount) {
        damageSlot(activeSlot(), amount);
    }

    private void damageSlot(int slot, long amount) {
        var stack = inventory.storage.getStackInSlot(slot);
        long damage = stack.getDamageValue() + amount;
        if (damage >= stack.getMaxDamage()) inventory.storage.setStackInSlot(slot, ItemStack.EMPTY);
        else {
            var damaged = stack.copy();
            damaged.setDamageValue((int) damage);
            inventory.storage.setStackInSlot(slot, damaged);
        }
    }

    @Override
    protected void onMachineChanged() {
        for (var controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine machine) {
                machine.getRecipeLogic().markLastRecipeDirty();
                machine.getRecipeLogic().updateTickSubscription();
            }
        }
    }
}

package com.gtocore.common.machine.multiblock.part;

import com.gtocore.api.electrochemistry.IonDefinition;
import com.gtocore.api.electrochemistry.IonMembrane;

import com.gtolib.api.item.MachineItemStackHandler;
import com.gtolib.api.machine.part.ItemPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;

import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import java.util.function.Predicate;

public final class SemipermeableMembraneHatch extends ItemPartMachine {

    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean locked;

    public SemipermeableMembraneHatch(MetaMachineBlockEntity holder) {
        super(holder, 1, stack -> IonMembrane.of(stack) != null);
    }

    @Override
    protected NotifiableStackInventory createMachineStorage(Predicate<ItemStack> filter) {
        var storage = new MachineItemStackHandler(this::getSlotLimit) {

            @Override
            public int extract(int slot, ItemStack existing, int amount, boolean simulate) {
                return locked ? 0 : super.extract(slot, existing, amount, simulate);
            }
        };
        var result = new NotifiableStackInventory(this, storage, IO.NONE, IO.BOTH);
        storage.setFilter(stack -> !locked && filter.test(stack));
        result.addChangedListener(this::onMachineChanged);
        return result;
    }

    public void setLocked(boolean locked) {
        if (this.locked == locked) return;
        this.locked = locked;
        onChanged();
        requestSync();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        setLocked(false);
    }

    public ItemStack membrane() {
        return inventory.storage.getStackInSlot(0);
    }

    public boolean permits(IonDefinition ion) {
        return IonMembrane.permits(membrane(), ion);
    }

    public void damage(long amount) {
        var stack = membrane();
        if (stack.isEmpty()) return;
        long damage = stack.getDamageValue() + amount;
        if (damage >= stack.getMaxDamage()) inventory.storage.setStackInSlot(0, ItemStack.EMPTY);
        else {
            var damaged = stack.copy();
            damaged.setDamageValue((int) damage);
            inventory.storage.setStackInSlot(0, damaged);
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

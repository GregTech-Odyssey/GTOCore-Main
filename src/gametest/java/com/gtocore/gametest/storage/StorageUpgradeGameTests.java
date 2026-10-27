package com.gtocore.gametest.storage;

import com.gtocore.common.data.SuperStorageUpgrades;

import com.gtolib.api.data.storage.CompartmentStorageRegistration;
import com.gtolib.api.data.storage.WoodenSuperStorageRegistration;
import com.gtolib.api.machine.storage.CompartmentStorageMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.StorageUpgradeItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.trait.StorageUpgrades;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import com.gto.datasynclib.LogicalSide;

/** Real definitions, saved fields, drop data and handheld tank capabilities share one capacity contract. */
@GameTestHolder("gtocore_storage_upgrades")
@PrefixGameTestTemplate(false)
public final class StorageUpgradeGameTests {

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void storageUpgrades(GameTestHelper helper) {
        helper.assertTrue(((QuantumChestMachine) create(GTMachines.CREATIVE_ITEM).getMetaMachine()).getStorageUpgrades().getItems().size() == 0, "creative chest accepts upgrades");
        helper.assertTrue(((QuantumTankMachine) create(GTMachines.CREATIVE_FLUID).getMetaMachine()).getStorageUpgrades().getItems().size() == 0, "creative tank accepts upgrades");
        checkAll(helper, CompartmentStorageRegistration.NORMAL_CHESTS);
        checkAll(helper, CompartmentStorageRegistration.NORMAL_TANKS);
        checkAll(helper, WoodenSuperStorageRegistration.CHESTS);
        checkAll(helper, WoodenSuperStorageRegistration.TANKS);
        for (int layout = 0; layout < 2; layout++) {
            checkAll(helper, CompartmentStorageRegistration.CHESTS[layout]);
            checkAll(helper, CompartmentStorageRegistration.TANKS[layout]);
            checkAll(helper, CompartmentStorageRegistration.WOODEN_CHESTS[layout]);
            checkAll(helper, CompartmentStorageRegistration.WOODEN_TANKS[layout]);
        }
        helper.succeed();
    }

    private static void checkAll(GameTestHelper helper, MachineDefinition[] definitions) {
        for (var definition : definitions) {
            if (definition == null) continue;
            try {
                check(helper, definition);
            } catch (RuntimeException | Error failure) {
                org.slf4j.LoggerFactory.getLogger(StorageUpgradeGameTests.class).error("Storage upgrade regression for {}", definition.getId(), failure);
                throw failure;
            }
        }
    }

    private static MetaMachineBlockEntity create(MachineDefinition definition) {
        return (MetaMachineBlockEntity) definition.getBlockEntityType().create(BlockPos.ZERO, definition.defaultBlockState());
    }

    private static StorageUpgrades upgrades(MetaMachine machine) {
        if (machine instanceof CompartmentStorageMachine storage) return storage.getStorageUpgrades();
        return machine instanceof QuantumChestMachine chest ? chest.getStorageUpgrades() : ((QuantumTankMachine) machine).getStorageUpgrades();
    }

    private static long maximum(MetaMachine machine) {
        if (machine instanceof CompartmentStorageMachine storage) return storage.getMax();
        return machine instanceof QuantumChestMachine chest ? chest.getMax() : ((QuantumTankMachine) machine).getMax();
    }

    private static long limit(MetaMachine machine) {
        if (machine instanceof CompartmentStorageMachine storage) return storage.getMaxAmount();
        return machine instanceof QuantumChestMachine chest ? chest.getMaxAmount() : ((QuantumTankMachine) machine).getMaxAmount();
    }

    private static void setLimit(MetaMachine machine, long amount) {
        if (machine instanceof CompartmentStorageMachine storage) storage.setMaxAmount(amount);
        else if (machine instanceof QuantumChestMachine chest) chest.setMaxAmount(amount);
        else ((QuantumTankMachine) machine).setMaxAmount(amount);
    }

    private static void sync(MetaMachineBlockEntity source, MetaMachineBlockEntity target, boolean full) {
        source.getFieldDataManager().updateFieldDirtyFlags(LogicalSide.SERVER, false);
        target.getFieldDataManager().readFromNetworkBuffer(LogicalSide.CLIENT, source.getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, full));
    }

    private static void check(GameTestHelper helper, MachineDefinition definition) {
        var holder = create(definition);
        var machine = holder.getMetaMachine();
        String id = definition.getId().toString();
        long base = maximum(machine);
        var items = upgrades(machine).getItems();
        helper.assertTrue(items.size() == 4, id + ": upgrade slot count");
        helper.assertTrue(items.insertItem(0, new ItemStack(Items.DIAMOND), false).getCount() == 1, id + ": invalid item accepted");
        StorageUpgradeItem[] types = { SuperStorageUpgrades.BRONZE.get(), SuperStorageUpgrades.STEEL.get(),
                SuperStorageUpgrades.ALUMINIUM.get(), SuperStorageUpgrades.TITANIUM.get(), SuperStorageUpgrades.TUNGSTEN_STEEL.get() };
        for (int tier = 0; tier < types.length; tier++) {
            var remainder = items.insertItem(0, new ItemStack(types[tier], 64), false);
            helper.assertTrue(remainder.getCount() == 63 && items.amountAt(0) == 1, id + ": slot must accept exactly one upgrade");
            helper.assertTrue(maximum(machine) == StorageUpgrades.multiply(base, 2 << tier), id + ": individual multiplier");
            items.extractItem(0, 1, false);
        }
        for (int slot = 0; slot < 4; slot++) items.insertItem(slot, SuperStorageUpgrades.TUNGSTEN_STEEL.asStack(), false);
        long capacity = StorageUpgrades.multiply(base, 1_048_576);
        helper.assertTrue(maximum(machine) == capacity && limit(machine) == capacity, id + ": four upgrades must multiply and saturate");
        var replica = create(definition);
        sync(holder, replica, true);
        helper.assertTrue(maximum(replica.getMetaMachine()) == capacity && limit(replica.getMetaMachine()) == capacity, id + ": full client capacity sync");
        helper.assertTrue(((IDropSaveMachine) machine).saveBreak(), id + ": empty machine loses installed upgrades");

        var tag = new CompoundTag();
        ((IDropSaveMachine) machine).saveToItem(tag);
        var placed = create(definition).getMetaMachine();
        ((IDropSaveMachine) placed).loadFromItem(tag);
        helper.assertTrue(maximum(placed) == capacity && limit(placed) == capacity, id + ": break/place capacity");
        for (int slot = 0; slot < 4; slot++) helper.assertTrue(upgrades(placed).getItems().amountAt(slot) == 1, id + ": break/place upgrade item");

        var restored = create(definition);
        restored.load(holder.saveWithoutMetadata());
        helper.assertTrue(maximum(restored.getMetaMachine()) == capacity && limit(restored.getMetaMachine()) == capacity, id + ": world save capacity");
        setLimit(machine, 123);
        sync(holder, replica, false);
        helper.assertTrue(limit(replica.getMetaMachine()) == 123, id + ": client capacity limit sync");
        var limited = create(definition);
        limited.load(holder.saveWithoutMetadata());
        helper.assertTrue(limit(limited.getMetaMachine()) == 123, id + ": custom limit world save");
        setLimit(machine, capacity);

        boolean tank = machine instanceof QuantumTankMachine || machine instanceof CompartmentStorageMachine storage && storage.isTank();
        var handler = tank ? machine.getFluidHandlerCap(null, false) : machine.getItemHandlerCap(null, false);
        @SuppressWarnings("unchecked")
        var storage = (com.gregtechceu.gtceu.api.transfer.key.IKeyHandler<AEKey>) handler;
        AEKey key = tank ? AEFluidKey.of(net.minecraft.world.level.material.Fluids.WATER) : AEItemKey.of(Items.IRON_INGOT);
        long stored = storage.slotLimit(0);
        helper.assertTrue(storage.insert(0, key, stored, false) == stored, id + ": upgraded insertion");
        for (int slot = 0; slot < 4; slot++) items.extractItem(slot, 1, false);
        sync(holder, replica, false);
        helper.assertTrue(maximum(replica.getMetaMachine()) == base, id + ": removed upgrade client sync");
        helper.assertTrue(maximum(machine) == base && storage.amountAt(0) == stored, id + ": removing upgrades loses contents");
        helper.assertTrue(storage.insert(0, key, 1, false) == 0, id + ": overfull storage accepts input");
        helper.assertTrue(storage.extract(0, key, stored, false) == stored, id + ": overfull storage cannot extract");

        if (tank && base < Integer.MAX_VALUE / 2) {
            var stack = definition.asStack();
            stack.setTag(tag.copy());
            var fluid = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(() -> new AssertionError(id + ": tank capability"));
            helper.assertTrue(fluid.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, (int) base + 1000), IFluidHandler.FluidAction.EXECUTE) == base + 1000,
                    id + ": handheld tank ignores upgrades");
        }
    }
}

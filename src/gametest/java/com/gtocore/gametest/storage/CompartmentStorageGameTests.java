package com.gtocore.gametest.storage;

import com.gtolib.api.data.CompartmentStorageRegistration;
import com.gtolib.api.data.SuperStorageRegistration;
import com.gtolib.api.data.WoodenSuperStorageRegistration;
import com.gtolib.storage.CompartmentStorageMachine;
import com.gtolib.storage.SuperChestControllerMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gtolib.storage.CompartmentStorageGeometry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import org.joml.Vector3f;

@GameTestHolder("gtocore")
@PrefixGameTestTemplate(false)
public final class CompartmentStorageGameTests {

    private static final BlockPos STORAGE = new BlockPos(1, 1, 1);
    private static final BlockPos COPY = new BlockPos(2, 1, 1);
    private static final BlockPos CONTROLLER = new BlockPos(1, 1, 2);

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void compartmentStorageRegression(GameTestHelper helper) {
        for (int layout = 0; layout < 2; layout++) {
            int slots = layout == 0 ? 2 : 4;
            for (int tier = 0; tier < GTValues.TIER_COUNT; tier++) {
                if (CompartmentStorageRegistration.CHESTS[layout][tier] != null) {
                    var base = (QuantumChestMachine) place(helper, COPY, GTMachines.SUPER_CHEST[tier]);
                    checkStorage(helper, CompartmentStorageRegistration.CHESTS[layout][tier], slots, base.getMax(), false);
                }
                if (CompartmentStorageRegistration.TANKS[layout][tier] != null) {
                    var base = (QuantumTankMachine) place(helper, COPY, GTMachines.SUPER_TANK[tier]);
                    checkStorage(helper, CompartmentStorageRegistration.TANKS[layout][tier], slots, base.getMax(), true);
                }
            }
            for (var wood : WoodenSuperStorageRegistration.Wood.values()) {
                checkStorage(helper, CompartmentStorageRegistration.WOODEN_CHESTS[layout][wood.ordinal()], slots, WoodenSuperStorageRegistration.ITEM_CAPACITY, false);
                checkStorage(helper, CompartmentStorageRegistration.WOODEN_TANKS[layout][wood.ordinal()], slots, WoodenSuperStorageRegistration.FLUID_CAPACITY, true);
            }
        }
        checkController(helper);
        checkFacing(helper);
        checkHandheldTank(helper);
        checkPlayerDeposit(helper);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void compartmentAutomaticOutput(GameTestHelper helper) {
        var chest = (CompartmentStorageMachine) place(helper, STORAGE, CompartmentStorageRegistration.WOODEN_CHESTS[1][0]);
        var chestTarget = (CompartmentStorageMachine) place(helper, COPY, CompartmentStorageRegistration.WOODEN_CHESTS[1][0]);
        var tank = (CompartmentStorageMachine) place(helper, CONTROLLER, CompartmentStorageRegistration.WOODEN_TANKS[1][0]);
        var tankTarget = (CompartmentStorageMachine) place(helper, new BlockPos(2, 1, 2), CompartmentStorageRegistration.WOODEN_TANKS[1][0]);
        var diamond = AEItemKey.of(Items.DIAMOND);
        var emerald = AEItemKey.of(Items.EMERALD);
        var water = AEFluidKey.of(Fluids.WATER);
        var lava = AEFluidKey.of(Fluids.LAVA);
        chest.inventory().insert(1, diamond, 8, false);
        chest.inventory().insert(3, emerald, 17, false);
        tank.inventory().insert(1, water, 2000, false);
        tank.inventory().insert(3, lava, 3000, false);
        chest.setOutputFacingItems(Direction.EAST);
        tank.setOutputFacingFluids(Direction.EAST);
        helper.assertTrue(chest.getItemHandlerCap(Direction.NORTH, false) == null && tank.getFluidHandlerCap(Direction.NORTH, false) == null, "Front face exposed automation capabilities");
        helper.assertTrue(chest.getItemHandlerCap(Direction.EAST, false).insert(diamond, 1, true) == 0, "Output face accepted input by default");
        chest.setAllowInputFromOutputSideItems(true);
        helper.assertTrue(chest.getItemHandlerCap(Direction.EAST, false).insert(diamond, 1, true) == 1, "Shared input permission did not update capabilities");
        chest.setWorkingEnabled(true);
        tank.setWorkingEnabled(true);
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(chest.inventory().isEmpty() && chestTarget.inventory().count(diamond) == 8 && chestTarget.inventory().count(emerald) == 17, "Automatic item output missed a compartment");
            helper.assertTrue(tank.inventory().isEmpty() && tankTarget.inventory().count(water) == 2000 && tankTarget.inventory().count(lava) == 3000, "Automatic fluid output missed a compartment");
            helper.succeed();
        });
    }

    private static MetaMachine place(GameTestHelper helper, BlockPos pos, MachineDefinition definition) {
        helper.setBlock(pos, definition.defaultBlockState());
        return MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(pos));
    }

    private static void checkStorage(GameTestHelper helper, MachineDefinition definition, int slots, long capacity, boolean tank) {
        var machine = (CompartmentStorageMachine) place(helper, STORAGE, definition);
        var storage = machine.inventory();
        helper.assertTrue(storage.size() == slots && machine.getMax() == capacity, "Wrong compartment count or tier capacity: " + definition.getId());
        long sum = 0;
        for (int i = 0; i < slots; i++) sum += storage.slotLimit(i);
        helper.assertTrue(sum == capacity, "Dividing capacity changed the total");
        machine.createUIWidget();
        if (capacity < slots) return;
        var first = tank ? AEFluidKey.of(Fluids.WATER) : AEItemKey.of(Items.DIAMOND_PICKAXE);
        var second = tank ? AEFluidKey.of(Fluids.LAVA) : AEItemKey.of(Items.IRON_INGOT);
        long limit = storage.slotLimit(0);
        helper.assertTrue(storage.insert(0, first, limit + 1, true) == limit && storage.amountAt(0) == 0, "Simulation changed inventory or exceeded its share");
        helper.assertTrue(storage.insert(0, first, limit + 1, false) == limit, "Real insert did not honor compartment capacity");
        helper.assertTrue(storage.insert(1, second, 37, false) == 37 && storage.keyAt(0) == first && storage.keyAt(1) == second, "Compartments are not independent");
        helper.assertTrue(storage.insert(0, second, 1, false) == 0, "Mixed keys in one compartment");
        machine.gtolib$setStorageLocked(true);
        helper.assertTrue(machine.lockedKey(0) == first && machine.lockedKey(1) == second, "Shared lock mode lost individual keys");
        helper.assertTrue(storage.extract(1, second, 37, true) == 37 && storage.amountAt(1) == 37, "Simulated extraction mutated inventory");
        storage.extract(1, second, 37, false);
        helper.assertTrue(storage.insert(1, first, 1, false) == 0 && machine.lockedKey(1) == second, "Empty locked compartment accepted another key");
        machine.setVoiding(true);
        helper.assertTrue(storage.insert(0, first, 99, false) == 99 && storage.amountAt(0) == limit, "Void overflow increased stored amount");
        machine.setMaxAmount(7);
        helper.assertTrue(storage.amountAt(0) == limit && storage.insert(0, first, 1, true) == 1, "Reducing capacity lost contents or disabled voiding");
        machine.setAllowInputFromOutputSideItems(true);
        machine.setWorkingEnabled(true);
        var drop = new CompoundTag();
        machine.saveToItem(drop);
        var copy = (CompartmentStorageMachine) place(helper, COPY, definition);
        copy.loadFromItem(drop);
        helper.assertTrue(copy.inventory().amountAt(0) == limit && copy.inventory().keyAt(0) == first && copy.lockedKey(1) == second,
                "Drop/placement lost compartment contents or locks");
        helper.assertTrue(copy.isLocked() && copy.isVoiding() && copy.getMaxAmount() == 7 && copy.isWorkingEnabled() && copy.isAllowInputFromOutputSideItems(), "Drop/placement lost shared settings");
        var worldTag = machine.holder.saveWithoutMetadata();
        copy.holder.load(worldTag);
        helper.assertTrue(copy.inventory().amountAt(0) == limit && copy.isLocked() && copy.getMaxAmount() == 7, "World save/load lost contents or settings");
        machine.setVoiding(false);
        machine.gtolib$setStorageLocked(false);
        storage.extract(0, first, limit, false);
        machine.setMaxAmount(capacity);
        machine.setVoiding(true);
        // Aggregate IO must consume free storage before reporting the remainder as destroyed.
        helper.assertTrue(storage.insert(first, storage.slotLimit(0) + 3, false) == storage.slotLimit(0) + 3 && storage.amountAt(1) == 3, "Aggregate voiding destroyed items while another compartment had space");
    }

    private static void checkController(GameTestHelper helper) {
        var controller = (SuperChestControllerMachine) place(helper, CONTROLLER, SuperStorageRegistration.CONTROLLER);
        var chest = (CompartmentStorageMachine) place(helper, STORAGE, CompartmentStorageRegistration.WOODEN_CHESTS[1][0]);
        var tank = (CompartmentStorageMachine) place(helper, COPY, CompartmentStorageRegistration.WOODEN_TANKS[1][0]);
        var item = AEItemKey.of(Items.DIAMOND);
        var other = AEItemKey.of(Items.EMERALD);
        var water = AEFluidKey.of(Fluids.WATER);
        chest.inventory().insert(0, item, 11, false);
        chest.inventory().insert(3, other, 23, false);
        tank.inventory().insert(2, water, 5000, false);
        chest.gtolib$getStorageLink().bind(controller);
        tank.gtolib$getStorageLink().bind(controller);
        var items = controller.getItemHandlerCap(null, false);
        var fluids = controller.getFluidHandlerCap(null, false);
        helper.assertTrue(items.size() == 4 && fluids.size() == 4 && items.count(item) == 11 && items.count(other) == 23 && fluids.count(water) == 5000, "Controller did not index every compartment");
        helper.assertTrue(items.extract(other, 23, true) == 23 && items.count(other) == 23, "Controller simulation mutated contents");
        items.extract(other, 23, false);
        helper.assertTrue(chest.inventory().amountAt(3) == 0 && items.count(other) == 0, "Controller extraction used the wrong compartment");
        helper.assertTrue(items.insert(other, 19, false) == 19 && items.count(other) == 19, "Controller failed to refresh an empty compartment");
        chest.gtolib$getStorageLink().unload();
        helper.assertTrue(items.count(item) == 0 && fluids.count(water) == 5000, "Unloading a chest removed the wrong index entries");
        chest.gtolib$getStorageLink().getNetMachine();
        helper.assertTrue(items.count(item) == 11, "Controller did not reconnect all compartments");
        chest.setVoiding(true);
        long request = chest.inventory().slotLimit(0) + 100;
        helper.assertTrue(items.insert(item, request, false) == request && items.count(item) == request + 11,
                "Controller destroyed overflow before using other compartments");
        var newKey = AEItemKey.of(Items.GOLD_INGOT);
        helper.assertTrue(items.insert(newKey, 100000, false) == 100000 && items.count(newKey) > 0 && items.count(newKey) < 100000,
                "Controller did not void a new key's overflow after filling its last empty compartment");
    }

    private static void checkFacing(GameTestHelper helper) {
        var machine = (CompartmentStorageMachine) place(helper, STORAGE, CompartmentStorageRegistration.WOODEN_CHESTS[1][1]);
        machine.setOutputFacingItems(null);
        for (var front : Direction.values()) {
            machine.setFrontFacing(front);
            for (var up : new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST }) {
                machine.setUpwardsFacing(up);
                for (int slot = 0; slot < 4; slot++) {
                    var point = new Vector3f(slot % 2 == 0 ? .25F : -.25F, slot < 2 ? .25F : -.25F, -.5F);
                    CompartmentStorageGeometry.rotation(front, up).transformPosition(point);
                    var location = Vec3.atCenterOf(machine.getPos()).add(point.x, point.y, point.z);
                    helper.assertTrue(machine.hitSlot(new BlockHitResult(location, front, machine.getPos(), false)) == slot, "Click/model disagreement after rotation: " + front + "/" + up + "/" + slot);
                }
                helper.assertTrue(machine.hitSlot(new BlockHitResult(Vec3.atCenterOf(machine.getPos()), front, machine.getPos(), false)) == -1, "Divider click should open the GUI");
            }
        }
    }

    private static void checkHandheldTank(GameTestHelper helper) {
        var definition = CompartmentStorageRegistration.WOODEN_TANKS[1][0];
        var stack = definition.asStack();
        var cap = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new);
        helper.assertTrue(cap.getTanks() == 4 && cap.getTankCapacity(0) == 64000, "Handheld tank capacity differs from placed capacity");
        helper.assertTrue(cap.fill(new FluidStack(Fluids.WATER, 64000), IFluidHandler.FluidAction.SIMULATE) == 64000 && cap.getFluidInTank(0).isEmpty(), "Handheld simulation changed contents");
        cap.fill(new FluidStack(Fluids.WATER, 64000), IFluidHandler.FluidAction.EXECUTE);
        cap.fill(new FluidStack(Fluids.LAVA, 3000), IFluidHandler.FluidAction.EXECUTE);
        var placed = (CompartmentStorageMachine) place(helper, COPY, definition);
        placed.loadFromItem(stack.getTag());
        helper.assertTrue(placed.inventory().amountAt(0) == 64000 && placed.inventory().amountAt(1) == 3000, "Placing a handheld tank lost fluids");
        var drop = new CompoundTag();
        placed.saveToItem(drop);
        var restored = definition.asStack();
        // Initialize first: capability readers must also notice tags assigned after construction.
        var restoredCap = restored.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(IllegalStateException::new);
        restored.setTag(drop);
        helper.assertTrue(restoredCap.drain(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE).getAmount() == 1000 && restoredCap.getFluidInTank(0).getAmount() == 64000, "Handheld drain changed another compartment");
    }

    private static void checkPlayerDeposit(GameTestHelper helper) {
        var chest = (CompartmentStorageMachine) place(helper, STORAGE, CompartmentStorageRegistration.WOODEN_CHESTS[1][9]);
        var tank = (CompartmentStorageMachine) place(helper, COPY, CompartmentStorageRegistration.WOODEN_TANKS[1][9]);
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(Items.GOLD_INGOT, 9));
        var point = new Vector3f(.25F, -.25F, -.5F);
        CompartmentStorageGeometry.rotation(chest.getFrontFacing(), chest.getUpwardsFacing()).transformPosition(point);
        var hit = new BlockHitResult(Vec3.atCenterOf(chest.getPos()).add(point.x, point.y, point.z), chest.getFrontFacing(), chest.getPos(), false);
        chest.onUse(chest.getBlockState(), helper.getLevel(), chest.getPos(), player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(chest.inventory().amountAt(2) == 9 && chest.inventory().amountAt(0) == 0 && player.getMainHandItem().isEmpty(), "Right-click deposited into the wrong compartment");
        player.setItemInHand(InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(Items.WATER_BUCKET));
        point.set(-.25F, .25F, -.5F);
        CompartmentStorageGeometry.rotation(tank.getFrontFacing(), tank.getUpwardsFacing()).transformPosition(point);
        hit = new BlockHitResult(Vec3.atCenterOf(tank.getPos()).add(point.x, point.y, point.z), tank.getFrontFacing(), tank.getPos(), false);
        tank.onUse(tank.getBlockState(), helper.getLevel(), tank.getPos(), player, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(tank.inventory().amountAt(1) == 1000 && tank.inventory().amountAt(0) == 0 && player.getMainHandItem().is(Items.BUCKET), "Fluid container deposit mixed compartments or lost its remainder");
    }
}

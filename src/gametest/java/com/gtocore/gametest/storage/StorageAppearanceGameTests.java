package com.gtocore.gametest.storage;

import com.gtolib.api.data.storage.CompartmentStorageRegistration;
import com.gtolib.api.data.storage.SuperStorageRegistration;
import com.gtolib.api.data.storage.WoodenSuperStorageRegistration;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IStorageAppearance;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.gto.datasynclib.LogicalSide;

/** Exercises real machine definitions and the protected GTOLib field scanning path. */
@GameTestHolder("gtocore")
@PrefixGameTestTemplate(false)
public final class StorageAppearanceGameTests {

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void allStorageVariants(GameTestHelper helper) {
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
        check(helper, SuperStorageRegistration.CONTROLLER);
        check(helper, SuperStorageRegistration.EXTENSION);
        helper.succeed();
    }

    private static void checkAll(GameTestHelper helper, MachineDefinition[] definitions) {
        for (var definition : definitions) {
            if (definition != null) check(helper, definition);
        }
    }

    private static MetaMachineBlockEntity create(MachineDefinition definition) {
        return (MetaMachineBlockEntity) definition.getBlockEntityType().create(BlockPos.ZERO, definition.defaultBlockState());
    }

    private static void sync(MetaMachineBlockEntity source, MetaMachineBlockEntity target, boolean full) {
        source.getFieldDataManager().updateFieldDirtyFlags(LogicalSide.SERVER, false);
        target.getFieldDataManager().readFromNetworkBuffer(LogicalSide.CLIENT, source.getFieldDataManager().writeToNetworkBuffer(LogicalSide.SERVER, full));
    }

    private static void check(GameTestHelper helper, MachineDefinition definition) {
        var holder = create(definition);
        var machine = holder.getMetaMachine();
        String id = definition.getId().toString();
        helper.assertTrue(machine instanceof IStorageAppearance, id + ": missing appearance feature");
        var appearance = ((IStorageAppearance) machine).getStorageAppearance();
        var sample = new ItemStack(Blocks.OAK_LOG, 16);
        appearance.setSample(sample);
        helper.assertTrue(sample.getCount() == 16 && appearance.getBlock() == Blocks.OAK_LOG, id + ": sample consumed or rejected");
        appearance.setSample(new ItemStack(Items.DIAMOND));
        helper.assertTrue(appearance.getBlock() == Blocks.OAK_LOG, id + ": accepted a non-block item");

        var restored = create(definition);
        restored.load(holder.saveWithoutMetadata());
        var restoredAppearance = ((IStorageAppearance) restored.getMetaMachine()).getStorageAppearance();
        helper.assertTrue(restoredAppearance.getBlock() == Blocks.OAK_LOG, id + ": world save lost appearance");

        var drop = (IDropSaveMachine) machine;
        helper.assertTrue(drop.saveBreak(), id + ": empty machine would drop without appearance");
        var tag = new CompoundTag();
        drop.saveToItem(tag);
        var placed = create(definition);
        ((IDropSaveMachine) placed.getMetaMachine()).loadFromItem(tag);
        helper.assertTrue(((IStorageAppearance) placed.getMetaMachine()).getStorageAppearance().getBlock() == Blocks.OAK_LOG,
                id + ": break and replace lost appearance");

        var replica = new RenderReplica(definition);
        var replicaAppearance = ((IStorageAppearance) replica.getMetaMachine()).getStorageAppearance();
        sync(holder, replica, true);
        helper.assertTrue(replicaAppearance.getBlock() == Blocks.OAK_LOG, id + ": full client sync lost appearance");
        helper.assertTrue(replica.invalidations > 0, id + ": initial appearance did not invalidate the model");
        int invalidations = replica.invalidations;
        appearance.setSample(new ItemStack(Blocks.BRICKS));
        sync(holder, replica, false);
        helper.assertTrue(replicaAppearance.getBlock() == Blocks.BRICKS, id + ": changed sample did not sync");
        helper.assertTrue(replica.invalidations > invalidations, id + ": changed appearance did not invalidate the model");
        invalidations = replica.invalidations;
        appearance.setSample(ItemStack.EMPTY);
        sync(holder, replica, false);
        helper.assertTrue(replicaAppearance.getBlock() == Blocks.AIR, id + ": clearing sample did not sync");
        helper.assertTrue(replica.invalidations > invalidations, id + ": clearing appearance did not invalidate the model");
        appearance.loadCustomPersistedData(new CompoundTag());
        helper.assertTrue(appearance.getBlock() == Blocks.AIR, id + ": old save must use default appearance");
    }

    private static final class RenderReplica extends MetaMachineBlockEntity {

        private int invalidations;

        private RenderReplica(MachineDefinition definition) {
            super(definition.getBlockEntityType(), BlockPos.ZERO, definition.defaultBlockState());
        }

        @Override
        public void scheduleRenderUpdate() {
            invalidations++;
        }
    }
}

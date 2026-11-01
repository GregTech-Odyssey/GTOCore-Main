package com.gtocore.gametest.storage;

import com.gtolib.api.data.storage.CompartmentStorageRegistration;
import com.gtolib.api.data.storage.WoodenSuperStorageRegistration;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.config.ConfigHolder.MachineConfigs.SuperStorageInteraction;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.stacks.AEItemKey;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

@GameTestHolder("gtocore_storage_interactions")
@PrefixGameTestTemplate(false)
public final class StorageInteractionGameTests {

    private static final GameProfile PROFILE = new GameProfile(new UUID(0, 0x5702), "StorageModes");

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void bothInteractionModes(GameTestHelper helper) {
        var previous = ConfigHolder.INSTANCE.machines.superStorageInteraction;
        var player = FakePlayerFactory.get(helper.getLevel(), PROFILE);
        try {
            for (var mode : SuperStorageInteraction.values()) {
                ConfigHolder.INSTANCE.machines.superStorageInteraction = mode;
                checkChest(helper, player, CompartmentStorageRegistration.NORMAL_CHESTS[GTValues.LV], mode);
                checkChest(helper, player, WoodenSuperStorageRegistration.CHESTS[0], mode);
                checkTank(helper, player, CompartmentStorageRegistration.NORMAL_TANKS[GTValues.LV], mode);
                checkTank(helper, player, WoodenSuperStorageRegistration.TANKS[0], mode);
            }
        } finally {
            ConfigHolder.INSTANCE.machines.superStorageInteraction = previous;
            player.getInventory().clearContent();
        }
        helper.succeed();
    }

    private static MetaMachine place(GameTestHelper helper, MachineDefinition definition) {
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var level = helper.getLevel();
        level.destroyBlock(pos, false);
        var state = definition.defaultBlockState();
        level.setBlockAndUpdate(pos, state);
        state.getBlock().setPlacedBy(level, pos, state, null, definition.asStack());
        return MetaMachine.getMachine(level, pos);
    }

    private static BlockHitResult hit(MetaMachine machine, boolean casing) {
        var front = machine.getFrontFacing();
        var target = Vec3.atCenterOf(machine.getPos()).relative(front, .5);
        if (casing) target = target.relative(front.getAxis() == Direction.Axis.Y ? Direction.EAST : Direction.UP, .48);
        return new BlockHitResult(target, front, machine.getPos(), false);
    }

    private static void aim(Player player, MetaMachine machine, BlockHitResult hit) {
        var eye = Vec3.atCenterOf(machine.getPos()).relative(machine.getFrontFacing(), 2);
        player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
        player.xo = player.getX();
        player.yo = player.getY();
        player.zo = player.getZ();
        player.lookAt(EntityAnchorArgument.Anchor.EYES, hit.getLocation());
    }

    private static void checkChest(GameTestHelper helper, Player player, MachineDefinition definition, SuperStorageInteraction mode) {
        var chest = (QuantumChestMachine) place(helper, definition);
        var level = helper.getLevel();
        var pos = chest.getPos();
        var storage = chest.getItemHandlerCap(null, false);
        var key = AEItemKey.of(Items.IRON_INGOT);
        String id = definition.getId() + "/" + mode;
        player.getInventory().clearContent();
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 5));
        helper.assertTrue(chest.onUse(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND, hit(chest, false)).consumesAction(), id + ": insert click");
        helper.assertTrue(storage.amountAt(0) == 5 && player.getMainHandItem().isEmpty(), id + ": insert held items");
        aim(player, chest, hit(chest, true));
        helper.assertTrue(!chest.onLeftClick(player, level, InteractionHand.MAIN_HAND, pos, chest.getFrontFacing()), id + ": casing allows mining");
        long afterCasing = mode == SuperStorageInteraction.GTCEU ? 4 : 5;
        helper.assertTrue(storage.amountAt(0) == afterCasing, id + ": GTCEu extracts across the whole front");
        aim(player, chest, hit(chest, false));
        boolean canceled = chest.onLeftClick(player, level, InteractionHand.MAIN_HAND, pos, chest.getFrontFacing());
        helper.assertTrue(canceled == (mode == SuperStorageInteraction.DRAWER), id + ": only drawers cancel display mining");
        helper.assertTrue(storage.keyAt(0) == key && storage.amountAt(0) == afterCasing - 1, id + ": extract one item");
    }

    private static void checkTank(GameTestHelper helper, Player player, MachineDefinition definition, SuperStorageInteraction mode) {
        var tank = (QuantumTankMachine) place(helper, definition);
        var level = helper.getLevel();
        var pos = tank.getPos();
        var storage = tank.getFluidHandlerCap(null, false);
        String id = definition.getId() + "/" + mode;
        player.getInventory().clearContent();
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        tank.onUse(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND, hit(tank, true));
        long rimAmount = mode == SuperStorageInteraction.GTCEU ? 1000 : 0;
        helper.assertTrue(storage.amountAt(0) == rimAmount, id + ": GTCEu accepts containers across the whole front");
        if (mode == SuperStorageInteraction.DRAWER) {
            tank.onUse(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND, hit(tank, false));
        }
        helper.assertTrue(storage.amountAt(0) == 1000 && player.getMainHandItem().is(Items.BUCKET), id + ": deposit returns an empty bucket");
        tank.onUse(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND, hit(tank, false));
        boolean gtceu = mode == SuperStorageInteraction.GTCEU;
        helper.assertTrue(storage.amountAt(0) == (gtceu ? 0 : 1000), id + ": only GTCEu fills containers on right click");
        helper.assertTrue(player.getMainHandItem().is(gtceu ? Items.WATER_BUCKET : Items.BUCKET), id + ": right click preserves container contents");
        if (gtceu) {
            tank.onUse(level.getBlockState(pos), level, pos, player, InteractionHand.MAIN_HAND, hit(tank, false));
        }
        aim(player, tank, hit(tank, false));
        boolean canceled = tank.onLeftClick(player, level, InteractionHand.MAIN_HAND, pos, tank.getFrontFacing());
        helper.assertTrue(canceled == !gtceu, id + ": only drawers cancel container left click");
        helper.assertTrue(storage.amountAt(0) == (gtceu ? 1000 : 0), id + ": only drawers extract on left click");
        helper.assertTrue(player.getMainHandItem().is(gtceu ? Items.BUCKET : Items.WATER_BUCKET), id + ": left click preserves container contents");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
        helper.assertTrue(!tank.onLeftClick(player, level, InteractionHand.MAIN_HAND, pos, tank.getFrontFacing()), id + ": ordinary tools can mine tanks");
    }
}

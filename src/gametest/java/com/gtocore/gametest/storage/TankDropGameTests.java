package com.gtocore.gametest.storage;

import com.gtolib.api.data.storage.CompartmentStorageRegistration;
import com.gtolib.api.data.storage.WoodenSuperStorageRegistration;
import com.gtolib.api.machine.storage.CompartmentStorageMachine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.stacks.AEFluidKey;
import io.netty.buffer.Unpooled;

@GameTestHolder("gtocore_tank_drops")
@PrefixGameTestTemplate(false)
public final class TankDropGameTests {

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tankDropsRetainFluids(GameTestHelper helper) {
        checkAll(helper, CompartmentStorageRegistration.NORMAL_TANKS);
        checkAll(helper, WoodenSuperStorageRegistration.TANKS);
        for (int layout = 0; layout < 2; layout++) {
            checkAll(helper, CompartmentStorageRegistration.TANKS[layout]);
            checkAll(helper, CompartmentStorageRegistration.WOODEN_TANKS[layout]);
        }
        helper.succeed();
    }

    private static void checkAll(GameTestHelper helper, MachineDefinition[] definitions) {
        for (var definition : definitions) {
            if (definition == null) continue;
            check(helper, definition, new ItemStack(Items.IRON_PICKAXE));
            check(helper, definition, ItemStack.EMPTY);
            check(helper, definition, new ItemStack(Items.BUCKET));
        }
    }

    private static void check(GameTestHelper helper, MachineDefinition definition, ItemStack held) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var state = definition.defaultBlockState();
        level.setBlockAndUpdate(pos, state);
        state.getBlock().setPlacedBy(level, pos, state, null, definition.asStack());
        var machine = MetaMachine.getMachine(level, pos);
        IKeyHandler<AEFluidKey> storage = machine.getFluidHandlerCap(null, false);
        String id = definition.getId().toString();
        var fluidTag = new CompoundTag();
        fluidTag.putString("tankDropTest", id);
        var water = AEFluidKey.of(Fluids.WATER, fluidTag);
        long amount = Math.min(storage.slotLimit(0), (long) Integer.MAX_VALUE + 1000);
        helper.assertTrue(storage.insert(0, water, amount, false) == amount, id + ": initial fill");
        var player = FakePlayerFactory.getMinecraft(level);
        helper.assertTrue(!player.isCreative(), id + ": player must mine in survival mode");
        var front = machine.getFrontFacing();
        var display = Vec3.atCenterOf(pos);
        if (machine instanceof CompartmentStorageMachine) display = display.add(storage.size() == 4 ? .25 : 0, .25, 0);
        var casing = Vec3.atCenterOf(pos).add(.46, .46, 0).relative(front, .5);
        var target = held.is(Items.BUCKET) ? casing : display;
        var eye = display.relative(front, 2);
        player.setPos(eye.x, eye.y - player.getEyeHeight(), eye.z);
        player.xo = player.getX();
        player.yo = player.getY();
        player.zo = player.getZ();
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target);
        helper.assertTrue(player.pick(player.getBlockReach(), 1, false) instanceof BlockHitResult hit && hit.getBlockPos().equals(pos) && hit.getDirection() == front,
                id + ": player must target the front display");
        for (int cycle = 0; cycle < 2; cycle++) {
            if (held.is(Items.BUCKET)) {
                player.lookAt(EntityAnchorArgument.Anchor.EYES, casing);
                player.xRotO = player.getXRot();
                player.yRotO = player.getYRot();
                player.yHeadRotO = player.getYHeadRot();
                player.lookAt(EntityAnchorArgument.Anchor.EYES, display);
                // A full container cannot take fluid; it still activates the display interaction.
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
                var entering = ForgeHooks.onLeftClickBlock(player, pos, front, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
                helper.assertTrue(entering.isCanceled(), id + ": current display hit must cancel mining");
                player.xRotO = player.getXRot();
                player.yRotO = player.getYRot();
                player.yHeadRotO = player.getYHeadRot();
                player.lookAt(EntityAnchorArgument.Anchor.EYES, casing);
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            var click = ForgeHooks.onLeftClickBlock(player, pos, front, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
            helper.assertTrue(!click.isCanceled(), id + ": front interaction cancels mining with " + held);
            helper.assertTrue(storage.keyAt(0) == water && storage.amountAt(0) == amount, id + ": mining click changes stored fluid");
            var holder = level.getBlockEntity(pos);
            helper.assertTrue(state.onDestroyedByPlayer(level, pos, player, true, level.getFluidState(pos)), id + ": destroy tank");
            state.getBlock().destroy(level, pos, state);
            state.getBlock().playerDestroy(level, player, pos, state, holder, held);
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1), entity -> entity.getItem().is(definition.asItem()));
            helper.assertTrue(drops.size() == 1, id + ": tank drop");
            var drop = drops.get(0).getItem().copy();
            drops.get(0).discard();
            var packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeItem(drop);
            drop = packet.readItem();
            packet.release();
            var fluid = drop.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElseThrow(() -> new AssertionError(id + ": tank capability"));
            helper.assertTrue(fluid.getFluidInTank(0).getRawFluid() == Fluids.WATER, id + ": dropped fluid type");
            helper.assertTrue(fluidTag.equals(fluid.getFluidInTank(0).getTag()), id + ": dropped fluid tag");
            helper.assertTrue(fluid.getFluidInTank(0).getAmount() == Math.min(amount, Integer.MAX_VALUE), id + ": dropped fluid display amount");
            level.setBlockAndUpdate(pos, state);
            state.getBlock().setPlacedBy(level, pos, state, null, drop);
            storage = MetaMachine.getMachine(level, pos).getFluidHandlerCap(null, false);
            helper.assertTrue(storage.keyAt(0) == water && storage.amountAt(0) == amount, id + ": replacing tank loses fluid");
        }
        level.destroyBlock(pos, false);
    }
}

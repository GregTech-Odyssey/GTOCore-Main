package com.gtocore.gametest.drone;

import com.gtocore.common.data.GTOItems;

import com.gtolib.drone.DroneMode;
import com.gtolib.drone.DroneServer;
import com.gtolib.drone.DroneSettings;
import com.gtolib.drone.DroneSlot;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IDataComponentItem;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.mojang.authlib.GameProfile;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import io.netty.buffer.Unpooled;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.inventory.CurioStacksHandler;

import java.util.HashMap;
import java.util.UUID;

@GameTestHolder("gtocore")
@PrefixGameTestTemplate(false)
public final class DroneCuriosGameTests {

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void equippedDroneToggleAndLifecycle(GameTestHelper helper) throws ReflectiveOperationException {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(new UUID(0, 0xD20F), "DroneCurios"));
        var inventory = CuriosApi.getCuriosInventory(player).orElse(null);
        helper.assertTrue(inventory != null, "Player has a Curios inventory");
        var slot = new CurioStacksHandler(inventory, DroneSlot.CURIOS_SLOT);
        var previousSlots = new HashMap<>(inventory.getCurios());
        var testSlots = new HashMap<>(previousSlots);
        testSlots.put(DroneSlot.CURIOS_SLOT, slot);
        inventory.setCurios(testSlots);
        var stacks = slot.getStacks();
        try {
            var heldDrone = GTOItems.IV_DRONE.asStack();
            GTCapabilityHelper.getElectricItem(heldDrone).charge(100, Integer.MAX_VALUE, true, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, heldDrone);
            toggle(player);
            helper.assertTrue(!DroneServer.active(player), "An empty Curios slot does not fall back to a held drone");
            stacks.setStackInSlot(0, new ItemStack(Items.STONE));
            toggle(player);
            helper.assertTrue(!DroneServer.active(player), "A non-drone cannot start a Curios session");
            var drone = GTOItems.HV_DRONE.asStack();
            stacks.setStackInSlot(0, drone);
            toggle(player);
            helper.assertTrue(!DroneServer.active(player), "An uncharged drone cannot start a session");
            var electric = GTCapabilityHelper.getElectricItem(drone);
            electric.charge(100, Integer.MAX_VALUE, true, false);
            drone.getOrCreateTag().putInt("work", 1);
            toggle(player);
            helper.assertTrue(!DroneServer.active(player), "A working drone cannot start a session");
            drone.getOrCreateTag().remove("work");
            toggle(player);
            helper.assertTrue(DroneServer.active(player), "The toggle packet starts a session from the Curios slot");
            var movedBody = player.position().add(10, 1, 0);
            var velocity = new Vec3(0.25, 0.1, 0.5);
            player.setPos(movedBody);
            player.setDeltaMovement(velocity);
            tick(player);
            helper.assertTrue(DroneServer.active(player) && player.position().equals(movedBody) && player.getDeltaMovement().equals(velocity),
                    "Drone ticks preserve body position and velocity without exiting after body displacement");
            var settings = new DroneSettings(0.125f, DroneMode.MODEL, true, false, null, null, player.level().dimension().location());
            var buf = new FriendlyByteBuf(Unpooled.buffer());
            try {
                buf.writeByte(2);
                settings.writeToBuf(buf);
                control(player, buf);
            } finally {
                buf.release();
            }
            helper.assertTrue(settings.equals(IDataComponentItem.get(drone, DroneSettings.KEY)), "Settings are saved on the equipped drone");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
            for (int i = 0; i < 20; i++) tick(player);
            helper.assertTrue(DroneServer.active(player) && electric.getCharge() == 96, "Changing the held item preserves the session and drains the Curios drone");
            helper.assertTrue(GTCapabilityHelper.getElectricItem(heldDrone).getCharge() == 100, "The held drone is not drained");
            var replacement = drone.copy();
            stacks.setStackInSlot(0, replacement);
            for (int i = 0; i < 20; i++) tick(player);
            helper.assertTrue(DroneServer.active(player) && GTCapabilityHelper.getElectricItem(replacement).getCharge() == 92 && electric.getCharge() == 96,
                    "Inventory synchronization rebinds the session to the current Curios stack");
            toggle(player);
            helper.assertTrue(!DroneServer.active(player), "The same toggle packet exits the active session");
            toggle(player);
            helper.assertTrue(DroneServer.active(player), "The equipped drone can be enabled again");
            stacks.setStackInSlot(0, ItemStack.EMPTY);
            tick(player);
            helper.assertTrue(!DroneServer.active(player), "Removing the equipped drone ends the session");
        } finally {
            DroneServer.stop(player);
            inventory.setCurios(previousSlots);
            player.getInventory().clearContent();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void equippedDroneCombatAndBlockUseRestoreHands(GameTestHelper helper) throws ReflectiveOperationException {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(new UUID(0, 0xD210), "DroneCurioCombat"));
        var inventory = CuriosApi.getCuriosInventory(player).orElse(null);
        helper.assertTrue(inventory != null, "Player has a Curios inventory");
        var slot = new CurioStacksHandler(inventory, DroneSlot.CURIOS_SLOT);
        var previousSlots = new HashMap<>(inventory.getCurios());
        var testSlots = new HashMap<>(previousSlots);
        testSlots.put(DroneSlot.CURIOS_SLOT, slot);
        inventory.setCurios(testSlots);
        var main = new ItemStack(Items.DIAMOND_SWORD);
        var off = new ItemStack(Items.SHIELD);
        player.setItemInHand(InteractionHand.MAIN_HAND, main);
        player.setItemInHand(InteractionHand.OFF_HAND, off);
        player.getAttributes().addTransientAttributeModifiers(main.getAttributeModifiers(EquipmentSlot.MAINHAND));
        double originalDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double originalSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
        var pos = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 2, 1)));
        player.setPos(pos);
        var target = EntityType.COW.create(helper.getLevel());
        target.setPos(pos.add(0, 0, 2));
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        target.setNoAi(true);
        helper.getLevel().addFreshEntity(target);
        try {
            var drone = GTOItems.HV_DRONE.asStack();
            GTCapabilityHelper.getElectricItem(drone).charge(100, Integer.MAX_VALUE, true, false);
            var affixData = drone.getOrCreateTagElement(AffixHelper.AFFIX_DATA);
            affixData.putString(AffixHelper.RARITY, "apotheosis:epic");
            var affixes = new net.minecraft.nbt.CompoundTag();
            affixes.putFloat("apotheosis:sword/mob_effect/weakening", 1);
            affixData.put(AffixHelper.AFFIXES, affixes);
            slot.getStacks().setStackInSlot(0, drone);
            player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            toggle(player);
            interact(player, 3);
            helper.assertTrue(target.getHealth() < 100 && target.hasEffect(MobEffects.WEAKNESS), "Remote attacks use the Curios drone's damage and on-hit affixes");
            helper.assertTrue(target.getLastDamageSource() != null && target.getLastDamageSource().getMsgId().equals("drone"), "Curios attacks retain drone damage attribution");
            helper.assertTrue(player.getMainHandItem() == main && player.getOffhandItem() == off && DroneSlot.CURIO.stack(player) == drone,
                    "Remote attacks restore both hands and keep the drone equipped");
            helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) == originalDamage && player.getAttributeValue(Attributes.ATTACK_SPEED) == originalSpeed,
                    "Remote attacks restore held-item attributes");
            var chest = new BlockPos(2, 3, 4);
            helper.setBlock(chest, Blocks.CHEST);
            player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(helper.absolutePos(chest)));
            interact(player, 4);
            helper.assertTrue(player.containerMenu != player.inventoryMenu, "The equipped drone can open a remote container");
            helper.assertTrue(player.getMainHandItem() == main && player.getOffhandItem() == off && player.position().equals(pos),
                    "Remote block use restores both hands and the body position");
            helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) == originalDamage && player.getAttributeValue(Attributes.ATTACK_SPEED) == originalSpeed,
                    "Remote block use restores held-item attributes");
        } finally {
            DroneServer.stop(player);
            target.discard();
            inventory.setCurios(previousSlots);
            player.getAttributes().removeAttributeModifiers(main.getAttributeModifiers(EquipmentSlot.MAINHAND));
            player.getInventory().clearContent();
        }
        helper.succeed();
    }

    private static void toggle(ServerPlayer player) throws ReflectiveOperationException {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeByte(9);
            control(player, buf);
        } finally {
            buf.release();
        }
    }

    private static void interact(ServerPlayer player, int operation) throws ReflectiveOperationException {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeByte(operation).writeFloat(player.getYRot()).writeFloat(player.getXRot());
            control(player, buf);
        } finally {
            buf.release();
        }
    }

    private static void control(ServerPlayer player, FriendlyByteBuf buf) throws ReflectiveOperationException {
        var method = DroneServer.class.getDeclaredMethod("control", ServerPlayer.class, FriendlyByteBuf.class);
        method.setAccessible(true);
        method.invoke(null, player, buf);
    }

    private static void tick(ServerPlayer player) throws ReflectiveOperationException {
        var method = DroneServer.class.getDeclaredMethod("tick", TickEvent.PlayerTickEvent.class);
        method.setAccessible(true);
        method.invoke(null, new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
    }
}

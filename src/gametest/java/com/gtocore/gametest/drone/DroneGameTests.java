package com.gtocore.gametest.drone;

import com.gtocore.common.data.GTOItems;

import com.gtolib.drone.DroneCombat;
import com.gtolib.drone.DroneMagnet;
import com.gtolib.drone.DroneServer;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

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
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.mojang.authlib.GameProfile;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.netty.buffer.Unpooled;

import java.util.UUID;

@GameTestHolder("gtocore")
@PrefixGameTestTemplate(false)
public final class DroneGameTests {

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void magnetAndSwordAffixes(GameTestHelper helper) throws ReflectiveOperationException {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(new UUID(0, 0xD20E), "DroneEffects"));
        try {
            checkMagnet(helper, player);
            checkCombat(helper, player, InteractionHand.MAIN_HAND);
            checkCombat(helper, player, InteractionHand.OFF_HAND);
        } finally {
            DroneServer.stop(player);
            player.getAttributes().removeAttributeModifiers(player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND));
            player.getAttributes().removeAttributeModifiers(player.getOffhandItem().getAttributeModifiers(EquipmentSlot.OFFHAND));
            player.getInventory().clearContent();
        }
        helper.succeed();
    }

    private static void checkMagnet(GameTestHelper helper, ServerPlayer player) {
        var center = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2)));
        player.setPos(center.add(30, 0, 0));
        player.getInventory().clearContent();
        var inside = drop(helper, center.add(9, 0, 0), 4);
        var edge = drop(helper, center.add(0, 10, 0), 2);
        var corner = drop(helper, center.add(8, 0, 8), 3);
        var delayed = drop(helper, center, 5);
        delayed.setPickUpDelay(20);
        var owned = drop(helper, center, 6);
        owned.setTarget(new UUID(0, 0xBEEF));
        var magnet = new DroneMagnet();
        magnet.collect(player, center);
        helper.assertTrue(!inside.isAlive() && !edge.isAlive(), "Magnet collects within the sphere including its boundary");
        helper.assertTrue(player.getInventory().countItem(Items.DIAMOND) == 6, "Remote pickup goes to the player's inventory");
        helper.assertTrue(corner.isAlive() && delayed.isAlive() && owned.isAlive(), "Magnet preserves spherical range, delay and ownership");
        corner.discard();
        delayed.discard();
        owned.discard();
        for (int i = 0; i < player.getInventory().items.size(); i++) player.getInventory().items.set(i, new ItemStack(Items.STONE, 64));
        player.getInventory().items.set(0, new ItemStack(Items.DIAMOND, 63));
        var overflow = drop(helper, center, 4);
        magnet.collect(player, center);
        helper.assertTrue(overflow.isAlive() && overflow.getItem().getCount() == 3 && player.getInventory().items.get(0).getCount() == 64,
                "Partial pickup retains every item that does not fit");
        magnet.collect(player, center);
        helper.assertTrue(overflow.isAlive() && overflow.getItem().getCount() == 3, "A full inventory leaves items on the ground");
        overflow.discard();
        player.getInventory().clearContent();
        int experience = player.totalExperience;
        var insideXp = experience(helper, center.add(9, 0, 0), 7);
        var cornerXp = experience(helper, center.add(8, 0, 8), 5);
        player.takeXpDelay = 0;
        magnet.collect(player, center);
        helper.assertTrue(!insideXp.isAlive() && player.totalExperience == experience + 7,
                "Remote experience pickup credits the player");
        helper.assertTrue(cornerXp.isAlive(), "Experience pickup uses the same spherical range");
        var delayedXp = experience(helper, center, 3);
        magnet.collect(player, center);
        helper.assertTrue(delayedXp.isAlive(), "Remote experience pickup respects the player's cooldown");
        delayedXp.discard();
        cornerXp.discard();
        var damaged = new ItemStack(Items.DIAMOND_SWORD);
        damaged.enchant(Enchantments.MENDING, 1);
        damaged.setDamageValue(12);
        player.setItemInHand(InteractionHand.MAIN_HAND, damaged);
        var mendingXp = experience(helper, center, 7);
        player.takeXpDelay = 0;
        magnet.collect(player, center);
        helper.assertTrue(!mendingXp.isAlive() && damaged.getDamageValue() == 0 && player.totalExperience == experience + 8,
                "Remote experience pickup repairs Mending equipment before granting leftover experience");
        player.takeXpDelay = 0;
        player.getInventory().clearContent();
    }

    private static ExperienceOrb experience(GameTestHelper helper, Vec3 pos, int value) {
        var orb = new ExperienceOrb(helper.getLevel(), pos.x, pos.y, pos.z, value);
        helper.getLevel().addFreshEntity(orb);
        return orb;
    }

    private static ItemEntity drop(GameTestHelper helper, Vec3 pos, int count) {
        var item = new ItemEntity(helper.getLevel(), pos.x, pos.y, pos.z, new ItemStack(Items.DIAMOND, count));
        item.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    private static void checkCombat(GameTestHelper helper, ServerPlayer player, InteractionHand hand) throws ReflectiveOperationException {
        for (var drone : new ItemStack[] { GTOItems.HV_DRONE.asStack(), GTOItems.EV_DRONE.asStack(), GTOItems.IV_DRONE.asStack(), GTOItems.HYPERDIMENSIONAL_DRONE.asStack() }) {
            helper.assertTrue(LootCategory.forItem(drone) == LootCategory.SWORD, "Every handheld drone is an Apotheosis sword");
        }
        var drone = GTOItems.HV_DRONE.asStack();
        var affixData = drone.getOrCreateTagElement(AffixHelper.AFFIX_DATA);
        affixData.putString(AffixHelper.RARITY, "apotheosis:epic");
        var affixes = new net.minecraft.nbt.CompoundTag();
        affixes.putFloat("apotheosis:sword/attribute/violent", 1);
        affixes.putFloat("apotheosis:sword/mob_effect/weakening", 1);
        affixes.putFloat("apotheosis:telepathic", 1);
        affixData.put(AffixHelper.AFFIXES, affixes);
        helper.assertTrue(AffixHelper.getAffixes(drone).size() == 3, "Drone accepts sword damage, on-hit and loot affixes");
        GTCapabilityHelper.getElectricItem(drone).charge(100, Integer.MAX_VALUE, true, false);
        var other = new ItemStack(Items.DIAMOND_SWORD);
        player.getInventory().clearContent();
        player.setItemInHand(hand, drone);
        player.setItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, other);
        player.getAttributes().addTransientAttributeModifiers(player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND));
        player.getAttributes().addTransientAttributeModifiers(player.getOffhandItem().getAttributeModifiers(EquipmentSlot.OFFHAND));
        double originalDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double originalSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
        helper.assertTrue(DroneCombat.damage(player.getAttribute(Attributes.ATTACK_DAMAGE), 14) >= Integer.MAX_VALUE,
                "Ultimate drone damage is not truncated by the vanilla attribute limit");
        var pos = Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2)));
        player.setPos(pos);
        var target = EntityType.COW.create(helper.getLevel());
        target.setPos(pos.add(0, 0, 2));
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        target.setNoAi(true);
        helper.getLevel().addFreshEntity(target);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        helper.assertTrue(DroneServer.toggle(player, drone, hand), "Drone session starts");
        var control = DroneServer.class.getDeclaredMethod("control", ServerPlayer.class, FriendlyByteBuf.class);
        control.setAccessible(true);
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeByte(3).writeFloat(player.getYRot()).writeFloat(player.getXRot());
            control.invoke(null, player, buf);
        } finally {
            buf.release();
        }
        helper.assertTrue(target.getHealth() < 100, "Remote attack damages the target: " + hand);
        helper.assertTrue(target.hasEffect(MobEffects.WEAKNESS), "Remote attack triggers Apotheosis post-hit effects: " + hand);
        helper.assertTrue(target.getLastDamageSource() != null && target.getLastDamageSource().getMsgId().equals("drone"), "Remote attack keeps drone damage attribution");
        helper.assertTrue(player.getItemInHand(hand) == drone && player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND) == other,
                "Remote attack restores both original hands");
        helper.assertTrue(player.getAttributeValue(Attributes.ATTACK_DAMAGE) == originalDamage && player.getAttributeValue(Attributes.ATTACK_SPEED) == originalSpeed,
                "Remote attack restores both hands' attributes");
        DroneServer.stop(player);
        target.invulnerableTime = 0;
        target.setHealth(1);
        helper.assertTrue(DroneServer.toggle(player, drone, hand), "Drone restarts for the killing attack");
        buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeByte(3).writeFloat(player.getYRot()).writeFloat(player.getXRot());
            control.invoke(null, player, buf);
        } finally {
            buf.release();
        }
        helper.assertTrue(!target.isAlive(), "Drone killing attack succeeds");
        var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos, pos).inflate(4));
        helper.assertTrue(!drops.isEmpty(), "Drone kill generates loot");
        for (var loot : drops) {
            helper.assertTrue(loot.position().equals(player.position()), "Telepathic loot effect applies during drone kills: " + hand);
            loot.discard();
        }
        target.discard();
        DroneServer.stop(player);
        player.getAttributes().removeAttributeModifiers(player.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND));
        player.getAttributes().removeAttributeModifiers(player.getOffhandItem().getAttributeModifiers(EquipmentSlot.OFFHAND));
        player.getInventory().clearContent();
    }
}

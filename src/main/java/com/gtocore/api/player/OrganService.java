package com.gtocore.api.player;

import com.gtocore.common.data.GTODamageTypes;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.utils.OrganUtilsKt;

import com.gtolib.api.capability.IWirelessChargerInteraction;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.PlayerData;
import com.gtolib.api.player.attribute.PlayerAttributes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.gtolib.api.player.attribute.PlayerAttributes.FREE_MOV_STATE;

public class OrganService implements IOrganService {

    private static final double MOVEMENT_SPEED_FACTOR = 0.1D * 0.1D * 2.3D;

    @Override
    public void tick(ServerPlayer player) {
        PlayerData playerData = IEnhancedPlayer.of(player).getPlayerData();
        PlayerAttributes attributes = playerData.getPlayerAttributes();

        updateMovementSpeed(playerData, attributes);
        updateFreeMov(playerData, attributes);
        updateBlockReach(playerData, attributes);
        updateFlight(player, playerData, attributes);
        tickPlanetDamage(player, playerData);
        tickFood(player, playerData);
        tickLiverCleanse(player, playerData);
        tickWaterBreathing(player, playerData);
        updateArmor(playerData, attributes);
    }

    private static void updateMovementSpeed(PlayerData playerData, PlayerAttributes attributes) {
        for (int tier = 0; tier <= 4; tier++) {
            if (playerData.organTierCache.getInt(OrganType.LeftLeg) >= tier && playerData.organTierCache.getInt(OrganType.RightLeg) >= tier) {
                attributes.addNumericCurrent(PlayerAttributes.SPEED_ABLE, (float) (MOVEMENT_SPEED_FACTOR * tier));
            }
        }
    }

    private static void updateFreeMov(PlayerData playerData, PlayerAttributes attributes) {
        attributes.setBooleanAvailable(FREE_MOV_STATE, attributes.getBoolean(PlayerAttributes.FREE_MOV_STATE).isAvailable() || OrganUtilsKt.getSetOrganTier(playerData) >= 3);
    }

    private static void updateBlockReach(PlayerData playerData, PlayerAttributes attributes) {
        if (playerData.organTierCache.getInt(OrganType.RightArm) >= 2) {
            attributes.addNumericCurrent(PlayerAttributes.BLOCK_REACH, 4.0F);
        }
    }

    private static void updateFlight(ServerPlayer player, PlayerData playerData, PlayerAttributes attributes) {
        boolean shouldFly = OrganUtilsKt.getSetOrganTier(playerData) >= 4 || attributes.getBoolean(PlayerAttributes.WARDEN_STATE).getCurrent(); // 四级器官创造飞
        Map<OrganType, List<ItemStack>> organStacks = OrganUtilsKt.ktGetOrganStack(playerData);
        Collection<List<ItemStack>> values = organStacks.values();

        for (List<ItemStack> stacks : values) {
            for (ItemStack stack : stacks) {
                if (stack.getItem() == GTOOrganItems.INSTANCE.getFAIRY_WING().asItem() || stack.getItem() == GTOOrganItems.INSTANCE.getMANA_STEEL_WING().asItem()) {
                    if (tryUsingDurabilityWing(stack, player, attributes)) {
                        shouldFly = true;
                        finishFlightUpdate(shouldFly, attributes);
                        return;
                    }
                }
            }
        }

        for (List<ItemStack> stacks : values) {
            for (ItemStack stack : stacks) {
                if (stack.getItem() == GTOOrganItems.INSTANCE.getMECHANICAL_WING().asItem() && whenUsingElectricWing(stack, player, playerData, attributes)) {
                    shouldFly = true;
                    finishFlightUpdate(shouldFly, attributes);
                    return;
                }
            }
        }

        finishFlightUpdate(shouldFly, attributes);
    }

    private static void finishFlightUpdate(boolean shouldFly, PlayerAttributes attributes) {
        attributes.setBooleanCurrent(PlayerAttributes.WING_STATE, shouldFly);
        attributes.orBooleanCurrent(PlayerAttributes.CAN_FLY, shouldFly);
    }

    private static void tickPlanetDamage(ServerPlayer player, PlayerData playerData) {
        Planet planet = PlanetApi.API.getPlanet(player.level());
        if (planet == null || !player.gameMode.isSurvival()) return;
        if (GTODimensions.OVERWORLD == planet.dimension() || GTODimensions.GLACIO == planet.dimension()) return;
        if (!GTODimensions.isPlanet(planet.dimension())) return;

        int tier = planet.tier();
        int lowerTierTag = ((tier - 1) / 2) + 1;
        if (OrganUtilsKt.getSetOrganTier(playerData) >= lowerTierTag) return;

        Component customComponent = Component.translatable("gtocore.death.attack.turbulence_of_another_star", player.getName(), tier, "最低Tier " + lowerTierTag);
        float currentCount = playerData.floatCache.getOrDefault("try_attack_count", 0.0F) + 1.0F;

        player.hurt(GTODamageTypes.getGenericDamageSource(player, customComponent, () -> playerData.floatCache.put("try_attack_count", 0.0F)), currentCount);
        if (currentCount > 40.0F) {
            player.server.tell(new TickTask(1, player::kill));
            player.server.getPlayerList().broadcastSystemMessage(customComponent, true);
            playerData.floatCache.put("try_attack_count", 0.0F);
        } else {
            playerData.floatCache.put("try_attack_count", currentCount);
        }
    }

    private static void tickFood(ServerPlayer player, PlayerData playerData) {
        if (OrganUtilsKt.getSetOrganTier(playerData) < 3) return;
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        player.getFoodData().setExhaustion(0.0F);
    }

    private static void tickLiverCleanse(ServerPlayer player, PlayerData playerData) {
        if (playerData.organTierCache.getInt(OrganType.Liver) < 2) return;
        if (player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }
        if (player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
        }
    }

    private static void tickWaterBreathing(ServerPlayer player, PlayerData playerData) {
        if (playerData.organTierCache.getInt(OrganType.Lung) >= 2 && player.isInWater()) {
            player.setAirSupply(300);
        }
    }

    private static void updateArmor(PlayerData playerData, PlayerAttributes attributes) {
        int setOrganTier = OrganUtilsKt.getSetOrganTier(playerData);
        if (setOrganTier >= 1 && setOrganTier <= 4) {
            float armorValue = 5.0F * setOrganTier;
            attributes.addNumericCurrent(PlayerAttributes.ARMOR, armorValue);
            attributes.addNumericCurrent(PlayerAttributes.ARMOR_TOUGHNESS, armorValue);
        }
    }

    private static boolean whenUsingElectricWing(ItemStack stack, ServerPlayer player, PlayerData playerData, PlayerAttributes attributes) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;

        attributes.setNumericCurrent(PlayerAttributes.FLY_SPEED_ABLE, 0.25F);
        IWirelessChargerInteraction.charge(playerData.getNetMachine(), stack);
        if (player.getAbilities().flying && player.level().getBlockState(player.getOnPos().below()).getBlock() == Blocks.AIR) {
            electricItem.discharge(GTValues.V[GTValues.EV], electricItem.getTier(), true, false, false);
        }
        return true;
    }

    private static boolean tryUsingDurabilityWing(ItemStack stack, ServerPlayer player, PlayerAttributes attributes) {
        int durability = stack.getMaxDamage() - stack.getDamageValue();
        if (durability <= 0) return false;

        if (player.getAbilities().flying && player.level().getBlockState(player.getOnPos().below()).getBlock() == Blocks.AIR) {
            stack.hurtAndBreak(1, player, playerEntity -> playerEntity.sendSystemMessage(Component.translatable("gtocore.player.organ.you_wing_is_broken")));
        }
        attributes.setNumericCurrent(PlayerAttributes.FLY_SPEED_ABLE, 0.15F);
        return true;
    }
}

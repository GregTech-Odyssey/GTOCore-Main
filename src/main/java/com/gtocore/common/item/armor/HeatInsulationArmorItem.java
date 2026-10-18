package com.gtocore.common.item.armor;

import com.gtocore.client.renderer.item.HeatInsulationArmorRenderer;
import com.gtocore.common.data.GTODamageTypes;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.common.data.GTDamageTypes;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@DataGeneratorScanned
public final class HeatInsulationArmorItem extends ArmorItem {

    // Tint GTCEu hazmat's main cloth color (#A7704B) to the insulation theme (#5C2D06).
    public static final int TEXTURE_TINT = 0x8C6614;

    private static final String OUTER_TEXTURE = "gtceu:textures/armor/hazmat_1.png";
    private static final String INNER_TEXTURE = "gtceu:textures/armor/hazmat_2.png";
    private static final EquipmentSlot[] ARMOR_SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    @RegisterLanguage(cn = "每件减免25%蒸汽排气口、热铁锭等造成的热伤害", en = "Each piece reduces heat damage from steam vents, hot iron ingots, etc. by 25%")
    private static final String STEAM_PROTECTION = "gtocore.armor.heat_insulation.steam";
    @RegisterLanguage(cn = "每件减免15%加热器等造成的热浪伤害", en = "Each piece reduces heat wave damage from heaters, etc. by 15%")
    private static final String HEAT_PROTECTION = "gtocore.armor.heat_insulation.heat";

    public HeatInsulationArmorItem(Type type, Properties properties) {
        super(ArmorMaterials.CHAIN, type, properties);
    }

    public static void onLivingAttack(LivingAttackEvent event) {
        var entity = event.getEntity();
        var source = event.getSource();
        if (entity.level().isClientSide || event.getAmount() <= 0 || !source.is(GTDamageTypes.HEAT.key)) return;
        if (entity.isInvulnerableTo(source) || entity.isDeadOrDying() || entity.hasEffect(MobEffects.FIRE_RESISTANCE)) return;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof HeatInsulationArmorItem armor) || armor.getEquipmentSlot() != slot) return;
        }
        // Cancel before LivingEntity.hurt starts its hurt animation, sound and impact handling.
        event.setCanceled(true);
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            consumeDurability(entity.getItemBySlot(slot), entity, slot);
        }
    }

    public static void onLivingHurt(LivingHurtEvent event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide || event.getAmount() <= 0) return;
        var source = event.getSource();
        int reductionPerPiece;
        if (source.is(GTODamageTypes.MACHINE_HEAT_WAVE)) {
            reductionPerPiece = 15;
        } else if (source.is(GTDamageTypes.HEAT.key)) {
            reductionPerPiece = 25;
        } else {
            return;
        }
        int pieces = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            var stack = entity.getItemBySlot(slot);
            if (stack.getItem() instanceof HeatInsulationArmorItem armor && armor.getEquipmentSlot() == slot) {
                pieces++;
                consumeDurability(stack, entity, slot);
            }
        }
        if (pieces == 0) return;
        int remainingPercent = 100 - pieces * reductionPerPiece;
        event.setAmount(event.getAmount() * remainingPercent / 100.0F);
        if (remainingPercent == 0) event.setCanceled(true);
    }

    private static void consumeDurability(ItemStack stack, LivingEntity entity, EquipmentSlot slot) {
        if (!stack.isDamageableItem() || entity instanceof Player player && player.getAbilities().instabuild) return;
        int damage = stack.getDamageValue() + 1;
        if (entity instanceof ServerPlayer player) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(player, stack, damage);
        }
        stack.setDamageValue(damage);
        if (damage >= stack.getMaxDamage()) {
            entity.broadcastBreakEvent(slot);
            if (entity instanceof Player player) {
                player.awardStat(Stats.ITEM_BROKEN.get(stack.getItem()));
            }
            stack.shrink(1);
            stack.setDamageValue(0);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new HeatInsulationArmorRenderer());
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
        return slot == EquipmentSlot.LEGS ? INNER_TEXTURE : OUTER_TEXTURE;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> lines, @NotNull TooltipFlag advanced) {
        super.appendHoverText(stack, level, lines, advanced);
        lines.add(Component.translatable(STEAM_PROTECTION));
        lines.add(Component.translatable(HEAT_PROTECTION));
    }
}

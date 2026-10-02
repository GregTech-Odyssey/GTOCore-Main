package com.gtocore.common.item

import net.minecraft.util.RandomSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class RandomPositiveFoodItem(properties: Properties, nutrition: Int, saturation: Float) :
    Item(
        properties.food(
            FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationMod(saturation)
                .alwaysEat()
                .build(),
        ),
    ) {
    override fun finishUsingItem(stack: ItemStack, level: Level, entity: LivingEntity): ItemStack {
        val resultStack = super.finishUsingItem(stack, level, entity)
        if (!level.isClientSide) entity.addEffect(randomPositiveEffectInstance(level.getRandom()))
        return resultStack
    }

    private fun randomPositiveEffectInstance(rng: RandomSource): MobEffectInstance {
        val candidates = arrayOf(
            MobEffects.REGENERATION,
            MobEffects.DAMAGE_BOOST,
            MobEffects.MOVEMENT_SPEED,
            MobEffects.DIG_SPEED,
            MobEffects.ABSORPTION,
            MobEffects.JUMP,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.NIGHT_VISION,
            MobEffects.WATER_BREATHING,
            MobEffects.HEALTH_BOOST,
            MobEffects.LUCK,
        )
        val effect = candidates[rng.nextInt(candidates.size)]
        val duration = 20 * (10 + rng.nextInt(21)) // 10~30 秒
        val amplifier = rng.nextInt(2) // 0~1
        return MobEffectInstance(effect, duration, amplifier)
    }
}

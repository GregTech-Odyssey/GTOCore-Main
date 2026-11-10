package com.gtocore.api.data;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GTOFoodProperties {

    public final FoodProperties MAGNETIZED_GOLDEN_CARROT_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(1.2F)
            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 1200, 1), 1.0F)
            .build();
    public final FoodProperties MAGNETIZED_GOLDEN_APPLE_FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(1.2F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 2), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 1200, 0), 1.0F)
            .alwaysEat()
            .build();
    public final FoodProperties MAGNETIZED_GUANYIN_SOIL_FOOD = new FoodProperties.Builder()
            .nutrition(0)
            .saturationMod(0)
            .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 200, 0), 0.3f)
            .alwaysEat()
            .build();
    public final FoodProperties MAGNETIZED_MANA_COOKIE_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(2F)
            .effect(() -> new MobEffectInstance(MobEffects.SATURATION, 2400, 0), 1.0F)
            .alwaysEat()
            .build();
    public final FoodProperties MAGNETIZED_ENCHANTED_GOLDEN_APPLE_FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(1.2F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 400, 3), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 2), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 6), 1.0F)
            .alwaysEat()
            .build();
    public final FoodProperties MAGNETIZED_GOLDEN_MELON_SLICE_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(0.6F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
            .alwaysEat()
            .build();
}

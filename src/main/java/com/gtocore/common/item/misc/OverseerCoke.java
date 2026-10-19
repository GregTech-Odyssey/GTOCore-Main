package com.gtocore.common.item.misc;

import com.gtocore.common.data.GTOEffects;

import com.gtolib.api.GTOValues;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.registries.ItemBuilder;
import com.gtolib.utils.ColorUtils;
import com.gtolib.utils.TintableModelUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.FoodStats;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

import com.gto.registrate.Registrate;
import com.gto.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.common.data.GTItems.attach;
import static com.gtolib.utils.register.ItemRegisterUtils.item;

/**
 * 监工可乐与大瓶监工可乐：饮用后跳跃推进单方块机器的配方进度，大瓶作用于周围 5×5×5。
 */
@DataGeneratorScanned
public final class OverseerCoke {

    @RegisterLanguage(key = "effect.gtocore.large_mysterious_boost", cn = "机械之神降临", en = "Descent of the Machine God")
    public static final String LARGE_EFFECT_NAME = "effect.gtocore.large_mysterious_boost";
    @RegisterLanguage(cn = "饮用后在工作中的单方块机器上跳跃，可推进其配方进度", en = "After drinking, jumping on a working single-block machine advances its recipe progress")
    public static final String TOOLTIP_SINGLE = "gtocore.tooltip.item.overseer_coke.single";
    @RegisterLanguage(cn = "饮用后跳跃，可推进周围 5×5×5 范围内工作中的单方块机器的配方进度", en = "After drinking, jumping advances the recipe progress of working single-block machines within a 5×5×5 area")
    public static final String TOOLTIP_AREA = "gtocore.tooltip.item.overseer_coke.area";
    @RegisterLanguage(cn = "可加速的配方电压上限：%s", en = "Highest recipe voltage boosted: %s")
    public static final String TOOLTIP_TIER = "gtocore.tooltip.item.overseer_coke.tier";
    @RegisterLanguage(cn = "效果持续：%s 分钟", en = "Effect duration: %s min")
    public static final String TOOLTIP_DURATION = "gtocore.tooltip.item.overseer_coke.duration";

    private static final int RADIUS = 2;
    private static final int MAX_BOOST = 20 * 30;

    private OverseerCoke() {}

    @SuppressWarnings("unchecked")
    public static ItemEntry<ComponentItem>[] register(boolean large) {
        ItemEntry<ComponentItem>[] entries = new ItemEntry[GTValues.TIER_COUNT];
        String texture = large ? "item/skill/large_mysterious_boost_medicine/" : "item/skill/mysterious_boost_medicine/";
        for (int tier : GTValues.ALL_TIERS) {
            int minutes = (large ? 10 : 5) + (large ? 2 : 1) * tier;
            ItemBuilder<ComponentItem, Registrate> builder = large ?
                    item("gto_large_overseer_coke_" + GTValues.VN[tier].toLowerCase(), "GTO牌 " + GTOValues.VNFR[tier] + " 大瓶监工可乐", ComponentItem::create) :
                    item("gto_overseer_coke_" + GTValues.VN[tier].toLowerCase(), "GTO牌 " + GTOValues.VNFR[tier] + " 监工可乐", ComponentItem::create);
            builder.tips(Tips.feature(Component.translatable(large ? TOOLTIP_AREA : TOOLTIP_SINGLE)),
                    Tips.detail(Component.translatable(TOOLTIP_TIER, GTValues.VNF[tier])),
                    Tips.detail(Component.translatable(TOOLTIP_DURATION, minutes)));
            if (large) {
                builder.lang("GTO " + GTValues.VN[tier] + " Large Overseer Coke");
            }
            builder.model((ctx, prov) -> TintableModelUtils.createTintableModel(ctx, prov, large ?
                    new String[] { "item/skill/normal/normal_border", "item/skill/normal/tier_border",
                            texture + "0_bottle", texture + "1_face", texture + "2_liquid1", texture + "3_liquid2",
                            texture + "4_liquid3", texture + "5_gtologo", texture + "6_sparkle" } :
                    new String[] { "item/skill/normal/normal_border", "item/skill/normal/tier_border",
                            texture + "0_bottle", texture + "1_face", texture + "2_liquid1", texture + "3_liquid2",
                            texture + "4_liquid3", texture + "5_gtologo", texture + "6_gtologobase", texture + "7_straw" }))
                    .color(() -> () -> (stack, tintIndex) -> {
                        if (tintIndex == 1) {
                            return GTValues.VCM[tier];
                        }
                        if (tintIndex < 4 || tintIndex > 6) {
                            return -1;
                        }
                        RandomSource rng = GTValues.RNG;
                        return ColorUtils.generateStepGradient(rng.nextInt(0x1000000), rng.nextInt(0x1000000), 70, 3)[tintIndex - 4];
                    })
                    .onRegister(attach(new MysteriousBoostPotionBehaviour()))
                    .onRegister(attach(new FoodStats(new FoodProperties.Builder().effect(
                            () -> new MobEffectInstance(large ? GTOEffects.LARGE_MYSTERIOUS_BOOST.get() : GTOEffects.MYSTERIOUS_BOOST.get(), 20 * 60 * minutes, tier), 1.0f)
                            .alwaysEat()
                            .nutrition(tier)
                            .saturationMod(tier / 10f)
                            .build(), true, null)));
            entries[tier] = builder.register();
        }
        return entries;
    }

    public static void onJump(ServerPlayer player, ServerLevel level) {
        BlockPos onPos = player.getOnPos();
        MobEffectInstance large = player.getEffect(GTOEffects.LARGE_MYSTERIOUS_BOOST.get());
        if (large != null) {
            boost(level, onPos, RADIUS, large);
        }
        MobEffectInstance single = player.getEffect(GTOEffects.MYSTERIOUS_BOOST.get());
        if (single != null) {
            boost(level, onPos, 0, single);
        }
    }

    private static void boost(ServerLevel level, BlockPos center, int radius, MobEffectInstance effect) {
        int tier = effect.getAmplifier();
        int spent = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (!(MetaMachine.getMachine(level, pos) instanceof WorkableTieredMachine machine)) continue;
            RecipeLogic logic = machine.getRecipeLogic();
            if (!logic.isWorking()) continue;
            GTRecipe recipe = logic.getLastRecipe();
            if (recipe == null || RecipeHelper.getRecipeEUtTier(recipe) > tier) continue;
            int current = logic.getProgress();
            int max = logic.getMaxProgress();
            int progress = Math.min(current + Math.min(max / GTValues.RNG.nextInt(6, 10), MAX_BOOST), max - 1);
            if (progress <= current) continue;
            logic.setProgress(progress);
            spent = Math.max(spent, progress - current);
            level.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 3, 0.3, 0.2, 0.3, 0.02);
        }
        effect.duration -= spent;
    }
}

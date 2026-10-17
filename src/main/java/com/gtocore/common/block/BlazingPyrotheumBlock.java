package com.gtocore.common.block;

import com.gtocore.client.renderer.fx.SolarSurfaceFX;
import com.gtocore.common.data.GTODamageTypes;
import com.gtocore.common.data.GTOFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

public final class BlazingPyrotheumBlock extends LiquidBlock {

    public BlazingPyrotheumBlock(Properties properties) {
        super(GTOFluids.BLAZING_PYROTHEUM, properties.mapColor(MapColor.COLOR_ORANGE).strength(100.0f).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.animateTick(state, level, pos, random);
        SolarSurfaceFX.animateTick(level, pos, random);
    }

    @Override
    public void entityInside(@NotNull BlockState blockstate, @NotNull Level world, @NotNull BlockPos pos, @NotNull Entity entity) {
        super.entityInside(blockstate, world, pos, entity);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 15));
            livingEntity.hurt(GTODamageTypes.getBlazingPlasmaDamageSource(entity), 15);
        }
    }
}

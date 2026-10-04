package com.gtocore.mixin.mc;

import com.gtocore.integration.ae.SolarStormHandler;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelWeatherMixin extends Level {

    private ServerLevelWeatherMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess,
                                    Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler,
                                    boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    /** Solar weather inherits the overworld's flags, but the no-skylight branch skips intensity updates. */
    @Inject(method = "advanceWeatherCycle",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;dimensionType()Lnet/minecraft/world/level/dimension/DimensionType;"))
    private void gto$advanceSolarStormIntensity(CallbackInfo ci) {
        var level = (ServerLevel) (Object) this;
        if (level.dimension() != SolarStormHandler.SOLAR_SURFACE) return;
        // Run after vanilla captures the old isRaining(), before its packet broadcasts below the skylight branch.
        oRainLevel = rainLevel;
        oThunderLevel = thunderLevel;
        var data = level.getLevelData();
        rainLevel = Mth.clamp(rainLevel + (data.isRaining() ? 0.01F : -0.01F), 0.0F, 1.0F);
        thunderLevel = Mth.clamp(thunderLevel + (data.isThundering() ? 0.01F : -0.01F), 0.0F, 1.0F);
    }

    /** Cover natural weather, /weather and sleep, including the solar Level's inherited weather. */
    @WrapOperation(method = { "advanceWeatherCycle", "setWeatherParameters", "resetWeatherCycle" },
                   at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setRaining(Z)V"))
    private void gto$updateSolarStormConnections(ServerLevelData data, boolean raining, Operation<Void> original) {
        var level = (ServerLevel) (Object) this;
        boolean previous = data.isRaining();
        original.call(data, raining);
        if (previous == data.isRaining()) return;
        // DerivedLevelData reads the overworld's rain flag and ignores its own setRaining calls.
        // Dispatch from the Level that actually changed the flag, before AE can transfer resources.
        var solar = level.getServer().getLevel(SolarStormHandler.SOLAR_SURFACE);
        if (solar != null) SolarStormHandler.update(solar);
    }
}

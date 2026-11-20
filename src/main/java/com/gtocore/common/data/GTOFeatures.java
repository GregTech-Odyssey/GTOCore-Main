package com.gtocore.common.data;

import com.gtocore.common.worldgen.feature.MeteoriteFeature;
import com.gtocore.common.worldgen.feature.SolarSurfaceBlobFeature;

import com.gtolib.GTOCore;
import com.gtolib.mc.world.HangingColumnClusterFeature;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GTOFeatures {

    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, GTOCore.MOD_ID);

    public static final RegistryObject<MeteoriteFeature> METEORITE = FEATURES.register("meteorite", MeteoriteFeature::new);
    public static final RegistryObject<SolarSurfaceBlobFeature> SOLAR_SURFACE_BLOB = FEATURES.register("solar_surface_blob", SolarSurfaceBlobFeature::new);
    public static final RegistryObject<HangingColumnClusterFeature> HANGING_COLUMN_CLUSTER = FEATURES.register("hanging_column_cluster", HangingColumnClusterFeature::new);

    private GTOFeatures() {}

    public static void init(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}

package com.gtocore.api.data;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import earth.terrarium.adastra.common.registry.ModFluids;
import earth.terrarium.adastra.common.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

public class RocketFuels {

    public static final Item[] ROCKET = new Item[] {
            ModItems.TIER_1_ROCKET.get(),
            ModItems.TIER_2_ROCKET.get(),
            ModItems.TIER_3_ROCKET.get(),
            ModItems.TIER_4_ROCKET.get(),
            RegistriesUtils.getItem("ad_astra_rocketed:tier_5_rocket"),
            RegistriesUtils.getItem("ad_astra_rocketed:tier_6_rocket"),
            RegistriesUtils.getItem("ad_astra_rocketed:tier_7_rocket")
    };

    public static final Fluid[] FUEL = new Fluid[] {
            GTMaterials.RocketFuel.getFluid(),
            GTOMaterials.RocketFuelRp1.getFluid(),
            GTOMaterials.DenseHydrazineFuelMixture.getFluid(),
            GTOMaterials.RocketFuelCn3h7o3.getFluid(),
            GTOMaterials.RocketFuelH8n4c2o4.getFluid(),
            ModFluids.CRYO_FUEL.get(),
            GTOMaterials.StellarEnergyRocketFuel.getFluid() };

    public static final Dimension[] PLANETS;
    public static final Item[] drones = {
            GTOItems.SPACE_DRONE_MK1.asItem(), GTOItems.SPACE_DRONE_MK2.asItem(), GTOItems.SPACE_DRONE_MK3.asItem(),
            GTOItems.SPACE_DRONE_MK4.asItem(), GTOItems.SPACE_DRONE_MK5.asItem(), GTOItems.SPACE_DRONE_MK6.asItem()
    };
    public static final FluidStack[][] fuels = {
            { GTMaterials.RocketFuel.getFluid(10000), GTOMaterials.RocketFuelRp1.getFluid(6000) },
            { GTOMaterials.RocketFuelRp1.getFluid(10000), GTOMaterials.DenseHydrazineFuelMixture.getFluid(6000) },
            { GTOMaterials.DenseHydrazineFuelMixture.getFluid(10000), GTOMaterials.RocketFuelH8n4c2o4.getFluid(6000) },
            { GTOMaterials.RocketFuelCn3h7o3.getFluid(10000), GTOMaterials.RocketFuelH8n4c2o4.getFluid(6000) },
            { GTOMaterials.RocketFuelH8n4c2o4.getFluid(10000), new FluidStack(ModFluids.CRYO_FUEL.get(), 6000) },
            { new FluidStack(ModFluids.CRYO_FUEL.get(), 10000), GTOMaterials.StellarEnergyRocketFuel.getFluid(6000) }
    };

    static {
        List<Dimension> list = new ArrayList<>();
        GTODimensions.forEachPlanet(list::add);
        PLANETS = list.toArray(new Dimension[0]);
    }
}

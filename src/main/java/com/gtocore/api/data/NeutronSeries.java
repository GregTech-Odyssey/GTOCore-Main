package com.gtocore.api.data;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class NeutronSeries {

    public static final Map<Item, Integer> NEUTRON_SOURCES;
    public static final Map<Fluid, Integer> COOLANTS;
    public static final Map<Fluid, Fluid> COOLANT_OUTPUTS;

    static {
        ImmutableMap.Builder<Item, Integer> builder = ImmutableMap.builder();
        builder.put(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Graphite).getItem(), -1000);
        builder.put(ChemicalHelper.get(TagPrefix.dustSmall, GTMaterials.Graphite).getItem(), -250);
        builder.put(ChemicalHelper.get(TagPrefix.dustTiny, GTMaterials.Graphite).getItem(), -100);
        builder.put(ChemicalHelper.get(GTOTagPrefix.PARTICLE_SOURCE, GTOMaterials.AntinomyBerylliumSource).getItem(), 10);
        builder.put(ChemicalHelper.get(GTOTagPrefix.PARTICLE_SOURCE, GTOMaterials.PlutoniumBerylliumSource).getItem(), 100);
        builder.put(ChemicalHelper.get(GTOTagPrefix.PARTICLE_SOURCE, GTOMaterials.Californium252Source).getItem(), 1000);
        NEUTRON_SOURCES = builder.build();
        ImmutableMap.Builder<Fluid, Integer> builder1 = ImmutableMap.builder();
        builder1.put(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID), 80);
        builder1.put(GTOMaterials.LiquidNitrogen.getFluid(), 4);
        builder1.put(GTMaterials.DistilledWater.getFluid(), 1);
        COOLANTS = builder1.build();
        ImmutableMap.Builder<Fluid, Fluid> builder2 = ImmutableMap.builder();
        builder2.put(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID), GTMaterials.Helium.getFluid(FluidStorageKeys.GAS));
        builder2.put(GTOMaterials.LiquidNitrogen.getFluid(), GTMaterials.Nitrogen.getFluid());
        builder2.put(GTMaterials.DistilledWater.getFluid(), GTMaterials.Steam.getFluid());
        COOLANT_OUTPUTS = builder2.build();
    }
}

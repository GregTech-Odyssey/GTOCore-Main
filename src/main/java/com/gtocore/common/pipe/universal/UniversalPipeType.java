package com.gtocore.common.pipe.universal;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

public enum UniversalPipeType implements IPipeType<UniversalPipeProperties>, StringRepresentable {

    MANGANESE("锰", GTMaterials.Manganese, GTMaterials.Tin, GTMaterials.Lead),
    GALLIUM("镓", GTMaterials.Gallium, GTMaterials.Brass, GTMaterials.Bronze),
    BISMUTH_BRONZE("铋青铜", GTMaterials.BismuthBronze, GTMaterials.Electrum, GTMaterials.Steel),
    PALLADIUM("钯", GTMaterials.Palladium, GTMaterials.Platinum, GTMaterials.StainlessSteel),
    RHODIUM("铑", GTMaterials.Rhodium, GTMaterials.Ultimet, GTMaterials.Titanium),
    YTTRIUM_BARIUM_CUPRATE("钇钡铜氧", GTMaterials.YttriumBariumCuprate, GTMaterials.Osmiridium, GTMaterials.TungstenSteel);

    public static final ResourceLocation TYPE = GTOCore.id("universal");

    private static final long BASE_RATE = 64;
    private static final long MILLIBUCKETS_PER_BUCKET = 1000;

    public final String cnName;
    public final Material material;
    public final Material itemPipe;
    public final Material fluidPipe;
    public final UniversalPipeProperties properties;

    UniversalPipeType(String cnName, Material material, Material itemPipe, Material fluidPipe) {
        this.cnName = cnName;
        this.material = material;
        this.itemPipe = itemPipe;
        this.fluidPipe = fluidPipe;
        long rate = BASE_RATE << ordinal();
        this.properties = new UniversalPipeProperties(rate, rate * MILLIBUCKETS_PER_BUCKET);
    }

    @Override
    public float getThickness() {
        return 0.625F;
    }

    /** 本档每秒过多少桶流体（1 桶 = 1000 mB），供工具提示显示。 */
    public long bucketsPerSecond() {
        return properties.fluidThroughput() / MILLIBUCKETS_PER_BUCKET;
    }

    @Override
    public UniversalPipeProperties modifyProperties(UniversalPipeProperties baseProperties) {
        return properties;
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return material.getName();
    }
}

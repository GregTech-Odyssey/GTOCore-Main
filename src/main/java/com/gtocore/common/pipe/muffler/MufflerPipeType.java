package com.gtocore.common.pipe.muffler;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum MufflerPipeType implements IPipeType<MufflerPipeProperties>, StringRepresentable {

    NORMAL("常规", GTMaterials.Steel);

    public static final ResourceLocation TYPE = GTCEu.id("muffler");

    public final String cnName;
    public final Material material;

    MufflerPipeType(String cnName, Material material) {
        this.cnName = cnName;
        this.material = material;
    }

    @Override
    public float getThickness() {
        return 0.375F;
    }

    @Override
    public MufflerPipeProperties modifyProperties(MufflerPipeProperties baseProperties) {
        return baseProperties;
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}

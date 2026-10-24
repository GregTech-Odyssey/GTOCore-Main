package com.gtocore.common.pipe.steam;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.pipenet.IPipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum SteamPipeType implements IPipeType<SteamPipeProperties>, StringRepresentable {

    STEAM;

    public static final ResourceLocation TYPE = GTCEu.id("steam");

    @Override
    public float getThickness() {
        return 0.5F;
    }

    @Override
    public SteamPipeProperties modifyProperties(SteamPipeProperties baseProperties) {
        return SteamPipeProperties.INSTANCE;
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

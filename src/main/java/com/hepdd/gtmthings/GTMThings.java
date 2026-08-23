package com.hepdd.gtmthings;

import net.minecraft.resources.ResourceLocation;

import com.hepdd.gtmthings.data.*;
import lombok.experimental.UtilityClass;

import static net.minecraft.resources.ResourceLocation.tryBuild;

@UtilityClass
public class GTMThings {

    public static final String MOD_ID = "gtmthings";
    public static final String NAME = "GTM Things";

    public static ResourceLocation id(String name) {
        return tryBuild(MOD_ID, name);
    }
}

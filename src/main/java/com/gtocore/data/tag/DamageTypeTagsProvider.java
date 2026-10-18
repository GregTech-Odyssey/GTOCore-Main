package com.gtocore.data.tag;

import com.gtocore.common.data.GTODamageTypes;

import com.gtolib.GTOCore;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public final class DamageTypeTagsProvider extends TagsProvider<DamageType> {

    public DamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, GTOCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.IS_FIRE).add(GTODamageTypes.MACHINE_HEAT_WAVE);
        tag(DamageTypeTags.BYPASSES_ARMOR).add(GTODamageTypes.MACHINE_HEAT_WAVE);
    }
}

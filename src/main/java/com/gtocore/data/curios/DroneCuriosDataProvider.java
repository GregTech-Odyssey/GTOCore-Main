package com.gtocore.data.curios;

import com.gtolib.GTOCore;
import com.gtolib.drone.DroneSlot;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public final class DroneCuriosDataProvider extends CuriosDataProvider {

    public DroneCuriosDataProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(GTOCore.MOD_ID, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        createSlot(DroneSlot.CURIOS_SLOT)
                .size(1)
                .addCosmetic(false)
                .renderToggle(false)
                .icon(ResourceLocation.fromNamespaceAndPath(GTOCore.MOD_ID, "slot/empty_drone_slot"))
                .addValidator(ResourceLocation.fromNamespaceAndPath("curios", "tag"));
        createEntities("player").addPlayer().addSlots(DroneSlot.CURIOS_SLOT);
    }
}

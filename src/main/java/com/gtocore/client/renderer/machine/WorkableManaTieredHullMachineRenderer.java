package com.gtocore.client.renderer.machine;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.WorkableOverlayModel;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class WorkableManaTieredHullMachineRenderer extends ManaTieredHullMachineRenderer {

    private final WorkableOverlayModel overlayModel;

    public WorkableManaTieredHullMachineRenderer(int tier, ResourceLocation workableModel) {
        super(tier, GTOCore.id("block/machine/mana_hull_machine"));
        this.overlayModel = new WorkableOverlayModel(workableModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof IWorkable workable) {
            modelToUse(definition, machine).bakeQuads(side, modelState, workable.isActive(), workable.isWorkingEnabled())
                    .forEach(quad -> quads.add(Quad.from(quad, overlayQuadsOffset()).rebake()));
        } else {
            modelToUse(definition, machine).bakeQuads(side, modelState, false, false)
                    .forEach(quad -> quads.add(Quad.from(quad, overlayQuadsOffset()).rebake()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            overlayModel.registerTextureAtlas(register);
        }
    }

    /// predicate for the model to use based on the machine definition and meta machine
    protected WorkableOverlayModel modelToUse(MachineDefinition definition, @Nullable MetaMachine machine) {
        return overlayModel;
    }

    private static float overlayQuadsOffset() {
        return 0.002f;
    }
}

package com.gtocore.client.renderer.machine;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.client.bakedpipeline.Quad;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("removal")
public class OverlayManaTieredMachineRenderer extends ManaTieredHullMachineRenderer {

    private final IModelRenderer overlayModel;

    public OverlayManaTieredMachineRenderer(int tier, ResourceLocation overlayModel) {
        super(tier, GTOCore.id("block/machine/mana_hull_machine"));
        this.overlayModel = new IModelRenderer(overlayModel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        overlayModel.getRotatedModel(frontFacing).getQuads(definition.defaultBlockState(), side, rand)
                .forEach(quad -> quads.add(Quad.from(quad, overlayQuadsOffset()).rebake()));
    }

    private static float overlayQuadsOffset() {
        return 0.004f;
    }
}

package com.gtocore.client.renderer.machine;

import com.gtocore.client.renderer.fx.FXManager;
import com.gtocore.client.renderer.fx.SolarStormReactorFX;
import com.gtocore.common.machine.multiblock.electric.SolarStormAggregationReactor;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

public class SolarStormAggregationReactorRenderer extends WorkableCasingMachineRenderer {

    public SolarStormAggregationReactorRenderer() {
        super(GTOCore.id("block/casings/singularity_reinforced_stellar_shielding_casing"), GTCEu.id("block/multiblock/fusion_reactor"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (blockEntity.getLevel() != Minecraft.getInstance().level ||
                !(blockEntity instanceof MetaMachineBlockEntity holder) ||
                !(holder.getMetaMachine() instanceof SolarStormAggregationReactor machine) ||
                !machine.isFormed() || !machine.recipeLogic.isWorking())
            return;
        FXManager.upsertFX(machine, () -> new SolarStormReactorFX(machine), SolarStormReactorFX::refresh);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getViewDistance() {
        return 256;
    }
}

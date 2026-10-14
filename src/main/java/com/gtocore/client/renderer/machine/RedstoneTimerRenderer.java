package com.gtocore.client.renderer.machine;

import com.gtocore.common.machine.noenergy.RedstoneTimerMachine;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.MachineRenderer;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedstoneTimerRenderer extends MachineRenderer {

    private final IModelRenderer litModel;
    private final IModelRenderer pausedModel;

    public RedstoneTimerRenderer() {
        super(GTOCore.id("block/machine/redstone_timer/idle"));
        this.litModel = new IModelRenderer(GTOCore.id("block/machine/redstone_timer/lit"));
        this.pausedModel = new IModelRenderer(GTOCore.id("block/machine/redstone_timer/paused"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, @Nullable Direction modelFacing,
                              ModelState modelState) {
        if (machine instanceof RedstoneTimerMachine timer) {
            int visual = timer.getVisual();
            IModelRenderer model = (visual & RedstoneTimerMachine.VISUAL_RUNNING) == 0 ? pausedModel :
                    (visual & RedstoneTimerMachine.VISUAL_LIT) != 0 ? litModel : null;
            if (model != null) {
                quads.addAll(model.getRotatedModel(frontFacing).getQuads(definition.defaultBlockState(), side, rand));
                return;
            }
        }
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
    }
}

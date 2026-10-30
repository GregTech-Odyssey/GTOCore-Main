package com.gtocore.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.MachineRenderer;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/// 通用联合机器模型渲染：第一个模型作为基础模型，其余模型依次叠加渲染。
/// 用来把「所有机器共用的外壳/框架」拆成一个模型，各机器只保留自己独有的部分。
/// （原版模型的 parent 只在子模型没有 elements 时继承父模型，无法合并两套 elements，所以只能在渲染阶段拼接。）
public class CompositeMachineRenderer extends MachineRenderer {

    private final List<IModelRenderer> extraModels;

    public CompositeMachineRenderer(ResourceLocation baseModel, ResourceLocation... extraModels) {
        super(baseModel);
        this.extraModels = new ArrayList<>(extraModels.length);
        for (ResourceLocation model : extraModels) {
            this.extraModels.add(new IModelRenderer(model));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        var state = definition.defaultBlockState();
        for (IModelRenderer model : extraModels) {
            quads.addAll(model.getRotatedModel(frontFacing).getQuads(state, side, rand));
        }
    }
}

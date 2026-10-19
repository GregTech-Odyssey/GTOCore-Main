package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

public final class BedrockDrillingRigMachine extends ElectricMultiblockMachine {

    public BedrockDrillingRigMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean keepSubscribing() {
        return true;
    }

    private BlockPos bedrockPos() {
        return getPos().offset(0, -9, 0);
    }

    @Override
    public boolean checkConditions(RecipeHandlerUnit unit, @Nullable GTRecipeDefinition recipe) {
        var level = getLevel();
        if (level == null || !level.getBlockState(bedrockPos()).is(Blocks.BEDROCK)) {
            IdleReason.NO_BEDROCK.setReason(this);
            return false;
        }
        return super.checkConditions(unit, recipe);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        var level = getLevel();
        if (level != null && GTValues.RNG.nextInt(10) == 0 && level.getBlockState(bedrockPos()).is(Blocks.BEDROCK)) {
            level.setBlockAndUpdate(bedrockPos(), Blocks.AIR.defaultBlockState());
        }
    }
}

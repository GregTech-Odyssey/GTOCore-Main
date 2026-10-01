package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

public final class BedrockDrillingRigMachine extends ElectricMultiblockMachine implements IIssueProvider {

    public BedrockDrillingRigMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean keepSubscribing() {
        return true;
    }

    @Override
    public boolean checkConditions(RecipeHandlerUnit unit, @Nullable GTRecipeDefinition recipe) {
        Level level = getLevel();
        boolean value = false;
        if (level != null) {
            value = level.getBlockState(getPos().offset(0, -9, 0)).getBlock() == Blocks.BEDROCK;
            if (value && GTValues.RNG.nextInt(10) == 1) {
                level.setBlockAndUpdate(getPos().offset(0, -9, 0), Blocks.AIR.defaultBlockState());
            }
        }
        if (!value) {
            IdleReason.NO_BEDROCK.report(this, IssueStage.CONDITION, recipe);
            return false;
        }
        return super.checkConditions(unit, recipe);
    }

    @Override
    public void collectIssues(IssueSink sink) {
        Level level = getLevel();
        if (level != null && isFormed() && level.getBlockState(getPos().offset(0, -9, 0)).getBlock() != Blocks.BEDROCK) IdleReason.NO_BEDROCK.collect(sink);
    }
}

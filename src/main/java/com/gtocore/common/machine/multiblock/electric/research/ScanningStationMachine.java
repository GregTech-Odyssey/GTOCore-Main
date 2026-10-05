package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.TeamResearchSavedData;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.common.item.DataCrystalItem;
import com.gtocore.common.machine.multiblock.part.research.ResearchHolderMachine;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyTypes;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ScanningStationMachine extends ElectricMultiblockMachine {

    private ResearchHolderMachine objectHolder;

    @SaveToDisk
    private ResearchPoints researchPoints;

    public ScanningStationMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            if (part instanceof ResearchHolderMachine scanningHolder) {
                if (scanningHolder.getFrontFacing() != getFrontFacing()) {
                    onStructureInvalid();
                    return;
                }
                this.objectHolder = scanningHolder;
                // 添加物品流体处理器（包含扫描槽、催化剂槽和数据槽）
                addHandlerList(RecipeHandlerUnit.of(IO.IN, scanningHolder.getAsHandler()));
            }
        }

        // 必须同时有扫描部件
        if (objectHolder == null) {
            onStructureInvalid();
        }
    }

    @Override
    public boolean checkPattern() {
        boolean isFormed = super.checkPattern();
        if (isFormed && objectHolder != null && objectHolder.getFrontFacing() != getFrontFacing()) {
            onStructureInvalid();
        }
        return isFormed;
    }

    @Override
    public void onStructureInvalid() {
        if (objectHolder != null) {
            objectHolder.setLocked(false);
            objectHolder = null;
        }
        super.onStructureInvalid();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .setWorkingStatusKeys("gtceu.multiblock.idling", "gtceu.multiblock.work_paused",
                        "gtocore.machine.analysis")
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addWorkingStatusLine()
                .addProgressLineOnlyPercent(recipeLogic.getProgressPercent());
    }

    @Override
    public boolean matchRecipeOutput(GTRecipe recipe) {
        return true;
    }

    @Override
    public boolean handleRecipeInput(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (super.handleRecipeInput(unit, recipe)) {
            if (objectHolder != null) objectHolder.setLocked(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleRecipeOutput(GTRecipe originalRecipe) {
        if (researchPoints != null) {
            var teamData = TeamResearchSavedData.getOrCreateContext(getOwnerUUID());
            teamData.addResearchPoints(researchPoints);
            for (TechTreeManager manager : TechTreeManager.getManagers()) {
                manager.triggerAllResearchUnlock(getOwnerUUID());
            }
            researchPoints = null;
        }
        objectHolder.setLocked(false);
        return true;
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        var crystal = recipe.itemInputs.isEmpty() ? null : recipe.itemInputs.ingredient(0).displayKey();
        if (crystal instanceof AEItemKey crystalKey && crystalKey.getItem() instanceof DataCrystalItem item0) {
            unit.forEachKey(AEKeyTypes.ITEMS, false, (key, amount) -> {
                if (!(key instanceof AEItemKey itemKey) || itemKey.getItem() != item0) return false;
                researchPoints = DataCrystalItem.getResearchData(itemKey.toStack(1));
                return !researchPoints.isEmpty();
            });
        }
        if (researchPoints == null) {
            IdleReason.LACK_MATERIAL.setReason(this);
            return null;
        }
        return super.getRealRecipe(unit, recipe);
    }
}

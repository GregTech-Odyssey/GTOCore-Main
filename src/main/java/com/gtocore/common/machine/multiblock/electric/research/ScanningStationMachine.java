package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.common.item.DataCrystalItem;
import com.gtocore.common.machine.multiblock.part.ResearchHolderMachine;
import com.gtocore.data.recipe.research.AnalyzeData;

import com.gtolib.GTOCore;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ScanningStationMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder {

    private ResearchHolderMachine objectHolder;

    @SaveToDisk(saveNull = true)
    private ResearchPoints researchPoints;

    public ScanningStationMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            if (part instanceof ResearchHolderMachine scanningHolder) {
                if (scanningHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
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
        if (isFormed && objectHolder != null && objectHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
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
        var lastRecipe = getRecipeLogic().getLastRecipe();
        if (lastRecipe != null && researchPoints != null) {
            var teamData = TeamResearchSavedDtat.getOrCreateContext(getOwnerUUID());
            teamData.addResearchPoints(researchPoints);
            AnalyzeData.TechTree.triggerAllResearchUnlock(getOwnerUUID());
            researchPoints = null;
        }
        objectHolder.setLocked(false);
        return true;
    }

    @Override
    public @Nullable GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        AtomicReference<GTRecipeDefinition> recipeObjectHolder = new AtomicReference<>();
        unit.forEachItems(false, (stack, amount) -> {
            var item = stack.getItem();
            if (!(item instanceof DataCrystalItem item1)) return false;
            var tier = item1.tier;
            var data = DataCrystalItem.getResearchData(stack);
            if (!data.isEmpty()) {
                var recipe = RecipeBuilder.ofRaw().inputItems(stack.copyWithCount(1))
                        .duration(200 * GTOCore.difficulty).EUt(3L << (2 * tier + 11)).CWUt(16L << tier).durationIsTotalCWU(true)
                        .build();
                researchPoints = data;
                recipeObjectHolder.set(recipe);
                return true;
            }
            return false;
        });
        return recipeObjectHolder.get();
    }
}

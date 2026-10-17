package com.gtocore.common.machine.multiblock.steam;

import com.gtocore.api.machine.part.IHeatContainerPart;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentList;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@DataGeneratorScanned
public class LargeSteamCracker extends BaseSteamMultiblockMachine {

    @RegisterLanguage(cn = "温度并行上限", en = "Temperature Parallel Limit")
    private static final String PARALLEL_LIMIT = "gtocore.machine.steam_cracker.parallel_limit";
    @RegisterLanguage(cn = "原料效率：%s", en = "Raw Material Efficiency: %s")
    private static final String EFFICIENCY = "gtocore.machine.steam_cracker.efficiency";
    @RegisterLanguage(cn = "重度裂化产物占比：%s", en = "Severely Cracked Product Share: %s")
    private static final String SEVERE_SHARE = "gtocore.machine.steam_cracker.severe_share";
    @RegisterLanguage(cn = "连续完成批数", en = "Consecutive Completed Batches")
    private static final String COMPLETED_BATCHES = "gtocore.machine.steam_cracker.completed_batches";

    @SaveToDisk(defaultValue = "0")
    private int completedBatches;

    private IHeatContainerPart heatHatch;

    public LargeSteamCracker(MetaMachineBlockEntity holder) {
        super(holder, 16, 32, 1);
    }

    @Override
    boolean oc() {
        return true;
    }

    private double getTemperature() {
        return heatHatch == null ? 300 : heatHatch.getHeatContainer().getTemperature();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (var part : getParts()) {
            if (part instanceof IHeatContainerPart heatContainerPart) {
                heatHatch = heatContainerPart;
                break;
            }
        }
    }

    @Override
    protected int getParallelLimit() {
        return SteamCrackingPerformance.parallelLimit(getTemperature());
    }

    private int getCompletedBatches() {
        return getRecipeLogic().getTotalContinuousRunningTime() == 0 ? 0 : completedBatches;
    }

    private int getEfficiencyPercent() {
        return SteamCrackingPerformance.efficiencyPercent(getCompletedBatches());
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe r) {
        r.bake();
        var outputs = r.fluidOutputs;
        long total = (outputs.amount(0) + outputs.amount(1)) * getEfficiencyPercent() / 40;
        long severe = SteamCrackingPerformance.severeAmount(total, getTemperature());
        var builder = new ContentList.Builder(2);
        if (severe > 0) builder.add(outputs.ingredient(0), severe);
        if (total > severe) builder.add(outputs.ingredient(1), total - severe);
        // 先确定实际产物，再计算并行，确保同时检查两种流体的输出空间。
        r.fluidOutputs = builder.build();
        return super.getRealRecipe(unit, r);
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (getRecipeLogic().getTotalContinuousRunningTime() == 0) completedBatches = 0;
        super.beforeWorking(unit, recipe);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        completedBatches = Math.min(SteamCrackingPerformance.FULL_EFFICIENCY_BATCHES, getCompletedBatches() + 1);
    }

    @Override
    public void onWaiting() {
        completedBatches = 0;
        super.onWaiting();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isWorkingAllowed) completedBatches = 0;
        super.setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    public void onStructureInvalid() {
        completedBatches = 0;
        heatHatch = null;
        super.onStructureInvalid();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed() || MultiblockPage.isScreenText()) return;
        textList.add(Component.translatable("gtceu.multiblock.hpca.temperature", getTemperature()).append(" K"));
        textList.add(Component.translatable(EFFICIENCY, getEfficiencyPercent()).append("%"));
        textList.add(Component.translatable(SEVERE_SHARE, SteamCrackingPerformance.severeAmount(100, getTemperature())).append("%"));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading("gtceu.multiblock.hpca.temperature", MultiblockPage.decimalText(this::getTemperature, "K"));
        page.addNumber(PARALLEL_LIMIT, this::getParallelLimit, "");
        page.addReading(EFFICIENCY, MultiblockPage.percentText(this::getEfficiencyPercent));
        page.addReading(SEVERE_SHARE, MultiblockPage.percentText(() -> SteamCrackingPerformance.severeAmount(100, getTemperature())));
        page.addNumber(COMPLETED_BATCHES, this::getCompletedBatches, "");
    }
}

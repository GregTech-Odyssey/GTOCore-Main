package com.gtocore.common.machine.trait;

import com.gtocore.api.machine.part.IRadiationHatch;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import com.gtolib.api.machine.trait.MultiblockTrait;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@DataGeneratorScanned
public class RadioactivityTrait extends MultiblockTrait implements IIssueProvider {

    @RegisterLanguage(cn = "辐射剂量", en = "Radiation Dose")
    private static final String DOSE = "gtocore.machine.radioactivity_trait.dose";

    @SaveToDisk(defaultValue = "0")
    private int recipeRadioactivity;

    private final Set<IRadiationHatch> radiationHatchPartMachines = new ReferenceOpenHashSet<>();

    public RadioactivityTrait(IMultiblockTraitHolder machine) {
        super(machine);
    }

    @Override
    public void onPartScan(IMultiPart part) {
        if (part instanceof IRadiationHatch radiationHatchPartMachine) {
            radiationHatchPartMachines.add(radiationHatchPartMachine);
        }
    }

    @Override
    public void onStructureInvalid() {
        radiationHatchPartMachines.clear();
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (!MultiblockPage.isScreenText()) textList.add(Component.translatable("gtocore.recipe.radioactivity", getRecipeRadioactivity()));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addNumber(DOSE, this::getRecipeRadioactivity, "Sv");
    }

    @Override
    public GTRecipe modifyRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        recipeRadioactivity = recipe.data.getInt(GTORecipeDataKeys.RADIOACTIVITY);
        if (recipeRadioactivity > 0) {
            int current = getRecipeRadioactivity();
            if (outside(recipeRadioactivity, current)) {
                IdleReason.RADIATION.report(machine, IssueStage.MODIFIER, recipeRadioactivity, current, recipe.definition);
                return null;
            }
        }
        return recipe;
    }

    @Override
    public void collectIssues(IssueSink sink) {
        var recipe = IdleReason.issueRecipe(machine);
        if (recipe == null) return;
        int need = recipe.data.getInt(GTORecipeDataKeys.RADIOACTIVITY);
        if (need <= 0) return;
        int current = getRecipeRadioactivity();
        if (outside(need, current)) IdleReason.RADIATION.collect(sink, need, current);
    }

    @Override
    public void afterWorking() {
        recipeRadioactivity = 0;
        super.afterWorking();
    }

    protected int getRecipeRadioactivity() {
        int radioactivity = 0;
        for (IRadiationHatch partMachine : radiationHatchPartMachines) {
            radioactivity += partMachine.getRadioactivity();
        }
        return radioactivity;
    }

    private static boolean outside(int need, int radioactivity) {
        return radioactivity > need + 5 || radioactivity < need - 5;
    }
}

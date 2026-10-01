package com.gtocore.common.machine.multiblock.electric.space.spacestaion.recipe;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.multiblock.electric.space.spacestaion.RecipeExtension;
import com.gtocore.common.machine.trait.RadioactivityTrait;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@DataGeneratorScanned
public class SpaceBioResearchModule extends RecipeExtension implements IResearchPointsOperation {

    @SaveToDisk
    private final Trait radioactivityTrait;

    @SaveToDisk(defaultValue = "80")
    private int radioactivity = 80;

    public SpaceBioResearchModule(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
        radioactivityTrait = new Trait(this);
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (!isWorkspaceReady()) {
            setIdleReason(this::getWorkspaceNotReadyReason);
            return null;
        }
        if (recipe.data.containsKey(GTORecipeDataKeys.FILTER_CASING) && recipe.data.getInt(GTORecipeDataKeys.FILTER_CASING) > core.getTypes().size()) {
            IdleReason.INSUFFICIENT_CLEANROOM.setReason(this, recipe.data.getInt(GTORecipeDataKeys.FILTER_CASING), core.getTypes().size());
            return null;
        }
        if (recipe.definition.recipeType == GTORecipeTypes.BIO_RESEARCH_RECIPES) {
            if (!isWorkspaceReady()) {
                setIdleReason(this::getWorkspaceNotReadyReason);
                return null;
            }
            return RecipeModifier.OVERCLOCKING.applyModifier(this, unit, recipe);
        }
        return super.getRealRecipe(unit, recipe);
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        if (!MultiblockPage.isScreenText()) list.add(Component.translatable(LANGUAGE_SPACE_RADIATION_INTENSITY, radioactivity));
        super.customText(list);
    }

    @Override
    public void addControls(ControlPanel controls) {
        super.addControls(controls);
        controls.addInt(LANGUAGE_RADIATION_INTENSITY_CONTROL, () -> radioactivity, value -> radioactivity = value, 0, 80);
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        var result = super.handleTickRecipe(recipe);
        var intensity = recipe.data.getInt(GTORecipeDataKeys.RADIOACTIVITY_END);
        if (getProgress() == getMaxProgress() - 1 && intensity > 0 && outside(intensity)) {
            IdleReason.RADIATION.setReason(this, intensity, radioactivityTrait.getRecipeRadioactivity());
            return false;
        }
        return result;
    }

    @Override
    public void regressRecipe(RecipeLogic recipeLogic) {
        super.regressRecipe(recipeLogic);
        if (recipeLogic.getLastRecipe() != null) {
            var intensity = recipeLogic.getLastRecipe().data.getInt(GTORecipeDataKeys.RADIOACTIVITY_END);
            if (intensity > 0 && outside(intensity)) {
                recipeLogic.resetRecipeLogic();
            }
        }
    }

    private boolean outside(int intensity) {
        var r = radioactivityTrait.getRecipeRadioactivity();
        return r < intensity - 5 || r > intensity + 5;
    }

    private class Trait extends RadioactivityTrait {

        Trait(IMultiblockTraitHolder machine) {
            super(machine);
        }

        @Override
        public int getRecipeRadioactivity() {
            return super.getRecipeRadioactivity() + radioactivity;
        }
    }

    @RegisterLanguage(cn = "由环境维护舱提供的超净等级不足", en = "Insufficient cleanroom level provided by the Environmental Maintenance Module")
    private static final String LANGUAGE_INSUFFICIENT_CLEANROOM = "gtocore.machine.space_bio_research_module.insufficient_cleanroom";
    @RegisterLanguage(cn = "宇宙辐射强度: %s Sv", en = "Space Radiation Intensity: %s Sv")
    private static final String LANGUAGE_SPACE_RADIATION_INTENSITY = "gtocore.machine.space_bio_research_module.space_radiation_intensity";
    @RegisterLanguage(cn = "宇宙辐射强度（Sv）", en = "Space Radiation Intensity (Sv)")
    private static final String LANGUAGE_RADIATION_INTENSITY_CONTROL = "gtocore.machine.space_bio_research_module.radiation_intensity_control";
}

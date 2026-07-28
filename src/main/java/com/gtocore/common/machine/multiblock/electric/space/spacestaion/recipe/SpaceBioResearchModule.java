package com.gtocore.common.machine.multiblock.electric.space.spacestaion.recipe;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.multiblock.electric.space.spacestaion.RecipeExtension;
import com.gtocore.common.machine.trait.RadioactivityTrait;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IMultiblockTraitHolder;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import com.google.common.collect.ImmutableSet;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@DataGeneratorScanned
public class SpaceBioResearchModule extends RecipeExtension implements IResearchPointsOperation {

    @SaveToDisk
    private final Trait radioactivityTrait;

    @SaveToDisk
    private int radioactivity = 80;

    public SpaceBioResearchModule(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
        radioactivityTrait = new Trait(this);
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (!isWorkspaceReady()) {
            setIdleReason(IdleReason.CANNOT_WORK_IN_SPACE);
            return null;
        }
        if (recipe.data.containsKey(GTORecipeDataKeys.FILTER_CASING) && recipe.data.getInt(GTORecipeDataKeys.FILTER_CASING) > core.getTypes().size()) {
            setIdleReason(Component.translatable(LANGUAGE_INSUFFICIENT_CLEANROOM));
            return null;
        }
        if (recipe.definition.recipeType == GTORecipeTypes.BIO_RESEARCH_RECIPES) {
            if (!isWorkspaceReady()) {
                setIdleReason(IdleReason.CANNOT_WORK_IN_SPACE);
                return null;
            }
            return RecipeModifier.OVERCLOCKING.applyModifier(this, unit, recipe);
        }
        return super.getRealRecipe(unit, recipe);
    }

    @Override
    public Set<BlockPos> getModulePositions() {
        var pos = getPos();
        var fFacing = getFrontFacing();
        var uFacing = getUpwardsFacing();
        boolean isFlipped = isFlipped();
        var hallwayCenter = pos.relative(fFacing, 2).relative(RelativeDirection.LEFT.getRelative(fFacing, uFacing, isFlipped), 23);
        ImmutableSet.Builder<BlockPos> builder = ImmutableSet.builder();
        for (RelativeDirection dir : RelativeDirection.values()) {
            if (dir == RelativeDirection.RIGHT || dir == RelativeDirection.LEFT) continue;
            var newFFacing = dir.getRelative(fFacing, uFacing, isFlipped);
            var newUFacing = RelativeDirection.UP.getRelative(newFFacing, uFacing, isFlipped);
            var shiftedPos = hallwayCenter.relative(newFFacing, 12);
            builder.add(shiftedPos.relative(RelativeDirection.UP.getRelative(newFFacing, newUFacing, isFlipped), 2));
            builder.add(shiftedPos.relative(RelativeDirection.DOWN.getRelative(newFFacing, newUFacing, isFlipped), 2));
            builder.add(shiftedPos.relative(RelativeDirection.LEFT.getRelative(newFFacing, newUFacing, isFlipped), 2));
            builder.add(shiftedPos.relative(RelativeDirection.RIGHT.getRelative(newFFacing, newUFacing, isFlipped), 2));
        }
        return builder.build();
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        list.add(Component.translatable(LANGUAGE_SPACE_RADIATION_INTENSITY, radioactivity)
                .append(ComponentPanelWidget.withButton(Component.literal(" [-]"), "Sub"))
                .append(ComponentPanelWidget.withButton(Component.literal(" [+]"), "Add")));
        super.customText(list);
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            var amount = clickData.isCtrlClick ? 40 : (clickData.isShiftClick ? 8 : 1);
            radioactivity = Mth.clamp(radioactivity + ("Add".equals(componentData) ? amount : -amount), 0, 80);
        }
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        var result = super.handleTickRecipe(recipe);
        var intensity = recipe.data.getInt(GTORecipeDataKeys.RADIOACTIVITY_END);
        if (getProgress() == getMaxProgress() - 1 && intensity > 0 && outside(intensity)) {
            com.gtocore.data.IdleReason.RADIATION.setReason(this);
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
}

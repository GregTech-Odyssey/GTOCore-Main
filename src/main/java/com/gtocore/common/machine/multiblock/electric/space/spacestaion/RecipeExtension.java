package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.feature.multiblock.ICrossRecipeElectricMachine;
import com.gtolib.api.machine.trait.CrossRecipeTrait;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToLongFunction;

public class RecipeExtension extends Extension implements ICrossRecipeElectricMachine {

    private boolean hasLaserInput = false;
    @SaveToDisk
    private final CrossRecipeTrait crossRecipeTrait;

    @NotNull
    private ToLongFunction<RecipeExtension> parallel = MachineUtils::getHatchParallel;

    public RecipeExtension(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
        crossRecipeTrait = createCrossRecipeTrait();
    }

    @Override
    public void onPartScan(@NotNull IMultiPart iMultiPart) {
        super.onPartScan(iMultiPart);
        if (hasLaserInput) return;
        for (var partAbility : new PartAbility[] {
                PartAbility.INPUT_LASER, GTOPartAbility.OVERCLOCK_HATCH, GTOPartAbility.THREAD_HATCH }) {
            if (partAbility.isApplicable(iMultiPart.self().getBlockState().getBlock()))
                hasLaserInput = true;
        }
    }

    @Override
    public void onStructureFormed() {
        hasLaserInput = false;
        super.onStructureFormed();
    }

    @Override
    public @Nullable ICleanroomProvider getCleanroom() {
        return this;
    }

    public void setParallel(@NotNull ToLongFunction<RecipeExtension> parallel) {
        this.parallel = parallel;
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        crossRecipeTrait.attachConfigurators(configuratorPanel);
    }

    @Override
    public @Nullable GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        return null;
    }

    @Override
    public boolean searchRecipe() {
        return true;
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (!isWorkspaceReady()) {
            reportWorkspaceNotReady(this);
            return null;
        }
        if (hasLaserInput && !core.hasLaserBoost()) {
            IdleReason.TECH_NODE_LOCKED.report(this, IssueStage.MODIFIER, null);
            return null;
        }

        return ICrossRecipeElectricMachine.super.getRealRecipe(unit, RecipeModifier.multiplier(recipe, 1, core.getDurationMultiplier()));
    }

    @Override
    public CrossRecipeTrait getCrossRecipeTrait() {
        return crossRecipeTrait;
    }

    private CrossRecipeTrait createCrossRecipeTrait() {
        return new CrossRecipeTrait(this, false, true, machine -> parallel.applyAsLong((RecipeExtension) machine)) {

            @Override
            public double getOverclockFactor() {
                if (overclockHatchPartMachine == null) return 0.55;
                var mul = overclockHatchPartMachine.getCurrentMultiplier();
                if (isWorkspaceReady() && core.getServiceMachineMap().get(SpaceStationEnergyConversionModule.class) != null) {
                    return mul / (mul + 1.0);
                }
                return mul;
            }
        };
    }
}

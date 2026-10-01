package com.gtocore.common.recipe.condition;

import com.gtocore.api.machine.part.IVacuumPartMachine;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.feature.IVacuumMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.issue.IssueType;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.api.systems.OxygenApi;
import org.jetbrains.annotations.Nullable;

public final class VacuumCondition extends RecipeCondition {

    private final int tier;

    public VacuumCondition(int tier) {
        this.tier = tier;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtocore.recipe.vacuum.tier", tier);
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        MetaMachine machine = holder.self();

        if (machine instanceof MultiblockControllerMachine controllerMachine) {
            if (checkVacuumTier(controllerMachine.getParts())) {
                return true;
            }
        }

        for (Direction side : GTUtil.DIRECTIONS) {
            if (side.getAxis() != Direction.Axis.Y && checkNeighborVacuumTier(machine, side)) {
                return true;
            }
        }
        Level level = machine.getLevel();
        return !OxygenApi.API.hasOxygen(level, machine.getPos()) && PlanetApi.API.isSpace(level);
    }

    @Override
    public IssueType getIssueType() {
        return IdleReason.VACUUM_TIER.type();
    }

    @Override
    public void reportFailure(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe, int index) {
        holder.reportIssue(IdleReason.VACUUM_TIER.type(), IssueStage.CONDITION, IO.NONE, null, index, tier, -1, recipe);
    }

    @Override
    public @Nullable Component describeCurrent(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        return Component.translatable("gtocore.issue.current.vacuum", currentTier(holder.self()));
    }

    private static int currentTier(MetaMachine machine) {
        int best = 0;
        if (machine instanceof MultiblockControllerMachine controllerMachine) {
            for (IMultiPart part : controllerMachine.getParts()) {
                if (part instanceof IVacuumPartMachine vacuumMachine) best = Math.max(best, vacuumMachine.getVacuumTier());
            }
        }
        for (Direction side : GTUtil.DIRECTIONS) {
            if (side.getAxis() != Direction.Axis.Y && machine.getNeighborMachine(side) instanceof IVacuumMachine vacuumMachine) {
                best = Math.max(best, vacuumMachine.getVacuumTier());
            }
        }
        return best;
    }

    private boolean checkVacuumTier(IMultiPart[] parts) {
        for (IMultiPart part : parts) {
            if (part instanceof IVacuumPartMachine vacuumMachine && vacuumMachine.getVacuumTier() >= tier) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNeighborVacuumTier(MetaMachine machine, Direction side) {
        if (machine.getNeighborMachine(side) instanceof IVacuumMachine vacuumMachine) {
            return vacuumMachine.getVacuumTier() >= tier;
        }
        return false;
    }
}

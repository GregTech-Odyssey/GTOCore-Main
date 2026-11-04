package com.gtocore.common.machine.electric.beam;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.beam.BeamBatch;
import com.gtolib.api.beam.BeamRay;
import com.gtolib.api.beam.BeamRoutes;
import com.gtolib.api.beam.IBeamOperator;
import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.world.phys.Vec3;

import com.gto.datasynclib.annotations.SaveToDisk;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class BeamPolarizerMachine extends SimpleNoEnergyMachine implements IBeamOperator {

    @SaveToDisk(defaultValue = "0")
    private double polarizationAngleRad;

    public BeamPolarizerMachine(MetaMachineBlockEntity holder) {
        super(holder, 0, t -> 16000);
    }

    @Override
    public void route(BeamRay incoming, Vec3 hit, BeamRoutes outputs) {
        outputs.straight(incoming, hit);
    }

    @Override
    public void evaluate(BeamBatch batch) {
        for (int i = 0; i < batch.size(); i++) {
            var pass = batch.pass(i);
            pass.forward(0, 1);
            pass.output(0).rotate(polarizationAngleRad);
        }
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return super.createRecipeLogic(args);
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        polarizationAngleRad = BeamMachineUtils.normalizeAngle(recipe.data.getDouble(GTORecipeDataKeys.RAY_POLARIZATION));
        requestUpdate();
    }

    @Override
    public void afterWorking() {
        polarizationAngleRad = 0.0D;
        requestUpdate();
        super.afterWorking();
    }

    @Override
    public boolean hasAutoOutputItem() {
        return false;
    }

    private void requestUpdate() {
        if (getLevel() != null) requestRayBeamUpdate(getLevel(), getPos());
    }
}

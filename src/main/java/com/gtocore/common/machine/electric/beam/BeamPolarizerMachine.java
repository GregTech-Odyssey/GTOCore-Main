package com.gtocore.common.machine.electric.beam;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.beam.BeamNode;
import com.gtolib.api.beam.BeamPassContext;
import com.gtolib.api.beam.IBeamOperator;
import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.world.phys.Vec3;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

@Getter
public class BeamPolarizerMachine extends SimpleNoEnergyMachine implements IBeamOperator {

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private double polarizationAngleRad;

    public BeamPolarizerMachine(MetaMachineBlockEntity holder) {
        super(holder, 0, t -> 16000);
    }

    @Override
    public BeamNode operate(BeamPassContext context, BeamNode lastNode, Vec3 exactPos) {
        var properties = lastNode.propertiesSnapshot().copy();
        properties.polarization = BeamMachineUtils.normalizeAngle(properties.polarization + polarizationAngleRad);
        properties.intensity = (long) (properties.intensity * context.decay());
        return new BeamNode(exactPos.x, exactPos.y, exactPos.z, properties);
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return super.createRecipeLogic(args);
    }

    @Override
    public boolean findRecipe(GTRecipeType type, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle) {
        return super.findRecipe(type, canHandle);
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

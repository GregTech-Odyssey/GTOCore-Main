package com.gtocore.mixin.gtm.machine;

import com.gtocore.common.recipe.condition.HeatCondition;
import com.gtocore.common.recipe.condition.SpaceWorkspaceCondition;

import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;
import com.gtolib.api.machine.feature.ISpaceWorkspaceMachine;
import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.machine.heat.SolarHeatHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;

import com.gto.datasynclib.datastream.data.StringMapData;
import earth.terrarium.adastra.api.planets.PlanetApi;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@org.spongepowered.asm.mixin.Mixin(com.gregtechceu.gtceu.api.machine.WorkableTieredMachine.class)
public abstract class WorkableTieredMachineMixin extends MetaMachine implements IWorkInSpaceMachine, IEnhancedRecipeLogicMachine {

    @Unique
    private static final RecipeCondition[] GTO$EMPTY_RECIPE_CONDITIONS = new RecipeCondition[0];

    @Unique
    private ISpaceWorkspaceMachine gto$workspaceProvider;

    @Unique
    private SolarHeatHandler gto$solarHeat;

    @Unique
    private RecipeCondition[] gto$additionalRecipeConditions = GTO$EMPTY_RECIPE_CONDITIONS;

    public WorkableTieredMachineMixin(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ISpaceWorkspaceMachine getWorkspaceProvider() {
        return gto$workspaceProvider;
    }

    @Override
    public void setWorkspaceProvider(ISpaceWorkspaceMachine iSpaceWorkspaceMachine) {
        gto$workspaceProvider = iSpaceWorkspaceMachine;
    }

    @Override
    @Nullable
    public SolarHeatHandler getSolarHeatHandler() {
        return gto$solarHeat;
    }

    @Override
    public RecipeCondition[] getAdditionalRecipeConditions() {
        return gto$additionalRecipeConditions;
    }

    @Override
    public void writeCustomSaveData(StringMapData data) {
        super.writeCustomSaveData(data);
        if (gto$solarHeat != null) {
            var heatData = gto$solarHeat.getFieldDataManager().writeToData();
            if (!heatData.isNull()) data.put("gto$solarHeat", heatData);
        }
    }

    @Override
    public void readCustomSaveData(StringMapData data, int dataVersion) {
        super.readCustomSaveData(data, dataVersion);
        var heatData = data.get("gto$solarHeat");
        if (heatData == null || heatData.isNull()) return;
        if (gto$solarHeat == null) {
            gto$solarHeat = new SolarHeatHandler(getHolder());
            gto$solarHeat.setSideIOCondition(side -> true);
        }
        gto$solarHeat.getFieldDataManager().readFromData(heatData, dataVersion);
    }

    @Inject(method = "onLoad", at = @At("TAIL"), remap = false)
    private void gto$loadSolarHeat(CallbackInfo ci) {
        if (isOnSolarSurface()) {
            if (gto$solarHeat == null) {
                gto$solarHeat = new SolarHeatHandler(getHolder());
                gto$solarHeat.setSideIOCondition(side -> true);
            }
            gto$solarHeat.onLoad();
            gto$additionalRecipeConditions = PlanetApi.API.isSpace(self().getLevel()) ?
                    new RecipeCondition[] { HeatCondition.maximumMachineTemperature(800), new SpaceWorkspaceCondition(this) } :
                    new RecipeCondition[] { HeatCondition.maximumMachineTemperature(800) };
        } else if (PlanetApi.API.isSpace(self().getLevel())) {
            gto$additionalRecipeConditions = new RecipeCondition[] { new SpaceWorkspaceCondition(this) };
        } else {
            gto$additionalRecipeConditions = GTO$EMPTY_RECIPE_CONDITIONS;
        }
    }

    @Inject(method = "onUnload", at = @At("HEAD"), remap = false)
    private void gto$unloadSolarHeat(CallbackInfo ci) {
        if (gto$solarHeat != null) gto$solarHeat.onUnLoad();
    }
}

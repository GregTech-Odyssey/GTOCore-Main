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

import com.gto.datasynclib.annotations.Access;
import com.gto.datasynclib.annotations.Codec;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.data.ByteData;
import com.gto.datasynclib.datastream.data.Data;
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
    @SaveToDisk
    @Access(instanceAsValue = true)
    @Codec(writeToData = "gTOdyssey$writeToData", readFromData = "gTOdyssey$readFromData")
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

    @Unique
    private Data gTOdyssey$writeToData(SolarHeatHandler handler) {
        return ByteData.FALSE;
    }

    @Unique
    private SolarHeatHandler gTOdyssey$readFromData(Data data) {
        gto$solarHeat = new SolarHeatHandler(getHolder());
        gto$solarHeat.setSideIOCondition(side -> true);
        return gto$solarHeat;
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

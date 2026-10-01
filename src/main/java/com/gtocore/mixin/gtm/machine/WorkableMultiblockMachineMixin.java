package com.gtocore.mixin.gtm.machine;

import com.gtocore.common.recipe.condition.HeatCondition;
import com.gtocore.common.recipe.condition.SpaceWorkspaceCondition;

import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;
import com.gtolib.api.machine.feature.ISpaceWorkspaceMachine;
import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.machine.feature.multiblock.IEnhancedMultiblockMachine;
import com.gtolib.api.machine.heat.SolarHeatHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.server.level.ServerLevel;

import com.gto.datasynclib.datastream.data.StringMapData;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import earth.terrarium.adastra.api.planets.PlanetApi;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorkableMultiblockMachine.class)
public abstract class WorkableMultiblockMachineMixin extends MultiblockControllerMachine implements IWorkInSpaceMachine, IEnhancedRecipeLogicMachine {

    @Unique
    private static final RecipeCondition[] GTO$EMPTY_RECIPE_CONDITIONS = new RecipeCondition[0];

    @Shadow(remap = false)
    @Final
    protected List<ISubscription> traitSubscriptions;

    @Unique
    private ISpaceWorkspaceMachine gto$workspaceProvider;

    @Unique
    private SolarHeatHandler gto$solarHeat;

    @Unique
    private RecipeCondition[] gto$additionalRecipeConditions = GTO$EMPTY_RECIPE_CONDITIONS;

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

    protected WorkableMultiblockMachineMixin(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isOnSolarSurface()) {
            if (gto$solarHeat == null) {
                gto$solarHeat = new SolarHeatHandler(getHolder());
                gto$solarHeat.setSideIOCondition(side -> true);
            }
            gto$solarHeat.onLoad();
            gto$additionalRecipeConditions = PlanetApi.API.isSpace(self().getLevel()) ?
                    new RecipeCondition[] { HeatCondition.maximumMachineTemperature(1800), new SpaceWorkspaceCondition(this) } :
                    new RecipeCondition[] { HeatCondition.maximumMachineTemperature(1800) };
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

    @Inject(method = "onStructureFormed", at = @At("TAIL"), remap = false)
    private void onPartScan(CallbackInfo ci) {
        updateSolarDimensions();
        for (var part : parts) {
            if (this instanceof IEnhancedMultiblockMachine enhancedRecipeLogicMachine) {
                enhancedRecipeLogicMachine.onPartScan(part);
            }
        }
    }

    @Inject(method = "onStructureInvalid", at = @At("TAIL"), remap = false)
    private void gto$resetSolarDimensions(CallbackInfo ci) {
        var heat = getSolarHeatHandler();
        if (heat != null) heat.updateSolarDimensions(1, 1, 1);
    }

    @Override
    public void addHandlerList(RecipeHandlerUnit unit) {
        if (unit == RecipeHandlerUnit.NO_DATA || unit.handlerIO == IO.NONE || unit.allHandlers.length == 0) return;
        getCapabilitiesProxy().computeIfAbsent(unit.handlerIO, i -> new ArrayList<>()).add(unit);
        var list = getCapabilitiesFlat().computeIfAbsent(unit.handlerIO, i -> new ArrayList<>());
        for (var handler : unit.allHandlers) {
            if (list.contains(handler)) continue;
            list.add(handler);
        }
        if (this instanceof IEnhancedMultiblockMachine enhancedRecipeLogicMachine && (unit.itemHandlers.length > 0 || unit.fluidHandlers.length > 0)) {
            traitSubscriptions.add(unit.subscribe(() -> enhancedRecipeLogicMachine.onContentChanges(unit)));
            if (getLevel() instanceof ServerLevel serverLevel) {
                TaskHandler.enqueueTask(serverLevel, () -> enhancedRecipeLogicMachine.onContentChanges(unit));
            }
        }
    }
}

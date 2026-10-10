package com.gtocore.mixin.gtm.machine;

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

import com.gto.datasynclib.datastream.codec.ValueOps;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
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
import java.util.Map;

@SuppressWarnings("AddedMixinMembersNamePattern")
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
    public void writeCustomSaveData(Map<String, Object> data, ValueOps ops) {
        super.writeCustomSaveData(data, ops);
        if (gto$solarHeat != null) {
            var heatData = gto$solarHeat.getFieldDataManager().writeToValue(ops);
            if (!ops.isNull(heatData)) data.put("gto$solarHeat", heatData);
        }
    }

    @Override
    public void readCustomSaveData(Map<String, Object> data, ValueOps ops) {
        super.readCustomSaveData(data, ops);
        var heatData = data.get("gto$solarHeat");
        if (gto$solarHeat == null) {
            gto$solarHeat = new SolarHeatHandler.MultiblockSolarHeatHandler(getHolder());
        }
        if (ops.isNull(heatData)) return;
        gto$solarHeat.getFieldDataManager().readFromValue(heatData, ops);
    }

    protected WorkableMultiblockMachineMixin(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isOnSolarSurface()) {
            if (gto$solarHeat == null) {
                gto$solarHeat = new SolarHeatHandler.MultiblockSolarHeatHandler(getHolder());
            }
            gto$solarHeat.onLoad();
        }
        gto$additionalRecipeConditions = IWorkInSpaceMachine.getAdditionalRecipeConditionsForMachine(this).toArray(new RecipeCondition[0]);
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
        if (heat instanceof SolarHeatHandler.MultiblockSolarHeatHandler h) h.updateSolarDimensions(1, 1, 1);
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

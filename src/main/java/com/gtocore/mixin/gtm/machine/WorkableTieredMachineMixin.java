package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.feature.IEnhancedRecipeLogicMachine;
import com.gtolib.api.machine.feature.ISpaceWorkspaceMachine;
import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.machine.heat.SolarHeatHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;

import net.minecraft.core.Direction;

import com.gto.datasynclib.datastream.codec.ValueOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@SuppressWarnings("AddedMixinMembersNamePattern")
@org.spongepowered.asm.mixin.Mixin(com.gregtechceu.gtceu.api.machine.WorkableTieredMachine.class)
public abstract class WorkableTieredMachineMixin extends TieredEnergyMachine implements IWorkInSpaceMachine, IEnhancedRecipeLogicMachine {

    @Unique
    private static final RecipeCondition[] GTO$EMPTY_RECIPE_CONDITIONS = new RecipeCondition[0];

    @Unique
    private ISpaceWorkspaceMachine gto$workspaceProvider;

    @Unique
    private SolarHeatHandler gto$solarHeat;

    @Unique
    private RecipeCondition[] gto$additionalRecipeConditions = GTO$EMPTY_RECIPE_CONDITIONS;

    public WorkableTieredMachineMixin(MetaMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
    }

    // TieredEnergyMachine's class method takes precedence over the heat interface's default method.
    @Override
    public @Nullable <T> Object getGTCapability(@NotNull Class<T> cap, @Nullable Direction side) {
        if (cap == IHeatContainer.class) {
            var heat = getHeatContainer();
            if (heat != null) {
                return testHeatCapability(side) ? heat : GTCapability.EMPTY;
            }
        }
        return super.getGTCapability(cap, side);
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
            gto$solarHeat = new SolarHeatHandler.SimpleSolarHeatHandler(getHolder());
        }
        if (ops.isNull(heatData)) return;
        gto$solarHeat.getFieldDataManager().readFromValue(heatData, ops);
    }

    @Inject(method = "onLoad", at = @At("TAIL"), remap = false)
    private void gto$loadSolarHeat(CallbackInfo ci) {
        if (isOnSolarSurface()) {
            if (gto$solarHeat == null) {
                gto$solarHeat = new SolarHeatHandler.SimpleSolarHeatHandler(getHolder());
            }
            gto$solarHeat.onLoad();
        }
        gto$additionalRecipeConditions = IWorkInSpaceMachine.getAdditionalRecipeConditionsForMachine(this).toArray(new RecipeCondition[0]);
    }

    @Inject(method = "onUnload", at = @At("HEAD"), remap = false)
    private void gto$unloadSolarHeat(CallbackInfo ci) {
        if (gto$solarHeat != null) gto$solarHeat.onUnLoad();
    }
}

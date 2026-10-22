package com.gtocore.common.machine.trait;

import com.gtocore.common.machine.multiblock.electric.voidseries.INFFluidDrillMachine;

import com.gtolib.api.machine.impl.DrillingControlCenterMachine;
import com.gtolib.api.machine.trait.IFluidDrillLogic;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.data.worldgen.bedrock.BedrockVeinEntry;
import com.gregtechceu.gtceu.api.data.worldgen.bedrock.BedrockVeinKind;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.machine.trait.BedrockVeinDrillLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.trait.FluidDrillLogic;
import com.gregtechceu.gtceu.uiwidgets.recipe.RecipeIOList;

import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public final class INFFluidDrillLogic extends BedrockVeinDrillLogic<BedrockFluidDefinition> implements IFluidDrillLogic {

    private DrillingControlCenterMachine cache;

    public INFFluidDrillLogic(INFFluidDrillMachine machine) {
        super(machine, BedrockVeinKind.FLUID);
    }

    @Override
    public INFFluidDrillMachine getMachine() {
        return (INFFluidDrillMachine) super.getMachine();
    }

    @Override
    protected boolean canDrill() {
        return true;
    }

    @Override
    protected int rigMultiplier() {
        return getMachine().getBasis();
    }

    @Override
    protected int produced(BedrockVeinEntry<BedrockFluidDefinition> entry) {
        int produced = super.produced(entry);
        DrillingControlCenterMachine machine = getNetMachine();
        return machine == null ? produced : (int) (produced * machine.getMultiplier());
    }

    @Override
    protected GTRecipe buildDrillRecipe(BedrockFluidDefinition vein, int amount) {
        return RecipeBuilder.ofRaw().outputFluids(new FluidStack(vein.fluid(), amount)).duration(MAX_PROGRESS).EUt(drillEUt()).buildRawRecipe();
    }

    @Override
    protected void addExpectedOutputs(List<RecipeIOList.Entry> outputs, BedrockFluidDefinition vein, int produced) {
        FluidDrillLogic.expectedOutputs(outputs, vein, produced);
    }

    @Override
    protected void onDrillFinish() {}

    @Override
    public DrillingControlCenterMachine getNetMachineCache() {
        return cache;
    }

    @Override
    public void setNetMachineCache(DrillingControlCenterMachine cache) {
        this.cache = cache;
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        this.cache = null;
    }
}

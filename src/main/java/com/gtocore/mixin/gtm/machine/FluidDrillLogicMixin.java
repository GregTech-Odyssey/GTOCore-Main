package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.machine.impl.DrillingControlCenterMachine;
import com.gtolib.api.machine.trait.IFluidDrillLogic;

import com.gregtechceu.gtceu.api.data.worldgen.bedrock.BedrockVeinEntry;
import com.gregtechceu.gtceu.api.data.worldgen.bedrock.BedrockVeinKind;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IVeinDrillMachine;
import com.gregtechceu.gtceu.api.machine.trait.BedrockVeinDrillLogic;
import com.gregtechceu.gtceu.common.machine.trait.FluidDrillLogic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FluidDrillLogic.class)
public abstract class FluidDrillLogicMixin extends BedrockVeinDrillLogic<BedrockFluidDefinition> implements IFluidDrillLogic {

    @Unique
    private DrillingControlCenterMachine gtolib$cache;

    protected FluidDrillLogicMixin(IVeinDrillMachine machine, BedrockVeinKind<BedrockFluidDefinition> kind) {
        super(machine, kind);
    }

    @Override
    protected int produced(BedrockVeinEntry<BedrockFluidDefinition> entry) {
        int produced = super.produced(entry);
        DrillingControlCenterMachine machine = getNetMachine();
        return machine == null ? produced : (int) (produced * machine.getMultiplier());
    }

    @Override
    @SuppressWarnings("all")
    public DrillingControlCenterMachine getNetMachineCache() {
        return gtolib$cache;
    }

    @Override
    @SuppressWarnings("all")
    public void setNetMachineCache(DrillingControlCenterMachine cache) {
        this.gtolib$cache = cache;
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        removeNetMachineCache();
    }
}

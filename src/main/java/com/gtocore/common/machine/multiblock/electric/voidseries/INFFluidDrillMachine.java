package com.gtocore.common.machine.multiblock.electric.voidseries;

import com.gtocore.common.machine.trait.INFFluidDrillLogic;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IVeinDrillMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.uiwidgets.vein.VeinDrillReadouts;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class INFFluidDrillMachine extends ElectricMultiblockMachine implements IVeinDrillMachine {

    private final int t;
    @Getter
    private final int basis;

    public INFFluidDrillMachine(MetaMachineBlockEntity holder, int tier, int basis) {
        super(holder);
        this.t = tier;
        this.basis = basis;
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new INFFluidDrillLogic(this);
    }

    @Override
    public INFFluidDrillLogic getRecipeLogic() {
        return (INFFluidDrillLogic) super.getRecipeLogic();
    }

    @Override
    public int getDrillTier() {
        return t;
    }

    @Override
    public int getEnergyTier() {
        return Math.min(t + 1, tier);
    }

    @Override
    protected boolean hasParallelReadout() {
        return false;
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        VeinDrillReadouts.add(page, getRecipeLogic(), true);
    }

    @Override
    public int getTier() {
        return this.t;
    }
}

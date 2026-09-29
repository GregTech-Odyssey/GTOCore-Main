package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.config.GTORules;

import com.gtolib.api.annotation.Scanned;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.fluids.PropertyFluidFilter;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.MultiblockTankMachine;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

@Scanned
public class LargeSteamStorageTank extends MultiblockTankMachine {

    public LargeSteamStorageTank(MetaMachineBlockEntity holder, Object... args) {
        super(holder, GTORules.STEAM_TANK_CAPACITY.get(), new MyPropertyFluidFilter(), args);
    }

    private static final class MyPropertyFluidFilter extends PropertyFluidFilter {

        private MyPropertyFluidFilter() {
            super(true, false);
        }

        @Override
        public boolean test(@NotNull FluidStack stack) {
            return stack.getFluid() == GTMaterials.Steam.getFluid();
        }
    }
}

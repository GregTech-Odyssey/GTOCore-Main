package com.gtocore.common.machine.multiblock.storage;

import com.gtocore.config.GTORules;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.fluids.PropertyFluidFilter;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.primitive.MultiblockTankMachine;

import appeng.api.stacks.AEFluidKey;

import org.jetbrains.annotations.NotNull;

public class LargeSteamStorageTank extends MultiblockTankMachine {

    public LargeSteamStorageTank(MetaMachineBlockEntity holder, Object... args) {
        super(holder, GTORules.STEAM_TANK_CAPACITY.get(), new MyPropertyFluidFilter(), args);
    }

    private static final class MyPropertyFluidFilter extends PropertyFluidFilter {

        private MyPropertyFluidFilter() {
            super(true, false);
        }

        @Override
        public boolean test(@NotNull AEFluidKey stack) {
            return stack.getFluid() == GTMaterials.Steam.getFluid();
        }
    }
}

package com.gtocore.common.machine.multiblock.part;

import com.gtocore.common.data.GTOMachines;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.uipro.styletemplate.MachineEra;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine.IS_STEEL;

public class SteamFluidHatchPartMachine extends FluidHatchPartMachine {

    public SteamFluidHatchPartMachine(MetaMachineBlockEntity holder, IO io) {
        super(holder, 1, io, 8000, 1);
    }

    @Override
    public @Nullable ResourceLocation getWindowSkin() {
        return MachineEra.steam(IS_STEEL).getSkin();
    }

    @Override
    protected @NotNull NotifiableInventory<AEItemKey> createCircuitItemHandler(Object @NotNull... args) {
        return NotifiableInventory.empty(this, AEKeyType.items());
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.superAttachConfigurators(configuratorPanel);
    }

    @Override
    public boolean swapIO() {
        BlockPos blockPos = getHolder().pos();
        MachineDefinition newDefinition = null;
        if (io == IO.IN) {
            newDefinition = GTOMachines.STEAM_FLUID_OUTPUT_HATCH;
        } else if (io == IO.OUT) {
            newDefinition = GTOMachines.STEAM_FLUID_INPUT_HATCH;
        }
        if (newDefinition == null) return false;
        BlockState newBlockState = newDefinition.get().defaultBlockState();
        getLevel().setBlockAndUpdate(blockPos, newBlockState);
        if (getLevel().getBlockEntity(blockPos) instanceof MetaMachineBlockEntity newHolder) {
            if (newHolder.getMetaMachine() instanceof FluidHatchPartMachine newMachine) {
                newMachine.setFrontFacing(this.getFrontFacing());
                newMachine.setUpwardsFacing(this.getUpwardsFacing());
                newMachine.setPaintingColor(this.getPaintingColor());
                var from = this.tank.storage;
                var to = newMachine.tank.storage;
                for (int i = 0; i < from.size() && i < to.size(); i++) {
                    to.set(i, from.keyAt(i), from.amountAt(i));
                }
            }
        }
        return true;
    }
}

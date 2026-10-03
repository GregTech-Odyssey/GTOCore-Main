package com.gtocore.common.machine.multiblock.electric;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistillationTower;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationTowerMachine extends ElectricMultiblockMachine implements IDistillationTower {

    @Getter
    private final List<RecipeHandlerUnit> fluidOutputs = new ArrayList<>();

    public DistillationTowerMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public Comparator<IMultiPart> getPartSorter() {
        return Comparator.comparingInt(p -> p.self().getPos().getY());
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        fluidOutputs.clear();
        addOutputs();
    }

    @Override
    public void onStructureInvalid() {
        fluidOutputs.clear();
        super.onStructureInvalid();
    }

    @Override
    public int getYOffset() {
        return 1;
    }
}

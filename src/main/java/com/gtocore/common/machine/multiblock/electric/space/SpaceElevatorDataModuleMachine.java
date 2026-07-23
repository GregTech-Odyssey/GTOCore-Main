package com.gtocore.common.machine.multiblock.electric.space;

import com.gtocore.common.machine.multiblock.part.research.SimpleResearchTagPartMachine;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpaceElevatorDataModuleMachine extends SpaceElevatorModuleMachine {

    private @Nullable SimpleResearchTagPartMachine researchTagPartMachine;

    private static final int WORKS_PER_PRODUCED_DATA = GTOCore.isExpert() ? 75 : 50;

    @SyncToClient
    @SaveToDisk
    private long moduleWorks = 0;

    public SpaceElevatorDataModuleMachine(MetaMachineBlockEntity holder, boolean powerModuleTier) {
        super(holder, powerModuleTier);
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof SimpleResearchTagPartMachine researchTagPart) {
            this.researchTagPartMachine = researchTagPart;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.researchTagPartMachine = null;
    }

    private int getWorksPerProducedData() {
        return WORKS_PER_PRODUCED_DATA * 4 / (3 + getSpaceElevatorTier());
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        ((SpaceElevatorMachine) controller).dataModules.add(this);
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        ((SpaceElevatorMachine) controller).dataModules.remove(this);
        super.removedFromController(controller);
    }

    void addModuleWorks(long works) {
        moduleWorks += works;
        if (getController() != null && moduleWorks >= getWorksPerProducedData()) {
            // Calculate the amount of data to add
            var data = moduleWorks / getWorksPerProducedData();
            moduleWorks %= getWorksPerProducedData();
            if (researchTagPartMachine != null) {
                researchTagPartMachine.addData(data * (getController().netMachineCache == null ? 1 : 2));
            }
        }
    }
}

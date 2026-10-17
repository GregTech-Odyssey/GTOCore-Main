package com.gtocore.common.machine.multiblock.part.maintenance;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.DummyCleanroom;
import com.gregtechceu.gtceu.common.machine.multiblock.part.AutoMaintenanceHatchPartMachine;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CMHatchPartMachine extends AutoMaintenanceHatchPartMachine {

    public static final ICleanroomProvider DUMMY_CLEANROOM = DummyCleanroom.create(1);
    public static final ICleanroomProvider STERILE_DUMMY_CLEANROOM = DummyCleanroom.create(2);
    public static final ICleanroomProvider LAW_DUMMY_CLEANROOM = DummyCleanroom.create(3);

    private final ICleanroomProvider cleanroomTypes;

    public CMHatchPartMachine(MetaMachineBlockEntity metaTileEntityId,
                              ICleanroomProvider cleanroomTypes) {
        super(metaTileEntityId);
        this.cleanroomTypes = cleanroomTypes;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ICleanroomReceiver receiver) {
            receiver.setCleanroom(cleanroomTypes);
        }
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        if (controller instanceof ICleanroomReceiver receiver && receiver.getCleanroom() == cleanroomTypes) {
            receiver.setCleanroom(null);
        }
    }
}

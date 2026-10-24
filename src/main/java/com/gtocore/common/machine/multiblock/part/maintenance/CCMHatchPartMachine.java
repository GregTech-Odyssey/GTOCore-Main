package com.gtocore.common.machine.multiblock.part.maintenance;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.uiwidgets.display.MachineScreen;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CCMHatchPartMachine extends ACMHatchPartMachine {

    private final ICleanroomProvider cleanroomTypes;

    public CCMHatchPartMachine(MetaMachineBlockEntity metaTileEntityId,
                               ICleanroomProvider cleanroomTypes) {
        super(metaTileEntityId);
        this.cleanroomTypes = cleanroomTypes;
    }

    @Override
    protected void addReadouts(MachineScreen screen) {
        super.addReadouts(screen);
        var name = ICleanroomProvider.getCleanroomTooltip(cleanroomTypes.getCleanroomTier());
        screen.addLine(READ_CLEANROOM, () -> name);
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

    @RegisterLanguage(cn = "提供洁净环境", en = "Provides Cleanroom Environment")
    private static final String TIP_CLEANROOM = "gtocore.machine.configurable_maintenance.cleanroom";

    public static TipSpec tips(ICleanroomProvider cleanroom) {
        return tipsBuilder().group(Component.translatable(TIP_CLEANROOM), l -> {
            for (int tier = 1; tier <= cleanroom.getCleanroomTier(); tier++) {
                l.supported(ICleanroomProvider.getCleanroomTooltip(tier));
            }
        }).build();
    }
}

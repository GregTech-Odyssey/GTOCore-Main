package com.gtocore.common.machine.multiblock.part.maintenance;

import com.gtocore.api.machine.part.IVacuumPartMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.uiwidgets.display.MachineScreen;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CVCHatchPartMachine extends ACMHatchPartMachine implements IVacuumPartMachine {

    public CVCHatchPartMachine(MetaMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public int getVacuumTier() {
        return VACUUM_TIER;
    }

    private static final int VACUUM_TIER = 4;
    private static final Component VACUUM_TEXT = Component.literal(Integer.toString(VACUUM_TIER));

    @Override
    protected void addReadouts(MachineScreen screen) {
        super.addReadouts(screen);
        screen.addLine(READ_VACUUM, () -> VACUUM_TEXT);
    }

    @RegisterLanguage(cn = "满足真空等级不高于 %s 的配方条件", en = "Satisfies recipe vacuum requirements up to tier %s")
    private static final String TIP_VACUUM = "gtocore.machine.configurable_maintenance.vacuum";

    public static TipSpec tips() {
        return tipsBuilder().group(null, l -> {
            l.statText(Component.translatable("gtocore.recipe.vacuum.tier", VACUUM_TIER));
            l.detail(Component.translatable(TIP_VACUUM, VACUUM_TIER));
        }).build();
    }
}

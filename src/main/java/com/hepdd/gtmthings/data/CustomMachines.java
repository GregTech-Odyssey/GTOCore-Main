package com.hepdd.gtmthings.data;

import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.common.machine.multiblock.part.ae.MEDualOutputPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEDualOutputPartMachineV2;

import com.gtolib.api.data.storage.MEHatchMigration;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class CustomMachines {

    static {
        GTMTHINGS_REGISTRATE.defaultCreativeTab(CreativeModeTabs.MORE_MACHINES);
    }

    @Deprecated
    public static final MachineDefinition LEGACY_ME_EXPORT_BUFFER = MEHatchMigration.registerLegacy(GTMTHINGS_REGISTRATE, "me_export_buffer", name -> GTMTHINGS_REGISTRATE.machine(name, MEDualOutputPartMachine::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("me.export")
            .tier(LuV)
            .tips(Tips.attention(Component.translatable("gtocore.item.deprecated")))
            .register());

    public static final MachineDefinition ME_EXPORT_BUFFER = GTMTHINGS_REGISTRATE.machine("me_export_buffer_v2", MEDualOutputPartMachineV2::new)
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("me.export")
            .tier(LuV)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    public static void init() {}
}

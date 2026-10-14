package com.hepdd.gtmthings.data;

import com.gtocore.common.wireless.energy.WirelessEnergyInterfaceMachine;
import com.gtocore.common.wireless.energy.WirelessEnergyMonitorMachine;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;

import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.block.machine.multiblock.part.computation.WirelessOpticalComputationHatchMachine;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.hepdd.gtmthings.common.registry.GTMTRegistration.GTMTHINGS_REGISTRATE;

public class WirelessMachines {

    static {
        GTMTHINGS_REGISTRATE.defaultCreativeTab(CreativeModeTabs.MORE_MACHINES);
    }

    public static final MachineDefinition WIRELESS_ENERGY_MONITOR = GTMTHINGS_REGISTRATE
            .machine("wireless_energy_monitor", WirelessEnergyMonitorMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .workableCasingRenderer(GTOCore.id("block/casings/wireless_energy_tower_frame"), GTMThings.id("block/machines/wireless_energy_monitor"))
            .tier(IV)
            .register();

    public static final MachineDefinition WIRELESS_ENERGY_INTERFACE = GTMTHINGS_REGISTRATE
            .machine("wireless_energy_interface", WirelessEnergyInterfaceMachine::new)
            .rotationState(RotationState.ALL)
            .workableCasingRenderer(GTOCore.id("block/casings/wireless_energy_tower_frame"), GTOCore.id("block/machines/wireless_energy_interface"))
            .tier(LV)
            .register();

    public static final MachineDefinition WIRELESS_COMPUTATION_HATCH_TRANSMITTER = GTMTHINGS_REGISTRATE
            .machine("wireless_computation_transmitter_hatch", (holder) -> new WirelessOpticalComputationHatchMachine(holder, true))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.COMPUTATION_DATA_TRANSMISSION)
            .renderer(() -> new OverlayTieredMachineRenderer(UV, GTCEu.id("block/machine/part/computation_data_hatch")))
            .tips(Tips.feature(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tooltip.1")),
                    Tips.requirement(Component.translatable("gtmthings.machine.wireless_computation_transmitter_hatch.tooltip.2")))
            .tier(UV)
            .register();

    public static final MachineDefinition WIRELESS_COMPUTATION_HATCH_RECEIVER = GTMTHINGS_REGISTRATE
            .machine("wireless_computation_receiver_hatch", (holder) -> new WirelessOpticalComputationHatchMachine(holder, false))
            .rotationState(RotationState.ALL)
            .abilities(PartAbility.COMPUTATION_DATA_RECEPTION)
            .renderer(() -> new OverlayTieredMachineRenderer(UV, GTCEu.id("block/machine/part/computation_data_hatch")))
            .tips(Tips.feature(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tooltip.1")),
                    Tips.requirement(Component.translatable("gtmthings.machine.wireless_computation_receiver_hatch.tooltip.2")))
            .tier(UV)
            .register();

    public static void init() {};
}

package com.gtocore.common.data.machines;

import com.gtocore.common.data.GTOBlocks;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.machine.multiblockpro.MachineProtocol;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;
import com.gregtechceu.gtceu.api.pattern.Predicates;

@DataGeneratorScanned
public final class GTOMachineProtocols {

    @RegisterLanguage(cn = "太空电梯模块", en = "Space Elevator Module")
    private static final String SPACE_ELEVATOR_MODULE_NAME = "gtocore.machine_protocol.space_elevator_module";
    @RegisterLanguage(cn = "巨型太空电梯模块", en = "Mega Space Elevator Module")
    private static final String MEGA_SPACE_ELEVATOR_MODULE_NAME = "gtocore.machine_protocol.mega_space_elevator_module";
    @RegisterLanguage(cn = "纳米集成加工模块", en = "Nanites Processing Module")
    private static final String NANITES_MODULE_NAME = "gtocore.machine_protocol.nanites_module";
    @RegisterLanguage(cn = "衔接舱接口", en = "Docking Port")
    private static final String STATION_DOCKING_NAME = "gtocore.machine_protocol.station_docking";
    @RegisterLanguage(cn = "舱段接口", en = "Module Port")
    private static final String STATION_JUNCTION_NAME = "gtocore.machine_protocol.station_junction";
    @RegisterLanguage(cn = "光伏帆板接口", en = "Photovoltaic Sail Dock")
    private static final String PHOTOVOLTAIC_SAIL_NAME = "gtocore.machine_protocol.photovoltaic_sail";
    @RegisterLanguage(cn = "生物振荡电刺激器", en = "Bio-Oscillation Electric Stimulator")
    private static final String BIO_STIMULATOR_NAME = "gtocore.machine_protocol.bio_stimulator";

    public static final MachineProtocol SPACE_ELEVATOR_MODULE = MachineProtocol.parentChild(GTOCore.id("space_elevator_module"), SPACE_ELEVATOR_MODULE_NAME);
    public static final MachineProtocol MEGA_SPACE_ELEVATOR_MODULE = MachineProtocol.parentChild(GTOCore.id("mega_space_elevator_module"), MEGA_SPACE_ELEVATOR_MODULE_NAME);
    public static final MachineProtocol NANITES_MODULE = MachineProtocol.parentChild(GTOCore.id("nanites_module"), NANITES_MODULE_NAME);
    public static final MachineProtocol BIO_STIMULATOR = MachineProtocol.parentChild(GTOCore.id("bio_stimulator"), BIO_STIMULATOR_NAME);

    public static final MachineProtocol STATION_DOCKING = MachineProtocol.parentChild(GTOCore.id("station_docking"), STATION_DOCKING_NAME,
            () -> Predicates.blocks(GTOBlocks.TITANIUM_ALLOY_FRAME_INTERNAL.get()));
    public static final MachineProtocol STATION_JUNCTION = MachineProtocol.parentChild(GTOCore.id("station_junction"), STATION_JUNCTION_NAME,
            () -> Predicates.blocks(GTOBlocks.TITANIUM_ALLOY_FRAME_INTERNAL.get()));

    public static final MachineProtocol PHOTOVOLTAIC_SAIL = MachineProtocol.parentChild(GTOCore.id("photovoltaic_sail"), PHOTOVOLTAIC_SAIL_NAME, Predicates::any);

    public static final PortKey MODULE_PORT = PortKey.of("module_port");
    public static final PortKey SAIL_PORT = PortKey.of("sail_port");
    public static final PortKey STATION_RING = PortKey.of("station_ring");
    public static final PortKey MEGA_MODULE_PORT = PortKey.of("mega_module_port");

    private GTOMachineProtocols() {}
}

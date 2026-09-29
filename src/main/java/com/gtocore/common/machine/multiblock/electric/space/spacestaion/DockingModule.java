package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;

@DataGeneratorScanned
public class DockingModule extends Conjunction {

    public static final PortKey HUB_OUT = PortKey.of("hub_out");
    public static final PortKey HUB_IN = PortKey.of("hub_in");
    @RegisterLanguage(cn = "枢纽形态", en = "Hub Form")
    private static final String HUB_NAME = "gtocore.multiblock.space_station_docking_module.hub";
    @RegisterLanguage(cn = "第一项为侧接形态，控制器位于走廊侧壁；第二项为后接形态，控制器位于枢纽后方；两种形态的对接口位置不同", en = "The first option is the side form, with the controller on the corridor wall; the second is the rear form, with the controller behind the hub; the two forms have different connector positions")
    private static final String HUB_DESC = "gtocore.multiblock.space_station_docking_module.hub.desc";
    public static final ParamKey HUB = ParamKey.of(HUB_NAME, HUB_DESC);
    @RegisterLanguage(cn = "侧接形态", en = "Side Form")
    private static final String SIDE_NAME = "gtocore.multiblock.space_station_docking_module.side";
    @RegisterLanguage(cn = "后接形态", en = "Rear Form")
    private static final String REAR_NAME = "gtocore.multiblock.space_station_docking_module.rear";
    public static final ParamKey SIDE_FORM = ParamKey.of(SIDE_NAME);
    public static final ParamKey REAR_FORM = ParamKey.of(REAR_NAME);

    public DockingModule(MetaMachineBlockEntity holder) {
        super(holder);
    }
}

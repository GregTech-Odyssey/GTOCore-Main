package com.gtocore.api.machine.part;

import com.gtocore.common.data.GTOMachines;

import com.gtolib.api.lang.CNEN;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.GTMachines;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import java.util.Map;

public final class GTOPartAbility extends PartAbility {

    public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

    public static final PartAbility NEUTRON_ACCELERATOR = new GTOPartAbility("neutron_accelerator", "gtocore.part_ability.neutron_accelerator", "中子加速器", "Neutron Accelerator");
    public static final PartAbility THREAD_HATCH = new GTOPartAbility("thread_hatch", "gtocore.part_ability.thread_hatch", "线程仓", "Thread Hatch");
    public static final PartAbility OVERCLOCK_HATCH = new GTOPartAbility("overclock_hatch", "gtocore.part_ability.overclock_hatch", "超频仓", "Overclock Hatch");
    public static final PartAbility ACCELERATE_HATCH = new GTOPartAbility("accelerate_hatch", "gtocore.part_ability.accelerate_hatch", "加速仓", "Accelerate Hatch");
    public static final PartAbility DRONE_HATCH = new GTOPartAbility("drone_hatch", "gtocore.part_ability.drone_hatch", "无人机仓", "Drone Hatch");
    public static final PartAbility PASSTHROUGH_HATCH_MANA = new GTOPartAbility("passthrogh_hatch_mana", "gtocore.part_ability.passthrogh_hatch_mana", "魔力通行仓", "Mana Passthrough Hatch");
    public static final PartAbility INPUT_MANA = new GTOPartAbility("input_mana", "gtocore.part_ability.input_mana", "魔力输入仓", "Input Mana");
    public static final PartAbility OUTPUT_MANA = new GTOPartAbility("output_mana", "gtocore.part_ability.output_mana", "魔力输出仓", "Output Mana");
    public static final PartAbility EXTRACT_MANA = new GTOPartAbility("extract_mana", "gtocore.part_ability.extract_mana", "魔力抽取仓", "Extract Mana");
    public static final PartAbility COMPUTING_COMPONENT = new GTOPartAbility("computing_component", "gtocore.part_ability.computing_component", "计算组件", "Computing Component Hatch");
    public static final PartAbility CATALYST_HATCH = new GTOPartAbility("catalyst_hatch", "gtocore.part_ability.catalyst_hatch", "催化剂仓", "Catalyst Hatch");
    public static final PartAbility MANA_AMPLIFIER_HATCH = new GTOPartAbility("mana_amplifier_hatch", "gtocore.part_ability.mana_amplifier_hatch", "魔力增幅仓", "Mana Amplifier Hatch");

    public static final PartAbility ITEMS_INPUT_BUS = new GTOPartAbility("items_input", "gtocore.part_ability.items_input", "物品输入仓", "Items Input");
    public static final PartAbility ITEMS_OUTPUT_BUS = new GTOPartAbility("items_output", "gtocore.part_ability.items_output", "物品输出仓", "Items Output");

    public static final PartAbility STEAM_IMPORT_FLUIDS = new GTOPartAbility("steam_import_fluids", "gtocore.part_ability.steam_import_fluids", "蒸汽流体输入仓", "Steam Import Fluids");
    public static final PartAbility STEAM_EXPORT_FLUIDS = new GTOPartAbility("steam_export_fluids", "gtocore.part_ability.steam_export_fluids", "蒸汽流体输出仓", "Steam Export Fluids");

    public static final PartAbility HEAT_CONDUCTION = new GTOPartAbility("heat_conduct", "gtocore.part_ability.heat_conduct", "热传导仓", "Heat Conduct Hatch");
    public static final PartAbility RADIATION_HATCH = new GTOPartAbility("radiation_hatch", "gtocore.part_ability.radiation_hatch", "放射仓", "Radiation Hatch");
    // 仅用于放入附属模块的描述中
    public static final PartAbility EXTRA_ENERGY_HATCH = new GTOPartAbility("extra_energy_hatch", "gtocore.part_ability.extra_energy_hatch", "额外能源仓", "Extra Energy Hatch");

    public static final PartAbility ME_STORAGE_ACCESS = new GTOPartAbility("me_storage_access", "gtocore.part_ability.me_storage_access", "ME存储访问", "ME Storage Access");

    public GTOPartAbility(String name, String translationKey, String cn, String en) {
        super(name, translationKey);
        if (LANG != null) {
            LANG.put(translationKey, new CNEN(cn, en));
        }
    }

    public static void init() {
        PartAbility.STEAM_IMPORT_ITEMS.register(2, GTMachines.ITEM_IMPORT_BUS[0].get());
        PartAbility.STEAM_EXPORT_ITEMS.register(2, GTMachines.ITEM_EXPORT_BUS[0].get());
        STEAM_IMPORT_FLUIDS.register(2, GTOMachines.INFINITE_INTAKE_HATCH.get());
        for (var machine : GTMachines.ITEM_IMPORT_BUS) {
            if (machine != null) ITEMS_INPUT_BUS.register(machine.getTier(), machine.get());
        }
        for (var machine : GTMachines.ITEM_EXPORT_BUS) {
            if (machine != null) ITEMS_OUTPUT_BUS.register(machine.getTier(), machine.get());
        }
    }
}

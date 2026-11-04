package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAESlot;

import com.gregtechceu.gtceu.datasynclib.GTDataFixer;

import appeng.api.stacks.GenericStack;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.ValueCodec;

/** A configuration view of an ME input. Actual slot stocks are never included. */
public record MEInputSettings(boolean distinct, int circuit, GenericStack[] configs) {

    public static void init() {
        DataSyncCodec.register(MEInputSettings.class, GTDataFixer.VERSION, ValueCodec.composite(
                ValueCodec.BOOLEAN, MEInputSettings::distinct,
                ValueCodec.INT, MEInputSettings::circuit,
                DataSyncCodec.get(GenericStack[].class).toValueCodec().optional(), MEInputSettings::configs,
                MEInputSettings::new));
    }

    static MEInputSettings capture(boolean distinct, int circuit, boolean copySlots, ExportOnlyAESlot[] slots) {
        GenericStack[] configs = null;
        if (copySlots) {
            configs = new GenericStack[slots.length];
            for (int i = 0; i < slots.length; i++) configs[i] = slots[i].getConfig();
        }
        return new MEInputSettings(distinct, circuit, configs);
    }

    void applyConfigs(ExportOnlyAESlot[] slots) {
        if (configs == null) return;
        for (int i = 0; i < slots.length; i++) slots[i].setConfig(i < configs.length ? configs[i] : null);
    }
}

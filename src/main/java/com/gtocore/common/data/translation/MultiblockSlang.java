package com.gtocore.common.data.translation;

import com.gtocore.api.lang.GTOTips;

import com.gregtechceu.gtceu.api.tooltip.TipSpec;

public final class MultiblockSlang {

    public static final TipSpec not_allow_standard_energy_hatch = GTOTips.of("multiblock_slang.not_allow_standard_energy_hatch", t -> {
        t.unsupported(t.tr("无法使用普通能源仓", "Cannot use standard energy hatch"));
        t.note(t.tr("此机器功率需求过高，无法使用普通能源仓", "This machine's power requirement is too high to use standard energy hatch"));
    });

    private MultiblockSlang() {}

    public static void init() {}
}

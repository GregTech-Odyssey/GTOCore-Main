package com.gtocore.common.data.translation

import com.gtocore.api.lang.ComponentListSupplier
import com.gtocore.api.misc.AutoInitialize
import com.gtocore.common.data.translation.ComponentSlang.Bar
import com.gtocore.common.data.translation.ComponentSlang.Tab

object MultiblockSlang : AutoInitialize<MultiblockSlang>() {
    val not_allow_standard_energy_hatch = ComponentListSupplier {
        setTranslationPrefix("multiblock_slang.not_allow_standard_energy_hatch")
        add(Bar(1).orange() + ("无法使用普通能源仓" translatedTo "Cannot use standard energy hatch"))
        add(Tab(1) + ("此机器功率需求过高，无法使用普通能源仓" translatedTo "This machine's power requirement is too high to use standard energy hatch").gray())
    }
}

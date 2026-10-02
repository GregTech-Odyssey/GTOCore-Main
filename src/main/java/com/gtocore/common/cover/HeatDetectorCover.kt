package com.gtocore.common.cover

import net.minecraft.MethodsReturnNonnullByDefault
import net.minecraft.core.Direction

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper
import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.common.cover.detector.DetectorCover
import com.gtolib.api.capability.IHeatContainer

import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
class HeatDetectorCover(definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction) : DetectorCover(definition, coverHolder, attachedSide) {
    protected override fun update() {
        val container = GTCapabilityHelper.getBlockEntityGTCapability(IHeatContainer::class.java, coverHolder.holder(), null) ?: return
        setRedstoneSignalOutput(container.signal)
    }
}

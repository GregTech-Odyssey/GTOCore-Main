package com.gtocore.common.cover

import net.minecraft.core.Direction

import com.gregtechceu.gtceu.api.capability.ICoverable
import com.gregtechceu.gtceu.api.cover.CoverDefinition
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler
import com.gregtechceu.gtceu.common.cover.PumpCover
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.gregtechceu.gtceu.utils.GTTransferUtils

class SteamPumpCover(definition: CoverDefinition, coverHolder: ICoverable, attachedSide: Direction) : PumpCover(definition, coverHolder, attachedSide, 1, 1000000) {
    protected override fun hasFilterUI(): Boolean = false

    protected override fun transferAny(source: ICustomFluidStackHandler, destination: ICustomFluidStackHandler, platformTransferLimit: Int): Int = GTTransferUtils.transferFluidsFiltered(source, destination, { it.getFluid() === STEAM }, platformTransferLimit)

    companion object {
        private val STEAM = GTMaterials.Steam.getFluid()
    }
}

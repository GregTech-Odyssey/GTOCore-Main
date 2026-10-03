package com.gtocore.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyTransfer;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.storage.AEKeyFilter;

import org.jetbrains.annotations.NotNull;

public final class SteamPumpCover extends PumpCover {

    private static final Fluid STEAM = GTMaterials.Steam.getFluid();
    private static final AEKeyFilter STEAM_FILTER = k -> k instanceof AEFluidKey f && f.getFluid() == STEAM;

    public SteamPumpCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, 1, 1000000);
    }

    @Override
    protected boolean hasFilterUI() {
        return false;
    }

    @Override
    protected int transferAny(@NotNull IKeyHandler<AEFluidKey> source, @NotNull IKeyHandler<AEFluidKey> destination, int platformTransferLimit) {
        return (int) KeyTransfer.transfer(source, destination, platformTransferLimit, STEAM_FILTER);
    }
}

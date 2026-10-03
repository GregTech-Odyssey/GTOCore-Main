package com.gtocore.common.wireless.energy.map;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.data.UICodecs;

import net.minecraft.world.entity.player.Player;

import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

public final class GridMapTarget implements IUIHolder {

    static final ByteStreamCodec<GridMapTarget> CODEC = ByteStreamCodec.composite(
            ByteStreamCodec.INT_CODEC, GridMapTarget::focusDimRef,
            UICodecs.enumOf(GridMapMode.class), GridMapTarget::mode,
            GridMapTarget::new);

    private final int focusDimRef;
    private final GridMapMode mode;

    public GridMapTarget(int focusDimRef, GridMapMode mode) {
        this.focusDimRef = focusDimRef;
        this.mode = mode;
    }

    public int focusDimRef() {
        return focusDimRef;
    }

    public GridMapMode mode() {
        return mode;
    }

    @Override
    public ModularUI createUI(Player player) {
        return GridMapUIFactory.createUI(this, player, null, focusDimRef, mode);
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return GTCEu.isClientThread();
    }

    @Override
    public void markAsDirty() {}
}

package com.gtocore.common.machine.multiblock.steam;

import com.gtocore.config.GTORules;

import com.gtolib.api.annotation.Scanned;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

@Scanned
public final class LargeSteamMultiblockMachine extends BaseSteamMultiblockMachine {

    public LargeSteamMultiblockMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORules.LARGE_STEAM_PARALLELS.get(), GTORules.LARGE_STEAM_DURATION.get());
    }

    public LargeSteamMultiblockMachine(MetaMachineBlockEntity holder, int eut) {
        super(holder, GTORules.LARGE_STEAM_PARALLELS.get(), eut, GTORules.LARGE_STEAM_DURATION.get());
    }

    @Override
    boolean oc() {
        return true;
    }
}

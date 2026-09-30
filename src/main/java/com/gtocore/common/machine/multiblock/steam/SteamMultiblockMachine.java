package com.gtocore.common.machine.multiblock.steam;

import com.gtocore.config.GTORules;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

public final class SteamMultiblockMachine extends BaseSteamMultiblockMachine {

    public SteamMultiblockMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORules.STEAM_PARALLELS.get(), GTORules.STEAM_DURATION.get());
    }

    public SteamMultiblockMachine(MetaMachineBlockEntity holder, int eut) {
        super(holder, GTORules.STEAM_PARALLELS.get(), eut, GTORules.STEAM_DURATION.get());
    }
}

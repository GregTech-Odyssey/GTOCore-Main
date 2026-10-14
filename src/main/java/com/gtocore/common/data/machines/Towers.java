package com.gtocore.common.data.machines;

import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Slot;

final class Towers {

    private Towers() {}

    static Slot.PieceSlot layers(Piece layer, Piece top, ParamKey count, int min, int max) {
        return Slot.chain(layer, PortKey.IN, PortKey.OUT, PortKey.IN).count(count, min, max)
                .atPort(PortKey.OUT, Slot.one(top, PortKey.IN));
    }
}

package vazkii.botania.forge.integration.ae2;

import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;

final class ManaAccess {

    private ManaAccess() {}

    static boolean canExtract(ManaReceiver receiver) {
        return receiver instanceof ManaPool || receiver instanceof ManaCollector;
    }

    static long insert(ManaReceiver receiver, long amount, boolean simulate) {
        return receiver.addMana(amount, simulate);
    }

    static long extract(ManaReceiver receiver, long amount, boolean simulate) {
        return canExtract(receiver) ? receiver.removeMana(amount, simulate) : 0;
    }

    static long stored(ManaReceiver receiver) {
        return canExtract(receiver) ? Math.max(0, receiver.getCurrentManaLong()) : 0;
    }
}

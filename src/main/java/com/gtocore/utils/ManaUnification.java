package com.gtocore.utils;

import com.gtolib.api.capability.IManaContainer;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import vazkii.botania.api.mana.ManaEndpoint;

/// 魔力与魔源的唯一换算：4 魔源 = 1 魔力；魔源折魔力向下取整，魔力折魔源饱和不溢出
/// GT 魔力容器与魔源方块按魔力单位接入标准传输 {@link ManaEndpoint#transfer}
public final class ManaUnification {

    /// 1 魔力折 4 魔源。
    public static final int SOURCE_PER_MANA = 4;

    private static final long MAX_SOURCE = Long.MAX_VALUE >> 2;

    private ManaUnification() {}

    public static long manaToSource(long mana) {
        return mana > MAX_SOURCE ? Long.MAX_VALUE : mana << 2;
    }

    public static long sourceToMana(long source) {
        return source >> 2;
    }

    public static ManaEndpoint of(IManaContainer container, int rateMultiplier) {
        return new ManaEndpoint() {

            @Override
            public long addMana(long amount, boolean simulate) {
                return rateMultiplier == 0 ? container.addManaUnrestricted(amount, simulate) : container.addMana(amount, rateMultiplier, simulate);
            }

            @Override
            public long removeMana(long amount, boolean simulate) {
                return rateMultiplier == 0 ? container.removeManaUnrestricted(amount, simulate) : container.removeMana(amount, rateMultiplier, simulate);
            }
        };
    }

    public static ManaEndpoint of(ISourceTile tile) {
        return new ManaEndpoint() {

            @Override
            public long addMana(long amount, boolean simulate) {
                long given = tile.addSource(manaToSource(amount), simulate);
                long extra = given % SOURCE_PER_MANA;
                if (extra != 0 && !simulate) tile.removeSource(extra, false);
                return sourceToMana(given);
            }

            @Override
            public long removeMana(long amount, boolean simulate) {
                long taken = tile.removeSource(manaToSource(amount), simulate);
                long extra = taken % SOURCE_PER_MANA;
                if (extra != 0 && !simulate) tile.addSource(extra, false);
                return sourceToMana(taken);
            }
        };
    }
}

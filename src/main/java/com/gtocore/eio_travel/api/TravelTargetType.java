package com.gtocore.eio_travel.api;

import com.gregtechceu.gtceu.api.item.IDataComponentItem;
import com.gregtechceu.gtceu.api.item.ItemDataComponentKey;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.item.ItemStack;

public enum TravelTargetType {

    ANCHOR,
    TEAMMATE,
    PATTERN_NODE;

    public static final ItemDataComponentKey<Integer, IntTag> ENABLED = ItemDataComponentKey.create("TravelTargets", IntTag::valueOf, IntTag::getAsInt);
    public static final ItemDataComponentKey<Boolean, ByteTag> SHOW_HIDDEN = ItemDataComponentKey.create("TravelShowHidden", ByteTag::valueOf, t -> t.getAsByte() != 0);
    public static final TravelTargetType[] VALUES = values();
    private static final int DEFAULT_MASK = ANCHOR.getBit() | TEAMMATE.getBit();

    public int getBit() {
        return 1 << ordinal();
    }

    public static int getMask(ItemStack staff) {
        return IDataComponentItem.getOrDefault(staff, ENABLED, DEFAULT_MASK);
    }

    public static boolean showsHidden(ItemStack staff) {
        return IDataComponentItem.getOrDefault(staff, SHOW_HIDDEN, false);
    }

    public static void setShowHidden(ItemStack staff, boolean show) {
        if (show) {
            IDataComponentItem.set(staff, SHOW_HIDDEN, true);
        } else {
            IDataComponentItem.remove(staff, SHOW_HIDDEN);
        }
    }

    public boolean isEnabled(int mask) {
        return (mask & getBit()) != 0;
    }

    public boolean isEnabled(ItemStack staff) {
        return isEnabled(getMask(staff));
    }

    public void setEnabled(ItemStack staff, boolean enabled) {
        int mask = getMask(staff);
        IDataComponentItem.set(staff, ENABLED, enabled ? mask | getBit() : mask & ~getBit());
    }
}

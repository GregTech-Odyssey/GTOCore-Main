package com.gtocore.utils;

/// 魔力与魔源的唯一换算：1 魔源 = 4 魔力；魔力折魔源向下取整，魔源折魔力饱和不溢出
public final class ManaUnification {

    public static final int MANA_PER_SOURCE = 4;

    private ManaUnification() {}

    public static long manaToSource(long mana) {
        return mana / MANA_PER_SOURCE;
    }

    public static long sourceToMana(long source) {
        return source > Long.MAX_VALUE / MANA_PER_SOURCE ? Long.MAX_VALUE : source * MANA_PER_SOURCE;
    }
}

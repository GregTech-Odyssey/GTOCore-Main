package com.gtocore.utils;

/// 魔力与魔源的唯一换算：4 魔源 = 1 魔力；魔源折魔力向下取整，魔力折魔源饱和不溢出
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
}

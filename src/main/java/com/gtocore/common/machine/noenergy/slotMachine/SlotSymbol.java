package com.gtocore.common.machine.noenergy.slotMachine;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 老虎机使用的全部符号定义：每个值携带稳定 id、名称翻译键和二连/三连赔付倍率
 * （奖励 = 下注 × 倍率，与档位无关）。
 *
 * <p>
 * {@link #HONEY_BOTTLE} 是百搭符号：自身不赔付，只在中奖线判定时替代普通符号；各档位的符号比例由
 * {@link SlotMachineRules} 定义。取值方法名与字段同名（{@code id()}、{@code wild()} 等），
 * 由 Lombok 按 fluent 命名生成。
 */
@Getter
@Accessors(fluent = true)
public enum SlotSymbol {

    SWEET_BERRIES(0, "item.minecraft.sweet_berries", 2, 0, false),
    CHORUS_FRUIT(1, "item.minecraft.chorus_fruit", 3, 0, false),
    MELON_SLICE(2, "item.minecraft.melon_slice", 4, 0, false),
    APPLE(3, "item.minecraft.apple", 6, 2, false),
    GOLDEN_CARROT(4, "item.minecraft.golden_carrot", 10, 3, false),
    GOLDEN_APPLE(5, "item.minecraft.golden_apple", 20, 5, false),
    EMERALD(6, "item.minecraft.emerald", 35, 8, false),
    NETHER_STAR(7, "item.minecraft.nether_star", 120, 12, false),
    HONEY_BOTTLE(8, "item.minecraft.honey_bottle", 0, 0, true);

    /** 按 id 直接索引的缓存，避免每次按 id 还原符号都分配一遍 {@code values()} 数组。 */
    private static final SlotSymbol[] VALUES = values();

    private final int id;
    private final String translateKey;
    private final int threeMatchMultiplier;
    private final int twoMatchMultiplier;
    private final boolean wild;

    SlotSymbol(int id, String translateKey, int threeMatchMultiplier, int twoMatchMultiplier, boolean wild) {
        this.id = id;
        this.translateKey = translateKey;
        this.threeMatchMultiplier = threeMatchMultiplier;
        this.twoMatchMultiplier = twoMatchMultiplier;
        this.wild = wild;
    }

    /** 从稳定 id 恢复符号；非法 id（旧档或损坏数据）回退到最基础的甜浆果，不影响机器加载。 */
    public static SlotSymbol byId(int id) {
        if (id < 0 || id >= VALUES.length) return SWEET_BERRIES;
        return VALUES[id];
    }

    /** 符号总数；避免调用方重复分配 {@code values()}。 */
    static int count() {
        return VALUES.length;
    }
}

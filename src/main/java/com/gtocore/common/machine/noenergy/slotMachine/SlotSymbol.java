package com.gtocore.common.machine.noenergy.slotMachine;

import com.gtocore.common.data.GTOItems;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * 老虎机使用的全部符号定义：每个值携带稳定 id、名称翻译键和二连/三连赔付倍率
 * （奖励 = 下注 × 倍率，与档位无关）。
 *
 * <p>
 * {@link #SYMBOL8} 是百搭符号：自身不赔付，只在中奖线判定时替代普通符号；各档位的符号比例由
 * {@link SlotMachineRules} 定义。取值方法名与字段同名（{@code id()}、{@code wild()} 等），
 * 由 Lombok 按 fluent 命名生成。
 */
@Getter
@Accessors(fluent = true)
public enum SlotSymbol {

    SYMBOL0(0, 2, 0, false),
    SYMBOL1(1, 3, 0, false),
    SYMBOL2(2, 4, 0, false),
    SYMBOL3(3, 6, 2, false),
    SYMBOL4(4, 10, 3, false),
    SYMBOL5(5, 20, 5, false),
    SYMBOL6(6, 35, 8, false),
    SYMBOL7(7, 120, 12, false),
    SYMBOL8(8, 0, 0, true);

    /** 按 id 直接索引的缓存，避免每次按 id 还原符号都分配一遍 {@code values()} 数组。 */
    private static final SlotSymbol[] VALUES = values();

    private final int id;
    private final int threeMatchMultiplier;
    private final int twoMatchMultiplier;
    private final boolean wild;

    SlotSymbol(int id, int threeMatchMultiplier, int twoMatchMultiplier, boolean wild) {
        this.id = id;
        this.threeMatchMultiplier = threeMatchMultiplier;
        this.twoMatchMultiplier = twoMatchMultiplier;
        this.wild = wild;
    }

    /** 从稳定 id 恢复符号；非法 id（旧档或损坏数据）回退到最基础的甜浆果，不影响机器加载。 */
    public static SlotSymbol byId(int id) {
        if (id < 0 || id >= VALUES.length) return SYMBOL0;
        return VALUES[id];
    }

    /** 符号总数；避免调用方重复分配 {@code values()}。 */
    static int count() {
        return VALUES.length;
    }

    public static ItemStack symbolItem(SlotSymbol symbol) {
        return switch (symbol) {
            case SYMBOL0 -> GTItems.TRANSISTOR.asStack();
            case SYMBOL1 -> GTItems.GRAVITATION_ENGINE.asStack();
            case SYMBOL2 -> GTItems.BLACKLIGHT.asStack();
            case SYMBOL3 -> GTItems.TOOL_DATA_MODULE.asStack();
            case SYMBOL4 -> GTOItems.SCINTILLATOR.asStack();
            case SYMBOL5 -> GTOItems.NUCLEAR_STAR.asStack();
            case SYMBOL6 -> GTOItems.OBSIDIAN_MATRIX.asStack();
            case SYMBOL7 -> GTOItems.CHAOTIC_CORE.asStack();
            case SYMBOL8 -> GTOItems.QUANTUM_ANOMALY.asStack();
        };
    }

    public static Component symbolName(SlotSymbol symbol) {
        return switch (symbol) {
            case SYMBOL0 -> Component.translatable("item.gtceu.transistor");
            case SYMBOL1 -> Component.translatable("item.gtceu.gravitation_engine_unit");
            case SYMBOL2 -> Component.translatable("item.gtceu.blacklight");
            case SYMBOL3 -> Component.translatable("item.gtceu.data_module");
            case SYMBOL4 -> Component.translatable("item.gtocore.scintillator");
            case SYMBOL5 -> Component.translatable("item.gtocore.nuclear_star");
            case SYMBOL6 -> Component.translatable("item.gtocore.obsidian_matrix");
            case SYMBOL7 -> Component.translatable("item.gtocore.chaotic_core");
            case SYMBOL8 -> Component.translatable("item.gtocore.quantum_anomaly");
        };
    }
}

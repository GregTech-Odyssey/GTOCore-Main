package com.gtocore.data.transaction.data;

import com.gtocore.api.data.tag.GTOTagPrefix;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTOMaterials.*;

/** 实体币定义，金额单位为技术员币。 */
public final class CoinExchange {

    public static final int TYPE_COUNT = 10;

    private CoinExchange() {}

    /** 按兑币区顺序查询材料，避免在材料初始化前缓存可被重新指派的材料字段。 */
    public static Material material(int tier) {
        return switch (tier) {
            case 0 -> Copper;
            case 1 -> Cupronickel;
            case 2 -> Silver;
            case 3 -> Gold;
            case 4 -> Osmium;
            case 5 -> Naquadah;
            case 6 -> Neutronium;
            case 7 -> Adamantine;
            case 8 -> Infinity;
            case 9 -> Neutron;
            default -> throw new IndexOutOfBoundsException(tier);
        };
    }

    /** 一枚实体币的面值，相邻面额按 8:1 兑换。 */
    public static long value(int tier) {
        if (tier < 0 || tier >= TYPE_COUNT) throw new IndexOutOfBoundsException(tier);
        return 1L << (tier * 3);
    }

    /** 仅在物品注册完成后调用；缓存兑币区实际使用的物品，包括被替换的中子素币。 */
    public static Item item(int tier) {
        return CoinItems.ITEMS[tier];
    }

    public static ItemStack[] itemStacks() {
        return Arrays.stream(CoinItems.ITEMS).map(ItemStack::new).toArray(ItemStack[]::new);
    }

    /** 非兑币区货币返回 -1；不接受空槽或普通材料物品。 */
    public static int tierOf(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        Item item = stack.getItem();
        for (int tier = 0; tier < TYPE_COUNT; tier++) {
            if (item == CoinItems.ITEMS[tier]) return tier;
        }
        return -1;
    }

    private static final class CoinItems {

        private static final Item[] ITEMS = createItems();

        private static Item[] createItems() {
            Item[] items = new Item[TYPE_COUNT];
            for (int tier = 0; tier < TYPE_COUNT; tier++) {
                items[tier] = ChemicalHelper.get(GTOTagPrefix.COIN, material(tier)).getItem();
            }
            return items;
        }
    }
}

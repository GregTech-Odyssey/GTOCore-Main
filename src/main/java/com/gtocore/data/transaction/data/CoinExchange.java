package com.gtocore.data.transaction.data;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTOMaterials.*;

/**
 * 实体币总表：每种货币登记一个 {@link CoinSeries}（面额序与实体币物品），并提供按货币种类拆分硬币掉到世界等通用操作。
 * 没有实体币的货币（能量币、里程碑币等）不在这里，{@link #byCurrency(String)} 返回 {@code null}。
 */
public final class CoinExchange {

    /** 材料币种数：铜到中子各一种，面额 8 倍递增。 */
    private static final int MATERIAL_TYPE_COUNT = 10;
    /** 游戏币种数：面额 1 到 10^7。 */
    private static final int GAME_COIN_TYPE_COUNT = 8;

    private CoinExchange() {}

    /** 技术员币：GT 材料币，面额 1<<(3*tier)，相邻面额 8:1。 */
    public static CoinSeries techOperator() {
        return TechOperator.SERIES;
    }

    /** 游戏币：GTO 游戏币，面额 10^tier。 */
    public static CoinSeries gameCoins() {
        return GameCoins.SERIES;
    }

    /** 按货币种类取实体币系列；该货币没有实体币时返回 {@code null}。 */
    @Nullable
    public static CoinSeries byCurrency(String currencyId) {
        if (TradeLang.TECH_OPERATOR_COIN.equals(currencyId)) return techOperator();
        if (TradeLang.GAME_COINS.equals(currencyId)) return gameCoins();
        return null;
    }

    /**
     * 把指定货币的金额按面额从高到低拆成实体币丢到世界坐标处。
     *
     * @param level      掉落所在世界
     * @param pos        掉落位置
     * @param currencyId 货币种类
     * @param amount     掉落的金额
     * @return 实际掉落的金额；货币没有实体币或世界不是服务端时返回 {@code 0}
     */
    public static long dropToWorld(Level level, BlockPos pos, String currencyId, long amount) {
        CoinSeries series = byCurrency(currencyId);
        return series == null ? 0 : series.dropToWorld(level, pos, amount);
    }

    /** 按兑币区顺序查询材料，避免在材料初始化前缓存可被重新指派的材料字段。 */
    private static Material material(int tier) {
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

    /** 材料币的物品只有在物品注册完成后才取得到，放进嵌套持有类延迟创建。 */
    private static final class TechOperator {

        private static final CoinSeries SERIES = createSeries();

        private static CoinSeries createSeries() {
            long[] values = new long[MATERIAL_TYPE_COUNT];
            Item[] items = new Item[MATERIAL_TYPE_COUNT];
            for (int tier = 0; tier < MATERIAL_TYPE_COUNT; tier++) {
                values[tier] = 1L << (tier * 3);
                items[tier] = ChemicalHelper.get(GTOTagPrefix.COIN, material(tier)).getItem();
            }
            return new CoinSeries(TradeLang.TECH_OPERATOR_COIN, values, items);
        }
    }

    /** 游戏币的物品同样要等 GTOItems 注册完成，延迟创建；下标顺序与 GTOItems 的声明一致，面额为 10^下标。 */
    private static final class GameCoins {

        private static final CoinSeries SERIES = createSeries();

        private static CoinSeries createSeries() {
            long[] values = new long[GAME_COIN_TYPE_COUNT];
            long value = 1;
            for (int tier = 0; tier < GAME_COIN_TYPE_COUNT; tier++) {
                values[tier] = value;
                value *= 10;
            }
            Item[] items = {
                    GTOItems.GAME_COINS_1.asItem(),
                    GTOItems.GAME_COINS_10.asItem(),
                    GTOItems.GAME_COINS_100.asItem(),
                    GTOItems.GAME_COINS_1K.asItem(),
                    GTOItems.GAME_COINS_10K.asItem(),
                    GTOItems.GAME_COINS_100K.asItem(),
                    GTOItems.GAME_COINS_1M.asItem(),
                    GTOItems.GAME_COINS_10M.asItem() };
            return new CoinSeries(TradeLang.GAME_COINS, values, items);
        }
    }
}

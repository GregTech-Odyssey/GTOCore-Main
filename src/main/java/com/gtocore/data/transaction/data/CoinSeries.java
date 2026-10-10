package com.gtocore.data.transaction.data;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 一种货币的实体币系列：面额由低到高各对应一种物品，金额按最小面额（面额最小的硬币）计。
 *
 * <p>
 * 相邻面额都取前一个的整数倍（技术员币 8 倍、游戏币 10 倍），且都从面额 1 起，所以任意非负金额都能按面额从高到低
 * 贪心拆成整数枚实体币，不产生余数。系列内容在初始化后不变，拆栈只按物品的最大堆叠切分。
 */
public final class CoinSeries {

    private final String currencyId;
    private final long[] values;
    private final Item[] items;

    CoinSeries(String currencyId, long[] values, Item[] items) {
        this.currencyId = currencyId;
        this.values = values;
        this.items = items;
    }

    /** 本系列对应的货币种类，与钱包和交易条目里的货币 id 一致。 */
    public String currencyId() {
        return currencyId;
    }

    /** 实体币种数，即面额数量。 */
    public int typeCount() {
        return values.length;
    }

    /** 第 tier 种实体币的面额，下标越大面额越大。 */
    public long value(int tier) {
        return values[tier];
    }

    /** 第 tier 种实体币的物品。 */
    public Item item(int tier) {
        return items[tier];
    }

    /** 第 tier 种实体币的单枚模板，用于界面材质与交易说明。 */
    public ItemStack itemStack(int tier) {
        return new ItemStack(items[tier]);
    }

    /** 兑币区鬼影槽用的整组模板；每次新建，调用方可安全持有。 */
    public ItemStack[] itemStacks() {
        ItemStack[] stacks = new ItemStack[items.length];
        for (int tier = 0; tier < items.length; tier++) {
            stacks[tier] = new ItemStack(items[tier]);
        }
        return stacks;
    }

    /** 非本系列的物品返回 -1；不取数量，空槽也是 -1。 */
    public int tierOf(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        Item item = stack.getItem();
        for (int tier = 0; tier < items.length; tier++) {
            if (item == items[tier]) return tier;
        }
        return -1;
    }

    /**
     * 把金额按面额从高到低拆成实体币丢到世界坐标处，每枚硬币按物品的最大堆叠切栈。
     *
     * @param level  掉落所在世界，非服务端时不掉落
     * @param pos    掉落位置，按方块中心生成
     * @param amount 掉落的金额
     * @return 实际掉落的金额
     */
    public long dropToWorld(Level level, BlockPos pos, long amount) {
        if (amount <= 0 || !(level instanceof ServerLevel serverLevel)) return 0;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        long remaining = amount;
        for (int tier = values.length - 1; tier >= 0; tier--) {
            long count = remaining / values[tier];
            if (count == 0) continue;
            remaining -= count * values[tier];
            int maxStackSize = items[tier].getMaxStackSize();
            while (count > 0) {
                int stackSize = (int) Math.min(count, maxStackSize);
                ItemEntity entity = new ItemEntity(serverLevel, x, y, z, new ItemStack(items[tier], stackSize));
                entity.setNoPickUpDelay();
                serverLevel.addFreshEntity(entity);
                count -= stackSize;
            }
        }
        return amount - remaining;
    }
}

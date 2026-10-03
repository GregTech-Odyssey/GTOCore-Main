package com.gtocore.data.transaction;

import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.Keys;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import java.util.List;

public class TradingStationTool {

    /////////////////////////////////////
    // ********* 输入输出方法 ********* //
    /////////////////////////////////////

    /**
     * 计算库存对输入物品列表的最大支持乘数（即库存最多能满足多少组输入列表消耗）
     *
     * @param handler       物品库存处理器
     * @param requiredItems 需求物品列表（每个ItemStack的count为每组需求数量）
     * @return 最大支持乘数（若列表为空或无需求则返回Integer.MAX_VALUE，否则取最小限制乘数）
     */
    public static int checkMaxMultiplier(IKeyHandler<AEItemKey> handler, List<ItemStack> requiredItems) {
        if (requiredItems.isEmpty()) {
            return Integer.MAX_VALUE; // 无需求则支持无限大乘数
        }

        long maxMultiplier = Integer.MAX_VALUE;

        for (ItemStack required : requiredItems) {
            if (required.isEmpty()) {
                continue; // 跳过空物品
            }

            int requiredPerMulti = required.getCount();
            if (requiredPerMulti <= 0) {
                continue; // 每组需求为0，视为不限制该物品
            }

            // 统计库存中该物品的总数量
            AEItemKey key = Keys.item(required);
            long totalInStock = key == null ? 0 : handler.count(key);

            // 计算该物品支持的最大乘数（总库存 / 每组需求）
            maxMultiplier = Math.min(maxMultiplier, totalInStock / requiredPerMulti);

            if (maxMultiplier == 0) {
                break; // 已无法支持1组，提前退出
            }
        }

        return (int) maxMultiplier;
    }

    /**
     * 从库存中扣除 x 组输入物品列表（每个物品扣除数量 = 列表中物品数量 × x）
     *
     * @param handler    物品库存处理器
     * @param items      待扣除的物品列表（每组数量）
     * @param multiplier 乘数 x（必须>0，否则不执行）
     */
    public static void deductMultipliedItems(IKeyHandler<AEItemKey> handler, List<ItemStack> items, int multiplier) {
        if (multiplier <= 0 || items.isEmpty()) {
            return; // 乘数无效或无物品，不执行
        }

        for (ItemStack item : items) {
            if (item.isEmpty()) {
                continue; // 跳过空物品
            }

            int perItemCount = item.getCount();
            if (perItemCount <= 0) {
                continue; // 每组数量为0，无需扣除
            }

            // 计算总扣除数量 = 每组数量 × 乘数
            long totalToDeduct = (long) perItemCount * multiplier;
            AEItemKey key = Keys.item(item);
            if (key == null) {
                continue;
            }

            handler.extract(key, totalToDeduct, false);
        }
    }

    /**
     * 向库存中添加 x 组输入物品列表，库存不足时将剩余物品掷出掉落
     *
     * @param handler    物品库存处理器
     * @param items      待添加的物品列表（每组数量）
     * @param multiplier 乘数 x（必须>0，否则不执行）
     */
    public static void addMultipliedItems(IKeyHandler<AEItemKey> handler, List<ItemStack> items, int multiplier, Level level, BlockPos pos) {
        // 边界校验：乘数无效、无物品、世界或坐标为空时直接返回
        if (multiplier <= 0 || items.isEmpty()) {
            return;
        }

        // 遍历物品列表，按乘数添加每个物品
        for (ItemStack item : items) {
            // 跳过空物品或每组数量为0的物品
            if (item.isEmpty() || item.getCount() <= 0) {
                continue;
            }

            // 计算总添加数量 = 每组数量 × 乘数
            long totalToAdd = (long) item.getCount() * multiplier;
            AEItemKey key = Keys.item(item);
            if (key == null) {
                continue;
            }

            long remaining = totalToAdd - handler.insert(key, totalToAdd, false);

            if (remaining > 0 && level instanceof ServerLevel server) {
                // 拆分剩余物品为多个最大堆叠（避免单个实体超过最大堆叠）
                int maxStackSize = item.getMaxStackSize();
                while (remaining > 0) {
                    int dropCount = (int) Math.min(remaining, maxStackSize);
                    ItemStack dropStack = item.copyWithCount(dropCount);

                    // 生成物品实体：在pos上方1格位置，无拾取延迟
                    ItemEntity itemEntity = new ItemEntity(
                            server,
                            pos.getX() + 0.5, // 中心x坐标
                            pos.getY() + 1,   // 上方1格y坐标
                            pos.getZ() + 0.5, // 中心z坐标
                            dropStack);
                    itemEntity.setNoPickUpDelay(); // 立即可拾取
                    server.addFreshEntity(itemEntity); // 添加到世界
                    remaining -= dropCount; // 减少剩余数量
                }
            }
        }
    }

    /**
     * 计算库存中流体总量能支持多少组输入流体列表消耗（每组量为列表中各流体的量）
     *
     * @param tank           流体库存槽
     * @param requiredFluids 需求流体列表（每个FluidStack的amount为每组需求）
     * @return 最大支持乘数（若列表为空返回Integer.MAX_VALUE，若某流体不足则返回0）
     */
    public static int checkMaxConsumeMultiplier(IKeyHandler<AEFluidKey> tank, List<FluidStack> requiredFluids) {
        if (requiredFluids.isEmpty()) {
            return Integer.MAX_VALUE; // 无需求则支持无限大乘数
        }

        long maxMultiplier = Integer.MAX_VALUE;

        for (FluidStack required : requiredFluids) {
            if (required.isEmpty() || required.getAmount() <= 0) {
                continue; // 跳过空流体或每组需求为0的项
            }

            // 统计库存中该流体的总存量（遍历所有槽，累加相同流体的量）
            AEFluidKey key = Keys.fluid(required);
            long totalInTank = key == null ? 0 : tank.count(key);

            // 计算该流体支持的最大乘数（总存量 ÷ 每组需求）
            maxMultiplier = Math.min(maxMultiplier, totalInTank / required.getAmount());

            if (maxMultiplier == 0) {
                break; // 已无法支持1组，提前退出
            }
        }

        return (int) maxMultiplier;
    }

    /**
     * 计算库存剩余容量能容纳多少组输入流体列表（每组量为列表中各流体的量）
     *
     * @param tank        流体库存槽
     * @param inputFluids 待添加的流体列表（每个FluidStack的amount为每组量）
     * @return 最大可容纳乘数（若列表为空返回Integer.MAX_VALUE，若某流体无容量则返回0）
     */
    public static int checkMaxCapacityMultiplier(IKeyHandler<AEFluidKey> tank, List<FluidStack> inputFluids) {
        if (inputFluids.isEmpty()) {
            return Integer.MAX_VALUE; // 无输入则支持无限大乘数
        }

        long maxMultiplier = Integer.MAX_VALUE;

        for (FluidStack input : inputFluids) {
            if (input.isEmpty() || input.getAmount() <= 0) {
                continue; // 跳过空流体或每组量为0的项
            }

            // 计算库存中该流体的剩余总容量（总容量 - 现有量）
            AEFluidKey key = Keys.fluid(input);
            long totalRemainingCapacity = 0;
            if (key != null) {
                for (int slot = 0; slot < tank.size(); slot++) {
                    totalRemainingCapacity = Keys.add(totalRemainingCapacity, tank.spaceFor(slot, key));
                }
            }

            // 计算该流体可容纳的最大乘数（剩余容量 ÷ 每组量）
            maxMultiplier = Math.min(maxMultiplier, totalRemainingCapacity / input.getAmount());

            if (maxMultiplier == 0) {
                break; // 已无法容纳1组，提前退出
            }
        }

        return (int) maxMultiplier;
    }

    /**
     * 从流体库存中扣除 x 组输入流体列表（每个流体扣除量 = 每组量 × x）
     *
     * @param tank       流体库存槽
     * @param fluids     待扣除的流体列表（每组量）
     * @param multiplier 乘数 x（必须>0，否则不执行）
     * @return 实际扣除的乘数（若库存不足，可能小于x；完全成功则返回x）
     */
    public static int deductMultipliedFluids(IKeyHandler<AEFluidKey> tank, List<FluidStack> fluids, int multiplier) {
        if (multiplier <= 0 || fluids.isEmpty()) {
            return 0; // 无效参数，不执行
        }

        // 按实际乘数扣除每个流体
        for (FluidStack fluid : fluids) {
            if (fluid.isEmpty() || fluid.getAmount() <= 0) {
                continue;
            }

            long totalToDeduct = (long) fluid.getAmount() * multiplier;
            AEFluidKey key = Keys.fluid(fluid);
            if (key == null) {
                continue;
            }

            tank.extract(key, totalToDeduct, false);
        }

        return multiplier;
    }

    /**
     * 向流体库存中添加 x 组输入流体列表（每个流体添加量 = 每组量 × x）
     *
     * @param tank       流体库存槽
     * @param fluids     待添加的流体列表（每组量）
     * @param multiplier 乘数 x（必须>0，否则不执行）
     * @return 实际添加的乘数（若容量不足，可能小于x；完全成功则返回x）
     */
    public static int addMultipliedFluids(IKeyHandler<AEFluidKey> tank, List<FluidStack> fluids, int multiplier) {
        if (multiplier <= 0 || fluids.isEmpty()) {
            return 0; // 无效参数，不执行
        }

        // 按实际乘数添加每个流体
        for (FluidStack fluid : fluids) {
            if (fluid.isEmpty() || fluid.getAmount() <= 0) {
                continue;
            }

            long totalToAdd = (long) fluid.getAmount() * multiplier;
            AEFluidKey key = Keys.fluid(fluid);
            if (key == null) {
                continue;
            }

            tank.insert(key, totalToAdd, false);
        }

        return multiplier;
    }
}

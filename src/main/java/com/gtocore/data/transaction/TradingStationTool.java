package com.gtocore.data.transaction;

import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
    public static int checkMaxMultiplier(ICustomItemStackHandler handler, List<ItemStack> requiredItems) {
        if (requiredItems.isEmpty()) {
            return Integer.MAX_VALUE; // 无需求则支持无限大乘数
        }

        int maxMultiplier = Integer.MAX_VALUE;

        for (ItemStack required : requiredItems) {
            if (required.isEmpty()) {
                continue; // 跳过空物品
            }

            int requiredPerMulti = required.getCount();
            if (requiredPerMulti <= 0) {
                continue; // 每组需求为0，视为不限制该物品
            }

            // 统计库存中该物品的总数量
            int totalInStock = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack slotStack = handler.getStackInSlot(i);
                if (ItemStack.isSameItemSameTags(slotStack, required)) {
                    totalInStock += slotStack.getCount();
                }
            }

            // 计算该物品支持的最大乘数（总库存 / 每组需求）
            int itemMaxMulti = totalInStock / requiredPerMulti;
            maxMultiplier = Math.min(maxMultiplier, itemMaxMulti);

            if (maxMultiplier == 0) {
                break; // 已无法支持1组，提前退出
            }
        }

        return maxMultiplier;
    }

    /**
     * 从库存中扣除 x 组输入物品列表（每个物品扣除数量 = 列表中物品数量 × x）
     *
     * @param handler    物品库存处理器
     * @param items      待扣除的物品列表（每组数量）
     * @param multiplier 乘数 x（必须>0，否则不执行）
     */
    public static void deductMultipliedItems(ICustomItemStackHandler handler, List<ItemStack> items, int multiplier) {
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
            int totalToDeduct = perItemCount * multiplier;
            if (totalToDeduct <= 0) {
                continue;
            }

            // 从库存中扣除（复用现有逻辑的思路）
            int remaining = totalToDeduct;
            for (int i = 0; i < handler.getSlots() && remaining > 0; i++) {
                ItemStack slotStack = handler.getStackInSlot(i);
                if (ItemStack.isSameItemSameTags(slotStack, item)) {
                    int take = Math.min(slotStack.getCount(), remaining);
                    slotStack.shrink(take);
                    if (slotStack.isEmpty()) {
                        handler.setStackInSlot(i, ItemStack.EMPTY);
                    }
                    remaining -= take;
                }
            }
        }
    }

    /**
     * 向库存中添加 x 组输入物品列表，库存不足时将剩余物品掷出掉落
     *
     * @param handler    物品库存处理器
     * @param items      待添加的物品列表（每组数量）
     * @param multiplier 乘数 x（必须>0，否则不执行）
     */
    public static void addMultipliedItems(ICustomItemStackHandler handler, List<ItemStack> items, int multiplier, Level level, BlockPos pos) {
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
            int totalToAdd = item.getCount() * multiplier;
            if (totalToAdd <= 0) {
                continue;
            }

            // 复制物品堆并设置总数量（避免修改原列表物品）
            ItemStack toAdd = item.copy();
            toAdd.setCount(totalToAdd);
            ItemStack remaining = toAdd.copy(); // 剩余待添加的数量
            int maxStackSize = toAdd.getMaxStackSize(); // 物品最大堆叠数

            // 第一步：填充现有相同物品的堆叠（非空槽位）
            for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
                ItemStack existing = handler.getStackInSlot(i);
                // 只处理相同物品且未达最大堆叠的槽位
                if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, remaining) && existing.getCount() < maxStackSize) {
                    int addAmount = Math.min(remaining.getCount(), maxStackSize - existing.getCount());
                    existing.grow(addAmount); // 增加现有堆叠数量
                    remaining.shrink(addAmount); // 减少剩余待添加数量
                }
            }

            // 第二步：填充空槽位（处理剩余未添加的物品）
            while (!remaining.isEmpty()) {
                boolean foundSlot = false; // 标记是否找到空槽位
                for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
                    if (handler.getStackInSlot(i).isEmpty()) {
                        // 计算当前槽位可放置的数量（不超过最大堆叠）
                        int putAmount = Math.min(remaining.getCount(), maxStackSize);
                        ItemStack toPut = remaining.copy();
                        toPut.setCount(putAmount);
                        handler.setStackInSlot(i, toPut); // 放置物品
                        remaining.shrink(putAmount); // 减少剩余数量
                        foundSlot = true;
                    }
                }

                // 若未找到空槽位（库存已满），将剩余物品掷出
                if (!foundSlot) {
                    // 拆分剩余物品为多个最大堆叠（避免单个实体超过最大堆叠）
                    if (level instanceof ServerLevel server) {
                        while (!remaining.isEmpty()) {
                            int dropCount = Math.min(remaining.getCount(), maxStackSize);
                            ItemStack dropStack = remaining.copy();
                            dropStack.setCount(dropCount);

                            // 生成物品实体：在pos上方1格位置，无拾取延迟
                            ItemEntity itemEntity = new ItemEntity(
                                    server,
                                    pos.getX() + 0.5, // 中心x坐标
                                    pos.getY() + 1,   // 上方1格y坐标
                                    pos.getZ() + 0.5, // 中心z坐标
                                    dropStack);
                            itemEntity.setNoPickUpDelay(); // 立即可拾取
                            server.addFreshEntity(itemEntity); // 添加到世界
                            remaining.shrink(dropCount); // 减少剩余数量
                        }
                        break; // 处理完剩余物品后退出循环
                    }
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
    public static int checkMaxConsumeMultiplier(ICustomFluidStackHandler tank, List<FluidStack> requiredFluids) {
        if (requiredFluids.isEmpty()) {
            return Integer.MAX_VALUE; // 无需求则支持无限大乘数
        }

        int maxMultiplier = Integer.MAX_VALUE;

        for (FluidStack required : requiredFluids) {
            if (required.isEmpty() || required.getAmount() <= 0) {
                continue; // 跳过空流体或每组需求为0的项
            }

            // 统计库存中该流体的总存量（遍历所有槽，累加相同流体的量）
            int totalInTank = 0;
            for (int slot = 0; slot < tank.getTanks(); slot++) {
                FluidStack stored = tank.getFluidInTank(slot);
                if (stored.isFluidEqual(required)) { // 流体类型必须完全匹配
                    totalInTank += stored.getAmount();
                }
            }

            // 计算该流体支持的最大乘数（总存量 ÷ 每组需求）
            int fluidMaxMulti = totalInTank / required.getAmount();
            maxMultiplier = Math.min(maxMultiplier, fluidMaxMulti);

            if (maxMultiplier == 0) {
                break; // 已无法支持1组，提前退出
            }
        }

        return maxMultiplier;
    }

    /**
     * 计算库存剩余容量能容纳多少组输入流体列表（每组量为列表中各流体的量）
     *
     * @param tank        流体库存槽
     * @param inputFluids 待添加的流体列表（每个FluidStack的amount为每组量）
     * @return 最大可容纳乘数（若列表为空返回Integer.MAX_VALUE，若某流体无容量则返回0）
     */
    public static int checkMaxCapacityMultiplier(ICustomFluidStackHandler tank, List<FluidStack> inputFluids) {
        if (inputFluids.isEmpty()) {
            return Integer.MAX_VALUE; // 无输入则支持无限大乘数
        }

        int maxMultiplier = Integer.MAX_VALUE;

        for (FluidStack input : inputFluids) {
            if (input.isEmpty() || input.getAmount() <= 0) {
                continue; // 跳过空流体或每组量为0的项
            }

            // 计算库存中该流体的剩余总容量（总容量 - 现有量）
            int totalRemainingCapacity = 0;
            for (int slot = 0; slot < tank.getTanks(); slot++) {
                // 仅统计可容纳该流体的槽（需通过isFluidValid校验）
                if (!tank.isFluidValid(slot, input)) {
                    continue;
                }
                FluidStack stored = tank.getFluidInTank(slot);
                // 空槽或同类型流体槽可容纳
                if (stored.isEmpty() || stored.isFluidEqual(input)) {
                    totalRemainingCapacity += (tank.getTankCapacity(slot) - stored.getAmount());
                }
            }

            // 计算该流体可容纳的最大乘数（剩余容量 ÷ 每组量）
            int fluidMaxMulti = totalRemainingCapacity / input.getAmount();
            maxMultiplier = Math.min(maxMultiplier, fluidMaxMulti);

            if (maxMultiplier == 0) {
                break; // 已无法容纳1组，提前退出
            }
        }

        return maxMultiplier;
    }

    /**
     * 从流体库存中扣除 x 组输入流体列表（每个流体扣除量 = 每组量 × x）
     *
     * @param tank       流体库存槽
     * @param fluids     待扣除的流体列表（每组量）
     * @param multiplier 乘数 x（必须>0，否则不执行）
     * @return 实际扣除的乘数（若库存不足，可能小于x；完全成功则返回x）
     */
    public static int deductMultipliedFluids(ICustomFluidStackHandler tank, List<FluidStack> fluids, int multiplier) {
        if (multiplier <= 0 || fluids.isEmpty()) {
            return 0; // 无效参数，不执行
        }

        // 按实际乘数扣除每个流体
        for (FluidStack fluid : fluids) {
            if (fluid.isEmpty() || fluid.getAmount() <= 0) {
                continue;
            }

            int totalToDeduct = fluid.getAmount() * multiplier;
            if (totalToDeduct <= 0) {
                continue;
            }

            // 复制流体栈并设置扣除总量，调用drain方法执行扣除
            FluidStack toDeduct = fluid.copy();
            toDeduct.setAmount(totalToDeduct);
            tank.drain(toDeduct, IFluidHandler.FluidAction.EXECUTE);
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
    public static int addMultipliedFluids(ICustomFluidStackHandler tank, List<FluidStack> fluids, int multiplier) {
        if (multiplier <= 0 || fluids.isEmpty()) {
            return 0; // 无效参数，不执行
        }

        // 按实际乘数添加每个流体
        for (FluidStack fluid : fluids) {
            if (fluid.isEmpty() || fluid.getAmount() <= 0) {
                continue;
            }

            int totalToAdd = fluid.getAmount() * multiplier;
            if (totalToAdd <= 0) {
                continue;
            }

            // 复制流体栈并设置添加总量，调用fill方法执行添加
            FluidStack toAdd = fluid.copy();
            toAdd.setAmount(totalToAdd);
            tank.fill(toAdd, IFluidHandler.FluidAction.EXECUTE);
        }

        return multiplier;
    }
}

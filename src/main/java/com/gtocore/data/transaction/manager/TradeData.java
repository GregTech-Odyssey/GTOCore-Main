package com.gtocore.data.transaction.manager;

import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 一次交易所需的服务端上下文。
 *
 * @param level       交易站所在世界
 * @param pos         交易站位置
 * @param inputItem   物品输入库存
 * @param outputItem  物品输出库存
 * @param inputFluid  流体输入库存
 * @param outputFluid 流体输出库存
 * @param uuid        交易所属玩家 UUID
 * @param sharedUUIDs 与该玩家共享钱包的玩家 UUID
 * @param teamUUID    无线能量和魔力所属队伍 UUID
 */
public record TradeData(@Nullable Level level, BlockPos pos, IKeyHandler<AEItemKey> inputItem,
                        IKeyHandler<AEItemKey> outputItem, IKeyHandler<AEFluidKey> inputFluid,
                        IKeyHandler<AEFluidKey> outputFluid, UUID uuid, List<UUID> sharedUUIDs, UUID teamUUID) {

}

package com.gtocore.common.machine.noenergy.minesweeper;

import com.gtocore.common.data.GTOBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 扫雷控制方块：铺盘后放在棋盘右下角，顶面分三块区域，照参考 BaseVerticalRefreshBlock 的三段交互——
 * <b>西</b>（左中）重制一局、<b>东北</b>（左上）只收盘面、<b>东南</b>（右下）连核心方块一起关掉。
 */
public class MinesweeperControlBlock extends Block implements EntityBlock {

    public MinesweeperControlBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MinesweeperControlBlockEntity(GTOBlockEntities.MINESWEEPER_CONTROL.get(), pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (hit.getDirection() != Direction.UP) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof MinesweeperControlBlockEntity control)) return InteractionResult.PASS;
        BlockPos core = control.getCore();
        if (core == null || !(level.getBlockEntity(core) instanceof MinesweeperCoreBlockEntity coreEntity)) {
            MinesweeperCoreBlock.send(serverPlayer, Component.translatable(MinesweeperCoreBlock.MSG_NO_CORE));
            return InteractionResult.SUCCESS;
        }
        // 三段区域整体逆时针转 90°：把点击点先转回原布局再判（u' = 1 - v、v' = u）
        double u = 1 - (hit.getLocation().z - pos.getZ());
        double v = hit.getLocation().x - pos.getX();
        if (u < 0.5 && v >= 0.25 && v < 0.75) {
            coreEntity.restart(serverPlayer);
        } else if (u >= 0.5 && v < 0.5) {
            coreEntity.minimize(serverPlayer);
        } else if (u >= 0.5) {
            coreEntity.close(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}

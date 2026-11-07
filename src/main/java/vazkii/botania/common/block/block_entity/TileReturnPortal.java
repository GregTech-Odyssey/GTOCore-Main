package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import vazkii.botania.common.BotaniaBlocks;
import vazkii.botania.common.alfheim.Alfheim;
import vazkii.botania.common.alfheim.AlfheimPortalHandler;
import vazkii.botania.common.alfheim.AlfheimTeleporter;

/**
 * 返回门方块实体：结构失效时自毁，玩家在门内停留满计时后送回主世界。
 */
public class TileReturnPortal extends BotaniaBlockEntity {

    public TileReturnPortal(BlockPos pos, BlockState state) {
        this(BotaniaBlockEntities.RETURN_PORTAL.get(), pos, state);
    }

    public TileReturnPortal(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileReturnPortal self) {
        if (level.isClientSide) {
            return;
        }
        if (!validPortal(level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }
        if (!AlfheimPortalHandler.shouldCheck(level)) {
            return;
        }
        AABB area = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            if (player instanceof ServerPlayer serverPlayer && AlfheimPortalHandler.setInPortal(level, player)) {
                AlfheimTeleporter.teleportToOverworld(serverPlayer, pos);
            }
        }
    }

    /** 底部 3×3 需要同一种木材：活木或梦木，带发光变体。 */
    public static boolean validPortal(Level level, BlockPos pos) {
        if (!Alfheim.DIMENSION.equals(level.dimension())) {
            return false;
        }
        Boolean dreamwood = null;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                Block requiredLiving = x == 0 || z == 0 ? BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG.get() : BotaniaBlocks.LIVINGWOOD_LOG.get();
                Block requiredDream = x == 0 || z == 0 ? BotaniaBlocks.GLIMMERING_DREAMWOOD_LOG.get() : BotaniaBlocks.DREAMWOOD_LOG.get();
                Block actual = level.getBlockState(pos.offset(x, 0, z)).getBlock();
                if (dreamwood == null) {
                    if (actual == requiredLiving) {
                        dreamwood = false;
                    } else if (actual == requiredDream) {
                        dreamwood = true;
                    } else {
                        return false;
                    }
                } else if (dreamwood ? actual != requiredDream : actual != requiredLiving) {
                    return false;
                }
            }
        }
        return true;
    }
}

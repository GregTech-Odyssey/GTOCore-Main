package vazkii.botania.common.alfheim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.BotaniaBlocks;
import vazkii.botania.common.MythicBotanyBlocks;
import vazkii.botania.common.worldgen.HorizontalPos;

/** 亚尔夫海姆与主世界之间的传送。 */
public final class AlfheimTeleporter {

    private AlfheimTeleporter() {}

    public static boolean teleportToAlfheim(ServerPlayer player, BlockPos sourcePos) {
        ServerLevel target = player.server.getLevel(Alfheim.DIMENSION);
        if (target == null) {
            player.setPortalCooldown(200);
            return false;
        }
        BlockPos pos = findBlock(target, new HorizontalPos(sourcePos), MythicBotanyBlocks.MB_RETURN_PORTAL.get());
        if (pos == null) {
            pos = vazkii.botania.common.worldgen.AlfheimWorldGen
                    .highestFreeBlock(target, new HorizontalPos(sourcePos)).below();
            target.setBlock(pos.north(), BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG.get().defaultBlockState(), 3);
            target.setBlock(pos.south(), BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG.get().defaultBlockState(), 3);
            target.setBlock(pos.east(), BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG.get().defaultBlockState(), 3);
            target.setBlock(pos.west(), BotaniaBlocks.GLIMMERING_LIVINGWOOD_LOG.get().defaultBlockState(), 3);
            target.setBlock(pos.offset(-1, 0, -1), BotaniaBlocks.LIVINGWOOD_LOG.get().defaultBlockState(), 3);
            target.setBlock(pos.offset(-1, 0, 1), BotaniaBlocks.LIVINGWOOD_LOG.get().defaultBlockState(), 3);
            target.setBlock(pos.offset(1, 0, -1), BotaniaBlocks.LIVINGWOOD_LOG.get().defaultBlockState(), 3);
            target.setBlock(pos.offset(1, 0, 1), BotaniaBlocks.LIVINGWOOD_LOG.get().defaultBlockState(), 3);
            target.setBlock(pos, MythicBotanyBlocks.MB_RETURN_PORTAL.get().defaultBlockState(), 3);
        }
        player.teleportTo(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setPortalCooldown();
        return true;
    }

    public static void teleportToOverworld(ServerPlayer player, BlockPos sourcePos) {
        ServerLevel target = player.server.overworld();
        BlockPos pos = findBlock(target, new HorizontalPos(sourcePos), BotaniaBlocks.ALFHEIM_PORTAL.get());
        if (pos != null) {
            pos = pos.above();
        } else {
            pos = vazkii.botania.common.worldgen.AlfheimWorldGen
                    .highestFreeBlock(target, new HorizontalPos(sourcePos));
        }
        player.teleportTo(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setPortalCooldown();
    }

    @Nullable
    private static BlockPos findBlock(ServerLevel level, HorizontalPos sourcePos, Block block) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(sourcePos.x(), level.getMaxBuildHeight(),
                sourcePos.z());
        while (pos.getY() > level.getMinBuildHeight()) {
            pos.move(Direction.DOWN);
            if (level.getBlockState(pos).is(block)) {
                return pos.immutable();
            }
        }
        return null;
    }
}

package vazkii.botania.forge.integration.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaReceiver;

/** 从方块实体直接查找魔力接收者。 */
final class ManaReceiverLookup {

    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;

    ManaReceiverLookup(ServerLevel level, BlockPos pos, Direction side) {
        this.level = level;
        this.pos = pos.immutable();
        this.side = side;
    }

    @Nullable
    ManaReceiver find() {
        if (!level.isLoaded(pos)) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ManaReceiver receiver) {
            return receiver;
        }
        return GenericInventoryManaReceiver.find(be, side);
    }
}

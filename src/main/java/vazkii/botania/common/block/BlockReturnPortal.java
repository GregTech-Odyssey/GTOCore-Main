package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.TileReturnPortal;

/** 亚尔夫海姆返回门：由传送器生成，玩家进入后返回主世界。 */
public class BlockReturnPortal extends Block implements EntityBlock {

    public BlockReturnPortal(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TileReturnPortal(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != BotaniaBlockEntities.RETURN_PORTAL.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> TileReturnPortal.serverTick(tickLevel, pos,
                tickState, (TileReturnPortal) be);
    }
}

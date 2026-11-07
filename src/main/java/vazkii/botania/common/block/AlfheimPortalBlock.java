package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.AlfheimPortalBlockEntity;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;

/**
 * 精灵门核心：按原模组的 5×5×5 结构激活，激活后可通行到亚尔夫海姆。
 */
public class AlfheimPortalBlock extends Block implements EntityBlock {

    public AlfheimPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ReplicaVisualBlocks.PORTAL_STATE,
                ReplicaVisualBlocks.PortalState.OFF));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ReplicaVisualBlocks.PORTAL_STATE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AlfheimPortalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != BotaniaBlockEntities.ALF_PORTAL.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> AlfheimPortalBlockEntity.serverTick(tickLevel,
                pos, tickState, (AlfheimPortalBlockEntity) be);
    }
}

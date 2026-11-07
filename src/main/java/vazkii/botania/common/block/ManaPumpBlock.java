package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.mana.ManaPumpBlockEntity;

/**
 * 魔力泵：把魔力送入或送出矿车，红石信号停止工作。
 */
public class ManaPumpBlock extends Block implements EntityBlock {

    private static final VoxelShape X_SHAPE = box(0, 0, 4, 16, 8, 12);
    private static final VoxelShape Z_SHAPE = box(4, 0, 0, 12, 8, 16);

    public ManaPumpBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @NotNull
    @Override
    public BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                mirror.mirror(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @NotNull
    @Override
    public BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                               @NotNull CollisionContext context) {
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ManaPumpBlockEntity pump ? pump.comparator : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ManaPumpBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != BotaniaBlockEntities.PUMP.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> ManaPumpBlockEntity.commonTick(tickLevel, pos,
                tickState, (ManaPumpBlockEntity) be);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                            @NotNull RandomSource random) {
        if (level.hasNeighborSignal(pos)) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
    }
}

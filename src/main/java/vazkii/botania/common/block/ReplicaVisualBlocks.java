package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

/**
 * 复刻方块的多状态类型。
 *
 * <p>
 * 状态属性与状态切换仿照原模组对应方块：朝向方块按点击方向放置并支持旋转、镜像；
 * 大型魔力池按相邻池体更新连接；侦测方块输出红石信号。只复刻状态，不移植原模组的功能逻辑。
 * </p>
 */
public final class ReplicaVisualBlocks {

    private ReplicaVisualBlocks() {}

    /** 水平四向朝向，对应 {@code facing=north/south/east/west}。 */
    public static class FacingHorizontal extends HorizontalDirectionalBlock {

        public FacingHorizontal(Properties properties) {
            super(properties);
            registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        public BlockState rotate(BlockState state, Rotation rotation) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        @Override
        public BlockState mirror(BlockState state, Mirror mirror) {
            return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
        }
    }

    /** 六向朝向，对应 {@code facing=down/up/north/south/east/west}。 */
    public static class FacingAny extends DirectionalBlock {

        public FacingAny(Properties properties) {
            super(properties);
            registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
        }

        @Override
        public BlockState rotate(BlockState state, Rotation rotation) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        @Override
        public BlockState mirror(BlockState state, Mirror mirror) {
            return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
        }
    }

    /** 双层花，对应 {@code half=lower/upper}；放置与破坏时按原版双层方块成对处理。 */
    public static class DoublePlant extends DoublePlantBlock {

        public DoublePlant(Properties properties) {
            super(properties);
        }

        @Override
        public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            return true;
        }
    }

    /** 通电方块：可被红石充能并输出与原模组相同的信号强度。 */
    public static class Powered extends Block {

        public Powered(Properties properties) {
            super(properties);
            registerDefaultState(defaultBlockState().setValue(BlockStateProperties.POWERED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(BlockStateProperties.POWERED);
        }

        @Override
        public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                    boolean isMoving) {
            super.neighborChanged(state, level, pos, block, fromPos, isMoving);
            if (level.isClientSide) return;
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != state.getValue(BlockStateProperties.POWERED)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, powered), Block.UPDATE_CLIENTS);
            }
        }

        @Override
        public boolean isSignalSource(BlockState state) {
            return true;
        }

        @Override
        public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
            return state.getValue(BlockStateProperties.POWERED) ? 15 : 0;
        }
    }

    /** 幽灵铁轨形状，取值与原模组方块状态一致。 */
    public enum RailShape implements StringRepresentable {

        NORTH_SOUTH,
        EAST_WEST,
        ASCENDING_EAST,
        ASCENDING_WEST,
        ASCENDING_NORTH,
        ASCENDING_SOUTH;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** 精灵传送门状态，取值与原模组方块状态一致。 */
    public enum PortalState implements StringRepresentable {

        OFF,
        ON_X,
        ON_Z;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** 合成箱图案，取值与原模组方块状态一致。 */
    public enum CratePattern implements StringRepresentable {

        NONE,
        CRAFTY_1_1,
        CRAFTY_1_2,
        CRAFTY_1_3,
        CRAFTY_2_1,
        CRAFTY_2_2,
        CRAFTY_2_3,
        CRAFTY_3_1,
        CRAFTY_3_2,
        CRAFTY_DONUT;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** 铁轨形状，对应 {@code shape}。 */
    public static final EnumProperty<RailShape> RAIL_SHAPE = EnumProperty.create("shape", RailShape.class);

    /** 精灵传送门状态，对应 {@code state}。 */
    public static final EnumProperty<PortalState> PORTAL_STATE = EnumProperty.create("state", PortalState.class);

    /** 合成箱图案，对应 {@code pattern}。 */
    public static final EnumProperty<CratePattern> CRATE_PATTERN = EnumProperty.create("pattern", CratePattern.class);

    /** 幽灵铁轨方块，状态取自静态属性。 */
    public static class RailShaped extends Block {

        public RailShaped(Properties properties) {
            super(properties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(RAIL_SHAPE);
        }
    }

    /** 精灵传送门方块，状态取自静态属性。 */
    public static class PortalStated extends Block {

        public PortalStated(Properties properties) {
            super(properties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(PORTAL_STATE);
        }
    }

    /** 合成箱方块，状态取自静态属性。 */
    public static class CratePatterned extends Block {

        public CratePatterned(Properties properties) {
            super(properties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(CRATE_PATTERN);
        }
    }

    /** 朝向属性访问别名。 */
    public static DirectionProperty horizontalFacing() {
        return HorizontalDirectionalBlock.FACING;
    }

    /** 六向朝向属性访问别名。 */
    public static DirectionProperty anyFacing() {
        return DirectionalBlock.FACING;
    }

    /** 朝向默认值。 */
    public static Direction defaultFacing() {
        return Direction.NORTH;
    }
}

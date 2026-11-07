package vazkii.botania.common.block.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.mana.LargeManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPoolSpec;

import java.util.function.BiFunction;

/**
 * 大型魔力池。
 *
 * <p>
 * 保持原模组的多单元外观：每个单元记录自己相对 3×3 核心的偏移，只有同一核心的单元互相连接，
 * 两个相邻的池不会被合并；核心与单元的关系决定围栏、碰撞形状、核心标记与掉落。
 * 容量来自 {@link ManaPoolSpec#LARGE}。
 * </p>
 */
public class LargeManaPoolBlock extends ManaPoolBlock {

    public static final BooleanProperty CORE = BooleanProperty.create("core");
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty NORTH_EAST = BooleanProperty.create("north_east");
    public static final BooleanProperty SOUTH_EAST = BooleanProperty.create("south_east");
    public static final BooleanProperty SOUTH_WEST = BooleanProperty.create("south_west");
    public static final BooleanProperty NORTH_WEST = BooleanProperty.create("north_west");

    public static final int FLOOR = 4;
    public static final int RIM = 4;

    private static final VoxelShape[] SHAPES = new VoxelShape[256];

    static {
        for (int i = 0; i < SHAPES.length; i++) {
            boolean n = (i & 1) != 0;
            boolean e = (i & 2) != 0;
            boolean s = (i & 4) != 0;
            boolean w = (i & 8) != 0;
            boolean ne = (i & 16) != 0;
            boolean se = (i & 32) != 0;
            boolean sw = (i & 64) != 0;
            boolean nw = (i & 128) != 0;
            VoxelShape shape = box(0, 0, 0, 16, FLOOR, 16);
            if (!n) {
                shape = Shapes.or(shape, box(0, FLOOR, 0, 16, 16, RIM));
            }
            if (!s) {
                shape = Shapes.or(shape, box(0, FLOOR, 16 - RIM, 16, 16, 16));
            }
            if (!w) {
                shape = Shapes.or(shape, box(0, FLOOR, 0, RIM, 16, 16));
            }
            if (!e) {
                shape = Shapes.or(shape, box(16 - RIM, FLOOR, 0, 16, 16, 16));
            }
            if (n && e && !ne) {
                shape = Shapes.or(shape, box(16 - RIM, FLOOR, 0, 16, 16, RIM));
            }
            if (s && e && !se) {
                shape = Shapes.or(shape, box(16 - RIM, FLOOR, 16 - RIM, 16, 16, 16));
            }
            if (s && w && !sw) {
                shape = Shapes.or(shape, box(0, FLOOR, 16 - RIM, RIM, 16, 16));
            }
            if (n && w && !nw) {
                shape = Shapes.or(shape, box(0, FLOOR, 0, RIM, 16, RIM));
            }
            SHAPES[i] = shape.optimize();
        }
    }

    private final BiFunction<BlockPos, BlockState, ? extends LargeManaPoolBlockEntity> factory;

    public LargeManaPoolBlock(Properties properties) {
        this(properties, ManaPoolSpec.LARGE, LargeManaPoolBlockEntity::new);
    }

    public LargeManaPoolBlock(Properties properties, ManaPoolSpec spec,
                              BiFunction<BlockPos, BlockState, ? extends LargeManaPoolBlockEntity> factory) {
        super(Variant.DEFAULT, properties, spec);
        this.factory = factory;
        registerDefaultState(defaultBlockState()
                .setValue(CORE, false)
                .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false)
                .setValue(NORTH_EAST, false).setValue(SOUTH_EAST, false).setValue(SOUTH_WEST, false)
                .setValue(NORTH_WEST, false));
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> {
            if (!(be instanceof LargeManaPoolBlockEntity pool)) {
                return;
            }
            if (tickLevel.isClientSide) {
                LargeManaPoolBlockEntity.clientTick(tickLevel, pos, tickState, pool);
            } else {
                LargeManaPoolBlockEntity.serverTick(tickLevel, pos, tickState, pool);
            }
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CORE, NORTH, EAST, SOUTH, WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST);
    }

    private static int shapeIndex(BlockState state) {
        return (state.getValue(NORTH) ? 1 : 0) | (state.getValue(EAST) ? 2 : 0) | (state.getValue(SOUTH) ? 4 : 0) | (state.getValue(WEST) ? 8 : 0) | (state.getValue(NORTH_EAST) ? 16 : 0) | (state.getValue(SOUTH_EAST) ? 32 : 0) | (state.getValue(SOUTH_WEST) ? 64 : 0) | (state.getValue(NORTH_WEST) ? 128 : 0);
    }

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[shapeIndex(state)];
    }

    @NotNull
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPES[shapeIndex(state)];
    }

    // ==================== 连接 ====================

    /** 按各单元记录的核心位置计算连接状态：同一核心相连，两个核心相距 3 格时视为两池相邻。 */
    public BlockState withConnections(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos n = pos.north();
        BlockPos e = pos.east();
        BlockPos s = pos.south();
        BlockPos w = pos.west();
        BlockPos ne = n.east();
        BlockPos se = s.east();
        BlockPos sw = s.west();
        BlockPos nw = n.west();
        if (!loaded(level, n) || !loaded(level, e) || !loaded(level, s) || !loaded(level, w) || !loaded(level, ne) || !loaded(level, se) || !loaded(level, sw) || !loaded(level, nw)) {
            return state;
        }
        BlockPos self = coreOf(level, pos);
        BlockPos cn = coreOf(level, n);
        BlockPos ce = coreOf(level, e);
        BlockPos cs = coreOf(level, s);
        BlockPos cw = coreOf(level, w);
        BlockPos cne = coreOf(level, ne);
        BlockPos cse = coreOf(level, se);
        BlockPos csw = coreOf(level, sw);
        BlockPos cnw = coreOf(level, nw);
        return state.setValue(NORTH, linked(self, cn))
                .setValue(EAST, linked(self, ce))
                .setValue(SOUTH, linked(self, cs))
                .setValue(WEST, linked(self, cw))
                .setValue(NORTH_EAST, linked(cn, cne) && linked(ce, cne))
                .setValue(SOUTH_EAST, linked(cs, cse) && linked(ce, cse))
                .setValue(SOUTH_WEST, linked(cs, csw) && linked(cw, csw))
                .setValue(NORTH_WEST, linked(cn, cnw) && linked(cw, cnw));
    }

    private static boolean loaded(BlockGetter level, BlockPos pos) {
        return !(level instanceof LevelReader reader) || reader.hasChunkAt(pos);
    }

    @Nullable
    private BlockPos coreOf(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof LargeManaPoolBlockEntity part && part.getBlockState().is(this) ? part.getCorePos() : null;
    }

    /** 两个单元是否属于同一池，或分属两个刚好相邻的池。 */
    public static boolean linked(@Nullable BlockPos coreA, @Nullable BlockPos coreB) {
        if (coreA == null || coreB == null || coreA.getY() != coreB.getY()) {
            return false;
        }
        int dx = Math.abs(coreB.getX() - coreA.getX());
        int dz = Math.abs(coreB.getZ() - coreA.getZ());
        return dx == 0 && dz == 0 || dx == 3 && dz == 0 || dx == 0 && dz == 3;
    }

    /** 刷新以某点为中心、给定半径内所有本方块单元的连接与核心标记。 */
    public void refreshConnections(Level level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, 0, radius))) {
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.is(this)) {
                continue;
            }
            BlockState updated = withConnections(level, pos, state);
            if (level.getBlockEntity(pos) instanceof LargeManaPoolBlockEntity pool && updated.getValue(CORE) != pool.isCore()) {
                updated = updated.setValue(CORE, pool.isCore());
            }
            if (updated != state) {
                level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
            }
        }
    }

    @NotNull
    @Override
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState,
                                  @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        return direction.getAxis().isHorizontal() ? withConnections(level, pos, state) : state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            refreshConnections(level, pos, 2);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            refreshConnections(level, pos, 2);
        }
    }

    // ==================== 破坏 ====================

    /** 只有核心掉落物品；非核心单元由 {@link #playerWillDestroy} 代为掉出核心的那一份。 */
    @NotNull
    @Override
    public java.util.List<net.minecraft.world.item.ItemStack> getDrops(@NotNull BlockState state, @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        return state.getValue(CORE) ? super.getDrops(state, params) : java.util.List.of();
    }

    /** 破坏非核心单元时，把核心的资源也一并掉出，并移除核心（随后由 onRemove 级联清掉其余单元）。 */
    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && !state.getValue(CORE) && level.getBlockEntity(pos) instanceof LargeManaPoolBlockEntity part) {
            BlockPos core = part.getCorePos();
            BlockState coreState = level.isLoaded(core) ? level.getBlockState(core) : null;
            if (coreState != null && coreState.is(this)) {
                if (!player.isCreative() && player.hasCorrectToolForDrops(coreState)) {
                    Block.dropResources(coreState, level, core, level.getBlockEntity(core), player, player.getMainHandItem());
                }
                level.setBlock(core, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    /** 核心被破坏时清掉同一池的其余单元；其他方式移除单元时连带破坏核心，保证整池一起消失。 */
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved) {
        if (!newState.is(this) && !level.isClientSide && level.getBlockEntity(pos) instanceof LargeManaPoolBlockEntity part) {
            BlockPos core = part.getCorePos();
            if (state.getValue(CORE)) {
                for (BlockPos other : BlockPos.betweenClosed(core.offset(-1, 0, -1), core.offset(1, 0, 1))) {
                    if (!other.equals(pos) && level.isLoaded(other) && level.getBlockEntity(other) instanceof LargeManaPoolBlockEntity otherPart && otherPart.getCorePos().equals(core)) {
                        level.setBlock(other.immutable(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
                refreshConnections(level, core, 2);
            } else if (level.isLoaded(core) && level.getBlockState(core).is(this)) {
                level.destroyBlock(core, true);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}

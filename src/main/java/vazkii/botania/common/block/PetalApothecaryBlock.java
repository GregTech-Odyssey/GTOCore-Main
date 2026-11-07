package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.PetalApothecaryBlockEntity;

/**
 * 花药台方块：形状与原模组一致，负责水桶交互与物品碰撞转发。
 */
public class PetalApothecaryBlock extends Block implements EntityBlock {

    private static final VoxelShape BASE = box(2, 0, 2, 14, 2, 14);
    private static final VoxelShape PILLAR = box(4, 2, 4, 12, 11, 12);
    private static final VoxelShape TOP = box(2, 11, 2, 14, 16, 14);
    private static final VoxelShape TOP_CUTOUT = box(3, 12, 3, 13, 16, 13);
    private static final VoxelShape SHAPE_INTERACT = Shapes.or(Shapes.or(BASE, PILLAR), TOP);
    private static final VoxelShape SHAPE = Shapes.join(SHAPE_INTERACT, TOP_CUTOUT, BooleanOp.ONLY_FIRST);

    public PetalApothecaryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                               @NotNull CollisionContext context) {
        return SHAPE;
    }

    @NotNull
    @Override
    public VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return SHAPE_INTERACT;
    }

    @Override
    public void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                             @NotNull Entity entity) {
        if (level.getBlockEntity(pos) instanceof PetalApothecaryBlockEntity altar && entity instanceof ItemEntity item) {
            altar.collideEntityItem(item);
        }
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                 @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof PetalApothecaryBlockEntity altar) {
            ItemStack stack = player.getItemInHand(hand);
            if (altar.getFluid() == PetalApothecaryBlockEntity.Fluid.EMPTY && stack.is(Items.WATER_BUCKET)) {
                if (!level.isClientSide) {
                    altar.setFluid(PetalApothecaryBlockEntity.Fluid.WATER);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    player.addItem(new ItemStack(Items.BUCKET));
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new PetalApothecaryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != BotaniaBlockEntities.PETAL_APOTHECARY.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> PetalApothecaryBlockEntity.serverTick(tickLevel,
                pos, tickState, (PetalApothecaryBlockEntity) be);
    }
}

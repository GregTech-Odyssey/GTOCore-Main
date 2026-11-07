package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;

/**
 * 符文祭坛方块：形状与原模组一致，右键投入材料，最后投入活石开始吸收魔力。
 */
public class RunicAltarBlock extends Block implements EntityBlock {

    private static final VoxelShape TOP = box(0, 6, 0, 16, 12, 16);
    private static final VoxelShape BOTTOM = box(2, 0, 2, 14, 6, 14);
    private static final VoxelShape SHAPE = Shapes.join(TOP, BOTTOM, BooleanOp.OR);

    public RunicAltarBlock(BlockBehaviour.Properties properties) {
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
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return SHAPE;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                 @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof RunicAltarBlockEntity altar && !player.isShiftKeyDown() && altar.addItem(player, player.getItemInHand(hand))) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new RunicAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != BotaniaBlockEntities.RUNIC_ALTAR.get()) {
            return null;
        }
        return (BlockEntityTicker<T>) (tickLevel, pos, tickState, be) -> RunicAltarBlockEntity.serverTick(tickLevel, pos,
                tickState, (RunicAltarBlockEntity) be);
    }
}

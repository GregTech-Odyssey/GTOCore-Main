package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

/**
 * 盆栽花方块。
 *
 * <p>
 * 碰撞箱与花盆一致：底部居中的 6×6×6 区域（5 到 11 宽、0 到 6 高），
 * 不占满整格，也不遮挡相邻方块的面。
 * </p>
 */
public class PottedFlowerBlock extends Block {

    private static final VoxelShape SHAPE = box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);

    public PottedFlowerBlock(BlockBehaviour.Properties properties) {
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
}

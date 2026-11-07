package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

/**
 * 微光蘑菇。
 *
 * <p>
 * 与原模组一致，蘑菇可以放在任意非空气方块上，不吃骨粉。
 * </p>
 */
public class MysticalMushroomBlock extends BushBlock {

    private static final VoxelShape SHAPE = box(4.8, 0, 4.8, 12.8, 16, 12.8);

    public final DyeColor color;

    public MysticalMushroomBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return !state.isAir() && !state.liquid();
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                               @NotNull CollisionContext context) {
        return SHAPE;
    }
}

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
 * 浮空花方块。
 *
 * <p>
 * 碰撞箱与原模组一致：居中 12.8 格见方的立方体（1.6 到 14.4），
 * 小于整格，因此不会占满方块空间；方块本身也不遮挡相邻方块的面。
 * </p>
 */
public class FloatingFlowerBlock extends Block {

    private static final VoxelShape SHAPE = box(1.6, 1.6, 1.6, 14.4, 14.4, 14.4);

    public FloatingFlowerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                               @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull net.minecraft.world.level.Level level,
                            @NotNull BlockPos pos, @NotNull net.minecraft.util.RandomSource random) {
        if (random.nextInt(6) != 0) {
            return;
        }
        level.addParticle(
                vazkii.botania.client.fx.SparkleParticleData.sparkle(random.nextFloat(), 1F, 1F, 1F, 5),
                pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.4 + random.nextDouble() * 0.4,
                pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0, 0.02, 0);
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                                        @NotNull CollisionContext context) {
        return SHAPE;
    }
}

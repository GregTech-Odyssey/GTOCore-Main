package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.BotaniaReplica;

/**
 * 植物魔法花的公共方块基类。
 *
 * <p>
 * 神秘花、荧光花以及功能花、产魔花、特殊花的方块都从这里派生：只能放在泥土标签的方块上，
 * 外形是小花，用骨粉会变成同色的高神秘花。
 * </p>
 */
public class BotaniaFlowerBlock extends BushBlock implements BonemealableBlock {

    private static final VoxelShape SHAPE = box(5, 0, 5, 11, 10, 11);

    public final DyeColor color;

    public BotaniaFlowerBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                               @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isValidBonemealTarget(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state,
                                         boolean isClient) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(@NotNull net.minecraft.world.level.Level level, @NotNull RandomSource random,
                                     @NotNull BlockPos pos, @NotNull BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public void performBonemeal(@NotNull ServerLevel level, @NotNull RandomSource random, @NotNull BlockPos pos,
                                @NotNull BlockState state) {
        Block doubleFlower = BotaniaReplica.BOTANIA.getBlock(color.getName() + "_double_flower");
        if (doubleFlower instanceof DoublePlantBlock) {
            DoublePlantBlock.placeAt(level, doubleFlower.defaultBlockState(), pos, Block.UPDATE_ALL);
        }
    }
}

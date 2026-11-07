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
 * 埋藏花瓣。
 *
 * <p>
 * 贴在泥土上的一层花瓣：外形只有一格高的 1 像素，没有碰撞箱，用骨粉会变成同色的高神秘花。
 * </p>
 */
public class BuriedPetalBlock extends BushBlock implements BonemealableBlock {

    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 1, 16);

    public final DyeColor color;

    public BuriedPetalBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
                               @NotNull CollisionContext context) {
        return SHAPE;
    }

    /** 与原模组一致，破坏埋藏花瓣掉回对应的神秘花瓣。 */
    @NotNull
    @Override
    public java.util.List<net.minecraft.world.item.ItemStack> getDrops(@NotNull BlockState state,
                                                                       @NotNull net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        net.minecraft.world.item.Item petal = BotaniaReplica.BOTANIA.getItem(color.getName() + "_petal");
        return petal == null ? java.util.List.of() : java.util.List.of(new net.minecraft.world.item.ItemStack(petal));
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

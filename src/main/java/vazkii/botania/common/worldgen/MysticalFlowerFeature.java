package vazkii.botania.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.BotaniaReplica;
import vazkii.botania.common.block.BotaniaFlowerBlock;

/** 神秘花的地块生成：与放置单个方块相同，但神秘花有概率直接长成同色高神秘花。 */
public class MysticalFlowerFeature extends Feature<MysticalFlowerConfig> {

    public MysticalFlowerFeature(Codec<MysticalFlowerConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<MysticalFlowerConfig> ctx) {
        MysticalFlowerConfig config = ctx.config();
        WorldGenLevel level = ctx.level();
        BlockPos pos = ctx.origin();
        BlockState state = config.toPlace().getState(ctx.random(), pos);
        if (!state.canSurvive(level, pos)) {
            return false;
        }
        if (state.getBlock().getClass() == BotaniaFlowerBlock.class && ctx.random().nextFloat() < config.tallChance()) {
            if (!level.isEmptyBlock(pos.above())) {
                return false;
            }
            DyeColor color = ((BotaniaFlowerBlock) state.getBlock()).color;
            Block doubleFlower = BotaniaReplica.BOTANIA.getBlock(color.getName() + "_double_flower");
            if (!(doubleFlower instanceof DoublePlantBlock)) {
                return false;
            }
            DoublePlantBlock.placeAt(level, doubleFlower.defaultBlockState(), pos, Block.UPDATE_CLIENTS);
        } else {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
        return true;
    }
}

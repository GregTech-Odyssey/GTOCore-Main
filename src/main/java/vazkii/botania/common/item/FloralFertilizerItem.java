package vazkii.botania.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.BotaniaReplica;
import vazkii.botania.common.BotaniaTags;
import vazkii.botania.common.block.BotaniaFlowerBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 花肥。
 *
 * <p>
 * 点在泥土标签方块上，在周围随机长出神秘花、高神秘花或微光蘑菇。
 * </p>
 */
public class FloralFertilizerItem extends Item {

    private static final int RANGE = 3;
    private static final float TALL_CHANCE = 0.05F;

    public FloralFertilizerItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (!level.getBlockState(pos).is(BlockTags.DIRT)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            Optional<HolderSet.Named<Block>> flowersTag = BuiltInRegistries.BLOCK.getTag(BotaniaTags.Blocks.MYSTICAL_FLOWERS);
            Optional<HolderSet.Named<Block>> mushroomsTag = BuiltInRegistries.BLOCK.getTag(BotaniaTags.Blocks.SHIMMERING_MUSHROOMS);
            boolean flowersAvailable = flowersTag.map(holders -> holders.size() > 0).orElse(false);
            boolean mushroomsAvailable = mushroomsTag.map(holders -> holders.size() > 0).orElse(false);

            List<BlockPos> validCoords = new ArrayList<>();
            for (BlockPos candidate : BlockPos.betweenClosed(
                    pos.getX() - RANGE, pos.getY() - 2, pos.getZ() - RANGE,
                    pos.getX() + RANGE, pos.getY() + 2, pos.getZ() + RANGE)) {
                if (!level.isInWorldBounds(candidate) || !level.isEmptyBlock(candidate)) {
                    continue;
                }
                BlockState below = level.getBlockState(candidate.below());
                if (flowersAvailable && canPlaceFlower(below, level) || mushroomsAvailable && canPlaceMushroom(below)) {
                    validCoords.add(candidate.immutable());
                }
            }

            int count = level.random.nextIntBetweenInclusive(5, 7);
            while (count > 0 && !validCoords.isEmpty()) {
                count--;
                BlockPos coords = validCoords.remove(level.random.nextInt(validCoords.size()));
                BlockState below = level.getBlockState(coords.below());
                boolean tryFlower = flowersAvailable && canPlaceFlower(below, level);
                boolean tryMushroom = mushroomsAvailable && canPlaceMushroom(below);
                if (tryMushroom && (!tryFlower || level.random.nextInt(3) == 0)) {
                    mushroomsTag.get().getRandomElement(level.random)
                            .map(Holder::value)
                            .ifPresent(block -> level.setBlockAndUpdate(coords, block.defaultBlockState()));
                } else if (tryFlower) {
                    count--;
                    flowersTag.get().getRandomElement(level.random).ifPresent(holder -> placeFlower(level, coords, holder.value()));
                }
            }
            ctx.getItemInHand().shrink(1);
        } else {
            for (int i = 0; i < 15; i++) {
                double x = pos.getX() - RANGE + level.random.nextInt(RANGE * 2 + 1) + Math.random();
                double y = pos.getY() + 1;
                double z = pos.getZ() - RANGE + level.random.nextInt(RANGE * 2 + 1) + Math.random();
                WispParticleData data = WispParticleData.wisp(0.15F + (float) Math.random() * 0.25F,
                        (float) Math.random(), (float) Math.random(), (float) Math.random(), 1);
                level.addParticle(data, x, y, z, 0, (float) Math.random() * 0.1F - 0.05F, 0);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** 神秘花有概率直接长成同色高神秘花。 */
    private static void placeFlower(Level level, BlockPos pos, Block flower) {
        if (flower instanceof BotaniaFlowerBlock botaniaFlower && level.getBlockState(pos.above()).isAir() && level.random.nextFloat() < TALL_CHANCE) {
            Block doubleFlower = BotaniaReplica.BOTANIA.getBlock(botaniaFlower.color.getName() + "_double_flower");
            if (doubleFlower instanceof DoublePlantBlock) {
                DoublePlantBlock.placeAt(level, doubleFlower.defaultBlockState(), pos, Block.UPDATE_CLIENTS);
                return;
            }
        }
        level.setBlockAndUpdate(pos, flower.defaultBlockState());
    }

    private static boolean canPlaceMushroom(BlockState below) {
        return below.is(BlockTags.MUSHROOM_GROW_BLOCK);
    }

    private static boolean canPlaceFlower(BlockState below, Level level) {
        return below.is(BlockTags.DIRT) && !level.dimensionType().ultraWarm();
    }
}

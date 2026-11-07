package vazkii.botania.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nonnull;

public class RandomFoliagePlacer extends FoliagePlacer {

    public static final Codec<RandomFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProvider.CODEC.fieldOf("radius").forGetter(placer -> placer.radius),
            IntProvider.CODEC.fieldOf("offset").forGetter(placer -> placer.offset))
            .apply(instance, RandomFoliagePlacer::new));

    public RandomFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Nonnull
    @Override
    protected FoliagePlacerType<?> type() {
        return MythicBotanyWorldgen.RANDOM_FOLIAGE;
    }

    @Override
    protected void createFoliage(@Nonnull LevelSimulatedReader level, @Nonnull FoliageSetter blockSetter, @Nonnull RandomSource random, @Nonnull TreeConfiguration config, int maxFreeTreeHeight, @Nonnull FoliageAttachment attachment, int foliageHeight, int foliageRadius, int offset) {
        int leaves = 3 + random.nextInt(3);
        for (int i = 0; i < leaves; i++) {
            int x = random.nextInt(5) - 2;
            int y = random.nextInt(5) - 2;
            int z = random.nextInt(5) - 2;
            if (this.placeLeave(level, blockSetter, attachment.pos().offset(x, y, z), random, config)) {
                int nx = x == 0 ? 0 : x > 0 ? x - 1 : x + 1;
                int ny = y == 0 ? 0 : y > 0 ? y - 1 : y + 1;
                int nz = z == 0 ? 0 : z > 0 ? z - 1 : z + 1;
                this.placeLeave(level, blockSetter, attachment.pos().offset(nx, ny, nz), random, config);
            }
        }
    }

    @Override
    public int foliageHeight(@Nonnull RandomSource random, int height, @Nonnull TreeConfiguration config) {
        return 3;
    }

    @Override
    protected boolean shouldSkipLocation(@Nonnull RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return localX == range && localZ == range && (random.nextInt(2) == 0 || localY == 0);
    }

    private boolean placeLeave(LevelSimulatedReader level, FoliageSetter blockSetter, BlockPos pos, RandomSource rand, TreeConfiguration config) {
        if (level.isStateAtPosition(pos, state -> state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(Blocks.WATER))) {
            blockSetter.set(pos, config.foliageProvider.getState(rand, pos));
            return true;
        } else {
            return false;
        }
    }

    public IntProvider getRadius() {
        return this.radius;
    }

    public IntProvider getOffset() {
        return this.offset;
    }
}

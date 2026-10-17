package com.gtocore.common.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import com.arsmeteorites.arsmeteorites.common.RecipeRegistry;
import com.arsmeteorites.arsmeteorites.common.RecipeRegistry.MeteoriteType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Collection;
import java.util.Iterator;

public final class MeteoriteFeature extends Feature<MeteoriteFeature.Configuration> {

    private static final int MAX_RADIUS = 16;

    public MeteoriteFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        MeteoriteType type = getRandomType(context.random());
        if (type == null || type.meteorites().length == 0) return false;

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int radius = context.config().radius().sample(random);
        Block[] blocks = type.meteorites();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        boolean placed = placeIfEmpty(level, origin, blocks[0].defaultBlockState());

        for (int shell = 1; shell <= radius; shell++) {
            placed |= placeShell(level, origin, mutablePos, random, type, shell, radius);
        }
        return placed;
    }

    private boolean placeShell(WorldGenLevel level, BlockPos origin, BlockPos.MutableBlockPos mutablePos, RandomSource random,
                               MeteoriteType type, int shell, int radius) {
        Block[] blocks = type.meteorites();
        int[] weights = type.weights();
        int model = type.model();
        double progress = (double) shell / radius;
        BlockState shellState = null;
        int layerStart = 0;
        int layerCount = 0;
        int layerWeight = 0;

        if (model == 1) {
            shellState = getFixedBlock(blocks, weights, type.totalWeight(), progress);
        } else if (model == 2) {
            shellState = getRandomBlock(blocks, weights, 0, blocks.length, type.totalWeight(), random);
        } else if (model == 3 || model == 4) {
            // Ars Meteorites stores block counts, radial weights, per-layer block weights, then both totals.
            int layerIndex = getLayerIndex(type.layer(), progress);
            if (layerIndex >= 0) {
                int[] layers = type.layer();
                int totalLayers = layers[layers.length - 2];
                layerCount = layers[layerIndex];
                for (int i = 0; i < layerIndex; i++) {
                    int count = layers[i];
                    if (count < 0 || count > blocks.length - layerStart) {
                        layerStart = -1;
                        break;
                    }
                    layerStart += count;
                }
                if (layerStart >= 0 && layerCount > 0 && layerCount <= blocks.length - layerStart) {
                    layerWeight = layers[totalLayers * 2 + layerIndex];
                    if (model == 4) {
                        shellState = getRandomBlock(blocks, weights, layerStart, layerCount, layerWeight, random);
                    }
                } else {
                    layerStart = -1;
                }
            } else {
                layerStart = -1;
            }
        }

        int outerSquared = shell * shell;
        int innerRadius = shell - 1;
        int innerSquared = innerRadius * innerRadius;
        boolean placed = false;
        for (int x = -shell; x <= shell; x++) {
            int xSquared = x * x;
            if (xSquared > outerSquared) continue;
            for (int y = -shell; y <= shell; y++) {
                int xySquared = xSquared + y * y;
                if (xySquared > outerSquared) continue;
                for (int z = -shell; z <= shell; z++) {
                    int distanceSquared = xySquared + z * z;
                    if (distanceSquared <= innerSquared || distanceSquared > outerSquared) continue;

                    mutablePos.setWithOffset(origin, x, y, z);
                    if (!level.isEmptyBlock(mutablePos)) continue;

                    BlockState state = shellState;
                    if (state == null) {
                        if (model == 3 && layerStart >= 0) {
                            state = getRandomBlock(blocks, weights, layerStart, layerCount, layerWeight, random);
                        } else {
                            state = getRandomBlock(blocks, weights, 0, blocks.length, type.totalWeight(), random);
                        }
                    }
                    setBlock(level, mutablePos, state);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private boolean placeIfEmpty(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.isEmptyBlock(pos)) return false;
        setBlock(level, pos, state);
        return true;
    }

    private static MeteoriteType getRandomType(RandomSource random) {
        Collection<MeteoriteType> types = RecipeRegistry.getAllTypes();
        if (types.isEmpty()) return null;

        int selected = random.nextInt(types.size());
        Iterator<MeteoriteType> iterator = types.iterator();
        while (selected-- > 0) iterator.next();
        return iterator.next();
    }

    private static BlockState getRandomBlock(Block[] blocks, int[] weights, int start, int count, int totalWeight, RandomSource random) {
        if (start < 0 || count <= 0 || start > blocks.length - count || start > weights.length - count || totalWeight <= 0) {
            return blocks[0].defaultBlockState();
        }

        int selected = random.nextInt(totalWeight);
        int accumulated = 0;
        int end = start + count;
        for (int i = start; i < end; i++) {
            accumulated += weights[i];
            if (selected < accumulated) return blocks[i].defaultBlockState();
        }
        return blocks[start].defaultBlockState();
    }

    private static BlockState getFixedBlock(Block[] blocks, int[] weights, int totalWeight, double progress) {
        if (weights.length < blocks.length || totalWeight <= 0) return blocks[0].defaultBlockState();

        int selected = (int) (totalWeight * progress);
        int accumulated = 0;
        for (int i = 0; i < blocks.length; i++) {
            accumulated += weights[i];
            if (selected < accumulated) return blocks[i].defaultBlockState();
        }
        return blocks[blocks.length - 1].defaultBlockState();
    }

    private static int getLayerIndex(int[] layers, double progress) {
        if (layers.length < 5) return -1;

        int totalLayers = layers[layers.length - 2];
        if (totalLayers <= 0 || totalLayers > (layers.length - 2) / 3) return -1;

        int distributionTotal = layers[layers.length - 1];
        if (distributionTotal <= 0) return -1;

        int selected = (int) (distributionTotal * progress);
        int accumulated = 0;
        for (int i = 0; i < totalLayers; i++) {
            accumulated += layers[totalLayers + i];
            if (selected <= accumulated) return i;
        }
        return totalLayers - 1;
    }

    public record Configuration(IntProvider radius) implements FeatureConfiguration {

        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                IntProvider.codec(1, MAX_RADIUS).fieldOf("radius").forGetter(Configuration::radius))
                .apply(instance, Configuration::new));
    }
}

package com.gtocore.common.worldgen.feature;

import com.gtocore.common.data.GTOBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public final class SolarSurfaceBlobFeature extends Feature<BlockStateConfiguration> {

    public SolarSurfaceBlobFeature() {
        super(BlockStateConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        // WORLD_SURFACE_WG includes the fluid surface, unlike OCEAN_FLOOR_WG.
        if (!level.getBlockState(origin.below()).is(GTOBlocks.BLAZING_PYROTHEUM.get())) return false;

        RandomSource random = context.random();
        BlockPos.MutableBlockPos center = origin.mutable();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // Match the three overlapping lobes of vanilla mossy cobblestone boulders.
        for (int lobe = 0; lobe < 3; lobe++) {
            int radiusX = random.nextInt(2);
            int radiusY = random.nextInt(2);
            int radiusZ = random.nextInt(2);
            float radius = (radiusX + radiusY + radiusZ) * 0.333F + 0.5F;
            for (int x = -radiusX; x <= radiusX; x++) {
                for (int y = -radiusY; y <= radiusY; y++) {
                    for (int z = -radiusZ; z <= radiusZ; z++) {
                        if (x * x + y * y + z * z > radius * radius) continue;
                        pos.setWithOffset(center, x, y - 7, z);
                        if (level.isOutsideBuildHeight(pos)) continue;
                        setBlock(level, pos, context.config().state);
                    }
                }
            }
            center.move(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
        }
        return true;
    }
}

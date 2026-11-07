package vazkii.botania.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

public class ShatteredTrunkPlacer extends TrunkPlacer {

    public static final Codec<ShatteredTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 32).fieldOf("base_height").forGetter(placer -> placer.baseHeight),
            Codec.intRange(0, 24).fieldOf("height_rand_a").forGetter(placer -> placer.heightRandA),
            Codec.intRange(0, 24).fieldOf("height_rand_b").forGetter(placer -> placer.heightRandB))
            .apply(instance, ShatteredTrunkPlacer::new));

    public ShatteredTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Nonnull
    @Override
    protected TrunkPlacerType<?> type() {
        return MythicBotanyWorldgen.SHATTERED_TRUNK;
    }

    @Nonnull
    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(@Nonnull LevelSimulatedReader level, @Nonnull BiConsumer<BlockPos, BlockState> blockSetter, @Nonnull RandomSource random, int freeTreeHeight, BlockPos pos, @Nonnull TreeConfiguration config) {
        List<FoliagePlacer.FoliageAttachment> list = new ArrayList<>();
        setDirtAt(level, blockSetter, random, pos.below(), config);
        for (Direction dir : Direction.values()) {
            if (dir.getAxis() != Direction.Axis.Y && random.nextInt(3) == 0) {
                BlockPos.MutableBlockPos mpos = pos.relative(Direction.UP).relative(dir).mutable();
                for (int i = 0; i < 3; i++) {
                    mpos.move(0, -1, 0);
                    if (!this.placeLog(level, blockSetter, mpos, random, config)) {
                        break;
                    }
                }
                if (level.isStateAtPosition(pos, state -> state.canOcclude())) {
                    setDirtAt(level, blockSetter, random, pos.below(), config);
                }
            }
        }
        int straightTrunkSize = Math.max(3, freeTreeHeight - 3);
        for (int i = 0; i < straightTrunkSize; i++) {
            this.placeLog(level, blockSetter, pos.above(i), random, config);
        }
        int shatters = 3 + random.nextInt(3);
        shatterLoop:
        for (int i = 0; i < shatters; i++) {
            int xd = random.nextInt(3);
            int zd = random.nextInt(3);
            int xs = random.nextBoolean() ? 1 : -1;
            int zs = random.nextBoolean() ? 1 : -1;
            BlockPos.MutableBlockPos mpos = pos.above(straightTrunkSize - 1).mutable();
            int shatterSize = 3 + random.nextInt(4);
            for (int j = 0; j < shatterSize; j++) {
                int xm = xd == 0 ? 0 : (xd / 2) + random.nextInt((xd + 1) / 2);
                int zm = zd == 0 ? 0 : (zd / 2) + random.nextInt((zd + 1) / 2);
                int x = 0;
                int z = 0;
                while (x < xm || z < zm) {
                    mpos.move(x < xm ? xs : 0, random.nextInt(3) != 0 ? 1 : 0, z < zm ? zs : 0);
                    x = Math.min(xm, x + 1);
                    z = Math.min(zm, z + 1);
                    if (this.placeLog(level, blockSetter, mpos, random, config)) {
                        list.add(new FoliagePlacer.FoliageAttachment(mpos.immutable(), 0, false));
                    } else {
                        continue shatterLoop;
                    }
                }
            }
        }
        return list;
    }

    private boolean placeLog(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, BlockPos pos, RandomSource rand, TreeConfiguration config) {
        if (level.isStateAtPosition(pos, state -> state.isAir() || state.is(AlfheimWorldGen.ALFHEIM_LEAVES) || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(Blocks.WATER))) {
            blockSetter.accept(pos, config.trunkProvider.getState(rand, pos));
            return true;
        } else {
            return false;
        }
    }
}

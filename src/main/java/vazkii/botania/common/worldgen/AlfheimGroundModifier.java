package vazkii.botania.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class AlfheimGroundModifier extends PlacementModifier {

    public static final AlfheimGroundModifier INSTANCE = new AlfheimGroundModifier();
    public static final Codec<AlfheimGroundModifier> CODEC = Codec.unit(INSTANCE);

    private AlfheimGroundModifier() {}

    @Nonnull
    @Override
    public PlacementModifierType<?> type() {
        return MythicBotanyWorldgen.ALFHEIM_GROUND;
    }

    @Nonnull
    @Override
    public Stream<BlockPos> getPositions(@Nonnull PlacementContext context, @Nonnull RandomSource random, @Nonnull BlockPos pos) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(pos.getX(), context.getMinBuildHeight() + context.getGenDepth(), pos.getZ());
        while (mpos.getY() >= context.getMinBuildHeight()) {
            if (!AlfheimWorldGen.passthrough(context.getLevel().getBlockState(mpos))) {
                return Stream.of(mpos.immutable().relative(Direction.UP));
            }
            mpos.move(Direction.DOWN);
        }
        return Stream.of();
    }
}

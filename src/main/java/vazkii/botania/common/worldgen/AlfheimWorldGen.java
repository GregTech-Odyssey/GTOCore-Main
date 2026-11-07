package vazkii.botania.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import vazkii.botania.common.BotaniaBlocks;
import vazkii.botania.common.MythicBotanyBlocks;

public final class AlfheimWorldGen {

    public static final TagKey<net.minecraft.world.level.block.Block> BASE_STONE_ALFHEIM = TagKey.create(Registries.BLOCK, new ResourceLocation("mythicbotany", "base_stone_alfheim"));
    public static final TagKey<net.minecraft.world.level.block.Block> ALFHEIM_LEAVES = TagKey.create(Registries.BLOCK, new ResourceLocation("mythicbotany", "alfheim_leaves"));
    public static final RuleTest livingrock = new BlockMatchTest(BotaniaBlocks.LIVINGROCK.get());
    public static final RuleTest alfheimStone = new TagMatchTest(BASE_STONE_ALFHEIM);

    private AlfheimWorldGen() {}

    public static BlockPos highestFreeBlock(LevelAccessor level, HorizontalPos hor) {
        return hor.trace(level.getMaxBuildHeight(), level.getMinBuildHeight(), p -> !level.isEmptyBlock(p) && !passthrough(level.getBlockState(p)));
    }

    public static boolean passthrough(BlockState state) {
        return (state.canBeReplaced() && state.getBlock() != Blocks.WATER && state.getBlock() != Blocks.LAVA && !(state.getBlock() instanceof LiquidBlock)) ||
                state.is(BlockTags.LEAVES) || state.getBlock() == MythicBotanyBlocks.getBlock("dreamwood_leaves") || state.getBlock() == BotaniaBlocks.LIVINGROCK.get();
    }
}

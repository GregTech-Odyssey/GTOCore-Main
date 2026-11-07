package vazkii.botania.common.rune;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.SpecialRuneOutput;
import vazkii.botania.common.MythicBotanyBlocks;

import java.util.List;

/**
 * 妙尔尼尔的特殊产出：仪式完成时把妙尔尼尔掉落在仪式中心。
 */
public class MjoellnirRuneOutput extends SpecialRuneOutput {

    public static final MjoellnirRuneOutput INSTANCE = new MjoellnirRuneOutput();

    private MjoellnirRuneOutput() {
        super(new ResourceLocation("mythicbotany", "mjoellnir"));
    }

    @Override
    public void apply(Level level, BlockPos center, List<ItemStack> consumedStacks) {
        net.minecraft.world.level.block.Block.popResource(level, center.above(), new ItemStack(MythicBotanyBlocks.MB_MJOELLNIR.get()));
    }

    @Override
    public List<ItemStack> getJeiOutputItems() {
        return List.of(new ItemStack(MythicBotanyBlocks.MB_MJOELLNIR.get()));
    }
}

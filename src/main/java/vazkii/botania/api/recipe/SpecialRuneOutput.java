package vazkii.botania.api.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * 符文仪式的特殊产出：完成仪式时执行，例如把妙尔尼尔放进世界。
 */
public abstract class SpecialRuneOutput {

    public final ResourceLocation id;

    protected SpecialRuneOutput(ResourceLocation id) {
        this.id = id;
    }

    public abstract void apply(Level level, BlockPos center, List<ItemStack> consumedStacks);

    public List<ItemStack> getJeiOutputItems() {
        return ImmutableList.of();
    }
}

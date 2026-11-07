package vazkii.botania.api.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;

import java.util.List;

/**
 * 符文仪式的特殊投入：在常规物品之外对仪式现场做额外检查与处理，例如献祭生物。
 *
 * <p>
 * {@link #apply} 返回左侧消息表示条件不满足、仪式不启动；返回右侧数据表示已应用，取消仪式时用它还原现场。
 * </p>
 */
public abstract class SpecialRuneInput {

    public final ResourceLocation id;

    protected SpecialRuneInput(ResourceLocation id) {
        this.id = id;
    }

    public abstract Either<MutableComponent, CompoundTag> apply(Level level, BlockPos pos, RuneRitualRecipe recipe);

    public abstract void cancel(Level level, BlockPos pos, RuneRitualRecipe recipe, CompoundTag nbt);

    public List<Ingredient> getJeiInputItems() {
        return ImmutableList.of();
    }
}

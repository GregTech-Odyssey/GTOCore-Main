package vazkii.botania.common.block.block_entity.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.item.ManaDissolvable;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.recipe.ManaInfusionRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有魔力池共用的物品注入。
 *
 * <p>
 * 每 tick 每个池按 {@link ManaPoolSpec#infusionRate()} 注入手上物品；配方按输入物品与池下方的催化剂匹配。
 * 配方表按 {@code RecipeManager} 实例缓存，重建一次后复用。
 * </p>
 */
public final class ManaInfusion {

    private static final String TAG_INFUSION_SPAWNED = "botaniaManaInfusionSpawned";

    private static volatile Object cachedManager;
    private static volatile List<ManaInfusionRecipe> cachedRecipes = List.of();

    private ManaInfusion() {}

    /**
     * 处理一次注入。
     *
     * @return 实际注入的物品数量
     */
    public static int process(Level level, ManaPool pool, ItemEntity item, BlockState catalyst, int rate, BlockPos outputPos) {
        if (level.isClientSide || rate <= 0 || !item.isAlive()) {
            return 0;
        }
        ItemStack stack = item.getItem();
        if (stack.getItem() instanceof ManaDissolvable dissolvable) {
            for (int i = 0; i < rate && item.isAlive() && !item.getItem().isEmpty(); i++) {
                dissolvable.onDissolveTick(pool, item);
            }
            stack = item.getItem();
        }
        if (!item.isAlive() || stack.isEmpty() || item.getPersistentData().getBoolean(TAG_INFUSION_SPAWNED)) {
            return 0;
        }
        ManaInfusionRecipe recipe = find(level, stack, catalyst);
        if (recipe == null) {
            return 0;
        }
        long cost = Math.max(0, recipe.getManaToConsume());
        int count = Math.min(rate, stack.getCount());
        if (cost > 0) {
            count = (int) Math.min(count, pool.getCurrentManaLong() / cost);
        }
        if (count <= 0) {
            return 0;
        }
        if (cost > 0) {
            long taken = pool.removeMana(cost * count, false);
            count = (int) (taken / cost);
            long leftover = taken - cost * count;
            if (leftover > 0) {
                pool.addMana(leftover, false);
            }
            if (count <= 0) {
                return 0;
            }
        }
        ItemStack output = recipe.getRecipeOutput(level.registryAccess(), stack);
        stack.shrink(count);
        item.setItem(stack);
        int total = output.getCount() * count;
        boolean first = true;
        while (total > 0) {
            ItemStack part = output.copyWithCount(Math.min(total, output.getMaxStackSize()));
            total -= part.getCount();
            if (first && item.getOwner() instanceof Player player) {
                part.onCraftedBy(level, player, output.getCount() * count);
            }
            first = false;
            ItemEntity outputItem = new ItemEntity(level, outputPos.getX() + 0.5, outputPos.getY() + 1.5,
                    outputPos.getZ() + 0.5, part);
            outputItem.getPersistentData().putBoolean(TAG_INFUSION_SPAWNED, true);
            level.addFreshEntity(outputItem);
        }
        return count;
    }

    /** 按输入物品与催化剂查找配方，催化剂配方优先于无催化剂配方。 */
    @Nullable
    public static ManaInfusionRecipe find(Level level, @NotNull ItemStack stack, @NotNull BlockState catalyst) {
        ManaInfusionRecipe plain = null;
        for (ManaInfusionRecipe recipe : recipes(level)) {
            if (!recipe.matches(stack)) {
                continue;
            }
            var recipeCatalyst = recipe.getRecipeCatalyst();
            if (recipeCatalyst == null) {
                if (plain == null) {
                    plain = recipe;
                }
            } else if (recipeCatalyst.test(catalyst)) {
                return recipe;
            }
        }
        return plain;
    }

    private static List<ManaInfusionRecipe> recipes(Level level) {
        Object manager = level.getRecipeManager();
        if (manager != cachedManager) {
            List<ManaInfusionRecipe> list = new ArrayList<>();
            for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
                if (recipe instanceof ManaInfusionRecipe infusion) {
                    list.add(infusion);
                }
            }
            cachedRecipes = list;
            cachedManager = manager;
        }
        return cachedRecipes;
    }
}

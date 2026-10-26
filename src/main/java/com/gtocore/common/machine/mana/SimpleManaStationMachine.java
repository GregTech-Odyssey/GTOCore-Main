package com.gtocore.common.machine.mana;

import com.gtolib.api.recipe.GTORecipeModifiers;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// 只吃魔力的单方块站点机器：不接电力，带 EU 的配方（输入或输出）一律不可运行；MANAt 配方按档位魔力超频。
public class SimpleManaStationMachine extends SimpleManaMachine {

    public SimpleManaStationMachine(MetaMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
    }

    @Nullable
    @Override
    public GTRecipe doModifyRecipe(RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (recipe.eut != 0) return null;
        return GTORecipeModifiers.manaOverclocking(this, unit, recipe, getTierMana());
    }
}

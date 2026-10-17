package com.gtocore.integration.emi;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe;
import com.gregtechceu.gtceu.integration.emi.recipe.GTRecipeEMICategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import org.jetbrains.annotations.Nullable;

/** 通过正常的 tooltip 管线绘制 EMI 配方，避免预览被材料网格的滚动区裁剪。 */
@OnlyIn(Dist.CLIENT)
public record WildcardRecipeTooltip(RecipeTooltipComponent component, Component categoryName) implements TooltipComponent {

    public static void register(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WildcardRecipeTooltip.class, WildcardRecipeTooltip::component);
    }

    @Nullable
    public static WildcardRecipeTooltip of(ResourceLocation id) {
        var recipe = EmiApi.getRecipeManager().getRecipe(id);
        if (recipe == null) {
            var definition = GTRecipeDefinition.RECIPES.get(id);
            if (definition == null) return null;
            recipe = new GTEmiRecipe(definition, GTRecipeEMICategory.CATEGORIES.apply(definition.recipeCategory));
        }
        return new WildcardRecipeTooltip(new RecipeTooltipComponent(recipe), recipe.getCategory().getName());
    }
}

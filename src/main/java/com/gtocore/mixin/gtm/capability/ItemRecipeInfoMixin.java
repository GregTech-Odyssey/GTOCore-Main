package com.gtocore.mixin.gtm.capability;

import com.gtocore.api.data.tag.GTOTagPrefix;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 配方页物品槽的 GTO 补充（研究所需的数据/科技节点改由 {@code ResearchRecipeCondition} 以展示槽显示）：
 * <ul>
 * <li>GTO 催化剂（{@code GTOTagPrefix.CATALYST}）输入按催化剂显示，不标消耗概率；</li>
 * <li>悬停提示末尾补一行 AE 风格的数量。</li>
 * </ul>
 */
@Mixin(ItemRecipeInfo.class)
public abstract class ItemRecipeInfoMixin {

    @Inject(method = "applyWidgetInfo", at = @At("TAIL"), remap = false)
    private void gto$applyContentInfo(Widget widget, int index, boolean isXEI, IO io, GTRecipeTypeUI.RecipeHolder recipeHolder,
                                      GTRecipeType recipeType, GTRecipeDefinition recipe, Content<ItemIngredient> content,
                                      Object storage, int recipeTier, int chanceTier, CallbackInfo ci) {
        if (!(widget instanceof SlotWidget slot) || content == null) return;
        if (io == IO.IN && content.inner.getInnerItemStack().getItem() instanceof TagPrefixItem item && item.tagPrefix == GTOTagPrefix.CATALYST) {
            slot.setIngredientIO(IngredientIO.CATALYST);
            slot.setXEIChance(0);
        }
        slot.setOnAddedTooltips((w, tooltips) -> {
            GTRecipeWidget.setConsumedChance(content, ChanceLogic.OR, tooltips, recipeTier, chanceTier, recipe.chanceFunction);
            tooltips.add(Component.translatable("gui.tooltips.ae2.Amount", content.amount).withStyle(ChatFormatting.GRAY));
        });
    }
}

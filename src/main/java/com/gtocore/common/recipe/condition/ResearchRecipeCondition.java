package com.gtocore.common.recipe.condition;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.integration.emi.research.EmiResearchHelper;
import com.gtocore.integration.emi.research.TechNodeEmiStack;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;
import com.gregtechceu.gtceu.integration.xei.handlers.item.CycleItemStackHandler;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uiwidgets.recipe.RecipeDisplaySlots;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import lombok.Getter;

import java.util.List;

@Getter
public class ResearchRecipeCondition extends RecipeCondition {

    private final TechNode techNode;
    private final ItemStack dataStack;
    /// 为true表示需要研究节点本身解锁才能使用该配方，为false表示只要含有研究节点配方的dataAccessHatch就能使用该配方
    private final boolean requiresNode;

    public ResearchRecipeCondition(TechNode techNode, ResourceLocation recipeId, GTRecipeType recipeType) {
        this(techNode, recipeId, recipeType, false);
    }

    public ResearchRecipeCondition(TechNode techNode, ResourceLocation recipeId, GTRecipeType recipeType, boolean requiresNode) {
        this.techNode = techNode;
        this.requiresNode = requiresNode;
        dataStack = techNode.getTierItem();
        ResearchManager.writeResearchToNBT(dataStack.getOrCreateTag(), recipeId.toString(), recipeType);
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtocore.recipe.require_technode", techNode.getDisplayName());
    }

    /**
     * 配方页：一句"需要研究某节点"，并附一个催化剂展示槽——需要节点本身解锁时显示科技节点（可在 EMI 里查看），
     * 否则显示该节点等级的数据物品。
     */
    @Override
    public void appendInfo(GTRecipeDefinition recipe, RecipeInfoBuilder info) {
        info.link(this::getTooltips, () -> EmiResearchHelper.openTechNode(techNode));
        info.slot(this::createResearchSlot);
    }

    private Widget createResearchSlot() {
        if (requiresNode && techNode.icon != null) {
            var slot = new ItemSlot(new CycleItemStackHandler(List.of(List.of(techNode.icon.wrapForDisplayOrFilter()))), 0, false, false) {

                @Override
                public List<Object> getXEIIngredients() {
                    return List.of(new TechNodeEmiStack(techNode));
                }
            };
            slot.setIngredientIO(IngredientIO.CATALYST);
            return slot;
        }
        return RecipeDisplaySlots.item(dataStack, IngredientIO.CATALYST);
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (requiresNode) {
            var owner = holder.self().getOwner();
            return owner != null && TechTreeSavedData.isUnlocked(owner.getUUID(), techNode);
        }
        if (holder instanceof IDataAccessHatch dataAccessHatch && dataAccessHatch.isRecipeAvailable(recipe)) {
            return true;
        } else if (holder instanceof IMultiController controller) {
            for (var p : controller.getParts()) {
                if (p instanceof IDataAccessHatch dataAccessHatch && dataAccessHatch.isRecipeAvailable(recipe)) {
                    return true;
                }
            }
        }
        return false;
    }
}

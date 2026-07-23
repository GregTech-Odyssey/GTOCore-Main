package com.gtocore.common.recipe.condition;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeSavedData;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

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
        return Component.translatable("gtocore.recipe.require_technode");
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

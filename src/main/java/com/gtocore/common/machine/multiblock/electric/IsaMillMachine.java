package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.BallHatchPartMachine;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.utils.MathUtil;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class IsaMillMachine extends ElectricMultiblockMachine implements IIssueProvider {

    private BallHatchPartMachine ballHatchPartMachine;

    public IsaMillMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onPartScan(IMultiPart part) {
        super.onPartScan(part);
        if (ballHatchPartMachine == null && part instanceof BallHatchPartMachine ballHatchPart) {
            ballHatchPartMachine = ballHatchPart;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        ballHatchPartMachine = null;
    }

    @Override
    @Nullable
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        recipe = super.getRealRecipe(unit, recipe);
        if (recipe == null) return null;
        CustomItemStackHandler storage = ballHatchPartMachine.getInventory().storage;
        ItemStack item = storage.getStackInSlot(0);
        int tier = BallHatchPartMachine.GRINDBALL.getOrDefault(item.getItem(), 0);
        int need = recipe.data.getInt(GTORecipeDataKeys.GRINDBALL);
        if (tier == need) {
            var level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, item) + 1;
            int damage = item.getDamageValue() + MathUtil.saturatedCast(recipe.parallels / level) + 1;
            if (damage < item.getMaxDamage()) {
                item.setDamageValue(damage);
            } else {
                storage.setStackInSlot(0, ItemStack.EMPTY);
            }
            return recipe;
        }
        IdleReason.GRIND_BALL.report(this, IssueStage.MODIFIER, need, tier, recipe.definition);
        return null;
    }

    @Override
    public void collectIssues(IssueSink sink) {
        var hatch = ballHatchPartMachine;
        if (hatch != null && hatch.getInventory().storage.getStackInSlot(0).isEmpty()) IdleReason.GRIND_BALL.collect(sink);
    }
}

package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantmentRecipe;
import dev.emi.emi.api.stack.EmiStack;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class EnchantmentEmiRecipe extends ApparatusEmiRecipe {

    public EnchantmentEmiRecipe(EnchantmentRecipe recipe) {
        super(ArsNouveauEmi.ENCHANTMENT, recipe.getId(), recipe,
                recipe.enchantLevel > 1 ? EmiStack.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(recipe.enchantment, recipe.enchantLevel - 1))) : EmiStack.of(Items.BOOK),
                EmiStack.of(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(recipe.enchantment, recipe.enchantLevel))));
    }
}

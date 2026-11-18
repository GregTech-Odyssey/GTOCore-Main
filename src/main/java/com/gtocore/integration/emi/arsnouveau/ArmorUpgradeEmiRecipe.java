package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.hollingsworth.arsnouveau.api.enchanting_apparatus.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.api.perk.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.api.perk.IPerkProvider;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class ArmorUpgradeEmiRecipe extends ApparatusEmiRecipe {

    private final Component tierText;

    private ArmorUpgradeEmiRecipe(ArmorUpgradeRecipe recipe, ResourceLocation id, ItemStack input, ItemStack output) {
        super(ArsNouveauEmi.ARMOR_UPGRADE, id, recipe, EmiStack.of(input), EmiStack.of(output));
        tierText = Component.translatable("gtocore.tier.value", 1 + recipe.tier);
    }

    public static ArmorUpgradeEmiRecipe of(ArmorUpgradeRecipe recipe, Item armor) {
        ItemStack input = armor.getDefaultInstance();
        ItemStack output = input.copy();
        IPerkProvider<ItemStack> provider = PerkRegistry.getPerkProvider(armor);
        if (provider != null) {
            if (provider.getPerkHolder(input) instanceof ArmorPerkHolder holder) holder.setTier(recipe.tier - 1);
            if (provider.getPerkHolder(output) instanceof ArmorPerkHolder holder) holder.setTier(recipe.tier);
        }
        ResourceLocation base = recipe.getId();
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(armor);
        ResourceLocation id = new ResourceLocation(base.getNamespace(), "/" + base.getPath() + "/" + key.getNamespace() + "/" + key.getPath());
        return new ArmorUpgradeEmiRecipe(recipe, id, input, output);
    }

    @Override
    protected void addText(WidgetHolder widgets) {
        addLine(widgets, tierText, 0);
        super.addText(widgets);
    }
}

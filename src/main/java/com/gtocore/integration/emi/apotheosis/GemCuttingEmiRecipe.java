package com.gtocore.integration.emi.apotheosis;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.cutting.GemCuttingMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// 改写自 Apotheosis（Shadows_of_Fire，MIT）的 JEI 集成
// https://github.com/Shadows-of-Fire/Apotheosis
final class GemCuttingEmiRecipe extends BasicEmiRecipe {

    private static final ResourceLocation TEX = new ResourceLocation(ApotheosisEmi.MODID, "textures/gui/gem_cutting_jei.png");

    private final EmiStack gem;
    private final EmiIngredient materials;
    private final EmiStack dust;

    GemCuttingEmiRecipe(EmiRecipeCategory category, Gem g, LootRarity rarity, LootRarity min) {
        super(category, id(g, rarity), 148, 78);
        ItemStack gemStack = GemRegistry.createGemStack(g, rarity);
        this.gem = EmiStack.of(gemStack);
        this.dust = EmiStack.of(new ItemStack(Adventure.Items.GEM_DUST.get(), GemCuttingMenu.getDustCost(rarity)));
        List<EmiStack> mats = new ArrayList<>(3);
        if (rarity == min) {
            mats.add(EmiStack.of(new ItemStack(rarity.getMaterial(), GemCuttingMenu.STD_MAT_COST)));
            mats.add(EmiStack.of(new ItemStack(rarity.next().getMaterial(), GemCuttingMenu.NEXT_MAT_COST)));
        } else if ("ancient".equals(RarityRegistry.INSTANCE.getKey(rarity.next()).getPath())) {
            mats.add(EmiStack.of(new ItemStack(rarity.prev().getMaterial(), GemCuttingMenu.PREV_MAT_COST)));
            mats.add(EmiStack.of(new ItemStack(rarity.getMaterial(), GemCuttingMenu.STD_MAT_COST)));
        } else {
            mats.add(EmiStack.of(new ItemStack(rarity.prev().getMaterial(), GemCuttingMenu.PREV_MAT_COST)));
            mats.add(EmiStack.of(new ItemStack(rarity.getMaterial(), GemCuttingMenu.STD_MAT_COST)));
            mats.add(EmiStack.of(new ItemStack(rarity.next().getMaterial(), GemCuttingMenu.NEXT_MAT_COST)));
        }
        this.materials = EmiIngredient.of(mats);
        this.inputs = Arrays.asList(EmiStack.of(gemStack, 2), materials, dust);
        this.outputs = Collections.singletonList(EmiStack.of(GemRegistry.createGemStack(g, rarity.next())));
    }

    private static ResourceLocation id(Gem g, LootRarity rarity) {
        ResourceLocation gid = g.getId();
        ResourceLocation rid = RarityRegistry.INSTANCE.getKey(rarity);
        return new ResourceLocation(ApotheosisEmi.MODID, "/gem_cutting/" + gid.getNamespace() + "/" + gid.getPath() + "/" + rid.getNamespace() + "/" + rid.getPath());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEX, 0, 0, 148, 78, 0, 0);
        widgets.addSlot(gem, 47, 36).drawBack(false);
        widgets.addSlot(materials, 47, 3).drawBack(false);
        widgets.addSlot(gem, 18, 55).drawBack(false);
        widgets.addSlot(dust, 75, 55).drawBack(false);
        widgets.addSlot(outputs.get(0), 116, 34).drawBack(false).recipeContext(this);
    }
}

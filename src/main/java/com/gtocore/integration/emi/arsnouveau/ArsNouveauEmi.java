package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantmentRecipe;
import com.hollingsworth.arsnouveau.api.recipe.BuddingConversionRecipe;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.client.container.IAutoFillTerminal;
import com.hollingsworth.arsnouveau.common.armor.AnimatedMagicArmor;
import com.hollingsworth.arsnouveau.common.crafting.recipes.CrushRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.DyeRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectCrush;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.hollingsworth.arsnouveau.setup.registry.MenuRegistry;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Comparator;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class ArsNouveauEmi implements EmiPlugin {

    static final Category GLYPH = new Category("glyph_recipe", EmiStack.of(BlockRegistry.SCRIBES_BLOCK), "block.ars_nouveau.scribes_table");
    static final Category CRUSH = new Category("crush", EmiStack.of(EffectCrush.INSTANCE.glyphItem), "ars_nouveau.crush_recipe");
    static final Category IMBUEMENT = new Category("imbuement", EmiStack.of(BlockRegistry.IMBUEMENT_BLOCK), "block.ars_nouveau.imbuement_chamber");
    static final Category APPARATUS = new Category("enchanting_apparatus", EmiStack.of(BlockRegistry.ENCHANTING_APP_BLOCK), "ars_nouveau.enchanting_apparatus");
    static final Category ENCHANTMENT = new Category("enchantment_apparatus", EmiStack.of(BlockRegistry.ENCHANTING_APP_BLOCK), "ars_nouveau.enchanting");
    static final Category ARMOR_UPGRADE = new Category("armor_upgrade", EmiStack.of(BlockRegistry.ENCHANTING_APP_BLOCK), "ars_nouveau.armor_upgrade");
    static final Category BUDDING_CONVERSION = new Category("budding_conversion", EmiStack.of(ItemsRegistry.AMETHYST_GOLEM_CHARM), "ars_nouveau.budding_conversion_recipe");

    static {
        IAutoFillTerminal.updateSearch.add(new IAutoFillTerminal.ISearchHandler() {

            @Override
            public void setSearch(String text) {
                EmiApi.setSearchText(text);
            }

            @Override
            public String getName() {
                return "EMI";
            }

            @Override
            public String getSearch() {
                return EmiApi.getSearchText();
            }
        });
    }

    @Override
    public void register(EmiRegistry registry) {
        Category[] categories = { GLYPH, CRUSH, IMBUEMENT, APPARATUS, ENCHANTMENT, ARMOR_UPGRADE, BUDDING_CONVERSION };
        for (Category category : categories) {
            registry.addCategory(category);
            registry.addWorkstation(category, category.workstation);
        }
        registry.addRecipeHandler(MenuRegistry.STORAGE.get(), new StorageLecternEmiHandler());

        ObjectArrayList<Item> armors = new ObjectArrayList<>();
        for (Item item : PerkRegistry.getPerkProviderItems()) {
            if (item instanceof AnimatedMagicArmor) armors.add(item);
        }
        armors.sort(Comparator.comparing(BuiltInRegistries.ITEM::getKey));

        RecipeManager manager = registry.getRecipeManager();
        RegistryAccess access = Minecraft.getInstance().level.registryAccess();
        OpenCacheHashSet<ResourceLocation> dyeIds = new OpenCacheHashSet<>();
        for (ImbuementRecipe recipe : manager.getAllRecipesFor(RecipeRegistry.IMBUEMENT_TYPE.get())) {
            registry.addRecipe(new ImbuementEmiRecipe(recipe));
        }
        for (Recipe<?> recipe : manager.getRecipes()) {
            if (recipe instanceof GlyphRecipe glyph) {
                registry.addRecipe(new GlyphEmiRecipe(glyph));
            } else if (recipe instanceof EnchantmentRecipe enchantment) {
                registry.addRecipe(new EnchantmentEmiRecipe(enchantment));
            } else if (recipe instanceof ArmorUpgradeRecipe upgrade) {
                for (Item armor : armors) {
                    if (((AnimatedMagicArmor) armor).getMinTier() < upgrade.tier) registry.addRecipe(ArmorUpgradeEmiRecipe.of(upgrade, armor));
                }
            } else if (recipe instanceof EnchantingApparatusRecipe apparatus) {
                if (!apparatus.excludeJei()) registry.addRecipe(new ApparatusEmiRecipe(apparatus));
            } else if (recipe instanceof CrushRecipe crush) {
                registry.addRecipe(new CrushEmiRecipe(crush));
            } else if (recipe instanceof BuddingConversionRecipe budding) {
                registry.addRecipe(new BuddingConversionEmiRecipe(budding));
            } else if (recipe instanceof DyeRecipe dye) {
                ItemStack result = dye.getResultItem(access);
                ArsDyeEmiRecipe emi = ArsDyeEmiRecipe.of(dye, result);
                if (emi != null) {
                    registry.addRecipe(emi);
                    dyeIds.add(dye.getId());
                }
            }
        }
        if (!dyeIds.isEmpty()) {
            registry.removeRecipes(r -> r.getCategory() == VanillaEmiRecipeCategories.CRAFTING && !(r instanceof ArsDyeEmiRecipe) && dyeIds.contains(r.getId()));
        }
    }

    static final class Category extends EmiRecipeCategory {

        private final String key;
        private final EmiStack workstation;

        private Category(String path, EmiStack icon, String key) {
            super(ArsNouveau.id(path), icon);
            this.key = key;
            this.workstation = icon;
        }

        @Override
        public Component getName() {
            return Component.translatable(key);
        }
    }
}

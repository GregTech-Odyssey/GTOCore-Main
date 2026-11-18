package com.gtocore.integration.emi.apotheosis;

import com.gtocore.integration.emi.HiddenItems;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import dev.shadowsoffire.apotheosis.adventure.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.ench.Ench;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingRecipe;
import dev.shadowsoffire.apotheosis.potion.PotionCharmItem;
import dev.shadowsoffire.apotheosis.potion.PotionCharmRecipe;
import dev.shadowsoffire.apotheosis.potion.PotionEnchantingRecipe;
import dev.shadowsoffire.apotheosis.village.fletching.FletchingContainer;
import dev.shadowsoffire.apotheosis.village.fletching.FletchingRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// 改写自 Apotheosis（Shadows_of_Fire，MIT）的 JEI 集成
// https://github.com/Shadows-of-Fire/Apotheosis
public final class ApotheosisEmi implements EmiPlugin {

    static final String MODID = Apotheosis.MODID;

    @Override
    public void register(EmiRegistry registry) {
        if (Apotheosis.enableEnch) registerEnchanting(registry);
        if (Apotheosis.enableAdventure) registerAdventure(registry);
        if (Apotheosis.enablePotion) registerPotion(registry);
        if (Apotheosis.enableVillage) registerVillage(registry);
    }

    static ResourceLocation synthetic(String type, ResourceLocation a) {
        return new ResourceLocation(MODID, "/" + type + "/" + a.getNamespace() + "/" + a.getPath());
    }

    private static void info(EmiRegistry registry, ItemStack stack, String key, String name) {
        registry.addRecipe(new EmiInfoRecipe(Collections.singletonList(EmiStack.of(stack)),
                Collections.singletonList(Component.translatable(key)), new ResourceLocation(MODID, "/info/" + name)));
    }

    private static void registerEnchanting(EmiRegistry registry) {
        Category category = new Category("enchanting", EmiStack.of(Items.ENCHANTING_TABLE), "apotheosis.recipes.enchanting");
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(Items.ENCHANTING_TABLE));

        List<EnchantingEmiRecipe> infusions = new ArrayList<>();
        List<EmiStack> charms = null;
        List<EmiStack> unbreakable = null;
        for (EnchantingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(Apoth.RecipeTypes.INFUSION)) {
            if (recipe instanceof PotionEnchantingRecipe && Apotheosis.enablePotion) {
                if (charms == null) {
                    charms = new ArrayList<>();
                    unbreakable = new ArrayList<>();
                    for (Potion p : ForgeRegistries.POTIONS) {
                        if (!PotionCharmItem.isValidPotion(p)) continue;
                        ItemStack charm = PotionUtils.setPotion(new ItemStack(Apoth.Items.POTION_CHARM.get()), p);
                        charms.add(EmiStack.of(charm));
                        ItemStack copy = charm.copy();
                        copy.getOrCreateTag().putBoolean("Unbreakable", true);
                        unbreakable.add(EmiStack.of(copy));
                    }
                }
                infusions.add(new EnchantingEmiRecipe(category, recipe, charms, unbreakable));
            } else {
                infusions.add(new EnchantingEmiRecipe(category, recipe, null, null));
            }
        }
        infusions.sort(Comparator.comparingDouble(r -> r.eterna));
        for (EnchantingEmiRecipe recipe : infusions) registry.addRecipe(recipe);

        info(registry, new ItemStack(Items.ENCHANTING_TABLE), "info.apotheosis.enchanting", "enchanting");
        info(registry, new ItemStack(Ench.Blocks.LIBRARY.get()), "info.apotheosis.library", "library");

        EmiRecipeCategory anvil = VanillaEmiRecipeCategories.ANVIL_REPAIRING;
        if (anvil == null) return;
        ItemStack enchSword = new ItemStack(Items.DIAMOND_SWORD);
        EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SHARPNESS, 1), enchSword);
        ItemStack cursedSword = new ItemStack(Items.DIAMOND_SWORD);
        EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.BINDING_CURSE, 1), cursedSword);
        ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SHARPNESS, 1), enchBook);
        anvil(registry, anvil, "cobweb", enchSword, new ItemStack(Items.COBWEB), new ItemStack(Items.DIAMOND_SWORD));
        if (Ench.Items.PRISMATIC_WEB.isPresent()) {
            anvil(registry, anvil, "prismatic_web", cursedSword, new ItemStack(Ench.Items.PRISMATIC_WEB.get()), new ItemStack(Items.DIAMOND_SWORD));
        }
        if (Ench.Items.SCRAP_TOME.isPresent()) {
            anvil(registry, anvil, "scrap_tome", enchSword, new ItemStack(Ench.Items.SCRAP_TOME.get()), enchBook);
        }
        anvil(registry, anvil, "anvil_repair", new ItemStack(Items.DAMAGED_ANVIL), new ItemStack(Items.IRON_BLOCK), new ItemStack(Items.ANVIL));
    }

    private static void anvil(EmiRegistry registry, EmiRecipeCategory category, String name, ItemStack left, ItemStack right, ItemStack out) {
        if (hidden(left.getItem()) || hidden(right.getItem()) || hidden(out.getItem())) return;
        registry.addRecipe(new ApothAnvilEmiRecipe(category, new ResourceLocation(MODID, "/anvil/" + name),
                EmiStack.of(left), EmiStack.of(right), EmiStack.of(out)));
    }

    private static boolean hidden(Item item) {
        return HiddenItems.isItemHidden(item);
    }

    private static String gemKey(ItemStack stack) {
        GemInstance inst = GemInstance.unsocketed(stack);
        if (!inst.isValidUnsocketed()) return String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem()));
        return inst.gem().getId() + "@" + inst.rarity().getId();
    }

    private static void registerAdventure(EmiRegistry registry) {
        registry.setDefaultComparison(Adventure.Items.GEM.get(), Comparison.compareData(s -> gemKey(s.getItemStack())));

        Category smithing = new Category("smithing", EmiStack.of(Items.SMITHING_TABLE), "title.apotheosis.smithing");
        Category salvaging = new Category("salvaging", EmiStack.of(Adventure.Blocks.SALVAGING_TABLE.get()), "title.apotheosis.salvaging");
        Category cutting = new Category("gem_cutting", EmiStack.of(Adventure.Blocks.GEM_CUTTING_TABLE.get()), "menu.apotheosis.gem_cutting");
        registry.addCategory(smithing);
        registry.addCategory(salvaging);
        registry.addCategory(cutting);
        registry.addWorkstation(smithing, EmiStack.of(Items.SMITHING_TABLE));
        registry.addWorkstation(salvaging, EmiStack.of(Adventure.Blocks.SALVAGING_TABLE.get()));
        registry.addWorkstation(cutting, EmiStack.of(Adventure.Blocks.GEM_CUTTING_TABLE.get()));

        boolean gemsReady = !GemRegistry.INSTANCE.getValues().isEmpty() && !RarityRegistry.INSTANCE.getOrderedRarities().isEmpty();

        if (gemsReady) {
            Gem gemObj = GemRegistry.INSTANCE.getRandomItem(new LegacyRandomSource(1854));
            if (gemObj != null) {
                ItemStack gem = new ItemStack(Adventure.Items.GEM.get());
                GemItem.setGem(gem, gemObj);
                AffixHelper.setRarity(gem, gemObj.getMaxRarity());
                info(registry, gem, "info.apotheosis.socketing", "socketing");
            }
        }
        info(registry, new ItemStack(Adventure.Items.GEM_DUST.get()), "info.apotheosis.gem_crushing", "gem_crushing");
        info(registry, new ItemStack(Adventure.Items.SIGIL_OF_UNNAMING.get()), "info.apotheosis.unnaming", "unnaming");

        List<ItemStack> dummies = new ArrayList<>(5);
        for (Item item : new Item[] { Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW }) {
            dummies.add(new ItemStack(item));
        }
        List<EmiStack> plain = new ArrayList<>(5);
        List<EmiStack> socketed = new ArrayList<>(5);
        for (ItemStack s : dummies) {
            plain.add(EmiStack.of(s));
            ItemStack c = s.copy();
            SocketHelper.setSockets(c, 1);
            socketed.add(EmiStack.of(c));
        }
        List<EmiStack> withGems = null;
        for (SmithingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING)) {
            if (recipe instanceof AddSocketsRecipe add) {
                registry.addRecipe(new ApothSmithingEmiRecipe(smithing, synthetic("smithing", add.getId()), plain, EmiIngredient.of(add.getInput()), socketed,
                        Component.translatable("text.apotheosis.socket_limit", add.getMaxSockets()), 125 / 2));
            } else if (recipe instanceof WithdrawalRecipe withdrawal) {
                if (withGems == null) withGems = withdrawalInputs(dummies, gemsReady);
                registry.addRecipe(new ApothSmithingEmiRecipe(smithing, synthetic("smithing", withdrawal.getId()), withGems,
                        EmiStack.of(Adventure.Items.SIGIL_OF_WITHDRAWAL.get()), socketed, Component.translatable("text.apotheosis.gems_returned"), (108 + 32) / 2));
            }
        }

        List<SalvagingEmiRecipe> salvage = new ArrayList<>();
        for (SalvagingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(Apoth.RecipeTypes.SALVAGING)) {
            salvage.add(new SalvagingEmiRecipe(salvaging, recipe));
        }
        salvage.sort(Comparator.comparingInt(r -> r.maxOut));
        for (SalvagingEmiRecipe recipe : salvage) registry.addRecipe(recipe);

        if (gemsReady) {
            LootRarity min = RarityRegistry.getMinRarity().get();
            LootRarity max = RarityRegistry.getMaxRarity().get();
            for (Gem g : GemRegistry.INSTANCE.getValues()) {
                LootRarity r = min;
                while (r != max) {
                    if (g.clamp(r) == r) registry.addRecipe(new GemCuttingEmiRecipe(cutting, g, r, min));
                    r = r.next();
                }
            }
        }
    }

    private static List<EmiStack> withdrawalInputs(List<ItemStack> dummies, boolean gemsReady) {
        List<EmiStack> list = new ArrayList<>(dummies.size());
        RandomSource rand = new LegacyRandomSource(1854);
        LootRarity max = gemsReady ? RarityRegistry.getMaxRarity().get() : null;
        for (ItemStack d : dummies) {
            ItemStack s = d.copy();
            SocketHelper.setSockets(s, 1);
            if (max != null) {
                Gem gem = GemRegistry.INSTANCE.getRandomItem(rand, 0, g -> g.isValidIn(s, ItemStack.EMPTY, max));
                if (gem != null) {
                    ItemStack gemStack = GemRegistry.createGemStack(gem, max);
                    SocketHelper.setGems(s, new SocketedGems(ImmutableList.of(GemInstance.unsocketed(gemStack))));
                }
            }
            list.add(EmiStack.of(s));
        }
        return list;
    }

    private static void registerPotion(EmiRegistry registry) {
        Item charmItem = Apoth.Items.POTION_CHARM.get();
        registry.setDefaultComparison(charmItem, Comparison.compareData(s -> PotionUtils.getPotion(s.getNbt())));

        List<Map.Entry<ResourceLocation, Potion>> potions = new ArrayList<>();
        for (var e : ForgeRegistries.POTIONS.getEntries()) {
            if (PotionCharmItem.isValidPotion(e.getValue())) potions.add(Map.entry(e.getKey().location(), e.getValue()));
        }
        for (CraftingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (!(recipe instanceof PotionCharmRecipe charm)) continue;
            registry.removeRecipes(charm.getId());
            int w = charm.getWidth();
            int h = charm.getHeight();
            List<Ingredient> ings = charm.getIngredients();
            for (var e : potions) {
                ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.POTION), e.getValue());
                List<EmiIngredient> grid = new ArrayList<>(9);
                for (int i = 0; i < 9; i++) {
                    int x = i % 3;
                    int y = i / 3;
                    if (x >= w || y >= h) {
                        grid.add(EmiStack.EMPTY);
                        continue;
                    }
                    int idx = y * w + x;
                    grid.add(charm.getPotionSlots().contains(idx) ? EmiStack.of(potion) : EmiIngredient.of(ings.get(idx)));
                }
                ItemStack out = PotionUtils.setPotion(new ItemStack(charmItem), e.getValue());
                ResourceLocation id = new ResourceLocation(MODID, "/" + charm.getId().getPath() + "/" + e.getKey().getNamespace() + "/" + e.getKey().getPath());
                registry.addRecipe(new EmiCraftingRecipe(grid, EmiStack.of(out), id, false));
            }
        }
    }

    private static void registerVillage(EmiRegistry registry) {
        Category category = new Category("fletching", EmiStack.of(Items.FLETCHING_TABLE), "apotheosis.recipes.fletching");
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(Items.FLETCHING_TABLE));
        for (FletchingRecipe recipe : registry.getRecipeManager().getAllRecipesFor(Apoth.RecipeTypes.FLETCHING)) {
            registry.addRecipe(new FletchingEmiRecipe(category, recipe));
        }
        registry.addRecipeHandler(Apoth.Menus.FLETCHING.get(), new FletchingHandler(category));
    }

    static final class Category extends EmiRecipeCategory {

        private final Component name;

        Category(String path, EmiStack icon, String key) {
            super(new ResourceLocation(MODID, path), icon, icon);
            this.name = Component.translatable(key);
        }

        @Override
        public Component getName() {
            return name;
        }
    }

    private record FletchingHandler(EmiRecipeCategory category) implements StandardRecipeHandler<FletchingContainer> {

        @Override
        public List<Slot> getInputSources(FletchingContainer handler) {
            return handler.slots.subList(1, handler.slots.size());
        }

        @Override
        public List<Slot> getCraftingSlots(FletchingContainer handler) {
            return handler.slots.subList(1, 4);
        }

        @Override
        public Slot getOutputSlot(FletchingContainer handler) {
            return handler.slots.get(0);
        }

        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == category;
        }
    }
}

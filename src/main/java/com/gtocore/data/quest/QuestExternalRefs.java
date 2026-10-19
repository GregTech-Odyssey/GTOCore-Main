package com.gtocore.data.quest;

import com.gtocore.integration.Mods;

import com.gtolib.api.quest.OptionalItem;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.utils.RLUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;
import net.potionstudios.biomeswevegone.world.item.BWGItems;

import com.arsmeteorites.arsmeteorites.common.ConjureMeteoritesRitual;
import com.buuz135.functionalstorage.FunctionalStorage;
import com.buuz135.functionalstorage.item.StorageUpgradeItem;
import com.github.leopoko.solclassic.item.BasketItem;
import com.github.leopoko.solclassic.item.WickerBasketItem;
import com.hollingsworth.arsnouveau.api.registry.RitualRegistry;
import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.common.items.RitualTablet;
import com.hollingsworth.arsnouveau.common.ritual.RitualAwakening;
import com.hollingsworth.arsnouveau.common.ritual.RitualBinding;
import com.hollingsworth.arsnouveau.common.ritual.RitualBreed;
import com.hollingsworth.arsnouveau.common.ritual.RitualCloudshaper;
import com.hollingsworth.arsnouveau.common.ritual.RitualDig;
import com.hollingsworth.arsnouveau.common.ritual.RitualDisintegration;
import com.hollingsworth.arsnouveau.common.ritual.RitualFlight;
import com.hollingsworth.arsnouveau.common.ritual.RitualHealing;
import com.hollingsworth.arsnouveau.common.ritual.RitualMoonfall;
import com.hollingsworth.arsnouveau.common.ritual.RitualOvergrowth;
import com.hollingsworth.arsnouveau.common.ritual.RitualPillagerRaid;
import com.hollingsworth.arsnouveau.common.ritual.RitualScrying;
import com.hollingsworth.arsnouveau.common.ritual.RitualSunrise;
import com.hollingsworth.arsnouveau.common.ritual.RitualWarp;
import com.hollingsworth.arsnouveau.common.ritual.RitualWildenSummoning;
import de.maxhenkel.pipez.blocks.ModBlocks;
import dev.gigaherz.toolbelt.ToolBelt;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import umpaz.farmersrespite.common.registry.FRItems;
import vazkii.patchouli.common.item.PatchouliItems;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.function.Supplier;

public final class QuestExternalRefs {

    public static final ResourceKey<Structure> OIL_WELL = ResourceKey.create(Registries.STRUCTURE, RLUtils.fromNamespaceAndPath("ad_astra", "oil_well"));
    public static final ItemLike RITUAL_AWAKENING = () -> ritualTablet(RitualAwakening.class);
    public static final ItemLike RITUAL_BINDING = () -> ritualTablet(RitualBinding.class);
    public static final ItemLike RITUAL_BURROWING = () -> ritualTablet(RitualDig.class);
    public static final ItemLike RITUAL_CHALLENGE = () -> ritualTablet(RitualPillagerRaid.class);
    public static final ItemLike RITUAL_CLOUDSHAPING = () -> ritualTablet(RitualCloudshaper.class);
    public static final ItemLike RITUAL_CONJURE_METEORITES = () -> ritualTablet(ConjureMeteoritesRitual.class);
    public static final ItemLike RITUAL_DISINTEGRATION = () -> ritualTablet(RitualDisintegration.class);
    public static final ItemLike RITUAL_FERTILITY = () -> ritualTablet(RitualBreed.class);
    public static final ItemLike RITUAL_FLIGHT = () -> ritualTablet(RitualFlight.class);
    public static final ItemLike RITUAL_MOONFALL = () -> ritualTablet(RitualMoonfall.class);
    public static final ItemLike RITUAL_OVERGROWTH = () -> ritualTablet(RitualOvergrowth.class);
    public static final ItemLike RITUAL_RESTORATION = () -> ritualTablet(RitualHealing.class);
    public static final ItemLike RITUAL_SCRYING = () -> ritualTablet(RitualScrying.class);
    public static final ItemLike RITUAL_SUNRISE = () -> ritualTablet(RitualSunrise.class);
    public static final ItemLike RITUAL_WARPING = () -> ritualTablet(RitualWarp.class);
    public static final ItemLike RITUAL_WILDEN_SUMMON = () -> ritualTablet(RitualWildenSummoning.class);
    public static final ItemLike SOLCLASSIC_BASKET = () -> byItemClass(BasketItem.class);
    public static final ItemLike SOLCLASSIC_WICKER_BASKET = () -> byItemClass(WickerBasketItem.class);
    public static final ItemLike SOURCESTONE = () -> byId("ars_nouveau", "sourcestone");
    public static final TagKey<Item> TAG_C_DYES = TagKey.create(Registries.ITEM, RLUtils.fromNamespaceAndPath("c", "dyes"));
    public static final TagKey<Item> TAG_FORGE_LOGS_ARCHWOOD = TagKey.create(Registries.ITEM, RLUtils.fromNamespaceAndPath("forge", "logs/archwood"));
    public static final TagKey<Item> TAG_GTOCORE_VEIN_ESSENCE = TagKey.create(Registries.ITEM, RLUtils.fromNamespaceAndPath("gtocore", "vein_essence"));

    private QuestExternalRefs() {}

    public static ItemStack astroduxBook() {
        return QuestStacks.withNbt(new ItemStack(PatchouliItems.BOOK), "{\"patchouli:book\":\"ad_astra:astrodux\"}");
    }

    public static ItemStack healingWine() {
        return QuestStacks.withNbt(new ItemStack(ExtraBotanyItems.infiniteWine), "{brewKey:\"botania:healing\"}");
    }

    public static ItemStack mageslayerGem() {
        return QuestStacks.withNbt(new ItemStack(Adventure.Items.GEM.get()), "{gem:\"apotheosis:the_end/mageslayer\",affix_data:{rarity:\"apotheosis:ancient\"}}");
    }

    public static Item ritualTablet(Class<? extends AbstractRitual> ritual) {
        for (RitualTablet tablet : RitualRegistry.getRitualItemMap().values()) {
            if (ritual.isInstance(tablet.ritual)) return tablet;
        }
        throw new IllegalStateException("No ritual tablet for " + ritual.getName());
    }

    public static Item byItemClass(Class<? extends Item> type) {
        Item found = null;
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item.getClass() != type) continue;
            if (found != null) throw new IllegalStateException("More than one item of " + type.getName());
            found = item;
        }
        if (found == null) throw new IllegalStateException("No item of " + type.getName());
        return found;
    }

    private static Item byId(String namespace, String path) {
        Item item = ForgeRegistries.ITEMS.getValue(RLUtils.fromNamespaceAndPath(namespace, path));
        if (item == null || item == Items.AIR) throw new IllegalStateException("Missing item " + namespace + ":" + path);
        return item;
    }

    private static OptionalItem optional(Mods mod, Supplier<? extends ItemLike> item) {
        return new OptionalItem() {

            @Override
            public boolean available() {
                return mod.isLoaded();
            }

            @Override
            public Item asItem() {
                return mod.isLoaded() ? item.get().asItem() : Items.AIR;
            }
        };
    }

    public static final class BiomesWeveGoneItems {

        public static final OptionalItem BLUEBERRY_PIE = optional(Mods.BIOMESWEVEGONE, () -> BWGItems.BLUEBERRY_PIE.get());
        public static final OptionalItem GREEN_APPLE_PIE = optional(Mods.BIOMESWEVEGONE, () -> BWGItems.GREEN_APPLE_PIE.get());

        private BiomesWeveGoneItems() {}
    }

    public static final class ConstructionWandItems {

        public static final OptionalItem CORE_ANGEL = optional(Mods.CONSTRUCTION_WAND, () -> thetadev.constructionwand.items.ModItems.CORE_ANGEL.get());
        public static final OptionalItem INFINITY_WAND = optional(Mods.CONSTRUCTION_WAND, () -> thetadev.constructionwand.items.ModItems.WAND_INFINITY.get());
        public static final OptionalItem IRON_WAND = optional(Mods.CONSTRUCTION_WAND, () -> thetadev.constructionwand.items.ModItems.WAND_IRON.get());
        public static final OptionalItem STONE_WAND = optional(Mods.CONSTRUCTION_WAND, () -> thetadev.constructionwand.items.ModItems.WAND_STONE.get());

        private ConstructionWandItems() {}
    }

    public static final class FactoryBlocksItems {

        public static final OptionalItem FACTORY = optional(Mods.FACTORY_BLOCKS, () -> byId("factory_blocks", "factory"));

        private FactoryBlocksItems() {}
    }

    public static final class FarmersDelightItems {

        public static final OptionalItem APPLE_CIDER = optional(Mods.FARMERSDELIGHT, () -> ModItems.APPLE_CIDER.get());
        public static final OptionalItem APPLE_PIE = optional(Mods.FARMERSDELIGHT, () -> ModItems.APPLE_PIE.get());
        public static final OptionalItem BACON = optional(Mods.FARMERSDELIGHT, () -> ModItems.BACON.get());
        public static final OptionalItem BACON_AND_EGGS = optional(Mods.FARMERSDELIGHT, () -> ModItems.BACON_AND_EGGS.get());
        public static final OptionalItem BACON_SANDWICH = optional(Mods.FARMERSDELIGHT, () -> ModItems.BACON_SANDWICH.get());
        public static final OptionalItem BAKED_COD_STEW = optional(Mods.FARMERSDELIGHT, () -> ModItems.BAKED_COD_STEW.get());
        public static final OptionalItem BAMBOO_BASKET = optional(Mods.FARMERSDELIGHT, () -> ModItems.BAMBOO_BASKET.get());
        public static final OptionalItem BARBECUE_STICK = optional(Mods.FARMERSDELIGHT, () -> ModItems.BARBECUE_STICK.get());
        public static final OptionalItem BEEF_PATTY = optional(Mods.FARMERSDELIGHT, () -> ModItems.BEEF_PATTY.get());
        public static final OptionalItem BEEF_STEW = optional(Mods.FARMERSDELIGHT, () -> ModItems.BEEF_STEW.get());
        public static final OptionalItem BONE_BROTH = optional(Mods.FARMERSDELIGHT, () -> ModItems.BONE_BROTH.get());
        public static final OptionalItem CABBAGE = optional(Mods.FARMERSDELIGHT, () -> ModItems.CABBAGE.get());
        public static final OptionalItem CABBAGE_LEAF = optional(Mods.FARMERSDELIGHT, () -> ModItems.CABBAGE_LEAF.get());
        public static final OptionalItem CABBAGE_ROLLS = optional(Mods.FARMERSDELIGHT, () -> ModItems.CABBAGE_ROLLS.get());
        public static final OptionalItem CABBAGE_SEEDS = optional(Mods.FARMERSDELIGHT, () -> ModItems.CABBAGE_SEEDS.get());
        public static final OptionalItem CAKE_SLICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.CAKE_SLICE.get());
        public static final OptionalItem CHICKEN_CUTS = optional(Mods.FARMERSDELIGHT, () -> ModItems.CHICKEN_CUTS.get());
        public static final OptionalItem CHICKEN_SANDWICH = optional(Mods.FARMERSDELIGHT, () -> ModItems.CHICKEN_SANDWICH.get());
        public static final OptionalItem CHICKEN_SOUP = optional(Mods.FARMERSDELIGHT, () -> ModItems.CHICKEN_SOUP.get());
        public static final OptionalItem CHOCOLATE_PIE = optional(Mods.FARMERSDELIGHT, () -> ModItems.CHOCOLATE_PIE.get());
        public static final OptionalItem CHOCOLATE_PIE_SLICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.CHOCOLATE_PIE_SLICE.get());
        public static final OptionalItem COD_ROLL = optional(Mods.FARMERSDELIGHT, () -> ModItems.COD_ROLL.get());
        public static final OptionalItem COD_SLICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.COD_SLICE.get());
        public static final OptionalItem COOKED_BACON = optional(Mods.FARMERSDELIGHT, () -> ModItems.COOKED_BACON.get());
        public static final OptionalItem COOKED_CHICKEN_CUTS = optional(Mods.FARMERSDELIGHT, () -> ModItems.COOKED_CHICKEN_CUTS.get());
        public static final OptionalItem COOKED_COD_SLICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.COOKED_COD_SLICE.get());
        public static final OptionalItem COOKED_MUTTON_CHOPS = optional(Mods.FARMERSDELIGHT, () -> ModItems.COOKED_MUTTON_CHOPS.get());
        public static final OptionalItem COOKED_RICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.COOKED_RICE.get());
        public static final OptionalItem COOKED_SALMON_SLICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.COOKED_SALMON_SLICE.get());
        public static final OptionalItem COOKING_POT = optional(Mods.FARMERSDELIGHT, () -> ModItems.COOKING_POT.get());
        public static final OptionalItem CUTTING_BOARD = optional(Mods.FARMERSDELIGHT, () -> ModItems.CUTTING_BOARD.get());
        public static final OptionalItem DOG_FOOD = optional(Mods.FARMERSDELIGHT, () -> ModItems.DOG_FOOD.get());
        public static final OptionalItem DUMPLINGS = optional(Mods.FARMERSDELIGHT, () -> ModItems.DUMPLINGS.get());
        public static final OptionalItem EGG_SANDWICH = optional(Mods.FARMERSDELIGHT, () -> ModItems.EGG_SANDWICH.get());
        public static final OptionalItem FISH_STEW = optional(Mods.FARMERSDELIGHT, () -> ModItems.FISH_STEW.get());
        public static final OptionalItem FLINT_KNIFE = optional(Mods.FARMERSDELIGHT, () -> ModItems.FLINT_KNIFE.get());
        public static final OptionalItem FRIED_EGG = optional(Mods.FARMERSDELIGHT, () -> ModItems.FRIED_EGG.get());
        public static final OptionalItem FRIED_RICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.FRIED_RICE.get());
        public static final OptionalItem FRUIT_SALAD = optional(Mods.FARMERSDELIGHT, () -> ModItems.FRUIT_SALAD.get());
        public static final OptionalItem GLOW_BERRY_CUSTARD = optional(Mods.FARMERSDELIGHT, () -> ModItems.GLOW_BERRY_CUSTARD.get());
        public static final OptionalItem GRILLED_SALMON = optional(Mods.FARMERSDELIGHT, () -> ModItems.GRILLED_SALMON.get());
        public static final OptionalItem HAM = optional(Mods.FARMERSDELIGHT, () -> ModItems.HAM.get());
        public static final OptionalItem HAMBURGER = optional(Mods.FARMERSDELIGHT, () -> ModItems.HAMBURGER.get());
        public static final OptionalItem HONEY_COOKIE = optional(Mods.FARMERSDELIGHT, () -> ModItems.HONEY_COOKIE.get());
        public static final OptionalItem HONEY_GLAZED_HAM = optional(Mods.FARMERSDELIGHT, () -> ModItems.HONEY_GLAZED_HAM.get());
        public static final OptionalItem HONEY_GLAZED_HAM_BLOCK = optional(Mods.FARMERSDELIGHT, () -> ModItems.HONEY_GLAZED_HAM_BLOCK.get());
        public static final OptionalItem HOT_COCOA = optional(Mods.FARMERSDELIGHT, () -> ModItems.HOT_COCOA.get());
        public static final OptionalItem KELP_ROLL = optional(Mods.FARMERSDELIGHT, () -> ModItems.KELP_ROLL.get());
        public static final OptionalItem KELP_ROLL_SLICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.KELP_ROLL_SLICE.get());
        public static final OptionalItem MELON_JUICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.MELON_JUICE.get());
        public static final OptionalItem MELON_POPSICLE = optional(Mods.FARMERSDELIGHT, () -> ModItems.MELON_POPSICLE.get());
        public static final OptionalItem MINCED_BEEF = optional(Mods.FARMERSDELIGHT, () -> ModItems.MINCED_BEEF.get());
        public static final OptionalItem MIXED_SALAD = optional(Mods.FARMERSDELIGHT, () -> ModItems.MIXED_SALAD.get());
        public static final OptionalItem MUSHROOM_RICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.MUSHROOM_RICE.get());
        public static final OptionalItem MUTTON_CHOPS = optional(Mods.FARMERSDELIGHT, () -> ModItems.MUTTON_CHOPS.get());
        public static final OptionalItem MUTTON_WRAP = optional(Mods.FARMERSDELIGHT, () -> ModItems.MUTTON_WRAP.get());
        public static final OptionalItem NETHER_SALAD = optional(Mods.FARMERSDELIGHT, () -> ModItems.NETHER_SALAD.get());
        public static final OptionalItem NOODLE_SOUP = optional(Mods.FARMERSDELIGHT, () -> ModItems.NOODLE_SOUP.get());
        public static final OptionalItem ONION = optional(Mods.FARMERSDELIGHT, () -> ModItems.ONION.get());
        public static final OptionalItem PASTA_WITH_MEATBALLS = optional(Mods.FARMERSDELIGHT, () -> ModItems.PASTA_WITH_MEATBALLS.get());
        public static final OptionalItem PASTA_WITH_MUTTON_CHOP = optional(Mods.FARMERSDELIGHT, () -> ModItems.PASTA_WITH_MUTTON_CHOP.get());
        public static final OptionalItem PIE_CRUST = optional(Mods.FARMERSDELIGHT, () -> ModItems.PIE_CRUST.get());
        public static final OptionalItem PUMPKIN_SLICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.PUMPKIN_SLICE.get());
        public static final OptionalItem PUMPKIN_SOUP = optional(Mods.FARMERSDELIGHT, () -> ModItems.PUMPKIN_SOUP.get());
        public static final OptionalItem RAW_PASTA = optional(Mods.FARMERSDELIGHT, () -> ModItems.RAW_PASTA.get());
        public static final OptionalItem RICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.RICE.get());
        public static final OptionalItem RICE_ROLL_MEDLEY_BLOCK = optional(Mods.FARMERSDELIGHT, () -> ModItems.RICE_ROLL_MEDLEY_BLOCK.get());
        public static final OptionalItem ROASTED_MUTTON_CHOPS = optional(Mods.FARMERSDELIGHT, () -> ModItems.ROASTED_MUTTON_CHOPS.get());
        public static final OptionalItem ROAST_CHICKEN_BLOCK = optional(Mods.FARMERSDELIGHT, () -> ModItems.ROAST_CHICKEN_BLOCK.get());
        public static final OptionalItem SALMON_ROLL = optional(Mods.FARMERSDELIGHT, () -> ModItems.SALMON_ROLL.get());
        public static final OptionalItem SALMON_SLICE = optional(Mods.FARMERSDELIGHT, () -> ModItems.SALMON_SLICE.get());
        public static final OptionalItem SHEPHERDS_PIE_BLOCK = optional(Mods.FARMERSDELIGHT, () -> ModItems.SHEPHERDS_PIE_BLOCK.get());
        public static final OptionalItem SMOKED_HAM = optional(Mods.FARMERSDELIGHT, () -> ModItems.SMOKED_HAM.get());
        public static final OptionalItem SQUID_INK_PASTA = optional(Mods.FARMERSDELIGHT, () -> ModItems.SQUID_INK_PASTA.get());
        public static final OptionalItem STEAK_AND_POTATOES = optional(Mods.FARMERSDELIGHT, () -> ModItems.STEAK_AND_POTATOES.get());
        public static final OptionalItem STUFFED_POTATO = optional(Mods.FARMERSDELIGHT, () -> ModItems.STUFFED_POTATO.get());
        public static final OptionalItem STUFFED_PUMPKIN_BLOCK = optional(Mods.FARMERSDELIGHT, () -> ModItems.STUFFED_PUMPKIN_BLOCK.get());
        public static final OptionalItem SWEET_BERRY_CHEESECAKE = optional(Mods.FARMERSDELIGHT, () -> ModItems.SWEET_BERRY_CHEESECAKE.get());
        public static final OptionalItem SWEET_BERRY_COOKIE = optional(Mods.FARMERSDELIGHT, () -> ModItems.SWEET_BERRY_COOKIE.get());
        public static final OptionalItem TOMATO = optional(Mods.FARMERSDELIGHT, () -> ModItems.TOMATO.get());
        public static final OptionalItem TOMATO_SAUCE = optional(Mods.FARMERSDELIGHT, () -> ModItems.TOMATO_SAUCE.get());
        public static final OptionalItem TOMATO_SEEDS = optional(Mods.FARMERSDELIGHT, () -> ModItems.TOMATO_SEEDS.get());
        public static final OptionalItem VEGETABLE_NOODLES = optional(Mods.FARMERSDELIGHT, () -> ModItems.VEGETABLE_NOODLES.get());
        public static final OptionalItem VEGETABLE_SOUP = optional(Mods.FARMERSDELIGHT, () -> ModItems.VEGETABLE_SOUP.get());

        private FarmersDelightItems() {}
    }

    public static final class FarmersRespiteItems {

        public static final OptionalItem BLACK_COD = optional(Mods.FARMERSDELIGHT, () -> FRItems.BLACK_COD.get());
        public static final OptionalItem BLACK_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.BLACK_TEA.get());
        public static final OptionalItem BLACK_TEA_LEAVES = optional(Mods.FARMERSDELIGHT, () -> FRItems.BLACK_TEA_LEAVES.get());
        public static final OptionalItem BLAZING_CHILI = optional(Mods.FARMERSDELIGHT, () -> FRItems.BLAZING_CHILI.get());
        public static final OptionalItem COFFEE = optional(Mods.FARMERSDELIGHT, () -> FRItems.COFFEE.get());
        public static final OptionalItem COFFEE_BEANS = optional(Mods.FARMERSDELIGHT, () -> FRItems.COFFEE_BEANS.get());
        public static final OptionalItem COFFEE_BERRIES = optional(Mods.FARMERSDELIGHT, () -> FRItems.COFFEE_BERRIES.get());
        public static final OptionalItem COFFEE_CAKE = optional(Mods.FARMERSDELIGHT, () -> FRItems.COFFEE_CAKE.get());
        public static final OptionalItem COFFEE_CAKE_SLICE = optional(Mods.FARMERSDELIGHT, () -> FRItems.COFFEE_CAKE_SLICE.get());
        public static final OptionalItem DANDELION_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.DANDELION_TEA.get());
        public static final OptionalItem GAMBLERS_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.GAMBLERS_TEA.get());
        public static final OptionalItem GREEN_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.GREEN_TEA.get());
        public static final OptionalItem GREEN_TEA_COOKIE = optional(Mods.FARMERSDELIGHT, () -> FRItems.GREEN_TEA_COOKIE.get());
        public static final OptionalItem GREEN_TEA_LEAVES = optional(Mods.FARMERSDELIGHT, () -> FRItems.GREEN_TEA_LEAVES.get());
        public static final OptionalItem KETTLE = optional(Mods.FARMERSDELIGHT, () -> FRItems.KETTLE.get());
        public static final OptionalItem LONG_APPLE_CIDER = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_APPLE_CIDER.get());
        public static final OptionalItem LONG_BLACK_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_BLACK_TEA.get());
        public static final OptionalItem LONG_COFFEE = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_COFFEE.get());
        public static final OptionalItem LONG_DANDELION_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_DANDELION_TEA.get());
        public static final OptionalItem LONG_GAMBLERS_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_GAMBLERS_TEA.get());
        public static final OptionalItem LONG_GREEN_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_GREEN_TEA.get());
        public static final OptionalItem LONG_PURULENT_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_PURULENT_TEA.get());
        public static final OptionalItem LONG_ROSE_HIP_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_ROSE_HIP_TEA.get());
        public static final OptionalItem LONG_YELLOW_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.LONG_YELLOW_TEA.get());
        public static final OptionalItem NETHER_WART_SOURDOUGH = optional(Mods.FARMERSDELIGHT, () -> FRItems.NETHER_WART_SOURDOUGH.get());
        public static final OptionalItem PURULENT_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.PURULENT_TEA.get());
        public static final OptionalItem ROSE_HIP_PIE = optional(Mods.FARMERSDELIGHT, () -> FRItems.ROSE_HIP_PIE.get());
        public static final OptionalItem ROSE_HIP_PIE_SLICE = optional(Mods.FARMERSDELIGHT, () -> FRItems.ROSE_HIP_PIE_SLICE.get());
        public static final OptionalItem ROSE_HIP_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.ROSE_HIP_TEA.get());
        public static final OptionalItem STRONG_APPLE_CIDER = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_APPLE_CIDER.get());
        public static final OptionalItem STRONG_BLACK_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_BLACK_TEA.get());
        public static final OptionalItem STRONG_COFFEE = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_COFFEE.get());
        public static final OptionalItem STRONG_GAMBLERS_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_GAMBLERS_TEA.get());
        public static final OptionalItem STRONG_GREEN_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_GREEN_TEA.get());
        public static final OptionalItem STRONG_HOT_COCOA = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_HOT_COCOA.get());
        public static final OptionalItem STRONG_MELON_JUICE = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_MELON_JUICE.get());
        public static final OptionalItem STRONG_PURULENT_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_PURULENT_TEA.get());
        public static final OptionalItem STRONG_ROSE_HIP_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_ROSE_HIP_TEA.get());
        public static final OptionalItem STRONG_YELLOW_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.STRONG_YELLOW_TEA.get());
        public static final OptionalItem TEA_CURRY = optional(Mods.FARMERSDELIGHT, () -> FRItems.TEA_CURRY.get());
        public static final OptionalItem YELLOW_TEA = optional(Mods.FARMERSDELIGHT, () -> FRItems.YELLOW_TEA.get());
        public static final OptionalItem YELLOW_TEA_LEAVES = optional(Mods.FARMERSDELIGHT, () -> FRItems.YELLOW_TEA_LEAVES.get());

        private FarmersRespiteItems() {}
    }

    public static final class FunctionalStorageItems {

        public static final OptionalItem MAX_STORAGE_UPGRADE = optional(Mods.FUNCTIONALSTORAGE, () -> FunctionalStorage.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.MAX_STORAGE).get());

        private FunctionalStorageItems() {}
    }

    public static final class ImmersiveAircraftItems {

        public static final OptionalItem AIRSHIP = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.AIRSHIP.get());
        public static final OptionalItem BIPLANE = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.BIPLANE.get());
        public static final OptionalItem BOMB_BAY = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.BOMB_BAY.get());
        public static final OptionalItem CARGO_AIRSHIP = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.CARGO_AIRSHIP.get());
        public static final OptionalItem ECO_ENGINE = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.ECO_ENGINE.get());
        public static final OptionalItem ENGINE = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.ENGINE.get());
        public static final OptionalItem ENHANCED_PROPELLER = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.ENHANCED_PROPELLER.get());
        public static final OptionalItem GYRODYNE = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.GYRODYNE.get());
        public static final OptionalItem GYROSCOPE = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.GYROSCOPE.get());
        public static final OptionalItem HEAVY_CROSSBOW = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.HEAVY_CROSSBOW.get());
        public static final OptionalItem HULL = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.HULL.get());
        public static final OptionalItem HULL_REINFORCEMENT = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.HULL_REINFORCEMENT.get());
        public static final OptionalItem IMPROVED_LANDING_GEAR = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.IMPROVED_LANDING_GEAR.get());
        public static final OptionalItem INDUSTRIAL_GEARS = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.INDUSTRIAL_GEARS.get());
        public static final OptionalItem NETHER_ENGINE = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.NETHER_ENGINE.get());
        public static final OptionalItem PROPELLER = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.PROPELLER.get());
        public static final OptionalItem QUADROCOPTER = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.QUADROCOPTER.get());
        public static final OptionalItem ROTARY_CANNON = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.ROTARY_CANNON.get());
        public static final OptionalItem SAIL = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.SAIL.get());
        public static final OptionalItem STEEL_BOILER = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.STEEL_BOILER.get());
        public static final OptionalItem STURDY_PIPES = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.STURDY_PIPES.get());
        public static final OptionalItem TELESCOPE = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.TELESCOPE.get());
        public static final OptionalItem WARSHIP = optional(Mods.IMMERSIVE_AIRCRAFT, () -> immersive_aircraft.Items.WARSHIP.get());

        private ImmersiveAircraftItems() {}
    }

    public static final class MythicBotanyItems {

        public static final OptionalItem ALFSTEEL_INGOT = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModItems.alfsteelIngot);
        public static final OptionalItem ALFSTEEL_PYLON = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.alfsteelPylon);
        public static final OptionalItem AQUAPANTHUS = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.aquapanthus);
        public static final OptionalItem ASGARD_RUNE = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModItems.asgardRune);
        public static final OptionalItem CENTRAL_RUNE_HOLDER = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.centralRuneHolder);
        public static final OptionalItem EXOBLAZE = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.exoblaze);
        public static final OptionalItem FEYSYTHIA = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.feysythia);
        public static final OptionalItem FIMBULTYR_TABLET = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModItems.fimbultyrTablet);
        public static final OptionalItem GJALLAR_HORN_FULL = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModItems.gjallarHornFull);
        public static final OptionalItem HELLEBORE = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.hellebore);
        public static final OptionalItem KVASIR_MEAD = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModItems.kvasirMead);
        public static final OptionalItem MANA_INFUSER = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.manaInfuser);
        public static final OptionalItem MJOELLNIR = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.mjoellnir);
        public static final OptionalItem PETRUNIA = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.petrunia);
        public static final OptionalItem RAINDELETIA = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.raindeletia);
        public static final OptionalItem RUNE_HOLDER = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.runeHolder);
        public static final OptionalItem WITHER_ACONITE = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.witherAconite);
        public static final OptionalItem YGGDRASIL_BRANCH = optional(Mods.MYTHICBOTANY, () -> mythicbotany.register.ModBlocks.yggdrasilBranch);

        private MythicBotanyItems() {}
    }

    public static final class PipezItems {

        public static final OptionalItem FLUID_PIPE = optional(Mods.PIPEZ, () -> ModBlocks.FLUID_PIPE.get());
        public static final OptionalItem ITEM_PIPE = optional(Mods.PIPEZ, () -> ModBlocks.ITEM_PIPE.get());

        private PipezItems() {}
    }

    public static final class SophisticatedBackpacksItems {

        public static final OptionalItem ADVANCED_COMPACTING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_COMPACTING_UPGRADE.get());
        public static final OptionalItem ADVANCED_DEPOSIT_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_DEPOSIT_UPGRADE.get());
        public static final OptionalItem ADVANCED_FEEDING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_FEEDING_UPGRADE.get());
        public static final OptionalItem ADVANCED_FILTER_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_FILTER_UPGRADE.get());
        public static final OptionalItem ADVANCED_MAGNET_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_MAGNET_UPGRADE.get());
        public static final OptionalItem ADVANCED_PICKUP_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_PICKUP_UPGRADE.get());
        public static final OptionalItem ADVANCED_REFILL_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_REFILL_UPGRADE.get());
        public static final OptionalItem ADVANCED_RESTOCK_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_RESTOCK_UPGRADE.get());
        public static final OptionalItem ADVANCED_VOID_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.ADVANCED_VOID_UPGRADE.get());
        public static final OptionalItem AUTO_BLASTING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_BLASTING_UPGRADE.get());
        public static final OptionalItem AUTO_SMELTING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_SMELTING_UPGRADE.get());
        public static final OptionalItem AUTO_SMOKING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.AUTO_SMOKING_UPGRADE.get());
        public static final OptionalItem BACKPACK = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BACKPACK.get());
        public static final OptionalItem BLASTING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BLASTING_UPGRADE.get());
        public static final OptionalItem COMPACTING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.COMPACTING_UPGRADE.get());
        public static final OptionalItem COPPER_BACKPACK = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.COPPER_BACKPACK.get());
        public static final OptionalItem DEPOSIT_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.DEPOSIT_UPGRADE.get());
        public static final OptionalItem DIAMOND_BACKPACK = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.DIAMOND_BACKPACK.get());
        public static final OptionalItem FEEDING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.FEEDING_UPGRADE.get());
        public static final OptionalItem FILTER_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.FILTER_UPGRADE.get());
        public static final OptionalItem GOLD_BACKPACK = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.GOLD_BACKPACK.get());
        public static final OptionalItem IRON_BACKPACK = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.IRON_BACKPACK.get());
        public static final OptionalItem MAGNET_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.MAGNET_UPGRADE.get());
        public static final OptionalItem NETHERITE_BACKPACK = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.NETHERITE_BACKPACK.get());
        public static final OptionalItem PICKUP_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.PICKUP_UPGRADE.get());
        public static final OptionalItem REFILL_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.REFILL_UPGRADE.get());
        public static final OptionalItem RESTOCK_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.RESTOCK_UPGRADE.get());
        public static final OptionalItem SMELTING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.SMELTING_UPGRADE.get());
        public static final OptionalItem SMOKING_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.SMOKING_UPGRADE.get());
        public static final OptionalItem STACK_UPGRADE_OMEGA_TIER = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_OMEGA_TIER.get());
        public static final OptionalItem STACK_UPGRADE_STARTER_TIER = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_STARTER_TIER.get());
        public static final OptionalItem STACK_UPGRADE_TIER_1 = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_1.get());
        public static final OptionalItem STACK_UPGRADE_TIER_2 = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_2.get());
        public static final OptionalItem STACK_UPGRADE_TIER_3 = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_3.get());
        public static final OptionalItem STACK_UPGRADE_TIER_4 = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.STACK_UPGRADE_TIER_4.get());
        public static final OptionalItem TANK_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.TANK_UPGRADE.get());
        public static final OptionalItem UPGRADE_BASE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.UPGRADE_BASE.get());
        public static final OptionalItem VOID_UPGRADE = optional(Mods.SOPHISTICATEDBACKPACKS, () -> net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.VOID_UPGRADE.get());

        private SophisticatedBackpacksItems() {}
    }

    public static final class ToolBeltItems {

        public static final OptionalItem BELT = optional(Mods.TOOLBELT, () -> ToolBelt.BELT.get());
        public static final OptionalItem POUCH = optional(Mods.TOOLBELT, () -> ToolBelt.POUCH.get());

        private ToolBeltItems() {}
    }
}

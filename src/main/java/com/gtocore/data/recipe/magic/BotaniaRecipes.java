package com.gtocore.data.recipe.magic;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gtolib.utils.RegistriesUtils;
import com.gtolib.utils.TagUtils;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.content.Chance;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import com.hollingsworth.arsnouveau.common.datagen.RecipeDatagen;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import vazkii.botania.api.recipe.RuneRitualRecipe;
import vazkii.botania.common.*;
import vazkii.botania.common.ExtraBotanyTags;
import vectorwing.farmersdelight.common.tag.CommonTags;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.api.data.tag.GTOTagPrefix.SUPERCONDUCTOR_BASE;
import static com.gtocore.common.data.GTOItems.COLORFUL_MYSTICAL_FLOWER;
import static com.gtocore.common.data.GTOMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.*;

public final class BotaniaRecipes {

    public static void init() {
        // // 植物魔法系列测试配方
        // if (GTCEu.isDev()) {
        // // 植物酿造 - 好像只能酿造植物魔法的那些
        // BrewRecipeBuilder.builder("iron100")
        // .brew(BotaniaBrews.clear)
        // .addIngredient(ItemTags.SMALL_FLOWERS)
        // .addIngredient(Items.DIAMOND_BLOCK)
        // .addIngredient(BotaniaTags.Items.RUNES)
        // .save();
        //
        // RuneRitualRecipeBuilder.builder("mana_crystal_ritual")
        // .centerRune(Items.DIRT) // 中心符文：魔法符文
        // .addOuterRune(Items.DIAMOND, 1, 0, true)
        // .addOuterRune(Items.DIAMOND, 0, 1, true)
        // .mana(300)
        // .ticks(200)
        // .addInput(Items.STONE)
        // .addInput(Items.COAL)
        // .addOutput(Items.GLASS)
        // .addOutput(new ItemStack(Items.GOLD_INGOT, 5))
        // .save();
        // }
        //
        // /////////////////////////////////////
        // // *********** 魔法配方 *********** //
        // /////////////////////////////////////
        //
        // // 白雏菊
        // {
        // PureDaisyRecipe("livingclay", StateIngredientHelper.of(BlockTags.DIRT), ChemicalHelper.getBlock(block,
        // Livingclay));
        // PureDaisyRecipe("livingwood", StateIngredientHelper.of(BlockTagProvider.ARCHWOOD_LOGS),
        // vazkii.botania.common.BotaniaBlocks.getBlock("livingwood_log"));
        //
        // }
        //
        // // 魔力池
        // {
        // ManaInfusionRecipe("pulsating", Ingredient.of(ChemicalHelper.getItem(SUPERCONDUCTOR_BASE, PulsatingAlloy)),
        // new ItemStack(ChemicalHelper.getItem(wireGtSingle, PulsatingAlloy)), 400, null, null);
        // ManaInfusionRecipe("conductivee", Ingredient.of(ChemicalHelper.getItem(SUPERCONDUCTOR_BASE,
        // ConductiveAlloy)), new ItemStack(ChemicalHelper.getItem(wireGtSingle, ConductiveAlloy)), 1600, null, null);
        // ManaInfusionRecipe("energeticalloy", Ingredient.of(ChemicalHelper.getItem(SUPERCONDUCTOR_BASE,
        // EnergeticAlloy)), new ItemStack(ChemicalHelper.getItem(wireGtSingle, EnergeticAlloy)), 6400, null, null);
        // ManaInfusionRecipe("vibrantalloy", Ingredient.of(ChemicalHelper.getItem(SUPERCONDUCTOR_BASE, VibrantAlloy)),
        // new ItemStack(ChemicalHelper.getItem(wireGtSingle, VibrantAlloy)), 25600, null, null);
        // ManaInfusionRecipe("endsteel", Ingredient.of(ChemicalHelper.getItem(SUPERCONDUCTOR_BASE, EndSteel)), new
        // ItemStack(ChemicalHelper.getItem(wireGtSingle, EndSteel)), 102400, null, null);
        //
        // ManaInfusionRecipe("manasteel", Ingredient.of(ChemicalHelper.getItem(ingot, Steel)), new
        // ItemStack(ChemicalHelper.getItem(ingot, Manasteel)), 3000, null, null);
        // ManaInfusionRecipe("manasteel_block", Ingredient.of(ChemicalHelper.getItem(block, Steel)), new
        // ItemStack(ChemicalHelper.getItem(block, Manasteel)), 27000, null, null);
        // ManaInfusionRecipe("mana_pearl", Ingredient.of(RegistriesUtils.getItem("torchmaster:frozen_pearl")), new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("mana_pearl")), 6000, null, null);
        // ManaInfusionRecipe("mana_string", Ingredient.of(ItemsRegistry.MAGE_FIBER.asItem()), new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("mana_string")), 1250, null, null);
        //
        // ManaInfusionRecipe("life_essence", Ingredient.of(GTOItems.UNSTABLE_GAIA_SOUL.asItem()), new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("life_essence")), 500000, ChemicalHelper.getBlock(block,
        // Gaia),
        // null);
        //
        // ManaInfusionRecipe("infused_gold", Ingredient.of(Items.GOLD_INGOT), new
        // ItemStack(ChemicalHelper.getItem(ingot, InfusedGold)), 8000, null, null);
        // ManaInfusionRecipe("original_bronze_dust", Ingredient.of(ChemicalHelper.getItem(ingot, Bronze)), new
        // ItemStack(ChemicalHelper.getItem(ingot, OriginalBronze)), 6000, null, null);
        //
        // }
        //
        // // 花药台
        // {
        // ApothecaryRecipe("colorful_mystical_flower", Ingredient.of(CommonTags.Items.SEEDS), new
        // ItemStack(COLORFUL_MYSTICAL_FLOWER, 4),
        // new Ingredient[] { Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("white_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("light_gray_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("gray_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("black_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("brown_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("red_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("orange_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("yellow_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("lime_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("green_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("cyan_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("light_blue_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("blue_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("purple_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("magenta_petal")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("pink_petal")) });
        //
        // }
        //
        // // 符文祭坛
        // {
        // RunicAltarRecipe("runerock_block", 1000000, new ItemStack(ChemicalHelper.getItem(block, Runerock), 8), false,
        // new Ingredient[] { Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_earth")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_air")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_fire")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_water")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_spring")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_summer")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_autumn")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_winter")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_mana")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_lust")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_gluttony")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_greed")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_sloth")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_wrath")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_envy")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("rune_pride")) });
        // RunicAltarRecipe("runerock_block_plas", 1000000, new ItemStack(ChemicalHelper.getItem(block, Runerock), 16),
        // false,
        // new Ingredient[] { Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("asgard_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("vanaheim_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("alfheim_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("midgard_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("joetunheim_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("muspelheim_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("niflheim_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("nidavellir_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("helheim_rune")) });
        // RunicAltarRecipe("transmutation_catalyst", 10000000, new ItemStack(GTOBlocks.TRANSMUTATION_CATALYST), false,
        // new Ingredient[] { Ingredient.of(GTOItems.PHILOSOPHERS_STONE), Ingredient.of(GTOItems.PHILOSOPHERS_STONE),
        // Ingredient.of(GTOItems.PHILOSOPHERS_STONE),
        // Ingredient.of(GTOItems.PHILOSOPHERS_STONE), Ingredient.of(GTOItems.PHILOSOPHERS_STONE),
        // Ingredient.of(GTOItems.PHILOSOPHERS_STONE) });
        // }
        //
        // // 泰拉凝聚板
        // {
        // TAgglomerationRecipe("thaumium_ingot", 500000, new ItemStack(ChemicalHelper.getItem(ingot, Thaumium)),
        // new Ingredient[] { Ingredient.of(ChemicalHelper.getItem(ingot, Livingsteel)),
        // Ingredient.of(ItemsRegistry.SOURCE_GEM), Ingredient.of(ChemicalHelper.getItem(ingot, OriginalBronze)),
        // Ingredient.of(ItemsRegistry.MANIPULATION_ESSENCE) });
        // TAgglomerationRecipe("gaiasteel_ingot", 2500000, new ItemStack(ChemicalHelper.getItem(ingot, Gaiasteel), 3),
        // new Ingredient[] { Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("asgard_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("vanaheim_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("alfheim_rune")),
        // Ingredient.of(ChemicalHelper.getItem(ingot, Alfsteel)),
        // Ingredient.of(ChemicalHelper.getItem(ingot, Runerock)),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("midgard_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("joetunheim_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("muspelheim_rune")),
        // Ingredient.of(ChemicalHelper.getItem(ingot, Alfsteel)), Ingredient.of(ChemicalHelper.getItem(ingot,
        // Runerock)),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("niflheim_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("nidavellir_rune")),
        // Ingredient.of(vazkii.botania.common.MythicBotanyItems.getItem("helheim_rune")),
        // Ingredient.of(ChemicalHelper.getItem(ingot, Alfsteel)),
        // Ingredient.of(ChemicalHelper.getItem(ingot, Runerock)) });
        // TAgglomerationRecipe("gaia_ingot", 5000000, new ItemStack(ChemicalHelper.getItem(ingot, Gaia), 2),
        // new Ingredient[] { Ingredient.of(ChemicalHelper.getItem(ingot, Gaiasteel)),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("life_essence")),
        // Ingredient.of(ChemicalHelper.getItem(ingot,
        // Gaiasteel)), Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("life_essence")) });
        //
        // TAgglomerationRecipe("alfsteel_ingot", 1500000, new ItemStack(ChemicalHelper.getItem(ingot, Alfsteel)),
        // new Ingredient[] { Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("elementium_ingot")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("pixie_dust")),
        // Ingredient.of(vazkii.botania.common.BotaniaItems.getItem("dragonstone")) });
        // }
        //
        // // 精灵门
        // {
        // ElvenTradeRecipeBuilder.builder("dragonstone")
        // .addInput(ItemsRegistry.SOURCE_GEM)
        // .addOutput(vazkii.botania.common.BotaniaItems.getItem("dragonstone"))
        // .save();
        //
        // ElvenTradeRecipeBuilder.builder("dragonstone_block")
        // .addInput(BlockRegistry.SOURCE_GEM_BLOCK)
        // .addOutput(vazkii.botania.common.BotaniaBlocks.getBlock("dragonstone_block"))
        // .save();
        //
        // ElvenTradeRecipeBuilder.builder("colorful_mystical_flower")
        // .addInput(vazkii.botania.common.BotaniaItems.getItem("fertilizer"))
        // .addOutput(COLORFUL_MYSTICAL_FLOWER, 4)
        // .save();
        // }
        //
        // // 凝矿兰
        // {
        // // Material组
        // Material[][] materials = new Material[][] {
        // new Material[] { GTMaterials.Iron, GTMaterials.CassiteriteSand, GTMaterials.Galena, GTMaterials.GarnetRed,
        // GTMaterials.Goethite, GTMaterials.Magnetite, GTMaterials.Gypsum, GTMaterials.Hematite,
        // GTMaterials.Spessartine, GTMaterials.Silver, GTMaterials.Tin, GTMaterials.Gold, GTMaterials.Mica,
        // GTMaterials.Lazurite, GTMaterials.Graphite, GTMaterials.TricalciumPhosphate, GTMaterials.Sodalite,
        // GTMaterials.Kyanite, GTMaterials.Tantalite, GTMaterials.FullersEarth, GTMaterials.GarnetSand,
        // GTMaterials.Cobaltite, GTMaterials.Diamond, GTMaterials.Ruby, GTMaterials.Apatite, GTMaterials.Garnierite,
        // GTOMaterials.SalamanderCrystal, GTMaterials.Almandine, GTMaterials.Olivine, GTMaterials.Pyrochlore,
        // GTMaterials.Realgar, GTMaterials.RockSalt, GTMaterials.Cassiterite, GTMaterials.Grossular,
        // GTMaterials.Sapphire, GTMaterials.Coal, GTMaterials.Cinnabar, GTMaterials.Chalcopyrite,
        // GTMaterials.YellowLimonite, GTMaterials.Lepidolite, GTOMaterials.PerditioCrystal, GTMaterials.Zeolite,
        // GTMaterials.Redstone, GTMaterials.Pyrite, GTOMaterials.GnomeCrystal, GTMaterials.VanadiumMagnetite,
        // GTMaterials.Pentlandite, GTMaterials.Amethyst, GTMaterials.Lapis, GTOMaterials.SylphCrystal,
        // GTMaterials.GreenSapphire, GTMaterials.Soapstone, GTMaterials.Pyrope, GTMaterials.Bentonite,
        // GTMaterials.Pollucite, GTMaterials.Talc, GTMaterials.Salt, GTMaterials.GarnetYellow,
        // GTOMaterials.UndineCrystal, GTMaterials.Calcite, GTMaterials.Oilsands, GTMaterials.GraniticMineralSand,
        // GTMaterials.Malachite, GTMaterials.Pyrolusite, GTMaterials.Opal, GTMaterials.Diatomite, GTMaterials.Asbestos,
        // GTMaterials.BasalticMineralSand, GTMaterials.Nickel, GTMaterials.Spodumene, GTMaterials.GlauconiteSand,
        // GTMaterials.Copper, GTMaterials.Lead },
        // new Material[] { GTMaterials.Barite, GTMaterials.Iron, GTMaterials.Lepidolite, GTMaterials.CertusQuartz,
        // GTMaterials.Chalcocite, GTMaterials.Tetrahedrite, GTMaterials.Spessartine, GTMaterials.Bornite,
        // GTMaterials.Sphalerite, GTMaterials.Alunite, GTMaterials.Topaz, GTMaterials.Powellite, GTMaterials.Lazurite,
        // GTMaterials.NetherQuartz, GTMaterials.Stibnite, GTMaterials.Sodalite, GTMaterials.Tantalite,
        // GTMaterials.Pyrite, GTMaterials.Saltpeter, GTMaterials.Pentlandite, GTMaterials.Lapis,
        // GTMaterials.GreenSapphire, GTMaterials.Cobaltite, GTMaterials.Wulfenite, GTMaterials.Pyrope,
        // GTMaterials.Garnierite, GTMaterials.Salt, GTMaterials.Quartzite, GTMaterials.Beryllium,
        // GTMaterials.Almandine, GTMaterials.Calcite, GTMaterials.RockSalt, GTMaterials.Electrotine,
        // GTMaterials.Pyrolusite, GTMaterials.Diatomite, GTMaterials.Molybdenum, GTMaterials.BlueTopaz,
        // GTMaterials.Grossular, GTMaterials.Nickel, GTMaterials.Sapphire, GTMaterials.Molybdenite,
        // GTMaterials.Spodumene, GTMaterials.Emerald, GTMaterials.Sulfur, GTMaterials.Copper, GTMaterials.Chalcopyrite
        // },
        // new Material[] { GTMaterials.Barite, GTMaterials.Sulfur, GTMaterials.YellowLimonite,
        // GTMaterials.CertusQuartz, GTMaterials.Chalcocite, GTMaterials.Tetrahedrite, GTMaterials.Spessartine,
        // GTMaterials.Hematite, GTMaterials.Goethite, GTMaterials.Bornite, GTMaterials.Sphalerite,
        // GTMaterials.Powellite, GTMaterials.Alunite, GTMaterials.Redstone, GTMaterials.Gold, GTMaterials.Topaz,
        // GTMaterials.NetherQuartz, GTMaterials.Stibnite, GTMaterials.Tantalite, GTMaterials.Pyrite,
        // GTMaterials.Saltpeter, GTMaterials.Wulfenite, GTMaterials.Ruby, GTMaterials.Quartzite,
        // GTMaterials.Pyrolusite, GTMaterials.Diatomite, GTMaterials.Electrotine, GTMaterials.Grossular,
        // GTMaterials.Beryllium, GTMaterials.BlueTopaz, GTMaterials.Molybdenum, GTMaterials.Molybdenite,
        // GTMaterials.Emerald, GTMaterials.Cinnabar, GTMaterials.Copper },
        // new Material[] { GTMaterials.GarnetSand, GTMaterials.Uraninite, GTMaterials.Monazite, GTMaterials.Ilmenite,
        // GTMaterials.CassiteriteSand, GTMaterials.Magnetite, GTMaterials.Aluminium, GTMaterials.Talc, GTMaterials.Tin,
        // GTMaterials.Soapstone, GTMaterials.Gold, GTMaterials.Bauxite, GTMaterials.Diatomite, GTMaterials.Cassiterite,
        // GTMaterials.Asbestos, GTMaterials.Neodymium, GTMaterials.Pitchblende, GTMaterials.Pentlandite,
        // GTMaterials.VanadiumMagnetite, GTMaterials.Bastnasite, GTMaterials.GlauconiteSand },
        // new Material[] { GTMaterials.Iron, GTMaterials.YellowLimonite, GTMaterials.Tetrahedrite,
        // GTMaterials.GarnetRed, GTMaterials.Goethite, GTMaterials.Magnetite, GTMaterials.Gypsum, GTMaterials.Hematite,
        // GTMaterials.Bornite, GTMaterials.Palladium, GTMaterials.Alunite, GTMaterials.Gold, GTMaterials.Mica,
        // GTMaterials.Stibnite, GTMaterials.TricalciumPhosphate, GTMaterials.Kyanite, GTMaterials.Pyrite,
        // GTMaterials.Scheelite, GTMaterials.FullersEarth, GTMaterials.VanadiumMagnetite, GTMaterials.Saltpeter,
        // GTMaterials.Amethyst, GTMaterials.Lithium, GTMaterials.Platinum, GTMaterials.Tungstate,
        // GTMaterials.Pollucite, GTMaterials.GarnetYellow, GTMaterials.Cooperite, GTMaterials.Apatite,
        // GTMaterials.Pyrochlore, GTMaterials.GraniticMineralSand, GTMaterials.Opal, GTMaterials.Malachite,
        // GTMaterials.Diatomite, GTMaterials.Electrotine, GTMaterials.BasalticMineralSand, GTMaterials.Copper,
        // GTMaterials.Chalcopyrite },
        // new Material[] { GTOMaterials.Desh, GTMaterials.Sulfur, GTMaterials.YellowLimonite, GTMaterials.Galena,
        // GTMaterials.Magnetite, GTMaterials.Hematite, GTMaterials.Goethite, GTMaterials.Silver, GTMaterials.Powellite,
        // GTMaterials.Sphalerite, GTMaterials.Gold, GTMaterials.Chromite, GTMaterials.Graphite, GTMaterials.Pyrite,
        // GTMaterials.VanadiumMagnetite, GTMaterials.Wulfenite, GTMaterials.Diamond, GTMaterials.Bentonite,
        // GTMaterials.Olivine, GTMaterials.Molybdenite, GTMaterials.Magnesite, GTMaterials.Molybdenum,
        // GTMaterials.Coal, GTMaterials.GlauconiteSand, GTMaterials.Lead },
        // new Material[] { GTMaterials.Chalcocite, GTMaterials.Spessartine, GTOMaterials.Calorite, GTMaterials.Bornite,
        // GTMaterials.Palladium, GTMaterials.Zeolite, GTMaterials.Alunite, GTMaterials.Topaz, GTMaterials.Tantalite,
        // GTMaterials.Saltpeter, GTMaterials.Pentlandite, GTMaterials.Platinum, GTMaterials.Cobaltite,
        // GTMaterials.Garnierite, GTMaterials.Cooperite, GTMaterials.Pyrochlore, GTMaterials.Realgar,
        // GTMaterials.Pyrolusite, GTMaterials.Diatomite, GTMaterials.Electrotine, GTMaterials.Cassiterite,
        // GTMaterials.Grossular, GTMaterials.Nickel, GTMaterials.BlueTopaz, GTMaterials.Cobalt,
        // GTMaterials.Chalcopyrite },
        // new Material[] { GTMaterials.Monazite, GTMaterials.YellowLimonite, GTMaterials.Magnetite,
        // GTMaterials.Hematite, GTMaterials.Goethite, GTMaterials.Spessartine, GTMaterials.Zeolite, GTMaterials.Gold,
        // GTMaterials.Neodymium, GTMaterials.Tantalite, GTMaterials.Scheelite, GTMaterials.Bastnasite,
        // GTMaterials.Pentlandite, GTMaterials.Lithium, GTMaterials.Tungstate, GTMaterials.Bentonite, GTMaterials.Talc,
        // GTMaterials.Olivine, GTMaterials.Soapstone, GTMaterials.Realgar, GTMaterials.Malachite,
        // GTMaterials.Pyrolusite, GTOMaterials.Ostrum, GTMaterials.Cassiterite, GTMaterials.Grossular,
        // GTMaterials.GlauconiteSand, GTMaterials.Chalcopyrite },
        // new Material[] { GTMaterials.Sulfur, GTMaterials.YellowLimonite, GTMaterials.Naquadah, GTMaterials.Hematite,
        // GTMaterials.Plutonium239, GTMaterials.Magnetite, GTMaterials.Gypsum, GTMaterials.Goethite,
        // GTMaterials.Sphalerite, GTMaterials.Powellite, GTMaterials.Graphite, GTMaterials.Pyrite,
        // GTMaterials.FullersEarth, GTMaterials.Diamond, GTOMaterials.Celestine, GTMaterials.Wulfenite,
        // GTMaterials.Bentonite, GTMaterials.Cooperite, GTMaterials.Olivine, GTMaterials.GraniticMineralSand,
        // GTMaterials.Malachite, GTMaterials.BasalticMineralSand, GTMaterials.Molybdenite, GTMaterials.Molybdenum,
        // GTMaterials.Coal, GTMaterials.Trona, GTMaterials.GlauconiteSand },
        // new Material[] { GTMaterials.Cobaltite, GTMaterials.Ilmenite, GTMaterials.Pollucite, GTMaterials.Aluminium,
        // GTMaterials.Talc, GTMaterials.Garnierite, GTMaterials.Zeolite, GTMaterials.Tin, GTMaterials.Soapstone,
        // GTMaterials.Realgar, GTMaterials.Bauxite, GTMaterials.Mica, GTMaterials.Cassiterite, GTMaterials.Kyanite,
        // GTMaterials.Nickel, GTMaterials.Beryllium, GTMaterials.Emerald, GTMaterials.Pentlandite,
        // GTMaterials.GlauconiteSand, GTMaterials.Chalcopyrite },
        // new Material[] { GTMaterials.Ruby, GTMaterials.Iron, GTMaterials.Platinum, GTMaterials.Galena,
        // GTMaterials.Wulfenite, GTMaterials.Titanium, GTMaterials.Chalcocite, GTMaterials.Palladium,
        // GTMaterials.Ilmenite, GTMaterials.Bornite, GTMaterials.Cooperite, GTMaterials.Silver, GTMaterials.Redstone,
        // GTMaterials.Powellite, GTMaterials.Topaz, GTMaterials.BlueTopaz, GTMaterials.Molybdenum,
        // GTMaterials.Molybdenite, GTMaterials.Pyrite, GTMaterials.Cinnabar, GTMaterials.Lead, GTMaterials.Copper,
        // GTMaterials.Chalcopyrite },
        // new Material[] { GTMaterials.Uraninite, GTOMaterials.Desh, GTMaterials.YellowLimonite, GTMaterials.Magnetite,
        // GTMaterials.CassiteriteSand, GTMaterials.Hematite, GTMaterials.Goethite, GTMaterials.Tetrahedrite,
        // GTMaterials.Chromite, GTMaterials.Stibnite, GTMaterials.TricalciumPhosphate, GTMaterials.VanadiumMagnetite,
        // GTMaterials.GarnetSand, GTMaterials.GreenSapphire, GTMaterials.Pyrope, GTMaterials.Pyrochlore,
        // GTMaterials.Apatite, GTMaterials.Almandine, GTMaterials.Malachite, GTMaterials.Diatomite,
        // GTMaterials.Asbestos, GTMaterials.Sapphire, GTMaterials.Magnesite, GTMaterials.Pitchblende,
        // GTMaterials.Copper },
        // new Material[] { GTMaterials.Amethyst, GTMaterials.Uraninite, GTMaterials.Naquadah, GTMaterials.GarnetRed,
        // GTMaterials.Galena, GTMaterials.Plutonium239, GTMaterials.Pollucite, GTMaterials.Apatite,
        // GTMaterials.Quartzite, GTMaterials.GarnetYellow, GTMaterials.Tungsten, GTMaterials.Silver,
        // GTMaterials.Pyrochlore, GTMaterials.Mica, GTMaterials.Opal, GTMaterials.Pyrolusite,
        // GTMaterials.TricalciumPhosphate, GTMaterials.Kyanite, GTMaterials.Tantalite, GTMaterials.Pitchblende,
        // GTMaterials.CertusQuartz, GTMaterials.Barite, GTOMaterials.Zircon, GTMaterials.Lead },
        // new Material[] { GTMaterials.Monazite, GTMaterials.Lepidolite, GTMaterials.CassiteriteSand,
        // GTMaterials.Bornite, GTMaterials.Tin, GTMaterials.Palladium, GTMaterials.Mica, GTMaterials.Gold,
        // GTMaterials.Lazurite, GTMaterials.Kyanite, GTMaterials.Sodalite, GTMaterials.Neodymium,
        // GTMaterials.Tantalite, GTMaterials.Scheelite, GTMaterials.Lithium, GTMaterials.Bastnasite,
        // GTOMaterials.Zircon, GTMaterials.GarnetSand, GTMaterials.Lapis, GTMaterials.Platinum, GTOMaterials.Celestine,
        // GTMaterials.Tungstate, GTMaterials.Pollucite, GTMaterials.Salt, GTMaterials.Cooperite, GTMaterials.Tungsten,
        // GTMaterials.Oilsands, GTMaterials.Calcite, GTMaterials.RockSalt, GTOMaterials.Ostrum, GTMaterials.Pyrolusite,
        // GTMaterials.Diatomite, GTMaterials.Cassiterite, GTMaterials.Asbestos, GTMaterials.Spodumene,
        // GTMaterials.Coal, GTMaterials.Trona },
        // new Material[] { GTMaterials.Ruby, GTOMaterials.Calorite, GTMaterials.Naquadah, GTMaterials.Galena,
        // GTMaterials.Lepidolite, GTMaterials.Plutonium239, GTMaterials.RockSalt, GTMaterials.Gypsum, GTMaterials.Salt,
        // GTMaterials.Apatite, GTMaterials.Pyrochlore, GTMaterials.Silver, GTMaterials.Oilsands,
        // GTMaterials.GraniticMineralSand, GTMaterials.Redstone, GTMaterials.BasalticMineralSand,
        // GTMaterials.TricalciumPhosphate, GTMaterials.Spodumene, GTMaterials.Cinnabar, GTMaterials.FullersEarth,
        // GTMaterials.Coal, GTMaterials.Cobalt, GTMaterials.Lead },
        // new Material[] { GTMaterials.Iron, GTOMaterials.Desh, GTMaterials.Spessartine, GTMaterials.Hematite,
        // GTMaterials.Goethite, GTMaterials.Bornite, GTMaterials.Tin, GTMaterials.Alunite, GTMaterials.Mica,
        // GTMaterials.Graphite, GTMaterials.TricalciumPhosphate, GTMaterials.Kyanite, GTMaterials.Neodymium,
        // GTMaterials.Tantalite, GTMaterials.FullersEarth, GTMaterials.Scheelite, GTMaterials.Lithium,
        // GTMaterials.Platinum, GTOMaterials.Celestine, GTMaterials.Ruby, GTMaterials.Olivine, GTMaterials.Almandine,
        // GTMaterials.Electrotine, GTMaterials.Grossular, GTMaterials.BlueTopaz, GTMaterials.Molybdenite,
        // GTMaterials.Beryllium, GTMaterials.Emerald, GTMaterials.Coal, GTMaterials.Chalcopyrite, GTMaterials.Monazite,
        // GTMaterials.Chalcocite, GTMaterials.Naquadah, GTMaterials.Ilmenite, GTMaterials.Zeolite,
        // GTMaterials.Chromite, GTMaterials.Stibnite, GTOMaterials.GnomeCrystal, GTMaterials.Lapis,
        // GTMaterials.Bentonite, GTMaterials.Pyrope, GTMaterials.Pollucite, GTMaterials.Salt, GTMaterials.Soapstone,
        // GTOMaterials.UndineCrystal, GTMaterials.Calcite, GTMaterials.Pyrolusite, GTMaterials.Opal,
        // GTMaterials.Asbestos, GTMaterials.Nickel, GTMaterials.Magnesite, GTMaterials.Trona, GTMaterials.Pitchblende,
        // GTMaterials.Copper, GTMaterials.Lead, GTMaterials.CertusQuartz, GTMaterials.Magnetite, GTMaterials.GarnetRed,
        // GTMaterials.Galena, GTMaterials.Plutonium239, GTMaterials.CassiteriteSand, GTMaterials.Gypsum,
        // GTMaterials.Aluminium, GTMaterials.Palladium, GTMaterials.Silver, GTMaterials.Topaz, GTMaterials.Gold,
        // GTMaterials.Lazurite, GTMaterials.Sodalite, GTMaterials.Bastnasite, GTOMaterials.Zircon,
        // GTMaterials.GarnetSand, GTMaterials.Wulfenite, GTMaterials.Cobaltite, GTMaterials.Tungstate,
        // GTMaterials.Diamond, GTMaterials.Garnierite, GTMaterials.Cooperite, GTOMaterials.SalamanderCrystal,
        // GTMaterials.Tungsten, GTMaterials.Apatite, GTMaterials.Pyrochlore, GTMaterials.Realgar, GTMaterials.RockSalt,
        // GTMaterials.Cassiterite, GTMaterials.Sapphire, GTMaterials.Cinnabar, GTMaterials.Cobalt, GTMaterials.Barite,
        // GTMaterials.Uraninite, GTMaterials.Sulfur, GTMaterials.YellowLimonite, GTMaterials.Lepidolite,
        // GTMaterials.Titanium, GTMaterials.Tetrahedrite, GTOMaterials.Calorite, GTOMaterials.PerditioCrystal,
        // GTMaterials.Powellite, GTMaterials.Redstone, GTMaterials.Sphalerite, GTMaterials.Bauxite,
        // GTMaterials.NetherQuartz, GTMaterials.Pyrite, GTMaterials.Saltpeter, GTMaterials.VanadiumMagnetite,
        // GTMaterials.Pentlandite, GTMaterials.Amethyst, GTMaterials.GreenSapphire, GTMaterials.Quartzite,
        // GTMaterials.Talc, GTMaterials.GarnetYellow, GTOMaterials.SylphCrystal, GTMaterials.Oilsands,
        // GTMaterials.GraniticMineralSand, GTMaterials.Diatomite, GTMaterials.Malachite, GTOMaterials.Ostrum,
        // GTMaterials.BasalticMineralSand, GTMaterials.Molybdenum, GTMaterials.Spodumene, GTMaterials.GlauconiteSand },
        // };
        //
        // // int组
        // int[][] material_weights = new int[][] {
        // new int[] { 1454, 1200, 500, 1125, 6000, 725, 200, 2400, 375, 333, 5333, 200, 166, 1125, 544, 333, 750, 250,
        // 187, 400, 800, 250, 272, 400, 500, 375, 474, 642, 125, 166, 500, 428, 3666, 562, 214, 2090, 200, 6136, 2400,
        // 142, 1896, 1000, 600, 1454, 474, 400, 250, 750, 750, 474, 214, 375, 428, 187, 83, 250, 285, 750, 474, 375,
        // 600, 400, 1200, 375, 375, 400, 800, 600, 250, 142, 312, 1454, 166 },
        // new int[] { 166, 1454, 142, 333, 437, 3999, 375, 218, 333, 125, 437, 17, 1125, 1200, 999, 750, 187, 2120,
        // 375, 125, 750, 214, 250, 53, 428, 375, 285, 900, 964, 642, 375, 428, 250, 375, 250, 17, 656, 562, 250, 214,
        // 35, 142, 1284, 1000, 3453, 3636 },
        // new int[] { 166, 1000, 750, 333, 437, 3999, 375, 750, 1125, 218, 333, 17, 125, 600, 375, 437, 1200, 999, 187,
        // 666, 375, 53, 400, 900, 375, 250, 250, 562, 964, 656, 17, 35, 1284, 200, 1999 },
        // new int[] { 800, 93, 150, 300, 1200, 600, 300, 250, 5333, 375, 200, 600, 400, 2666, 800, 150, 561, 125, 400,
        // 450, 250 },
        // new int[] { 1454, 2400, 3999, 1125, 6000, 600, 200, 2400, 75, 25, 125, 200, 166, 999, 333, 250, 1454, 699,
        // 400, 400, 375, 750, 233, 50, 466, 83, 750, 50, 500, 166, 400, 375, 1200, 250, 250, 600, 3453, 3636 },
        // new int[] { 100, 1000, 750, 500, 293, 750, 1125, 333, 17, 333, 375, 225, 544, 666, 56, 53, 272, 187, 125, 35,
        // 200, 17, 90, 62, 166 },
        // new int[] { 437, 375, 150, 293, 25, 1000, 125, 437, 187, 375, 125, 50, 250, 375, 50, 150, 500, 375, 250, 250,
        // 1000, 562, 250, 656, 300, 2500 },
        // new int[] { 150, 2400, 125, 2400, 6000, 375, 1000, 266, 150, 187, 699, 450, 125, 233, 466, 187, 250, 125,
        // 375, 500, 1200, 375, 133, 1000, 562, 312, 2500 },
        // new int[] { 1000, 2400, 561, 2400, 93, 125, 200, 6000, 333, 17, 544, 666, 400, 272, 150, 53, 187, 150, 125,
        // 400, 1200, 600, 35, 17, 90, 300, 62 },
        // new int[] { 250, 300, 83, 300, 250, 375, 1000, 5333, 375, 500, 600, 166, 3666, 250, 250, 964, 1284, 250, 250,
        // 2500 },
        // new int[] { 400, 1454, 50, 500, 53, 133, 437, 25, 266, 293, 50, 333, 600, 17, 437, 656, 17, 35, 1454, 200,
        // 166, 1454, 3636 },
        // new int[] { 93, 100, 2400, 168, 1200, 2400, 6000, 3999, 225, 999, 333, 56, 800, 214, 428, 166, 500, 642,
        // 1200, 400, 800, 214, 200, 561, 1999 },
        // new int[] { 750, 93, 561, 1125, 500, 93, 83, 500, 500, 750, 200, 333, 166, 166, 375, 200, 333, 250, 200, 561,
        // 333, 166, 200, 166 },
        // new int[] { 150, 142, 1200, 75, 5333, 25, 166, 266, 1125, 250, 750, 150, 200, 699, 233, 450, 200, 800, 750,
        // 50, 150, 466, 83, 285, 200, 200, 600, 375, 428, 133, 200, 400, 2666, 800, 142, 2000, 300 },
        // new int[] { 400, 150, 561, 500, 142, 93, 428, 200, 285, 500, 316, 333, 600, 400, 600, 600, 333, 142, 200,
        // 400, 2000, 300, 166 },
        // new int[] { 1454, 100, 375, 3150, 7125, 293, 5333, 125, 166, 544, 333, 250, 150, 387, 400, 699, 233, 50, 150,
        // 400, 125, 642, 250, 562, 656, 35, 964, 1284, 2090, 6136, 150, 437, 561, 566, 1000, 225, 999, 474, 750, 187,
        // 428, 83, 285, 375, 474, 375, 575, 375, 800, 250, 200, 300, 561, 3453, 166, 333, 893, 1125, 500, 93, 1200,
        // 200, 300, 25, 333, 437, 841, 1125, 750, 450, 200, 800, 53, 250, 466, 272, 375, 200, 474, 200, 500, 316, 500,
        // 428, 3666, 214, 200, 300, 166, 93, 1000, 3150, 142, 133, 3999, 150, 1896, 17, 600, 333, 600, 1200, 2120, 375,
        // 456, 250, 750, 214, 900, 250, 750, 474, 600, 400, 650, 1200, 133, 600, 17, 142, 312 },
        // };
        //
        // OrechidRecipe(1, "overworld_", Blocks.STONE, TagPrefix.ore, materials[0], material_weights[0]);
        // OrechidRecipe(2, "the_nether_", Blocks.NETHERRACK, TagPrefix.oreNetherrack, materials[2],
        // material_weights[2]);
        // OrechidRecipe(1, "moon_", ModBlocks.MOON_STONE.get(), GTOTagPrefix.MOON_STONE, materials[3],
        // material_weights[3]);
        // OrechidRecipe(2, "mars_", ModBlocks.MARS_STONE.get(), GTOTagPrefix.MARS_STONE, materials[4],
        // material_weights[4]);
        // OrechidRecipe(2, "venus_", ModBlocks.VENUS_STONE.get(), GTOTagPrefix.VENUS_STONE, materials[5],
        // material_weights[5]);
        // OrechidRecipe(2, "mercury_", ModBlocks.MERCURY_STONE.get(), GTOTagPrefix.MERCURY_STONE, materials[6],
        // material_weights[6]);
        // OrechidRecipe(1, "ceres_", GTOBlocks.CERES_STONE.get(), GTOTagPrefix.CERES_STONE, materials[7],
        // material_weights[7]);
        // OrechidRecipe(1, "io_", GTOBlocks.IO_STONE.get(), GTOTagPrefix.IO_STONE, materials[8], material_weights[8]);
        // OrechidRecipe(1, "ganymede_", GTOBlocks.GANYMEDE_STONE.get(), GTOTagPrefix.GANYMEDE_STONE, materials[9],
        // material_weights[9]);
        // OrechidRecipe(1, "enceladus_", GTOBlocks.ENCELADUS_STONE.get(), GTOTagPrefix.ENCELADUS_STONE, materials[10],
        // material_weights[10]);
        // OrechidRecipe(1, "titan_", GTOBlocks.TITAN_STONE.get(), GTOTagPrefix.TITAN_STONE, materials[11],
        // material_weights[11]);
        // OrechidRecipe(1, "pluto_", GTOBlocks.PLUTO_STONE.get(), GTOTagPrefix.PLUTO_STONE, materials[12],
        // material_weights[12]);
        // OrechidRecipe(3, "glacio_", ModBlocks.GLACIO_STONE.get(), GTOTagPrefix.GLACIO_STONE, materials[13],
        // material_weights[13]);
        // OrechidRecipe(3, "otherside_", DDBlocks.SCULK_STONE.get(), GTOTagPrefix.SCULK_STONE, materials[15],
        // material_weights[15]);
        //
        // OrechidIgnemRecipeBuilder.builder("the_nether_ancient_debris")
        // .input(Blocks.NETHERRACK)
        // .output(Blocks.ANCIENT_DEBRIS)
        // .weight(10)
        // .save();
        //
        // Material[] ores = { PerditioCrystal, GnomeCrystal, SylphCrystal, UndineCrystal, SalamanderCrystal };
        // for (Material material : ores) {
        // MarimorphosisRecipeBuilder.builder("living_rock_" + material.getName().toLowerCase())
        // .input(vazkii.botania.common.BotaniaBlocks.getBlock("livingrock"))
        // .output(ChemicalHelper.getBlock(GTOTagPrefix.LIVING_STONE, material))
        // .weight(10)
        // .biomeTag(BiomeTags.IS_FOREST)
        // .save();
        // }
        //
        // Block[] stones = {
        // Blocks.NETHERRACK,
        // Blocks.END_STONE,
        // ModBlocks.MOON_STONE.get(),
        // ModBlocks.MARS_STONE.get(),
        // ModBlocks.VENUS_STONE.get(),
        // ModBlocks.MERCURY_STONE.get(),
        // GTOBlocks.CERES_STONE.get(),
        // GTOBlocks.IO_STONE.get(),
        // GTOBlocks.GANYMEDE_STONE.get(),
        // GTOBlocks.ENCELADUS_STONE.get(),
        // GTOBlocks.TITAN_STONE.get(),
        // GTOBlocks.PLUTO_STONE.get(),
        // ModBlocks.GLACIO_STONE.get(),
        // DDBlocks.SCULK_STONE.get(),
        // };
        //
        // int[] stones_weights = { 1, 1, 1, 1, 1, 2, 4, 8, 20, 50, 150, 500, 2000, 8000, 2000, 500, 150, 50, 20, 8, 4,
        // 2, 1, 1, 1, 1, 1, };
        //
        // for (int i = 0; i < 12; i++) {
        // for (int j = 0; j < 14; j++) {
        // MarimorphosisRecipeBuilder.builder("star_stone_" + i + "_" + stones[j].getName())
        // .input(GTOBlocks.STAR_STONE[i].get())
        // .output(stones[j])
        // .weight(stones_weights[13 - i + j])
        // .biomeTag(BiomeTags.IS_OVERWORLD)
        // .save();
        // }
        // }
        //
        // }
        //
        // // 祭坛锻造
        // {
        // PedestalRecipeBuilder.builder("heros_soul")
        // .input(vazkii.botania.common.ExtraBotanyItems.getItem("hero_medal"))
        // .output(GTOItems.HEROS_SOUL.asStack(4))
        // .smashTools(ExtraBotanyTags.Items.HAMMERS)
        // .strike(20)
        // .exp(10)
        // .save();
        // }
        //
        // // 精灵交易
        // {
        // ElfExchangeRecipe("elf_quartz", new ItemStack(Items.QUARTZ, 4), new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("quartz_elven"), 4));
        // ElfExchangeRecipe("elf_glass", new ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("mana_glass"), 4),
        // new
        // ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("elf_glass"), 4));
        // ElfExchangeRecipe("dreamwood_log", new
        // ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("livingwood_log"), 4),
        // new ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("dreamwood_log"), 4));
        // ElfExchangeRecipe("dreamwood", new ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("livingwood"), 4),
        // new
        // ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("dreamwood"), 4));
        // ElfExchangeRecipe("elementium", new ItemStack(vazkii.botania.common.BotaniaItems.getItem("manasteel_ingot"),
        // 8), new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("elementium_ingot"), 4));
        // ElfExchangeRecipe("elementium_block", new
        // ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("manasteel_block"),
        // 8), new ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("elementium_block"), 4));
        // ElfExchangeRecipe("pixie_dust", new ItemStack(vazkii.botania.common.BotaniaItems.getItem("mana_pearl"), 4),
        // new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("pixie_dust"), 4));
        // ElfExchangeRecipe("dragonstone", new ItemStack(ItemsRegistry.SOURCE_GEM, 4), new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("dragonstone"), 4));
        // ElfExchangeRecipe("dragonstone_block", new ItemStack(BlockRegistry.SOURCE_GEM_BLOCK, 4), new
        // ItemStack(vazkii.botania.common.BotaniaBlocks.getBlock("dragonstone_block"), 4));
        //
        // // 额外植物学
        // ElfExchangeRecipe("elementium_quartz", new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("quartz_mana"), 8), new
        // ItemStack(vazkii.botania.common.ExtraBotanyItems.getItem("elementium_quartz"), 4));
        //
        // // GTO
        // ElfExchangeRecipe("colorful_mystical_flower", new
        // ItemStack(vazkii.botania.common.BotaniaItems.getItem("fertilizer"),
        // 4), new ItemStack(COLORFUL_MYSTICAL_FLOWER, 16));
        // }
        //
        // 神话植物学 符文仪式：诗之蜜酒
        {
            List<RuneRitualRecipe.RunePosition> runes = new ArrayList<>();
            Item ancientMaterial = Adventure.Items.ANCIENT_MATERIAL.get();

            // 远古材料：边长 3 的方框
            for (int i : new int[] { -3, -1, 1 }) {
                addRune(runes, ancientMaterial, i, 3);
                addRune(runes, ancientMaterial, -i, -3);
                addRune(runes, ancientMaterial, 3, -i);
                addRune(runes, ancientMaterial, -3, i);
            }
            // 蜜脾块：边长 2 的方框
            for (int i = -1; i <= 1; i++) {
                addRune(runes, Items.HONEYCOMB_BLOCK, i, 2);
                addRune(runes, Items.HONEYCOMB_BLOCK, i, -2);
                addRune(runes, Items.HONEYCOMB_BLOCK, 2, i);
                addRune(runes, Items.HONEYCOMB_BLOCK, -2, i);
            }
            // 蜂蜜块：边长 4 的方框
            for (int i = -3; i <= 3; i++) {
                addRune(runes, Items.HONEY_BLOCK, i, 4);
                addRune(runes, Items.HONEY_BLOCK, i, -4);
                addRune(runes, Items.HONEY_BLOCK, 4, i);
                addRune(runes, Items.HONEY_BLOCK, -4, i);
            }
            // 植物魔法符文
            Item[] runeItem1 = {
                    BotaniaItems.RUNE_EARTH.get(), BotaniaItems.RUNE_AIR.get(), BotaniaItems.RUNE_FIRE.get(),
                    BotaniaItems.RUNE_WATER.get(), BotaniaItems.RUNE_SPRING.get(), BotaniaItems.RUNE_SUMMER.get(),
                    BotaniaItems.RUNE_AUTUMN.get(), BotaniaItems.RUNE_WINTER.get(), BotaniaItems.RUNE_MANA.get(),
                    BotaniaItems.RUNE_LUST.get(), BotaniaItems.RUNE_GLUTTONY.get(), BotaniaItems.RUNE_GREED.get(),
                    BotaniaItems.RUNE_SLOTH.get(), BotaniaItems.RUNE_WRATH.get(), BotaniaItems.RUNE_ENVY.get(),
                    BotaniaItems.RUNE_PRIDE.get()
            };
            int k = -4;
            for (int i = 0; i < 8; i++) {
                addRune(runes, runeItem1[i], k, 5);
                addRune(runes, runeItem1[i], -k, -5);
                addRune(runes, runeItem1[i + 8], 5, -k);
                addRune(runes, runeItem1[i + 8], -5, k);
                k++;
                if (k == 0) k++;
            }
            // 神话植物学符文
            Item[] runeItem2 = {
                    MythicBotanyItems.MB_ASGARD_RUNE.get(), MythicBotanyItems.MB_VANAHEIM_RUNE.get(), MythicBotanyItems.MB_ALFHEIM_RUNE.get(),
                    MythicBotanyItems.MB_JOETUNHEIM_RUNE.get(), MythicBotanyItems.MB_MUSPELHEIM_RUNE.get(), MythicBotanyItems.MB_NIFLHEIM_RUNE.get(),
                    MythicBotanyItems.MB_NIDAVELLIR_RUNE.get(), MythicBotanyItems.MB_HELHEIM_RUNE.get(), MythicBotanyItems.MB_MIDGARD_RUNE.get()
            };
            k = -2;
            for (int i = 0; i < 3; i++) {
                addRune(runes, runeItem2[i], k, 3);
                addRune(runes, runeItem2[i], -k, -3);
                addRune(runes, runeItem2[i + 3], 3, -k);
                addRune(runes, runeItem2[i + 3], -3, k);
                k += 2;
            }
            addRune(runes, runeItem2[6], 0, 1);
            addRune(runes, runeItem2[6], 0, -1);
            addRune(runes, runeItem2[7], 1, 0);
            addRune(runes, runeItem2[7], -1, 0);
            addRune(runes, runeItem2[8], 2, -2);
            addRune(runes, runeItem2[8], -2, 2);
            // 星辰石与符文石
            for (int i : new int[] { 5, 4, 2, 1 }) {
                addRune(runes, GTOBlocks.STAR_STONE[i].get().asItem(), i, i);
                addRune(runes, GTOBlocks.STAR_STONE[i].get().asItem(), -i, -i);
            }
            Item runerock = ChemicalHelper.getBlock(block, Runerock).asItem();
            for (int i : new int[] { 5, 4, 1 }) {
                addRune(runes, runerock, -i, i);
                addRune(runes, runerock, i, -i);
            }
            // 元素精华
            addRune(runes, ItemsRegistry.EARTH_ESSENCE.get(), 0, -5);
            addRune(runes, ItemsRegistry.AIR_ESSENCE.get(), 0, 5);
            addRune(runes, ItemsRegistry.WATER_ESSENCE.get(), -5, 0);
            addRune(runes, ItemsRegistry.FIRE_ESSENCE.get(), 5, 0);

            List<Ingredient> inputs = List.of(
                    Ingredient.of(ChemicalHelper.getItem(gemFlawless, OriginCoreCrystal)),
                    Ingredient.of(ChemicalHelper.getItem(gemFlawless, StarBloodCrystal)),
                    Ingredient.of(GTOItems.PHILOSOPHERS_STONE.get()),
                    Ingredient.of(ChemicalHelper.getItem(gemFlawless, SoulJadeCrystal)),
                    Ingredient.of(ChemicalHelper.getItem(gemFlawless, RemnantSpiritStone)));
            RuneRitualRecipe mead = new RuneRitualRecipe(
                    new ResourceLocation("mythicbotany", "mythicbotany_rune_rituals/kvasir_mead"), "",
                    Ingredient.of(MythicBotanyItems.MB_KVASIR_BLOOD.get()), runes,
                    10_000_000, 18_000, inputs,
                    List.of(MythicBotanyItems.MB_KVASIR_MEAD.asStack()), null, null);
            GTRecipes.RECIPE_MAP.put(mead.getId(), mead);
        }

        /////////////////////////////////////
        // ********** GT机器配方 ********** //
        /////////////////////////////////////

        // 魔力灌注 - 魔力池
        {
            // === 基础配方 ===//
            InfusionManaPoolRecipe("manasteel", ChemicalHelper.getItem(ingot, Steel), new ItemStack(BotaniaItems.getItem("manasteel_ingot")), 3000, null, null);
            InfusionManaPoolRecipe("manasteel_block", ChemicalHelper.getItem(block, Steel), new ItemStack(BotaniaBlocks.getBlock("manasteel_block")), 27000, null, null);
            InfusionManaPoolRecipe("mana_pearl", RegistriesUtils.getItem("torchmaster:frozen_pearl"), new ItemStack(BotaniaItems.getItem("mana_pearl")), 6000, null, null);
            InfusionManaPoolRecipe("mana_diamond", Items.DIAMOND, new ItemStack(BotaniaItems.getItem("mana_diamond")), 10000, null, null);
            InfusionManaPoolRecipe("mana_diamond_block", Items.DIAMOND_BLOCK, new ItemStack(BotaniaBlocks.getBlock("mana_diamond_block")), 90000, null, null);
            InfusionManaPoolRecipe("piston_relay", Items.PISTON, new ItemStack(Items.HONEY_BOTTLE), 15000, null, null);
            InfusionManaPoolRecipe("mana_cookie", Items.COOKIE, new ItemStack(BotaniaItems.getItem("mana_cookie")), 20000, null, null);
            InfusionManaPoolRecipe("grass_seeds", Items.GRASS, new ItemStack(BotaniaItems.getItem("grass_seeds")), 2500, null, null);
            InfusionManaPoolRecipe("podzol_seeds", Items.DEAD_BUSH, new ItemStack(BotaniaItems.getItem("podzol_seeds")), 2500, null, null);
            InfusionManaPoolRecipe("mycel_seeds", Items.RED_MUSHROOM, new ItemStack(BotaniaItems.getItem("mycelium_seeds")), 6500, null, null);
            InfusionManaPoolRecipe("mana_quartz", Items.QUARTZ, new ItemStack(BotaniaItems.getItem("quartz_mana")), 250, null, null);
            InfusionManaPoolRecipe("tiny_potato", Items.POTATO, new ItemStack(BotaniaBlocks.getBlock("tiny_potato")), 1337, null, null);
            InfusionManaPoolRecipe("mana_glass", Items.GLASS, new ItemStack(BotaniaBlocks.getBlock("mana_glass")), 150, null, null);
            InfusionManaPoolRecipe("mana_string", ItemsRegistry.MAGE_FIBER.asItem(), new ItemStack(BotaniaItems.getItem("mana_string")), 1250, null, null);
            InfusionManaPoolRecipe("mana_bottle", Items.GLASS_BOTTLE, new ItemStack(BotaniaItems.getItem("mana_bottle")), 5000, null, null);
            InfusionManaPoolRecipe("hydroangeas_motif", BotaniaBlocks.getBlock("hydroangeas").asItem(), new ItemStack(BotaniaBlocks.getBlock("hydroangeas_motif")), 2500, null, null);

            // === 炼药配方 ===//
            InfusionManaPoolRecipe("rotten_flesh_to_leather", Items.ROTTEN_FLESH, new ItemStack(Items.LEATHER), 600, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("chiseled_stone_bricks", Items.STONE_BRICKS, new ItemStack(Blocks.CHISELED_STONE_BRICKS), 150, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("ice", Items.SNOW_BLOCK, new ItemStack(Blocks.ICE), 2250, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("vine_to_lily_pad", Items.VINE, new ItemStack(Blocks.LILY_PAD), 320, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:vine_and_lily_pad_cycle");
            InfusionManaPoolRecipe("lily_pad_to_vine", Items.LILY_PAD, new ItemStack(Blocks.VINE), 320, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:vine_and_lily_pad_cycle");
            InfusionManaPoolRecipe("potato_unpoison", Items.POISONOUS_POTATO, new ItemStack(Items.POTATO), 1200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("blaze_rod_to_nether_wart", Items.BLAZE_ROD, new ItemStack(Items.NETHER_WART), 4000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("book_to_name_tag", Items.WRITABLE_BOOK, new ItemStack(Items.NAME_TAG), 6000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("wool_deconstruct", Items.WHITE_WOOL, new ItemStack(Items.STRING, 3), 100, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("cactus_to_slime", Items.CACTUS, new ItemStack(Items.SLIME_BALL), 1200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:cactus_and_slime_cycle");
            InfusionManaPoolRecipe("slime_to_cactus", Items.SLIME_BALL, new ItemStack(Blocks.CACTUS), 1200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:cactus_and_slime_cycle");
            InfusionManaPoolRecipe("ender_pearl_from_ghast_tear", Items.GHAST_TEAR, new ItemStack(Items.ENDER_PEARL), 28000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("cobble_to_sand", Items.COBBLESTONE, new ItemStack(Blocks.SAND), 50, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("terracotta_to_red_sand", Items.TERRACOTTA, new ItemStack(Blocks.RED_SAND), 50, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("coarse_dirt", Items.DIRT, new ItemStack(Blocks.COARSE_DIRT), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("soul_soil", Items.SOUL_SAND, new ItemStack(Blocks.SOUL_SOIL), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("stone_to_andesite", Items.STONE, new ItemStack(Blocks.ANDESITE), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("dripleaf_shrinking", Items.BIG_DRIPLEAF, new ItemStack(Blocks.SMALL_DRIPLEAF), 500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);
            InfusionManaPoolRecipe("chorus_fruit_to_flower", Items.POPPED_CHORUS_FRUIT, new ItemStack(Blocks.CHORUS_FLOWER), 10000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), null);

            // === 方块分解 ===//
            InfusionManaPoolRecipe("glowstone_deconstruct", Items.GLOWSTONE, new ItemStack(Items.GLOWSTONE_DUST, 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("quartz_deconstruct", Items.QUARTZ_BLOCK, new ItemStack(Items.QUARTZ, 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("dark_quartz_deconstruct", BotaniaBlocks.getBlock("dark_quartz").asItem(), new ItemStack(BotaniaItems.getItem("quartz_dark"), 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("mana_quartz_deconstruct", BotaniaBlocks.getBlock("mana_quartz").asItem(), new ItemStack(BotaniaItems.getItem("quartz_mana"), 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("blaze_quartz_deconstruct", BotaniaBlocks.getBlock("blaze_quartz").asItem(), new ItemStack(BotaniaItems.getItem("quartz_blaze"), 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("lavender_quartz_deconstruct", BotaniaBlocks.getBlock("lavender_quartz").asItem(), new ItemStack(BotaniaItems.getItem("quartz_lavender"), 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("red_quartz_deconstruct", BotaniaBlocks.getBlock("red_quartz").asItem(), new ItemStack(BotaniaItems.getItem("quartz_red"), 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("elf_quartz_deconstruct", BotaniaBlocks.getBlock("elf_quartz").asItem(), new ItemStack(BotaniaItems.getItem("quartz_elven"), 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("sunny_quartz_deconstruct", BotaniaBlocks.getBlock("sunny_quartz").asItem(), new ItemStack(BotaniaItems.getItem("quartz_sunny"), 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("clay_deconstruct", Items.CLAY, new ItemStack(Items.CLAY_BALL, 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");
            InfusionManaPoolRecipe("brick_deconstruct", Items.BRICKS, new ItemStack(Items.BRICK, 4), 25, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:block_deconstruction");

            // === 循环配方 ===//
            // 原木循环 (log_cycle)
            InfusionManaPoolRecipe("oak_log_to_spruce_log", Items.OAK_LOG, new ItemStack(Blocks.SPRUCE_LOG), 40, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:log_cycle");
            InfusionManaPoolRecipe("spruce_log_to_birch_log", Items.SPRUCE_LOG, new ItemStack(Blocks.BIRCH_LOG), 40, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:log_cycle");
            InfusionManaPoolRecipe("birch_log_to_jungle_log", Items.BIRCH_LOG, new ItemStack(Blocks.JUNGLE_LOG), 40, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:log_cycle");
            InfusionManaPoolRecipe("jungle_log_to_acacia_log", Items.JUNGLE_LOG, new ItemStack(Blocks.ACACIA_LOG), 40, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:log_cycle");
            InfusionManaPoolRecipe("acacia_log_to_dark_oak_log", Items.ACACIA_LOG, new ItemStack(Blocks.DARK_OAK_LOG), 40, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:log_cycle");
            InfusionManaPoolRecipe("dark_oak_log_to_mangrove_log", Items.DARK_OAK_LOG, new ItemStack(Blocks.MANGROVE_LOG), 40, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:log_cycle");
            InfusionManaPoolRecipe("mangrove_log_to_cherry_log", Items.MANGROVE_LOG, new ItemStack(Blocks.CHERRY_LOG), 40, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:log_cycle");
            InfusionManaPoolRecipe("cherry_log_to_oak_log", Items.CHERRY_LOG, new ItemStack(Blocks.OAK_LOG), 40, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:log_cycle");

            // 青蛙灯循环 (froglight_cycle)
            InfusionManaPoolRecipe("ochre_froglight_to_verdant_froglight", Items.OCHRE_FROGLIGHT, new ItemStack(Blocks.VERDANT_FROGLIGHT), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:froglight_cycle");
            InfusionManaPoolRecipe("verdant_froglight_to_pearlescent_froglight", Items.VERDANT_FROGLIGHT, new ItemStack(Blocks.PEARLESCENT_FROGLIGHT), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:froglight_cycle");
            InfusionManaPoolRecipe("pearlescent_froglight_to_ochre_froglight", Items.PEARLESCENT_FROGLIGHT, new ItemStack(Blocks.OCHRE_FROGLIGHT), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:froglight_cycle");

            // 树苗循环（sapling_cycle，共7种+红树和樱花）
            InfusionManaPoolRecipe("oak_sapling_to_spruce_sapling", Items.OAK_SAPLING, new ItemStack(Blocks.SPRUCE_SAPLING), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:sapling_cycle");
            InfusionManaPoolRecipe("spruce_sapling_to_birch_sapling", Items.SPRUCE_SAPLING, new ItemStack(Blocks.BIRCH_SAPLING), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:sapling_cycle");
            InfusionManaPoolRecipe("birch_sapling_to_jungle_sapling", Items.BIRCH_SAPLING, new ItemStack(Blocks.JUNGLE_SAPLING), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:sapling_cycle");
            InfusionManaPoolRecipe("jungle_sapling_to_acacia_sapling", Items.JUNGLE_SAPLING, new ItemStack(Blocks.ACACIA_SAPLING), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:sapling_cycle");
            InfusionManaPoolRecipe("acacia_sapling_to_dark_oak_sapling", Items.ACACIA_SAPLING, new ItemStack(Blocks.DARK_OAK_SAPLING), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:sapling_cycle");
            InfusionManaPoolRecipe("dark_oak_sapling_to_mangrove_propagule", Items.DARK_OAK_SAPLING, new ItemStack(Blocks.MANGROVE_PROPAGULE), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:sapling_cycle");
            InfusionManaPoolRecipe("mangrove_propagule_to_cherry_sapling", Items.MANGROVE_PROPAGULE, new ItemStack(Blocks.CHERRY_SAPLING), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:sapling_cycle");
            InfusionManaPoolRecipe("cherry_sapling_to_oak_sapling", Items.CHERRY_SAPLING, new ItemStack(Blocks.OAK_SAPLING), 120, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:sapling_cycle");

            // 鱼类循环
            InfusionManaPoolRecipe("cod_to_salmon", Items.COD, new ItemStack(Items.SALMON), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:fish_cycle");
            InfusionManaPoolRecipe("salmon_to_tropical_fish", Items.SALMON, new ItemStack(Items.TROPICAL_FISH), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:fish_cycle");
            InfusionManaPoolRecipe("tropical_fish_to_pufferfish", Items.TROPICAL_FISH, new ItemStack(Items.PUFFERFISH), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:fish_cycle");
            InfusionManaPoolRecipe("pufferfish_to_cod", Items.PUFFERFISH, new ItemStack(Items.COD), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:fish_cycle");

            // 石头循环（stone_cycle）
            InfusionManaPoolRecipe("diorite_to_granite", Items.DIORITE, new ItemStack(Blocks.GRANITE), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:stone_cycle");
            InfusionManaPoolRecipe("granite_to_andesite", Items.GRANITE, new ItemStack(Blocks.ANDESITE), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:stone_cycle");
            InfusionManaPoolRecipe("andesite_to_diorite", Items.ANDESITE, new ItemStack(Blocks.DIORITE), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:stone_cycle");

            // 1.17石头循环（117_stone_cycle）
            InfusionManaPoolRecipe("tuff_to_calcite", Items.TUFF, new ItemStack(Blocks.CALCITE), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:117_stone_cycle");
            InfusionManaPoolRecipe("calcite_to_deepslate", Items.CALCITE, new ItemStack(Blocks.DEEPSLATE), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:117_stone_cycle");
            InfusionManaPoolRecipe("deepslate_to_tuff", Items.DEEPSLATE, new ItemStack(Blocks.TUFF), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:117_stone_cycle");

            // 花循环（flower_cycle）
            InfusionManaPoolRecipe("dandelion_to_poppy", Items.DANDELION, new ItemStack(Blocks.POPPY), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("poppy_to_blue_orchid", Items.POPPY, new ItemStack(Blocks.BLUE_ORCHID), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("blue_orchid_to_allium", Items.BLUE_ORCHID, new ItemStack(Blocks.ALLIUM), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("allium_to_azure_bluet", Items.ALLIUM, new ItemStack(Blocks.AZURE_BLUET), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("azure_bluet_to_red_tulip", Items.AZURE_BLUET, new ItemStack(Blocks.RED_TULIP), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("red_tulip_to_orange_tulip", Items.RED_TULIP, new ItemStack(Blocks.ORANGE_TULIP), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("orange_tulip_to_white_tulip", Items.ORANGE_TULIP, new ItemStack(Blocks.WHITE_TULIP), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("white_tulip_to_pink_tulip", Items.WHITE_TULIP, new ItemStack(Blocks.PINK_TULIP), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("pink_tulip_to_oxeye_daisy", Items.PINK_TULIP, new ItemStack(Blocks.OXEYE_DAISY), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("oxeye_daisy_to_cornflower", Items.OXEYE_DAISY, new ItemStack(Blocks.CORNFLOWER), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("cornflower_to_lily_of_the_valley", Items.CORNFLOWER, new ItemStack(Blocks.LILY_OF_THE_VALLEY), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("lily_of_the_valley_to_sunflower", Items.LILY_OF_THE_VALLEY, new ItemStack(Blocks.SUNFLOWER), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("sunflower_to_lilac", Items.SUNFLOWER, new ItemStack(Blocks.LILAC), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("lilac_to_rose_bush", Items.LILAC, new ItemStack(Blocks.ROSE_BUSH), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("rose_bush_to_peony", Items.ROSE_BUSH, new ItemStack(Blocks.PEONY), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");
            InfusionManaPoolRecipe("peony_to_dandelion", Items.PEONY, new ItemStack(Blocks.DANDELION), 400, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_cycle");

            // 种子循环（crop_cycle）
            InfusionManaPoolRecipe("cocoa_beans_to_wheat_seeds", Items.COCOA_BEANS, new ItemStack(Items.WHEAT_SEEDS), 6000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:crop_cycle");
            InfusionManaPoolRecipe("wheat_seeds_to_potato", Items.WHEAT_SEEDS, new ItemStack(Items.POTATO), 6000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:crop_cycle");
            InfusionManaPoolRecipe("potato_to_carrot", Items.POTATO, new ItemStack(Items.CARROT), 6000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:crop_cycle");
            InfusionManaPoolRecipe("carrot_to_beetroot_seeds", Items.CARROT, new ItemStack(Items.BEETROOT_SEEDS), 6000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:crop_cycle");
            InfusionManaPoolRecipe("beetroot_seeds_to_melon_seeds", Items.BEETROOT_SEEDS, new ItemStack(Items.MELON_SEEDS), 6000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:crop_cycle");
            InfusionManaPoolRecipe("melon_seeds_to_pumpkin_seeds", Items.MELON_SEEDS, new ItemStack(Items.PUMPKIN_SEEDS), 6000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:crop_cycle");
            InfusionManaPoolRecipe("pumpkin_seeds_to_cocoa_beans", Items.PUMPKIN_SEEDS, new ItemStack(Items.COCOA_BEANS), 6000, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:crop_cycle");

            // 萤石红石循环（glowstone_and_redstone_cycle）
            InfusionManaPoolRecipe("redstone_to_glowstone_dust", Items.REDSTONE, new ItemStack(Items.GLOWSTONE_DUST), 300, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:glowstone_and_redstone_cycle");
            InfusionManaPoolRecipe("glowstone_dust_to_redstone", Items.GLOWSTONE_DUST, new ItemStack(Items.REDSTONE), 300, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:glowstone_and_redstone_cycle");

            // 灌木循环（shrub_cycle）
            InfusionManaPoolRecipe("fern_to_dead_bush", Items.FERN, new ItemStack(Blocks.DEAD_BUSH), 500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:shrub_cycle");
            InfusionManaPoolRecipe("dead_bush_to_grass", Items.DEAD_BUSH, new ItemStack(Blocks.GRASS), 500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:shrub_cycle");
            InfusionManaPoolRecipe("grass_to_fern", Items.GRASS, new ItemStack(Blocks.FERN), 500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:shrub_cycle");

            // 浆果循环（berry_cycle）
            InfusionManaPoolRecipe("apple_to_sweet_berries", Items.APPLE, new ItemStack(Items.SWEET_BERRIES), 240, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:berry_cycle");
            InfusionManaPoolRecipe("sweet_berries_to_glow_berries", Items.SWEET_BERRIES, new ItemStack(Items.GLOW_BERRIES), 240, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:berry_cycle");
            InfusionManaPoolRecipe("glow_berries_to_apple", Items.GLOW_BERRIES, new ItemStack(Items.APPLE), 240, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:berry_cycle");

            // 火药和燧石循环（gunpowder_and_flint_cycle）
            InfusionManaPoolRecipe("gunpowder_to_flint", Items.GUNPOWDER, new ItemStack(Items.FLINT), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "gunpowder_and_flint_cycle");
            InfusionManaPoolRecipe("flint_to_gunpowder", Items.FLINT, new ItemStack(Items.GUNPOWDER), 200, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "gunpowder_and_flint_cycle");

            // === 迷你花配方 ===//
            InfusionManaPoolRecipe("agricarnation_chibi", BotaniaBlocks.getBlock("agricarnation").asItem(), new ItemStack(BotaniaBlocks.getBlock("agricarnation_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("clayconia_chibi", BotaniaBlocks.getBlock("clayconia").asItem(), new ItemStack(BotaniaBlocks.getBlock("clayconia_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("bellethorn_chibi", BotaniaBlocks.getBlock("bellethorn").asItem(), new ItemStack(BotaniaBlocks.getBlock("bellethorn_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("bubbell_chibi", BotaniaBlocks.getBlock("bubbell").asItem(), new ItemStack(BotaniaBlocks.getBlock("bubbell_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("hopperhock_chibi", BotaniaBlocks.getBlock("hopperhock").asItem(), new ItemStack(BotaniaBlocks.getBlock("hopperhock_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("jiyuulia_chibi", BotaniaBlocks.getBlock("jiyuulia").asItem(), new ItemStack(BotaniaBlocks.getBlock("jiyuulia_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("tangleberrie_chibi", BotaniaBlocks.getBlock("tangleberrie").asItem(), new ItemStack(BotaniaBlocks.getBlock("tangleberrie_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("marimorphosis_chibi", BotaniaBlocks.getBlock("marimorphosis").asItem(), new ItemStack(BotaniaBlocks.getBlock("marimorphosis_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("rannuncarpus_chibi", BotaniaBlocks.getBlock("rannuncarpus").asItem(), new ItemStack(BotaniaBlocks.getBlock("rannuncarpus_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");
            InfusionManaPoolRecipe("solegnolia_chibi", BotaniaBlocks.getBlock("solegnolia").asItem(), new ItemStack(BotaniaBlocks.getBlock("solegnolia_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");

            // === 复制配方 ===//
            InfusionManaPoolRecipe("redstone_dupe", Items.REDSTONE, new ItemStack(Items.REDSTONE, 2), 5000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("glowstone_dupe", Items.GLOWSTONE_DUST, new ItemStack(Items.GLOWSTONE_DUST, 2), 5000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("quartz_dupe", Items.QUARTZ, new ItemStack(Items.QUARTZ, 2), 2500, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("coal_dupe", Items.COAL, new ItemStack(Items.COAL, 2), 2100, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("snowball_dupe", Items.SNOWBALL, new ItemStack(Items.SNOWBALL, 2), 200, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("netherrack_dupe", Items.NETHERRACK, new ItemStack(Blocks.NETHERRACK, 2), 200, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("soul_sand_dupe", Items.SOUL_SAND, new ItemStack(Blocks.SOUL_SAND, 2), 1500, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("gravel_dupe", Items.GRAVEL, new ItemStack(Blocks.GRAVEL, 2), 720, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("grass_dupe", Items.GRASS, new ItemStack(Blocks.GRASS, 2), 800, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);

            // 树叶复制
            InfusionManaPoolRecipe("oak_leaves_dupe", Items.OAK_LEAVES, new ItemStack(Blocks.OAK_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("birch_leaves_dupe", Items.BIRCH_LEAVES, new ItemStack(Blocks.BIRCH_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("spruce_leaves_dupe", Items.SPRUCE_LEAVES, new ItemStack(Blocks.SPRUCE_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("jungle_leaves_dupe", Items.JUNGLE_LEAVES, new ItemStack(Blocks.JUNGLE_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("acacia_leaves_dupe", Items.ACACIA_LEAVES, new ItemStack(Blocks.ACACIA_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("dark_oak_leaves_dupe", Items.DARK_OAK_LEAVES, new ItemStack(Blocks.DARK_OAK_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("azalea_leaves_dupe", Items.AZALEA_LEAVES, new ItemStack(Blocks.AZALEA_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("flowering_azalea_leaves_dupe", Items.FLOWERING_AZALEA_LEAVES, new ItemStack(Blocks.FLOWERING_AZALEA_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("mangrove_leaves_dupe", Items.MANGROVE_LEAVES, new ItemStack(Blocks.MANGROVE_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);
            InfusionManaPoolRecipe("cherry_leaves_dupe", Items.CHERRY_LEAVES, new ItemStack(Blocks.CHERRY_LEAVES, 2), 2000, BotaniaBlocks.getBlock("conjuration_catalyst").asItem(), null);

            // 神话植物学
            InfusionManaPoolRecipe("gjallar_horn_empty", BotaniaItems.getItem("horn_grass"), new ItemStack(MythicBotanyItems.getItem("gjallar_horn_empty")), 20000, null, null);

            // 额外植物学
            InfusionManaPoolRecipe("nightmare_fuel", Items.COAL, new ItemStack(ExtraBotanyItems.getItem("nightmare_fuel")), 2000, null, null);
            InfusionManaPoolRecipe("fried_chicken", Items.COOKED_CHICKEN, new ItemStack(ExtraBotanyItems.getItem("fried_chicken")), 600, null, null);
            InfusionManaPoolRecipe("snowball_to_ender_pearl", Items.SNOWBALL, new ItemStack(Items.ENDER_PEARL), 2000, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("diamond_horse_armor_to_shulker_shell", Items.DIAMOND_HORSE_ARMOR, new ItemStack(Items.SHULKER_SHELL), 20000, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("apple_to_chorus_fruit", Items.APPLE, new ItemStack(Items.CHORUS_FRUIT), 500, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("stone_to_end_stone", Items.STONE, new ItemStack(Items.END_STONE), 500, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("cobblestone_to_nether_rack", Items.COBBLESTONE, new ItemStack(Items.NETHERRACK), 500, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("sand_to_soul_sand", Items.SAND, new ItemStack(Items.SOUL_SAND), 500, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("iron_ore_to_quartz_ore", Items.IRON_ORE, new ItemStack(Items.NETHER_QUARTZ_ORE), 2000, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("blaze_rod_dupe", Items.BLAZE_ROD, new ItemStack(Items.BLAZE_ROD, 2), 2000, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("nether_star_to_totem_of_undying", Items.NETHER_STAR, new ItemStack(Items.TOTEM_OF_UNDYING), 50000, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("the_origin_to_elytra", ExtraBotanyItems.getItem("the_origin"), new ItemStack(Items.ELYTRA), 50000, ExtraBotanyBlocks.getBlock("dimension_catalyst").asItem(), null);
            InfusionManaPoolRecipe("necrofleur_chibi", ExtraBotanyBlocks.getBlock("necrofleur").asItem(), new ItemStack(ExtraBotanyBlocks.getBlock("necrofleur_chibi")), 2500, BotaniaBlocks.getBlock("alchemy_catalyst").asItem(), "botania:flower_shrinking");

            // GTO配方
            InfusionManaPoolRecipe("pulsating", ChemicalHelper.getItem(SUPERCONDUCTOR_BASE, PulsatingAlloy), new ItemStack(ChemicalHelper.getItem(wireGtSingle, PulsatingAlloy)), 400, null, null);
            InfusionManaPoolRecipe("conductivee", ChemicalHelper.getItem(SUPERCONDUCTOR_BASE, ConductiveAlloy), new ItemStack(ChemicalHelper.getItem(wireGtSingle, ConductiveAlloy)), 1600, null, null);
            InfusionManaPoolRecipe("energeticalloy", ChemicalHelper.getItem(SUPERCONDUCTOR_BASE, EnergeticAlloy), new ItemStack(ChemicalHelper.getItem(wireGtSingle, EnergeticAlloy)), 6400, null, null);
            InfusionManaPoolRecipe("vibrantalloy", ChemicalHelper.getItem(SUPERCONDUCTOR_BASE, VibrantAlloy), new ItemStack(ChemicalHelper.getItem(wireGtSingle, VibrantAlloy)), 25600, null, null);
            InfusionManaPoolRecipe("endsteel", ChemicalHelper.getItem(SUPERCONDUCTOR_BASE, EndSteel), new ItemStack(ChemicalHelper.getItem(wireGtSingle, EndSteel)), 102400, null, null);

            InfusionManaPoolRecipe("infused_gold", Items.GOLD_INGOT, new ItemStack(ChemicalHelper.getItem(ingot, InfusedGold)), 8000, null, null);
            InfusionManaPoolRecipe("original_bronze_dust", ChemicalHelper.getItem(ingot, Bronze), new ItemStack(ChemicalHelper.getItem(ingot, OriginalBronze)), 6000, null, null);

            InfusionManaPoolRecipe("life_essence", GTOItems.UNSTABLE_GAIA_SOUL.asItem(), new ItemStack(BotaniaItems.getItem("life_essence")), 500000, ChemicalHelper.getItem(block, Gaia), null);

            InfusionManaPoolRecipe("mana_powder_dust", TagUtils.createTGItemTag("dusts"), new ItemStack(BotaniaItems.getItem("mana_powder")), 20, null, null);
            InfusionManaPoolRecipe("mana_powder_dye", Tags.Items.DYES, new ItemStack(BotaniaItems.getItem("mana_powder")), 20, null, null);
        }

        // 白雏菊（魔力灌注机 2 号电路）
        {
            InfusionPureDaisyRecipe("livingclay", Items.DIRT, ChemicalHelper.getItem(block, Livingclay));
            InfusionPureDaisyRecipe("livingrock", Items.STONE, BotaniaBlocks.getBlock("livingrock").asItem());
            InfusionPureDaisyRecipe("livingwood", RecipeDatagen.ARCHWOOD_LOG, BotaniaBlocks.getBlock("livingwood_log").asItem());

            InfusionPureDaisyRecipe("cobblestone", Items.NETHERRACK, Items.COBBLESTONE);
            InfusionPureDaisyRecipe("end_stone_to_cobbled_deepslate", Items.END_STONE, Items.COBBLED_DEEPSLATE);
            InfusionPureDaisyRecipe("sand", Items.SOUL_SAND, Items.SAND);
            InfusionPureDaisyRecipe("packed_ice", Items.ICE, Items.PACKED_ICE);
            InfusionPureDaisyRecipe("blue_ice", Items.PACKED_ICE, Items.BLUE_ICE);
            InfusionPureDaisyRecipe("obsidian", BotaniaBlocks.getBlock("blaze_block").asItem(), Items.OBSIDIAN);

        }

        // 符文祭坛（工业祭坛 1 号电路）
        {
            IndustrialAltarRecipe1("water_rune", 5200, new ItemStack(BotaniaItems.getItem("rune_water"), 2), new Item[] { BotaniaItems.getItem("mana_powder"), BotaniaItems.getItem("manasteel_ingot"), Items.BONE_MEAL, Items.SUGAR_CANE, Items.FISHING_ROD });
            IndustrialAltarRecipe1("fire_rune", 5200, new ItemStack(BotaniaItems.getItem("rune_fire"), 2), new Item[] { BotaniaItems.getItem("mana_powder"), BotaniaItems.getItem("manasteel_ingot"), Items.GUNPOWDER, Items.NETHER_BRICK, Items.NETHER_WART });
            IndustrialAltarRecipe1("earth_rune", 5200, new ItemStack(BotaniaItems.getItem("rune_earth"), 2), new Item[] { BotaniaItems.getItem("mana_powder"), BotaniaItems.getItem("manasteel_ingot"), Items.COAL_BLOCK, Items.STONE, Items.RED_MUSHROOM });
            IndustrialAltarRecipe1("air_rune", 5200, new ItemStack(BotaniaItems.getItem("rune_air"), 2), new Item[] { BotaniaItems.getItem("mana_powder"), BotaniaItems.getItem("manasteel_ingot"), Items.WHITE_CARPET, Items.FEATHER, Items.STRING });

            IndustrialAltarRecipe1("spring_rune", 8000, new ItemStack(BotaniaItems.getItem("rune_spring")), new Item[] { BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_fire"), Items.OAK_SAPLING, Items.OAK_SAPLING, Items.OAK_SAPLING, Items.WHEAT });
            IndustrialAltarRecipe1("summer_rune", 8000, new ItemStack(BotaniaItems.getItem("rune_summer")), new Item[] { BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_air"), Items.SAND, Items.SAND, Items.SLIME_BALL, Items.MELON_SLICE });
            IndustrialAltarRecipe1("autumn_rune", 8000, new ItemStack(BotaniaItems.getItem("rune_autumn")), new Item[] { BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_air"), Items.OAK_LEAVES, Items.OAK_LEAVES, Items.OAK_LEAVES, Items.SPIDER_EYE });
            IndustrialAltarRecipe1("winter_rune", 8000, new ItemStack(BotaniaItems.getItem("rune_winter")), new Item[] { BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_earth"), Items.SNOW_BLOCK, Items.SNOW_BLOCK, Items.WHITE_WOOL, Items.CAKE });

            IndustrialAltarRecipe1("mana_rune", 8000, new ItemStack(BotaniaItems.getItem("rune_mana")), new Item[] { BotaniaItems.getItem("manasteel_ingot"), BotaniaItems.getItem("manasteel_ingot"), BotaniaItems.getItem("manasteel_ingot"), BotaniaItems.getItem("manasteel_ingot"), BotaniaItems.getItem("manasteel_ingot"), BotaniaItems.getItem("mana_pearl") });

            IndustrialAltarRecipe1("lust_rune", 12000, new ItemStack(BotaniaItems.getItem("rune_lust")), new Item[] { BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("mana_diamond"), BotaniaItems.getItem("mana_diamond") });
            IndustrialAltarRecipe1("gluttony_rune", 12000, new ItemStack(BotaniaItems.getItem("rune_gluttony")), new Item[] { BotaniaItems.getItem("rune_winter"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("mana_diamond"), BotaniaItems.getItem("mana_diamond") });
            IndustrialAltarRecipe1("greed_rune", 12000, new ItemStack(BotaniaItems.getItem("rune_greed")), new Item[] { BotaniaItems.getItem("rune_spring"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("mana_diamond"), BotaniaItems.getItem("mana_diamond") });
            IndustrialAltarRecipe1("sloth_rune", 12000, new ItemStack(BotaniaItems.getItem("rune_sloth")), new Item[] { BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("mana_diamond"), BotaniaItems.getItem("mana_diamond") });
            IndustrialAltarRecipe1("wrath_rune", 12000, new ItemStack(BotaniaItems.getItem("rune_wrath")), new Item[] { BotaniaItems.getItem("rune_winter"), BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("mana_diamond"), BotaniaItems.getItem("mana_diamond") });
            IndustrialAltarRecipe1("envy_rune", 12000, new ItemStack(BotaniaItems.getItem("rune_envy")), new Item[] { BotaniaItems.getItem("rune_winter"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("mana_diamond"), BotaniaItems.getItem("mana_diamond") });
            IndustrialAltarRecipe1("pride_rune", 12000, new ItemStack(BotaniaItems.getItem("rune_pride")), new Item[] { BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("mana_diamond"), BotaniaItems.getItem("mana_diamond") });

            // 神话植物学
            IndustrialAltarRecipe1("midgar_runed", 16000, new ItemStack(MythicBotanyItems.getItem("midgard_rune")), new Item[] { BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_spring"), BotaniaItems.getItem("rune_greed"), Items.GRASS_BLOCK, BotaniaItems.getItem("manasteel_ingot") });
            IndustrialAltarRecipe1("alfheim_rune", 16000, new ItemStack(MythicBotanyItems.getItem("alfheim_rune")), new Item[] { BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("rune_lust"), Items.OAK_LEAVES, BotaniaItems.getItem("elementium_ingot") });
            IndustrialAltarRecipe1("muspelheim_rune", 16000, new ItemStack(MythicBotanyItems.getItem("muspelheim_rune")), new Item[] { BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("rune_wrath"), Items.MAGMA_BLOCK, Items.NETHER_BRICK });
            IndustrialAltarRecipe1("niflheim_rune", 16000, new ItemStack(MythicBotanyItems.getItem("niflheim_rune")), new Item[] { BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_winter"), BotaniaItems.getItem("rune_wrath"), Items.BLUE_ICE, Items.IRON_INGOT });
            IndustrialAltarRecipe1("asgard_rune", 16000, new ItemStack(MythicBotanyItems.getItem("asgard_rune")), new Item[] { BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("rune_pride"), BotaniaItems.getItem("rainbow_rod"), Items.NETHERITE_INGOT });
            IndustrialAltarRecipe1("vanaheim_rune", 16000, new ItemStack(MythicBotanyItems.getItem("vanaheim_rune")), new Item[] { BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_spring"), BotaniaItems.getItem("rune_pride"), BotaniaBlocks.getBlock("alfheim_portal").asItem(), BotaniaItems.getItem("terrasteel_ingot") });
            IndustrialAltarRecipe1("helheim_rune", 16000, new ItemStack(MythicBotanyItems.getItem("helheim_rune")), new Item[] { BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("rune_envy"), Items.SKELETON_SKULL, Items.GOLD_INGOT });
            IndustrialAltarRecipe1("nidavellir_rune", 16000, new ItemStack(MythicBotanyItems.getItem("nidavellir_rune")), new Item[] { BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_winter"), BotaniaItems.getItem("rune_sloth"), Items.IRON_BLOCK, Items.COPPER_INGOT });
            IndustrialAltarRecipe1("joetunheim_rune", 16000, new ItemStack(MythicBotanyItems.getItem("joetunheim_rune")), new Item[] { BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("rune_gluttony"), Items.BLACKSTONE, Items.BLACKSTONE });

            // 额外植物学
            IndustrialAltarRecipe2("zadkiel", 500000, new ItemStack(ExtraBotanyItems.getItem("zadkiel")), new Item[] { Items.ICE, Items.BLUE_ICE, Items.PACKED_ICE, Items.SNOW_BLOCK, Items.POWDER_SNOW_BUCKET, Items.TOTEM_OF_UNDYING });
            IndustrialAltarRecipe2("orichalcos_ingot", 150000, new ItemStack(ExtraBotanyItems.getItem("orichalcos_ingot"), 2), new Item[] { ExtraBotanyItems.getItem("hero_medal"), ExtraBotanyItems.getItem("gilded_potato_mashed"), BotaniaItems.getItem("gaia_ingot"), BotaniaItems.getItem("gaia_ingot"), BotaniaItems.getItem("life_essence"), BotaniaItems.getItem("life_essence"), BotaniaItems.getItem("life_essence"), BotaniaItems.getItem("life_essence") });
            IndustrialAltarRecipe2("shadowium_ingot", 4200, new ItemStack(ExtraBotanyItems.getItem("shadowium_ingot"), 2), new Item[] { BotaniaItems.getItem("elementium_ingot"), ExtraBotanyItems.getItem("gilded_potato_mashed"), ExtraBotanyItems.getItem("nightmare_fuel"), ExtraBotanyItems.getItem("nightmare_fuel"), ExtraBotanyItems.getItem("nightmare_fuel") });
            IndustrialAltarRecipe2("photonium_ingot", 4200, new ItemStack(ExtraBotanyItems.getItem("photonium_ingot"), 2), new Item[] { BotaniaItems.getItem("elementium_ingot"), ExtraBotanyItems.getItem("gilded_potato_mashed"), ExtraBotanyItems.getItem("spirit_fragment"), ExtraBotanyItems.getItem("spirit_fragment"), ExtraBotanyItems.getItem("spirit_fragment") });
            IndustrialAltarRecipe2("gilded_potato", 800, new ItemStack(ExtraBotanyItems.getItem("gilded_potato")), new Item[] { Items.POTATO, Items.GOLD_NUGGET });
            IndustrialAltarRecipe2("orichalcos_hammer", 1000000, new ItemStack(Items.HONEY_BOTTLE), new Item[] { ExtraBotanyItems.getItem("orichalcos_ingot"), ExtraBotanyItems.getItem("gilded_potato_mashed"), ExtraBotanyItems.getItem("the_chaos"), ExtraBotanyItems.getItem("the_origin"), ExtraBotanyItems.getItem("the_end") });

            IndustrialAltarRecipe2("runerock_block", 1000000, ChemicalHelper.get(block, Runerock, 8), new Item[] { BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_spring"), BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("rune_winter"), BotaniaItems.getItem("rune_mana"), BotaniaItems.getItem("rune_lust"), BotaniaItems.getItem("rune_gluttony"), BotaniaItems.getItem("rune_greed"), BotaniaItems.getItem("rune_sloth"), BotaniaItems.getItem("rune_wrath"), BotaniaItems.getItem("rune_envy"), BotaniaItems.getItem("rune_pride"), });
            IndustrialAltarRecipe2("runerock_block_plas", 1000000, ChemicalHelper.get(block, Runerock, 16), new Item[] { MythicBotanyItems.getItem("asgard_rune"), MythicBotanyItems.getItem("vanaheim_rune"), MythicBotanyItems.getItem("alfheim_rune"), MythicBotanyItems.getItem("midgard_rune"), MythicBotanyItems.getItem("joetunheim_rune"), MythicBotanyItems.getItem("muspelheim_rune"), MythicBotanyItems.getItem("niflheim_rune"), MythicBotanyItems.getItem("nidavellir_rune"), MythicBotanyItems.getItem("helheim_rune") });

            IndustrialAltarRecipe2("transmutation_catalyst", 50000000, new ItemStack(GTOBlocks.TRANSMUTATION_CATALYST), new Item[] { GTOItems.PHILOSOPHERS_STONE.asItem(), GTOItems.PHILOSOPHERS_STONE.asItem(), GTOItems.PHILOSOPHERS_STONE.asItem(), GTOItems.PHILOSOPHERS_STONE.asItem(), GTOItems.PHILOSOPHERS_STONE.asItem(), GTOItems.PHILOSOPHERS_STONE.asItem() });
        }

        // 花药台（工业祭坛 2/3/4/5 号电路）
        {
            IndustrialAltarRecipe3(3, "pure_daisy_flower", new ItemStack(BotaniaBlocks.getBlock("pure_daisy")), new Item[] { BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal") });
            IndustrialAltarRecipe3(3, "manastar_flower", new ItemStack(BotaniaBlocks.getBlock("manastar")), new Item[] { BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("cyan_petal") });
            IndustrialAltarRecipe3(3, "endoflame_flower", new ItemStack(BotaniaBlocks.getBlock("endoflame")), new Item[] { BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("light_gray_petal") });
            IndustrialAltarRecipe3(3, "hydroangea_flowers", new ItemStack(BotaniaBlocks.getBlock("hydroangeas")), new Item[] { BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal") });
            IndustrialAltarRecipe3(3, "thermalily_flower", new ItemStack(BotaniaBlocks.getBlock("thermalily")), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_fire") });
            IndustrialAltarRecipe3(3, "rosa_arcana_flower", new ItemStack(BotaniaBlocks.getBlock("rosa_arcana")), new Item[] { BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("rune_mana") });
            IndustrialAltarRecipe3(3, "munchdew_flower", new ItemStack(BotaniaBlocks.getBlock("munchdew")), new Item[] { BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("rune_gluttony") });
            IndustrialAltarRecipe3(4, "entropinnyum_flower", new ItemStack(BotaniaBlocks.getBlock("entropinnyum")), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("rune_wrath"), BotaniaItems.getItem("rune_fire") });
            IndustrialAltarRecipe3(4, "kekimurus_flower", new ItemStack(BotaniaBlocks.getBlock("kekimurus")), new Item[] { BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("rune_gluttony"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(3, "gourmaryllis_flower", new ItemStack(BotaniaBlocks.getBlock("gourmaryllis")), new Item[] { BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_summer") });
            IndustrialAltarRecipe3(3, "narslimmus_flower", new ItemStack(BotaniaBlocks.getBlock("narslimmus")), new Item[] { BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("rune_water") });
            IndustrialAltarRecipe3(4, "spectrolus_flower", new ItemStack(BotaniaBlocks.getBlock("spectrolus")), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("rune_winter"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(3, "rafflowsia_flower", new ItemStack(BotaniaBlocks.getBlock("rafflowsia")), new Item[] { BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_pride"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(3, "shulk_me_not_flower", new ItemStack(BotaniaBlocks.getBlock("shulk_me_not")), new Item[] { BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("life_essence"), BotaniaItems.getItem("rune_envy"), BotaniaItems.getItem("rune_wrath") });
            IndustrialAltarRecipe3(3, "dandelifeon_flower", new ItemStack(BotaniaBlocks.getBlock("dandelifeon")), new Item[] { BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("redstone_root"), BotaniaItems.getItem("life_essence") });
            IndustrialAltarRecipe3(3, "jaded_amaranthus_flower", new ItemStack(BotaniaBlocks.getBlock("jaded_amaranthus")), new Item[] { BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("rune_spring"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "bellethorn_flower", new ItemStack(BotaniaBlocks.getBlock("bellethorn")), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "dreadthorn_flower", new ItemStack(BotaniaBlocks.getBlock("dreadthorn")), new Item[] { BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "heisei_dream_flower", new ItemStack(BotaniaBlocks.getBlock("heisei_dream")), new Item[] { BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("rune_wrath"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(3, "tigerseye_flower", new ItemStack(BotaniaBlocks.getBlock("tigerseye")), new Item[] { BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("rune_autumn") });
            IndustrialAltarRecipe3(3, "orechid_flower", new ItemStack(BotaniaBlocks.getBlock("orechid")), new Item[] { BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("rune_pride"), BotaniaItems.getItem("rune_greed"), BotaniaItems.getItem("redstone_root"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(4, "orechid_ignem_flower", new ItemStack(BotaniaBlocks.getBlock("orechid_ignem")), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("rune_pride"), BotaniaItems.getItem("rune_greed"), BotaniaItems.getItem("redstone_root"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(4, "fallen_kanade_flower", new ItemStack(BotaniaBlocks.getBlock("fallen_kanade")), new Item[] { BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("rune_spring") });
            IndustrialAltarRecipe3(3, "exoflame_flower", new ItemStack(BotaniaBlocks.getBlock("exoflame")), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_summer") });
            IndustrialAltarRecipe3(3, "agricarnation_flower", new ItemStack(BotaniaBlocks.getBlock("agricarnation")), new Item[] { BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("rune_spring"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "hopperhock_flower", new ItemStack(BotaniaBlocks.getBlock("hopperhock")), new Item[] { BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(4, "tangleberrie_flower", new ItemStack(BotaniaBlocks.getBlock("tangleberrie")), new Item[] { BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("rune_earth") });
            IndustrialAltarRecipe3(3, "jiyuulia_flower", new ItemStack(BotaniaBlocks.getBlock("jiyuulia")), new Item[] { BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_air") });
            IndustrialAltarRecipe3(3, "rannuncarpus_flower", new ItemStack(BotaniaBlocks.getBlock("rannuncarpus")), new Item[] { BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "hyacidu_flowers", new ItemStack(BotaniaBlocks.getBlock("hyacidus")), new Item[] { BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "pollidisiac_flower", new ItemStack(BotaniaBlocks.getBlock("pollidisiac")), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("rune_lust"), BotaniaItems.getItem("rune_fire") });
            IndustrialAltarRecipe3(3, "clayconia_flower", new ItemStack(BotaniaBlocks.getBlock("clayconia")), new Item[] { BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("rune_earth") });
            IndustrialAltarRecipe3(3, "loonium_flower", new ItemStack(BotaniaBlocks.getBlock("loonium")), new Item[] { BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("rune_sloth"), BotaniaItems.getItem("rune_gluttony"), BotaniaItems.getItem("rune_envy"), BotaniaItems.getItem("redstone_root"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(4, "daffomill_flower", new ItemStack(BotaniaBlocks.getBlock("daffomill")), new Item[] { BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "vinculotus_flower", new ItemStack(BotaniaBlocks.getBlock("vinculotus")), new Item[] { BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_sloth"), BotaniaItems.getItem("rune_lust"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(4, "spectranthemum_flower", new ItemStack(BotaniaBlocks.getBlock("spectranthemum")), new Item[] { BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("rune_envy"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("redstone_root"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(3, "medumone_flower", new ItemStack(BotaniaBlocks.getBlock("medumone")), new Item[] { BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "marimorphosis_flower", new ItemStack(BotaniaBlocks.getBlock("marimorphosis")), new Item[] { BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(4, "bubbell_flower", new ItemStack(BotaniaBlocks.getBlock("bubbell")), new Item[] { BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(3, "solegnolia_flower", new ItemStack(BotaniaBlocks.getBlock("solegnolia")), new Item[] { BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "bergamute_flower", new ItemStack(BotaniaBlocks.getBlock("bergamute")), new Item[] { BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(4, "labellia_flower", new ItemStack(BotaniaBlocks.getBlock("labellia")), new Item[] { BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("redstone_root"), BotaniaItems.getItem("pixie_dust") });
            IndustrialAltarRecipe3(3, "motif_daybloom_flower", new ItemStack(BotaniaBlocks.getBlock("daybloom_motif")), new Item[] { BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("light_blue_petal") });
            IndustrialAltarRecipe3(3, "motif_nightshade_flower", new ItemStack(BotaniaBlocks.getBlock("nightshade_motif")), new Item[] { BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("gray_petal") });

            // 神话植物学
            IndustrialAltarRecipe3(3, "exoblaze_flower", MythicBotanyBlocks.getBlock("exoblaze").asItem().getDefaultInstance(), new Item[] { BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("rune_fire"), Items.BLAZE_POWDER });
            IndustrialAltarRecipe3(3, "wither_aconite_flower", MythicBotanyBlocks.getBlock("wither_aconite").asItem().getDefaultInstance(), new Item[] { BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("rune_pride"), Blocks.WITHER_ROSE.asItem() });
            IndustrialAltarRecipe3(4, "aquapanthus_flower", MythicBotanyBlocks.getBlock("aquapanthus").asItem().getDefaultInstance(), new Item[] { BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("cyan_petal") });
            IndustrialAltarRecipe3(3, "hellebore_flower", MythicBotanyBlocks.getBlock("hellebore").asItem().getDefaultInstance(), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("rune_fire") });
            IndustrialAltarRecipe3(4, "raindeletia_flower", MythicBotanyBlocks.getBlock("raindeletia").asItem().getDefaultInstance(), new Item[] { BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("rune_water") });
            IndustrialAltarRecipe3(3, "petrunia_flower", MythicBotanyBlocks.getBlock("petrunia").asItem().getDefaultInstance(), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("brown_petal"), MythicBotanyItems.getItem("gjallar_horn_full"), BotaniaItems.getItem("phantom_ink") });

            // 额外植物学
            IndustrialAltarRecipe3(3, "trade_orchid", new ItemStack(ExtraBotanyBlocks.getBlock("trade_orchid")), new Item[] { BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("rune_greed"), BotaniaItems.getItem("rune_lust"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(3, "woodienia", new ItemStack(ExtraBotanyBlocks.getBlock("woodienia")), new Item[] { BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("gray_petal"), ExtraBotanyItems.getItem("elementium_quartz"), BotaniaItems.getItem("rune_gluttony"), BotaniaItems.getItem("redstone_root") });
            IndustrialAltarRecipe3(4, "reikarlily", new ItemStack(ExtraBotanyBlocks.getBlock("reikarlily")), new Item[] { BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("rune_pride"), BotaniaItems.getItem("rune_envy"), BotaniaItems.getItem("rune_sloth"), BotaniaItems.getItem("life_essence") });
            IndustrialAltarRecipe3(3, "bellflower", new ItemStack(ExtraBotanyBlocks.getBlock("bellflower")), new Item[] { BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("lime_petal"), ExtraBotanyItems.getItem("spirit_fragment") });
            IndustrialAltarRecipe3(4, "annoyingflower", new ItemStack(ExtraBotanyBlocks.getBlock("annoyingflower")), new Item[] { BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("rune_mana"), ExtraBotanyItems.getItem("spirit_fragment") });
            IndustrialAltarRecipe3(3, "stonesia", new ItemStack(ExtraBotanyBlocks.getBlock("stonesia")), new Item[] { BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("life_essence"), BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("rune_gluttony") });
            IndustrialAltarRecipe3(4, "edelweiss", new ItemStack(ExtraBotanyBlocks.getBlock("edelweiss")), new Item[] { BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("mana_powder"), BotaniaItems.getItem("rune_mana"), BotaniaItems.getItem("rune_winter") });
            IndustrialAltarRecipe3(3, "resoncund", new ItemStack(ExtraBotanyBlocks.getBlock("resoncund")), new Item[] { BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("rune_lust"), BotaniaItems.getItem("rune_gluttony") });
            IndustrialAltarRecipe3(5, "sunshine_lily", new ItemStack(ExtraBotanyBlocks.getBlock("sunshine_lily")), new Item[] { BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("orange_petal") });
            IndustrialAltarRecipe3(4, "moonlight_lily", new ItemStack(ExtraBotanyBlocks.getBlock("moonlight_lily")), new Item[] { BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("gray_petal") });
            IndustrialAltarRecipe3(3, "serenitian", new ItemStack(ExtraBotanyBlocks.getBlock("serenitian")), new Item[] { BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("rune_mana"), BotaniaItems.getItem("rune_sloth"), BotaniaItems.getItem("rune_greed"), BotaniaItems.getItem("life_essence"), Items.WITHER_ROSE });
            IndustrialAltarRecipe3(3, "twinstar", new ItemStack(ExtraBotanyBlocks.getBlock("twinstar")), new Item[] { BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("mana_powder"), BotaniaItems.getItem("mana_powder") });
            IndustrialAltarRecipe3(3, "omniviolet", new ItemStack(ExtraBotanyBlocks.getBlock("omniviolet")), new Item[] { BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("rune_spring"), BotaniaItems.getItem("rune_mana"), BotaniaItems.getItem("rune_lust") });
            IndustrialAltarRecipe3(4, "tinkle", new ItemStack(ExtraBotanyBlocks.getBlock("tinkle")), new Item[] { BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("rune_water"), BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("mana_powder"), ExtraBotanyItems.getItem("spirit_fragment"), ExtraBotanyItems.getItem("spirit_fragment") });
            IndustrialAltarRecipe3(3, "blood_enchantress", new ItemStack(ExtraBotanyBlocks.getBlock("blood_enchantress")), new Item[] { BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("rune_wrath") });
            IndustrialAltarRecipe3(4, "mirrowtunia", new ItemStack(ExtraBotanyBlocks.getBlock("mirrowtunia")), new Item[] { BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("rune_wrath"), BotaniaItems.getItem("rune_pride"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("mana_powder") });
            IndustrialAltarRecipe3(3, "manalink", new ItemStack(ExtraBotanyBlocks.getBlock("manalink")), new Item[] { BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("rune_sloth"), BotaniaItems.getItem("rune_lust"), BotaniaItems.getItem("life_essence") });
            IndustrialAltarRecipe3(3, "necrofleur", new ItemStack(ExtraBotanyBlocks.getBlock("necrofleur")), new Item[] { BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("pink_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("rune_wrath"), BotaniaItems.getItem("mana_powder") });
            IndustrialAltarRecipe3(3, "enchanter", new ItemStack(ExtraBotanyBlocks.getBlock("enchanter")), new Item[] { BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("rune_pride"), BotaniaItems.getItem("rune_greed"), BotaniaItems.getItem("rune_gluttony"), BotaniaItems.getItem("life_essence") });

            // GTO 多彩神秘花瓣
            IndustrialAltarRecipe3(6, "colorful_mystical_flower", new ItemStack(COLORFUL_MYSTICAL_FLOWER), new Item[] { BotaniaItems.getItem("white_petal"), BotaniaItems.getItem("light_gray_petal"), BotaniaItems.getItem("gray_petal"), BotaniaItems.getItem("black_petal"), BotaniaItems.getItem("brown_petal"), BotaniaItems.getItem("red_petal"), BotaniaItems.getItem("orange_petal"), BotaniaItems.getItem("yellow_petal"), BotaniaItems.getItem("lime_petal"), BotaniaItems.getItem("green_petal"), BotaniaItems.getItem("cyan_petal"), BotaniaItems.getItem("light_blue_petal"), BotaniaItems.getItem("blue_petal"), BotaniaItems.getItem("purple_petal"), BotaniaItems.getItem("magenta_petal"), BotaniaItems.getItem("pink_petal") });
        }

        // 锻造祭坛（工业祭坛 6 号电路）
        {
            PedestalSmashRecipe("gilded_potato_mashed", 180, Ingredient.of(ExtraBotanyTags.Items.HAMMERS), new ItemStack(ExtraBotanyItems.getItem("gilded_potato")), new ItemStack(ExtraBotanyItems.getItem("gilded_potato_mashed")));
            PedestalSmashRecipe("spirit_fragment", 180, Ingredient.of(ExtraBotanyTags.Items.HAMMERS), new ItemStack(ExtraBotanyItems.getItem("spirit_fuel")), new ItemStack(ExtraBotanyItems.getItem("spirit_fragment")));
            PedestalSmashRecipe("heros_soul", 360, Ingredient.of(ExtraBotanyTags.Items.HAMMERS), new ItemStack(ExtraBotanyItems.getItem("hero_medal")), new ItemStack(GTOItems.HEROS_SOUL, 4));
        }

        // 凝聚板
        {
            TERRA_PLATE_RECIPES.builder("terrasteel_ingot")
                    .inputItems(ingot, GTOMaterials.Manasteel)
                    .inputItems("botania:mana_pearl")
                    .inputItems(gem, GTOMaterials.ManaDiamond)
                    .outputItems(ingot, GTOMaterials.Terrasteel)
                    .MANAt(500)
                    .duration(500000 / 500)
                    .save();

            TERRA_PLATE_RECIPES.builder("thaumium_ingot")
                    .inputItems(ingot, Livingsteel)
                    .inputItems(ItemsRegistry.SOURCE_GEM)
                    .inputItems(ingot, OriginalBronze)
                    .inputItems(ItemsRegistry.MANIPULATION_ESSENCE)
                    .outputItems(ingot, GTOMaterials.Thaumium)
                    .MANAt(500)
                    .duration(500000 / 500)
                    .save();

            TERRA_PLATE_RECIPES.builder("alfsteel_ingot")
                    .inputItems(BotaniaItems.getItem("elementium_ingot"))
                    .inputItems(BotaniaItems.getItem("pixie_dust"))
                    .inputItems(BotaniaItems.getItem("dragonstone"))
                    .outputItems(ingot, Alfsteel)
                    .MANAt(500)
                    .duration(1500000 / 500)
                    .save();

            TERRA_PLATE_RECIPES.builder("gaiasteel_ingot")
                    .inputItems(MythicBotanyItems.getItem("asgard_rune"))
                    .inputItems(MythicBotanyItems.getItem("vanaheim_rune"))
                    .inputItems(MythicBotanyItems.getItem("alfheim_rune"))
                    .inputItems(MythicBotanyItems.getItem("midgard_rune"))
                    .inputItems(MythicBotanyItems.getItem("joetunheim_rune"))
                    .inputItems(MythicBotanyItems.getItem("muspelheim_rune"))
                    .inputItems(MythicBotanyItems.getItem("niflheim_rune"))
                    .inputItems(MythicBotanyItems.getItem("nidavellir_rune"))
                    .inputItems(MythicBotanyItems.getItem("helheim_rune"))
                    .inputItems(ingot, Alfsteel, 3)
                    .inputItems(ingot, Runerock, 3)
                    .outputItems(ingot, Gaiasteel, 3)
                    .MANAt(500)
                    .duration(2500000 / 500)
                    .save();

            TERRA_PLATE_RECIPES.builder("gaia_ingot")
                    .inputItems(ingot, Gaiasteel, 2)
                    .inputItems(BotaniaItems.getItem("life_essence"), 2)
                    .outputItems(ingot, Gaia, 2)
                    .MANAt(500)
                    .duration(5000000 / 500)
                    .save();

            TERRA_PLATE_RECIPES.builder("aerialite_ingot")
                    .inputItems(BotaniaItems.getItem("ender_air_bottle"), 2)
                    .inputItems(gem, Dragonstone, 2)
                    .inputItems(Items.PHANTOM_MEMBRANE.asItem(), 2)
                    .notConsumableFluid(GTOMaterials.Aerialite, 144)
                    .outputItems(ingot, Aerialite, 3)
                    .MANAt(500)
                    .duration(500000 / 500)
                    .save();

            TERRA_PLATE_RECIPES.builder("the_universe")
                    .inputItems(ExtraBotanyItems.getItem("the_chaos"), 2)
                    .inputItems(ExtraBotanyItems.getItem("the_origin"), 2)
                    .inputItems(ExtraBotanyItems.getItem("the_end"), 2)
                    .outputItems(ExtraBotanyItems.getItem("the_universe"), 3)
                    .MANAt(500)
                    .duration(500000 / 500)
                    .save();
        }

        // 多方块特供配方
        {
            // 魔力灌注
            MANA_INFUSER_RECIPES.builder("bifrost_perm")
                    .notConsumable("botania:rainbow_rod")
                    .inputItems(BotaniaBlocks.getBlock("elf_glass"), 16)
                    .outputItems(BotaniaBlocks.getBlock("bifrost_perm"), 16)
                    .duration(200)
                    .MANAt(1)
                    .save();

            MANA_INFUSER_RECIPES.builder("gjallar_horn_full")
                    .notConsumable(MythicBotanyBlocks.getBlock("yggdrasil_branch").asItem())
                    .inputItems(MythicBotanyItems.getItem("gjallar_horn_empty"), 16)
                    .outputItems(MythicBotanyItems.getItem("gjallar_horn_full"), 16)
                    .duration(400)
                    .MANAt(240)
                    .save();

            // 魔力凝聚
            {
                String[] EndremEyes = {
                        "black_eye", "cold_eye",
                        "nether_eye", "old_eye", "rogue_eye", "cursed_eye",
                        "guardian_eye", "magical_eye"
                };
                String[] EndremEyes_input = {
                        "minecraft:sculk_catalyst", "ad_astra:ice_shard",
                        "botania:quartz_blaze", "botania:forest_eye", "botania:redstone_root", "botania:life_essence",
                        "minecraft:prismarine_crystals", "botania:mana_bottle"
                };

                for (int i = 0; i < EndremEyes.length; i++) {
                    MANA_CONDENSER_RECIPES.builder(EndremEyes[i])
                            .inputItems(GTItems.QUANTUM_EYE)
                            .inputItems(EndremEyes_input[i])
                            .outputItems("endrem:" + EndremEyes[i])
                            .duration(200)
                            .MANAt(128)
                            .save();
                }

                MANA_CONDENSER_RECIPES.builder("enriched_naquadah_trinium_europium_duranide")
                        .inputItems(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 4)
                        .outputItems(TagPrefix.wireGtSingle, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 4)
                        .inputFluids(GTOMaterials.Aether, 1000)
                        .duration(80)
                        .MANAt(2048)
                        .save();

                MANA_CONDENSER_RECIPES.builder("ruthenium_trinium_americium_neutronate")
                        .inputItems(GTOTagPrefix.SUPERCONDUCTOR_BASE, GTMaterials.RutheniumTriniumAmericiumNeutronate, 4)
                        .outputItems(TagPrefix.wireGtSingle, GTMaterials.RutheniumTriniumAmericiumNeutronate, 4)
                        .inputFluids(GTOMaterials.Aether, 1000)
                        .duration(80)
                        .MANAt(8192)
                        .save();

                MANA_CONDENSER_RECIPES.builder("nether_star")
                        .chancedInput(MythicBotanyItems.getItem("helheim_rune"), Chance.of(9500).dropPerOverclock(100))
                        .inputItems(BotaniaItems.getItem("quartz_elven"), 4)
                        .inputItems(dust, GTMaterials.Iridium)
                        .outputItems(gem, GTMaterials.NetherStar, 2)
                        .inputFluids(GTMaterials.NetherAir, 4000)
                        .duration(200)
                        .MANAt(512)
                        .save();

                MANA_CONDENSER_RECIPES.builder("terrasteel_ingot")
                        .inputItems(ingot, GTOMaterials.Manasteel, 2)
                        .inputItems("botania:mana_pearl", 2)
                        .inputItems(gem, GTOMaterials.ManaDiamond, 2)
                        .outputItems(ingot, GTOMaterials.Terrasteel, 3)
                        .notConsumableFluid(GTOMaterials.Terrasteel, 144)
                        .MANAt(512)
                        .duration(400)
                        .save();

                MANA_CONDENSER_RECIPES.builder("thaumium_ingot")
                        .inputItems(ingot, Livingsteel, 2)
                        .inputItems(ItemsRegistry.SOURCE_GEM, 2)
                        .inputItems(ingot, OriginalBronze, 2)
                        .inputItems(ItemsRegistry.MANIPULATION_ESSENCE, 2)
                        .outputItems(ingot, GTOMaterials.Thaumium, 3)
                        .notConsumableFluid(GTOMaterials.Thaumium, 144)
                        .MANAt(512)
                        .duration(400)
                        .save();

                MANA_CONDENSER_RECIPES.builder("alfsteel_ingot")
                        .inputItems(BotaniaItems.getItem("elementium_ingot"), 2)
                        .inputItems(BotaniaItems.getItem("pixie_dust"), 2)
                        .inputItems(BotaniaItems.getItem("dragonstone"), 2)
                        .outputItems(ingot, Alfsteel, 3)
                        .notConsumableFluid(GTOMaterials.Alfsteel, 144)
                        .MANAt(1536)
                        .duration(400)
                        .save();

                MANA_CONDENSER_RECIPES.builder("gaiasteel_ingot")
                        .inputItems(MythicBotanyItems.getItem("asgard_rune"), 2)
                        .inputItems(MythicBotanyItems.getItem("vanaheim_rune"), 2)
                        .inputItems(MythicBotanyItems.getItem("alfheim_rune"), 2)
                        .inputItems(MythicBotanyItems.getItem("midgard_rune"), 2)
                        .inputItems(MythicBotanyItems.getItem("joetunheim_rune"), 2)
                        .inputItems(MythicBotanyItems.getItem("muspelheim_rune"), 2)
                        .inputItems(MythicBotanyItems.getItem("niflheim_rune"), 2)
                        .inputItems(MythicBotanyItems.getItem("nidavellir_rune"), 2)
                        .inputItems(MythicBotanyItems.getItem("helheim_rune"), 2)
                        .inputItems(ingot, Alfsteel, 6)
                        .inputItems(ingot, Runerock, 6)
                        .outputItems(ingot, Gaiasteel, 9)
                        .notConsumableFluid(GTOMaterials.Gaiasteel, 576)
                        .MANAt(2560)
                        .duration(400)
                        .save();

                MANA_CONDENSER_RECIPES.builder("gaia_ingot")
                        .inputItems(ingot, Gaiasteel, 4)
                        .inputItems(BotaniaItems.getItem("life_essence"), 4)
                        .outputItems(ingot, Gaia, 6)
                        .notConsumableFluid(GTOMaterials.Gaia, 288)
                        .MANAt(5120)
                        .duration(400)
                        .save();

                MANA_CONDENSER_RECIPES.builder("aerialite_ingot")
                        .inputItems(BotaniaItems.getItem("ender_air_bottle"), 2)
                        .inputItems(gem, Dragonstone, 2)
                        .inputItems(Items.PHANTOM_MEMBRANE.asItem(), 2)
                        .notConsumableFluid(GTOMaterials.Aerialite, 144)
                        .outputItems(ingot, Aerialite, 3)
                        .MANAt(256)
                        .duration(200)
                        .save();

                MANA_CONDENSER_RECIPES.builder("the_universe")
                        .inputItems(ExtraBotanyItems.getItem("the_chaos"), 2)
                        .inputItems(ExtraBotanyItems.getItem("the_origin"), 2)
                        .inputItems(ExtraBotanyItems.getItem("the_end"), 2)
                        .outputItems(ExtraBotanyItems.getItem("the_universe"), 3)
                        .MANAt(512)
                        .duration(200)
                        .save();
            }

            // 精灵门：兑换袋 / 贤者之石
            {
                ElfExchangeRecipe(1, "pandoras_box", 20000, 100, new ItemStack(ExtraBotanyItems.getItem("hero_medal"), 2), new ItemStack(ExtraBotanyItems.getItem("pandoras_box")));
                ElfExchangeRecipe(2, "nine_and_three_quarters", 10000, 100, new ItemStack(ExtraBotanyItems.getItem("hero_medal")), new ItemStack(ExtraBotanyItems.getItem("nine_and_three_quarters_reward_bag"), 40));
                // 价值 4000 100 1 10 80 80(大概？)
                ElfExchangeRecipe(3, "eins", 2000, 100, new ItemStack(ExtraBotanyItems.getItem("hero_medal")), new ItemStack(ExtraBotanyItems.getItem("eins_reward_bag"), 4000));
                ElfExchangeRecipe(4, "zwei", 4000, 100, new ItemStack(ExtraBotanyItems.getItem("hero_medal")), new ItemStack(ExtraBotanyItems.getItem("zwei_reward_bag"), 400));
                ElfExchangeRecipe(5, "drei", 6000, 100, new ItemStack(ExtraBotanyItems.getItem("hero_medal")), new ItemStack(ExtraBotanyItems.getItem("drei_reward_bag"), 50));
                ElfExchangeRecipe(6, "vier", 6000, 100, new ItemStack(ExtraBotanyItems.getItem("hero_medal")), new ItemStack(ExtraBotanyItems.getItem("vier_reward_bag"), 50));

                ElfExchangeRecipe(7, "philosophers_stone", 20000, 600, new ItemStack(ExtraBotanyItems.getItem("hero_medal"), 32), new ItemStack(GTOItems.PHILOSOPHERS_STONE));
            }

            // 符文支托与大师符文支托
            {
                VanillaRecipeHelper.addShapedRecipe(new ResourceLocation("mythicbotany", "rune_holder"),
                        com.gtocore.common.data.machines.MythicBotanyMachines.RUNE_HOLDER.asStack(),
                        " w ", "wdw", 'w', Tags.Items.INGOTS_IRON, 'd', BotaniaItems.getItem("mana_powder"));
                VanillaRecipeHelper.addShapedRecipe(new ResourceLocation("mythicbotany", "central_rune_holder"),
                        com.gtocore.common.data.machines.MythicBotanyMachines.CENTRAL_RUNE_HOLDER.asStack(),
                        " w ", "wdw", 'w', Tags.Items.GEMS_EMERALD, 'd', BotaniaItems.getItem("mana_powder"));
            }
        }
    }

    /** 符文仪式的一个符文位：物品、坐标，仪式完成后消耗。 */
    private static void addRune(List<RuneRitualRecipe.RunePosition> runes, Item rune, int x, int z) {
        runes.add(new RuneRitualRecipe.RunePosition(Ingredient.of(rune), x, z, true));
    }

    // private static void PureDaisyRecipe(
    // String id,
    // StateIngredient input,
    // Block output) {
    // PureDaisyRecipeBuilder.builder(id)
    // .input(input)
    // .output(output)
    // .save();
    // }
    //
    // private static void ManaInfusionRecipe(
    // String id,
    // Ingredient input,
    // ItemStack output,
    // int mana,
    // Block customCatalyst,
    // String group) {
    // ManaInfusionRecipeBuilder.builder(id)
    // .input(input)
    // .output(output)
    // .mana(mana)
    // .group(group)
    // .customCatalyst(customCatalyst)
    // .save();
    // }
    //
    // private static void ApothecaryRecipe(
    // String id,
    // Ingredient catalyst,
    // ItemStack output,
    // Ingredient[] input) {
    // var build = PetalApothecaryRecipeBuilder.builder(id);
    // build
    // .reagent(catalyst)
    // .output(output);
    // for (Ingredient ingredient : input) build.addIngredient(ingredient);
    // build.save();
    // }
    //
    // private static void RunicAltarRecipe(
    // String id,
    // int mana,
    // ItemStack output,
    // Boolean setHeadRecipe,
    // Ingredient[] input) {
    // var build = RunicAltarRecipeBuilder.builder(id);
    // build
    // .output(output)
    // .mana(mana)
    // .setHeadRecipe(setHeadRecipe);
    // for (Ingredient ingredient : input) build.addIngredient(ingredient);
    // build.save();
    // }
    //
    // private static void TAgglomerationRecipe(
    // String id,
    // int mana,
    // ItemStack output,
    // Ingredient[] input) {
    // var build = TerrestrialAgglomerationRecipeBuilder.builder(id);
    // build
    // .output(output)
    // .mana(mana);
    // for (Ingredient ingredient : input) build.addIngredient(ingredient);
    // build.save();
    // }
    //
    // private static void OrechidRecipe(
    // int id,
    // String name,
    // Block input,
    // TagPrefix output,
    // Material[] material,
    // int[] weight) {
    // if (id == 1) {
    // for (int k = 0; k < material.length; k++) {
    // OrechidRecipeBuilder.builder(name + material[k].getName().toLowerCase())
    // .input(input)
    // .output(ChemicalHelper.getBlock(output, material[k]))
    // .weight(weight[k])
    // .save();
    // }
    // } else if (id == 2) {
    // for (int k = 0; k < material.length; k++) {
    // OrechidIgnemRecipeBuilder.builder(name + material[k].getName().toLowerCase())
    // .input(input)
    // .output(ChemicalHelper.getBlock(output, material[k]))
    // .weight(weight[k])
    // .save();
    // }
    // } else if (id == 3) {
    // for (int k = 0; k < material.length; k++) {
    // MarimorphosisRecipeBuilder.builder(name + material[k].getName().toLowerCase())
    // .input(input)
    // .output(ChemicalHelper.getBlock(output, material[k]))
    // .weight(weight[k])
    // .biomeTag(BiomeTags.IS_OVERWORLD)
    // .save();
    // }
    // }
    // }

    private static void ElfExchangeRecipe(
                                          int circuitMeta,
                                          String id,
                                          int mana,
                                          int duration,
                                          ItemStack input,
                                          ItemStack output) {
        var build = ELVEN_PORTAL_RECIPES.builder(id);
        if (circuitMeta != 0) build.circuitMeta(circuitMeta);
        build
                .inputItems(input)
                .outputItems(output)
                .duration(duration)
                .MANAt(mana)
                .save();
    }

    private static void ElfExchangeRecipe(
                                          String id,
                                          ItemStack input,
                                          ItemStack output) {
        ElfExchangeRecipe(0, id, 8, 10, input, output);
    }

    private static void InfusionManaPoolRecipe(
                                               String id,
                                               Object input,
                                               ItemStack output,
                                               int mana,
                                               Item customCatalyst,
                                               String group) {
        Item customCatalystItem = (customCatalyst == null) ? BotaniaBlocks.getBlock("livingrock").asItem() : customCatalyst;
        int manat = Math.max(mana / 500, 1);
        int count = 1;
        if (manat == 1) count = Math.max(500 / mana, 1);
        var r = MANA_POOL_RECIPES.builder(id);
        if (customCatalyst != null) r.notConsumable(customCatalystItem);
        r.inputItems(input)
                .outputItems(output)
                .duration(20)
                .MANAt(mana / 20)
                .save();
        MANA_INFUSER_RECIPES.builder(id)
                .notConsumable(customCatalystItem)
                .inputItems(input, count)
                .outputItems(output.copyWithCount(output.getCount() * count))
                .duration(20)
                .circuitMeta(1)
                .MANAt(manat)
                .save();
    }

    private static void InfusionPureDaisyRecipe(
                                                String id,
                                                Object input,
                                                Item output) {
        PURE_DAISY_RECIPES.builder(id)
                .inputItems(input)
                .outputItems(output)
                .duration(20)
                .MANAt(1)
                .save();
        MANA_INFUSER_RECIPES.builder(id)
                .notConsumable(BotaniaBlocks.getBlock("pure_daisy").asItem())
                .inputItems(input, 32)
                .outputItems(output, 32)
                .duration(600)
                .circuitMeta(2)
                .MANAt(1)
                .save();
    }

    private static final List<Item> runeItems = List.of(
            BotaniaItems.getItem("rune_earth"), BotaniaItems.getItem("rune_air"), BotaniaItems.getItem("rune_fire"), BotaniaItems.getItem("rune_water"),
            BotaniaItems.getItem("rune_spring"), BotaniaItems.getItem("rune_summer"), BotaniaItems.getItem("rune_autumn"), BotaniaItems.getItem("rune_winter"),
            BotaniaItems.getItem("rune_mana"), BotaniaItems.getItem("rune_lust"), BotaniaItems.getItem("rune_gluttony"), BotaniaItems.getItem("rune_greed"),
            BotaniaItems.getItem("rune_sloth"), BotaniaItems.getItem("rune_wrath"), BotaniaItems.getItem("rune_envy"), BotaniaItems.getItem("rune_pride"),
            MythicBotanyItems.getItem("asgard_rune"), MythicBotanyItems.getItem("vanaheim_rune"), MythicBotanyItems.getItem("alfheim_rune"),
            MythicBotanyItems.getItem("joetunheim_rune"), MythicBotanyItems.getItem("muspelheim_rune"), MythicBotanyItems.getItem("niflheim_rune"),
            MythicBotanyItems.getItem("nidavellir_rune"), MythicBotanyItems.getItem("helheim_rune"), MythicBotanyItems.getItem("midgard_rune"));

    private static void IndustrialAltarRecipe1(
                                               String id,
                                               int mana,
                                               ItemStack output,
                                               Item[] inputs) {
        Reference2IntOpenHashMap<Item> CountMap = new Reference2IntOpenHashMap<>();
        for (Item item : inputs) CountMap.addTo(item, 1);
        var build1 = RUNIC_ALTAR_RECIPES.builder(id);
        build1
                .inputItems(BotaniaBlocks.getBlock("livingrock").asItem())
                .outputItems(output.copyWithCount(output.getCount()))
                .duration(mana / 100)
                .MANAt(100);
        CountMap.reference2IntEntrySet().fastForEach(entry -> {
            if (runeItems.contains(entry.getKey()))
                build1.notConsumable(entry.getKey(), entry.getIntValue());
            else
                build1.inputItems(entry.getKey(), entry.getIntValue());
        });
        build1.save();

        var build2 = INDUSTRIAL_ALTAR_RECIPES.builder(id);
        build2
                .inputItems(BotaniaBlocks.getBlock("livingrock").asItem())
                .outputItems(output.copyWithCount(output.getCount()))
                .duration(300)
                .circuitMeta(1)
                .MANAt(mana / 50);
        CountMap.reference2IntEntrySet().fastForEach(entry -> {
            if (runeItems.contains(entry.getKey()))
                build2.notConsumable(entry.getKey(), entry.getIntValue());
            else
                build2.inputItems(entry.getKey(), entry.getIntValue());
        });
        build2.save();

        var build3 = INDUSTRIAL_ALTAR_RECIPES.builder(id + "_plas");
        build3
                .inputItems(BotaniaBlocks.getBlock("livingrock").asItem(), 16)
                .outputItems(output.copyWithCount(output.getCount() << 4))
                .duration(300)
                .circuitMeta(2)
                .MANAt(mana / 50);
        CountMap.reference2IntEntrySet().fastForEach(entry -> build3.inputItems(entry.getKey(), entry.getIntValue()));
        build3.save();
    }

    private static void IndustrialAltarRecipe2(
                                               String id,
                                               int mana,
                                               ItemStack output,
                                               Item[] inputs) {
        Reference2IntOpenHashMap<Item> CountMap = new Reference2IntOpenHashMap<>();
        for (Item item : inputs) CountMap.addTo(item, 1);

        var build1 = RUNIC_ALTAR_RECIPES.builder(id);
        build1
                .inputItems(BotaniaBlocks.getBlock("livingrock").asItem())
                .outputItems(output)
                .duration(mana / 100)
                .MANAt(100);
        CountMap.reference2IntEntrySet().fastForEach(entry -> build1.inputItems(entry.getKey(), entry.getIntValue()));
        build1.save();

        var build2 = INDUSTRIAL_ALTAR_RECIPES.builder(id);
        build2
                .inputItems(BotaniaBlocks.getBlock("livingrock").asItem(), 4)
                .outputItems(output, output.getCount() * 4)
                .duration(300)
                .circuitMeta(1)
                .MANAt(mana / 50);
        CountMap.reference2IntEntrySet().fastForEach(entry -> build2.inputItems(entry.getKey(), entry.getIntValue() * 4));
        build2.save();
    }

    private static void IndustrialAltarRecipe3(
                                               int circuitMeta,
                                               String id,
                                               ItemStack output,
                                               Item[] inputs) {
        Reference2IntOpenHashMap<Item> CountMap = new Reference2IntOpenHashMap<>();
        for (Item item : inputs) CountMap.addTo(item, 1);
        var build1 = PETAL_APOTHECARY_RECIPES.builder(id);
        build1
                .inputItems(CommonTags.Items.SEEDS)
                .inputFluids(Water, 1000)
                .outputItems(output)
                .duration(20)
                .circuitMeta(circuitMeta)
                .MANAt(1);
        CountMap.reference2IntEntrySet().fastForEach(entry -> build1.inputItems(entry.getKey()));
        build1.save();

        var build2 = INDUSTRIAL_ALTAR_RECIPES.builder(id);
        build2
                .inputItems(CommonTags.Items.SEEDS, 8)
                .inputFluids(Water, 8000)
                .outputItems(output, 8)
                .duration(20)
                .circuitMeta(circuitMeta)
                .MANAt(32);
        CountMap.reference2IntEntrySet().fastForEach(entry -> build2.inputItems(entry.getKey(), entry.getIntValue() * 2));
        build2.save();
    }

    private static void PedestalSmashRecipe(
                                            String id,
                                            int mana,
                                            Ingredient hammer,
                                            ItemStack input,
                                            ItemStack output) {
        PEDESTAL_RECIPES.builder(id)
                .notConsumable(hammer)
                .inputItems(input)
                .outputItems(output)
                .duration(mana * 2)
                .MANAt(25)
                .save();
        INDUSTRIAL_ALTAR_RECIPES.builder(id)
                .notConsumable(hammer)
                .circuitMeta(6)
                .inputItems(input)
                .outputItems(output)
                .duration(40)
                .MANAt(mana)
                .save();
    }
}

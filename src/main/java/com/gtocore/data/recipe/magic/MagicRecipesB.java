package com.gtocore.data.recipe.magic;

import com.gtocore.common.data.*;
import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.ManaMultiBlock;
import com.gtocore.data.tag.Tags;

import com.gtolib.GTOCore;
import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import mythicbotany.register.ModItems;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BotaniaItems;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.GAS;
import static com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.LIQUID;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.api.data.tag.GTOTagPrefix.CRYSTAL_SEED;
import static com.gtocore.common.data.GTOItems.*;
import static com.gtocore.common.data.GTOMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.*;
import static com.gtocore.common.machine.mana.CelestialHandler.*;
import static com.gtocore.common.machine.mana.multiblock.ResonanceFlowerMachine.toResonanceTag;
import static com.gtocore.utils.PlayerHeadUtils.itemStackAddNbtString;

public final class MagicRecipesB {

    public static void init() {
        // 炼金锅3
        {
            ALCHEMY_CAULDRON_RECIPES.recipeBuilder("cycle_of_blossoms_solvent_fust")
                    .inputItems(COLORFUL_MYSTICAL_FLOWER, 32)
                    .inputItems(dust, StarStone, 4)
                    .inputFluids(FractalPetalSolvent, 2000)
                    .inputFluids(Ethanol, 1000)
                    .chancedOutput(CycleofBlossomsSolvent.getFluid(1500), 10, 0)
                    .chancedOutput(FractalPetalSolvent.getFluid(250), 1, 0)
                    .duration(240)
                    .temperature(1200)
                    .addData(GTORecipeDataKeys.PARAM1, 20)
                    .addData(GTORecipeDataKeys.PARAM2, 20)
                    .addData(GTORecipeDataKeys.PARAM3, 20)
                    .save();
        }

        // 魔力组装的各种配方
        {
            // 铭刻之布
            ASSEMBLER_RECIPES.recipeBuilder("affix_canvas")
                    .notConsumable("ars_nouveau:wilden_tribute")
                    .notConsumable("botania:life_essence")
                    .notConsumable("extrabotany:hero_medal")
                    .inputItems("botania:manaweave_cloth", 16)
                    .inputItems("apotheosis:uncommon_material", 8)
                    .inputItems("apotheosis:epic_material", 4)
                    .inputItems("apotheosis:mythic_material", 2)
                    .inputItems("apotheosis:infused_breath", 2)
                    .inputItems("apotheosis:gem_dust", 64)
                    .outputItems(GTOItems.AFFIX_CANVAS, 16)
                    .inputFluids(GTOMaterials.Animium, 1000)
                    .duration(20)
                    .MANAt(1024)
                    .save();

        }

        // 苍穹凝聚器
        {
            CELESTIAL_CONDENSER_RECIPES.recipeBuilder("astral_silver")
                    .inputItems(ingot, Silver)
                    .outputItems(ingot, AstralSilver)
                    .addData(LUNARA, 1000)
                    .duration(10)
                    .save();

            CELESTIAL_CONDENSER_RECIPES.recipeBuilder("helio_coal")
                    .inputItems(Items.COAL)
                    .outputItems(HELIO_COAL)
                    .addData(SOLARIS, 1000)
                    .duration(10)
                    .save();

            CELESTIAL_CONDENSER_RECIPES.recipeBuilder("ender_diamond")
                    .inputItems(Items.DIAMOND)
                    .outputItems(ENDER_DIAMOND)
                    .addData(VOIDFLUX, 1000)
                    .duration(10)
                    .save();

            CELESTIAL_CONDENSER_RECIPES.recipeBuilder("star_stone_0")
                    .inputItems(BotaniaBlocks.shimmerrock.asItem())
                    .outputItems(GTOBlocks.STAR_STONE[0].asItem())
                    .addData(ANY, 2000)
                    .duration(10)
                    .save();

            for (int i = 0; i < 11; i++) {
                CELESTIAL_CONDENSER_RECIPES.recipeBuilder("star_stone_" + (i + 1))
                        .inputItems(GTOBlocks.STAR_STONE[i].asItem())
                        .outputItems(GTOBlocks.STAR_STONE[i + 1].asItem())
                        .addData(i < 6 ? ANY : STELLARM, 2000 * (i + 2))
                        .duration(10)
                        .save();
            }

            CELESTIAL_CONDENSER_RECIPES.recipeBuilder("nether_star")
                    .inputItems(ModItems.fadedNetherStar)
                    .outputItems(Items.NETHER_STAR)
                    .addData(VOIDFLUX, 4000)
                    .duration(10)
                    .save();

            CELESTIAL_CONDENSER_RECIPES.builder("the_solaris_lens")
                    .inputItems(TagPrefix.block, GTOMaterials.ElfGlass)
                    .outputItems(GTOBlocks.THE_SOLARIS_LENS.asItem())
                    .duration(100)
                    .addData(SOLARIS, 50000)
                    .save();

            ItemStack manaTablet = BotaniaItems.manaTablet.getDefaultInstance();
            ItemNBTHelper.setInt(manaTablet, "mana", 500000);
            CELESTIAL_CONDENSER_RECIPES.builder("add_mana_to_mana_tablet")
                    .inputItems(BotaniaItems.manaTablet)
                    .outputItems(manaTablet)
                    .duration(100)
                    .addData(ANY, 5000)
                    .save();
        }

        // 符文铭刻
        {
            Item[] runeItem1 = {
                    BotaniaItems.runeEarth, BotaniaItems.runeAir, BotaniaItems.runeFire, BotaniaItems.runeWater,
                    BotaniaItems.runeSpring, BotaniaItems.runeSummer, BotaniaItems.runeAutumn, BotaniaItems.runeWinter,
                    BotaniaItems.runeMana, BotaniaItems.runeLust, BotaniaItems.runeGluttony, BotaniaItems.runeGreed,
                    BotaniaItems.runeSloth, BotaniaItems.runeWrath, BotaniaItems.runeEnvy, BotaniaItems.runePride,
            };
            Item[] runeItem2 = {
                    ModItems.asgardRune, ModItems.vanaheimRune, ModItems.alfheimRune,
                    ModItems.midgardRune, ModItems.joetunheimRune, ModItems.muspelheimRune,
                    ModItems.niflheimRune, ModItems.nidavellirRune, ModItems.helheimRune,
            };

            for (Item rune : runeItem1) {
                RUNE_ENGRAVING_RECIPES.recipeBuilder("engraving_" + rune.toString())
                        .notConsumable(rune)
                        .inputItems(BotaniaBlocks.livingrock.asItem())
                        .inputFluids(Animium, 3000)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .outputItems(rune, 9)
                        .MANAt(128)
                        .duration(200)
                        .save();
            }
            for (Item rune : runeItem2) {
                RUNE_ENGRAVING_RECIPES.recipeBuilder("engraving_" + rune.toString())
                        .notConsumable(rune)
                        .inputItems(BotaniaBlocks.livingrock.asItem())
                        .inputFluids(Animium, 9000)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .outputItems(rune, 9)
                        .MANAt(256)
                        .duration(200)
                        .save();
            }
        }

        // 权宜之计
        {
            // 魔力输入输出
            {
                int[] values = { GTValues.ZPM, GTValues.UV, GTValues.UHV, GTValues.UEV, GTValues.UIV, GTValues.UXV, GTValues.OpV };
                for (int value : values) {
                    VanillaRecipeHelper.addShapedRecipe(GTOCore.id(VN[value].toLowerCase() + "_mana_extract_hatch"), ManaMachine.MANA_EXTRACT_HATCH[value],
                            "AEA", "CDC", "AEA",
                            'A', GTOItems.STOPGAP_MEASURES.asStack(), 'C', GTMachines.BATTERY_BUFFER_16[value].asStack(), 'D', GTMachines.CHARGER_4[value].asStack(), 'E', GTMachines.ENERGY_INPUT_HATCH[value].asStack());

                    VanillaRecipeHelper.addShapedRecipe(GTOCore.id(VN[value].toLowerCase() + "_mana_input_hatch"), ManaMachine.MANA_INPUT_HATCH[value],
                            "AAA", "ABA", "AAA",
                            'A', GTOItems.STOPGAP_MEASURES.asStack(), 'B', GTMachines.SUBSTATION_ENERGY_INPUT_HATCH[value].asStack());

                    VanillaRecipeHelper.addShapedRecipe(GTOCore.id(VN[value].toLowerCase() + "_mana_output_hatch"), ManaMachine.MANA_OUTPUT_HATCH[value],
                            "AAA", "ABA", "AAA",
                            'A', GTOItems.STOPGAP_MEASURES.asStack(), 'B', GTMachines.SUBSTATION_ENERGY_OUTPUT_HATCH[value].asStack());

                    VanillaRecipeHelper.addShapedRecipe(GTOCore.id(VN[value].toLowerCase() + "_wireless_mana_input_hatch"), ManaMachine.WIRELESS_MANA_INPUT_HATCH[value],
                            "AAA", "ABA", "AAA",
                            'A', GTOItems.STOPGAP_MEASURES.asStack(), 'B', GTOMachines.WIRELESS_INPUT_HATCH_64[value].asStack());

                    VanillaRecipeHelper.addShapedRecipe(GTOCore.id(VN[value].toLowerCase() + "_wireless_mana_output_hatch"), ManaMachine.WIRELESS_MANA_OUTPUT_HATCH[value],
                            "AAA", "ABA", "AAA",
                            'A', GTOItems.STOPGAP_MEASURES.asStack(), 'B', GTOMachines.WIRELESS_OUTPUT_HATCH_64[value].asStack());
                }
            }
        }

        // 凋零的下界之星
        ARC_GENERATOR_RECIPES.recipeBuilder("make_faded_nether_star")
                .inputItems(dust, NetherEmber, 64)
                .inputItems(dust, StarStone)
                .inputFluids(NetherAir, 8000)
                .inputFluids(Salamander.getFluid(LIQUID, 200))
                .inputFluids(Mana, 32000)
                .outputItems(itemStackAddNbtString(ModItems.fadedNetherStar.getDefaultInstance(), "{Damage:1100000}"), 4)
                .duration(20)
                .EUt(VA[IV])
                .save();

        ARC_GENERATOR_RECIPES.recipeBuilder("make_nether_star")
                .inputItems(ModItems.fadedNetherStar, 4)
                .inputItems(dustTiny, NetherStar)
                .outputItems(Items.NETHER_STAR, 4)
                .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                .duration(20)
                .EUt(VA[UV])
                .save();

        // 液态魔力&魔力结晶
        {
            VACUUM_RECIPES.recipeBuilder("vacuum_mana_liquid")
                    .inputFluids(Mana.getFluid(GAS, 100000))
                    .outputFluids(Mana.getFluid(LIQUID, 1000))
                    .duration(2000)
                    .EUt(VA[HV])
                    .save();

            AUTOCLAVE_RECIPES.recipeBuilder("mana_crystal_seed")
                    .inputItems(gemExquisite, ManaDiamond)
                    .inputItems(gemExquisite, SourceGem)
                    .inputFluids(Mana.getFluid(LIQUID, 500))
                    .outputItems(CRYSTAL_SEED, Mana, 64)
                    .duration(100)
                    .EUt(VA[ULV])
                    .save();

            CRYSTALLIZATION_RECIPES.recipeBuilder("mana_crystal")
                    .inputItems(CRYSTAL_SEED, Mana)
                    .inputFluids(TheWaterFromTheWellOfWisdom, 500)
                    .outputItems(MANA_CRYSTAL)
                    .blastFurnaceTemp(3200)
                    .duration(1000)
                    .EUt(VA[HV])
                    .save();

            FORGE_HAMMER_RECIPES.recipeBuilder("mana_crystal_to_mana_crystal_seed")
                    .inputItems(MANA_CRYSTAL)
                    .outputItems(CRYSTAL_SEED, Mana, 4)
                    .duration(100)
                    .EUt(VA[ULV])
                    .save();
        }

        // 精粹回收
        {
            CHEMICAL_BATH_RECIPES.builder("enchantment_essence_recovery")
                    .inputItems(Tags.ENCHANTMENT_ESSENCE)
                    .inputFluids(TheWaterFromTheWellOfWisdom, 5)
                    .outputItems(ENCHANTMENT_ESSENCE.get("original"))
                    .duration(20)
                    .EUt(8)
                    .save();

            CHEMICAL_BATH_RECIPES.builder("affix_essence_recovery")
                    .inputItems(Tags.AFFIX_ESSENCE)
                    .inputFluids(TheWaterFromTheWellOfWisdom, 5)
                    .outputItems(AFFIX_ESSENCE.get("original"))
                    .duration(20)
                    .EUt(8)
                    .save();

        }

        INFUSER_CORE_RECIPES.builder("resonance_flower")
                .notConsumable(ModItems.fimbultyrTablet)
                .notConsumable(ExtraBotanyItems.manaRingMaster)
                .notConsumable(BotaniaItems.dice)
                .notConsumable(ExtraBotanyItems.pandorasBox)
                .inputItems(GTOBlocks.THE_ORIGIN_CASING.asItem(), 16)
                .inputItems(GTOBlocks.THE_END_CASING.asItem(), 16)
                .inputItems(GTOBlocks.THE_CHAOS_CASING.asItem(), 16)
                .inputItems(TagPrefix.gemFlawless, GTOMaterials.OriginCoreCrystal, 16)
                .inputItems(TagPrefix.gemFlawless, GTOMaterials.StarBloodCrystal, 16)
                .inputItems(TagPrefix.gemFlawless, GTOMaterials.SoulJadeCrystal, 16)
                .inputItems(TagPrefix.gemFlawless, GTOMaterials.RemnantSpiritStone, 16)
                .inputItems(TagPrefix.block, GTOMaterials.Runerock, 64)
                .inputItems(GTOBlocks.STAR_STONE[4], 64)
                .inputItems(GTOItems.PHILOSOPHERS_STONE)
                .inputItems(ItemsRegistry.WILDEN_TRIBUTE, 64)
                .inputItems(ItemsRegistry.MANIPULATION_ESSENCE, 64)
                .inputItems(TagPrefix.gem, GTMaterials.NetherStar, 64)
                .inputItems(AFFIX_ESSENCE.get("apotheosis:sword/special/thunderstruck"), 16)
                .inputFluids(GTMaterials.MaragingSteel300, L * 9 * 16)
                .inputFluids(GTOMaterials.EnergySolidifier, 8000)
                .inputFluids(GTOMaterials.Aether, FluidStorageKeys.LIQUID, 8000)
                .outputItems(ManaMultiBlock.RESONANCE_FLOWER)
                .duration(1200)
                .MANAt(32768)
                .save();

        // 元素共鸣
        {
            ELEMENTAL_RESONANCE.recipeBuilder("fluctuation")
                    .inputItems(ManaMultiBlock.RESONANCE_FLOWER)
                    .outputItems(ManaMultiBlock.RESONANCE_FLOWER)
                    .addData(GTORecipeDataKeys.RESONANCE, toResonanceTag(ChemicalHelper.get(dust, Livingrock), 5))
                    .MANAt(4)
                    .duration(10)
                    .circuitMeta(32)
                    .save();

            ELEMENTAL_RESONANCE.recipeBuilder("recycle_life_essence_from_gaia_dust")
                    .notConsumable(BotaniaItems.lifeEssence)
                    .inputItems(dust, Gaia, 16)
                    .inputItems(dust, StarStone)
                    .inputFluids(FinalPurifier, 250)
                    .outputItems(BotaniaItems.lifeEssence, 8)
                    .dimension(GTODimensions.ALFHEIM)
                    .addData(GTORecipeDataKeys.RESONANCE, toResonanceTag(TheWaterFromTheWellOfWisdom.getFluid(100), 10))
                    .MANAt(128)
                    .duration(3600)
                    .circuitMeta(8)
                    .save();

            ELEMENTAL_RESONANCE.recipeBuilder("recycle_nether_star_from_netherember_dust")
                    .notConsumable(Items.NETHER_STAR)
                    .inputItems(dust, NetherEmber, 256)
                    .inputItems(dust, StarStone)
                    .inputFluids(EnergySolidifier, 250)
                    .outputItems(Items.NETHER_STAR, 16)
                    .dimension(GTODimensions.ALFHEIM)
                    .addData(GTORecipeDataKeys.RESONANCE, toResonanceTag(TheWaterFromTheWellOfWisdom.getFluid(100), 10))
                    .MANAt(128)
                    .duration(3600)
                    .circuitMeta(8)
                    .save();

            // 寰宇星穹天体聚合圣坛
            ELEMENTAL_RESONANCE.builder("cosmic_celestial_spire_of_convergence")
                    .inputItems(GTOBlocks.SPELL_PRISM_CASING.asItem(), 8)
                    .inputItems(ManaMachine.CELESTIAL_CONDENSER, 64)
                    .inputItems(GTOBlocks.STAR_STONE[11], 16)
                    .inputItems(TagPrefix.block, GTOMaterials.BifrostPerm, 64)
                    .inputItems(GTItems.FIELD_GENERATOR_UHV, 64)
                    .inputItems(GTOItems.PHILOSOPHERS_STONE)
                    .inputFluids(GTOMaterials.Aether, FluidStorageKeys.LIQUID, 10000)
                    .inputFluids(GTOMaterials.CycleofBlossomsSolvent, 100000)
                    .inputFluids(GTOMaterials.WildenEssence, 100000)
                    .outputItems(ManaMultiBlock.COSMIC_CELESTIAL_SPIRE_OF_CONVERGENCE)
                    .addData(GTORecipeDataKeys.RESONANCE, toResonanceTag(new ItemStack(Items.NETHER_STAR), 5))
                    .duration(12000)
                    .MANAt(16384)
                    .save();
        }

        // 命树灵脉 - 处理线
        {
            // 基础副产
            {
                CHEMICAL_BATH_RECIPES.recipeBuilder("origin_core_crystal_crushed_ore_to_purified_ore")
                        .inputItems(crushed, OriginCoreCrystal)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .outputItems(crushedPurified, OriginCoreCrystal)
                        .chancedOutput(SOURCE_SPIRIT_DEBRIS.asItem(), 5000, 300)
                        .chancedOutput(dust, OriginCoreCrystal, 2000, 0)
                        .duration(200).EUt(VA[LV])
                        .category(GTRecipeCategories.ORE_BATHING)
                        .save();

                ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("origin_core_crystal_pure_dust_to_dust")
                        .inputItems(dustPure, OriginCoreCrystal)
                        .outputItems(dust, OriginCoreCrystal)
                        .chancedOutput(HOLY_ROOT_MYCELIUM.asItem(), 1000, 250)
                        .chancedOutput(dust, OriginCoreCrystal, 500, 0)
                        .duration(200).EUt(24)
                        .save();

                CHEMICAL_BATH_RECIPES.recipeBuilder("star_blood_crystal_crushed_ore_to_purified_ore")
                        .inputItems(crushed, StarBloodCrystal)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .outputItems(crushedPurified, StarBloodCrystal)
                        .chancedOutput(STAR_DEBRIS_SAND.asItem(), 5000, 300)
                        .chancedOutput(dust, StarBloodCrystal, 2000, 0)
                        .duration(200).EUt(VA[LV])
                        .category(GTRecipeCategories.ORE_BATHING)
                        .save();

                ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("star_blood_crystal_pure_dust_to_dust")
                        .inputItems(dustPure, StarBloodCrystal)
                        .outputItems(dust, StarBloodCrystal)
                        .chancedOutput(VEIN_BLOOD_MUCUS.asItem(), 1000, 250)
                        .chancedOutput(dust, StarBloodCrystal, 500, 0)
                        .duration(200).EUt(24)
                        .save();

                CHEMICAL_BATH_RECIPES.recipeBuilder("soul_jade_crystal_crushed_ore_to_purified_ore")
                        .inputItems(crushed, SoulJadeCrystal)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .outputItems(crushedPurified, SoulJadeCrystal)
                        .chancedOutput(SOUL_SHADOW_DUST.asItem(), 5000, 300)
                        .chancedOutput(dust, SoulJadeCrystal, 2000, 0)
                        .duration(200).EUt(VA[LV])
                        .category(GTRecipeCategories.ORE_BATHING)
                        .save();

                ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("soul_jade_crystal_pure_dust_to_dust")
                        .inputItems(dustPure, SoulJadeCrystal)
                        .outputItems(dust, SoulJadeCrystal)
                        .chancedOutput(CONSCIOUSNESS_THREAD.asItem(), 1000, 250)
                        .chancedOutput(dust, SoulJadeCrystal, 500, 0)
                        .duration(200).EUt(24)
                        .save();

                CHEMICAL_BATH_RECIPES.recipeBuilder("remnant_spirit_stone_crushed_ore_to_purified_ore")
                        .inputItems(crushed, RemnantSpiritStone)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .outputItems(crushedPurified, RemnantSpiritStone)
                        .chancedOutput(BONE_ASH_GRANULE.asItem(), 5000, 300)
                        .chancedOutput(dust, RemnantSpiritStone, 2000, 0)
                        .duration(200).EUt(VA[LV])
                        .category(GTRecipeCategories.ORE_BATHING)
                        .save();

                ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("remnant_spirit_stone_pure_dust_to_dust")
                        .inputItems(dustPure, RemnantSpiritStone)
                        .outputItems(dust, RemnantSpiritStone)
                        .chancedOutput(SPIRIT_BONE_FRAGMENT.asItem(), 1000, 250)
                        .chancedOutput(dust, RemnantSpiritStone, 500, 0)
                        .duration(200).EUt(24)
                        .save();
            }

            // 催化剂
            {
                MIXER_RECIPES.recipeBuilder("source_energy_extract")
                        .inputItems(dust, OriginCoreCrystal)
                        .inputItems(STAR_DEBRIS_SAND)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 2000)
                        .inputFluids(Mana.getFluid(LIQUID, 500))
                        .outputFluids(SourceEnergyExtract, 2500)
                        .outputItems(dust, ExtractionResidue)
                        .duration(8000)
                        .EUt(VA[LV])
                        .save();

                REACTION_FURNACE_RECIPES.recipeBuilder("star_vein_fusion")
                        .inputItems(dust, StarBloodCrystal)
                        .inputItems(SOUL_SHADOW_DUST)
                        .inputItems(MANA_CRYSTAL)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 3000)
                        .outputFluids(StarVeinFusion, 3200)
                        .outputItems(dust, FusionResidue)
                        .blastFurnaceTemp(4200)
                        .duration(8000)
                        .EUt(VA[LV])
                        .save();

                ARC_GENERATOR_RECIPES.recipeBuilder("star_vein_fusion")
                        .inputItems(dust, SoulJadeCrystal)
                        .inputItems(BONE_ASH_GRANULE)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1800)
                        .inputFluids(Mana.getFluid(LIQUID, 500))
                        .outputFluids(SoulThoughtHarmony, 2200)
                        .outputItems(dust, HarmonyResidue)
                        .duration(8000)
                        .EUt(VA[LV])
                        .save();

                INCUBATOR_RECIPES.recipeBuilder("remnant_erosion_activate")
                        .inputItems(dust, RemnantSpiritStone)
                        .inputItems(SOURCE_SPIRIT_DEBRIS)
                        .inputItems(MANA_CRYSTAL)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 2500)
                        .outputFluids(RemnantErosionActivate, 2800)
                        .outputItems(dust, ErosionActivateResidue)
                        .duration(8000)
                        .EUt(VA[LV])
                        .save();

                ALCHEMY_CAULDRON_RECIPES.recipeBuilder("final_purifier")
                        .inputItems(dust, OriginCoreCrystal)
                        .inputItems(dust, StarBloodCrystal)
                        .inputItems(dust, SoulJadeCrystal)
                        .inputItems(dust, RemnantSpiritStone)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .outputFluids(FinalPurifier, 800)
                        .duration(400)
                        .temperature(1200)
                        .MANAt(4)
                        .save();

                AUTOCLAVE_RECIPES.recipeBuilder("energy_solidifier")
                        .inputItems(SPIRIT_BONE_FRAGMENT)
                        .inputItems(CONSCIOUSNESS_THREAD)
                        .inputFluids(FinalPurifier, 600)
                        .outputFluids(EnergySolidifier, 800)
                        .duration(400)
                        .EUt(VA[EV])
                        .save();

            }

            // 循环催化剂
            {
                // 源核晶
                {
                    MIXER_RECIPES.recipeBuilder("origin_core_energy_body")
                            .inputItems(dust, OriginCoreCrystal, 2)
                            .inputItems(SOURCE_SPIRIT_DEBRIS, 3)
                            .inputItems(MANA_CRYSTAL)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 500)
                            .outputItems(ORIGIN_CORE_ENERGY_BODY)
                            .duration(600)
                            .EUt(VA[HV])
                            .save();

                    AUTOCLAVE_RECIPES.recipeBuilder("source_energy_catalyst_embryo")
                            .inputItems(ORIGIN_CORE_ENERGY_BODY)
                            .inputItems(HOLY_ROOT_MYCELIUM)
                            .inputFluids(SourceEnergyExtract, 300)
                            .outputItems(SOURCE_ENERGY_CATALYST_EMBRYO)
                            .duration(800)
                            .EUt(VA[EV])
                            .save();

                    DEHYDRATOR_RECIPES.recipeBuilder("source_energy_catalyst_crystal")
                            .inputItems(SOURCE_ENERGY_CATALYST_EMBRYO)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 500)
                            .inputFluids(Mana.getFluid(LIQUID, 500))
                            .outputItems(SOURCE_ENERGY_CATALYST_CRYSTAL)
                            .duration(400)
                            .EUt(VA[MV])
                            .save();

                    FORGE_HAMMER_RECIPES.recipeBuilder("regenerated_source_energy_body")
                            .inputItems(SOURCE_ENERGY_CATALYST_CRYSTAL_SHARD)
                            .outputItems(REGENERATED_SOURCE_ENERGY_BODY)
                            .duration(100)
                            .EUt(VA[ULV])
                            .save();

                    AUTOCLAVE_RECIPES.recipeBuilder("recycle_source_energy_catalyst_crystal")
                            .inputItems(REGENERATED_SOURCE_ENERGY_BODY)
                            .inputItems(dust, OriginCoreCrystalResidue)
                            .inputFluids(SourceEnergyExtract, 300)
                            .outputItems(SOURCE_ENERGY_CATALYST_CRYSTAL)
                            .duration(800)
                            .EUt(VA[EV])
                            .save();
                }

                // 星血晶
                {
                    REACTION_FURNACE_RECIPES.recipeBuilder("star_vein_base")
                            .inputItems(dust, StarBloodCrystal, 3)
                            .inputItems(STAR_DEBRIS_SAND, 2)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 2500)
                            .inputFluids(Mana.getFluid(LIQUID, 500))
                            .outputFluids(StarVeinBase, 2000)
                            .blastFurnaceTemp(3800)
                            .duration(1000)
                            .EUt(VA[HV])
                            .save();

                    DIGESTION_TREATMENT_RECIPES.recipeBuilder("star_vein_active")
                            .inputItems(VEIN_BLOOD_MUCUS)
                            .inputFluids(StarVeinBase, 2000)
                            .outputFluids(StarVeinActive, 1800)
                            .blastFurnaceTemp(4200)
                            .duration(6000)
                            .EUt(VA[MV])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("star_vein_catalyst_precursor")
                            .inputItems(MANA_CRYSTAL)
                            .inputFluids(StarVeinActive, 180)
                            .inputFluids(StarVeinFusion, 500)
                            .outputFluids(StarVeinCatalystPrecursor, 1500)
                            .duration(600)
                            .EUt(VA[IV])
                            .save();

                    MIXER_RECIPES.recipeBuilder("star_vein_catalyst")
                            .inputItems(MANA_CRYSTAL)
                            .inputFluids(StarVeinCatalystPrecursor, 1500)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 300)
                            .inputFluids(Mana.getFluid(LIQUID, 500))
                            .outputFluids(StarVeinCatalyst, 1600)
                            .duration(4000)
                            .EUt(VA[MV])
                            .save();

                    DISTILLERY_RECIPES.recipeBuilder("purified_star_vein_catalyst_waste")
                            .inputFluids(StarVeinCatalystWaste, 1000)
                            .outputFluids(PurifiedStarVeinCatalystWaste, 600)
                            .duration(2000)
                            .EUt(VA[HV])
                            .save();

                    DIGESTION_TREATMENT_RECIPES.recipeBuilder("regenerated_star_vein_active")
                            .inputItems(dust, StarBloodCrystal)
                            .inputItems(MANA_CRYSTAL)
                            .inputFluids(PurifiedStarVeinCatalystWaste, 800)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 500)
                            .outputFluids(RegeneratedStarVeinActive, 900)
                            .blastFurnaceTemp(3600)
                            .duration(600)
                            .EUt(VA[IV])
                            .save();

                    MIXER_RECIPES.recipeBuilder("recycle_star_vein_catalyst")
                            .inputItems(VEIN_BLOOD_MUCUS)
                            .inputItems(dust, StarBloodCrystalResidue, 2)
                            .inputFluids(RegeneratedStarVeinActive, 1800)
                            .inputFluids(Mana.getFluid(LIQUID, 500))
                            .outputFluids(StarVeinCatalyst, 2000)
                            .duration(2000)
                            .EUt(VA[MV])
                            .save();
                }

                // 魂玉晶
                {
                    AUTOCLAVE_RECIPES.recipeBuilder("soul_thought_condensate")
                            .inputItems(dust, SoulJadeCrystal, 2)
                            .inputItems(SOUL_SHADOW_DUST, 3)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 600)
                            .outputItems(SOUL_THOUGHT_CONDENSATE)
                            .duration(600)
                            .EUt(VA[EV])
                            .save();

                    ALLOY_SMELTER_RECIPES.recipeBuilder("anchored_soul_core")
                            .inputItems(SOUL_THOUGHT_CONDENSATE)
                            .inputItems(CONSCIOUSNESS_THREAD)
                            .outputItems(ANCHORED_SOUL_CORE)
                            .duration(4000)
                            .EUt(VA[MV])
                            .save();

                    ARC_FURNACE_RECIPES.recipeBuilder("soul_thought_catalyst_embryo")
                            .inputItems(ANCHORED_SOUL_CORE)
                            .inputFluids(SoulThoughtHarmony, 400)
                            .outputItems(SOUL_THOUGHT_CATALYST_EMBRYO)
                            .duration(3000)
                            .EUt(VA[HV])
                            .save();

                    ISOSTATIC_PRESSING_RECIPES.recipeBuilder("soul_thought_catalyst_core")
                            .inputItems(SOUL_THOUGHT_CATALYST_EMBRYO)
                            .inputItems(STAR_DEBRIS_SAND)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 800)
                            .inputFluids(Mana.getFluid(LIQUID, 500))
                            .outputItems(SOUL_THOUGHT_CATALYST_CORE)
                            .duration(6000)
                            .EUt(VA[EV])
                            .save();

                    AUTOCLAVE_RECIPES.recipeBuilder("regenerated_soul_core")
                            .inputItems(SOUL_THOUGHT_CATALYST_CORE_SHARD)
                            .inputItems(MANA_CRYSTAL)
                            .inputFluids(SoulThoughtHarmony, 200)
                            .outputItems(REGENERATED_SOUL_CORE)
                            .duration(600)
                            .EUt(VA[EV])
                            .save();

                    ISOSTATIC_PRESSING_RECIPES.recipeBuilder("recycle_soul_thought_catalyst_core")
                            .inputItems(REGENERATED_SOUL_CORE)
                            .inputItems(dust, SoulJadeCrystalResidue, 2)
                            .inputItems(CONSCIOUSNESS_THREAD)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 800)
                            .inputFluids(Mana.getFluid(LIQUID, 500))
                            .outputItems(SOUL_THOUGHT_CATALYST_CORE)
                            .duration(2000)
                            .EUt(VA[HV])
                            .save();
                }

                // 骸灵石
                {

                    MIXER_RECIPES.recipeBuilder("remnant_energy_adsorber")
                            .inputItems(dust, RemnantSpiritStone, 3)
                            .inputItems(BONE_ASH_GRANULE, 4)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 700)
                            .outputItems(REMNANT_ENERGY_ADSORBER)
                            .duration(800)
                            .EUt(VA[MV])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("remnant_erosion_catalyst_embryo")
                            .inputItems(REMNANT_ENERGY_ADSORBER)
                            .inputItems(SPIRIT_BONE_FRAGMENT)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 300)
                            .inputFluids(RemnantErosionActivate, 500)
                            .outputItems(REMNANT_EROSION_CATALYST_EMBRYO)
                            .inputFluids(Mana.getFluid(LIQUID, 500))
                            .duration(200)
                            .EUt(VA[EV])
                            .save();

                    MACERATOR_RECIPES.recipeBuilder("remnant_erosion_catalyst")
                            .inputItems(REMNANT_EROSION_CATALYST_EMBRYO)
                            .outputItems(dust, RemnantErosionCatalyst, 3)
                            .duration(4000)
                            .EUt(VA[EV])
                            .save();

                    MIXER_RECIPES.recipeBuilder("regenerated_remnant_energy_adsorber")
                            .inputItems(dust, InactiveRemnantErosionCatalyst, 5)
                            .inputItems(dust, RemnantSpiritStoneResidue, 2)
                            .inputFluids(Mana.getFluid(LIQUID, 500))
                            .outputItems(REGENERATED_REMNANT_ENERGY_ADSORBER)
                            .duration(800)
                            .EUt(VA[MV])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("recycle_remnant_erosion_catalyst")
                            .inputItems(REGENERATED_REMNANT_ENERGY_ADSORBER)
                            .inputFluids(RemnantErosionActivate, 200)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 300)
                            .outputItems(REMNANT_EROSION_CATALYST_EMBRYO)
                            .duration(200)
                            .EUt(VA[EV])
                            .save();
                }
            }

            // 简化产线&残渣产线
            {
                int chance = GTOCore.isEasy() ? 250 : 25;
                int chanceBoost = GTOCore.isEasy() ? 50 : 5;
                // 源核晶
                {
                    CHEMICAL_BATH_RECIPES.recipeBuilder("purify_refined_origin_core_crystal_ore")
                            .inputItems(crushedRefined, OriginCoreCrystal)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 400)
                            .outputItems(PURIFY_REFINED_ORIGIN_CORE_CRYSTAL_ORE)
                            .duration(2000).EUt(VA[HV])
                            .category(GTRecipeCategories.ORE_BATHING)
                            .save();

                    INFUSER_CORE_RECIPES.recipeBuilder("crudely_purified_origin_core_crystal_ore")
                            .inputItems(PURIFY_REFINED_ORIGIN_CORE_CRYSTAL_ORE, 5)
                            .inputItems(dust, OriginCoreCrystal, 5)
                            .inputItems(HOLY_ROOT_MYCELIUM)
                            .inputFluids(SourceEnergyExtract, 2000)
                            .inputFluids(FinalPurifier, 800)
                            .inputFluids(CycleofBlossomsSolvent, 200)
                            .outputItems(CRUDELY_PURIFIED_ORIGIN_CORE_CRYSTAL_ORE, 5)
                            .duration(8000).MANAt(VA[MV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("sifter_crudely_purified_origin_core_crystal_ore")
                            .inputItems(CRUDELY_PURIFIED_ORIGIN_CORE_CRYSTAL_ORE)
                            .chancedOutput(gemFlawless, OriginCoreCrystal, chance, chanceBoost)
                            .chancedOutput(gem, OriginCoreCrystal, chance * 2, 100)
                            .chancedOutput(dust, OriginCoreCrystalResidue, 6500 - 3 * chance, 600)
                            .chancedOutput(dustPure, OriginCoreCrystal, 5000, 0)
                            .duration(600).EUt(VA[HV])
                            .save();
                }

                // 星血晶
                {
                    CHEMICAL_BATH_RECIPES.recipeBuilder("purify_refined_star_blood_crystal_ore")
                            .inputItems(crushedRefined, StarBloodCrystal)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 400)
                            .outputItems(PURIFY_REFINED_STAR_BLOOD_CRYSTAL_ORE)
                            .duration(2000).EUt(VA[HV])
                            .category(GTRecipeCategories.ORE_BATHING)
                            .save();

                    INFUSER_CORE_RECIPES.recipeBuilder("crudely_fused_star_blood_crystal_ore")
                            .inputItems(PURIFY_REFINED_STAR_BLOOD_CRYSTAL_ORE, 5)
                            .inputItems(dust, StarBloodCrystal, 5)
                            .inputItems(VEIN_BLOOD_MUCUS)
                            .inputFluids(StarVeinFusion, 2000)
                            .inputFluids(EnergySolidifier, 800)
                            .inputFluids(CycleofBlossomsSolvent, 200)
                            .outputItems(CRUDELY_FUSED_STAR_BLOOD_CRYSTAL_ORE, 5)
                            .duration(8000).MANAt(VA[MV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("sifter_crudely_fused_star_blood_crystal_ore")
                            .inputItems(CRUDELY_FUSED_STAR_BLOOD_CRYSTAL_ORE)
                            .chancedOutput(gemFlawless, StarBloodCrystal, chance, chanceBoost)
                            .chancedOutput(gem, StarBloodCrystal, chance * 2, 100)
                            .chancedOutput(dust, StarBloodCrystalResidue, 6500 - 3 * chance, 600)
                            .chancedOutput(dustPure, StarBloodCrystal, 5000, 0)
                            .duration(600).EUt(VA[HV])
                            .save();
                }

                // 魂玉晶
                {
                    CHEMICAL_BATH_RECIPES.recipeBuilder("purify_refined_soul_jade_crystal_ore")
                            .inputItems(crushedRefined, SoulJadeCrystal)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 400)
                            .outputItems(PURIFY_REFINED_SOUL_JADE_CRYSTAL_ORE)
                            .duration(2000).EUt(VA[HV])
                            .category(GTRecipeCategories.ORE_BATHING)
                            .save();

                    INFUSER_CORE_RECIPES.recipeBuilder("crudely_harmonized_soul_jade_crystal_ore")
                            .inputItems(PURIFY_REFINED_SOUL_JADE_CRYSTAL_ORE, 5)
                            .inputItems(dust, SoulJadeCrystal, 5)
                            .inputItems(CONSCIOUSNESS_THREAD)
                            .inputFluids(SoulThoughtHarmony, 2000)
                            .inputFluids(FinalPurifier, 800)
                            .inputFluids(CycleofBlossomsSolvent, 200)
                            .outputItems(CRUDELY_HARMONIZED_SOUL_JADE_CRYSTAL_ORE, 5)
                            .duration(8000).MANAt(VA[MV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("sifter_crudely_harmonized_soul_jade_crystal_ore")
                            .inputItems(CRUDELY_HARMONIZED_SOUL_JADE_CRYSTAL_ORE)
                            .chancedOutput(gemFlawless, SoulJadeCrystal, chance, chanceBoost)
                            .chancedOutput(gem, SoulJadeCrystal, chance * 2, 100)
                            .chancedOutput(dust, SoulJadeCrystalResidue, 6500 - 3 * chance, 600)
                            .chancedOutput(dustPure, SoulJadeCrystal, 5000, 0)
                            .duration(600).EUt(VA[HV])
                            .save();
                }

                // 骸灵石
                {
                    CHEMICAL_BATH_RECIPES.recipeBuilder("purify_refined_remnant_spirit_stone_ore")
                            .inputItems(crushedRefined, RemnantSpiritStone)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 400)
                            .outputItems(PURIFY_REFINED_REMNANT_SPIRIT_STONE_ORE)
                            .duration(2000).EUt(VA[HV])
                            .category(GTRecipeCategories.ORE_BATHING)
                            .save();

                    INFUSER_CORE_RECIPES.recipeBuilder("crudely_shaped_remnant_spirit_stone_ore")
                            .inputItems(PURIFY_REFINED_REMNANT_SPIRIT_STONE_ORE, 5)
                            .inputItems(dust, RemnantSpiritStone, 5)
                            .inputItems(SPIRIT_BONE_FRAGMENT)
                            .inputFluids(RemnantErosionActivate, 2000)
                            .inputFluids(EnergySolidifier, 800)
                            .inputFluids(CycleofBlossomsSolvent, 200)
                            .outputItems(CRUDELY_SHAPED_REMNANT_SPIRIT_STONE_ORE, 5)
                            .duration(8000).MANAt(VA[MV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("sifter_crudely_shaped_remnant_spirit_stone_ore")
                            .inputItems(CRUDELY_SHAPED_REMNANT_SPIRIT_STONE_ORE)
                            .chancedOutput(gemFlawless, RemnantSpiritStone, chance, chanceBoost)
                            .chancedOutput(gem, RemnantSpiritStone, chance * 2, 100)
                            .chancedOutput(dust, RemnantSpiritStoneResidue, 6500 - 3 * chance, 600)
                            .chancedOutput(dustPure, RemnantSpiritStone, 5000, 0)
                            .duration(600).EUt(VA[HV])
                            .save();
                }
            }

            // ================================================================
            // 标准产线（每矿 9 步，1 精炼矿石 → 1 无暇宝石）
            // 第 1 步复用上方"简化产线&残渣产线"里已实现的化学浸洗配方，这里从第 2 步开始。
            // 每颗宝石投入：常规伴生 ×1、稀有伴生 ×1、跨矿塑形料 ×2、本矿催化剂 ×1、
            // 本矿试剂 600mB、净化剂 300mB、群芳轮迴溶剂 200mB；产出残渣粉 ×3，母液批内自平衡。
            // ================================================================
            {
                // -------------------- 源核晶标准产线（粉末产率 N = 3）--------------------
                {
                    CENTRIFUGE_RECIPES.recipeBuilder("origin_core_crystal_decontamination")
                            .inputItems(PURIFY_REFINED_ORIGIN_CORE_CRYSTAL_ORE)
                            .inputItems(SOURCE_SPIRIT_DEBRIS)
                            .outputItems(DECONTAMINATED_ORIGIN_CORE_CRYSTAL_ORE)
                            .duration(220).EUt(VA[MV])
                            .save();

                    CRUSHER_RECIPES.recipeBuilder("origin_core_crystal_coarse_grinding")
                            .inputItems(DECONTAMINATED_ORIGIN_CORE_CRYSTAL_ORE)
                            .outputItems(dust, CoarseOriginCoreCrystalDust, 3)
                            .outputItems(dust, OriginCoreCrystalResidue)
                            .duration(340).EUt(VA[HV])
                            .save();

                    REACTION_FURNACE_RECIPES.recipeBuilder("origin_core_crystal_catalytic_activation")
                            .inputItems(dust, CoarseOriginCoreCrystalDust, 3)
                            .inputItems(SOURCE_ENERGY_CATALYST_CRYSTAL)
                            .outputItems(dust, ActivatedOriginCoreCrystalDust, 3)
                            .outputItems(SOURCE_ENERGY_CATALYST_CRYSTAL_SHARD)
                            .blastFurnaceTemp(3600)
                            .duration(560).EUt(VA[EV])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("origin_core_crystal_reagent_reaction")
                            .inputItems(dust, ActivatedOriginCoreCrystalDust, 3)
                            .inputItems(HOLY_ROOT_MYCELIUM)
                            .inputFluids(SourceEnergyExtract, 600)
                            .inputFluids(OriginCoreCrystalMotherLiquor, 800)
                            .outputFluids(OriginCoreCrystalReactionSolution, 2000)
                            .outputItems(dust, OriginCoreCrystalResidue)
                            .duration(780).EUt(VA[IV])
                            .save();

                    DEHYDRATOR_RECIPES.recipeBuilder("origin_core_crystal_dehydration_grinding")
                            .inputFluids(OriginCoreCrystalReactionSolution, 2000)
                            .outputItems(dust, FineOriginCoreCrystalDust, 3)
                            .outputFluids(OriginCoreCrystalMotherLiquor, 800)
                            .duration(420).EUt(VA[LuV])
                            .save();

                    ISOSTATIC_PRESSING_RECIPES.recipeBuilder("origin_core_crystal_purification")
                            .inputItems(dust, FineOriginCoreCrystalDust, 3)
                            .inputFluids(FinalPurifier, 300)
                            .outputItems(dust, PurifiedOriginCoreCrystalDust)
                            .outputItems(dust, OriginCoreCrystalResidue)
                            .duration(680).EUt(VA[ZPM])
                            .save();

                    AUTOCLAVE_RECIPES.recipeBuilder("origin_core_crystal_shaping_infiltration")
                            .inputItems(dust, PurifiedOriginCoreCrystalDust)
                            .inputItems(STAR_DEBRIS_SAND, 2)
                            .inputFluids(CycleofBlossomsSolvent, 200)
                            .outputItems(INFILTRATED_ORIGIN_CORE_CRYSTAL)
                            .duration(460).EUt(VA[UV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("origin_core_crystal_spectral_sifting")
                            .inputItems(INFILTRATED_ORIGIN_CORE_CRYSTAL)
                            .chancedOutput(gemExquisite, OriginCoreCrystal, 300, 0)
                            .chancedOutput(gemFlawless, OriginCoreCrystal, 4000, 0)
                            .chancedOutput(gem, OriginCoreCrystal, 2500, 0)
                            .chancedOutput(dust, OriginCoreCrystalResidue, 3500, 0)
                            .duration(720).EUt(VA[UV])
                            .save();
                }

                // -------------------- 星血晶标准产线（粉末产率 N = 4）--------------------
                {
                    CENTRIFUGE_RECIPES.recipeBuilder("star_blood_crystal_decontamination")
                            .inputItems(PURIFY_REFINED_STAR_BLOOD_CRYSTAL_ORE)
                            .inputItems(STAR_DEBRIS_SAND)
                            .outputItems(DECONTAMINATED_STAR_BLOOD_CRYSTAL_ORE)
                            .duration(180).EUt(VA[LV])
                            .save();

                    CRUSHER_RECIPES.recipeBuilder("star_blood_crystal_coarse_grinding")
                            .inputItems(DECONTAMINATED_STAR_BLOOD_CRYSTAL_ORE)
                            .outputItems(dust, CoarseStarBloodCrystalDust, 4)
                            .outputItems(dust, StarBloodCrystalResidue)
                            .duration(280).EUt(VA[MV])
                            .save();

                    ALCHEMY_CAULDRON_RECIPES.recipeBuilder("star_blood_crystal_catalytic_activation")
                            .inputItems(dust, CoarseStarBloodCrystalDust, 4)
                            .inputFluids(StarVeinCatalyst, 500)
                            .outputItems(dust, ActivatedStarBloodCrystalDust, 4)
                            .outputFluids(StarVeinCatalystWaste, 500)
                            .duration(460)
                            .temperature(1800)
                            .MANAt(16)
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("star_blood_crystal_reagent_reaction")
                            .inputItems(dust, ActivatedStarBloodCrystalDust, 4)
                            .inputItems(VEIN_BLOOD_MUCUS)
                            .inputFluids(StarVeinFusion, 600)
                            .inputFluids(StarBloodCrystalMotherLiquor, 800)
                            .outputFluids(StarBloodCrystalReactionSolution, 2000)
                            .outputItems(dust, StarBloodCrystalResidue)
                            .duration(660).EUt(VA[EV])
                            .save();

                    DEHYDRATOR_RECIPES.recipeBuilder("star_blood_crystal_dehydration_grinding")
                            .inputFluids(StarBloodCrystalReactionSolution, 2000)
                            .outputItems(dust, FineStarBloodCrystalDust, 4)
                            .outputFluids(StarBloodCrystalMotherLiquor, 800)
                            .duration(360).EUt(VA[IV])
                            .save();

                    AUTOCLAVE_RECIPES.recipeBuilder("star_blood_crystal_purification")
                            .inputItems(dust, FineStarBloodCrystalDust, 4)
                            .inputFluids(EnergySolidifier, 300)
                            .outputItems(dust, PurifiedStarBloodCrystalDust)
                            .outputItems(dust, StarBloodCrystalResidue)
                            .duration(580).EUt(VA[LuV])
                            .save();

                    ISOSTATIC_PRESSING_RECIPES.recipeBuilder("star_blood_crystal_shaping_infiltration")
                            .inputItems(dust, PurifiedStarBloodCrystalDust)
                            .inputItems(SOUL_SHADOW_DUST, 2)
                            .inputFluids(CycleofBlossomsSolvent, 200)
                            .outputItems(INFILTRATED_STAR_BLOOD_CRYSTAL)
                            .duration(400).EUt(VA[ZPM])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("star_blood_crystal_spectral_sifting")
                            .inputItems(INFILTRATED_STAR_BLOOD_CRYSTAL)
                            .chancedOutput(gemExquisite, StarBloodCrystal, 300, 0)
                            .chancedOutput(gemFlawless, StarBloodCrystal, 4000, 0)
                            .chancedOutput(gem, StarBloodCrystal, 2500, 0)
                            .chancedOutput(dust, StarBloodCrystalResidue, 3500, 0)
                            .duration(600).EUt(VA[ZPM])
                            .save();
                }

                // -------------------- 魂玉晶标准产线（粉末产率 N = 3）--------------------
                {
                    CENTRIFUGE_RECIPES.recipeBuilder("soul_jade_crystal_decontamination")
                            .inputItems(PURIFY_REFINED_SOUL_JADE_CRYSTAL_ORE)
                            .inputItems(SOUL_SHADOW_DUST)
                            .outputItems(DECONTAMINATED_SOUL_JADE_CRYSTAL_ORE)
                            .duration(260).EUt(VA[HV])
                            .save();

                    CRUSHER_RECIPES.recipeBuilder("soul_jade_crystal_coarse_grinding")
                            .inputItems(DECONTAMINATED_SOUL_JADE_CRYSTAL_ORE)
                            .outputItems(dust, CoarseSoulJadeCrystalDust, 3)
                            .outputItems(dust, SoulJadeCrystalResidue)
                            .duration(420).EUt(VA[EV])
                            .save();

                    AUTOCLAVE_RECIPES.recipeBuilder("soul_jade_crystal_catalytic_activation")
                            .inputItems(dust, CoarseSoulJadeCrystalDust, 3)
                            .inputItems(SOUL_THOUGHT_CATALYST_CORE)
                            .outputItems(dust, ActivatedSoulJadeCrystalDust, 3)
                            .outputItems(SOUL_THOUGHT_CATALYST_CORE_SHARD)
                            .duration(640).EUt(VA[IV])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("soul_jade_crystal_reagent_reaction")
                            .inputItems(dust, ActivatedSoulJadeCrystalDust, 3)
                            .inputItems(CONSCIOUSNESS_THREAD)
                            .inputFluids(SoulThoughtHarmony, 600)
                            .inputFluids(SoulJadeCrystalMotherLiquor, 800)
                            .outputFluids(SoulJadeCrystalReactionSolution, 2000)
                            .outputItems(dust, SoulJadeCrystalResidue)
                            .duration(900).EUt(VA[LuV])
                            .save();

                    DEHYDRATOR_RECIPES.recipeBuilder("soul_jade_crystal_dehydration_grinding")
                            .inputFluids(SoulJadeCrystalReactionSolution, 2000)
                            .outputItems(dust, FineSoulJadeCrystalDust, 3)
                            .outputFluids(SoulJadeCrystalMotherLiquor, 800)
                            .duration(500).EUt(VA[ZPM])
                            .save();

                    ISOSTATIC_PRESSING_RECIPES.recipeBuilder("soul_jade_crystal_purification")
                            .inputItems(dust, FineSoulJadeCrystalDust, 3)
                            .inputFluids(FinalPurifier, 300)
                            .outputItems(dust, PurifiedSoulJadeCrystalDust)
                            .outputItems(dust, SoulJadeCrystalResidue)
                            .duration(820).EUt(VA[UV])
                            .save();

                    ALCHEMY_CAULDRON_RECIPES.recipeBuilder("soul_jade_crystal_shaping_infiltration")
                            .inputItems(dust, PurifiedSoulJadeCrystalDust)
                            .inputItems(BONE_ASH_GRANULE, 2)
                            .inputFluids(CycleofBlossomsSolvent, 200)
                            .outputItems(INFILTRATED_SOUL_JADE_CRYSTAL)
                            .duration(480)
                            .temperature(1200)
                            .MANAt(4096)
                            .save();

                    SIFTER_RECIPES.recipeBuilder("soul_jade_crystal_spectral_sifting")
                            .inputItems(INFILTRATED_SOUL_JADE_CRYSTAL)
                            .chancedOutput(gemExquisite, SoulJadeCrystal, 300, 0)
                            .chancedOutput(gemFlawless, SoulJadeCrystal, 4000, 0)
                            .chancedOutput(gem, SoulJadeCrystal, 2500, 0)
                            .chancedOutput(dust, SoulJadeCrystalResidue, 3500, 0)
                            .duration(780).EUt(VA[UV])
                            .save();
                }

                // -------------------- 骸灵石标准产线（粉末产率 N = 5）--------------------
                {
                    CENTRIFUGE_RECIPES.recipeBuilder("remnant_spirit_stone_decontamination")
                            .inputItems(PURIFY_REFINED_REMNANT_SPIRIT_STONE_ORE)
                            .inputItems(BONE_ASH_GRANULE)
                            .outputItems(DECONTAMINATED_REMNANT_SPIRIT_STONE_ORE)
                            .duration(200).EUt(VA[MV])
                            .save();

                    CRUSHER_RECIPES.recipeBuilder("remnant_spirit_stone_coarse_grinding")
                            .inputItems(DECONTAMINATED_REMNANT_SPIRIT_STONE_ORE)
                            .outputItems(dust, CoarseRemnantSpiritStoneDust, 5)
                            .outputItems(dust, RemnantSpiritStoneResidue)
                            .duration(380).EUt(VA[HV])
                            .save();

                    REACTION_FURNACE_RECIPES.recipeBuilder("remnant_spirit_stone_catalytic_activation")
                            .inputItems(dust, CoarseRemnantSpiritStoneDust, 5)
                            .inputItems(dust, RemnantErosionCatalyst)
                            .outputItems(dust, ActivatedRemnantSpiritStoneDust, 5)
                            .outputItems(dust, InactiveRemnantErosionCatalyst)
                            .blastFurnaceTemp(3600)
                            .duration(520).EUt(VA[EV])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("remnant_spirit_stone_reagent_reaction")
                            .inputItems(dust, ActivatedRemnantSpiritStoneDust, 5)
                            .inputItems(SPIRIT_BONE_FRAGMENT)
                            .inputFluids(RemnantErosionActivate, 600)
                            .inputFluids(RemnantSpiritStoneMotherLiquor, 800)
                            .outputFluids(RemnantSpiritStoneReactionSolution, 2000)
                            .outputItems(dust, RemnantSpiritStoneResidue)
                            .duration(740).EUt(VA[IV])
                            .save();

                    DEHYDRATOR_RECIPES.recipeBuilder("remnant_spirit_stone_dehydration_grinding")
                            .inputFluids(RemnantSpiritStoneReactionSolution, 2000)
                            .outputItems(dust, FineRemnantSpiritStoneDust, 5)
                            .outputFluids(RemnantSpiritStoneMotherLiquor, 800)
                            .duration(300).EUt(VA[HV])
                            .save();

                    AUTOCLAVE_RECIPES.recipeBuilder("remnant_spirit_stone_purification")
                            .inputItems(dust, FineRemnantSpiritStoneDust, 5)
                            .inputFluids(EnergySolidifier, 300)
                            .outputItems(dust, PurifiedRemnantSpiritStoneDust)
                            .outputItems(dust, RemnantSpiritStoneResidue)
                            .duration(620).EUt(VA[ZPM])
                            .save();

                    ISOSTATIC_PRESSING_RECIPES.recipeBuilder("remnant_spirit_stone_shaping_infiltration")
                            .inputItems(dust, PurifiedRemnantSpiritStoneDust)
                            .inputItems(SOURCE_SPIRIT_DEBRIS, 2)
                            .inputFluids(CycleofBlossomsSolvent, 200)
                            .outputItems(INFILTRATED_REMNANT_SPIRIT_STONE)
                            .duration(540).EUt(VA[UV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("remnant_spirit_stone_spectral_sifting")
                            .inputItems(INFILTRATED_REMNANT_SPIRIT_STONE)
                            .chancedOutput(gemExquisite, RemnantSpiritStone, 300, 0)
                            .chancedOutput(gemFlawless, RemnantSpiritStone, 4000, 0)
                            .chancedOutput(gem, RemnantSpiritStone, 2500, 0)
                            .chancedOutput(dust, RemnantSpiritStoneResidue, 3500, 0)
                            .duration(660).EUt(VA[ZPM])
                            .save();
                }
            }

            // ================================================================
            // 增产产线（每矿 9 步，1 精炼矿石 → 32 无暇宝石 = 3200%）
            // 从标准线第 6 步的"O 精粉"分支，B9 自行筛出成品后不再回到标准线。
            // 倍率：B1/B3/B5 各 ×2，B7 ×4（1 → 2 → 4 → 8 → 32）。
            // 交叉：B3 吃上游矿常规伴生、B4 吃对角矿稀有伴生、B5 吃下游矿催化剂副产、
            // B6 吃对角矿本矿试剂并以对角矿稀有伴生为共鸣物。
            // 星血晶的催化剂与副产为液体（星脉催化液 / 星脉催化残液），故 B5 用流体入 5 的
            // 大型化学反应釜、B7 用流体入 3 的炼金锅。
            // ================================================================
            {
                // -------------------- 源核晶增产产线（上游星血晶 / 下游骸灵石 / 对角魂玉晶）--------------------
                {
                    MIXER_RECIPES.recipeBuilder("origin_core_crystal_boost_synergize")
                            .inputItems(dust, FineOriginCoreCrystalDust)
                            .inputItems(dust, OriginCoreCrystalResidue, 4)
                            .inputItems(MANA_CRYSTAL, 4)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 800)
                            .outputFluids(SynergizedOriginCoreCrystalSlurry, 2000)
                            .duration(1200).EUt(VA[LuV])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("origin_core_crystal_boost_catalyze")
                            .inputFluids(SynergizedOriginCoreCrystalSlurry, 2000)
                            .inputItems(SOURCE_ENERGY_CATALYST_CRYSTAL, 4)
                            .inputFluids(SourceEnergyExtract, 6000)
                            .outputItems(dust, CatalyzedOriginCoreCrystalDust, 2)
                            .outputItems(SOURCE_ENERGY_CATALYST_CRYSTAL_SHARD, 4)
                            .duration(1700).EUt(VA[ZPM])
                            .save();

                    CRYSTALLIZATION_RECIPES.recipeBuilder("origin_core_crystal_boost_proliferate_1")
                            .inputItems(dust, CatalyzedOriginCoreCrystalDust, 2)
                            .inputItems(STAR_DEBRIS_SAND, 4)
                            .inputFluids(FinalPurifier, 1200)
                            .inputFluids(OriginCoreCrystalMotherLiquor, 800)
                            .outputItems(dust, ProliferatedOriginCoreCrystalDust, 4)
                            .blastFurnaceTemp(3600)
                            .duration(2100).EUt(VA[UV])
                            .save();

                    REACTION_FURNACE_RECIPES.recipeBuilder("origin_core_crystal_boost_coating")
                            .inputItems(dust, ProliferatedOriginCoreCrystalDust, 4)
                            .inputItems(HOLY_ROOT_MYCELIUM, 2)
                            .inputItems(CONSCIOUSNESS_THREAD, 2)
                            .inputFluids(CycleofBlossomsSolvent, 400)
                            .outputFluids(CoatedOriginCoreCrystalMelt, 4000)
                            .outputItems(dust, LeylineProliferationDregs, 6)
                            .blastFurnaceTemp(4200)
                            .duration(2600).EUt(VA[UHV])
                            .save();

                    LARGE_CHEMICAL_RECIPES.recipeBuilder("origin_core_crystal_boost_catalyze_2")
                            .inputFluids(CoatedOriginCoreCrystalMelt, 4000)
                            .inputItems(SOURCE_ENERGY_CATALYST_CRYSTAL, 8)
                            .inputItems(dust, InactiveRemnantErosionCatalyst, 8)
                            .inputFluids(SourceEnergyExtract, 12000)
                            .inputFluids(FinalPurifier, 8400)
                            .outputItems(dust, StrengthenedOriginCoreCrystalDust, 8)
                            .outputItems(SOURCE_ENERGY_CATALYST_CRYSTAL_SHARD, 8)
                            .duration(3400).EUt(VA[UEV])
                            .save();

                    ELEMENTAL_RESONANCE.recipeBuilder("origin_core_crystal_boost_resonance")
                            .inputItems(dust, StrengthenedOriginCoreCrystalDust, 8)
                            .inputItems(MANA_CRYSTAL, 28)
                            .inputFluids(SoulThoughtHarmony, 6000)
                            .inputFluids(OriginCoreCrystalMotherLiquor, 1200)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 2400)
                            .outputFluids(ResonatedOriginCoreCrystalSolution, 4000)
                            .outputItems(dust, LeylineProliferationDregs, 6)
                            .addData(GTORecipeDataKeys.RESONANCE, toResonanceTag(CONSCIOUSNESS_THREAD.asStack(), 8))
                            .MANAt(8192)
                            .duration(4200)
                            .circuitMeta(8)
                            .save();

                    ALCHEMY_CAULDRON_RECIPES.recipeBuilder("origin_core_crystal_boost_proliferate_2")
                            .inputFluids(ResonatedOriginCoreCrystalSolution, 4000)
                            .inputItems(SOURCE_ENERGY_CATALYST_CRYSTAL, 20)
                            .outputItems(dust, CrystallizedOriginCoreCrystalDust, 32)
                            .outputItems(SOURCE_ENERGY_CATALYST_CRYSTAL_SHARD, 20)
                            .outputItems(dust, OriginCoreCrystalResidue, 24)
                            .duration(5000)
                            .temperature(2400)
                            .MANAt(16384)
                            .save();

                    CHEMICAL_BATH_RECIPES.recipeBuilder("origin_core_crystal_boost_giant_crystal")
                            .inputItems(dust, CrystallizedOriginCoreCrystalDust, 32)
                            .inputItems(STAR_DEBRIS_SAND, 64)
                            .inputItems(dust, LeylineProliferationDregs, 12)
                            .inputFluids(CycleofBlossomsSolvent, 6400)
                            .outputItems(ORIGIN_CORE_CRYSTAL_GIANT_POLYCRYSTAL)
                            .outputItems(dust, OriginCoreCrystalResidue, 16)
                            .outputFluids(OriginCoreCrystalMotherLiquor, 2000)
                            .duration(3600).EUt(VA[UEV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("origin_core_crystal_boost_sifting")
                            .inputItems(ORIGIN_CORE_CRYSTAL_GIANT_POLYCRYSTAL)
                            .chancedOutput(gemExquisite, OriginCoreCrystal, 8, 3000, 0)
                            .chancedOutput(gemFlawless, OriginCoreCrystal, 16, 5000, 0)
                            .chancedOutput(gem, OriginCoreCrystal, 32, 2500, 0)
                            .chancedOutput(dust, OriginCoreCrystalResidue, 32, 3000, 0)
                            .duration(900).EUt(VA[UEV])
                            .save();
                }

                // -------------------- 星血晶增产产线（上游魂玉晶 / 下游源核晶 / 对角骸灵石）--------------------
                {
                    MIXER_RECIPES.recipeBuilder("star_blood_crystal_boost_synergize")
                            .inputItems(dust, FineStarBloodCrystalDust)
                            .inputItems(dust, StarBloodCrystalResidue, 4)
                            .inputItems(MANA_CRYSTAL, 4)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 800)
                            .outputFluids(SynergizedStarBloodCrystalSlurry, 2000)
                            .duration(1100).EUt(VA[ZPM])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("star_blood_crystal_boost_catalyze")
                            .inputFluids(SynergizedStarBloodCrystalSlurry, 2000)
                            .inputFluids(StarVeinCatalyst, 2000)
                            .inputFluids(StarVeinFusion, 6000)
                            .outputItems(dust, CatalyzedStarBloodCrystalDust, 2)
                            .outputFluids(StarVeinCatalystWaste, 2000)
                            .duration(1500).EUt(VA[LuV])
                            .save();

                    CRYSTALLIZATION_RECIPES.recipeBuilder("star_blood_crystal_boost_proliferate_1")
                            .inputItems(dust, CatalyzedStarBloodCrystalDust, 2)
                            .inputItems(SOUL_SHADOW_DUST, 4)
                            .inputFluids(EnergySolidifier, 1200)
                            .inputFluids(StarBloodCrystalMotherLiquor, 800)
                            .outputItems(dust, ProliferatedStarBloodCrystalDust, 4)
                            .blastFurnaceTemp(3600)
                            .duration(1900).EUt(VA[ZPM])
                            .save();

                    REACTION_FURNACE_RECIPES.recipeBuilder("star_blood_crystal_boost_coating")
                            .inputItems(dust, ProliferatedStarBloodCrystalDust, 4)
                            .inputItems(VEIN_BLOOD_MUCUS, 2)
                            .inputItems(SPIRIT_BONE_FRAGMENT, 2)
                            .inputFluids(CycleofBlossomsSolvent, 400)
                            .outputFluids(CoatedStarBloodCrystalMelt, 4000)
                            .outputItems(dust, LeylineProliferationDregs, 6)
                            .blastFurnaceTemp(4200)
                            .duration(2400).EUt(VA[UV])
                            .save();

                    LARGE_CHEMICAL_RECIPES.recipeBuilder("star_blood_crystal_boost_catalyze_2")
                            .inputFluids(CoatedStarBloodCrystalMelt, 4000)
                            .inputFluids(StarVeinCatalyst, 4000)
                            .inputItems(SOURCE_ENERGY_CATALYST_CRYSTAL_SHARD, 8)
                            .inputFluids(StarVeinFusion, 12000)
                            .inputFluids(EnergySolidifier, 8400)
                            .outputItems(dust, StrengthenedStarBloodCrystalDust, 8)
                            .outputFluids(StarVeinCatalystWaste, 4000)
                            .duration(3000).EUt(VA[UHV])
                            .save();

                    ELEMENTAL_RESONANCE.recipeBuilder("star_blood_crystal_boost_resonance")
                            .inputItems(dust, StrengthenedStarBloodCrystalDust, 8)
                            .inputItems(MANA_CRYSTAL, 28)
                            .inputFluids(RemnantErosionActivate, 6000)
                            .inputFluids(StarBloodCrystalMotherLiquor, 1200)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 2400)
                            .outputFluids(ResonatedStarBloodCrystalSolution, 4000)
                            .outputItems(dust, LeylineProliferationDregs, 6)
                            .addData(GTORecipeDataKeys.RESONANCE, toResonanceTag(SPIRIT_BONE_FRAGMENT.asStack(), 8))
                            .MANAt(6144)
                            .duration(4000)
                            .circuitMeta(8)
                            .save();

                    ALCHEMY_CAULDRON_RECIPES.recipeBuilder("star_blood_crystal_boost_proliferate_2")
                            .inputFluids(ResonatedStarBloodCrystalSolution, 4000)
                            .inputFluids(StarVeinCatalyst, 10000)
                            .outputItems(dust, CrystallizedStarBloodCrystalDust, 32)
                            .outputItems(dust, StarBloodCrystalResidue, 24)
                            .outputFluids(StarVeinCatalystWaste, 10000)
                            .duration(4600)
                            .temperature(2400)
                            .MANAt(12288)
                            .save();

                    CHEMICAL_BATH_RECIPES.recipeBuilder("star_blood_crystal_boost_giant_crystal")
                            .inputItems(dust, CrystallizedStarBloodCrystalDust, 32)
                            .inputItems(SOUL_SHADOW_DUST, 64)
                            .inputItems(dust, LeylineProliferationDregs, 12)
                            .inputFluids(CycleofBlossomsSolvent, 6400)
                            .outputItems(STAR_BLOOD_CRYSTAL_GIANT_POLYCRYSTAL)
                            .outputItems(dust, StarBloodCrystalResidue, 16)
                            .outputFluids(StarBloodCrystalMotherLiquor, 2000)
                            .duration(3400).EUt(VA[UEV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("star_blood_crystal_boost_sifting")
                            .inputItems(STAR_BLOOD_CRYSTAL_GIANT_POLYCRYSTAL)
                            .chancedOutput(gemExquisite, StarBloodCrystal, 8, 3000, 0)
                            .chancedOutput(gemFlawless, StarBloodCrystal, 16, 5000, 0)
                            .chancedOutput(gem, StarBloodCrystal, 32, 2500, 0)
                            .chancedOutput(dust, StarBloodCrystalResidue, 32, 3000, 0)
                            .duration(800).EUt(VA[UEV])
                            .save();
                }

                // -------------------- 魂玉晶增产产线（上游骸灵石 / 下游星血晶 / 对角源核晶）--------------------
                {
                    MIXER_RECIPES.recipeBuilder("soul_jade_crystal_boost_synergize")
                            .inputItems(dust, FineSoulJadeCrystalDust)
                            .inputItems(dust, SoulJadeCrystalResidue, 4)
                            .inputItems(MANA_CRYSTAL, 4)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 800)
                            .outputFluids(SynergizedSoulJadeCrystalSlurry, 2000)
                            .duration(1250).EUt(VA[LuV])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("soul_jade_crystal_boost_catalyze")
                            .inputFluids(SynergizedSoulJadeCrystalSlurry, 2000)
                            .inputItems(SOUL_THOUGHT_CATALYST_CORE, 4)
                            .inputFluids(SoulThoughtHarmony, 6000)
                            .outputItems(dust, CatalyzedSoulJadeCrystalDust, 2)
                            .outputItems(SOUL_THOUGHT_CATALYST_CORE_SHARD, 4)
                            .duration(1900).EUt(VA[UV])
                            .save();

                    CRYSTALLIZATION_RECIPES.recipeBuilder("soul_jade_crystal_boost_proliferate_1")
                            .inputItems(dust, CatalyzedSoulJadeCrystalDust, 2)
                            .inputItems(BONE_ASH_GRANULE, 4)
                            .inputFluids(FinalPurifier, 1200)
                            .inputFluids(SoulJadeCrystalMotherLiquor, 800)
                            .outputItems(dust, ProliferatedSoulJadeCrystalDust, 4)
                            .blastFurnaceTemp(3600)
                            .duration(2300).EUt(VA[UHV])
                            .save();

                    REACTION_FURNACE_RECIPES.recipeBuilder("soul_jade_crystal_boost_coating")
                            .inputItems(dust, ProliferatedSoulJadeCrystalDust, 4)
                            .inputItems(CONSCIOUSNESS_THREAD, 2)
                            .inputItems(HOLY_ROOT_MYCELIUM, 2)
                            .inputFluids(CycleofBlossomsSolvent, 400)
                            .outputFluids(CoatedSoulJadeCrystalMelt, 4000)
                            .outputItems(dust, LeylineProliferationDregs, 6)
                            .blastFurnaceTemp(4200)
                            .duration(2700).EUt(VA[UHV])
                            .save();

                    LARGE_CHEMICAL_RECIPES.recipeBuilder("soul_jade_crystal_boost_catalyze_2")
                            .inputFluids(CoatedSoulJadeCrystalMelt, 4000)
                            .inputItems(SOUL_THOUGHT_CATALYST_CORE, 8)
                            .inputFluids(StarVeinCatalystWaste, 4000)
                            .inputFluids(SoulThoughtHarmony, 12000)
                            .inputFluids(FinalPurifier, 8400)
                            .outputItems(dust, StrengthenedSoulJadeCrystalDust, 8)
                            .outputItems(SOUL_THOUGHT_CATALYST_CORE_SHARD, 8)
                            .duration(3600).EUt(VA[UEV])
                            .save();

                    ELEMENTAL_RESONANCE.recipeBuilder("soul_jade_crystal_boost_resonance")
                            .inputItems(dust, StrengthenedSoulJadeCrystalDust, 8)
                            .inputItems(MANA_CRYSTAL, 28)
                            .inputFluids(SourceEnergyExtract, 6000)
                            .inputFluids(SoulJadeCrystalMotherLiquor, 1200)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 2400)
                            .outputFluids(ResonatedSoulJadeCrystalSolution, 4000)
                            .outputItems(dust, LeylineProliferationDregs, 6)
                            .addData(GTORecipeDataKeys.RESONANCE, toResonanceTag(HOLY_ROOT_MYCELIUM.asStack(), 8))
                            .MANAt(12288)
                            .duration(4600)
                            .circuitMeta(8)
                            .save();

                    ALCHEMY_CAULDRON_RECIPES.recipeBuilder("soul_jade_crystal_boost_proliferate_2")
                            .inputFluids(ResonatedSoulJadeCrystalSolution, 4000)
                            .inputItems(SOUL_THOUGHT_CATALYST_CORE, 20)
                            .outputItems(dust, CrystallizedSoulJadeCrystalDust, 32)
                            .outputItems(SOUL_THOUGHT_CATALYST_CORE_SHARD, 20)
                            .outputItems(dust, SoulJadeCrystalResidue, 24)
                            .duration(5200)
                            .temperature(2400)
                            .MANAt(20480)
                            .save();

                    CHEMICAL_BATH_RECIPES.recipeBuilder("soul_jade_crystal_boost_giant_crystal")
                            .inputItems(dust, CrystallizedSoulJadeCrystalDust, 32)
                            .inputItems(BONE_ASH_GRANULE, 64)
                            .inputItems(dust, LeylineProliferationDregs, 12)
                            .inputFluids(CycleofBlossomsSolvent, 6400)
                            .outputItems(SOUL_JADE_CRYSTAL_GIANT_POLYCRYSTAL)
                            .outputItems(dust, SoulJadeCrystalResidue, 16)
                            .outputFluids(SoulJadeCrystalMotherLiquor, 2000)
                            .duration(3800).EUt(VA[UEV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("soul_jade_crystal_boost_sifting")
                            .inputItems(SOUL_JADE_CRYSTAL_GIANT_POLYCRYSTAL)
                            .chancedOutput(gemExquisite, SoulJadeCrystal, 8, 3000, 0)
                            .chancedOutput(gemFlawless, SoulJadeCrystal, 16, 5000, 0)
                            .chancedOutput(gem, SoulJadeCrystal, 32, 2500, 0)
                            .chancedOutput(dust, SoulJadeCrystalResidue, 32, 3000, 0)
                            .duration(950).EUt(VA[UEV])
                            .save();
                }

                // -------------------- 骸灵石增产产线（上游源核晶 / 下游魂玉晶 / 对角星血晶）--------------------
                {
                    MIXER_RECIPES.recipeBuilder("remnant_spirit_stone_boost_synergize")
                            .inputItems(dust, FineRemnantSpiritStoneDust)
                            .inputItems(dust, RemnantSpiritStoneResidue, 4)
                            .inputItems(MANA_CRYSTAL, 4)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 800)
                            .outputFluids(SynergizedRemnantSpiritStoneSlurry, 2000)
                            .duration(1300).EUt(VA[ZPM])
                            .save();

                    CHEMICAL_RECIPES.recipeBuilder("remnant_spirit_stone_boost_catalyze")
                            .inputFluids(SynergizedRemnantSpiritStoneSlurry, 2000)
                            .inputItems(dust, RemnantErosionCatalyst, 4)
                            .inputFluids(RemnantErosionActivate, 6000)
                            .outputItems(dust, CatalyzedRemnantSpiritStoneDust, 2)
                            .outputItems(dust, InactiveRemnantErosionCatalyst, 4)
                            .duration(1800).EUt(VA[ZPM])
                            .save();

                    CRYSTALLIZATION_RECIPES.recipeBuilder("remnant_spirit_stone_boost_proliferate_1")
                            .inputItems(dust, CatalyzedRemnantSpiritStoneDust, 2)
                            .inputItems(SOURCE_SPIRIT_DEBRIS, 4)
                            .inputFluids(EnergySolidifier, 1200)
                            .inputFluids(RemnantSpiritStoneMotherLiquor, 800)
                            .outputItems(dust, ProliferatedRemnantSpiritStoneDust, 4)
                            .blastFurnaceTemp(3600)
                            .duration(2200).EUt(VA[UV])
                            .save();

                    REACTION_FURNACE_RECIPES.recipeBuilder("remnant_spirit_stone_boost_coating")
                            .inputItems(dust, ProliferatedRemnantSpiritStoneDust, 4)
                            .inputItems(SPIRIT_BONE_FRAGMENT, 2)
                            .inputItems(VEIN_BLOOD_MUCUS, 2)
                            .inputFluids(CycleofBlossomsSolvent, 400)
                            .outputFluids(CoatedRemnantSpiritStoneMelt, 4000)
                            .outputItems(dust, LeylineProliferationDregs, 6)
                            .blastFurnaceTemp(4200)
                            .duration(3000).EUt(VA[UEV])
                            .save();

                    LARGE_CHEMICAL_RECIPES.recipeBuilder("remnant_spirit_stone_boost_catalyze_2")
                            .inputFluids(CoatedRemnantSpiritStoneMelt, 4000)
                            .inputItems(dust, RemnantErosionCatalyst, 8)
                            .inputItems(SOUL_THOUGHT_CATALYST_CORE_SHARD, 8)
                            .inputFluids(RemnantErosionActivate, 12000)
                            .inputFluids(EnergySolidifier, 8400)
                            .outputItems(dust, StrengthenedRemnantSpiritStoneDust, 8)
                            .outputItems(dust, InactiveRemnantErosionCatalyst, 8)
                            .duration(3800).EUt(VA[UEV])
                            .save();

                    ELEMENTAL_RESONANCE.recipeBuilder("remnant_spirit_stone_boost_resonance")
                            .inputItems(dust, StrengthenedRemnantSpiritStoneDust, 8)
                            .inputItems(MANA_CRYSTAL, 28)
                            .inputFluids(StarVeinFusion, 6000)
                            .inputFluids(RemnantSpiritStoneMotherLiquor, 1200)
                            .inputFluids(TheWaterFromTheWellOfWisdom, 2400)
                            .outputFluids(ResonatedRemnantSpiritStoneSolution, 4000)
                            .outputItems(dust, LeylineProliferationDregs, 6)
                            .addData(GTORecipeDataKeys.RESONANCE, toResonanceTag(VEIN_BLOOD_MUCUS.asStack(), 8))
                            .MANAt(16384)
                            .duration(4800)
                            .circuitMeta(8)
                            .save();

                    ALCHEMY_CAULDRON_RECIPES.recipeBuilder("remnant_spirit_stone_boost_proliferate_2")
                            .inputFluids(ResonatedRemnantSpiritStoneSolution, 4000)
                            .inputItems(dust, RemnantErosionCatalyst, 20)
                            .outputItems(dust, CrystallizedRemnantSpiritStoneDust, 32)
                            .outputItems(dust, InactiveRemnantErosionCatalyst, 20)
                            .outputItems(dust, RemnantSpiritStoneResidue, 24)
                            .duration(5400)
                            .temperature(2400)
                            .MANAt(24576)
                            .save();

                    CHEMICAL_BATH_RECIPES.recipeBuilder("remnant_spirit_stone_boost_giant_crystal")
                            .inputItems(dust, CrystallizedRemnantSpiritStoneDust, 32)
                            .inputItems(SOURCE_SPIRIT_DEBRIS, 64)
                            .inputItems(dust, LeylineProliferationDregs, 12)
                            .inputFluids(CycleofBlossomsSolvent, 6400)
                            .outputItems(REMNANT_SPIRIT_STONE_GIANT_POLYCRYSTAL)
                            .outputItems(dust, RemnantSpiritStoneResidue, 16)
                            .outputFluids(RemnantSpiritStoneMotherLiquor, 2000)
                            .duration(4000).EUt(VA[UEV])
                            .save();

                    SIFTER_RECIPES.recipeBuilder("remnant_spirit_stone_boost_sifting")
                            .inputItems(REMNANT_SPIRIT_STONE_GIANT_POLYCRYSTAL)
                            .chancedOutput(gemExquisite, RemnantSpiritStone, 8, 3000, 0)
                            .chancedOutput(gemFlawless, RemnantSpiritStone, 16, 5000, 0)
                            .chancedOutput(gem, RemnantSpiritStone, 32, 2500, 0)
                            .chancedOutput(dust, RemnantSpiritStoneResidue, 32, 3000, 0)
                            .duration(1000).EUt(VA[UEV])
                            .save();
                }
            }

            // ================================================================
            // 母液调制（启动配方，每矿 1 条）
            // 标准线第 5 步先耗母液、第 6 步才产出，增产线 B3/B6 先耗、B8 才产出，
            // 因此两条线首次运行都要先用本配方备料。
            // 注意：输入只能吃残渣粉，不得加入"命树增产残料"——残料产自增产线 B4/B6，
            // 位于母液之后，会形成循环依赖导致整条线无法启动。
            // ================================================================
            {
                MIXER_RECIPES.recipeBuilder("origin_core_crystal_mother_liquor_preparation")
                        .inputItems(dust, OriginCoreCrystalResidue, 2)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .inputFluids(Mana.getFluid(LIQUID, 500))
                        .outputFluids(OriginCoreCrystalMotherLiquor, 1000)
                        .duration(440).EUt(VA[MV])
                        .save();

                MIXER_RECIPES.recipeBuilder("star_blood_crystal_mother_liquor_preparation")
                        .inputItems(dust, StarBloodCrystalResidue, 2)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .inputFluids(Mana.getFluid(LIQUID, 500))
                        .outputFluids(StarBloodCrystalMotherLiquor, 1000)
                        .duration(400).EUt(VA[MV])
                        .save();

                MIXER_RECIPES.recipeBuilder("soul_jade_crystal_mother_liquor_preparation")
                        .inputItems(dust, SoulJadeCrystalResidue, 2)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .inputFluids(Mana.getFluid(LIQUID, 500))
                        .outputFluids(SoulJadeCrystalMotherLiquor, 1000)
                        .duration(380).EUt(VA[LV])
                        .save();

                MIXER_RECIPES.recipeBuilder("remnant_spirit_stone_mother_liquor_preparation")
                        .inputItems(dust, RemnantSpiritStoneResidue, 2)
                        .inputFluids(TheWaterFromTheWellOfWisdom, 1000)
                        .inputFluids(Mana.getFluid(LIQUID, 500))
                        .outputFluids(RemnantSpiritStoneMotherLiquor, 1000)
                        .duration(460).EUt(VA[MV])
                        .save();
            }
        }
    }
}

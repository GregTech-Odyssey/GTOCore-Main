package com.gtocore.data.tag;

import com.gtocore.api.data.material.GTOMaterialFlags;
import com.gtocore.common.data.*;
import com.gtocore.data.recipe.ae2.GTOInfCells;

import com.gtolib.drone.DroneSlot;
import com.gtolib.utils.TagUtils;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTTags;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

import com.kyanite.deeperdarker.content.DDBlocks;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import earth.terrarium.adastra.common.registry.ModBlocks;
import vazkii.botania.common.BotaniaBlocks;
import vazkii.botania.common.BotaniaTags;

import java.util.Comparator;

public final class TagsHandler {

    public static void initBlock() {
        create(Tags.ALL_LAYER_STONE, Blocks.END_STONE, ModBlocks.MOON_STONE.get(), ModBlocks.MARS_STONE.get(), ModBlocks.VENUS_STONE.get(),
                ModBlocks.MERCURY_STONE.get(), ModBlocks.GLACIO_STONE.get(), GTOBlocks.TITAN_STONE.get(), GTOBlocks.PLUTO_STONE.get(),
                GTOBlocks.IO_STONE.get(), GTOBlocks.GANYMEDE_STONE.get(), GTOBlocks.ENCELADUS_STONE.get(), GTOBlocks.CERES_STONE.get(),
                DDBlocks.SCULK_STONE.get(), DDBlocks.GLOOMSLATE.get(), BotaniaBlocks.getBlock("livingrock"));
        create(CustomTags.MINEABLE_WITH_WRENCH, GTOGlobes.GLOBE_ENTRIES.stream()
                .sorted(Comparator.comparing(s -> s.get().getDescriptionId()))
                .map(RegistryEntry::get).toArray(Block[]::new));
        create(BlockTags.MINEABLE_WITH_PICKAXE, GTOGlobes.GLOBE_ENTRIES.stream()
                .sorted(Comparator.comparing(s -> s.get().getDescriptionId()))
                .map(RegistryEntry::get).toArray(Block[]::new));
        create(CustomTags.MINEABLE_WITH_WRENCH, GTOGlobes.Blocks.GTO_FLAG.get());
        create(BlockTags.MINEABLE_WITH_AXE, GTOGlobes.Blocks.GTO_FLAG.get());
        create(BlockTags.MINEABLE_WITH_AXE, mineableBlocks("botania:abstruse_platform", "botania:alfheim_portal", "botania:avatar", "botania:bellows", "botania:cacophonium_block", "botania:crafty_crate", "botania:dreamwood", "botania:dreamwood_fence", "botania:dreamwood_fence_gate", "botania:dreamwood_log", "botania:dreamwood_planks", "botania:dreamwood_planks_slab", "botania:dreamwood_planks_stairs", "botania:dreamwood_slab", "botania:dreamwood_stairs", "botania:dreamwood_wall", "botania:drum_canopy", "botania:drum_gathering", "botania:drum_wild", "botania:elven_spreader", "botania:fel_pumpkin", "botania:framed_dreamwood", "botania:framed_livingwood", "botania:gaia_spreader", "botania:glimmering_dreamwood", "botania:glimmering_dreamwood_log", "botania:glimmering_livingwood", "botania:glimmering_livingwood_log", "botania:glimmering_stripped_dreamwood", "botania:glimmering_stripped_dreamwood_log", "botania:glimmering_stripped_livingwood", "botania:glimmering_stripped_livingwood_log", "botania:incense_plate", "botania:infrangible_platform", "botania:livingwood", "botania:livingwood_fence", "botania:livingwood_fence_gate", "botania:livingwood_log", "botania:livingwood_planks", "botania:livingwood_planks_slab", "botania:livingwood_planks_stairs", "botania:livingwood_slab", "botania:livingwood_stairs", "botania:livingwood_wall", "botania:mana_bomb", "botania:mana_spreader", "botania:mossy_dreamwood_planks", "botania:mossy_livingwood_planks", "botania:open_crate", "botania:pattern_framed_dreamwood", "botania:pattern_framed_livingwood", "botania:redstone_spreader", "botania:root", "botania:shimmerwood_planks", "botania:shimmerwood_planks_slab", "botania:shimmerwood_planks_stairs", "botania:spectral_platform", "botania:stripped_dreamwood", "botania:stripped_dreamwood_log", "botania:stripped_dreamwood_slab", "botania:stripped_dreamwood_stairs", "botania:stripped_dreamwood_wall", "botania:stripped_livingwood", "botania:stripped_livingwood_log", "botania:stripped_livingwood_slab", "botania:stripped_livingwood_stairs", "botania:stripped_livingwood_wall", "botania:turntable", "extrabotany:mana_charger", "mythicbotany:mana_collector", "mythicbotany:yggdrasil_branch"));
        create(BlockTags.MINEABLE_WITH_HOE, mineableBlocks("botania:black_petal_block", "botania:blue_petal_block", "botania:brown_petal_block", "botania:cell_block", "botania:cyan_petal_block", "botania:gray_petal_block", "botania:green_petal_block", "botania:light_blue_petal_block", "botania:light_gray_petal_block", "botania:lime_petal_block", "botania:magenta_petal_block", "botania:orange_petal_block", "botania:pink_petal_block", "botania:purple_petal_block", "botania:red_petal_block", "botania:white_petal_block", "botania:yellow_petal_block"));
        create(BlockTags.MINEABLE_WITH_PICKAXE, mineableBlocks("botania:alchemy_catalyst", "botania:apothecary_deepslate", "botania:apothecary_default", "botania:apothecary_desert", "botania:apothecary_forest", "botania:apothecary_fungal", "botania:apothecary_livingrock", "botania:apothecary_mesa", "botania:apothecary_mossy", "botania:apothecary_mountain", "botania:apothecary_plains", "botania:apothecary_swamp", "botania:apothecary_taiga", "botania:azulejo_0", "botania:azulejo_1", "botania:azulejo_10", "botania:azulejo_11", "botania:azulejo_12", "botania:azulejo_13", "botania:azulejo_14", "botania:azulejo_15", "botania:azulejo_2", "botania:azulejo_3", "botania:azulejo_4", "botania:azulejo_5", "botania:azulejo_6", "botania:azulejo_7", "botania:azulejo_8", "botania:azulejo_9", "botania:bifrost_pane", "botania:bifrost_perm", "botania:black_pavement", "botania:black_pavement_slab", "botania:black_pavement_stairs", "botania:blaze_block", "botania:blaze_quartz", "botania:blaze_quartz_pillar", "botania:blaze_quartz_slab", "botania:blaze_quartz_stairs", "botania:blue_pavement", "botania:blue_pavement_slab", "botania:blue_pavement_stairs", "botania:brewery", "botania:chiseled_blaze_quartz", "botania:chiseled_dark_quartz", "botania:chiseled_elf_quartz", "botania:chiseled_lavender_quartz", "botania:chiseled_livingrock_bricks", "botania:chiseled_mana_quartz", "botania:chiseled_metamorphic_desert_bricks", "botania:chiseled_metamorphic_forest_bricks", "botania:chiseled_metamorphic_fungal_bricks", "botania:chiseled_metamorphic_mesa_bricks", "botania:chiseled_metamorphic_mountain_bricks", "botania:chiseled_metamorphic_plains_bricks", "botania:chiseled_metamorphic_swamp_bricks", "botania:chiseled_metamorphic_taiga_bricks", "botania:chiseled_red_quartz", "botania:chiseled_sunny_quartz", "botania:conjuration_catalyst", "botania:corporea_block", "botania:corporea_brick", "botania:corporea_brick_slab", "botania:corporea_brick_stairs", "botania:corporea_brick_wall", "botania:corporea_crystal_cube", "botania:corporea_funnel", "botania:corporea_index", "botania:corporea_interceptor", "botania:corporea_retainer", "botania:corporea_slab", "botania:corporea_stairs", "botania:cracked_livingrock_bricks", "botania:creative_pool", "botania:dark_quartz", "botania:dark_quartz_pillar", "botania:dark_quartz_slab", "botania:dark_quartz_stairs", "botania:diluted_pool", "botania:dragonstone_block", "botania:elementium_block", "botania:elf_glass", "botania:elf_glass_pane", "botania:elf_quartz", "botania:elf_quartz_pillar", "botania:elf_quartz_slab", "botania:elf_quartz_stairs", "botania:ender_eye_block", "botania:fabulous_pool", "botania:fluix_mana_pool", "botania:forest_eye", "botania:gaia_pylon", "botania:green_pavement", "botania:green_pavement_slab", "botania:green_pavement_stairs", "botania:hourglass", "botania:large_mana_pool", "botania:lavender_quartz", "botania:lavender_quartz_pillar", "botania:lavender_quartz_slab", "botania:lavender_quartz_stairs", "botania:livingrock", "botania:livingrock_bricks", "botania:livingrock_bricks_slab", "botania:livingrock_bricks_stairs", "botania:livingrock_bricks_wall", "botania:livingrock_slab", "botania:livingrock_slate", "botania:livingrock_stairs", "botania:livingrock_wall", "botania:mana_detector", "botania:mana_diamond_block", "botania:mana_distributor", "botania:mana_fluxfield", "botania:mana_glass", "botania:mana_glass_pane", "botania:mana_pool", "botania:mana_pylon", "botania:mana_quartz", "botania:mana_quartz_pillar", "botania:mana_quartz_slab", "botania:mana_quartz_stairs", "botania:mana_void", "botania:manasteel_block", "botania:metamorphic_desert_bricks", "botania:metamorphic_desert_bricks_slab", "botania:metamorphic_desert_bricks_stairs", "botania:metamorphic_desert_bricks_wall", "botania:metamorphic_desert_cobblestone", "botania:metamorphic_desert_cobblestone_slab", "botania:metamorphic_desert_cobblestone_stairs", "botania:metamorphic_desert_cobblestone_wall", "botania:metamorphic_desert_stone", "botania:metamorphic_desert_stone_slab", "botania:metamorphic_desert_stone_stairs", "botania:metamorphic_desert_stone_wall", "botania:metamorphic_forest_bricks", "botania:metamorphic_forest_bricks_slab", "botania:metamorphic_forest_bricks_stairs", "botania:metamorphic_forest_bricks_wall", "botania:metamorphic_forest_cobblestone", "botania:metamorphic_forest_cobblestone_slab", "botania:metamorphic_forest_cobblestone_stairs", "botania:metamorphic_forest_cobblestone_wall", "botania:metamorphic_forest_stone", "botania:metamorphic_forest_stone_slab", "botania:metamorphic_forest_stone_stairs", "botania:metamorphic_forest_stone_wall", "botania:metamorphic_fungal_bricks", "botania:metamorphic_fungal_bricks_slab", "botania:metamorphic_fungal_bricks_stairs", "botania:metamorphic_fungal_bricks_wall", "botania:metamorphic_fungal_cobblestone", "botania:metamorphic_fungal_cobblestone_slab", "botania:metamorphic_fungal_cobblestone_stairs", "botania:metamorphic_fungal_cobblestone_wall", "botania:metamorphic_fungal_stone", "botania:metamorphic_fungal_stone_slab", "botania:metamorphic_fungal_stone_stairs", "botania:metamorphic_fungal_stone_wall", "botania:metamorphic_mesa_bricks", "botania:metamorphic_mesa_bricks_slab", "botania:metamorphic_mesa_bricks_stairs", "botania:metamorphic_mesa_bricks_wall", "botania:metamorphic_mesa_cobblestone", "botania:metamorphic_mesa_cobblestone_slab", "botania:metamorphic_mesa_cobblestone_stairs", "botania:metamorphic_mesa_cobblestone_wall", "botania:metamorphic_mesa_stone", "botania:metamorphic_mesa_stone_slab", "botania:metamorphic_mesa_stone_stairs", "botania:metamorphic_mesa_stone_wall", "botania:metamorphic_mountain_bricks", "botania:metamorphic_mountain_bricks_slab", "botania:metamorphic_mountain_bricks_stairs", "botania:metamorphic_mountain_bricks_wall", "botania:metamorphic_mountain_cobblestone", "botania:metamorphic_mountain_cobblestone_slab", "botania:metamorphic_mountain_cobblestone_stairs", "botania:metamorphic_mountain_cobblestone_wall", "botania:metamorphic_mountain_stone", "botania:metamorphic_mountain_stone_slab", "botania:metamorphic_mountain_stone_stairs", "botania:metamorphic_mountain_stone_wall", "botania:metamorphic_plains_bricks", "botania:metamorphic_plains_bricks_slab", "botania:metamorphic_plains_bricks_stairs", "botania:metamorphic_plains_bricks_wall", "botania:metamorphic_plains_cobblestone", "botania:metamorphic_plains_cobblestone_slab", "botania:metamorphic_plains_cobblestone_stairs", "botania:metamorphic_plains_cobblestone_wall", "botania:metamorphic_plains_stone", "botania:metamorphic_plains_stone_slab", "botania:metamorphic_plains_stone_stairs", "botania:metamorphic_plains_stone_wall", "botania:metamorphic_swamp_bricks", "botania:metamorphic_swamp_bricks_slab", "botania:metamorphic_swamp_bricks_stairs", "botania:metamorphic_swamp_bricks_wall", "botania:metamorphic_swamp_cobblestone", "botania:metamorphic_swamp_cobblestone_slab", "botania:metamorphic_swamp_cobblestone_stairs", "botania:metamorphic_swamp_cobblestone_wall", "botania:metamorphic_swamp_stone", "botania:metamorphic_swamp_stone_slab", "botania:metamorphic_swamp_stone_stairs", "botania:metamorphic_swamp_stone_wall", "botania:metamorphic_taiga_bricks", "botania:metamorphic_taiga_bricks_slab", "botania:metamorphic_taiga_bricks_stairs", "botania:metamorphic_taiga_bricks_wall", "botania:metamorphic_taiga_cobblestone", "botania:metamorphic_taiga_cobblestone_slab", "botania:metamorphic_taiga_cobblestone_stairs", "botania:metamorphic_taiga_cobblestone_wall", "botania:metamorphic_taiga_stone", "botania:metamorphic_taiga_stone_slab", "botania:metamorphic_taiga_stone_stairs", "botania:metamorphic_taiga_stone_wall", "botania:mossy_livingrock_bricks", "botania:mossy_livingrock_bricks_slab", "botania:mossy_livingrock_bricks_stairs", "botania:mossy_livingrock_bricks_wall", "botania:natura_pylon", "botania:piston_relay", "botania:polished_livingrock", "botania:polished_livingrock_slab", "botania:polished_livingrock_stairs", "botania:polished_livingrock_wall", "botania:prism", "botania:pump", "botania:red_pavement", "botania:red_pavement_slab", "botania:red_pavement_stairs", "botania:red_quartz", "botania:red_quartz_pillar", "botania:red_quartz_slab", "botania:red_quartz_stairs", "botania:red_string_comparator", "botania:red_string_container", "botania:red_string_dispenser", "botania:red_string_fertilizer", "botania:red_string_interceptor", "botania:red_string_relay", "botania:runic_altar", "botania:shimmerrock", "botania:shimmerrock_slab", "botania:shimmerrock_stairs", "botania:spark_changer", "botania:spawner_claw", "botania:starfield", "botania:sunny_quartz", "botania:sunny_quartz_pillar", "botania:sunny_quartz_slab", "botania:sunny_quartz_stairs", "botania:terra_plate", "botania:terrasteel_block", "botania:tiny_planet_block", "botania:white_pavement", "botania:white_pavement_slab", "botania:white_pavement_stairs", "botania:yellow_pavement", "botania:yellow_pavement_slab", "botania:yellow_pavement_stairs", "extrabotany:aerialite_block", "extrabotany:chiseled_elementium_quartz_block", "extrabotany:chiseled_gaia_quartz_block", "extrabotany:dimension_catalyst", "extrabotany:elementium_quartz_block", "extrabotany:elementium_quartz_bricks", "extrabotany:elementium_quartz_pillar", "extrabotany:elementium_quartz_slab", "extrabotany:elementium_quartz_stairs", "extrabotany:gaia_quartz_block", "extrabotany:gaia_quartz_bricks", "extrabotany:gaia_quartz_pillar", "extrabotany:gaia_quartz_slab", "extrabotany:gaia_quartz_stairs", "extrabotany:orichalcos_block", "extrabotany:pedestal_calcite", "extrabotany:pedestal_livingrock", "extrabotany:photonium_block", "extrabotany:power_frame", "extrabotany:shadowium_block", "extrabotany:smooth_elementium_quartz", "extrabotany:smooth_elementium_quartz_slab", "extrabotany:smooth_elementium_quartz_stairs", "extrabotany:smooth_gaia_quartz", "extrabotany:smooth_gaia_quartz_slab", "extrabotany:smooth_gaia_quartz_stairs", "mythicbotany:alfsteel_block", "mythicbotany:alfsteel_pylon", "mythicbotany:dragonstone_ore", "mythicbotany:elementium_ore", "mythicbotany:gold_ore", "mythicbotany:mana_infuser", "mythicbotany:raw_elementium_block"));
        create(BlockTags.NEEDS_DIAMOND_TOOL, mineableBlocks("extrabotany:orichalcos_block", "mythicbotany:dragonstone_ore"));
        create(BlockTags.NEEDS_IRON_TOOL, mineableBlocks("extrabotany:aerialite_block", "mythicbotany:alfsteel_block", "mythicbotany:elementium_ore", "mythicbotany:gold_ore", "mythicbotany:raw_elementium_block"));
        create(BlockTags.NEEDS_STONE_TOOL, mineableBlocks("extrabotany:photonium_block", "extrabotany:shadowium_block"));
        create(BlockTags.MINEABLE_WITH_SHOVEL, mineableBlocks("botania:black_floating_flower", "botania:blue_floating_flower", "botania:brown_floating_flower", "botania:cyan_floating_flower", "botania:dry_grass", "botania:enchanted_soil", "botania:floating_agricarnation", "botania:floating_agricarnation_chibi", "botania:floating_bellethorn", "botania:floating_bellethorn_chibi", "botania:floating_bergamute", "botania:floating_bubbell", "botania:floating_bubbell_chibi", "botania:floating_clayconia", "botania:floating_clayconia_chibi", "botania:floating_daffomill", "botania:floating_dandelifeon", "botania:floating_dreadthorn", "botania:floating_endoflame", "botania:floating_entropinnyum", "botania:floating_exoflame", "botania:floating_fallen_kanade", "botania:floating_gourmaryllis", "botania:floating_heisei_dream", "botania:floating_hopperhock", "botania:floating_hopperhock_chibi", "botania:floating_hyacidus", "botania:floating_hydroangeas", "botania:floating_jaded_amaranthus", "botania:floating_jiyuulia", "botania:floating_jiyuulia_chibi", "botania:floating_kekimurus", "botania:floating_labellia", "botania:floating_loonium", "botania:floating_manastar", "botania:floating_marimorphosis", "botania:floating_marimorphosis_chibi", "botania:floating_medumone", "botania:floating_munchdew", "botania:floating_narslimmus", "botania:floating_orechid", "botania:floating_orechid_ignem", "botania:floating_pollidisiac", "botania:floating_pure_daisy", "botania:floating_rafflowsia", "botania:floating_rannuncarpus", "botania:floating_rannuncarpus_chibi", "botania:floating_rosa_arcana", "botania:floating_shulk_me_not", "botania:floating_solegnolia", "botania:floating_solegnolia_chibi", "botania:floating_spectranthemum", "botania:floating_spectrolus", "botania:floating_tangleberrie", "botania:floating_tangleberrie_chibi", "botania:floating_thermalily", "botania:floating_tigerseye", "botania:floating_vinculotus", "botania:golden_grass", "botania:gray_floating_flower", "botania:green_floating_flower", "botania:infused_grass", "botania:light_blue_floating_flower", "botania:light_gray_floating_flower", "botania:lime_floating_flower", "botania:magenta_floating_flower", "botania:mutated_grass", "botania:orange_floating_flower", "botania:pink_floating_flower", "botania:purple_floating_flower", "botania:red_floating_flower", "botania:scorched_grass", "botania:vivid_grass", "botania:white_floating_flower", "botania:yellow_floating_flower", "extrabotany:floating_annoyingflower", "extrabotany:floating_bellflower", "extrabotany:floating_blood_enchantress", "extrabotany:floating_edelweiss", "extrabotany:floating_enchanter", "extrabotany:floating_manalink", "extrabotany:floating_mirrowtunia", "extrabotany:floating_moonlight_lily", "extrabotany:floating_necrofleur", "extrabotany:floating_necrofleur_chibi", "extrabotany:floating_omniviolet", "extrabotany:floating_reikarlily", "extrabotany:floating_resoncund", "extrabotany:floating_serenitian", "extrabotany:floating_stonesia", "extrabotany:floating_sunshine_lily", "extrabotany:floating_tinkle", "extrabotany:floating_trade_orchid", "extrabotany:floating_twinstar", "extrabotany:floating_woodienia"));

        create(BotaniaTags.Blocks.UNETHICAL_TNT_CHECK,
                GTBlocks.INDUSTRIAL_TNT.get(),
                GTBlocks.POWDERBARREL.get());
        create(CustomTags.CLEANROOM_FLOORS,
                GTOMachines.TRAVEL_ANCHOR.get());
    }

    public static void initItem() {
        create(Tags.HUMAN_EGG, Items.VILLAGER_SPAWN_EGG, Items.WITCH_SPAWN_EGG);
        create(Tags.SIFTABLES,
                GTOItems.CRUDELY_PURIFIED_ORIGIN_CORE_CRYSTAL_ORE.asItem(),
                GTOItems.CRUDELY_FUSED_STAR_BLOOD_CRYSTAL_ORE.asItem(),
                GTOItems.CRUDELY_HARMONIZED_SOUL_JADE_CRYSTAL_ORE.asItem(),
                GTOItems.CRUDELY_SHAPED_REMNANT_SPIRIT_STONE_ORE.asItem());
        create(ResourceLocation.fromNamespaceAndPath("curios", "curio"),
                GTOItems.WIRELESS_ME2IN1.asItem());
        create(ResourceLocation.fromNamespaceAndPath("curios", DroneSlot.CURIOS_SLOT),
                GTOItems.HV_DRONE.asItem(), GTOItems.EV_DRONE.asItem(), GTOItems.IV_DRONE.asItem(), GTOItems.HYPERDIMENSIONAL_DRONE.asItem());
        create(ResourceLocation.parse("forge:ingots/redstone_alloy"),
                ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.RedAlloy));
        var concretes = new Item[GTOInfCells.CONCRETE_BLOCKS.length];
        for (int i = 0; i < concretes.length; i++) {
            concretes[i] = GTOInfCells.CONCRETE_BLOCKS[i].asItem();
        }
        create(TagUtils.createForgeItemTag("concrete"), concretes);
        Item[] isaProcessableItems = GTRegistries.MATERIALS.values().stream()
                .filter(material -> material.hasFlag(GTOMaterialFlags.GENERATE_MILLED))
                .flatMap(material -> TagPrefix.ORES.keySet().stream().map(tagPrefix -> ChemicalHelper.getItem(tagPrefix, material)).filter(item -> item != Items.AIR))
                .sorted(Comparator.comparing(Item::toString))
                .toArray(Item[]::new);
        create(TagUtils.createTGItemTag("isa_processable"), isaProcessableItems);
    }

    public static void initFluid() {
        create(Tags.PURIFY_WATER, GTOMaterials.FilteredSater.getFluid());
        create(Tags.PURIFY_WATER, GTOMaterials.OzoneWater.getFluid());
        create(Tags.PURIFY_WATER, GTOMaterials.FlocculentWater.getFluid());
        create(Tags.PURIFY_WATER, GTOMaterials.PHNeutralWater.getFluid());
        create(Tags.PURIFY_WATER, GTOMaterials.ExtremeTemperatureWater.getFluid());
        create(Tags.PURIFY_WATER, GTOMaterials.ElectricEquilibriumWater.getFluid());
        create(Tags.PURIFY_WATER, GTOMaterials.DegassedWater.getFluid());
        create(Tags.PURIFY_WATER, GTOMaterials.BaryonicPerfectionWater.getFluid());
    }

    private static void create(ResourceLocation location, Block... rls) {
        for (var block : rls) {
            GTTags.addBlockEntry(block, location);
        }
    }

    private static void create(ResourceLocation location, Item... rls) {
        for (var item : rls) {
            GTTags.addItemEntry(item, location);
        }
    }

    private static void create(ResourceLocation location, Fluid... rls) {
        for (var fluid : rls) {
            GTTags.addFluidEntry(fluid, location);
        }
    }

    private static void create(TagKey<Block> tagKey, Block... rls) {
        for (var block : rls) {
            GTTags.addBlockEntry(block, tagKey.location());
        }
    }

    private static void create(TagKey<Item> tagKey, Item... rls) {
        for (var item : rls) {
            GTTags.addItemEntry(item, tagKey.location());
        }
    }

    private static void create(TagKey<Fluid> tagKey, Fluid... rls) {
        for (var fluid : rls) {
            GTTags.addFluidEntry(fluid, tagKey.location());
        }
    }

    /** 把魔法模组的方块按 id 解析成方块，用于挖掘工具标签。 */
    private static Block[] mineableBlocks(String... ids) {
        Block[] blocks = new Block[ids.length];
        for (int i = 0; i < ids.length; i++) {
            blocks[i] = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(new ResourceLocation(ids[i]));
        }
        return blocks;
    }
}

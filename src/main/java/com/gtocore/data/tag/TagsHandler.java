package com.gtocore.data.tag;

import com.gtocore.api.data.material.GTOMaterialFlags;
import com.gtocore.common.data.*;

import com.gtolib.utils.TagUtils;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
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
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.Comparator;

public final class TagsHandler {

    public static void initBlock() {
        create(Tags.ALL_LAYER_STONE, Blocks.END_STONE, ModBlocks.MOON_STONE.get(), ModBlocks.MARS_STONE.get(), ModBlocks.VENUS_STONE.get(),
                ModBlocks.MERCURY_STONE.get(), ModBlocks.GLACIO_STONE.get(), GTOBlocks.TITAN_STONE.get(), GTOBlocks.PLUTO_STONE.get(),
                GTOBlocks.IO_STONE.get(), GTOBlocks.GANYMEDE_STONE.get(), GTOBlocks.ENCELADUS_STONE.get(), GTOBlocks.CERES_STONE.get(),
                DDBlocks.SCULK_STONE.get(), DDBlocks.GLOOMSLATE.get(), BotaniaBlocks.livingrock);
        create(CustomTags.MINEABLE_WITH_WRENCH, GTOGlobes.GLOBE_ENTRIES.stream()
                .sorted(Comparator.comparing(s -> s.get().getDescriptionId()))
                .map(RegistryEntry::get).toArray(Block[]::new));
        create(BlockTags.MINEABLE_WITH_PICKAXE, GTOGlobes.GLOBE_ENTRIES.stream()
                .sorted(Comparator.comparing(s -> s.get().getDescriptionId()))
                .map(RegistryEntry::get).toArray(Block[]::new));
        create(CustomTags.MINEABLE_WITH_WRENCH, GTOGlobes.Blocks.GTO_FLAG.get());
        create(BlockTags.MINEABLE_WITH_AXE, GTOGlobes.Blocks.GTO_FLAG.get());

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
        create(ResourceLocation.parse("forge:ingots/redstone_alloy"),
                ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.RedAlloy));
        Item[] isaProcessableItems = GTCEuAPI.materialManager.getRegistries().stream()
                .flatMap(registry -> registry.getAllMaterials().stream())
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
}

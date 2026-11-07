package com.gtocore.common.data.material;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.kyanite.deeperdarker.content.DDItems;
import earth.terrarium.adastra.common.registry.ModBlocks;
import earth.terrarium.adastra.common.registry.ModItems;
import vazkii.botania.common.*;

import static com.gregtechceu.gtceu.common.data.GTItems.CARBON_FIBERS;
import static com.gregtechceu.gtceu.common.data.GTItems.CARBON_MESH;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.api.data.tag.GTOTagPrefix.*;
import static com.gtocore.common.data.GTOItems.*;
import static com.gtocore.common.data.GTOMaterials.*;

public final class MaterialIgnored {

    public static void init() {
        dustSmall.setIgnored(Mana);
        dustTiny.setIgnored(Mana);
        plateDouble.setIgnored(Livingwood);
        plateDouble.setIgnored(Dreamwood);
        plateDouble.setIgnored(Shimmerwood);
        plateDouble.setIgnored(Livingclay);
        plateDouble.setIgnored(Livingrock);
        plateDouble.setIgnored(Runerock);
        plateDouble.setIgnored(Shimmerrock);
        plateDouble.setIgnored(ManaGlass);
        plateDouble.setIgnored(ElfGlass);
        plateDouble.setIgnored(BifrostPerm);

        OreProperty oreProp = Bastnasite.getProperty(PropertyKey.ORE);
        oreProp.getOreByProducts().clear();
        oreProp.setOreByProducts(Neodymium, Monazite);

        oreProp = Redstone.getProperty(PropertyKey.ORE);
        oreProp.getOreByProducts().clear();
        oreProp.setOreByProducts(Cinnabar, Ruby, Glowstone);

        oreProp = Neodymium.getProperty(PropertyKey.ORE);
        oreProp.getOreByProducts().clear();
        oreProp.setOreByProducts(SamariumRefinedPowder);

        oreProp = Monazite.getProperty(PropertyKey.ORE);
        oreProp.getOreByProducts().clear();
        oreProp.setOreByProducts(Thorium, Neodymium, Bastnasite);

        oreProp = GaiaCore.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(PerditioCrystal, NetherEmber, NetherEmber, Gaia);
        oreProp.setWashedIn(FlowingCiphers, 500);

        oreProp = NetherEmber.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(PerditioCrystal, GaiaCore, GaiaCore, NetherStar);
        oreProp.setWashedIn(Undine, 500);

        TagPrefix.gem.setIgnored(Fluix, () -> AEItems.FLUIX_CRYSTAL);
        TagPrefix.block.setIgnored(Fluix, AEBlocks.FLUIX_BLOCK::block);
        TagPrefix.dust.setIgnored(Fluix, () -> AEItems.FLUIX_DUST);

        TagPrefix.gem.setIgnored(CertusQuartz, () -> AEItems.CERTUS_QUARTZ_CRYSTAL);
        TagPrefix.block.setIgnored(CertusQuartz, AEBlocks.QUARTZ_BLOCK::block);
        TagPrefix.dust.setIgnored(CertusQuartz, () -> AEItems.CERTUS_QUARTZ_DUST);

        TagPrefix.dust.setIgnored(EnderPearl, () -> AEItems.ENDER_DUST);

        TagPrefix.gem.setIgnored(SoulCrystal, () -> DDItems.SOUL_CRYSTAL::get);
        TagPrefix.dust.setIgnored(SoulCrystal, () -> DDItems.SOUL_DUST::get);

        TagPrefix.gem.setIgnored(Resonarium, () -> DDItems.RESONARIUM::get);
        TagPrefix.plate.setIgnored(Resonarium, () -> DDItems.RESONARIUM_PLATE::get);

        TagPrefix.rawOre.setIgnored(Desh, ModItems.RAW_DESH);
        TagPrefix.rawOre.setIgnored(Ostrum, ModItems.RAW_OSTRUM);
        TagPrefix.rawOre.setIgnored(Calorite, ModItems.RAW_CALORITE);
        TagPrefix.rawOreBlock.setIgnored(Desh, ModBlocks.RAW_DESH_BLOCK);
        TagPrefix.rawOreBlock.setIgnored(Ostrum, ModBlocks.RAW_OSTRUM_BLOCK);
        TagPrefix.rawOreBlock.setIgnored(Calorite, ModBlocks.RAW_CALORITE_BLOCK);
        TagPrefix.plate.setIgnored(Iron, ModItems.IRON_PLATE);
        TagPrefix.rod.setIgnored(Iron, ModItems.IRON_ROD);
        TagPrefix.ingot.setIgnored(Steel, ModItems.STEEL_INGOT);
        TagPrefix.nugget.setIgnored(Steel, ModItems.STEEL_NUGGET);
        TagPrefix.plate.setIgnored(Steel, ModItems.STEEL_PLATE);
        TagPrefix.rod.setIgnored(Steel, ModItems.STEEL_ROD);
        TagPrefix.block.setIgnored(Steel, ModBlocks.STEEL_BLOCK);
        TagPrefix.ingot.setIgnored(Desh, ModItems.DESH_INGOT);
        TagPrefix.nugget.setIgnored(Desh, ModItems.DESH_NUGGET);
        TagPrefix.plate.setIgnored(Desh, ModItems.DESH_PLATE);
        TagPrefix.block.setIgnored(Desh, ModBlocks.DESH_BLOCK);
        TagPrefix.ingot.setIgnored(Ostrum, ModItems.OSTRUM_INGOT);
        TagPrefix.nugget.setIgnored(Ostrum, ModItems.OSTRUM_NUGGET);
        TagPrefix.plate.setIgnored(Ostrum, ModItems.OSTRUM_PLATE);
        TagPrefix.block.setIgnored(Ostrum, ModBlocks.OSTRUM_BLOCK);
        TagPrefix.ingot.setIgnored(Calorite, ModItems.CALORITE_INGOT);
        TagPrefix.nugget.setIgnored(Calorite, ModItems.CALORITE_NUGGET);
        TagPrefix.plate.setIgnored(Calorite, ModItems.CALORITE_PLATE);
        TagPrefix.block.setIgnored(Calorite, ModBlocks.CALORITE_BLOCK);

        TagPrefix.dust.setIgnored(Mana, () -> () -> BotaniaItems.getItem("mana_powder"));
        TagPrefix.ingot.setIgnored(Manasteel, () -> () -> BotaniaItems.getItem("manasteel_ingot"));
        TagPrefix.nugget.setIgnored(Manasteel, () -> () -> BotaniaItems.getItem("manasteel_nugget"));
        TagPrefix.block.setIgnored(Manasteel, () -> BotaniaBlocks.getBlock("manasteel_block"));
        TagPrefix.ingot.setIgnored(Terrasteel, () -> () -> BotaniaItems.getItem("terrasteel_ingot"));
        TagPrefix.nugget.setIgnored(Terrasteel, () -> () -> BotaniaItems.getItem("terrasteel_nugget"));
        TagPrefix.block.setIgnored(Terrasteel, () -> BotaniaBlocks.getBlock("terrasteel_block"));
        TagPrefix.ingot.setIgnored(Elementium, () -> () -> BotaniaItems.getItem("elementium_ingot"));
        TagPrefix.nugget.setIgnored(Elementium, () -> () -> BotaniaItems.getItem("elementium_nugget"));
        TagPrefix.block.setIgnored(Elementium, () -> BotaniaBlocks.getBlock("elementium_block"));
        TagPrefix.ingot.setIgnored(Gaia, () -> () -> BotaniaItems.getItem("gaia_ingot"));
        TagPrefix.gem.setIgnored(ManaDiamond, () -> () -> BotaniaItems.getItem("mana_diamond"));
        TagPrefix.block.setIgnored(ManaDiamond, () -> BotaniaBlocks.getBlock("mana_diamond_block"));
        TagPrefix.gem.setIgnored(Dragonstone, () -> () -> BotaniaItems.getItem("dragonstone"));
        TagPrefix.block.setIgnored(Dragonstone, () -> BotaniaBlocks.getBlock("dragonstone_block"));
        TagPrefix.gem.setIgnored(SourceGem, () -> ItemsRegistry.SOURCE_GEM);
        TagPrefix.block.setIgnored(SourceGem, () -> BlockRegistry.SOURCE_GEM_BLOCK);

        TagPrefix.nugget.setIgnored(Orichalcos, () -> ExtraBotanyItems.getItem("orichalcos_nugget"));
        TagPrefix.ingot.setIgnored(Orichalcos, () -> ExtraBotanyItems.getItem("orichalcos_ingot"));
        TagPrefix.block.setIgnored(Orichalcos, () -> ExtraBotanyBlocks.getBlock("orichalcos_block"));
        TagPrefix.nugget.setIgnored(Photonium, () -> ExtraBotanyItems.getItem("photonium_nugget"));
        TagPrefix.ingot.setIgnored(Photonium, () -> ExtraBotanyItems.getItem("photonium_ingot"));
        TagPrefix.block.setIgnored(Photonium, () -> ExtraBotanyBlocks.getBlock("photonium_block"));
        TagPrefix.nugget.setIgnored(Shadowium, () -> ExtraBotanyItems.getItem("shadowium_nugget"));
        TagPrefix.ingot.setIgnored(Shadowium, () -> ExtraBotanyItems.getItem("shadowium_ingot"));
        TagPrefix.block.setIgnored(Shadowium, () -> ExtraBotanyBlocks.getBlock("shadowium_block"));
        TagPrefix.nugget.setIgnored(Aerialite, () -> ExtraBotanyItems.getItem("aerialite_nugget"));
        TagPrefix.ingot.setIgnored(Aerialite, () -> ExtraBotanyItems.getItem("aerialite_ingot"));
        TagPrefix.block.setIgnored(Aerialite, () -> ExtraBotanyBlocks.getBlock("aerialite_block"));
        TagPrefix.ingot.setIgnored(Alfsteel, () -> () -> MythicBotanyItems.getItem("alfsteel_ingot"));
        TagPrefix.nugget.setIgnored(Alfsteel, () -> () -> MythicBotanyItems.getItem("alfsteel_nugget"));
        TagPrefix.block.setIgnored(Alfsteel, () -> MythicBotanyBlocks.getBlock("alfsteel_block"));

        TagPrefix.block.setIgnored(Livingwood, () -> BotaniaBlocks.getBlock("livingwood_planks"));
        TagPrefix.block.setIgnored(Dreamwood, () -> BotaniaBlocks.getBlock("dreamwood_planks"));
        TagPrefix.block.setIgnored(Shimmerwood, () -> BotaniaBlocks.getBlock("shimmerwood_planks"));
        TagPrefix.block.setIgnored(Livingrock, () -> BotaniaBlocks.getBlock("livingrock"));
        TagPrefix.block.setIgnored(Shimmerrock, () -> BotaniaBlocks.getBlock("shimmerrock"));
        TagPrefix.block.setIgnored(ManaGlass, () -> BotaniaBlocks.getBlock("mana_glass"));
        TagPrefix.block.setIgnored(ElfGlass, () -> BotaniaBlocks.getBlock("elf_glass"));
        TagPrefix.block.setIgnored(BifrostPerm, () -> BotaniaBlocks.getBlock("bifrost_perm"));

        COIN.setIgnored(Neutron, () -> NEUTRON_COIN);

        ingot.setIgnored(Etrium, ModItems.ETRIUM_INGOT);
        nugget.setIgnored(Etrium, ModItems.ETRIUM_NUGGET);
        block.setIgnored(Etrium, ModBlocks.ETRIUM_BLOCK);
        plate.setIgnored(Etrium, ModItems.ETRIUM_PLATE);
        rod.setIgnored(Etrium, ModItems.ETRIUM_ROD);

        FIBER_TOW.setIgnored(Carbon, () -> CARBON_FIBERS);
        FIBER_MESH.setIgnored(Carbon, () -> CARBON_MESH);
        FIBER.setIgnored(Kevlar, () -> KEVLAR_FIBER);
        FIBER_MESH.setIgnored(Kevlar, () -> WOVEN_KEVLAR);
        FIBER.setIgnored(BorosilicateGlass, () -> ChemicalHelper.get(wireFine, BorosilicateGlass).getItem());

        NANO.setIgnored(SiliconCarbide, () -> ChemicalHelper.get(dust, NanoScaleSiliconCarbide).getItem());
        NANO.setIgnored(Tungsten, () -> ChemicalHelper.get(dust, NanoScaleTungsten).getItem());
        NANO.setIgnored(Alumina, () -> ChemicalHelper.get(dust, NanoScaleAlumina).getItem());
        NANO.setIgnored(YttriumOxide, () -> ChemicalHelper.get(dust, NanoScaleYttria).getItem());
        NANO.setIgnored(Niobium, () -> ChemicalHelper.get(dust, NanoScaleNiobium).getItem());
        NANO.setIgnored(Zirconium, () -> ChemicalHelper.get(dust, NanoScaleZirconium).getItem());
        NANO.setIgnored(Rhenium, () -> ChemicalHelper.get(dust, NanoScaleRhenium).getItem());
        NANO.setIgnored(Molybdenum, () -> ChemicalHelper.get(dust, NanoScaleMolybdenum).getItem());
        NANO.setIgnored(Aluminium, () -> ChemicalHelper.get(dust, NanoAluminum).getItem());

        block.modifyMaterialAmount(Fluix, 4);
    }
}

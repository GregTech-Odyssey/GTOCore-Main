package com.gtocore.common.data;

import com.gtolib.GTOCore;
import com.gtolib.api.lang.CNEN;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.fluids.FluidStack;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import java.util.*;
import java.util.function.Consumer;

import static com.gtolib.api.data.GTODimensions.*;

@SuppressWarnings("unused")
public final class GTOBedrockFluids {

    public static final Map<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

    public static final Map<ResourceKey<Level>, List<FluidStack>> ALL_BEDROCK_FLUID = new O2OOpenCacheHashMap<>();

    private static final BedrockFluidDefinition VOID_HEAVY_OIL = create(GTCEu.id("void_heavy_oil_deposit"),
            "虚空重油矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.HeavyOilWithSand::getFluid : GTMaterials.OilHeavy::getFluid)
                    .weight(15)
                    .yield(100, 200)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(20)
                    .dimensions(Collections.singleton(VOID)));

    private static final BedrockFluidDefinition VOID_LIGHT_OIL = create(GTCEu.id("void_light_oil_deposit"),
            "虚空轻油矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.LightOilWithSand::getFluid : GTMaterials.OilLight::getFluid)
                    .weight(25)
                    .yield(175, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(25)
                    .dimensions(Collections.singleton(VOID)));

    private static final BedrockFluidDefinition VOID_NATURAL_GAS = create(GTCEu.id("void_natural_gas_deposit"),
            "虚空天然气矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.ShaleGas::getFluid : GTMaterials.NaturalGas::getFluid)
                    .weight(15)
                    .yield(100, 175)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(20)
                    .dimensions(Collections.singleton(VOID)));

    private static final BedrockFluidDefinition VOID_OIL = create(GTCEu.id("void_oil_deposit"),
            "虚空石油矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.TightOilCrudeOil::getFluid : GTMaterials.Oil::getFluid)
                    .weight(20)
                    .yield(175, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(25)
                    .dimensions(Collections.singleton(VOID)));

    private static final BedrockFluidDefinition VOID_RAW_OIL = create(GTCEu.id("void_raw_oil_deposit"),
            "虚空原油矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.ShaleOil::getFluid : GTMaterials.RawOil::getFluid)
                    .weight(20)
                    .yield(200, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(25)
                    .dimensions(Collections.singleton(VOID)));

    private static final BedrockFluidDefinition VOID_ALT_WATER = create(GTCEu.id("void_salt_water_deposit"),
            "虚空盐水矿藏",
            builder -> builder
                    .fluid(GTMaterials.SaltWater::getFluid)
                    .weight(10)
                    .yield(50, 100)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(15)
                    .dimensions(Collections.singleton(VOID)));

    private static final BedrockFluidDefinition FLAT_HEAVY_OIL = create(GTCEu.id("flat_heavy_oil_deposit"),
            "超平坦重油矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.HeavyOilWithSand::getFluid : GTMaterials.OilHeavy::getFluid)
                    .weight(15)
                    .yield(100, 200)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(20)
                    .dimensions(Collections.singleton(FLAT)));

    private static final BedrockFluidDefinition FLAT_LIGHT_OIL = create(GTCEu.id("flat_light_oil_deposit"),
            "超平坦轻油矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.LightOilWithSand::getFluid : GTMaterials.OilLight::getFluid)
                    .weight(25)
                    .yield(175, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(25)
                    .dimensions(Collections.singleton(FLAT)));

    private static final BedrockFluidDefinition FLAT_NATURAL_GAS = create(GTCEu.id("flat_natural_gas_deposit"),
            "超平坦天然气矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.ShaleGas::getFluid : GTMaterials.NaturalGas::getFluid)
                    .weight(15)
                    .yield(100, 175)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(20)
                    .dimensions(Collections.singleton(FLAT)));

    private static final BedrockFluidDefinition FLAT_OIL = create(GTCEu.id("flat_oil_deposit"),
            "超平坦石油矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.TightOilCrudeOil::getFluid : GTMaterials.Oil::getFluid)
                    .weight(20)
                    .yield(175, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(25)
                    .dimensions(Collections.singleton(FLAT)));

    private static final BedrockFluidDefinition FLAT_RAW_OIL = create(GTCEu.id("flat_raw_oil_deposit"),
            "超平坦原油矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.ShaleOil::getFluid : GTMaterials.RawOil::getFluid)
                    .weight(20)
                    .yield(200, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(25)
                    .dimensions(Collections.singleton(FLAT)));

    private static final BedrockFluidDefinition FLAT_ALT_WATER = create(GTCEu.id("flat_salt_water_deposit"),
            "超平坦盐水矿藏",
            builder -> builder
                    .fluid(GTMaterials.SaltWater::getFluid)
                    .weight(10)
                    .yield(50, 100)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(15)
                    .dimensions(Collections.singleton(FLAT)));

    private static final BedrockFluidDefinition HELIUM_3 = create(GTCEu.id("helium3_deposit"),
            "氦-3矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.EasyToEscapeMixedGas::getFluid : GTMaterials.Helium3::getFluid)
                    .weight(10)
                    .yield(50, 180)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(MOON)));

    private static final BedrockFluidDefinition HELIUM = create(GTCEu.id("helium_deposit"),
            "氦矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.EasyToEscapeMixedGas::getFluid : GTMaterials.Helium::getFluid)
                    .weight(20)
                    .yield(50, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(MOON)));

    private static final BedrockFluidDefinition SULFURIC_ACID = create(GTCEu.id("sulfuric_acid_deposit"),
            "硫酸矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.ImpureSulfuricAcid::getFluid : GTMaterials.SulfuricAcid::getFluid)
                    .weight(20)
                    .yield(100, 250)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(VENUS)));

    private static final BedrockFluidDefinition DEUTERIUM = create(GTCEu.id("deuterium_deposit"),
            "氘矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.FlashExplosionGas::getFluid : GTMaterials.Deuterium::getFluid)
                    .weight(15)
                    .yield(80, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(MERCURY)));

    private static final BedrockFluidDefinition RADON = create(GTCEu.id("radon_deposit"),
            "氡矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.HighRadiationGas::getFluid : GTMaterials.Radon::getFluid)
                    .weight(20)
                    .yield(50, 80)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(MARS)));

    private static final BedrockFluidDefinition CERES_RADON = create(GTCEu.id("ceres_radon_deposit"),
            "谷神星氡矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.HighRadiationGas::getFluid : GTMaterials.Radon::getFluid)
                    .weight(15)
                    .yield(100, 250)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(CERES)));

    private static final BedrockFluidDefinition METHANE = create(GTCEu.id("methane_deposit"),
            "甲烷矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.LightGas::getFluid : GTMaterials.Methane::getFluid)
                    .weight(20)
                    .yield(100, 250)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(TITAN)));

    private static final BedrockFluidDefinition BENZENE = create(GTCEu.id("benzene_deposit"),
            "苯矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.AromaticHydrocarbonMixture::getFluid : GTMaterials.Benzene::getFluid)
                    .weight(15)
                    .yield(60, 160)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(TITAN)));

    private static final BedrockFluidDefinition CHARCOAL_BYPRODUCTS = create(GTCEu.id("charcoal_byproducts"),
            "木炭副产矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.ResidualOilMixture::getFluid : GTMaterials.CharcoalByproducts::getFluid)
                    .weight(10)
                    .yield(80, 260)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(TITAN)));

    private static final BedrockFluidDefinition COAL_GAS = create(GTCEu.id("coal_gas_deposit"),
            "煤气矿藏",
            builder -> builder
                    .fluid(GTMaterials.CoalGas::getFluid)
                    .weight(20)
                    .yield(100, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(IO)));

    private static final BedrockFluidDefinition NITRIC_ACID = create(GTCEu.id("nitric_acid_deposit"),
            "硝酸矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.ImpureNitricAcid::getFluid : GTMaterials.NitricAcid::getFluid)
                    .weight(20)
                    .yield(80, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(PLUTO)));

    private static final BedrockFluidDefinition HYDROCHLORIC_ACID = create(GTCEu.id("hydrochloric_acid_deposit"),
            "盐酸矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.MixedHydrochloricAcid::getFluid : GTMaterials.HydrochloricAcid::getFluid)
                    .weight(20)
                    .yield(100, 350)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(GANYMEDE)));

    private static final BedrockFluidDefinition CERES_XENON = create(GTCEu.id("ceres_xenon_deposit"),
            "氙矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.MixedXenon::getFluid : GTMaterials.Xenon::getFluid)
                    .weight(20)
                    .yield(100, 250)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(CERES)));

    private static final BedrockFluidDefinition CERES_KRYPTON = create(GTCEu.id("ceres_krypton_deposit"),
            "氪矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.MixedKrypton::getFluid : GTMaterials.Krypton::getFluid)
                    .weight(20)
                    .yield(100, 250)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(CERES)));

    private static final BedrockFluidDefinition CERES_NEON = create(GTCEu.id("ceres_neon_deposit"),
            "氖矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.MixedNeon::getFluid : GTMaterials.Neon::getFluid)
                    .weight(20)
                    .yield(100, 250)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(CERES)));

    private static final BedrockFluidDefinition FLUORINE = create(GTCEu.id("fluorine_deposit"),
            "氟矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.MixedFluorine::getFluid : GTMaterials.Fluorine::getFluid)
                    .weight(10)
                    .yield(180, 320)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(ENCELADUS)));

    private static final BedrockFluidDefinition CHLORINE = create(GTCEu.id("chlorine_deposit"),
            "氯矿藏",
            builder -> builder
                    .fluid(GTOCore.isExpert() ? GTOMaterials.BleachingGas::getFluid : GTMaterials.Chlorine::getFluid)
                    .weight(20)
                    .yield(180, 420)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(ENCELADUS)));

    private static final BedrockFluidDefinition UNKNOWWATER = create(GTCEu.id("unknowwater_deposit"),
            "不明液体矿藏",
            builder -> builder
                    .fluid(GTOMaterials.UnknowWater::getFluid)
                    .weight(20)
                    .yield(40, 60)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(Collections.singleton(BARNARDA_C)));

    private static final BedrockFluidDefinition FRACTAL_PETAL_SOLVENT = create(GTCEu.id("fractal_petal_solvent_deposit"),
            "碎蕊调和溶剂矿藏",
            builder -> builder
                    .fluid(GTOMaterials.FractalPetalSolvent::getFluid)
                    .weight(180)
                    .yield(10, 30)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(5)
                    .dimensions(Collections.singleton(ALFHEIM)));

    private static final BedrockFluidDefinition THE_WATER_FROM_THE_WELL_OF_WISDOM = create(GTCEu.id("the_water_from_the_well_of_wisdom_deposit"),
            "智慧之泉水矿藏",
            builder -> builder
                    .fluid(GTOMaterials.TheWaterFromTheWellOfWisdom::getFluid)
                    .weight(40)
                    .yield(20, 40)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(5)
                    .biomes(5000, ResourceKey.create(Registries.BIOME, RLUtils.fromNamespaceAndPath("mythicbotany", "alfheim_lakes")))
                    .dimensions(Collections.singleton(ALFHEIM)));

    private static final BedrockFluidDefinition ANIMIUM = create(GTCEu.id("animium_deposit"),
            "灵髓液矿藏",
            builder -> builder
                    .fluid(GTOMaterials.Animium::getFluid)
                    .weight(20)
                    .yield(10, 20)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(5)
                    .biomes(100, ResourceKey.create(Registries.BIOME, RLUtils.fromNamespaceAndPath("mythicbotany", "golden_fields")))
                    .dimensions(Collections.singleton(ALFHEIM)));

    public static BedrockFluidDefinition HEAVY_OIL = create(GTCEu.id("heavy_oil_deposit"), "重油矿藏", builder -> builder
            .fluid(GTOCore.isExpert() ? GTOMaterials.HeavyOilWithSand::getFluid : GTMaterials.OilHeavy::getFluid)
            .weight(15)
            .yield(100, 200)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20)
            .biomes(5, BiomeTags.IS_OCEAN)
            .biomes(10, CustomTags.IS_SANDY)
            .dimensions(overworld()));

    public static BedrockFluidDefinition LIGHT_OIL = create(GTCEu.id("light_oil_deposit"), "轻油矿藏", builder -> builder
            .fluid(GTOCore.isExpert() ? GTOMaterials.LightOilWithSand::getFluid : GTMaterials.OilLight::getFluid)
            .weight(25)
            .yield(175, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .dimensions(overworld()));

    public static BedrockFluidDefinition NATURAL_GAS = create(GTCEu.id("natural_gas_deposit"), "天然气矿藏", builder -> builder
            .fluid(GTOCore.isExpert() ? GTOMaterials.ShaleGas::getFluid : GTMaterials.NaturalGas::getFluid)
            .weight(15)
            .yield(100, 175)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(20)
            .dimensions(overworld()));

    public static BedrockFluidDefinition OIL = create(GTCEu.id("oil_deposit"), "石油矿藏", builder -> builder
            .fluid(GTOCore.isExpert() ? GTOMaterials.ShaleOil::getFluid : GTMaterials.Oil::getFluid)
            .weight(20)
            .yield(175, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .biomes(5, BiomeTags.IS_OCEAN)
            .biomes(5, CustomTags.IS_SANDY)
            .dimensions(overworld()));

    public static BedrockFluidDefinition RAW_OIL = create(GTCEu.id("raw_oil_deposit"), "原油矿藏", builder -> builder
            .fluid(GTOCore.isExpert() ? GTOMaterials.TightOilCrudeOil::getFluid : GTMaterials.RawOil::getFluid)
            .weight(20)
            .yield(200, 300)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(25)
            .dimensions(overworld()));

    public static BedrockFluidDefinition SALT_WATER = create(GTCEu.id("salt_water_deposit"), "盐水矿藏", builder -> builder
            .fluid(GTMaterials.SaltWater::getFluid)
            .weight(0)
            .yield(50, 100)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(15)
            .dimensions(overworld())
            .biomes(200, Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_FROZEN_OCEAN)
            .biomes(150, BiomeTags.IS_OCEAN));

    //////////////////////////////////////
    // ******** NETHER ********//
    //////////////////////////////////////
    public static BedrockFluidDefinition LAVA = create(GTCEu.id("lava_deposit"), "熔岩矿藏", builder -> builder
            .fluid(GTMaterials.Lava::getFluid)
            .weight(65)
            .yield(125, 250)
            .depletionAmount(1)
            .depletionChance(100)
            .depletedYield(30)
            .dimensions(nether()));

    public static BedrockFluidDefinition NETHER_NATURAL_GAS = create(GTCEu.id("nether_natural_gas_deposit"), "下界天然气矿藏",
            builder -> builder.fluid(GTOCore.isExpert() ? GTOMaterials.ShaleGas::getFluid : GTMaterials.NaturalGas::getFluid)
                    .weight(35)
                    .yield(150, 300)
                    .depletionAmount(1)
                    .depletionChance(100)
                    .depletedYield(40)
                    .dimensions(nether()));

    private static BedrockFluidDefinition create(ResourceLocation id, String cn, Consumer<BedrockFluidDefinition.Builder> consumer) {
        if (LANG != null) {
            String name = id.getPath();
            CNEN lang = new CNEN(cn, FormattingUtil.toEnglishName(name));
            if (LANG.containsKey(name)) {
                GTOCore.LOGGER.error("Repetitive Key: {}", id);
            }
            if (LANG.containsValue(lang)) {
                GTOCore.LOGGER.error("Repetitive Value: {}", lang);
            }
            LANG.put(name, lang);
            return null;
        }
        BedrockFluidDefinition.Builder builder = BedrockFluidDefinition.builder(id);
        consumer.accept(builder);
        var definition = builder.register();
        addVoid(definition);
        return definition;
    }

    public static void addVoid(BedrockFluidDefinition definition) {
        ResourceKey<Level> dimension = definition.dimensionFilter.iterator().next();
        if (dimension != VOID || dimension != FLAT || dimension != CREATE) {
            List<FluidStack> fluidStacks = ALL_BEDROCK_FLUID.computeIfAbsent(dimension, k -> new ArrayList<>());
            fluidStacks.add(new FluidStack(definition.getStoredFluid().get(), Math.max(1, definition.getMaximumYield() * definition.getWeight())));
            ALL_BEDROCK_FLUID.put(dimension, fluidStacks);
        }
    }

    public static Set<ResourceKey<Level>> overworld() {
        return Collections.singleton(Level.OVERWORLD);
    }

    public static Set<ResourceKey<Level>> nether() {
        return Collections.singleton(Level.NETHER);
    }

    public static void init() {}
}

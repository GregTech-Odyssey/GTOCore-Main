package vazkii.botania.common.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.RegisterEvent;

/**
 * 神话植物学 Alfheim 世界生成类型的复刻注册。
 *
 * <p>
 * 维度与群系数据来自神话植物学，缺少这些注册类型时对应 JSON 无法加载。
 * </p>
 */
public final class MythicBotanyWorldgen {

    public static final Feature<AbandonedApothecaryConfiguration> ABANDONED_APOTHECARIES = new AbandonedApothecaryFeature();
    public static final Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration> MANA_CRYSTALS = new ManaCrystalFeature();
    public static final Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration> MOTIF_FLOWERS = new MotifFlowerFeature();
    public static final Feature<net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration> WHEAT_FIELDS = new WheatFeature();

    public static final FoliagePlacerType<RandomFoliagePlacer> RANDOM_FOLIAGE = new FoliagePlacerType<>(RandomFoliagePlacer.CODEC);
    public static final TrunkPlacerType<ShatteredTrunkPlacer> SHATTERED_TRUNK = new TrunkPlacerType<>(ShatteredTrunkPlacer.CODEC);
    public static final PlacementModifierType<AlfheimGroundModifier> ALFHEIM_GROUND = () -> AlfheimGroundModifier.CODEC;

    private MythicBotanyWorldgen() {}

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.FEATURE)) {
            event.register(Registries.FEATURE, id("abandoned_apothecaries"), () -> ABANDONED_APOTHECARIES);
            event.register(Registries.FEATURE, id("mana_crystals"), () -> MANA_CRYSTALS);
            event.register(Registries.FEATURE, id("motif_flowers"), () -> MOTIF_FLOWERS);
            event.register(Registries.FEATURE, id("wheat_fields"), () -> WHEAT_FIELDS);
        } else if (event.getRegistryKey().equals(Registries.FOLIAGE_PLACER_TYPE)) {
            event.register(Registries.FOLIAGE_PLACER_TYPE, id("random_foliage"), () -> RANDOM_FOLIAGE);
        } else if (event.getRegistryKey().equals(Registries.TRUNK_PLACER_TYPE)) {
            event.register(Registries.TRUNK_PLACER_TYPE, id("shattered_trunk"), () -> SHATTERED_TRUNK);
        } else if (event.getRegistryKey().equals(Registries.PLACEMENT_MODIFIER_TYPE)) {
            event.register(Registries.PLACEMENT_MODIFIER_TYPE, id("alfheim_ground"), () -> ALFHEIM_GROUND);
        }
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("mythicbotany", path);
    }
}

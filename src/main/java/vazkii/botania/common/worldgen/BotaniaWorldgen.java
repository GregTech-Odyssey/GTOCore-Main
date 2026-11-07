package vazkii.botania.common.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.RegisterEvent;

/** 植物魔法自然生成类型的注册：神秘花地块与蘑菇地块共用同一个 Feature。 */
public final class BotaniaWorldgen {

    public static final Feature<MysticalFlowerConfig> MYSTICAL_FLOWER = new MysticalFlowerFeature(MysticalFlowerConfig.CODEC);

    private BotaniaWorldgen() {}

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.FEATURE)) {
            event.register(Registries.FEATURE, new ResourceLocation("botania", "mystical_flower"), () -> MYSTICAL_FLOWER);
        }
    }
}

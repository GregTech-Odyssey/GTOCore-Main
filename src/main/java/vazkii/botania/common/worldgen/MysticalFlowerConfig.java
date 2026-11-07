package vazkii.botania.common.worldgen;

import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 神秘花/蘑菇地块的配置：要放置的方块状态与长成高神秘花的概率。 */
public record MysticalFlowerConfig(float tallChance, BlockStateProvider toPlace) implements FeatureConfiguration {

    public static final Codec<MysticalFlowerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("tall_chance").forGetter(MysticalFlowerConfig::tallChance),
            BlockStateProvider.CODEC.fieldOf("to_place").forGetter(MysticalFlowerConfig::toPlace))
            .apply(instance, MysticalFlowerConfig::new));
}

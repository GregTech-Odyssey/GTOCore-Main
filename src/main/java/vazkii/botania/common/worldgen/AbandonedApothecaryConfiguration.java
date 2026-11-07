package vazkii.botania.common.worldgen;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record AbandonedApothecaryConfiguration(List<BlockState> states, List<Item> petals) implements FeatureConfiguration {

    public static final Codec<AbandonedApothecaryConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.listOf().fieldOf("states").forGetter(AbandonedApothecaryConfiguration::states),
            ForgeRegistries.ITEMS.getCodec().listOf().fieldOf("petals").forGetter(AbandonedApothecaryConfiguration::petals))
            .apply(instance, AbandonedApothecaryConfiguration::new));
}

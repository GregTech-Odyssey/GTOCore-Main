package vazkii.botania.common.alfheim;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** 亚尔夫海姆维度键。 */
public final class Alfheim {

    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation("mythicbotany", "alfheim"));

    private Alfheim() {}

    public static boolean isAlfheim(Level level) {
        return level.dimension() == DIMENSION;
    }
}

package vazkii.botania.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

/** 水晶模型层与烘焙定义。 */
public final class PylonModelLayers {

    public static final ModelLayerLocation PIXIE = make("pixie");
    public static final ModelLayerLocation PYLON_MANA = make("pylon_mana");
    public static final ModelLayerLocation PYLON_NATURA = make("pylon_natura");
    public static final ModelLayerLocation PYLON_GAIA = make("pylon_gaia");

    private PylonModelLayers() {}

    private static ModelLayerLocation make(String name) {
        return new ModelLayerLocation(new ResourceLocation("botania", name), "main");
    }

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PIXIE, () -> LayerDefinition.create(PixieModel.createMesh(), 64, 32));
        event.registerLayerDefinition(PYLON_MANA, () -> LayerDefinition.create(ManaPylonModel.createMesh(), 64, 64));
        event.registerLayerDefinition(PYLON_NATURA, () -> LayerDefinition.create(NaturaPylonModel.createMesh(), 64, 64));
        event.registerLayerDefinition(PYLON_GAIA, () -> LayerDefinition.create(GaiaPylonModel.createMesh(), 64, 64));
    }
}

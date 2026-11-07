package vazkii.botania.client.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

/** 复刻内容用到的模型层。 */
public final class BotaniaModelLayers {

    public static final ModelLayerLocation CORPOREA_INDEX = new ModelLayerLocation(
            new ResourceLocation("botania", "corporea_index"), "main");

    private BotaniaModelLayers() {}

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CORPOREA_INDEX, BotaniaModelLayers::createCorporeaIndex);
    }

    public static LayerDefinition createCorporeaIndex() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("ring", CubeListBuilder.create()
                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }
}

package vazkii.botania.client.model.armor;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

/** 盔甲模型层。内层为护腿，外层为其余部位。 */
public final class ArmorModelLayers {

    public static final ModelLayerLocation MANASTEEL_INNER = make("manasteel_inner_armor");
    public static final ModelLayerLocation MANASTEEL_OUTER = make("manasteel_outer_armor");
    public static final ModelLayerLocation ELEMENTIUM_INNER = make("elementium_inner_armor");
    public static final ModelLayerLocation ELEMENTIUM_OUTER = make("elementium_outer_armor");
    public static final ModelLayerLocation TERRASTEEL_INNER = make("terrasteel_inner_armor");
    public static final ModelLayerLocation TERRASTEEL_OUTER = make("terrasteel_outer_armor");
    public static final ModelLayerLocation MANAWEAVE_INNER = make("manaweave_inner_armor");
    public static final ModelLayerLocation MANAWEAVE_OUTER = make("manaweave_outer_armor");

    private ArmorModelLayers() {}

    private static ModelLayerLocation make(String name) {
        return new ModelLayerLocation(new ResourceLocation("botania", name), "main");
    }

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MANASTEEL_INNER, () -> LayerDefinition.create(ManasteelArmorModel.createInsideMesh(), 64, 128));
        event.registerLayerDefinition(MANASTEEL_OUTER, () -> LayerDefinition.create(ManasteelArmorModel.createOutsideMesh(), 64, 128));
        event.registerLayerDefinition(ELEMENTIUM_INNER, () -> LayerDefinition.create(ElementiumArmorModel.createInsideMesh(), 64, 128));
        event.registerLayerDefinition(ELEMENTIUM_OUTER, () -> LayerDefinition.create(ElementiumArmorModel.createOutsideMesh(), 64, 128));
        event.registerLayerDefinition(TERRASTEEL_INNER, () -> LayerDefinition.create(TerrasteelArmorModel.createInsideMesh(), 64, 128));
        event.registerLayerDefinition(TERRASTEEL_OUTER, () -> LayerDefinition.create(TerrasteelArmorModel.createOutsideMesh(), 64, 128));
        event.registerLayerDefinition(MANAWEAVE_INNER, () -> LayerDefinition.create(ManaweaveRobeModel.createInsideMesh(), 64, 128));
        event.registerLayerDefinition(MANAWEAVE_OUTER, () -> LayerDefinition.create(ManaweaveRobeModel.createOutsideMesh(), 64, 128));
    }
}

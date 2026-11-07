package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.model.AlfPixieModel;
import vazkii.botania.client.model.PylonModelLayers;
import vazkii.botania.common.entity.AlfPixie;

/** 亚尔夫海姆精灵渲染。 */
public class AlfPixieRender extends MobRenderer<AlfPixie, AlfPixieModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("mythicbotany", "textures/entity/alf_pixie.png");

    public AlfPixieRender(EntityRendererProvider.Context context) {
        super(context, new AlfPixieModel(context.bakeLayer(PylonModelLayers.PIXIE)), 0);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull AlfPixie entity) {
        return TEXTURE;
    }
}

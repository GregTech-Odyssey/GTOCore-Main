package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.client.model.PixieModel;
import vazkii.botania.client.model.PylonModelLayers;
import vazkii.botania.common.entity.PixieEntity;

/**
 * 精灵渲染：普通精灵与盖亚战中的暗精灵共用模型，仅贴图不同。
 */
public class PixieRenderer extends MobRenderer<PixieEntity, PixieModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("botania", "model/pixie");
    private static final ResourceLocation TEXTURE_HALLOWEEN = new ResourceLocation("botania", "model/pixie_halloween");

    public PixieRenderer(EntityRendererProvider.Context context) {
        super(context, new PixieModel(context.bakeLayer(PylonModelLayers.PIXIE)), 0.0F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull PixieEntity entity) {
        return entity.getPixieType() == 1 ? TEXTURE_HALLOWEEN : TEXTURE;
    }
}

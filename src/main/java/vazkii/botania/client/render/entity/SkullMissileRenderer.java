package vazkii.botania.client.render.entity;

import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.entity.SkullMissileEntity;

/** 头颅导弹渲染：骷髅头模型。 */
public class SkullMissileRenderer extends EntityRenderer<SkullMissileEntity> {

    private static final ResourceLocation SKULL = new ResourceLocation("extrabotany",
            "textures/entity/skull_missile/skull_missile.png");
    private final SkullModel model;

    public SkullMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(ModelLayers.PLAYER_HEAD));
    }

    @Override
    public void render(@NotNull SkullMissileEntity entity, float yaw, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light) {
        ms.pushPose();
        ms.scale(-1.0F, -1.0F, 1.0F);
        ms.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.model.setupAnim(0.0F, entity.getYRot(), entity.getXRot());
        this.model.renderToBuffer(ms, buffers.getBuffer(this.model.renderType(SKULL)), light,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        ms.popPose();
        super.render(entity, yaw, partialTicks, ms, buffers, light);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull SkullMissileEntity entity) {
        return SKULL;
    }
}

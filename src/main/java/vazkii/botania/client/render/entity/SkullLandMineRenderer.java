package vazkii.botania.client.render.entity;

import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.entity.SkullLandMineEntity;

/** 头颅地雷渲染：闪烁的玩家头颅，临近爆炸时放大。 */
public class SkullLandMineRenderer extends EntityRenderer<SkullLandMineEntity> {

    private final SkullModel model;

    public SkullLandMineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SkullModel(context.bakeLayer(ModelLayers.PLAYER_HEAD));
    }

    @Override
    public void render(@NotNull SkullLandMineEntity entity, float entityYaw, float partialTick, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int packedLight) {
        ms.pushPose();
        int fuse = entity.getExplodeCountDown();
        if ((float) fuse - partialTick + 1.0F < 10.0F) {
            float scaleFactor = 1.0F - ((float) fuse - partialTick + 1.0F) / 10.0F;
            scaleFactor = Mth.clamp(scaleFactor, 0.0F, 1.0F);
            scaleFactor *= scaleFactor;
            scaleFactor *= scaleFactor;
            float scale = 1.0F + scaleFactor * 0.3F;
            ms.scale(scale, scale, scale);
        }
        ms.scale(-1.0F, -1.0F, 1.0F);
        ms.mulPose(Axis.YP.rotationDegrees(180.0F));
        int overlay = fuse / 5 % 2 == 0 ? OverlayTexture.NO_OVERLAY : OverlayTexture.pack(10, 10);
        this.model.setupAnim(0.0F, entity.getYRot(), entity.getXRot());
        this.model.renderToBuffer(ms, buffers.getBuffer(this.model.renderType(getTextureLocation(entity))),
                packedLight, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        ms.popPose();
        super.render(entity, entityYaw, partialTick, ms, buffers, packedLight);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull SkullLandMineEntity entity) {
        return entity.getTexture();
    }
}

package vazkii.botania.client.render.block_entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;

/** 多媒体索引的环与核心渲染，物品栏中同样复用（{@code index} 为 null）。 */
public class CorporeaIndexBlockEntityRenderer implements BlockEntityRenderer<CorporeaIndexBlockEntity> {

    private static final RenderType LAYER = RenderType.entityCutoutNoCull(
            new ResourceLocation("botania", "textures/model/corporea_index.png"));
    private static final float ANGLE = (float) Math.sin(Math.toRadians(45));

    private final ModelPart ring;
    private final ModelPart cube;

    public CorporeaIndexBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(BotaniaModelLayers.CORPOREA_INDEX);
        this.ring = root.getChild("ring");
        this.cube = root.getChild("cube");
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull CorporeaIndexBlockEntity blockEntity) {
        return true;
    }

    @Override
    public void render(@Nullable CorporeaIndexBlockEntity index, float partialTicks, @NotNull PoseStack ms,
                       @NotNull MultiBufferSource buffers, int light, int overlay) {
        ms.pushPose();
        ms.translate(0.5, 0, 0.5);

        float rotation = (ClientTickHandler.ticksInGame + partialTicks) * 2;
        if (index == null) {
            ms.scale(1.3f, 1.3f, 1.3f);
            ms.translate(0, -0.1, 0);
        }

        VertexConsumer buffer = buffers.getBuffer(LAYER);
        ms.pushPose();
        ms.translate(0.0D, -1, 0.0D);
        ms.mulPose(Axis.YP.rotationDegrees(rotation));
        ms.translate(0.0D, 1.5F, 0.0D);
        ms.mulPose(new Quaternionf().rotateAxis((float) Math.toRadians(60.0F), ANGLE, 0.0F, ANGLE));
        this.ring.render(ms, buffer, light, overlay);
        ms.scale(0.875F, 0.875F, 0.875F);
        ms.mulPose(new Quaternionf().rotateAxis((float) Math.toRadians(60.0F), ANGLE, 0.0F, ANGLE));
        ms.mulPose(Axis.YP.rotationDegrees(rotation));
        this.ring.render(ms, buffer, light, overlay);
        ms.scale(0.875F, 0.875F, 0.875F);
        ms.mulPose(new Quaternionf().rotateAxis((float) Math.toRadians(60.0F), ANGLE, 0.0F, ANGLE));
        ms.mulPose(Axis.YP.rotationDegrees(rotation));
        this.cube.render(ms, buffer, light, overlay);
        ms.popPose();
        ms.popPose();
    }
}

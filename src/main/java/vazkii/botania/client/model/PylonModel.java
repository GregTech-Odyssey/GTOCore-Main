package vazkii.botania.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * 水晶模型的渲染分组：环与晶体分开绘制，以便各自独立旋转与浮动。
 */
public interface PylonModel {

    void renderRing(PoseStack ms, VertexConsumer buffer, int light, int overlay);

    void renderCrystal(PoseStack ms, VertexConsumer buffer, int light, int overlay);
}

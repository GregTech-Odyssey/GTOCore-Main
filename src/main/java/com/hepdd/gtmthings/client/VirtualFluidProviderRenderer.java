package com.hepdd.gtmthings.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.item.VirtualFluidProviderBehavior;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

public final class VirtualFluidProviderRenderer implements IRenderer {

    public static final VirtualFluidProviderRenderer INSTANCE = new VirtualFluidProviderRenderer();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        FluidStack fluid = VirtualFluidProviderBehavior.getVirtualFluid(stack);
        poseStack.pushPose();
        if (!fluid.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
            var fluidSprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTypeExtensions.getStillTexture(fluid));

            int color = fluidTypeExtensions.getTintColor(fluid);
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            float a = ((color >> 24) & 0xFF) / 255.0F;

            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            RenderSystem.enableBlend();

            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            Matrix4f matrix = poseStack.last().pose();
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder builder = tess.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

            float minU = fluidSprite.getU0();
            float maxU = fluidSprite.getU1();
            float minV = fluidSprite.getV0();
            float maxV = fluidSprite.getV1();

            builder.vertex(matrix, 1, 1, 0).color(r, g, b, a).uv(maxU, minV).endVertex();
            builder.vertex(matrix, 0, 1, 0).color(r, g, b, a).uv(minU, minV).endVertex();
            builder.vertex(matrix, 0, 0, 0).color(r, g, b, a).uv(minU, maxV).endVertex();
            builder.vertex(matrix, 1, 0, 0).color(r, g, b, a).uv(maxU, maxV).endVertex();

            tess.end();
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
        if (transformType == ItemDisplayContext.GUI) {
            poseStack.pushPose();
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder builder = tess.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            var sprite = ModelFactory.getBlockSprite(GTMThings.id("item/virtual_fluid_provider"));
            RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            float minU = sprite.getU0();
            float maxU = sprite.getU1();
            float minV = sprite.getV0();
            float maxV = sprite.getV1();
            Matrix4f pos = poseStack.last().pose();
            builder.vertex(pos, 1, 1, 0).uv(maxU, minV).endVertex();
            builder.vertex(pos, 0, 1, 0).uv(minU, minV).endVertex();
            builder.vertex(pos, 0, 0, 0).uv(minU, maxV).endVertex();
            builder.vertex(pos, 1, 0, 0).uv(maxU, maxV).endVertex();
            tess.end();
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}

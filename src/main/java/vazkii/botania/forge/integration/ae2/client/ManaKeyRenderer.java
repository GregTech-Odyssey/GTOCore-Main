package vazkii.botania.forge.integration.ae2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;

import appeng.api.client.AEKeyRenderHandler;

import com.mojang.blaze3d.vertex.PoseStack;
import vazkii.botania.forge.integration.ae2.ManaKey;

final class ManaKeyRenderer implements AEKeyRenderHandler<ManaKey> {

    private static TextureAtlasSprite sprite() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new net.minecraft.resources.ResourceLocation("botania", "block/mana_ae_icon"));
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, ManaKey stack) {
        guiGraphics.blit(x, y, 0, 16, 16, sprite());
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, ManaKey what, float scale, int combinedLight, Level level) {
        var sprite = sprite();
        var pose = poseStack.last();
        var buffer = buffers.getBuffer(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
        float half = scale / 2;
        buffer.vertex(pose.pose(), -half, half, 0).color(255, 255, 255, 255).uv(sprite.getU0(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(pose.normal(), 0, 0, 1).endVertex();
        buffer.vertex(pose.pose(), -half, -half, 0).color(255, 255, 255, 255).uv(sprite.getU0(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(pose.normal(), 0, 0, 1).endVertex();
        buffer.vertex(pose.pose(), half, -half, 0).color(255, 255, 255, 255).uv(sprite.getU1(), sprite.getV1()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(pose.normal(), 0, 0, 1).endVertex();
        buffer.vertex(pose.pose(), half, half, 0).color(255, 255, 255, 255).uv(sprite.getU1(), sprite.getV0()).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(pose.normal(), 0, 0, 1).endVertex();
    }

    @Override
    public Component getDisplayName(ManaKey stack) {
        return stack.getDisplayName();
    }
}

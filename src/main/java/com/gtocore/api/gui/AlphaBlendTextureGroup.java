package com.gtocore.api.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.mojang.blaze3d.systems.RenderSystem;

/** 按纹理顺序使用源 Alpha 混合，支持半透明图层叠加。 */
public final class AlphaBlendTextureGroup extends GuiTextureGroup {

    public AlphaBlendTextureGroup(IGuiTexture... textures) {
        super(textures);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void preDraw(GuiGraphics graphics, float x, float y, float width, float height) {
        super.preDraw(graphics, x, y, width, height);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void postDraw(GuiGraphics graphics, float x, float y, float width, float height) {
        RenderSystem.disableBlend();
        super.postDraw(graphics, x, y, width, height);
    }
}

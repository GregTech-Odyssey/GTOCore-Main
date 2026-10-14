package com.gtocore.client.renderer;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import org.lwjgl.opengl.GL14;

import java.io.IOException;

public final class SolarGlobeGlowShader extends ShaderInstance {

    private static final BlendMode OPAQUE_BLEND = new BlendMode();

    public SolarGlobeGlowShader(ResourceProvider resourceProvider, ResourceLocation shaderLocation) throws IOException {
        super(resourceProvider, shaderLocation, DefaultVertexFormat.POSITION_TEX);
    }

    @Override
    public void apply() {
        // BlendMode only updates lastApplied on opaque/translucent transitions. Force that
        // transition so super.apply() caches the same mode used by vanilla position_tex.
        OPAQUE_BLEND.apply();
        super.apply();
        // Change the actual draw to additive without replacing the vanilla-compatible cache.
        RenderSystem.enableBlend();
        RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
    }
}

package com.gtocore.integration.emi.research;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.client.renderer.GTORenderTypes;

import com.gtolib.GTOCore;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Set;

import static com.gtocore.integration.emi.research.EmiResearchHelper.*;

public class ResearchTagEmiStack extends EmiStack {

    private static final int ICON_SIZE = 16;
    private static final float SHADER_PADDING = 8.0F;

    public final ResearchTag tag;

    public ResearchTagEmiStack(ResearchTag tag) {
        super();
        this.tag = tag;
    }

    @Override
    public boolean isEqual(EmiStack stack) {
        return stack instanceof ResearchTagEmiStack other && other.tag == this.tag;
    }

    @Override
    public EmiStack copy() {
        return new ResearchTagEmiStack(tag).setAmount(amount).setChance(chance);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
        if ((flags & RENDER_ICON) != 0) {
            ResourceLocation texture = GTOCore.id("textures/gui/researchtags/" + tag.getName() + ".png");
            graphics.blit(texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            graphics.flush();
            renderShader(graphics, texture, x, y);
        }
        if ((flags & RENDER_AMOUNT) != 0 && amount != 1L) {
            EmiRenderHelper.renderAmount(EmiDrawContext.wrap(graphics), x, y, Component.literal(Long.toString(amount)));
        }
    }

    private void renderShader(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        ShaderInstance shader = GTORenderTypes.getShader(GTORenderTypes.ITEM_RESONANCE_WAVE);
        if (shader == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Matrix4f pose = graphics.pose().last().pose();
        float guiScale = (float) minecraft.getWindow().getGuiScale();
        float maskWidth = ICON_SIZE * (float) Math.hypot(pose.m00(), pose.m01()) * guiScale;
        float maskHeight = ICON_SIZE * (float) Math.hypot(pose.m10(), pose.m11()) * guiScale;

        applyShaderParams(shader, minecraft, maskWidth, maskHeight);
        int textureId = minecraft.getTextureManager().getTexture(texture).getId();
        shader.setSampler("Sampler0", textureId);

        int color = tag.getColor();
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float alpha = (color >>> 24) / 255.0F;

        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, textureId);

        try {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            float minX = x - SHADER_PADDING;
            float minY = y - SHADER_PADDING;
            float maxX = x + ICON_SIZE + SHADER_PADDING;
            float maxY = y + ICON_SIZE + SHADER_PADDING;
            float uvPadding = SHADER_PADDING / ICON_SIZE;
            builder.vertex(pose, maxX, maxY, 0.0F).uv(1.0F + uvPadding, 1.0F + uvPadding).endVertex();
            builder.vertex(pose, minX, maxY, 0.0F).uv(-uvPadding, 1.0F + uvPadding).endVertex();
            builder.vertex(pose, minX, minY, 0.0F).uv(-uvPadding, -uvPadding).endVertex();
            builder.vertex(pose, maxX, minY, 0.0F).uv(1.0F + uvPadding, -uvPadding).endVertex();
            if (shader.MODEL_VIEW_MATRIX != null) {
                shader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
            }
            if (shader.PROJECTION_MATRIX != null) {
                shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
            }
            if (shader.COLOR_MODULATOR != null) {
                shader.COLOR_MODULATOR.set(red, green, blue, alpha);
            }
            shader.apply();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            BufferUploader.draw(builder.end());
        } finally {
            shader.clear();
            shader.safeGetUniform("useLocalMaskUv").set(0.0F);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
        }
    }

    private static void applyShaderParams(ShaderInstance shader, Minecraft minecraft, float maskWidth, float maskHeight) {
        shader.safeGetUniform("time").set((float) (System.currentTimeMillis() % 100000L) / 1000.0F);
        shader.safeGetUniform("resolution").set((float) minecraft.getWindow().getScreenWidth(), (float) minecraft.getWindow().getScreenHeight());
        shader.safeGetUniform("mousePos").set(
                (float) (minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth()),
                (float) (minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight()));
        shader.safeGetUniform("maskTextureSize").set(maskWidth, maskHeight);
        shader.safeGetUniform("maskViewportOrigin").set(0.0F, 0.0F);
        shader.safeGetUniform("useLocalMaskUv").set(1.0F);
        shader.safeGetUniform("outlineColor").set(1F, 1F, 1F, 1F);
        shader.safeGetUniform("waveColor").set(1F, 1F, 1F, 1F);
        shader.safeGetUniform("outlineWidth").set(0.75F);
        shader.safeGetUniform("waveStart").set(0.05F);
        shader.safeGetUniform("waveWidth").set(0.03F);
        shader.safeGetUniform("waveSpacing").set(6.0F);
        shader.safeGetUniform("waveSpeed").set(6.6F);
        shader.safeGetUniform("waveLifetime").set(0.05F);
        shader.safeGetUniform("waveDecay").set(0.9F);
        shader.safeGetUniform("waveStrength").set(0.6F);
        shader.safeGetUniform("maxDistance").set(8.0F);
        shader.safeGetUniform("edgeSoftness").set(0.95F);
        shader.safeGetUniform("alphaCutoff").set(0.1F);
        shader.safeGetUniform("overlayPadding").set(0.5F);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public CompoundTag getNbt() {
        return null;
    }

    @Override
    public Object getKey() {
        return tag;
    }

    @Override
    public ResourceLocation getId() {
        return GTOCore.id("research_tag/" + tag.getName());
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(getName(),
                Component.translatable(DOMAIN_DATA_DESC).withStyle(ChatFormatting.DARK_GRAY),
                Component.translatable(TEAM_TOTAL_DESC).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return getTooltipText().stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable(DOMAIN_DATA_NAME, tag.getDisplayName().withStyle(style -> style.withColor(tag.getColor())));
    }

    public static void registerResearchTagEmiStack(Set<EmiStack> c) {
        for (var it : ResearchTag.TAGS.values()) {
            c.add(new ResearchTagEmiStack(it));
        }
    }
}

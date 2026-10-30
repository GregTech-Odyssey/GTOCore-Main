package com.gtocore.client.renderer.sky;

import com.gtocore.client.renderer.GTORenderTypes;
import com.gtocore.common.data.StarMapDysonSync;

import net.minecraft.client.renderer.ShaderInstance;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import java.util.function.Supplier;

/** 天空中的细密蜂巢由片元着色器直接采样，前后半球各用一个四边形。 */
final class DysonSkyShell {

    private static final float MASK_PADDING = 1.1f;
    private static final float HEX_SIZE_SCALE = 0.15f;
    private static final float ORIGINAL_MASK_SCALE = 40 / 24f;
    private static final Supplier<ShaderInstance> SHADER = () -> GTORenderTypes.getShader(GTORenderTypes.DYSON_SKY_SHELL_SHADER_LOCATION);

    private float completion, rotation, tilt, seconds;

    void prepare(int launched, long millis) {
        completion = Math.clamp(launched / (float) StarMapDysonSync.COMPLETE, 0, 1);
        rotation = (float) ((millis % 90_000) * (2 * Math.PI / 90_000));
        tilt = (float) (Math.toRadians(25) * Math.sin((millis % 180_000) * (2 * Math.PI / 180_000)));
        seconds = millis / 1000f;
    }

    static float maskHalf(float coreHalf) {
        return coreHalf * MASK_PADDING;
    }

    static float gridScale(float starHalf, float coreHalf) {
        // 遮罩收缩后同步补偿网格频率，使最终可见六边形边长仍为旧版的 15%。
        return maskHalf(coreHalf) / (starHalf * ORIGINAL_MASK_SCALE * HEX_SIZE_SCALE);
    }

    void drawSky(Matrix4f matrix, float starHalf, float coreHalf, float distance, float alpha, boolean rear) {
        if (completion <= 0 || alpha <= 0) return;
        var shader = SHADER.get();
        if (shader == null) return;
        float half = maskHalf(coreHalf);
        shader.safeGetUniform("Completion").set(completion);
        shader.safeGetUniform("GridScale").set(gridScale(starHalf, coreHalf));
        shader.safeGetUniform("StarCoreRatio").set(coreHalf / half);
        shader.safeGetUniform("Rotation").set(rotation, tilt);
        shader.safeGetUniform("Time").set(seconds);
        shader.safeGetUniform("Hemisphere").set(rear ? -1f : 1f);
        shader.safeGetUniform("Opacity").set(rear ? alpha / 3 : alpha);
        RenderSystem.setShader(SHADER);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        var builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, -half, distance, -half).uv(0, 0).endVertex();
        builder.vertex(matrix, half, distance, -half).uv(1, 0).endVertex();
        builder.vertex(matrix, half, distance, half).uv(1, 1).endVertex();
        builder.vertex(matrix, -half, distance, half).uv(0, 1).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }
}

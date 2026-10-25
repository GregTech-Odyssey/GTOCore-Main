package com.gtocore.client.renderer;

import com.gtolib.GTOCore;
import com.gtolib.api.data.Galaxy;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class GTORenderTypes extends RenderType {

    private static ShaderInstance blackHoleEventHorizonShader;
    private static ShaderInstance dimensionallyTranscendentOverlayShader;
    private static ShaderInstance stellarForgeVortexShader;
    private static ShaderInstance solarSurfaceVortexShader;
    private static final Map<ResourceLocation, ShaderInstance> SHADERS = new ConcurrentHashMap<>();

    private static final ShaderStateShard BLACK_HOLE_EVENT_HORIZON_SHADER = new ShaderStateShard(() -> Objects.requireNonNull(blackHoleEventHorizonShader, "Black hole shader not loaded"));
    private static final ShaderStateShard DIMENSIONALLY_TRANSCENDENT_OVERLAY_SHADER = new ShaderStateShard(() -> Objects.requireNonNull(dimensionallyTranscendentOverlayShader, "Dimensionally transcendent overlay shader not loaded"));
    private static final ShaderStateShard STELLAR_FORGE_VORTEX_SHADER = new ShaderStateShard(() -> Objects.requireNonNull(stellarForgeVortexShader, "Stellar forge vortex shader not loaded"));
    private static final ShaderStateShard SOLAR_SURFACE_VORTEX_SHADER = new ShaderStateShard(() -> Objects.requireNonNull(solarSurfaceVortexShader, "Solar surface vortex shader not loaded"));
    // Match the shader JSON on every draw, including when ShaderInstance reuses its blend-state cache.
    private static final TransparencyStateShard DYSON_FORCE_FIELD_TRANSPARENCY = new TransparencyStateShard("dyson_force_field_transparency", GTORenderTypes::setupDysonForceFieldBlend, GTORenderTypes::clearDysonForceFieldBlend);

    private static void setupDysonForceFieldBlend() {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    private static void clearDysonForceFieldBlend() {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static final RenderType LIGHT_CYLINDER = RenderType.create("light_cylinder",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP, 256, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(NO_CULL)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .createCompositeState(false));
    public static final RenderType LIGHT_TRIANGLES = RenderType.create("light_triangles",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 131072, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(CULL)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .createCompositeState(false));
    public static final RenderType LIGHT_CYLINDER_TEXTURED = RenderType.create("light_cylinder_textured",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLE_STRIP, 131072, true, false,
            RenderType.CompositeState.builder()
                    .setLightmapState(LIGHTMAP)
                    .setCullState(NO_CULL)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setShaderState(RenderStateShard.POSITION_TEX_SHADER)
                    .createCompositeState(false));
    public static final RenderType BLACK_HOLE_CORE = RenderType.create("black_hole_core",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 131072, false, false,
            RenderType.CompositeState.builder()
                    .setCullState(NO_CULL)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .createCompositeState(false));
    public static final RenderType BLACK_HOLE_EVENT_HORIZON = RenderType.create("black_hole_event_horizon",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 131072, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setShaderState(BLACK_HOLE_EVENT_HORIZON_SHADER)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));
    public static final RenderType DIMENSIONALLY_TRANSCENDENT_OVERLAY = RenderType.create("dimensionally_transcendent_overlay",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 131072, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setShaderState(DIMENSIONALLY_TRANSCENDENT_OVERLAY_SHADER)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));
    public static final RenderType STELLAR_FORGE_VORTEX = RenderType.create("stellar_forge_vortex",
            DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.TRIANGLES, 262144, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setShaderState(STELLAR_FORGE_VORTEX_SHADER)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType SOLAR_SURFACE_VORTEX = RenderType.create("solar_surface_vortex",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES, 256, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setShaderState(SOLAR_SURFACE_VORTEX_SHADER)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType SOLAR_STORM_HEAT = RenderType.create("solar_storm_heat",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES, 256, false, false,
            RenderType.CompositeState.builder()
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setShaderState(new ShaderStateShard(() -> getShader(GTORenderTypes.SOLAR_STORM_HEAT_SHADER_LOCATION)))
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType SOLAR_STORM_PARTICLES = RenderType.create("solar_storm_particles",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 131072, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType DYSON_FORCE_FIELD = RenderType.create("dyson_force_field",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES, 256, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(DYSON_FORCE_FIELD_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setShaderState(new ShaderStateShard(() -> getShader(GTORenderTypes.DYSON_FORCE_FIELD_SHADER_LOCATION)))
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType SOLAR_ORBIT_SUN = RenderType.create("solar_orbit_sun",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES, 256, false, false,
            RenderType.CompositeState.builder()
                    .setTextureState(new TextureStateShard(Galaxy.SOLAR.getStar(), false, false))
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setShaderState(POSITION_TEX_SHADER)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType SOLAR_GLOBE = RenderType.create("solar_globe",
            DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setShaderState(new ScopedShaderStateShard(() -> getShader(GTORenderTypes.SOLAR_GLOBE_SHADER_LOCATION)))
                    .createCompositeState(false));

    public static final RenderType SOLAR_GLOBE_GLOW = RenderType.create("solar_globe_glow",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setWriteMaskState(COLOR_WRITE)
                    .setShaderState(new ScopedShaderStateShard(() -> getShader(GTORenderTypes.SOLAR_GLOBE_GLOW_SHADER_LOCATION)))
                    .createCompositeState(false));

    public static final RenderType SOLAR_STORM_REACTOR = RenderType.create("solar_storm_reactor",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setCullState(NO_CULL)
                    .setShaderState(new ShaderStateShard(() -> getShader(GTORenderTypes.SOLAR_STORM_REACTOR_SHADER_LOCATION)))
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType SOLAR_STORM_REACTOR_RINGS = RenderType.create("solar_storm_reactor_rings",
            DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 131072, false, false,
            RenderType.CompositeState.builder()
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setShaderState(new ShaderStateShard(() -> getShader(GTORenderTypes.SOLAR_STORM_REACTOR_RINGS_SHADER_LOCATION)))
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false));

    public static final RenderType FROST_COAT = frostCoat("");
    public static final RenderType FROST_GLINT_A = frostGlint("_a");
    public static final RenderType FROST_GLINT_B = frostGlint("_b");
    public static final RenderType FROST_COAT_ENDERMAN = frostCoat("_enderman");
    public static final RenderType FROST_GLINT_ENDERMAN_A = frostGlint("_enderman_a");
    public static final RenderType FROST_GLINT_ENDERMAN_B = frostGlint("_enderman_b");

    private static RenderType frostCoat(String variant) {
        return RenderType.create("gtocore_frost_coat" + variant,
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTextureState(new TextureStateShard(GTOCore.id("textures/entity/frost_coat" + variant + ".png"), false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(true));
    }

    private static RenderType frostGlint(String variant) {
        return RenderType.create("gtocore_frost_glint" + variant,
                DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_EYES_SHADER)
                        .setTextureState(new TextureStateShard(GTOCore.id("textures/entity/frost_glint" + variant + ".png"), false, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));
    }

    private GTORenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    // The default shader shard leaves its shader active after a shared-buffer draw.
    private static final class ScopedShaderStateShard extends ShaderStateShard {

        private ShaderInstance previousShader;
        private final Supplier<ShaderInstance> restoreShader = () -> previousShader;

        private ScopedShaderStateShard(Supplier<ShaderInstance> shader) {
            super(shader);
        }

        @Override
        public void setupRenderState() {
            previousShader = RenderSystem.getShader();
            super.setupRenderState();
        }

        @Override
        public void clearRenderState() {
            super.clearRenderState();
            RenderSystem.setShader(restoreShader);
            previousShader = null;
        }
    }

    public static final ResourceLocation BLACK_HOLE_EVENT_HORIZON_SHADER_LOCATION = GTOCore.id("black_hole_event_horizon");
    public static final ResourceLocation DIMENSIONALLY_TRANSCENDENT_OVERLAY_SHADER_LOCATION = GTOCore.id("dimensionally_transcendent_overlay");
    public static final ResourceLocation STELLAR_FORGE_VORTEX_SHADER_LOCATION = GTOCore.id("stellar_forge_vortex");
    public static final ResourceLocation SOLAR_SURFACE_VORTEX_SHADER_LOCATION = GTOCore.id("solar_surface_vortex");
    public static final ResourceLocation SOLAR_STORM_HEAT_SHADER_LOCATION = GTOCore.id("solar_storm_heat");
    public static final ResourceLocation DYSON_FORCE_FIELD_SHADER_LOCATION = GTOCore.id("dyson_force_field");
    public static final ResourceLocation DYSON_SKY_SHELL_SHADER_LOCATION = GTOCore.id("dyson_sky_shell");
    public static final ResourceLocation SOLAR_STORM_REACTOR_SHADER_LOCATION = GTOCore.id("solar_storm_reactor");
    public static final ResourceLocation SOLAR_STORM_REACTOR_RINGS_SHADER_LOCATION = GTOCore.id("solar_storm_reactor_rings");
    public static final ResourceLocation SOLAR_GLOBE_SHADER_LOCATION = GTOCore.id("solar_globe");
    public static final ResourceLocation SOLAR_GLOBE_GLOW_SHADER_LOCATION = GTOCore.id("solar_globe_glow");

    public static final ResourceLocation CRUPTIX = GTOCore.id("cruptix");
    public static final ResourceLocation ITEM_RESONANCE_WAVE = GTOCore.id("item_resonance_wave");

    @OnlyIn(Dist.CLIENT)
    public static void setBlackHoleEventHorizonShader(ShaderInstance shader) {
        blackHoleEventHorizonShader = shader;
        SHADERS.put(BLACK_HOLE_EVENT_HORIZON_SHADER_LOCATION, shader);
    }

    @OnlyIn(Dist.CLIENT)
    public static ShaderInstance getBlackHoleEventHorizonShader() {
        return blackHoleEventHorizonShader;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setDimensionallyTranscendentOverlayShader(ShaderInstance shader) {
        dimensionallyTranscendentOverlayShader = shader;
        SHADERS.put(DIMENSIONALLY_TRANSCENDENT_OVERLAY_SHADER_LOCATION, shader);
    }

    @OnlyIn(Dist.CLIENT)
    public static ShaderInstance getDimensionallyTranscendentOverlayShader() {
        return dimensionallyTranscendentOverlayShader;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setStellarForgeVortexShader(ShaderInstance shader) {
        stellarForgeVortexShader = shader;
        SHADERS.put(STELLAR_FORGE_VORTEX_SHADER_LOCATION, shader);
    }

    @OnlyIn(Dist.CLIENT)
    public static ShaderInstance getStellarForgeVortexShader() {
        return stellarForgeVortexShader;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setSolarSurfaceVortexShader(ShaderInstance shader) {
        solarSurfaceVortexShader = shader;
        SHADERS.put(SOLAR_SURFACE_VORTEX_SHADER_LOCATION, shader);
    }

    @OnlyIn(Dist.CLIENT)
    public static ShaderInstance getSolarSurfaceVortexShader() {
        return solarSurfaceVortexShader;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setShader(ResourceLocation shaderLocation, ShaderInstance shader) {
        SHADERS.put(shaderLocation, shader);
    }

    @OnlyIn(Dist.CLIENT)
    public static ShaderInstance getShader(ResourceLocation shaderLocation) {
        return SHADERS.get(shaderLocation);
    }
}

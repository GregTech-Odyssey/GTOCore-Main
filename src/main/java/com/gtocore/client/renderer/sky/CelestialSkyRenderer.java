package com.gtocore.client.renderer.sky;

import com.gtocore.common.data.CelestialOrbits;

import com.gtolib.GTOCore;
import com.gtolib.api.data.CelestialBody;
import com.gtolib.api.data.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * 在星球天空按轨道位置画恒星与其他天体（方形贴图，统一 16 格）：黄道按自转轴倾角斜穿太阳轨迹，轨道倾角放大显示；张角压缩后换算大小。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public final class CelestialSkyRenderer {

    private static final float SKY_DISTANCE = 100;
    private static final float SUN_HALF = 20;
    private static final double SIZE_EXPONENT = 0.25;
    private static final float MIN_HALF = 6;
    private static final float MAX_HALF = 24;
    private static final float DAY_DIM = 0.5f;
    private static final float HOME_HALF = 80;
    private static final double LATITUDE_SCALE = 4;
    private static final float VANILLA_MOON_HALF = 20;
    private static final double GLARE_FADE = Math.toRadians(6);
    private static final double MAX_LATITUDE = Math.toRadians(70);
    private static final int TEXELS = 16;
    private static final double SUN_FROM_EARTH = 2 * Math.atan(695_700 / CelestialBody.AU_KM);
    private static final int CAPACITY = CelestialBody.all().size() + 3;
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[CAPACITY];
    private static final float[] HALVES = new float[CAPACITY];
    private static final float[] LONGITUDES = new float[CAPACITY];
    private static final float[] LATITUDES = new float[CAPACITY];
    private static final float[] ALPHAS = new float[CAPACITY];
    private static final double[] DISTANCES = new double[CAPACITY];
    private static final boolean[] FIXED = new boolean[CAPACITY];
    private static final int[] ORDER = new int[CAPACITY];
    private static final double[] OBSERVER = new double[3];
    private static final double[] TARGET = new double[3];
    private static final Object2ObjectOpenHashMap<ResourceLocation, ResourceLocation> RESAMPLED = new Object2ObjectOpenHashMap<>();

    private CelestialSkyRenderer() {}

    @Nullable
    private static CelestialBody observer(ClientLevel level) {
        var dimension = Dimension.getIncludingOrbits(level.dimension());
        return dimension != null ? dimension.getBody() : null;
    }

    private static boolean inOrbit(ClientLevel level) {
        return Dimension.get(level.dimension()) == null;
    }

    public static boolean replacesPlanetSky(@Nullable ClientLevel level) {
        if (level == null || !CelestialOrbits.hasClientSeed()) return false;
        var body = observer(level);
        return body != null && (body != CelestialBody.EARTH || inOrbit(level));
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        CelestialOrbits.clearClientSeed();
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;
        var level = Minecraft.getInstance().level;
        if (level == null || !CelestialOrbits.hasClientSeed()) return;
        var observer = observer(level);
        if (observer == null) return;
        float partialTick = event.getPartialTick();
        boolean orbit = inOrbit(level);
        var dimension = Dimension.get(level.dimension());
        var environment = dimension != null ? dimension.getEnvironment() : null;
        float daylight = environment != null && environment.oxygen() ? 1 - Math.min(1, level.getStarBrightness(partialTick) * 2) : 0;
        int count = collect(observer, orbit, level.getGameTime() + partialTick, daylight);
        if (count == 0) return;
        draw(event.getPoseStack(), count, level.getTimeOfDay(partialTick) * 360, (float) Math.toDegrees(observer.getObliquity()),
                1 - level.getRainLevel(partialTick));
    }

    private static int collect(CelestialBody observer, boolean orbit, double ticks, float daylight) {
        long seed = CelestialOrbits.clientSeed();
        var galaxy = observer.getGalaxy();
        CelestialOrbits.position(seed, observer, ticks, OBSERVER);
        double sx = -OBSERVER[0], sy = -OBSERVER[1], sz = -OBSERVER[2];
        double sunLongitude = Math.atan2(sy, sx), sunLatitude = Math.atan2(sz, Math.hypot(sx, sy));
        int count = 0;
        double starDistance = Math.sqrt(sx * sx + sy * sy + sz * sz) * CelestialBody.AU_KM;
        boolean vanillaSky = observer == CelestialBody.EARTH && !orbit;
        count = put(count, vanillaSky ? null : galaxy.getStar(), half(galaxy.getStarRadiusKm(), starDistance), starDistance, 0, 0, 0);
        if (vanillaSky) {
            count = put(count, null, VANILLA_MOON_HALF, 0, 180, 0, 0);
        }
        for (var body : CelestialBody.all()) {
            if (body == observer || body.getGalaxy() != galaxy || !visibleFrom(observer, body, vanillaSky)) continue;
            CelestialOrbits.position(seed, body, ticks, TARGET);
            double dx = TARGET[0] - OBSERVER[0], dy = TARGET[1] - OBSERVER[1], dz = TARGET[2] - OBSERVER[2];
            double horizontal = Math.hypot(dx, dy);
            double longitude = Math.atan2(dy, dx) - sunLongitude;
            double latitude = Math.atan2(dz, horizontal) - sunLatitude;
            latitude = Math.max(-MAX_LATITUDE, Math.min(MAX_LATITUDE, latitude * LATITUDE_SCALE));
            double distance = Math.sqrt(horizontal * horizontal + dz * dz) * CelestialBody.AU_KM;
            count = put(count, resampled(body.getTexture()), half(body.getRadiusKm(), distance), distance, (float) Math.toDegrees(longitude),
                    (float) Math.toDegrees(latitude), daylight);
        }
        occlude(count);
        if (orbit) {
            count = put(count, resampled(observer.getTexture()), HOME_HALF, 0, 0, 0, 0);
            FIXED[count - 1] = true;
        }
        return count;
    }

    private static void occlude(int count) {
        for (int i = 0; i < count; i++) {
            if (TEXTURES[i] == null) continue;
            float fade = 1;
            double lonI = Math.toRadians(LONGITUDES[i]), latI = Math.toRadians(LATITUDES[i]);
            for (int j = 0; j < count; j++) {
                if (j == i || HALVES[j] <= HALVES[i]) continue;
                double latJ = Math.toRadians(LATITUDES[j]);
                double cos = Math.sin(latI) * Math.sin(latJ) + Math.cos(latI) * Math.cos(latJ) * Math.cos(lonI - Math.toRadians(LONGITUDES[j]));
                double reach = Math.atan(HALVES[i] / SKY_DISTANCE) + Math.atan(HALVES[j] / SKY_DISTANCE);
                fade = Math.min(fade, (float) Math.max(0, Math.min(1, (Math.acos(Math.max(-1, Math.min(1, cos))) - reach) / GLARE_FADE)));
            }
            ALPHAS[i] *= fade;
        }
    }

    private static boolean visibleFrom(CelestialBody observer, CelestialBody body, boolean vanillaSky) {
        if (vanillaSky && body == CelestialBody.MOON) return false;
        var primary = body.getPrimary();
        return primary == null || primary == observer || primary == observer.getPrimary();
    }

    private static float half(double radiusKm, double distanceKm) {
        double apparent = 2 * Math.atan(radiusKm / Math.max(distanceKm, radiusKm));
        return (float) Math.min(MAX_HALF, Math.max(MIN_HALF, SUN_HALF * Math.pow(apparent / SUN_FROM_EARTH, SIZE_EXPONENT)));
    }

    private static int put(int index, @Nullable ResourceLocation texture, float half, double distanceKm, float longitude, float latitude, float daylight) {
        TEXTURES[index] = texture;
        HALVES[index] = half;
        LONGITUDES[index] = longitude;
        LATITUDES[index] = latitude;
        ALPHAS[index] = 1 - daylight * DAY_DIM;
        FIXED[index] = false;
        DISTANCES[index] = distanceKm;
        return index + 1;
    }

    private static ResourceLocation resampled(ResourceLocation source) {
        var cached = RESAMPLED.get(source);
        if (cached != null) return cached;
        var result = source;
        var minecraft = Minecraft.getInstance();
        try (var stream = minecraft.getResourceManager().open(source); var image = NativeImage.read(stream)) {
            if (image.getWidth() != TEXELS || image.getHeight() != TEXELS) {
                var scaled = new NativeImage(TEXELS, TEXELS, true);
                for (int y = 0; y < TEXELS; y++) {
                    for (int x = 0; x < TEXELS; x++) {
                        scaled.setPixelRGBA(x, y, image.getPixelRGBA(x * image.getWidth() / TEXELS, y * image.getHeight() / TEXELS));
                    }
                }
                result = GTOCore.id("sky/" + source.getNamespace() + "/" + source.getPath().replace('/', '_').replace(".png", ""));
                minecraft.getTextureManager().register(result, new DynamicTexture(scaled));
            }
        } catch (Exception e) {
            GTOCore.LOGGER.warn("Failed to resample sky texture {}", source, e);
        }
        RESAMPLED.put(source, result);
        return result;
    }

    private static void draw(PoseStack poseStack, int count, float sunAngle, float obliquity, float alpha) {
        for (int i = 0; i < count; i++) {
            int j = i;
            while (j > 0 && DISTANCES[ORDER[j - 1]] < DISTANCES[i]) {
                ORDER[j] = ORDER[j - 1];
                j--;
            }
            ORDER[j] = i;
        }
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        var builder = Tesselator.getInstance().getBuilder();
        for (int k = 0; k < count; k++) {
            int i = ORDER[k];
            if (TEXTURES[i] == null || ALPHAS[i] <= 0.01f) continue;
            RenderSystem.setShaderColor(1, 1, 1, alpha * ALPHAS[i]);
            RenderSystem.setShaderTexture(0, TEXTURES[i]);
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            if (FIXED[i]) {
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(sunAngle));
                poseStack.mulPose(Axis.YP.rotationDegrees(obliquity));
                poseStack.mulPose(Axis.XP.rotationDegrees(-LONGITUDES[i]));
                poseStack.mulPose(Axis.ZP.rotationDegrees(LATITUDES[i]));
                poseStack.mulPose(Axis.YP.rotationDegrees(-obliquity));
            }
            var matrix = poseStack.last().pose();
            float half = HALVES[i];
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(matrix, -half, SKY_DISTANCE, -half).uv(0, 0).endVertex();
            builder.vertex(matrix, half, SKY_DISTANCE, -half).uv(1, 0).endVertex();
            builder.vertex(matrix, half, SKY_DISTANCE, half).uv(1, 1).endVertex();
            builder.vertex(matrix, -half, SKY_DISTANCE, half).uv(0, 1).endVertex();
            BufferUploader.drawWithShader(builder.end());
            poseStack.popPose();
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
}

package com.gtocore.client.screen.starmap.base;

import com.gtolib.GTOCore;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * 星图的方块精灵：把天体贴图按整数格重采样并加 1 像素描边后注册成动态纹理，按来源缓存；天体在首次使用时解析一次并存在目录条目上。
 */
@OnlyIn(Dist.CLIENT)
public final class StarSprites {

    public static final ResourceLocation MARKERS = GTCEu.id("textures/gui/uipro/starfield/markers.png");
    public static final int MARKER_LOCK = 0;
    public static final int MARKER_STATION = 1;
    public static final int MARKER_HERE = 2;
    public static final int NO_TINT = 0xFFFFFFFF;
    public static final ResourceLocation FALLBACK_PLANET = RLUtils.ad("textures/environment/earth.png");
    public static final ResourceLocation FALLBACK_STAR = RLUtils.ad("textures/environment/red_sun.png");
    private static final Object2BooleanOpenHashMap<ResourceLocation> TEXTURE_EXISTS = new Object2BooleanOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, Sprite> SPRITES = new Object2ObjectOpenHashMap<>();

    private StarSprites() {}

    public record Sprite(ResourceLocation id, int size) {}

    public static ResourceLocation texture(ResourceLocation wanted, ResourceLocation fallback) {
        if (!TEXTURE_EXISTS.containsKey(wanted)) {
            TEXTURE_EXISTS.put(wanted, Minecraft.getInstance().getResourceManager().getResource(wanted).isPresent());
        }
        return TEXTURE_EXISTS.getBoolean(wanted) ? wanted : fallback;
    }

    public static Sprite planet(ResourceLocation icon, int texels) {
        return sprite(texture(icon, FALLBACK_PLANET), texels, true);
    }

    public static Sprite star(ResourceLocation icon, int texels) {
        return sprite(texture(icon, FALLBACK_STAR), texels, false);
    }

    public static Sprite body(StarCatalog catalog, int body) {
        var entry = catalog.bodies.get(body);
        if (entry.sprite == null) entry.sprite = planet(entry.icon, entry.isMoon() ? StarGeometry.SATELLITE_TEXELS : StarGeometry.PLANET_TEXELS);
        return entry.sprite;
    }

    public static Sprite badge(StarCatalog catalog, int body) {
        var entry = catalog.bodies.get(body);
        if (entry.badge == null) entry.badge = entry.isMoon() ? planet(entry.icon, StarGeometry.PLANET_TEXELS) : body(catalog, body);
        return entry.badge;
    }

    public static Sprite anchor(StarCatalog catalog, int anchor) {
        var entry = catalog.anchors.get(anchor);
        if (entry.sprite == null) entry.sprite = planet(entry.icon, StarGeometry.ANCHOR_TEXELS);
        return entry.sprite;
    }

    public static Sprite system(StarCatalog catalog, int system) {
        var entry = catalog.systems.get(system);
        if (entry.sprite == null) entry.sprite = star(entry.star, StarGeometry.STAR_TEXELS);
        return entry.sprite;
    }

    private static Sprite sprite(ResourceLocation source, int texels, boolean outlined) {
        int outline = UITheme.MAP_SPRITE_OUTLINE;
        String key = source + "|" + texels + "|" + outlined + "|" + Integer.toHexString(outline);
        var sprite = SPRITES.get(key);
        if (sprite == null) {
            sprite = bake(key, source, texels, outlined, outline);
            SPRITES.put(key, sprite);
        }
        return sprite;
    }

    private static Sprite bake(String key, ResourceLocation source, int n, boolean outlined, int outlineArgb) {
        int pad = outlined ? 1 : 0, size = n + 2 * pad;
        var out = new NativeImage(size, size, true);
        try (var stream = Minecraft.getInstance().getResourceManager().open(source); var in = NativeImage.read(stream)) {
            var solid = new boolean[size * size];
            for (int y = 0; y < n; y++) {
                for (int x = 0; x < n; x++) {
                    int color = in.getPixelRGBA(x * in.getWidth() / n, y * in.getHeight() / n);
                    if ((color >>> 24) == 0) continue;
                    solid[(y + pad) * size + x + pad] = true;
                    out.setPixelRGBA(x + pad, y + pad, color);
                }
            }
            if (outlined) outline(out, solid, size, 0xFF000000 | (outlineArgb & 0xFF) << 16 | (outlineArgb >> 8 & 0xFF) << 8 | outlineArgb >> 16 & 0xFF);
        } catch (Exception e) {
            GTOCore.LOGGER.warn("Failed to bake star map sprite {}", source, e);
        }
        var id = GTOCore.id("starmap/sprite_" + Integer.toHexString(key.hashCode()));
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(out));
        return new Sprite(id, size);
    }

    private static void outline(NativeImage out, boolean[] solid, int size, int abgr) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (solid[y * size + x]) continue;
                if ((x > 0 && solid[y * size + x - 1]) || (x < size - 1 && solid[y * size + x + 1]) ||
                        (y > 0 && solid[(y - 1) * size + x]) || (y < size - 1 && solid[(y + 1) * size + x])) {
                    out.setPixelRGBA(x, y, abgr);
                }
            }
        }
    }

    public static void draw(GuiGraphics graphics, Sprite sprite, int centerX, int centerY, int size, int tint) {
        boolean tinted = tint != NO_TINT;
        if (tinted) RenderSystem.setShaderColor((tint >> 16 & 0xFF) / 255f, (tint >> 8 & 0xFF) / 255f, (tint & 0xFF) / 255f, (tint >>> 24) / 255f);
        RenderSystem.enableBlend();
        graphics.blit(sprite.id(), centerX - size / 2, centerY - size / 2, size, size, 0, 0, sprite.size(), sprite.size(), sprite.size(), sprite.size());
        if (tinted) RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void marker(GuiGraphics graphics, int index, int centerX, int centerY) {
        RenderSystem.enableBlend();
        graphics.blit(MARKERS, centerX - 8, centerY - 8, index * 16, 0, 16, 16, 48, 16);
    }
}

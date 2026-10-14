package com.gtocore.client.screen.starmap.base;

import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 天体在画布上的位置与尺寸：只在缩放或目录修订号改变时重算并写入预分配数组，绘制与命中测试只读数组。
 */
@OnlyIn(Dist.CLIENT)
public final class StarGeometry {

    public static final float TILT = 0.55f;
    public static final float FOLD_SCALE = 0.65f;
    public static final float LABEL_MIN_SCALE = 0.6f;
    public static final float MOON_LABEL_SCALE = 1.4f;
    public static final int PLANET_TEXELS = 16;
    public static final int SATELLITE_TEXELS = 8;
    public static final int ANCHOR_TEXELS = 16;
    public static final int STAR_TEXELS = 24;
    private static final int HIT_PAD = 6;
    private static final int MOON_GAP = 16;
    private static final int MOON_STEP_GAP = 4;
    private static final float CARD_ROOM = 90;
    private static final float CARD_SCALE = 2;
    static final float MOON_FIRST = (PLANET_TEXELS + 2) / 2f + (SATELLITE_TEXELS + 2) / 2f + CARD_ROOM / CARD_SCALE;
    static final float MOON_STEP = SATELLITE_TEXELS + 2 + CARD_ROOM / CARD_SCALE;

    private final StarCatalog catalog;
    private float scale = Float.NaN;
    private int revision = -1, unit = 1;
    private float[] xs = new float[0], ys = new float[0], parentXs = new float[0], parentYs = new float[0], moonDistances = new float[0];
    private float[] anchorXs = new float[0], anchorYs = new float[0];
    private int[] pixels = new int[0];

    public StarGeometry(StarCatalog catalog) {
        this.catalog = catalog;
    }

    public static int unit(float scale) {
        return Math.max(1, (int) Math.floor(scale + 0.1f));
    }

    public static int spritePixels(int spriteSize, float scale) {
        return scale < FOLD_SCALE ? (spriteSize + 1) / 2 : spriteSize * unit(scale);
    }

    public StarCatalog catalog() {
        return catalog;
    }

    public void update(float scale) {
        scale = Math.max(0.01f, scale);
        if (scale == this.scale && revision == catalog.revision()) return;
        this.scale = scale;
        this.unit = unit(scale);
        if (revision != catalog.revision() || xs.length != catalog.size()) allocate();
        revision = catalog.revision();
        for (int i = 0; i < xs.length; i++) place(i);
        for (int a = 0; a < anchorXs.length; a++) {
            var anchor = catalog.anchors.get(a);
            float ring = catalog.ringRadius(anchor.system, anchor.order);
            anchorXs[a] = catalog.systemX(anchor.system) + anchor.cos * ring;
            anchorYs[a] = catalog.systemY(anchor.system) + anchor.sin * ring * TILT;
        }
    }

    private void allocate() {
        int n = catalog.size();
        xs = new float[n];
        ys = new float[n];
        parentXs = new float[n];
        parentYs = new float[n];
        moonDistances = new float[n];
        pixels = new int[n];
        anchorXs = new float[catalog.anchorCount()];
        anchorYs = new float[catalog.anchorCount()];
    }

    private void place(int i) {
        var body = catalog.bodies.get(i);
        if (body.realm) {
            xs[i] = body.realmX;
            ys[i] = body.realmY;
            pixels[i] = spritePixels(PLANET_TEXELS + 2, scale);
            return;
        }
        if (body.stellar) {
            xs[i] = catalog.systemX(body.system);
            ys[i] = catalog.systemY(body.system);
            pixels[i] = spritePixels(STAR_TEXELS, scale);
            return;
        }
        float ring = catalog.ringRadius(body.system, body.order);
        float cx = catalog.systemX(body.system), cy = catalog.systemY(body.system);
        if (!body.isMoon()) {
            xs[i] = cx + body.cos * ring;
            ys[i] = cy + body.sin * ring * TILT;
            pixels[i] = spritePixels(PLANET_TEXELS + 2, scale);
            return;
        }
        parentXs[i] = cx + body.parentCos * ring;
        parentYs[i] = cy + body.parentSin * ring * TILT;
        float distance = moonDistance(body);
        moonDistances[i] = distance;
        xs[i] = parentXs[i] + body.cos * distance;
        ys[i] = parentYs[i] + body.sin * distance * TILT;
        pixels[i] = spritePixels(SATELLITE_TEXELS + 2, scale);
    }

    private float moonDistance(StarEntries.BodyEntry body) {
        int parent = (PLANET_TEXELS + 2) * unit;
        int moon = (SATELLITE_TEXELS + 2) * unit;
        float pixels = parent / 2f + moon / 2f + MOON_GAP + body.moon * (moon + MOON_STEP_GAP);
        float world = Math.max(pixels / scale, MOON_FIRST + body.moon * MOON_STEP);
        float axis = Math.max(Math.abs(body.cos), Math.abs(body.sin) * TILT);
        return Math.max(body.orbitRadius, world / Math.max(0.2f, axis));
    }

    public float scale() {
        return scale;
    }

    public int unit() {
        return unit;
    }

    public float x(int body) {
        return xs[body];
    }

    public float y(int body) {
        return ys[body];
    }

    public float parentX(int body) {
        return parentXs[body];
    }

    public float parentY(int body) {
        return parentYs[body];
    }

    public int pixels(int body) {
        return pixels[body];
    }

    public boolean isFolded(int body) {
        return scale < FOLD_SCALE && catalog.isMoon(body);
    }

    public float moonDistance(int body) {
        return moonDistances[body];
    }

    public int starPixels() {
        return spritePixels(STAR_TEXELS, scale);
    }

    public float anchorX(int anchor) {
        return anchorXs[anchor];
    }

    public float anchorY(int anchor) {
        return anchorYs[anchor];
    }

    public int anchorPixels() {
        return spritePixels(ANCHOR_TEXELS + 2, scale);
    }

    public void bounds(int body, float[] out) {
        float size;
        if (isFolded(body)) {
            size = 0;
        } else if (catalog.isRealm(body)) {
            size = (PLANET_TEXELS * unit + HIT_PAD) / scale;
        } else {
            size = (pixels[body] + HIT_PAD) / scale;
        }
        out[0] = xs[body] - size / 2;
        out[1] = ys[body] - size / 2;
        out[2] = size;
        out[3] = size;
    }

    public void anchorBounds(int anchor, float[] out) {
        float size = (anchorPixels() + HIT_PAD) / scale;
        out[0] = anchorXs[anchor] - size / 2;
        out[1] = anchorYs[anchor] - size / 2;
        out[2] = size;
        out[3] = size;
    }

    public CanvasRect rect(int body) {
        var out = new float[4];
        bounds(body, out);
        return CanvasRect.of(out[0], out[1], out[2], out[3]);
    }

    public CanvasRect anchorRect(int anchor) {
        var out = new float[4];
        anchorBounds(anchor, out);
        return CanvasRect.of(out[0], out[1], out[2], out[3]);
    }
}

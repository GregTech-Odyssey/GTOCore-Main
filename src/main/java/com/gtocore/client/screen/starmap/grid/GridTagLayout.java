package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.PixelPen;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarGeometry;

import com.gregtechceu.gtceu.uipro.render.UISegments;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 信息卡的摆放：先收集本帧可见天体与锚点的贴图、名称区域作障碍，再按「盖住自己的名称 → 右 → 左 → 下 → 上」依次试位，
 * 先找不压自己线路的方位；不压别的天体、别人的名称和已放下的卡片。只依赖数据与镜头，不依赖悬停状态；全部用预分配的整数数组。
 */
@OnlyIn(Dist.CLIENT)
final class GridTagLayout {

    static final int DOT_HEIGHT = 5, CAPACITY = GridView.MAX_NODES + 64;
    private static final int GAP = 2, SIDE_GAP = 3, MARGIN = 80, CANDIDATES = 5;
    private static final int PLANET = 0, MOON_LEFT = 1, MOON_RIGHT = 2, ANCHOR = 3;

    private final GridMapClient client;
    private final GridModel model;
    private final StarCatalog catalog;
    private final StarGeometry geometry;
    private final PixelPen pen;
    private final int[] spot = new int[2];
    private final float[] range = new float[2];
    private final int[] rects = new int[CAPACITY * 4];
    private int[] blocks = new int[0], owners = new int[0];
    private int blockCount, placed;
    private int owner, targetX, targetY, half, dotRoom, kind;
    private boolean labelAbove;

    GridTagLayout(GridMapClient client) {
        this.client = client;
        this.model = client.model();
        this.catalog = client.catalog();
        this.geometry = client.geometry();
        this.pen = client.pen();
    }

    static int anchorOwner(int anchor) {
        return -2 - anchor;
    }

    int placed() {
        return placed;
    }

    int rect(int index, int edge) {
        return rects[index * 4 + edge];
    }

    boolean dot(int body) {
        int node = model.node(body);
        return node >= 0 && !model.isLineOnly(node) && pen.scale() < GridNodeTags.COMPACT_SCALE && !geometry.isFolded(body);
    }

    void begin() {
        placed = 0;
        blockCount = 0;
        int size = (catalog.size() + catalog.anchorCount()) * 2;
        if (owners.length < size) {
            blocks = new int[size * 4];
            owners = new int[size];
        }
        for (int body = 0; body < catalog.size(); body++) {
            if (geometry.isFolded(body)) continue;
            int x = pen.x(geometry.x(body)), y = pen.y(geometry.y(body)), h = geometry.pixels(body) / 2;
            if (!near(x, y, h)) continue;
            block(-1, x - h - 1, y - h - 1, x + h + 1, y + h + 1 + (dot(body) ? GAP + DOT_HEIGHT : 0));
            if (labelShown(body)) label(body, x, y, h);
        }
        for (int anchor = 0; anchor < catalog.anchorCount(); anchor++) {
            int x = pen.x(geometry.anchorX(anchor)), y = pen.y(geometry.anchorY(anchor)), h = geometry.anchorPixels() / 2;
            if (!near(x, y, h)) continue;
            block(-1, x - h - 1, y - h - 1, x + h + 1, y + h + 1);
            if (pen.scale() < StarGeometry.LABEL_MIN_SCALE) continue;
            int width = pen.width(catalog.anchorName(anchor)), left = x - width / 2;
            block(anchorOwner(anchor), left - 2, y + h + 3, left + width + 1, y + h + 13);
        }
    }

    private boolean near(int x, int y, int h) {
        return x + h + MARGIN >= pen.clipLeft() && x - h - MARGIN <= pen.clipRight() && y + h + MARGIN >= pen.clipTop() && y - h - MARGIN <= pen.clipBottom();
    }

    private void block(int blockOwner, int left, int top, int right, int bottom) {
        int k = blockCount++;
        blocks[k * 4] = left;
        blocks[k * 4 + 1] = top;
        blocks[k * 4 + 2] = right;
        blocks[k * 4 + 3] = bottom;
        owners[k] = blockOwner;
    }

    private boolean labelShown(int body) {
        boolean lit = body == catalog.current();
        if (catalog.isRealm(body)) return lit || pen.scale() >= StarGeometry.LABEL_MIN_SCALE;
        if (catalog.isMoon(body)) return lit || pen.scale() >= StarGeometry.MOON_LABEL_SCALE;
        return lit || pen.scale() >= StarGeometry.LABEL_MIN_SCALE;
    }

    private void label(int body, int x, int y, int h) {
        int width = pen.width(catalog.name(body));
        if (catalog.isMoon(body)) {
            int left = geometry.x(body) < geometry.parentX(body) ? x - h - 3 - width : x + h + 3;
            block(body, left - 2, y - 5, left + width + 1, y + 5);
            return;
        }
        int top = above(body, x, y, h, width) ? y - h - 12 : y + h + 4, left = x - width / 2;
        block(body, left - 2, top - 1, left + width + 1, top + 9);
    }

    private boolean above(int body, int x, int y, int h, int width) {
        if (catalog.isRealm(body)) return false;
        int system = catalog.system(body), starHalf = geometry.starPixels() / 2 + 1, below = y + h + 4, labelHalf = width / 2 + 2;
        int starX = pen.x(catalog.systemX(system)), starY = pen.y(catalog.systemY(system));
        return below - 1 < starY + starHalf && below + 9 > starY - starHalf && x - labelHalf < starX + starHalf && x + labelHalf > starX - starHalf;
    }

    boolean place(int body, int width, int height, boolean forced) {
        owner = body;
        targetX = pen.x(model.worldX(body));
        targetY = pen.y(model.worldY(body));
        half = geometry.pixels(body) / 2;
        dotRoom = dot(body) ? GAP + DOT_HEIGHT : 0;
        kind = !catalog.isMoon(body) ? PLANET : geometry.x(body) < geometry.parentX(body) ? MOON_LEFT : MOON_RIGHT;
        labelAbove = kind == PLANET && above(body, targetX, targetY, half, pen.width(catalog.name(body)));
        return placeTarget(width, height, forced);
    }

    boolean placeAnchor(int anchor, int width, int height) {
        owner = anchorOwner(anchor);
        targetX = pen.x(geometry.anchorX(anchor));
        targetY = pen.y(geometry.anchorY(anchor));
        half = geometry.anchorPixels() / 2;
        dotRoom = 0;
        kind = ANCHOR;
        labelAbove = false;
        return placeTarget(width, height, false);
    }

    private boolean placeTarget(int width, int height, boolean forced) {
        if (placed * 4 >= rects.length) return false;
        for (int pass = 0; pass < 2; pass++) {
            for (int k = 0; k < CANDIDATES; k++) {
                candidate(k, width, height);
                int r = spot[0] + width, b = spot[1] + height;
                if (free(spot[0], spot[1], r, b) && (pass > 0 || !crossesLines(spot[0], spot[1], r, b))) return commit(width, height);
            }
        }
        if (!forced) return false;
        candidate(0, width, height);
        var area = client.obstructions();
        set(Math.max(area.left(), Math.min(spot[0], area.right() - width)), Math.max(area.top(), Math.min(spot[1], area.bottom() - height)));
        return commit(width, height);
    }

    private boolean commit(int width, int height) {
        int k = placed++;
        rects[k * 4] = spot[0];
        rects[k * 4 + 1] = spot[1];
        rects[k * 4 + 2] = spot[0] + width;
        rects[k * 4 + 3] = spot[1] + height;
        return true;
    }

    private void candidate(int k, int width, int height) {
        int x = targetX, y = targetY;
        switch (k) {
            case 0 -> cover(width, height);
            case 1 -> set(x + half + SIDE_GAP, y - height / 2);
            case 2 -> set(x - half - SIDE_GAP - width, y - height / 2);
            case 3 -> set(x - width / 2, y + half + SIDE_GAP + dotRoom);
            default -> set(x - width / 2, y - half - SIDE_GAP - height);
        }
    }

    private void cover(int width, int height) {
        if (kind == MOON_LEFT) set(targetX - half - GAP - width, targetY - 5);
        else if (kind == MOON_RIGHT) set(targetX + half + GAP, targetY - 5);
        else if (labelAbove) set(targetX - width / 2, targetY - half - GAP - height);
        else set(targetX - width / 2, targetY + half + GAP + dotRoom);
    }

    private void set(int left, int top) {
        spot[0] = left;
        spot[1] = top;
    }

    private boolean crossesLines(int left, int top, int right, int bottom) {
        if (owner < 0) return false;
        var links = client.links();
        for (int l = 0; l < model.lineCount(); l++) {
            if (!model.touches(l, owner) || !links.isStablyShown(l)) continue;
            int a = model.lineA(l), b = model.lineB(l);
            if (UISegments.clip(pen.x(model.worldX(a)), pen.y(model.worldY(a)), pen.x(model.worldX(b)), pen.y(model.worldY(b)), left, top, right, bottom,
                    range))
                return true;
        }
        return false;
    }

    boolean blocks(int left, int top, int right, int bottom) {
        for (int k = 0; k < placed; k++) {
            if (left < rects[k * 4 + 2] && right > rects[k * 4] && top < rects[k * 4 + 3] && bottom > rects[k * 4 + 1]) return true;
        }
        return false;
    }

    private boolean free(int left, int top, int right, int bottom) {
        if (!client.obstructions().fits(left, top, right, bottom) || blocks(left, top, right, bottom)) return false;
        for (int k = 0; k < blockCount; k++) {
            if (owners[k] == owner) continue;
            if (left < blocks[k * 4 + 2] && right > blocks[k * 4] && top < blocks[k * 4 + 3] && bottom > blocks[k * 4 + 1]) return false;
        }
        return true;
    }

    int hit(float screenX, float screenY) {
        for (int k = placed - 1; k >= 0; k--) {
            if (screenX >= rects[k * 4] && screenX < rects[k * 4 + 2] && screenY >= rects[k * 4 + 1] && screenY < rects[k * 4 + 3]) return k;
        }
        return -1;
    }
}

package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.PixelPen;

import com.gregtechceu.gtceu.uipro.animation.UIClock;
import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.render.UISegments;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * 沿能量流向移动的方块粒子：亮头暗尾、各线相位错开、间距与速度随流量分档；只在线路的可见区间布点，
 * 先按可见长度估算总数，超过上限时统一拉大间距再画。
 */
@OnlyIn(Dist.CLIENT)
final class GridParticles implements CanvasLayer {

    private static final int LANES = GridView.MAX_LINES * 2;

    private final GridMapClient client;
    private final GridModel model;
    private final GridLinkLayer links;
    private final PixelPen pen;
    private CanvasPainter painter;
    private final float[] range = new float[2];
    private final int[] laneLine = new int[LANES];
    private final boolean[] laneReverse = new boolean[LANES];
    private final boolean[] laneShared = new boolean[LANES];
    private final float[] laneFrom = new float[LANES], laneTo = new float[LANES];
    private int laneCount;

    GridParticles(GridMapClient client, GridLinkLayer links) {
        this.client = client;
        this.model = client.model();
        this.links = links;
        this.pen = client.pen();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
        if (!client.view().toggles().isMotion() || model.lineCount() == 0) return;
        this.painter = painter;
        pen.begin(painter.graphics(), client.canvas());
        pen.beginFine();
        float demand = collect();
        float factor = demand > GridStyle.MAX_PARTICLES ? demand / GridStyle.MAX_PARTICLES : 1;
        for (int k = 0; k < laneCount; k++) drawLane(k, factor);
        painter.flush();
        pen.endFine();
        pen.end();
    }

    private float collect() {
        laneCount = 0;
        float demand = 0;
        for (int l = 0; l < model.lineCount(); l++) {
            int level = model.level(l);
            if (!GridStyle.hasParticles(level) || !links.isShown(l) || client.isDimmed(l)) continue;
            boolean ab = model.flowAB(l) >= GridStyle.STEADY, ba = model.flowBA(l) >= GridStyle.STEADY;
            if (!ab && !ba || !visible(l)) continue;
            int major = majorLength(l);
            float spacing = GridStyle.spacing(level) * pen.density();
            boolean shared = ab && ba && links.width(l) >= 2;
            if (ab) demand += addLane(l, false, shared, range[0] * major, range[1] * major) / spacing;
            if (ba) demand += addLane(l, true, shared, (1 - range[1]) * major, (1 - range[0]) * major) / spacing;
        }
        return demand;
    }

    private boolean visible(int line) {
        int a = model.lineA(line), b = model.lineB(line);
        int x0 = pen.fineX(model.worldX(a)), y0 = pen.fineY(model.worldY(a)), x1 = pen.fineX(model.worldX(b)), y1 = pen.fineY(model.worldY(b));
        int pad = 2 * (GridStyle.HEAD + GridStyle.TAIL), d = pen.density();
        return (x0 != x1 || y0 != y1) &&
                UISegments.clip(x0, y0, x1, y1, pen.clipLeft() * d - pad, pen.clipTop() * d - pad, pen.clipRight() * d + pad, pen.clipBottom() * d + pad, range);
    }

    private int majorLength(int line) {
        int a = model.lineA(line), b = model.lineB(line);
        int dx = Math.abs(pen.fineX(model.worldX(b)) - pen.fineX(model.worldX(a))), dy = Math.abs(pen.fineY(model.worldY(b)) - pen.fineY(model.worldY(a)));
        return Math.max(dx, dy);
    }

    private float addLane(int line, boolean reverse, boolean shared, float from, float to) {
        laneLine[laneCount] = line;
        laneReverse[laneCount] = reverse;
        laneShared[laneCount] = shared;
        laneFrom[laneCount] = from;
        laneTo[laneCount] = to;
        laneCount++;
        return Math.max(0, to - from);
    }

    private void drawLane(int lane, float factor) {
        int line = laneLine[lane], level = model.level(line);
        boolean reverse = laneReverse[lane];
        int start = reverse ? model.lineB(line) : model.lineA(line), end = reverse ? model.lineA(line) : model.lineB(line);
        int x0 = pen.fineX(model.worldX(start)), y0 = pen.fineY(model.worldY(start)), x1 = pen.fineX(model.worldX(end)), y1 = pen.fineY(model.worldY(end));
        boolean vertical = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        int a0 = vertical ? y0 : x0, b0 = vertical ? x0 : y0, a1 = vertical ? y1 : x1, b1 = vertical ? x1 : y1;
        int offset = laneShared[lane] ? (reverse ? -1 : 1) : 0;
        float spacing = GridStyle.spacing(level) * pen.density() * factor;
        var info = model.lineInfo(line);
        double seed = ((long) info.a() * 31 + info.b()) * 7 % (long) Math.max(1, spacing);
        float phase = (float) ((UIClock.millis() * GridStyle.speed(level) * pen.density() / 1000.0 + seed) % spacing);
        int length = Math.abs(a1 - a0), dir = a1 >= a0 ? 1 : -1, head = GridStyle.headColor(level), tail = GridStyle.tailColor(level);
        float to = Math.min(length, laneTo[lane] + GridStyle.HEAD + GridStyle.TAIL);
        for (float s = phase + (float) Math.ceil((laneFrom[lane] - phase) / spacing) * spacing; s <= to; s += spacing) {
            spark(vertical, a0, b0, a1, b1, a0 + dir * Math.round(s), dir, offset, head, tail);
        }
    }

    private void spark(boolean vertical, int a0, int b0, int a1, int b1, int head, int dir, int offset, int headColor, int tailColor) {
        int size = GridStyle.HEAD, back = head - size / 2;
        for (int i = 1; i <= GridStyle.TAIL; i++) {
            int a = dir > 0 ? back - i : back + size - 1 + i;
            if ((a - a0) * dir < 0) break;
            cell(vertical, a, minorOn(a0, b0, a1, b1, a) + offset, 1, size, tailColor);
        }
        cell(vertical, back, minorOn(a0, b0, a1, b1, head) + offset, size, size, headColor);
    }

    private void cell(boolean vertical, int major, int minor, int length, int thickness, int color) {
        int low = minor - thickness / 2;
        if (vertical) painter.fill(low, major, low + thickness, major + length, color);
        else painter.fill(major, low, major + length, low + thickness, color);
    }

    static int minorOn(int a0, int b0, int a1, int b1, int a) {
        if (a0 > a1) return minorOn(a1, b1, a0, b0, a);
        long da = (long) a1 - a0, db = (long) b1 - b0;
        if (da == 0) return b0;
        long steps = Math.floorDiv(((long) a - a0) * Math.abs(db) + da / 2, da);
        return (int) (b0 + (db < 0 ? -steps : steps));
    }
}

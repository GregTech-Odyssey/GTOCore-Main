package com.gtocore.client.screen.starmap.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/**
 * 点线像素椭圆：大椭圆按弧段剔除视口外的部分，点距相位取切比雪夫弧长，平移时点位不滑动；所有点攒成一批提交。
 */
@OnlyIn(Dist.CLIENT)
final class PixelEllipse {

    private static final int ARC_STEPS = 256;
    private static final int MAX_ARCS = 512;
    private static final double HALF_PI = Math.PI / 2;

    private PixelEllipse() {}

    static void draw(GuiGraphics graphics, int cx, int cy, float radius, float tilt, int color, int dotEvery, int l, int t, int r, int b) {
        int rx = Math.round(radius), ry = Math.round(radius * tilt);
        if (rx <= 0 || cx + rx < l || cx - rx >= r || cy + ry < t || cy - ry >= b) return;
        int steps = Math.max(16, (int) Math.ceil(2 * Math.PI * rx));
        int arcs = Mth.clamp(steps / ARC_STEPS, 1, MAX_ARCS);
        double bulge = rx * (1 - Math.cos(Math.PI / arcs)) + 2;
        VertexConsumer consumer = graphics.bufferSource().getBuffer(RenderType.gui());
        Matrix4f matrix = graphics.pose().last().pose();
        for (int k = 0; k < arcs; k++) {
            int from = (int) ((long) steps * k / arcs), to = (int) ((long) steps * (k + 1) / arcs);
            if (arcs > 1 && !arcVisible(cx, cy, rx, ry, from, to, steps, bulge, l, t, r, b)) continue;
            int count = arcs == 1 ? 0 : (int) Math.round(chebyshev(rx, ry, 2 * Math.PI * from / steps));
            int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE;
            for (int i = from; i < to; i++) {
                double angle = 2 * Math.PI * i / steps;
                int x = cx + (int) Math.round(Math.cos(angle) * rx), y = cy + (int) Math.round(Math.sin(angle) * ry);
                if (x == lastX && y == lastY) continue;
                lastX = x;
                lastY = y;
                if (count++ % dotEvery == 0 && x >= l && x < r && y >= t && y < b) quad(consumer, matrix, x, y, x + 1, y + 1, color);
            }
        }
        graphics.flush();
    }

    private static boolean arcVisible(int cx, int cy, int rx, int ry, int from, int to, int steps, double bulge, int l, int t, int r, int b) {
        double a0 = 2 * Math.PI * from / steps, a1 = 2 * Math.PI * to / steps;
        double x0 = cx + Math.cos(a0) * rx, y0 = cy + Math.sin(a0) * ry;
        double x1 = cx + Math.cos(a1) * rx, y1 = cy + Math.sin(a1) * ry;
        return Math.max(x0, x1) + bulge >= l && Math.min(x0, x1) - bulge < r && Math.max(y0, y1) + bulge >= t && Math.min(y0, y1) - bulge < b;
    }

    private static double chebyshev(int rx, int ry, double theta) {
        double quarter = Math.hypot(rx, ry), phi = Math.atan2(ry, rx);
        int q = Mth.clamp((int) (theta / HALF_PI), 0, 3);
        double local = theta - q * HALF_PI;
        return q * quarter + ((q & 1) == 0 ? partial(rx, ry, phi, local) : quarter - partial(rx, ry, phi, HALF_PI - local));
    }

    private static double partial(int rx, int ry, double phi, double angle) {
        return angle <= phi ? ry * Math.sin(angle) : ry * Math.sin(phi) + rx * (Math.cos(phi) - Math.cos(angle));
    }

    static void quad(VertexConsumer consumer, Matrix4f matrix, int x0, int y0, int x1, int y1, int color) {
        int a = color >>> 24, red = color >> 16 & 0xFF, green = color >> 8 & 0xFF, blue = color & 0xFF;
        consumer.vertex(matrix, x0, y0, 0).color(red, green, blue, a).endVertex();
        consumer.vertex(matrix, x0, y1, 0).color(red, green, blue, a).endVertex();
        consumer.vertex(matrix, x1, y1, 0).color(red, green, blue, a).endVertex();
        consumer.vertex(matrix, x1, y0, 0).color(red, green, blue, a).endVertex();
    }
}

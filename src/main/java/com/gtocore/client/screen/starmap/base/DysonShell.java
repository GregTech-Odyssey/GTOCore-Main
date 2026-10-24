package com.gtocore.client.screen.starmap.base;

import com.gtocore.common.data.StarMapDysonSync;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

/**
 * 方形像素蜂巢力场：面片与闭合轮廓共用三维旋转矩阵，再投影到固定方形遮罩，避免旋转时细边断裂。
 * 球面网格、建造顺序与闪烁种子只计算一次；随机慢闪只增强面片与描边亮度，不会熄灭轮廓。
 * 前后半球分开绘制，恒星遮挡背面，正面保持半透明。逐帧复用像素缓冲，不创建纹理或临时对象。
 */
@OnlyIn(Dist.CLIENT)
public final class DysonShell {

    static final int TEXELS = 40;
    private static final int COLUMNS = 24, ROW_RADIUS = 6, CELLS = COLUMNS * (ROW_RADIUS * 2 + 1);
    private static final int EDGE_STEPS = 3, VERTICES = 6 * EDGE_STEPS;
    private static final int OUTLINE_RGB = 0x69CFFF;
    private static final double ROOT_3 = Math.sqrt(3), PERIOD = ROOT_3 * COLUMNS;
    private static final long ROTATION_MILLIS = 90_000;
    private static final long AXIS_SWAY_MILLIS = 180_000;
    private static final double AXIS_SWAY_RADIANS = 25 * Math.PI / 180;
    private static final int[] RANK = new int[CELLS];
    private static final int[] PULSE_PERIOD = new int[CELLS];
    private static final double[] PULSE_PHASE = new double[CELLS];
    private static final float[] RAY_X = new float[TEXELS * TEXELS], RAY_Y = new float[TEXELS * TEXELS], RAY_Z = new float[TEXELS * TEXELS], DEPTH = new float[TEXELS * TEXELS];
    private static final int[] OUTLINE_WEIGHT = new int[TEXELS * TEXELS];
    private static final float[] MESH_X = new float[CELLS * VERTICES], MESH_Y = new float[CELLS * VERTICES], MESH_Z = new float[CELLS * VERTICES];

    static {
        for (int i = 0; i < CELLS; i++) RANK[i] = i;
        var random = new Random(0xD1505FEL);
        for (int i = CELLS - 1; i > 0; i--) {
            int j = random.nextInt(i + 1), rank = RANK[i];
            RANK[i] = RANK[j];
            RANK[j] = rank;
        }
        for (int i = 0; i < CELLS; i++) {
            int seed = RANK[i] * 1664525 + 1013904223;
            if ((seed & 3) != 0) continue;
            PULSE_PERIOD[i] = 4_000 + (seed >>> 16) % 5_000;
            PULSE_PHASE[i] = (seed & 0xFFFF) * (2 * Math.PI / 65536);
        }
        for (int cell = 0; cell < CELLS; cell++) {
            int row = cell / COLUMNS - ROW_RADIUS, column = cell % COLUMNS;
            double u = ROOT_3 * (column + row / 2.0), v = 1.5 * row;
            for (int edge = 0; edge < 6; edge++) {
                double a = Math.PI / 2 + edge * Math.PI / 3, b = a + Math.PI / 3;
                double ax = Math.cos(a), ay = Math.sin(a), bx = Math.cos(b), by = Math.sin(b);
                for (int step = 0; step < EDGE_STEPS; step++) {
                    double t = step / (double) EDGE_STEPS;
                    double longitude = (u + ax + (bx - ax) * t) * (2 * Math.PI / PERIOD);
                    double latitude = Math.clamp(v + ay + (by - ay) * t, -9, 9) * (Math.PI / 18);
                    int i = cell * VERTICES + edge * EDGE_STEPS + step;
                    float cos = (float) Math.cos(latitude);
                    MESH_X[i] = (float) Math.sin(longitude) * cos;
                    MESH_Y[i] = (float) Math.sin(latitude);
                    MESH_Z[i] = (float) Math.cos(longitude) * cos;
                }
            }
        }
        for (int y = 0; y < TEXELS; y++) {
            double ny = (2 * y + 1.0) / TEXELS - 1;
            for (int x = 0; x < TEXELS; x++) {
                int i = y * TEXELS + x;
                double nx = (2 * x + 1.0) / TEXELS - 1, square = 1 - Math.max(nx * nx, ny * ny);
                DEPTH[i] = (float) Math.sqrt(square);
                // 掠视角下多条蜂巢边会挤进外沿的少数像素；减弱这些像素，避免形成厚亮边框。
                double facing = Math.clamp((DEPTH[i] - 0.15) / 0.45, 0, 1);
                OUTLINE_WEIGHT[i] = (int) Math.round(256 * (0.25 + 0.75 * facing * facing * (3 - 2 * facing)));
                // 方形反投影还原单位球面射线，面片用旋转矩阵的转置采样。
                double horizontal = Math.sqrt(nx * nx + square), latitudeCos = Math.sqrt(1 - ny * ny);
                RAY_X[i] = (float) (nx * latitudeCos / horizontal);
                RAY_Y[i] = (float) ny;
                RAY_Z[i] = (float) (DEPTH[i] * latitudeCos / horizontal);
            }
        }
    }

    private final int[] front = new int[TEXELS * TEXELS], back = new int[TEXELS * TEXELS];
    private final float[] brightness = new float[CELLS];
    private final float[] projectedX = new float[VERTICES], projectedY = new float[VERTICES], projectedZ = new float[VERTICES];
    private float m00, m01, m02, m10, m11, m12, m20, m21, m22;
    private long frame = Long.MIN_VALUE;
    private int launched = -1;

    public void prepare(int launched, long millis) {
        if (this.launched == launched && frame == millis) return;
        this.launched = launched;
        frame = millis;
        // 固定随机建造顺序使进度增加只补格，不会让已有格子重新洗牌。
        int built = (int) Math.ceil(Math.clamp(launched / (double) StarMapDysonSync.COMPLETE, 0, 1) * CELLS);
        updateRotation(millis);
        for (int i = 0; i < CELLS; i++) {
            int period = PULSE_PERIOD[i];
            brightness[i] = period == 0 ? 1 : 0.55f + 0.45f * (float) (0.5 + 0.5 * Math.sin((millis % period) * (2 * Math.PI / period) + PULSE_PHASE[i]));
        }
        for (int i = 0; i < front.length; i++) {
            if (built == 0) {
                front[i] = back[i] = 0;
                continue;
            }
            float x = RAY_X[i], y = RAY_Y[i], z = RAY_Z[i];
            front[i] = sample(m00 * x + m10 * y + m20 * z, m01 * x + m11 * y + m21 * z, m02 * x + m12 * y + m22 * z, built, false);
            back[i] = sample(m00 * x + m10 * y - m20 * z, m01 * x + m11 * y - m21 * z, m02 * x + m12 * y - m22 * z, built, true);
        }
        if (built > 0) outline(built);
    }

    private void updateRotation(long millis) {
        double angle = (millis % ROTATION_MILLIS) * (2 * Math.PI / ROTATION_MILLIS);
        // 转轴在屏幕平面内缓慢左右摆动 ±25°，与自转叠加形成柔和的姿态变化。
        double phase = (millis % AXIS_SWAY_MILLIS) * (2 * Math.PI / AXIS_SWAY_MILLIS);
        double tilt = AXIS_SWAY_RADIANS * Math.sin(phase);
        double axisX = Math.sin(tilt), axisY = Math.cos(tilt), axisZ = 0;
        double c = Math.cos(angle), s = Math.sin(angle), d = 1 - c;
        // Rodrigues 矩阵：轴为单位向量；采样用转置，轮廓用正向，二者不会错位。
        m00 = (float) (c + axisX * axisX * d);
        m01 = (float) (axisX * axisY * d + axisZ * s);
        m02 = (float) (axisX * axisZ * d - axisY * s);
        m10 = (float) (axisY * axisX * d - axisZ * s);
        m11 = (float) (c + axisY * axisY * d);
        m12 = (float) (axisY * axisZ * d + axisX * s);
        m20 = (float) (axisZ * axisX * d + axisY * s);
        m21 = (float) (axisZ * axisY * d - axisX * s);
        m22 = (float) (c + axisZ * axisZ * d);
    }

    private int sample(float x, float y, float z, int built, boolean rear) {
        double u = Math.atan2(x, z) * (PERIOD / (2 * Math.PI)), v = Math.asin(Math.clamp(y, -1, 1)) * (18 / Math.PI);
        // Pointy-top axial 坐标取最近六边形；经度按整列环绕，旋转接缝保持连续。
        double r = v / 1.5, q = u / ROOT_3 - r / 2;
        int rq = (int) Math.round(q), rr = (int) Math.round(r), rs = (int) Math.round(-q - r);
        double dq = Math.abs(rq - q), dr = Math.abs(rr - r), ds = Math.abs(rs + q + r);
        if (dq > dr && dq > ds) rq = -rr - rs;
        else if (dr > ds) rr = -rq - rs;
        int cell = (rr + ROW_RADIUS) * COLUMNS + Math.floorMod(rq, COLUMNS);
        if (RANK[cell] >= built) return 0;
        int alpha = 18 + Math.round((1 - brightness[cell]) * 64);
        if (rear) alpha /= 3;
        // 限定色阶避免噪点，也让同色连续像素合成一个 quad。
        alpha = (alpha + 2) / 4 * 4;
        return alpha << 24 | 0x268BD8;
    }

    private void outline(int built) {
        for (int cell = 0; cell < CELLS; cell++) {
            if (RANK[cell] >= built) continue;
            int start = cell * VERTICES;
            for (int vertex = 0; vertex < VERTICES; vertex++) {
                int i = start + vertex;
                float x = MESH_X[i], y = MESH_Y[i], z = MESH_Z[i];
                float viewX = m00 * x + m01 * y + m02 * z, viewY = m10 * x + m11 * y + m12 * z, viewZ = m20 * x + m21 * y + m22 * z;
                float latitudeCos = (float) Math.sqrt(Math.max(0, 1 - viewY * viewY));
                // 极点的经度退化到一点；限住分母，避免投影出现 0/0。
                projectedX[vertex] = viewX / Math.max(0.000001f, Math.max(latitudeCos, Math.abs(viewZ) / Math.max(0.000001f, latitudeCos)));
                projectedY[vertex] = viewY;
                projectedZ[vertex] = viewZ;
            }
            int alpha = 120 + Math.round((1 - brightness[cell]) * 24);
            for (int vertex = 0; vertex < VERTICES; vertex++) {
                int next = (vertex + 1) % VERTICES;
                float x0 = projectedX[vertex], y0 = projectedY[vertex], z0 = projectedZ[vertex];
                float x1 = projectedX[next], y1 = projectedY[next], z1 = projectedZ[next];
                if ((z0 >= 0) == (z1 >= 0)) {
                    edge(z0 >= 0 ? front : back, x0, y0, x1, y1, z0 >= 0 ? alpha : alpha / 3);
                } else {
                    // 只在真实地平线处拆边；前后半球各自保持轮廓连续。
                    float t = z0 / (z0 - z1), x = x0 + (x1 - x0) * t, y = y0 + (y1 - y0) * t;
                    edge(z0 >= 0 ? front : back, x0, y0, x, y, z0 >= 0 ? alpha : alpha / 3);
                    edge(z1 >= 0 ? front : back, x, y, x1, y1, z1 >= 0 ? alpha : alpha / 3);
                }
            }
        }
    }

    private static void edge(int[] pixels, float fromX, float fromY, float toX, float toY, int alpha) {
        int x = Math.round((fromX + 1) * (TEXELS / 2f) - 0.5f), y = Math.round((fromY + 1) * (TEXELS / 2f) - 0.5f);
        int endX = Math.round((toX + 1) * (TEXELS / 2f) - 0.5f), endY = Math.round((toY + 1) * (TEXELS / 2f) - 0.5f);
        int dx = Math.abs(endX - x), dy = -Math.abs(endY - y), sx = x < endX ? 1 : -1, sy = y < endY ? 1 : -1;
        int error = dx + dy;
        while (true) {
            if (x >= 0 && x < TEXELS && y >= 0 && y < TEXELS) {
                int i = y * TEXELS + x;
                int weightedAlpha = (alpha * OUTLINE_WEIGHT[i] + 128) >> 8;
                // 邻格共边取较亮的一次，避免叠加变粗；方形裁切沿用面片的遮罩。
                if ((pixels[i] & 0xFFFFFF) != OUTLINE_RGB || (pixels[i] >>> 24) < weightedAlpha) {
                    pixels[i] = weightedAlpha << 24 | OUTLINE_RGB;
                }
            }
            if (x == endX && y == endY) break;
            int twice = 2 * error;
            if (twice >= dy) {
                error += dy;
                x += sx;
            }
            if (twice <= dx) {
                error += dx;
                y += sy;
            }
        }
    }

    private static int pixel(int[] pixels, int x, int y, int step) {
        int i = y * TEXELS + x, color = pixels[i];
        if (step == 1) return color;
        // 远景合并 2×2 的最亮像素，不能跳点采样，否则单像素边线又会丢失。
        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int candidate = pixels[i + dy * TEXELS + dx];
                if ((candidate >>> 24) > (color >>> 24)) color = candidate;
            }
        }
        return color;
    }

    void draw(PixelPen pen, int cx, int cy, int starPixels, boolean rear) {
        if (launched <= 0) return;
        int step = starPixels < StarGeometry.STAR_TEXELS ? 2 : 1;
        int unit = Math.max(1, starPixels / StarGeometry.STAR_TEXELS), size = TEXELS / step * unit;
        int left = cx - size / 2, top = cy - size / 2;
        if (!pen.touches(left, top, left + size, top + size)) return;
        var pixels = rear ? back : front;
        var graphics = pen.graphics();
        var consumer = graphics.bufferSource().getBuffer(RenderType.gui());
        var matrix = graphics.pose().last().pose();
        for (int y = 0; y < TEXELS; y += step) {
            int start = 0, color = pixel(pixels, 0, y, step);
            for (int x = step; x <= TEXELS; x += step) {
                int next = x < TEXELS ? pixel(pixels, x, y, step) : 0;
                if (next == color && x < TEXELS) continue;
                if ((color >>> 24) != 0) PixelEllipse.quad(consumer, matrix, left + start / step * unit, top + y / step * unit, left + x / step * unit, top + (y / step + 1) * unit, color);
                start = x;
                color = next;
            }
        }
        graphics.flush();
    }
}

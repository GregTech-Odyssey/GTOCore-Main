package com.gtocore.common.wireless.energy.map;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.function.IntSupplier;

/**
 * 电网星图图例：每行左侧是与画布相同画法的线路/标记样例，右侧是说明。
 */
public final class GridLegendPanel extends UIElement {

    private static final int ROW_HEIGHT = UISizes.CONTROL_HEIGHT;
    private static final int SAMPLE_WIDTH = 24, INDENT = 6;

    private enum Kind {
        IDLE,
        LOW,
        MID,
        HIGH,
        GUIDE,
        PATH,
        END,
        FOLDED,
        OUTSIDE,
        CURRENT,
        FOLDED_COUNT,
        TREND
    }

    public GridLegendPanel() {
        layout(l -> l.column().widthAuto());
        addChild(title(GridMapLang.LEGEND_TITLE, 0, 0, UITheme::panelText));
        addChild(title(GridMapLang.LEGEND_LINES, INDENT, 0, UITheme::textSecondary));
        row(Kind.IDLE, GridMapLang.LEGEND_IDLE);
        row(Kind.LOW, GridMapLang.LEGEND_LOW);
        row(Kind.MID, GridMapLang.LEGEND_MID);
        row(Kind.HIGH, GridMapLang.LEGEND_HIGH);
        row(Kind.GUIDE, GridMapLang.LEGEND_GUIDE);
        row(Kind.PATH, GridMapLang.LEGEND_PATH);
        addChild(title(GridMapLang.LEGEND_MARKS, INDENT, UISizes.SECTION_GAP, UITheme::textSecondary));
        row(Kind.END, GridMapLang.LEGEND_END);
        row(Kind.FOLDED, GridMapLang.LEGEND_FOLDED);
        row(Kind.OUTSIDE, GridMapLang.LEGEND_OUTSIDE);
        row(Kind.CURRENT, GridMapLang.LEGEND_CURRENT);
        row(Kind.FOLDED_COUNT, GridMapLang.LEGEND_FOLDED_COUNT);
        row(Kind.TREND, GridMapLang.LEGEND_TREND);
    }

    private static UIElement title(String key, int indent, int gap, IntSupplier color) {
        var text = Component.translatable(key);
        var line = TextLine.constant(UISizes.widthFor(text.getString()), text).bindClientColor(color);
        return line.layout(l -> l.marginLeft(indent).marginTop(gap));
    }

    private void row(Kind kind, String key) {
        var text = Component.translatable(key);
        var label = TextLine.constant(UISizes.widthFor(text.getString()), text).bindClientColor(UITheme::panelText);
        addChild(UIElement.centeredRow(ROW_HEIGHT).layout(l -> l.marginLeft(INDENT)).addChildren(new Sample(kind), label));
    }

    private static final class Sample extends Widget {

        private final Kind kind;

        private Sample(Kind kind) {
            super(0, 0, SAMPLE_WIDTH, ROW_HEIGHT);
            this.kind = kind;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX(), y = getPositionY(), w = getSizeWidth(), h = getSizeHeight();
            UIDraw.fillRect(graphics, x, y + 1, w, h - 2, UITheme.MAP_SKY_TOP);
            int mid = y + h / 2;
            switch (kind) {
                case IDLE, LOW, MID, HIGH, GUIDE, PATH -> nativeLine(graphics, x, w, mid);
                case END, FOLDED, OUTSIDE, CURRENT -> mark(graphics, x, w, mid);
                case FOLDED_COUNT -> {
                    body(graphics, x + 3, mid);
                    UIText.drawLeft(graphics, "+N", x + 12, mid - 4, UITheme.MAP_LABEL_DIM);
                }
                case TREND -> trend(graphics, x, mid);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void nativeLine(GuiGraphics graphics, int x, int w, int mid) {
            int s = Math.max(1, (int) Math.round(Minecraft.getInstance().getWindow().getGuiScale()));
            var pose = graphics.pose();
            pose.pushPose();
            pose.scale(1f / s, 1f / s, 1f);
            int left = (x + 2) * s, right = (x + w - 2) * s, center = mid * s + s / 2, width = right - left;
            switch (kind) {
                case IDLE -> line(graphics, left, right, center, 1, mix(UITheme.MAP_LINK_IDLE, UITheme.MAP_LINK_DIM, 0.5f));
                case LOW -> line(graphics, left, right, center, 1, mix(UITheme.MAP_LINK_LOW, UITheme.MAP_LINK_MID, 0.4f));
                case MID -> {
                    line(graphics, left, right, center, 2, UITheme.MAP_LINK_MID);
                    spark(graphics, left + width / 2, center, UITheme.MAP_SPARK, UITheme.MAP_LINK_LOW);
                }
                case HIGH -> {
                    line(graphics, left, right, center, 3, UITheme.MAP_LINK_HIGH);
                    UIDraw.fillRect(graphics, left + 6, center, width - 12, 1, UITheme.MAP_LINK_CORE);
                    spark(graphics, left + width / 2, center, mix(UITheme.MAP_SPARK, UITheme.MAP_SATURATED, 0.45f), UITheme.MAP_LINK_MID);
                }
                case GUIDE -> {
                    int color = mix(UITheme.MAP_ORBIT_FAR, UITheme.MAP_ORBIT, 0.5f);
                    for (int dx = 0; dx < width; dx += 2) UIDraw.fillRect(graphics, left + dx, center, 1, 1, color);
                }
                case PATH -> dashed(graphics, left, width, center);
                default -> {}
            }
            pose.popPose();
        }

        @OnlyIn(Dist.CLIENT)
        private void mark(GuiGraphics graphics, int x, int w, int mid) {
            switch (kind) {
                case END -> {
                    line(graphics, x, x + w - 8, mid, 2, UITheme.MAP_LINK_MID);
                    body(graphics, x + w - 8, mid);
                }
                case FOLDED -> {
                    int color = mix(UITheme.MAP_LINK_LOW, UITheme.MAP_LINK_MID, 0.4f);
                    UIDraw.fillRect(graphics, x + 2, mid, w - 15, 1, color);
                    UIDraw.fillRect(graphics, x + w - 14, mid - 2, 5, 5, UITheme.MAP_SPRITE_OUTLINE);
                    UIDraw.fillRect(graphics, x + w - 13, mid - 1, 3, 3, color);
                    body(graphics, x + w - 8, mid);
                }
                case OUTSIDE -> square(graphics, x + w / 2 - 4, mid - 4, 8, mix(UITheme.MAP_ORBIT, UITheme.MAP_LABEL_DIM, 0.12f));
                case CURRENT -> {
                    body(graphics, x + w / 2 - 3, mid);
                    brackets(graphics, x + w / 2 - 7, mid - 7, 14);
                }
                default -> {}
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static void line(GuiGraphics graphics, int left, int right, int mid, int thickness, int color) {
            int end = thickness + 2, endTop = mid - end / 2, top = mid - thickness / 2;
            UIDraw.fillRect(graphics, left + 1 + end, top, right - 2 * end - left - 2, thickness, color);
            UIDraw.fillRect(graphics, left + 1, endTop, end, end, color);
            UIDraw.fillRect(graphics, right - 1 - end, endTop, end, end, color);
        }

        @OnlyIn(Dist.CLIENT)
        private static void body(GuiGraphics graphics, int left, int mid) {
            UIDraw.fillRect(graphics, left, mid - 3, 6, 6, UITheme.MAP_SPRITE_OUTLINE);
            UIDraw.fillRect(graphics, left + 1, mid - 2, 4, 4, UITheme.MAP_LABEL_DIM);
        }

        @OnlyIn(Dist.CLIENT)
        private static void square(GuiGraphics graphics, int left, int top, int size, int color) {
            UIDraw.fillRect(graphics, left, top, size, 1, color);
            UIDraw.fillRect(graphics, left, top + size - 1, size, 1, color);
            UIDraw.fillRect(graphics, left, top + 1, 1, size - 2, color);
            UIDraw.fillRect(graphics, left + size - 1, top + 1, 1, size - 2, color);
        }

        @OnlyIn(Dist.CLIENT)
        private static void brackets(GuiGraphics graphics, int left, int top, int size) {
            int right = left + size - 1, bottom = top + size - 1, color = UITheme.MAP_CURRENT;
            UIDraw.fillRect(graphics, left, top, 3, 1, color);
            UIDraw.fillRect(graphics, left, top, 1, 3, color);
            UIDraw.fillRect(graphics, right - 2, top, 3, 1, color);
            UIDraw.fillRect(graphics, right, top, 1, 3, color);
            UIDraw.fillRect(graphics, left, bottom, 3, 1, color);
            UIDraw.fillRect(graphics, left, bottom - 2, 1, 3, color);
            UIDraw.fillRect(graphics, right - 2, bottom, 3, 1, color);
            UIDraw.fillRect(graphics, right, bottom - 2, 1, 3, color);
        }

        @OnlyIn(Dist.CLIENT)
        private static void spark(GuiGraphics graphics, int head, int mid, int headColor, int tailColor) {
            UIDraw.fillRect(graphics, head - 2, mid - 1, 2, 2, tailColor);
            UIDraw.fillRect(graphics, head, mid - 1, 2, 2, headColor);
        }

        private static int mix(int a, int b, float t) {
            int r = Math.round((a >> 16 & 0xFF) * (1 - t) + (b >> 16 & 0xFF) * t);
            int g = Math.round((a >> 8 & 0xFF) * (1 - t) + (b >> 8 & 0xFF) * t);
            int bl = Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
            return 0xFF000000 | r << 16 | g << 8 | bl;
        }

        @OnlyIn(Dist.CLIENT)
        private static void dashed(GuiGraphics graphics, int x, int w, int mid) {
            for (int dx = 3; dx < w - 3; dx += 5) {
                UIDraw.fillRect(graphics, x + dx, mid, Math.min(dx + 3, w - 3) - dx, 1, UITheme.MAP_PATH);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static void trend(GuiGraphics graphics, int x, int mid) {
            int up = x + 4, down = x + 15;
            UIDraw.fillRect(graphics, up + 2, mid - 1, 1, 1, UITheme.MAP_NET_UP);
            UIDraw.fillRect(graphics, up + 1, mid, 3, 1, UITheme.MAP_NET_UP);
            UIDraw.fillRect(graphics, up, mid + 1, 5, 1, UITheme.MAP_NET_UP);
            UIDraw.fillRect(graphics, down, mid - 1, 5, 1, UITheme.MAP_NET_DOWN);
            UIDraw.fillRect(graphics, down + 1, mid, 3, 1, UITheme.MAP_NET_DOWN);
            UIDraw.fillRect(graphics, down + 2, mid + 1, 1, 1, UITheme.MAP_NET_DOWN);
        }
    }
}

package com.gtocore.api.gui.ui.styletemplate;

import com.gtolib.GTOCore;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Ore UI（基岩版风格）贴图集，移植自 LDLib2 的 {@code OreSprites} 与 {@code ore.lss}
 * （Low-Drag-MC/LDLib2，LGPL-3.0；贴图 {@code textures/gui/ore_styles.png} 原样取自该项目）。
 * <p>
 * 每个精灵是图集上的一块区域加九宫格边距（左、上、右、下），拉伸到任意尺寸时四角不变、边与中心拉伸。
 * 按钮类精灵底边 4 像素是凸起的"台阶"，内容要比几何中心上移 1 像素才显得居中。
 */
public final class OreSprites {

    private OreSprites() {}

    public static final ResourceLocation TEXTURE = GTOCore.id("textures/gui/ore_styles.png");
    private static final int ATLAS = 256;

    // ==================== 按钮（底部 4px 台阶） ====================
    public static final Sprite BTN_DEFAULT = sprite(0, 0, 5, 7, 2, 2, 2, 4);
    public static final Sprite BTN_PRESSED = sprite(5, 0, 5, 7, 2, 2, 2, 4);
    public static final Sprite BTN_DISABLED = sprite(10, 0, 5, 7, 2, 2, 2, 4);
    public static final Sprite BTN_DEFAULT_GREEN = sprite(26, 0, 5, 7, 2, 2, 2, 4);
    public static final Sprite BTN_PRESSED_GREEN = sprite(31, 0, 5, 7, 2, 2, 2, 4);
    public static final Sprite BTN_DEFAULT_RED = sprite(36, 0, 5, 7, 2, 2, 2, 4);
    public static final Sprite BTN_PRESSED_RED = sprite(41, 0, 5, 7, 2, 2, 2, 4);

    public static final Sprite BTN_DEFAULT_SMALL = sprite(46, 0, 5, 5, 2, 2, 2, 2);
    public static final Sprite BTN_HOVER_SMALL = sprite(51, 0, 5, 5, 2, 2, 2, 2);
    public static final Sprite BTN_PRESSED_SMALL = sprite(56, 0, 5, 5, 2, 2, 2, 2);
    public static final Sprite BTN_DISABLED_SMALL = sprite(61, 0, 3, 3, 1, 1, 1, 1);
    public static final Sprite BTN_DEFAULT_SMALL_GREEN = sprite(64, 0, 5, 5, 2, 2, 2, 2);
    public static final Sprite BTN_HOVER_SMALL_GREEN = sprite(69, 0, 5, 5, 2, 2, 2, 2);
    public static final Sprite BTN_PRESSED_SMALL_GREEN = sprite(74, 0, 5, 5, 2, 2, 2, 2);

    public static final Sprite BTN_RECT_DEFAULT = sprite(0, 7, 13, 14, 2, 2, 2, 3);
    public static final Sprite BTN_RECT_DISABLED = sprite(0, 21, 13, 14, 2, 2, 2, 3);
    public static final Sprite BTN_RECT_HOVER = sprite(0, 35, 13, 14, 2, 2, 2, 3);

    // ==================== 槽位、框 ====================
    /** 物品槽底（图集原图，中心 #5C6565，偏暗）。 */
    public static final Sprite SLOT_LIGHT = sprite(15, 0, 3, 3, 1, 1, 1, 1);
    /** 流体槽底（图集原图，中心 #48494A，更暗）。 */
    public static final Sprite SLOT_GRAY = sprite(18, 0, 3, 3, 1, 1, 1, 1);
    /**
     * 明亮物品槽：图集里的槽偏暗，这里按 SLOT_LIGHT 的结构（左上内阴影、右下高光、1 像素边）改用原版物品槽的颜色——
     * 中心 #8B8B8B、左上 #373737、右下白色，与原版/GTM 界面里的槽一模一样。
     */
    public static final IGuiTexture SLOT_BRIGHT = new Bevel(0xFF373737, 0xFFFFFFFF, 0xFF8B8B8B);
    /** 流体槽：与 {@link #SLOT_BRIGHT} 同样的斜面，底色压深到 #6A6A6A，和物品槽一眼可分，又不至于黑成一块。 */
    public static final IGuiTexture FLUID_SLOT = new Bevel(0xFF373737, 0xFFFFFFFF, 0xFF6A6A6A);
    /** 深色小面板。 */
    public static final Sprite RECT = sprite(21, 0, 3, 4, 1, 1, 1, 2);
    /** 最深的内凹框（ore.lss 的输入框底）。 */
    public static final Sprite RECT2 = sprite(24, 0, 3, 5, 1, 3, 1, 1);
    /** 获得焦点时叠加的白框。 */
    public static final Sprite WHITE_BORDER = sprite(15, 3, 3, 3, 1, 1, 1, 1);

    // ==================== 开关、标签页 ====================
    public static final Sprite SWITCH_ON = sprite(13, 7, 24, 14, 0, 0, 0, 0);
    public static final Sprite SWITCH_OFF = sprite(13, 21, 24, 14, 0, 0, 0, 0);

    public static final Sprite TAB_OFF_DEFAULT = sprite(50, 7, 11, 7, 2, 2, 2, 4);
    public static final Sprite TAB_OFF_HOVER = sprite(50, 14, 11, 7, 2, 2, 2, 4);
    public static final Sprite TAB_ON_DEFAULT = sprite(61, 7, 11, 7, 2, 4, 2, 2);

    /** 窗口底色（原版容器的 #C6C6C6）。 */
    public static final int WINDOW_FILL = 0xFFC6C6C6;

    // ==================== 大面板（深浅两档成对：BORDER/BORDER_3、BORDER_4/BORDER_2、BORDER_6/BORDER_7） ====================
    /** 深灰凸起面板。 */
    public static final Sprite BORDER = sprite(0, 71, 62, 64, 5, 5, 5, 7);
    /** 暖灰凸起面板（BORDER_4 的浅色档）。 */
    public static final Sprite BORDER_2 = sprite(62, 71, 62, 64, 5, 5, 5, 7);
    /** 浅灰凸起面板（BORDER 的浅色档），区块用它。 */
    public static final Sprite BORDER_3 = sprite(0, 135, 62, 64, 5, 5, 5, 7);
    public static final Sprite BORDER_4 = sprite(62, 135, 62, 64, 5, 5, 5, 7);
    public static final Sprite BORDER_5 = sprite(0, 199, 50, 52, 3, 3, 3, 5);
    public static final Sprite BORDER_6 = sprite(128, 1, 128, 128, 6, 6, 6, 8);
    /** 浅灰大面板（BORDER_6 的浅色档），窗口外框用它。 */
    public static final Sprite BORDER_7 = sprite(128, 128, 128, 128, 3, 3, 3, 5);
    /**
     * 窗口外框：保留 BORDER_7 的圆角描边、内侧高光与底部厚边，底色由 #A1A1A1 换成原版容器的 #C6C6C6——
     * 原版物品槽的白色高光在深一些的底色上会连成一片白格子，显得发白。
     */
    public static final IGuiTexture BORDER_7_BRIGHT = new Refilled(BORDER_7, OreSprites.WINDOW_FILL, 2, 2, 2, 4);
    /**
     * 区块面板：1 像素 #8B8B8B 细边 + 比窗口略深的 #BDBDBD 底，像一块浅浅下凹的区域。
     * 不用 BORDER_3 的双层边框——它的浅色外圈和内侧高光在 #C6C6C6 窗口里会变成好几道发白的线。
     */
    public static final IGuiTexture GROOVE = new Bevel(0xFF8B8B8B, 0xFF8B8B8B, 0xFFBDBDBD);
    /** 按钮乘色：Ore 按钮面 #D0D1D4 比原版底色还亮，压暗一档到约 #9C9C9C，和原版一样"按钮比底色深"。 */
    public static final int BUTTON_TINT = 0xFFC0C0C0;

    // ==================== 图标 ====================
    public static final Sprite CHECK = sprite(50, 35, 10, 10, 0, 0, 0, 0);
    public static final Sprite DOWN = sprite(60, 35, 10, 10, 0, 0, 0, 0);

    // ==================== 文字色（ore.lss） ====================
    /** 浅色按钮、浅色面板上的文字（无阴影）。 */
    public static final int TEXT_DARK = 0xFF222222;
    /** 禁用按钮上的文字。 */
    public static final int TEXT_DISABLED = 0xFF666666;
    /** 深色框、彩色按钮上的文字。 */
    public static final int TEXT_LIGHT = 0xFFFFFFFF;
    /** 按住时给按钮底图叠的颜色。 */
    public static final int PRESSED_TINT = 0xFFDDDDDD;

    private static Sprite sprite(int u, int v, int w, int h, int left, int top, int right, int bottom) {
        return new Sprite(u, v, w, h, left, top, right, bottom, -1);
    }

    /**
     * 把 {@code base} 偏移 {@code (dx, dy)} 后再画。Ore 按钮类贴图底部是 4 像素厚边，按控件几何中心摆的图标会比按钮面低，
     * 把底图下移 1 像素，图标就正好落在按钮面中央（左侧标签页、配置按钮用）。
     */
    public record Shifted(IGuiTexture base, int dx, int dy) implements IGuiTexture {

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            base.draw(graphics, mouseX, mouseY, x + dx, y + dy, width, height);
        }
    }

    /**
     * 先画 {@code base}，再用 {@code fill} 重涂它的内部（距四边 {@code left/top/right/bottom}，四角各再内收 1 像素保住圆角），
     * 用来在不改贴图的前提下换一个精灵的底色。
     */
    public record Refilled(Sprite base, int fill, int left, int top, int right, int bottom) implements IGuiTexture {

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            if (width <= 0 || height <= 0) return;
            int l = (int) x, t = (int) y;
            base.draw(graphics, l, t, width, height);
            int x0 = l + left, y0 = t + top, x1 = l + width - right, y1 = t + height - bottom;
            graphics.fill(x0 + 1, y0, x1 - 1, y1, fill);
            graphics.fill(x0, y0 + 1, x0 + 1, y1 - 1, fill);
            graphics.fill(x1 - 1, y0 + 1, x1, y1 - 1, fill);
        }
    }

    /** 1 像素斜面：左上 {@code topLeft}、右下 {@code bottomRight}（另两个角取底色）、中间 {@code fill}，可拉伸到任意尺寸。 */
    public record Bevel(int topLeft, int bottomRight, int fill) implements IGuiTexture {

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            int l = (int) x, t = (int) y, r = l + width, b = t + height;
            if (width <= 0 || height <= 0) return;
            graphics.fill(l, t, r, b, fill);
            graphics.fill(l, t, r - 1, t + 1, topLeft);
            graphics.fill(l, t, l + 1, b - 1, topLeft);
            graphics.fill(l + 1, b - 1, r, b, bottomRight);
            graphics.fill(r - 1, t + 1, r, b, bottomRight);
        }
    }

    /**
     * 图集上的一块九宫格精灵，实现 LDLib1 的 {@link IGuiTexture}，可直接作为控件背景使用。
     */
    public record Sprite(int u, int v, int width, int height, int left, int top, int right, int bottom, int tint)
            implements IGuiTexture {

        /** 乘上颜色后的副本（对应 lss 里的 {@code color(#rrggbb)}）。 */
        public Sprite tinted(int argb) {
            return new Sprite(u, v, width, height, left, top, right, bottom, argb);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int w, int h) {
            draw(graphics, (int) x, (int) y, w, h);
        }

        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics graphics, int x, int y, int w, int h) {
            if (w <= 0 || h <= 0) return;
            RenderSystem.enableBlend();
            if (tint != -1) {
                graphics.setColor(((tint >> 16) & 0xFF) / 255f, ((tint >> 8) & 0xFF) / 255f, (tint & 0xFF) / 255f, ((tint >>> 24) & 0xFF) / 255f);
            }
            // 目标尺寸比边距还小时按比例压缩边距
            int l = Math.min(left, w / 2), r = Math.min(right, w - l);
            int t = Math.min(top, h / 2), b = Math.min(bottom, h - t);
            int cw = w - l - r, ch = h - t - b;
            int su = width - left - right, sv = height - top - bottom;
            // 上排
            part(graphics, x, y, l, t, u, v, left, top);
            part(graphics, x + l, y, cw, t, u + left, v, su, top);
            part(graphics, x + l + cw, y, r, t, u + width - right, v, right, top);
            // 中排
            part(graphics, x, y + t, l, ch, u, v + top, left, sv);
            part(graphics, x + l, y + t, cw, ch, u + left, v + top, su, sv);
            part(graphics, x + l + cw, y + t, r, ch, u + width - right, v + top, right, sv);
            // 下排
            part(graphics, x, y + t + ch, l, b, u, v + height - bottom, left, bottom);
            part(graphics, x + l, y + t + ch, cw, b, u + left, v + height - bottom, su, bottom);
            part(graphics, x + l + cw, y + t + ch, r, b, u + width - right, v + height - bottom, right, bottom);
            if (tint != -1) graphics.setColor(1f, 1f, 1f, 1f);
        }

        @OnlyIn(Dist.CLIENT)
        private static void part(GuiGraphics graphics, int x, int y, int w, int h, int u, int v, int uw, int vh) {
            if (w <= 0 || h <= 0 || uw <= 0 || vh <= 0) return;
            graphics.blit(TEXTURE, x, y, w, h, u, v, uw, vh, ATLAS, ATLAS);
        }
    }
}

package com.gtocore.api.gui.ui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 多行文本的换行、测量与绘制：先按空格分词填行，单词本身超宽时按字符硬切。
 * 行高为字体行高减 2，行间距由调用方给出。
 */
@OnlyIn(Dist.CLIENT)
public final class TextLayout {

    private TextLayout() {}

    /** 按最大宽度换行后的各行文字与整体尺寸。 */
    public record Block(List<String> lines, int width, int height) {}

    public static Block layout(String text, int maxWidth, int lineGap) {
        if (text.isEmpty()) return new Block(List.of(), 0, 0);
        var font = Minecraft.getInstance().font;
        var lines = wrap(text, font, maxWidth);
        int width = 0;
        for (var line : lines) width = Math.max(width, font.width(line));
        int lineHeight = font.lineHeight - 2;
        int height = lines.isEmpty() ? lineHeight : lines.size() * lineHeight + (lines.size() - 1) * lineGap;
        return new Block(lines, width, height);
    }

    public static void draw(GuiGraphics graphics, Block block, int lineGap, int color) {
        int lineHeight = Minecraft.getInstance().font.lineHeight - 2;
        var lines = block.lines();
        for (int i = 0; i < lines.size(); i++) {
            DrawerHelper.drawText(graphics, lines.get(i), 0f, i * (lineHeight + lineGap) - 1, 1f, color, false);
        }
    }

    private static List<String> wrap(String text, Font font, int maxWidth) {
        var lines = new ArrayList<String>();
        if (maxWidth <= 0) {
            lines.add(text);
            return lines;
        }
        var current = "";
        for (var word : text.split(" ", -1)) {
            var candidate = current.isEmpty() ? word : current + " " + word;
            if (font.width(candidate) <= maxWidth) {
                current = candidate;
            } else if (!current.isEmpty()) {
                lines.add(current);
                current = word;
            } else {
                splitByWidth(word, font, maxWidth, lines);
                current = "";
            }
        }
        if (!current.isEmpty()) lines.add(current);
        else if (lines.isEmpty()) lines.add("");
        return lines;
    }

    private static void splitByWidth(String word, Font font, int maxWidth, List<String> out) {
        var remaining = word;
        while (!remaining.isEmpty()) {
            int cut = 1;
            while (cut < remaining.length() && font.width(remaining.substring(0, cut + 1)) <= maxWidth) cut++;
            out.add(remaining.substring(0, cut));
            remaining = remaining.substring(cut);
        }
    }
}

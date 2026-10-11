package com.gtocore.api.lang;

import com.gtolib.utils.StringUtils;

import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.client.util.TooltipHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import dev.shadowsoffire.placebo.color.GradientColor;

import java.util.function.UnaryOperator;

/**
 * 提示文字片段的着色与拼接；彩虹、滚动色为动态文字，绘制时取当前帧颜色。
 */
public final class Tx {

    private Tx() {}

    public static MutableComponent lit(Object value) {
        return Component.literal(String.valueOf(value));
    }

    public static MutableComponent translatable(String key, Object... args) {
        return Component.translatable(key, args);
    }

    public static MutableComponent join(Component... parts) {
        MutableComponent result = Component.empty();
        for (Component part : parts) result.append(part);
        return result;
    }

    public static MutableComponent color(Component c, int rgb) {
        return c.copy().withStyle(s -> s.withColor(rgb));
    }

    public static MutableComponent color(Component c, ChatFormatting format) {
        return c.copy().withStyle(format);
    }

    public static MutableComponent gray(Component c) {
        return color(c, ChatFormatting.GRAY);
    }

    public static MutableComponent white(Component c) {
        return color(c, ChatFormatting.WHITE);
    }

    public static MutableComponent gold(Component c) {
        return color(c, ChatFormatting.GOLD);
    }

    public static MutableComponent red(Component c) {
        return color(c, ChatFormatting.RED);
    }

    public static MutableComponent yellow(Component c) {
        return color(c, ChatFormatting.YELLOW);
    }

    public static MutableComponent green(Component c) {
        return color(c, ChatFormatting.GREEN);
    }

    public static MutableComponent aqua(Component c) {
        return color(c, ChatFormatting.AQUA);
    }

    public static MutableComponent lightPurple(Component c) {
        return color(c, ChatFormatting.LIGHT_PURPLE);
    }

    public static MutableComponent blue(Component c) {
        return color(c, ChatFormatting.BLUE);
    }

    public static MutableComponent orange(Component c) {
        return color(c, 0xFFA500);
    }

    public static MutableComponent obfuscated(Component c) {
        return color(c, ChatFormatting.OBFUSCATED);
    }

    public static MutableComponent rainbow(Component c) {
        return Tips.dynamic(() -> c.copy().withStyle(TooltipHelper.RAINBOW.getCurrent()));
    }

    public static MutableComponent rainbowFast(Component c) {
        return Tips.dynamic(() -> c.copy().withStyle(TooltipHelper.RAINBOW_FAST.getCurrent()));
    }

    public static MutableComponent rainbowSlow(Component c) {
        return Tips.dynamic(() -> c.copy().withStyle(TooltipHelper.RAINBOW_SLOW.getCurrent()));
    }

    public static MutableComponent rainbowGradient(Component c) {
        return c.copy().withStyle(s -> s.withColor(GradientColor.RAINBOW));
    }

    public static MutableComponent rainbowGradient(Component c, float speed, int offset, boolean reverse) {
        return c.copy().withStyle(s -> s.withColor(new OffsetGradientColor(speed, offset, reverse)));
    }

    public static MutableComponent rainbowGradient(Component c, int offset) {
        return c.copy().withStyle(s -> s.withColor(new OffsetGradientColor(1f, offset)));
    }

    public static MutableComponent scrollSuprachronal(Component c) {
        return scroll(c, StringUtils::white_blue);
    }

    public static MutableComponent scrollFullColor(Component c) {
        return scroll(c, StringUtils::full_color);
    }

    public static MutableComponent scrollBioware(Component c) {
        return scroll(c, StringUtils::dark_green);
    }

    public static MutableComponent scrollOptical(Component c) {
        return scroll(c, StringUtils::golden);
    }

    public static MutableComponent scrollExotic(Component c) {
        return scroll(c, StringUtils::purplish_red);
    }

    public static MutableComponent scrollCosmic(Component c) {
        return scroll(c, StringUtils::dark_purplish_red);
    }

    private static MutableComponent scroll(Component c, UnaryOperator<String> effect) {
        return Tips.dynamic(() -> Component.literal(effect.apply(c.getString())));
    }
}

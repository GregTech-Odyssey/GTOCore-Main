package com.gtocore.common.wireless.energy.map;

import com.gtolib.api.data.Dimension;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.uipro.Level;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * 电网星图共用的读数格式：数值与速率、提亮后的电压文字、充满/耗尽时间、星球与维度名称（面板与画布共用）。
 */
public final class GridFormat {

    public static final Component NO_VALUE = Component.literal("—");
    static final String[] WINDOW_KEYS = { GridMapLang.WINDOW_NOW, GridMapLang.WINDOW_MINUTE, GridMapLang.WINDOW_HOUR, GridMapLang.WINDOW_DAY };
    static final String[] WINDOW_TIPS = { GridMapLang.WINDOW_NOW_TIP, GridMapLang.WINDOW_MINUTE_TIP, GridMapLang.WINDOW_HOUR_TIP,
            GridMapLang.WINDOW_DAY_TIP };
    private static final double STEADY = 1;
    private static final double TICKS_PER_SECOND = 20;
    private static final double MINUTE = 60, HOUR = 3600, DAY = 86_400, YEAR = 365 * DAY;
    private static final long MAX_YEARS = 999;
    private static final String[] UNITS = { "", "K", "M", "G", "T", "P", "E", "Z", "Y", "R", "Q" };
    private static final String[] TIER_TEXT = brightTiers();
    private static final int[] TIER_COLORS = { 0xFFA8A8A8, 0xFFDCDCDC, 0xFF7FE0FF, 0xFFFFC060, 0xFFE08CFF, 0xFF8C8CFF, 0xFFFF8CF0, 0xFFFF9A6B,
            0xFF5FD8D8, 0xFFFF7AA2, 0xFFB6F06A, 0xFF6FD8A0, 0xFFFFF07A, 0xFF7AA8FF, 0xFFFFB0B0 };
    private static final Component[] TIER_COMPONENTS = tierComponents();

    private GridFormat() {}

    public static String amount(double value) {
        if (!Double.isFinite(value)) return "0";
        return value < 0 ? "-" + NumberUtils.formatDouble(-value) : NumberUtils.formatDouble(value);
    }

    public static String compact(double value) {
        if (!Double.isFinite(value) || Math.abs(value) < 1) return "0";
        double abs = Math.abs(value);
        int unit = 0;
        while (abs >= 999.5 && unit < UNITS.length - 1) {
            abs /= 1000;
            unit++;
        }
        String digits = abs < 9.995 ? String.format(Locale.ROOT, "%.2f", abs) : abs < 99.95 ? String.format(Locale.ROOT, "%.1f", abs) :
                String.format(Locale.ROOT, "%.0f", abs);
        return (value < 0 ? "-" : "") + (unit == 0 ? String.valueOf(Math.round(abs)) : digits + UNITS[unit]);
    }

    public static String signed(double value) {
        if (!Double.isFinite(value) || Math.abs(value) < STEADY) return "0";
        return value > 0 ? "+" + NumberUtils.formatDouble(value) : "-" + NumberUtils.formatDouble(-value);
    }

    public static Component rate(double perTick) {
        return Component.translatable(GridMapLang.RATE, amount(perTick));
    }

    public static Component signedRate(double perTick) {
        return Component.translatable(GridMapLang.RATE, signed(perTick));
    }

    public static Component netRate(double perTick) {
        if (!Double.isFinite(perTick) || Math.abs(perTick) < STEADY) return rate(0);
        return Component.translatable(GridMapLang.RATE, (perTick > 0 ? "§a▲§r " : "§6▼§r ") + signed(perTick));
    }

    public static Level trend(double perTick) {
        if (!Double.isFinite(perTick) || Math.abs(perTick) < STEADY) return Level.NORMAL;
        return perTick > 0 ? Level.GOOD : Level.ERROR;
    }

    public static String tier(int tier) {
        return tier >= 0 && tier < TIER_TEXT.length ? TIER_TEXT[tier] : "—";
    }

    public static String tierPlain(int tier) {
        return tier >= 0 && tier < GTValues.VN.length ? GTValues.VN[tier] : "—";
    }

    public static int tierColor(int tier) {
        return TIER_COLORS[Math.max(0, Math.min(TIER_COLORS.length - 1, tier))];
    }

    public static Component tierText(int tier) {
        return tier >= 0 && tier < TIER_COMPONENTS.length ? TIER_COMPONENTS[tier] : NO_VALUE;
    }

    private static Component[] tierComponents() {
        var result = new Component[GTValues.VN.length];
        for (int i = 0; i < result.length; i++) result[i] = Component.literal(GTValues.VN[i]).withStyle(Style.EMPTY.withColor(tierColor(i) & 0xFFFFFF));
        return result;
    }

    private static String[] brightTiers() {
        var result = new String[GTValues.VNF.length];
        for (int i = 0; i < result.length; i++) {
            var chars = GTValues.VNF[i].toCharArray();
            for (int c = 0; c + 1 < chars.length; c++) {
                if (chars[c] == '§') chars[c + 1] = bright(chars[c + 1]);
            }
            result[i] = new String(chars);
        }
        return result;
    }

    private static char bright(char code) {
        return switch (code) {
            case '0', '8' -> '7';
            case '1' -> '9';
            case '2' -> 'a';
            case '3' -> 'b';
            case '4' -> 'c';
            case '5' -> 'd';
            default -> code;
        };
    }

    static Component storedDetail(double stored, double capacity) {
        return Component.translatable(GridMapLang.STORED_DETAIL, amount(stored), amount(capacity));
    }

    static long ppm(double stored, double capacity) {
        if (!(capacity > 0) || !Double.isFinite(stored)) return 0;
        return Math.max(0, Math.min(1_000_000L, Math.round(stored / capacity * 1_000_000)));
    }

    public static Component eta(double stored, double capacity, double deltaPerTick) {
        if (!Double.isFinite(deltaPerTick) || Math.abs(deltaPerTick) < STEADY) return Component.translatable(GridMapLang.ETA_STEADY);
        if (deltaPerTick > 0) {
            if (stored >= capacity) return Component.translatable(GridMapLang.ETA_FULL);
            var time = duration((capacity - stored) / (deltaPerTick * TICKS_PER_SECOND));
            return time == null ? Component.translatable(GridMapLang.ETA_FILL_BEYOND) : Component.translatable(GridMapLang.ETA_FILL, time);
        }
        if (stored <= 0) return Component.translatable(GridMapLang.ETA_EMPTY);
        var time = duration(stored / (-deltaPerTick * TICKS_PER_SECOND));
        return time == null ? Component.translatable(GridMapLang.ETA_DRAIN_BEYOND) : Component.translatable(GridMapLang.ETA_DRAIN, time);
    }

    @Nullable
    private static Component duration(double seconds) {
        if (!Double.isFinite(seconds) || seconds > MAX_YEARS * YEAR) return null;
        if (seconds < 3 * MINUTE) return Component.translatable(GridMapLang.TIME_SECONDS, Math.max(1, (long) Math.ceil(seconds)));
        if (seconds < 3 * HOUR) return Component.translatable(GridMapLang.TIME_MINUTES, Math.round(seconds / MINUTE));
        if (seconds < 3 * DAY) return Component.translatable(GridMapLang.TIME_HOURS, Math.round(seconds / HOUR));
        if (seconds < 2 * YEAR) return Component.translatable(GridMapLang.TIME_DAYS, Math.round(seconds / DAY));
        return Component.translatable(GridMapLang.TIME_YEARS, Math.min(MAX_YEARS, Math.round(seconds / YEAR)));
    }

    public static Component bodyName(@Nullable ResourceKey<net.minecraft.world.level.Level> key) {
        if (key == null) return NO_VALUE;
        var dimension = Dimension.get(key);
        if (dimension != null && !dimension.isWithinGalaxy()) return Component.translatable(dimension.getKey());
        var location = key.location();
        return Component.translatableWithFallback("planet.%s.%s".formatted(location.getNamespace(), location.getPath()), titleCase(location.getPath()));
    }

    public static Component dimensionName(@Nullable ResourceKey<net.minecraft.world.level.Level> key) {
        if (key == null) return NO_VALUE;
        var dimension = Dimension.get(key);
        return dimension != null ? Component.translatable(dimension.getKey()) : Component.literal(key.location().toString());
    }

    static Component location(@Nullable ResourceKey<net.minecraft.world.level.Level> key) {
        var dimension = key == null ? null : Dimension.get(key);
        if (dimension == null || !dimension.isWithinGalaxy()) return Component.translatable(GridMapLang.REALM);
        return Component.translatable(GridMapLang.LOCATION_VALUE, Component.translatable(dimension.getGalaxy().getTranslationKey()),
                Component.translatable(dimension.getKey()));
    }

    private static String titleCase(String path) {
        var builder = new StringBuilder(path.length());
        boolean upper = true;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '_') {
                builder.append(' ');
                upper = true;
            } else {
                builder.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return builder.toString();
    }
}

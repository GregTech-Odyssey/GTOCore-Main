package com.gtocore.utils;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterEnumLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import lombok.Getter;

@Getter
@DataGeneratorScanned
@RegisterEnumLang(keyPrefix = "gtocore")
public enum Evaluate {

    Terrible("糟糕", "Terrible", ChatFormatting.DARK_RED),
    Poor("较差", "Poor", ChatFormatting.RED),
    BelowAverage("中下", "Below Average", ChatFormatting.GOLD),
    Average("一般", "Average", ChatFormatting.YELLOW),
    AboveAverage("中上", "Above Average", ChatFormatting.GREEN),
    Good("良好", "Good", ChatFormatting.DARK_GREEN),
    Excellent("优秀", "Excellent", ChatFormatting.AQUA),
    Outstanding("卓越", "Outstanding", ChatFormatting.LIGHT_PURPLE),
    Perfect("完美", "Perfect", ChatFormatting.DARK_PURPLE);

    @RegisterEnumLang.CnValue("evaluate")
    private final String cn;
    @RegisterEnumLang.EnValue("evaluate")
    private final String en;
    private final ChatFormatting color;

    Evaluate(String cn, String en, ChatFormatting color) {
        this.cn = cn;
        this.en = en;
        this.color = color;
    }

    public MutableComponent getComponent() {
        return Component.translatable("gtocore.evaluate." + name()).withStyle(color);
    }

    public static Evaluate fromScore(int score) {
        if (score <= 20) {
            return Terrible;
        }
        if (score <= 30) {
            return Poor;
        }
        if (score <= 40) {
            return BelowAverage;
        }
        if (score <= 50) {
            return Average;
        }
        if (score <= 60) {
            return AboveAverage;
        }
        if (score <= 70) {
            return Good;
        }
        if (score <= 80) {
            return Excellent;
        }
        if (score <= 90) {
            return Outstanding;
        }
        return Perfect;
    }
}

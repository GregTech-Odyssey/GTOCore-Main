package com.gtocore.client.screen.starmap.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 星图底座的天体装饰：火箭星图与电网星图各自决定天体的着色、名称颜色、附加绘制、提示、选中与可选性。
 */
@OnlyIn(Dist.CLIENT)
public interface BodyDecorator {

    BodyDecorator NONE = new BodyDecorator() {};

    default int tint(int body) {
        return 0xFFFFFFFF;
    }

    default int labelColor(int body, int fallback) {
        return fallback;
    }

    default int orbitColor(int system, int order, int fallback) {
        return fallback;
    }

    default int anchorTint(int anchor) {
        return StarSprites.NO_TINT;
    }

    default int anchorLabelColor(int anchor, int fallback) {
        return fallback;
    }

    default boolean showsLabel(int body) {
        return true;
    }

    default boolean showsHereMarker(int body) {
        return true;
    }

    default boolean litOrbits() {
        return true;
    }

    default void drawAfter(GuiGraphics graphics, int body, int x, int y, int size, boolean hovered) {}

    default List<Component> tooltip(int body) {
        return Collections.emptyList();
    }

    default List<Component> anchorTooltip(int anchor) {
        return Collections.emptyList();
    }

    default boolean isSelected(int body) {
        return false;
    }

    default boolean isPickable(int body) {
        return true;
    }

    @Nullable
    default Component systemSubtitle(int system) {
        return null;
    }
}

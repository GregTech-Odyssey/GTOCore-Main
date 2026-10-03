package com.gtocore.common.wireless.energy.map;

import com.gtolib.api.data.Galaxy;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Dock;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.view.ZoomBar;

import net.minecraft.network.chat.Component;

import dev.vfyjxf.taffy.style.FlexWrap;

import java.util.function.BooleanSupplier;

/**
 * 电网星图底部悬浮栏：星系、缩放与定位、显示开关、返回机器、操作说明。
 */
final class GridMapDock {

    private static final int GALAXY_BUTTON_WIDTH = 64;

    private GridMapDock() {}

    static Dock build(GridMapRoot root) {
        var dock = new Dock().addGroup(galaxies(root.context())).addGroup(zoom(root)).addGroup(toggles(root)).addGroup(back(root))
                .addGroup(InfoIcon.info(GridMapLang.HELP, GridMapLang.HELP_SELECT, GridMapLang.HELP_DESELECT, GridMapLang.HELP_COMPARE,
                        GridMapLang.HELP_MOONS, CanvasView.HELP_PAN, CanvasView.HELP_ZOOM));
        dock.layout(l -> l.flexWrap(FlexWrap.WRAP));
        return dock;
    }

    private static UIElement galaxies(GridMapContext ctx) {
        var row = ZoomBar.dockRow();
        for (var galaxy : Galaxy.all()) {
            if (galaxy == Galaxy.NONE) continue;
            var button = Button.text(GALAXY_BUTTON_WIDTH, UISizes.DOCK_BUTTON_SIZE, Component.translatable(galaxy.getTranslationKey())).setOnClientClick(() -> {
                var navigator = ctx.navigator();
                if (navigator != null) navigator.showGalaxy(galaxy);
            });
            button.setSelected(() -> {
                var navigator = ctx.navigator();
                return navigator != null && navigator.isGalaxyShown(galaxy);
            });
            button.clientDisabled(() -> {
                var navigator = ctx.navigator();
                return navigator == null || !navigator.hasGalaxy(galaxy);
            });
            button.tooltips(GridMapLang.GALAXY_TIP);
            row.addChild(button);
        }
        return row;
    }

    private static ZoomBar zoom(GridMapRoot root) {
        var locate = ZoomBar.dockButton(UITheme.CANVAS_LOCATE, GridMapLang.LOCATE, root::locateCurrent);
        return ZoomBar.of(root.canvas(), true).zoom(false).fit(ZoomBar.FIT).minimap().add(locate).build();
    }

    private static UIElement toggles(GridMapRoot root) {
        var toggles = root.toggles();
        return ZoomBar.dockRow().addChildren(
                toggle(GridMapLang.TOGGLE_TAGS, GridMapLang.TOGGLE_TAGS_TIP, toggles::isTags, () -> toggles.setTags(!toggles.isTags())),
                toggle(GridMapLang.TOGGLE_IDLE, GridMapLang.TOGGLE_IDLE_TIP, toggles::isIdleLines, () -> toggles.setIdleLines(!toggles.isIdleLines())),
                toggle(GridMapLang.TOGGLE_MOTION, GridMapLang.TOGGLE_MOTION_TIP, toggles::isMotion, () -> toggles.setMotion(!toggles.isMotion())),
                toggle(GridMapLang.TOGGLE_LEGEND, GridMapLang.TOGGLE_LEGEND_TIP, toggles::isLegend, root::toggleLegend));
    }

    private static Button toggle(String key, String tooltipKey, BooleanSupplier on, Runnable flip) {
        var text = Component.translatable(key);
        var button = Button.text(UISizes.widthFor(text.getString()) + 2 * UISizes.TEXT_PADDING, UISizes.DOCK_BUTTON_SIZE, text).setOnClientClick(flip)
                .bindClientVariant(() -> on.getAsBoolean() ? UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT);
        button.tooltips(tooltipKey);
        return button;
    }

    private static Button back(GridMapRoot root) {
        var ctx = root.context();
        var text = Component.translatable(GridMapLang.BACK);
        var button = Button.text(UISizes.widthFor(text.getString()) + 2 * UISizes.TEXT_PADDING, UISizes.DOCK_BUTTON_SIZE, text).setOnClientClick(root::sendBack);
        button.disabled(() -> ctx.machine() == null, GridMapLang.FROM_TERMINAL);
        button.tooltips(GridMapLang.BACK_TIP);
        return button;
    }
}

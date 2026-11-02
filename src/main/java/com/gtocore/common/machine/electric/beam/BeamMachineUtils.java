package com.gtocore.common.machine.electric.beam;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.DecimalField;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.PercentField;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uiwidgets.number.NumberSettingPage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.function.Consumer;
import java.util.function.Supplier;

final class BeamMachineUtils {

    private static final double TWO_PI = Math.PI * 2.0D;

    private BeamMachineUtils() {}

    static Vec3 direction(float theta, float phi) {
        double cosPhi = Math.cos(phi);
        return new Vec3(cosPhi * Math.cos(theta), Math.sin(phi), cosPhi * Math.sin(theta)).normalize();
    }

    static float normalizeAngle(double angle) {
        if (!Double.isFinite(angle)) return 0.0F;
        angle %= TWO_PI;
        if (angle > Math.PI) angle -= TWO_PI;
        if (angle < -Math.PI) angle += TWO_PI;
        return (float) angle;
    }

    static float clampPitch(double phi) {
        if (!Double.isFinite(phi)) return 0.0F;
        return (float) Math.clamp(phi, -Math.PI / 2.0D, Math.PI / 2.0D);
    }

    static Vec3 cubeFrameIntersection(Vec3 center, Vec3 direction) {
        double max = Math.max(Math.abs(direction.x), Math.max(Math.abs(direction.y), Math.abs(direction.z)));
        return max <= 1.0E-9D ? center : center.add(direction.scale(0.5D / max));
    }

    static long doubled(long value) {
        if (value <= 0) return 0;
        return value > Long.MAX_VALUE / 2L ? Long.MAX_VALUE : value * 2L;
    }

    static long scaled(long value, double factor) {
        if (value <= 0 || factor <= 0.0D) return 0;
        if (factor >= 1.0D && value > Long.MAX_VALUE / factor) return Long.MAX_VALUE;
        return Math.clamp((long) Math.floor(value * factor), 0L, Long.MAX_VALUE);
    }

    static long energyCost(long intensity) {
        if (intensity <= 0) return 0;
        return intensity > (Long.MAX_VALUE >> 16) ? Long.MAX_VALUE : intensity << 16;
    }

    static Widget addAngleControls(Widget base,
                                   Supplier<Float> theta,
                                   Consumer<Float> setTheta,
                                   Supplier<Float> phi,
                                   Consumer<Float> setPhi) {
        var controls = UIElement.column(NumberSettingPage.COMPACT_WIDTH)
                .layout(l -> l.gapAll(UISizes.SECTION_GAP))
                .addChildren(
                        Form.numberRow(Component.translatable(BeamConfigurator.THETA), angleField(theta, setTheta, -180.0D, 180.0D)),
                        Form.numberRow(Component.translatable(BeamConfigurator.PHI), angleField(phi, setPhi, -90.0D, 90.0D)));
        return UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.SECTION_GAP).alignCenter())
                .addChildren(base, controls);
    }

    static Widget addReflectivityControl(Widget base, Supplier<Float> value, Consumer<Float> setter) {
        var controls = UIElement.column(NumberSettingPage.COMPACT_WIDTH)
                .layout(l -> l.gapAll(UISizes.SECTION_GAP))
                .addChild(Form.numberRow(Component.translatable(BeamConfigurator.REFLECTIVITY), reflectivityField(value, setter)));
        return UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.SECTION_GAP).alignCenter())
                .addChildren(base, controls);
    }

    private static DecimalField angleField(Supplier<Float> getter, Consumer<Float> setter,
                                           double minDegrees, double maxDegrees) {
        return DecimalField.of(NumberSettingPage.COMPACT_WIDTH,
                () -> Math.toDegrees(getter.get()), degrees -> setter.accept((float) Math.toRadians(degrees)),
                minDegrees, maxDegrees, 0.1, 1, 10, 50, 100);
    }

    private static PercentField reflectivityField(Supplier<Float> value, Consumer<Float> setter) {
        return PercentField.of(NumberSettingPage.COMPACT_WIDTH, value::get, v -> setter.accept((float) v),
                0.0D, 1.0D, PercentField.DEFAULT_STEP, 1, 5, 10, 50);
    }
}

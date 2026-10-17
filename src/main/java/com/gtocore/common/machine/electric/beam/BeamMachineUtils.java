package com.gtocore.common.machine.electric.beam;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

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
        var size = base.getSize();
        var group = new WidgetGroup(0, 0, size.width + 96, Math.max(size.height, 82));
        group.addWidget(base);
        int x = size.width + 4;
        group.addWidget(new LabelWidget(x, 8, Component.translatable(BeamConfigurator.THETA)));
        group.addWidget(angleField(x, 22, theta, setTheta, -180.0D, 180.0D));
        group.addWidget(new LabelWidget(x, 45, Component.translatable(BeamConfigurator.PHI)));
        group.addWidget(angleField(x, 59, phi, setPhi, -90.0D, 90.0D));
        return group;
    }

    static Widget addReflectivityControl(Widget base, Supplier<Float> value, Consumer<Float> setter) {
        var size = base.getSize();
        var group = new WidgetGroup(0, 0, size.width + 96, Math.max(size.height, 60));
        group.addWidget(base);
        int x = size.width + 4;
        group.addWidget(new LabelWidget(x, 8, Component.translatable(BeamConfigurator.REFLECTIVITY)));
        group.addWidget(floatField(x, 24, value, setter, 0.0D, 1.0D, 0.05f));
        return group;
    }

    private static TextFieldWidget floatField(int x, int y, Supplier<Float> getter, Consumer<Float> setter,
                                              double min, double max, float wheelDur) {
        return new TextFieldWidget(x, y, 88, 16,
                () -> String.valueOf(getter.get()), value -> {
                    try {
                        setter.accept((float) Math.clamp(Double.parseDouble(value), min, max));
                    } catch (NumberFormatException ignored) {
                        setter.accept(getter.get());
                    }
                }).setBackground(GuiTextures.NUMBER_BACKGROUND).setNumbersOnly((float) min, (float) max).setWheelDur(wheelDur);
    }

    private static TextFieldWidget angleField(int x, int y, Supplier<Float> getter, Consumer<Float> setter,
                                              double minDegrees, double maxDegrees) {
        return floatField(x, y,
                () -> (float) Math.toDegrees(getter.get()),
                degrees -> setter.accept((float) Math.toRadians(degrees)),
                minDegrees, maxDegrees, 5.0f);
    }
}

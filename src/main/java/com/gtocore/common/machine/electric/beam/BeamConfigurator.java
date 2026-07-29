package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.function.Consumer;
import java.util.function.Supplier;

@DataGeneratorScanned
final class BeamConfigurator implements IFancyConfigurator {

    private final Component title;
    private final Widget widget;

    private BeamConfigurator(Component title, Widget widget) {
        this.title = title;
        this.widget = widget;
    }

    static BeamConfigurator angles(Supplier<Float> theta, Consumer<Float> setTheta,
                                   Supplier<Float> phi, Consumer<Float> setPhi) {
        var group = new WidgetGroup(0, 0, 132, 76);
        group.addWidget(new LabelWidget(4, 4, Component.translatable(THETA)));
        group.addWidget(angleField(4, 18, theta, setTheta, -180.0D, 180.0D));
        group.addWidget(new LabelWidget(4, 38, Component.translatable(PHI)));
        group.addWidget(angleField(4, 52, phi, setPhi, -90.0D, 90.0D));
        return new BeamConfigurator(Component.translatable(THETA), group);
    }

    static BeamConfigurator reflectivity(Supplier<Float> value, Consumer<Float> setter) {
        var group = new WidgetGroup(0, 0, 132, 40);
        group.addWidget(new LabelWidget(4, 4, Component.translatable(REFLECTIVITY)));
        group.addWidget(floatField(4, 20, value, setter, 0.0D, 1.0D, 0.05f));
        return new BeamConfigurator(Component.translatable(SEMI_REFLECTOR), group);
    }

    private static TextFieldWidget floatField(int x, int y, Supplier<Float> getter, Consumer<Float> setter,
                                              double min, double max, float wheelDur) {
        return new TextFieldWidget(x, y, 124, 16, () -> String.valueOf(getter.get()), value -> {
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

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public com.lowdragmc.lowdraglib.gui.texture.IGuiTexture getIcon() {
        return GuiTextures.INFO_ICON;
    }

    @Override
    public Widget createConfigurator() {
        return widget;
    }

    @RegisterLanguage(cn = "光束配置器", en = "Ray Beam Configurator")
    public static final String TITLE = "gtocore.machine.ray_beam.configurator";
    @RegisterLanguage(cn = "水平角θ", en = "Theta (°)")
    public static final String THETA = "gtocore.machine.ray_beam.theta";
    @RegisterLanguage(cn = "垂直角φ", en = "Phi (°)")
    public static final String PHI = "gtocore.machine.ray_beam.phi";
    @RegisterLanguage(cn = "光束方向", en = "Ray beam direction")
    public static final String DIRECTION = "gtocore.machine.ray_beam.direction";
    @RegisterLanguage(cn = "反射率", en = "Reflectivity")
    public static final String REFLECTIVITY = "gtocore.machine.ray_beam.reflectivity";
    @RegisterLanguage(cn = "半透镜", en = "Semi-reflector")
    public static final String SEMI_REFLECTOR = "gtocore.machine.ray_beam.semi_reflector";
}

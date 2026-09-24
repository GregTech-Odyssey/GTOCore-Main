package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.DecimalField;
import com.gregtechceu.gtceu.uipro.elements.PercentField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.number.NumberSettingPage;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 光束机器的左侧小组件：光束方向（水平角 θ、垂直角 φ，单位度）或半透镜反射率。
 * <p>
 * 每项"标题一行 + 调节器一行"，单位写在标题后面：角度用小数调节器 {@link DecimalField}（步进 0.1°），反射率是比例，
 * 用百分数调节器 {@link PercentField}（0% ~ 100%，步进 1%）。数值以服务端为准：调节器在服务端按步进取整、夹到上下限再写入。
 * 宽度与其他数值小组件相同（{@link NumberSettingPage#COMPACT_WIDTH}）。
 */
@DataGeneratorScanned
final class BeamConfigurator implements IFancyConfigurator {

    private final Component title;
    private final Supplier<Widget> content;

    private BeamConfigurator(Component title, Supplier<Widget> content) {
        this.title = title;
        this.content = content;
    }

    static BeamConfigurator angles(Supplier<Float> theta, Consumer<Float> setTheta,
                                   Supplier<Float> phi, Consumer<Float> setPhi) {
        return new BeamConfigurator(Component.translatable(DIRECTION), () -> UIElement.column(NumberSettingPage.COMPACT_WIDTH)
                .layout(l -> l.gapAll(UISizes.SECTION_GAP))
                .addChildren(
                        labeled(THETA, angleField(theta, setTheta, -180.0D, 180.0D)),
                        labeled(PHI, angleField(phi, setPhi, -90.0D, 90.0D))));
    }

    static BeamConfigurator reflectivity(Supplier<Float> value, Consumer<Float> setter) {
        return new BeamConfigurator(Component.translatable(SEMI_REFLECTOR), () -> UIElement.column(NumberSettingPage.COMPACT_WIDTH)
                .addChild(labeled(REFLECTIVITY, PercentField.of(NumberSettingPage.COMPACT_WIDTH, value::get, v -> setter.accept((float) v),
                        0.0D, 1.0D, PercentField.DEFAULT_STEP, 1, 5, 10, 50))));
    }

    /** 标题一行、调节器一行。 */
    private static UIElement labeled(String key, UIElement field) {
        return UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(TextLine.translatable(LayoutStyle.AUTO, key).setColor(UITheme.TEXT), field);
    }

    /** 角度（小数调节器，步进 0.1°；四档 0.1° / 1° / 5° / 10°，单位写在标题里）：界面上是度，机器里存弧度。 */
    private static DecimalField angleField(Supplier<Float> getter, Consumer<Float> setter, double minDegrees, double maxDegrees) {
        return DecimalField.of(NumberSettingPage.COMPACT_WIDTH,
                () -> Math.toDegrees(getter.get()), degrees -> setter.accept((float) Math.toRadians(degrees)),
                minDegrees, maxDegrees, 0.1, 1, 10, 50, 100);
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IGuiTexture getIcon() {
        return WidgetIcons.INFO;
    }

    /** 每次展开都新建一份内容（两端各建各的，控件树相同）。 */
    @Override
    public Widget createConfigurator() {
        return content.get();
    }

    @RegisterLanguage(cn = "光束配置器", en = "Ray Beam Configurator")
    public static final String TITLE = "gtocore.machine.ray_beam.configurator";
    @RegisterLanguage(cn = "水平角θ（°）", en = "Theta (°)")
    public static final String THETA = "gtocore.machine.ray_beam.theta";
    @RegisterLanguage(cn = "垂直角φ（°）", en = "Phi (°)")
    public static final String PHI = "gtocore.machine.ray_beam.phi";
    @RegisterLanguage(cn = "光束方向", en = "Ray beam direction")
    public static final String DIRECTION = "gtocore.machine.ray_beam.direction";
    @RegisterLanguage(cn = "反射率（%%）", en = "Reflectivity (%%)")
    public static final String REFLECTIVITY = "gtocore.machine.ray_beam.reflectivity";
    @RegisterLanguage(cn = "半透镜", en = "Semi-reflector")
    public static final String SEMI_REFLECTOR = "gtocore.machine.ray_beam.semi_reflector";
}

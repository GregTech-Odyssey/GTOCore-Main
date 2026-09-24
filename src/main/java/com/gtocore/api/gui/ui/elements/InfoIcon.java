package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

/**
 * 信息图标：与按钮、输入框同高（{@link #SIZE} 见方）的圆形图标，悬停显示说明，放在需要额外解释的控件旁边。
 * 四种模式（{@link Kind}）：说明（蓝 i）、警告（黄 !）、错误（红 ×）、成功（绿 ✓）。
 * 说明是标题栏机器说明图标的同款；需要"悬停看说明"的标记一律用本组件，不要用按钮或文字自己拼。
 * 状态会随运行变化时用 {@link Indicator}，本组件的模式在构建时确定。
 */
public class InfoIcon extends UIElement {

    public static final int SIZE = UISizes.CONTROL_HEIGHT;

    public enum Kind {

        INFO(0),
        WARNING(1),
        ERROR(2),
        SUCCESS(3);

        private final int index;

        Kind(int index) {
            this.index = index;
        }

        public IGuiTexture texture() {
            return UITheme.infoIcon(index);
        }
    }

    private final Kind kind;

    public InfoIcon(Kind kind, Component... tooltips) {
        this.kind = kind;
        layout(l -> l.size(SIZE, SIZE));
        setHoverTooltips(tooltips);
    }

    public static InfoIcon info(String... translationKeys) {
        return new InfoIcon(Kind.INFO, translate(translationKeys));
    }

    public static InfoIcon warning(String... translationKeys) {
        return new InfoIcon(Kind.WARNING, translate(translationKeys));
    }

    public static InfoIcon error(String... translationKeys) {
        return new InfoIcon(Kind.ERROR, translate(translationKeys));
    }

    public static InfoIcon success(String... translationKeys) {
        return new InfoIcon(Kind.SUCCESS, translate(translationKeys));
    }

    public Kind getKind() {
        return kind;
    }

    private static Component[] translate(String[] translationKeys) {
        var tooltips = new Component[translationKeys.length];
        for (int i = 0; i < translationKeys.length; i++) tooltips[i] = Component.translatable(translationKeys[i]);
        return tooltips;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        kind.texture().draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), SIZE, SIZE);
    }
}

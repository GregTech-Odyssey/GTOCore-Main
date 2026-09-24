package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 与服务端字符串双向绑定的输入框，对应 LDLib2 {@code TextField.bind(DataBindingBuilder.string(getter, setter))}。
 * <p>
 * 外观与物品槽同为内凹框、白字，获得焦点时内圈加亮。标准高 {@link #HEIGHT}；
 * 宽度可固定，也可 {@code layout(l -> l.flexGrow(1))} 在横向行里吃满剩余宽度。
 * 同步走 LDLib1 {@code TextFieldWidget} 自带机制：服务端 getter 变化时下发，客户端输入后上行调用 setter。
 */
public class TextField extends UIElement {

    public static final int HEIGHT = UISizes.CONTROL_HEIGHT;

    private final Input input;

    public TextField(int width, Supplier<String> getter, Consumer<String> setter) {
        this.input = new Input(width, HEIGHT, getter, setter);
        addChild(input);
        layout(l -> l.column().size(width, HEIGHT));
    }

    /** 右键清空输入框（清空后同样上行给 setter）。 */
    public TextField setRightClickClear(boolean rightClickClear) {
        input.rightClickClear = rightClickClear;
        return this;
    }

    /** 输入为空且未获得焦点时显示的灰色提示文字（客户端取值）。 */
    public TextField setPlaceholder(Supplier<Component> placeholder) {
        input.placeholder = placeholder;
        return this;
    }

    public TextFieldWidget getInput() {
        return input;
    }

    /** 提示挂在内部输入框上：LDLib1 只显示鼠标下最内层控件的提示。 */
    @Override
    public Widget setHoverTooltips(Component... tooltipText) {
        input.setHoverTooltips(tooltipText);
        return this;
    }

    @Override
    protected void onSizeUpdate() {
        super.onSizeUpdate();
        if (input != null) input.setSize(new Size(getSizeWidth(), getSizeHeight()));
    }

    private static final class Input extends TextFieldWidget {

        private boolean rightClickClear;
        @Nullable
        private Supplier<Component> placeholder;

        private Input(int width, int height, Supplier<String> getter, Consumer<String> setter) {
            super(0, 0, width, height, getter, setter);
            setBordered(false);
            setTextColor(UITheme.FIELD_TEXT);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (rightClickClear && button == 1 && isMouseOverElement(mouseX, mouseY)) {
                textField.setValue("");
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            UITheme.drawInset(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), isFocus());
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            if (placeholder != null && !isFocus() && getCurrentString().isEmpty()) {
                var font = Minecraft.getInstance().font;
                var text = UITheme.clip(font, placeholder.get().getString(), getSizeWidth() - 4);
                graphics.drawString(font, text, getPositionX() + 2, getPositionY() + (getSizeHeight() - 8) / 2, UITheme.PLACEHOLDER_TEXT, false);
            }
        }
    }
}

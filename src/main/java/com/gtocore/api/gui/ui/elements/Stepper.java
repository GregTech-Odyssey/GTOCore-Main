package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.data.SyncValue;
import com.gtocore.api.gui.ui.data.SyncValueHost;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * 步进器 {@code [<] 值 [>]}：左右按钮或在数值上滚动鼠标滚轮来改变整数，适合电路编号、页码这类小范围选择。
 * <p>
 * 标准高 {@link #HEIGHT}，总宽 {@link #width(int)}（两个方形箭头 + 中间数值框 + 间距）。
 * 数值以服务端为准：点击/滚轮都在服务端计算新值后写入 {@code setter}，再经同步回显。
 * {@code wrap} 为真时越过边界回绕（32 的下一个是 0），否则停在边界并把箭头置灰。
 */
public class Stepper extends UIElement {

    public static final int HEIGHT = UISizes.CONTROL_HEIGHT;

    private static final int WHEEL_ID = SyncValueHost.ID_BASE - 2;

    private final IntSupplier getter;
    private final IntConsumer setter;
    private final int min;
    private final int max;
    private final boolean wrap;
    private final IntFunction<String> formatter;
    private final SyncValue<Integer> value;

    public Stepper(int valueWidth, IntSupplier getter, IntConsumer setter, int min, int max, boolean wrap, IntFunction<String> formatter) {
        this.getter = getter;
        this.setter = setter;
        this.min = min;
        this.max = max;
        this.wrap = wrap;
        this.formatter = formatter;
        this.value = addSyncValue(SyncValue.ofInt(getter::getAsInt, getter.getAsInt()));
        layout(l -> l.row().height(HEIGHT).gapAll(UISizes.GAP).alignCenter());
        addChildren(
                Button.glyph("<").setOnServerClick(() -> step(-1)).setEnabled(() -> wrap || value.getValue() > min),
                new ValueBox(valueWidth),
                Button.glyph(">").setOnServerClick(() -> step(1)).setEnabled(() -> wrap || value.getValue() < max));
    }

    /** 中间数值框宽 {@code valueWidth} 时的总宽度。 */
    public static int width(int valueWidth) {
        return 2 * UISizes.ICON_BUTTON + 2 * UISizes.GAP + valueWidth;
    }

    private void step(int delta) {
        int next = getter.getAsInt() + delta;
        if (wrap) next = Math.floorMod(next - min, max - min + 1) + min;
        else next = Math.clamp(next, min, max);
        setter.accept(next);
    }

    private final class ValueBox extends Widget {

        private ValueBox(int width) {
            super(0, 0, width, HEIGHT);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
            if (!isMouseOverElement(mouseX, mouseY) || wheelDelta == 0) return false;
            int delta = wheelDelta > 0 ? 1 : -1;
            writeClientAction(WHEEL_ID, buf -> buf.writeVarInt(delta));
            return true;
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id == WHEEL_ID) step(buffer.readVarInt() > 0 ? 1 : -1);
            else super.handleClientAction(id, buffer);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX(), y = getPositionY(), w = getSizeWidth(), h = getSizeHeight();
            UITheme.drawInset(graphics, x, y, w, h, false);
            UITheme.drawCenteredText(graphics, formatter.apply(value.getValue()), x + w / 2, y + (h - 8) / 2, w - 4, UITheme.FIELD_TEXT, false);
        }
    }
}

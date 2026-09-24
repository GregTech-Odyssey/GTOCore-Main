package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.data.SyncValue;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Supplier;

/**
 * 单行文字：宽度固定、高度固定为 {@link #HEIGHT}，放不下时截断并加省略号（鼠标停上去显示全文）。
 * <p>
 * 与 {@link Label} 的区别：{@code Label} 在客户端按文字实际宽高重排自身尺寸（空串只有 2 像素高、长文字会换行），
 * 而窗口尺寸只在建页时算一次，页面建好后才下发的文字会让布局溢出。{@code TextLine} 的尺寸在构造时就确定、
 * 与文字内容无关，适合显示名称、状态这类由服务端下发、长度不定的单行文字。
 * <p>
 * 高度取 {@link UISizes#TEXT_HEIGHT}（字体行高 9，与单行 {@code Label} 同高），这样它能和 {@code Label} 在区块里混排、行距一致；
 * 放进 {@link UISizes#CONTROL_HEIGHT} 高的控件行时用 {@code alignCenter()} 居中。
 * <p>
 * 文字由服务端取值下发（{@link SyncValue}）；构造时<strong>不会</strong>调用 getter，客户端显示下发前为空，
 * 所以 getter 可以放心依赖服务端独有的数据。两端文字相同的静态文字用 {@link #constant}。
 */
public class TextLine extends UIElement {

    /** 标准高度：单行文字高度。 */
    public static final int HEIGHT = UISizes.TEXT_HEIGHT;

    private final SyncValue<Component> text;
    private int color = UITheme.TEXT;

    public TextLine(int width, Supplier<Component> text, Component initial) {
        layout(l -> l.size(width, HEIGHT));
        this.text = addSyncValue(SyncValue.of(text, SyncValue.COMPONENT, initial));
    }

    /** 服务端取值下发的文字。 */
    public static TextLine of(int width, Supplier<Component> text) {
        return new TextLine(width, text, Component.empty());
    }

    /** 两端相同的固定文字（客户端一开始就显示，不等下发）。 */
    public static TextLine constant(int width, Component text) {
        return new TextLine(width, () -> text, text);
    }

    public static TextLine translatable(int width, String key) {
        return constant(width, Component.translatable(key));
    }

    /** 文字颜色，取 {@link UITheme} 里的颜色。 */
    public TextLine setColor(int color) {
        this.color = color;
        return this;
    }

    /** 最近一次同步到的文字。 */
    public Component getText() {
        return text.getValue();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        var font = Minecraft.getInstance().font;
        var clipped = UITheme.clip(font, text.getValue().getString(), getSizeWidth());
        graphics.drawString(font, clipped, getPositionX(), getPositionY() + (HEIGHT - 8) / 2, color, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        // 自带提示优先；没有提示且文字被截断时，悬停显示全文
        if (!tooltipTexts.isEmpty() || gui == null || gui.getModularUIGui() == null || !isMouseOverElement(mouseX, mouseY)) return;
        var font = Minecraft.getInstance().font;
        var full = text.getValue();
        if (font.width(full.getString()) > getSizeWidth()) {
            gui.getModularUIGui().setHoverTooltip(List.of(full), ItemStack.EMPTY, null, null);
        }
    }
}

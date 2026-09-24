package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.data.SyncValue;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;
import com.gtocore.api.gui.ui.utils.TextLayout;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 自动换行的文字块，文字由服务端下发（对应 LDLib2 {@code Label.bindDataSource(componentS2C)}）。
 * <p>
 * 默认原版容器正文色（深灰、无阴影）；单行高 {@link #HEIGHT}。
 * 客户端按文字实际宽高调整自身尺寸，最宽不超过 {@code maxWidth}；服务端尺寸不参与排版，保持初始值。
 */
public class Label extends UIElement {

    private static final int PADDING_Y = 1;
    private static final int LINE_GAP = 1;
    /** 单行文字的高度。 */
    public static final int HEIGHT = UISizes.TEXT_HEIGHT;
    /** 单行标签占的总高度（文字 + 上下各 1 像素留白）。 */
    public static final int LINE_HEIGHT = HEIGHT + 2 * PADDING_Y;

    private static final int DEFAULT_COLOR = UITheme.TEXT;

    private final SyncValue<Component> text;
    private final int maxWidth;
    private int color = DEFAULT_COLOR;
    @Nullable
    private Component laidOutText;
    @Nullable
    private Object block;

    public Label(Supplier<Component> text, int maxWidth) {
        this.maxWidth = maxWidth;
        this.text = addSyncValue(SyncValue.ofComponent(text));
        setSize(new Size(100, HEIGHT));
    }

    public static Label of(Supplier<Component> text, int maxWidth) {
        return new Label(text, maxWidth);
    }

    public static Label translatable(String key, int maxWidth) {
        var component = Component.translatable(key);
        return new Label(() -> component, maxWidth);
    }

    public Label setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void recomputeLayout() {
        // 尺寸由文字决定，不参与子元素排布
    }

    @Override
    public void initWidget() {
        super.initWidget();
        if (isRemote()) relayout();
    }

    @OnlyIn(Dist.CLIENT)
    private TextLayout.Block relayout() {
        var current = text.getValue();
        var laid = TextLayout.layout(current.getString(), maxWidth, LINE_GAP);
        laidOutText = current;
        block = laid;
        setSize(new Size(laid.width(), laid.height() + 2 * PADDING_Y));
        return laid;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        var laid = text.getValue() == laidOutText && block instanceof TextLayout.Block b ? b : relayout();
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(getPositionX(), getPositionY() + PADDING_Y, 0);
        TextLayout.draw(graphics, laid, LINE_GAP, color);
        pose.popPose();
    }
}

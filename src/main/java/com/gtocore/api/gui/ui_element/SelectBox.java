package com.gtocore.api.gui.ui_element;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.PageOverlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;

import dev.vfyjxf.taffy.style.TaffyPosition;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * 下拉选择栏：一个显示当前选项的按钮，点开后紧贴它下方浮出选项列表。
 *
 * <p>
 * 选项列表放在 {@link PageOverlay} 里，由窗口最后绘制、盖住处先拿到鼠标，因此可以压在下面的表格上。
 * 浮层自带一个与按钮同宽的标题按钮（盖住原按钮），点它或点浮层外面都收起；点选项时先在本端收起，
 * 再走 RPC 让服务端改选。
 *
 * <p>
 * 选项列表与当前选中项都在两端按同一份翻译键建出来，控件树与选择结果无关；改选请求挂在选择栏自己身上
 * ——浮层在服务端始终是隐藏的，挂在选项按钮上的请求会被激活链检查丢掉。
 */
public final class SelectBox extends UIElement {

    /**
     * @param width    按钮宽度，浮层与选项也按它排版
     * @param title    悬停说明（如「难度」）
     * @param options  各选项名字，下标就是选项序号
     * @param selected 当前选中项的序号，越界会被夹到范围内
     * @param select   服务端改选回调；收到的是选项序号
     */
    public SelectBox(int width, Component title, Component[] options, int selected, IntConsumer select) {
        this(width, title, options, () -> selected, select);
    }

    /** Keeps the displayed selection in sync when the server changes it without rebuilding the option list. */
    public SelectBox(int width, Component title, Component[] options, IntSupplier selected, IntConsumer select) {
        layout(l -> l.size(width, Button.HEIGHT));

        // 浮层先建出来：标题与选项的点击都要引用它，控件内容随后再填进去
        var dropdown = new Dropdown(width);
        var request = addRPC(ByteBufCodecs.INT, (player, index) -> select.accept(index));

        var header = Button.of(width).bindClientText(() -> options[Math.clamp(selected.getAsInt(), 0, options.length - 1)].getString());
        header.tooltips(title);
        header.setOnClientClick(() -> dropdown.setDisplay(true));

        var copy = Button.of(width).bindClientText(() -> options[Math.clamp(selected.getAsInt(), 0, options.length - 1)].getString());
        copy.setOnClientClick(() -> dropdown.setDisplay(false));

        var optionSection = UIElement.section(width).layout(l -> l.gapAll(UISizes.GAP));
        int optionWidth = width - 2 * UISizes.PANEL_PADDING;
        for (int index = 0; index < options.length; index++) {
            final int option = index;
            var button = Button.text(optionWidth, options[index]);
            button.bindClientVariant(() -> option == selected.getAsInt() ?
                    UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT);
            button.setOnClientClick(() -> {
                request.send(option);
                dropdown.setDisplay(false);
            });
            optionSection.addChild(button);
        }

        dropdown.fill(copy, optionSection);
        addChildren(header, dropdown);
    }

    /** 浮层：标题按钮 + 选项列表，绝对定位在原按钮的位置（与它同宽同高），显示时盖住原按钮。 */
    private static final class Dropdown extends PageOverlay {

        private final int width;
        @Nullable
        private Button header;
        @Nullable
        private UIElement options;

        private Dropdown(int width) {
            this.width = width;
            layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(0).top(0).column()
                    .width(width).gapAll(UISizes.GAP));
            setDisplay(false);
        }

        /** 填内容：两端各建一次同样的控件树，填入后仍是收起状态。 */
        private void fill(Button header, UIElement options) {
            this.header = header;
            this.options = options;
            addChildren(header, options);
            setDisplay(false);
        }

        /** 标题按钮与选项列表都算浮层本身，落在它们上面的点击不算「点在外面」。 */
        @Override
        public boolean isMouseOverElement(double mouseX, double mouseY) {
            return header != null && header.isMouseOverElement(mouseX, mouseY) ||
                    options != null && options.isMouseOverElement(mouseX, mouseY);
        }

        @Override
        protected void onOutsideClick() {
            setDisplay(false);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void drawOverlayBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            // 标题按钮与选项区块各自画底图，浮层本身不再画一层。
        }
    }
}

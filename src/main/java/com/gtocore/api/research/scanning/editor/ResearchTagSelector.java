package com.gtocore.api.research.scanning.editor;

import com.gtocore.api.research.ResearchTag;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.Binding;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.VirtualList;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.PageOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import dev.vfyjxf.taffy.style.TaffyPosition;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** 用 uipro 页内浮层承载研究领域下拉列表，选中值通过 Binding 由服务端确认。 */
final class ResearchTagSelector extends UIElement {

    private static final int WIDTH = 3 * UISizes.BUTTON_WIDTH;

    private final Binding<Integer> selected;
    private final Options options;

    ResearchTagSelector(ResearchTag[] tags, Supplier<ResearchTag> getter, Consumer<ResearchTag> setter) {
        layout(l -> l.column().width(WIDTH).height(UISizes.CONTROL_HEIGHT));
        selected = addBinding(Binding.bindInt(() -> ResearchTag.TAGS.getId(getter.get()),
                index -> setter.accept(tags[index]), 0, tags.length - 1));
        var labels = new String[tags.length];
        var titles = new String[tags.length];
        for (int i = 0; i < tags.length; i++) {
            labels[i] = tags[i].getDisplayName().getString();
            titles[i] = labels[i] + " ▾";
        }
        options = new Options(labels);
        addChild(Button.of(LayoutStyle.AUTO).bindClientText(() -> titles[selected.getValue()])
                .setOnClientClick(() -> options.setDisplay(!options.isVisible())));
        addChild(options);
    }

    private final class Options extends PageOverlay {

        private Options(String[] labels) {
            layout(l -> l.column().positionType(TaffyPosition.ABSOLUTE).left(0).top(UISizes.CONTROL_HEIGHT + UISizes.GAP)
                    .width(WIDTH).paddingAll(UISizes.GAP));
            var scroller = new ScrollerView("scanning.editor.tags", WIDTH - 2 * UISizes.GAP,
                    Math.min(8, labels.length) * UISizes.CONTROL_HEIGHT).setResizable(false);
            scroller.addScrollViewChild(new VirtualList(1, UISizes.CONTROL_HEIGHT, 0, () -> labels.length,
                    index -> Button.of(LayoutStyle.AUTO).bindClientText(() -> labels[index])
                            .bindClientVariant(() -> selected.getValue() == index ? UITheme.ButtonVariant.CONFIRM : UITheme.ButtonVariant.DEFAULT)
                            .setOnClientClick(() -> {
                                selected.set(index);
                                setDisplay(false);
                            })));
            addChild(scroller);
            setDisplay(false);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void onOutsideClick() {
            // 点击选择按钮时交给它切换开关，避免先关闭浮层后又立刻打开。
            var minecraft = Minecraft.getInstance();
            var window = minecraft.getWindow();
            double x = minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
            double y = minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
            if (ResearchTagSelector.this.isMouseOverElement(x, y)) return;
            setDisplay(false);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void drawOverlayBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            UITheme.PANEL.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
    }
}

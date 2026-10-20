package com.gtocore.integration.emi.planner;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.elements.VirtualList;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.planner.PlannerText;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.vfyjxf.taffy.style.FlexWrap;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

final class PlannerWidgets {

    static final int ROW = UISizes.SLOT_SIZE;
    static final int ENTRY_HEIGHT = 2 * ROW + 2 * UISizes.PANEL_PADDING + UISizes.GAP;

    private PlannerWidgets() {}

    static Component text(String key, String english) {
        return Component.literal(PlannerText.tr(key, english));
    }

    static TextLine label(Component text) {
        return TextLine.constant(LayoutStyle.AUTO, text).bindClientColor(UITheme::panelText);
    }

    static TextLine label(String text) {
        return label(Component.literal(text));
    }

    static Button button(Component text, Runnable action) {
        return Button.text(Math.max(UISizes.BUTTON_WIDTH, UISizes.widthFor(text.getString()) + 2 * UISizes.TEXT_PADDING), text)
                .setOnClientClick(action);
    }

    static Button button(String key, String english, Runnable action) {
        return button(text(key, english), action);
    }

    static UIElement wrap() {
        return UIElement.row(LayoutStyle.AUTO).layout(l -> l.flexWrap(FlexWrap.WRAP).gapAll(UISizes.GAP).alignCenter());
    }

    static UIElement section() {
        return UIElement.section().layout(l -> l.gapAll(UISizes.GAP));
    }

    static ScrollerView list(String id, int rowHeight, IntSupplier count, IntFunction<Widget> factory) {
        return list(id, new VirtualList(1, rowHeight, UISizes.GAP, count, factory));
    }

    static ScrollerView list(String id, VirtualList list) {
        var scroll = ScrollerView.page("planner." + id, LayoutStyle.AUTO).addScrollViewChild(list);
        scroll.getLayoutStyle().height(0).flex(1);
        return scroll;
    }

    static UIElement titled(String key, String english, Widget content) {
        return section().addChildren(label(text(key, english)), content);
    }

    static StackIcon icon(EmiIngredient stack, IntConsumer click) {
        return new StackIcon(stack, click);
    }

    static EmiStack firstOutput(EmiRecipe recipe) {
        if (recipe != null) {
            for (var stack : recipe.getOutputs()) if (stack != null && !stack.isEmpty()) return stack;
        }
        return EmiStack.EMPTY;
    }

    static final class StackIcon extends UIElement {

        private final EmiIngredient stack;
        private final IntConsumer click;

        StackIcon(EmiIngredient stack, IntConsumer click) {
            this.stack = stack == null ? EmiStack.EMPTY : stack;
            this.click = click;
            layout(l -> l.size(ROW, ROW));
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            UITheme.ITEM_SLOT.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), ROW, ROW);
            stack.render(graphics, getPositionX() + 1, getPositionY() + 1, partialTicks);
        }

        @Override
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (isPointerOver(mouseX, mouseY)) {
                EmiRenderHelper.drawTooltip(Minecraft.getInstance().screen, EmiDrawContext.wrap(graphics), stack.getTooltip(), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isPointerOver(mouseX, mouseY)) return false;
            click.accept(button);
            return true;
        }

        @Override
        public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
            return isPointerOver(mouseX, mouseY) ? stack : null;
        }
    }
}

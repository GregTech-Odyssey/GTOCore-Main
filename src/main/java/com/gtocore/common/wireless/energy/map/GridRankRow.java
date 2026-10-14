package com.gtocore.common.wireless.energy.map;

import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

final class GridRankRow extends UIElement {

    private final IntSupplier target;
    private final IntConsumer action;

    GridRankRow(int valueWidth, Supplier<Component> name, Supplier<Component> value, Supplier<Level> level, IntSupplier target, IntConsumer action,
                String tooltipKey) {
        this.target = target;
        this.action = action;
        layout(l -> l.row().height(StatusLine.HEIGHT).gapAll(UISizes.GAP).alignCenter());
        var nameText = TextLine.of(0, name).bindClientColor(UITheme::panelText);
        var valueText = TextLine.of(valueWidth, value).setTextAlign(Horizontal.RIGHT).bindLevel(level);
        addChildren(nameText.layout(l -> l.flex(1)), valueText);
        tooltips(tooltipKey);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (target.getAsInt() >= 0 && isPointerOver(mouseX, mouseY)) {
            UIDraw.hoverOverlay(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int index = target.getAsInt();
        if (button != 0 || index < 0 || !isPointerOver(mouseX, mouseY)) return false;
        action.accept(index);
        playButtonClickSound();
        return true;
    }
}

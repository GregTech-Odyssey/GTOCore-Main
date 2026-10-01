package com.gtocore.common.machine.tesseract;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.animation.UIClock;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIPixels;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;

public final class DistributionStrip extends UIElement {

    private static final int BARS = 4;
    private static final String[] LABELS = { "1", "2", "3", "4" };
    private static final int HEIGHT = UISizes.PROGRESS_BAR_HEIGHT;
    private static final long CYCLE_MS = 4000;
    private static final float FILL_SHARE = 0.8F;
    private static final float SEQUENTIAL_TOTAL = 2.6F;
    private static final float ROUND_ROBIN_TOTAL = 0.65F;

    private final SyncValue<Boolean> roundRobin;

    public DistributionStrip(BooleanSupplier roundRobin) {
        layout(l -> l.height(HEIGHT));
        this.roundRobin = addSyncValue(SyncValue.ofBool(roundRobin));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX(), y = getPositionY(), width = getSizeWidth();
        int barWidth = (width - (BARS - 1) * UISizes.GAP) / BARS;
        float phase = Math.min(1F, (UIClock.millis() % CYCLE_MS) / (CYCLE_MS * FILL_SHARE));
        boolean even = roundRobin.getValue();
        var font = Minecraft.getInstance().font;
        for (int i = 0; i < BARS; i++) {
            int bx = x + i * (barWidth + UISizes.GAP);
            float ratio = even ? phase * ROUND_ROBIN_TOTAL : Math.max(0F, Math.min(1F, phase * SEQUENTIAL_TOTAL - i));
            UIDraw.progressTrack(graphics, bx, y, barWidth, HEIGHT);
            UIDraw.progressFill(graphics, bx, y, barWidth, HEIGHT, 0, ratio, UITheme.FLOW_GREEN_MID);
            var label = LABELS[i];
            UIText.drawLeft(graphics, label, UIPixels.center(bx, barWidth, font.width(label)), UIText.centerY(y, HEIGHT), UITheme.TEXT);
        }
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }
}

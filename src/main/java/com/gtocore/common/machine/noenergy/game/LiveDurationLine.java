package com.gtocore.common.machine.noenergy.game;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.LongSupplier;

/**
 * 状态面板里的一行「名称 …… 用时」。
 *
 * <p>
 * 数值不走 {@code SyncValue}：服务端只在开局时同步开始刻，客户端每帧用本地 {@code ClientLevel#getGameTime()}
 * 算已过去的刻数，不必每秒发包，也不会因快照不更新而停住。已完成的局由调用方返回最终用时。
 */
public final class LiveDurationLine extends UIElement {

    private final Component label;
    private final LongSupplier elapsedTicks;

    public LiveDurationLine(String labelKey, LongSupplier elapsedTicks) {
        this.label = Component.translatable(labelKey);
        this.elapsedTicks = elapsedTicks;
        layout(style -> style.size(LayoutStyle.AUTO, StatusLine.HEIGHT));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        int x = getPositionX();
        int y = UIText.centerY(getPositionY(), getSizeHeight());
        UIText.drawLeft(graphics, label.getString(), x, y, UITheme.TEXT_SECONDARY);
        UIText.drawRight(graphics, AbstractGameMachine.duration(elapsedTicks.getAsLong()).getString(),
                x + getSizeWidth(), y, UITheme.TEXT);
    }
}

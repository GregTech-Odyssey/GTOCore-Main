package com.gtocore.client;

import com.gtocore.common.quest.hot.QuestHotProgress;

import com.gtolib.quest.client.book.QuestRefreshBar;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class QuestReloadScreen extends Screen {

    @Nullable
    private final Screen previous;

    QuestReloadScreen(@Nullable Screen previous) {
        super(Component.empty());
        this.previous = previous;
    }

    @Override
    public void tick() {
        if (!QuestHotProgress.blocking() && minecraft != null && minecraft.screen == this) minecraft.setScreen(previous);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (previous != null) previous.render(graphics, -1, -1, partialTick);
        QuestRefreshBar.draw(graphics, width, height);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

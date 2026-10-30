package com.gtocore.client.website;

import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uipro.window.PopupCard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.IntPredicate;

/**
 * 官网相关的纯客户端弹窗外壳（联网授权、官网登录、/bug 上传进度、物品攻略选择）：屏幕中央一张卡片，关闭时回到打开前的界面。
 */
@OnlyIn(Dist.CLIENT)
public final class WebsitePopupScreen extends ModularUIGuiContainer {

    private static final int MARGIN = 8;

    private final Host host;
    @Nullable
    private final Screen previous;
    @Nullable
    private final IntPredicate keys;

    private WebsitePopupScreen(Host host, ModularUI ui, @Nullable Screen previous, @Nullable IntPredicate keys) {
        super(ui, -1);
        this.host = host;
        this.previous = previous;
        this.keys = keys;
        ui.initWidgets();
    }

    public static void open(String id, @Nullable Screen previous, Function<Runnable, Popup> popup) {
        open(id, previous, popup, null);
    }

    public static void open(String id, @Nullable Screen previous, Function<Runnable, Popup> popup, @Nullable IntPredicate keys) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        var window = minecraft.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();
        var screen = new WebsitePopupScreen[1];
        Runnable close = () -> {
            if (minecraft.screen == screen[0]) minecraft.setScreen(previous);
        };
        var host = new Host(id, popup.apply(close), width, height, close);
        var ui = new ModularUI(width, height, IUIHolder.EMPTY, minecraft.player).widget(host);
        screen[0] = new WebsitePopupScreen(host, ui, previous, keys);
        minecraft.setScreen(screen[0]);
    }

    public static boolean isOpen(String id) {
        return Minecraft.getInstance().screen instanceof WebsitePopupScreen screen && screen.host.id.equals(id);
    }

    @Override
    public void init() {
        modularUI.setSize(width, height);
        host.resize(width, height);
        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keys != null && keys.test(keyCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.screen == this) minecraft.setScreen(previous);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class Host extends UIElement implements ILocalUI {

        private final String id;
        private final PopupCard card;

        private Host(String id, Popup popup, int width, int height, Runnable close) {
            this.id = id;
            setClientSideWidget();
            layout(l -> l.size(width, height).column().alignCenter().justifyContent(AlignContent.CENTER));
            card = new PopupCard(id, popup, height - 2 * MARGIN, close).setResizable(false);
            addChild(card);
        }

        private void resize(int width, int height) {
            layout(l -> l.size(width, height));
            card.setMaxHeight(height - 2 * MARGIN);
        }
    }
}

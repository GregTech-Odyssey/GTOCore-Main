package com.gtocore.api.gui.ui.window;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.styletemplate.UISizes;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 弹出面板的内容：标题 + 内容构建器。由 {@link MachineWindow#registerPopup} 注册的工厂按参数创建，两端各建一次。
 * <p>
 * 面板外观固定：Ore 窗口外框，第一行标题与 {@code [×]}，下面是宽 {@link UISizes#POPUP_CONTENT_WIDTH} 的纵向内容，
 * 高度随内容增长，超过主窗口高度时滚动。{@code content} 往传入的纵向元素里加子元素即可（通常是若干
 * {@link UIElement#section} 区块）。
 *
 * @param title   标题，服务端取值后下发
 * @param content 内容构建器
 */
public record Popup(Supplier<Component> title, Consumer<UIElement> content) {

    public static Popup of(Supplier<Component> title, Consumer<UIElement> content) {
        return new Popup(title, content);
    }
}

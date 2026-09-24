package com.gtocore.integration.ae.wireless;

import com.gregtechceu.gtceu.uipro.UIElement;

/**
 * 原位切换的页面容器：同一时间只显示一个子元素，其余 {@code display: none}（不绘制、不占位置），
 * 容器尺寸随当前页变化（高度跟随显示的那一页，不再取所有页中最高的）。
 * <p>
 * 切换只改显示状态，不增删控件，两端控件树始终一致，客户端操作的下标路由不受影响。
 * 切换属于界面状态：由按钮的 {@code setOnClick}（两端各执行一次）调用 {@link #select}，不放在机器上。
 */
final class WirelessSwitch extends UIElement {

    WirelessSwitch(UIElement... pages) {
        addChildren(pages);
        select(0);
    }

    void select(int index) {
        for (int i = 0; i < widgets.size(); i++) {
            ((UIElement) widgets.get(i)).setDisplay(i == index);
        }
    }
}

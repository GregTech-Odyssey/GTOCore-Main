package com.gtocore.api.gui.ui;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

/**
 * Shift+点击快速转移物品时的目标优先级。LDLib 原本按槽位注册顺序挑目标槽，后打开的弹出面板里的槽总排在最后；
 * 实现本接口的控件（通常是一个容器，如弹出面板）为它下面所有槽位声明优先级，数值大的先放，默认 0。
 * 一个槽的优先级取控件树里离它最近的实现者（见 {@link #of}），由 {@code ModularUIContainerMixin} 在排序时使用。
 */
public interface IShiftClickPriority {

    /** 普通槽位。 */
    int DEFAULT = 0;
    /** 打开着的弹出面板：玩家正在操作的窗口，优先接收。 */
    int POPUP = 100;

    int getShiftClickPriority();

    /** 从槽位往上找最近的声明者，没有则为 {@link #DEFAULT}。 */
    static int of(Widget widget) {
        for (Widget current = widget; current != null; current = current.getParent()) {
            if (current instanceof IShiftClickPriority priority) return priority.getShiftClickPriority();
        }
        return DEFAULT;
    }
}

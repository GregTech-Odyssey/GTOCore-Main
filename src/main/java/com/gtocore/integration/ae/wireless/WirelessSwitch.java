package com.gtocore.integration.ae.wireless;

import com.gtocore.api.gui.ui.UIElement;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

/**
 * 叠放容器：所有子元素都摆在 (0, 0)，同一时间只显示其中一个。
 * <p>
 * 切换只改可见性（{@code setVisible} + {@code setActive}），不增删控件，两端控件树始终一致，客户端操作的下标路由不受影响。
 * 切换属于界面状态：由按钮的 {@code setOnClick}（两端各执行一次）调用 {@link #select}，不放在机器上。
 * 尺寸是所有子元素外接矩形，构造时就定下（子元素自身尺寸固定），不会因切换改变。
 */
final class WirelessSwitch extends UIElement {

    private boolean placing;

    WirelessSwitch(Widget... children) {
        addChildren(children);
        select(0);
    }

    void select(int index) {
        for (int i = 0; i < widgets.size(); i++) {
            var widget = widgets.get(i);
            widget.setVisible(i == index);
            widget.setActive(i == index);
        }
    }

    @Override
    protected void recomputeLayout() {
        if (placing) return;
        placing = true;
        try {
            int width = 0, height = 0;
            for (var widget : widgets) {
                widget.setSelfPosition(new Position(0, 0));
                width = Math.max(width, widget.getSizeWidth());
                height = Math.max(height, widget.getSizeHeight());
            }
            if (getSizeWidth() != width || getSizeHeight() != height) setSize(new Size(width, height));
        } finally {
            placing = false;
        }
    }
}

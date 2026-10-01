package com.gtocore.integration.ae.wireless;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.UIStructure;

import com.gto.datasynclib.datastream.codec.ByteStreamCodec;

/**
 * 原位切换的页面容器：同一时间只显示一个子元素，其余 {@code display: none}（不绘制、不占位置），
 * 容器尺寸随当前页变化（高度跟随显示的那一页，不再取所有页中最高的）。
 * <p>
 * 切换只改显示状态，不增删控件，两端控件树始终一致。
 */
final class WirelessSwitch extends UIElement {

    private final UIStructure<Integer> page;
    private int index = -1;

    WirelessSwitch(UIElement... pages) {
        addChildren(pages);
        page = addStructure(ByteStreamCodec.INT_CODEC, () -> index)
                .validate(value -> value >= 0 && value < widgets.size())
                .apply(this::show);
        show(0);
    }

    void select(int index) {
        page.request(index);
    }

    private void show(int index) {
        this.index = index;
        for (int i = 0; i < widgets.size(); i++) {
            ((UIElement) widgets.get(i)).setDisplay(i == index);
        }
    }
}

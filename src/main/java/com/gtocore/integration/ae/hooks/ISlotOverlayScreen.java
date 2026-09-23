package com.gtocore.integration.ae.hooks;

/**
 * AE 界面上有浮在物品槽之上的面板时实现：面板覆盖处不显示下方物品槽的悬停高亮与提示。
 */
public interface ISlotOverlayScreen {

    /**
     * @param mouseX 屏幕绝对坐标
     * @param mouseY 屏幕绝对坐标
     */
    boolean gto$isOverlayAt(double mouseX, double mouseY);
}

package com.gtocore.integration.emi;

import com.lowdragmc.lowdraglib.emi.ModularWrapperWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.screen.WidgetGroup;

import java.util.List;

/**
 * EMI 配方界面里的滚轮交给配方页上的 LDLib / uipro 控件。
 * <p>
 * EMI 的控件没有滚轮事件，配方界面的滚轮一律用来翻页。这里在 EMI 翻页之前，把滚轮按鼠标位置转给配方页
 * （{@link ModularWrapperWidget} 包着的 LDLib 界面）：页里的控件照常实现 {@code Widget.mouseWheelMove}，
 * 返回 true 即消费（不翻页），返回 false 时 EMI 照旧翻页。坐标换算与 {@link ModularWrapperWidget} 处理点击相同。
 */
public final class EmiRecipeWheel {

    private EmiRecipeWheel() {}

    /** @return 是否有配方页上的控件消费了这次滚轮 */
    public static boolean dispatch(List<WidgetGroup> page, double mouseX, double mouseY, double amount) {
        for (var group : page) {
            int x = (int) mouseX - group.x(), y = (int) mouseY - group.y();
            for (Widget widget : group.widgets) {
                if (!(widget instanceof ModularWrapperWidget wrapper) || !widget.getBounds().contains(x, y)) continue;
                var modular = wrapper.modular;
                if (modular.mouseScrolled(x + modular.getLeft(), y + modular.getTop(), amount)) return true;
            }
        }
        return false;
    }
}

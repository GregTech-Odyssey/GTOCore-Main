package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.UIElement;

import com.gto.datasynclib.listener.IntNotifiableHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 分页视图：同一时间只构建当前页，页码由一个机器级同步字段 {@link IntNotifiableHolder} 驱动。
 * <p>
 * 页码字段在任一端变化（服务端改值或客户端收到同步）都会先执行 {@code onPageChanged}，再重建当前页；
 * 两端各自重建，服务端新页的初始数据经 LDLib1 的 {@code addWidget} 初始化通道下发给客户端对齐。
 * 注意：构造时会覆盖该字段已有的收发监听。
 */
public class PageView extends UIElement {

    private final IntNotifiableHolder pageSelector;
    private final List<Consumer<UIElement>> pages = new ArrayList<>();

    public PageView(int width, int height, IntNotifiableHolder pageSelector, Runnable onPageChanged) {
        layout(l -> l.column().size(width, height));
        this.pageSelector = pageSelector;
        pageSelector.setReceiverListener((side, oldPage, newPage) -> {
            onPageChanged.run();
            refresh();
        });
        pageSelector.setSenderListener((side, oldPage, newPage) -> {
            onPageChanged.run();
            refresh();
        });
    }

    public PageView addPage(Consumer<UIElement> page) {
        pages.add(page);
        return this;
    }

    public int getPageCount() {
        return pages.size();
    }

    /** 丢弃当前内容，按页码重新构建当前页。 */
    public void refresh() {
        clearAllWidgets();
        if (pages.isEmpty()) return;
        int page = Math.max(0, Math.min(pageSelector.get(), pages.size() - 1));
        var content = UIElement.column(getContentWidth());
        pages.get(page).accept(content);
        addWidget(content);
        initWidget();
    }
}

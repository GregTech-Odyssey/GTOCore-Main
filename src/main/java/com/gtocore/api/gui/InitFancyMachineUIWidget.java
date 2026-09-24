package com.gtocore.api.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * 构造时先执行一次 {@code init} 的 {@link FancyMachineUIWidget}，
 * 并吞掉 {@code detectAndSendChanges} 里的异常（记日志），避免单个页面出错把整个界面同步打断。
 */
public class InitFancyMachineUIWidget extends FancyMachineUIWidget {

    private static final Logger LOGGER = LogUtils.getLogger();

    public InitFancyMachineUIWidget(IFancyUIProvider mainPage, int width, int height) {
        this(mainPage, width, height, () -> {});
    }

    public InitFancyMachineUIWidget(IFancyUIProvider mainPage, int width, int height, Runnable init) {
        super(mainPage, width, height);
        init.run();
    }

    @Override
    public void detectAndSendChanges() {
        try {
            super.detectAndSendChanges();
        } catch (Exception e) {
            LOGGER.error("Failed to sync fancy machine UI", e);
        }
    }
}

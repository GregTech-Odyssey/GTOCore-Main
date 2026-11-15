package com.gtocore.common.forge;

import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import org.apache.logging.log4j.LogManager;

/**
 * Forge 事件总线并行初始化同一父事件的两个子事件时会丢掉其中一个的监听表，此后订阅父事件的监听永远收不到它，表现为个别模组的配置偶发整局不加载。
 * 在模组并行构造之前，单线程依次建好配置事件三个子类的监听表。
 */
public final class ConfigEventLists {

    private static boolean prepared;

    private ConfigEventLists() {}

    public static synchronized void prepare() {
        if (prepared) return;
        prepared = true;
        EventListenerHelper.getListenerList(ModConfigEvent.Loading.class);
        EventListenerHelper.getListenerList(ModConfigEvent.Reloading.class);
        EventListenerHelper.getListenerList(ModConfigEvent.Unloading.class);
        LogManager.getLogger("GTOCore").info("Config event listener lists prepared before mod construction");
    }
}

package com.gtocore.integration.ae.wireless;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 客户端缓存：本玩家可访问的网络（id → 名称）与本玩家的收藏网络，由 {@link WirelessSync} 整体下发替换。
 * 用于世界内高亮、Jade 显示网络名，以及无线界面建页时决定列表可见行数（{@link #size}，只影响客户端尺寸）；
 * 机器界面的数据一律走界面自己的同步，不读这里。
 */
public final class WirelessClientCache {

    private record Snapshot(Map<String, String> names, String favorite) {}

    private static volatile Snapshot snapshot = new Snapshot(Map.of(), "");

    private WirelessClientCache() {}

    static void accept(Map<String, String> names, String favorite) {
        snapshot = new Snapshot(Map.copyOf(names), favorite);
    }

    /** 可访问网络的名称；不可访问或未知时返回 null。 */
    @Nullable
    public static String name(String id) {
        return snapshot.names().get(id);
    }

    /** 可访问网络的个数。 */
    public static int size() {
        return snapshot.names().size();
    }

    /** 本玩家的收藏网络 id，没有收藏时为空串。 */
    public static String favorite() {
        return snapshot.favorite();
    }

    /** 断开连接时清空，避免换服后显示上一个服务器的网络。 */
    public static void clear() {
        snapshot = new Snapshot(Map.of(), "");
    }
}

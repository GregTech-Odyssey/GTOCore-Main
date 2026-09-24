package com.gtocore.integration.ae.wireless;

import com.gtocore.api.gui.ui.elements.StatusLine;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 一次打开的无线界面的界面状态（两端各一份）：操作玩家、输入框缓冲、最近一次操作结果。
 * 这些都是"这个界面"的状态，不放进机器的同步字段——多人同时打开同一台机器时互不干扰。
 */
final class WirelessUIContext {

    /** 状态行显示多久后清空（tick）。 */
    private static final int STATUS_TICKS = 60;

    final boolean remote;
    final Player player;
    /** 计时：机器用 {@code getOffsetTimer()}，配置器用玩家的 {@code tickCount}。 */
    private final IntSupplier clock;

    /** 新建网络输入框的内容（服务端这份由输入框上行写入）。 */
    String pendingName = "";
    /** 改名输入框的内容。 */
    String renameBuffer = "";

    @Nullable
    private WirelessStatus lastResult;
    private int resultTime;
    @Nullable
    private UUID ownerNameKey;
    private String ownerName = "";

    WirelessUIContext(Player player, IntSupplier clock) {
        this.player = player;
        this.remote = player.level().isClientSide();
        this.clock = clock;
    }

    /** 服务端：发起操作的玩家。 */
    ServerPlayer serverPlayer() {
        return (ServerPlayer) player;
    }

    UUID uuid() {
        return player.getUUID();
    }

    /** 服务端：网络数据。 */
    WirelessNetworks networks() {
        return WirelessNetworks.get(serverPlayer().server);
    }

    /** 服务端：网络所有者的显示名。界面每 tick 取值，按所有者缓存，所有者变了才重新查找玩家名。 */
    String ownerName(UUID owner) {
        if (!owner.equals(ownerNameKey)) {
            ownerNameKey = owner;
            ownerName = WirelessNetworks.ownerName(serverPlayer().server, owner);
        }
        return ownerName;
    }

    /** 服务端：记下操作结果，状态面板第一行显示它 {@link #STATUS_TICKS} tick，之后回到常规状态。 */
    void report(WirelessStatus result) {
        lastResult = result;
        resultTime = clock.getAsInt();
    }

    /** 服务端：最近 {@link #STATUS_TICKS} tick 内的操作结果；没有时为 null。 */
    @Nullable
    WirelessStatus recentResult() {
        return lastResult != null && clock.getAsInt() - resultTime < STATUS_TICKS ? lastResult : null;
    }

    /** 状态行文字：有最近的操作结果时显示结果，否则显示 {@code normal}。 */
    Component stateText(Supplier<Component> normal) {
        var result = recentResult();
        return result != null ? result.message() : normal.get();
    }

    /** 状态行等级：有最近的操作结果时成功为正常、失败为错误，否则取 {@code normal}。 */
    StatusLine.Level stateLevel(Supplier<StatusLine.Level> normal) {
        var result = recentResult();
        if (result == null) return normal.get();
        return result.ok() ? StatusLine.Level.GOOD : StatusLine.Level.ERROR;
    }
}

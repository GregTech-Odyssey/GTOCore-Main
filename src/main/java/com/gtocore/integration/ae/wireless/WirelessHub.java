package com.gtocore.integration.ae.wireless;

import com.gtocore.common.data.GTOMachines;

import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.pathing.ControllerState;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每个无线网络在运行时持有的一个虚拟 AE 节点（hub）：不在世界里、不与任何方块相邻，挂在主世界，懒创建，随服务器存活。
 * <p>
 * 成员机器各自只负责自己到 hub 的<strong>一条</strong>连接（见 {@link WirelessMachine#attachWireless}），
 * 同一网络的所有成员因此在同一个 AE 网格里。成员区块/维度卸载时 AE 销毁成员节点、自动拆边，这里什么都不用做；
 * 删除网络时销毁 hub，AE 同样自动拆掉所有成员的边。不保存任何连接句柄，需要时从节点的连接表里现找。
 */
public final class WirelessHub {

    private static final Map<String, WirelessHub> HUBS = new HashMap<>();

    private final IManagedGridNode node;

    private WirelessHub(MinecraftServer server) {
        this.node = GridHelper.createManagedNode(this, Listener.INSTANCE)
                .setInWorldNode(false)
                .setFlags(GridFlags.DENSE_CAPACITY)
                .setIdlePowerUsage(0)
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .setVisualRepresentation(GTOMachines.ME_WIRELESS_CONNECTION_MACHINE.asItem());
        this.node.create(server.overworld(), null);
    }

    public static WirelessHub getOrCreate(MinecraftServer server, String networkId) {
        return HUBS.computeIfAbsent(networkId, id -> new WirelessHub(server));
    }

    @Nullable
    public static WirelessHub get(String networkId) {
        return HUBS.get(networkId);
    }

    /** 删除网络时调用：销毁 hub 节点，AE 自动拆掉所有成员到它的连接。 */
    public static void destroy(String networkId) {
        var hub = HUBS.remove(networkId);
        if (hub != null) hub.node.destroy();
    }

    /**
     * 主世界卸载（服务器停止）时清表。AE 在世界卸载时销毁该世界的全部节点（含 hub），
     * 这里只丢弃引用，保证不会再拿到已销毁的 hub，下一个存档（单人切档）从空表开始、不会串网。
     */
    public static void clearAll() {
        HUBS.clear();
    }

    /**
     * 断开主人已无权使用所在网络的成员（队伍变动后调用）。只断边、不改 id：权限恢复后下次加载自动接回，
     * 与 {@link WirelessMachine#attachWireless} 的判定一致。
     */
    static void detachUnauthorized(WirelessNetworks networks) {
        for (var entry : HUBS.entrySet()) {
            var network = networks.get(entry.getKey());
            if (network == null) continue;
            for (var member : entry.getValue().members()) {
                var owner = member.self().getOwnerUUID();
                if (owner != null && !network.canUse(owner)) member.detachWireless();
            }
        }
    }

    public IGridNode node() {
        return node.getNode();
    }

    /** 该成员节点是否已有一条连到本 hub 的边。 */
    public boolean isConnected(IGridNode member) {
        var hubNode = node();
        for (var connection : member.getConnections()) {
            if (connection.getOtherSide(member) == hubNode) return true;
        }
        return false;
    }

    /** 当前已连上的成员数（= hub 的连接数）。 */
    public int memberCount() {
        return node().getConnections().size();
    }

    /**
     * 成员构成的指纹：由各条连接对象的身份组成，成员增减、换人都会变化。界面用它判断成员列表是否需要重建，
     * 不必每 tick 生成成员信息再排序。
     */
    public int membershipStamp() {
        int stamp = 1;
        for (var connection : node().getConnections()) stamp = 31 * stamp + System.identityHashCode(connection);
        return stamp;
    }

    /** 当前已连上的成员机器（已加载的；未加载区块里的成员不在其中）。 */
    public List<WirelessMachine> members() {
        var hubNode = node();
        var connections = hubNode.getConnections();
        var result = new ArrayList<WirelessMachine>(connections.size());
        for (var connection : connections) {
            if (connection.getOtherSide(hubNode).getOwner() instanceof WirelessMachine machine) result.add(machine);
        }
        return result;
    }

    /** 所在网格的控制器状态；多台分属不同控制器的机器入同一网络时为 {@link ControllerState#CONTROLLER_CONFLICT}。 */
    public ControllerState controllerState() {
        return node().getGrid().getPathingService().getControllerState();
    }

    private enum Listener implements IGridNodeListener<WirelessHub> {

        INSTANCE;

        @Override
        public void onSaveChanges(WirelessHub hub, IGridNode node) {}
    }
}

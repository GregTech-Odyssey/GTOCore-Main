package com.gtocore.integration.ae.wireless;

import com.gtocore.common.data.GTOMachines;
import com.gtocore.integration.ae.SolarStormHandler;
import com.gtocore.mixin.ae2.GridNodeAccessor;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;

import com.gto.datasynclib.datastream.DataComponentKey;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * 无线网络的虚拟 AE hub，注册表挂在所属 Level capability 上，随世界释放。
 * 主世界与太阳表面各持有本地 hub，跨维度边受太阳风暴影响；成员到本地 hub 的边始终保留。
 * 成员卸载或网络删除时由 AE 节点销毁处理拆边，不保存连接句柄，也不扫描 tick。
 */
public final class WirelessHub {

    private static final DataComponentKey<O2OOpenCacheHashMap<String, WirelessHub>> KEY = DataComponentKey.createNoCodec("wireless_me_hubs");

    private final ServerLevel level;
    private final String networkId;
    private final IManagedGridNode node;

    private WirelessHub(ServerLevel level, String networkId) {
        this.level = level;
        this.networkId = networkId;
        node = GridHelper.createManagedNode(this, Listener.INSTANCE)
                .setInWorldNode(false)
                .setFlags(GridFlags.DENSE_CAPACITY)
                .setIdlePowerUsage(0)
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .setVisualRepresentation(GTOMachines.ME_WIRELESS_CONNECTION_MACHINE.asItem());
        node.create(level, null);
        if (SolarStormHandler.isSolarSurface(level)) {
            GridHelper.createConnection(node(), getOrCreate(level.getServer(), networkId).node());
        }
    }

    private static WirelessHub getOrCreate(ServerLevel level, String networkId) {
        var hubs = ILevel.getCapability(level, KEY);
        if (hubs == null) {
            hubs = new O2OOpenCacheHashMap<>();
            ILevel.setCapability(level, KEY, hubs);
        }
        return hubs.computeIfAbsent(networkId, id -> new WirelessHub(level, networkId));
    }

    public static WirelessHub getOrCreate(MinecraftServer server, String networkId) {
        return getOrCreate(server.overworld(), networkId);
    }

    @Nullable
    private static WirelessHub get(ServerLevel level, String networkId) {
        var hubs = ILevel.getCapability(level, KEY);
        return hubs == null ? null : hubs.get(networkId);
    }

    @Nullable
    public static WirelessHub get(MinecraftServer server, String networkId) {
        return get(server.overworld(), networkId);
    }

    /** 删除网络的各维度 hub，AE 自动拆边。capability 的生命周期由 Level 管理。 */
    public static void destroy(MinecraftServer server, String networkId) {
        for (var level : server.getAllLevels()) {
            var hubs = ILevel.getCapability(level, KEY);
            if (hubs == null) continue;
            var hub = hubs.remove(networkId);
            if (hub != null) hub.node.destroy();
        }
    }

    /** 队伍变化后只断开无权使用网络的成员，不修改其网络 id。 */
    static void detachUnauthorized(MinecraftServer server, WirelessNetworks networks) {
        for (var level : server.getAllLevels()) {
            var hubs = ILevel.getCapability(level, KEY);
            if (hubs == null) continue;
            var iterator = hubs.object2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                var network = networks.get(entry.getKey());
                if (network == null) continue;
                // Member detaches mutate the connection list, so use a member snapshot here.
                for (var member : entry.getValue().localMembers()) {
                    var owner = member.self().getOwnerUUID();
                    if (owner != null && !network.canUse(owner)) member.detachWireless();
                }
            }
        }
    }

    public IGridNode node() {
        return node.getNode();
    }

    /** 成员连向其维度一侧；仅 hub 之间的边跨越太阳表面边界。 */
    public IGridNode node(ServerLevel level) {
        return SolarStormHandler.isSolarSurface(level) ? getOrCreate(level, networkId).node() : node();
    }

    /** 查询时不创建本地 hub。 */
    @Nullable
    public IGridNode existingNode(ServerLevel level) {
        if (!SolarStormHandler.isSolarSurface(level)) return node();
        var hub = get(level, networkId);
        return hub == null ? null : hub.node();
    }

    public boolean isConnected(IGridNode member) {
        var hubNode = existingNode(member.getLevel());
        if (hubNode == null) return false;
        var connections = ((GridNodeAccessor) member).gto$getConnections();
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).getOtherSide(member) == hubNode) return true;
        }
        return false;
    }

    /** 当前已连上的成员数，直接计数，不构造成员列表。 */
    public int memberCount() {
        var other = otherHub();
        return localMemberCount() + (other == null ? 0 : other.localMemberCount());
    }

    @Nullable
    private WirelessHub otherHub() {
        var otherLevel = SolarStormHandler.isSolarSurface(level) ? level.getServer().overworld() : level.getServer().getLevel(SolarStormHandler.SOLAR_SURFACE);
        return otherLevel == null ? null : get(otherLevel, networkId);
    }

    private int localMemberCount() {
        int count = 0;
        var node = node();
        var connections = ((GridNodeAccessor) node).gto$getConnections();
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).getOtherSide(node).getOwner() instanceof WirelessMachine) count++;
        }
        return count;
    }

    /** 成员身份指纹，忽略受风暴影响的 hub 间连接，避免无效重建界面成员列表。 */
    public int membershipStamp() {
        int stamp = membershipStamp(1);
        var other = otherHub();
        return other == null ? stamp : other.membershipStamp(stamp);
    }

    private int membershipStamp(int stamp) {
        var node = node();
        var connections = ((GridNodeAccessor) node).gto$getConnections();
        for (int i = 0; i < connections.size(); i++) {
            var connection = connections.get(i);
            if (connection.getOtherSide(node).getOwner() instanceof WirelessMachine) stamp = 31 * stamp + System.identityHashCode(connection);
        }
        return stamp;
    }

    public ArrayList<WirelessMachine> members() {
        var result = new ArrayList<WirelessMachine>(memberCount());
        addMembers(result);
        var other = otherHub();
        if (other != null) other.addMembers(result);
        return result;
    }

    private ArrayList<WirelessMachine> localMembers() {
        var result = new ArrayList<WirelessMachine>(((GridNodeAccessor) node()).gto$getConnections().size());
        addMembers(result);
        return result;
    }

    private void addMembers(ArrayList<WirelessMachine> result) {
        var node = node();
        var connections = ((GridNodeAccessor) node).gto$getConnections();
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).getOtherSide(node).getOwner() instanceof WirelessMachine machine) result.add(machine);
        }
    }

    private enum Listener implements IGridNodeListener<WirelessHub> {

        INSTANCE;

        @Override
        public void onSaveChanges(WirelessHub hub, IGridNode node) {}
    }
}

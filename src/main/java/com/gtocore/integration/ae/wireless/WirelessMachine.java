package com.gtocore.integration.ae.wireless;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import appeng.api.networking.GridHelper;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 可加入 GTO 无线 ME 网络的机器（ME 无线连接机、各种 ME 部件）。
 * <p>
 * 拓扑：每个网络一个虚拟 hub 节点（{@link WirelessHub}），每台成员机器只维护自己到 hub 的<strong>一条</strong>连接，
 * 同网所有成员因此处在同一个 AE 网格里。没有源/子节点之分，没有分配表、没有周期扫描。
 * <p>
 * 生命周期：
 * <ul>
 * <li>{@code onLoad}：在 {@code super.onLoad()} 之后调用 {@link #onWirelessLoad()}，注册 AE 首 tick 回调
 * （排在 GTM 建节点的回调之后，同一区块先进先出），回调里 {@link #attachWireless()}。</li>
 * <li>{@code onUnload}：什么都不做——AE 销毁节点时自动拆边。</li>
 * <li>放置：{@link #onWirelessPlaced} 处理潜行放置自动加入收藏网络。</li>
 * </ul>
 * 实现类只需提供网络 id 字段（存档键固定为 {@code _connectedNetworkId}，{@code @SyncToClient} 供客户端高亮与 Jade）。
 */
@DataGeneratorScanned
public interface WirelessMachine extends IGridConnectedMachine {

    @RegisterLanguage(cn = "已连接：%s", en = "Connected: %s")
    String KEY_CONNECTED = "gtocore.wireless.connected";
    @RegisterLanguage(cn = "无权查看的网络", en = "Inaccessible network")
    String KEY_UNKNOWN_NETWORK = "gtocore.wireless.jade.unknown_network";
    @RegisterLanguage(cn = "未加入无线网络", en = "Not in a wireless network")
    String KEY_STATE_STANDALONE = "gtocore.wireless.state.standalone";
    @RegisterLanguage(cn = "无线连接正常，ME 网络在线", en = "Wireless link up, ME network online")
    String KEY_STATE_ONLINE = "gtocore.wireless.state.online";
    @RegisterLanguage(cn = "已加入无线网络，但 ME 网络离线", en = "Joined, but the ME network is offline")
    String KEY_STATE_OFFLINE = "gtocore.wireless.state.offline";
    @RegisterLanguage(cn = "机器所有者无权使用此网络，未连接", en = "The machine owner cannot use this network; not linked")
    String KEY_STATE_NO_PERMISSION = "gtocore.wireless.state.no_permission";
    @RegisterLanguage(cn = "无线网络数据不可用", en = "Wireless network data unavailable")
    String KEY_STATE_UNAVAILABLE = "gtocore.wireless.state.unavailable";

    /** 无线连接状态（服务端计算），界面指示灯与 Jade 共用。 */
    enum LinkState {

        /** 没有加入任何无线网络。 */
        STANDALONE,
        /** 已连上 hub，且 ME 网络在线。 */
        ONLINE,
        /** 已加入网络，但未连上 hub 或 ME 网络离线（未供电、无控制器、控制器冲突等）。 */
        OFFLINE,
        /** 机器所有者无权使用该网络，未连接。 */
        NO_PERMISSION,
        /** 无线网络数据不可用（存档文件损坏或版本过新），未连接，id 保留。 */
        UNAVAILABLE;

        public Component describe() {
            return switch (this) {
                case STANDALONE -> Component.translatable(KEY_STATE_STANDALONE);
                case ONLINE -> Component.translatable(KEY_STATE_ONLINE);
                case OFFLINE -> Component.translatable(KEY_STATE_OFFLINE);
                case NO_PERMISSION -> Component.translatable(KEY_STATE_NO_PERMISSION);
                case UNAVAILABLE -> Component.translatable(KEY_STATE_UNAVAILABLE);
            };
        }
    }

    // ==================== 实现类提供 ====================

    /** 当前网络 id，空串表示未加入。 */
    String getWirelessNetworkId();

    void setWirelessNetworkId(String id);

    /** 是否允许加入无线网络（例如简易样板总成不允许）。 */
    default boolean allowWirelessConnection() {
        return true;
    }

    // ==================== 生命周期 ====================

    /** 在 {@code onLoad} 的 {@code super.onLoad()} 之后调用（仅服务端生效）：节点建好后的首 tick 接回网络。 */
    default void onWirelessLoad() {
        var machine = self();
        if (machine.isRemote()) return;
        GridHelper.onFirstTick(machine.holder, blockEntity -> attachWireless());
    }

    /** 潜行放置时自动加入放置者收藏的网络；失败在 actionbar 提示原因。 */
    default void onWirelessPlaced(@Nullable LivingEntity placer, ItemStack stack) {
        if (!(placer instanceof ServerPlayer player) || !player.isShiftKeyDown()) return;
        var favorite = WirelessNetworks.get(player.server).favorite(player.getUUID());
        if (favorite == null) return;
        var status = joinWireless(player, favorite);
        if (!status.ok()) player.displayClientMessage(status.message(), true);
    }

    // ==================== 连接 ====================

    /**
     * 按当前 id 连到对应网络的 hub（幂等）。网络已被删除（例如删除时本机所在区块未加载）则清空 id；
     * 机器有主人且主人无权使用该网络时不连（{@link LinkState#NO_PERMISSION}，界面与 Jade 显示原因）；
     * 无线数据不可用时不连、不改 id。
     */
    default void attachWireless() {
        var id = getWirelessNetworkId();
        if (id.isEmpty()) return;
        if (!allowWirelessConnection()) {
            clearWirelessNetworkId();
            return;
        }
        var server = Objects.requireNonNull(self().getLevel()).getServer();
        var networks = WirelessNetworks.get(Objects.requireNonNull(server));
        if (!networks.isAvailable()) return;
        var network = networks.get(id);
        if (network == null) {
            clearWirelessNetworkId();
            return;
        }
        var owner = self().getOwnerUUID();
        if (owner != null && !network.canUse(owner)) return;
        var node = getMainNode().getNode();
        // 放置当 tick 节点还没建：已排队的首 tick 回调会再调用本方法接上
        if (node == null) return;
        var hub = WirelessHub.getOrCreate(server, id);
        if (hub.isConnected(node)) return;
        GridHelper.createConnection(node, hub.node());
    }

    /**
     * 机器主人（有的话）能否使用该网络。操作玩家能管理机器、能用网络还不够：OP 可以管理别人的机器，
     * 但机器仍以主人身份接入，主人无权使用的网络接不上（{@link #attachWireless} 同样按主人判定）。
     */
    private boolean ownerCanUse(WirelessNetwork network) {
        var owner = self().getOwnerUUID();
        return owner == null || network.canUse(owner);
    }

    /** 拆掉本机到当前网络 hub 的连接（从节点的连接表里现找，不持有句柄）。只用于主动离开或换网。 */
    default void detachWireless() {
        var hub = WirelessHub.get(getWirelessNetworkId());
        var node = getMainNode().getNode();
        if (hub == null || node == null) return;
        var hubNode = hub.node();
        for (var connection : node.getConnections()) {
            if (connection.getOtherSide(node) == hubNode) {
                connection.destroy();
                return;
            }
        }
    }

    /** 由玩家发起的加入（界面、配置器、潜行放置）：先校验两层权限，失败时不写 id。 */
    default WirelessStatus joinWireless(ServerPlayer player, String id) {
        if (!allowWirelessConnection()) return WirelessStatus.NOT_ALLOWED;
        var networks = WirelessNetworks.get(player.server);
        if (!networks.isAvailable()) return WirelessStatus.UNAVAILABLE;
        if (!WirelessPermissions.canManage(player, self())) return WirelessStatus.NO_PERMISSION_MACHINE;
        var network = networks.get(id);
        if (network == null) return WirelessStatus.NOT_FOUND;
        if (!network.canUse(player.getUUID()) || !ownerCanUse(network)) return WirelessStatus.NO_PERMISSION_NETWORK;
        if (!id.equals(getWirelessNetworkId())) {
            detachWireless();
            setWirelessNetworkId(id);
            markWirelessChanged();
        }
        attachWireless();
        return WirelessStatus.OK;
    }

    /**
     * 界面"新建并加入"的预检，与 {@link #joinWireless} 的校验一致（新网络的所有者就是发起玩家）：
     * 先检查再创建，不会留下建好了却加不进去的网络。
     */
    default WirelessStatus checkCreateAndJoin(ServerPlayer player) {
        if (!allowWirelessConnection()) return WirelessStatus.NOT_ALLOWED;
        if (!WirelessNetworks.get(player.server).isAvailable()) return WirelessStatus.UNAVAILABLE;
        if (!WirelessPermissions.canManage(player, self())) return WirelessStatus.NO_PERMISSION_MACHINE;
        var owner = self().getOwnerUUID();
        if (owner != null && !WirelessPermissions.sameTeam(owner, player.getUUID())) return WirelessStatus.NO_PERMISSION_NETWORK;
        return WirelessStatus.OK;
    }

    /** 由玩家发起的离开。 */
    default WirelessStatus leaveWireless(ServerPlayer player) {
        if (!WirelessNetworks.get(player.server).isAvailable()) return WirelessStatus.UNAVAILABLE;
        if (!WirelessPermissions.canManage(player, self())) return WirelessStatus.NO_PERMISSION_MACHINE;
        detachWireless();
        clearWirelessNetworkId();
        return WirelessStatus.OK;
    }

    // ==================== 查询（服务端） ====================

    @Nullable
    default WirelessNetwork getWirelessNetwork() {
        var id = getWirelessNetworkId();
        if (id.isEmpty()) return null;
        var server = Objects.requireNonNull(self().getLevel()).getServer();
        return WirelessNetworks.get(Objects.requireNonNull(server)).get(id);
    }

    /** 是否已有一条连到当前网络 hub 的边。 */
    default boolean isWirelessLinked() {
        var hub = WirelessHub.get(getWirelessNetworkId());
        var node = getMainNode().getNode();
        return hub != null && node != null && hub.isConnected(node);
    }

    default LinkState getWirelessLinkState() {
        if (getWirelessNetworkId().isEmpty()) return LinkState.STANDALONE;
        if (!isWirelessAvailable()) return LinkState.UNAVAILABLE;
        var network = getWirelessNetwork();
        if (network == null) return LinkState.OFFLINE;
        if (!ownerCanUse(network)) return LinkState.NO_PERMISSION;
        return isWirelessLinked() && getMainNode().isOnline() ? LinkState.ONLINE : LinkState.OFFLINE;
    }

    /** 服务端：无线数据是否可用。 */
    default boolean isWirelessAvailable() {
        var server = Objects.requireNonNull(self().getLevel()).getServer();
        return WirelessNetworks.get(Objects.requireNonNull(server)).isAvailable();
    }

    // ==================== 辅助 ====================

    /** id 改动后：标记存盘并同步到客户端（高亮、Jade 读客户端字段）。 */
    default void markWirelessChanged() {
        self().onChanged();
        self().requestSync();
    }

    private void clearWirelessNetworkId() {
        setWirelessNetworkId("");
        markWirelessChanged();
    }

    /** ME 部件侧边的无线网络标签页。 */
    default IFancyUIProvider getWirelessUIProvider() {
        return WirelessMachineUI.tab(this);
    }
}

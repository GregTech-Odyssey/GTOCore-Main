package com.gtocore.integration.ae.wireless;

import com.gtolib.api.scope.TeamSignal;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.server.level.ServerLevel;

import com.suried.gtoutils.schedule.OwnedScope;
import com.suried.gtoutils.schedule.Schedulers;
import com.suried.gtoutils.schedule.Scope;
import com.suried.gtoutils.schedule.Signal;

/**
 * 已加入无线网络且节点已建好的机器的连接作用域：网络存在、主人有权使用、节点在时接上 hub，条件变假时断开。
 * 卸载或换网时整个结束，不碰节点，由 AE 销毁节点或 hub 时拆边。
 */
public final class WirelessLink {

    private final MetaMachine host;
    private final OwnedScope scope;
    private final Signal ownerChanged = new Signal();

    private WirelessLink(MetaMachine host, ServerLevel level) {
        this.host = host;
        this.scope = OwnedScope.root(this, Schedulers.of(level));
    }

    static void refresh(WirelessMachine machine) {
        close(machine);
        var self = machine.self();
        if (self.isRemoved() || !(self.getLevel() instanceof ServerLevel level)) return;
        var id = machine.getWirelessNetworkId();
        if (id.isEmpty() || machine.getMainNode().getNode() == null) return;
        if (!machine.allowWirelessConnection()) {
            machine.assignWirelessNetwork("");
            return;
        }
        var networks = WirelessNetworks.get(level.getServer());
        if (!networks.isAvailable()) return;
        if (networks.get(id) == null) {
            machine.assignWirelessNetwork("");
            return;
        }
        var link = new WirelessLink(self, level);
        machine.setWirelessLink(link);
        link.scope.when(() -> link.permitted(machine, networks, id), s -> link.attach(machine, s));
    }

    static void close(WirelessMachine machine) {
        var link = machine.getWirelessLink();
        if (link == null) return;
        machine.setWirelessLink(null);
        link.scope.dispose();
    }

    static void ownerChanged(WirelessMachine machine) {
        var link = machine.getWirelessLink();
        if (link != null) link.ownerChanged.emit();
    }

    private boolean permitted(WirelessMachine machine, WirelessNetworks networks, String id) {
        networks.track();
        ownerChanged.track();
        var network = networks.get(id);
        if (network == null) return false;
        var owner = machine.self().getOwnerUUID();
        TeamSignal.teamOf(owner);
        TeamSignal.teamOf(network.owner());
        return (owner == null || network.canUse(owner)) && machine.getMainNode().getNode() != null;
    }

    private void attach(WirelessMachine machine, Scope linked) {
        machine.attachWireless();
        linked.onDispose(() -> {
            if (scope.isActive()) machine.detachWireless();
        });
    }

    @Override
    public String toString() {
        return host.getDefinition().getId() + " @ " + host.getPos().toShortString();
    }
}

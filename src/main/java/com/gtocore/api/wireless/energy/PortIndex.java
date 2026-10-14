package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.server.level.ServerLevel;

import com.gto.datasynclib.datastream.DataComponentKey;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Collections;
import java.util.List;

/**
 * 每个世界按账户索引在线端点，只供监视器读取逐机器流量；随世界卸载释放。
 */
public final class PortIndex {

    private static final DataComponentKey<PortIndex> KEY = DataComponentKey.createNoCodec("gtocore_wireless_port_index");

    private final Reference2ObjectOpenHashMap<EnergyAccount, ObjectArrayList<EnergyPort>> ports = new Reference2ObjectOpenHashMap<>();

    public static PortIndex of(ServerLevel level) {
        PortIndex index = ILevel.getCapability(level, KEY);
        if (index == null) {
            index = new PortIndex();
            ILevel.setCapability(level, KEY, index);
        }
        return index;
    }

    public static List<EnergyPort> ports(ServerLevel level, EnergyAccount account) {
        PortIndex index = ILevel.getCapability(level, KEY);
        if (index == null) return Collections.emptyList();
        var list = index.ports.get(account);
        return list == null ? Collections.emptyList() : list;
    }

    void add(EnergyPort port) {
        var list = ports.computeIfAbsent(port.account, k -> new ObjectArrayList<>());
        port.indexSlot = list.size();
        port.index = this;
        list.add(port);
    }

    void remove(EnergyPort port) {
        var list = ports.get(port.account);
        int slot = port.indexSlot;
        if (list != null && slot >= 0 && slot < list.size() && list.get(slot) == port) {
            var tail = list.remove(list.size() - 1);
            if (tail != port) {
                list.set(slot, tail);
                tail.indexSlot = slot;
            }
            if (list.isEmpty()) ports.remove(port.account);
        }
        port.indexSlot = -1;
        port.index = null;
    }
}

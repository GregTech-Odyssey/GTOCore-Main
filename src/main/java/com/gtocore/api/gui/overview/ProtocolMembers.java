package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.MachineProtocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

final class ProtocolMembers {

    private record Entry(int source, List<MultiblockMachineDefinition> members) {}

    private static final ConcurrentHashMap<MachineProtocol, Entry> CACHE = new ConcurrentHashMap<>();

    private ProtocolMembers() {}

    static List<MultiblockMachineDefinition> of(MachineProtocol protocol) {
        var source = protocol.getMembers();
        var cached = CACHE.get(protocol);
        if (cached != null && cached.source == source.size()) return cached.members;
        var list = new ArrayList<MultiblockMachineDefinition>(source.size());
        for (var member : source) {
            if (member instanceof MultiblockMachineDefinition multi) list.add(multi);
        }
        var entry = new Entry(source.size(), Collections.unmodifiableList(list));
        CACHE.put(protocol, entry);
        return entry.members;
    }
}

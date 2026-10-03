package com.gtocore.api.wireless.energy;

import java.util.Arrays;

final class SettleReceipt {

    int count;
    int arcCount;
    int until;
    boolean reserve;
    long restHi, restLo;
    double total;
    double reachable;
    int priority;
    GridNode origin = GridNode.EMPTY;
    GridNode[] nodes = new GridNode[4];
    long[] his = new long[4];
    long[] los = new long[4];
    Arc[] arcs = new Arc[4];
    long[] amounts = new long[4];

    void clear() {
        Arrays.fill(nodes, 0, count, null);
        Arrays.fill(arcs, 0, arcCount, null);
        count = 0;
        arcCount = 0;
        total = 0;
        origin = GridNode.EMPTY;
    }

    void consume(long h, long l) {
        long r = restLo - l;
        restHi = restHi - h + (r >> 63);
        restLo = r & U126.MASK;
    }

    void add(GridNode node, long hi, long lo) {
        if (count == nodes.length) {
            int size = count << 1;
            nodes = Arrays.copyOf(nodes, size);
            his = Arrays.copyOf(his, size);
            los = Arrays.copyOf(los, size);
        }
        nodes[count] = node;
        his[count] = hi;
        los[count] = lo;
        total += U126.toDouble(hi, lo);
        count++;
    }

    void addArc(Arc arc, long amount) {
        if (arcCount == arcs.length) {
            int size = arcCount << 1;
            arcs = Arrays.copyOf(arcs, size);
            amounts = Arrays.copyOf(amounts, size);
        }
        arcs[arcCount] = arc;
        amounts[arcCount] = amount;
        arcCount++;
    }
}

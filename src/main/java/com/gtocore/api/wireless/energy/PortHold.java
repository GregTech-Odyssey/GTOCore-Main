package com.gtocore.api.wireless.energy;

/**
 * 端点因存量不足（或空间不足）而缺的量，按端点所在节点与优先级登记，供同维度低优先级的整笔结算让位。
 */
final class PortHold {

    private GridNode node = GridNode.EMPTY;
    private int priority;
    private long draw, put;

    long draw() {
        return draw;
    }

    long put() {
        return put;
    }

    void set(GridNode target, int targetPriority, long newDraw, long newPut) {
        if (node != target || priority != targetPriority) {
            if (node != GridNode.EMPTY && (draw != 0 || put != 0)) node.hold(priority, -draw, -put);
            draw = 0;
            put = 0;
            node = target;
            priority = targetPriority;
        }
        if (target == GridNode.EMPTY || (newDraw == draw && newPut == put)) return;
        target.hold(targetPriority, newDraw - draw, newPut - put);
        draw = newDraw;
        put = newPut;
    }

    void clear() {
        set(node, priority, 0, 0);
    }
}

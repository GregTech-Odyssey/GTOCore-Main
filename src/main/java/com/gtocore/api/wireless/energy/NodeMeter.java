package com.gtocore.api.wireless.energy;

import org.jetbrains.annotations.Nullable;

/**
 * 节点级流量统计：按端点所在节点归属出入电，懒创建；被触及的节点随账户统计每秒滚动一次。
 */
final class NodeMeter {

    static final int IN = 0, OUT = 1, LOSS = 2;

    final EnergyStats stats = new EnergyStats();
    private int second = -1;
    private double secIn, secOut, secLoss;

    static void in(EnergyAccount account, GridNode node, double gross, double lost) {
        var meter = touch(account, node);
        if (meter == null) return;
        meter.secIn += gross;
        meter.secLoss += lost;
    }

    static void out(EnergyAccount account, GridNode node, double amount) {
        var meter = touch(account, node);
        if (meter != null) meter.secOut += amount;
    }

    @Nullable
    private static NodeMeter touch(EnergyAccount account, GridNode node) {
        if (node == GridNode.EMPTY) return null;
        var meter = node.meter;
        if (meter == null) node.meter = meter = new NodeMeter();
        int now = GridClock.second();
        if (meter.second != now) {
            meter.second = now;
            account.touchedNodes.add(node);
        }
        return meter;
    }

    void roll() {
        stats.push(second, secIn, secOut, secLoss);
        secIn = 0;
        secOut = 0;
        secLoss = 0;
    }

    static double window(@Nullable EnergyStats stats, int kind, int window, int second) {
        if (stats == null) return 0;
        int w = GridView.clampWindow(window);
        if (w == 0) {
            return switch (kind) {
                case IN -> stats.nowIn(second);
                case OUT -> stats.nowOut(second);
                default -> stats.nowLoss(second);
            };
        }
        var span = w == 1 ? EnergyStats.Window.MINUTE : w == 2 ? EnergyStats.Window.HOUR : EnergyStats.Window.DAY;
        return switch (kind) {
            case IN -> stats.avgIn(span, second);
            case OUT -> stats.avgOut(span, second);
            default -> stats.avgLoss(span, second);
        };
    }

    static double[] windows(@Nullable EnergyStats stats, int kind, int second) {
        var values = new double[GridView.WINDOWS];
        for (int w = 0; w < values.length; w++) values[w] = window(stats, kind, w, second);
        return values;
    }
}

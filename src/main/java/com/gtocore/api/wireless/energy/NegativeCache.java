package com.gtocore.api.wireless.energy;

/**
 * 本 tick 某节点在某电压及以上已远程取空（存满）的结论，账户资源代号变化即失效；limited 表示结论里有线路受限的成分。
 */
final class NegativeCache {

    private int tick = -1;
    private int gen;
    private int voltage;
    boolean limited;

    boolean hit(int now, int currentGen, int atVoltage) {
        return tick == now && gen == currentGen && atVoltage >= voltage;
    }

    void mark(int now, int currentGen, int atVoltage, boolean lineLimited) {
        if (tick == now && gen == currentGen) {
            voltage = Math.min(voltage, atVoltage);
            limited |= lineLimited;
        } else {
            tick = now;
            gen = currentGen;
            voltage = atVoltage;
            limited = lineLimited;
        }
    }
}

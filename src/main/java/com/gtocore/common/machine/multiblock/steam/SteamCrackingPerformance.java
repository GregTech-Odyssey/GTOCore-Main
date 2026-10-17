package com.gtocore.common.machine.multiblock.steam;

final class SteamCrackingPerformance {

    static final int FULL_EFFICIENCY_BATCHES = 20;
    private static final int MIN_TEMPERATURE = 300;
    private static final int MAX_TEMPERATURE = 900;

    private SteamCrackingPerformance() {}

    static int parallelLimit(double temperature) {
        return (int) Math.pow(4, (clampTemperature(temperature) - MIN_TEMPERATURE) / 300.0);
    }

    static int efficiencyPercent(int completedBatches) {
        return 40 + 65 * Math.clamp(completedBatches, 0, FULL_EFFICIENCY_BATCHES) / FULL_EFFICIENCY_BATCHES;
    }

    static long severeAmount(long total, double temperature) {
        return (long) (total * (clampTemperature(temperature) - MIN_TEMPERATURE) / (MAX_TEMPERATURE - MIN_TEMPERATURE));
    }

    private static double clampTemperature(double temperature) {
        return Math.clamp(temperature, MIN_TEMPERATURE, MAX_TEMPERATURE);
    }
}

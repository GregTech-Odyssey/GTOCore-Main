package com.gtocore.api.wireless.energy;

/**
 * 按秒汇总的流量统计：分钟（60×1 秒）、小时（60×1 分钟）、天（24×1 小时）三个环，空闲期不需要推进。
 */
public final class EnergyStats {

    public enum Window {

        MINUTE,
        HOUR,
        DAY
    }

    private final Ring minute = new Ring(60, 1);
    private final Ring hour = new Ring(60, 60);
    private final Ring day = new Ring(24, 3600);
    private int firstSecond = -1;
    private int lastSecond = -1;
    private double lastIn, lastOut, lastLoss;

    public void push(int second, double in, double out, double loss) {
        if (firstSecond < 0) firstSecond = second;
        minute.add(second, in, out, loss);
        hour.add(second, in, out, loss);
        day.add(second, in, out, loss);
        lastSecond = second;
        lastIn = in;
        lastOut = out;
        lastLoss = loss;
    }

    public double nowIn(int currentSecond) {
        return currentSecond - lastSecond <= 1 ? lastIn / 20 : 0;
    }

    public double nowOut(int currentSecond) {
        return currentSecond - lastSecond <= 1 ? lastOut / 20 : 0;
    }

    public double nowLoss(int currentSecond) {
        return currentSecond - lastSecond <= 1 ? lastLoss / 20 : 0;
    }

    public double avgIn(Window window, int currentSecond) {
        return ring(window).avg(0, currentSecond, firstSecond);
    }

    public double avgOut(Window window, int currentSecond) {
        return ring(window).avg(1, currentSecond, firstSecond);
    }

    public double avgLoss(Window window, int currentSecond) {
        return ring(window).avg(2, currentSecond, firstSecond);
    }

    public double[] history(Window window, boolean input, int currentSecond) {
        return ring(window).history(input ? 0 : 1, currentSecond);
    }

    private Ring ring(Window window) {
        return switch (window) {
            case MINUTE -> minute;
            case HOUR -> hour;
            case DAY -> day;
        };
    }

    private static final class Ring {

        private final int length;
        private final int span;
        private final double[] in;
        private final double[] out;
        private final double[] loss;
        private final int[] bucket;

        private Ring(int length, int span) {
            this.length = length;
            this.span = span;
            this.in = new double[length];
            this.out = new double[length];
            this.loss = new double[length];
            this.bucket = new int[length];
            java.util.Arrays.fill(bucket, -1);
        }

        private void add(int second, double i, double o, double l) {
            int b = second / span;
            int slot = b % length;
            if (bucket[slot] != b) {
                bucket[slot] = b;
                in[slot] = 0;
                out[slot] = 0;
                loss[slot] = 0;
            }
            in[slot] += i;
            out[slot] += o;
            loss[slot] += l;
        }

        private double[] history(int kind, int currentSecond) {
            int current = currentSecond / span;
            double[] values = kind == 0 ? in : out;
            double[] result = new double[length];
            for (int i = 0; i < length; i++) {
                int b = current - length + 1 + i;
                if (b < 0) continue;
                int slot = b % length;
                if (bucket[slot] == b) result[i] = values[slot];
            }
            return result;
        }

        private double avg(int kind, int currentSecond, int firstSecond) {
            if (firstSecond < 0) return 0;
            int current = currentSecond / span;
            double[] values = kind == 0 ? in : kind == 1 ? out : loss;
            double sum = 0;
            for (int s = 0; s < length; s++) {
                int b = bucket[s];
                if (b >= 0 && b > current - length && b <= current) sum += values[s];
            }
            long seconds = Math.min((long) length * span, Math.max(1, currentSecond - firstSecond));
            return sum / (seconds * 20);
        }
    }
}

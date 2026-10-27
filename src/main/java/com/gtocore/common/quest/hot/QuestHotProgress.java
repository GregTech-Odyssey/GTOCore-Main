package com.gtocore.common.quest.hot;

/**
 * 热加载/热修改的进度：每个任务一张令牌，代码被外部改动时为阻塞（全屏锁定），操作表自己的写入为后台（右上角显示，不影响操作）。
 */
public final class QuestHotProgress {

    public static final String WRITE = "写入代码";
    public static final String READ = "读取代码";

    public static final class Job {

        private final boolean blocking;
        private boolean ended;

        private Job(boolean blocking) {
            this.blocking = blocking;
        }

        public boolean blocking() {
            return blocking;
        }

        public void stage(String phase, String detail, float start, float limit, long expected) {
            QuestHotProgress.stage(phase, detail, start, limit, expected);
        }

        public void end() {
            synchronized (QuestHotProgress.class) {
                if (ended) return;
                ended = true;
                active--;
                if (blocking) blockingActive--;
            }
        }
    }

    private static int active;
    private static int blockingActive;
    private static volatile String phase = "";
    private static volatile String detail = "";
    private static volatile float from;
    private static volatile float to;
    private static volatile long stageStart;
    private static volatile long expectedMillis = 1;

    private QuestHotProgress() {}

    public static synchronized Job begin(boolean blocking) {
        if (active == 0) stage(READ, "准备", 0, 0.05f, 200);
        active++;
        if (blocking) blockingActive++;
        return new Job(blocking);
    }

    private static void stage(String newPhase, String newDetail, float start, float limit, long expected) {
        phase = newPhase;
        detail = newDetail;
        from = start;
        to = limit;
        expectedMillis = Math.max(1, expected);
        stageStart = System.currentTimeMillis();
    }

    public static synchronized boolean active() {
        return active > 0;
    }

    public static synchronized boolean blocking() {
        return blockingActive > 0;
    }

    public static String phase() {
        return phase;
    }

    public static String detail() {
        return detail;
    }

    public static float fraction() {
        double elapsed = System.currentTimeMillis() - stageStart;
        double eased = 1 - Math.exp(-elapsed / expectedMillis);
        return (float) (from + (to - from) * eased);
    }
}

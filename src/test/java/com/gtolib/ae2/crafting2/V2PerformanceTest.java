package com.gtolib.ae2.crafting2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@Tag("performance")
@DisplayName("V2 性能预警（预算约为实测中位数的 10 倍）")
class V2PerformanceTest {

    private static final int WARMUP = 2;
    private static final int RUNS = 5;

    private static long medianMillis(String name, Supplier<Run> body) {
        for (int i = 0; i < WARMUP; i++) body.get().assertComputed();
        var times = new long[RUNS];
        for (int i = 0; i < RUNS; i++) {
            var run = body.get();
            run.assertComputed();
            times[i] = run.nanos;
        }
        Arrays.sort(times);
        long ms = times[RUNS / 2] / 1_000_000;
        System.out.println("[perf] " + name + " median=" + ms + " ms");
        return ms;
    }

    /**
     * 场景：一个样板要 1 万种不同原料 l0…l9999，每种原料各由一张「ri → li」合成，ri 都有库存；下单 1 次。
     * 关注：单个算子输入特别多时的开销（V2 每个输入都线性扫描一遍已排序算子，整体近似平方）。
     * 预算：中位数 ≤ 2000 ms（2026-10-11 本机约 200–270 ms）。
     */
    @Test
    @DisplayName("宽扇入：一个样板要 1 万种原料")
    void wideFanIn() {
        var env = new CraftTestEnv();
        var sb = new StringBuilder();
        for (int i = 0; i < 10_000; i++) {
            env.stock("r" + i, 10);
            env.pattern("r" + i + " -> l" + i);
            if (i > 0) sb.append(" + ");
            sb.append("l").append(i);
        }
        env.pattern(sb + " -> y");
        long ms = medianMillis("wideFanIn", () -> env.craft("y", 1));
        assertTrue(ms <= 2000, "宽扇入 " + ms + " ms 超出预算");
    }

    /**
     * 场景：2000 层直链，下单链尾 1 个。
     * 关注：递归深度与逐层开销。
     * 预算：中位数 ≤ 150 ms（本机约 13 ms）。
     */
    @Test
    @DisplayName("深链：2000 层")
    void deepChain() {
        var env = new CraftTestEnv().stock("k0", 10);
        for (int i = 0; i < 2000; i++) env.pattern("k" + i + " -> k" + (i + 1));
        long ms = medianMillis("deepChain", () -> env.craft("k2000", 1));
        assertTrue(ms <= 150, "深链 " + ms + " ms 超出预算");
    }

    /**
     * 场景：40 层 × 每层 250 个物品共 1 万张样板，每个物品用下一层随机 3 个物品（约 3 万条边，大量共享），最底层是库存原料；下单 1000 个。
     * 关注：大规模共享 DAG 的建图、拓扑排序与传播。
     * 预算：中位数 ≤ 2000 ms（本机约 190–215 ms）。
     */
    @Test
    @DisplayName("分层共享 DAG：40 层 × 250 个（1 万样板），每个用下一层 3 个")
    void layeredDag() {
        var env = layered(40, 250, 3);
        long ms = medianMillis("layeredDag", () -> env.craft("y", 1000));
        assertTrue(ms <= 2000, "分层 DAG " + ms + " ms 超出预算");
    }

    /**
     * 场景：先测一条 10 层小链的耗时，再往网络里加 5 万张与它无关的样板后重测同一个请求。
     * 关注：计算量应只取决于本单可达的子图，而不是网络里样板的总数。
     * 预算：加样板后的耗时不超过空网络耗时 + 50 ms（本机两者都约 0 ms）。
     */
    @Test
    @DisplayName("大网络小请求：5 万个无关样板不应拖慢一条 10 层链")
    void bigNetworkSmallRequest() {
        var env = new CraftTestEnv().stock("c0", 10);
        for (int i = 0; i < 10; i++) env.pattern("c" + i + " -> c" + (i + 1));
        long small = medianMillis("smallRequestAlone", () -> env.craft("c10", 1));
        for (int i = 0; i < 50_000; i++) {
            env.stock("u" + i, 1);
            env.pattern("u" + i + " -> v" + i);
        }
        long big = medianMillis("smallRequestIn50k", () -> env.craft("c10", 1));
        assertTrue(big <= small + 50, "5 万样板网络里的小请求 " + big + " ms，空网络 " + small + " ms");
    }

    /**
     * 场景：20 层 × 100 的分层 DAG，分别下单 1 个和 10^12 个。
     * 关注：耗时与下单数量无关。
     * 预算：10^12 的耗时不超过数量为 1 时的 5 倍（至少放宽到 50 ms）；本机两者都约 11–14 ms。
     */
    @Test
    @DisplayName("数量无关：同一张图 10^12 与 1 耗时相当")
    void amountIndependent() {
        var env = layered(20, 100, 3);
        long one = medianMillis("amount=1", () -> env.craft("y", 1));
        long huge = medianMillis("amount=1e12", () -> env.craft("y", 1_000_000_000_000L));
        assertTrue(huge <= Math.max(50, one * 5), "10^12 耗时 " + huge + " ms，数量 1 时 " + one + " ms");
    }

    /**
     * 场景：CRAFT_LESS，20 层 × 100 的分层 DAG，底层原料每种只有 37 个，下单 100 万个，需要二分找最大可做数。
     * 关注：CRAFT_LESS 每次二分都整图重算，约 20 次全量计算的总开销。
     * 预算：中位数 ≤ 2000 ms（本机约 170–180 ms）。
     */
    @Test
    @DisplayName("CRAFT_LESS 二分：库存只够约三分之一")
    void craftLessBinarySearch() {
        var env = layered(20, 100, 3, 37);
        long ms = medianMillis("craftLess", () -> env.craftLess("y", 1_000_000));
        assertTrue(ms <= 2000, "CRAFT_LESS " + ms + " ms 超出预算");
    }

    private static CraftTestEnv layered(int layers, int width, int fan) {
        return layered(layers, width, fan, Long.MAX_VALUE / 4);
    }

    private static CraftTestEnv layered(int layers, int width, int fan, long rawStock) {
        var env = new CraftTestEnv();
        var rnd = new SplittableRandom(20261011L);
        for (int j = 0; j < width; j++) env.stock("n" + layers + "_" + j, rawStock);
        for (int l = layers - 1; l >= 0; l--) {
            for (int j = 0; j < width; j++) {
                var sb = new StringBuilder();
                var used = new boolean[width];
                for (int f = 0; f < fan; f++) {
                    int k = rnd.nextInt(width);
                    while (used[k]) k = (k + 1) % width;
                    used[k] = true;
                    if (f > 0) sb.append(" + ");
                    sb.append("n").append(l + 1).append('_').append(k);
                }
                env.pattern(sb + " -> n" + l + "_" + j);
            }
        }
        var root = new StringBuilder();
        for (int j = 0; j < width; j++) {
            if (j > 0) root.append(" + ");
            root.append("n0_").append(j);
        }
        env.pattern(root + " -> y");
        return env;
    }
}

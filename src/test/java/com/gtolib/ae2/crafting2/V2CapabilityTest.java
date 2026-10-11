package com.gtolib.ae2.crafting2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("V2 能做到的")
class V2CapabilityTest {

    /**
     * 场景：网络里只有样板「2 x → y」，库存 x×100，下单 y×10。
     * 期望：样板执行 10 次，用掉 x×20，不缺料，按计划回放能产出 10 个 y。
     */
    @Test
    @DisplayName("单样板按需求展开次数与原料")
    void singlePattern() {
        var env = new CraftTestEnv().stock("x", 100);
        var p = env.pattern("2 x -> y");
        var run = env.craft("y", 10).assertOk();
        assertEquals(10, run.times(p));
        assertEquals(20, run.used("x"));
    }

    /**
     * 场景：样板「x → 3 y」每次产出 3 个，库存 x×10，下单 y×10。
     * 期望：次数向上取整为 4 次（产出 12 个），用掉 x×4。
     */
    @Test
    @DisplayName("每次产出多个时次数向上取整")
    void multiOutputRoundsUp() {
        var env = new CraftTestEnv().stock("x", 10);
        var p = env.pattern("x -> 3 y");
        var run = env.craft("y", 10).assertOk();
        assertEquals(4, run.times(p));
        assertEquals(4, run.used("x"));
    }

    /**
     * 场景：样板三个输入格都放 x（「x + x + x → y」），库存 x×100，下单 y×5。
     * 期望：同一物品的多个格子合并为每次 3 个，执行 5 次、用掉 x×15。
     */
    @Test
    @DisplayName("同一样板里同一物品占多格会合并计数")
    void repeatedInputSlotsCondense() {
        var env = new CraftTestEnv().stock("x", 100);
        var p = env.pattern("x + x + x -> y");
        var run = env.craft("y", 5).assertOk();
        assertEquals(5, run.times(p));
        assertEquals(15, run.used("x"));
    }

    /**
     * 场景：三层链「3 x → 2 a」「2 a → b」「b + a → y」，库存 x×1000，下单 y×5。
     * 期望：y 做 5 次、b 做 5 次；a 的总需求 = b 用的 10 + y 用的 5 = 15，做 8 次（产出 16），用掉 x×24。
     */
    @Test
    @DisplayName("多层链逐层展开")
    void chain() {
        var env = new CraftTestEnv().stock("x", 1000);
        var pa = env.pattern("3 x -> 2 a");
        var pb = env.pattern("2 a -> b");
        var py = env.pattern("b + a -> y");
        var run = env.craft("y", 5).assertOk();
        assertEquals(5, run.times(py));
        assertEquals(5, run.times(pb));
        assertEquals(8, run.times(pa));
        assertEquals(24, run.used("x"));
    }

    /**
     * 场景：菱形依赖，y 需要 a 和 b，a 要 2 个 c、b 要 3 个 c，c 由 x 合成；库存 x×1000，下单 y×4。
     * 期望：c 的需求按 4×2 + 4×3 = 20 汇总成一个任务只算一次，c 做 20 次、用掉 x×20。
     */
    @Test
    @DisplayName("菱形依赖：共享中间物的需求汇总后只算一次")
    void diamondAggregatesSharedIntermediate() {
        var env = new CraftTestEnv().stock("x", 1000);
        var pc = env.pattern("x -> c");
        env.pattern("2 c -> a");
        env.pattern("3 c -> b");
        env.pattern("a + b -> y");
        var run = env.craft("y", 4).assertOk();
        assertEquals(20, run.times(pc));
        assertEquals(20, run.used("x"));
    }

    /**
     * 场景：「x → a」「2 a → y」，库存里已有 a×3、x×100，下单 y×5（共需 a×10）。
     * 期望：先用库存的 3 个 a，剩下 7 个再合成，a 的样板执行 7 次。
     */
    @Test
    @DisplayName("中间物先用库存，不够的部分再合成")
    void intermediateUsesStockFirst() {
        var env = new CraftTestEnv().stock("x", 100).stock("a", 3);
        var pa = env.pattern("x -> a");
        env.pattern("2 a -> y");
        var run = env.craft("y", 5).assertOk();
        assertEquals(3, run.used("a"));
        assertEquals(7, run.times(pa));
    }

    /**
     * 场景：「x → y」，库存里已有 y×100，下单 y×5。
     * 期望：下单就是要合成，库存里的 y 不抵扣（与 AE 原版一致），样板执行 5 次、不动用 y。
     */
    @Test
    @DisplayName("下单物品自己的库存不抵扣（下单即合成，与 AE 原版一致）")
    void targetStockIsNotUsed() {
        var env = new CraftTestEnv().stock("x", 100).stock("y", 100);
        var p = env.pattern("x -> y");
        var run = env.craft("y", 5).assertOk();
        assertEquals(5, run.times(p));
        assertEquals(0, run.used("y"));
    }

    /**
     * 场景：样板「2 x + z → y」，库存 x×3、z×100，下单 y×5（共需 x×10、z×5）。
     * 期望：精确报缺 x×7，计划标为模拟；够用的 z×5 照常计入用量。
     */
    @Test
    @DisplayName("缺原料时精确报缺，其它原料照常计入")
    void reportsExactMissing() {
        var env = new CraftTestEnv().stock("x", 3).stock("z", 100);
        env.pattern("2 x + z -> y");
        var run = env.craft("y", 5).assertMissing("x", 7);
        assertTrue(run.plan.simulation());
        assertEquals(5, run.used("z"));
    }

    /**
     * 场景：只有样板「2 a → y」，a 既没有样板也没有库存，下单 y×3。
     * 期望：报缺 a×6。
     */
    @Test
    @DisplayName("中间物没有样板也没有库存时报缺中间物")
    void missingIntermediateWithoutPattern() {
        var env = new CraftTestEnv();
        env.pattern("2 a -> y");
        env.craft("y", 3).assertMissing("a", 6);
    }

    /**
     * 场景：网络里有一对互逆样板「p → q」「q → p」，但下单的 y 只用「x → y」，库存 x×10，下单 y×10。
     * 期望：环不在本单可达范围内，不影响计算，计划正常。
     */
    @Test
    @DisplayName("网络里与本单无关的环不影响计算")
    void unrelatedCycleIsIgnored() {
        var env = new CraftTestEnv().stock("x", 10);
        env.pattern("p -> q");
        env.pattern("q -> p");
        env.pattern("x -> y");
        env.craft("y", 10).assertOk();
    }

    /**
     * 场景：催化剂样板「x + c → y + c」（c 每次原样返还），库存 x×1000、c×1，下单 y×100。
     * 期望：c 只需要 1 份反复使用，执行 100 次、只用掉 c×1，计划可回放。
     */
    @Test
    @DisplayName("催化剂（同键返还）只占一份")
    void catalystNeedsOneCopy() {
        var env = new CraftTestEnv().stock("x", 1000).stock("c", 1);
        var p = env.pattern("x + c -> y + c");
        var run = env.craft("y", 100).assertOk();
        assertEquals(100, run.times(p));
        assertEquals(1, run.used("c"));
    }

    /**
     * 场景：催化剂样板「x + c → y + c」，库存 x×1000，没有 c，下单 y×100。
     * 期望：只报缺 1 份催化剂 c，而不是 100 份。
     */
    @Test
    @DisplayName("催化剂缺失时只报缺一份")
    void missingCatalystReportsOneCopy() {
        var env = new CraftTestEnv().stock("x", 1000);
        env.pattern("x + c -> y + c");
        env.craft("y", 100).assertMissing("c", 1);
    }

    /**
     * 场景：中间物 s 有自增样板「s + x → 2 s」（每次净增 1 个），y 要 4 个 s；库存 x×1000、s×1（种子），下单 y×25（共需 s×100）。
     * 期望：自增样板执行 100 次，只用掉 1 个种子 s，计划可回放。
     */
    @Test
    @DisplayName("中间物自增样板（有种子）可以规划")
    void intermediateSelfIncreaseWithSeed() {
        var env = new CraftTestEnv().stock("x", 1000).stock("s", 1);
        var ps = env.pattern("s + x -> 2 s");
        env.pattern("4 s -> y");
        var run = env.craft("y", 25).assertOk();
        assertEquals(100, run.times(ps));
        assertEquals(1, run.used("s"));
    }

    /**
     * 场景：同上的自增样板「s + x → 2 s」和「4 s → y」，但库存里没有 s，下单 y×25。
     * 期望：如实报缺 1 个种子 s，不假装能做。
     */
    @Test
    @DisplayName("中间物自增样板没有种子时报缺一份种子")
    void intermediateSelfIncreaseWithoutSeed() {
        var env = new CraftTestEnv().stock("x", 1000);
        env.pattern("s + x -> 2 s");
        env.pattern("4 s -> y");
        env.craft("y", 25).assertMissing("s", 1);
    }

    /**
     * 场景：a 有两条路线「x → a」「z → a」，两种原料各有 100 个，「a → y」，下单 y×10。
     * 期望：两条路线合计做满 10 个 a，不缺料（V2 是两边平分，在两边都有料时结果正确）。
     */
    @Test
    @DisplayName("同一物品多个样板且都有料时能凑齐")
    void multiPathWithEnoughStockOnAllRoutes() {
        var env = new CraftTestEnv().stock("x", 100).stock("z", 100);
        var p1 = env.pattern("x -> a");
        var p2 = env.pattern("z -> a");
        env.pattern("a -> y");
        var run = env.craft("y", 10).assertOk();
        assertEquals(10, run.times(p1) + run.times(p2));
    }

    /**
     * 场景：「x → a + junk」会顺带产出副产物 junk，「a → y」，库存 x×10，下单 y×3。
     * 期望：用不到的副产物不影响计划，也不报缺 junk。
     */
    @Test
    @DisplayName("用不到的副产物不报缺")
    void unusedByproductIsHarmless() {
        var env = new CraftTestEnv().stock("x", 10);
        env.pattern("x -> a + junk");
        env.pattern("a -> y");
        var run = env.craft("y", 3).assertOk();
        assertEquals(0, run.missing("junk"));
    }

    /**
     * 场景：样板「e + x → y」，e 没有库存但可由发信器（合成卡）提供，库存 x×10，下单 y×3。
     * 期望：e×3 记入 emittedItems 交给发信器，而不是记成缺料。
     */
    @Test
    @DisplayName("可发信物品记入 emittedItems 而不是缺料")
    void emittableGoesToEmitted() {
        var env = new CraftTestEnv().stock("x", 10).emit("e");
        env.pattern("e + x -> y");
        var run = env.craft("y", 3);
        run.assertComputed();
        assertEquals(3, run.emitted("e"));
        assertTrue(run.plan.missingItems().isEmpty(), run::describe);
    }

    /**
     * 场景：机器下单方式 CRAFT_LESS，「x → y」，库存 x×547，下单 y×1000。
     * 期望：库存不够时做到最大可做数 547，不缺料，计划可回放。
     */
    @Test
    @DisplayName("CRAFT_LESS：库存不够时做到最大可做数")
    void craftLessReturnsMaximum() {
        var env = new CraftTestEnv().stock("x", 547);
        env.pattern("x -> y");
        var run = env.craftLess("y", 1000);
        run.assertContract();
        assertEquals(547, run.plan.finalOutput().amount());
        assertTrue(run.plan.missingItems().isEmpty(), run::describe);
        run.assertReplays(547);
    }

    /**
     * 场景：CRAFT_LESS，「x → y」，完全没有 x，下单 y×1000。
     * 期望：一个都做不了时按原数量 1000 给出缺料计划（报缺 x×1000），让玩家能看到缺什么。
     */
    @Test
    @DisplayName("CRAFT_LESS：一个都做不了时按原数量报缺")
    void craftLessNothingPossible() {
        var env = new CraftTestEnv();
        env.pattern("x -> y");
        var run = env.craftLess("y", 1000).assertMissing("x", 1000);
        assertEquals(1000, run.plan.finalOutput().amount());
    }

    /**
     * 场景：「x → a」「2 a → 3 y」，库存 x×4×10^12，下单 y×3×10^12。
     * 期望：y 样板精确执行 10^12 次、a 样板 2×10^12 次（2^53 以内 double 不丢精度）。
     */
    @Test
    @DisplayName("10^12 级次数精确（2^53 以内）")
    void trillionCraftsExact() {
        long n = 1_000_000_000_000L;
        var env = new CraftTestEnv().stock("x", 4 * n);
        var pa = env.pattern("x -> a");
        var py = env.pattern("2 a -> 3 y");
        var run = env.craft("y", 3 * n).assertOk();
        assertEquals(n, run.times(py));
        assertEquals(2 * n, run.times(pa));
    }

    /**
     * 场景：1000 层的直链「k0 → k1 → … → k1000」，库存 k0×1，下单 k1000×1。
     * 期望：正常算出计划，不栈溢出。
     */
    @Test
    @DisplayName("1000 层深链")
    void deepChain1000() {
        var env = new CraftTestEnv().stock("k0", 1);
        for (int i = 0; i < 1000; i++) env.pattern("k" + i + " -> k" + (i + 1));
        env.craft("k1000", 1).assertOk();
    }
}

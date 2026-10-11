package com.gtolib.ae2.crafting2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("limitation")
@DisplayName("V2 做不到的（预期失败）")
class V2LimitationTest {

    /**
     * 场景：网络里同时有「9 锭 → 块」和「块 → 9 锭」两张互逆样板，库存锭×9，下单块×1。
     * 期望：直接用库存的 9 个锭做 1 个块。
     * 现状：两张样板构成环，V2 整单报「检测到样板循环」并返回错误计划。
     */
    @Test
    @DisplayName("环：互逆样板（锭↔块）在库存够用时应能合成")
    void reversiblePairWithStock() {
        var env = new CraftTestEnv().stock("ingot", 9);
        env.pattern("9 ingot -> block");
        env.pattern("block -> 9 ingot");
        env.craft("block", 1).assertOk();
    }

    /**
     * 场景：目标 y 用「2 锭 → y」，网络里另有锭↔块互逆样板，库存锭×10，下单 y×3。
     * 期望：用 6 个锭直接做，与环无关。
     * 现状：V2 不看库存就把可达范围内的所有样板建图，碰到环整单报错。玩家网络里最常见的就是这种情况。
     */
    @Test
    @DisplayName("环：链上任一层有互逆样板就整单失败")
    void reversiblePairBelowTarget() {
        var env = new CraftTestEnv().stock("ingot", 10);
        env.pattern("9 ingot -> block");
        env.pattern("block -> 9 ingot");
        env.pattern("2 ingot -> y");
        env.craft("y", 3).assertOk();
    }

    /**
     * 场景：末影锭三元环「末影水晶 + x → 末影锭」「末影锭 → 末影等离子」「末影等离子 → 末影水晶」，库存末影水晶×1、x×1，下单末影锭×1。
     * 期望：直接用库存的水晶做 1 个末影锭。
     * 现状：整单报「检测到样板循环，共 3 个节点」（玩家截图里的报错）。
     */
    @Test
    @DisplayName("环：三元环（末影锭→末影等离子→末影水晶→末影锭）在库存够用时应能合成")
    void threeCycleWithStock() {
        var env = new CraftTestEnv().stock("crystal", 1).stock("x", 1);
        env.pattern("crystal + x -> enderium");
        env.pattern("enderium -> plasma");
        env.pattern("plasma -> crystal");
        env.craft("enderium", 1).assertOk();
    }

    /**
     * 场景：工作流体回收，「a + w → y + spent」用掉流体 w 并产出废流体 spent，「spent → w」把废流体再生；库存 a×10、w×1，下单 y×10。
     * 期望：一份 w 循环使用，做 1 次再生 1 次，共 10 轮，不缺料。
     * 现状：spent 只作副产物出现，不被当成来源，V2 报缺 spent×9。
     */
    @Test
    @DisplayName("环：工作流体回收（用过的流体再生）应只需一份初始流体")
    void workingFluidLoop() {
        var env = new CraftTestEnv().stock("a", 10).stock("w", 1);
        env.pattern("a + w -> y + spent");
        env.pattern("spent -> w");
        env.craft("y", 10).assertOk();
    }

    /**
     * 场景：「x → a + z」一次同时产出 a 和副产物 z，「a + z → y」正好需要这两样；库存 x×1，下单 y×1。
     * 期望：一次合成同时满足 a 和 z，不缺料。
     * 现状：副产物不能满足同一计划里的其它需求，V2 报缺 z×1。
     */
    @Test
    @DisplayName("副产物应能满足同一计划内的其它需求")
    void byproductFeedsSibling() {
        var env = new CraftTestEnv().stock("x", 1);
        env.pattern("x -> a + z");
        env.pattern("a + z -> y");
        env.craft("y", 1).assertOk();
    }

    /**
     * 场景：a 有两条路线「x → a」「z → a」，库存 x×5、z×3，下单 a×8。
     * 期望：按库存分配为 5 + 3，正好做满。
     * 现状：V2 写死 1:1 平分成 4 + 4，z 路线缺 1。
     */
    @Test
    @DisplayName("多路径：应按各路线库存分配（5+3 凑 8）")
    void multiPathSplitsByStock() {
        var env = new CraftTestEnv().stock("x", 5).stock("z", 3);
        env.pattern("x -> a");
        env.pattern("z -> a");
        env.craft("a", 8).assertOk();
    }

    /**
     * 场景：同样两条路线，只有 x×10、完全没有 z，下单 a×10。
     * 期望：全部走 x 路线。
     * 现状：照样平分 5 + 5，报缺 z×5。
     */
    @Test
    @DisplayName("多路径：一条路线没料时应全走另一条")
    void multiPathFallsBack() {
        var env = new CraftTestEnv().stock("x", 10);
        env.pattern("x -> a");
        env.pattern("z -> a");
        env.craft("a", 10).assertOk();
    }

    /**
     * 场景：两条路线，先注册的「x → a」优先级更高，两种原料各 100 个，下单 a×10。
     * 期望：高优先级路线够用，10 次全部走它，低优先级 0 次。
     * 现状：V2 丢掉了优先级顺序，平分 5 + 5。
     */
    @Test
    @DisplayName("多路径：高优先级路线够用时只用它")
    void multiPathHonorsPriority() {
        var env = new CraftTestEnv().stock("x", 100).stock("z", 100);
        var high = env.pattern("x -> a");
        var low = env.pattern("z -> a");
        var run = env.craft("a", 10).assertOk();
        assertEquals(10, run.times(high), run::describe);
        assertEquals(0, run.times(low), run::describe);
    }

    /**
     * 场景：机器下单 CRAFT_LESS，两条路线库存 x×5、z×3，下单 a×10。
     * 期望：最多能做 8 个，返回 8。
     * 现状：平分导致二分出的最大值只有 6 或 7（取决于哈希顺序）。
     */
    @Test
    @DisplayName("多路径：CRAFT_LESS 应做满 8")
    void craftLessMultiPath() {
        var env = new CraftTestEnv().stock("x", 5).stock("z", 3);
        env.pattern("x -> a");
        env.pattern("z -> a");
        var run = env.craftLess("a", 10);
        run.assertHonest();
        assertEquals(8, run.plan.finalOutput().amount(), run::describe);
    }

    /**
     * 场景：重复 30 次新建同样的网络（两条路线、各 100 个原料），每次都下单 a×7（奇数，平分有余数）。
     * 期望：每次的分配完全相同。
     * 现状：余数分给哪条路线取决于对象的身份哈希顺序，运行之间在 3/4 和 4/3 之间变化。
     */
    @Test
    @DisplayName("确定性：相同网络与请求每次得到相同计划")
    void deterministicSplit() {
        String first = null;
        for (int i = 0; i < 30; i++) {
            var env = new CraftTestEnv().stock("x", 100).stock("z", 100);
            var p1 = env.pattern("x -> a");
            var p2 = env.pattern("z -> a");
            var run = env.craft("a", 7).assertComputed();
            String split = run.times(p1) + "/" + run.times(p2);
            if (first == null) first = split;
            assertEquals(first, split, "两条路线的分配在不同运行间变化");
        }
    }

    /**
     * 场景：「e + x → y」，e 由发信器提供，库存 x×10，下单 y×3。
     * 期望：计划没有缺料，不应标为模拟。
     * 现状：缺料移进 emittedItems 后模拟标记没有复位，计划仍是模拟；机器下单会因此被拒。
     */
    @Test
    @DisplayName("发信器：只靠发信器满足的计划不应标为模拟（否则机器下单被拒）")
    void emitterOnlyPlanIsNotSimulation() {
        var env = new CraftTestEnv().stock("x", 10).emit("e");
        env.pattern("e + x -> y");
        var run = env.craft("y", 3).assertComputed();
        assertFalse(run.plan.simulation(), run::describe);
    }

    /**
     * 场景：自增样板「s + x → 3 s」（每次净增 2），「101 s → y」；库存没有 s，下单 y×1。
     * 期望：没有种子就报缺 1 个 s（或给出能执行的计划）。
     * 现状：101 向上取整做 51 次会多出 1 个 s，V2 把这个未来才有的产物当成种子，计划声称不缺料，usedItems 里却用了库存中不存在的 s。
     */
    @Test
    @DisplayName("自增：没有种子时不应拿本单取整多出的产物充当种子")
    void selfIncreaseSeedFromRoundingSurplus() {
        var env = new CraftTestEnv().stock("x", 1000);
        env.pattern("s + x -> 3 s");
        env.pattern("101 s -> y");
        env.craft("y", 1).assertHonest();
    }

    /**
     * 场景：自增样板「s + x → 2 s」，库存 s×1（种子）、x×1000，直接下单 s×100。
     * 期望：用 1 个种子滚出 100 个。
     * 现状：V2 禁止下单物品本身是自增产物，直接拒绝。
     */
    @Test
    @DisplayName("自增：直接下单自增物品（有种子）应能合成")
    void requestSelfIncreasingItem() {
        var env = new CraftTestEnv().stock("x", 1000).stock("s", 1);
        env.pattern("s + x -> 2 s");
        env.craft("s", 100).assertOk();
    }

    /**
     * 场景：网络里有一张净产出为 0 的样板「a → a」，以及「a → y」，没有 a，下单 y×1。
     * 期望：忽略无意义的样板，正常报缺 a×1。
     * 现状：建图时抛 ComputingComponentBuildFailedException，计算直接以异常结束。
     */
    @Test
    @DisplayName("健壮性：净产出为 0 的样板（a -> a）不应让计算抛异常")
    void zeroNetPatternDoesNotThrow() {
        var env = new CraftTestEnv();
        env.pattern("a -> a");
        env.pattern("a -> y");
        var run = env.craft("y", 1);
        assertFalse(run.threw(), run::describe);
        run.assertMissing("a", 1);
    }

    /**
     * 场景：「x → y」，库存 x×2^61，下单 y×(2^60 + 1)。
     * 期望：精确执行 2^60 + 1 次。
     * 现状：次数用 double 计算，超过 2^53 丢精度，只做 2^60 次，少 1 个。
     */
    @Test
    @DisplayName("数量：超过 2^53 的次数仍应精确")
    void exactBeyondDoublePrecision() {
        long amount = (1L << 60) + 1;
        var env = new CraftTestEnv().stock("x", 1L << 61);
        var p = env.pattern("x -> y");
        var run = env.craft("y", amount).assertOk();
        assertEquals(amount, run.times(p));
    }

    /**
     * 场景：「10 亿 x → y」，库存 x 为 Long.MAX，下单 y×10^10（总需求 10^19 超出 long）。
     * 期望：报「数量过大」或给出真实缺料，不能假装成功。
     * 现状：乘法溢出成负数后这项需求被静默丢掉，计划显示不缺料、也不抽 x，实际无法执行。
     */
    @Test
    @DisplayName("数量：乘法溢出不应变成“不缺料”的假成功")
    void overflowIsNotSilentSuccess() {
        var env = new CraftTestEnv().stock("x", Long.MAX_VALUE);
        env.pattern("1000000000 x -> y");
        var run = env.craft("y", 10_000_000_000L);
        assertFalse(run.threw(), run::describe);
        if (!run.rejected()) run.assertHonest();
    }

    /**
     * 场景：20000 层的直链「k0 → … → k20000」，库存 k0×1，下单 k20000×1。
     * 期望：正常算出计划。
     * 现状：求强连通分量用递归，栈溢出。
     */
    @Test
    @DisplayName("规模：20000 层深链不应栈溢出")
    void deepChain20000() {
        var env = new CraftTestEnv().stock("k0", 1);
        for (int i = 0; i < 20_000; i++) env.pattern("k" + i + " -> k" + (i + 1));
        env.craft("k20000", 1).assertOk();
    }
}

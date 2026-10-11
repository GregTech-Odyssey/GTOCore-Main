package com.gtolib.ae2.crafting2;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 一次 executeV2 的结果与断言；replay 按"同键返还只需在场"的串行语义回放计划，检查计划真能执行出目标数量。
 */
final class Run {

    final CraftTestEnv env;
    final AEKey target;
    final long amount;
    final ICraftingPlan plan;
    final Throwable error;
    final long nanos;

    Run(CraftTestEnv env, AEKey target, long amount, ICraftingPlan plan, Throwable error, long nanos) {
        this.env = env;
        this.target = target;
        this.amount = amount;
        this.plan = plan;
        this.error = error;
        this.nanos = nanos;
    }

    boolean threw() {
        return error != null;
    }

    boolean rejected() {
        return plan != null && plan.patternTimes() == Object2LongMaps.EMPTY_MAP;
    }

    long times(CraftTestEnv.TestPattern p) {
        return plan.patternTimes().getLong(p);
    }

    long missing(String id) {
        return plan.missingItems().get(env.key(id));
    }

    long used(String id) {
        return plan.usedItems().get(env.key(id));
    }

    long emitted(String id) {
        return plan.emittedItems().get(env.key(id));
    }

    String describe() {
        if (error != null) return "threw " + error;
        if (rejected()) return "rejected (error plan)";
        var sb = new StringBuilder();
        sb.append("final=").append(plan.finalOutput().amount()).append(" simulation=").append(plan.simulation());
        sb.append(" times={");
        for (var e : plan.patternTimes().object2LongEntrySet()) sb.append(e.getKey()).append(": ").append(e.getLongValue()).append("; ");
        sb.append("} used=").append(counter(plan.usedItems())).append(" missing=").append(counter(plan.missingItems()));
        sb.append(" emitted=").append(counter(plan.emittedItems()));
        return sb.toString();
    }

    private static String counter(appeng.api.stacks.KeyCounter c) {
        var sb = new StringBuilder("{");
        for (var e : c) sb.append(e.getKey()).append('=').append(e.getLongValue()).append(' ');
        return sb.append('}').toString();
    }

    Run assertComputed() {
        assertNull(error, () -> "计算抛异常: " + error);
        assertNotNull(plan);
        assertFalse(rejected(), "计算被拒（返回错误计划）");
        return this;
    }

    Run assertContract() {
        assertComputed();
        assertInstanceOf(Object2LongOpenHashMap.class, plan.patternTimes(), "patternTimes 必须是 Object2LongOpenHashMap（ExecutingCraftingJob 会强转）");
        for (var e : plan.patternTimes().object2LongEntrySet()) assertTrue(e.getLongValue() > 0, () -> "次数 ≤0: " + e.getKey());
        for (var e : plan.usedItems()) {
            assertTrue(e.getLongValue() > 0, () -> "usedItems 非正: " + e.getKey());
            assertTrue(e.getLongValue() <= env.stock.get(e.getKey()), () -> "usedItems 超过库存: " + e.getKey() + " used=" + e.getLongValue() + " stock=" + env.stock.get(e.getKey()));
        }
        for (var e : plan.missingItems()) assertTrue(e.getLongValue() > 0, () -> "missingItems 非正: " + e.getKey());
        for (var e : plan.emittedItems()) assertTrue(env.emittable.contains(e.getKey()), () -> "发信了不可发信的键: " + e.getKey());
        assertEquals(!plan.missingItems().isEmpty(), plan.simulation(), () -> "simulation 应与是否缺料一致: " + describe());
        return this;
    }

    Run assertOk() {
        assertContract();
        assertTrue(plan.missingItems().isEmpty(), () -> "不应缺料: " + describe());
        assertFalse(plan.simulation(), () -> "不应是模拟计划: " + describe());
        assertEquals(amount, plan.finalOutput().amount(), "最终数量");
        assertReplays(amount);
        return this;
    }

    Run assertHonest() {
        assertContract();
        if (plan.missingItems().isEmpty()) assertReplays(plan.finalOutput().amount());
        return this;
    }

    Run assertMissing(String id, long expected) {
        assertContract();
        assertEquals(expected, missing(id), () -> "缺 " + id + ": " + describe());
        return this;
    }

    Run assertReplays(long expectedTarget) {
        var inv = new Reference2LongOpenHashMap<AEKey>();
        for (var e : plan.usedItems()) inv.addTo(e.getKey(), e.getLongValue());
        for (var e : plan.emittedItems()) inv.addTo(e.getKey(), e.getLongValue());
        var remaining = new Object2LongOpenHashMap<IPatternDetails>(plan.patternTimes());
        var order = new ObjectArrayList<>(remaining.keySet());
        for (int round = 0; round < 100_000; round++) {
            boolean progressed = false;
            for (var p : order) {
                long left = remaining.getLong(p);
                if (left <= 0) continue;
                long k = fireable(p, inv, left);
                if (k <= 0) continue;
                fire(p, inv, k);
                remaining.put(p, left - k);
                progressed = true;
            }
            if (!progressed) break;
        }
        for (var p : order) assertEquals(0, remaining.getLong(p), () -> "回放卡住，样板未执行完: " + p + " 剩 " + remaining.getLong(p) + "，" + describe());
        long produced = inv.getLong(target);
        assertTrue(produced >= expectedTarget, () -> "回放得到 " + target + " ×" + produced + "，少于 " + expectedTarget + "，" + describe());
        for (var e : inv.reference2LongEntrySet()) assertTrue(e.getLongValue() >= 0, () -> "回放出现负库存: " + e.getKey());
        return this;
    }

    private static long fireable(IPatternDetails p, Reference2LongOpenHashMap<AEKey> inv, long max) {
        long k = max;
        for (var in : p.getInputs()) {
            AEKey key = in.getPossibleInputs()[0].what();
            long a = in.getMultiplier();
            long r = returned(p, key);
            long have = inv.getLong(key);
            if (have < a) return 0;
            if (r >= a) continue;
            k = Math.min(k, (have - a) / (a - r) + 1);
        }
        return k;
    }

    private static void fire(IPatternDetails p, Reference2LongOpenHashMap<AEKey> inv, long k) {
        for (var in : p.getInputs()) inv.addTo(in.getPossibleInputs()[0].what(), -Math.multiplyExact(k, in.getMultiplier()));
        for (GenericStack out : p.getOutputs()) inv.addTo(out.what(), Math.multiplyExact(k, out.amount()));
    }

    private static long returned(IPatternDetails p, AEKey key) {
        long r = 0;
        for (GenericStack out : p.getOutputs()) if (out.what() == key) r += out.amount();
        return r;
    }
}

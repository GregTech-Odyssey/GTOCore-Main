package com.gtocore.gametest.crafting;

import appeng.api.stacks.AEKeyObjectMap;
import appeng.api.stacks.AEKeySet;
import com.gtolib.ae2.crafting2.logic.MainLogic;
import com.gtolib.ae2.crafting2.utils.DisplayLevel;

import net.minecraft.server.level.ServerLevel;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.CraftingPlan;
import appeng.me.service.helpers.NetworkCraftingProviders;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

/**
 * 游戏内的 V2 计算环境：真实物品键与样板、真实世界，网格与合成服务用代理，只提供 V2 需要的样板查询与可发信集合。
 */
final class GameCraftEnv {

    final ObjectArrayList<IPatternDetails> patterns = new ObjectArrayList<>();
    final AEKeyObjectMap<AEKey, ObjectArrayList<IPatternDetails>> producers = new AEKeyObjectMap<>();
    final KeyCounter stock = new KeyCounter();
    final AEKeySet<AEKey> emittable = new AEKeySet<>();
    private final IGrid grid;

    GameCraftEnv(ServerLevel level) {
        var providers = new NetworkCraftingProviders() {

            @Override
            public Set<AEKey> getEmittableKeys() {
                return emittable;
            }

            @Override
            public Collection<IPatternDetails> getCraftingFor(AEKey key) {
                return craftingFor(key);
            }
        };
        ICraftingService service = proxy(ICraftingService.class, (p, m, a) -> switch (m.getName()) {
            case "getCraftingFor" -> craftingFor((AEKey) a[0]);
            case "getCraftingProviders" -> providers;
            default -> null;
        });
        IGridNode node = proxy(IGridNode.class, (p, m, a) -> m.getName().equals("getLevel") ? level : null);
        grid = proxy(IGrid.class, (p, m, a) -> switch (m.getName()) {
            case "getService", "getCraftingService" -> service;
            case "getPivot" -> node;
            default -> null;
        });
    }

    void add(IPatternDetails pattern) {
        patterns.add(pattern);
        producers.computeIfAbsent(pattern.getPrimaryOutput().what(), k -> new ObjectArrayList<>()).add(pattern);
    }

    Collection<IPatternDetails> craftingFor(AEKey key) {
        var list = producers.get(key);
        return list == null ? Collections.emptyList() : list;
    }

    Outcome run(AEKey target, long amount, CalculationStrategy strategy) {
        ICraftingSimulationRequester requester = proxy(ICraftingSimulationRequester.class, (p, m, a) -> null);
        try {
            return new Outcome(MainLogic.executeV2(grid, stock, requester, target, amount, strategy, () -> DisplayLevel.NONE), null);
        } catch (RuntimeException e) {
            return new Outcome(null, e);
        }
    }

    record Outcome(ICraftingPlan plan, RuntimeException error) {

        boolean rejected() {
            return plan != null && plan.patternTimes() == Object2LongMaps.EMPTY_MAP;
        }

        String describe(GameCraftEnv env) {
            if (error != null) return "THREW " + error.getClass().getSimpleName();
            if (rejected()) return "REJECTED";
            var times = new TreeMap<Integer, Long>();
            for (var e : plan.patternTimes().object2LongEntrySet()) times.put(env.patterns.indexOf(e.getKey()), e.getLongValue());
            var alloc = new TreeMap<String, Long>();
            var allocations = ((CraftingPlan) plan).getGtocore$allocations();
            if (allocations != null) {
                for (var e : allocations.reference2ObjectEntrySet()) {
                    for (var p : e.getValue().object2LongEntrySet()) alloc.put(e.getKey() + "@" + env.patterns.indexOf(p.getKey()), p.getLongValue());
                }
            }
            return "f=" + plan.finalOutput().amount() + " s=" + plan.simulation() + " b=" + plan.bytes() + " t=" + times + " u=" + counter(plan.usedItems()) +
                    " m=" + counter(plan.missingItems()) + " e=" + counter(plan.emittedItems()) + " a=" + alloc;
        }

        private static TreeMap<String, Long> counter(KeyCounter counter) {
            var map = new TreeMap<String, Long>();
            for (var e : counter) map.put(e.getKey().toString(), e.getLongValue());
            return map;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(GameCraftEnv.class.getClassLoader(), new Class<?>[] { type }, (p, m, a) -> {
            if (m.getDeclaringClass() == Object.class) {
                return switch (m.getName()) {
                    case "hashCode" -> System.identityHashCode(p);
                    case "equals" -> p == a[0];
                    default -> type.getSimpleName() + "Proxy";
                };
            }
            return handler.invoke(p, m, a);
        });
    }
}

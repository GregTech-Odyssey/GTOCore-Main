package com.gtolib.ae2.crafting2;

import com.gtolib.ae2.crafting2.logic.MainLogic;
import com.gtolib.ae2.crafting2.utils.DisplayLevel;
import com.gtolib.ae2.crafting2.utils.PerfLogger;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.me.service.helpers.NetworkCraftingProviders;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 不加载 MC/mixin 的 V2 测试环境：假键、假处理样板、假网格，直接调 MainLogic.executeV2。
 * 样板写法 "2 a + c -> 3 b + c"，第一个产物是主产物（与 AE 一致，只按主产物建索引）。
 */
final class CraftTestEnv {

    private static final Unsafe UNSAFE = unsafe();
    private static ServerLevel level;

    final Object2ObjectOpenHashMap<String, TestKey> keys = new Object2ObjectOpenHashMap<>();
    final Reference2ObjectOpenHashMap<AEKey, ObjectArrayList<IPatternDetails>> producers = new Reference2ObjectOpenHashMap<>();
    final ObjectArrayList<TestPattern> patterns = new ObjectArrayList<>();
    final KeyCounter stock = new KeyCounter();
    final ReferenceOpenHashSet<AEKey> emittable = new ReferenceOpenHashSet<>();
    private final IGrid grid;

    CraftTestEnv() {
        bootstrap();
        var service = craftingService();
        var node = proxy(IGridNode.class, (p, m, a) -> m.getName().equals("getLevel") ? level : null);
        grid = proxy(IGrid.class, (p, m, a) -> switch (m.getName()) {
            case "getService" -> service;
            case "getCraftingService" -> service;
            case "getPivot" -> node;
            default -> null;
        });
    }

    TestKey key(String id) {
        return keys.computeIfAbsent(id, TestKey::new);
    }

    CraftTestEnv stock(String id, long amount) {
        stock.add(key(id), amount);
        return this;
    }

    CraftTestEnv emit(String id) {
        emittable.add(key(id));
        return this;
    }

    TestPattern pattern(String spec) {
        int arrow = spec.indexOf("->");
        var p = TestPattern.create(stacks(spec.substring(0, arrow)), stacks(spec.substring(arrow + 2)));
        patterns.add(p);
        producers.computeIfAbsent(p.out[0].what(), k -> new ObjectArrayList<>()).add(p);
        return p;
    }

    private GenericStack[] stacks(String side) {
        var parts = side.split("\\+");
        var out = new GenericStack[parts.length];
        for (int i = 0; i < parts.length; i++) {
            var tokens = parts[i].trim().split("\\s+");
            out[i] = tokens.length == 1 ? new GenericStack(key(tokens[0]), 1) : new GenericStack(key(tokens[1]), Long.parseLong(tokens[0]));
        }
        return out;
    }

    Run craft(String target, long amount) {
        return run(key(target), amount, CalculationStrategy.REPORT_MISSING_ITEMS);
    }

    Run craftLess(String target, long amount) {
        return run(key(target), amount, CalculationStrategy.CRAFT_LESS);
    }

    Run run(AEKey target, long amount, CalculationStrategy strategy) {
        ICraftingSimulationRequester requester = proxy(ICraftingSimulationRequester.class, (p, m, a) -> null);
        long start = System.nanoTime();
        try {
            ICraftingPlan plan = MainLogic.executeV2(grid, stock, requester, target, amount, strategy, () -> DisplayLevel.NONE);
            return new Run(this, target, amount, plan, null, System.nanoTime() - start);
        } catch (Throwable t) {
            return new Run(this, target, amount, null, t, System.nanoTime() - start);
        }
    }

    private ICraftingService craftingService() {
        var providers = allocate(FakeProviders.class);
        providers.env = this;
        return proxy(ICraftingService.class, (p, m, a) -> switch (m.getName()) {
            case "getCraftingFor" -> craftingFor((AEKey) a[0]);
            case "getCraftingProviders" -> providers;
            case "canEmitFor" -> emittable.contains((AEKey) a[0]);
            default -> null;
        });
    }

    Collection<IPatternDetails> craftingFor(AEKey key) {
        var list = producers.get(key);
        return list == null ? Collections.emptyList() : list;
    }

    static final class FakeProviders extends NetworkCraftingProviders {

        CraftTestEnv env;

        @Override
        public Set<AEKey> getEmittableKeys() {
            return env.emittable;
        }

        @Override
        public boolean canEmitFor(AEKey key) {
            return env.emittable.contains(key);
        }

        @Override
        public Collection<IPatternDetails> getCraftingFor(AEKey key) {
            return env.craftingFor(key);
        }
    }

    static final class TestKey extends AEKey {

        final String id;

        TestKey(String id) {
            this.id = id;
        }

        @Override
        public AEKeyType getType() {
            return null;
        }

        @Override
        public AEKey dropSecondary() {
            return this;
        }

        @Override
        public CompoundTag toTag() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getPrimaryKey() {
            return this;
        }

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation("test", id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_"));
        }

        @Override
        public void writeToPacket(FriendlyByteBuf data) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Component computeDisplayName() {
            return Component.literal(id);
        }

        @Override
        public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {}

        @Override
        public String toString() {
            return id;
        }
    }

    static final class TestPattern extends AEProcessingPattern {

        GenericStack[] in;
        GenericStack[] out;
        IInput[] inputs;

        TestPattern() {
            super(null);
        }

        static TestPattern create(GenericStack[] in, GenericStack[] out) {
            var p = allocate(TestPattern.class);
            p.in = in;
            p.out = out;
            p.inputs = new IInput[in.length];
            for (int i = 0; i < in.length; i++) {
                var stack = new GenericStack[] { new GenericStack(in[i].what(), 1) };
                long multiplier = in[i].amount();
                p.inputs[i] = new IInput() {

                    @Override
                    public GenericStack[] getPossibleInputs() {
                        return stack;
                    }

                    @Override
                    public long getMultiplier() {
                        return multiplier;
                    }

                    @Override
                    public boolean isValid(AEKey input, Level level) {
                        return input == stack[0].what();
                    }

                    @Override
                    public AEKey getRemainingKey(AEKey template) {
                        return null;
                    }
                };
            }
            return p;
        }

        @Override
        public GenericStack[] getSparseInputs() {
            return in;
        }

        @Override
        public GenericStack[] getSparseOutputs() {
            return out;
        }

        @Override
        public GenericStack[] getOutputs() {
            return out;
        }

        @Override
        public IInput[] getInputs() {
            return inputs;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public String toString() {
            var sb = new StringBuilder();
            for (var s : in) sb.append(s.amount()).append(' ').append(s.what()).append(" + ");
            sb.setLength(sb.length() - 3);
            sb.append(" -> ");
            for (var s : out) sb.append(s.amount()).append(' ').append(s.what()).append(" + ");
            sb.setLength(sb.length() - 3);
            return sb.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(CraftTestEnv.class.getClassLoader(), new Class<?>[] { type }, (p, m, a) -> {
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

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) {
        try {
            return (T) UNSAFE.allocateInstance(type);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Unsafe unsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static synchronized void bootstrap() {
        if (level != null) return;
        PerfLogger.setDestroy(false);
        try {
            Field bootstrapped = net.minecraft.server.Bootstrap.class.getDeclaredField("isBootstrapped");
            bootstrapped.setAccessible(true);
            bootstrapped.setBoolean(null, true);
            Class.forName("net.minecraft.core.registries.BuiltInRegistries", true, CraftTestEnv.class.getClassLoader());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        level = allocate(ServerLevel.class);
    }
}

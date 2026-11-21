package com.gtocore.gametest.crafting;

import com.gtocore.mixin.ae2.crafting.NetworkCraftingProvidersAccessor;

import com.gtolib.api.wireless.energy.MachineMapCrafting;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.me.service.CraftingService;
import appeng.me.service.helpers.NetworkCraftingProviders;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 在真实服务端里用整合包的真实配方编码合成/切石样板，经 GTO 的解码路径后交给 V2 计算，锚定合成样板相关的行为。
 */
@GameTestHolder("gtocore")
@PrefixGameTestTemplate(false)
public final class CraftingV2GameTests {

    private static final Logger LOGGER = LoggerFactory.getLogger("gtocore-gametest");
    private static final long REQUEST = 1000;

    @GameTest(template = "empty")
    public static void mapCraftingRevisionIsGridLocal(GameTestHelper helper) {
        var storage = GameCraftEnv.proxy(IStorageService.class, (p, m, a) -> null);
        var energy = GameCraftEnv.proxy(IEnergyService.class, (p, m, a) -> null);
        var services = new CraftingService[2];
        var firstGrid = GameCraftEnv.proxy(IGrid.class, (p, m, a) -> services[0]);
        var secondGrid = GameCraftEnv.proxy(IGrid.class, (p, m, a) -> services[1]);
        services[0] = new CraftingService(firstGrid, storage, energy);
        services[1] = new CraftingService(secondGrid, storage, energy);

        var first = (MachineMapCrafting.Updates) services[0];
        var second = (MachineMapCrafting.Updates) services[1];
        long initialFirst = first.gto$getMapCraftingRevision();
        long initialSecond = second.gto$getMapCraftingRevision();
        MachineMapCrafting.markChanged(firstGrid);
        MachineMapCrafting.markChanged(firstGrid);
        if (first.gto$getMapCraftingRevision() != initialFirst + 2 || second.gto$getMapCraftingRevision() != initialSecond) {
            helper.fail("crafting changes must advance only the revision of their own grid");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mapPatternLookupPreservesProviderRotation(GameTestHelper helper) {
        var encoded = PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[] { new GenericStack(AEItemKey.of(Items.IRON_ORE), 1) },
                new GenericStack[] { new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1) });
        var pattern = decode(encoded, helper.getLevel());
        if (pattern == null) {
            helper.fail("could not decode processing pattern");
            return;
        }
        var patterns = List.of(pattern);
        InvocationHandler offerPattern = (p, m, a) -> switch (m.getName()) {
            case "getAvailablePatterns" -> patterns;
            case "getEmitableItems" -> Set.of();
            case "getPatternPriority" -> 0;
            default -> throw new UnsupportedOperationException(m.getName());
        };
        var first = GameCraftEnv.proxy(ICraftingProvider.class, offerPattern);
        var second = GameCraftEnv.proxy(ICraftingProvider.class, offerPattern);
        var firstNode = GameCraftEnv.proxy(IGridNode.class, (p, m, a) -> first);
        var secondNode = GameCraftEnv.proxy(IGridNode.class, (p, m, a) -> second);
        var registry = new NetworkCraftingProviders();
        registry.addProvider(firstNode);
        registry.addProvider(secondNode);

        var states = ((NetworkCraftingProvidersAccessor) registry).gto$getProviderStates();
        var state = (NetworkCraftingProvidersAccessor.ProviderStateAccessor) states.get(secondNode);
        if (state == null || !patterns.equals(state.gto$getMountedPatterns()) || registry.getMediums(pattern).iterator().next() != first) {
            helper.fail("reading mounted patterns changed which provider is selected first");
            return;
        }
        state.gto$getMountedPatterns();
        if (registry.getMediums(pattern).iterator().next() != second) {
            helper.fail("reading mounted patterns advanced the round-robin cursor");
            return;
        }
        registry.removeProvider(secondNode);
        if (states.containsKey(secondNode)) {
            helper.fail("removed provider is still present in the mounted pattern lookup");
            return;
        }
        helper.succeed();
    }

    /**
     * 场景：整合包里每一张工作台配方，按"不允许替换"和"允许替换＋流体替换"各编码一次样板并解码。
     * 解码成功的样板单独组成网络，分别在无库存和有库存时计算 1 个、1000 个和 CRAFT_LESS 1000 个，结果写入 craft-v2-gametest-dump.txt。
     * 期望：不允许替换、且不以产物为输入的样板，原料给足时计划不缺料，次数 = ⌈1000 / 每次产出⌉，每种原料用量 = 每次用量 × 次数。
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void craftingRecipeCorpus(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipes = new ObjectArrayList<>(level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING));
        recipes.sort(Comparator.comparing(r -> r.getId().toString()));
        var lines = new ObjectArrayList<String>();
        var failures = new ObjectArrayList<String>();
        int accepted = 0;
        int rejected = 0;
        int checked = 0;
        for (var recipe : recipes) {
            var inputs = gridOf(recipe);
            var output = recipe.getResultItem(level.registryAccess());
            if (inputs == null || output.isEmpty()) continue;
            for (boolean substitute : new boolean[] { false, true }) {
                String id = recipe.getId() + (substitute ? "#substitute" : "");
                var pattern = decode(PatternDetailsHelper.encodeCraftingPattern(recipe, inputs, output, substitute, substitute), level);
                if (pattern == null) {
                    rejected++;
                    lines.add(id + "|DECODE_REJECTED");
                    continue;
                }
                accepted++;
                dumpScenarios(level, id, pattern, lines);
                if (!substitute && !consumesOwnOutput(pattern)) {
                    checked++;
                    var problem = checkPlainPattern(level, pattern);
                    if (problem != null) failures.add(id + ": " + problem);
                }
            }
        }
        write("craft-v2-gametest-dump.txt", lines);
        LOGGER.info("crafting corpus: accepted {}, rejected {}, checked {}, failures {}", accepted, rejected, checked, failures.size());
        if (!failures.isEmpty()) helper.fail(failures.size() + " 张样板计算不符：" + String.join(" | ", failures.subList(0, Math.min(10, failures.size()))));
        helper.succeed();
    }

    /**
     * 场景：整合包里每一张切石机配方编码成切石样板，原料给足时计算 1000 个。
     * 期望：计划不缺料，次数 = ⌈1000 / 每次产出⌉，原料用量 = 次数。
     */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void stonecuttingRecipeCorpus(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipes = new ObjectArrayList<>(level.getRecipeManager().getAllRecipesFor(RecipeType.STONECUTTING));
        recipes.sort(Comparator.comparing(r -> r.getId().toString()));
        var lines = new ObjectArrayList<String>();
        var failures = new ObjectArrayList<String>();
        for (var recipe : recipes) {
            var items = recipe.getIngredients().get(0).getItems();
            var output = recipe.getResultItem(level.registryAccess());
            if (items.length == 0 || output.isEmpty()) continue;
            var pattern = decode(PatternDetailsHelper.encodeStonecuttingPattern(recipe, AEItemKey.of(items[0]), AEItemKey.of(output), false), level);
            if (pattern == null) {
                lines.add(recipe.getId() + "|DECODE_REJECTED");
                continue;
            }
            dumpScenarios(level, recipe.getId().toString(), pattern, lines);
            if (consumesOwnOutput(pattern)) continue;
            var problem = checkPlainPattern(level, pattern);
            if (problem != null) failures.add(recipe.getId() + ": " + problem);
        }
        write("craft-v2-gametest-stonecutting-dump.txt", lines);
        if (!failures.isEmpty()) helper.fail(failures.size() + " 张切石样板计算不符：" + String.join(" | ", failures.subList(0, Math.min(10, failures.size()))));
        helper.succeed();
    }

    private static void dumpScenarios(ServerLevel level, String id, IPatternDetails pattern, List<String> lines) {
        var target = pattern.getPrimaryOutput().what();
        var env = new GameCraftEnv(level);
        env.add(pattern);
        lines.add(id + "|empty x1|" + env.run(target, 1, CalculationStrategy.REPORT_MISSING_ITEMS).describe(env));
        lines.add(id + "|empty x" + REQUEST + "|" + env.run(target, REQUEST, CalculationStrategy.REPORT_MISSING_ITEMS).describe(env));
        for (var input : pattern.getInputs()) env.stock.add(input.getPossibleInputs()[0].what(), 64);
        lines.add(id + "|stock64 x" + REQUEST + "|" + env.run(target, REQUEST, CalculationStrategy.REPORT_MISSING_ITEMS).describe(env));
        lines.add(id + "|stock64 less x" + REQUEST + "|" + env.run(target, REQUEST, CalculationStrategy.CRAFT_LESS).describe(env));
    }

    private static String checkPlainPattern(ServerLevel level, IPatternDetails pattern) {
        var target = pattern.getPrimaryOutput().what();
        long times = (REQUEST + pattern.getPrimaryOutput().amount() - 1) / pattern.getPrimaryOutput().amount();
        var expectedUse = new Reference2LongOpenHashMap<AEKey>();
        for (var input : pattern.getInputs()) {
            var first = input.getPossibleInputs()[0];
            expectedUse.addTo(first.what(), first.amount() * input.getMultiplier() * times);
        }
        var env = new GameCraftEnv(level);
        env.add(pattern);
        for (var e : expectedUse.reference2LongEntrySet()) env.stock.add(e.getKey(), e.getLongValue());
        var outcome = env.run(target, REQUEST, CalculationStrategy.REPORT_MISSING_ITEMS);
        if (outcome.plan() == null || outcome.rejected()) return outcome.describe(env);
        var plan = outcome.plan();
        if (plan.simulation() || !plan.missingItems().isEmpty()) return "缺料 " + outcome.describe(env);
        if (plan.patternTimes().getLong(pattern) != times) return "次数应为 " + times + "：" + outcome.describe(env);
        for (var e : expectedUse.reference2LongEntrySet()) {
            if (plan.usedItems().get(e.getKey()) != e.getLongValue()) return e.getKey() + " 用量应为 " + e.getLongValue() + "：" + outcome.describe(env);
        }
        return null;
    }

    private static boolean consumesOwnOutput(IPatternDetails pattern) {
        var target = pattern.getPrimaryOutput().what();
        for (var input : pattern.getInputs()) {
            for (var candidate : input.getPossibleInputs()) {
                if (candidate.what() == target) return true;
            }
        }
        return false;
    }

    private static ItemStack[] gridOf(CraftingRecipe recipe) {
        var ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) return null;
        int width = recipe instanceof IShapedRecipe<?> shaped ? shaped.getRecipeWidth() : 3;
        var grid = new ItemStack[9];
        Arrays.fill(grid, ItemStack.EMPTY);
        boolean any = false;
        for (int i = 0; i < ingredients.size(); i++) {
            var items = ingredients.get(i).getItems();
            if (items.length == 0) continue;
            int slot = recipe instanceof IShapedRecipe<?> ? (i / width) * 3 + i % width : i;
            if (slot >= 9) return null;
            grid[slot] = items[0].copyWithCount(1);
            any = true;
        }
        return any ? grid : null;
    }

    private static IPatternDetails decode(ItemStack encoded, ServerLevel level) {
        try {
            return PatternDetailsHelper.decodePattern(encoded, level);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static void write(String file, List<String> lines) {
        try {
            Files.write(Path.of(file), lines);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

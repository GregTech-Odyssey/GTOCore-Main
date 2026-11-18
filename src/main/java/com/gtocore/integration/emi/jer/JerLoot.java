package com.gtocore.integration.emi.jer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// 改写自 Just Enough Resources（way2muchnoise，"Don't Be a Jerk" 非商业许可）
// https://github.com/way2muchnoise/JustEnoughResources
final class JerLoot {

    private static final int SAMPLES = 100;

    private final MinecraftServer server;
    private final LootDataManager data;
    private final Gson gson = LootDataType.TABLE.parser();
    private @Nullable LootContext context;

    JerLoot(MinecraftServer server) {
        this.server = server;
        this.data = server.getLootData();
    }

    Collection<ResourceLocation> tableIds() {
        return data.getKeys(LootDataType.TABLE);
    }

    Table read(ResourceLocation id, boolean withConditions) {
        List<Drop> drops = new ArrayList<>();
        int[] stacks = new int[2];
        try {
            collect(id, withConditions, new OpenCacheHashSet<>(), drops, stacks);
        } catch (RuntimeException e) {
            return Table.EMPTY;
        }
        return new Table(drops, stacks[0], stacks[1]);
    }

    private void collect(ResourceLocation id, boolean withConditions, OpenCacheHashSet<ResourceLocation> path, List<Drop> out, int[] stacks) {
        if (!path.add(id)) return;
        LootTable table = data.getLootTable(id);
        if (table == LootTable.EMPTY) {
            path.remove(id);
            return;
        }
        JsonObject json = gson.toJsonTree(table).getAsJsonObject();
        for (JsonElement poolElement : array(json, "pools")) {
            JsonObject pool = poolElement.getAsJsonObject();
            stacks[0] += min(pool.get("rolls"));
            stacks[1] += max(pool.get("rolls")) + max(pool.get("bonus_rolls"));
            JsonArray entries = array(pool, "entries");
            int totalWeight = 0;
            for (JsonElement entry : entries) {
                JsonObject e = entry.getAsJsonObject();
                if (isSingleton(type(e, "type"))) totalWeight += weight(e);
            }
            for (JsonElement entry : entries) {
                JsonObject e = entry.getAsJsonObject();
                String type = type(e, "type");
                if ("minecraft:item".equals(type)) {
                    Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(e.get("name").getAsString()));
                    if (item == Items.AIR || totalWeight <= 0) continue;
                    Drop.Builder drop = new Drop.Builder(new ItemStack(item), (float) weight(e) / totalWeight);
                    for (JsonElement function : array(e, "functions")) applyFunction(function.getAsJsonObject(), drop);
                    if (withConditions) {
                        for (JsonElement condition : array(e, "conditions")) applyCondition(condition.getAsJsonObject(), drop);
                        for (JsonElement condition : array(pool, "conditions")) applyCondition(condition.getAsJsonObject(), drop);
                    }
                    out.add(drop.build());
                } else if ("minecraft:loot_table".equals(type)) {
                    collect(new ResourceLocation(e.get("name").getAsString()), withConditions, path, out, stacks);
                }
            }
        }
        path.remove(id);
    }

    private void applyFunction(JsonObject function, Drop.Builder drop) {
        switch (type(function, "function")) {
            case "minecraft:set_count" -> {
                JsonElement count = function.get("count");
                drop.min = Math.max(min(count), 0);
                drop.max = max(count);
                drop.stack.setCount(Math.max(drop.min, 1));
            }
            case "minecraft:looting_enchant" -> drop.looting = true;
            case "gtceu:random_weight" -> {
                drop.min = Math.max(function.get("min").getAsInt(), 0);
                drop.max = function.get("max").getAsInt();
                drop.stack.setCount(Math.max(drop.min, 1));
                applyRaw(function, drop);
            }
            case "minecraft:enchant_randomly", "minecraft:enchant_with_levels", "minecraft:furnace_smelt" -> {}
            case "minecraft:set_damage" -> {
                try {
                    gson.fromJson(function, LootItemFunction.class).apply(drop.stack, context());
                } catch (RuntimeException ignored) {}
            }
            default -> applyRaw(function, drop);
        }
    }

    private void applyRaw(JsonObject function, Drop.Builder drop) {
        int count = drop.stack.getCount();
        try {
            ItemStack result = gson.fromJson(function, LootItemFunction.class).apply(drop.stack, null);
            if (result != null && !result.isEmpty()) drop.stack = result;
        } catch (RuntimeException ignored) {}
        drop.stack.setCount(count);
    }

    private static void applyCondition(JsonObject condition, Drop.Builder drop) {
        switch (type(condition, "condition")) {
            case "minecraft:killed_by_player" -> drop.playerKill = true;
            case "minecraft:random_chance" -> drop.chance = condition.get("chance").getAsFloat();
            case "minecraft:random_chance_with_looting" -> {
                drop.chance = condition.get("chance").getAsFloat();
                drop.looting = true;
            }
            default -> {}
        }
    }

    private int min(@Nullable JsonElement provider) {
        if (provider == null || provider.isJsonNull()) return 0;
        if (provider.isJsonPrimitive()) return Mth.floor(provider.getAsFloat());
        JsonObject object = provider.getAsJsonObject();
        String type = type(object, "type");
        if (type.isEmpty() || "minecraft:uniform".equals(type)) return min(object.get("min"));
        if ("minecraft:constant".equals(type)) return min(object.get("value"));
        if ("minecraft:binomial".equals(type)) return 0;
        return sample(provider, true);
    }

    private int max(@Nullable JsonElement provider) {
        if (provider == null || provider.isJsonNull()) return 0;
        if (provider.isJsonPrimitive()) return Mth.floor(provider.getAsFloat());
        JsonObject object = provider.getAsJsonObject();
        String type = type(object, "type");
        if (type.isEmpty() || "minecraft:uniform".equals(type)) return max(object.get("max"));
        if ("minecraft:constant".equals(type)) return max(object.get("value"));
        if ("minecraft:binomial".equals(type)) return max(object.get("n"));
        return sample(provider, false);
    }

    private int sample(JsonElement provider, boolean lowest) {
        try {
            NumberProvider number = gson.fromJson(provider, NumberProvider.class);
            LootContext ctx = context();
            int result = number.getInt(ctx);
            for (int i = 1; i < SAMPLES; i++) {
                int value = number.getInt(ctx);
                result = lowest ? Math.min(result, value) : Math.max(result, value);
            }
            return result;
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private LootContext context() {
        if (context == null) {
            context = new LootContext.Builder(new LootParams.Builder(server.overworld()).create(LootContextParamSets.EMPTY))
                    .withOptionalRandomSeed(42L).create(null);
        }
        return context;
    }

    private static boolean isSingleton(String type) {
        return switch (type) {
            case "minecraft:alternatives", "minecraft:group", "minecraft:sequence", "" -> false;
            default -> true;
        };
    }

    private static int weight(JsonObject entry) {
        JsonElement weight = entry.get("weight");
        return weight == null ? 1 : weight.getAsInt();
    }

    private static String type(JsonObject object, String key) {
        JsonElement type = object.get(key);
        if (type == null || !type.isJsonPrimitive()) return "";
        String value = type.getAsString();
        return value.indexOf(':') < 0 ? "minecraft:" + value : value;
    }

    private static JsonArray array(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : new JsonArray();
    }

    record Table(List<Drop> drops, int minStacks, int maxStacks) {

        static final Table EMPTY = new Table(Collections.emptyList(), 0, 0);
    }

    record Drop(ItemStack stack, int min, int max, float chance, boolean playerKill, boolean looting) {

        float sortIndex() {
            return Math.min(chance, 1F) * (min + max);
        }

        static final class Builder {

            ItemStack stack;
            int min = 1;
            int max = 1;
            float chance;
            boolean playerKill;
            boolean looting;

            Builder(ItemStack stack, float chance) {
                this.stack = stack;
                this.chance = chance;
            }

            Drop build() {
                return new Drop(stack, min, Math.max(min, max), chance, playerKill, looting);
            }
        }
    }
}

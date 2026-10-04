package com.gtocore.api.report;

import com.gtocore.integration.Mods;
import com.gtocore.integration.lang.LangAdaptor;

import com.gtolib.GTOCore;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * 导出全部生物（实体类型）相关信息，以及全部生物与全部生物蛋的列表。
 * <p>
 * 输出结构（{@code logs/report/entity_report_<时间戳>/}）：
 * <ul>
 * <li>{@code entities.json}：全部实体（含全部生物）列表，每条包含 ID、中英文名、分类、真实实体类、是否生物
 * （是否继承 {@code LivingEntity}）与是否 Mob、尺寸、是否抗火、能否召唤、默认战利品表、生成放置方式、
 * 全部属性、全部生物群系刷怪信息、实体标签、对应生物蛋；</li>
 * <li>{@code spawn_eggs.json}：全部生物蛋列表（物品 ID、对应生物、中英文名、主次颜色）；</li>
 * <li>{@code entities.csv} / {@code spawn_eggs.csv}：上述两份列表的表格形式；</li>
 * <li>{@code index.md}：汇总统计、两张列表表格以及可复制的 ID 清单。</li>
 * </ul>
 */
public class EntityReport {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Comparator<JsonObject> BIOME_SPAWN_ORDER = Comparator
            .comparing((JsonObject spawn) -> spawn.get("biome").getAsString())
            .thenComparing(spawn -> spawn.get("category").getAsString());

    private static final String ENTITY_CSV_HEADER = "实体ID,命名空间,路径,本地化键,中文名,英文名,分类,分类名,实体类,是否生物,是否Mob,宽度,高度,是否抗火,可远离玩家生成,可召唤,默认战利品表,生成放置,生成高度图,生物蛋物品,属性数量,群系刷怪数量,标签数量\n";

    private static final String SPAWN_EGG_CSV_HEADER = "生物蛋物品,本地化键,中文名,英文名,对应生物,生物中文名,生物英文名,主色,次色\n";

    /**
     * 导出生物报告。只有取不到 {@link MinecraftServer}（纯客户端）时才会失败。
     */
    public static void generateReport() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            GTOCore.LOGGER.error("生物报告生成失败：无法获取服务器实例，可能不在服务端环境或服务器未启动");
            return;
        }

        String generatedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Path baseDir = Paths.get("logs", "report",
                "entity_report_" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
        try {
            Files.createDirectories(baseDir);

            // 生物 → 生物蛋，以及生物蛋 → 生物（Forge 的 TYPE_MAP 同时覆盖原版与模组生物蛋）
            // 非生物类型 fromEntityType 返回 null，因此无需预先过滤
            Map<EntityType<?>, SpawnEggItem> eggsByType = new IdentityHashMap<>();
            Map<SpawnEggItem, EntityType<?>> typesByEgg = new IdentityHashMap<>();
            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                SpawnEggItem egg = ForgeSpawnEggItem.fromEntityType(entityType);
                if (egg == null) continue;
                eggsByType.put(entityType, egg);
                typesByEgg.put(egg, entityType);
            }

            Map<EntityType<?>, List<String>> tagsByType = collectEntityTags();
            Map<EntityType<?>, JsonArray> spawnsByType = collectBiomeSpawns(server);

            JsonArray entities = new JsonArray();
            JsonArray spawnEggs = new JsonArray();
            Map<String, Integer> categoryCounts = new TreeMap<>();
            Level level = server.overworld();
            int livingCount = 0;
            int mobCount = 0;
            int fallbackCount = 0;

            for (EntityType<?> entityType : sortedEntityTypes()) {
                EntityClass entityClass = resolveEntityClass(level, entityType);
                if (entityClass.entityClass() == null) fallbackCount++;
                JsonObject entityJson = buildEntityJson(entityType, entityClass, eggsByType.get(entityType),
                        tagsByType.get(entityType), spawnsByType.get(entityType));
                entities.add(entityJson);

                if (entityJson.get("living_entity").getAsBoolean()) livingCount++;
                if (entityJson.get("mob").getAsBoolean()) mobCount++;
                categoryCounts.merge(entityJson.get("category").getAsString(), 1, Integer::sum);

                SpawnEggItem egg = eggsByType.get(entityType);
                if (egg != null) spawnEggs.add(buildSpawnEggJson(egg, entityType));
            }

            // 以物品注册表为准补全生物蛋列表：不依赖生物与生物蛋的对应关系
            for (Item item : sortedSpawnEggItems()) {
                if (typesByEgg.containsKey(item)) continue;
                spawnEggs.add(buildSpawnEggJson((SpawnEggItem) item, null));
            }

            JsonObject entitiesRoot = new JsonObject();
            entitiesRoot.addProperty("generated_at", generatedAt);
            entitiesRoot.addProperty("entity_count", entities.size());
            entitiesRoot.addProperty("living_count", livingCount);
            entitiesRoot.addProperty("mob_count", mobCount);
            entitiesRoot.addProperty("spawn_egg_count", spawnEggs.size());
            entitiesRoot.add("entities", entities);
            Files.writeString(baseDir.resolve("entities.json"), GSON.toJson(entitiesRoot), StandardCharsets.UTF_8);

            JsonObject spawnEggsRoot = new JsonObject();
            spawnEggsRoot.addProperty("generated_at", generatedAt);
            spawnEggsRoot.addProperty("count", spawnEggs.size());
            spawnEggsRoot.add("spawn_eggs", spawnEggs);
            Files.writeString(baseDir.resolve("spawn_eggs.json"), GSON.toJson(spawnEggsRoot), StandardCharsets.UTF_8);

            Files.writeString(baseDir.resolve("entities.csv"), '\uFEFF' + buildEntitiesCsv(entities),
                    StandardCharsets.UTF_8);
            Files.writeString(baseDir.resolve("spawn_eggs.csv"), '\uFEFF' + buildSpawnEggsCsv(spawnEggs),
                    StandardCharsets.UTF_8);
            Files.writeString(baseDir.resolve("index.md"),
                    buildIndexMarkdown(generatedAt, entities, spawnEggs, categoryCounts, livingCount, mobCount),
                    StandardCharsets.UTF_8);

            GTOCore.LOGGER.info("生物报告已生成：{} 个实体类型（生物 {} 个，其中 Mob {} 个），{} 个生物蛋，输出目录 {}",
                    entities.size(), livingCount, mobCount, spawnEggs.size(), baseDir.toAbsolutePath());
            if (fallbackCount > 0) {
                GTOCore.LOGGER.warn("有 {} 个实体类型无法实例化，其生物判定回退为属性与分类判断", fallbackCount);
            }
        } catch (IOException e) {
            GTOCore.LOGGER.error("写入生物报告文件时发生错误", e);
        }
    }

    private static List<EntityType<?>> sortedEntityTypes() {
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) entityTypes.add(entityType);
        entityTypes.sort((a, b) -> BuiltInRegistries.ENTITY_TYPE.getKey(a).toString()
                .compareTo(BuiltInRegistries.ENTITY_TYPE.getKey(b).toString()));
        return entityTypes;
    }

    private static List<Item> sortedSpawnEggItems() {
        List<Item> eggItems = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof SpawnEggItem) eggItems.add(item);
        }
        eggItems.sort((a, b) -> BuiltInRegistries.ITEM.getKey(a).toString()
                .compareTo(BuiltInRegistries.ITEM.getKey(b).toString()));
        return eggItems;
    }

    /**
     * 实体类型标签：实体 → 标签 ID 列表（已排序）。
     */
    private static Map<EntityType<?>, List<String>> collectEntityTags() {
        Map<EntityType<?>, List<String>> tagsByType = new IdentityHashMap<>();
        for (var tagEntry : BuiltInRegistries.ENTITY_TYPE.getTags().toList()) {
            String tagId = tagEntry.getFirst().location().toString();
            for (Holder<EntityType<?>> holder : tagEntry.getSecond()) {
                tagsByType.computeIfAbsent(holder.value(), key -> new ArrayList<>()).add(tagId);
            }
        }
        tagsByType.values().forEach(Collections::sort);
        return tagsByType;
    }

    /**
     * 生物群系刷怪表：生物 → 该生物在各生物群系的刷怪信息（生物群系、分类、权重、数量、生成能耗）。
     */
    private static Map<EntityType<?>, JsonArray> collectBiomeSpawns(MinecraftServer server) {
        Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registries.BIOME);

        List<Biome> sortedBiomes = new ArrayList<>();
        for (Biome biome : biomes) sortedBiomes.add(biome);
        sortedBiomes.sort((a, b) -> biomes.getKey(a).toString().compareTo(biomes.getKey(b).toString()));

        Map<EntityType<?>, List<JsonObject>> spawnsByType = new IdentityHashMap<>();
        for (Biome biome : sortedBiomes) {
            ResourceLocation biomeId = biomes.getKey(biome);
            MobSpawnSettings settings = biome.getMobSettings();
            for (MobCategory category : settings.getSpawnerTypes()) {
                for (MobSpawnSettings.SpawnerData spawner : settings.getMobs(category).unwrap()) {
                    JsonObject spawnJson = new JsonObject();
                    spawnJson.addProperty("biome", biomeId.toString());
                    spawnJson.addProperty("category", category.getName());
                    spawnJson.addProperty("weight", spawner.getWeight().asInt());
                    spawnJson.addProperty("min_count", spawner.minCount);
                    spawnJson.addProperty("max_count", spawner.maxCount);
                    MobSpawnSettings.MobSpawnCost cost = settings.getMobSpawnCost(spawner.type);
                    if (cost != null) {
                        spawnJson.addProperty("energy_budget", cost.energyBudget());
                        spawnJson.addProperty("charge", cost.charge());
                    }
                    spawnsByType.computeIfAbsent(spawner.type, key -> new ArrayList<>()).add(spawnJson);
                }
            }
        }

        Map<EntityType<?>, JsonArray> result = new IdentityHashMap<>();
        for (Map.Entry<EntityType<?>, List<JsonObject>> entry : spawnsByType.entrySet()) {
            entry.getValue().sort(BIOME_SPAWN_ORDER);
            JsonArray array = new JsonArray();
            entry.getValue().forEach(array::add);
            result.put(entry.getKey(), array);
        }
        return result;
    }

    /**
     * 取得实体类型的真实实体类并判定是否为生物。
     * <p>
     * 1.20.1 的 {@link EntityType#getBaseClass()} 固定返回 {@code Entity.class}（占位实现），
     * 用它做 {@code isAssignableFrom} 会把所有实体都判成非生物，所以这里按实体类型实例化一次
     * （与刷怪蛋生成实体同一条路径）取真实类：是否为生物按是否继承 {@link LivingEntity} 判定，
     * {@code mob} 只是在此之上的细分（是否继承 {@link Mob}）。
     * 无法实例化的类型（如 player）回退为属性与分类判断。
     */
    private static EntityClass resolveEntityClass(Level level, EntityType<?> entityType) {
        Entity instance = createInstance(level, entityType);
        if (instance != null) {
            return new EntityClass(instance.getClass(), instance instanceof LivingEntity, instance instanceof Mob);
        }
        return new EntityClass(null, DefaultAttributes.hasSupplier(entityType),
                entityType.getCategory() != MobCategory.MISC);
    }

    private static Entity createInstance(Level level, EntityType<?> entityType) {
        try {
            return entityType.create(level);
        } catch (Exception e) {
            GTOCore.LOGGER.debug("实体类型 {} 无法实例化，改用属性与分类判断生物", entityType, e);
            return null;
        }
    }

    private static JsonObject buildEntityJson(EntityType<?> entityType, EntityClass entityClass, SpawnEggItem egg,
                                              List<String> tags, JsonArray biomeSpawns) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        MobCategory category = entityType.getCategory();
        String translationKey = entityType.getDescriptionId();
        SpawnPlacements.Type placementType = SpawnPlacements.getPlacementType(entityType);
        Heightmap.Types heightmap = SpawnPlacements.getHeightmapType(entityType);

        JsonObject json = new JsonObject();
        json.addProperty("id", id.toString());
        json.addProperty("namespace", id.getNamespace());
        json.addProperty("path", id.getPath());
        json.addProperty("translation_key", translationKey);
        addNames(json, translationKey);
        json.addProperty("category", category.name());
        json.addProperty("category_name", category.getName());
        json.addProperty("category_friendly", category.isFriendly());
        json.addProperty("category_persistent", category.isPersistent());
        json.addProperty("category_max_instances_per_chunk", category.getMaxInstancesPerChunk());
        json.addProperty("category_despawn_distance", category.getDespawnDistance());
        json.addProperty("entity_class", entityClass.entityClass() == null ? "" : entityClass.entityClass().getName());
        json.addProperty("class_detected_by", entityClass.entityClass() == null ? "fallback" : "instantiated");
        json.addProperty("living_entity", entityClass.livingEntity());
        json.addProperty("mob", entityClass.mob());
        json.addProperty("width", entityType.getWidth());
        json.addProperty("height", entityType.getHeight());
        json.addProperty("fire_immune", entityType.fireImmune());
        json.addProperty("can_spawn_far_from_player", entityType.canSpawnFarFromPlayer());
        json.addProperty("can_summon", entityType.canSummon());
        json.addProperty("default_loot_table", String.valueOf(entityType.getDefaultLootTable()));
        json.addProperty("spawn_placement", placementType == null ? "" : placementType.name());
        json.addProperty("spawn_heightmap", heightmap == null ? "" : heightmap.getSerializedName());

        JsonArray attributes = buildAttributes(entityType);
        json.addProperty("attribute_count", attributes.size());
        json.add("attributes", attributes);

        JsonArray biomeSpawnArray = biomeSpawns == null ? new JsonArray() : biomeSpawns;
        json.addProperty("biome_spawn_count", biomeSpawnArray.size());
        json.add("biome_spawns", biomeSpawnArray);

        JsonArray tagArray = new JsonArray();
        if (tags != null) tags.forEach(tagArray::add);
        json.addProperty("tag_count", tagArray.size());
        json.add("tags", tagArray);

        if (egg != null) json.add("spawn_egg", buildSpawnEggJson(egg, entityType));
        return json;
    }

    /**
     * 生物的默认属性：遍历属性注册表取该生物实际拥有的属性与基础值。
     */
    @SuppressWarnings("unchecked")
    private static JsonArray buildAttributes(EntityType<?> entityType) {
        JsonArray attributes = new JsonArray();
        if (!DefaultAttributes.hasSupplier(entityType)) return attributes;

        AttributeSupplier supplier = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) entityType);
        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            if (!supplier.hasAttribute(attribute)) continue;

            JsonObject attributeJson = new JsonObject();
            attributeJson.addProperty("id", String.valueOf(BuiltInRegistries.ATTRIBUTE.getKey(attribute)));
            attributeJson.addProperty("translation_key", attribute.getDescriptionId());
            addNames(attributeJson, attribute.getDescriptionId());
            attributeJson.addProperty("default_value", supplier.getBaseValue(attribute));
            attributes.add(attributeJson);
        }
        return attributes;
    }

    /**
     * 生物蛋信息；{@code entityType} 为空表示该生物蛋无法对应到已注册生物。
     */
    private static JsonObject buildSpawnEggJson(SpawnEggItem egg, EntityType<?> entityType) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(egg);
        JsonObject json = new JsonObject();
        json.addProperty("item", itemId.toString());
        json.addProperty("translation_key", egg.getDescriptionId());
        addNames(json, egg.getDescriptionId());
        json.addProperty("primary_color", colorHex(egg.getColor(0)));
        json.addProperty("secondary_color", colorHex(egg.getColor(1)));

        if (entityType != null) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            String entityTranslationKey = entityType.getDescriptionId();
            json.addProperty("entity_type", entityId.toString());
            json.addProperty("entity_translation_key", entityTranslationKey);
            addNames(json, entityTranslationKey, "entity_name_zh", "entity_name_en");
        }
        return json;
    }

    private static void addNames(JsonObject json, String translationKey) {
        addNames(json, translationKey, "name_zh", "name_en");
    }

    private static void addNames(JsonObject json, String translationKey, String zhKey, String enKey) {
        if (!Mods.LANG.isLoaded()) return;
        json.addProperty(zhKey, LangAdaptor.langCn(Component.translatable(translationKey)));
        json.addProperty(enKey, LangAdaptor.langEn(Component.translatable(translationKey)));
    }

    private static String buildEntitiesCsv(JsonArray entities) {
        StringBuilder csv = new StringBuilder(ENTITY_CSV_HEADER);
        for (var element : entities) {
            JsonObject entity = element.getAsJsonObject();
            csv.append(csvText(entity, "id")).append(',')
                    .append(csvText(entity, "namespace")).append(',')
                    .append(csvText(entity, "path")).append(',')
                    .append(csvText(entity, "translation_key")).append(',')
                    .append(csvText(entity, "name_zh")).append(',')
                    .append(csvText(entity, "name_en")).append(',')
                    .append(csvText(entity, "category")).append(',')
                    .append(csvText(entity, "category_name")).append(',')
                    .append(csvText(entity, "entity_class")).append(',')
                    .append(flag(entity, "living_entity")).append(',')
                    .append(flag(entity, "mob")).append(',')
                    .append(csvText(entity, "width")).append(',')
                    .append(csvText(entity, "height")).append(',')
                    .append(flag(entity, "fire_immune")).append(',')
                    .append(flag(entity, "can_spawn_far_from_player")).append(',')
                    .append(flag(entity, "can_summon")).append(',')
                    .append(csvText(entity, "default_loot_table")).append(',')
                    .append(csvText(entity, "spawn_placement")).append(',')
                    .append(csvText(entity, "spawn_heightmap")).append(',')
                    .append(spawnEggItemId(entity)).append(',')
                    .append(csvText(entity, "attribute_count")).append(',')
                    .append(csvText(entity, "biome_spawn_count")).append(',')
                    .append(csvText(entity, "tag_count")).append('\n');
        }
        return csv.toString();
    }

    private static String buildSpawnEggsCsv(JsonArray spawnEggs) {
        StringBuilder csv = new StringBuilder(SPAWN_EGG_CSV_HEADER);
        for (var element : spawnEggs) {
            JsonObject egg = element.getAsJsonObject();
            csv.append(csvText(egg, "item")).append(',')
                    .append(csvText(egg, "translation_key")).append(',')
                    .append(csvText(egg, "name_zh")).append(',')
                    .append(csvText(egg, "name_en")).append(',')
                    .append(csvText(egg, "entity_type")).append(',')
                    .append(csvText(egg, "entity_name_zh")).append(',')
                    .append(csvText(egg, "entity_name_en")).append(',')
                    .append(csvText(egg, "primary_color")).append(',')
                    .append(csvText(egg, "secondary_color")).append('\n');
        }
        return csv.toString();
    }

    private static String buildIndexMarkdown(String generatedAt, JsonArray entities, JsonArray spawnEggs,
                                             Map<String, Integer> categoryCounts, int livingCount, int mobCount) {
        StringBuilder markdown = new StringBuilder("# 生物报告\n\n");
        markdown.append("- 生成时间: ").append(generatedAt).append('\n');
        markdown.append("- 实体类型总数: ").append(entities.size()).append('\n');
        markdown.append("- 生物（LivingEntity 及其子类）数量: ").append(livingCount).append('\n');
        markdown.append("- 其中 Mob 数量: ").append(mobCount).append('\n');
        markdown.append("- 非生物实体数量: ").append(entities.size() - livingCount).append('\n');
        markdown.append("- 生物蛋数量: ").append(spawnEggs.size()).append('\n');
        markdown.append("\n## 分类统计\n\n");
        markdown.append("| 分类 | 数量 |\n|-----|------|\n");
        categoryCounts.forEach((category, count) -> markdown.append("| ").append(category).append(" | ")
                .append(count).append(" |\n"));

        markdown.append("\n## 全部生物/实体列表\n\n");
        markdown.append("| 实体 ID | 中文名 | 英文名 | 分类 | 是生物 | 是Mob | 生物蛋 | 属性数 | 群系刷怪数 | 标签数 |\n");
        markdown.append("|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|\n");
        for (var element : entities) {
            JsonObject entity = element.getAsJsonObject();
            markdown.append("| `").append(text(entity, "id")).append("` | ")
                    .append(text(entity, "name_zh")).append(" | ")
                    .append(text(entity, "name_en")).append(" | ")
                    .append(text(entity, "category")).append(" | ")
                    .append(flag(entity, "living_entity")).append(" | ")
                    .append(flag(entity, "mob")).append(" | ")
                    .append(spawnEggItemId(entity)).append(" | ")
                    .append(text(entity, "attribute_count")).append(" | ")
                    .append(text(entity, "biome_spawn_count")).append(" | ")
                    .append(text(entity, "tag_count")).append(" |\n");
        }

        markdown.append("\n## 全部生物蛋列表\n\n");
        markdown.append("| 生物蛋 | 对应生物 | 中文名 | 英文名 | 主色 | 次色 |\n");
        markdown.append("|-----|-----|-----|-----|-----|-----|\n");
        for (var element : spawnEggs) {
            JsonObject egg = element.getAsJsonObject();
            markdown.append("| `").append(text(egg, "item")).append("` | `")
                    .append(text(egg, "entity_type")).append("` | ")
                    .append(text(egg, "name_zh")).append(" | ")
                    .append(text(egg, "name_en")).append(" | ")
                    .append(text(egg, "primary_color")).append(" | ")
                    .append(text(egg, "secondary_color")).append(" |\n");
        }

        markdown.append("\n## 全部实体 ID 列表\n\n```\n");
        for (var element : entities) markdown.append(text(element.getAsJsonObject(), "id")).append('\n');
        markdown.append("```\n");

        markdown.append("\n## 全部生物蛋物品 ID 列表\n\n```\n");
        for (var element : spawnEggs) markdown.append(text(element.getAsJsonObject(), "item")).append('\n');
        markdown.append("```\n");
        return markdown.toString();
    }

    private static String spawnEggItemId(JsonObject entity) {
        return entity.has("spawn_egg") ? text(entity.getAsJsonObject("spawn_egg"), "item") : "";
    }

    private static String text(JsonObject json, String key) {
        return json.has(key) ? json.get(key).getAsString() : "";
    }

    private static String flag(JsonObject json, String key) {
        return json.has(key) && json.get(key).getAsBoolean() ? "是" : "否";
    }

    private static String csvText(JsonObject json, String key) {
        return csv(text(json, key));
    }

    private static String csv(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0) return value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static String colorHex(int color) {
        return String.format(Locale.ROOT, "#%06X", color & 0xFFFFFF);
    }

    /**
     * 实体类型的真实实体类与生物判定结果。
     * {@code livingEntity} 为是否继承 {@code LivingEntity}（报告的“是否生物”），{@code mob} 为是否继承 {@code Mob}；
     * {@code entityClass} 为空表示无法实例化，此时两者由回退逻辑给出。
     */
    private record EntityClass(Class<? extends Entity> entityClass, boolean livingEntity, boolean mob) {}
}

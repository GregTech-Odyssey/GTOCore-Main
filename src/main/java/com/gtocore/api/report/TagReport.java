package com.gtocore.api.report;

import com.gtolib.GTOCore;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
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
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * 导出游戏中所有注册表（物品、方块、流体、实体类型、生物群系、结构……）的全部标签。
 * <p>
 * 输出结构（{@code logs/report/tag_report_<时间戳>/}）：
 * <ul>
 * <li>按标签类型（标签所属注册表）分文件夹，如 {@code item/}、{@code block/}、{@code worldgen_biome/}；</li>
 * <li>每个标签一个 JSON 文件，文件名由标签所在命名空间与标签路径拼接而成，如 {@code forge_ingots_iron.json}；</li>
 * <li>文件内容包含标签的全部信息：标签 ID、注册表、类型、命名空间、路径、条目数量、各命名空间分布以及全部存储条目
 * （条目 ID、命名空间、路径、本地化键）；</li>
 * <li>根目录的 {@code index.json} / {@code index.md} 汇总所有类型与标签及其对应文件。</li>
 * </ul>
 */
public class TagReport {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern ILLEGAL_FILE_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

    /**
     * 导出全部标签报告。只能在没有 {@link MinecraftServer} 时（纯客户端）失败。
     */
    public static void generateReport() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            GTOCore.LOGGER.error("标签报告生成失败：无法获取服务器实例，可能不在服务端环境或服务器未启动");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        Path baseDir = Paths.get("logs", "report", "tag_report_" + timestamp);
        try {
            Files.createDirectories(baseDir);

            StringBuilder markdown = new StringBuilder("# 全部标签报告\n\n");
            JsonArray typeArray = new JsonArray();
            int totalTags = 0;
            int totalEntries = 0;

            for (Map.Entry<ResourceLocation, Registry<?>> entry : collectRegistries(server).entrySet()) {
                TypeSummary summary = exportRegistry(entry.getValue(), entry.getKey(), baseDir);
                if (summary == null) continue;

                totalTags += summary.tagCount();
                totalEntries += summary.entryCount();
                typeArray.add(summary.json());
                appendMarkdown(markdown, summary);
                GTOCore.LOGGER.info("[Tags] {} ({})：{} 个标签，{} 个条目",
                        summary.type(), summary.registry(), summary.tagCount(), summary.entryCount());
            }

            JsonObject index = new JsonObject();
            index.addProperty("generated_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            index.addProperty("type_count", typeArray.size());
            index.addProperty("total_tags", totalTags);
            index.addProperty("total_entries", totalEntries);
            index.add("types", typeArray);
            Files.writeString(baseDir.resolve("index.json"), GSON.toJson(index), StandardCharsets.UTF_8);

            markdown.append("\n****报告结束****\n");
            Files.writeString(baseDir.resolve("index.md"), markdown.toString(), StandardCharsets.UTF_8);

            GTOCore.LOGGER.info("标签报告已生成：{} 个类型，{} 个标签，{} 个条目，输出目录 {}",
                    typeArray.size(), totalTags, totalEntries, baseDir.toAbsolutePath());
        } catch (IOException e) {
            GTOCore.LOGGER.error("写入标签报告文件时发生错误", e);
        }
    }

    /**
     * 收集全部注册表：服务器注册表访问（含生物群系、结构等数据包注册表）+ 内置/Forge 自定义注册表，按 ID 去重。
     */
    private static Map<ResourceLocation, Registry<?>> collectRegistries(MinecraftServer server) {
        Map<ResourceLocation, Registry<?>> registries = new TreeMap<>();
        server.registryAccess().registries()
                .forEach(entry -> registries.putIfAbsent(entry.key().location(), entry.value()));
        BuiltInRegistries.REGISTRY.forEach(registry -> registries.putIfAbsent(registry.key().location(), registry));
        return registries;
    }

    /**
     * 导出单个注册表的全部标签，返回该类型的汇总信息；该注册表没有任何标签时返回 {@code null}。
     */
    private static TypeSummary exportRegistry(Registry<?> registry, ResourceLocation registryId, Path baseDir) throws IOException {
        String type = registryId.getPath().replace('/', '_');
        Map<ResourceLocation, JsonObject> tags = new TreeMap<>();

        registry.getTags().forEach(entry -> {
            TagKey<?> tagKey = entry.getFirst();
            tags.put(tagKey.location(), buildTagJson(registryId, type, tagKey, entry.getSecond()));
        });
        if (tags.isEmpty()) return null;

        Path typeDir = baseDir.resolve(type);
        Files.createDirectories(typeDir);

        JsonArray tagArray = new JsonArray();
        StringBuilder markdown = new StringBuilder();
        Set<String> usedFileNames = new HashSet<>(tags.size());
        int entryCount = 0;

        for (Map.Entry<ResourceLocation, JsonObject> tagEntry : tags.entrySet()) {
            ResourceLocation tagId = tagEntry.getKey();
            JsonObject tagJson = tagEntry.getValue();
            String fileName = uniqueFileName(usedFileNames, tagId);
            String relativePath = type + "/" + fileName;
            Files.writeString(typeDir.resolve(fileName), GSON.toJson(tagJson), StandardCharsets.UTF_8);

            int size = tagJson.get("entry_count").getAsInt();
            int namespaceCount = tagJson.get("namespace_count").getAsInt();
            entryCount += size;

            JsonObject indexEntry = new JsonObject();
            indexEntry.addProperty("tag", tagId.toString());
            indexEntry.addProperty("file", relativePath);
            indexEntry.addProperty("entry_count", size);
            indexEntry.addProperty("namespace_count", namespaceCount);
            tagArray.add(indexEntry);

            markdown.append("| `").append(tagId).append("` | ").append(size).append(" | ")
                    .append(namespaceCount).append(" | ").append(relativePath).append(" |\n");
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("registry", registryId.toString());
        json.addProperty("tag_count", tagArray.size());
        json.addProperty("entry_count", entryCount);
        json.add("tags", tagArray);

        return new TypeSummary(type, registryId.toString(), tagArray.size(), entryCount, json, markdown.toString());
    }

    /**
     * 构建单个标签的完整信息：标识信息 + 全部存储条目。
     */
    private static JsonObject buildTagJson(ResourceLocation registryId, String type, TagKey<?> tagKey, HolderSet.Named<?> holders) {
        ResourceLocation tagId = tagKey.location();
        JsonObject tagJson = new JsonObject();
        tagJson.addProperty("tag", tagId.toString());
        tagJson.addProperty("registry", registryId.toString());
        tagJson.addProperty("type", type);
        tagJson.addProperty("namespace", tagId.getNamespace());
        tagJson.addProperty("path", tagId.getPath());

        JsonArray entries = new JsonArray();
        Map<String, Integer> byNamespace = new TreeMap<>();
        for (Holder<?> holder : holders) {
            ResourceLocation elementId = holder.unwrapKey().map(ResourceKey::location).orElse(null);
            if (elementId == null) continue;

            JsonObject entryJson = new JsonObject();
            entryJson.addProperty("id", elementId.toString());
            entryJson.addProperty("namespace", elementId.getNamespace());
            entryJson.addProperty("path", elementId.getPath());
            String descriptionId = getDescriptionId(holder.value());
            if (descriptionId != null) entryJson.addProperty("description_id", descriptionId);
            entries.add(entryJson);

            byNamespace.merge(elementId.getNamespace(), 1, Integer::sum);
        }

        JsonObject namespaceCounts = new JsonObject();
        byNamespace.forEach(namespaceCounts::addProperty);

        tagJson.addProperty("entry_count", entries.size());
        tagJson.addProperty("namespace_count", byNamespace.size());
        tagJson.addProperty("empty", entries.isEmpty());
        tagJson.add("by_namespace", namespaceCounts);
        tagJson.add("entries", entries);
        return tagJson;
    }

    /**
     * 标签类型文件夹内的文件名：命名空间 + 标签路径；标签路径不同但拼接结果相同时追加序号避免互相覆盖。
     */
    private static String uniqueFileName(Set<String> usedFileNames, ResourceLocation tagId) {
        String base = ILLEGAL_FILE_CHARS.matcher(tagId.getNamespace() + "_" + tagId.getPath()).replaceAll("_");
        String fileName = base + ".json";
        for (int i = 2; !usedFileNames.add(fileName); i++) {
            fileName = base + "_" + i + ".json";
        }
        return fileName;
    }

    /**
     * 条目在对应注册表类型下可用的本地化键；没有本地化键的类型返回 {@code null}。
     */
    private static String getDescriptionId(Object value) {
        if (value instanceof Item item) return item.getDescriptionId();
        if (value instanceof Block block) return block.getDescriptionId();
        if (value instanceof EntityType<?> entityType) return entityType.getDescriptionId();
        if (value instanceof Fluid fluid) return fluid.getFluidType().getDescriptionId();
        return null;
    }

    private static void appendMarkdown(StringBuilder markdown, TypeSummary summary) {
        markdown.append("\n## ").append(summary.type()).append(" (").append(summary.registry()).append(")\n\n");
        markdown.append("- 标签数量: ").append(summary.tagCount()).append('\n');
        markdown.append("- 条目总数: ").append(summary.entryCount()).append("\n\n");
        markdown.append("| 标签 | 条目数 | 命名空间数 | 文件 |\n");
        markdown.append("|-----|------|------|------|\n");
        markdown.append(summary.markdown());
    }

    private record TypeSummary(String type, String registry, int tagCount, int entryCount, JsonObject json,
                               String markdown) {}
}

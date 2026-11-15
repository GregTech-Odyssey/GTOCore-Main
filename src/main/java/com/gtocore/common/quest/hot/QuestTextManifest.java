package com.gtocore.common.quest.hot;

import com.gtolib.GTOCore;
import com.gtolib.api.quest.QuestBook;
import com.gtolib.api.quest.QuestDetailLang;
import com.gtolib.api.quest.QuestLine;
import com.gtolib.api.quest.QuestRef;
import com.gtolib.api.quest.QuestScreenLang;
import com.gtolib.api.quest.QuestStyle;
import com.gtolib.quest.QuestServerLang;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * 任务文本清单：图鉴导出时由探针调用，把全部任务文本行（键、原文、样式）与每个任务可改文字的源码形态写成一份 JSON，
 * 随图鉴进官网，用于机器翻译与收集文字修改。
 */
public final class QuestTextManifest {

    public static final int SCHEMA = 1;
    static final String[] PARTS = { "title", "subtitle", "desc" };
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private QuestTextManifest() {}

    public static boolean write(Path target) {
        var root = QuestHotConfig.sourceRoot();
        if (root == null) {
            GTOCore.LOGGER.warn("[任务文本清单] 未找到章节源码目录，未生成清单");
            return false;
        }
        var src = QuestSource.load(root);
        if (!src.parseErrors.isEmpty()) {
            GTOCore.LOGGER.warn("[任务文本清单] 章节源码解析失败，未生成清单：{}", QuestHot.summarize(src.parseErrors));
            return false;
        }
        QuestServerLang.init();
        QuestScreenLang.init();
        QuestDetailLang.init();
        var texts = new Object2ObjectLinkedOpenHashMap<String, String>();
        QuestBook.collectLang(texts::put);
        var lines = new JsonArray();
        for (var it = texts.object2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            var line = new JsonObject();
            line.addProperty("key", entry.getKey());
            line.addProperty("text", entry.getValue());
            var owner = QuestLine.bound(entry.getKey());
            if (owner != null) {
                var args = args(owner);
                if (!args.isEmpty()) line.add("args", args);
            }
            lines.add(line);
        }
        var chapters = new JsonArray();
        var all = QuestBook.chapters();
        for (int i = 0; i < all.size(); i++) {
            var chapter = all.get(i);
            var json = new JsonObject();
            json.addProperty("id", chapter.id());
            if (chapter.titleKey() != null) json.addProperty("title", chapter.titleKey());
            var group = chapter.group();
            if (group != null && group.isDevOnly(chapter)) json.addProperty("devOnly", true);
            chapters.add(json);
        }
        var quests = new JsonArray();
        for (var file : src.files) {
            if (file.chapterId == null) continue;
            for (var decl : file.quests) {
                var quest = new JsonObject();
                quest.addProperty("key", decl.key());
                var parts = new JsonObject();
                for (var part : PARTS) {
                    var entries = QuestEdits.readText(decl, part);
                    if (!entries.isEmpty()) parts.add(part, GSON.toJsonTree(entries));
                }
                quest.add("parts", parts);
                quests.add(quest);
            }
        }
        var manifest = new JsonObject();
        manifest.addProperty("schema", SCHEMA);
        manifest.addProperty("version", GTOCore.getPackVersion());
        manifest.add("lines", lines);
        manifest.add("chapters", chapters);
        manifest.add("quests", quests);
        try {
            var parent = target.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(target, GSON.toJson(manifest), StandardCharsets.UTF_8);
            GTOCore.LOGGER.info("[任务文本清单] 已写入 {}：{} 行文本，{} 个任务", target, lines.size(), quests.size());
            return true;
        } catch (IOException e) {
            GTOCore.LOGGER.error("[任务文本清单] 写入 {} 失败", target, e);
            return false;
        }
    }

    private static JsonArray args(QuestLine line) {
        var out = new JsonArray();
        for (var arg : line.args()) {
            var json = new JsonObject();
            if (arg instanceof QuestRef ref) {
                json.addProperty("ref", ref.kind().name().toLowerCase(Locale.ROOT));
            } else if (arg instanceof QuestStyle link) {
                if (link.linkTarget() != null) json.addProperty("quest", link.linkTarget().key());
                if (link.linkUrl() != null) json.addProperty("url", link.linkUrl());
            }
            var style = arg.style();
            if (style.getColor() != null) json.addProperty("color", style.getColor().getValue());
            if (style.isBold()) json.addProperty("bold", true);
            if (style.isItalic()) json.addProperty("italic", true);
            if (style.isUnderlined()) json.addProperty("underline", true);
            if (style.isStrikethrough()) json.addProperty("strike", true);
            if (style.isObfuscated()) json.addProperty("obfuscated", true);
            out.add(json);
        }
        return out;
    }
}

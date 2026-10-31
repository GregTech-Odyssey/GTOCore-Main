package com.gtocore.data.lang;

import com.gtolib.GTOCore;
import com.gtolib.api.quest.QuestBook;
import com.gtolib.api.quest.QuestRef;

import net.minecraftforge.fml.ModList;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * runData 时检查任务用到的标签有没有中文译名，缺失的打印到日志并给出建议的语言键。
 */
final class QuestTagLangCheck {

    private QuestTagLangCheck() {}

    static void run(Set<String> ownKeys) {
        var known = new OpenCacheHashSet<String>();
        known.addAll(ownKeys);
        for (var file : ModList.get().getModFiles()) collect(file.getFile().findResource("assets"), known);
        var missing = new O2OOpenCacheHashMap<String, String>();
        QuestBook.forEachUsedTag((tag, where) -> {
            var keys = QuestRef.tagTranslationKeys(tag);
            for (var key : keys) {
                if (known.contains(key)) return;
            }
            missing.merge(keys[0] + "（#" + tag.location() + "）", where, (a, b) -> a.contains(b) ? a : a + "、" + b);
        });
        if (missing.isEmpty()) {
            GTOCore.LOGGER.info("任务用到的标签都有中文译名");
            return;
        }
        GTOCore.LOGGER.warn("任务用到的 {} 个标签没有中文译名，请在 LangHandler 中补充：", missing.size());
        missing.forEach((key, where) -> GTOCore.LOGGER.warn("  {}，用于 {}", key, where));
    }

    private static void collect(Path assets, Set<String> known) {
        if (assets == null || !Files.isDirectory(assets)) return;
        try (var namespaces = Files.list(assets)) {
            for (var namespace : (Iterable<Path>) namespaces::iterator) {
                var lang = namespace.resolve("lang").resolve("zh_cn.json");
                if (!Files.isRegularFile(lang)) continue;
                try (var reader = Files.newBufferedReader(lang)) {
                    known.addAll(JsonParser.parseReader(reader).getAsJsonObject().keySet());
                } catch (IOException | JsonParseException | IllegalStateException ignored) {}
            }
        } catch (IOException ignored) {}
    }
}

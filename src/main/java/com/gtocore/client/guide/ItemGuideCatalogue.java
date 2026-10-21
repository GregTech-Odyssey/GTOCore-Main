package com.gtocore.client.guide;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * gtodyssey.com 攻略目录（GET /api/item-guides，schema 1）的解析与校验；超限或格式不对的条目直接丢弃。
 */
public record ItemGuideCatalogue(List<String> versions, List<Article> articles, List<Link> links) {

    public static final int SCHEMA = 1;
    static final int MAX_VERSIONS = 512;
    static final int MAX_ARTICLES = 5000;
    static final int MAX_LINKS = 20000;
    static final int MAX_TITLE = 300;
    static final List<String> LOCALES = List.of("en", "zh", "ja", "ru", "es", "pt");
    static final Pattern UUID = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    static final Pattern ITEM_ID = Pattern.compile("^[a-z0-9_.-]{1,64}:[a-z0-9_./-]{1,192}$");
    static final Pattern VERSION = Pattern.compile("^[0-9A-Za-z][0-9A-Za-z.+_-]{0,63}$");
    static final Pattern VOLTAGE = Pattern.compile("^[A-Za-z]{1,8}$");

    public record Article(String id, String sourceLocale, long published, @Nullable String voltage, Map<String, String> titles) {

        public boolean hasLocale(String locale) {
            return titles.containsKey(locale);
        }
    }

    public record Link(int article, boolean fluid, String id, int version) {}

    public static ItemGuideCatalogue parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (intOf(root, "schema") != SCHEMA) throw new IllegalArgumentException("unsupported item guide schema");

        var versions = new ObjectArrayList<String>();
        for (var element : arrayOf(root, "versions", MAX_VERSIONS)) {
            String version = stringOf(element);
            if (version == null || !VERSION.matcher(version).matches()) throw new IllegalArgumentException("bad version");
            versions.add(version);
        }

        var articles = new ObjectArrayList<Article>();
        var given = arrayOf(root, "articles", MAX_ARTICLES);
        var positions = new int[given.size()];
        int index = 0;
        for (var element : given) {
            Article article = element.isJsonObject() ? article(element.getAsJsonObject()) : null;
            positions[index++] = article == null ? -1 : articles.size();
            if (article != null) articles.add(article);
        }

        var links = new ObjectArrayList<Link>();
        for (var element : arrayOf(root, "links", MAX_LINKS)) {
            if (!element.isJsonObject()) continue;
            var object = element.getAsJsonObject();
            int a = intOf(object, "a"), v = intOf(object, "v");
            String type = stringOf(object.get("type")), id = stringOf(object.get("id"));
            if (a < 0 || a >= positions.length || positions[a] < 0 || v < 0 || v >= versions.size()) continue;
            if (id == null || !ITEM_ID.matcher(id).matches()) continue;
            if (!"item".equals(type) && !"fluid".equals(type)) continue;
            links.add(new Link(positions[a], "fluid".equals(type), id, v));
        }
        return new ItemGuideCatalogue(Collections.unmodifiableList(versions), Collections.unmodifiableList(articles), Collections.unmodifiableList(links));
    }

    @Nullable
    private static Article article(JsonObject object) {
        String id = stringOf(object.get("id"));
        String source = stringOf(object.get("sourceLocale"));
        String published = stringOf(object.get("published"));
        if (id == null || !UUID.matcher(id).matches() || source == null || !LOCALES.contains(source) || published == null) return null;
        long when;
        try {
            when = Instant.parse(published).toEpochMilli();
        } catch (DateTimeParseException e) {
            return null;
        }
        String voltage = stringOf(object.get("voltage"));
        if (voltage != null && !VOLTAGE.matcher(voltage).matches()) voltage = null;
        var titles = new Object2ObjectOpenHashMap<String, String>();
        if (object.get("title") instanceof JsonObject given) {
            for (String locale : LOCALES) {
                String title = clean(stringOf(given.get(locale)));
                if (title != null) titles.put(locale, title);
            }
        }
        if (!titles.containsKey(source)) return null;
        return new Article(id, source, when, voltage, Collections.unmodifiableMap(titles));
    }

    @Nullable
    static String clean(@Nullable String text) {
        if (text == null) return null;
        var out = new StringBuilder(Math.min(text.length(), MAX_TITLE));
        for (int i = 0; i < text.length() && out.length() < MAX_TITLE; i++) {
            char c = text.charAt(i);
            if (c == '§' || Character.isISOControl(c)) continue;
            out.append(c);
        }
        String result = out.toString().strip();
        return result.isEmpty() ? null : result;
    }

    private static JsonArray arrayOf(JsonObject object, String key, int max) {
        if (!(object.get(key) instanceof JsonArray array)) throw new IllegalArgumentException("missing " + key);
        if (array.size() > max) throw new IllegalArgumentException("too many " + key);
        return array;
    }

    private static int intOf(JsonObject object, String key) {
        var element = object.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) return -1;
        return element.getAsInt();
    }

    @Nullable
    private static String stringOf(@Nullable JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) return null;
        return element.getAsString();
    }
}

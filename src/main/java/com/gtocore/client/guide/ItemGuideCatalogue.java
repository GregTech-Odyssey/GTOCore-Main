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
 * gtodyssey.com 攻略目录（GET /api/item-guides/v1，schema 1）的解析与校验；超限或格式不对的条目直接丢弃，不认识的字段忽略。
 */
public record ItemGuideCatalogue(List<Locale> locales, List<String> versions, List<Article> articles, List<Link> links) {

    public static final int SCHEMA = 1;
    static final int MAX_VERSIONS = 512;
    static final int MAX_ARTICLES = 5000;
    static final int MAX_LINKS = 20000;
    static final int MAX_TITLE = 300;
    static final int MAX_LOCALES = 32;
    static final int MAX_LOCALE_NAME = 32;
    static final List<String> FALLBACK_LOCALES = List.of("en", "zh", "ja", "ru", "es", "pt");
    static final Pattern LOCALE = Pattern.compile("^[a-z]{2,3}(-[a-z0-9]{2,8})?$");
    static final Pattern UUID = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    static final Pattern ITEM_ID = Pattern.compile("^[a-z0-9_.-]{1,64}:[a-z0-9_./-]{1,192}$");
    static final Pattern VERSION = Pattern.compile("^[0-9A-Za-z][0-9A-Za-z.+_-]{0,63}$");
    static final Pattern VOLTAGE = Pattern.compile("^[A-Za-z]{1,8}$");

    public record Locale(String id, String name) {}

    public record Article(String id, String sourceLocale, long published, @Nullable String voltage, Map<String, String> titles) {

        public boolean hasLocale(String locale) {
            return titles.containsKey(locale);
        }
    }

    public record Link(int article, boolean fluid, String id, int version) {}

    public static ItemGuideCatalogue parse(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (intOf(root, "schema") != SCHEMA) throw new IllegalArgumentException("unsupported item guide schema");
        var locales = locales(root);

        var versions = new ObjectArrayList<String>();
        var givenVersions = arrayOf(root, "versions");
        for (int i = 0, n = Math.min(givenVersions.size(), MAX_VERSIONS); i < n; i++) {
            String version = stringOf(givenVersions.get(i));
            versions.add(version != null && VERSION.matcher(version).matches() ? version : null);
        }

        var articles = new ObjectArrayList<Article>();
        var given = arrayOf(root, "articles");
        var positions = new int[Math.min(given.size(), MAX_ARTICLES)];
        for (int i = 0; i < positions.length; i++) {
            var element = given.get(i);
            Article article = element.isJsonObject() ? article(element.getAsJsonObject(), locales) : null;
            positions[i] = article == null ? -1 : articles.size();
            if (article != null) articles.add(article);
        }

        var links = new ObjectArrayList<Link>();
        var givenLinks = arrayOf(root, "links");
        for (int i = 0, n = Math.min(givenLinks.size(), MAX_LINKS); i < n; i++) {
            if (!(givenLinks.get(i) instanceof JsonObject object)) continue;
            int a = intOf(object, "a"), v = intOf(object, "v");
            String type = stringOf(object.get("type")), id = stringOf(object.get("id"));
            if (a < 0 || a >= positions.length || positions[a] < 0 || v < 0 || v >= versions.size() || versions.get(v) == null) continue;
            if (id == null || !ITEM_ID.matcher(id).matches()) continue;
            if (!"item".equals(type) && !"fluid".equals(type)) continue;
            links.add(new Link(positions[a], "fluid".equals(type), id, v));
        }
        return new ItemGuideCatalogue(Collections.unmodifiableList(locales), Collections.unmodifiableList(versions), Collections.unmodifiableList(articles), Collections.unmodifiableList(links));
    }

    private static List<Locale> locales(JsonObject root) {
        var locales = new ObjectArrayList<Locale>();
        if (root.get("locales") instanceof JsonArray given) {
            for (int i = 0, n = Math.min(given.size(), MAX_LOCALES); i < n; i++) {
                if (!(given.get(i) instanceof JsonObject object)) continue;
                String id = stringOf(object.get("id"));
                if (id == null || !LOCALE.matcher(id).matches() || indexOf(locales, id) >= 0) continue;
                String name = clean(stringOf(object.get("name")));
                if (name != null && name.length() > MAX_LOCALE_NAME) name = name.substring(0, MAX_LOCALE_NAME);
                locales.add(new Locale(id, name != null ? name : id));
            }
        }
        if (locales.isEmpty()) {
            for (String id : FALLBACK_LOCALES) locales.add(new Locale(id, id.toUpperCase(java.util.Locale.ROOT)));
        }
        return locales;
    }

    static int indexOf(List<Locale> locales, String id) {
        for (int i = 0; i < locales.size(); i++) {
            if (locales.get(i).id().equals(id)) return i;
        }
        return -1;
    }

    @Nullable
    private static Article article(JsonObject object, List<Locale> locales) {
        String id = stringOf(object.get("id"));
        String source = stringOf(object.get("sourceLocale"));
        String published = stringOf(object.get("published"));
        if (id == null || !UUID.matcher(id).matches() || source == null || indexOf(locales, source) < 0 || published == null) return null;
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
            for (var locale : locales) {
                String title = clean(stringOf(given.get(locale.id())));
                if (title != null) titles.put(locale.id(), title);
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

    private static JsonArray arrayOf(JsonObject object, String key) {
        if (!(object.get(key) instanceof JsonArray array)) throw new IllegalArgumentException("missing " + key);
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

package com.gtocore.common.quest.hot;

import com.gtocore.common.quest.hot.QuestEdits.Op;
import com.gtocore.common.quest.hot.QuestEdits.TextEntry;
import com.gtocore.common.quest.hot.QuestSource.QuestDecl;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * 官网数据集导入：审核通过的文本指令经 QuestEdits 写回章节源码，译文写成独立的语言文件，指令带来的新行的译文一并收下。
 * 不启动游戏；先算完全部结果再一次写入，逐条报告结果，基准对不上的指令不写入，留给人处理。
 */
public final class QuestTextImport {

    public static final int SCHEMA = 1;
    public static final String LANG_NAMESPACE = "gtocore_quest";
    private static final int PRINTED_REJECTIONS = 20;
    private static final Pattern ARGUMENT = Pattern.compile("%(?:\\d+\\$)?[sd]");
    private static final String[] MARKERS = { "[item]", "[itemTag]", "[fluid]", "[fluidTag]" };
    private static final String QUEST_KEY_PREFIX = "gtocore.quest.";
    private static final String TARGET_PREFIX = "q:";
    private static final String[] COLORS = { "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED", "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE",
            "GREEN", "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE" };
    private static final String[] MOD_CONSTANTS = { "BOLD", "ITALIC", "UNDERLINE", "STRIKE" };
    private static final String[] MOD_SUFFIXES = { ".bold()", ".italic()", ".underline()", ".strike()" };
    private static final String STYLE_PREFIX = "QuestStyle.";
    private static final String STYLE_CLASS = "com.gtolib.api.quest.QuestStyle";
    private static final Pattern LINK_TOKEN = Pattern.compile("link\\(\"([a-z0-9_]+/[a-z0-9_]+)\"\\)");
    private static final int URL_LIMIT = 300;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    record Dataset(int schema, @Nullable Map<String, Map<String, String>> translations, @Nullable List<Edit> edits) {}

    record Edit(@Nullable String id, int number, @Nullable Op op, @Nullable String base) {}

    record Outcome(@Nullable String id, int number, @Nullable String target, @Nullable String part, String status, @Nullable String detail,
                   @Nullable List<TextEntry> base, @Nullable List<TextEntry> current, @Nullable List<TextEntry> wanted) {}

    record Rejected(String key, String reason) {}

    record LocaleOutcome(String locale, int written, int brought, int orphaned, List<Rejected> rejected, @Nullable String problem) {}

    record Report(boolean check, int applied, int unchanged, int conflicts, int failed, List<Outcome> edits, List<LocaleOutcome> locales,
                  List<String> files, List<String> written, @Nullable String error) {}

    private QuestTextImport() {}

    public static void main(String[] args) {
        boolean check = false;
        var paths = new ObjectArrayList<String>();
        for (var arg : args) {
            if (arg.equals("--check")) check = true;
            else paths.add(arg);
        }
        if (paths.size() != 2) {
            System.err.println("用法：QuestTextImport <数据集.json> <工作树根目录> [--check]");
            System.exit(64);
        }
        System.exit(run(Path.of(paths.get(0)), Path.of(paths.get(1)), check));
    }

    static int run(Path datasetPath, Path root, boolean check) {
        var outcomes = new ObjectArrayList<Outcome>();
        var locales = new ObjectArrayList<LocaleOutcome>();
        var pending = new Object2ObjectLinkedOpenHashMap<Path, String>();
        var written = new ObjectArrayList<String>();
        String error;
        try {
            error = plan(datasetPath, root, outcomes, locales, pending);
            if (error == null && !check) error = write(root, pending, written);
        } catch (IOException | RuntimeException e) {
            error = "导入中止：" + e;
        }
        int applied = 0, unchanged = 0, conflicts = 0, failed = 0;
        for (var outcome : outcomes) {
            switch (outcome.status) {
                case "applied" -> applied++;
                case "unchanged" -> unchanged++;
                case "conflict" -> conflicts++;
                default -> failed++;
            }
        }
        var files = new ObjectArrayList<String>(pending.size());
        for (var path : pending.keySet()) files.add(relative(root, path));
        var report = new Report(check, applied, unchanged, conflicts, failed, outcomes, locales, files, written, error);
        var reportPath = root.resolve("build").resolve("quest-text").resolve("import-report.json");
        try {
            Files.createDirectories(reportPath.getParent());
            Files.writeString(reportPath, GSON.toJson(report), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("报告写入失败 " + reportPath + "：" + e);
            print(report, null);
            return 1;
        }
        print(report, reportPath);
        if (error != null) return 1;
        return conflicts + failed == 0 ? 0 : 2;
    }

    @Nullable
    private static String plan(Path datasetPath, Path root, List<Outcome> outcomes, List<LocaleOutcome> locales, Map<Path, String> pending) throws IOException {
        var dataset = GSON.fromJson(Files.readString(datasetPath, StandardCharsets.UTF_8), Dataset.class);
        if (dataset == null || dataset.schema != SCHEMA) return "数据集格式版本不是 " + SCHEMA + "，请用与本工具匹配的官网版本重新下载";
        var src = QuestSource.refresh(root.resolve("src").resolve("main").resolve("java"), null).copy();
        if (!src.parseErrors.isEmpty()) return "章节源码解析失败，未做任何修改：" + String.join("；", src.parseErrors);
        var before = new O2OOpenCacheHashMap<Path, String>();
        var brought = new TreeMap<String, String>();
        List<Edit> edits = dataset.edits == null ? Collections.emptyList() : dataset.edits;
        for (var edit : edits) outcomes.add(apply(src, edit, before, brought));
        for (var file : src.files) {
            var original = before.get(file.path);
            if (original != null && !original.equals(file.content)) pending.put(file.path, file.content);
        }
        translations(dataset, root, brought, locales, pending);
        return null;
    }

    @Nullable
    private static String write(Path root, Map<Path, String> pending, List<String> written) {
        var staged = new ObjectArrayList<Path>(pending.size());
        try {
            for (var entry : pending.entrySet()) {
                var target = entry.getKey();
                Files.createDirectories(target.getParent());
                var temp = target.resolveSibling(target.getFileName() + ".import.tmp");
                staged.add(temp);
                Files.writeString(temp, entry.getValue(), StandardCharsets.UTF_8);
            }
            int i = 0;
            for (var target : pending.keySet()) {
                Files.move(staged.get(i++), target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                written.add(relative(root, target));
            }
            return null;
        } catch (IOException e) {
            return "写入中断，已写入 " + written.size() + "/" + pending.size() + " 个文件：" + e;
        } finally {
            for (var temp : staged) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException e) {
                    System.err.println("临时文件未能删除 " + temp + "：" + e);
                }
            }
        }
    }

    private static Outcome apply(QuestSource src, Edit edit, O2OOpenCacheHashMap<Path, String> before, Map<String, String> brought) {
        var op = edit.op;
        if (op == null || op.target() == null || op.arg() == null) return fail(edit, "指令缺少目标或文本位置");
        if (!QuestEdits.TEXT.equals(op.type())) return fail(edit, "只接受 text 指令，收到 " + op.type());
        if (!(src.find(op.target()) instanceof QuestDecl decl)) return fail(edit, "找不到 " + op.target() + " 的源码声明");
        List<TextEntry> wanted, base;
        try {
            wanted = normalize(QuestEdits.decode(op.text()));
            base = normalize(QuestEdits.decode(edit.base));
        } catch (JsonParseException e) {
            return fail(edit, "指令内容不是合法的文本条目：" + e.getMessage());
        }
        var problem = problem(wanted);
        if (problem != null) return fail(edit, problem);
        var current = normalize(QuestEdits.readText(decl, op.arg()));
        var styled = new ObjectArrayList<TextEntry>(wanted.size());
        var foreign = restyle(src, decl, wanted, current, styled);
        if (foreign != null) return fail(edit, foreign);
        wanted = styled;
        if (current.equals(wanted)) {
            bring(op.target(), wanted, brought);
            return new Outcome(edit.id, edit.number, op.target(), op.arg(), "unchanged", "源码已是目标内容", null, null, null);
        }
        if (!current.equals(base)) {
            return new Outcome(edit.id, edit.number, op.target(), op.arg(), "conflict", "源码当前内容与这条修改的基准不同，未写入", base, current, wanted);
        }
        var error = QuestEdits.apply(src, Op.text(op.target(), op.arg(), wanted, imports(wanted)), before);
        if (error != null) return fail(edit, error);
        bring(op.target(), wanted, brought);
        return new Outcome(edit.id, edit.number, op.target(), op.arg(), "applied", null, null, null, null);
    }

    private static void bring(String target, List<TextEntry> entries, Map<String, String> brought) {
        if (!target.startsWith(TARGET_PREFIX)) return;
        var prefix = QUEST_KEY_PREFIX + target.substring(TARGET_PREFIX.length()).replace('/', '.') + ".";
        for (var entry : entries) {
            if (entry.isLine()) brought.put(prefix + crc32(entry.text()), entry.text());
        }
    }

    private static String crc32(String text) {
        var crc = new CRC32();
        crc.update(text.getBytes(StandardCharsets.UTF_8));
        return String.format("%08x", crc.getValue());
    }

    private static Outcome fail(Edit edit, String detail) {
        var op = edit.op;
        return new Outcome(edit.id, edit.number, op == null ? null : op.target(), op == null ? null : op.arg(), "failed", detail, null, null, null);
    }

    private static List<TextEntry> normalize(List<TextEntry> entries) {
        var out = new ObjectArrayList<TextEntry>(entries.size());
        for (var entry : entries) {
            if (entry == null || entry.kind() == null) throw new JsonParseException("条目缺少 kind");
            out.add(switch (entry.kind()) {
                case "line" -> TextEntry.line(entry.text() == null ? "" : entry.text(), entry.args() == null ? Collections.emptyList() : entry.args());
                case "blank" -> TextEntry.blank();
                case "page" -> TextEntry.page();
                case "raw" -> TextEntry.raw(entry.raw() == null ? "" : entry.raw());
                default -> throw new JsonParseException("未知的条目种类 " + entry.kind());
            });
        }
        return out;
    }

    @Nullable
    private static String problem(List<TextEntry> entries) {
        for (var entry : entries) {
            if (entry.kind().equals("raw") && entry.raw().isBlank()) return "raw 条目为空";
            if (!entry.isLine()) continue;
            var text = entry.text();
            var problem = lineProblem(text);
            if (problem != null) return problem;
            var signature = signature(text);
            if (signature == null) return "花括号不成对或嵌套：" + text;
            if (signature.size() != entry.args().size()) return "文字有 " + signature.size() + " 个占位，但带了 " + entry.args().size() + " 个参数：" + text;
        }
        return null;
    }

    @Nullable
    private static String translationProblem(String source, @Nullable String text) {
        if (text == null) return "文字为空";
        var problem = lineProblem(text);
        if (problem != null) return problem;
        var wanted = signature(source);
        if (wanted == null) return "原文的花括号不成对或嵌套";
        var signature = signature(text);
        if (signature == null) return "花括号不成对或嵌套";
        if (!wanted.equals(signature)) return "占位与原文不一致：原文 " + wanted.size() + " 个，译文 " + signature.size() + " 个，种类和顺序须相同";
        var arguments = arguments(source);
        if (!arguments.equals(arguments(text))) return "%s 参数与原文不一致：原文是 " + arguments + "，个数和顺序须相同";
        return null;
    }

    @Nullable
    private static List<String> signature(String text) {
        List<String> found = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '{') {
                int end = text.indexOf('}', i + 1);
                if (end < 0) return null;
                int nested = text.indexOf('{', i + 1);
                if (nested >= 0 && nested < end) return null;
                found.add("{}");
                i = end + 1;
                continue;
            }
            if (c == '}') return null;
            String marker = null;
            if (c == '[') {
                for (var candidate : MARKERS) {
                    if (text.startsWith(candidate, i)) marker = candidate;
                }
            }
            if (marker != null) {
                found.add(marker);
                i += marker.length();
            } else {
                i++;
            }
        }
        return found;
    }

    private static boolean hasLegacyCode(String text) {
        for (int i = 0; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if ((c == '&' || c == '§') && "0123456789abcdefklmnorABCDEFKLMNOR#".indexOf(text.charAt(i + 1)) >= 0) return true;
        }
        return false;
    }

    private static List<String> arguments(String text) {
        List<String> found = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '{') {
                int end = text.indexOf('}', i + 1);
                if (end < 0) break;
                i = end + 1;
            } else if (c == '%' && i + 1 < text.length() && text.charAt(i + 1) == '%') {
                i += 2;
            } else {
                var matcher = ARGUMENT.matcher(text).region(i, Math.min(text.length(), i + 8));
                if (c == '%' && matcher.lookingAt()) {
                    found.add(matcher.group());
                    i = matcher.end();
                } else {
                    i++;
                }
            }
        }
        return found;
    }

    @Nullable
    private static String lineProblem(String text) {
        if (text.isBlank()) return "文字为空";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c <= 0x1F || c >= 0x7F && c <= 0x9F || c >= 0x200B && c <= 0x200F || c >= 0x2028 && c <= 0x202E || c >= 0x2060 && c <= 0x2069 || c == 0xFEFF) {
                return String.format("文字带不允许的字符 U+%04X（第 %d 个字符）", (int) c, i + 1);
            }
        }
        if (hasLegacyCode(text)) return "文字不能带 & 或 § 颜色符号";
        return null;
    }

    @Nullable
    private static String restyle(QuestSource src, QuestDecl decl, List<TextEntry> wanted, List<TextEntry> current, List<TextEntry> out) {
        var raws = new OpenCacheHashSet<String>();
        var styles = new OpenCacheHashSet<String>();
        var bases = new OpenCacheHashSet<String>();
        var refs = new O2OOpenCacheHashMap<String, OpenCacheHashSet<String>>();
        for (var have : current) {
            if (have.kind().equals("raw")) raws.add(have.raw());
            if (!have.isLine()) continue;
            var slots = signature(have.text());
            if (slots == null || slots.size() != have.args().size()) continue;
            for (int i = 0; i < slots.size(); i++) {
                var arg = have.args().get(i);
                if (slots.get(i).equals("{}")) {
                    styles.add(arg);
                    bases.add(Peeled.of(arg).head);
                } else {
                    refs.computeIfAbsent(slots.get(i), k -> new OpenCacheHashSet<>()).add(arg);
                }
            }
        }
        var expr = new StringBuilder();
        for (var entry : wanted) {
            if (entry.kind().equals("raw") && !raws.contains(entry.raw())) return "raw 条目不在源码现有内容里，文本指令不能带入新代码：" + entry.raw();
            if (!entry.isLine() || entry.args().isEmpty()) {
                out.add(entry);
                continue;
            }
            var slots = signature(entry.text());
            var args = new ObjectArrayList<String>(entry.args().size());
            for (int i = 0; i < entry.args().size(); i++) {
                var arg = entry.args().get(i);
                var slot = slots.get(i);
                if (!slot.equals("{}")) {
                    var known = refs.get(slot);
                    if (known == null || !known.contains(arg)) return slot + " 的参数不在源码现状这一部分的 " + slot + " 参数里，文本指令不能带入新代码：" + arg + "（" + entry.text() + "）";
                    args.add(arg);
                } else if (styles.contains(arg)) {
                    args.add(arg);
                } else {
                    expr.setLength(0);
                    var problem = style(src, decl, arg, bases, expr);
                    if (problem != null) return problem + "（" + entry.text() + "）";
                    args.add(expr.toString());
                }
            }
            out.add(TextEntry.line(entry.text(), args));
        }
        return null;
    }

    @Nullable
    private static String style(QuestSource src, QuestDecl decl, String arg, OpenCacheHashSet<String> bases, StringBuilder out) {
        var peeled = Peeled.of(arg);
        var head = peeled.head;
        int mods = peeled.mods;
        boolean prefixed = head.startsWith(STYLE_PREFIX);
        var name = prefixed ? head.substring(STYLE_PREFIX.length()) : head;
        int mod = indexOf(MOD_CONSTANTS, name);
        if (mod >= 0) {
            mods |= 1 << mod;
            mod = Integer.numberOfTrailingZeros(mods);
            out.append(constant(decl, MOD_CONSTANTS[mod]));
            mods &= ~(1 << mod);
        } else if (indexOf(COLORS, name) >= 0) {
            out.append(prefixed ? head : constant(decl, name));
        } else if (head.length() >= 7 && head.startsWith("url(\"") && head.endsWith("\")")) {
            var problem = urlProblem(head.substring(5, head.length() - 2));
            if (problem != null) return problem + "：" + arg;
            out.append(head);
        } else if (LINK_TOKEN.matcher(head).matches()) {
            var key = head.substring(6, head.length() - 2);
            if (!(src.find(TARGET_PREFIX + key) instanceof QuestDecl target)) return "找不到链接目标任务 " + key + "：" + arg;
            out.append("link(").append(src.refText(decl.file, target, decl.file.start(decl.var))).append(')');
        } else if (bases.contains(head)) {
            out.append(head);
        } else {
            return "样式参数不在白名单里（颜色与粗斜下删常量、url(\"http(s)://…\")、link(\"章节/任务\")、源码现状这一部分已有样式的底），文本指令不能带入新代码：" + arg;
        }
        for (int m = 0; m < MOD_SUFFIXES.length; m++) {
            if ((mods & 1 << m) != 0) out.append(MOD_SUFFIXES[m]);
        }
        return null;
    }

    private record Peeled(String head, int mods) {

        static Peeled of(String expr) {
            var head = expr.strip();
            int mods = 0;
            boolean peeled = true;
            while (peeled) {
                peeled = false;
                for (int m = 0; m < MOD_SUFFIXES.length; m++) {
                    if (head.endsWith(MOD_SUFFIXES[m])) {
                        head = head.substring(0, head.length() - MOD_SUFFIXES[m].length()).strip();
                        mods |= 1 << m;
                        peeled = true;
                    }
                }
            }
            return new Peeled(head, mods);
        }
    }

    private static String constant(QuestDecl decl, String name) {
        return decl.file.fields.containsKey(name) ? STYLE_PREFIX + name : name;
    }

    private static int indexOf(String[] names, String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) return i;
        }
        return -1;
    }

    @Nullable
    private static String urlProblem(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return "网址必须以 http:// 或 https:// 开头";
        if (url.length() > URL_LIMIT) return "网址超过 " + URL_LIMIT + " 个字符";
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '"' || c == '\\' || Character.isWhitespace(c) || Character.isSpaceChar(c) || Character.isISOControl(c) || Character.getType(c) == Character.FORMAT) {
                return String.format("网址带不允许的字符 U+%04X（第 %d 个字符）", (int) c, i + 1);
            }
        }
        return null;
    }

    private static List<String> imports(List<TextEntry> entries) {
        for (var entry : entries) {
            if (!entry.isLine()) continue;
            for (var arg : entry.args()) {
                if (arg.startsWith(STYLE_PREFIX)) return Collections.singletonList(STYLE_CLASS);
            }
        }
        return Collections.emptyList();
    }

    private static void translations(Dataset dataset, Path root, Map<String, String> brought, List<LocaleOutcome> out, Map<Path, String> pending) throws IOException {
        if (dataset.translations == null || dataset.translations.isEmpty()) return;
        var zhPath = root.resolve("src/generated/resources/assets/gtocore/lang/zh_cn.json");
        Map<String, String> zh = GSON.fromJson(Files.readString(zhPath, StandardCharsets.UTF_8), new TypeToken<Map<String, String>>() {}.getType());
        var dir = root.resolve("src/main/resources/assets").resolve(LANG_NAMESPACE).resolve("lang");
        for (var locale : new TreeMap<>(dataset.translations).entrySet()) {
            if (!locale.getKey().matches("[a-z]{2,3}_[a-z]{2,3}") || locale.getValue() == null) {
                out.add(new LocaleOutcome(locale.getKey(), 0, 0, 0, Collections.emptyList(), "语言代码不合法或没有译文，已跳过"));
                continue;
            }
            var kept = new TreeMap<String, String>();
            var rejected = new ObjectArrayList<Rejected>();
            int orphaned = 0, fresh = 0;
            for (var entry : new TreeMap<>(locale.getValue()).entrySet()) {
                var source = zh.get(entry.getKey());
                boolean isNew = source == null && brought.containsKey(entry.getKey());
                if (isNew) source = brought.get(entry.getKey());
                if (source == null) {
                    orphaned++;
                    continue;
                }
                var reason = translationProblem(source, entry.getValue());
                if (reason != null) {
                    rejected.add(new Rejected(entry.getKey(), reason));
                    continue;
                }
                kept.put(entry.getKey(), entry.getValue());
                if (isNew) fresh++;
            }
            var target = dir.resolve(locale.getKey() + ".json");
            var content = GSON.toJson(kept) + "\n";
            boolean changed = Files.isRegularFile(target) ? !Files.readString(target, StandardCharsets.UTF_8).equals(content) : !kept.isEmpty();
            if (changed) pending.put(target, content);
            out.add(new LocaleOutcome(locale.getKey(), kept.size(), fresh, orphaned, rejected, null));
        }
    }

    private static String relative(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }

    private static void print(Report report, @Nullable Path reportPath) {
        var out = System.out;
        out.println(report.check ? "任务文本导入（仅检查，未写入源码和语言文件）" : "任务文本导入");
        out.println("文本指令：写入 " + report.applied + "，已是目标内容 " + report.unchanged + "，冲突 " + report.conflicts + "，失败 " + report.failed);
        for (var outcome : report.edits) {
            if (outcome.status.equals("applied") || outcome.status.equals("unchanged")) continue;
            out.println("  #" + outcome.number + " " + outcome.target + " " + outcome.part + "：" + outcome.status + "，" + outcome.detail);
        }
        for (var locale : report.locales) {
            if (locale.problem != null) {
                out.println("译文 " + locale.locale + "：" + locale.problem);
                continue;
            }
            out.println("译文 " + locale.locale + "：保留 " + locale.written + " 行（其中本次指令带来的新行 " + locale.brought + " 行），原文已不存在 " + locale.orphaned + " 行，被拒 " + locale.rejected.size() + " 行");
            for (int i = 0; i < locale.rejected.size() && i < PRINTED_REJECTIONS; i++) {
                var rejected = locale.rejected.get(i);
                out.println("  " + rejected.key + "：" + rejected.reason);
            }
            if (locale.rejected.size() > PRINTED_REJECTIONS) out.println("  其余 " + (locale.rejected.size() - PRINTED_REJECTIONS) + " 行见报告");
        }
        if (report.error != null) {
            System.err.println(report.error);
            out.println("已写入 " + report.written.size() + " 个文件" + (report.written.isEmpty() ? "" : "：" + String.join("、", report.written)));
        } else {
            out.println((report.check ? "将改动 " : "已改动 ") + report.files.size() + " 个文件");
        }
        if (reportPath != null) out.println("详细结果见 " + reportPath);
        if (report.error == null && report.conflicts + report.failed > 0) out.println("有冲突或失败的指令：按报告里的 base、current、wanted 逐条人工处理后，在官网把对应修改标记为已处理");
        else if (report.error == null && !report.check && report.applied > 0) out.println("下一步：compileJava，再 runData 重新生成语言文件；下次导出图鉴时新清单随版本进官网，这些修改在官网载入清单后标为已进入游戏");
    }
}

package com.gtocore.common.quest.hot;

import com.gtocore.common.quest.hot.QuestSource.Decl;
import com.gtocore.common.quest.hot.QuestSource.FileModel;
import com.gtocore.common.quest.hot.QuestSource.LinkDecl;
import com.gtocore.common.quest.hot.QuestSource.QuestDecl;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

/**
 * 热修改的操作与落地：每个操作都是"设成某值"的幂等语义，按源码身份定位，只依赖语法模型、不读任务书，可在后台线程执行。
 */
public final class QuestEdits {

    public static final String MOVE = "move";
    public static final String FLAG = "flag";
    public static final String AMOUNT = "amount";
    public static final String DEP = "dep";
    public static final String CREATE = "create";
    public static final String COPY = "copy";
    public static final String DELETE = "delete";
    public static final String TEXT = "text";
    public static final String MAIN_LINE = "mainLine";
    public static final String TASK_ADD = "taskAdd";
    public static final String TASK_REMOVE = "taskRemove";
    public static final String TASK_ITEM = "taskItem";
    public static final String TASK_OPT = "taskOpt";
    public static final String REWARD_ADD = "rewardAdd";
    public static final String REWARD_REMOVE = "rewardRemove";
    public static final String STYLE_IMPORT = "static com.gtolib.api.quest.QuestStyle.*";
    private static final com.google.gson.Gson GSON = new com.google.gson.Gson();
    private static final java.lang.reflect.Type ENTRIES = new com.google.gson.reflect.TypeToken<List<TextEntry>>() {}.getType();

    public record TextEntry(String kind, @Nullable String text, @Nullable List<String> args, @Nullable String raw) {

        public static TextEntry line(String text, List<String> args) {
            return new TextEntry("line", text, List.copyOf(args), null);
        }

        public static TextEntry blank() {
            return new TextEntry("blank", null, null, null);
        }

        public static TextEntry page() {
            return new TextEntry("page", null, null, null);
        }

        public static TextEntry raw(String raw) {
            return new TextEntry("raw", null, null, raw);
        }

        public boolean isLine() {
            return "line".equals(kind);
        }
    }

    public static String encode(List<TextEntry> entries) {
        return GSON.toJson(entries, ENTRIES);
    }

    public static List<TextEntry> decode(@Nullable String json) {
        if (json == null || json.isEmpty()) return List.of();
        List<TextEntry> entries = GSON.fromJson(json, ENTRIES);
        return entries == null ? List.of() : entries;
    }

    public record Op(long seq, String type, String target, @Nullable String chapter, @Nullable String arg, @Nullable String text,
                     double x, double y, int index, long value, boolean on, @Nullable List<String> imports, boolean live) {

        public static Op move(String target, double x, double y) {
            return new Op(0, MOVE, target, null, null, null, x, y, 0, 0, false, null, false);
        }

        public static Op flag(String target, String method, boolean on) {
            return new Op(0, FLAG, target, null, method, null, 0, 0, 0, 0, on, null, false);
        }

        public static Op amount(String target, int index, long value) {
            return new Op(0, AMOUNT, target, null, null, null, 0, 0, index, value, false, null, false);
        }

        public static Op dependency(String target, String prerequisite, boolean present) {
            return new Op(0, DEP, target, null, prerequisite, null, 0, 0, 0, 0, present, null, false);
        }

        public static Op create(String chapter, String id, double x, double y, String itemArgs, List<String> imports) {
            return new Op(0, CREATE, "q:" + chapter + "/" + id, chapter, id, itemArgs, x, y, 0, 0, false, List.copyOf(imports), false);
        }

        public static Op copy(String chapter, String field, String targetQuest, double x, double y) {
            return new Op(0, COPY, "c:" + chapter + ":" + field, chapter, field, targetQuest, x, y, 0, 0, false, null, false);
        }

        public static Op text(String target, String part, List<TextEntry> entries, List<String> imports) {
            return new Op(0, TEXT, target, null, part, encode(entries), 0, 0, 0, 0, false, List.copyOf(imports), false);
        }

        public static Op taskAdd(String target, int index, String argText, List<String> imports) {
            return new Op(0, TASK_ADD, target, null, null, argText, System.nanoTime(), 0, index, 0, false, List.copyOf(imports), false);
        }

        public static Op taskRemove(String target, int index) {
            return new Op(0, TASK_REMOVE, target, null, null, null, System.nanoTime(), 0, index, 0, false, null, false);
        }

        public static Op taskItem(String target, int index, String itemArgs, List<String> imports) {
            return new Op(0, TASK_ITEM, target, null, null, itemArgs, 0, 0, index, 0, false, List.copyOf(imports), false);
        }

        public static Op taskOption(String target, int index, String method, boolean on) {
            return new Op(0, TASK_OPT, target, null, method, null, 0, 0, index, 0, on, null, false);
        }

        public static Op rewardAdd(String target, String callText, List<String> imports) {
            return new Op(0, REWARD_ADD, target, null, null, callText, System.nanoTime(), 0, 0, 0, false, List.copyOf(imports), false);
        }

        public static Op rewardRemove(String target, int site) {
            return new Op(0, REWARD_REMOVE, target, null, null, null, System.nanoTime(), 0, site, 0, false, null, false);
        }

        public static Op mainLine(String chapter, String argsText) {
            return new Op(0, MAIN_LINE, "chapter:" + chapter, chapter, null, argsText, 0, 0, 0, 0, false, null, false);
        }

        public static Op delete(String target) {
            return new Op(0, DELETE, target, null, null, null, 0, 0, 0, 0, false, null, false);
        }

        Op withSeq(long seq) {
            return new Op(seq, type, target, chapter, arg, text, x, y, index, value, on, imports, live);
        }

        Op movedTo(double nx, double ny) {
            return new Op(seq, type, target, chapter, arg, text, nx, ny, index, value, on, imports, live);
        }

        Op withLive(boolean applied) {
            return new Op(seq, type, target, chapter, arg, text, x, y, index, value, on, imports, applied);
        }

        public String key() {
            return switch (type) {
                case FLAG, DEP, TEXT -> type + "|" + target + "|" + arg;
                case AMOUNT, TASK_ITEM -> type + "|" + target + "|" + index;
                case TASK_OPT -> type + "|" + target + "|" + arg + "|" + index;
                case TASK_ADD, TASK_REMOVE, REWARD_ADD, REWARD_REMOVE -> type + "|" + target + "|" + Double.doubleToLongBits(x);
                case CREATE, COPY -> "node|" + target;
                default -> type + "|" + target;
            };
        }
    }

    private QuestEdits() {}

    @Nullable
    static String apply(QuestSource src, Op op, O2OOpenCacheHashMap<Path, String> before) {
        var edit = new Edit();
        String error = switch (op.type) {
            case MOVE -> move(src, op, edit);
            case FLAG -> flag(src, op, edit);
            case AMOUNT -> amount(src, op, edit);
            case DEP -> dependency(src, op, edit);
            case CREATE -> create(src, op, edit);
            case COPY -> copy(src, op, edit);
            case DELETE -> delete(src, op, edit);
            case TEXT -> text(src, op, edit);
            case MAIN_LINE -> mainLine(src, op, edit);
            case TASK_ADD -> taskAdd(src, op, edit);
            case TASK_REMOVE -> taskRemove(src, op, edit);
            case TASK_ITEM -> taskItem(src, op, edit);
            case TASK_OPT -> taskOption(src, op, edit);
            case REWARD_ADD -> rewardAdd(src, op, edit);
            case REWARD_REMOVE -> rewardRemove(src, op, edit);
            default -> "未知操作 " + op.type;
        };
        if (error != null || edit.isEmpty()) return error;
        for (var it = edit.splices.object2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            var file = entry.getKey();
            var content = Edit.render(file, entry.getValue());
            if (content == null) return file.name() + "：修改位置重叠";
            before.putIfAbsent(file.path, file.content);
            var parseError = src.update(file, content);
            if (parseError != null) return parseError;
        }
        return null;
    }

    private static String missing(Op op) {
        return "找不到 " + op.target + " 的源码声明";
    }

    @Nullable
    private static String move(QuestSource src, Op op, Edit edit) {
        var decl = src.find(op.target);
        String x = number(op.x), y = number(op.y);
        if (decl instanceof QuestDecl quest) {
            var at = quest.calls("at");
            if (at.size() != 1) return quest.where() + "：需要恰好一个 .at(x, y)";
            var args = at.get(0).getArguments();
            edit.replace(quest.file, args.get(0), x);
            edit.replace(quest.file, args.get(1), y);
            return null;
        }
        if (decl instanceof LinkDecl link) {
            var args = link.call.getArguments();
            edit.replace(link.file, args.get(1), x);
            edit.replace(link.file, args.get(2), y);
            return null;
        }
        return missing(op);
    }

    static ObjectArrayList<MethodInvocationTree> flagCalls(QuestDecl decl, String method) {
        var calls = decl.calls(method);
        calls.removeIf(call -> !call.getArguments().isEmpty());
        return calls;
    }

    @Nullable
    private static String flag(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var calls = flagCalls(decl, op.arg);
        if (op.on == !calls.isEmpty()) return null;
        if (op.on) edit.insert(decl.file, decl.chainEnd(), "\n" + decl.callIndent() + "." + op.arg + "()");
        else for (var call : calls) edit.removeCall(decl, call);
        return null;
    }

    record AmountSite(@Nullable ExpressionTree literal, int insertAt, long value, ExpressionTree subject, boolean task, int liveIndex, boolean scaled) {}

    static ObjectArrayList<AmountSite> amountSites(QuestDecl decl) {
        var out = new ObjectArrayList<AmountSite>();
        var file = decl.file;
        int taskIndex = 0;
        for (var call : decl.calls("task")) {
            for (var arg : call.getArguments()) {
                int index = taskIndex++;
                var item = QuestItemSyntax.taskRoot(arg);
                if (item == null) continue;
                var itemArgs = QuestItemSyntax.itemArgs(item);
                if (itemArgs == null) continue;
                var args = item.getArguments();
                var count = args.size() > itemArgs.size() ? args.get(args.size() - 1) : null;
                out.add(new AmountSite(count, file.end(itemArgs.get(itemArgs.size() - 1)), count == null ? 1 : QuestSource.number(count).longValue(), itemArgs.get(0), true, index, false));
            }
        }
        int rewardIndex = 0;
        for (var call : decl.chain) {
            var name = QuestDecl.name(call);
            int first = rewardIndex;
            switch (name) {
                case "reward", "easySubsidy", "normalSubsidy", "expertSubsidy" -> rewardIndex += call.getArguments().size();
                case "scaledReward" -> rewardIndex += 3;
                default -> {
                    continue;
                }
            }
            if (!name.equals("scaledReward") && !name.equals("reward")) continue;
            var site = countSite(call);
            if (site == null) continue;
            var count = last(site);
            out.add(new AmountSite(count, -1, QuestSource.number(count).longValue(), site.getArguments().get(0), false, first, name.equals("scaledReward")));
        }
        return out;
    }

    @Nullable
    private static MethodInvocationTree countSite(MethodInvocationTree call) {
        var args = call.getArguments();
        if (args.size() >= 2 && QuestSource.number(last(call)) != null) return call;
        if (args.size() == 1 && args.get(0) instanceof MethodInvocationTree inner && inner.getArguments().size() >= 2 && QuestSource.number(last(inner)) != null) return inner;
        return null;
    }

    private static ExpressionTree last(MethodInvocationTree call) {
        var args = call.getArguments();
        return args.get(args.size() - 1);
    }

    @Nullable
    private static String amount(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var sites = amountSites(decl);
        if (op.index < 0 || op.index >= sites.size()) return decl.where() + "：第 " + (op.index + 1) + " 个数量位置不存在";
        var site = sites.get(op.index);
        if (site.value == op.value) return null;
        if (site.literal != null) edit.replace(decl.file, site.literal, Long.toString(op.value));
        else edit.insert(decl.file, site.insertAt, ", " + op.value);
        return null;
    }

    @Nullable
    private static String dependency(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var target = src.find(op.arg);
        if (target == null) return "找不到依赖目标 " + op.arg;
        if (target instanceof LinkDecl link && link.field == null) return "章节装饰里的复制节点没有字段，不能当依赖";
        for (var call : decl.calls("dependsOn")) {
            var args = call.getArguments();
            for (int i = 0; i < args.size(); i++) {
                if (src.resolve(decl.file, args.get(i)) != target) continue;
                if (!op.on) edit.removeArgument(decl, call, i);
                return null;
            }
        }
        if (!op.on) return null;
        var ref = src.refText(decl.file, target, decl.file.start(decl.var));
        var existing = decl.calls("dependsOn");
        if (existing.isEmpty()) {
            edit.insert(decl.file, decl.chainEnd(), "\n" + decl.callIndent() + ".dependsOn(" + ref + ")");
        } else {
            var call = existing.get(existing.size() - 1);
            var args = call.getArguments();
            if (args.isEmpty()) edit.insert(decl.file, decl.file.end(call) - 1, ref);
            else edit.insert(decl.file, decl.file.end(args.get(args.size() - 1)), ", " + ref);
        }
        return null;
    }

    @Nullable
    private static String create(QuestSource src, Op op, Edit edit) {
        var file = src.byChapter.get(op.chapter);
        if (file == null || file.chapterField == null || file.chapter == null) return "找不到章节 " + op.chapter + " 的源码";
        if (file.questsById.containsKey(op.arg)) return null;
        var field = op.arg.toUpperCase(java.util.Locale.ROOT);
        if (file.fields.containsKey(field)) return file.name() + "：字段 " + field + " 已存在";
        var indent = file.indentAt(file.start(file.chapter.var));
        var imports = new ObjectArrayList<String>();
        if (op.imports != null) imports.addAll(op.imports);
        imports.add("com.gtolib.api.quest.Quest");
        imports.add("com.gtolib.api.quest.task.Task");
        edit.imports(file, imports);
        edit.insert(file, file.membersEnd, "\n\n" + indent + "public static final Quest " + field + " = " + file.chapterField + ".quest(\"" + op.arg + "\", q -> q\n" +
                indent + "        .at(" + number(op.x) + ", " + number(op.y) + ")\n" +
                indent + "        .task(Task.item(" + op.text + ")));");
        return null;
    }

    @Nullable
    private static String mainLine(QuestSource src, Op op, Edit edit) {
        var file = src.byChapter.get(op.chapter);
        if (file == null || file.mainLine == null) return "章节 " + op.chapter + " 的源码里没有 .mainLine(…)";
        var args = file.mainLine.getArguments();
        edit.replaceRange(file, file.start(args.get(0)), file.end(args.get(args.size() - 1)), op.text);
        return null;
    }

    record TaskArg(MethodInvocationTree call, int argIndex, ExpressionTree arg) {}

    static ObjectArrayList<TaskArg> taskArgs(QuestDecl decl) {
        var out = new ObjectArrayList<TaskArg>();
        for (var call : decl.calls("task")) {
            var args = call.getArguments();
            for (int i = 0; i < args.size(); i++) out.add(new TaskArg(call, i, args.get(i)));
        }
        return out;
    }

    @Nullable
    static MethodInvocationTree optionCall(ExpressionTree arg, String method) {
        var e = arg;
        while (e instanceof MethodInvocationTree call && call.getMethodSelect() instanceof MemberSelectTree select) {
            if (select.getIdentifier().contentEquals(method) && call.getArguments().isEmpty()) return call;
            e = select.getExpression();
        }
        return null;
    }

    @Nullable
    private static String taskAdd(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var tasks = taskArgs(decl);
        if (op.imports != null) edit.imports(decl.file, op.imports);
        if (tasks.isEmpty()) {
            edit.insert(decl.file, decl.chainEnd(), "\n" + decl.callIndent() + ".task(" + op.text + ")");
        } else if (op.index >= tasks.size()) {
            var last = tasks.get(tasks.size() - 1);
            edit.insert(decl.file, decl.file.end(last.arg()), ", " + op.text);
        } else {
            var at = tasks.get(Math.max(0, op.index));
            edit.insert(decl.file, decl.file.start(at.arg()), op.text + ", ");
        }
        return null;
    }

    @Nullable
    private static String taskRemove(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var tasks = taskArgs(decl);
        if (op.index < 0 || op.index >= tasks.size()) return decl.where() + "：第 " + (op.index + 1) + " 个目标不存在";
        var task = tasks.get(op.index);
        edit.removeArgument(decl, task.call(), task.argIndex());
        return null;
    }

    @Nullable
    private static String taskItem(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var tasks = taskArgs(decl);
        if (op.index < 0 || op.index >= tasks.size()) return decl.where() + "：第 " + (op.index + 1) + " 个目标不存在";
        var root = QuestItemSyntax.taskRoot(tasks.get(op.index).arg());
        var itemArgs = root == null ? null : QuestItemSyntax.itemArgs(root);
        if (itemArgs == null) return decl.where() + "：第 " + (op.index + 1) + " 个目标不是 Task.item(…)";
        if (op.imports != null) edit.imports(decl.file, op.imports);
        edit.replaceRange(decl.file, decl.file.start(itemArgs.get(0)), decl.file.end(itemArgs.get(itemArgs.size() - 1)), op.text);
        return null;
    }

    @Nullable
    private static String taskOption(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var tasks = taskArgs(decl);
        if (op.index < 0 || op.index >= tasks.size()) return decl.where() + "：第 " + (op.index + 1) + " 个目标不存在";
        var arg = tasks.get(op.index).arg();
        var call = optionCall(arg, op.arg);
        if (op.on && call == null) edit.insert(decl.file, decl.file.end(arg), "." + op.arg + "()");
        else if (!op.on && call != null) edit.removeCall(decl.file, call);
        return null;
    }

    record RewardSite(MethodInvocationTree call, int argIndex) {}

    static ObjectArrayList<RewardSite> rewardSites(QuestDecl decl) {
        var out = new ObjectArrayList<RewardSite>();
        for (var call : decl.chain) {
            var name = QuestDecl.name(call);
            if (!name.equals("scaledReward") && !name.equals("reward")) continue;
            if (countSite(call) == null) continue;
            out.add(new RewardSite(call, name.equals("scaledReward") || call.getArguments().size() != 1 ? -1 : 0));
        }
        return out;
    }

    @Nullable
    private static String rewardAdd(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        if (op.imports != null) edit.imports(decl.file, op.imports);
        edit.insert(decl.file, decl.chainEnd(), "\n" + decl.callIndent() + "." + op.text);
        return null;
    }

    @Nullable
    private static String rewardRemove(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var sites = rewardSites(decl);
        if (op.index < 0 || op.index >= sites.size()) return decl.where() + "：第 " + (op.index + 1) + " 个奖励不存在";
        edit.removeCall(decl, sites.get(op.index).call());
        return null;
    }

    @Nullable
    private static String copy(QuestSource src, Op op, Edit edit) {
        var file = src.byChapter.get(op.chapter);
        if (file == null || file.chapterField == null || file.chapter == null) return "找不到章节 " + op.chapter + " 的源码";
        if (file.fields.containsKey(op.arg)) return null;
        var target = src.find(op.text);
        if (target == null) return "找不到复制目标 " + op.text;
        edit.imports(file, List.of("com.gtolib.api.quest.QuestLink"));
        var indent = file.indentAt(file.start(file.chapter.var));
        edit.insert(file, file.membersEnd, "\n\n" + indent + "public static final QuestLink " + op.arg + " = " + file.chapterField + ".copy(" + src.refText(file, target) + ", " +
                number(op.x) + ", " + number(op.y) + ");");
        return null;
    }

    static List<TextEntry> readText(QuestDecl decl, String part) {
        var out = new ObjectArrayList<TextEntry>();
        var file = decl.file;
        if (part.equals("desc")) {
            var desc = decl.desc();
            if (desc == null || !desc.valid()) return out;
            for (var call : desc.chain()) {
                var name = QuestDecl.name(call);
                var args = call.getArguments();
                if (name.equals("line") && args.isEmpty()) out.add(TextEntry.blank());
                else if (name.equals("line")) out.add(lineEntry(file, args));
                else if (name.equals("page") && args.isEmpty()) out.add(TextEntry.page());
                else out.add(TextEntry.raw(file.content.substring(decl.nameStart(call), file.end(call))));
            }
            return out;
        }
        for (var call : decl.calls(part)) {
            if (!call.getArguments().isEmpty()) out.add(lineEntry(file, call.getArguments()));
        }
        return out;
    }

    private static TextEntry lineEntry(FileModel file, List<? extends ExpressionTree> args) {
        var text = args.get(0) instanceof LiteralTree literal && literal.getValue() instanceof String value ? value : file.text(args.get(0));
        var rest = new ObjectArrayList<String>(args.size() - 1);
        for (int i = 1; i < args.size(); i++) rest.add(file.text(args.get(i)));
        return TextEntry.line(text, rest);
    }

    private static String lineCall(String name, TextEntry entry) {
        var sb = new StringBuilder(name).append('(').append(literal(entry.text() == null ? "" : entry.text()));
        if (entry.args() != null) for (var arg : entry.args()) sb.append(", ").append(arg);
        return sb.append(')').toString();
    }

    private static int anchorAfter(QuestDecl decl, String... names) {
        int end = -1;
        for (var call : decl.chain) {
            var name = QuestDecl.name(call);
            for (var wanted : names) {
                if (wanted.equals(name)) end = Math.max(end, decl.file.end(call));
            }
        }
        return end >= 0 ? end : decl.file.end(decl.calls("at").get(0));
    }

    @Nullable
    private static String text(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var entries = decode(op.text);
        var file = decl.file;
        var indent = decl.callIndent();
        boolean styled = false;
        for (var entry : entries) styled |= entry.args() != null && !entry.args().isEmpty();
        var imports = new ObjectArrayList<String>();
        if (op.imports != null) imports.addAll(op.imports);
        if (styled) imports.add(STYLE_IMPORT);
        switch (op.arg) {
            case "title" -> {
                var lines = new ObjectArrayList<TextEntry>();
                for (var entry : entries) if (entry.isLine()) lines.add(entry);
                if (lines.size() > 1) return "标题只能有一行";
                var calls = decl.calls("title");
                if (calls.size() > 1) return decl.where() + "：有多个 .title(…)";
                if (lines.isEmpty()) {
                    for (var call : calls) edit.removeCall(decl, call);
                } else if (calls.isEmpty()) {
                    edit.insert(file, file.end(decl.calls("at").get(0)), "\n" + indent + "." + lineCall("title", lines.get(0)));
                } else {
                    edit.replaceRange(file, decl.nameStart(calls.get(0)), file.end(calls.get(0)), lineCall("title", lines.get(0)));
                }
            }
            case "subtitle" -> {
                var calls = decl.calls("subtitle");
                var sb = new StringBuilder();
                for (var entry : entries) if (entry.isLine()) sb.append("\n").append(indent).append('.').append(lineCall("subtitle", entry));
                if (calls.isEmpty()) {
                    if (!sb.isEmpty()) edit.insert(file, anchorAfter(decl, "title"), sb.toString());
                } else {
                    edit.replaceRange(file, decl.receiverEnd(calls.get(0)), file.end(calls.get(0)), sb.toString());
                    for (int i = 1; i < calls.size(); i++) edit.removeCall(decl, calls.get(i));
                }
            }
            case "desc" -> {
                var desc = decl.desc();
                if (desc != null && !desc.valid()) return decl.where() + "：描述不是 d -> d.line(…) 的链式写法";
                var param = desc == null ? "d" : desc.param();
                var inner = indent + "        ";
                var sb = new StringBuilder();
                if (!entries.isEmpty()) {
                    sb.append("\n").append(indent).append(".desc(").append(param).append(" -> ").append(param);
                    for (var entry : entries) {
                        sb.append("\n").append(inner).append('.');
                        switch (entry.kind()) {
                            case "line" -> sb.append(lineCall("line", entry));
                            case "blank" -> sb.append("line()");
                            case "page" -> sb.append("page()");
                            default -> sb.append(entry.raw());
                        }
                    }
                    sb.append(')');
                }
                if (desc == null) {
                    if (!sb.isEmpty()) edit.insert(file, anchorAfter(decl, "title", "subtitle"), sb.toString());
                } else {
                    edit.replaceRange(file, decl.receiverEnd(desc.call()), file.end(desc.call()), sb.toString());
                }
            }
            default -> {
                return "未知的文本位置 " + op.arg;
            }
        }
        if (!edit.isEmpty()) edit.imports(file, imports);
        return null;
    }

    @Nullable
    private static String delete(QuestSource src, Op op, Edit edit) {
        var decl = src.find(op.target);
        if (decl == null) return null;
        if (decl.field != null) {
            var refs = src.referencesTo(decl);
            if (!refs.isEmpty()) {
                var ref = refs.get(0);
                return decl.field + " 还被 " + ref.file().name() + ":" + ref.file().line(ref.pos()) + " 等 " + refs.size() + " 处引用，先去掉这些依赖或复制再删";
            }
        }
        if (decl.var != null) {
            edit.removeDeclaration(decl);
            return null;
        }
        if (decl instanceof LinkDecl link) {
            var wrapper = linkCall(link);
            if (wrapper == null) return link.where() + "：只能删除 .link(QuestLink.of(…)) 形式的装饰复制";
            edit.removeCall(link.file, wrapper);
        }
        return null;
    }

    @Nullable
    private static MethodInvocationTree linkCall(LinkDecl link) {
        var found = new MethodInvocationTree[1];
        new TreeScanner<Void, Void>() {

            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
                if (node.getMethodSelect() instanceof MemberSelectTree select && select.getIdentifier().contentEquals("link") &&
                        node.getArguments().size() == 1 && node.getArguments().get(0) == link.call) {
                    found[0] = node;
                }
                return super.visitMethodInvocation(node, unused);
            }
        }.scan(link.file.unit, null);
        return found[0];
    }

    public static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

    static String number(double value) {
        long hundredths = Math.round(value * 100);
        if (hundredths == 0) return "0";
        if (hundredths % 100 == 0) return Long.toString(hundredths / 100);
        return BigDecimal.valueOf(hundredths, 2).stripTrailingZeros().toPlainString();
    }

    private static String literal(String text) {
        var sb = new StringBuilder(text.length() + 2).append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\') sb.append('\\');
            sb.append(c);
        }
        return sb.append('"').toString();
    }

    private record Splice(int start, int end, String text) {}

    private static final class Edit {

        private final O2OOpenCacheHashMap<FileModel, ObjectArrayList<Splice>> splices = new O2OOpenCacheHashMap<>();

        boolean isEmpty() {
            return splices.isEmpty();
        }

        private ObjectArrayList<Splice> of(FileModel file) {
            var list = splices.get(file);
            if (list == null) {
                list = new ObjectArrayList<>();
                splices.put(file, list);
            }
            return list;
        }

        void replace(FileModel file, Tree tree, String text) {
            if (file.text(tree).equals(text)) return;
            of(file).add(new Splice(file.start(tree), file.end(tree), text));
        }

        void replaceRange(FileModel file, int start, int end, String text) {
            if (file.content.substring(start, end).equals(text)) return;
            of(file).add(new Splice(start, end, text));
        }

        void insert(FileModel file, int pos, String text) {
            of(file).add(new Splice(pos, pos, text));
        }

        void removeCall(QuestDecl decl, MethodInvocationTree call) {
            of(decl.file).add(new Splice(decl.receiverEnd(call), decl.file.end(call), ""));
        }

        void removeCall(FileModel file, MethodInvocationTree call) {
            of(file).add(new Splice(file.end(((MemberSelectTree) call.getMethodSelect()).getExpression()), file.end(call), ""));
        }

        void removeArgument(QuestDecl decl, MethodInvocationTree call, int index) {
            var args = call.getArguments();
            var file = decl.file;
            if (args.size() == 1) removeCall(decl, call);
            else if (index > 0) of(file).add(new Splice(file.end(args.get(index - 1)), file.end(args.get(index)), ""));
            else of(file).add(new Splice(file.start(args.get(0)), file.start(args.get(1)), ""));
        }

        void removeDeclaration(Decl decl) {
            var file = decl.file;
            var content = file.content;
            int start = content.lastIndexOf('\n', file.start(decl.var) - 1) + 1;
            int prev = content.lastIndexOf('\n', start - 2) + 1;
            if (start >= 2 && content.substring(prev, start).isBlank()) start = prev;
            int end = file.end(decl.var);
            if (end < content.length() && content.charAt(end) == '\n') end++;
            else if (end + 1 < content.length() && content.charAt(end) == '\r' && content.charAt(end + 1) == '\n') end += 2;
            of(file).add(new Splice(start, end, ""));
        }

        void imports(FileModel file, List<String> wanted) {
            var sb = new StringBuilder();
            var added = new OpenCacheHashSet<String>();
            for (var name : wanted) {
                if (name.startsWith("static ")) {
                    if (file.staticImports.contains(name.substring(7)) || !added.add(name)) continue;
                } else if (file.imports.contains(name) || !added.add(name)) {
                    continue;
                }
                sb.append("\nimport ").append(name).append(';');
            }
            if (!sb.isEmpty()) insert(file, file.importsEnd, sb.toString());
        }

        @Nullable
        static String render(FileModel file, ObjectArrayList<Splice> list) {
            list.sort((a, b) -> a.start != b.start ? Integer.compare(b.start, a.start) : Integer.compare(b.end, a.end));
            var sb = new StringBuilder(file.content);
            int limit = Integer.MAX_VALUE;
            for (var splice : list) {
                if (splice.end > limit) return null;
                sb.replace(splice.start, splice.end, splice.text);
                limit = splice.start;
            }
            return sb.toString();
        }
    }
}

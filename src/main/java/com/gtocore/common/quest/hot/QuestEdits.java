package com.gtocore.common.quest.hot;

import com.gtocore.common.quest.hot.QuestSource.ChainDecl;
import com.gtocore.common.quest.hot.QuestSource.Decl;
import com.gtocore.common.quest.hot.QuestSource.DecoLineDecl;
import com.gtocore.common.quest.hot.QuestSource.FileModel;
import com.gtocore.common.quest.hot.QuestSource.ImageDecl;
import com.gtocore.common.quest.hot.QuestSource.LinkDecl;
import com.gtocore.common.quest.hot.QuestSource.QuestDecl;
import com.gtocore.common.quest.hot.QuestSource.TableDecl;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
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
    public static final String TASK_ADD = "taskAdd";
    public static final String TASK_REMOVE = "taskRemove";
    public static final String TASK_ITEM = "taskItem";
    public static final String TASK_OPT = "taskOpt";
    public static final String REWARD_ADD = "rewardAdd";
    public static final String REWARD_REMOVE = "rewardRemove";
    public static final String REWARD_ARG_ADD = "rewardArgAdd";
    public static final String TABLE_ENTRY_ADD = "tableEntryAdd";
    public static final String TABLE_ENTRY_REMOVE = "tableEntryRemove";
    public static final String TABLE_WEIGHT = "tableWeight";
    public static final String TABLE_AMOUNT = "tableAmount";
    public static final String TABLE_TITLE = "tableTitle";
    public static final String TABLE_NUMBER = "tableNumber";
    public static final String TABLE_CREATE = "tableCreate";
    public static final String TABLE_DELETE = "tableDelete";
    public static final String DUPLICATE = "duplicate";
    public static final String SHAPE = "shape";
    public static final String ICON_SCALE = "iconScale";
    public static final String PRESET = "preset";
    public static final String NODE_SCALE = "nodeScale";
    public static final String NODE_FILL = "nodeFill";
    public static final String ICON = "icon";
    public static final String LINK_MAIN = "linkMain";
    public static final String REWARD_TIER = "rewardTier";
    public static final String VISIBLE_TIER = "visibleTier";
    public static final String DEP_RULE = "depRule";
    public static final String DEP_HIDDEN = "depHidden";
    public static final String IMAGE_GEOM = "imageGeom";
    public static final String IMAGE_ADD = "imageAdd";
    public static final String IMAGE_DELETE = "imageDelete";
    public static final String DECO_LINE_ADD = "decoLineAdd";
    public static final String DECO_LINE_DELETE = "decoLineDelete";
    public static final String STYLE_IMPORT = "static com.gtolib.api.quest.QuestStyle.*";
    private static final com.google.gson.Gson GSON = new com.google.gson.Gson();
    private static final java.lang.reflect.Type ENTRIES = new com.google.gson.reflect.TypeToken<List<TextEntry>>() {}.getType();
    private static final java.lang.reflect.Type DUPLICATE_TYPE = new com.google.gson.reflect.TypeToken<DuplicatePayload>() {}.getType();

    public record DuplicateRef(int offset, String original, @Nullable String className, String chapter, String field) {}

    public record DuplicateDepArg(int start, int end, String chapter, String field) {}

    public record DuplicateDepCall(int callStart, int callEnd, List<DuplicateDepArg> args) {}

    public record DuplicateRemoval(int start, int end) {}

    public record DuplicatePayload(String chapterField, String sourceField, String field, int fieldOffset, int idStart, int idEnd,
                                   int xStart, int xEnd, int yStart, int yEnd, String body,
                                   List<DuplicateRef> refs, @Nullable List<DuplicateDepCall> deps,
                                   @Nullable List<DuplicateRemoval> removals, List<String> imports) {}

    public static String encodeDuplicate(DuplicatePayload payload) {
        return GSON.toJson(payload, DUPLICATE_TYPE);
    }

    @Nullable
    public static DuplicatePayload decodeDuplicate(@Nullable String json) {
        if (json == null || json.isEmpty()) return null;
        return GSON.fromJson(json, DUPLICATE_TYPE);
    }

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
                     double x, double y, int index, long value, boolean on, @Nullable List<String> imports, boolean live,
                     double w, double h) {

        public Op(long seq, String type, String target, @Nullable String chapter, @Nullable String arg, @Nullable String text,
                  double x, double y, int index, long value, boolean on, @Nullable List<String> imports, boolean live) {
            this(seq, type, target, chapter, arg, text, x, y, index, value, on, imports, live, 0, 0);
        }

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
            return dependency(target, prerequisite, present, false);
        }

        public static Op dependency(String target, String prerequisite, boolean present, boolean hidden) {
            return new Op(0, DEP, target, null, prerequisite, null, 0, 0, hidden ? 1 : 0, 0, present, null, false);
        }

        public static Op create(String chapter, String id, double x, double y, String itemArgs, List<String> imports) {
            return new Op(0, CREATE, "q:" + chapter + "/" + id, chapter, id, itemArgs, x, y, 0, 0, false, List.copyOf(imports), false);
        }

        public static Op copy(String chapter, String field, String targetQuest, double x, double y, boolean mainNode) {
            return new Op(0, COPY, "c:" + chapter + ":" + field, chapter, field, targetQuest, x, y, 0, 0, mainNode, null, false);
        }

        public static Op duplicate(String chapter, String field, String questId, String payload, double x, double y) {
            return new Op(0, DUPLICATE, "q:" + chapter + "/" + questId, chapter, field, payload, x, y, 0, 0, false, null, false);
        }

        public static Op shape(String target, String constant) {
            return new Op(0, SHAPE, target, null, constant, null, 0, 0, 0, 0, false, null, false);
        }

        public static Op iconScale(String target, double scale) {
            return new Op(0, ICON_SCALE, target, null, null, null, scale, 0, 0, 0, false, null, false);
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

        public static Op rewardRemove(String target, int entry) {
            return new Op(0, REWARD_REMOVE, target, null, null, null, System.nanoTime(), 0, entry, 0, false, null, false);
        }

        public static Op rewardArgAdd(String target, int firstEntry, int argIndex, String argText) {
            return new Op(0, REWARD_ARG_ADD, target, null, null, argText, System.nanoTime(), 0, firstEntry, argIndex, false, null, false);
        }

        public static Op tableEntryAdd(String target, int index, String callText, List<String> imports) {
            return new Op(0, TABLE_ENTRY_ADD, target, null, null, callText, System.nanoTime(), 0, index, 0, false, List.copyOf(imports), false);
        }

        public static Op tableEntryRemove(String target, int index) {
            return new Op(0, TABLE_ENTRY_REMOVE, target, null, null, null, System.nanoTime(), 0, index, 0, false, null, false);
        }

        public static Op tableWeight(String target, int index, long weight) {
            return new Op(0, TABLE_WEIGHT, target, null, null, null, 0, 0, index, weight, false, null, false);
        }

        public static Op tableAmount(String target, int index, long value) {
            return new Op(0, TABLE_AMOUNT, target, null, null, null, 0, 0, index, value, false, null, false);
        }

        public static Op tableTitle(String target, String title) {
            return new Op(0, TABLE_TITLE, target, null, null, title, 0, 0, 0, 0, false, null, false);
        }

        public static Op tableNumber(String target, String method, long value) {
            return new Op(0, TABLE_NUMBER, target, null, method, null, 0, 0, 0, value, false, null, false);
        }

        public static Op tableCreate(String id, String field, String title) {
            return new Op(0, TABLE_CREATE, "t:" + id, null, field, title, 0, 0, 0, 0, false, null, false);
        }

        public static Op tableDelete(String id) {
            return new Op(0, TABLE_DELETE, "t:" + id, null, null, null, 0, 0, 0, 0, false, null, false);
        }

        public static Op preset(String target, String constant) {
            return new Op(0, PRESET, target, null, constant, null, 0, 0, 0, 0, false, null, false);
        }

        public static Op nodeScale(String target, double scale) {
            return new Op(0, NODE_SCALE, target, null, null, null, scale, 0, 0, 0, false, null, false);
        }

        public static Op nodeFill(String target, int rgb) {
            return new Op(0, NODE_FILL, target, null, null, null, 0, 0, 0, rgb, false, null, false);
        }

        public static Op icon(String target, String argsText, List<String> imports, boolean restore, @Nullable String oldGroup) {
            return new Op(0, ICON, target, null, oldGroup, argsText, 0, 0, 0, 0, restore, List.copyOf(imports), false);
        }

        public static Op linkMain(String target, boolean on) {
            return new Op(0, LINK_MAIN, target, null, null, null, 0, 0, 0, 0, on, null, false);
        }

        public static Op rewardTier(String target, int index, String group, String expression, int argIndex, boolean on, @Nullable String restoreTier) {
            return new Op(0, REWARD_TIER, target, restoreTier, group, expression, 0, 0, index, argIndex, on, null, false);
        }

        public static Op visibleTier(String target, String tiers, boolean present) {
            return new Op(0, VISIBLE_TIER, target, null, tiers, null, 0, 0, 0, 0, present, null, false);
        }

        public static Op dependencyRule(String target, String rule, long count) {
            return new Op(0, DEP_RULE, target, null, rule, null, 0, 0, 0, count, false, null, false);
        }

        public static Op dependencyHidden(String target, String prerequisite, boolean hidden) {
            return new Op(0, DEP_HIDDEN, target, null, prerequisite, null, 0, 0, 0, 0, hidden, null, false);
        }

        public static Op delete(String target) {
            return new Op(0, DELETE, target, null, null, null, 0, 0, 0, 0, false, null, false);
        }

        public static Op imageGeom(String target, double x, double y, double w, double h) {
            return new Op(0, IMAGE_GEOM, target, null, null, null, x, y, 0, 0, false, null, false, w, h);
        }

        public static Op imageAdd(String chapter, String target, String textureField, double x, double y, double w, double h, @Nullable String text, int index) {
            return new Op(0, IMAGE_ADD, target, chapter, textureField, text, x, y, index, 0, false, null, false, w, h);
        }

        public static Op imageDelete(String chapter, String target, String identities) {
            return new Op(0, IMAGE_DELETE, target, chapter, identities, null, 0, 0, 0, 0, false, null, false, 0, 0);
        }

        public static Op decoLineAdd(String chapter, String target, String fromText, String toText, @Nullable String text, int index) {
            return new Op(0, DECO_LINE_ADD, target, chapter, fromText + "|" + toText, text, 0, 0, index, 0, false, null, false);
        }

        public static Op decoLineDelete(String chapter, String target, String identities) {
            return new Op(0, DECO_LINE_DELETE, target, chapter, identities, null, 0, 0, 0, 0, false, null, false, 0, 0);
        }

        Op withSeq(long seq) {
            return new Op(seq, type, target, chapter, arg, text, x, y, index, value, on, imports, live, w, h);
        }

        Op movedTo(double nx, double ny) {
            return new Op(seq, type, target, chapter, arg, text, nx, ny, index, value, on, imports, live, w, h);
        }

        Op withLive(boolean applied) {
            return new Op(seq, type, target, chapter, arg, text, x, y, index, value, on, imports, applied, w, h);
        }

        public String key() {
            return switch (type) {
                case FLAG, DEP, TEXT, TABLE_NUMBER, PRESET -> type + "|" + target + "|" + arg;
                case ICON -> type + "|" + target + "|" + text;
                case LINK_MAIN -> type + "|" + target + "|" + on;
                case NODE_SCALE -> type + "|" + target + "|" + Double.doubleToLongBits(x);
                case NODE_FILL -> type + "|" + target + "|" + value;
                case REWARD_TIER -> type + "|" + target + "|" + index;
                case VISIBLE_TIER -> type + "|" + target + "|" + arg;
                case DEP_RULE -> type + "|" + target;
                case DEP_HIDDEN -> type + "|" + target + "|" + arg;
                case AMOUNT, TASK_ITEM, TABLE_WEIGHT, TABLE_AMOUNT -> type + "|" + target + "|" + index;
                case TABLE_ENTRY_ADD, TABLE_ENTRY_REMOVE -> type + "|" + target + "|" + Double.doubleToLongBits(x);
                case TASK_OPT -> type + "|" + target + "|" + arg + "|" + index;
                case TASK_ADD, TASK_REMOVE, REWARD_ADD, REWARD_REMOVE, REWARD_ARG_ADD -> type + "|" + target + "|" + Double.doubleToLongBits(x);
                case CREATE, COPY, DUPLICATE -> "node|" + target;
                case IMAGE_GEOM, IMAGE_ADD, IMAGE_DELETE -> type + "|" + target;
                case DECO_LINE_ADD -> type + "|" + target + "|" + arg;
                case DECO_LINE_DELETE -> type + "|" + target;
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
            case DUPLICATE -> duplicate(src, op, edit);
            case SHAPE -> shape(src, op, edit);
            case ICON_SCALE -> iconScale(src, op, edit);
            case PRESET -> preset(src, op, edit);
            case NODE_SCALE -> nodeScale(src, op, edit);
            case NODE_FILL -> nodeFill(src, op, edit);
            case ICON -> icon(src, op, edit);
            case LINK_MAIN -> linkMain(src, op, edit);
            case REWARD_TIER -> rewardTier(src, op, edit);
            case VISIBLE_TIER -> visibleTier(src, op, edit);
            case DEP_RULE -> dependencyRule(src, op, edit);
            case DEP_HIDDEN -> dependencyHidden(src, op, edit);
            case DELETE -> delete(src, op, edit);
            case TEXT -> text(src, op, edit);
            case TASK_ADD -> taskAdd(src, op, edit);
            case TASK_REMOVE -> taskRemove(src, op, edit);
            case TASK_ITEM -> taskItem(src, op, edit);
            case TASK_OPT -> taskOption(src, op, edit);
            case REWARD_ADD -> rewardAdd(src, op, edit);
            case REWARD_REMOVE -> rewardRemove(src, op, edit);
            case REWARD_ARG_ADD -> rewardArgAdd(src, op, edit);
            case TABLE_ENTRY_ADD, TABLE_ENTRY_REMOVE, TABLE_WEIGHT, TABLE_AMOUNT, TABLE_TITLE, TABLE_NUMBER, TABLE_DELETE -> table(src, op, edit);
            case TABLE_CREATE -> tableCreate(src, op, edit);
            case IMAGE_GEOM -> imageGeom(src, op, edit);
            case IMAGE_ADD -> imageAdd(src, op, edit);
            case IMAGE_DELETE -> imageDelete(src, op, edit);
            case DECO_LINE_ADD -> decoLineAdd(src, op, edit);
            case DECO_LINE_DELETE -> decoLineDelete(src, op, edit);
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
                var itemArgs = item == null ? null : QuestItemSyntax.itemArgs(item);
                if (itemArgs != null) {
                    var args = item.getArguments();
                    var count = args.size() > itemArgs.size() ? args.get(args.size() - 1) : null;
                    out.add(new AmountSite(count, file.end(itemArgs.get(itemArgs.size() - 1)), count == null ? 1 : QuestSource.number(count).longValue(), itemArgs.get(0), true, index, false));
                    continue;
                }
                var root = factoryCall(arg, "Task");
                if (root == null || root.getArguments().size() < 2) continue;
                var args = root.getArguments();
                ExpressionTree count = switch (QuestDecl.name(root)) {
                    case "tag", "kill", "stat" -> args.get(args.size() - 1);
                    case "anyOf" -> args.get(0);
                    default -> null;
                };
                if (count == null || QuestSource.number(count) == null) continue;
                out.add(new AmountSite(count, -1, QuestSource.number(count).longValue(), args.get(count == args.get(0) ? 1 : 0), true, index, false));
            }
        }
        for (var entry : rewardEntries(decl)) {
            boolean scaled = false;
            if (entry.count != null) {
                out.add(new AmountSite(entry.count, -1, QuestSource.number(entry.count).longValue(), entry.subject, false, entry.liveIndex, scaled));
            } else if (entry.root != null && QuestDecl.name(entry.root).equals("item") && entry.root.getArguments().size() == 1) {
                out.add(new AmountSite(null, file.end(entry.subject), 1, entry.subject, false, entry.liveIndex, false));
            }
        }
        return out;
    }

    record RewardEntry(MethodInvocationTree call, int argIndex, String group, @Nullable String tier, int liveIndex, @Nullable MethodInvocationTree root,
                       @Nullable ExpressionTree count, ExpressionTree subject) {

        ExpressionTree expression() {
            return call.getArguments().get(argIndex);
        }
    }

    static ObjectArrayList<RewardEntry> rewardEntries(QuestDecl decl) {
        var out = new ObjectArrayList<RewardEntry>();
        int liveIndex = 0;
        for (var call : decl.chain) {
            var name = QuestDecl.name(call);
            var args = call.getArguments();
            switch (name) {
                case "rewardFor" -> {
                    if (args.isEmpty()) continue;
                    String tier = tierId(args.get(0));
                    for (int i = 1; i < args.size(); i++) {
                        var root = factoryCall(args.get(i), "Reward");
                        var rootArgs = root == null ? null : root.getArguments();
                        var count = rootArgs != null && !rootArgs.isEmpty() && QuestSource.number(last(root)) != null ? last(root) : null;
                        out.add(new RewardEntry(call, i, name, tier, liveIndex++, root, count, rootArgs == null || rootArgs.isEmpty() ? args.get(i) : rootArgs.get(0)));
                    }
                }
                case "reward" -> {
                    for (int i = 0; i < args.size(); i++) {
                        var root = factoryCall(args.get(i), "Reward");
                        var rootArgs = root == null ? null : root.getArguments();
                        var count = rootArgs != null && !rootArgs.isEmpty() && QuestSource.number(last(root)) != null ? last(root) : null;
                        out.add(new RewardEntry(call, i, name, null, liveIndex++, root, count, rootArgs == null || rootArgs.isEmpty() ? args.get(i) : rootArgs.get(0)));
                    }
                }
                default -> {}
            }
        }
        return out;
    }

    @Nullable
    private static String tierId(ExpressionTree arg) {
        var e = arg;
        while (e instanceof MethodInvocationTree call && call.getMethodSelect() instanceof MemberSelectTree select) e = select.getExpression();
        if (e instanceof MemberSelectTree select) return select.getIdentifier().toString();
        if (e instanceof IdentifierTree id) return id.getName().toString();
        return null;
    }

    @Nullable
    static MethodInvocationTree factoryCall(ExpressionTree arg, String owner) {
        var e = arg;
        while (e instanceof MethodInvocationTree call && call.getMethodSelect() instanceof MemberSelectTree select) {
            if (select.getExpression() instanceof IdentifierTree id && id.getName().contentEquals(owner)) return call;
            e = select.getExpression();
        }
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
        for (var call : decl.dependencyCalls()) {
            var args = call.getArguments();
            for (int i = 0; i < args.size(); i++) {
                if (src.resolve(decl.file, args.get(i)) != target) continue;
                if (!op.on) edit.removeArgument(decl, call, i);
                return null;
            }
        }
        if (!op.on) return null;
        var ref = src.refText(decl.file, target, decl.file.start(decl.var));
        insertDependency(edit, decl, op.index == 1 ? "hiddenDependsOn" : "dependsOn", ref);
        return null;
    }

    private static void insertDependency(Edit edit, QuestDecl decl, String method, String ref) {
        var existing = decl.calls(method);
        if (existing.isEmpty()) {
            edit.insert(decl.file, decl.chainEnd(), "\n" + decl.callIndent() + "." + method + "(" + ref + ")");
            return;
        }
        var call = existing.get(existing.size() - 1);
        var args = call.getArguments();
        if (args.isEmpty()) edit.insert(decl.file, decl.file.end(call) - 1, ref);
        else edit.insert(decl.file, decl.file.end(args.get(args.size() - 1)), ", " + ref);
    }

    @Nullable
    private static String dependencyHidden(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var target = src.find(op.arg);
        if (target == null) return "找不到依赖目标 " + op.arg;
        for (var call : decl.dependencyCalls()) {
            var args = call.getArguments();
            for (int i = 0; i < args.size(); i++) {
                if (src.resolve(decl.file, args.get(i)) != target) continue;
                if (QuestSource.ChainDecl.hiddenDependency(call) == op.on) return null;
                var ref = decl.file.text(args.get(i));
                edit.removeArgument(decl, call, i);
                insertDependency(edit, decl, op.on ? "hiddenDependsOn" : "dependsOn", ref);
                return null;
            }
        }
        return decl.where() + "：依赖位置已变化，请重新打开";
    }

    @Nullable
    private static String visibleTier(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var file = decl.file;
        var calls = decl.calls("onlyVisibleOn");
        if (!op.on) {
            for (var call : calls) edit.removeCall(decl, call);
            return null;
        }
        edit.imports(file, List.of("com.gtolib.api.quest.QuestTierSet"));
        var text = "onlyVisibleOn(QuestTierSet." + op.arg() + ")";
        if (calls.isEmpty()) {
            edit.insert(file, decl.chainEnd(), "\n" + decl.callIndent() + "." + text);
        } else {
            edit.replaceRange(file, decl.nameStart(calls.get(0)), file.end(calls.get(0)), text);
            for (int i = 1; i < calls.size(); i++) edit.removeCall(decl, calls.get(i));
        }
        return null;
    }

    @Nullable
    private static String dependencyRule(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        for (var name : List.of("requireAny", "requireAllStarted", "requireAnyStarted", "requireCount")) {
            for (var call : decl.calls(name)) edit.removeCall(decl, call);
        }
        var call = switch (op.arg()) {
            case "ONE_COMPLETED" -> "requireAny()";
            case "ALL_STARTED" -> "requireAllStarted()";
            case "ANY_STARTED" -> "requireAnyStarted()";
            case "COUNT" -> "requireCount(" + op.value + ")";
            default -> null;
        };
        if (call != null) edit.insert(decl.file, decl.chainEnd(), "\n" + decl.callIndent() + "." + call);
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
    private static String preset(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var calls = decl.calls("preset");
        var file = decl.file;
        if (op.arg().isEmpty()) {
            for (var call : calls) edit.removeCall(decl, call);
            return null;
        }
        var text = "QuestPreset." + op.arg();
        if (!calls.isEmpty()) {
            var args = calls.get(0).getArguments();
            if (args.size() == 1) edit.replace(file, args.get(0), text);
            else edit.replaceRange(file, decl.nameStart(calls.get(0)), file.end(calls.get(0)), "preset(" + text + ")");
        } else {
            var at = decl.calls("at");
            if (at.size() != 1) return decl.where() + "：需要恰好一个 .at(x, y)";
            edit.insert(file, file.end(at.get(0)), "\n" + decl.callIndent() + ".preset(" + text + ")");
        }
        edit.imports(file, List.of("com.gtolib.api.quest.QuestPreset"));
        return null;
    }

    @Nullable
    private static String nodeScale(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var file = decl.file;
        var calls = decl.calls("nodeScale");
        if (!calls.isEmpty() && !calls.get(0).getArguments().isEmpty()) {
            edit.replace(file, calls.get(0).getArguments().get(0), number(op.x()));
            return null;
        }
        var at = decl.calls("at");
        if (at.size() != 1) return decl.where() + "：需要恰好一个 .at(x, y)";
        edit.insert(file, file.end(at.get(0)), "\n" + decl.callIndent() + ".nodeScale(" + number(op.x()) + ")");
        return null;
    }

    @Nullable
    private static String nodeFill(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var file = decl.file;
        var calls = decl.calls("nodeFill");
        if (op.value < 0) {
            for (var call : calls) edit.removeCall(decl, call);
            return null;
        }
        var text = String.format("0x%06X", op.value & 0xFFFFFF);
        if (!calls.isEmpty() && !calls.get(0).getArguments().isEmpty()) {
            edit.replace(file, calls.get(0).getArguments().get(0), text);
            return null;
        }
        var at = decl.calls("at");
        if (at.size() != 1) return decl.where() + "：需要恰好一个 .at(x, y)";
        edit.insert(file, file.end(at.get(0)), "\n" + decl.callIndent() + ".nodeFill(" + text + ")");
        return null;
    }

    @Nullable
    private static String icon(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var file = decl.file;
        var iconCalls = decl.calls("icon");
        var stackCalls = decl.calls("iconStack");
        if (op.on) {
            for (var call : iconCalls) edit.removeCall(decl, call);
            for (var call : stackCalls) edit.removeCall(decl, call);
            if (op.text().isEmpty()) return null;
            var at = decl.calls("at");
            if (at.size() != 1) return decl.where() + "：需要恰好一个 .at(x, y)";
            var text = op.arg() != null && op.arg().equals("iconStack") ? op.text() : "icon(" + op.text() + ")";
            edit.insert(file, file.end(at.get(0)), "\n" + decl.callIndent() + "." + text);
            return null;
        }
        if (op.imports != null) edit.imports(file, op.imports);
        for (var call : stackCalls) edit.removeCall(decl, call);
        if (!iconCalls.isEmpty()) {
            var args = iconCalls.get(0).getArguments();
            if (args.size() == 1) edit.replace(file, args.get(0), op.text());
            else edit.replaceRange(file, decl.nameStart(iconCalls.get(0)), file.end(iconCalls.get(0)), "icon(" + op.text() + ")");
            return null;
        }
        var at = decl.calls("at");
        if (at.size() != 1) return decl.where() + "：需要恰好一个 .at(x, y)";
        edit.insert(file, file.end(at.get(0)), "\n" + decl.callIndent() + ".icon(" + op.text() + ")");
        return null;
    }

    @Nullable
    private static String linkMain(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof LinkDecl decl)) return missing(op);
        var file = decl.file;
        int end = file.end(decl.call);
        int pos = end;
        while (pos < file.content.length() && Character.isWhitespace(file.content.charAt(pos))) pos++;
        boolean present = file.content.startsWith(".mainNode()", pos);
        if (op.on && !present) edit.insert(file, end, ".mainNode()");
        else if (!op.on && present) edit.replaceRange(file, pos, pos + ".mainNode()".length(), "");
        return null;
    }

    @Nullable
    private static String rewardTier(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var file = decl.file;
        if (op.on) {
            var entries = rewardEntries(decl);
            if (op.index < 0 || op.index >= entries.size()) return decl.where() + "：奖励位置已变化，请重新打开";
            var entry = entries.get(op.index);
            if (op.arg().equals(effectiveTier(entry))) return null;
            var expr = file.text(entry.expression());
            edit.imports(file, List.of("com.gtolib.api.quest.QuestTierSet"));
            if (entry.group.equals("rewardFor") && entry.call.getArguments().size() == 2) {
                edit.replace(file, entry.call.getArguments().get(0), "QuestTierSet." + op.arg());
                return null;
            }
            if (entry.call.getArguments().size() == 1) {
                edit.replaceRange(file, decl.nameStart(entry.call), file.end(entry.call), "rewardFor(QuestTierSet." + op.arg() + ", " + expr + ")");
                return null;
            }
            edit.removeArgument(decl, entry.call, entry.argIndex);
            edit.insert(file, decl.chainEnd(), "\n" + decl.callIndent() + ".rewardFor(QuestTierSet." + op.arg() + ", " + expr + ")");
            return null;
        }
        var expr = op.text();
        var entries = rewardEntries(decl);
        RewardEntry found = null;
        for (var entry : entries) {
            if (!file.text(entry.expression()).equals(expr)) continue;
            found = entry;
            break;
        }
        if (found == null) return decl.where() + "：找不到要恢复的奖励";
        edit.imports(file, List.of("com.gtolib.api.quest.QuestTierSet"));
        if (found.group.equals("rewardFor") && found.call.getArguments().size() == 2) {
            var text = op.arg().equals("rewardFor") ? "rewardFor(QuestTierSet." + (op.chapter() == null ? "ALL" : op.chapter()) + ", " + expr + ")" :
                    op.arg() + "(" + expr + ")";
            edit.replaceRange(file, decl.nameStart(found.call), file.end(found.call), text);
            return null;
        }
        edit.removeArgument(decl, found.call, found.argIndex);
        var calls = decl.calls(op.arg());
        if (!calls.isEmpty()) {
            var args = calls.get(0).getArguments();
            if (op.value < 0 || op.value >= args.size()) edit.insert(file, file.end(args.get(args.size() - 1)), ", " + expr);
            else edit.insert(file, file.start(args.get((int) op.value)), expr + ", ");
        } else if (op.arg().equals("rewardFor")) {
            String tier = op.chapter();
            edit.insert(file, decl.chainEnd(), "\n" + decl.callIndent() + ".rewardFor(QuestTierSet." + (tier == null ? "ALL" : tier) + ", " + expr + ")");
        } else {
            edit.insert(file, decl.chainEnd(), "\n" + decl.callIndent() + "." + op.arg() + "(" + expr + ")");
        }
        return null;
    }

    static String effectiveTier(RewardEntry entry) {
        return entry.tier != null ? entry.tier : "ALL";
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
        var entries = rewardEntries(decl);
        if (op.index < 0 || op.index >= entries.size()) return decl.where() + "：第 " + (op.index + 1) + " 个奖励不存在";
        var entry = entries.get(op.index);
        edit.removeArgument(decl, entry.call, entry.argIndex);
        return null;
    }

    @Nullable
    private static String rewardArgAdd(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var entries = rewardEntries(decl);
        if (op.index < 0 || op.index >= entries.size()) return decl.where() + "：第 " + (op.index + 1) + " 个奖励不存在";
        var args = entries.get(op.index).call.getArguments();
        int at = (int) op.value;
        if (at >= args.size()) edit.insert(decl.file, decl.file.end(args.get(args.size() - 1)), ", " + op.text);
        else edit.insert(decl.file, decl.file.start(args.get(Math.max(0, at))), op.text + ", ");
        return null;
    }

    record TableEntry(MethodInvocationTree call, ExpressionTree reward, @Nullable ExpressionTree count, @Nullable ExpressionTree weight) {}

    static ObjectArrayList<TableEntry> tableEntries(TableDecl decl) {
        var out = new ObjectArrayList<TableEntry>();
        for (var call : decl.calls("entry")) {
            var args = call.getArguments();
            if (args.isEmpty()) continue;
            var root = factoryCall(args.get(0), "Reward");
            var count = root != null && !root.getArguments().isEmpty() && QuestSource.number(last(root)) != null ? last(root) : null;
            out.add(new TableEntry(call, args.get(0), count, args.size() >= 2 && QuestSource.number(args.get(1)) != null ? args.get(1) : null));
        }
        return out;
    }

    @Nullable
    private static String table(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof TableDecl decl)) return missing(op);
        var file = decl.file;
        var entries = tableEntries(decl);
        switch (op.type) {
            case TABLE_DELETE -> edit.removeDeclaration(decl);
            case TABLE_TITLE -> setCall(decl, edit, "title", literal(op.text));
            case TABLE_NUMBER -> setCall(decl, edit, op.arg, Long.toString(op.value));
            case TABLE_ENTRY_ADD -> {
                if (op.imports != null) edit.imports(file, op.imports);
                int at = op.index < 0 || op.index >= entries.size() ? decl.chainEnd() : decl.receiverEnd(entries.get(op.index).call);
                edit.insert(file, at, "\n" + decl.callIndent() + "." + op.text);
            }
            default -> {
                if (op.index < 0 || op.index >= entries.size()) return decl.where() + "：第 " + (op.index + 1) + " 项内容不存在";
                var entry = entries.get(op.index);
                if (op.type.equals(TABLE_ENTRY_REMOVE)) {
                    edit.removeCall(decl, entry.call);
                } else if (op.type.equals(TABLE_WEIGHT)) {
                    if (entry.weight != null) edit.replace(file, entry.weight, Long.toString(op.value));
                    else edit.insert(file, file.end(entry.reward), ", " + op.value);
                } else {
                    if (entry.count == null) return decl.where() + "：第 " + (op.index + 1) + " 项内容没有数量";
                    edit.replace(file, entry.count, Long.toString(op.value));
                }
            }
        }
        return null;
    }

    private static void setCall(TableDecl decl, Edit edit, String method, String argText) {
        var calls = decl.calls(method);
        if (!calls.isEmpty() && !calls.get(0).getArguments().isEmpty()) {
            edit.replace(decl.file, calls.get(0).getArguments().get(0), argText);
            return;
        }
        var titles = decl.calls("title");
        int at = titles.isEmpty() || method.equals("title") ? decl.receiverEnd(decl.chain.get(0)) : decl.file.end(titles.get(0));
        edit.insert(decl.file, at, "\n" + decl.callIndent() + "." + method + "(" + argText + ")");
    }

    @Nullable
    private static String tableCreate(QuestSource src, Op op, Edit edit) {
        var file = src.tableFile();
        if (file == null || file.membersEnd < 0) return "找不到 " + QuestSource.TABLES_CLASS + ".java";
        if (src.find(op.target) != null || file.fields.containsKey(op.arg)) return file.name() + "：奖励表 " + op.target.substring(2) + " 已存在";
        var indent = file.tables.isEmpty() ? "    " : file.indentAt(file.start(file.tables.get(0).var));
        edit.insert(file, file.membersEnd, "\n\n" + indent + "public static final RewardTable " + op.arg + " = RewardTable.of(" + literal(op.target.substring(2)) + ", t -> t\n" +
                indent + "        .title(" + literal(op.text) + "));");
        return null;
    }

    @Nullable
    private static String imageGeom(QuestSource src, Op op, Edit edit) {
        var decl = src.find(op.target);
        if (!(decl instanceof ImageDecl image) || !image.valid || image.of == null) return "找不到图片源码 " + op.target;
        var args = image.of.getArguments();
        edit.replace(image.file, args.get(1), number(op.x));
        edit.replace(image.file, args.get(2), number(op.y));
        edit.replace(image.file, args.get(3), number(op.w));
        edit.replace(image.file, args.get(4), number(op.h));
        return null;
    }

    @Nullable
    private static String imageAdd(QuestSource src, Op op, Edit edit) {
        var file = src.byChapter.get(op.chapter);
        if (file == null || file.chapterField == null || file.classBodyEnd < 0) return "找不到章节 " + op.chapter + " 的源码";
        var call = op.text;
        if (call == null || call.isEmpty()) {
            call = ".image(QuestImage.of(QuestTextures." + op.arg + ", " + number(op.x) + ", " + number(op.y) + ", " + number(op.w) + ", " + number(op.h) + "))";
        }
        if (file.images.isEmpty()) {
            var indent = file.chapter == null ? "    " : file.indentAt(file.start(file.chapter.var));
            edit.insert(file, file.classBodyEnd, "\n\n" + indent + "static {\n" + indent + "    " + file.chapterField + ".decorate(d -> d\n" +
                    indent + "        " + call + ");\n" + indent + "}");
        } else if (op.index >= 0 && op.index < file.images.size()) {
            var at = file.images.get(op.index);
            int receiverEnd = file.end(((MemberSelectTree) at.call.getMethodSelect()).getExpression());
            int lineStart = file.content.lastIndexOf('\n', receiverEnd - 1) + 1;
            var indent = file.content.substring(lineStart, receiverEnd).isBlank() ? file.indentAt(receiverEnd) : file.indentAt(receiverEnd) + "        ";
            edit.insert(file, receiverEnd, "\n" + indent + call);
        } else {
            var last = file.images.get(file.images.size() - 1);
            int lineStart = file.content.lastIndexOf('\n', last.charStart - 1) + 1;
            var indent = file.content.substring(lineStart, last.charStart).isBlank() ? file.indentAt(last.charStart) : file.indentAt(last.charStart) + "        ";
            edit.insert(file, last.chainEnd, "\n" + indent + call);
        }
        edit.imports(file, List.of("com.gtocore.data.quest.QuestTextures", "com.gtolib.api.quest.QuestImage"));
        return null;
    }

    private static int addIndex(String identity) {
        int at = identity.lastIndexOf(':');
        if (at < 0 || at + 1 >= identity.length()) return -1;
        try {
            return Integer.parseInt(identity.substring(at + 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Nullable
    private static String imageDelete(QuestSource src, Op op, Edit edit) {
        var file = src.byChapter.get(op.chapter);
        if (file == null) return "找不到章节 " + op.chapter + " 的源码";
        if (op.arg == null || op.arg.isEmpty()) return "没有要删除的图片";
        var decls = new ObjectArrayList<ImageDecl>(4);
        for (var identity : op.arg.split(",")) {
            if (identity.isEmpty()) continue;
            var decl = src.find(identity);
            ImageDecl image = decl instanceof ImageDecl found && found.valid ? found : null;
            if (image == null && identity.startsWith("i:" + op.chapter + ":add:")) {
                int index = addIndex(identity);
                if (index >= 0 && index < file.images.size()) {
                    var candidate = file.images.get(index);
                    if (candidate.valid && candidate.textureField != null) image = candidate;
                }
            }
            if (image == null) return "找不到图片源码 " + identity;
            decls.add(image);
        }
        decls.sort((a, b) -> Integer.compare(b.charStart, a.charStart));
        var replacedBodies = new ObjectArrayList<MethodInvocationTree>(2);
        for (var image : decls) {
            if (replacedBodies.contains(image.decorate)) continue;
            int total = 0, deleted = 0;
            for (var other : file.images) {
                if (other.decorate != image.decorate) continue;
                total++;
                if (decls.contains(other)) deleted++;
            }
            if (total == 0 || total != deleted) continue;
            var argument = image.decorate.getArguments().get(0);
            if (!(argument instanceof LambdaExpressionTree lambda) || !(lambda.getBody() instanceof ExpressionTree body)) continue;
            if (decorationHasOtherCalls(lambda, "image")) continue;
            edit.replaceRange(file, file.start(body), file.end(body), "{}");
            replacedBodies.add(image.decorate);
        }
        for (var image : decls) {
            if (replacedBodies.contains(image.decorate)) continue;
            var content = image.file.content;
            int start = image.file.end(((MemberSelectTree) image.call.getMethodSelect()).getExpression());
            int end = image.chainEnd;
            int lineStart = content.lastIndexOf('\n', start - 1) + 1;
            int lineEnd = content.indexOf('\n', end);
            if (lineEnd < 0) lineEnd = content.length();
            if (content.substring(lineStart, start).isBlank() && content.substring(end, lineEnd).isBlank()) {
                start = lineStart;
                end = lineEnd < content.length() ? lineEnd + 1 : lineEnd;
            }
            edit.replaceRange(image.file, start, end, "");
        }
        return null;
    }

    @Nullable
    private static String decoLineAdd(QuestSource src, Op op, Edit edit) {
        var file = src.byChapter.get(op.chapter);
        if (file == null || file.chapterField == null || file.classBodyEnd < 0) return "找不到章节 " + op.chapter + " 的源码";
        var call = op.text;
        if (call == null || call.isEmpty()) {
            var refs = op.arg == null ? new String[0] : op.arg.split("\\|", 2);
            if (refs.length != 2) return "装饰线端点缺失";
            call = ".line(" + refs[0] + ", " + refs[1] + ")";
        }
        if (op.index >= 0 && op.index < file.decoLines.size()) {
            var at = file.decoLines.get(op.index);
            int receiverEnd = file.end(((MemberSelectTree) at.call.getMethodSelect()).getExpression());
            int lineStart = file.content.lastIndexOf('\n', receiverEnd - 1) + 1;
            var indent = file.content.substring(lineStart, receiverEnd).isBlank() ? file.indentAt(receiverEnd) : file.indentAt(receiverEnd) + "        ";
            edit.insert(file, receiverEnd, "\n" + indent + call);
            return null;
        }
        if (!file.decorateCalls.isEmpty()) {
            var last = file.decorateCalls.get(file.decorateCalls.size() - 1);
            var argument = last.getArguments().get(0);
            if (argument instanceof LambdaExpressionTree lambda && lambda.getBody() instanceof ExpressionTree body) {
                int lineStart = file.content.lastIndexOf('\n', file.start(body) - 1) + 1;
                var indent = file.content.substring(lineStart, file.start(body)).isBlank() ? file.indentAt(file.start(body)) : file.indentAt(file.start(body)) + "        ";
                edit.insert(file, file.end(body), "\n" + indent + call);
                return null;
            }
        }
        var indent = file.chapter == null ? "    " : file.indentAt(file.start(file.chapter.var));
        edit.insert(file, file.classBodyEnd, "\n\n" + indent + "static {\n" + indent + "    " + file.chapterField + ".decorate(d -> d\n" +
                indent + "        " + call + ");\n" + indent + "}");
        return null;
    }

    @Nullable
    private static String decoLineDelete(QuestSource src, Op op, Edit edit) {
        var file = src.byChapter.get(op.chapter);
        if (file == null) return "找不到章节 " + op.chapter + " 的源码";
        if (op.arg == null || op.arg.isEmpty()) return "没有要删除的装饰线";
        var decls = new ObjectArrayList<DecoLineDecl>(4);
        for (var identity : op.arg.split(",")) {
            if (identity.isEmpty()) continue;
            if (identity.startsWith("l:" + op.chapter + ":add:")) {
                int index = addIndex(identity);
                if (index < 0 || index >= file.decoLines.size()) return "找不到装饰线源码 " + identity;
                decls.add(file.decoLines.get(index));
                continue;
            }
            var decl = src.find(identity);
            if (!(decl instanceof DecoLineDecl line) || !line.valid) return "找不到装饰线源码 " + identity;
            decls.add(line);
        }
        decls.sort((a, b) -> Integer.compare(b.charStart, a.charStart));
        var replacedBodies = new ObjectArrayList<MethodInvocationTree>(2);
        for (var line : decls) {
            if (replacedBodies.contains(line.decorate)) continue;
            int total = 0, deleted = 0;
            for (var other : file.decoLines) {
                if (other.decorate != line.decorate) continue;
                total++;
                if (decls.contains(other)) deleted++;
            }
            if (total == 0 || total != deleted) continue;
            var argument = line.decorate.getArguments().get(0);
            if (decorationHasOtherCalls(argument, "line")) continue;
            if (!(argument instanceof LambdaExpressionTree lambda) || !(lambda.getBody() instanceof ExpressionTree body)) continue;
            edit.replaceRange(file, file.start(body), file.end(body), "{}");
            replacedBodies.add(line.decorate);
        }
        for (var line : decls) {
            if (replacedBodies.contains(line.decorate)) continue;
            var content = line.file.content;
            int start = line.file.end(((MemberSelectTree) line.call.getMethodSelect()).getExpression());
            int end = line.chainEnd;
            int lineStart = content.lastIndexOf('\n', start - 1) + 1;
            int lineEnd = content.indexOf('\n', end);
            if (lineEnd < 0) lineEnd = content.length();
            if (content.substring(lineStart, start).isBlank() && content.substring(end, lineEnd).isBlank()) {
                start = lineStart;
                end = lineEnd < content.length() ? lineEnd + 1 : lineEnd;
            }
            edit.replaceRange(line.file, start, end, "");
        }
        return null;
    }

    private static boolean decorationHasOtherCalls(ExpressionTree argument, String skip) {
        if (!(argument instanceof LambdaExpressionTree lambda)) return true;
        Tree e = lambda.getBody();
        while (e instanceof MethodInvocationTree call && call.getMethodSelect() instanceof MemberSelectTree select) {
            if (!select.getIdentifier().contentEquals(skip)) return true;
            e = select.getExpression();
        }
        return false;
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
                number(op.x) + ", " + number(op.y) + ")" + (op.on ? ".mainNode()" : "") + ";");
        return null;
    }

    private record Rep(int start, int end, String text) {}

    private static boolean inside(List<DuplicateRemoval> removals, int offset) {
        for (var removal : removals) {
            if (offset >= removal.start() && offset < removal.end()) return true;
        }
        return false;
    }

    @Nullable
    private static String duplicate(QuestSource src, Op op, Edit edit) {
        var file = src.byChapter.get(op.chapter);
        if (file == null || file.chapterField == null || file.chapter == null) return "找不到章节 " + op.chapter + " 的源码";
        var payload = decodeDuplicate(op.text);
        if (payload == null) return "复制数据损坏";
        int slash = op.target().indexOf('/');
        String questId = slash < 0 ? op.target() : op.target().substring(slash + 1);
        if (file.fields.containsKey(payload.field()) || file.questsById.containsKey(questId)) return null;
        var reps = new ObjectArrayList<Rep>(payload.refs().size() + 8);
        reps.add(new Rep(payload.yStart(), payload.yEnd(), number(op.y())));
        reps.add(new Rep(payload.xStart(), payload.xEnd(), number(op.x())));
        reps.add(new Rep(payload.idStart(), payload.idEnd(), literal(questId)));
        reps.add(new Rep(payload.fieldOffset(), payload.fieldOffset() + payload.sourceField().length(), payload.field()));
        if (payload.removals() != null) {
            for (var removal : payload.removals()) reps.add(new Rep(removal.start(), removal.end(), ""));
        }
        for (var ref : payload.refs()) {
            if (payload.removals() != null && inside(payload.removals(), ref.offset())) continue;
            String text;
            if (ref.chapter().equals(op.chapter())) text = ref.field();
            else if (ref.className() != null && !ref.className().isEmpty()) text = ref.className() + "." + ref.field();
            else text = ref.original();
            if (text.equals(ref.original())) continue;
            reps.add(new Rep(ref.offset(), ref.offset() + ref.original().length(), text));
        }
        reps.sort((a, b) -> Integer.compare(b.start, a.start));
        var body = new StringBuilder(payload.body());
        for (var rep : reps) {
            if (rep.start < 0 || rep.end > body.length() || rep.start > rep.end) return "复制数据位置越界";
            body.replace(rep.start, rep.end, rep.text);
        }
        if (!payload.chapterField().equals(file.chapterField)) {
            int at = body.indexOf(payload.chapterField() + ".quest(");
            if (at < 0) return "复制数据找不到章节字段 " + payload.chapterField();
            body.replace(at, at + payload.chapterField().length(), file.chapterField);
        }
        var indent = file.indentAt(file.start(file.chapter.var));
        edit.imports(file, payload.imports());
        edit.insert(file, file.membersEnd, "\n\n" + indent + body);
        return null;
    }

    @Nullable
    private static String shape(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var calls = decl.calls("shape");
        var file = decl.file;
        if (op.arg().isEmpty()) {
            for (var call : calls) edit.removeCall(decl, call);
            return null;
        }
        var text = "QuestShape." + op.arg();
        if (!calls.isEmpty()) {
            var args = calls.get(0).getArguments();
            if (args.size() == 1) edit.replace(file, args.get(0), text);
            else edit.replaceRange(file, decl.nameStart(calls.get(0)), file.end(calls.get(0)), "shape(" + text + ")");
        } else {
            var at = decl.calls("at");
            if (at.size() != 1) return decl.where() + "：需要恰好一个 .at(x, y)";
            edit.insert(file, file.end(at.get(0)), "\n" + decl.callIndent() + ".shape(" + text + ")");
        }
        edit.imports(file, List.of("com.gtolib.api.quest.QuestShape"));
        return null;
    }

    @Nullable
    private static String iconScale(QuestSource src, Op op, Edit edit) {
        if (!(src.find(op.target) instanceof QuestDecl decl)) return missing(op);
        var file = decl.file;
        var calls = decl.calls("iconScale");
        if (!calls.isEmpty() && !calls.get(0).getArguments().isEmpty()) {
            edit.replace(file, calls.get(0).getArguments().get(0), number(op.x()));
            return null;
        }
        var at = decl.calls("at");
        if (at.size() != 1) return decl.where() + "：需要恰好一个 .at(x, y)";
        edit.insert(file, file.end(at.get(0)), "\n" + decl.callIndent() + ".iconScale(" + number(op.x()) + ")");
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

    static String literal(String text) {
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

        void removeCall(ChainDecl decl, MethodInvocationTree call) {
            of(decl.file).add(new Splice(decl.receiverEnd(call), decl.file.end(call), ""));
        }

        void removeCall(FileModel file, MethodInvocationTree call) {
            of(file).add(new Splice(file.end(((MemberSelectTree) call.getMethodSelect()).getExpression()), file.end(call), ""));
        }

        void removeArgument(ChainDecl decl, MethodInvocationTree call, int index) {
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

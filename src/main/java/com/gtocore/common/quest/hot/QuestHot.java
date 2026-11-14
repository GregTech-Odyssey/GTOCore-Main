package com.gtocore.common.quest.hot;

import com.gtocore.common.quest.hot.QuestEdits.Op;
import com.gtocore.common.quest.hot.QuestSource.LinkDecl;

import com.gtolib.GTOCore;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestBook;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestDependency;
import com.gtolib.api.quest.QuestLang;
import com.gtolib.api.quest.QuestLine;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestLive;
import com.gtolib.api.quest.QuestScreenLang;
import com.gtolib.api.quest.reward.CommandReward;
import com.gtolib.api.quest.reward.ItemReward;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.reward.RewardTable;
import com.gtolib.api.quest.reward.TableReward;
import com.gtolib.api.quest.task.ItemTask;
import com.gtolib.api.quest.task.Task;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 任务书热加载/热修改的统一入口：界面上的每个修改都变成幂等操作交给 {@link QuestEditQueue}，画面按操作表即时显示，后台合并写回源码再热加载。
 * 开发环境每次启动都在这里校验章节源码能否被热修改系统完整解析，不能则直接报错。
 */
public final class QuestHot {

    public static final List<Flag> FLAGS = List.of(
            new Flag("optional", QuestScreenLang.EDIT_FLAG_OPTIONAL),
            new Flag("repeatable", QuestScreenLang.EDIT_FLAG_REPEATABLE),
            new Flag("milestone", QuestScreenLang.EDIT_FLAG_MILESTONE),
            new Flag("invisible", QuestScreenLang.EDIT_FLAG_INVISIBLE),
            new Flag("hideDependencyLines", QuestScreenLang.EDIT_FLAG_HIDE_DEPENDENCY_LINES),
            new Flag("hideDependentLines", QuestScreenLang.EDIT_FLAG_HIDE_DEPENDENT_LINES),
            new Flag("hideUntilDepsVisible", QuestScreenLang.EDIT_FLAG_HIDE_UNTIL_DEPS_VISIBLE),
            new Flag("hideUntilDepsComplete", QuestScreenLang.EDIT_FLAG_HIDE_UNTIL_DEPS_COMPLETE),
            new Flag("disableToast", QuestScreenLang.EDIT_FLAG_DISABLE_TOAST));
    private static final int MAX_REPORTED = 12;
    private static final int MAX_LABEL = 36;

    public record Result(boolean ok, String message) {

        public static Result ok(String message) {
            return new Result(true, message);
        }

        public static Result fail(String message) {
            return new Result(false, message);
        }
    }

    public record Flag(String method, QuestLang label) {}

    public record Placement(QuestDependency target, double x, double y) {}

    public record Dependency(String label, @Nullable QuestDependency target, @Nullable String identity, boolean pending) {}

    public record Amount(String label, long value, boolean task, int liveIndex, boolean scaled) {}

    private static boolean editable;
    private static String problem = "";

    @FunctionalInterface
    public interface Reloader {

        void reload(List<Path> paths, boolean blocking, boolean rebuild, Consumer<String> done);
    }

    @FunctionalInterface
    public interface LiveListener {

        void edited(boolean logic);
    }

    @Nullable
    private static LiveListener liveListener;

    @Nullable
    private static Runnable warmer;

    @Nullable
    private static Reloader reloader;
    @Nullable
    private static BiConsumer<Path, String> writeListener;
    @Nullable
    private static Consumer<RuntimeException> startupFailure;

    private QuestHot() {}

    public static void setReloader(Reloader reloader) {
        QuestHot.reloader = reloader;
    }

    static void reload(List<Path> paths, boolean blocking, boolean rebuild, Consumer<String> done) {
        var target = reloader;
        if (target == null) done.accept(null);
        else target.reload(paths, blocking, rebuild, done);
    }

    public static void setWriteListener(BiConsumer<Path, String> listener) {
        writeListener = listener;
    }

    static void writing(Path path, String content) {
        var target = writeListener;
        if (target != null) target.accept(path, content);
    }

    public static void setLiveListener(LiveListener listener) {
        liveListener = listener;
    }

    public static void setWarmer(Runnable warmer) {
        QuestHot.warmer = warmer;
    }

    public static void warm() {
        var target = warmer;
        if (target != null) target.run();
    }

    private static boolean edited(boolean applied, boolean logic) {
        var listener = liveListener;
        if (applied && listener != null) listener.edited(logic);
        return applied;
    }

    public static void setStartupFailureHandler(Consumer<RuntimeException> handler) {
        startupFailure = handler;
    }

    public static void validateAtStartup() {
        if (!QuestHotConfig.enabled()) return;
        long start = System.nanoTime();
        List<String> errors;
        try {
            errors = validateNow();
        } catch (RuntimeException | LinkageError e) {
            throw startupFailure(new IllegalStateException("任务章节源码校验本身出错，热加载/热修改系统不可用", e));
        }
        if (!errors.isEmpty()) {
            throw startupFailure(new IllegalStateException("任务章节源码不能被热加载/热修改系统解析（" + errors.size() + " 处），开发环境不允许这样写：\n" + String.join("\n", errors)));
        }
        GTOCore.LOGGER.info("[任务热修改] 章节源码校验通过：{} 个文件，{}ms", source().files.size(), (System.nanoTime() - start) / 1_000_000);
    }

    private static RuntimeException startupFailure(RuntimeException error) {
        GTOCore.LOGGER.error("[任务热修改] {}", error.getMessage(), error);
        var handler = startupFailure;
        if (handler != null) handler.accept(error);
        return error;
    }

    public static List<String> validateAfterReload() {
        return QuestHotConfig.enabled() ? validateNow() : List.of();
    }

    private static List<String> validateNow() {
        var errors = source().validate();
        editable = errors.isEmpty();
        problem = errors.isEmpty() ? "" : summarize(errors);
        return errors;
    }

    public static String summarize(List<String> errors) {
        var sb = new StringBuilder();
        for (int i = 0; i < Math.min(errors.size(), MAX_REPORTED); i++) sb.append(i == 0 ? "" : "\n").append(errors.get(i));
        if (errors.size() > MAX_REPORTED) sb.append("\n…还有 ").append(errors.size() - MAX_REPORTED).append(" 处");
        return sb.toString();
    }

    public static boolean editable() {
        return QuestHotConfig.enabled() && editable;
    }

    public static String problem() {
        return problem;
    }

    static QuestSource source() {
        return QuestSource.load(QuestHotConfig.sourceRoot());
    }

    @Nullable
    public static String identity(QuestDependency object) {
        if (object instanceof Quest quest) return "q:" + quest.key();
        if (!(object instanceof QuestLink link)) return null;
        var src = source();
        var decl = src.declOf(link);
        return decl == null ? movedLinkIdentity(src, link) : src.identityOf(decl);
    }

    @Nullable
    private static String movedLinkIdentity(QuestSource src, QuestLink link) {
        var ops = QuestEditQueue.pending();
        for (int i = ops.size() - 1; i >= 0; i--) {
            var op = ops.get(i);
            if (!op.type().equals(QuestEdits.MOVE) || op.x() != link.x() || op.y() != link.y()) continue;
            if (src.find(op.target()) instanceof LinkDecl decl && link.chapter().id().equals(decl.file.chapterId) &&
                    src.resolve(decl.file, decl.targetRef) instanceof QuestSource.QuestDecl target && target.key().equals(link.target().key())) {
                return op.target();
            }
        }
        return null;
    }

    private static String name(Quest quest) {
        var title = quest.title().getString();
        return title.isEmpty() ? quest.id() : title;
    }

    @Nullable
    private static Op pending(String type, String target, @Nullable String arg) {
        var ops = QuestEditQueue.pending();
        for (int i = ops.size() - 1; i >= 0; i--) {
            var op = ops.get(i);
            if (op.type().equals(type) && op.target().equals(target) && (arg == null || arg.equals(op.arg()))) return op;
        }
        return null;
    }

    public static double[] position(QuestDependency object) {
        var id = identity(object);
        var op = id == null ? null : pending(QuestEdits.MOVE, id, null);
        if (op != null) return new double[] { op.x(), op.y() };
        return object instanceof QuestLink link ? new double[] { link.x(), link.y() } : new double[] { ((Quest) object).x(), ((Quest) object).y() };
    }

    @Nullable
    private static Result guard() {
        if (!QuestHotConfig.enabled()) return Result.fail("热修改未启用");
        if (!editable) return Result.fail("章节源码无法解析，请先修复：\n" + problem);
        return null;
    }

    public static Result move(List<Placement> placements) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var forward = new ObjectArrayList<Op>(placements.size());
        var inverse = new ObjectArrayList<Op>(placements.size());
        var targets = new ObjectArrayList<QuestDependency>(placements.size());
        var after = new ObjectArrayList<double[]>(placements.size());
        var before = new ObjectArrayList<double[]>(placements.size());
        for (var placement : placements) {
            var id = identity(placement.target);
            if (id == null) return Result.fail("找不到节点的源码声明");
            var old = position(placement.target);
            double x = QuestEdits.round(placement.x), y = QuestEdits.round(placement.y);
            forward.add(Op.move(id, x, y));
            inverse.add(Op.move(id, old[0], old[1]));
            targets.add(placement.target);
            after.add(new double[] { x, y });
            before.add(old);
        }
        return QuestEditQueue.submit("移动 " + placements.size() + " 个任务", forward, inverse, () -> relocate(targets, after), () -> relocate(targets, before));
    }

    private static boolean relocate(List<QuestDependency> targets, List<double[]> positions) {
        for (int i = 0; i < targets.size(); i++) QuestLive.relocate(targets.get(i), positions.get(i)[0], positions.get(i)[1]);
        return edited(true, false);
    }

    public static boolean hasFlag(Quest quest, Flag flag) {
        var op = pending(QuestEdits.FLAG, "q:" + quest.key(), flag.method());
        if (op != null) return op.on();
        var decl = source().declOf(quest);
        return decl != null && !QuestEdits.flagCalls(decl, flag.method()).isEmpty();
    }

    public static Result setFlag(Quest quest, Flag flag, boolean on) {
        var blocked = guard();
        if (blocked != null) return blocked;
        if (hasFlag(quest, flag) == on) return Result.ok("");
        var id = "q:" + quest.key();
        return QuestEditQueue.submit((on ? "开启：" : "关闭：") + flag.label().cn() + " · " + name(quest), List.of(Op.flag(id, flag.method(), on)), List.of(Op.flag(id, flag.method(), !on)),
                () -> edited(QuestLive.setFlag(quest, flag.method(), on), true), () -> edited(QuestLive.setFlag(quest, flag.method(), !on), true));
    }

    public static List<Amount> amounts(Quest quest) {
        var out = new ObjectArrayList<Amount>();
        var decl = source().declOf(quest);
        if (decl == null) return out;
        var sites = QuestEdits.amountSites(decl);
        var tasks = quest.tasks();
        var id = "q:" + quest.key();
        var ops = QuestEditQueue.pending();
        for (int i = 0; i < sites.size(); i++) {
            var site = sites.get(i);
            String label;
            if (site.task()) {
                var task = site.liveIndex() < tasks.size() ? tasks.get(site.liveIndex()) : null;
                label = task instanceof ItemTask it && it.item() != null ? "目标：" + it.item().getDescription().getString() : "目标：" + clip(decl.file.text(site.subject()));
            } else {
                label = "奖励：" + clip(decl.file.text(site.subject()));
            }
            long value = site.value();
            for (int k = ops.size() - 1; k >= 0; k--) {
                var op = ops.get(k);
                if (op.type().equals(QuestEdits.AMOUNT) && op.target().equals(id) && op.index() == i) {
                    value = op.value();
                    break;
                }
            }
            out.add(new Amount(label, value, site.task(), site.liveIndex(), site.scaled()));
        }
        return out;
    }

    public static Result setAmount(Quest quest, int index, long value) {
        var blocked = guard();
        if (blocked != null) return blocked;
        if (value < 1) return Result.fail("数量至少为 1");
        var amounts = amounts(quest);
        if (index < 0 || index >= amounts.size()) return Result.fail("数量位置已变化，请重新打开");
        var amount = amounts.get(index);
        long old = amount.value();
        if (old == value) return Result.ok("");
        var id = "q:" + quest.key();
        return QuestEditQueue.submit("修改数量：" + name(quest), List.of(Op.amount(id, index, value)), List.of(Op.amount(id, index, old)),
                () -> edited(liveAmount(quest, amount, old, value), true), () -> edited(liveAmount(quest, amount, value, old), true));
    }

    private static boolean liveAmount(Quest quest, Amount amount, long from, long to) {
        return amount.task() ? QuestLive.setTaskCount(quest, amount.liveIndex(), from, to) : QuestLive.setRewardCount(quest, amount.liveIndex(), amount.scaled(), from, to);
    }

    private static String clip(String text) {
        var flat = text.replaceAll("\\s+", " ");
        return flat.length() <= MAX_LABEL ? flat : flat.substring(0, MAX_LABEL - 1) + "…";
    }

    public static List<Dependency> dependencies(Quest quest) {
        var src = source();
        var decl = src.declOf(quest);
        var out = new ObjectArrayList<Dependency>();
        if (decl == null) return out;
        var id = "q:" + quest.key();
        for (var call : decl.calls("dependsOn")) {
            for (var arg : call.getArguments()) {
                var target = src.resolve(decl.file, arg);
                var targetId = target == null ? null : src.identityOf(target);
                if (targetId != null) {
                    var op = pending(QuestEdits.DEP, id, targetId);
                    if (op != null && !op.on()) continue;
                }
                out.add(new Dependency(decl.file.text(arg), target == null ? null : objectOf(src, target), targetId, false));
            }
        }
        for (var op : QuestEditQueue.pending()) {
            if (!op.type().equals(QuestEdits.DEP) || !op.target().equals(id) || !op.on()) continue;
            boolean present = false;
            for (var dep : out) present |= op.arg().equals(dep.identity());
            if (present) continue;
            var target = src.find(op.arg());
            out.add(new Dependency(op.arg(), target == null ? null : objectOf(src, target), op.arg(), true));
        }
        return out;
    }

    @Nullable
    public static QuestDependency objectOf(String identity) {
        var src = source();
        var decl = src.find(identity);
        return decl == null ? null : objectOf(src, decl);
    }

    @Nullable
    private static QuestDependency objectOf(QuestSource src, QuestSource.Decl decl) {
        if (!(decl instanceof LinkDecl link)) return QuestSource.objectOf(decl);
        if (!(QuestBook.byKey(link.file.chapterId) instanceof QuestChapter chapter)) return null;
        var links = chapter.links();
        for (int i = 0; i < links.size(); i++) {
            if (src.declOf(links.get(i)) == link) return links.get(i);
        }
        return null;
    }

    public static boolean dependsOnDisplayed(Quest dependent, QuestDependency prerequisite) {
        var prereqId = identity(prerequisite);
        if (prereqId == null) return false;
        for (var dep : dependencies(dependent)) {
            if (prereqId.equals(dep.identity())) return true;
        }
        return false;
    }

    public record DependencyCheck(boolean present, String error) {

        public boolean allowed() {
            return error.isEmpty();
        }
    }

    public static DependencyCheck checkDependency(Quest dependent, QuestDependency prerequisite) {
        var blocked = guard();
        if (blocked != null) return new DependencyCheck(false, blocked.message());
        var prereqId = identity(prerequisite);
        if (prereqId == null) return new DependencyCheck(false, "找不到依赖目标的源码");
        if (prereqId.startsWith("d:")) return new DependencyCheck(false, "装饰中的复制节点不可作依赖，请改用原任务");
        var prerequisiteQuest = prerequisite instanceof QuestLink link ? link.target() : (Quest) prerequisite;
        boolean present = dependsOnDisplayed(dependent, prerequisite);
        if (!present) {
            if (prerequisiteQuest == dependent) return new DependencyCheck(false, "任务不能依赖自己");
            if (reaches(prerequisiteQuest, dependent, new ReferenceOpenHashSet<>())) {
                return new DependencyCheck(false, "会形成依赖环：" + name(prerequisiteQuest) + " 已依赖 " + name(dependent));
            }
        }
        var line = dependent.chapter().mainLine();
        int di = line.indexOf(dependent), pi = line.indexOf(prerequisiteQuest);
        if (di >= 0 && pi >= 0) {
            if (!present && pi != di - 1) return new DependencyCheck(false, "主线任务只能依赖上一个主线任务");
            if (present && pi == di - 1) return new DependencyCheck(true, "主线依赖不可单独移除，请用“移出主线”");
        }
        return new DependencyCheck(present, "");
    }

    public static Result toggleDependency(Quest dependent, QuestDependency prerequisite) {
        var check = checkDependency(dependent, prerequisite);
        if (!check.allowed()) return Result.fail(check.error());
        var prereqId = identity(prerequisite);
        var prerequisiteQuest = prerequisite instanceof QuestLink link ? link.target() : (Quest) prerequisite;
        var id = "q:" + dependent.key();
        boolean present = check.present();
        var label = (present ? "移除依赖：" : "添加依赖：") + name(prerequisiteQuest) + " → " + name(dependent);
        var before = QuestLive.dependencies(dependent);
        var after = new ObjectArrayList<QuestDependency>(before.size() + 1);
        for (var dep : before) {
            if (!present || !prereqId.equals(identity(dep))) after.add(dep);
        }
        if (!present) after.add(prerequisite);
        return QuestEditQueue.submit(label, List.of(Op.dependency(id, prereqId, !present)), List.of(Op.dependency(id, prereqId, present)),
                () -> setLiveDependencies(dependent, after), () -> setLiveDependencies(dependent, before));
    }

    private static boolean setLiveDependencies(Quest quest, List<QuestDependency> deps) {
        QuestLive.setDependencies(quest, deps);
        return edited(true, true);
    }

    private static boolean reaches(Quest from, Quest target, ReferenceOpenHashSet<Quest> seen) {
        if (!seen.add(from)) return false;
        for (var dep : dependencies(from)) {
            var object = dep.target();
            Quest quest = object instanceof Quest q ? q : object instanceof QuestLink l ? l.target() : null;
            if (quest == null) continue;
            if (quest == target || reaches(quest, target, seen)) return true;
        }
        var deps = from.dependencies();
        for (int i = 0; i < deps.size(); i++) {
            if (deps.get(i) instanceof Task task && (task.quest() == target || reaches(task.quest(), target, seen))) return true;
        }
        return false;
    }

    public static Result createQuest(QuestChapter chapter, double x, double y, Item item) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var src = source();
        var file = src.fileOf(chapter);
        if (file == null || file.chapterField == null) return Result.fail("找不到章节 " + chapter.id() + " 的源码");
        var syntax = QuestItemSyntax.of(src, item);
        if (syntax == null) return Result.fail(BuiltInRegistries.ITEM.getKey(item) + " 无可用 Java 写法，请在源码中手写");
        var base = BuiltInRegistries.ITEM.getKey(item).getPath().replaceAll("[^a-z0-9_]", "_");
        var id = base;
        for (int n = 2; taken(file, chapter.id(), id); n++) id = base + "_" + n;
        var op = Op.create(chapter.id(), id, QuestEdits.round(x), QuestEdits.round(y), syntax.args(), syntax.imports());
        return QuestEditQueue.submit("新建任务：" + chapter.id() + "/" + id, List.of(op), List.of(Op.delete(op.target())));
    }

    private static boolean taken(QuestSource.FileModel file, String chapter, String id) {
        if (file.questsById.containsKey(id) || file.fields.containsKey(id.toUpperCase(Locale.ROOT))) return true;
        var target = "q:" + chapter + "/" + id;
        for (var op : QuestEditQueue.pending()) {
            if (op.target().equals(target)) return true;
        }
        return false;
    }

    public static Result createCopy(QuestChapter chapter, Quest target, double x, double y) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var src = source();
        var file = src.fileOf(chapter);
        if (file == null || file.chapterField == null) return Result.fail("找不到章节 " + chapter.id() + " 的源码");
        var targetDecl = src.declOf(target);
        if (targetDecl == null) return Result.fail("找不到任务 " + target.key() + " 的源码声明");
        String field = null;
        for (int n = 1; field == null || file.fields.containsKey(field) || pending(QuestEdits.COPY, "c:" + chapter.id() + ":" + field, null) != null; n++) {
            field = targetDecl.field + "_COPY_" + n;
        }
        var op = Op.copy(chapter.id(), field, "q:" + target.key(), QuestEdits.round(x), QuestEdits.round(y));
        return QuestEditQueue.submit("放置复制节点：" + name(target), List.of(op), List.of(Op.delete(op.target())));
    }

    public static Result delete(List<? extends QuestDependency> objects) {
        var blocked = guard();
        if (blocked != null) return blocked;
        if (objects.isEmpty()) return Result.ok("");
        var src = source();
        var forward = new ObjectArrayList<Op>();
        var inverse = new ObjectArrayList<Op>();
        var deleting = new ObjectArrayList<QuestSource.Decl>();
        for (var object : objects) {
            var id = identity(object);
            var decl = id == null ? null : src.find(id);
            if (decl == null) return Result.fail("找不到要删除对象的源码");
            deleting.add(decl);
            forward.add(Op.delete(id));
            inverse.add(QuestEditQueue.undelete(id));
        }
        for (var decl : deleting) {
            if (decl.field == null) continue;
            for (var ref : src.referencesTo(decl)) {
                if (!inside(deleting, ref.file(), ref.pos()) && !removedByPending(src, ref)) {
                    return Result.fail(decl.field + " 被 " + ref.file().name() + ":" + ref.file().line(ref.pos()) + " 引用，请先移除该依赖或复制节点");
                }
            }
        }
        return QuestEditQueue.submit("删除 " + objects.size() + " 个节点", forward, inverse);
    }

    private static boolean inside(List<QuestSource.Decl> decls, QuestSource.FileModel file, int pos) {
        for (var decl : decls) {
            if (decl.file != file) continue;
            var tree = decl.var != null ? decl.var : decl instanceof LinkDecl link ? link.call : null;
            if (tree != null && pos >= file.start(tree) && pos < file.end(tree)) return true;
        }
        return false;
    }

    private static boolean removedByPending(QuestSource src, QuestSource.Ref ref) {
        var file = ref.file();
        for (var quest : file.quests) {
            if (ref.pos() < file.start(quest.var) || ref.pos() >= file.end(quest.var)) continue;
            var id = "q:" + quest.key();
            if (pending(QuestEdits.DELETE, id, null) != null) return true;
            for (var call : quest.calls("dependsOn")) {
                for (var arg : call.getArguments()) {
                    if (ref.pos() < file.start(arg) || ref.pos() >= file.end(arg)) continue;
                    var target = src.resolve(file, arg);
                    var targetId = target == null ? null : src.identityOf(target);
                    var op = targetId == null ? null : pending(QuestEdits.DEP, id, targetId);
                    return op != null && !op.on();
                }
            }
        }
        for (var link : file.links) {
            if (link.var == null || ref.pos() < file.start(link.var) || ref.pos() >= file.end(link.var)) continue;
            var id = src.identityOf(link);
            return id != null && pending(QuestEdits.DELETE, id, null) != null;
        }
        return false;
    }

    public static final List<String> MARKERS = List.of("item", "itemTag", "fluid", "fluidTag");

    public static List<QuestEdits.TextEntry> text(Quest quest, String part) {
        var op = pending(QuestEdits.TEXT, "q:" + quest.key(), part);
        if (op != null) return QuestEdits.decode(op.text());
        var decl = source().declOf(quest);
        return decl == null ? List.of() : QuestEdits.readText(decl, part);
    }

    public static Result setText(Quest quest, String part, List<QuestEdits.TextEntry> entries, List<String> imports) {
        return setText(quest, part, entries, imports, null);
    }

    public static List<Object> liveText(Quest quest, String part) {
        return switch (part) {
            case "title" -> QuestLive.title(quest) == null ? List.of() : List.of(QuestLive.title(quest));
            case "subtitle" -> QuestLive.subtitle(quest) == null ? List.of() : List.of(QuestLive.subtitle(quest));
            default -> QuestLive.description(quest);
        };
    }

    private static boolean setLiveText(Quest quest, String part, List<Object> parts) {
        var errors = switch (part) {
            case "title" -> QuestLive.setTitle(quest, parts.isEmpty() ? null : (QuestLine) parts.get(0));
            case "subtitle" -> QuestLive.setSubtitle(quest, parts.isEmpty() ? null : (QuestLine) parts.get(0));
            default -> QuestLive.setDescription(quest, parts);
        };
        if (!errors.isEmpty()) GTOCore.LOGGER.warn("[任务热修改] 无法直接应用文字修改，改走重载：{}", errors);
        return edited(errors.isEmpty(), false);
    }

    public static Result setText(Quest quest, String part, List<QuestEdits.TextEntry> entries, List<String> imports, @Nullable List<Object> liveParts) {
        var blocked = guard();
        if (blocked != null) return blocked;
        if (part.equals("title")) {
            int lines = 0;
            for (var entry : entries) if (entry.isLine()) lines++;
            if (lines > 1) return Result.fail("标题只能有一行");
        }
        for (var entry : entries) {
            if (!entry.isLine()) continue;
            var text = entry.text() == null ? "" : entry.text();
            if (text.indexOf('&') >= 0 || text.indexOf('§') >= 0 || text.indexOf('\n') >= 0) return Result.fail("文字中不可含 &、§ 或换行");
            if (!part.equals("desc")) {
                for (var marker : MARKERS) {
                    if (text.contains("[" + marker + "]")) return Result.fail("标题与副标题不可插入物品或流体");
                }
            }
        }
        var old = text(quest, part);
        if (old.equals(entries)) return Result.ok("");
        for (var entry : entries) {
            if (entry.isLine() && entry.args() != null && !entry.args().isEmpty() && !imports.contains(QuestEdits.STYLE_IMPORT)) {
                imports = new ObjectArrayList<>(imports);
                imports.add(QuestEdits.STYLE_IMPORT);
            }
        }
        var id = "q:" + quest.key();
        var label = (part.equals("title") ? "修改标题：" : part.equals("subtitle") ? "修改副标题：" : "修改描述：") + name(quest);
        if (liveParts == null) return QuestEditQueue.submit(label, List.of(Op.text(id, part, entries, imports)), List.of(Op.text(id, part, old, List.of())));
        var oldParts = liveText(quest, part);
        return QuestEditQueue.submit(label, List.of(Op.text(id, part, entries, imports)), List.of(Op.text(id, part, old, List.of())),
                () -> setLiveText(quest, part, liveParts), () -> setLiveText(quest, part, oldParts));
    }

    @Nullable
    public static String reference(Quest from, Quest target) {
        var src = source();
        var fromDecl = src.declOf(from);
        var targetDecl = src.declOf(target);
        return fromDecl == null || targetDecl == null ? null : src.refText(fromDecl.file, targetDecl, fromDecl.file.start(fromDecl.var));
    }

    public record ItemRef(String expression, List<String> imports) {}

    public static ItemRef styleRef(Quest quest, String constant) {
        var decl = source().declOf(quest);
        if (decl != null && decl.file.fields.containsKey(constant)) return new ItemRef("QuestStyle." + constant, List.of("com.gtolib.api.quest.QuestStyle"));
        return new ItemRef(constant, List.of());
    }

    @Nullable
    public static ItemRef itemRef(Item item) {
        var syntax = QuestItemSyntax.of(source(), item);
        return syntax == null ? null : new ItemRef("item(" + syntax.args() + ")", syntax.imports());
    }

    public record MainLineInfo(boolean available, int position, int size, @Nullable Quest previous, @Nullable Quest next) {

        public boolean member() {
            return position >= 0;
        }
    }

    public static MainLineInfo mainLine(Quest quest) {
        var line = quest.chapter().mainLine();
        int at = line.indexOf(quest);
        var file = source().fileOf(quest.chapter());
        return new MainLineInfo(file != null && file.mainLine != null, at, line.size(), at > 0 ? line.get(at - 1) : null, at >= 0 && at + 1 < line.size() ? line.get(at + 1) : null);
    }

    @Nullable
    private static String mainLineText(QuestSource src, QuestSource.FileModel file, List<Quest> line) {
        var sb = new StringBuilder();
        for (var quest : line) {
            var decl = src.declOf(quest);
            if (decl == null) return null;
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(file.mainLine == null ? src.refText(file, decl) : src.refText(file, decl, file.start(file.mainLine)));
        }
        return sb.toString();
    }

    @Nullable
    private static String currentMainLineText(QuestChapter chapter, QuestSource.FileModel file) {
        var op = pending(QuestEdits.MAIN_LINE, "chapter:" + chapter.id(), null);
        if (op != null) return op.text();
        if (file.mainLine == null) return null;
        var args = file.mainLine.getArguments();
        return file.content.substring(file.start(args.get(0)), file.end(args.get(args.size() - 1)));
    }

    private record DepChange(Quest quest, List<QuestDependency> before, List<QuestDependency> after) {}

    private static void depChange(ObjectArrayList<Op> forward, ObjectArrayList<Op> inverse, ObjectArrayList<DepChange> live, Quest dependent, Quest prerequisite, boolean present) {
        var id = "q:" + dependent.key();
        var prereqId = "q:" + prerequisite.key();
        if (dependsOnDisplayed(dependent, prerequisite) == present) return;
        forward.add(Op.dependency(id, prereqId, present));
        inverse.add(0, Op.dependency(id, prereqId, !present));
        DepChange change = null;
        for (var c : live) if (c.quest == dependent) change = c;
        if (change == null) {
            var before = QuestLive.dependencies(dependent);
            change = new DepChange(dependent, before, new ObjectArrayList<>(before));
            live.add(change);
        }
        if (present) change.after.add(prerequisite);
        else change.after.removeIf(dep -> dep == prerequisite || dep instanceof QuestLink link && link.target() == prerequisite);
    }

    private static Result submitMainLine(String label, Quest quest, QuestChapter chapter, List<Quest> newLine, boolean milestone, ObjectArrayList<Op> forward, ObjectArrayList<Op> inverse,
                                         ObjectArrayList<DepChange> deps) {
        var oldLine = new ObjectArrayList<Quest>(chapter.mainLine());
        boolean wasMilestone = false;
        for (var flag : FLAGS) if (flag.method().equals("milestone")) wasMilestone = hasFlag(quest, flag);
        boolean oldMilestone = wasMilestone;
        var id = "q:" + quest.key();
        if (oldMilestone != milestone) {
            forward.add(Op.flag(id, "milestone", milestone));
            inverse.add(0, Op.flag(id, "milestone", oldMilestone));
        }
        return QuestEditQueue.submit(label, forward, inverse, () -> {
            QuestLive.setMainLine(chapter, newLine);
            QuestLive.setFlag(quest, "milestone", milestone);
            for (var change : deps) QuestLive.setDependencies(change.quest, change.after);
            return edited(true, true);
        }, () -> {
            QuestLive.setMainLine(chapter, oldLine);
            QuestLive.setFlag(quest, "milestone", oldMilestone);
            for (var change : deps) QuestLive.setDependencies(change.quest, change.before);
            return edited(true, true);
        });
    }

    public static Result joinMainLine(Quest quest, Quest after) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var chapter = quest.chapter();
        var line = chapter.mainLine();
        if (line.contains(quest)) return Result.fail("该任务已在主线上");
        int at = line.indexOf(after);
        if (at < 0) return Result.fail("须选择本章主线上的任务");
        var src = source();
        var file = src.fileOf(chapter);
        if (file == null || file.mainLine == null) return Result.fail("本章源码缺少 .mainLine(…)，请先设定主线");
        var newLine = new ObjectArrayList<Quest>(line);
        newLine.add(at + 1, quest);
        var text = mainLineText(src, file, newLine);
        var oldText = currentMainLineText(chapter, file);
        if (text == null || oldText == null) return Result.fail("主线上有任务找不到源码声明");
        var next = at + 1 < line.size() ? line.get(at + 1) : null;
        var forward = new ObjectArrayList<Op>();
        var inverse = new ObjectArrayList<Op>();
        var deps = new ObjectArrayList<DepChange>();
        forward.add(Op.mainLine(chapter.id(), text));
        inverse.add(Op.mainLine(chapter.id(), oldText));
        depChange(forward, inverse, deps, quest, after, true);
        if (next != null) {
            depChange(forward, inverse, deps, next, quest, true);
            depChange(forward, inverse, deps, next, after, false);
        }
        return submitMainLine("设为主线：" + name(quest), quest, chapter, newLine, true, forward, inverse, deps);
    }

    public static Result leaveMainLine(Quest quest) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var chapter = quest.chapter();
        var line = chapter.mainLine();
        int at = line.indexOf(quest);
        if (at < 0) return Result.fail("该任务不在主线上");
        if (line.size() <= 1) return Result.fail("主线至少保留一个任务");
        var src = source();
        var file = src.fileOf(chapter);
        if (file == null || file.mainLine == null) return Result.fail("本章源码缺少 .mainLine(…)");
        var newLine = new ObjectArrayList<Quest>(line);
        newLine.remove(at);
        var text = mainLineText(src, file, newLine);
        var oldText = currentMainLineText(chapter, file);
        if (text == null || oldText == null) return Result.fail("主线上有任务找不到源码声明");
        var previous = at > 0 ? line.get(at - 1) : null;
        var next = at + 1 < line.size() ? line.get(at + 1) : null;
        var forward = new ObjectArrayList<Op>();
        var inverse = new ObjectArrayList<Op>();
        var deps = new ObjectArrayList<DepChange>();
        forward.add(Op.mainLine(chapter.id(), text));
        inverse.add(Op.mainLine(chapter.id(), oldText));
        if (next != null) {
            depChange(forward, inverse, deps, next, quest, false);
            if (previous != null) depChange(forward, inverse, deps, next, previous, true);
        }
        return submitMainLine("移出主线：" + name(quest), quest, chapter, newLine, false, forward, inverse, deps);
    }

    public record TaskRow(int index, @Nullable Task live, Component name, long count, int amountIndex, boolean item, boolean consume, boolean optional) {}

    public record RewardRow(int index, int amountIndex, @Nullable Reward live, Component name, long count, boolean scaled, String group) {}

    public static boolean structuralPending(Quest quest) {
        var id = "q:" + quest.key();
        for (var op : QuestEditQueue.pending()) {
            if (!op.target().equals(id)) continue;
            switch (op.type()) {
                case QuestEdits.TASK_ADD, QuestEdits.TASK_REMOVE, QuestEdits.TASK_ITEM, QuestEdits.REWARD_ADD, QuestEdits.REWARD_REMOVE, QuestEdits.REWARD_ARG_ADD -> {
                    return true;
                }
                default -> {}
            }
        }
        return false;
    }

    public static List<TaskRow> tasks(Quest quest) {
        var out = new ObjectArrayList<TaskRow>();
        var decl = source().declOf(quest);
        if (decl == null) return out;
        var args = QuestEdits.taskArgs(decl);
        var amounts = amounts(quest);
        var live = quest.tasks();
        var id = "q:" + quest.key();
        for (int i = 0; i < args.size(); i++) {
            var task = i < live.size() ? live.get(i) : null;
            int amountIndex = -1;
            for (int k = 0; k < amounts.size(); k++) {
                if (amounts.get(k).task() && amounts.get(k).liveIndex() == i) amountIndex = k;
            }
            long count = amountIndex >= 0 ? amounts.get(amountIndex).value() : task instanceof ItemTask it ? it.count() : 1;
            var icon = task instanceof ItemTask it && !it.isTag() && !it.isAnyOf() && it.item() != null && it.item() != net.minecraft.world.item.Items.AIR ? new ItemStack(it.item()) :
                    ItemStack.EMPTY;
            Component name = !icon.isEmpty() ? icon.getHoverName() : task == null ? Component.literal(clip(decl.file.text(args.get(i).arg()))) : task.title();
            boolean consume = option(id, i, "consume", QuestEdits.optionCall(args.get(i).arg(), "consume") != null);
            boolean optional = option(id, i, "optional", QuestEdits.optionCall(args.get(i).arg(), "optional") != null);
            out.add(new TaskRow(i, task, name, count, amountIndex, QuestItemSyntax.taskRoot(args.get(i).arg()) != null, consume, optional));
        }
        return out;
    }

    private static boolean option(String id, int index, String method, boolean fromSource) {
        var ops = QuestEditQueue.pending();
        for (int k = ops.size() - 1; k >= 0; k--) {
            var op = ops.get(k);
            if (op.type().equals(QuestEdits.TASK_OPT) && op.target().equals(id) && op.index() == index && method.equals(op.arg())) return op.on();
        }
        return fromSource;
    }

    public static List<RewardRow> rewards(Quest quest) {
        var out = new ObjectArrayList<RewardRow>();
        var decl = source().declOf(quest);
        if (decl == null) return out;
        var entries = QuestEdits.rewardEntries(decl);
        var amounts = amounts(quest);
        var live = quest.rewards();
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            boolean scaled = entry.argIndex() < 0;
            int at = entry.liveIndex() + (scaled ? 2 : 0);
            var reward = at < live.size() ? live.get(at) : null;
            int amountIndex = -1;
            for (int k = 0; k < amounts.size(); k++) {
                if (!amounts.get(k).task() && amounts.get(k).liveIndex() == entry.liveIndex()) amountIndex = k;
            }
            Component name = reward instanceof ItemReward item && !item.template().isEmpty() ? item.template().getHoverName() :
                    reward instanceof CommandReward command ? Component.empty().append(reward.title()).append("：" + command.command()) :
                            reward instanceof TableReward table ? Component.empty().append(QuestKinds.tableKind(table).label().get()).append("：").append(reward.title()) :
                                    reward != null ? reward.title() : Component.literal(clip(decl.file.text(entry.expression())));
            out.add(new RewardRow(i, amountIndex, reward, name, amountIndex < 0 ? 0 : amounts.get(amountIndex).value(), scaled, entry.group()));
        }
        return out;
    }

    public static Result addTask(Quest quest, QuestKinds.TaskKind kind, @Nullable Object value) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var src = source();
        var decl = src.declOf(quest);
        if (decl == null) return Result.fail("找不到任务的源码声明");
        var expr = kind.build(src, value);
        if (expr.text() == null) return Result.fail(expr.error());
        var imports = new ObjectArrayList<String>(expr.imports());
        imports.add("com.gtolib.api.quest.task.Task");
        int index = QuestEdits.taskArgs(decl).size();
        var id = "q:" + quest.key();
        return QuestEditQueue.submit("添加目标：" + name(quest), List.of(Op.taskAdd(id, index, expr.text(), imports)), List.of(Op.taskRemove(id, index)));
    }

    public static Result removeTask(Quest quest, int index) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var decl = source().declOf(quest);
        if (decl == null) return Result.fail("找不到任务的源码声明");
        var args = QuestEdits.taskArgs(decl);
        if (index < 0 || index >= args.size()) return Result.fail("目标位置已变化，请重新打开");
        var id = "q:" + quest.key();
        return QuestEditQueue.submit("移除目标：" + name(quest), List.of(Op.taskRemove(id, index)), List.of(Op.taskAdd(id, index, decl.file.text(args.get(index).arg()), List.of())));
    }

    public static Result replaceTaskItem(Quest quest, int index, Item item) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var decl = source().declOf(quest);
        if (decl == null) return Result.fail("找不到任务的源码声明");
        var args = QuestEdits.taskArgs(decl);
        if (index < 0 || index >= args.size()) return Result.fail("目标位置已变化，请重新打开");
        var root = QuestItemSyntax.taskRoot(args.get(index).arg());
        var itemArgs = root == null ? null : QuestItemSyntax.itemArgs(root);
        if (itemArgs == null) return Result.fail("非物品目标，请在源码中修改");
        var syntax = QuestItemSyntax.of(source(), item);
        if (syntax == null) return Result.fail(BuiltInRegistries.ITEM.getKey(item) + " 无可用 Java 写法，请在源码中手写");
        var file = decl.file;
        var old = file.content.substring(file.start(itemArgs.get(0)), file.end(itemArgs.get(itemArgs.size() - 1)));
        var id = "q:" + quest.key();
        return QuestEditQueue.submit("更换目标物品：" + name(quest), List.of(Op.taskItem(id, index, syntax.args(), syntax.imports())), List.of(Op.taskItem(id, index, old, List.of())));
    }

    public static Result setTaskOption(Quest quest, int index, String method, boolean on) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var id = "q:" + quest.key();
        return QuestEditQueue.submit((on ? "开启：" : "关闭：") + (method.equals("consume") ? "消耗" : "可选") + " · " + name(quest), List.of(Op.taskOption(id, index, method, on)), List.of(Op.taskOption(id, index, method, !on)));
    }

    public static Result addReward(Quest quest, QuestKinds.RewardKind kind, @Nullable Object value) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var src = source();
        var decl = src.declOf(quest);
        if (decl == null) return Result.fail("找不到任务的源码声明");
        var expr = kind.build(src, value);
        if (expr.text() == null) return Result.fail(expr.error());
        var imports = new ObjectArrayList<String>(expr.imports());
        imports.add("com.gtolib.api.quest.reward.Reward");
        var id = "q:" + quest.key();
        int entry = QuestEdits.rewardEntries(decl).size();
        return QuestEditQueue.submit("添加奖励：" + name(quest), List.of(Op.rewardAdd(id, "reward(" + expr.text() + ")", imports)), List.of(Op.rewardRemove(id, entry)));
    }

    public static Result removeReward(Quest quest, int index) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var decl = source().declOf(quest);
        if (decl == null) return Result.fail("找不到任务的源码声明");
        var entries = QuestEdits.rewardEntries(decl);
        if (index < 0 || index >= entries.size()) return Result.fail("奖励位置已变化，请重新打开");
        var entry = entries.get(index);
        var call = entry.call();
        var id = "q:" + quest.key();
        var inverse = entry.argIndex() < 0 || call.getArguments().size() == 1 ? Op.rewardAdd(id, decl.file.content.substring(decl.nameStart(call), decl.file.end(call)), List.of()) :
                Op.rewardArgAdd(id, index - entry.argIndex(), entry.argIndex(), decl.file.text(entry.expression()));
        return QuestEditQueue.submit("移除奖励：" + name(quest), List.of(Op.rewardRemove(id, index)), List.of(inverse));
    }

    public record TableRow(int index, @Nullable Reward live, Component name, long count, boolean counted, long weight) {}

    public record TableView(String id, Component title, String titleText, boolean titleEditable, long lootSize, long emptyWeight, List<TableRow> rows, boolean pending) {}

    @Nullable
    public static RewardTable liveTable(String id) {
        var tables = QuestBook.rewardTables();
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).id().equals(id)) return tables.get(i);
        }
        return null;
    }

    @Nullable
    public static TableView table(String id) {
        if (!(source().find("t:" + id) instanceof QuestSource.TableDecl decl)) return null;
        var live = liveTable(id);
        var entries = QuestEdits.tableEntries(decl);
        var liveEntries = live == null ? null : live.entries();
        var target = "t:" + id;
        var ops = QuestEditQueue.pending();
        boolean pending = false;
        var titles = decl.calls("title");
        var titleArgs = titles.isEmpty() ? null : titles.get(0).getArguments();
        String titleText = titleArgs != null && !titleArgs.isEmpty() && titleArgs.get(0) instanceof com.sun.source.tree.LiteralTree literal && literal.getValue() instanceof String text ? text : "";
        long lootSize = tableNumber(decl, "lootSize", 1), emptyWeight = tableNumber(decl, "emptyWeight", 0);
        long[] counts = new long[entries.size()], weights = new long[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            counts[i] = entry.count() == null ? 0 : QuestSource.number(entry.count()).longValue();
            weights[i] = entry.weight() == null ? 1 : QuestSource.number(entry.weight()).longValue();
        }
        for (var op : ops) {
            if (!op.target().equals(target)) continue;
            switch (op.type()) {
                case QuestEdits.TABLE_TITLE -> titleText = op.text();
                case QuestEdits.TABLE_NUMBER -> {
                    if (op.arg().equals("lootSize")) lootSize = op.value();
                    else emptyWeight = op.value();
                }
                case QuestEdits.TABLE_WEIGHT -> {
                    if (op.index() < weights.length) weights[op.index()] = op.value();
                }
                case QuestEdits.TABLE_AMOUNT -> {
                    if (op.index() < counts.length) counts[op.index()] = op.value();
                }
                default -> pending = true;
            }
        }
        var rows = new ObjectArrayList<TableRow>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            var reward = liveEntries != null && liveEntries.size() == entries.size() ? liveEntries.get(i).reward() : null;
            Component name = reward instanceof ItemReward item && !item.template().isEmpty() ? item.template().getHoverName() :
                    reward instanceof CommandReward command ? Component.empty().append(reward.title()).append("：" + command.command()) :
                            reward != null ? reward.title() : Component.literal(clip(decl.file.text(entry.reward())));
            rows.add(new TableRow(i, reward, name, counts[i], entry.count() != null, weights[i]));
        }
        boolean plainTitle = titleArgs == null || titleArgs.size() <= 1;
        return new TableView(id, titleText.isEmpty() && live != null ? live.title() : Component.literal(titleText), titleText, plainTitle, lootSize, emptyWeight, rows, pending);
    }

    private static long tableNumber(QuestSource.TableDecl decl, String method, long fallback) {
        var calls = decl.calls(method);
        if (calls.isEmpty() || calls.get(0).getArguments().isEmpty()) return fallback;
        var value = QuestSource.number(calls.get(0).getArguments().get(0));
        return value == null ? fallback : value.longValue();
    }

    private static Result tableOp(String id, String label, Op forward, Op inverse) {
        var blocked = guard();
        if (blocked != null) return blocked;
        return QuestEditQueue.submit(label + "：" + id, List.of(forward), List.of(inverse));
    }

    public static Result addTableEntry(String id, QuestKinds.RewardKind kind, @Nullable Object value) {
        var src = source();
        if (!(src.find("t:" + id) instanceof QuestSource.TableDecl decl)) return Result.fail("找不到奖励表 " + id + " 的源码声明");
        var expr = kind.build(src, value);
        if (expr.text() == null) return Result.fail(expr.error());
        var imports = new ObjectArrayList<String>(expr.imports());
        imports.add("com.gtolib.api.quest.reward.Reward");
        int index = QuestEdits.tableEntries(decl).size();
        return tableOp(id, "添加奖励表内容", Op.tableEntryAdd("t:" + id, index, "entry(" + expr.text() + ")", imports), Op.tableEntryRemove("t:" + id, index));
    }

    public static Result removeTableEntry(String id, int index) {
        if (!(source().find("t:" + id) instanceof QuestSource.TableDecl decl)) return Result.fail("找不到奖励表 " + id + " 的源码声明");
        var entries = QuestEdits.tableEntries(decl);
        if (index < 0 || index >= entries.size()) return Result.fail("内容位置已变化，请重新打开");
        var call = entries.get(index).call();
        var text = decl.file.content.substring(decl.nameStart(call), decl.file.end(call));
        return tableOp(id, "移除奖励表内容", Op.tableEntryRemove("t:" + id, index), Op.tableEntryAdd("t:" + id, index, text, List.of()));
    }

    public static Result setTableWeight(String id, int index, long weight) {
        var view = table(id);
        if (view == null || index < 0 || index >= view.rows().size()) return Result.fail("内容位置已变化，请重新打开");
        if (weight < 0) return Result.fail("权重不能为负数");
        long old = view.rows().get(index).weight();
        return old == weight ? Result.ok("") : tableOp(id, "修改权重", Op.tableWeight("t:" + id, index, weight), Op.tableWeight("t:" + id, index, old));
    }

    public static Result setTableCount(String id, int index, long count) {
        var view = table(id);
        if (view == null || index < 0 || index >= view.rows().size() || !view.rows().get(index).counted()) return Result.fail("内容位置已变化，请重新打开");
        if (count < 1) return Result.fail("数量至少为 1");
        long old = view.rows().get(index).count();
        return old == count ? Result.ok("") : tableOp(id, "修改数量", Op.tableAmount("t:" + id, index, count), Op.tableAmount("t:" + id, index, old));
    }

    public static Result setTableNumber(String id, String method, long value) {
        var view = table(id);
        if (view == null) return Result.fail("找不到奖励表 " + id + " 的源码声明");
        boolean loot = method.equals("lootSize");
        if (value < (loot ? 1 : 0)) return Result.fail(loot ? "抽取次数至少为 1" : "空奖权重不能为负数");
        long old = loot ? view.lootSize() : view.emptyWeight();
        return old == value ? Result.ok("") : tableOp(id, loot ? "修改抽取次数" : "修改空奖权重", Op.tableNumber("t:" + id, method, value), Op.tableNumber("t:" + id, method, old));
    }

    public static Result setTableTitle(String id, String title) {
        var view = table(id);
        if (view == null) return Result.fail("找不到奖励表 " + id + " 的源码声明");
        var text = title.strip();
        if (!view.titleEditable()) return Result.fail("标题带样式，请在源码中修改");
        if (text.isEmpty() || text.indexOf('{') >= 0 || text.indexOf('}') >= 0) return Result.fail("标题不能为空，也不能包含花括号");
        return text.equals(view.titleText()) ? Result.ok("") : tableOp(id, "修改标题", Op.tableTitle("t:" + id, text), Op.tableTitle("t:" + id, view.titleText()));
    }

    public static Result createTable(String id) {
        var blocked = guard();
        if (blocked != null) return blocked;
        if (!id.matches("[a-z][a-z0-9_]*")) return Result.fail("奖励表 id 只能用小写字母、数字、下划线，并以字母开头");
        var src = source();
        var file = src.tableFile();
        var field = id.toUpperCase(Locale.ROOT);
        if (file == null) return Result.fail("找不到 QuestRewardTables.java");
        if (src.find("t:" + id) != null || liveTable(id) != null || file.fields.containsKey(field)) return Result.fail("奖励表 " + id + " 已存在");
        for (var op : QuestEditQueue.pending()) {
            if (op.type().equals(QuestEdits.TABLE_CREATE) && op.target().equals("t:" + id)) return Result.fail("奖励表 " + id + " 已存在");
        }
        return QuestEditQueue.submit("新建奖励表：" + id, List.of(Op.tableCreate(id, field, id)), List.of(Op.tableDelete(id)));
    }

    public static Result undo() {
        return QuestEditQueue.undo();
    }

    public static Result redo() {
        return QuestEditQueue.redo();
    }

    public static void flushNow() {
        QuestEditQueue.flushNow();
    }

    @Nullable
    public static String location(QuestDependency object) {
        var src = source();
        if (object instanceof Quest quest) {
            var decl = src.declOf(quest);
            return decl == null ? null : decl.file.path + ":" + decl.file.line(decl.file.start(decl.var));
        }
        if (object instanceof QuestLink link) {
            var decl = src.declOf(link);
            return decl == null ? null : decl.file.path + ":" + decl.file.line(decl.file.start(decl.call));
        }
        if (object instanceof QuestChapter chapter) {
            var file = src.fileOf(chapter);
            return file == null || file.chapter == null ? null : file.path + ":" + file.line(file.start(file.chapter.var));
        }
        return null;
    }

    public static Result openInEditor(QuestDependency object) {
        var location = location(object);
        if (location == null) return Result.fail("找不到源码位置");
        try {
            new ProcessBuilder("cmd", "/c", "code", "-g", location).start();
        } catch (IOException e) {
            return Result.fail("无法打开 VS Code：" + e.getMessage());
        }
        return Result.ok("已在 VS Code 打开 " + Path.of(location.substring(0, location.lastIndexOf(':'))).getFileName());
    }
}

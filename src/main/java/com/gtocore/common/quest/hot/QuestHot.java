package com.gtocore.common.quest.hot;

import com.gtocore.common.quest.hot.QuestEdits.Op;
import com.gtocore.common.quest.hot.QuestSource.DecoLineDecl;
import com.gtocore.common.quest.hot.QuestSource.ImageDecl;
import com.gtocore.common.quest.hot.QuestSource.LinkDecl;
import com.gtocore.common.quest.hot.QuestSource.QuestDecl;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.GTOCore;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestBook;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestDecoLine;
import com.gtolib.api.quest.QuestDependency;
import com.gtolib.api.quest.QuestIcon;
import com.gtolib.api.quest.QuestLang;
import com.gtolib.api.quest.QuestLine;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestLive;
import com.gtolib.api.quest.QuestPreset;
import com.gtolib.api.quest.QuestScreenLang;
import com.gtolib.api.quest.QuestShape;
import com.gtolib.api.quest.QuestTierSet;
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

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
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

    public record ImageRef(String identity, String texture, double x, double y, double width, double height) {}

    public record ImagePlacement(String identity, double x, double y, double width, double height) {}

    public static List<ImageRef> images(QuestChapter chapter) {
        if (!QuestHotConfig.enabled() || !editable) return List.of();
        var src = source();
        var file = src.fileOf(chapter);
        if (file == null || file.images.size() != chapter.declaredImages().size()) return List.of();
        var out = new ObjectArrayList<ImageRef>(file.images.size());
        for (var decl : file.images) {
            var identity = src.identityOf(decl);
            if (identity == null || !decl.valid || decl.textureField == null) return List.of();
            double x = decl.x, y = decl.y, width = decl.width, height = decl.height;
            var op = pending(QuestEdits.IMAGE_GEOM, identity, null);
            if (op != null) {
                x = op.x();
                y = op.y();
                width = op.w();
                height = op.h();
            }
            out.add(new ImageRef(identity, decl.textureField, x, y, width, height));
        }
        return out;
    }

    public static Result moveImages(List<ImagePlacement> placements) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var src = source();
        var forward = new ObjectArrayList<Op>(placements.size());
        var inverse = new ObjectArrayList<Op>(placements.size());
        for (var placement : placements) {
            var decl = src.find(placement.identity());
            if (!(decl instanceof ImageDecl image) || !image.valid) return Result.fail("找不到图片源码 " + placement.identity());
            double x = image.x, y = image.y, width = image.width, height = image.height;
            var op = pending(QuestEdits.IMAGE_GEOM, placement.identity(), null);
            if (op != null) {
                x = op.x();
                y = op.y();
                width = op.w();
                height = op.h();
            }
            forward.add(Op.imageGeom(placement.identity(), placement.x(), placement.y(), placement.width(), placement.height()));
            inverse.add(Op.imageGeom(placement.identity(), x, y, width, height));
        }
        return QuestEditQueue.submit("移动 " + placements.size() + " 张图片", forward, inverse,
                () -> applyGeometry(forward), () -> applyGeometry(inverse));
    }

    private static boolean applyGeometry(List<Op> ops) {
        var src = source();
        for (var op : ops) {
            var decl = src.find(op.target());
            if (!(decl instanceof ImageDecl image) || image.declarationIndex < 0) return false;
            if (!(QuestBook.byKey(image.file.chapterId) instanceof QuestChapter chapter)) return false;
            var declared = chapter.declaredImages();
            if (image.declarationIndex >= declared.size()) return false;
            declared.get(image.declarationIndex).setGeometry(op.x(), op.y(), op.w(), op.h());
        }
        return !ops.isEmpty();
    }

    public static Result addImage(QuestChapter chapter, String textureField, double x, double y, double width, double height) {
        var blocked = guard();
        if (blocked != null) return blocked;
        boolean known = false;
        for (var entry : QuestTextures.ALL) {
            if (entry.field().equals(textureField)) {
                known = true;
                break;
            }
        }
        if (!known) return Result.fail("未登记的纹理 " + textureField);
        var identity = "i:" + chapter.id() + ":add:" + System.nanoTime();
        var forward = List.of(Op.imageAdd(chapter.id(), identity, textureField, x, y, width, height, null, -1));
        var inverse = List.of(Op.imageDelete(chapter.id(), chapter.id(), identity));
        return QuestEditQueue.submit("插入图片", forward, inverse);
    }

    public static Result deleteImages(QuestChapter chapter, List<String> identities) {
        var blocked = guard();
        if (blocked != null) return blocked;
        if (identities.isEmpty()) return Result.fail("没有选中图片");
        var src = source();
        var merged = new StringBuilder();
        var inverse = new ObjectArrayList<Op>(identities.size());
        for (var identity : identities) {
            var decl = src.find(identity);
            if (!(decl instanceof ImageDecl image) || !image.valid || image.textureField == null) return Result.fail("找不到图片源码 " + identity);
            int start = image.file.end(((MemberSelectTree) image.call.getMethodSelect()).getExpression());
            var text = image.file.content.substring(start, image.chainEnd);
            if (!merged.isEmpty()) merged.append(',');
            merged.append(identity);
            inverse.add(Op.imageAdd(chapter.id(), identity, image.textureField, image.x, image.y, image.width, image.height, text, image.declarationIndex));
        }
        var forward = List.of(Op.imageDelete(chapter.id(), chapter.id(), merged.toString()));
        return QuestEditQueue.submit("删除 " + identities.size() + " 张图片", forward, inverse);
    }

    public record DecoLineCheck(boolean present, String error) {

        public boolean allowed() {
            return error.isEmpty();
        }
    }

    public static DecoLineCheck checkDecoLine(QuestChapter chapter, QuestDependency from, QuestDependency to) {
        var blocked = guard();
        if (blocked != null) return new DecoLineCheck(false, blocked.message());
        var fromQuest = questOf(from);
        var toQuest = questOf(to);
        if (fromQuest == null || toQuest == null) return new DecoLineCheck(false, "找不到装饰线端点的任务");
        if (!inChapter(chapter, from) || !inChapter(chapter, to)) return new DecoLineCheck(false, "装饰线端点必须在当前章节");
        if (from == to || fromQuest == toQuest) return new DecoLineCheck(false, "装饰线不能连到自己");
        for (var line : chapter.lines()) {
            var otherFrom = questOf(line.from());
            var otherTo = questOf(line.to());
            if (otherFrom == fromQuest && otherTo == toQuest || otherFrom == toQuest && otherTo == fromQuest) {
                return new DecoLineCheck(true, "这条装饰线已经存在");
            }
        }
        if (dependsOnQuest(fromQuest, toQuest)) return new DecoLineCheck(false, "装饰线与真实依赖方向相反，请改画真实依赖方向");
        return new DecoLineCheck(false, "");
    }

    public static Result addDecoLine(QuestChapter chapter, QuestDependency from, QuestDependency to) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var check = checkDecoLine(chapter, from, to);
        if (!check.allowed()) return Result.fail(check.error());
        var src = source();
        var fromDecl = from instanceof QuestLink link ? src.declOf(link) : src.declOf((Quest) from);
        var toDecl = to instanceof QuestLink link ? src.declOf(link) : src.declOf((Quest) to);
        if (fromDecl == null || toDecl == null) return Result.fail("找不到装饰线端点的源码字段");
        if (fromDecl.field == null || toDecl.field == null) return Result.fail("装饰线端点必须是任务或镜像字段，内联镜像请先改成字段");
        if (from instanceof QuestLink link && link.chapter() != chapter) return Result.fail("装饰线端点必须在当前章节");
        if (to instanceof QuestLink link && link.chapter() != chapter) return Result.fail("装饰线端点必须在当前章节");
        var identity = "l:" + chapter.id() + ":add:" + System.nanoTime();
        var fromText = src.refText(fromDecl.file, fromDecl);
        var toText = src.refText(toDecl.file, toDecl);
        var forward = List.of(Op.decoLineAdd(chapter.id(), identity, fromText, toText, null, -1));
        var inverse = List.of(Op.decoLineDelete(chapter.id(), chapter.id(), identity));
        return QuestEditQueue.submit("添加装饰线", forward, inverse);
    }

    public static Result deleteDecoLine(QuestChapter chapter, QuestDecoLine line) {
        var blocked = guard();
        if (blocked != null) return blocked;
        int index = chapter.declaredLines().indexOf(line);
        if (index < 0) return Result.fail("找不到装饰线");
        var identity = "l:" + chapter.id() + ":" + index;
        var decl = source().find(identity);
        if (!(decl instanceof DecoLineDecl deco) || !deco.valid) return Result.fail("找不到装饰线源码");
        var src = source();
        var fromText = deco.fromRef == null ? "" : deco.file.text(deco.fromRef);
        var toText = deco.toRef == null ? "" : deco.file.text(deco.toRef);
        var text = deco.file.text(deco.call);
        var forward = List.of(Op.decoLineDelete(chapter.id(), chapter.id(), identity));
        var inverse = List.of(Op.decoLineAdd(chapter.id(), identity, fromText, toText, text, deco.declarationIndex));
        return QuestEditQueue.submit("删除装饰线", forward, inverse);
    }

    private static boolean inChapter(QuestChapter chapter, QuestDependency object) {
        if (object instanceof Quest quest) return quest.chapter() == chapter;
        if (object instanceof QuestLink link) return link.chapter() == chapter;
        return false;
    }

    @Nullable
    private static Quest questOf(QuestDependency object) {
        if (object instanceof Quest quest) return quest;
        if (object instanceof QuestLink link) return link.target();
        return null;
    }

    private static boolean dependsOnQuest(Quest dependent, Quest prerequisite) {
        var dependencies = dependent.dependencies();
        for (int i = 0; i < dependencies.size(); i++) {
            Object dependency = dependencies.get(i);
            var quest = dependency instanceof QuestLink link ? link.target() : dependency instanceof Quest value ? value : null;
            if (quest == prerequisite) return true;
        }
        return false;
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
        var target = prerequisite instanceof QuestLink link ? link.target() : (Quest) prerequisite;
        for (var dep : dependencies(dependent)) {
            if (prereqId != null && prereqId.equals(dep.identity())) return true;
            var object = dep.target();
            var existing = object instanceof QuestLink link ? link.target() : object instanceof Quest quest ? quest : null;
            if (existing == target) return true;
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
        var prerequisiteQuest = prerequisite instanceof QuestLink link ? link.target() : (Quest) prerequisite;
        if (prerequisiteQuest == null || identity(prerequisiteQuest) == null) return new DependencyCheck(false, "找不到依赖目标的源码");
        boolean present = dependsOnDisplayed(dependent, prerequisiteQuest);
        if (!present) {
            if (prerequisiteQuest == dependent) return new DependencyCheck(false, "任务不能依赖自己");
            if (reaches(prerequisiteQuest, dependent, new ReferenceOpenHashSet<>())) {
                return new DependencyCheck(false, "会形成依赖环：" + name(prerequisiteQuest) + " 已依赖 " + name(dependent));
            }
        }
        return new DependencyCheck(present, "");
    }

    public static Result toggleDependency(Quest dependent, QuestDependency prerequisite) {
        var check = checkDependency(dependent, prerequisite);
        if (!check.allowed()) return Result.fail(check.error());
        var prerequisiteQuest = prerequisite instanceof QuestLink link ? link.target() : (Quest) prerequisite;
        var prereqId = identity(prerequisiteQuest);
        var id = "q:" + dependent.key();
        boolean present = check.present();
        var label = (present ? "移除依赖：" : "添加依赖：") + name(prerequisiteQuest) + " → " + name(dependent);
        var before = QuestLive.dependencies(dependent);
        var after = new ObjectArrayList<QuestDependency>(before.size() + 1);
        for (var dep : before) {
            var target = dep instanceof QuestLink link ? link.target() : (Quest) dep;
            if (!present || target != prerequisiteQuest) after.add(dep);
        }
        if (!present) after.add(prerequisiteQuest);
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
        var op = Op.copy(chapter.id(), field, "q:" + target.key(), QuestEdits.round(x), QuestEdits.round(y), false);
        return QuestEditQueue.submit("放置复制节点：" + name(target), List.of(op), List.of(Op.delete(op.target())));
    }

    private record Clip(String chapter, String className, String chapterField, String field, String questId, String body,
                        int fieldOffset, int idStart, int idEnd, int xStart, int xEnd, int yStart, int yEnd,
                        double x, double y, List<QuestEdits.DuplicateRef> refs,
                        @Nullable List<QuestEdits.DuplicateDepCall> deps, List<String> imports) {}

    private record LinkClip(String targetId, @Nullable String field, double x, double y, boolean mainNode) {}

    private static final ObjectArrayList<Clip> CLIPBOARD = new ObjectArrayList<>();
    private static final ObjectArrayList<LinkClip> LINK_CLIPBOARD = new ObjectArrayList<>();
    private static double clipAnchorX;
    private static double clipAnchorY;

    public static boolean hasClipboard() {
        return !CLIPBOARD.isEmpty() || !LINK_CLIPBOARD.isEmpty();
    }

    public static Result copyToClipboard(List<? extends QuestDependency> objects) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var result = capture(objects);
        return result.ok() ? Result.ok("已复制 " + (CLIPBOARD.size() + LINK_CLIPBOARD.size()) + " 个节点") : result;
    }

    public static Result cutToClipboard(List<? extends QuestDependency> objects) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var backup = new ObjectArrayList<Clip>(CLIPBOARD);
        var linkBackup = new ObjectArrayList<LinkClip>(LINK_CLIPBOARD);
        var result = capture(objects);
        if (!result.ok()) return result;
        var deleted = delete(objects);
        if (!deleted.ok()) {
            CLIPBOARD.clear();
            CLIPBOARD.addAll(backup);
            LINK_CLIPBOARD.clear();
            LINK_CLIPBOARD.addAll(linkBackup);
            return deleted;
        }
        return Result.ok("已剪切 " + (CLIPBOARD.size() + LINK_CLIPBOARD.size()) + " 个节点");
    }

    @Nullable
    private static Quest targetOf(QuestDependency object) {
        if (object instanceof Quest quest) return quest;
        return object instanceof QuestLink link ? link.target() : null;
    }

    private static Result capture(List<? extends QuestDependency> objects) {
        var src = source();
        var clips = new ObjectArrayList<Clip>(objects.size());
        var links = new ObjectArrayList<LinkClip>(objects.size());
        double anchorX = Double.NaN, anchorY = Double.NaN;
        for (var object : objects) {
            if (object instanceof QuestLink link) {
                var id = identity(link);
                var decl = id == null ? null : src.find(id);
                if (src.declOf(link.target()) == null || !(decl instanceof LinkDecl linkDecl)) return Result.fail("复制节点 " + name(link.target()) + " 的源码写法无法复制");
                links.add(new LinkClip("q:" + link.target().key(), linkDecl.field, link.x(), link.y(), link.isMainNode()));
                if (Double.isNaN(anchorX)) {
                    anchorX = link.x();
                    anchorY = link.y();
                }
                continue;
            }
            var target = targetOf(object);
            if (target == null) return Result.fail("只能复制任务或复制节点");
            var decl = src.declOf(target);
            if (decl == null) return Result.fail("找不到任务 " + target.key() + " 的源码声明");
            var clip = captureDecl(src, decl);
            if (clip == null) return Result.fail("任务 " + target.key() + " 的源码写法无法复制");
            clips.add(clip);
            if (Double.isNaN(anchorX)) {
                anchorX = clip.x();
                anchorY = clip.y();
            }
        }
        if (clips.isEmpty() && links.isEmpty()) return Result.fail("没有可复制的任务");
        CLIPBOARD.clear();
        CLIPBOARD.addAll(clips);
        LINK_CLIPBOARD.clear();
        LINK_CLIPBOARD.addAll(links);
        clipAnchorX = anchorX;
        clipAnchorY = anchorY;
        return Result.ok("");
    }

    @Nullable
    private static Clip captureDecl(QuestSource src, QuestDecl decl) {
        var file = decl.file;
        if (decl.field == null || decl.var == null || file.chapterId == null || file.chapterField == null || !(decl.var.getInitializer() instanceof MethodInvocationTree init)) return null;
        int bodyStart = file.start(decl.var), bodyEnd = file.end(decl.var);
        var body = file.content.substring(bodyStart, bodyEnd);
        int fieldOffset = body.indexOf(decl.field);
        if (fieldOffset < 0) return null;
        var at = decl.calls("at");
        if (at.size() != 1 || at.get(0).getArguments().size() != 2) return null;
        var atArgs = at.get(0).getArguments();
        Double atX = QuestSource.number(atArgs.get(0)), atY = QuestSource.number(atArgs.get(1));
        if (atX == null || atY == null) return null;
        var idArg = init.getArguments().get(0);
        var refs = new ObjectArrayList<QuestEdits.DuplicateRef>();
        var roots = new OpenCacheHashSet<String>();
        new TreeScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void unused) {
                var target = src.resolve(file, node);
                if (target != null) addRef(node, target);
                else roots.add(node.getName().toString());
                return null;
            }

            @Override
            public Void visitMemberSelect(MemberSelectTree node, Void unused) {
                var target = src.resolve(file, node);
                if (target != null) {
                    addRef(node, target);
                    return null;
                }
                if (node.getExpression() instanceof IdentifierTree owner) roots.add(owner.getName().toString());
                return super.visitMemberSelect(node, unused);
            }

            private void addRef(Tree tree, QuestSource.Decl target) {
                if (target.field == null || target instanceof QuestSource.ChapterDecl) return;
                int offset = file.start(tree) - bodyStart;
                if (offset < 0 || offset >= body.length()) return;
                refs.add(new QuestEdits.DuplicateRef(offset, file.text(tree), target.file.className, target.file.chapterId, target.field));
            }
        }.scan(decl.var.getInitializer(), null);
        var imports = new ObjectArrayList<String>();
        for (var imp : file.imports) {
            if (roots.contains(imp.substring(imp.lastIndexOf('.') + 1))) imports.add(imp);
        }
        for (var imp : file.staticImports) {
            int dot = imp.lastIndexOf('.');
            if (dot < 0) continue;
            var owner = imp.substring(0, dot);
            if (roots.contains(owner.substring(owner.lastIndexOf('.') + 1))) imports.add("static " + imp);
        }
        imports.add("com.gtolib.api.quest.Quest");
        var deps = new ObjectArrayList<QuestEdits.DuplicateDepCall>();
        for (var call : decl.calls("dependsOn")) {
            var args = call.getArguments();
            if (args.isEmpty()) continue;
            var depArgs = new ObjectArrayList<QuestEdits.DuplicateDepArg>(args.size());
            for (var arg : args) {
                var target = src.resolve(file, arg);
                if (target == null || target.field == null || target instanceof QuestSource.ChapterDecl) continue;
                depArgs.add(new QuestEdits.DuplicateDepArg(file.start(arg) - bodyStart, file.end(arg) - bodyStart, target.file.chapterId, target.field));
            }
            if (!depArgs.isEmpty()) {
                deps.add(new QuestEdits.DuplicateDepCall(decl.receiverEnd(call) - bodyStart, file.end(call) - bodyStart, depArgs));
            }
        }
        return new Clip(file.chapterId, file.className, file.chapterField, decl.field, decl.questId, body,
                fieldOffset, file.start(idArg) - bodyStart, file.end(idArg) - bodyStart,
                file.start(atArgs.get(0)) - bodyStart, file.end(atArgs.get(0)) - bodyStart,
                file.start(atArgs.get(1)) - bodyStart, file.end(atArgs.get(1)) - bodyStart,
                atX, atY, refs, deps, imports);
    }

    private static boolean pendingNode(String chapter, String id, @Nullable String field) {
        var ops = QuestEditQueue.pending();
        for (int i = 0; i < ops.size(); i++) {
            var op = ops.get(i);
            if (!op.type().equals(QuestEdits.DUPLICATE)) continue;
            if (field != null ? field.equals(op.arg()) : op.target().equals("q:" + chapter + "/" + id)) return true;
        }
        return false;
    }

    private static String uniqueField(QuestSource.FileModel file, String chapter, String base) {
        for (int n = 1;; n++) {
            var candidate = base + "_COPY_" + n;
            if (!file.fields.containsKey(candidate) && !pendingNode(chapter, null, candidate)) return candidate;
        }
    }

    private static String uniqueId(QuestSource.FileModel file, String chapter, String base) {
        for (int n = 1;; n++) {
            var candidate = base + "_copy_" + n;
            if (!file.questsById.containsKey(candidate) && !pendingNode(chapter, candidate, null)) return candidate;
        }
    }

    public static Result paste(QuestChapter chapter, double x, double y) {
        var blocked = guard();
        if (blocked != null) return blocked;
        if (CLIPBOARD.isEmpty() && LINK_CLIPBOARD.isEmpty()) return Result.fail("剪贴板是空的");
        var src = source();
        var file = src.fileOf(chapter);
        if (file == null || file.chapterField == null) return Result.fail("找不到章节 " + chapter.id() + " 的源码");
        var ordered = orderedClips();
        if (ordered == null) return Result.fail("选中任务之间存在循环依赖，无法一次性粘贴");
        var fields = new ObjectArrayList<String>(ordered.size());
        var ids = new ObjectArrayList<String>(ordered.size());
        var mapping = new O2OOpenCacheHashMap<String, String>();
        for (var clip : ordered) {
            var field = uniqueField(file, chapter.id(), clip.field());
            var id = uniqueId(file, chapter.id(), clip.questId());
            fields.add(field);
            ids.add(id);
            mapping.put(clip.chapter() + "/" + clip.field(), field);
        }
        var forward = new ObjectArrayList<Op>(ordered.size() + LINK_CLIPBOARD.size());
        var inverse = new ObjectArrayList<Op>(ordered.size() + LINK_CLIPBOARD.size());
        for (int i = 0; i < ordered.size(); i++) {
            var clip = ordered.get(i);
            var removals = removals(clip, mapping);
            var refs = new ObjectArrayList<QuestEdits.DuplicateRef>(clip.refs().size());
            for (var ref : clip.refs()) {
                if (removed(removals, ref.offset())) continue;
                var mapped = mapping.get(ref.chapter() + "/" + ref.field());
                refs.add(mapped == null ? ref : new QuestEdits.DuplicateRef(ref.offset(), ref.original(), file.className, chapter.id(), mapped));
            }
            var payload = new QuestEdits.DuplicatePayload(clip.chapterField(), clip.field(), fields.get(i), clip.fieldOffset(),
                    clip.idStart(), clip.idEnd(), clip.xStart(), clip.xEnd(), clip.yStart(), clip.yEnd(), clip.body(), refs,
                    clip.deps(), removals, clip.imports());
            forward.add(Op.duplicate(chapter.id(), fields.get(i), ids.get(i), QuestEdits.encodeDuplicate(payload),
                    QuestEdits.round(x + clip.x() - clipAnchorX), QuestEdits.round(y + clip.y() - clipAnchorY)));
            inverse.add(0, Op.delete("q:" + chapter.id() + "/" + ids.get(i)));
        }
        for (var link : LINK_CLIPBOARD) {
            var field = uniqueField(file, chapter.id(), link.field() == null ? "LINK" : link.field());
            forward.add(Op.copy(chapter.id(), field, link.targetId(), QuestEdits.round(x + link.x() - clipAnchorX),
                    QuestEdits.round(y + link.y() - clipAnchorY), link.mainNode()));
            inverse.add(0, Op.delete("c:" + chapter.id() + ":" + field));
        }
        return QuestEditQueue.submit("粘贴 " + (ordered.size() + LINK_CLIPBOARD.size()) + " 个节点", forward, inverse);
    }

    @Nullable
    private static ObjectArrayList<Clip> orderedClips() {
        int n = CLIPBOARD.size();
        var state = new byte[n];
        var out = new ObjectArrayList<Clip>(n);
        for (int i = 0; i < n; i++) {
            if (!visitClip(i, state, out)) return null;
        }
        return out;
    }

    private static boolean visitClip(int index, byte[] state, ObjectArrayList<Clip> out) {
        if (state[index] == 2) return true;
        if (state[index] == 1) return false;
        state[index] = 1;
        for (var ref : CLIPBOARD.get(index).refs()) {
            int target = indexOfClip(ref.chapter(), ref.field());
            if (target >= 0 && !visitClip(target, state, out)) return false;
        }
        state[index] = 2;
        out.add(CLIPBOARD.get(index));
        return true;
    }

    private static int indexOfClip(String chapter, String field) {
        for (int i = 0; i < CLIPBOARD.size(); i++) {
            var clip = CLIPBOARD.get(i);
            if (clip.chapter().equals(chapter) && clip.field().equals(field)) return i;
        }
        return -1;
    }

    private static ObjectArrayList<QuestEdits.DuplicateRemoval> removals(Clip clip, O2OOpenCacheHashMap<String, String> mapping) {
        var raw = new ObjectArrayList<QuestEdits.DuplicateRemoval>();
        var deps = clip.deps();
        if (deps != null) {
            for (var call : deps) {
                var args = call.args();
                int first = -1;
                for (int k = 0; k < args.size(); k++) {
                    var arg = args.get(k);
                    if (mapping.containsKey(arg.chapter() + "/" + arg.field())) {
                        first = k;
                        break;
                    }
                }
                if (first < 0) {
                    raw.add(new QuestEdits.DuplicateRemoval(call.callStart(), call.callEnd()));
                    continue;
                }
                if (first > 0) {
                    raw.add(new QuestEdits.DuplicateRemoval(args.get(0).start(), args.get(first).start()));
                }
                for (int k = first + 1; k < args.size(); k++) {
                    var arg = args.get(k);
                    if (mapping.containsKey(arg.chapter() + "/" + arg.field())) continue;
                    raw.add(new QuestEdits.DuplicateRemoval(args.get(k - 1).end(), arg.end()));
                }
            }
        }
        return merge(raw);
    }

    private static ObjectArrayList<QuestEdits.DuplicateRemoval> merge(ObjectArrayList<QuestEdits.DuplicateRemoval> list) {
        if (list.size() < 2) return list;
        list.sort((a, b) -> Integer.compare(a.start(), b.start()));
        var out = new ObjectArrayList<QuestEdits.DuplicateRemoval>(list.size());
        var current = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            var next = list.get(i);
            if (next.start() <= current.end()) {
                current = new QuestEdits.DuplicateRemoval(current.start(), Math.max(current.end(), next.end()));
            } else {
                out.add(current);
                current = next;
            }
        }
        out.add(current);
        return out;
    }

    private static boolean removed(List<QuestEdits.DuplicateRemoval> removals, int offset) {
        for (var removal : removals) {
            if (offset >= removal.start() && offset < removal.end()) return true;
        }
        return false;
    }

    public static Result mirrorToChapter(List<? extends QuestDependency> objects, QuestChapter chapter, double x, double y) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var src = source();
        var file = src.fileOf(chapter);
        if (file == null || file.chapterField == null) return Result.fail("找不到章节 " + chapter.id() + " 的源码");
        var forward = new ObjectArrayList<Op>(objects.size());
        var inverse = new ObjectArrayList<Op>(objects.size());
        for (int i = 0; i < objects.size(); i++) {
            var target = targetOf(objects.get(i));
            if (target == null) return Result.fail("只能镜像任务节点");
            var decl = src.declOf(target);
            if (decl == null || decl.field == null) return Result.fail("找不到任务 " + target.key() + " 的源码声明");
            var field = uniqueLinkField(file, chapter.id(), decl.field);
            var op = Op.copy(chapter.id(), field, "q:" + target.key(), QuestEdits.round(x + i * 2), QuestEdits.round(y + i * 2), false);
            forward.add(op);
            inverse.add(0, Op.delete(op.target()));
        }
        return QuestEditQueue.submit("镜像 " + objects.size() + " 个任务到 " + chapter.id(), forward, inverse);
    }

    private static String uniqueLinkField(QuestSource.FileModel file, String chapter, String base) {
        for (int n = 1;; n++) {
            var candidate = base + "_COPY_" + n;
            if (!file.fields.containsKey(candidate) && pending(QuestEdits.COPY, "c:" + chapter + ":" + candidate, null) == null) return candidate;
        }
    }

    public static Result setShape(Quest quest, @Nullable QuestShape shape) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var id = "q:" + quest.key();
        var old = quest.shape();
        var constant = shape == null ? "" : shape.id().toUpperCase(Locale.ROOT);
        var oldConstant = old == null ? "" : old.id().toUpperCase(Locale.ROOT);
        return QuestEditQueue.submit("更改边框样式：" + name(quest), List.of(Op.shape(id, constant)), List.of(Op.shape(id, oldConstant)),
                () -> edited(QuestLive.setShape(quest, shape), true), () -> edited(QuestLive.setShape(quest, old), true));
    }

    public static Result setIconScale(Quest quest, double scale) {
        var blocked = guard();
        if (blocked != null) return blocked;
        double old = quest.iconScale();
        var id = "q:" + quest.key();
        return QuestEditQueue.submit("更改图标大小：" + name(quest), List.of(Op.iconScale(id, scale)), List.of(Op.iconScale(id, old)),
                () -> edited(QuestLive.setIconScale(quest, scale), true), () -> edited(QuestLive.setIconScale(quest, old), true));
    }

    public static boolean isMainNode(QuestDependency object) {
        return object instanceof Quest quest ? quest.mainNode() : object instanceof QuestLink link && link.isMainNode();
    }

    public static Result setMainNode(QuestDependency object, boolean on) {
        var blocked = guard();
        if (blocked != null) return blocked;
        if (isMainNode(object) == on) return Result.ok("");
        var id = identity(object);
        if (id == null) return Result.fail("找不到节点的源码");
        var label = (on ? "设为主节点：" : "取消主节点：") + (object instanceof Quest quest ? name(quest) : name(((QuestLink) object).target()) + "（镜像）");
        if (object instanceof Quest quest) {
            return QuestEditQueue.submit(label, List.of(Op.flag(id, "mainNode", on)), List.of(Op.flag(id, "mainNode", !on)),
                    () -> edited(QuestLive.setFlag(quest, "mainNode", on), true), () -> edited(QuestLive.setFlag(quest, "mainNode", !on), true));
        }
        var link = (QuestLink) object;
        return QuestEditQueue.submit(label, List.of(Op.linkMain(id, on)), List.of(Op.linkMain(id, !on)),
                () -> edited(QuestLive.setMainNode(link, on), true), () -> edited(QuestLive.setMainNode(link, !on), true));
    }

    public static Result setPreset(Quest quest, @Nullable QuestPreset preset) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var old = quest.preset();
        var next = preset == null ? "" : preset.name();
        var previous = old == null ? "" : old.name();
        var id = "q:" + quest.key();
        return QuestEditQueue.submit((preset == null ? "清除样式预设：" : "更改样式预设：" + preset.id()) + " · " + name(quest),
                List.of(Op.preset(id, next)), List.of(Op.preset(id, previous)),
                () -> edited(QuestLive.setPreset(quest, preset), true), () -> edited(QuestLive.setPreset(quest, old), true));
    }

    public static Result setNodeScale(Quest quest, double scale) {
        var blocked = guard();
        if (blocked != null) return blocked;
        double old = quest.nodeScale();
        var id = "q:" + quest.key();
        return QuestEditQueue.submit("更改节点大小：" + name(quest), List.of(Op.nodeScale(id, scale)), List.of(Op.nodeScale(id, old)),
                () -> edited(QuestLive.setNodeScale(quest, scale), true), () -> edited(QuestLive.setNodeScale(quest, old), true));
    }

    public static Result setNodeFill(Quest quest, int rgb) {
        var blocked = guard();
        if (blocked != null) return blocked;
        int old = quest.nodeFill();
        var id = "q:" + quest.key();
        return QuestEditQueue.submit((rgb < 0 ? "清除节点底色：" : "更改节点底色：") + name(quest),
                List.of(Op.nodeFill(id, rgb)), List.of(Op.nodeFill(id, old)),
                () -> edited(QuestLive.setNodeFill(quest, rgb), true), () -> edited(QuestLive.setNodeFill(quest, old), true));
    }

    public static Result setQuestIcon(Quest quest, Item item) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var syntax = QuestItemSyntax.of(source(), item);
        if (syntax == null) return Result.fail(BuiltInRegistries.ITEM.getKey(item) + " 无可用 Java 写法，请在源码中手写");
        var decl = source().declOf(quest);
        if (decl == null) return Result.fail("找不到任务的源码声明");
        var iconCalls = decl.calls("icon");
        var stackCalls = decl.calls("iconStack");
        String oldGroup = !stackCalls.isEmpty() ? "iconStack" : iconCalls.isEmpty() ? "" : "icon";
        String oldText = !stackCalls.isEmpty() ? decl.file.text(stackCalls.get(0)) :
                iconCalls.isEmpty() || iconCalls.get(0).getArguments().isEmpty() ? "" : decl.file.text(iconCalls.get(0).getArguments().get(0));
        var oldIcon = quest.icon();
        var id = "q:" + quest.key();
        return QuestEditQueue.submit("更改任务图标：" + name(quest), List.of(Op.icon(id, syntax.args(), syntax.imports(), false, null)),
                List.of(Op.icon(id, oldText, List.of(), true, oldGroup)),
                () -> edited(QuestLive.setIcon(quest, QuestIcon.item(item)), true), () -> edited(QuestLive.setIcon(quest, oldIcon), true));
    }

    public static Result setRewardTier(Quest quest, int index, QuestTierSet tiers) {
        var blocked = guard();
        if (blocked != null) return blocked;
        var decl = source().declOf(quest);
        if (decl == null) return Result.fail("找不到任务的源码声明");
        var entries = QuestEdits.rewardEntries(decl);
        if (index < 0 || index >= entries.size()) return Result.fail("奖励位置已变化，请重新打开");
        var entry = entries.get(index);
        if (entry.argIndex() < 0) return Result.fail("scaledReward 不能单独设置难度");
        var next = tiers.name();
        if (next.equals(QuestEdits.effectiveTier(entry))) return Result.ok("");
        var expr = decl.file.text(entry.expression());
        var id = "q:" + quest.key();
        var oldTiers = QuestTierSet.valueOf(QuestEdits.effectiveTier(entry));
        return QuestEditQueue.submit("设置奖励难度：" + name(quest), List.of(Op.rewardTier(id, index, next, expr, entry.argIndex(), true, null)),
                List.of(Op.rewardTier(id, index, entry.group(), expr, entry.argIndex(), false, entry.tier())),
                () -> edited(QuestLive.setRewardTier(quest, entry.liveIndex(), tiers), true),
                () -> edited(QuestLive.setRewardTier(quest, entry.liveIndex(), oldTiers), true));
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
            if (deleting.contains(decl)) continue;
            deleting.add(decl);
            forward.add(Op.delete(id));
            inverse.add(QuestEditQueue.undelete(id));
        }
        var error = cleanup(src, deleting, forward, inverse);
        if (error != null) return Result.fail(error);
        return QuestEditQueue.submit("删除 " + deleting.size() + " 个节点", forward, inverse);
    }

    private record Allowed(QuestSource.FileModel file, int start, int end) {}

    @Nullable
    private static String cleanup(QuestSource src, List<QuestSource.Decl> deleting, ObjectArrayList<Op> forward, ObjectArrayList<Op> inverse) {
        var allowed = new ObjectArrayList<Allowed>();
        for (var decl : deleting) {
            var tree = decl.var != null ? decl.var : decl instanceof LinkDecl link ? link.call : null;
            if (tree != null) allowed.add(new Allowed(decl.file, decl.file.start(tree), decl.file.end(tree)));
        }
        for (var file : src.files) {
            for (var quest : file.quests) {
                for (var call : quest.calls("dependsOn")) {
                    for (var arg : call.getArguments()) {
                        var target = src.resolve(file, arg);
                        if (!deleting.contains(target)) continue;
                        forward.add(Op.dependency("q:" + quest.key(), src.identityOf(target), false));
                        inverse.add(0, Op.dependency("q:" + quest.key(), src.identityOf(target), true));
                        allowed.add(new Allowed(file, file.start(arg), file.end(arg)));
                    }
                }
            }
        }
        var extra = new ObjectArrayList<QuestSource.Decl>();
        for (var file : src.files) {
            for (var link : file.links) {
                if (!deleting.contains(src.resolve(file, link.targetRef))) continue;
                if (link.field == null) return "有复制的内联装饰引用了要删除的任务，无法自动移除，请先去掉该装饰";
                if (link.var == null) return "有复制节点引用了要删除的任务，无法自动移除";
                var id = src.identityOf(link);
                if (id == null) return "有复制节点引用了要删除的任务，无法自动移除";
                forward.add(0, Op.delete(id));
                inverse.add(0, QuestEditQueue.undelete(id));
                extra.add(link);
                allowed.add(new Allowed(file, file.start(link.var), file.end(link.var)));
            }
        }
        deleting.addAll(extra);
        var extraLines = new ObjectArrayList<DecoLineDecl>();
        for (var file : src.files) {
            for (var line : file.decoLines) {
                if (extraLines.contains(line) || file.chapterId == null) continue;
                var from = src.resolve(file, line.fromRef);
                var to = src.resolve(file, line.toRef);
                if (!deleting.contains(from) && !deleting.contains(to)) continue;
                var id = src.identityOf(line);
                if (id == null) return "有装饰线引用了要删除的节点，无法自动移除";
                forward.add(Op.decoLineDelete(file.chapterId, file.chapterId, id));
                var fromText = line.fromRef == null ? "" : line.file.text(line.fromRef);
                var toText = line.toRef == null ? "" : line.file.text(line.toRef);
                inverse.add(0, Op.decoLineAdd(file.chapterId, id, fromText, toText, line.file.text(line.call), line.declarationIndex));
                allowed.add(new Allowed(file, file.start(line.call), file.end(line.call)));
                extraLines.add(line);
            }
        }
        for (var decl : deleting) {
            if (decl.field == null) continue;
            for (var ref : src.referencesTo(decl)) {
                boolean ok = false;
                for (var allow : allowed) {
                    if (allow.file == ref.file() && ref.pos() >= allow.start && ref.pos() < allow.end) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) return decl.field + " 被" + ownerOf(ref) + "引用，无法自动移除，请先去掉该引用";
            }
        }
        return null;
    }

    private static String ownerOf(QuestSource.Ref ref) {
        var file = ref.file();
        for (var quest : file.quests) {
            if (ref.pos() >= file.start(quest.var) && ref.pos() < file.end(quest.var)) {
                if (QuestBook.byKey(quest.key()) instanceof Quest live) {
                    return "章节「" + live.chapter().title().getString() + "」的任务「" + live.title().getString() + "」";
                }
            }
        }
        if (file.chapterId != null && QuestBook.byKey(file.chapterId) instanceof QuestChapter chapter) {
            return "章节「" + chapter.title().getString() + "」的章节装饰";
        }
        return "章节 " + file.name();
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

    public record TaskRow(int index, @Nullable Task live, Component name, long count, int amountIndex, boolean item, boolean consume, boolean optional) {}

    public record RewardRow(int index, int amountIndex, @Nullable Reward live, Component name, long count, boolean scaled, String group, String tier) {}

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
            out.add(new RewardRow(i, amountIndex, reward, name, amountIndex < 0 ? 0 : amounts.get(amountIndex).value(), scaled, entry.group(), QuestEdits.effectiveTier(entry)));
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
        return open(location);
    }

    public static Result openImageInEditor(String identity) {
        var decl = source().find(identity);
        if (!(decl instanceof ImageDecl image)) return Result.fail("找不到图片源码");
        return open(image.file.path + ":" + image.file.line(image.charStart));
    }

    public static Result openDecoLineInEditor(QuestDecoLine line) {
        int index = line.chapter().declaredLines().indexOf(line);
        if (index < 0) return Result.fail("找不到装饰线源码");
        var decl = source().find("l:" + line.chapter().id() + ":" + index);
        if (!(decl instanceof DecoLineDecl deco)) return Result.fail("找不到装饰线源码");
        return open(deco.file.path + ":" + deco.file.line(deco.charStart));
    }

    private static Result open(String location) {
        try {
            new ProcessBuilder("cmd", "/c", "code", "-g", location).start();
        } catch (IOException e) {
            return Result.fail("无法打开 VS Code：" + e.getMessage());
        }
        return Result.ok("已在 VS Code 打开 " + Path.of(location.substring(0, location.lastIndexOf(':'))).getFileName());
    }
}

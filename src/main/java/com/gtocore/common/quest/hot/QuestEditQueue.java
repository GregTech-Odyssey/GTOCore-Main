package com.gtocore.common.quest.hot;

import com.gtocore.common.quest.hot.QuestEdits.Op;

import com.gtolib.GTOCore;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

/**
 * 热修改的全局操作表：编辑立即入表并落盘（崩溃后下次启动重放），同一对象的同类操作按幂等键去重；
 * 编辑停顿后在后台合并写回源码并热加载（不打断操作）；代码被外部改动时先把操作表写进去再热加载（全屏锁定）。
 */
public final class QuestEditQueue {

    private static final long IDLE_MILLIS = 1500;
    private static final int MAX_HISTORY = 200;
    private static final String UNDELETE = "undelete";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private record Journal(long nextSeq, List<Op> pending) {}

    private record Action(String label, List<Op> forward, List<Op> inverse, @Nullable BooleanSupplier liveForward, @Nullable BooleanSupplier liveInverse, int generation) {}

    private static final Object LOCK = new Object();
    private static final ObjectArrayList<Op> PENDING = new ObjectArrayList<>();
    private static final ObjectArrayList<Action> UNDO = new ObjectArrayList<>();
    private static final ObjectArrayList<Action> REDO = new ObjectArrayList<>();
    private static final OpenCacheHashSet<Path> EXTERNAL = new OpenCacheHashSet<>();
    private static final ScheduledExecutorService WORKER = Executors.newSingleThreadScheduledExecutor(r -> {
        var thread = new Thread(r, "GTO Quest Edit Queue");
        thread.setDaemon(true);
        return thread;
    });
    private static long nextSeq = 1;
    private static int generation;
    private static volatile int version;
    private static volatile boolean flushing;
    private static boolean loaded;
    private static List<Op> snapshot = Collections.emptyList();
    private static int snapshotVersion = -1;
    @Nullable
    private static ScheduledFuture<?> scheduled;
    @Nullable
    private static BiConsumer<Boolean, String> reporter;

    private QuestEditQueue() {}

    public static void setReporter(BiConsumer<Boolean, String> reporter) {
        QuestEditQueue.reporter = reporter;
    }

    private static void report(boolean ok, String message) {
        var target = reporter;
        if (target != null) target.accept(ok, message);
        else GTOCore.LOGGER.info("[任务热修改] {}", message);
    }

    @Nullable
    private static Path journalPath() {
        var root = QuestHot.sourceRoot();
        if (root == null) return null;
        var worktree = root.toAbsolutePath().getParent().getParent().getParent();
        return worktree.resolve("run").resolve("quest-edit-journal.json");
    }

    public static void load() {
        synchronized (LOCK) {
            if (loaded) return;
            loaded = true;
            var path = journalPath();
            if (path == null || !Files.isRegularFile(path)) return;
            try {
                var journal = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), Journal.class);
                if (journal != null && journal.pending != null) {
                    for (var op : journal.pending) PENDING.add(op.withLive(false));
                    nextSeq = Math.max(nextSeq, journal.nextSeq);
                }
            } catch (IOException | JsonParseException e) {
                GTOCore.LOGGER.warn("[任务热修改] 读取操作表 {} 失败，已忽略", path, e);
            }
            version++;
        }
        if (!PENDING.isEmpty()) report(true, "操作表里有 " + PENDING.size() + " 项上次未写入源码的修改，开始重放");
    }

    private static void save() {
        var path = journalPath();
        if (path == null) return;
        String json;
        synchronized (LOCK) {
            json = GSON.toJson(new Journal(nextSeq, new ObjectArrayList<>(PENDING)));
        }
        try {
            Files.createDirectories(path.getParent());
            var temp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temp, json, StandardCharsets.UTF_8);
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            GTOCore.LOGGER.warn("[任务热修改] 保存操作表 {} 失败", path, e);
        }
    }

    public static int version() {
        return version;
    }

    public static boolean flushing() {
        return flushing;
    }

    public static List<Op> pending() {
        synchronized (LOCK) {
            if (snapshotVersion != version) {
                snapshot = List.copyOf(PENDING);
                snapshotVersion = version;
            }
            return snapshot;
        }
    }

    public static int pendingCount() {
        return pending().size();
    }

    @Nullable
    public static String undoLabel() {
        synchronized (LOCK) {
            return UNDO.isEmpty() ? null : UNDO.top().label;
        }
    }

    @Nullable
    public static String redoLabel() {
        synchronized (LOCK) {
            return REDO.isEmpty() ? null : REDO.top().label;
        }
    }

    static QuestHot.Result submit(String label, List<Op> forward, List<Op> inverse) {
        return submit(label, forward, inverse, null, null);
    }

    static QuestHot.Result submit(String label, List<Op> forward, List<Op> inverse, @Nullable BooleanSupplier liveForward, @Nullable BooleanSupplier liveInverse) {
        boolean live = liveForward != null && liveForward.getAsBoolean();
        synchronized (LOCK) {
            for (var op : forward) enqueue(op.withLive(live));
            UNDO.push(new Action(label, forward, inverse, liveForward, liveInverse, generation));
            if (UNDO.size() > MAX_HISTORY) UNDO.remove(0);
            REDO.clear();
            version++;
        }
        save();
        schedule(IDLE_MILLIS);
        return QuestHot.Result.ok(label);
    }

    private static void enqueue(Op op) {
        switch (op.type()) {
            case UNDELETE -> {
                PENDING.removeIf(p -> p.type().equals(QuestEdits.DELETE) && p.target().equals(op.target()));
                return;
            }
            case QuestEdits.DELETE -> {
                boolean created = PENDING.removeIf(p -> (p.type().equals(QuestEdits.CREATE) || p.type().equals(QuestEdits.COPY)) && p.target().equals(op.target()));
                PENDING.removeIf(p -> p.target().equals(op.target()) && !p.type().equals(QuestEdits.DELETE));
                if (created) return;
            }
            case QuestEdits.MOVE -> {
                for (int i = 0; i < PENDING.size(); i++) {
                    var p = PENDING.get(i);
                    if ((p.type().equals(QuestEdits.CREATE) || p.type().equals(QuestEdits.COPY)) && p.target().equals(op.target())) {
                        PENDING.set(i, p.movedTo(op.x(), op.y()));
                        return;
                    }
                }
            }
            default -> {}
        }
        var key = op.key();
        PENDING.removeIf(p -> p.key().equals(key));
        PENDING.add(op.withSeq(nextSeq++));
    }

    static Op undelete(String target) {
        return new Op(0, UNDELETE, target, null, null, null, 0, 0, 0, 0, false, null, false);
    }

    private static boolean deletePending(String target) {
        for (var p : PENDING) {
            if (p.type().equals(QuestEdits.DELETE) && p.target().equals(target)) return true;
        }
        return false;
    }

    public static QuestHot.Result undo() {
        Action action;
        synchronized (LOCK) {
            if (UNDO.isEmpty()) return QuestHot.Result.fail("没有可撤销的修改");
            action = UNDO.top();
            for (var op : action.inverse) {
                if (op.type().equals(UNDELETE) && !deletePending(op.target())) {
                    return QuestHot.Result.fail("删除已写入源码，无法在游戏里撤销；请在 VS Code 源代码管理里还原");
                }
            }
            UNDO.pop();
            boolean live = action.generation == generation && action.liveInverse != null && action.liveInverse.getAsBoolean();
            for (var op : action.inverse) enqueue(op.withLive(live));
            REDO.push(action);
            version++;
        }
        save();
        schedule(IDLE_MILLIS);
        return QuestHot.Result.ok("撤销：" + action.label);
    }

    public static QuestHot.Result redo() {
        Action action;
        synchronized (LOCK) {
            if (REDO.isEmpty()) return QuestHot.Result.fail("没有可重做的修改");
            action = REDO.pop();
            boolean live = action.generation == generation && action.liveForward != null && action.liveForward.getAsBoolean();
            for (var op : action.forward) enqueue(op.withLive(live));
            UNDO.push(action);
            version++;
        }
        save();
        schedule(IDLE_MILLIS);
        return QuestHot.Result.ok("重做：" + action.label);
    }

    public static void bookRebuilt() {
        synchronized (LOCK) {
            generation++;
            PENDING.replaceAll(op -> op.withLive(false));
            version++;
        }
    }

    public static void flushNow() {
        schedule(0);
    }

    public static void externalChange(Collection<Path> paths) {
        synchronized (LOCK) {
            EXTERNAL.addAll(paths);
        }
        WORKER.execute(QuestEditQueue::flush);
    }

    private static synchronized void schedule(long delay) {
        var previous = scheduled;
        if (previous != null) previous.cancel(false);
        scheduled = WORKER.schedule(QuestEditQueue::flush, delay, TimeUnit.MILLISECONDS);
    }

    private static int order(Op op) {
        if (!op.type().equals(QuestEdits.DELETE)) return 0;
        return op.target().startsWith("q:") ? 2 : 1;
    }

    private static void flush() {
        if (flushing || QuestHot.sourceRoot() == null) return;
        ObjectArrayList<Op> batch;
        ObjectArrayList<Path> external;
        synchronized (LOCK) {
            external = new ObjectArrayList<>(EXTERNAL);
            EXTERNAL.clear();
            if (PENDING.isEmpty() && external.isEmpty()) return;
            batch = new ObjectArrayList<>(PENDING);
            flushing = true;
        }
        boolean rebuild = !external.isEmpty();
        for (var op : batch) rebuild |= !op.live();
        var job = QuestHotProgress.begin(!external.isEmpty());
        job.stage(QuestHotProgress.WRITE, batch.size() + " 项操作", 0.05f, 0.25f, 200L + 40L * batch.size());
        batch.sort((a, b) -> order(a) != order(b) ? Integer.compare(order(a), order(b)) : Long.compare(a.seq(), b.seq()));
        var src = QuestSource.load(QuestHot.sourceRoot()).copy();
        var before = new O2OOpenCacheHashMap<Path, String>();
        var errors = new ObjectArrayList<String>();
        for (var op : batch) {
            var error = QuestEdits.apply(src, op, before);
            if (error != null) errors.add(error);
        }
        var after = new O2OOpenCacheHashMap<Path, String>();
        for (var file : src.files) {
            var original = before.get(file.path);
            if (original != null && !original.equals(file.content)) after.put(file.path, file.content);
        }
        if (!errors.isEmpty()) report(false, "有 " + errors.size() + " 项修改无法写入源码，已丢弃：\n" + QuestHot.summarize(errors));
        var written = after.isEmpty() ? new ObjectArrayList<Path>() : write(after);
        if (written == null) {
            report(false, "写入源码失败，这一批修改留在操作表里，稍后重试");
            flushing = false;
            job.end();
            return;
        }
        var paths = new ObjectArrayList<Path>(written);
        for (var path : external) {
            if (!paths.contains(path)) paths.add(path);
        }
        if (paths.isEmpty()) {
            finish(batch);
            job.end();
            return;
        }
        QuestHot.reload(paths, job.blocking(), rebuild, failure -> {
            if (failure != null && !after.isEmpty()) {
                boolean untouched = true;
                for (var it = after.object2ObjectEntrySet().fastIterator(); it.hasNext();) {
                    var entry = it.next();
                    if (!entry.getValue().equals(read(entry.getKey()))) untouched = false;
                }
                report(false, "这一批修改刷新失败，" + (untouched ? "已还原源码并丢弃" : "源码已在别处改动，未还原") + "：\n" + failure);
                if (untouched) {
                    var restored = write(before);
                    if (restored != null) QuestHot.reload(restored, job.blocking(), true, f -> {});
                }
            }
            finish(batch);
            job.end();
        });
    }

    private static void finish(List<Op> batch) {
        var seqs = new LongOpenHashSet(batch.size());
        for (var op : batch) seqs.add(op.seq());
        boolean external, more;
        synchronized (LOCK) {
            PENDING.removeIf(p -> seqs.contains(p.seq()));
            version++;
            external = !EXTERNAL.isEmpty();
            more = !PENDING.isEmpty();
        }
        flushing = false;
        save();
        if (external) WORKER.execute(QuestEditQueue::flush);
        else if (more) schedule(IDLE_MILLIS);
    }

    @Nullable
    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private static List<Path> write(O2OOpenCacheHashMap<Path, String> contents) {
        var paths = new ObjectArrayList<Path>(contents.size());
        for (var it = contents.object2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            QuestSource.forget(entry.getKey());
            try {
                Files.writeString(entry.getKey(), entry.getValue(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                GTOCore.LOGGER.warn("[任务热修改] 写入 {} 失败", entry.getKey(), e);
                return null;
            }
            paths.add(entry.getKey());
        }
        return paths;
    }
}

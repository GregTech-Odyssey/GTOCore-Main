package com.gtocore.client;

import com.gtocore.common.quest.hot.QuestEditQueue;
import com.gtocore.common.quest.hot.QuestHot;
import com.gtocore.common.quest.hot.QuestHotProgress;

import com.gtolib.GTOCore;
import com.gtolib.api.quest.QuestBook;
import com.gtolib.quest.QuestServer;
import com.gtolib.quest.client.ClientQuestData;
import com.gtolib.quest.client.book.QuestBookScreen;
import com.gtolib.quest.client.book.QuestBookSession;
import com.gtolib.quest.client.book.QuestIconRenderer;
import com.gtolib.quest.client.book.QuestRefreshBar;

import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * 任务书热加载的实现：监听 data/quest 源码或接收 {@link QuestHot} 提交的改动，游戏内只编译这些文件，用新类加载器重跑章节定义并重建整本任务书。
 * 只支持单人游戏；进度按存盘键迁移，打开着的任务书原地换成新内容；编译或构建失败时保留原任务书。
 */
@OnlyIn(Dist.CLIENT)
public final class QuestHotReload {

    private static final String PACKAGE = "com.gtocore.data.quest.";
    private static final String CONTENTS = PACKAGE + "QuestBookContents";
    private static final String PACKAGE_PATH = PACKAGE.replace('.', '/');
    private static final long DEBOUNCE_MS = 300;
    private static final int MAX_REPORTED_ERRORS = 8;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        var thread = new Thread(r, "GTO Quest Hot Reload");
        thread.setDaemon(true);
        return thread;
    });
    private static final O2OOpenCacheHashMap<Path, String> SUBMITTED = new O2OOpenCacheHashMap<>();
    private static final ConcurrentLinkedQueue<Runnable> DEFERRED = new ConcurrentLinkedQueue<>();
    private static boolean replayed;

    @Nullable
    private static Path questDir;
    private static List<Path> classpath = List.of();
    private static final ObjectArrayList<Path> CLASS_DIRS = new ObjectArrayList<>();
    @Nullable
    private static Path overlay;
    @Nullable
    private static StandardJavaFileManager fileManager;
    @Nullable
    private static JavaCompiler compiler;

    private QuestHotReload() {}

    public static void init() {
        var root = QuestHot.sourceRoot();
        if (root == null) return;
        QuestHot.setStartupFailureHandler(error -> Minecraft.getInstance().delayCrash(CrashReport.forThrowable(error, "GTOCore quest source validation")));
        var config = System.getProperty(QuestHot.CONFIG_PROPERTY);
        try {
            var lines = Files.readAllLines(Path.of(config), StandardCharsets.UTF_8);
            var entries = new ObjectArrayList<Path>(lines.size());
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) continue;
                var entry = Path.of(lines.get(i));
                entries.add(entry);
                if (Files.isDirectory(entry)) CLASS_DIRS.add(entry);
            }
            classpath = entries;
            overlay = Files.createTempDirectory("gtocore-quest-reload");
        } catch (IOException | RuntimeException e) {
            GTOCore.LOGGER.warn("[任务热加载] 读取配置 {} 失败，热加载未启用", config, e);
            return;
        }
        questDir = root.resolve(PACKAGE_PATH);
        QuestHot.setReloader(QuestHotReload::submit);
        QuestHot.setLiveListener(QuestHotReload::edited);
        QuestEditQueue.setReporter((ok, text) -> report(Component.literal("[任务热修改] " + text).withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED)));
        QuestEditQueue.load();
        MinecraftForge.EVENT_BUS.addListener(QuestHotReload::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(QuestHotReload::onScreenInit);
        MinecraftForge.EVENT_BUS.addListener(QuestHotReload::onScreenRender);
        MinecraftForge.EVENT_BUS.addListener(QuestHotReload::onGuiRender);
        var watcher = new Thread(QuestHotReload::watch, "GTO Quest Source Watcher");
        watcher.setDaemon(true);
        watcher.start();
        GTOCore.LOGGER.info("[任务热加载] 已启用，监听 {}", questDir);
    }

    public static boolean isEnabled() {
        return questDir != null;
    }

    public static void reloadAll() {
        var dir = questDir;
        if (dir == null) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            QuestEditQueue.externalChange(walk.filter(QuestHotReload::isSource).toList());
        } catch (IOException e) {
            report(Component.literal("[任务热加载] 读取源码目录失败：" + e).withStyle(ChatFormatting.RED));
        }
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (replayed || !(event.getScreen() instanceof TitleScreen)) return;
        replayed = true;
        if (QuestEditQueue.pendingCount() > 0) QuestEditQueue.flushNow();
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Runnable task;
        while ((task = DEFERRED.poll()) != null) task.run();
        var minecraft = Minecraft.getInstance();
        var screen = minecraft.screen;
        if (QuestHotProgress.blocking() && !(screen instanceof QuestReloadScreen) && !(screen instanceof QuestBookScreen) && !(screen instanceof AbstractContainerScreen<?>)) {
            minecraft.setScreen(new QuestReloadScreen(screen));
        }
    }

    private static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof QuestBookScreen) return;
        if (QuestHotProgress.active() && !QuestHotProgress.blocking()) QuestRefreshBar.drawCorner(event.getGuiGraphics(), event.getScreen().width);
    }

    private static void onGuiRender(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().screen == null && QuestHotProgress.active() && !QuestHotProgress.blocking()) {
            QuestRefreshBar.drawCorner(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth());
        }
    }

    public static void submit(Collection<Path> sources, boolean blocking, @Nullable Consumer<String> done) {
        submit(sources, blocking, true, done);
    }

    public static void submit(Collection<Path> sources, boolean blocking, boolean rebuild, @Nullable Consumer<String> done) {
        var batch = List.copyOf(sources);
        var job = QuestHotProgress.begin(blocking);
        job.stage(QuestHotProgress.READ, "编译 " + batch.size() + " 个文件", 0.25f, 0.75f, compiler == null ? 3000 : 400L + 150L * batch.size());
        synchronized (SUBMITTED) {
            for (var path : batch) {
                var content = read(path);
                if (content != null) SUBMITTED.put(path.toAbsolutePath().normalize(), content);
            }
        }
        EXECUTOR.execute(() -> reload(batch, rebuild, job, done));
    }

    private static boolean isSource(Path path) {
        return path.toString().endsWith(".java") && Files.isRegularFile(path);
    }

    @Nullable
    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean alreadySubmitted(Path path) {
        var content = read(path);
        synchronized (SUBMITTED) {
            return content != null && content.equals(SUBMITTED.get(path.toAbsolutePath().normalize()));
        }
    }

    private static void watch() {
        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            try (Stream<Path> walk = Files.walk(questDir)) {
                for (var dir : walk.filter(Files::isDirectory).toList()) register(service, dir);
            }
            var changed = new ObjectArrayList<Path>();
            while (true) {
                collect(service, service.take(), changed);
                WatchKey key;
                while ((key = service.poll(DEBOUNCE_MS, TimeUnit.MILLISECONDS)) != null) collect(service, key, changed);
                changed.removeIf(QuestHotReload::alreadySubmitted);
                if (changed.isEmpty()) continue;
                var batch = List.copyOf(changed);
                changed.clear();
                QuestEditQueue.externalChange(batch);
            }
        } catch (InterruptedException | ClosedWatchServiceException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            GTOCore.LOGGER.warn("[任务热加载] 源码监听中断", e);
        }
    }

    private static void register(WatchService service, Path dir) throws IOException {
        dir.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    private static void collect(WatchService service, WatchKey key, ObjectArrayList<Path> changed) {
        var dir = (Path) key.watchable();
        for (var event : key.pollEvents()) {
            if (!(event.context() instanceof Path name)) continue;
            var path = dir.resolve(name);
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(path)) {
                try {
                    register(service, path);
                } catch (IOException e) {
                    GTOCore.LOGGER.warn("[任务热加载] 无法监听新目录 {}", path, e);
                }
            } else if (isSource(path) && !changed.contains(path)) {
                changed.add(path);
            }
        }
        key.reset();
    }

    private static void reload(Collection<Path> sources, boolean rebuild, QuestHotProgress.Job job, @Nullable Consumer<String> done) {
        if (sources.isEmpty()) {
            job.end();
            finish(done, null);
            return;
        }
        long start = System.nanoTime();
        var errors = compile(sources);
        long compiled = System.nanoTime();
        if (!errors.isEmpty()) {
            var message = Component.literal("[任务热加载] 编译失败（" + errors.size() + " 处），任务书未改动").withStyle(ChatFormatting.RED);
            for (int i = 0; i < Math.min(errors.size(), MAX_REPORTED_ERRORS); i++) message.append(Component.literal("\n" + errors.get(i)).withStyle(ChatFormatting.GRAY));
            report(message);
            job.end();
            finish(done, "编译失败：" + errors.get(0));
            return;
        }
        if (!rebuild) {
            job.end();
            finish(done, null);
            return;
        }
        var loader = new Loader(overlay, CLASS_DIRS, QuestHotReload.class.getClassLoader());
        job.stage(QuestHotProgress.READ, "重建任务书", 0.75f, 0.98f, 600);
        DEFERRED.add(() -> apply(loader, sources.size(), (compiled - start) / 1_000_000, done, job));
    }

    private static void finish(@Nullable Consumer<String> done, @Nullable String failure) {
        if (done == null) return;
        Minecraft.getInstance().execute(() -> done.accept(failure));
    }

    private static List<String> compile(Collection<Path> sources) {
        var errors = new ObjectArrayList<String>();
        var javac = compiler();
        if (javac == null) {
            errors.add("当前运行环境没有 Java 编译器（需要用 JDK 启动游戏）");
            return errors;
        }
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var units = fileManager.getJavaFileObjectsFromPaths(sources);
        var options = List.of("-encoding", "UTF-8", "-proc:none", "-g", "-implicit:none", "-nowarn", "-Xlint:none");
        boolean ok = javac.getTask(null, fileManager, diagnostics, options, null, units).call();
        for (var diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() != Diagnostic.Kind.ERROR) continue;
            var source = diagnostic.getSource();
            var file = source == null ? "?" : Path.of(source.toUri()).getFileName().toString();
            errors.add(file + ":" + diagnostic.getLineNumber() + " " + diagnostic.getMessage(Locale.SIMPLIFIED_CHINESE));
        }
        if (!ok && errors.isEmpty()) errors.add("javac 返回失败但没有给出错误");
        return errors;
    }

    @Nullable
    private static JavaCompiler compiler() {
        if (compiler != null) return compiler;
        var javac = ToolProvider.getSystemJavaCompiler();
        if (javac == null) return null;
        var manager = javac.getStandardFileManager(null, Locale.SIMPLIFIED_CHINESE, StandardCharsets.UTF_8);
        var path = new ObjectArrayList<Path>(classpath.size() + 1);
        path.add(overlay);
        path.addAll(classpath);
        try {
            manager.setLocationFromPaths(StandardLocation.CLASS_PATH, path);
            manager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(overlay));
        } catch (IOException e) {
            GTOCore.LOGGER.warn("[任务热加载] 设置编译路径失败", e);
            return null;
        }
        fileManager = manager;
        compiler = javac;
        return javac;
    }

    private static void apply(Loader loader, int files, long compileMs, @Nullable Consumer<String> done, QuestHotProgress.Job job) {
        var minecraft = Minecraft.getInstance();
        var server = minecraft.getSingleplayerServer();
        if (minecraft.getConnection() != null && (server == null || server.isPublished())) {
            report(Component.literal("[任务热加载] 只支持单人游戏（未开放局域网），任务书未改动").withStyle(ChatFormatting.RED));
            job.end();
            if (done != null) done.accept("只支持单人游戏");
            return;
        }
        var chapters = QuestBook.chapters();
        var oldIds = new String[chapters.size()];
        for (int i = 0; i < oldIds.length; i++) oldIds[i] = chapters.get(i).id();
        Runnable registrar = () -> runContents(loader);
        long start = System.nanoTime();
        if (server == null) {
            int missing;
            try {
                missing = rebuildWithoutWorld(registrar);
            } catch (IllegalStateException | LinkageError e) {
                rebuildFailed(e, done, job);
                return;
            }
            afterRebuild(oldIds, false, missing, start, files, compileMs, done, job);
            return;
        }
        server.submit(() -> QuestServer.rebuildBook(registrar)).whenComplete((missing, error) -> minecraft.execute(() -> {
            if (error != null) rebuildFailed(error instanceof CompletionException && error.getCause() != null ? error.getCause() : error, done, job);
            else afterRebuild(oldIds, true, missing, start, files, compileMs, done, job);
        }));
    }

    private static void rebuildFailed(Throwable cause, @Nullable Consumer<String> done, QuestHotProgress.Job job) {
        GTOCore.LOGGER.warn("[任务热加载] 重建任务书失败", cause);
        report(Component.literal("[任务热加载] 重建失败，保留原任务书：\n").withStyle(ChatFormatting.RED)
                .append(Component.literal(String.valueOf(cause.getMessage())).withStyle(ChatFormatting.GRAY)));
        job.end();
        if (done != null) done.accept("重建失败：" + cause.getMessage());
    }

    private static void afterRebuild(String[] oldIds, boolean world, int missing, long start, int files, long compileMs, @Nullable Consumer<String> done, QuestHotProgress.Job job) {
        long rebuildMs = (System.nanoTime() - start) / 1_000_000;
        long clientStart = System.nanoTime();
        QuestIconRenderer.clearCaches();
        QuestBookSession.remapChapters(oldIds);
        if (world) ClientQuestData.clear();
        QuestEditQueue.bookRebuilt();
        QuestBookScreen.onBookRebuilt();
        GTOCore.LOGGER.info("[任务热加载] 重建 {}ms（服务端线程），客户端收尾 {}ms", rebuildMs, (System.nanoTime() - clientStart) / 1_000_000);
        job.end();
        var problems = QuestHot.validateAfterReload();
        var message = Component.literal("[任务热加载] 已重载 " + QuestBook.chapters().size() + " 章 " + QuestBook.quests().size() + " 个任务（编译 " + files + " 个文件 " + compileMs + "ms，重建 " + rebuildMs + "ms）")
                .withStyle(ChatFormatting.GREEN);
        if (missing > 0) message.append(Component.literal("\n有 " + missing + " 条进度对应的任务已不存在，存盘后会丢弃").withStyle(ChatFormatting.YELLOW));
        if (!problems.isEmpty()) {
            message.append(Component.literal("\n源码有 " + problems.size() + " 处不能被热修改系统解析，编辑模式已停用：\n" + QuestHot.summarize(problems)).withStyle(ChatFormatting.RED));
        }
        if (done == null || !problems.isEmpty()) report(message);
        if (done != null) done.accept(null);
    }

    private static void edited(boolean logic) {
        QuestBookScreen.onBookEdited();
        if (!logic) return;
        var server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) server.execute(QuestServer::onBookEdited);
    }

    private static int rebuildWithoutWorld(Runnable registrar) {
        QuestBook.rebuild(registrar);
        return 0;
    }

    private static void runContents(Loader loader) {
        try {
            var contents = Class.forName(CONTENTS, false, loader);
            MethodHandles.publicLookup().findStatic(contents, "init", MethodType.methodType(void.class)).invoke();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException("载入新的任务书内容失败：" + e, e);
        }
    }

    private static void report(Component message) {
        var minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.player != null) minecraft.player.sendSystemMessage(message);
            else GTOCore.LOGGER.info(message.getString());
        });
    }

    private static final class Loader extends ClassLoader {

        private final Path overlay;
        private final ObjectArrayList<Path> classDirs;

        private Loader(Path overlay, ObjectArrayList<Path> classDirs, ClassLoader parent) {
            super("gtocore-quest-reload", parent);
            this.overlay = overlay;
            this.classDirs = classDirs;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith(PACKAGE)) return super.loadClass(name, resolve);
            synchronized (getClassLoadingLock(name)) {
                var loaded = findLoadedClass(name);
                if (loaded == null) {
                    var bytes = read(name.replace('.', '/') + ".class");
                    if (bytes == null) throw new ClassNotFoundException(name);
                    loaded = defineClass(name, bytes, 0, bytes.length);
                }
                if (resolve) resolveClass(loaded);
                return loaded;
            }
        }

        @Nullable
        private byte[] read(String resource) throws ClassNotFoundException {
            try {
                var file = overlay.resolve(resource);
                if (Files.isRegularFile(file)) return Files.readAllBytes(file);
                for (int i = 0; i < classDirs.size(); i++) {
                    file = classDirs.get(i).resolve(resource);
                    if (Files.isRegularFile(file)) return Files.readAllBytes(file);
                }
                try (InputStream in = getParent().getResourceAsStream(resource)) {
                    return in == null ? null : in.readAllBytes();
                }
            } catch (IOException e) {
                throw new ClassNotFoundException(resource, e);
            }
        }
    }
}

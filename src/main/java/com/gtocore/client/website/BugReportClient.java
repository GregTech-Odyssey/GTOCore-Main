package com.gtocore.client.website;

import com.gtocore.common.website.BugLogs;
import com.gtocore.common.website.BugReportServer;
import com.gtocore.utils.GTOWebsite;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * /bug：取服务端的权威版本、环境与难度，以官网账号领反馈链接并预填，上传本机日志（服务器上再由服务端上传它的），逐文件显示进度，最后在聊天栏给出链接。
 */
@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class BugReportClient {

    @RegisterLanguage(cn = "提交bug反馈", en = "Bug report")
    static final String TITLE = "gtocore.bug.title";
    @RegisterLanguage(cn = "正在读取存档信息……", en = "Reading the save…")
    static final String STAGE_INFO = "gtocore.bug.stage.info";
    @RegisterLanguage(cn = "正在领取反馈链接……", en = "Opening a report link…")
    static final String STAGE_LINK = "gtocore.bug.stage.link";
    @RegisterLanguage(cn = "正在上传日志，关闭本窗口不影响上传。", en = "Uploading logs. Closing this window does not stop the upload.")
    static final String STAGE_UPLOAD = "gtocore.bug.stage.upload";
    @RegisterLanguage(cn = "已完成。请打开反馈页面补充剩余信息并提交。", en = "Done. Open the report page to fill in the rest and submit it.")
    static final String STAGE_DONE = "gtocore.bug.stage.done";
    @RegisterLanguage(cn = "失败：%s", en = "Failed: %s")
    static final String STAGE_FAILED = "gtocore.bug.stage.failed";
    @RegisterLanguage(cn = "等待中", en = "Waiting")
    static final String ROW_WAITING = "gtocore.bug.row.waiting";
    @RegisterLanguage(cn = "正在处理", en = "Preparing")
    static final String ROW_PREPARING = "gtocore.bug.row.preparing";
    @RegisterLanguage(cn = "正在上传", en = "Uploading")
    static final String ROW_UPLOADING = "gtocore.bug.row.uploading";
    @RegisterLanguage(cn = "已上传", en = "Uploaded")
    static final String ROW_DONE = "gtocore.bug.row.done";
    @RegisterLanguage(cn = "已上传（超过5MB，保留开头1MB与末尾2MB）", en = "Uploaded (over 5 MB, kept the first 1 MB and the last 2 MB)")
    static final String ROW_TRUNCATED = "gtocore.bug.row.truncated";
    @RegisterLanguage(cn = "上传失败：%s", en = "Upload failed: %s")
    static final String ROW_FAILED = "gtocore.bug.row.failed";
    @RegisterLanguage(cn = "服务端%s", en = "Server: %s")
    static final String ROW_SERVER = "gtocore.bug.row.server";
    @RegisterLanguage(cn = "本机%s", en = "This computer: %s")
    static final String ROW_CLIENT = "gtocore.bug.row.client";
    @RegisterLanguage(cn = "服务端日志刚上传过，5分钟内不再重复上传。", en = "The server log was uploaded moments ago; it is not uploaded again within 5 minutes.")
    static final String SERVER_COOLDOWN = "gtocore.bug.server.cooldown";
    @RegisterLanguage(cn = "服务器没有及时回应，已改用本机的信息。", en = "The server did not answer in time; this computer's details are used instead.")
    static final String SERVER_SILENT = "gtocore.bug.server.silent";
    @RegisterLanguage(cn = "打开反馈页面", en = "Open report page")
    static final String OPEN = "gtocore.bug.open";
    @RegisterLanguage(cn = "关闭", en = "Close")
    static final String CLOSE = "gtocore.bug.close";
    @RegisterLanguage(cn = "已有一次bug反馈正在进行。", en = "A bug report is already in progress.")
    static final String BUSY = "gtocore.bug.busy";
    @RegisterLanguage(cn = "日志%s超过5MB，已保留开头1MB与末尾2MB上传。", en = "The log %s was over 5 MB; its first 1 MB and last 2 MB were uploaded.")
    static final String CHAT_TRUNCATED = "gtocore.bug.chat.truncated";
    @RegisterLanguage(cn = "%s上传失败：%s", en = "%s could not be uploaded: %s")
    static final String CHAT_FAILED = "gtocore.bug.chat.failed";
    @RegisterLanguage(cn = "反馈链接已就绪，%s，在官网补充剩余信息后提交。", en = "Your report link is ready. %s, fill in the rest on the website and submit it.")
    static final String CHAT_READY = "gtocore.bug.chat.ready";
    @RegisterLanguage(cn = "点击此处打开", en = "Click here to open it")
    static final String CHAT_CLICK = "gtocore.bug.chat.click";
    @RegisterLanguage(cn = "bug反馈未完成：%s", en = "The bug report could not be started: %s")
    static final String CHAT_ABORTED = "gtocore.bug.chat.aborted";
    @RegisterLanguage(cn = "多人游戏中只有管理员（OP）可以使用/bug。", en = "In multiplayer, only operators can use /bug.")
    static final String OP_ONLY = "gtocore.bug.op_only";

    private static final long INFO_TIMEOUT_MS = 10_000;
    private static final long SERVER_IDLE_TIMEOUT_MS = 120_000;
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, "GTO-Bug");
        thread.setDaemon(true);
        return thread;
    });

    private static final class Row {

        private final boolean server;
        private final String name;
        private volatile int state = -1;
        private volatile long sent;
        private volatile long total;
        private volatile boolean truncated;
        private volatile String error = "";

        private Row(boolean server, String name) {
            this.server = server;
            this.name = name;
        }

        private boolean finished() {
            return state == BugReportServer.STATE_DONE || state == BugReportServer.STATE_FAILED;
        }
    }

    @Nullable
    private static volatile Session session;

    private static final class Session {

        private final List<Row> rows = new ObjectArrayList<>();
        private volatile Component stage = Component.translatable(STAGE_INFO);
        private volatile int stageColor = UITheme.textSecondary();
        private volatile boolean infoArrived;
        private volatile boolean serverUploads;
        private volatile boolean serverDone = true;
        private volatile long serverHeardAt;
        private volatile boolean cooldown;
        @Nullable
        private volatile String url;
        private volatile boolean finished;
    }

    private BugReportClient() {}

    public static void run() {
        var minecraft = Minecraft.getInstance();
        if (!minecraft.hasSingleplayerServer() && (minecraft.player == null || !minecraft.player.hasPermissions(BugReportServer.PERMISSION_LEVEL))) {
            chat(Component.translatable(OP_ONLY).withStyle(ChatFormatting.RED));
            return;
        }
        if (!WebsiteConsent.granted()) {
            WebsiteConsent.open(null);
            return;
        }
        if (WebsiteAccount.token() == null) {
            WebsiteAccount.open();
            return;
        }
        var current = session;
        if (current != null && !current.finished) {
            chat(Component.translatable(BUSY).withStyle(ChatFormatting.YELLOW));
            openProgress();
            return;
        }
        var started = new Session();
        session = started;
        openProgress();
        BugReportServer.request();
        CompletableFuture.delayedExecutor(INFO_TIMEOUT_MS, TimeUnit.MILLISECONDS).execute(() -> {
            if (session == started && !started.infoArrived) {
                minecraft.execute(() -> {
                    if (session != started || started.infoArrived) return;
                    chat(Component.translatable(SERVER_SILENT).withStyle(ChatFormatting.YELLOW));
                    begin(started, BugReportServer.packVersion(), minecraft.getSingleplayerServer() != null && !minecraft.getSingleplayerServer().isPublished() ? "single" : "multi", "", false, List.of());
                });
            }
        });
    }

    public static void onInfo(FriendlyByteBuf buf) {
        String version = buf.readUtf();
        String environment = buf.readUtf();
        String difficulty = buf.readUtf();
        boolean uploads = buf.readBoolean();
        int count = buf.readVarInt();
        var names = new ObjectArrayList<String>(count);
        for (int i = 0; i < count; i++) names.add(buf.readUtf());
        Minecraft.getInstance().execute(() -> {
            var current = session;
            if (current == null || current.infoArrived || current.finished) return;
            begin(current, version, environment, difficulty, uploads, names);
        });
    }

    public static void onServerProgress(FriendlyByteBuf buf) {
        int index = buf.readVarInt();
        String name = buf.readUtf();
        int state = buf.readByte();
        long sent = buf.readVarLong();
        long total = buf.readVarLong();
        boolean truncated = buf.readBoolean();
        String error = buf.readUtf();
        var current = session;
        if (current == null || !current.serverUploads) return;
        current.serverHeardAt = System.currentTimeMillis();
        if (state == BugReportServer.STATE_SKIPPED) {
            current.cooldown = true;
            for (var row : current.rows) {
                if (row.server) row.state = BugReportServer.STATE_FAILED;
            }
            current.serverDone = true;
            return;
        }
        int seen = 0;
        for (var row : current.rows) {
            if (!row.server) continue;
            if (seen++ == index) {
                row.state = state;
                row.sent = sent;
                row.total = total;
                row.truncated = truncated;
                row.error = error;
                return;
            }
        }
        if (name.isEmpty()) current.serverDone = true;
    }

    private static void begin(Session current, String version, String environment, String difficulty, boolean uploads, List<String> serverFiles) {
        current.infoArrived = true;
        current.serverUploads = uploads && !serverFiles.isEmpty();
        current.serverDone = !current.serverUploads;
        for (var source : BugLogs.sources("client")) current.rows.add(new Row(false, source.name()));
        if (current.serverUploads) for (String name : serverFiles) current.rows.add(new Row(true, name));
        stage(current, Component.translatable(STAGE_LINK), UITheme.textSecondary());
        if (WebsitePopupScreen.isOpen("bug_report")) openProgress();
        String token = WebsiteAccount.token();
        WORKER.execute(() -> work(current, token, version, environment, difficulty));
    }

    private static void work(Session current, @Nullable String account, String version, String environment, String difficulty) {
        String linkToken;
        try {
            var body = new JsonObject();
            if (!version.isEmpty()) body.addProperty("version", version);
            body.addProperty("priority", "default");
            body.addProperty("environment", environment);
            if (!difficulty.isEmpty()) body.addProperty("difficulty", difficulty);
            var request = HttpRequest.newBuilder(URI.create(GTOWebsite.origin() + "/api/game/v1/feedback-links"))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", WebsiteAccount.USER_AGENT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Cookie", "gto_locale=" + siteLocale())
                    .header("Authorization", "Bearer " + account)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();
            var response = GTOWebsite.http().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 401) {
                Minecraft.getInstance().execute(() -> {
                    abort(current, BugLogs.failure(response));
                    WebsiteAccount.signOut(true);
                    WebsiteAccount.open();
                });
                return;
            }
            if (response.statusCode() != 200 && response.statusCode() != 201) {
                String reason = BugLogs.failure(response);
                Minecraft.getInstance().execute(() -> abort(current, reason));
                return;
            }
            var root = JsonParser.parseString(response.body()).getAsJsonObject();
            current.url = root.get("url").getAsString();
            linkToken = root.get("token").getAsString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        } catch (Exception e) {
            String reason = String.valueOf(e.getMessage());
            Minecraft.getInstance().execute(() -> abort(current, reason));
            return;
        }
        stage(current, Component.translatable(STAGE_UPLOAD), UITheme.textSecondary());
        if (current.serverUploads) {
            current.serverHeardAt = System.currentTimeMillis();
            String token = linkToken;
            Minecraft.getInstance().execute(() -> BugReportServer.upload(token));
        }
        var sources = BugLogs.sources("client");
        int index = 0;
        for (var row : current.rows) {
            if (row.server) continue;
            var source = index < sources.size() ? sources.get(index) : null;
            index++;
            if (source == null) {
                row.state = BugReportServer.STATE_FAILED;
                continue;
            }
            row.state = BugReportServer.STATE_PREPARING;
            try {
                var prepared = BugLogs.prepare(source);
                row.truncated = prepared.truncated();
                row.state = BugReportServer.STATE_UPLOADING;
                BugLogs.upload(linkToken, prepared, WebsiteAccount.USER_AGENT, (sent, total) -> {
                    row.sent = sent;
                    row.total = total;
                });
                row.state = BugReportServer.STATE_DONE;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                GTOCore.LOGGER.warn("[Bug] log upload failed: {}", e.toString());
                row.error = String.valueOf(e.getMessage());
                row.state = BugReportServer.STATE_FAILED;
            }
        }
        while (!current.serverDone && System.currentTimeMillis() - current.serverHeardAt < SERVER_IDLE_TIMEOUT_MS) sleep(250);
        for (var row : current.rows) {
            if (row.server && !row.finished()) {
                row.state = BugReportServer.STATE_FAILED;
                if (row.error.isEmpty()) row.error = "timeout";
            }
        }
        Minecraft.getInstance().execute(() -> finish(current));
    }

    private static void finish(Session current) {
        current.finished = true;
        stage(current, Component.translatable(STAGE_DONE), UITheme.STATUS_TEXT_GOOD);
        if (current.cooldown) chat(Component.translatable(SERVER_COOLDOWN).withStyle(ChatFormatting.YELLOW));
        for (var row : current.rows) {
            Component name = Component.literal(row.name);
            if (row.state == BugReportServer.STATE_FAILED && !row.error.isEmpty()) {
                chat(Component.translatable(CHAT_FAILED, name, row.error).withStyle(ChatFormatting.RED));
            } else if (row.truncated) {
                chat(Component.translatable(CHAT_TRUNCATED, name).withStyle(ChatFormatting.YELLOW));
            }
        }
        String url = current.url;
        if (url == null) return;
        MutableComponent click = Component.translatable(CHAT_CLICK).withStyle(s -> s.withColor(ChatFormatting.AQUA).withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(url))));
        chat(Component.translatable(CHAT_READY, click).withStyle(ChatFormatting.GREEN));
    }

    private static void abort(Session current, String reason) {
        current.finished = true;
        stage(current, Component.translatable(STAGE_FAILED, reason), UITheme.STATUS_TEXT_ERROR);
        chat(Component.translatable(CHAT_ABORTED, reason).withStyle(ChatFormatting.RED));
    }

    private static void stage(Session current, Component text, int color) {
        current.stage = text;
        current.stageColor = color;
    }

    private static void openProgress() {
        WebsitePopupScreen.open("bug_report", null, close -> Popup.of(() -> Component.translatable(TITLE), content -> progress(content, close))
                .setContentWidth(2 * UISizes.CONTENT_WIDTH));
    }

    private static void progress(UIElement content, Runnable close) {
        var current = session;
        if (current == null) return;
        int width = 2 * UISizes.CONTENT_WIDTH;
        content.addChild(Label.of(width, () -> current.stage).bindClientColor(() -> current.stageColor));
        for (var row : current.rows) {
            var section = UIElement.column(width).layout(l -> l.gapAll(UISizes.GAP));
            section.addChild(ProgressBar.of(width, rowName(row), UITheme.barProgress(), () -> new ProgressBar.Progress(row.sent, row.state == BugReportServer.STATE_DONE ? 0 : Math.max(row.total, 1), 0)).percent());
            section.addChild(TextLine.of(width, () -> rowState(row)).bindClientColor(() -> row.state == BugReportServer.STATE_FAILED ? UITheme.STATUS_TEXT_ERROR : row.truncated ? UITheme.STATUS_TEXT_WARNING : UITheme.textSecondary()));
            content.addChild(section);
        }
        int action = 2 * UISizes.BUTTON_WIDTH;
        var closeButton = Button.translatable(action, CLOSE).setOnClientClick(close);
        var open = Button.translatable(action, OPEN).setVariant(UITheme.ButtonVariant.CONFIRM).setOnClientClick(() -> {
            String url = current.url;
            if (url == null || !current.finished) return;
            Util.getPlatform().openUri(url);
            close.run();
        });
        content.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(closeButton, UIElement.flexSpacer(), open));
    }

    private static Component rowName(Row row) {
        return Component.translatable(row.server ? ROW_SERVER : ROW_CLIENT, row.name);
    }

    private static Component rowState(Row row) {
        return switch (row.state) {
            case BugReportServer.STATE_PREPARING -> Component.translatable(ROW_PREPARING);
            case BugReportServer.STATE_UPLOADING -> Component.translatable(ROW_UPLOADING);
            case BugReportServer.STATE_DONE -> Component.translatable(row.truncated ? ROW_TRUNCATED : ROW_DONE);
            case BugReportServer.STATE_FAILED -> Component.translatable(ROW_FAILED, row.error.isEmpty() ? "-" : row.error);
            default -> Component.translatable(ROW_WAITING);
        };
    }

    private static String siteLocale() {
        String code = Minecraft.getInstance().getLanguageManager().getSelected();
        if (code.startsWith("zh")) return "zh";
        if (code.startsWith("ja")) return "ja";
        if (code.startsWith("ru")) return "ru";
        if (code.startsWith("es")) return "es";
        if (code.startsWith("pt")) return "pt";
        return "en";
    }

    private static void chat(Component text) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.sendSystemMessage(text);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

package com.gtocore.common.website;

import com.gtocore.client.website.BugReportClient;
import com.gtocore.config.GTOConfig;

import com.gtolib.GTOCore;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.api.rule.RuleManager;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLLoader;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * /bug 的服务端一半：回答权威的整合包版本、游戏环境与难度，专用服按服主配置用反馈链接 token 上传自己的日志并逐文件回报进度。
 */
public final class BugReportServer {

    public static final int STATE_PREPARING = 0;
    public static final int STATE_UPLOADING = 1;
    public static final int STATE_DONE = 2;
    public static final int STATE_FAILED = 3;
    public static final int STATE_SKIPPED = 4;
    public static final int PERMISSION_LEVEL = 2;

    private static final String USER_AGENT = "GTOCore-Bug-Server/1";
    private static final long UPLOAD_COOLDOWN_MS = 5 * 60 * 1000;
    private static final long PROGRESS_INTERVAL_MS = 200;
    private static final Object2LongOpenHashMap<UUID> LAST_UPLOAD = new Object2LongOpenHashMap<>();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, "GTO-Bug-Server");
        thread.setDaemon(true);
        return thread;
    });

    static final NetworkPack REQUEST = NetworkPack.registerC2S("gtocore:bug_request", BugReportServer::onRequest);
    static final NetworkPack INFO = NetworkPack.registerS2C("gtocore:bug_info", (player, buf) -> BugReportClient.onInfo(buf));
    static final NetworkPack UPLOAD = NetworkPack.registerC2S("gtocore:bug_upload", BugReportServer::onUpload);
    static final NetworkPack PROGRESS = NetworkPack.registerS2C("gtocore:bug_progress", (player, buf) -> BugReportClient.onServerProgress(buf));

    private BugReportServer() {}

    public static void init() {}

    public static void request() {
        REQUEST.send();
    }

    public static void upload(String linkToken) {
        UPLOAD.send(buf -> buf.writeUtf(linkToken, 64));
    }

    private static void onRequest(ServerPlayer player, FriendlyByteBuf buf) {
        var server = player.server;
        server.execute(() -> {
            if (!allowed(player)) return;
            String version = packVersion();
            String environment = server.isDedicatedServer() || server.isPublished() ? "multi" : "single";
            String difficulty = difficulty();
            boolean uploads = serverUploads(server);
            var files = uploads ? BugLogs.sources("server") : java.util.List.<BugLogs.Source>of();
            INFO.send(out -> {
                out.writeUtf(version);
                out.writeUtf(environment);
                out.writeUtf(difficulty);
                out.writeBoolean(uploads);
                out.writeVarInt(files.size());
                for (var file : files) out.writeUtf(file.name());
            }, player);
        });
    }

    private static void onUpload(ServerPlayer player, FriendlyByteBuf buf) {
        String token = buf.readUtf(64);
        var server = player.server;
        server.execute(() -> {
            if (!allowed(player) || !serverUploads(server) || !BugLogs.isLinkToken(token)) return;
            long now = System.currentTimeMillis();
            UUID id = player.getUUID();
            if (now - LAST_UPLOAD.getOrDefault(id, 0L) < UPLOAD_COOLDOWN_MS) {
                progress(server, id, 0, "", STATE_SKIPPED, 0, 0, false, "cooldown");
                return;
            }
            LAST_UPLOAD.put(id, now);
            WORKER.execute(() -> uploadAll(server, id, token));
        });
    }

    private static void uploadAll(MinecraftServer server, UUID player, String token) {
        var sources = BugLogs.sources("server");
        for (int i = 0; i < sources.size(); i++) {
            var source = sources.get(i);
            int index = i;
            progress(server, player, index, source.name(), STATE_PREPARING, 0, 0, false, "");
            try {
                var prepared = BugLogs.prepare(source);
                long[] lastSent = { 0 };
                BugLogs.upload(token, prepared, USER_AGENT, (sent, total) -> {
                    long now = System.currentTimeMillis();
                    if (sent < total && now - lastSent[0] < PROGRESS_INTERVAL_MS) return;
                    lastSent[0] = now;
                    progress(server, player, index, source.name(), STATE_UPLOADING, sent, total, prepared.truncated(), "");
                });
                progress(server, player, index, source.name(), STATE_DONE, prepared.bytes().length, prepared.bytes().length, prepared.truncated(), "");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                GTOCore.LOGGER.warn("[Bug] server log upload failed: {}", e.toString());
                progress(server, player, index, source.name(), STATE_FAILED, 0, 0, false, String.valueOf(e.getMessage()));
            }
        }
        progress(server, player, sources.size(), "", STATE_DONE, 0, 0, false, "");
    }

    private static void progress(MinecraftServer server, UUID id, int index, String name, int state, long sent, long total, boolean truncated, String error) {
        server.execute(() -> {
            var player = server.getPlayerList().getPlayer(id);
            if (player == null) return;
            PROGRESS.send(out -> {
                out.writeVarInt(index);
                out.writeUtf(name);
                out.writeByte(state);
                out.writeVarLong(sent);
                out.writeVarLong(total);
                out.writeBoolean(truncated);
                out.writeUtf(error.length() > 300 ? error.substring(0, 300) : error);
            }, player);
        });
    }

    private static boolean allowed(ServerPlayer player) {
        return player.server.isSingleplayerOwner(player.getGameProfile()) || player.hasPermissions(PERMISSION_LEVEL);
    }

    private static boolean serverUploads(MinecraftServer server) {
        return server.isDedicatedServer() && GTOConfig.INSTANCE.misc.bugReportServerLogs;
    }

    public static String packVersion() {
        String version = GTCEu.isDev() ? FMLLoader.getLoadingModList().getModFileById(GTOCore.MOD_ID).versionString() : GTOCore.getPackVersion();
        String hash = com.gtolib.MixinConfigPlugin.hash;
        return hash != null ? version + "-" + hash : version;
    }

    private static String difficulty() {
        var snapshot = RuleManager.current();
        return snapshot.changedCount() > 0 ? "custom" : snapshot.base().id;
    }
}

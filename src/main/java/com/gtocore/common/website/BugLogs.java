package com.gtocore.common.website;

import com.gtocore.utils.GTOWebsite;

import net.minecraftforge.fml.loading.FMLPaths;

import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * /bug 上传的日志：本端的 latest.log 与 24 小时内最新的崩溃报告，超出官网单文件上限时截到 3 MB 以内，IP 打码，带进度传到反馈链接。客户端与服务端共用。
 */
public final class BugLogs {

    public static final long SITE_LIMIT = 5L << 20;
    public static final int TRUNCATED_LIMIT = 3 << 20;
    private static final int HEAD_BYTES = 1 << 20;
    private static final long CRASH_WINDOW_MS = 24L * 60 * 60 * 1000;
    private static final Pattern TOKEN = Pattern.compile("^[A-Za-z0-9_-]{43}$");
    private static final Pattern IPV4 = Pattern.compile("(?<![\\w.])(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?!\\w|\\.\\d)");
    private static final Pattern IPV6 = Pattern.compile("(?<![\\w:.])(?:[0-9A-Fa-f]{0,4}:){2,7}[0-9A-Fa-f]{0,4}(?![\\w:.])");
    private static final String IPV4_MASK = "x.x.x.x";
    private static final String IPV6_MASK = "x:x:x:x";

    public record Source(Path path, String name) {}

    public record Prepared(String name, byte[] bytes, boolean truncated) {}

    @FunctionalInterface
    public interface Progress {

        void update(long sent, long total);
    }

    private BugLogs() {}

    public static boolean isLinkToken(String token) {
        return TOKEN.matcher(token).matches();
    }

    public static List<Source> sources(String side) {
        var out = new ObjectArrayList<Source>(2);
        Path game = FMLPaths.GAMEDIR.get();
        Path latest = game.resolve("logs").resolve("latest.log");
        if (Files.isRegularFile(latest)) out.add(new Source(latest, side + "-latest.log"));
        Path crash = newestCrash(game.resolve("crash-reports"));
        if (crash != null) out.add(new Source(crash, side + "-" + crash.getFileName()));
        return out;
    }

    @Nullable
    private static Path newestCrash(Path dir) {
        if (!Files.isDirectory(dir)) return null;
        long since = System.currentTimeMillis() - CRASH_WINDOW_MS;
        Path best = null;
        long bestTime = since;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "crash-*.txt")) {
            for (Path file : stream) {
                long time = Files.getLastModifiedTime(file).toMillis();
                if (time >= bestTime) {
                    best = file;
                    bestTime = time;
                }
            }
        } catch (IOException ignored) {}
        return best;
    }

    public static Prepared prepare(Source source) throws IOException {
        long size = Files.size(source.path());
        String text;
        boolean truncated = size > SITE_LIMIT;
        if (truncated) {
            text = truncate(source.path(), size);
        } else {
            text = new String(Files.readAllBytes(source.path()), StandardCharsets.UTF_8);
        }
        return new Prepared(source.name(), mask(text).getBytes(StandardCharsets.UTF_8), truncated);
    }

    private static String truncate(Path path, long size) throws IOException {
        int tailBytes = TRUNCATED_LIMIT - HEAD_BYTES - 256;
        byte[] head = new byte[HEAD_BYTES];
        byte[] tail = new byte[tailBytes];
        try (var file = new RandomAccessFile(path.toFile(), "r")) {
            file.readFully(head);
            file.seek(size - tailBytes);
            file.readFully(tail);
        }
        int headEnd = lastNewline(head);
        int tailStart = firstNewline(tail);
        long omitted = size - headEnd - (tailBytes - tailStart);
        return new String(head, 0, headEnd, StandardCharsets.UTF_8) +
                "\n\n[GTO /bug: " + omitted + " bytes omitted to fit the 3 MB upload limit]\n\n" +
                new String(tail, tailStart, tailBytes - tailStart, StandardCharsets.UTF_8);
    }

    private static int lastNewline(byte[] bytes) {
        for (int i = bytes.length - 1; i >= 0; i--) {
            if (bytes[i] == '\n') return i + 1;
        }
        return bytes.length;
    }

    private static int firstNewline(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '\n') return i + 1;
        }
        return 0;
    }

    static String mask(String text) {
        return maskIpv6(maskIpv4(text));
    }

    private static String maskIpv4(String text) {
        Matcher matcher = IPV4.matcher(text);
        if (!matcher.find()) return text;
        var out = new StringBuilder(text.length());
        int last = 0;
        do {
            String ip = matcher.group();
            out.append(text, last, matcher.start()).append(ip.equals("127.0.0.1") || ip.equals("0.0.0.0") ? ip : IPV4_MASK);
            last = matcher.end();
        } while (matcher.find());
        return out.append(text, last, text.length()).toString();
    }

    private static String maskIpv6(String text) {
        Matcher matcher = IPV6.matcher(text);
        if (!matcher.find()) return text;
        var out = new StringBuilder(text.length());
        int last = 0;
        do {
            String ip = matcher.group();
            out.append(text, last, matcher.start()).append(isIpv6(ip) ? IPV6_MASK : ip);
            last = matcher.end();
        } while (matcher.find());
        return out.append(text, last, text.length()).toString();
    }

    private static boolean isIpv6(String candidate) {
        if (candidate.equals("::1") || candidate.equals("::") || candidate.length() < 6) return false;
        if (candidate.contains("::")) return true;
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            if ((c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) return true;
        }
        return false;
    }

    public static void upload(String linkToken, Prepared file, String userAgent, Progress progress) throws IOException, InterruptedException {
        byte[] bytes = file.bytes();
        long total = bytes.length;
        progress.update(0, total);
        var body = HttpRequest.BodyPublishers.fromPublisher(HttpRequest.BodyPublishers.ofInputStream(() -> new CountingStream(bytes, progress)), total);
        var request = HttpRequest.newBuilder(URI.create(GTOWebsite.origin() + "/api/game/v1/feedback-links/" + linkToken + "/files"))
                .timeout(Duration.ofMinutes(3))
                .header("User-Agent", userAgent)
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("X-File-Name", URLEncoder.encode(file.name(), StandardCharsets.UTF_8).replace("+", "%20"))
                .POST(body)
                .build();
        var response = GTOWebsite.http().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 201) throw new IOException(failure(response));
        progress.update(total, total);
    }

    public static String failure(HttpResponse<String> response) {
        try {
            var root = JsonParser.parseString(response.body()).getAsJsonObject();
            if (root.has("detail")) return root.get("detail").getAsString();
            if (root.has("error")) return "HTTP " + response.statusCode() + " " + root.get("error").getAsString();
        } catch (RuntimeException ignored) {}
        return "HTTP " + response.statusCode();
    }

    private static final class CountingStream extends InputStream {

        private final ByteArrayInputStream in;
        private final Progress progress;
        private final long total;
        private long sent;

        private CountingStream(byte[] bytes, Progress progress) {
            this.in = new ByteArrayInputStream(bytes);
            this.progress = progress;
            this.total = bytes.length;
        }

        @Override
        public int read() {
            int value = in.read();
            if (value >= 0) progress.update(++sent, total);
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) {
            int read = in.read(buffer, offset, Math.min(length, 16 << 10));
            if (read > 0) {
                sent += read;
                progress.update(sent, total);
            }
            return read;
        }
    }
}

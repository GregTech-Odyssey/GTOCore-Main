package com.gtocore.utils;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;

/**
 * 官网地址；游戏里打开的官网地址都经 {@link #mark(URL)} 带上 from=game，网站据此把来源记为"游戏内"。
 */
public final class GTOWebsite {

    public static final String SITE = "https://gtodyssey.com";

    private static final String HOST = "gtodyssey.com";
    private static final String MARK = "from=game";
    private static final String ORIGIN = FMLLoader.isProduction() ? null : System.getProperty("gtocore.website.origin");

    public static final Path DIR = FMLPaths.GAMEDIR.get().resolve("gtocore");

    private static volatile HttpClient http;

    private GTOWebsite() {}

    public static String origin() {
        return ORIGIN != null ? ORIGIN : SITE;
    }

    public static HttpClient http() {
        var client = http;
        if (client == null) {
            synchronized (GTOWebsite.class) {
                client = http;
                if (client == null) {
                    client = HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .connectTimeout(Duration.ofSeconds(5))
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();
                    http = client;
                }
            }
        }
        return client;
    }

    public static void writeAtomically(Path target, String text) throws IOException {
        Files.createDirectories(target.getParent());
        var temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temp, text, StandardCharsets.UTF_8);
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public static URL mark(URL url) {
        if (!isWebsite(url) || hasMark(url.getQuery())) return url;
        String text = url.toString();
        int hash = text.indexOf('#');
        String base = hash < 0 ? text : text.substring(0, hash);
        String fragment = hash < 0 ? "" : text.substring(hash);
        try {
            return new URI(base + (url.getQuery() == null ? "?" : "&") + MARK + fragment).toURL();
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            return url;
        }
    }

    private static boolean isWebsite(URL url) {
        String protocol = url.getProtocol();
        if (!"https".equals(protocol) && !"http".equals(protocol)) return false;
        String host = url.getHost().toLowerCase(Locale.ROOT);
        if (host.equals(HOST) || host.equals("www." + HOST)) return true;
        if (ORIGIN == null) return false;
        try {
            URL dev = new URI(ORIGIN).toURL();
            return dev.getHost().equalsIgnoreCase(host) && dev.getPort() == url.getPort();
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean hasMark(String query) {
        if (query == null) return false;
        for (String part : query.split("&")) {
            if (part.equals(MARK)) return true;
        }
        return false;
    }
}

package com.gtocore.common.quest.hot;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class QuestHotConfig {

    public static final String PROPERTY = "gtocore.questHotReload";

    @Nullable
    private static Path sourceRoot;
    private static boolean resolved;

    private QuestHotConfig() {}

    public static boolean enabled() {
        return sourceRoot() != null;
    }

    @Nullable
    public static synchronized Path sourceRoot() {
        if (resolved) return sourceRoot;
        resolved = true;
        var config = System.getProperty(PROPERTY);
        if (config == null || !GTCEu.isDev()) return null;
        try {
            var lines = Files.readAllLines(Path.of(config), StandardCharsets.UTF_8);
            if (!lines.isEmpty()) sourceRoot = Path.of(lines.get(0));
        } catch (IOException e) {
            GTOCore.LOGGER.warn("[任务热修改] 读取配置 {} 失败，热加载与热修改未启用", config, e);
        }
        return sourceRoot;
    }
}

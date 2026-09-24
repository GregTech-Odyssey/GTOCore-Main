package com.gtocore.api.research.techtree.ui;

import com.gtolib.GTOCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Reader;

/**
 * 科技树画布内容（节点、连线、分区线、节点悬停提示）的配色，可由资源包 {@code gtocore:ui/techtree.json} 覆盖。
 * <p>
 * 窗口、面板、按钮、进度条等界面外观由框架主题（{@code UITheme}）统一决定，这里只管科技树自己的内容。
 * 默认值按亮色界面取：大面积颜色不比窗口底色 #C6C6C6 暗太多，状态色用深一档的绿 / 金，与框架状态色一致。
 */
public final class TechTreeStyle {

    public static final ResourceLocation RESOURCE = ResourceLocation.fromNamespaceAndPath("gtocore", "ui/techtree.json");
    private static final Gson GSON = new Gson();

    private static volatile TechTreeStyle current = defaults();
    public static final SimplePreparableReloadListener<Void> RELOAD_LISTENER = new SimplePreparableReloadListener<>() {

        @Override
        protected Void prepare(ResourceManager manager, ProfilerFiller profiler) {
            return null;
        }

        @Override
        protected void apply(Void ignored, ResourceManager manager, ProfilerFiller profiler) {
            TechTreeStyle.reload(manager);
        }
    };

    public final int tierSeparatorColor;
    public final int hoveredDependencyLineColor;
    public final int lockedNodeFill;
    public final int availableNodeFill;
    public final int unlockedNodeFill;
    public final int lockedNodeBorder;
    public final int availableNodeBorder;
    public final int unlockedNodeBorder;
    /// 数据中心正在研究的节点：底色与边框在两色之间呼吸（全树只有它会动）
    public final int researchingNodeFillLow;
    public final int researchingNodeFillHigh;
    public final int researchingNodeBorderLow;
    public final int researchingNodeBorderHigh;
    public final int lockedNodeOverlay;
    public final int nodeIconFallback;
    public final int nodeHoverOverlay;
    public final int defaultDependencyLine;
    public final int unlockedDependencyLine;
    public final int availableDependencyLine;
    public final int prerequisiteUnlockedDependencyLine;
    public final int cwuBarFill;
    public final int tooltipDescription;
    public final int tooltipPrerequisites;
    public final int tooltipUnlocked;
    public final int tooltipAvailable;
    public final int tooltipLocked;
    public final int tooltipResearching;

    private TechTreeStyle(JsonObject root) {
        JsonObject node = object(root, "node");
        JsonObject lines = object(root, "dependency_lines");
        JsonObject text = object(root, "text");
        JsonObject details = object(root, "details");

        tierSeparatorColor = color(root, "tier_separator_color", 0x40000000);
        hoveredDependencyLineColor = color(root, "hovered_dependency_line_color", 0xFFFFC83D);
        lockedNodeFill = color(node, "locked_fill", 0xFF8B8B8B);
        availableNodeFill = color(node, "available_fill", 0xFFD9C98C);
        unlockedNodeFill = color(node, "unlocked_fill", 0xFF8FBF7F);
        lockedNodeBorder = color(node, "locked_border", 0xFF555555);
        availableNodeBorder = color(node, "available_border", 0xFFB07A10);
        unlockedNodeBorder = color(node, "unlocked_border", 0xFF2E7D1E);
        researchingNodeFillLow = color(node, "researching_fill_low", 0xFF8CC7C2);
        researchingNodeFillHigh = color(node, "researching_fill_high", 0xFFB8ECE7);
        researchingNodeBorderLow = color(node, "researching_border_low", 0xFF006D6A);
        researchingNodeBorderHigh = color(node, "researching_border_high", 0xFF2FD3CB);
        lockedNodeOverlay = color(node, "locked_overlay", 0x80A0A0A0);
        nodeIconFallback = color(node, "icon_fallback", 0xFF202020);
        nodeHoverOverlay = color(node, "hover_overlay", 0x50FFFFFF);
        defaultDependencyLine = color(lines, "default", 0xFF8A8A8A);
        unlockedDependencyLine = color(lines, "node_unlocked", 0xFF3E8E2E);
        availableDependencyLine = color(lines, "node_available", 0xFFC49A1A);
        prerequisiteUnlockedDependencyLine = color(lines, "prerequisite_unlocked", 0xFF5C5C5C);
        cwuBarFill = color(details, "cwu_bar_fill", 0xFF6FD0C8);
        tooltipDescription = color(text, "widget_tooltip_description", 0xFFAAAAAA);
        tooltipPrerequisites = color(text, "widget_tooltip_prerequisites", 0xFFFFFF55);
        tooltipUnlocked = color(text, "widget_tooltip_unlocked", 0xFF55FF55);
        tooltipAvailable = color(text, "widget_tooltip_available", 0xFFFFAA00);
        tooltipLocked = color(text, "widget_tooltip_locked", 0xFFFF5555);
        tooltipResearching = color(text, "widget_tooltip_researching", 0xFF55FFFF);
    }

    public static TechTreeStyle get() {
        return current;
    }

    public static void reload(ResourceManager manager) {
        TechTreeStyle loaded = defaults();
        try {
            Resource resource = manager.getResource(RESOURCE).orElse(null);
            if (resource != null) {
                try (Reader reader = resource.openAsReader()) {
                    loaded = new TechTreeStyle(GSON.fromJson(reader, JsonObject.class));
                }
            }
        } catch (Exception e) {
            GTOCore.LOGGER.warn("Failed to load {}", RESOURCE, e);
        }
        current = loaded;
    }

    private static TechTreeStyle defaults() {
        return new TechTreeStyle(new JsonObject());
    }

    private static JsonObject object(JsonObject parent, String name) {
        JsonElement value = parent.get(name);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    private static int color(JsonObject parent, String name, int fallback) {
        JsonElement value = parent.get(name);
        if (value == null || value.isJsonNull()) return fallback;
        try {
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) return value.getAsInt();
            String text = value.getAsString().trim();
            if (text.startsWith("#")) text = text.substring(1);
            if (text.startsWith("0x") || text.startsWith("0X")) text = text.substring(2);
            long parsed = Long.parseLong(text, 16);
            return (int) (text.length() <= 6 ? parsed | 0xFF000000L : parsed);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}

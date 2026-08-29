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

/** Resource-pack configurable colors used by the research tree screens. */
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

    public int tierSeparatorColor;
    public int hoveredDependencyLineColor;
    public int selectedNodeBorderColor;
    public int lockedNodeFill;
    public int availableNodeFillLow;
    public int availableNodeFillHigh;
    public int unlockedNodeFill;
    public int lockedNodeBorder;
    public int availableNodeBorderLow;
    public int availableNodeBorderHigh;
    public int unlockedNodeBorder;
    public int defaultDependencyLine;
    public int unlockedDependencyLine;
    public int availableDependencyLine;
    public int prerequisiteUnlockedDependencyLine;
    public int lockedNodeOverlay;
    public int nodeIconFallback;
    public int nodeHoverBorder;
    public int nodeBoxFill;
    public int nodeBoxBorder;
    public int headerName;
    public int headerDescription;
    public int rowBackground;
    public int rowText;
    public int rowValue;
    public int rowCompletedValue;
    public int cwuBarFill;
    public int cwuBarBorder;
    public int prerequisiteHoverBorder;
    public int progressHighlightStart;
    public int widgetTooltipDescription;
    public int widgetTooltipPrerequisites;
    public int widgetTooltipUnlocked;
    public int widgetTooltipAvailable;
    public int widgetTooltipLocked;
    public int sideTabTierText;
    public int sideTabManagerText;
    public int sideTabNavigateText;
    public int sideTabTierItemText;
    public int selectorSelectedFill;
    public int selectorSelectedBorder;
    public int selectorHoverBorder;
    public int materialHighlightStart;

    private TechTreeStyle(JsonObject root) {
        JsonObject node = object(root, "node");
        JsonObject lines = object(root, "dependency_lines");
        JsonObject side = object(root, "side_tab");
        JsonObject text = object(root, "text");
        JsonObject selector = object(root, "selector");

        tierSeparatorColor = color(root, "tier_separator_color", 0x66FFFFFF);
        hoveredDependencyLineColor = color(root, "hovered_dependency_line_color", 0xFF4DE3E3);
        selectedNodeBorderColor = color(root, "selected_node_border_color", 0xFF8BE7DE);
        lockedNodeFill = color(node, "locked_fill", 0xFF2F2F34);
        availableNodeFillLow = color(node, "available_fill_low", 0xFF2F2F34);
        availableNodeFillHigh = color(node, "available_fill_high", 0xFF4C4C50);
        unlockedNodeFill = color(node, "unlocked_fill", 0xFF1E4D2B);
        lockedNodeBorder = color(node, "locked_border", 0xFF8C8C93);
        availableNodeBorderLow = color(node, "available_border_low", 0xFF8C8C93);
        availableNodeBorderHigh = color(node, "available_border_high", 0xFF9999A2);
        unlockedNodeBorder = color(node, "unlocked_border", 0xFF6CDA84);
        defaultDependencyLine = color(lines, "default", 0xFF4F4F57);
        unlockedDependencyLine = color(lines, "node_unlocked", 0xFF5CC978);
        availableDependencyLine = color(lines, "node_available", 0xFFE3C45D);
        prerequisiteUnlockedDependencyLine = color(lines, "prerequisite_unlocked", 0xFF7A7A82);
        lockedNodeOverlay = color(node, "locked_overlay", 0x55000000);
        nodeIconFallback = color(node, "icon_fallback", 0xFFFFFFFF);
        nodeHoverBorder = color(node, "hover_border", 0xFFFFFFFF);
        nodeBoxFill = color(side, "node_box_fill", 0xFF2F2F34);
        nodeBoxBorder = color(side, "node_box_border", 0xFF8C8C93);
        headerName = color(side, "header_name", 0xFFF3F3F3);
        headerDescription = color(side, "header_description", 0xFFB9B9C0);
        rowBackground = color(side, "row_background", 0xFF232328);
        rowText = color(side, "row_text", 0xFFF3F3F3);
        rowValue = color(side, "row_value", 0xFFD4D4DB);
        rowCompletedValue = color(side, "row_completed_value", 0xFF6CDA84);
        cwuBarFill = color(side, "cwu_bar_fill", 0xFF39C5BB);
        cwuBarBorder = color(side, "cwu_bar_border", 0xFF8BE7DE);
        prerequisiteHoverBorder = color(side, "prerequisite_hover_border", 0xFF39C5BB);
        progressHighlightStart = color(side, "progress_highlight_start", 0x00E2E2E2);
        widgetTooltipDescription = color(text, "widget_tooltip_description", 0xFFAAAAAA);
        widgetTooltipPrerequisites = color(text, "widget_tooltip_prerequisites", 0xFFFFFF55);
        widgetTooltipUnlocked = color(text, "widget_tooltip_unlocked", 0xFF55FF55);
        widgetTooltipAvailable = color(text, "widget_tooltip_available", 0xFFFFAA00);
        widgetTooltipLocked = color(text, "widget_tooltip_locked", 0xFFFF5555);
        sideTabTierText = color(text, "side_tab_tier", 0xFF5555FF);
        sideTabManagerText = color(text, "side_tab_manager", 0xFFAAAAAA);
        sideTabNavigateText = color(text, "side_tab_navigate", 0xFF55FF55);
        sideTabTierItemText = color(text, "side_tab_tier_item", 0xFF55FFFF);
        selectorSelectedFill = color(selector, "selected_fill", 0x5539C5BB);
        selectorSelectedBorder = color(selector, "selected_border", 0xFF39C5BB);
        selectorHoverBorder = color(selector, "hover_border", 0xFFF3F3F3);
        materialHighlightStart = color(side, "material_highlight_start", 0xFFFFFFFF);
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

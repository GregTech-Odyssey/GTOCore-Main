package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.StructureBlocks;
import com.gregtechceu.gtceu.uipro.ILayoutHost;
import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ItemView;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.view.ZoomBar;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uipro.window.PopupCard;
import com.gregtechceu.gtceu.uiwidgets.patternbuilder.PatternBuilderPanel;
import com.gregtechceu.gtceu.uiwidgets.structure.StructurePlans;
import com.gregtechceu.gtceu.uiwidgets.structure.StructureScene;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.vfyjxf.taffy.style.TaffyPosition;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class OverviewView extends UIElement implements ILayoutHost, ILocalUI {

    static final float FIT = 1.05f;
    private static final int MARGIN = 8;
    private static final int LEGEND_TEXT = 118;
    private static final int CONNECTED_COLOR = 0xC6C6C6;
    private static final int DETACHED_COLOR = 0xE0685C;
    private static final int GHOST_COLOR = 0x8CD8FF;
    private static final float GHOST_ALPHA = 0.55f;
    private static final int OVERLAY_DETACHED = 1;
    private static final int OVERLAY_GHOST = 2;
    private static final int OPAQUE = 0xFF000000;
    private static final int SWATCH_BORDER = 0xFF202020;
    private static final int SWATCH_CENTER = 0xFFFFFFFF;
    private static final int SWATCH_SIZE = 9;
    private static final int SWATCH_HALF = SWATCH_SIZE / 2;
    private static final int MIN_ZOOM = 8;
    private static final int DEFAULT_HEIGHT = 384;
    private static final int DEFAULT_MIN_Y = -64;
    private static final float MARKER_MATCH = 0.01f;

    private final Object2ObjectOpenHashMap<String, Layout> layouts = new Object2ObjectOpenHashMap<>();
    private final OverviewWidget owner;
    private final OverviewAdapter adapter;
    private final UIElement frame;
    private final StructureScene scene;
    private final UIElement legend;
    private final UIElement truncatedNotice;
    private final UIElement coarseNotice;
    private final OverviewSelector selector;
    private final OverviewBuildFlow flow;
    @Nullable
    private PopupCard card;
    @Nullable
    private PatternBuilderPanel builder;
    @Nullable
    private PopupCard builderPopup;
    @Nullable
    private String builderPopupKey;
    @Nullable
    private OverviewSnapshot snapshot;
    private final Long2ObjectOpenHashMap<BlockState> known = new Long2ObjectOpenHashMap<>();
    private final SnapshotWorld world = new SnapshotWorld();
    private final List<StructureScene.Marker> markers = new ArrayList<>();
    private int anchor = -1;
    private int placedBuilderWidth = -1;
    private boolean placing;

    private final class SnapshotWorld implements BlockGetter {

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            var state = known.get(pos.asLong());
            if (state != null) return state;
            var level = Minecraft.getInstance().level;
            return level != null && level.isLoaded(pos) ? level.getBlockState(pos) : Blocks.AIR.defaultBlockState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public int getHeight() {
            var level = Minecraft.getInstance().level;
            return level == null ? DEFAULT_HEIGHT : level.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            var level = Minecraft.getInstance().level;
            return level == null ? DEFAULT_MIN_Y : level.getMinBuildHeight();
        }
    }

    private OverviewView(OverviewWidget owner) {
        this.owner = owner;
        layout(l -> l.size(owner.getSizeWidth(), owner.getSizeHeight()));
        this.adapter = owner.getAdapter();
        setClientSideWidget();
        var icon = owner.getHost().getDefinition().asStack();
        scene = new StructureScene("overview", 100, 100, true);
        scene.setZoomButtons(true);
        scene.setOnMarker(this::onMarker);
        var back = Button.icon(UITheme.ARROW_LEFT).setOnClientClick(owner::requestBack);
        back.tooltips(OverviewWidget.LANG_BACK);
        var tools = ZoomBar.of(scene, false).zoom(true).fit(ZoomBar.RESET).build();
        var close = Button.glyph("×").setOnClientClick(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) player.closeContainer();
        });
        close.tooltips(MachineWindow.POPUP_CLOSE);
        var titleRow = UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(back, ItemView.of(icon),
                TextLine.constant(LayoutStyle.AUTO, Component.translatable(adapter.titleKey())).layout(l -> l.flex(1)),
                tools, close);
        frame = new UIElement().layout(l -> l.column().paddingAll(UISizes.POPUP_PADDING).gapAll(UISizes.GAP));
        frame.setBackground(UITheme.WINDOW);
        frame.addChild(titleRow);
        truncatedNotice = noticeRow(OverviewWidget.LANG_TRUNCATED);
        coarseNotice = noticeRow(OverviewWidget.LANG_COARSE);
        legend = UIElement.section();
        legend.addChildren(legendRow(new Swatch(OPAQUE | CONNECTED_COLOR, false), OverviewWidget.LANG_LEGEND_OK),
                legendRow(new Swatch(OPAQUE | DETACHED_COLOR, false), OverviewWidget.LANG_LEGEND_DETACHED),
                legendRow(new Swatch(OPAQUE | GHOST_COLOR, false), OverviewWidget.LANG_LEGEND_GHOST),
                legendRow(new Swatch(OPAQUE | adapter.legendColor(), true), OverviewWidget.LANG_LEGEND_ANCHOR),
                truncatedNotice, coarseNotice);
        selector = new OverviewSelector(this, adapter);
        flow = new OverviewBuildFlow(this, owner, adapter);
        addChildren(frame, scene, legend);
        place();
    }

    public static void attach(OverviewWidget owner) {
        var view = new OverviewView(owner);
        owner.addWidget(view);
        owner.setSink(view::apply);
    }

    private static UIElement legendRow(Widget swatch, String key) {
        return UIElement.centeredRow(UISizes.CONTROL_HEIGHT)
                .addChildren(swatch, TextLine.translatable(LEGEND_TEXT, key).setColor(UITheme.PANEL_TEXT));
    }

    private static UIElement noticeRow(String key) {
        var row = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.alignCenter())
                .addChildren(TextLine.translatable(SWATCH_SIZE + UISizes.GAP + LEGEND_TEXT, key).setColor(UITheme.STATUS_TEXT_WARNING));
        row.setDisplay(false);
        return row;
    }

    private static final class Swatch extends UIElement {

        private final int color;
        private final boolean diamond;

        Swatch(int color, boolean diamond) {
            this.color = color;
            this.diamond = diamond;
            layout(l -> l.size(SWATCH_SIZE, SWATCH_SIZE));
            setSize(new Size(SWATCH_SIZE, SWATCH_SIZE));
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX(), y = getPositionY();
            if (diamond) {
                for (int dy = -SWATCH_HALF; dy <= SWATCH_HALF; dy++) {
                    int half = SWATCH_HALF - Math.abs(dy);
                    graphics.fill(x + SWATCH_HALF - half, y + SWATCH_HALF + dy, x + SWATCH_HALF + 1 + half, y + SWATCH_HALF + 1 + dy,
                            Math.abs(dy) == SWATCH_HALF ? SWATCH_BORDER : color);
                }
                graphics.fill(x + SWATCH_HALF, y + SWATCH_HALF, x + SWATCH_HALF + 1, y + SWATCH_HALF + 1, SWATCH_CENTER);
                return;
            }
            graphics.fill(x, y, x + SWATCH_SIZE, y + SWATCH_SIZE, SWATCH_BORDER);
            graphics.fill(x + 1, y + 1, x + SWATCH_SIZE - 1, y + SWATCH_SIZE - 1, color);
        }
    }

    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        layout(l -> l.size(screenWidth, screenHeight));
        place();
        super.onScreenSizeUpdate(screenWidth, screenHeight);
    }

    int frameWidth() {
        return getSizeWidth() - 2 * MARGIN;
    }

    int frameHeight() {
        return getSizeHeight() - 2 * MARGIN;
    }

    int panelMaxHeight() {
        return Math.max(UISizes.SLOT_SIZE, frameHeight() - UISizes.CONTROL_HEIGHT - 3 * UISizes.GAP - 2 * UISizes.POPUP_PADDING);
    }

    BlockGetter world() {
        return world;
    }

    private void place() {
        if (frame == null || placing) return;
        placing = true;
        try {
            placeChildren();
        } finally {
            placing = false;
        }
    }

    private void placeChildren() {
        int width = frameWidth(), height = frameHeight();
        frame.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(MARGIN).top(MARGIN).size(width, height));
        int top = MARGIN + UISizes.POPUP_PADDING + UISizes.CONTROL_HEIGHT + UISizes.GAP;
        int left = MARGIN + UISizes.POPUP_PADDING;
        int right = MARGIN + width - UISizes.POPUP_PADDING;
        int bottom = MARGIN + height - UISizes.POPUP_PADDING;
        int rightInset = getSizeWidth() - right + UISizes.GAP;
        int bottomInset = getSizeHeight() - bottom + UISizes.GAP;
        scene.setPreferredSize(right - left, bottom - top);
        scene.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(left).top(top));
        legend.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(left + UISizes.GAP).bottom(bottomInset));
        int cardTop = top + UISizes.GAP;
        int maxHeight = bottom - cardTop - UISizes.GAP;
        if (card != null) {
            card.setMaxHeight(maxHeight);
            card.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).right(rightInset).top(cardTop));
        }
        if (builder != null) {
            placedBuilderWidth = builder.getSizeWidth();
            builder.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).right(rightInset).top(cardTop));
            if (builderPopup != null) {
                int popupInset = rightInset + placedBuilderWidth + UISizes.POPUP_GAP;
                builderPopup.setMaxHeight(maxHeight);
                builderPopup.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).right(popupInset).top(cardTop));
            }
        }
    }

    @Override
    public void onContentResized(Widget root) {}

    @Override
    protected void onLayoutFinished() {
        if (placing) return;
        if (card != null) flow.fitConfig(card);
        if (builder != null && builderPopup != null && builder.getSizeWidth() != placedBuilderWidth) place();
    }

    private void apply(OverviewSnapshot next) {
        snapshot = next;
        var connected = new Long2ObjectOpenHashMap<BlockState>();
        var detached = new Long2ObjectOpenHashMap<BlockState>();
        boolean coarse = next.coarse();
        for (var module : next.modules()) {
            var into = module.state() == OverviewSnapshot.CONNECTED ? connected : detached;
            var moduleLayout = layoutOf(module);
            if (coarse) collectOutline(module, moduleLayout, into);
            else module.collect(moduleLayout, into);
        }
        var keys = connected.keySet().iterator();
        while (keys.hasNext()) detached.remove(keys.nextLong());
        known.clear();
        known.putAll(detached);
        known.putAll(connected);
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE,
                maxZ = Integer.MIN_VALUE;
        var all = connected.isEmpty() ? detached : connected;
        var it = all.keySet().iterator();
        while (it.hasNext()) {
            long pos = it.nextLong();
            int x = BlockPos.getX(pos), y = BlockPos.getY(pos), z = BlockPos.getZ(pos);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        float dx = maxX - minX + 1, dy = maxY - minY + 1, dz = maxZ - minZ + 1;
        float zoom = all.isEmpty() ? -1 : Math.max(MIN_ZOOM, (float) Math.sqrt(dx * dx + dy * dy + dz * dz) * FIT);
        scene.show(connected, StructureScene.ALL_LAYERS, zoom);
        scene.setOverlay(OVERLAY_DETACHED, detached, red(DETACHED_COLOR), green(DETACHED_COLOR), blue(DETACHED_COLOR), 1);
        truncatedNotice.setDisplay(next.truncated());
        coarseNotice.setDisplay(coarse);
        legend.markLayoutDirty();
        if (anchor >= next.anchors().size()) clearSelection();
        selector.invalidate();
        refreshMarkers();
        flow.onSnapshot();
    }

    @Nullable
    private Layout layoutOf(OverviewSnapshot.Module module) {
        var key = module.id() + Arrays.toString(module.values());
        if (layouts.containsKey(key)) return layouts.get(key);
        var layout = module.layout();
        layouts.put(key, layout);
        return layout;
    }

    private static void collectOutline(OverviewSnapshot.Module module, @Nullable Layout layout, Long2ObjectOpenHashMap<BlockState> into) {
        var definition = module.definition();
        if (definition == null || layout == null) return;
        var items = StructurePlans.preview(definition, layout).items();
        into.putAll(StructureBlocks.worldBlocks(layout, items, module.pos(), module.front(), module.up(), module.flip()));
        into.put(module.pos().asLong(), OverviewDocking.controllerState(definition, module.front(), module.up()));
    }

    private static float red(int color) {
        return (color >> 16 & 0xFF) / 255f;
    }

    private static float green(int color) {
        return (color >> 8 & 0xFF) / 255f;
    }

    private static float blue(int color) {
        return (color & 0xFF) / 255f;
    }

    @Nullable
    OverviewSnapshot.Anchor selected() {
        if (snapshot == null || anchor < 0 || anchor >= snapshot.anchors().size()) return null;
        return snapshot.anchors().get(anchor);
    }

    int anchorIndex() {
        return anchor;
    }

    private void refreshMarkers() {
        if (snapshot == null) return;
        markers.clear();
        var anchors = snapshot.anchors();
        var kinds = adapter.kinds();
        for (int i = 0; i < anchors.size(); i++) {
            var data = anchors.get(i);
            var kind = kinds.get(Math.max(0, Math.min(kinds.size() - 1, data.kind())));
            var pos = new Vector3f(data.markerX(), data.markerY(), data.markerZ());
            markers.add(new StructureScene.Marker(pos, kind.color(), i == this.anchor, Collections.singletonList(Component.translatable(kind.tooltipKey()))));
        }
        scene.setMarkers(new ArrayList<>(markers));
    }

    private void onMarker(StructureScene.Marker marker) {
        if (snapshot == null) return;
        int index = -1;
        var anchors = snapshot.anchors();
        for (int i = 0; i < anchors.size(); i++) {
            var data = anchors.get(i);
            if (Math.abs(data.markerX() - marker.pos().x()) < MARKER_MATCH && Math.abs(data.markerY() - marker.pos().y()) < MARKER_MATCH &&
                    Math.abs(data.markerZ() - marker.pos().z()) < MARKER_MATCH)
                index = i;
        }
        if (index < 0) return;
        clearSelection();
        anchor = index;
        refreshMarkers();
        selector.select();
    }

    void setCard(@Nullable Popup popup, Runnable onClose) {
        if (card != null) removeWidget(card);
        card = popup == null ? null : new PopupCard("overview.card", popup, panelMaxHeight(), onClose);
        place();
        if (card != null) addChild(card);
    }

    void showGhost(Long2ObjectOpenHashMap<BlockState> blocks) {
        scene.setOverlay(OVERLAY_GHOST, blocks, red(GHOST_COLOR), green(GHOST_COLOR), blue(GHOST_COLOR), GHOST_ALPHA);
    }

    void clearGhost() {
        scene.setOverlay(OVERLAY_GHOST, null, 1, 1, 1, 1);
    }

    void openSelector() {
        selector.open();
    }

    void chooseDefinition(MultiblockMachineDefinition definition) {
        flow.choose(definition);
    }

    void showBuilder(PatternBuilderPanel panel) {
        panel.setPopupSlot(new PatternBuilderPanel.PopupSlot() {

            @Override
            public void toggle(String key, Supplier<Popup> factory) {
                if (key.equals(builderPopupKey)) closeBuilderPopup();
                else openBuilderPopup(key, factory);
            }

            @Override
            public boolean isOpen(String key) {
                return key.equals(builderPopupKey);
            }

            @Override
            public void rebuild() {}
        });
        if (card != null) {
            removeWidget(card);
            card = null;
        }
        builder = panel;
        place();
        addChild(panel);
    }

    private void openBuilderPopup(String key, Supplier<Popup> factory) {
        closeBuilderPopup();
        builderPopupKey = key;
        builderPopup = new PopupCard("overview.builder_popup", factory.get(), panelMaxHeight(), this::closeBuilderPopup);
        place();
        addChild(builderPopup);
    }

    private void closeBuilderPopup() {
        if (builderPopup != null) removeWidget(builderPopup);
        builderPopup = null;
        builderPopupKey = null;
        place();
    }

    void closeBuilder() {
        closeBuilderPopup();
        if (builder != null) removeWidget(builder);
        builder = null;
        flow.reopenConfig();
        place();
    }

    void clearSelection() {
        closeBuilderPopup();
        if (builder != null) removeWidget(builder);
        builder = null;
        flow.clear();
        anchor = -1;
        clearGhost();
        setCard(null, () -> {});
        refreshMarkers();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (builderPopup != null) {
                closeBuilderPopup();
                return true;
            }
            if (builder != null) {
                closeBuilder();
                return true;
            }
            if (flow.active()) {
                flow.backToSelector();
                return true;
            }
            if (anchor >= 0) {
                clearSelection();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        flow.tick();
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }
}

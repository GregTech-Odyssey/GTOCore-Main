package com.gtocore.common.machine.monitor;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.UIInput;
import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.canvas.ItemLayer;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.UICodecs;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.VirtualList;
import com.gregtechceu.gtceu.uipro.render.UILayers;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.datasynclib.util.ByteBufCodecExtends;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** The screen editor and the card coordinates used by its live preview and the block renderer. */
public final class MonitorEditor {

    private static final int CARD_GAP = 8;
    private static final int CARD_WIDTH = 150;
    private static final int CARD_HEIGHT = 42;
    private static final int CARD_BORDER_THICKNESS = 3;
    private static final int CARD_CORNER_RADIUS = 10;
    private static final int[] CARD_OUTER_CORNER = cornerInsets(CARD_CORNER_RADIUS);
    private static final int[] CARD_INNER_CORNER = cornerInsets(CARD_CORNER_RADIUS - CARD_BORDER_THICKNESS);
    private static final int PALETTE_HEIGHT = 5 * UISizes.SLOT_SIZE;
    private static final int SNAP_DISTANCE = 8;
    private static final Component NO_INFORMATION = Component.translatable("gtocore.machine.monitor.no_information");
    private static final StreamCodec<FriendlyByteBuf, MonitorLayout.Entry> ENTRY_CODEC = StreamCodec.of(
            (buf, value) -> {
                buf.writeLong(value.source());
                ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.encode(buf, value.component());
                buf.writeVarInt(value.x());
                buf.writeVarInt(value.y());
                buf.writeVarInt(value.width());
                buf.writeVarInt(value.height());
            },
            buf -> new MonitorLayout.Entry(buf.readLong(), ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.decode(buf),
                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));

    private MonitorEditor() {}

    public static Widget create(BasicMonitor monitor) {
        Manager.GridNetwork network = Manager.networkAt(monitor);
        if (network == null || monitor.getLevel() == null) {
            return Form.page().addChild(Form.label(NO_INFORMATION));
        }
        EditorCanvas canvas = new EditorCanvas(monitor, network);
        int sidebarWidth = 160;
        var sidebar = new ScrollerView("monitor.components", sidebarWidth, 280).setResizable(false);
        for (AbstractInfoProviderMonitor provider : network.providers(monitor.getLevel())) {
            sidebar.addScrollViewChild(Form.section(Component.translatable(provider.getDefinition().getDescriptionId())));
            long source = provider.getPos().asLong();
            var components = provider.getAvailableRLs().toArray(ResourceLocation[]::new);
            sidebar.addScrollViewChild(new VirtualList(1, PALETTE_HEIGHT, UISizes.SECTION_GAP,
                    () -> components.length, index -> new PaletteCard(sidebarWidth - 12, canvas, source, components[index])));
        }
        sidebar.addScrollViewChild(Form.label(Component.translatable("gtocore.machine.monitor.editor.help")));
        return new UIElement() {

            @Override
            @OnlyIn(Dist.CLIENT)
            protected void drawWidgetsForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks);
                // The scroller has restored its clip here, so the preview can leave the sidebar.
                if (UIInput.captured() instanceof PaletteCard card && card.canvas == canvas && card.moved) {
                    card.drawDragPreview(graphics, mouseX, mouseY);
                }
            }
        }.layout(l -> l.row().height(280).gapAll(UISizes.GAP))
                .addChildren(canvas, sidebar);
    }

    public static List<MonitorLayout.Entry> effectiveLayout(Manager.GridNetwork network, net.minecraft.world.level.Level level) {
        int screenWidth = network.width3D() * MonitorLayout.PIXELS_PER_BLOCK;
        int screenHeight = network.height3D() * MonitorLayout.PIXELS_PER_BLOCK;
        var saved = network.screenLayout(level);
        if (saved.contains("items")) {
            var loaded = MonitorLayout.load(saved);
            var providers = network.providers(level);
            loaded.removeIf(entry -> {
                if (entry.x() < 0 || entry.y() < 0 || entry.width() < 40 || entry.height() < 24 ||
                        entry.width() > screenWidth || entry.height() > screenHeight ||
                        entry.x() + entry.width() > screenWidth || entry.y() + entry.height() > screenHeight)
                    return true;
                for (AbstractInfoProviderMonitor provider : providers) {
                    if (provider.getPos().asLong() == entry.source() && provider.getAvailableRLs().contains(entry.component())) return false;
                }
                return true;
            });
            return loaded;
        }
        ArrayList<MonitorLayout.Entry> result = new ArrayList<>();
        int x = CARD_GAP, y = CARD_GAP, rowHeight = 0;
        for (Manager.GridNetwork.DisplayEntry display : network.getDisplayEntries()) {
            if (result.size() >= MonitorLayout.MAX_ENTRIES) break;
            IDisplayComponent component = display.component();
            int width = Math.min(screenWidth - CARD_GAP * 2, Math.max(CARD_WIDTH, component.getVisualWidth() + 16));
            int height = Math.min(screenHeight - CARD_GAP * 2, Math.max(CARD_HEIGHT, component.getVisualHeight() + 24));
            if (x + width > screenWidth - CARD_GAP && x > CARD_GAP) {
                x = CARD_GAP;
                y += rowHeight + CARD_GAP;
                rowHeight = 0;
            }
            if (y + height > screenHeight - CARD_GAP) break;
            result.add(new MonitorLayout.Entry(display.source(), component.getId(), x, y, width, height));
            x += width + CARD_GAP;
            rowHeight = Math.max(rowHeight, height);
        }
        return result;
    }

    @Nullable
    public static IDisplayComponent component(Manager.GridNetwork network, MonitorLayout.Entry entry) {
        for (Manager.GridNetwork.DisplayEntry display : network.getDisplayEntries()) {
            if (display.source() == entry.source() && display.component().getId().equals(entry.component())) return display.component();
        }
        return null;
    }

    public static void drawCard(GuiGraphics graphics, MonitorLayout.Entry entry, @Nullable IDisplayComponent component) {
        drawCard(graphics, entry.x(), entry.y(), entry.width(), entry.height(), component, false);
    }

    private static void drawCard(GuiGraphics graphics, int x, int y, int width, int height, @Nullable IDisplayComponent component, boolean hovered) {
        drawCardBackground(graphics, x, y, width, height, hovered);
        graphics.flush();
        if (component != null) drawCardContent(graphics, x, y, width, height, component);
    }

    private static int[] cornerInsets(int radius) {
        int[] insets = new int[radius];
        for (int row = 0; row < radius; row++) {
            double dy = radius - row - 0.5;
            insets[row] = (int) Math.round(radius - Math.sqrt(radius * radius - dy * dy));
        }
        return insets;
    }

    private static void fillRoundedRect(GuiGraphics graphics, int x, int y, int width, int height, int[] corner, int color) {
        int radius = corner.length;
        graphics.fill(x, y + radius, x + width, y + height - radius, color);
        for (int row = 0; row < radius; row++) {
            int inset = corner[row];
            graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
            graphics.fill(x + inset, y + height - row - 1, x + width - inset, y + height - row, color);
        }
    }

    private static void drawCardBackground(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered) {
        fillRoundedRect(graphics, x, y, width, height, CARD_OUTER_CORNER, hovered ? 0xFF7198CA : 0xFF394554);
        fillRoundedRect(graphics, x + CARD_BORDER_THICKNESS, y + CARD_BORDER_THICKNESS,
                width - CARD_BORDER_THICKNESS * 2, height - CARD_BORDER_THICKNESS * 2,
                CARD_INNER_CORNER, hovered ? 0xFF293543 : 0xFF1D232B);
    }

    private static void drawCardContent(GuiGraphics graphics, MonitorLayout.Entry entry, IDisplayComponent component) {
        drawCardContent(graphics, entry.x(), entry.y(), entry.width(), entry.height(), component);
    }

    private static void drawCardContent(GuiGraphics graphics, int x, int y, int cardWidth, int cardHeight, IDisplayComponent component) {
        int width = cardWidth - 16;
        int height = cardHeight - 16;
        float scale = Math.min((float) width / Math.max(1, component.getVisualWidth()),
                (float) height / Math.max(1, component.getVisualHeight()));
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8, y + 8, 0);
        graphics.pose().scale(scale, scale, 1);
        component.renderGui(graphics, 0, 0,
                Math.max(1, Math.round(width / scale)),
                Math.max(1, Math.round(height / scale)));
        graphics.pose().popPose();
    }

    private static final class PaletteCard extends UIElement {

        private final EditorCanvas canvas;
        private final MonitorLayout.Entry identity;
        @Nullable
        private MonitorLayout.Entry dragged;
        private boolean moved;
        private double pressX, pressY;
        private float grabX, grabY;

        private PaletteCard(int width, EditorCanvas canvas, long source, ResourceLocation component) {
            this.canvas = canvas;
            identity = new MonitorLayout.Entry(source, component, 0, 0, CARD_WIDTH, CARD_HEIGHT);
            layout(l -> l.column().size(width, PALETTE_HEIGHT));
            addChild(Form.label(Component.translatable(DisplayRegistry.langKey(component)))
                    .layout(l -> l.widthAuto().flexGrow(0).flexBasis(UISizes.TEXT_HEIGHT).height(UISizes.TEXT_HEIGHT)));
        }

        private float previewScale(int width, int height) {
            return Math.min((float) getSizeWidth() / width,
                    (float) (getSizeHeight() - UISizes.TEXT_HEIGHT - UISizes.GAP) / height);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void drawWidgetsBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
            var component = component(canvas.network, identity);
            int width = canvas.cardWidth(component), height = canvas.cardHeight(component);
            float scale = previewScale(width, height);
            float x = getPositionX() + (getSizeWidth() - width * scale) / 2;
            float y = getPositionY() + UISizes.TEXT_HEIGHT + UISizes.GAP;
            drawPreview(graphics, x, y, scale, width, height, component, isMouseOverElement(mouseX, mouseY));
        }

        @OnlyIn(Dist.CLIENT)
        private void drawPreview(GuiGraphics graphics, float x, float y, float scale, int width, int height,
                                 @Nullable IDisplayComponent component, boolean hovered) {
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(scale, scale, 1);
            drawCard(graphics, 0, 0, width, height, component, hovered);
            if (component == null) {
                var font = Minecraft.getInstance().font;
                graphics.drawString(font, NO_INFORMATION, (width - font.width(NO_INFORMATION)) / 2,
                        (height - UISizes.TEXT_HEIGHT) / 2, 0xFFFFFFFF, false);
            }
            graphics.pose().popPose();
        }

        @OnlyIn(Dist.CLIENT)
        private void drawDragPreview(GuiGraphics graphics, double mouseX, double mouseY) {
            if (dragged == null) return;
            float scale = previewScale(dragged.width(), dragged.height());
            float x = (float) mouseX - grabX * dragged.width() * scale;
            float y = (float) mouseY - grabY * dragged.height() * scale;
            if (canvas.isInViewport(mouseX, mouseY)) {
                scale = canvas.scale();
                x = canvas.viewportX() + (canvas.dropX(dragged, mouseX, grabX) - canvas.offsetX()) * scale;
                y = canvas.viewportY() + (canvas.dropY(dragged, mouseY, grabY) - canvas.offsetY()) * scale;
            }
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, UILayers.TOP);
            drawPreview(graphics, x, y, scale, dragged.width(), dragged.height(), component(canvas.network, dragged), true);
            graphics.pose().popPose();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !isMouseOverElement(mouseX, mouseY)) return false;
            moved = false;
            pressX = mouseX;
            pressY = mouseY;
            var component = component(canvas.network, identity);
            int width = canvas.cardWidth(component), height = canvas.cardHeight(component);
            dragged = new MonitorLayout.Entry(identity.source(), identity.component(), 0, 0, width, height);
            float scale = previewScale(width, height);
            float x = getPositionX() + (getSizeWidth() - width * scale) / 2;
            float y = getPositionY() + UISizes.TEXT_HEIGHT + UISizes.GAP;
            grabX = Mth.clamp((float) (mouseX - x) / (width * scale), 0, 1);
            grabY = Mth.clamp((float) (mouseY - y) / (height * scale), 0, 1);
            canvas.armedComponent = null;
            UIInput.capture(this, 0);
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (button != 0 || UIInput.captured() != this) return false;
            if (Math.abs(mouseX - pressX) >= UISizes.GAP * 2 || Math.abs(mouseY - pressY) >= UISizes.GAP * 2) moved = true;
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (button != 0 || UIInput.captured() != this) return false;
            UIInput.release(this);
            if (moved) {
                if (dragged != null && canvas.isInViewport(mouseX, mouseY)) canvas.addFromPalette(dragged, mouseX, mouseY, grabX, grabY);
            } else {
                canvas.arm(identity.source(), identity.component());
            }
            moved = false;
            dragged = null;
            return true;
        }
    }

    private static final class EditorCanvas extends CanvasView {

        private final BasicMonitor monitor;
        private final Manager.GridNetwork network;
        private final RPC<List<MonitorLayout.Entry>> save;
        private final ArrayList<MonitorLayout.Entry> entries = new ArrayList<>();
        private long armedSource = Long.MIN_VALUE;
        @Nullable
        private ResourceLocation armedComponent;
        private int dragging = -1;
        private boolean resizing;
        private float dragStartX, dragStartY;
        private MonitorLayout.Entry dragStart;
        private boolean loaded;

        private EditorCanvas(BasicMonitor monitor, Manager.GridNetwork network) {
            super("monitor.screen", 400, 280);
            this.monitor = monitor;
            this.network = network;
            save = addRPC(UICodecs.list(ENTRY_CODEC, MonitorLayout.MAX_ENTRIES), (player, submitted) -> {
                if (player.distanceToSqr(monitor.getPos().getX() + .5, monitor.getPos().getY() + .5, monitor.getPos().getZ() + .5) > 64) return;
                var current = Manager.networkAt(monitor);
                if (current == null || current != network || monitor.getLevel() == null) return;
                int screenWidth = current.width3D() * MonitorLayout.PIXELS_PER_BLOCK;
                int screenHeight = current.height3D() * MonitorLayout.PIXELS_PER_BLOCK;
                ArrayList<MonitorLayout.Entry> checked = new ArrayList<>(submitted.size());
                for (MonitorLayout.Entry entry : submitted) {
                    if (entry.width() < 40 || entry.height() < 24 || entry.width() > screenWidth || entry.height() > screenHeight ||
                            entry.x() < 0 || entry.y() < 0 || entry.x() + entry.width() > screenWidth || entry.y() + entry.height() > screenHeight)
                        return;
                    boolean valid = false;
                    for (AbstractInfoProviderMonitor provider : current.providers(monitor.getLevel())) {
                        if (provider.getPos().asLong() == entry.source() && provider.getAvailableRLs().contains(entry.component())) {
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) return;
                    checked.add(entry);
                }
                current.applyScreenLayout(monitor.getLevel(), MonitorLayout.save(checked));
            }).limit(2);
            setScene(this::buildScene);
            setOnClientBackgroundClick((x, y, button) -> {
                if (button == 0 && armedComponent != null) addCard(Math.round(x), Math.round(y));
            });
            setInitialView(view -> view.fitView());
        }

        private void arm(long source, ResourceLocation component) {
            armedSource = source;
            armedComponent = component;
        }

        @OnlyIn(Dist.CLIENT)
        private void addFromPalette(MonitorLayout.Entry entry, double mouseX, double mouseY, float grabX, float grabY) {
            addCard(entry.source(), entry.component(), dropX(entry, mouseX, grabX), dropY(entry, mouseY, grabY), entry.width(), entry.height());
        }

        private int cardWidth(@Nullable IDisplayComponent component) {
            return Math.min(network.width3D() * MonitorLayout.PIXELS_PER_BLOCK,
                    component == null ? CARD_WIDTH : Math.max(CARD_WIDTH, component.getVisualWidth() + 16));
        }

        private int cardHeight(@Nullable IDisplayComponent component) {
            return Math.min(network.height3D() * MonitorLayout.PIXELS_PER_BLOCK,
                    component == null ? CARD_HEIGHT : Math.max(CARD_HEIGHT, component.getVisualHeight() + 24));
        }

        private int dropX(MonitorLayout.Entry entry, double mouseX, float grabX) {
            return Mth.clamp(Math.round(toWorldX(mouseX) - grabX * entry.width()),
                    0, network.width3D() * MonitorLayout.PIXELS_PER_BLOCK - entry.width());
        }

        private int dropY(MonitorLayout.Entry entry, double mouseY, float grabY) {
            return Mth.clamp(Math.round(toWorldY(mouseY) - grabY * entry.height()),
                    0, network.height3D() * MonitorLayout.PIXELS_PER_BLOCK - entry.height());
        }

        @OnlyIn(Dist.CLIENT)
        private void buildScene(CanvasView canvas) {
            if (!loaded) {
                entries.addAll(effectiveLayout(network, monitor.getLevel()));
                loaded = !entries.isEmpty() || network.screenLayout(monitor.getLevel()).contains("items");
            }
            int width = network.width3D() * MonitorLayout.PIXELS_PER_BLOCK;
            int height = network.height3D() * MonitorLayout.PIXELS_PER_BLOCK;
            canvas.addLayer(new CanvasLayer() {

                @Override
                public CanvasRect bounds() {
                    return CanvasRect.of(0, 0, width, height);
                }

                @Override
                public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
                    painter.fill(0, 0, width, height, 0xFF10151C);
                    painter.outline(CanvasRect.of(0, 0, width, height), 2, 0xFF53647A);
                    if (entries.isEmpty()) {
                        painter.flush();
                        var font = Minecraft.getInstance().font;
                        painter.graphics().drawString(font, NO_INFORMATION, (width - font.width(NO_INFORMATION)) / 2,
                                height / 2, 0xFFFFFFFF, false);
                    }
                }
            });
            ItemLayer<Card> cards = new ItemLayer<>();
            for (int i = 0; i < entries.size(); i++) cards.add(new Card(i));
            canvas.addLayer(cards);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void updateScreen() {
            super.updateScreen();
            if (!loaded && !network.getDisplayEntries().isEmpty()) rebuildScene();
        }

        @OnlyIn(Dist.CLIENT)
        private void refresh() {
            rebuildScene();
        }

        @OnlyIn(Dist.CLIENT)
        private void submit() {
            save.send(List.copyOf(entries));
        }

        @OnlyIn(Dist.CLIENT)
        private void addCard(int x, int y) {
            if (armedComponent == null) return;
            IDisplayComponent component = null;
            for (Manager.GridNetwork.DisplayEntry display : network.getDisplayEntries()) {
                if (display.source() == armedSource && display.component().getId().equals(armedComponent)) {
                    component = display.component();
                    break;
                }
            }
            addCard(armedSource, armedComponent, x, y, cardWidth(component), cardHeight(component));
        }

        @OnlyIn(Dist.CLIENT)
        private void addCard(long source, ResourceLocation component, int x, int y, int width, int height) {
            if (entries.size() >= MonitorLayout.MAX_ENTRIES) return;
            for (MonitorLayout.Entry entry : entries) {
                if (entry.source() == source && entry.component().equals(component)) return;
            }
            int maxX = network.width3D() * MonitorLayout.PIXELS_PER_BLOCK - width;
            int maxY = network.height3D() * MonitorLayout.PIXELS_PER_BLOCK - height;
            entries.add(new MonitorLayout.Entry(source, component, Mth.clamp(x, 0, maxX), Mth.clamp(y, 0, maxY), width, height));
            armedComponent = null;
            submit();
            refresh();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected boolean onViewPress(double mouseX, double mouseY, int button) {
            if (button == 0 && itemAt(mouseX, mouseY) instanceof Card card) {
                dragging = card.index;
                dragStart = entries.get(dragging);
                dragStartX = toWorldX(mouseX);
                dragStartY = toWorldY(mouseY);
                resizing = dragStart.x() + dragStart.width() - dragStartX < 12 && dragStart.y() + dragStart.height() - dragStartY < 12;
                return super.onViewPress(mouseX, mouseY, button);
            }
            return super.onViewPress(mouseX, mouseY, button);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void onViewDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (dragging < 0) {
                super.onViewDrag(mouseX, mouseY, button, dragX, dragY);
                return;
            }
            int dx = Math.round(toWorldX(mouseX) - dragStartX), dy = Math.round(toWorldY(mouseY) - dragStartY);
            int screenWidth = network.width3D() * MonitorLayout.PIXELS_PER_BLOCK;
            int screenHeight = network.height3D() * MonitorLayout.PIXELS_PER_BLOCK;
            MonitorLayout.Entry updated;
            if (resizing) {
                updated = new MonitorLayout.Entry(dragStart.source(), dragStart.component(), dragStart.x(), dragStart.y(),
                        Mth.clamp(dragStart.width() + dx, 40, screenWidth - dragStart.x()),
                        Mth.clamp(dragStart.height() + dy, 24, screenHeight - dragStart.y()));
            } else {
                updated = new MonitorLayout.Entry(dragStart.source(), dragStart.component(),
                        Mth.clamp(dragStart.x() + dx, 0, screenWidth - dragStart.width()),
                        Mth.clamp(dragStart.y() + dy, 0, screenHeight - dragStart.height()), dragStart.width(), dragStart.height());
            }
            if (!Screen.hasControlDown()) updated = snap(updated, dragging, screenWidth, screenHeight);
            entries.set(dragging, updated);
            refresh();
        }

        private MonitorLayout.Entry snap(MonitorLayout.Entry entry, int index, int screenWidth, int screenHeight) {
            if (resizing) {
                int right = snapResizeEdge(entry.x() + entry.width(), entry.x(), true, index, screenWidth);
                int bottom = snapResizeEdge(entry.y() + entry.height(), entry.y(), false, index, screenHeight);
                return new MonitorLayout.Entry(entry.source(), entry.component(), entry.x(), entry.y(), right - entry.x(), bottom - entry.y());
            }
            int x = snapCoordinate(entry.x(), entry.width(), true, index, screenWidth);
            int y = snapCoordinate(entry.y(), entry.height(), false, index, screenHeight);
            return new MonitorLayout.Entry(entry.source(), entry.component(), x, y, entry.width(), entry.height());
        }

        private int snapCoordinate(int coordinate, int size, boolean horizontal, int movingIndex, int limit) {
            int best = coordinate;
            int bestDistance = SNAP_DISTANCE + 1;
            for (int i = 0; i < entries.size(); i++) {
                if (i == movingIndex) continue;
                MonitorLayout.Entry other = entries.get(i);
                int start = horizontal ? other.x() : other.y();
                int otherSize = horizontal ? other.width() : other.height();
                for (int movingAnchor = 0; movingAnchor < 3; movingAnchor++) {
                    int offset = movingAnchor == 0 ? 0 : movingAnchor == 1 ? size : size / 2;
                    for (int targetAnchor = 0; targetAnchor < 3; targetAnchor++) {
                        int target = targetAnchor == 0 ? start : targetAnchor == 1 ? start + otherSize : start + otherSize / 2;
                        int candidate = target - offset;
                        int distance = Math.abs(candidate - coordinate);
                        if (distance <= SNAP_DISTANCE && distance < bestDistance && candidate >= 0 && candidate + size <= limit) {
                            best = candidate;
                            bestDistance = distance;
                        }
                    }
                }
            }
            return best;
        }

        private int snapResizeEdge(int edge, int fixedStart, boolean horizontal, int movingIndex, int limit) {
            int best = edge;
            int bestDistance = SNAP_DISTANCE + 1;
            int minimum = horizontal ? 40 : 24;
            for (int i = 0; i < entries.size(); i++) {
                if (i == movingIndex) continue;
                MonitorLayout.Entry other = entries.get(i);
                int start = horizontal ? other.x() : other.y();
                int size = horizontal ? other.width() : other.height();
                for (int movingAnchor = 0; movingAnchor < 2; movingAnchor++) {
                    for (int targetAnchor = 0; targetAnchor < 3; targetAnchor++) {
                        int target = targetAnchor == 0 ? start : targetAnchor == 1 ? start + size : start + size / 2;
                        int candidate = movingAnchor == 0 ? target : fixedStart + 2 * (target - fixedStart);
                        int distance = Math.abs(candidate - edge);
                        int candidateSize = candidate - fixedStart;
                        if (distance <= SNAP_DISTANCE && distance < bestDistance && candidateSize >= minimum && candidateSize <= limit - fixedStart) {
                            best = candidate;
                            bestDistance = distance;
                        }
                    }
                }
            }
            return best;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void onViewRelease(double mouseX, double mouseY, int button, boolean click) {
            if (dragging >= 0) {
                dragging = -1;
                submit();
                return;
            }
            if (button == 1 && itemAt(mouseX, mouseY) instanceof Card card) {
                entries.remove(card.index);
                submit();
                refresh();
                return;
            }
            super.onViewRelease(mouseX, mouseY, button, click);
        }

        private final class Card implements CanvasItem {

            private final int index;

            private Card(int index) {
                this.index = index;
            }

            @Override
            public CanvasRect bounds() {
                var entry = entries.get(index);
                return CanvasRect.of(entry.x(), entry.y(), entry.width(), entry.height());
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public void drawShape(CanvasPainter painter, boolean hovered) {
                var bounds = bounds();
                drawCardBackground(painter.graphics(), Math.round(bounds.x()), Math.round(bounds.y()),
                        Math.round(bounds.width()), Math.round(bounds.height()), hovered);
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public void drawContent(CanvasPainter painter, boolean hovered) {
                var entry = entries.get(index);
                IDisplayComponent component = component(network, entry);
                if (component != null) drawCardContent(painter.graphics(), entry, component);
            }

            @Override
            public List<Component> tooltip() {
                return List.of(Component.translatable(DisplayRegistry.langKey(entries.get(index).component())));
            }
        }
    }
}

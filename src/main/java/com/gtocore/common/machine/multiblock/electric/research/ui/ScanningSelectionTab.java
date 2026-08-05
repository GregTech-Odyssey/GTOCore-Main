package com.gtocore.common.machine.multiblock.electric.research.ui;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.integration.jech.PinYinUtils;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.client.gui.me.common.StackSizeRenderer;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.ColorUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import static com.gregtechceu.gtceu.common.data.GTMaterials.NULL;
import static com.gtolib.utils.AEChemicalHelper.getMaterial;
import static java.lang.Math.abs;
import static java.lang.Math.sin;

@DataGeneratorScanned
public class ScanningSelectionTab implements IFancyUIProvider {

    private static final int PAGE_WIDTH = 184;
    private static final int PAGE_HEIGHT = 152;

    @RegisterLanguage(cn = "扫描队列", en = "Scanning Queue")
    private static final String TAB_NAME = "gtocore.research.scanning_selection_tab.name";
    @RegisterLanguage(cn = "搜索可扫描物品或流体", en = "Search available scan targets")
    private static final String SEARCH_TOOLTIP = "gtocore.research.scanning_selection_tab.search";
    @RegisterLanguage(cn = "没有可用的扫描目标", en = "No scan targets are available.")
    private static final String EMPTY_ENTRIES = "gtocore.research.scanning_selection_tab.empty";
    @RegisterLanguage(cn = "没有符合搜索条件的扫描目标", en = "No scan targets match the search.")
    private static final String FILTER_EMPTY_ENTRIES = "gtocore.research.scanning_selection_tab.filter_empty";
    @RegisterLanguage(cn = "点击选择", en = "Click to select")
    private static final String SELECT_TOOLTIP = "gtocore.research.scanning_selection_tab.select";
    @RegisterLanguage(cn = "点击取消选择", en = "Click to deselect")
    private static final String DESELECT_TOOLTIP = "gtocore.research.scanning_selection_tab.deselect";
    @RegisterLanguage(cn = "已扫描", en = "Scanned")
    private static final String SCANNED_TOOLTIP = "gtocore.research.scanning_selection_tab.scanned";
    @RegisterLanguage(cn = "不可扫描", en = "Unscannable")
    private static final String UNSCANNABLE_TOOLTIP = "gtocore.research.scanning_selection_tab.unscannable";
    @RegisterLanguage(cn = "清空选择", en = "Clear selection")
    private static final String CLEAR_TOOLTIP = "gtocore.research.scanning_selection_tab.clear";
    @RegisterLanguage(cn = "重新加载可用扫描目标", en = "Reload available scan targets")
    private static final String RELOAD_TOOLTIP = "gtocore.research.scanning_selection_tab.reload";
    @RegisterLanguage(cn = "确认选择", en = "Confirm selection")
    private static final String EXPORT_TOOLTIP = "gtocore.research.scanning_selection_tab.export";
    @RegisterLanguage(cn = "切换机器工作模式", en = "Switch machine work mode")
    private static final String WORK_MODE_TOOLTIP = "gtocore.research.scanning_selection_tab.work_mode";
    @RegisterLanguage(cn = "当前模式：%s", en = "Current mode: %s")
    private static final String CURRENT_WORK_MODE_TOOLTIP = "gtocore.research.scanning_selection_tab.work_mode.current";
    @RegisterLanguage(cn = "持续检测并扫描未学习目标", en = "Continuously detect and scan unlearned targets")
    private static final String WORK_MODE_SCAN_UNLEARNED_ONLY = "gtocore.research.scanning_selection_tab.work_mode.scan_unlearned_only";
    @RegisterLanguage(cn = "检测并扫描未学习目标，若无目标时停止", en = "Detect and scan unlearned targets, stop when no targets are available")
    private static final String WORK_MODE_SCAN_UNLEARNED_ONCE = "gtocore.research.scanning_selection_tab.work_mode.scan_unlearned_once";
    @RegisterLanguage(cn = "循环扫描左侧选择的目标", en = "Continuously scan the targets selected on the left")
    private static final String WORK_MODE_SCAN_SELECTED_ONLY = "gtocore.research.scanning_selection_tab.work_mode.scan_selected_only";
    @RegisterLanguage(cn = "扫描一次左侧选择的目标后停止", en = "Scan the targets selected on the left once, then stop")
    private static final String WORK_MODE_SCAN_SELECTED_ONCE = "gtocore.research.scanning_selection_tab.work_mode.scan_selected_once";
    @RegisterLanguage(cn = "属于材料<%s>的扫描目标", en = "Scan targets belonging to material <%s>")
    private static final String WORK_MODE_SCAN_MATERIAL = "gtocore.research.scanning_selection_tab.work_mode.scan_material";

    private final ScanningInfoProvider holder;
    private final UUID ownerId;

    public ScanningSelectionTab(ScanningInfoProvider holder, UUID ownerId) {
        this.holder = holder;
        this.ownerId = ownerId;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        sideTabs.setMainTab(this);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget fancyMachineUIWidget) {
        return new ScanningSelectionWidget(holder, ownerId);
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTItems.TOOL_DATA_ORB.asItem());
    }

    @Override
    public Component getTitle() {
        return Component.translatable(TAB_NAME);
    }

    @Override
    public List<Component> getTabTooltips() {
        return Collections.singletonList(Component.translatable(TAB_NAME));
    }

    private static final class ScanningSelectionWidget extends WidgetGroup {

        private static final int UPDATE_STATE = 100;
        private static final int ACTION_TOGGLE = 111;
        private static final int ACTION_SELECT_VISIBLE = 66;
        private static final int SEARCH_FIELD_HEIGHT = 14;
        private static final int ENTRY_PANEL_X = 4;
        private static final int ENTRY_PANEL_Y = 22;
        private static final int ENTRY_PANEL_WIDTH = 150;
        private static final int ENTRY_PANEL_HEIGHT = 126;
        private static final int ENTRY_COLUMNS = 8;
        private static final int ENTRY_SIZE = 18;
        private static final int ENTRY_CONTENT_X = 2;
        private static final int ENTRY_CONTENT_Y = 4;
        private static final int BUTTON_X = 160;
        private static final int BUTTON_SIZE = 18;
        private static final int MAX_KEY_PACKET_COUNT = 1 << 20;

        private final ScanningInfoProvider holder;
        private final UUID ownerId;
        private final DraggableScrollableWidgetGroup entryPanel;
        private final WidgetGroup entryContent;
        private final Map<AEKey, AEKeyWidget> entryWidgetCache = new HashMap<>();
        private final Set<AEKey> mountedEntryKeys = new HashSet<>();
        private final EmptyEntryWidget emptyEntryWidget = new EmptyEntryWidget(
                2, 2, ENTRY_PANEL_WIDTH - 12, ENTRY_PANEL_HEIGHT - 12, Component.empty());
        private List<AEKey> filteredEntries = List.of();
        private boolean emptyEntryWidgetMounted;
        private String searchText = "";
        private SyncState currentState = SyncState.EMPTY;
        private SyncState lastSentState = SyncState.EMPTY;

        private ScanningSelectionWidget(ScanningInfoProvider holder, UUID ownerId) {
            super(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
            this.holder = holder;
            this.ownerId = ownerId;
            setBackground(GuiTextures.BACKGROUND_INVERSE);

            TextFieldWidget searchField = new TextFieldWidget(
                    ENTRY_PANEL_X, 4, ENTRY_PANEL_WIDTH, SEARCH_FIELD_HEIGHT,
                    () -> searchText, this::setSearchText);
            searchField.setBackground(GuiTextures.NUMBER_BACKGROUND);
            searchField.setHoverTooltips(Component.translatable(SEARCH_TOOLTIP));
            searchField.setMaxStringLength(64);
            searchField.setClientSideWidget();
            addWidget(searchField);

            entryPanel = new DraggableScrollableWidgetGroup(
                    ENTRY_PANEL_X, ENTRY_PANEL_Y, ENTRY_PANEL_WIDTH, ENTRY_PANEL_HEIGHT)
                    .setBackground(GuiTextures.DISPLAY)
                    .setYScrollBarWidth(2)
                    .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
            entryContent = new WidgetGroup(
                    ENTRY_CONTENT_X, ENTRY_CONTENT_Y, ENTRY_PANEL_WIDTH - 8, ENTRY_PANEL_HEIGHT - 8);
            entryPanel.addWidget(entryContent);
            entryPanel.getMoveCallbacks().add((xOffset, yOffset) -> refreshMountedEntryWidgets());
            addWidget(entryPanel);

            addWidget(createActionButton(ENTRY_PANEL_Y, CLEAR_TOOLTIP, click -> {
                if (click.isRemote) {
                    applyClientSelection(Set.of());
                } else {
                    setServerSelection(Set.of());
                }
            }, GuiTextures.CLIPBOARD_BUTTON.getSubTexture(0, 0.75, 1, 0.25)));
            addWidget(createActionButton(ENTRY_PANEL_Y + 20, RELOAD_TOOLTIP, click -> {
                if (!click.isRemote) {
                    holder.reloadAvailableAEKeys();
                    syncState();
                }
            }, GTOGuiTextures.REFRESH.copy().scale(16 / 20f)));
            addWidget(createActionButton(ENTRY_PANEL_Y + 40, EXPORT_TOOLTIP, click -> {
                if (!click.isRemote) {
                    holder.exportSelectedAEKeys(Set.copyOf(currentState.selected()));
                }
            }, GuiTextures.BUTTON_CHECK));
            addWidget(new WorkModeButton(
                    BUTTON_X, ENTRY_PANEL_Y + 60, BUTTON_SIZE, BUTTON_SIZE, holder,
                    click -> holder.setWorkMode(nextWorkMode(holder.getWorkMode()))));

            rebuildEntryWidgets(0);
        }

        private ButtonWidget createActionButton(int y, String tooltip,
                                                Consumer<ClickData> action, IGuiTexture overlay) {
            ButtonWidget button = new ButtonWidget(
                    BUTTON_X, y, BUTTON_SIZE, BUTTON_SIZE,
                    new GuiTextureGroup(GuiTextures.BUTTON, overlay), action);
            button.setHoverTooltips(Component.translatable(tooltip));
            return button;
        }

        private static ScanningInfoProvider.WorkMode nextWorkMode(ScanningInfoProvider.WorkMode mode) {
            ScanningInfoProvider.WorkMode[] modes = ScanningInfoProvider.WorkMode.values();
            return modes[(mode.ordinal() + 1) % modes.length];
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            SyncState state = buildState();
            applyState(state);
            lastSentState = state;
            writeState(buffer, state);
            super.writeInitialData(buffer);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            applyState(readState(buffer));
            super.readInitialData(buffer);
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            syncState();
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (id == UPDATE_STATE) {
                applyState(readState(buffer));
                return;
            }
            super.readUpdateInfo(id, buffer);
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id == ACTION_SELECT_VISIBLE) {
                var requested = readKeys(buffer, ReferenceOpenHashSet::new);
                setServerSelection(requested);
                return;
            } else if (id == ACTION_TOGGLE) {
                toggleServerSelection(AEKey.readKey(buffer));
                return;
            }
            super.handleClientAction(id, buffer);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_A && isCtrlDown()) {
                selectVisibleEntries();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @OnlyIn(Dist.CLIENT)
        private void selectVisibleEntries() {
            List<AEKey> selected = filteredEntries;
            applyClientSelection(selected);
            writeClientAction(ACTION_SELECT_VISIBLE, buffer -> writeKeys(buffer, selected));
            playButtonClickSound();
        }

        private void syncState() {
            if (isRemote()) {
                return;
            }
            SyncState state = buildState();
            if (state.equals(lastSentState)) {
                return;
            }
            applyState(state);
            lastSentState = state;
            writeUpdateInfo(UPDATE_STATE, buffer -> writeState(buffer, state));
        }

        private SyncState buildState() {
            List<AEKey> entries = new ArrayList<>();
            Set<AEKey> availableKeys = holder.getAvailableAEKeys();
            if (availableKeys != null) {
                ReferenceOpenHashSet<AEKey> uniqueKeys = new ReferenceOpenHashSet<>();
                for (AEKey key : availableKeys) {
                    if (key != null && uniqueKeys.add(key)) {
                        entries.add(key);
                    }
                }
            }
            entries.sort(Comparator
                    .comparing(this::isUnscannable)
                    .thenComparing(this::hasScanned)
                    .thenComparing(entry -> getMaterial(entry).getName())
                    .thenComparing(entry -> entry.getDisplayName().getString().toLowerCase(Locale.ROOT)));

            ReferenceSet<AEKey> available = new ReferenceOpenHashSet<>(entries);
            Set<AEKey> selected = holder.getSelectedAEKeys();
            selected.retainAll(available);
            return new SyncState(List.copyOf(entries), Set.copyOf(selected));
        }

        private void applyState(SyncState state) {
            boolean entriesChanged = !currentState.entries().equals(state.entries());
            currentState = state;
            updateCachedWidgetEntries(state.entries());
            if (entriesChanged) {
                rebuildEntryWidgets(entryPanel == null ? 0 : entryPanel.getScrollYOffset());
            } else if (isRemote()) {
                filteredEntries = getFilteredEntries();
            }
        }

        private void writeState(FriendlyByteBuf buffer, SyncState state) {
            writeKeys(buffer, state.entries());
            writeKeys(buffer, state.selected());
        }

        private SyncState readState(FriendlyByteBuf buffer) {
            var entries = readKeys(buffer, ArrayList::new);
            var selected = readKeys(buffer, ReferenceOpenHashSet::new);
            return new SyncState(List.copyOf(entries), Set.copyOf(selected));
        }

        private static void writeKeys(FriendlyByteBuf buffer, Collection<AEKey> keys) {
            buffer.writeVarInt(keys.size());
            for (AEKey key : keys) {
                AEKey.writeKey(buffer, key);
            }
        }

        private static <C extends Collection<AEKey>> C readKeys(FriendlyByteBuf buffer, IntFunction<C> collectionFactory) {
            int size = buffer.readVarInt();
            C collection = collectionFactory.apply(size);
            for (int i = 0; i < size; i++) {
                AEKey key = AEKey.readKey(buffer);
                if (key != null) {
                    collection.add(key);
                }
            }
            return collection;
        }

        private void rebuildEntryWidgets(int scrollOffset) {
            if (!isRemote()) return;
            entryContent.clearAllWidgets();
            mountedEntryKeys.clear();
            emptyEntryWidgetMounted = false;
            filteredEntries = getFilteredEntries();

            int rows = (filteredEntries.size() + ENTRY_COLUMNS - 1) / ENTRY_COLUMNS;
            int contentHeight = Math.max(ENTRY_PANEL_HEIGHT - 8, rows * ENTRY_SIZE);
            entryContent.setSize(ENTRY_PANEL_WIDTH - 8, contentHeight);
            int maxScrollOffset = Math.max(0, contentHeight + ENTRY_CONTENT_Y - ENTRY_PANEL_HEIGHT);
            entryPanel.setScrollYOffset(Math.min(scrollOffset, maxScrollOffset));
            refreshMountedEntryWidgets();
        }

        private List<AEKey> getFilteredEntries() {
            if (!isRemote()) {
                return currentState.entries();
            }
            String normalizedSearch = searchText.trim().toLowerCase(Locale.ROOT);
            if (normalizedSearch.isEmpty()) {
                return currentState.entries();
            }
            return currentState.entries().stream()
                    .filter(entry -> PinYinUtils.match(entry.getDisplayName().getString().toLowerCase(Locale.ROOT), normalizedSearch))
                    .toList();
        }

        private void refreshMountedEntryWidgets() {
            if (!isRemote()) return;
            if (filteredEntries.isEmpty()) {
                clearMountedEntryWidgets();
                emptyEntryWidget.setText(Component.translatable(
                        currentState.entries().isEmpty() ? EMPTY_ENTRIES : FILTER_EMPTY_ENTRIES));
                if (!emptyEntryWidgetMounted) {
                    entryContent.addWidget(emptyEntryWidget);
                    emptyEntryWidgetMounted = true;
                }
                return;
            }

            if (emptyEntryWidgetMounted) {
                entryContent.removeWidget(emptyEntryWidget);
                emptyEntryWidgetMounted = false;
            }

            // Keep the full content height for scrolling, but mount only rows intersecting the viewport.
            int viewportTop = entryPanel.getScrollYOffset() - ENTRY_CONTENT_Y;
            int viewportBottom = viewportTop + ENTRY_PANEL_HEIGHT;
            int firstRow = Math.max(0, Math.floorDiv(viewportTop, ENTRY_SIZE));
            int rowCount = (filteredEntries.size() + ENTRY_COLUMNS - 1) / ENTRY_COLUMNS;
            int lastRowExclusive = Math.min(rowCount, Math.floorDiv(viewportBottom - 1, ENTRY_SIZE) + 1);
            int firstIndex = Math.min(filteredEntries.size(), firstRow * ENTRY_COLUMNS);
            int lastIndexExclusive = Math.min(filteredEntries.size(), lastRowExclusive * ENTRY_COLUMNS);

            Set<AEKey> visibleKeys = new HashSet<>();
            for (int i = firstIndex; i < lastIndexExclusive; i++) {
                visibleKeys.add(filteredEntries.get(i));
            }

            var mountedIterator = mountedEntryKeys.iterator();
            while (mountedIterator.hasNext()) {
                AEKey key = mountedIterator.next();
                if (!visibleKeys.contains(key)) {
                    AEKeyWidget widget = entryWidgetCache.get(key);
                    if (widget != null) {
                        entryContent.removeWidget(widget);
                    }
                    mountedIterator.remove();
                }
            }

            for (int i = firstIndex; i < lastIndexExclusive; i++) {
                AEKey entry = filteredEntries.get(i);
                AEKeyWidget widget = entryWidgetCache.computeIfAbsent(
                        entry, key -> new AEKeyWidget(this, key, 0, 0));
                widget.setEntry(entry);
                widget.setSelfPosition((i % ENTRY_COLUMNS) * ENTRY_SIZE, (i / ENTRY_COLUMNS) * ENTRY_SIZE);
                if (mountedEntryKeys.add(entry)) {
                    entryContent.addWidget(widget);
                }
            }
        }

        private void clearMountedEntryWidgets() {
            for (AEKey key : mountedEntryKeys) {
                AEKeyWidget widget = entryWidgetCache.get(key);
                if (widget != null) {
                    entryContent.removeWidget(widget);
                }
            }
            mountedEntryKeys.clear();
        }

        private void updateCachedWidgetEntries(Collection<AEKey> entries) {
            for (AEKey entry : entries) {
                AEKeyWidget widget = entryWidgetCache.get(entry);
                if (widget != null) {
                    widget.setEntry(entry);
                }
            }
        }

        private void setSearchText(String newText) {
            String normalized = newText == null ? "" : newText;
            if (!normalized.equals(searchText)) {
                searchText = normalized;
                rebuildEntryWidgets(0);
            }
        }

        private void applyClientSelection(Collection<AEKey> s) {
            var selected = new LinkedHashSet<>(s);
            selected.removeIf(this::isUnscannable);
            currentState = new SyncState(currentState.entries(), selected);
        }

        private void setServerSelection(Collection<AEKey> requested) {
            Set<AEKey> available = new ReferenceOpenHashSet<>(currentState.entries());
            Set<AEKey> selected = new LinkedHashSet<>(requested);
            selected.retainAll(available);
            selected.removeIf(this::isUnscannable);
            currentState = new SyncState(currentState.entries(), selected);
            syncState();
        }

        private void toggleClientSelection(AEKey key) {
            if (isUnscannable(key)) return;
            Set<AEKey> selected = new LinkedHashSet<>(currentState.selected());
            if (!selected.add(key)) {
                selected.remove(key);
            }
            applyClientSelection(selected);
        }

        private void toggleServerSelection(AEKey key) {
            if (isUnscannable(key) ||
                    currentState.entries().stream().noneMatch(entry -> entry == key)) {
                return;
            }
            Set<AEKey> selected = new LinkedHashSet<>(currentState.selected());
            if (!selected.add(key)) {
                selected.remove(key);
            }
            currentState = new SyncState(currentState.entries(), Set.copyOf(selected));
            syncState();
        }

        private boolean hasScanned(AEKey key) {
            return DataScanningManager.hasScanned(key, ownerId);
        }

        private boolean isUnscannable(AEKey key) {
            return DataScanningManager.isUnscannable(key, ownerId);
        }

        public void mouseClicked(AEKey entry) {
            writeClientAction(ACTION_TOGGLE, buffer -> AEKey.writeKey(buffer, entry));
            toggleClientSelection(entry);
            playButtonClickSound();
        }
    }

    private record SyncState(List<AEKey> entries, Set<AEKey> selected) {

        private static final SyncState EMPTY = new SyncState(List.of(), Set.of());
    }

    private static final class EmptyEntryWidget extends Widget {

        private Component text;

        private EmptyEntryWidget(int x, int y, int width, int height, Component text) {
            super(x, y, width, height);
            this.text = text;
            setClientSideWidget();
        }

        private void setText(Component text) {
            this.text = text;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            Position pos = getPosition();
            Size size = getSize();
            graphics.drawWordWrap(
                    Minecraft.getInstance().font, text,
                    pos.x + 2, pos.y + size.height / 2 - Minecraft.getInstance().font.lineHeight,
                    size.width - 4, 0xFFAAAAAA);
        }
    }

    private static final class AEKeyWidget extends Widget {

        private final ScanningSelectionWidget parentWidget;
        private AEKey entry;

        private AEKeyWidget(ScanningSelectionWidget parentWidget, AEKey entry, int x, int y) {
            super(x, y, 18, 18);
            this.parentWidget = parentWidget;
            this.entry = entry;
            setClientSideWidget();
        }

        private void setEntry(AEKey entry) {
            this.entry = entry;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            Position pos = getPosition();
            Size size = getSize();
            boolean selected = parentWidget.currentState.selected().contains(entry);
            boolean isUnscannable = parentWidget.isUnscannable(entry);

            GuiTextures.SLOT.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
            if (isUnscannable) {
                DrawerHelper.drawSolidRect(graphics, pos.x + 1, pos.y + 1, size.width - 2, size.height - 2, 0x55101010);
            } else if (selected) {
                DrawerHelper.drawSolidRect(graphics, pos.x + 1, pos.y + 1, size.width - 2, size.height - 2, 0x5539C5BB);
            }
            AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, pos.x + 1, pos.y + 1, entry);

            if (isUnscannable) {
                StackSizeRenderer.renderSizeLabel(
                        graphics, Minecraft.getInstance().font,
                        pos.x + 1,
                        pos.y + 17 - Minecraft.getInstance().font.lineHeight * 0.5f,
                        Component.translatable(UNSCANNABLE_TOOLTIP), 0.5f, true, true);
            } else if (parentWidget.hasScanned(entry)) {
                StackSizeRenderer.renderSizeLabel(
                        graphics, Minecraft.getInstance().font,
                        pos.x + 1,
                        pos.y + 17 - Minecraft.getInstance().font.lineHeight * 0.5f,
                        Component.translatable(SCANNED_TOOLTIP), 0.5f, true, true);
            }

            var hasMatColor = getMaterial(entry) != NULL;
            var matColor = getMaterial(entry).getMaterialRGB() | 0xFF000000;

            int color;
            if (selected) {
                color = 0xFF39C5BB;
            } else if (isMouseOverEntry(mouseX, mouseY)) {
                color = 0xFFF3F3F3;
            } else if (parentWidget.hasScanned(entry)) {
                color = 0xFFC5BB39;
            } else if (isUnscannable) {
                color = 0xFF202020;
            } else if (hasMatColor) {
                color = matColor;
            } else return;
            if (hasMatColor) {
                color = ColorUtils.blendColor(color, matColor, (float) abs(sin(System.currentTimeMillis() / 400.0)));
            }
            DrawerHelper.drawBorder(graphics, pos.x, pos.y, size.width, size.height, color, 1);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (isMouseOverEntry(mouseX, mouseY)) {
                gui.getModularUIGui().setHoverTooltip(buildTooltip(entry), ItemStack.EMPTY, null, null);
            }
        }

        private List<Component> buildTooltip(AEKey entry) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(entry.getDisplayName().copy().withStyle(ChatFormatting.WHITE));
            if (parentWidget.hasScanned(entry)) {
                tooltip.add(Component.translatable(SCANNED_TOOLTIP).withStyle(ChatFormatting.GOLD));
            }
            if (parentWidget.isUnscannable(entry)) {
                tooltip.add(Component.translatable(UNSCANNABLE_TOOLTIP).withStyle(ChatFormatting.RED));
            }
            if (!parentWidget.isUnscannable(entry)) {
                tooltip.add(Component.translatable(
                        parentWidget.currentState.selected().contains(entry) ? DESELECT_TOOLTIP : SELECT_TOOLTIP)
                        .withStyle(parentWidget.currentState.selected().contains(entry) ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            }
            if (getMaterial(entry) != NULL) {
                tooltip.add(Component.translatable(WORK_MODE_SCAN_MATERIAL, getMaterial(entry).getLocalizedName())
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            return tooltip;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverEntry(mouseX, mouseY) || button != 0) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            parentWidget.mouseClicked(entry);
            return true;
        }

        @OnlyIn(Dist.CLIENT)
        private boolean isMouseOverEntry(double mouseX, double mouseY) {
            return Widget.isMouseOver(
                    parentWidget.entryPanel.getPosition().x, parentWidget.entryPanel.getPosition().y,
                    parentWidget.entryPanel.getSize().width, parentWidget.entryPanel.getSize().height,
                    mouseX, mouseY) &&
                    Widget.isMouseOver(
                            getPosition().x, getPosition().y, getSize().width, getSize().height,
                            mouseX, mouseY);
        }
    }

    private static final class WorkModeButton extends ButtonWidget {

        private final ScanningInfoProvider holder;

        private WorkModeButton(int x, int y, int width, int height, ScanningInfoProvider holder,
                               Consumer<ClickData> action) {
            super(x, y, width, height,
                    new GuiTextureGroup(GuiTextures.BUTTON, GuiTextures.BUTTON_POWER), action);
            this.holder = holder;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void drawTooltipTexts(int mouseX, int mouseY) {
            setHoverTooltips(
                    Component.translatable(WORK_MODE_TOOLTIP),
                    Component.translatable(CURRENT_WORK_MODE_TOOLTIP, getWorkModeName(holder.getWorkMode()))
                            .withStyle(ChatFormatting.GRAY));
            super.drawTooltipTexts(mouseX, mouseY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            setButtonTexture(GuiTextures.BUTTON, getWorkModeTexture(holder.getWorkMode()).copy().scale(16 / 18f));
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }

        private static Component getWorkModeName(ScanningInfoProvider.WorkMode mode) {
            return Component.translatable(switch (mode) {
                case SCAN_UNLEARNED_ONLY -> WORK_MODE_SCAN_UNLEARNED_ONLY;
                case SCAN_UNLEARNED_ONCE -> WORK_MODE_SCAN_UNLEARNED_ONCE;
                case SCAN_SELECTED_ONLY -> WORK_MODE_SCAN_SELECTED_ONLY;
                case SCAN_SELECTED_ONCE -> WORK_MODE_SCAN_SELECTED_ONCE;
            });
        }

        @OnlyIn(Dist.CLIENT)
        private static IGuiTexture getWorkModeTexture(ScanningInfoProvider.WorkMode mode) {
            return switch (mode) {
                case SCAN_UNLEARNED_ONLY -> GTOGuiTextures.INTELLIGENT_SCANNER_1;
                case SCAN_UNLEARNED_ONCE -> GTOGuiTextures.INTELLIGENT_SCANNER_2;
                case SCAN_SELECTED_ONLY -> GTOGuiTextures.INTELLIGENT_SCANNER_3;
                case SCAN_SELECTED_ONCE -> GTOGuiTextures.INTELLIGENT_SCANNER_4;
            };
        }
    }
}

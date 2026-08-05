package com.gtocore.common.machine.multiblock.electric.research.ui;

import com.gtocore.api.research.TeamResearchSavedDtat;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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
    @RegisterLanguage(cn = "清空选择", en = "Clear selection")
    private static final String CLEAR_TOOLTIP = "gtocore.research.scanning_selection_tab.clear";
    @RegisterLanguage(cn = "重新加载可用扫描目标", en = "Reload available scan targets")
    private static final String RELOAD_TOOLTIP = "gtocore.research.scanning_selection_tab.reload";
    @RegisterLanguage(cn = "导出所选扫描目标", en = "Export selected scan targets")
    private static final String EXPORT_TOOLTIP = "gtocore.research.scanning_selection_tab.export";
    @RegisterLanguage(cn = "切换机器工作模式", en = "Switch machine work mode")
    private static final String WORK_MODE_TOOLTIP = "gtocore.research.scanning_selection_tab.work_mode";
    @RegisterLanguage(cn = "当前模式：%s", en = "Current mode: %s")
    private static final String CURRENT_WORK_MODE_TOOLTIP = "gtocore.research.scanning_selection_tab.work_mode.current";
    @RegisterLanguage(cn = "持续扫描未学习目标", en = "Continuously scan unlearned targets")
    private static final String WORK_MODE_SCAN_UNLEARNED_ONLY = "gtocore.research.scanning_selection_tab.work_mode.scan_unlearned_only";
    @RegisterLanguage(cn = "扫描一次未学习目标后停止", en = "Stop after scanning unlearned targets once")
    private static final String WORK_MODE_SCAN_UNLEARNED_ONCE = "gtocore.research.scanning_selection_tab.work_mode.scan_unlearned_once";
    @RegisterLanguage(cn = "只扫描选定目标", en = "Scan selected targets only")
    private static final String WORK_MODE_SCAN_SELECTED_ONLY = "gtocore.research.scanning_selection_tab.work_mode.scan_selected_only";
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
        private static final int ACTION_SELECT_VISIBLE = 1;
        private static final int SEARCH_FIELD_HEIGHT = 14;
        private static final int ENTRY_PANEL_X = 4;
        private static final int ENTRY_PANEL_Y = 22;
        private static final int ENTRY_PANEL_WIDTH = 150;
        private static final int ENTRY_PANEL_HEIGHT = 126;
        private static final int ENTRY_COLUMNS = 8;
        private static final int BUTTON_X = 160;
        private static final int BUTTON_SIZE = 18;
        private static final int MAX_KEY_PACKET_COUNT = 1 << 20;

        private final ScanningInfoProvider holder;
        private final UUID ownerId;
        private final DraggableScrollableWidgetGroup entryPanel;
        private final WidgetGroup entryContent;
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
            entryContent = new WidgetGroup(2, 4, ENTRY_PANEL_WIDTH - 8, ENTRY_PANEL_HEIGHT - 8);
            entryPanel.addWidget(entryContent);
            addWidget(entryPanel);

            addWidget(createActionButton(ENTRY_PANEL_Y, CLEAR_TOOLTIP, click -> {
                if (click.isRemote) {
                    applyClientSelection(Set.of());
                } else {
                    setServerSelection(Set.of());
                }
            }));
            addWidget(createActionButton(ENTRY_PANEL_Y + 20, RELOAD_TOOLTIP, click -> {
                if (!click.isRemote) {
                    holder.reloadAvailableAEKeys();
                    syncState();
                }
            }));
            addWidget(createActionButton(ENTRY_PANEL_Y + 40, EXPORT_TOOLTIP, click -> {
                if (!click.isRemote) {
                    holder.exportSelectedAEKeys(Set.copyOf(currentState.selected()));
                }
            }));
            addWidget(new WorkModeButton(
                    BUTTON_X, ENTRY_PANEL_Y + 60, BUTTON_SIZE, BUTTON_SIZE, holder,
                    click -> {
                        if (!click.isRemote) {
                            holder.setWorkMode(nextWorkMode(holder.getWorkMode()));
                        }
                    }));

            rebuildEntryWidgets(0);
        }

        private ButtonWidget createActionButton(int y, String tooltip,
                                                Consumer<ClickData> action) {
            ButtonWidget button = new ButtonWidget(
                    BUTTON_X, y, BUTTON_SIZE, BUTTON_SIZE,
                    new GuiTextureGroup(GuiTextures.BUTTON, GuiTextures.BUTTON_POWER), action);
            button.setHoverTooltips(Component.translatable(tooltip));
            return button;
        }

        private static ScanningInfoProvider.WorkMode nextWorkMode(ScanningInfoProvider.WorkMode mode) {
            ScanningInfoProvider.WorkMode[] modes = ScanningInfoProvider.WorkMode.values();
            return modes[(mode.ordinal() + 1) % modes.length];
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            super.writeInitialData(buffer);
            SyncState state = buildState();
            applyState(state);
            lastSentState = state;
            writeState(buffer, state);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            super.readInitialData(buffer);
            applyState(readState(buffer));
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
                int count = buffer.readVarInt();
                if (count < 0 || count > MAX_KEY_PACKET_COUNT) {
                    return;
                }
                Set<AEKey> requested = new HashSet<>();
                for (int i = 0; i < count; i++) {
                    AEKey key = AEKey.readKey(buffer);
                    if (key != null) {
                        requested.add(key);
                    }
                }
                setServerSelection(requested);
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
            List<AEKey> selected = getVisibleEntries();
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
                Set<AEKey> uniqueKeys = new HashSet<>();
                for (AEKey key : availableKeys) {
                    if (key != null && uniqueKeys.add(key)) {
                        entries.add(key);
                    }
                }
            }
            entries.sort(Comparator
                    .comparing(this::hasScanned)
                    .thenComparing(entry -> getMaterial(entry).getName())
                    .thenComparing(entry -> entry.getDisplayName().getString().toLowerCase(Locale.ROOT)));

            ReferenceSet<AEKey> available = new ReferenceOpenHashSet<>(entries);
            Set<AEKey> selected = holder.getSelectedAEKeys();
            selected.retainAll(available);
            return new SyncState(List.copyOf(entries), Set.copyOf(selected));
        }

        private void applyState(SyncState state) {
            currentState = state;
            rebuildEntryWidgets(entryPanel == null ? 0 : entryPanel.getScrollYOffset());
        }

        private void writeState(FriendlyByteBuf buffer, SyncState state) {
            buffer.writeVarInt(state.entries().size());
            for (AEKey entry : state.entries()) {
                AEKey.writeKey(buffer, entry);
            }
            writeKeys(buffer, state.selected());
        }

        private SyncState readState(FriendlyByteBuf buffer) {
            int entryCount = buffer.readVarInt();
            List<AEKey> entries = new ArrayList<>(entryCount);
            for (int i = 0; i < entryCount; i++) {
                AEKey key = AEKey.readKey(buffer);
                if (key != null) {
                    entries.add(key);
                }
            }
            int selectedCount = buffer.readVarInt();
            Set<AEKey> selected = new LinkedHashSet<>();
            for (int i = 0; i < selectedCount; i++) {
                AEKey key = AEKey.readKey(buffer);
                if (key != null) {
                    selected.add(key);
                }
            }
            return new SyncState(List.copyOf(entries), Set.copyOf(selected));
        }

        private static void writeKeys(FriendlyByteBuf buffer, Collection<AEKey> keys) {
            buffer.writeVarInt(keys.size());
            for (AEKey key : keys) {
                AEKey.writeKey(buffer, key);
            }
        }

        private void rebuildEntryWidgets(int scrollOffset) {
            entryContent.clearAllWidgets();
            List<AEKey> visibleEntries = getVisibleEntries();
            for (int i = 0; i < visibleEntries.size(); i++) {
                AEKey entry = visibleEntries.get(i);
                int x = (i % ENTRY_COLUMNS) * 18;
                int y = (i / ENTRY_COLUMNS) * 18;
                entryContent.addWidget(new AEKeyWidget(this, entry, x, y));
            }

            if (visibleEntries.isEmpty()) {
                entryContent.addWidget(new EmptyEntryWidget(
                        2, 2, ENTRY_PANEL_WIDTH - 12, ENTRY_PANEL_HEIGHT - 12,
                        Component.translatable(currentState.entries().isEmpty() ? EMPTY_ENTRIES : FILTER_EMPTY_ENTRIES)));
            }

            int rows = (visibleEntries.size() + ENTRY_COLUMNS - 1) / ENTRY_COLUMNS;
            entryContent.setSize(ENTRY_PANEL_WIDTH - 8, Math.max(ENTRY_PANEL_HEIGHT - 8, rows * 18));
            entryPanel.setScrollYOffset(scrollOffset);
        }

        private List<AEKey> getVisibleEntries() {
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

        private void setSearchText(String newText) {
            String normalized = newText == null ? "" : newText;
            if (!normalized.equals(searchText)) {
                searchText = normalized;
                rebuildEntryWidgets(0);
            }
        }

        private void applyClientSelection(Collection<AEKey> selected) {
            currentState = new SyncState(currentState.entries(), Set.copyOf(selected));
            rebuildEntryWidgets(entryPanel.getScrollYOffset());
        }

        private void setServerSelection(Collection<AEKey> requested) {
            Set<AEKey> available = new ReferenceOpenHashSet<>(currentState.entries());
            Set<AEKey> selected = new LinkedHashSet<>(requested);
            selected.retainAll(available);
            currentState = new SyncState(currentState.entries(), Set.copyOf(selected));
            syncState();
        }

        private void toggleClientSelection(AEKey key) {
            Set<AEKey> selected = new LinkedHashSet<>(currentState.selected());
            if (!selected.add(key)) {
                selected.remove(key);
            }
            applyClientSelection(selected);
        }

        private void toggleServerSelection(AEKey key) {
            if (currentState.entries().stream().noneMatch(entry -> entry == key)) {
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
            return TeamResearchSavedDtat.hasScanned(key, ownerId);
        }
    }

    private record SyncState(List<AEKey> entries, Set<AEKey> selected) {

        private static final SyncState EMPTY = new SyncState(List.of(), Set.of());
    }

    private static final class EmptyEntryWidget extends Widget {

        private final Component text;

        private EmptyEntryWidget(int x, int y, int width, int height, Component text) {
            super(x, y, width, height);
            this.text = text;
            setClientSideWidget();
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

        private static final int ACTION_TOGGLE = 1;

        private final ScanningSelectionWidget parentWidget;
        private final AEKey entry;

        private AEKeyWidget(ScanningSelectionWidget parentWidget, AEKey entry, int x, int y) {
            super(x, y, 18, 18);
            this.parentWidget = parentWidget;
            this.entry = entry;
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id == ACTION_TOGGLE) {
                parentWidget.toggleServerSelection(entry);
                return;
            }
            super.handleClientAction(id, buffer);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            Position pos = getPosition();
            Size size = getSize();
            boolean selected = parentWidget.currentState.selected().contains(entry);

            GuiTextures.SLOT.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);
            if (selected) {
                DrawerHelper.drawSolidRect(graphics, pos.x + 1, pos.y + 1, size.width - 2, size.height - 2, 0x5539C5BB);
            }
            AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, pos.x + 1, pos.y + 1, entry);

            if (parentWidget.hasScanned(entry)) {
                StackSizeRenderer.renderSizeLabel(
                        graphics, Minecraft.getInstance().font,
                        pos.x + 1,
                        pos.y + 17 - Minecraft.getInstance().font.lineHeight * 0.5f,
                        Component.translatable(SCANNED_TOOLTIP), 0.5f, true, true);
            }

            var hasMatColor = getMaterial(entry) != NULL;
            var matColor = getMaterial(entry).getMaterialRGB();

            int color;
            if (selected) {
                color = 0xFF39C5BB;
            } else if (isMouseOverEntry(mouseX, mouseY)) {
                color = 0xFFF3F3F3;
            } else if (parentWidget.hasScanned(entry)) {
                color = 0xFFC5BB39;
            } else return;
            if (hasMatColor) {
                color = ColorUtils.blendColor(color, matColor, (float) abs(sin(System.currentTimeMillis() / 1000.0)));
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
            tooltip.add(Component.translatable(
                    parentWidget.currentState.selected().contains(entry) ? DESELECT_TOOLTIP : SELECT_TOOLTIP)
                    .withStyle(parentWidget.currentState.selected().contains(entry) ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            tooltip.add(Component.translatable(WORK_MODE_SCAN_MATERIAL, getMaterial(entry).getLocalizedName())
                    .withStyle(ChatFormatting.DARK_GRAY));
            return tooltip;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverEntry(mouseX, mouseY) || button != 0) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            writeClientAction(ACTION_TOGGLE, buffer -> {});
            parentWidget.toggleClientSelection(entry);
            playButtonClickSound();
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

        private static Component getWorkModeName(ScanningInfoProvider.WorkMode mode) {
            return Component.translatable(switch (mode) {
                case SCAN_UNLEARNED_ONLY -> WORK_MODE_SCAN_UNLEARNED_ONLY;
                case SCAN_UNLEARNED_ONCE -> WORK_MODE_SCAN_UNLEARNED_ONCE;
                case SCAN_SELECTED_ONLY -> WORK_MODE_SCAN_SELECTED_ONLY;
            });
        }
    }
}

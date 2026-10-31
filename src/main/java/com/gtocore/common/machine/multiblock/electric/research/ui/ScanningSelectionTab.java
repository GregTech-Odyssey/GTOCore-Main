package com.gtocore.common.machine.multiblock.electric.research.ui;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.integration.jech.PinYinUtils;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.Binding;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.data.UICodecs;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.elements.VirtualList;
import com.gregtechceu.gtceu.uipro.elements.ae.AEKeySlot;
import com.gregtechceu.gtceu.uipro.render.FittedText;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UILayers;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyIntMap;

import com.gto.datasynclib.datastream.codec.StreamCodec;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.gregtechceu.gtceu.common.data.GTMaterials.NULL;
import static com.gtolib.utils.AEChemicalHelper.getMaterial;

@DataGeneratorScanned
public class ScanningSelectionTab implements IFancyUIProvider {

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
    @RegisterLanguage(cn = "未扫描", en = "Not scanned")
    private static final String UNSCANNED_TOOLTIP = "gtocore.research.scanning_selection_tab.unscanned";
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
    @RegisterLanguage(cn = "循环扫描所选目标", en = "Continuously scan the selected targets")
    private static final String WORK_MODE_SCAN_SELECTED_ONLY = "gtocore.research.scanning_selection_tab.work_mode.scan_selected_only";
    @RegisterLanguage(cn = "扫描一次所选目标后停止", en = "Scan the selected targets once, then stop")
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
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return new ScanningSelectionPage(holder, ownerId);
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

    private record ScanTarget(AEKey key, int flags) {

        private static final int SCANNED = 1;
        private static final int UNSCANNABLE = 2;
        private static final StreamCodec<FriendlyByteBuf, ScanTarget> CODEC = StreamCodec.of((buffer, target) -> {
            AEKey.writeKey(buffer, target.key());
            buffer.writeByte(target.flags());
        }, buffer -> new ScanTarget(AEKey.readKey(buffer), buffer.readUnsignedByte()));

        private boolean scanned() {
            return (flags & SCANNED) != 0;
        }

        private boolean unscannable() {
            return (flags & UNSCANNABLE) != 0;
        }
    }

    private static final class ScanningSelectionPage extends UIElement {

        private static final int MAX_TARGETS = 1 << 20;
        private static final int REFRESH_TICKS = 20;
        private static final StreamCodec<FriendlyByteBuf, List<ScanTarget>> TARGETS_CODEC = UICodecs.list(ScanTarget.CODEC, MAX_TARGETS);
        private static final StreamCodec<FriendlyByteBuf, List<AEKey>> KEYS_CODEC = UICodecs.list(StreamCodec.of(AEKey::writeKey, AEKey::readKey), MAX_TARGETS);

        private final ScanningInfoProvider holder;
        private final UUID ownerId;
        private final AEKeyIntMap<AEKey> targetStates = new AEKeyIntMap<>();
        private ObjectArrayList<ScanTarget> targetSnapshot = new ObjectArrayList<>(0);
        private ObjectArrayList<AEKey> selectionSnapshot = new ObjectArrayList<>(0);
        private final Binding<ScanningInfoProvider.WorkMode> workMode;
        private final RPC<List<AEKey>> confirm;
        private final RPC<Unit> reload;
        private final Content content;
        private int refreshTicks;

        private ScanningSelectionPage(ScanningInfoProvider holder, UUID ownerId) {
            this.holder = holder;
            this.ownerId = ownerId;
            targetStates.defaultReturnValue(-1);
            layout(l -> l.column());
            addSyncValue(SyncValue.of(() -> targetSnapshot, TARGETS_CODEC, Collections.emptyList())
                    .onChanged(this::applyTargets));
            addSyncValue(SyncValue.of(this::selectionSnapshot, KEYS_CODEC, Collections.emptyList())
                    .onChanged(this::applySelection));
            workMode = addBinding(Binding.bindEnum(ScanningInfoProvider.WorkMode.class, holder::getWorkMode, holder::setWorkMode));
            confirm = addRPC(KEYS_CODEC, (player, keys) -> confirmSelection(keys));
            reload = addRPC(player -> {
                holder.reloadAvailableAEKeys();
                refreshTargets();
            });
            content = new Content();
            addChild(content);
        }

        private void applyTargets(List<ScanTarget> value) {
            if (isRemote()) content.applyTargets(value);
        }

        private void applySelection(List<AEKey> value) {
            if (isRemote()) content.applySelection(value);
        }

        @Override
        public void initWidget() {
            if (!isRemote()) refreshTargets();
            super.initWidget();
        }

        @Override
        public void detectAndSendChanges() {
            if (++refreshTicks >= REFRESH_TICKS) {
                refreshTicks = 0;
                refreshTargets();
            }
            super.detectAndSendChanges();
        }

        private void refreshTargets() {
            var available = holder.getAvailableAEKeys();
            boolean changed = targetStates.size() != available.size();
            var team = TeamUtil.getTeamUUID(ownerId);
            for (var key : available) {
                int flags = DataScanningManager.hasScanned(key, ownerId) ? ScanTarget.SCANNED : 0;
                if (DataScanningManager.isUnscannable(key, team)) flags |= ScanTarget.UNSCANNABLE;
                if (targetStates.put(key, flags) != flags) changed = true;
            }
            if (!changed) return;
            targetStates.keySet().removeIf(key -> !available.contains(key));
            var snapshot = new ObjectArrayList<ScanTarget>(available.size());
            for (var key : available) snapshot.add(new ScanTarget(key, targetStates.getInt(key)));
            // SyncValue compares by equality: keep the previous list unchanged after publishing it.
            targetSnapshot = snapshot;
        }

        private ObjectArrayList<AEKey> selectionSnapshot() {
            var selected = holder.getSelectedAEKeys();
            if (selected.size() != selectionSnapshot.size() || !selected.containsAll(selectionSnapshot)) {
                selectionSnapshot = new ObjectArrayList<>(selected);
            }
            return selectionSnapshot;
        }

        private void confirmSelection(List<AEKey> requested) {
            var available = holder.getAvailableAEKeys();
            var selected = new ReferenceOpenHashSet<AEKey>(Math.min(requested.size(), available.size()));
            var team = TeamUtil.getTeamUUID(ownerId);
            for (var key : requested) {
                if (available.contains(key) && !DataScanningManager.isUnscannable(key, team)) selected.add(key);
            }
            holder.exportSelectedAEKeys(selected);
            refreshTargets();
        }

        /** Local controls read synchronized snapshots; all machine changes go through the page's RPCs. */
        private final class Content extends UIElement implements ILocalUI {

            private static final int COLUMNS = UISizes.SLOTS_PER_ROW - 1;
            private static final int GRID_HEIGHT = 6 * UISizes.SLOT_SIZE;
            private static final Comparator<ScanTarget> TARGET_ORDER = Comparator.comparing(ScanTarget::unscannable)
                    .thenComparing(ScanTarget::scanned)
                    .thenComparing(target -> getMaterial(target.key()).getName())
                    .thenComparing(target -> target.key().getDisplayName().getString().toLowerCase(Locale.ROOT));

            private final ReferenceLinkedOpenHashSet<AEKey> selected = new ReferenceLinkedOpenHashSet<>();
            private AEKeyIntMap<AEKey> flags = new AEKeyIntMap<>();
            private ObjectArrayList<ScanTarget> entries = new ObjectArrayList<>(0);
            private final ObjectArrayList<ScanTarget> filtered = new ObjectArrayList<>(0);
            private final VirtualList grid;
            private final ScrollerView scroller;
            private final Label empty;
            private final Component noTargets = Component.translatable(EMPTY_ENTRIES);
            private final Component noMatches = Component.translatable(FILTER_EMPTY_ENTRIES);
            private String searchText = "";
            private ScanningInfoProvider.WorkMode describedMode;
            private Component modeDescription = Component.empty();

            private Content() {
                layout(l -> l.column());
                var search = TextField.of(UISizes.CONTENT_WIDTH, () -> searchText, this::setSearchText)
                        .setPlaceholder(Component.translatable(SEARCH_TOOLTIP)).setRightClickClear(true);
                search.getInput().setMaxStringLength(64);
                search.tooltips(SEARCH_TOOLTIP);
                grid = new VirtualList(COLUMNS, UISizes.SLOT_SIZE, 0, filtered::size,
                        index -> new TargetSlot(filtered.get(index)));
                grid.getLayoutStyle().width(COLUMNS * UISizes.SLOT_SIZE);
                empty = Label.of(UISizes.CONTENT_WIDTH - ScrollerView.SCROLL_BAR_SPACE,
                        () -> entries.isEmpty() ? noTargets : noMatches)
                        .bindClientColor(UITheme::textSecondary);
                scroller = new ScrollerView("research.scanning.targets", UISizes.CONTENT_WIDTH, GRID_HEIGHT).fitPage();
                scroller.addScrollViewChild(grid);
                scroller.addScrollViewChild(empty);
                var actions = UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(
                        Button.translatable(UISizes.BUTTON_WIDTH, CLEAR_TOOLTIP).setOnClientClick(selected::clear),
                        Button.icon(GTOGuiTextures.REFRESH).tooltips(RELOAD_TOOLTIP).setOnClientClick(() -> reload.send(Unit.INSTANCE)),
                        UIElement.flexSpacer(),
                        Button.translatable(UISizes.BUTTON_WIDTH, EXPORT_TOOLTIP).setVariant(UITheme.ButtonVariant.CONFIRM)
                                .setOnClientClick(() -> confirm.send(new ObjectArrayList<>(selected))));
                var mode = Button.icon(UISizes.SLOT_SIZE, GTOGuiTextures.INTELLIGENT_SCANNER_1)
                        .bindClientIcon(() -> getWorkModeTexture(workMode.getValue()))
                        .tooltips(WORK_MODE_TOOLTIP)
                        .setOnClientClick(() -> workMode.set(nextWorkMode(workMode.getValue())));
                var description = TextLine.of(0, this::modeDescription).bindClientColor(UITheme::text)
                        .layout(l -> l.flex(1));
                addChild(Form.page().addChildren(search, scroller, actions,
                        UIElement.centeredRow(UISizes.SLOT_SIZE).addChildren(mode, description)));
                setClientSideWidget();
            }

            private Component modeDescription() {
                var mode = workMode.getValue();
                if (describedMode != mode) {
                    describedMode = mode;
                    modeDescription = Component.translatable(CURRENT_WORK_MODE_TOOLTIP, getWorkModeName(mode));
                }
                return modeDescription;
            }

            private void applyTargets(List<ScanTarget> value) {
                entries = new ObjectArrayList<>(value);
                entries.sort(TARGET_ORDER);
                flags = new AEKeyIntMap<>(entries.size());
                for (var target : entries) flags.put(target.key(), target.flags());
                pruneSelection();
                refreshFilter();
            }

            private void applySelection(List<AEKey> value) {
                selected.clear();
                selected.addAll(value);
                pruneSelection();
            }

            private void pruneSelection() {
                selected.removeIf(key -> !flags.containsKey(key) || (flags.getInt(key) & ScanTarget.UNSCANNABLE) != 0);
            }

            private void setSearchText(String value) {
                if (searchText.equals(value)) return;
                searchText = value;
                refreshFilter();
                scroller.setScrollYOffset(0);
            }

            private void refreshFilter() {
                filtered.clear();
                filtered.ensureCapacity(entries.size());
                String query = searchText.trim().toLowerCase(Locale.ROOT);
                for (var target : entries) {
                    if (query.isEmpty() || PinYinUtils.match(target.key().getDisplayName().getString().toLowerCase(Locale.ROOT), query)) {
                        filtered.add(target);
                    }
                }
                grid.refresh();
                empty.setDisplay(filtered.isEmpty());
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
                if (keyCode != GLFW.GLFW_KEY_A || !isCtrlDown()) return false;
                selected.clear();
                for (var target : filtered) {
                    if (!target.unscannable()) selected.add(target.key());
                }
                playButtonClickSound();
                return true;
            }

            private final class TargetSlot extends AEKeySlot {

                private final ScanTarget target;
                private final Component scanStatus;
                private final ObjectArrayList<Component> selectTooltips;
                private final ObjectArrayList<Component> deselectTooltips;
                private FittedText statusText;

                private TargetSlot(ScanTarget target) {
                    super(target::key, () -> 1);
                    this.target = target;
                    scanStatus = Component.translatable(target.scanned() ? SCANNED_TOOLTIP : UNSCANNED_TOOLTIP)
                            .withStyle(target.scanned() ? ChatFormatting.GREEN : ChatFormatting.GRAY);
                    setShowAmount(false);
                    setSelected(() -> selected.contains(target.key()));
                    clientDisabled(target::unscannable, UNSCANNABLE_TOOLTIP);
                    selectTooltips = targetTooltips(false);
                    deselectTooltips = targetTooltips(true);
                    bindTooltips(() -> isSelected() ? deselectTooltips : selectTooltips);
                }

                private ObjectArrayList<Component> targetTooltips(boolean selected) {
                    var lines = new ObjectArrayList<Component>(3);
                    lines.add(scanStatus);
                    if (!target.unscannable()) {
                        lines.add(Component.translatable(selected ? DESELECT_TOOLTIP : SELECT_TOOLTIP).withStyle(ChatFormatting.GRAY));
                    }
                    var material = getMaterial(target.key());
                    if (material != NULL) {
                        lines.add(Component.translatable(WORK_MODE_SCAN_MATERIAL, material.getLocalizedName()).withStyle(ChatFormatting.DARK_GRAY));
                    }
                    return lines;
                }

                @Override
                @OnlyIn(Dist.CLIENT)
                protected void drawSlotMark(GuiGraphics graphics, int x, int y, boolean darkSlot) {
                    var material = getMaterial(target.key());
                    if (material != NULL) UIDraw.underline(graphics, x + 1, y + 1, SIZE - 2, material.getMaterialARGB());
                }

                @Override
                @OnlyIn(Dist.CLIENT)
                public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                    super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
                    if (statusText == null) statusText = new FittedText();
                    String text = statusText.fit(scanStatus, Integer.MAX_VALUE);
                    float scale = Math.min(0.5f, (float) (SIZE - 2) / statusText.width());
                    int width = (int) Math.ceil(statusText.width() * scale);
                    int height = (int) Math.ceil(UIText.GLYPH_HEIGHT * scale);
                    var pose = graphics.pose();
                    pose.pushPose();
                    pose.translate(getPositionX() + SIZE - width - 1, getPositionY() + SIZE - height - 1, UILayers.ITEM_OVERLAY);
                    UIDraw.fillRect(graphics, 0, 0, width, height, 0xDD101010);
                    pose.scale(scale, scale, 1);
                    UIText.drawLeft(graphics, text, 0, 0, target.scanned() ? 0xFF8FE58B : 0xFFCCCCCC);
                    pose.popPose();
                }

                @Override
                @OnlyIn(Dist.CLIENT)
                public boolean mouseClicked(double mouseX, double mouseY, int button) {
                    if (button != 0 || !isMouseOverElement(mouseX, mouseY)) return false;
                    if (!isDisabled()) {
                        if (!selected.add(target.key())) selected.remove(target.key());
                        playButtonClickSound();
                    }
                    return true;
                }
            }
        }
    }

    private static ScanningInfoProvider.WorkMode nextWorkMode(ScanningInfoProvider.WorkMode mode) {
        var modes = ScanningInfoProvider.WorkMode.values();
        return modes[(mode.ordinal() + 1) % modes.length];
    }

    private static Component getWorkModeName(ScanningInfoProvider.WorkMode mode) {
        return Component.translatable(switch (mode) {
            case SCAN_UNLEARNED_ONLY -> WORK_MODE_SCAN_UNLEARNED_ONLY;
            case SCAN_UNLEARNED_ONCE -> WORK_MODE_SCAN_UNLEARNED_ONCE;
            case SCAN_SELECTED_ONLY -> WORK_MODE_SCAN_SELECTED_ONLY;
            case SCAN_SELECTED_ONCE -> WORK_MODE_SCAN_SELECTED_ONCE;
        });
    }

    private static IGuiTexture getWorkModeTexture(ScanningInfoProvider.WorkMode mode) {
        return switch (mode) {
            case SCAN_UNLEARNED_ONLY -> GTOGuiTextures.INTELLIGENT_SCANNER_1;
            case SCAN_UNLEARNED_ONCE -> GTOGuiTextures.INTELLIGENT_SCANNER_2;
            case SCAN_SELECTED_ONLY -> GTOGuiTextures.INTELLIGENT_SCANNER_3;
            case SCAN_SELECTED_ONCE -> GTOGuiTextures.INTELLIGENT_SCANNER_4;
        };
    }
}

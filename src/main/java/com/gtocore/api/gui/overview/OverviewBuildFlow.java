package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.BuildUpload;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Layout;
import com.gregtechceu.gtceu.api.machine.multiblockpro.StructureBuild;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uipro.window.PopupCard;
import com.gregtechceu.gtceu.uiwidgets.patternbuilder.PatternBuilderModel;
import com.gregtechceu.gtceu.uiwidgets.patternbuilder.PatternBuilderPanel;
import com.gregtechceu.gtceu.uiwidgets.structure.PartSlots;
import com.gregtechceu.gtceu.uiwidgets.structure.StructureBuildFlow;
import com.gregtechceu.gtceu.uiwidgets.structure.StructureConfigView;
import com.gregtechceu.gtceu.uiwidgets.structure.StructurePlans;
import com.gregtechceu.gtceu.uiwidgets.structure.StructurePreviewScreen;
import com.gregtechceu.gtceu.uiwidgets.structure.StructureProjection;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
final class OverviewBuildFlow {

    private static final long REFRESH_DELAY_MS = 200;
    private static final int PARTS_PER_ROW = UISizes.SLOTS_PER_ROW;

    private final OverviewView view;
    private final OverviewWidget owner;
    private final OverviewAdapter adapter;
    @Nullable
    private Choice choice;

    private static final class Choice {

        final MultiblockMachineDefinition definition;
        final int[] values;
        final StructureConfigView config;
        final WidgetGroup parts = new WidgetGroup(0, 0, PARTS_PER_ROW * UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
        List<OverviewDocking.DockPose> poses = Collections.emptyList();
        int pose;
        long refreshAt;
        @Nullable
        PatternBuilderModel model;
        @Nullable
        Layout modelLayout;
        @Nullable
        Layout modelBase;
        int modelVersion = -1;

        Choice(MultiblockMachineDefinition definition, int[] values, StructureConfigView config) {
            this.definition = definition;
            this.values = values;
            this.config = config;
        }

        @Nullable
        OverviewDocking.DockPose current() {
            return poses.isEmpty() ? null : poses.get(Math.floorMod(pose, poses.size()));
        }

        Item[] items(Layout layout) {
            var items = StructurePlans.preview(definition, layout).items();
            if (model != null && modelLayout != null) items = StructurePlans.merge(StructurePlans.assign(model, modelLayout), items);
            return items;
        }
    }

    OverviewBuildFlow(OverviewView view, OverviewWidget owner, OverviewAdapter adapter) {
        this.view = view;
        this.owner = owner;
        this.adapter = adapter;
    }

    boolean active() {
        return choice != null;
    }

    void clear() {
        choice = null;
    }

    void onSnapshot() {
        if (choice != null) choice.refreshAt = System.currentTimeMillis();
    }

    void fitConfig(PopupCard card) {
        var current = choice;
        if (current == null || current.config.getParent() == null) return;
        current.config.fitViewHeight(card.getContentLimit() - current.config.getParent().getSizeHeight() + current.config.viewHeight());
    }

    void tick() {
        var current = choice;
        if (current == null) return;
        if (current.refreshAt > 0 && System.currentTimeMillis() >= current.refreshAt) {
            current.refreshAt = 0;
            recompute();
        }
        if (current.model != null && current.model.getVersion() != current.modelVersion) {
            current.modelVersion = current.model.getVersion();
            refreshGhost();
        }
    }

    void choose(MultiblockMachineDefinition definition) {
        var structure = definition.displayStructure();
        if (structure == null) return;
        var values = StructureBuildFlow.remembered(definition, structure);
        var excluded = StructureConfigView.defaultExcluded(structure.tree(), values);
        var holder = new Choice[1];
        var config = new StructureConfigView(definition, structure, values, excluded, () -> {
            if (holder[0] != null) holder[0].refreshAt = System.currentTimeMillis() + REFRESH_DELAY_MS;
        }, null, UISizes.POPUP_CONTENT_WIDTH, view.frameWidth() / 3, view.frameHeight() / 3);
        choice = holder[0] = new Choice(definition, values, config);
        recompute();
        openConfig();
    }

    void reopenConfig() {
        if (choice != null) openConfig();
    }

    private void openConfig() {
        var current = choice;
        if (current == null) return;
        var definition = current.definition;
        var popup = Popup.of(definition::asStack, () -> definition.asStack().getHoverName(), column -> {
            var rotate = Button.translatable(UISizes.BUTTON_WIDTH, OverviewWidget.LANG_ROTATE).setOnClientClick(() -> {
                current.pose++;
                refreshGhost();
            }).disabled(() -> current.poses.size() < 2, OverviewWidget.LANG_SINGLE);
            rotate.tooltips(OverviewWidget.LANG_ROTATE_TOOLTIP);
            column.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(
                    TextLine.of(LayoutStyle.AUTO, () -> current.poses.isEmpty() ? Component.translatable(OverviewWidget.LANG_BLOCKED) :
                            Component.translatable(OverviewWidget.LANG_ORIENTATION, Math.floorMod(current.pose, current.poses.size()) + 1, current.poses.size()))
                            .bindLevel(() -> current.poses.isEmpty() ? Level.ERROR : Level.NORMAL).layout(l -> l.flex(1)),
                    rotate));
            column.addChild(current.config);
            var partsSection = UIElement.section();
            partsSection.addChild(TextLine.translatable(LayoutStyle.AUTO, StructurePreviewScreen.PARTS).setColor(UITheme.PANEL_TEXT));
            partsSection.addChild(current.parts);
            column.addChild(partsSection);
            var project = Button.translatable(LayoutStyle.AUTO, StructurePreviewScreen.PROJECT).layout(l -> l.flex(1))
                    .disabled(() -> current.current() == null, OverviewWidget.LANG_BLOCKED).setOnClientClick(this::project);
            var build = Button.translatable(LayoutStyle.AUTO, StructurePreviewScreen.BUILD).layout(l -> l.flex(1))
                    .setVariant(UITheme.ButtonVariant.CONFIRM)
                    .disabled(() -> current.current() == null || current.config.listLayout() == null, OverviewWidget.LANG_BLOCKED)
                    .setOnClientClick(this::openBuilder);
            column.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(project, build));
        });
        view.setCard(popup, this::backToSelector);
    }

    void backToSelector() {
        choice = null;
        view.clearGhost();
        view.openSelector();
    }

    private void recompute() {
        var current = choice;
        var data = view.selected();
        if (current == null || data == null) return;
        var layout = current.config.currentLayout();
        if (layout == null) {
            current.poses = Collections.emptyList();
        } else {
            var items = StructurePlans.preview(current.definition, layout).items();
            if (current.modelBase != layout) {
                current.model = null;
                current.modelLayout = null;
            }
            current.poses = adapter.orientations(current.definition, layout, items, data, view.world());
        }
        refreshGhost();
        fillParts();
    }

    private void refreshGhost() {
        var current = choice;
        if (current == null) return;
        var pose = current.current();
        var layout = current.config.currentLayout();
        if (pose == null || layout == null) {
            view.clearGhost();
            return;
        }
        view.showGhost(OverviewDocking.worldBlocks(current.definition, layout, current.items(layout), pose));
    }

    private void fillParts() {
        var current = choice;
        if (current == null) return;
        current.parts.clearAllWidgets();
        var listed = current.config.listLayout();
        if (listed == null) return;
        var slots = PartSlots.create(StructurePlans.preview(current.definition, listed).parts());
        for (int i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);
            slot.setSelfPosition(new Position((i % PARTS_PER_ROW) * UISizes.SLOT_SIZE, (i / PARTS_PER_ROW) * UISizes.SLOT_SIZE));
            current.parts.addWidget(slot);
        }
        int rows = Math.max(1, (slots.size() + PARTS_PER_ROW - 1) / PARTS_PER_ROW);
        current.parts.setSize(new Size(PARTS_PER_ROW * UISizes.SLOT_SIZE, rows * UISizes.SLOT_SIZE));
        UIElement.markLayoutDirty(current.parts);
    }

    private void project() {
        var current = choice;
        if (current == null) return;
        var pose = current.current();
        var listed = current.config.listLayout();
        if (pose == null || listed == null) return;
        StructureProjection.show(pose.port(), current.definition, OverviewDocking.worldBlocks(current.definition, listed, current.items(listed), pose), -1);
        var player = Minecraft.getInstance().player;
        if (player != null) player.closeContainer();
    }

    private void openBuilder() {
        var current = choice;
        var player = Minecraft.getInstance().player;
        if (current == null || player == null) return;
        var pose = current.current();
        var listed = current.config.listLayout();
        if (pose == null || listed == null) return;
        var icon = current.definition.asStack();
        var model = StructurePlans.modelBuilder(icon, listed, false).build(StructureBuildFlow.inventoryStock(player));
        model.selectMinimum();
        current.model = model;
        current.modelLayout = listed;
        current.modelBase = current.config.currentLayout();
        current.modelVersion = -1;
        int anchorIndex = view.anchorIndex();
        var values = current.values.clone();
        var panel = new PatternBuilderPanel(model, icon, icon.getHoverName(), Integer.MAX_VALUE, view.panelMaxHeight(), () -> {
            int upload = BuildUpload.send(current.definition, values, StructurePlans.assign(model, listed));
            if (upload < 0) player.sendSystemMessage(Component.translatable(StructureBuild.INVALID));
            else owner.requestBuild(anchorIndex, current.definition, values, pose, upload);
            view.clearSelection();
        }, view::closeBuilder, new PatternBuilderPanel.Footer(StructureBuildFlow.BUILD_TITLE, StructureBuildFlow.BUILD, StructureBuildFlow.INCLUDE,
                StructureBuildFlow.BLOCKED, false, true, view::closeBuilder));
        view.showBuilder(panel);
    }
}

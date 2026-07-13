package com.gtocore.api.research.ui;

import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.techtree.TechTreeManager;
import com.gtocore.api.techtree.ui.TechTreeWidget;
import com.gtocore.data.recipe.research.AnalyzeData;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

public class ResearchInfoTab implements IFancyUIProvider {

    private static final int TREE_WIDTH = 176;
    private static final int CONTENT_HEIGHT = 166;
    private static final int SIDE_TAB_GAP = 10;
    private static final int SIDE_TAB_WIDTH = 166;

    private final @NotNull TechTreeManager manager;
    private final BiFunction<FancyMachineUIWidget, ResearchTreeSideTab, Widget> innerContentFactory;

    public ResearchInfoTab(@Nullable BiFunction<FancyMachineUIWidget, ResearchTreeSideTab, Widget> innerContentFactory) {
        this.manager = AnalyzeData.TechTree;
        this.innerContentFactory = innerContentFactory == null ? (uiWidget, sideTab) -> new WidgetGroup() : innerContentFactory;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var root = new WidgetGroup(0, 0, TREE_WIDTH + SIDE_TAB_GAP + SIDE_TAB_WIDTH, CONTENT_HEIGHT);
        var treeWidget = new TechTreeWidget(0, 0, TREE_WIDTH, CONTENT_HEIGHT, manager, TeamResearchSavedDtat::getOrCreateContext);
        var sideTab = new ResearchTreeSideTab(TREE_WIDTH + SIDE_TAB_GAP, 0, SIDE_TAB_WIDTH, CONTENT_HEIGHT, manager, TeamResearchSavedDtat::getOrCreateContext);
        sideTab.setInnerContent(innerContentFactory.apply(widget, sideTab));
        treeWidget.setOnNodeClicked(sideTab::toggleNode);
        root.addWidget(treeWidget);
        root.addWidget(sideTab);
        return root;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return manager.getIcon();
    }

    @Override
    public List<Component> getTabTooltips() {
        return List.of(TechTreeManager.getTreeName(manager));
    }

    @Override
    public Component getTitle() {
        return TechTreeManager.getTreeName(manager);
    }
}

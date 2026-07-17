package com.gtocore.integration.emi.research;

import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.ui.TechTreeSideTab;
import com.gtocore.api.research.techtree.ui.TechTreeWidget;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.TechTreeViewer;

import com.gtolib.GTOCore;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

public final class TechTreeEmiRecipe extends ModularEmiRecipe<WidgetGroup> {

    private static final int TREE_WIDTH = 176;
    private static final int CONTENT_HEIGHT = 166;
    private static final int SIDE_TAB_GAP = 10;
    private static final int SIDE_TAB_WIDTH = 166;

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            GTOCore.id("research"), EmiStack.of(GTOItems.STOPGAP_MEASURES.asItem())) {

        @Override
        public Component getName() {
            return Component.translatable(TechTreeViewer.NAME);
        }
    };

    private final TechTreeManager manager;

    private TechTreeEmiRecipe(TechTreeManager manager) {
        super(() -> createWidget(manager));
        this.manager = manager;
    }

    public static void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(GTOItems.STOPGAP_MEASURES.asItem()));
        TechTreeManager.getManagers().forEach(manager -> registry.addRecipe(new TechTreeEmiRecipe(manager)));
    }

    private static WidgetGroup createWidget(TechTreeManager manager) {
        var root = new WidgetGroup(0, 0, TREE_WIDTH + SIDE_TAB_GAP + SIDE_TAB_WIDTH, CONTENT_HEIGHT);
        var treeWidget = new TechTreeWidget(0, 0, TREE_WIDTH, CONTENT_HEIGHT, manager, TeamResearchSavedDtat::getOrCreateContext);
        treeWidget.setClientSideWidget();
        var sideTab = new TechTreeSideTab(TREE_WIDTH + SIDE_TAB_GAP, 0, SIDE_TAB_WIDTH, CONTENT_HEIGHT, manager, TeamResearchSavedDtat::getOrCreateContext);
        sideTab.setClientSideWidget();
        treeWidget.setOnNodeClicked(sideTab::toggleNode);
        root.addWidget(treeWidget);
        root.addWidget(sideTab);
        return root;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return GTOCore.id("research/" + manager.getId());
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}

package com.gtocore.integration.emi.research;

import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.research.techtree.TechNode;
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
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TechTreeEmiRecipe extends ModularEmiRecipe<WidgetGroup> {

    private static final int TREE_WIDTH = 176;
    private static final int CONTENT_HEIGHT = 166;
    private static final int SIDE_TAB_GAP = 10;
    private static final int SIDE_TAB_WIDTH = 166;

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            GTOCore.id("research"), EmiStack.of(GTOItems.BLUE_HALIDE_LAMP.asStack())) {

        @Override
        public Component getName() {
            return Component.translatable(TechTreeViewer.NAME);
        }
    };

    private final TechNode node;
    private final List<EmiIngredient> techNodeInput;
    private final List<EmiStack> recipeOutputs;

    private TechTreeEmiRecipe(TechNode node) {
        super(() -> createWidget(node));
        this.node = node;
        this.techNodeInput = List.of(new TechNodeEmiStack(node));
        this.recipeOutputs = EmiResearchHelper.toEmiStacks(node.getRecipePrimaryOutputs());
    }

    public static void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(GTOItems.STOPGAP_MEASURES.asItem()));
        registry.addDeferredRecipes(recipeConsumer -> TechTreeManager.getManagers()
                .forEach(manager -> manager.getAllNodes().forEachRemaining(node -> recipeConsumer.accept(new TechTreeEmiRecipe(node)))));
    }

    private static WidgetGroup createWidget(TechNode node) {
        TechTreeManager manager = node.getManager();
        var root = new WidgetGroup(0, 0, TREE_WIDTH + SIDE_TAB_GAP + SIDE_TAB_WIDTH, CONTENT_HEIGHT);
        var treeWidget = new TechTreeWidget(0, 0, TREE_WIDTH, CONTENT_HEIGHT, manager, TeamResearchSavedDtat::getOrCreateContext);
        treeWidget.setClientSideWidget();
        var sideTab = new TechTreeSideTab(TREE_WIDTH + SIDE_TAB_GAP, 0, SIDE_TAB_WIDTH, CONTENT_HEIGHT, manager, TeamResearchSavedDtat::getOrCreateContext);
        sideTab.setClientSideWidget();
        treeWidget.setOnNodeClicked(clickedNode -> {
            sideTab.toggleNode(clickedNode);
            treeWidget.setSelectedNode(sideTab.getSelectedNode());
        });
        sideTab.showNode(node);
        treeWidget.focusNode(node);
        root.addWidget(treeWidget);
        root.addWidget(sideTab);
        return root;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return techNodeInput;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return recipeOutputs;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return GTOCore.id("research/" + node.getManager().getId() + "/" + node.name);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }
}

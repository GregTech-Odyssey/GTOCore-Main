package com.gtocore.integration.emi.research;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.ui.TechNodeDetails;
import com.gtocore.api.research.techtree.ui.TechTreeBrowser;
import com.gtocore.api.research.techtree.ui.TechTreeView;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.item.TechTreeViewer;
import com.gtocore.integration.emi.EmiPageLayout;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.screen.EmiScreenManager;
import dev.vfyjxf.taffy.style.AlignItems;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class TechTreeEmiRecipe extends ModularEmiRecipe<Widget> implements EmiPageLayout.Paged {

    private static final Widget PLACEHOLDER = new Widget(0, 0, 0, 0);
    private static final int DETAILS_WIDTH = UISizes.POPUP_CONTENT_WIDTH + ScrollerView.SCROLL_BAR_SPACE;
    private static final int COMPACT_HEIGHT = 8 * UISizes.SLOT;
    private static final GTRecipeWidget.PageFrame COMPACT_FRAME = new GTRecipeWidget.PageFrame(DETAILS_WIDTH, COMPACT_HEIGHT, 0, false);

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
    private int pagedButtons = -1;
    private int pagedWidth;
    private GTRecipeWidget.PageFrame frame = COMPACT_FRAME;

    private TechTreeEmiRecipe(TechNode node) {
        super(() -> PLACEHOLDER);
        this.node = node;
        this.techNodeInput = List.of(new TechNodeEmiStack(node));
        this.recipeOutputs = EmiResearchHelper.toEmiStacks(node.getRecipePrimaryOutputs());
        this.widget = () -> createWidget(node, frame);
    }

    public static void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(ExResearchMachines.DATA_CENTER.asItem()));
        registry.addDeferredRecipes(recipeConsumer -> TechTreeManager.getManagers()
                .forEach(manager -> manager.getAllNodes().forEach(node -> recipeConsumer.accept(new TechTreeEmiRecipe(node)))));
    }

    private static Widget createWidget(TechNode node, GTRecipeWidget.PageFrame frame) {
        var widget = new Page(node, frame);
        widget.setClientSideWidget();
        return widget;
    }

    @Override
    public int getPagedWidth() {
        int width = Math.max(DETAILS_WIDTH, EmiPageLayout.minPageWidth());
        pagedWidth = width + (width & 1);
        pagedButtons = EmiPageLayout.sideButtons(this);
        return EmiPageLayout.displayWidth(pagedWidth, pagedButtons);
    }

    @Override
    public int getPagedHeight() {
        int area = EmiPageLayout.recipeAreaHeight(CATEGORY);
        return area > 0 ? area : COMPACT_HEIGHT;
    }

    @Override
    public int getDisplayWidth() {
        return DETAILS_WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return COMPACT_HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (pagedButtons >= 0 && EmiPageLayout.claimPagedGroup(widgets, EmiPageLayout.displayWidth(pagedWidth, pagedButtons))) {
            frame = new GTRecipeWidget.PageFrame(pagedWidth, widgets.getHeight(), pagedButtons, true);
        } else {
            frame = COMPACT_FRAME;
        }
        super.addWidgets(widgets);
    }

    @Override
    public List<Widget> getFlatWidgetCollection(Widget widget) {
        return Collections.emptyList();
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
        return recipeId(node);
    }

    public static ResourceLocation recipeId(TechNode node) {
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

    private static final class Page extends UIElement implements ILocalUI {

        private final GTRecipeWidget.PageFrame frame;
        private double lastMouseX = Double.NaN, lastMouseY = Double.NaN;

        private Page(TechNode node, GTRecipeWidget.PageFrame frame) {
            this.frame = frame;
            int width = frame.minWidth(), height = frame.fillHeight();
            int lowerHeight = Math.max(UISizes.CONTROL_HEIGHT, frame.notchHeight());
            layout(l -> l.column().size(width, height).gapAll(UISizes.SECTION_GAP));

            var details = new UIElement().layout(l -> l.column().gapAll(UISizes.SECTION_GAP));
            TechNodeDetails.build(details, node, new TechTreeView.Navigator(() -> Minecraft.getInstance().player, EmiResearchHelper::openTechNode), false, null);
            var scroller = new ScrollerView("techtree.emi.details", width, height - lowerHeight - UISizes.SECTION_GAP).setResizable(false);
            scroller.addScrollViewChild(details);
            addChild(scroller);

            var open = Button.translatable(frame.besideNotch(width), EmiResearchHelper.OPEN_TECH_TREE)
                    .setOnClientClick(() -> TechTreeBrowser.request(node));
            var lower = new UIElement().layout(l -> l.row().height(lowerHeight).gapAll(UISizes.SECTION_GAP).alignItems(AlignItems.END));
            lower.addChild(open);
            if (frame.sideButtons() > 0) lower.addChild(UIElement.spacer(GTRecipeWidget.PageFrame.NOTCH_WIDTH, 0));
            addChild(lower);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            GTRecipeWidget.drawPageCard(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), frame);
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!Double.isNaN(lastMouseX) && getXEIIngredientOverMouse(lastMouseX, lastMouseY) instanceof EmiIngredient ingredient) {
                if (EmiScreenManager.stackInteraction(new EmiStackInteraction(ingredient, null, true), bind -> bind.matchesKey(keyCode, scanCode))) {
                    return true;
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}

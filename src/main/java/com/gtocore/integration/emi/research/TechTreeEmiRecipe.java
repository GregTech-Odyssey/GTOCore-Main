package com.gtocore.integration.emi.research;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.ui.TechTreeView;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.item.TechTreeViewer;
import com.gtocore.integration.emi.EmiPageLayout;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.screen.EmiScreenManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TechTreeEmiRecipe extends ModularEmiRecipe<Widget> implements EmiPageLayout.Paged {

    private static final Widget PLACEHOLDER = new Widget(0, 0, 0, 0);
    private static final int PAGE_MIN_WIDTH = UISizes.WINDOW_WIDTH, PAGE_MAX_WIDTH = 26 * UISizes.SLOT;
    private static final int EMI_SIDEBARS_WIDTH = 2 * 5 * UISizes.SLOT;
    private static final int COMPACT_WIDTH = UISizes.WINDOW_WIDTH, COMPACT_HEIGHT = 8 * UISizes.SLOT;
    private static final GTRecipeWidget.PageFrame COMPACT_FRAME = new GTRecipeWidget.PageFrame(COMPACT_WIDTH, COMPACT_HEIGHT, 0, false);
    private static final int CARD_WIDTH = UISizes.POPUP_CONTENT_WIDTH + 2 * UISizes.POPUP_PADDING;
    private static final float INITIAL_SCALE = 1;

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

    /** 纯客户端界面：与研究窗口同一个科技树视图（定位到本节点）。 */
    private static Widget createWidget(TechNode node, GTRecipeWidget.PageFrame frame) {
        var widget = new Page(node, frame);
        widget.setClientSideWidget();
        return widget;
    }

    @Override
    public int getPagedWidth() {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int min = Math.max(PAGE_MIN_WIDTH, EmiPageLayout.minPageWidth());
        pagedWidth = Mth.clamp(screenWidth - EMI_SIDEBARS_WIDTH - EmiPageLayout.SCREEN_SIDES, min, PAGE_MAX_WIDTH) & ~1;
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
        return COMPACT_WIDTH;
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

    /**
     * 配方页：与研究窗口同一个科技树视图（详情卡片浮在画布右侧），一开始以本节点为中心。
     * 没有服务端的界面（{@link ILocalUI}）：同步值取本端、卡片在本端直接构建。不能拖拽缩放、不套用锁定的尺寸。
     * 另外处理 EMI 的按键（查配方 / 用途 / 收藏）：EMI 配方页只把按键转给界面、不带鼠标位置，这里记下最近一帧的鼠标位置。
     */
    private static final class Page extends UIElement implements ILocalUI {

        private final GTRecipeWidget.PageFrame frame;
        private double lastMouseX = Double.NaN, lastMouseY = Double.NaN;

        private Page(TechNode node, GTRecipeWidget.PageFrame frame) {
            this.frame = frame;
            int width = frame.minWidth(), height = frame.fillHeight();
            int canvasWidth = frame.besideNotch(width);
            layout(l -> l.row().size(width, height));
            var view = new TechTreeView(node.getManager(), "techtree.emi.canvas", canvasWidth, height)
                    .setInitialNode(node, INITIAL_SCALE, canvasWidth >= 2 * CARD_WIDTH);
            view.getCanvas().setResizable(false);
            view.setOnOtherTree(other -> EmiApi.displayRecipes(new TechNodeEmiStack(other)));
            addChild(view);
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

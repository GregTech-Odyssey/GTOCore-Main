package com.gtocore.integration.emi.research;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.ui.TechTreeView;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.item.TechTreeViewer;

import com.gtolib.GTOCore;

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
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.EmiScreenManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TechTreeEmiRecipe extends ModularEmiRecipe<Widget> {

    /// 画布的最小 / 最大尺寸：在这个范围里按屏幕与 EMI 配方页的上限尽量撑大（详情卡片浮在画布右侧，不另占宽度）
    private static final int CANVAS_MIN_WIDTH = 176, CANVAS_MAX_WIDTH = 26 * UISizes.SLOT;
    private static final int CANVAS_MIN_HEIGHT = 8 * UISizes.SLOT;
    /// EMI 配方页：配方区域上方标题与翻页占 46（RecipeTab.getVerticalRecipeSpace），配方页左右各 8 像素边
    private static final int EMI_HEADER_HEIGHT = 46, EMI_SIDE_PADDING = 16;
    /// EMI 配方页高度 = min(配置上限, 屏幕高 − 52 − 竖直边距)，与 RecipeScreen 一致
    private static final int EMI_SCREEN_MARGIN = 52;
    /// 两侧给 EMI 的物品列表与收藏栏留的宽度
    private static final int EMI_SIDEBARS_WIDTH = 2 * 5 * UISizes.SLOT;
    /// 画布尺寸：注册配方时按当时的屏幕算一次，之后每次建界面都用它（EMI 只在注册时量一次整页尺寸）
    private static int canvasWidth = CANVAS_MIN_WIDTH, canvasHeight = CANVAS_MIN_HEIGHT;
    /// 初始缩放：比 1 倍小一些，节点周围多露出几列
    private static final float INITIAL_SCALE = 0.75f;

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
        computeCanvasSize();
        registry.addCategory(CATEGORY);
        registry.addWorkstation(CATEGORY, EmiStack.of(ExResearchMachines.DATA_CENTER.asItem()));
        registry.addDeferredRecipes(recipeConsumer -> TechTreeManager.getManagers()
                .forEach(manager -> manager.getAllNodes().forEach(node -> recipeConsumer.accept(new TechTreeEmiRecipe(node)))));
    }

    /** 画布尽量撑大，但整页不超过 EMI 配方页能放下的高度（超出会被截掉）和屏幕宽度。 */
    private static void computeCanvasSize() {
        var window = Minecraft.getInstance().getWindow();
        int screenWidth = window.getGuiScaledWidth(), screenHeight = window.getGuiScaledHeight();
        int pageHeight = Math.min(EmiConfig.maximumRecipeScreenHeight, screenHeight - EMI_SCREEN_MARGIN - EmiConfig.verticalMargin);
        canvasHeight = Math.max(CANVAS_MIN_HEIGHT, pageHeight - EMI_HEADER_HEIGHT);
        canvasWidth = Mth.clamp(screenWidth - EMI_SIDEBARS_WIDTH - EMI_SIDE_PADDING, CANVAS_MIN_WIDTH, CANVAS_MAX_WIDTH);
    }

    /** 纯客户端界面：与研究窗口同一个科技树视图（定位到本节点），右侧浮着本节点的详情卡片。 */
    private static Widget createWidget(TechNode node) {
        var widget = new Page(node, canvasWidth, canvasHeight);
        widget.setClientSideWidget();
        return widget;
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
     * 配方页：与研究窗口同一个科技树视图（详情卡片浮在画布右侧），一开始以本节点为中心并打开它的详情。
     * 没有服务端的界面（{@link ILocalUI}）：同步值取本端、卡片在本端直接构建。画布尺寸固定（EMI 在注册配方时就量好了整页尺寸），不能拖拽缩放、不套用锁定的尺寸。
     * 另外处理 EMI 的按键（查配方 / 用途 / 收藏）：EMI 配方页只把按键转给界面、不带鼠标位置，这里记下最近一帧的鼠标位置。
     */
    private static final class Page extends UIElement implements ILocalUI {

        private double lastMouseX = Double.NaN, lastMouseY = Double.NaN;

        private Page(TechNode node, int width, int height) {
            layout(l -> l.column());
            var view = new TechTreeView(node.getManager(), "techtree.emi.canvas", width, height).setInitialNode(node, INITIAL_SCALE);
            view.getCanvas().setResizable(false);
            view.setOnOtherTree(other -> EmiApi.displayRecipes(new TechNodeEmiStack(other)));
            addChild(view);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
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

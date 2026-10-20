package com.gtocore.integration.emi.planner;

import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.ScreenHost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.planner.PlannerText;
import dev.emi.emi.planner.ProductionPlanner;
import dev.emi.emi.screen.ProductionPlannerGraphScreen;
import dev.emi.emi.screen.ProductionPlannerGraphScreen.Action;
import dev.vfyjxf.taffy.style.FlexWrap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/** Client-only uipro host; EMI owns graph construction, routing and saved node positions. */
@OnlyIn(Dist.CLIENT)
public final class ProductionPlannerGraphUI extends ModularUIGuiContainer {

    private final ProductionPlannerGraphScreen graph;
    private final GraphHost host;

    private ProductionPlannerGraphUI(ModularUI ui, GraphHost host, ProductionPlannerGraphScreen graph) {
        super(ui, -1);
        this.host = host;
        this.graph = graph;
        graph.initEmbedded(ui.getWidth(), ui.getHeight());
        ui.initWidgets();
    }

    public static Screen create(Screen parent) {
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();
        var graph = new ProductionPlannerGraphScreen(parent);
        var host = new GraphHost(graph);
        var ui = new ModularUI(width, height, IUIHolder.EMPTY, minecraft.player).widget(host);
        return new ProductionPlannerGraphUI(ui, host, graph);
    }

    @Override
    public void init() {
        modularUI.setSize(width, height);
        graph.initEmbedded(width, height);
        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        if (graph.keyPressed(keyCode, scanCode, modifiers)) {
            host.canvas.restoreGraphView();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        graph.onClose();
    }

    @Override
    public void removed() {
        host.canvas.releasePointer();
        graph.mouseReleased(0, 0, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {}

    private static Component text(String key, String english) {
        return Component.literal(PlannerText.tr(key, english));
    }

    private static final class GraphHost extends ScreenHost implements ILocalUI {

        private final GraphCanvas canvas;

        private GraphHost(ProductionPlannerGraphScreen graph) {
            setClientSideWidget();
            layout(l -> l.column());
            canvas = new GraphCanvas(graph);
            var title = text("graph.title", "Production Flow Graph").copy().append(" - ")
                    .append(ProductionPlanner.displayName(ProductionPlanner.getActiveIndex()));
            var header = UIElement.section().layout(l -> l.paddingAll(UISizes.PANEL_PADDING).gapAll(UISizes.SECTION_GAP));
            header.addChild(TextLine.constant(LayoutStyle.AUTO, title).bindClientColor(UITheme::panelText));
            var toolbar = UIElement.row(LayoutStyle.AUTO).layout(l -> l.flexWrap(FlexWrap.WRAP).gapAll(UISizes.GAP));
            toolbar.addChild(Button.text(UISizes.BUTTON_WIDTH, text("graph.back", "BACK"))
                    .setOnClientClick(graph::onClose).tooltips(text("graph.back_help", "Return to Production Planner")));
            toolbar.addChildren(
                    action(graph, Action.FIT, "fit", "FIT", "Fit the whole graph to the screen"),
                    action(graph, Action.FULL, "full", "FULL", "Show the complete production graph"),
                    action(graph, Action.TARGET_PATH, "target_path", "TARGET", "Highlight only flows that lead to the selected Line target"),
                    action(graph, Action.SELECTED_PATH, "selected_path", "SELECTED", "Highlight inputs and target path through the selected node"),
                    toggle(graph, Action.BYPRODUCTS, "byproducts", "BYPRODUCTS", "Show or hide non-target net output cards"),
                    toggle(graph, Action.CONTEXT, "context", "CONTEXT", "Keep unrelated branches visible while focusing a path"),
                    action(graph, Action.HIDDEN, "hidden", "HIDDEN", "Click to reveal hidden nodes; Shift+click restores all hidden nodes"),
                    action(graph, Action.AUTO_LAYOUT, "auto_layout", "AUTO LAYOUT", "Clear manual node positions and rebuild the automatic layout"));
            header.addChild(toolbar);
            var footer = UIElement.section().layout(l -> l.paddingAll(UISizes.PANEL_PADDING).gapAll(UISizes.GAP));
            footer.addChild(TextLine.of(LayoutStyle.AUTO, graph::getGraphStats).bindClientColor(UITheme::panelText));
            var legend = UIElement.row(LayoutStyle.AUTO).layout(l -> l.flexWrap(FlexWrap.WRAP).gapAll(UISizes.SECTION_GAP));
            legend.addChildren(legend("external", "External", 0xFF69A7E8), legend("internal", "Internal", 0xFF71C47B),
                    legend("output", "Output", 0xFFE7A45D), legend("recycle", "RECYCLE", 0xFFC58BE2));
            footer.addChild(legend);
            footer.addChild(TextLine.constant(LayoutStyle.AUTO, text("graph.hint",
                    "LMB node: focus path | Shift+LMB: open recipe | H: hide/show | RMB drag: move | Shift+RMB drag: free move | drag: pan | wheel: zoom | F: fit | Esc: back"))
                    .bindClientColor(UITheme::panelText));
            addChild(Form.page().layout(l -> l.flex(1).minWidth(0).gapAll(0)).addChildren(header, canvas, footer));
        }

        private Button action(ProductionPlannerGraphScreen graph, Action action, String key, String label, String help) {
            var name = text("graph." + key, label);
            int width = Math.max(UISizes.BUTTON_WIDTH, UISizes.widthFor(name.getString()) + 2 * UISizes.TEXT_PADDING);
            return Button.text(width, name).setOnClientClick(() -> {
                graph.performAction(action);
                canvas.restoreGraphView();
            })
                    .setSelected(() -> graph.isActionSelected(action)).clientDisabled(() -> !graph.isActionEnabled(action))
                    .tooltips(text("graph." + key + "_help", help));
        }

        private Button toggle(ProductionPlannerGraphScreen graph, Action action, String key, String label, String help) {
            var on = text("graph." + key + "_on", label + " ON").getString();
            var off = text("graph." + key + "_off", label + " OFF").getString();
            int width = Math.max(UISizes.widthFor(on), UISizes.widthFor(off)) + 2 * UISizes.TEXT_PADDING;
            return action(graph, action, key, label, help).bindClientText(() -> graph.isActionSelected(action) ? on : off)
                    .layout(l -> l.width(width));
        }

        private static TextLine legend(String key, String label, int color) {
            var name = text("graph." + key, label);
            return TextLine.constant(UISizes.widthFor(name.getString()), name).setColor(color);
        }
    }

    private static final class GraphCanvas extends CanvasView {

        private final ProductionPlannerGraphScreen graph;
        private final Component emptyText = text("graph.no_recipes", "No active recipes to graph");
        private Bounds seenBounds;
        @Nullable
        private CanvasRect cachedBounds;
        private float partialTicks;
        private int nodeButton = -1;

        private GraphCanvas(ProductionPlannerGraphScreen graph) {
            super("emi.production_planner.graph", ScreenHost.SERVER_WIDTH, ScreenHost.SERVER_HEIGHT);
            this.graph = graph;
            setFrame(false, 0);
            setResizable(false);
            setRememberView(false);
            setScaleRange(0.1f, 3.0f);
            setInitialView(view -> restoreGraphView());
            layout(l -> l.size(LayoutStyle.AUTO, 0).flex(1).minHeight(1));
            setScene(canvas -> canvas.addLayer(CanvasLayer.of(painter -> {
                int mouseX = Float.isNaN(painter.mouseX()) ? Integer.MIN_VALUE : (int) painter.mouseX();
                int mouseY = Float.isNaN(painter.mouseY()) ? Integer.MIN_VALUE : (int) painter.mouseY();
                graph.renderGraphContent(painter.graphics(), mouseX, mouseY, this.partialTicks);
            })));
        }

        @Override
        public boolean hasMinimap() {
            return false;
        }

        @Override
        @Nullable
        public CanvasRect contentBounds() {
            var bounds = graph.getGraphBounds();
            if (bounds != seenBounds) {
                seenBounds = bounds;
                cachedBounds = bounds.empty() ? null : CanvasRect.of(bounds.x(), bounds.y(), bounds.width(), bounds.height());
            }
            return cachedBounds;
        }

        private void restoreGraphView() {
            setView((float) graph.getViewLeft(), (float) graph.getViewTop(), graph.getViewScale(), false);
        }

        @Override
        protected void onViewChanged() {
            graph.setGraphView(scale(), offsetX(), offsetY());
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            this.partialTicks = partialTicks;
            graph.setEmbeddedViewport(viewportX(), viewportY(), viewportWidth(), viewportHeight());
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            if (contentBounds() == null) {
                UIText.drawLeft(graphics, emptyText, viewportX() + (viewportWidth() - UIText.width(emptyText)) / 2,
                        UIText.centerY(viewportY(), viewportHeight()), UITheme.panelText());
            }
        }

        @Override
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (!isInteracting() && nodeButton < 0) graph.renderGraphTooltip(graphics, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if ((button == 0 || button == 1) && isPointerOver(mouseX, mouseY) && graph.hasNodeAt(mouseX, mouseY)) {
                cancelViewAnimation();
                graph.mouseClicked(mouseX, mouseY, button);
                if (Minecraft.getInstance().screen == getGui().getModularUIGui()) {
                    nodeButton = button;
                    restoreGraphView();
                    capturePointer(button);
                }
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (nodeButton == button) return graph.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (nodeButton == button) {
                nodeButton = -1;
                releasePointer();
                graph.mouseReleased(mouseX, mouseY, button);
                return true;
            }
            return super.mouseReleased(mouseX, mouseY, button);
        }
    }
}

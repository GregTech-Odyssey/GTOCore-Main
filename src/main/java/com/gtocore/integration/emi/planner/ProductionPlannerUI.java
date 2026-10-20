package com.gtocore.integration.emi.planner;

import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.VirtualList;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.ScreenHost;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.forge.EmiForgePlatform;
import dev.emi.emi.planner.ProductionPlanner;
import dev.emi.emi.planner.ProductionPlanner.Line;
import dev.emi.emi.planner.ProductionPlanner.TargetMode;
import dev.emi.emi.screen.ProductionPlannerGraphScreen;
import dev.emi.emi.screen.ProductionPlannerScreen;
import dev.emi.emi.screen.ProductionPlannerScreen.PlannerDisplayRow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Locale;
import java.util.function.Consumer;

import static com.gtocore.integration.emi.planner.PlannerWidgets.*;

/** All production-planner pages share one local UI and the inventory that opened EMI. */
public final class ProductionPlannerUI extends ModularUIGuiContainer implements ProductionPlannerScreen.Host {

    private final PlannerHost host;

    private ProductionPlannerUI(ModularUI ui, PlannerHost host) {
        super(ui, -1);
        this.host = host;
        host.screen = this;
        host.session.bindUIHost(this);
        ui.initWidgets();
    }

    public static Screen create(AbstractContainerScreen<?> parent) {
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        var host = new PlannerHost(new ProductionPlannerScreen(parent));
        var ui = new ModularUI(window.getGuiScaledWidth(), window.getGuiScaledHeight(), IUIHolder.EMPTY, minecraft.player).widget(host);
        return new ProductionPlannerUI(ui, host);
    }

    @Override
    public ProductionPlannerScreen planner() {
        return host.session;
    }

    @Override
    public void init() {
        modularUI.setSize(width, height);
        ProductionPlanner.cancelPendingRecipeReplacement();
        host.dirty = true;
        super.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean editing = lastFocus instanceof TextFieldWidget input && input.isFocus();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (editing && super.keyPressed(keyCode, scanCode, modifiers)) return true;
            host.back();
            return true;
        }
        if (editing) return super.keyPressed(keyCode, scanCode, modifiers);
        if (hasControlDown() && keyCode == GLFW.GLFW_KEY_Z) {
            if (ProductionPlanner.undo()) host.reset();
            return true;
        }
        if (hasControlDown() && keyCode == GLFW.GLFW_KEY_F) {
            host.home();
            host.focusSearch = true;
            return true;
        }
        if (minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        host.session.onClose();
    }

    @Override
    public Component getTitle() {
        return host.session.getTitle();
    }

    @Override
    public void removed() {
        ProductionPlanner.save();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {}
}

final class PlannerHost extends ScreenHost implements ILocalUI {

    final ProductionPlannerScreen session;
    ProductionPlannerUI screen;
    final ArrayDeque<Consumer<UIElement>> pages = new ArrayDeque<>();
    final ObjectArrayList<PlannerDisplayRow> rows = new ObjectArrayList<>();
    private final ObjectArrayList<PlannerDisplayRow> availableRows = new ObjectArrayList<>();
    private final ObjectArrayList<String> searchTexts = new ObjectArrayList<>();
    String query = "";
    String status = "";
    boolean dirty = true;
    boolean focusSearch;
    private TextField search;
    private VirtualList entryList;
    private ScrollerView entryScroll;
    private int savedEntryScroll;
    private int pendingEntryScroll = -1;

    PlannerHost(ProductionPlannerScreen session) {
        this.session = session;
        setClientSideWidget();
        setBackground(UITheme.PANEL);
        layout(l -> l.column());
    }

    Line line() {
        return ProductionPlanner.getOrCreateActiveLine();
    }

    void change(Runnable mutation) {
        ProductionPlanner.beginHistoryAction();
        mutation.run();
        ProductionPlanner.endHistoryAction();
        ProductionPlanner.save();
        dirty = true;
    }

    void show(Consumer<UIElement> page) {
        pages.addLast(page);
        dirty = true;
    }

    void replace(Consumer<UIElement> page) {
        pages.removeLast();
        show(page);
    }

    void back() {
        if (pages.isEmpty()) screen.onClose();
        else {
            pages.removeLast();
            dirty = true;
        }
    }

    void home() {
        pages.clear();
        dirty = true;
    }

    void reset() {
        query = "";
        home();
    }

    @Override
    protected void onScreenResized(int width, int height) {
        dirty = true;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (pendingEntryScroll >= 0 && entryScroll != null && entryScroll.getSizeHeight() > 0) {
            entryScroll.setScrollYOffset(pendingEntryScroll);
            pendingEntryScroll = -1;
        }
        if (dirty) {
            dirty = false;
            if (entryScroll != null) savedEntryScroll = entryScroll.getScrollYOffset();
            search = null;
            entryList = null;
            entryScroll = null;
            clearAllWidgets();
            var page = Form.page().layout(l -> l.flex(1).minWidth(0).gapAll(UISizes.GAP));
            addChild(page);
            if (pages.isEmpty()) main(page);
            else pages.getLast().accept(page);
            if (!status.isBlank()) page.addChild(section().addChild(label(status)));
        }
        if (focusSearch && search != null) {
            focusSearch = false;
            search.getInput().setFocus(true);
        }
    }

    void heading(UIElement page, String title) {
        var name = label(title);
        name.layout(l -> l.flex(1));
        page.addChild(section().addChild(wrap().addChildren(button("graph.back", "BACK", this::back), name)));
    }

    private void main(UIElement page) {
        var toolbar = wrap();
        toolbar.addChildren(button(Component.literal(ProductionPlanner.displayName(ProductionPlanner.getActiveIndex())),
                () -> show(this::lines)).layout(l -> l.width(UISizes.CONTENT_WIDTH)), button(Component.literal("+"), () -> change(() -> {
                    ProductionPlanner.createLine();
                    query = "";
                })),
                button(Component.literal("×"), () -> change(() -> {
                    ProductionPlanner.removeActiveLine();
                    query = "";
                })));
        search = TextField.of(0, () -> query, value -> {
            query = value;
            refreshEntries();
            if (entryScroll != null) entryScroll.setScrollYOffset(0);
        })
                .setRightClickClear(true).setPlaceholder(text("search.placeholder", "Search recipes, machines, items…"));
        search.layout(l -> l.flex(1).minWidth(UISizes.CONTENT_WIDTH));
        toolbar.addChild(search);
        page.addChild(section().addChild(toolbar));
        var totals = session.calculate(line());
        var summary = UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP));
        summary.addChildren(PlannerReports.flows(this, "summary.external", "External Inputs", totals.externalInputs(), TargetMode.INPUT),
                PlannerReports.flows(this, "summary.internal", "Internal Flow", totals.internalFlow(), null),
                PlannerReports.flows(this, "summary.outputs", "Net Outputs", totals.netOutputs(), TargetMode.OUTPUT));
        page.addChild(summary);
        page.addChild(PlannerReports.targets(this));
        var displayRows = session.buildPlannerDisplayRows(line());
        availableRows.clear();
        searchTexts.clear();
        availableRows.ensureCapacity(displayRows.size());
        searchTexts.ensureCapacity(displayRows.size());
        for (var row : displayRows) {
            availableRows.add(row);
            searchTexts.add(session.searchText(line(), row));
        }
        refreshEntries();
        entryList = new VirtualList(1, ENTRY_HEIGHT, UISizes.GAP, rows::size, i -> entry(rows.get(i)));
        entryScroll = list("entries", entryList);
        page.addChild(entryScroll);
        pendingEntryScroll = savedEntryScroll;
        var footer = wrap();
        footer.addChildren(button(Component.literal(line().getStandardVoltageName()), () -> show(p -> PlannerEntries.voltage(this, p, null))),
                button("footer.apply_short", "APPLY", () -> change(() -> ProductionPlanner.applyLineStandardVoltageToAll(line()))),
                button("groups.title", "Groups and Links", () -> show(p -> PlannerGroups.groups(this, p, null))),
                button("footer.tools", "TOOLS", () -> show(this::tools)),
                button("report.title", "Line Report", () -> show(p -> PlannerReports.report(this, p, 0))),
                button("tools.graph", "GRAPH", () -> Minecraft.getInstance().setScreen(ProductionPlannerGraphScreen.create(screen))),
                button("ui.undo", "UNDO", () -> { if (ProductionPlanner.undo()) reset(); }).clientDisabled(() -> !ProductionPlanner.canUndo()));
        page.addChild(section().addChild(footer));
    }

    private void refreshEntries() {
        rows.clear();
        var filter = query.strip().toLowerCase(Locale.ROOT);
        rows.ensureCapacity(availableRows.size());
        for (int i = 0; i < availableRows.size(); i++) {
            if (filter.isEmpty() || searchTexts.get(i).contains(filter)) rows.add(availableRows.get(i));
        }
        if (entryList != null) entryList.refresh();
    }

    private UIElement entry(PlannerDisplayRow row) {
        var panel = section().layout(l -> l.height(ENTRY_HEIGHT));
        if (row.group() != null) {
            var group = row.group();
            panel.addChild(wrap().addChildren(button(Component.literal(group.getDisplayName(line())),
                    () -> change(() -> ProductionPlanner.setGroupCollapsed(line(), group, !group.isCollapsed()))).layout(l -> l.flex(1)),
                    button("header.cfg", "CONFIG", () -> show(p -> PlannerGroups.groups(this, p, group)))));
            return panel;
        }
        var entry = row.entry();
        var recipe = entry.getRecipe();
        var name = label(recipe == null ? entry.getRecipeId().toString() : session.recipeName(recipe));
        name.layout(l -> l.flex(1));
        var first = UIElement.centeredRow(ROW).addChildren(icon(firstOutput(recipe), b -> {
            if (recipe != null) EmiApi.displayRecipe(recipe);
        }), name,
                button("header.cfg", "CONFIG", () -> show(p -> PlannerEntries.entry(this, p, entry))),
                button(Component.literal("R"), () -> session.openRecipeReplacement(line(), entry)),
                button(Component.literal("×"), () -> change(() -> ProductionPlanner.removeEntry(line(), entry))));
        var details = new StringBuilder(entry.getMachineProfileName()).append(" | M ").append(entry.getMachines()).append(" | P ")
                .append(entry.getParallel()).append(" | ").append(entry.getVoltageName()).append(" | ").append(entry.getOcDisplayLabel())
                .append(" | ").append(session.formatDisplayRate(line().getEffectiveRate(entry))).append('/').append(ProductionPlannerScreen.displaySuffix());
        if (entry.getGroupId() > 0) details.append(" | ").append(line().getEntryGroupName(entry));
        String mode = line().isBalanceEnabled() ? "mode.balance" : entry.isAutomatic() ? "mode.auto" : "mode.manual";
        var second = UIElement.centeredRow(ROW).addChildren(label(details.toString()).layout(l -> l.flex(1)),
                button(mode, line().isBalanceEnabled() ? "BAL" : entry.isAutomatic() ? "AUTO" : "MANUAL", () -> change(() -> {
                    if (line().isBalanceEnabled()) ProductionPlanner.disableBalance(line());
                    else if (!ProductionPlanner.setAutomatic(entry, !entry.isAutomatic())) show(p -> PlannerEntries.entry(this, p, entry));
                })));
        panel.addChildren(first, second);
        return panel;
    }

    void stackClick(EmiStack stack, int button) {
        if (stack == null || stack.isEmpty()) return;
        if (button == 1) session.openStackUses(stack);
        else session.openStackRecipes(stack);
    }

    void flowClick(ProductionPlannerScreen.Flow flow, TargetMode mode, int button) {
        if (button == 2) {
            change(() -> {
                boolean balance = line().isBalanceEnabled();
                ProductionPlanner.setGroupLinkMode(line(), null, flow.stack, line().getLinkMode(null, flow.stack).nextRoot());
                if (balance) ProductionPlanner.balanceLine(line());
            });
        } else targetClick(flow.stack, mode, button);
    }

    void targetClick(EmiStack stack, TargetMode mode, int button) {
        if (button == 0 && ((mode == TargetMode.INPUT && Screen.hasControlDown()) || (mode == TargetMode.OUTPUT && !Screen.hasShiftDown() && !Screen.hasControlDown()))) {
            change(() -> ProductionPlanner.setBalanceTarget(line(), stack,
                    (stack.getKey() instanceof net.minecraft.world.level.material.Fluid ? 1000.0 : 1.0) / ProductionPlannerScreen.displayMultiplier(), mode));
        } else stackClick(stack, button);
    }

    private void lines(UIElement page) {
        heading(page, text("line", "Line").getString());
        page.addChild(list("lines", 2 * ROW, () -> ProductionPlanner.lines().size(), i -> {
            var name = TextField.of(0, () -> ProductionPlanner.displayName(i), value -> change(() -> ProductionPlanner.renameLine(i, value)))
                    .commitOnSubmit(value -> true);
            name.layout(l -> l.flex(1));
            return section().addChild(UIElement.centeredRow(ROW).addChildren(name,
                    button("footer.apply_short", "APPLY", () -> {
                        change(() -> ProductionPlanner.setActiveIndex(i));
                        query = "";
                        home();
                    })));
        }));
    }

    private void tools(UIElement page) {
        heading(page, text("footer.tools", "TOOLS").getString());
        page.addChild(section().addChild(wrap().addChildren(
                button("tools.export_line", "EXPORT JSON", () -> {
                    var json = ProductionPlanner.exportActiveLineJson();
                    if (json.isBlank()) {
                        status = text("transfer.export_failed", "Export failed").getString();
                        dirty = true;
                        return;
                    }
                    Minecraft.getInstance().keyboardHandler.setClipboard(json);
                    var path = ProductionPlanner.writeActiveLineExportFile(json);
                    status = path.isBlank() ? text("transfer.json_copied", "Line JSON copied to clipboard").getString() : path;
                    dirty = true;
                }),
                button("tools.import_line", "IMPORT JSON", () -> change(() -> {
                    var result = ProductionPlanner.importLineJson(Minecraft.getInstance().keyboardHandler.getClipboard());
                    status = result.message();
                    if (result.success()) query = "";
                })),
                button(Component.literal("/" + ProductionPlannerScreen.displaySuffix()), () -> {
                    ProductionPlannerScreen.cycleDisplayTimeUnit();
                    dirty = true;
                }),
                button("migration.title", "Recipe Changes", () -> show(p -> PlannerReports.changes(this, p))),
                button("report.title", "Line Report", () -> show(p -> PlannerReports.report(this, p, 0))),
                button("tools.graph", "GRAPH", () -> Minecraft.getInstance().setScreen(ProductionPlannerGraphScreen.create(screen))))));
        if (EmiForgePlatform.isDevelopmentEnvironment()) {
            page.addChild(button("tools.gto_audit", "GTO AUDIT", () -> show(PlannerReports.audit(this))));
        }
    }
}

package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.StructurePattern;
import com.gregtechceu.gtceu.uipro.ElementState;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uiwidgets.structure.StructureBuildFlow;
import com.gregtechceu.gtceu.uiwidgets.structure.StructurePlans;
import com.gregtechceu.gtceu.uiwidgets.structure.StructureScene;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
final class OverviewSelector {

    private static final int TILE = 52;
    private static final int THUMB = 48;
    private static final int PER_PAGE = 6;
    private static final int COLUMNS = 3;
    private static final int TILE_HOVER = 0x40FFFFFF;
    private static final int TILE_IDLE = 0x18000000;

    private final OverviewView view;
    private final OverviewAdapter adapter;
    private final Reference2BooleanOpenHashMap<MultiblockMachineDefinition> fits = new Reference2BooleanOpenHashMap<>();
    private int category;
    private int page;

    OverviewSelector(OverviewView view, OverviewAdapter adapter) {
        this.view = view;
        this.adapter = adapter;
    }

    void invalidate() {
        fits.clear();
    }

    void select() {
        fits.clear();
        category = initialCategory();
        page = 0;
        open();
    }

    private int initialCategory() {
        int count = adapter.categories().size();
        for (int i = 0; i < count; i++) {
            if (anyFits(i)) return i;
        }
        for (int i = 0; i < count; i++) {
            if (!members(i).isEmpty()) return i;
        }
        return 0;
    }

    private List<MultiblockMachineDefinition> members(int category) {
        var list = new ArrayList<MultiblockMachineDefinition>();
        var data = view.selected();
        if (data == null) return list;
        for (var member : adapter.members(data)) {
            if (member.hasStructure() && adapter.category(member) == category) list.add(member);
        }
        return list;
    }

    private boolean fits(MultiblockMachineDefinition definition) {
        if (fits.containsKey(definition)) return fits.getBoolean(definition);
        boolean result = false;
        var structure = StructurePattern.of(definition);
        var data = view.selected();
        if (structure != null && data != null) {
            var layout = structure.layout(StructureBuildFlow.remembered(definition, structure));
            if (layout != null) {
                result = !adapter.orientations(definition, layout, StructurePlans.preview(definition, layout).items(), data, view.world()).isEmpty();
            }
        }
        fits.put(definition, result);
        return result;
    }

    private boolean anyFits(int category) {
        for (var member : members(category)) {
            if (fits(member)) return true;
        }
        return false;
    }

    void open() {
        var data = view.selected();
        String unavailable = data == null ? OverviewWidget.LANG_CATEGORY_UNAVAILABLE : adapter.unavailableKey(data);
        var popup = Popup.of(() -> Component.translatable(adapter.chooseKey()), column -> {
            var categories = adapter.categories();
            if (categories.size() > 1) {
                var tabs = ButtonGroup.single(categories.size(), categories::get, () -> category, i -> {
                    category = i;
                    page = 0;
                    open();
                });
                tabs.optionDisabled(i -> members(i).isEmpty(), unavailable);
                column.addChild(tabs);
            }
            var list = members(category);
            var section = UIElement.section();
            if (list.isEmpty()) {
                section.addChild(TextLine.translatable(LayoutStyle.AUTO, OverviewWidget.LANG_NONE).setColor(UITheme.PANEL_TEXT));
            } else {
                int pages = (list.size() + PER_PAGE - 1) / PER_PAGE;
                page = Math.min(page, pages - 1);
                UIElement row = null;
                for (int i = page * PER_PAGE; i < Math.min(list.size(), (page + 1) * PER_PAGE); i++) {
                    if ((i - page * PER_PAGE) % COLUMNS == 0) {
                        row = new UIElement().layout(l -> l.row().gapAll(UISizes.GAP));
                        section.addChild(row);
                    }
                    row.addChild(new Tile(list.get(i), fits(list.get(i)), view::chooseDefinition));
                }
                if (pages > 1) {
                    var prev = Button.icon(UITheme.ARROW_LEFT).setOnClientClick(() -> {
                        page = Math.max(0, page - 1);
                        open();
                    }).disabled(() -> page == 0, null);
                    var next = Button.icon(UITheme.ARROW_RIGHT).setOnClientClick(() -> {
                        page = Math.min(pages - 1, page + 1);
                        open();
                    }).disabled(() -> page >= pages - 1, null);
                    section.addChild(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(prev,
                            TextLine.constant(LayoutStyle.AUTO, Component.literal((page + 1) + " / " + pages)).setTextAlign(Horizontal.CENTER).layout(l -> l.flex(1)), next));
                }
            }
            column.addChild(section);
        });
        view.setCard(popup, view::clearSelection);
    }

    private static final class Tile extends UIElement {

        private final MultiblockMachineDefinition definition;
        private final boolean enabled;
        private final Consumer<MultiblockMachineDefinition> onClick;

        Tile(MultiblockMachineDefinition definition, boolean enabled, Consumer<MultiblockMachineDefinition> onClick) {
            this.definition = definition;
            this.enabled = enabled;
            this.onClick = onClick;
            layout(l -> l.column().width(TILE).alignCenter().gapAll(1).paddingBottom(1));
            var structure = StructurePattern.of(definition);
            var layout = structure == null ? null : structure.layout(structure.defaultValues());
            var thumb = new StructureScene(THUMB, THUMB, false);
            if (layout != null) {
                float dx = layout.width(), dy = layout.height(), dz = layout.depth();
                thumb.show(StructurePlans.preview(definition, layout).blocks(), StructureScene.ALL_LAYERS,
                        (float) Math.sqrt(dx * dx + dy * dy + dz * dz) * OverviewView.FIT);
            }
            addChild(thumb);
            addChild(Label.of(TILE - 2, definition.asStack()::getHoverName));
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX(), y = getPositionY(), w = getSizeWidth(), h = getSizeHeight();
            graphics.fill(x, y, x + w, y + h, enabled && isMouseOverElement(mouseX, mouseY) ? TILE_HOVER : TILE_IDLE);
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            if (!enabled) UIDraw.disabledHatch(graphics, x, y, w, h);
        }

        @Override
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (gui == null || !isMouseOverElement(mouseX, mouseY)) return;
            var lines = new ArrayList<Component>();
            lines.add(definition.asStack().getHoverName());
            if (!enabled) ElementState.appendDisabledLines(lines, Component.translatable(OverviewWidget.LANG_NO_FIT));
            gui.getModularUIGui().setHoverTooltip(lines, ItemStack.EMPTY, null, null);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isMouseOverElement(mouseX, mouseY) && (button == 0 || button == 1)) {
                if (!enabled) return true;
                playButtonClickSound();
                onClick.accept(definition);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}

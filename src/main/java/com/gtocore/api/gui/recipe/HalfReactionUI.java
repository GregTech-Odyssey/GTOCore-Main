package com.gtocore.api.gui.recipe;

import com.gtocore.api.electrochemistry.HalfReaction;
import com.gtocore.api.electrochemistry.HalfReactionRecipeExtension;
import com.gtocore.api.gui.widget.IonSlot;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.RecipeInfo;
import com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeSlotLayout;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeSlots;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import com.google.common.collect.Table;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.vfyjxf.taffy.style.TaffyPosition;

import static com.gregtechceu.gtceu.api.recipe.ui.RecipeSlotLayouts.*;

/** Aseprite artwork and coordinates with UIPro's standard panel and ingredient slots. */
public final class HalfReactionUI extends GTRecipeTypeUI {

    private static final int WIDTH = 198 - 18;
    private static final int HEIGHT = 174 - 27;
    private static final int SLOT_SIZE = UISizes.SLOT_SIZE;
    private static final String TEXTURES = "gtocore:textures/gui/uipro/electrochemical/";
    private static final ResourceTexture CELL = new ResourceTexture(TEXTURES + "cell.png");
    private static final ResourceTexture INPUT_LINE = new ResourceTexture(TEXTURES + "input_line.png");
    private static final ResourceTexture ION_ARROW = new ResourceTexture(TEXTURES + "ion_arrow.png");
    private static final ResourceTexture PRODUCT_ARROW = new ResourceTexture(TEXTURES + "product_arrow.png");
    private static final ResourceTexture IONS = new ResourceTexture(TEXTURES + "ions.png");
    private static final ResourceTexture PRODUCT = new ResourceTexture(TEXTURES + "product.png");
    private static final ResourceTexture TRANSFER_CELL = new ResourceTexture(TEXTURES + "transfer_cell.png");
    private static final ResourceTexture TRANSFER_INPUT_LINE = new ResourceTexture(TEXTURES + "transfer_input_line.png");
    private static final ResourceTexture TRANSFER_ION_ARROW = new ResourceTexture(TEXTURES + "transfer_ion_arrow.png");
    private static final ResourceTexture TRANSFER_PRODUCT_ARROW = new ResourceTexture(TEXTURES + "transfer_product_arrow.png");
    private final GTRecipeTypeUI transferUI;

    public HalfReactionUI(GTRecipeType type) {
        super(type);
        setSlotLayout(RecipeSlotLayout.fitting(HalfCellCanvas::new));
        transferUI = new GTRecipeTypeUI(type).setSlotLayout(RecipeSlotLayout.fitting(IonTransferCanvas::new));
    }

    private static int count(RecipeSlots slots, IO io) {
        int count = 0;
        for (var cap : slots.capabilities(io)) count += slots.count(io, cap);
        return count;
    }

    private static int extraRows(int count, int columns, int reservedRows) {
        return Math.max(0, (count + columns - 1) / columns - reservedRows) * SLOT_SIZE;
    }

    private static int ionExtraHeight(GTRecipeDefinition recipe) {
        var reaction = recipe.data.getData(HalfReactionRecipeExtension.INSTANCE);
        return reaction == null ? 0 : extraRows(reaction.ions().size(), 2, 2);
    }

    @Override
    public Size getSlotAreaSize(GTRecipeDefinition recipe) {
        var reaction = recipe.data.getData(HalfReactionRecipeExtension.INSTANCE);
        if (reaction != null && !reaction.requiredIons().isEmpty()) {
            var size = transferUI.getSlotAreaSize(recipe);
            return new Size(size.width, size.height + transferExtraHeight(reaction));
        }
        var size = super.getSlotAreaSize(recipe);
        return new Size(size.width, size.height + ionExtraHeight(recipe));
    }

    @Override
    public WidgetGroup createRecipeTemplate(GTRecipeDefinition recipe, Table<IO, RecipeInfo, Object> storages) {
        var reaction = recipe.data.getData(HalfReactionRecipeExtension.INSTANCE);
        if (reaction != null && !reaction.requiredIons().isEmpty()) {
            var canvas = (IonTransferCanvas) transferUI.createRecipeTemplate(recipe, storages);
            canvas.addIons(reaction);
            return canvas;
        }
        var canvas = (HalfCellCanvas) super.createRecipeTemplate(recipe, storages);
        canvas.addIons(recipe);
        return canvas;
    }

    private static int producedIonHeight(HalfReaction reaction) {
        return reaction.ions().isEmpty() ? 0 : 64 + extraRows(reaction.ions().size(), 2, 1);
    }

    private static int transferExtraHeight(HalfReaction reaction) {
        return extraRows(reaction.requiredIons().size(), 2, 1) + producedIonHeight(reaction);
    }

    private static void placeSlots(UIElement canvas, RecipeSlots slots, IO io, int columns, int minimum, int x, int y) {
        // Empty positions from the design are decoration, never phantom ingredients.
        for (int i = 0; i < minimum; i++) image(canvas, UITheme.ITEM_SLOT, x + i % columns * SLOT_SIZE, y + i / columns * SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        int index = 0;
        for (var cap : slots.capabilities(io)) {
            int count = slots.count(io, cap);
            for (int i = 0; i < count; i++) {
                var slot = slots.slot(io, cap, i, null);
                place(canvas, slot, x + index % columns * SLOT_SIZE, y + index / columns * SLOT_SIZE);
                index++;
            }
        }
    }

    private static final class HalfCellCanvas extends UIElement {

        private final int inputExtra;
        private final int baseHeight;
        private final UIElement products;

        private HalfCellCanvas(RecipeSlots slots) {
            inputExtra = extraRows(count(slots, IO.IN), 4, 1);
            int outputExtra = extraRows(count(slots, IO.OUT), 2, 2);
            baseHeight = HEIGHT + inputExtra + outputExtra;
            layout(l -> l.size(WIDTH, baseHeight));
            setBackground(UITheme.PANEL);
            image(this, CELL, 12, 55 - 16 + inputExtra, 71, 86);
            image(this, INPUT_LINE, 30, 40 - 16 + inputExtra, 68, 29);
            image(this, IONS, 89, 70 - 16 + inputExtra, 20, 18);
            image(this, ION_ARROW, 91, 89 - 16 + inputExtra, 37, 10);
            placeSlots(this, slots, IO.IN, 4, 4, 28, 20 - 16);
            image(this, UITheme.FLUID_SLOT, 134, 83 - 16 + inputExtra, SLOT_SIZE, SLOT_SIZE);
            image(this, UITheme.FLUID_SLOT, 152, 83 - 16 + inputExtra, SLOT_SIZE, SLOT_SIZE);

            products = new UIElement().layout(l -> l.positionType(TaffyPosition.ABSOLUTE)
                    .left(0).top(120 - 16 + inputExtra).size(WIDTH, 40 + outputExtra));
            image(products, PRODUCT, 91, 0, 9, 24);
            image(products, PRODUCT_ARROW, 61, 10, 68, 20);
            placeSlots(products, slots, IO.OUT, 2, 2, 134, 4);
            addChild(products);
        }

        private void addIons(GTRecipeDefinition recipe) {
            int extra = ionExtraHeight(recipe);
            layout(l -> l.height(baseHeight + extra));
            products.layout(l -> l.top(120 - 16 + inputExtra + extra));
            var reaction = recipe.data.getData(HalfReactionRecipeExtension.INSTANCE);
            if (reaction == null) return;
            int index = 0;
            for (var ion : reaction.ions()) {
                var slot = new IonSlot(ion, IO.OUT);
                place(this, slot, 134 + index % 2 * SLOT_SIZE, 83 - 16 + inputExtra + index / 2 * SLOT_SIZE);
                index++;
            }
        }
    }

    private static final class IonTransferCanvas extends UIElement {

        private final int inputExtra;
        private final int baseHeight;
        private final UIElement cell;
        private final UIElement products;

        private IonTransferCanvas(RecipeSlots slots) {
            inputExtra = extraRows(count(slots, IO.IN), 4, 1);
            int outputExtra = extraRows(count(slots, IO.OUT), 2, 2);
            baseHeight = 147 + inputExtra + outputExtra;
            layout(l -> l.size(WIDTH, baseHeight));
            setBackground(UITheme.PANEL);
            placeSlots(this, slots, IO.IN, 4, 4, 28, 12 - 8);
            image(this, UITheme.FLUID_SLOT, 110 + 5, 30 - 8 + inputExtra, SLOT_SIZE, SLOT_SIZE);
            image(this, UITheme.FLUID_SLOT, 128 + 5, 30 - 8 + inputExtra, SLOT_SIZE, SLOT_SIZE);

            cell = new UIElement().layout(l -> l.positionType(TaffyPosition.ABSOLUTE)
                    .left(0).top(inputExtra - 8).size(WIDTH, 136));
            image(cell, TRANSFER_CELL, 12, 50 - 8 - 3, 76, 86);
            image(cell, TRANSFER_INPUT_LINE, 30, 35 - 8 - 3, 68, 29);
            image(cell, IONS, 93, 45 - 8 - 3, 20, 18);
            image(cell, TRANSFER_ION_ARROW, 88, 53 - 8 - 3, 59, 36);
            addChild(cell);

            products = new UIElement().layout(l -> l.positionType(TaffyPosition.ABSOLUTE)
                    .left(0).top(120 - 16 + inputExtra).size(WIDTH, 40 + outputExtra));
            image(products, PRODUCT, 91, -3, 9, 24);
            image(products, TRANSFER_PRODUCT_ARROW, 61, 10 - 3, 68, 20);
            placeSlots(products, slots, IO.OUT, 2, 2, 134, 1);
            addChild(products);
        }

        private void addIons(HalfReaction reaction) {
            int requiredExtra = extraRows(reaction.requiredIons().size(), 2, 1);
            layout(l -> l.height(baseHeight + transferExtraHeight(reaction)));
            cell.layout(l -> l.top(inputExtra + requiredExtra));
            products.layout(l -> l.top(115 - 8 + inputExtra + requiredExtra + producedIonHeight(reaction)));
            int index = 0;
            for (var ion : reaction.requiredIons()) {
                var slotX = 115 + index % 2 * SLOT_SIZE;
                var slotY = 30 - 8 + inputExtra + index / 2 * SLOT_SIZE;
                image(this, UITheme.FLUID_SLOT, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
                place(this, new IonSlot(ion, IO.IN), slotX, slotY);
                index++;
            }
            if (reaction.ions().isEmpty()) return;
            int y = 138 + inputExtra + requiredExtra;
            image(this, IONS, 88, y - 6 - 3, 20, 18);
            image(this, ION_ARROW, 90, y + 19 - 6 - 3, 37, 10);
            index = 0;
            for (var ion : reaction.ions()) {
                place(this, new IonSlot(ion, IO.OUT), 134 + 6 + index % 2 * SLOT_SIZE, y + 13 + index / 2 * SLOT_SIZE);
                index++;
            }
        }
    }
}

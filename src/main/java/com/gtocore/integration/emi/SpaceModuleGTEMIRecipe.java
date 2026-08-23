package com.gtocore.integration.emi;

import com.gtocore.api.data.RocketFuels;

import com.gtolib.api.recipe.ContentBuilder;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SpaceModuleGTEMIRecipe extends GTEMIRecipe {

    private final List<GTRecipeDefinition> variants;
    private GTRecipeDefinition selectedRecipe;
    private final EmiIngredient selectedDrone;
    private final EmiIngredient selectedFuel;
    private final List<EmiIngredient> possibleInputs;

    public static void addGroupedRecipes(Iterable<GTRecipeDefinition> recipes, EmiRecipeCategory category,
                                         Consumer<SpaceModuleGTEMIRecipe> consumer) {
        var grouped = new LinkedHashMap<RecipeOutputKey, ArrayList<GTRecipeDefinition>>();
        for (GTRecipeDefinition recipe : recipes) {
            grouped.computeIfAbsent(RecipeOutputKey.of(recipe), key -> new ArrayList<>()).add(recipe);
        }
        grouped.values().forEach(group -> consumer.accept(new SpaceModuleGTEMIRecipe(group, category)));
    }

    public SpaceModuleGTEMIRecipe(List<GTRecipeDefinition> variants, EmiRecipeCategory category) {
        super(variants.getFirst(), category);
        this.variants = List.copyOf(variants);
        this.selectedRecipe = variants.getFirst();
        this.selectedDrone = new SelectedIngredient(() -> getDrone(selectedRecipe));
        this.selectedFuel = new SelectedIngredient(() -> getFuel(selectedRecipe));
        this.inputs = List.of(selectedDrone, selectedFuel);
        this.possibleInputs = createPossibleInputs();
        this.catalysts = List.of();
        this.widget = this::createVariantWidget;
        initRecipeOutputs();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return possibleInputs;
    }

    private List<EmiIngredient> createPossibleInputs() {
        var drones = new ArrayList<EmiIngredient>(variants.size());
        var fuels = new ArrayList<EmiIngredient>(variants.size());
        for (GTRecipeDefinition variant : variants) {
            addIfAbsent(drones, getDrone(variant));
            addIfAbsent(fuels, getFuel(variant).copy().setAmount(10_000));
        }
        return List.of(EmiIngredient.of(drones), EmiIngredient.of(fuels));
    }

    private static void addIfAbsent(ArrayList<EmiIngredient> ingredients, EmiIngredient candidate) {
        for (EmiIngredient ingredient : ingredients) {
            if (sameIngredient(ingredient, candidate)) return;
        }
        ingredients.add(candidate);
    }

    @Override
    protected EmiIngredient resolveSlotIngredient(IRecipeIngredientSlot slot, EmiIngredient ingredient) {
        if (sameIngredient(ingredient, getDrone(selectedRecipe))) return selectedDrone;
        if (sameIngredient(ingredient, getFuel(selectedRecipe))) return selectedFuel;
        return ingredient;
    }

    @Override
    protected long getTankCapacity(IRecipeIngredientSlot slot, EmiIngredient ingredient) {
        return ingredient == selectedFuel ? 1 : super.getTankCapacity(slot, ingredient);
    }

    private void initRecipeOutputs() {
        recipe.itemOutputs.forEach(content -> {
            if (content.inner instanceof ItemIngredient ingredient) {
                float chance = (float) content.chance / ContentBuilder.maxChance;
                outputs.add((EmiStack) getEmiIngredient(ingredient, false).setChance(chance));
            }
        });
        recipe.fluidOutputs.forEach(content -> {
            if (content.inner instanceof FluidIngredient ingredient && ingredient.getFluid() != null) {
                float chance = (float) content.chance / ContentBuilder.maxChance;
                outputs.add(EmiStack.of(ingredient.getFluid(), ingredient.nbt, ingredient.amount).setChance(chance));
            }
        });
    }

    private Widget createVariantWidget() {
        var root = new WidgetGroup(0, 0, width, height);
        root.addWidget(createSelectedRecipeWidget(root, false));
        return root;
    }

    private GTRecipeWidget createSelectedRecipeWidget(WidgetGroup root, boolean clearSlots) {
        var recipeWidget = new GTRecipeWidget(selectedRecipe);
        addVariantButtons(recipeWidget, root, true);
        addVariantButtons(recipeWidget, root, false);
        if (clearSlots) clearIngredientSlots(recipeWidget);
        return recipeWidget;
    }

    private void addVariantButtons(GTRecipeWidget recipeWidget, WidgetGroup root, boolean drone) {
        if (!hasAlternative(drone)) return;
        Widget target = findIngredientSlot(recipeWidget, drone ? getDrone(selectedRecipe) : getFuel(selectedRecipe));
        if (target == null) return;
        int x = target.getPosition().x - recipeWidget.getPosition().x - 9;
        int y = target.getPosition().y - recipeWidget.getPosition().y;
        recipeWidget.addWidget(new ButtonWidget(x, y, 8, 8, click -> selectVariant(root, drone, -1))
                .setButtonTexture(GuiTextures.BUTTON_LEFT.copy().rotate(45))
                .setHoverTooltips(drone ? "gtocore.emi.space_elevator.prev_drone" : "gtocore.emi.space_elevator.prev_fuel"));
        recipeWidget.addWidget(new ButtonWidget(x, y + 9, 8, 8, click -> selectVariant(root, drone, 1))
                .setButtonTexture(GuiTextures.BUTTON_RIGHT.copy().rotate(45))
                .setHoverTooltips(drone ? "gtocore.emi.space_elevator.next_drone" : "gtocore.emi.space_elevator.next_fuel"));
    }

    private void selectVariant(WidgetGroup root, boolean drone, int direction) {
        var matching = new ArrayList<GTRecipeDefinition>(variants.size());
        EmiIngredient fixed;
        fixed = drone ? getFuel(selectedRecipe) : getDrone(selectedRecipe);
        for (GTRecipeDefinition variant : variants) {
            EmiIngredient ingredient = drone ? getFuel(variant) : getDrone(variant);
            if (sameIngredient(ingredient, fixed)) matching.add(variant);
        }
        if (matching.size() < 2) return;
        int current = matching.indexOf(selectedRecipe);
        selectedRecipe = matching.get(Math.floorMod(current + direction, matching.size()));
        root.clearAllWidgets();
        root.addWidget(createSelectedRecipeWidget(root, true));
        root.detectAndSendChanges();
        root.updateScreen();
    }

    private boolean hasAlternative(boolean drone) {
        EmiIngredient fixed;
        fixed = drone ? getFuel(selectedRecipe) : getDrone(selectedRecipe);
        int count = 0;
        for (GTRecipeDefinition variant : variants) {
            EmiIngredient ingredient = drone ? getFuel(variant) : getDrone(variant);
            if (sameIngredient(ingredient, fixed) && ++count > 1) return true;
        }
        return false;
    }

    private static Widget findIngredientSlot(GTRecipeWidget widget, EmiIngredient ingredient) {
        for (Widget child : widget.getContainedWidgets(true)) {
            if (child instanceof IRecipeIngredientSlot slot) {
                var candidate = EmiIngredient.of((List<? extends EmiIngredient>) (List<?>) slot.getXEIIngredients());
                if (sameIngredient(candidate, ingredient)) return child;
            }
        }
        return null;
    }

    private static void clearIngredientSlots(GTRecipeWidget widget) {
        for (Widget child : widget.getContainedWidgets(true)) {
            if (child instanceof com.gregtechceu.gtceu.api.gui.widget.SlotWidget slot) {
                slot.setHandlerSlot(ICustomItemStackHandler.EMPTY, 0);
                slot.setDrawHoverOverlay(false).setDrawHoverTips(false);
            } else if (child instanceof com.gregtechceu.gtceu.api.gui.widget.TankWidget tank) {
                tank.setFluidTank(EmptyFluidHandler.INSTANCE);
                tank.setDrawHoverOverlay(false).setDrawHoverTips(false);
            }
        }
    }

    private static EmiIngredient getDrone(GTRecipeDefinition recipe) {
        for (var content : recipe.itemInputs) {
            if (!(content.inner instanceof ItemIngredient ingredient)) continue;
            for (Item drone : RocketFuels.drones) {
                if (ingredient.testItem(drone)) return getEmiIngredient(ingredient, true);
            }
        }
        return EmiStack.EMPTY;
    }

    private static EmiIngredient getFuel(GTRecipeDefinition recipe) {
        for (var content : recipe.fluidInputs) {
            if (content.inner instanceof FluidIngredient ingredient && ingredient.getFluid() != null) {
                return EmiStack.of(ingredient.getFluid(), ingredient.nbt, ingredient.amount);
            }
        }
        return EmiStack.EMPTY;
    }

    private static boolean sameIngredient(EmiIngredient first, EmiIngredient second) {
        return first.getAmount() == second.getAmount() && EmiIngredient.areEqual(first, second);
    }

    private record RecipeContentKey(Object ingredient, long amount, int chance, int tierChanceBoost) {

        private static RecipeContentKey of(Content<?> content) {
            return new RecipeContentKey(content.inner, content.amount, content.chance, content.tierChanceBoost);
        }
    }

    private record RecipeOutputKey(List<RecipeContentKey> items, List<RecipeContentKey> fluids) {

        private static RecipeOutputKey of(GTRecipeDefinition recipe) {
            var items = new ArrayList<RecipeContentKey>(recipe.itemOutputs.size());
            var fluids = new ArrayList<RecipeContentKey>(recipe.fluidOutputs.size());
            recipe.itemOutputs.forEach(content -> items.add(RecipeContentKey.of(content)));
            recipe.fluidOutputs.forEach(content -> fluids.add(RecipeContentKey.of(content)));
            return new RecipeOutputKey(List.copyOf(items), List.copyOf(fluids));
        }
    }

    private record SelectedIngredient(Supplier<EmiIngredient> supplier) implements EmiIngredient {

        private EmiIngredient current() {
            return supplier.get();
        }

        @Override
        public List<EmiStack> getEmiStacks() {
            return current().getEmiStacks();
        }

        @Override
        public EmiIngredient copy() {
            return current().copy();
        }

        @Override
        public long getAmount() {
            return current().getAmount();
        }

        @Override
        public EmiIngredient setAmount(long amount) {
            return current().copy().setAmount(amount);
        }

        @Override
        public float getChance() {
            return current().getChance();
        }

        @Override
        public EmiIngredient setChance(float chance) {
            return current().copy().setChance(chance);
        }

        @Override
        public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
            current().render(graphics, x, y, delta, flags);
        }

        @Override
        public List<ClientTooltipComponent> getTooltip() {
            return current().getTooltip();
        }
    }
}

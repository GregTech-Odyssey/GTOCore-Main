package com.gtocore.integration.emi;

import com.gtocore.api.data.RocketFuels;

import com.gtolib.api.recipe.ContentBuilder;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeSlotLayouts;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

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

    /** 所有方案里最大的页面尺寸：切换方案时页面大小不变。 */
    @Override
    protected Size measure(GTRecipeWidget.PageFrame frame) {
        int width = 0, height = 0;
        for (var variant : variants) {
            var size = GTRecipeWidget.getPageSize(variant, frame);
            width = Math.max(width, size.width);
            height = Math.max(height, size.height);
        }
        return new Size(width, height);
    }

    /** 建页：外框在这里定下（悬停预览等会改写 {@link #frame}），各方案都按最大尺寸建，切换时沿用同一外框。 */
    private Widget createVariantWidget() {
        var size = measure(frame);
        var pageFrame = new GTRecipeWidget.PageFrame(size.width, size.height, frame.sideButtons(), frame.card());
        var root = new WidgetGroup(0, 0, size.width, size.height);
        root.addWidget(createSelectedRecipeWidget(root, pageFrame, false));
        return root;
    }

    private GTRecipeWidget createSelectedRecipeWidget(WidgetGroup root, GTRecipeWidget.PageFrame pageFrame, boolean clearSlots) {
        var recipeWidget = new GTRecipeWidget(selectedRecipe, pageFrame);
        var occupied = occupiedAreas(recipeWidget);
        addVariantButtons(recipeWidget, root, pageFrame, true, occupied);
        addVariantButtons(recipeWidget, root, pageFrame, false, occupied);
        if (clearSlots) clearIngredientSlots(recipeWidget);
        return recipeWidget;
    }

    /**
     * 目标槽的切换控件：槽上方一个 ▲ 按钮换高一级的方案、下方一个 ▼ 按钮换低一级的方案（到头时禁用），鼠标在槽上滚轮同样上下换
     * （EMI 配方界面的滚轮经 {@link EmiRecipeWheel} 交给配方页）。上下放不下时改放左（▼）右（▲）；
     * 位置不出舞台（槽位区的底板）、不压住槽、进度条或其他按钮（配方页是 flex 布局，按坐标摆放要用绝对定位）。
     */
    private void addVariantButtons(GTRecipeWidget recipeWidget, WidgetGroup root, GTRecipeWidget.PageFrame pageFrame, boolean drone, List<Rect2i> occupied) {
        if (!hasAlternative(drone)) return;
        Widget target = findIngredientSlot(recipeWidget, drone ? getDrone(selectedRecipe) : getFuel(selectedRecipe));
        if (target == null) return;
        int slotX = target.getPosition().x - recipeWidget.getPosition().x;
        int slotY = target.getPosition().y - recipeWidget.getPosition().y;
        int size = Button.ICON_SIZE, gap = UISizes.GAP, centerY = slotY + (UISizes.SLOT - size) / 2, centerX = slotX + (UISizes.SLOT - size) / 2;
        // 每组候选：{ 高一级按钮的位置, 低一级按钮的位置 }
        int[][][] candidates = {
                { { centerX, slotY - size - gap }, { centerX, slotY + UISizes.SLOT + gap } },
                { { slotX + UISizes.SLOT + gap, centerY }, { slotX - size - gap, centerY } } };
        var stage = stageArea(recipeWidget);
        for (int[][] pair : candidates) {
            var up = new Rect2i(pair[0][0], pair[0][1], size, size);
            var down = new Rect2i(pair[1][0], pair[1][1], size, size);
            if (!fitsFree(up, stage, occupied) || !fitsFree(down, stage, occupied)) continue;
            occupied.add(up);
            occupied.add(down);
            RecipeSlotLayouts.place(recipeWidget, variantButton(root, pageFrame, drone, 1), up.getX(), up.getY());
            RecipeSlotLayouts.place(recipeWidget, variantButton(root, pageFrame, drone, -1), down.getX(), down.getY());
            break;
        }
        RecipeSlotLayouts.place(recipeWidget, new VariantWheel(delta -> selectVariant(root, pageFrame, drone, delta > 0 ? 1 : -1)), slotX, slotY);
    }

    /** 高一级（{@code direction} = 1，▲）或低一级（-1，▼）的切换按钮，到头时禁用。 */
    private Button variantButton(WidgetGroup root, GTRecipeWidget.PageFrame pageFrame, boolean drone, int direction) {
        boolean up = direction > 0;
        var button = Button.icon(up ? UITheme.ARROW_UP : UITheme.ARROW_DOWN);
        button.setOnClientClick(click -> selectVariant(root, pageFrame, drone, direction));
        button.disabled(() -> neighbor(drone, direction) == null, up ? "gtceu.uipro.stepper.at_max" : "gtceu.uipro.stepper.at_min");
        String key = drone ? (up ? "gtocore.emi.space_elevator.next_drone" : "gtocore.emi.space_elevator.prev_drone") :
                (up ? "gtocore.emi.space_elevator.next_fuel" : "gtocore.emi.space_elevator.prev_fuel");
        button.setHoverTooltips(Component.translatable(key));
        return button;
    }

    /** 盖在切换槽上的透明区域：只接滚轮（向上高一级、向下低一级），点击和提示照常落到下面的槽。 */
    private static final class VariantWheel extends Widget {

        private final java.util.function.DoubleConsumer onWheel;

        private VariantWheel(java.util.function.DoubleConsumer onWheel) {
            super(0, 0, UISizes.SLOT, UISizes.SLOT);
            this.onWheel = onWheel;
        }

        @Override
        public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
            if (!isMouseOverElement(mouseX, mouseY) || wheelDelta == 0) return false;
            onWheel.accept(wheelDelta);
            return true;
        }
    }

    /** 舞台（配方页第一个子元素：槽位区的底板）的区域（相对配方页）。 */
    private static Rect2i stageArea(GTRecipeWidget widget) {
        return relative(widget, widget.widgets.getFirst());
    }

    /** 舞台里已经被占住的区域（相对配方页）：槽、进度条等所有不是容器的控件。 */
    private static List<Rect2i> occupiedAreas(GTRecipeWidget widget) {
        var areas = new ArrayList<Rect2i>();
        var stage = widget.widgets.getFirst();
        if (!(stage instanceof WidgetGroup group)) return areas;
        for (Widget child : group.getContainedWidgets(true)) {
            if (!(child instanceof WidgetGroup) || child instanceof IRecipeIngredientSlot) areas.add(relative(widget, child));
        }
        return areas;
    }

    private static Rect2i relative(GTRecipeWidget widget, Widget child) {
        return new Rect2i(child.getPosition().x - widget.getPosition().x, child.getPosition().y - widget.getPosition().y, child.getSize().width, child.getSize().height);
    }

    private static boolean fitsFree(Rect2i rect, Rect2i bounds, List<Rect2i> occupied) {
        if (rect.getX() < bounds.getX() || rect.getY() < bounds.getY() || rect.getX() + rect.getWidth() > bounds.getX() + bounds.getWidth() ||
                rect.getY() + rect.getHeight() > bounds.getY() + bounds.getHeight())
            return false;
        for (var other : occupied) {
            if (rect.getX() < other.getX() + other.getWidth() && other.getX() < rect.getX() + rect.getWidth() &&
                    rect.getY() < other.getY() + other.getHeight() && other.getY() < rect.getY() + rect.getHeight())
                return false;
        }
        return true;
    }

    /** 换到高一级（{@code direction} = 1）或低一级（-1）的方案；已经到头时不变。 */
    private void selectVariant(WidgetGroup root, GTRecipeWidget.PageFrame pageFrame, boolean drone, int direction) {
        var next = neighbor(drone, direction);
        if (next == null) return;
        selectedRecipe = next;
        root.clearAllWidgets();
        root.addWidget(createSelectedRecipeWidget(root, pageFrame, true));
        root.detectAndSendChanges();
        root.updateScreen();
    }

    /** 另一种输入不变时，当前方案的相邻方案（按方案顺序，即等级高低）；没有时为 null。 */
    @Nullable
    private GTRecipeDefinition neighbor(boolean drone, int direction) {
        var matching = new ArrayList<GTRecipeDefinition>(variants.size());
        EmiIngredient fixed = drone ? getFuel(selectedRecipe) : getDrone(selectedRecipe);
        for (GTRecipeDefinition variant : variants) {
            EmiIngredient ingredient = drone ? getFuel(variant) : getDrone(variant);
            if (sameIngredient(ingredient, fixed)) matching.add(variant);
        }
        int index = matching.indexOf(selectedRecipe) + direction;
        return index >= 0 && index < matching.size() ? matching.get(index) : null;
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

package com.gtocore.integration.emi;

import com.gtocore.api.research.recipe.ResearchPointsRecipeExtion;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;

import com.gtolib.api.recipe.ContentBuilder;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import com.gto.datasynclib.util.ItemStackHashStrategy;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.emi.ModularForegroundRenderWidget;
import com.lowdragmc.lowdraglib.emi.ModularWrapperWidget;
import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntSupplier;

/**
 * GT 配方在 EMI 里的一页。启动时要为全部配方建对象，所以构造时不建界面、也不算尺寸：
 * 配方页（{@link GTRecipeWidget}）在显示时才建，尺寸在 EMI 第一次排版这个类别时按配方算一次
 * （{@link GTRecipeWidget#getPageSize}，不建控件）。
 * <p>
 * 其余场合（收藏栏悬停预览、截图、生产规划图……）没有右侧按钮、也不分页，{@link #getDisplayWidth}/{@link #getDisplayHeight}
 * 报按内容的紧凑尺寸，页面不挖缺口、不画卡片，保留 EMI 的底框。
 * <p>
 * 右侧按钮数取决于当前打开的界面（能否"填充配方"）和 EMI 设置（可在游戏里改），每次排版重新数，尺寸按按钮数分别缓存。
 */
public class GTEMIRecipe extends ModularEmiRecipe<Widget> implements EmiPageLayout.Paged {

    /// 交给父类构造器的占位控件：父类构造时只读它的尺寸，真正的尺寸见 getDisplayWidth / getDisplayHeight
    private static final Widget PLACEHOLDER = new Widget(0, 0, 0, 0);

    private final EmiRecipeCategory category;
    protected final GTRecipeDefinition recipe;
    public final IntSupplier displayPriority;
    private final Size[] pagedSizes = new Size[6];
    @Nullable
    private Size compactSize;
    /// 最近一次翻页排版时的右侧按钮数，-1 为还没排过（addWidgets 据此认出翻页排版的页面）
    private int pagedButtons = -1;
    /// 正在建的页面的外框（addWidgets 里设好再建页）
    protected GTRecipeWidget.PageFrame frame = GTRecipeWidget.PageFrame.COMPACT;

    public GTEMIRecipe(GTRecipeDefinition recipe, EmiRecipeCategory category) {
        super(() -> PLACEHOLDER);
        this.recipe = recipe;
        this.category = category;
        this.displayPriority = () -> recipe.priority;
        this.inputs = null;
        this.widget = () -> new GTRecipeWidget(recipe, frame);
    }

    /** 翻页排版时的页面外框：至少与 EMI 界面最小宽度对齐，填满 {@code fillHeight}，右下角给 {@code buttons} 个 EMI 按钮留缺口，画卡片。 */
    protected GTRecipeWidget.PageFrame pagedFrame(int fillHeight, int buttons) {
        return new GTRecipeWidget.PageFrame(EmiPageLayout.minPageWidth(), fillHeight, buttons, true);
    }

    protected Size pagedSize(int buttons) {
        if (buttons >= pagedSizes.length) return measure(pagedFrame(0, buttons));
        var size = pagedSizes[buttons];
        if (size == null) pagedSizes[buttons] = size = measure(pagedFrame(0, buttons));
        return size;
    }

    /** 紧凑尺寸：按内容大小、不挖缺口、不画卡片。 */
    protected Size compactSize() {
        var size = compactSize;
        if (size == null) compactSize = size = measure(GTRecipeWidget.PageFrame.COMPACT);
        return size;
    }

    /** 按外框量页面尺寸（不建控件）。一个 EMI 配方显示多个配方方案时取最大的。 */
    protected Size measure(GTRecipeWidget.PageFrame frame) {
        return GTRecipeWidget.getPageSize(recipe, frame);
    }

    /** 翻页排版时报给 EMI 的显示宽度：页宽减去伸进缺口的按钮列。同时记下这次的按钮数。 */
    @Override
    public int getPagedWidth() {
        int buttons = EmiPageLayout.sideButtons(this);
        pagedButtons = buttons;
        return pagedDisplayWidth(buttons);
    }

    private int pagedDisplayWidth(int buttons) {
        return EmiPageLayout.displayWidth(pagedSize(buttons).width, buttons);
    }

    @Override
    public int getPagedHeight() {
        return pagedSize(Math.max(pagedButtons, 0)).height;
    }

    @Override
    public int getDisplayWidth() {
        return compactSize().width;
    }

    @Override
    public int getDisplayHeight() {
        return compactSize().height;
    }

    @Override
    public List<EmiStack> getOutputs() {
        if (inputs == null) initRecipe();
        return outputs;
    }

    public int getTier() {
        return recipe.tier;
    }

    public GTRecipeType getRecipeType() {
        return recipe.recipeType;
    }

    @SuppressWarnings("all")
    protected static EmiIngredient getEmiIngredient(ItemIngredient ingredient, boolean input) {
        Ingredient inner = ingredient.inner;
        ItemStack[] itemStacks = inner.getItems();
        if (itemStacks.length == 0) return EmiStack.EMPTY;
        ItemStack itemStack = itemStacks[0];
        long amount = ingredient.amount;
        for (Ingredient.Value value : inner.values) {
            if (input && value instanceof Ingredient.TagValue tagValue) {
                return new TagEmiIngredient(tagValue.tag, amount);
            } else {
                Item item = itemStack.getItem();
                CompoundTag nbt = itemStack.getTag();
                if (nbt == null || nbt.isEmpty()) {
                    return new ItemEmiStack(item, null, amount);
                }
                var stack = new ItemEmiStack(item, nbt, amount);
                stack.comparison(EmiPort.compareStrict());
                return stack;
            }
        }
        return EmiStack.EMPTY;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        if (inputs == null) initRecipe();
        return inputs;
    }

    @Override
    public List<Widget> getFlatWidgetCollection(Widget widget) {
        return Collections.emptyList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return recipe.id;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        frame = frameFor(widgets);
        var widget = this.widget.get();
        var modular = new ModularWrapper<>(widget);
        modular.setRecipeWidget(0, 0);

        synchronized (CACHE_OPENED) {
            CACHE_OPENED.add(modular);
        }
        List<Widget> widgetList = new ArrayList<>();
        if (widget instanceof WidgetGroup group) {
            for (Widget w : group.widgets) {
                widgetList.add(w);
                if (w instanceof WidgetGroup group1) {
                    widgetList.addAll(group1.getContainedWidgets(true));
                }
            }
        } else {
            widgetList.add(widget);
        }
        List<dev.emi.emi.api.widget.Widget> slots = new ArrayList<>();
        for (com.lowdragmc.lowdraglib.gui.widget.Widget w : widgetList) {
            if (w instanceof IRecipeIngredientSlot slot) {
                // 滚动区里的槽交给滚动区自己画和响应（EMI 槽不会跟着滚动、也不会被裁剪）
                if (GTEmiRecipe.isInsideScroller(w)) continue;
                var io = slot.getIngredientIO();
                if (io != null && io != IngredientIO.RENDER_ONLY) {
                    // noinspection unchecked
                    var ingredients = EmiIngredient
                            .of((List<? extends EmiIngredient>) (List<?>) slot.getXEIIngredients());
                    ingredients = resolveSlotIngredient(slot, ingredients);

                    SlotWidget slotWidget = null;
                    // Clear the LDLib slots & add EMI slots based on them.
                    if (slot instanceof com.gregtechceu.gtceu.api.gui.widget.SlotWidget slotW) {
                        slotW.setHandlerSlot(ICustomItemStackHandler.EMPTY, 0);
                        slotW.setDrawHoverOverlay(false).setDrawHoverTips(false);
                    } else if (slot instanceof com.gregtechceu.gtceu.api.gui.widget.TankWidget tankW) {
                        tankW.setFluidTank(EmptyFluidHandler.INSTANCE);
                        tankW.setDrawHoverOverlay(false).setDrawHoverTips(false);
                        long capacity = getTankCapacity(slot, ingredients);
                        slotWidget = new TankWidget(ingredients, w.getPosition().x, w.getPosition().y,
                                w.getSize().width, w.getSize().height, capacity);
                    }
                    if (slotWidget == null) {
                        slotWidget = new SlotWidget(ingredients, w.getPosition().x, w.getPosition().y);
                    }

                    slotWidget
                            .customBackground(null, w.getPosition().x, w.getPosition().y, w.getSize().width,
                                    w.getSize().height)
                            .drawBack(false);
                    if (io == IngredientIO.CATALYST) {
                        slotWidget.catalyst(true);
                    } else if (io == IngredientIO.OUTPUT) {
                        slotWidget.recipeContext(this);
                    }
                    for (Component component : w.getTooltipTexts()) {
                        slotWidget.appendTooltip(component);
                    }
                    slots.add(slotWidget);
                }
            }
        }
        widgets.add(new ModularWrapperWidget(modular, slots));
        slots.forEach(widgets::add);
        widgets.add(new ModularForegroundRenderWidget(modular));
    }

    protected GTRecipeWidget.PageFrame frameFor(WidgetHolder widgets) {
        if (pagedButtons >= 0 && EmiPageLayout.claimPagedGroup(widgets, pagedDisplayWidth(pagedButtons))) {
            return pagedFrame(widgets.getHeight(), pagedButtons);
        }
        return GTRecipeWidget.PageFrame.COMPACT;
    }

    protected EmiIngredient resolveSlotIngredient(IRecipeIngredientSlot slot, EmiIngredient ingredient) {
        return ingredient;
    }

    protected long getTankCapacity(IRecipeIngredientSlot slot, EmiIngredient ingredient) {
        return Math.max(1, ingredient.getAmount());
    }

    private void initRecipe() {
        inputs = new ArrayList<>();
        recipe.itemInputs.forEach(c -> {
            if (c.inner instanceof ItemIngredient ingredient) {
                float chance = (float) c.chance / ContentBuilder.maxChance;
                EmiIngredient emiIngredient = getEmiIngredient(ingredient, true).setChance(chance);
                if (chance > 0) {
                    inputs.add(emiIngredient);
                } else {
                    catalysts.add(emiIngredient);
                }
            }
        });
        recipe.fluidInputs.forEach(c -> {
            if (c.inner instanceof FluidIngredient ingredient) {
                var fluid = ingredient.getFluid();
                if (fluid != null) {
                    float chance = (float) c.chance / ContentBuilder.maxChance;
                    EmiIngredient emiIngredient = EmiStack.of(fluid, ingredient.nbt, ingredient.amount).setChance(chance);
                    if (chance > 0) {
                        inputs.add(emiIngredient);
                    } else {
                        catalysts.add(emiIngredient);
                    }
                }
            }
        });
        recipe.itemOutputs.forEach(c -> {
            if (c.inner instanceof ItemIngredient ingredient) {
                float chance = (float) c.chance / ContentBuilder.maxChance;
                outputs.add((EmiStack) getEmiIngredient(ingredient, false).setChance(chance));
            }
        });
        recipe.fluidOutputs.forEach(c -> {
            if (c.inner instanceof FluidIngredient ingredient) {
                float chance = (float) c.chance / ContentBuilder.maxChance;
                var fluid = ingredient.getFluid();
                if (fluid != null) {
                    outputs.add(EmiStack.of(fluid, ingredient.nbt, ingredient.amount).setChance(chance));
                }
            }
        });
        if (recipe.recipeType.isScanner()) {
            ResearchManager.ResearchItem researchData = null;
            for (var content : recipe.itemOutputs) {
                var stack = content.inner.getInnerItemStack();
                if (stack.isEmpty()) continue;
                researchData = ResearchManager.readResearchId(stack);
                if (researchData != null) break;
            }
            if (researchData != null) {
                var possibleRecipes = researchData.recipeType().getDataStickEntry(researchData.researchId());
                Set<ItemStack> cache = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.ITEM);
                if (possibleRecipes != null) {
                    for (var r : possibleRecipes) {
                        var outputs = r.itemOutputs;
                        if (outputs.isEmpty()) continue;
                        var outputContent = outputs.getFirst();
                        var ingredient = outputContent.inner;
                        var stack = ingredient.getInnerItemStack();
                        if (stack.isEmpty()) continue;
                        if (!cache.contains(stack)) {
                            cache.add(stack);
                            super.outputs.add((EmiStack) getEmiIngredient(ingredient, false));
                        }
                    }
                }
            }
        }
        if (recipe.data.containsKey(ResearchPointsRecipeExtion.INSTANCE)) {
            var points = recipe.data.getData(ResearchPointsRecipeExtion.INSTANCE);
            if (points != null) {
                for (var it = points.reference2LongEntrySet().fastIterator(); it.hasNext();) {
                    var entry = it.next();
                    var researchTag = entry.getKey();
                    var amount = entry.getLongValue();
                    super.outputs.add(new ResearchTagEmiStack(researchTag).setAmount(amount));
                }
            }
        }
    }
}

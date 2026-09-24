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
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.client.Minecraft;
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
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.widget.RecipeBackground;
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
 * 一页只放一个配方，配方页是一张填满配方界面的卡片。EMI 的排版规则（{@code RecipeScreen}/{@code RecipeTab}/{@code RecipeDisplay}）：
 * <ul>
 * <li>界面高 {@code min(maximumRecipeScreenHeight, 屏高 - 52 - verticalMargin)}，配方区从顶部 37 像素起、高为界面高 - 46
 * （工作台在底部时再减 23）；配方按显示高度逐个往下排，放不下就换页——显示高度等于配方区高度就是一页一个；</li>
 * <li>界面宽 = 类别里最宽的 {@code 显示宽 + 右侧按钮列宽 13} + 16；配方从左边 8 像素起，底框外扩 4 像素；</li>
 * <li>右侧按钮（填充配方、合成树、默认配方……）画在 {@code x = 显示宽 + 5}，12 见方、间距 14，自显示区底边向上排。</li>
 * </ul>
 * 所以翻页排版时（{@code RecipeDisplay} 构造，经 {@code RecipeDisplayMixin} 取 {@link #getPagedWidth}/{@link #getPagedHeight}）
 * 把显示宽度报成"页宽 - 13"，按钮正好落进卡片右下角的缺口；显示高度报成配方区高度，页面撑满整页。
 * 其余场合（收藏栏悬停预览、截图、生产规划图……）没有右侧按钮、也不分页，{@link #getDisplayWidth}/{@link #getDisplayHeight}
 * 报按内容的紧凑尺寸，页面不挖缺口、不画卡片，保留 EMI 的底框。
 * <p>
 * 右侧按钮数取决于当前打开的界面（能否"填充配方"）和 EMI 设置（可在游戏里改），每次排版重新数，尺寸按按钮数分别缓存。
 */
public class GTEMIRecipe extends ModularEmiRecipe<Widget> {

    /// 交给父类构造器的占位控件：父类构造时只读它的尺寸，真正的尺寸见 getDisplayWidth / getDisplayHeight
    private static final Widget PLACEHOLDER = new Widget(0, 0, 0, 0);

    /// EMI 把右侧按钮画在显示宽度右边这么远处；按钮列宽 = 这个距离 + 按钮宽 - 卡片外扩
    private static final int EMI_BUTTON_OFFSET = 5;
    private static final int EMI_BUTTON_COLUMN = EMI_BUTTON_OFFSET + GTRecipeWidget.SIDE_BUTTON_INSET;
    /// EMI 配方界面：左右边距之和、配方区上下占用、底部工作台一行的高度
    private static final int EMI_SCREEN_SIDES = 16;
    private static final int EMI_RECIPE_AREA_CHROME = 46;
    private static final int EMI_SCREEN_CHROME = 52;
    private static final int EMI_BOTTOM_WORKSTATIONS = 23;
    /// GTO 的 EMI 分支多出的两个右侧按钮（生产流程图、配方书签叠加），装的是原版 EMI 时没有
    private static final boolean HAS_OVERLAY_BUTTON = classExists("dev.emi.emi.widget.RecipeOverlayButtonWidget");
    private static final boolean HAS_PLANNER_BUTTON = classExists("dev.emi.emi.widget.RecipePlannerButtonWidget");

    private final EmiRecipeCategory category;
    protected final GTRecipeDefinition recipe;
    public final IntSupplier displayPriority;
    /// 翻页排版时按内容的最小页面尺寸，按右侧按钮数分别缓存（下标为按钮数，最多 5 个）；紧凑尺寸
    private final Size[] pagedSizes = new Size[6];
    @Nullable
    private Size compactSize;
    /// 最近一次翻页排版时的右侧按钮数，-1 为还没排过（addWidgets 据此认出翻页排版的页面）
    private int pagedButtons = -1;
    /// EMI 正在翻页排版建页（RecipeDisplay.getWidgets 期间，由 RecipeDisplayMixin 设置；只在渲染线程上用）
    private static boolean pagedBuild;
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

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, GTEMIRecipe.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /** 翻页排版时的页面外框：至少与 EMI 界面最小宽度对齐，填满 {@code fillHeight}，右下角给 {@code buttons} 个 EMI 按钮留缺口，画卡片。 */
    protected GTRecipeWidget.PageFrame pagedFrame(int fillHeight, int buttons) {
        int minWidth = Math.max(UISizes.CONTENT_WIDTH, EmiConfig.minimumRecipeScreenWidth - EMI_SCREEN_SIDES);
        return new GTRecipeWidget.PageFrame(minWidth, fillHeight, buttons, true);
    }

    /** 翻页排版时按内容的最小页面尺寸（不含填满高度）。 */
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

    /** EMI 此刻会在本配方右侧画几个按钮，与 {@code RecipeDisplay} 的判断一致。 */
    private int sideButtons() {
        int count = 0;
        if (EmiConfig.recipeFillButton && EmiRecipeFiller.isSupported(this)) count++;
        if (getId() != null && !getOutputs().isEmpty()) {
            if (HAS_OVERLAY_BUTTON) count++;
            if (HAS_PLANNER_BUTTON && !getInputs().isEmpty()) count++;
        }
        if (supportsRecipeTree()) {
            if (EmiConfig.recipeTreeButton) count++;
            if (EmiConfig.recipeDefaultButton) count++;
        }
        return count;
    }

    /** 当前 EMI 配方界面里一个配方最多能占的高度；不在配方界面里时为 0，按内容大小。 */
    private int recipeAreaHeight() {
        if (!(Minecraft.getInstance().screen instanceof RecipeScreen)) return 0;
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int height = Math.min(EmiConfig.maximumRecipeScreenHeight, screenHeight - EMI_SCREEN_CHROME - EmiConfig.verticalMargin) - EMI_RECIPE_AREA_CHROME;
        if (EmiConfig.workstationLocation == SidebarSide.BOTTOM &&
                (!EmiApi.getRecipeManager().getWorkstations(category).isEmpty() || RecipeScreen.resolve != null)) {
            height -= EMI_BOTTOM_WORKSTATIONS;
        }
        return height;
    }

    /** 翻页排版时报给 EMI 的显示宽度：页宽减去伸进缺口的按钮列。同时记下这次的按钮数。 */
    public int getPagedWidth() {
        int buttons = sideButtons();
        pagedButtons = buttons;
        return pagedDisplayWidth(buttons);
    }

    private int pagedDisplayWidth(int buttons) {
        return pagedSize(buttons).width - (buttons > 0 ? EMI_BUTTON_COLUMN : 0);
    }

    /** 翻页排版时报给 EMI 的显示高度：撑满配方区，一页一个配方。 */
    public int getPagedHeight() {
        return Math.max(pagedSize(Math.max(pagedButtons, 0)).height, recipeAreaHeight());
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

    /** 由 {@code RecipeDisplayMixin} 在 EMI 翻页排版建页（{@code RecipeDisplay.getWidgets}）前后设置。 */
    public static void setPagedBuild(boolean paged) {
        pagedBuild = paged;
    }

    /** 当前是否在 EMI 翻页排版建页。 */
    public static boolean isPagedBuild() {
        return pagedBuild;
    }

    /**
     * 翻页排版建的页面（{@link #isPagedBuild}，且 EMI 按 {@link #getPagedWidth} 的宽度建了 {@code WidgetGroup}）：
     * 填满高度、挖缺口、画卡片并删掉 EMI 的默认底框（缺口处不能再露出底框）；其余场合用紧凑外框。
     */
    protected GTRecipeWidget.PageFrame frameFor(WidgetHolder widgets) {
        if (pagedBuild && pagedButtons >= 0 && widgets instanceof dev.emi.emi.screen.WidgetGroup group &&
                widgets.getWidth() == pagedDisplayWidth(pagedButtons)) {
            group.widgets.removeIf(RecipeBackground.class::isInstance);
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

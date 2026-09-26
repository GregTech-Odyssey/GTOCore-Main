package com.gtocore.api.gui.recipe;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeSlotLayout;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeSlots;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import static com.gregtechceu.gtceu.api.recipe.ui.RecipeSlotLayouts.*;
import static com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection.*;

/**
 * GTO 配方类型的专用槽位区排布（原来是 LDLib 编辑器存的 .rtui，改成 Java）。
 * 配方所需的研究数据由研究条件作为配方页底部的展示槽显示，排布里不再留数据球槽。
 */
public final class GTORecipeLayouts {

    private GTORecipeLayouts() {}

    private static final ResourceTexture COMPONENT_ASSEMBLY_LINE_1 = new ResourceTexture("gtceu:textures/gui/progress_bar/progress_bar_component_assembly_line_1.png");
    private static final ResourceTexture COMPONENT_ASSEMBLY_LINE_2 = new ResourceTexture("gtceu:textures/gui/progress_bar/progress_bar_component_assembly_line_2.png");
    private static final ResourceTexture DIMENSIONAL_FOCUS_ENGRAVING = new ResourceTexture("gtceu:textures/gui/progress_bar/progress_bardimensional_focus_engraving_array.png");
    private static final ResourceTexture HEAT_EXCHANGER = new ResourceTexture("gtocore:textures/gui/heat_exchanger.png");
    /** 大量产出的配方（宇宙模拟、随机虚空采矿）每行的槽数。 */
    private static final int WIDE_COLUMNS = 12;
    /** 大量产出的滚动区最多显示的行数。 */
    private static final int WIDE_VISIBLE_ROWS = 4;

    /** 部件装配：左 3×3 物品输入，右 3×3 流体输入（低一行），其上的装配线底图从物品网格一直延伸到右上角的输出。 */
    public static final RecipeSlotLayout COMPONENT_ASSEMBLY = slots -> {
        var canvas = canvas(150, 80);
        // 底图先放，槽位画在它上面
        place(canvas, slots.progress(COMPONENT_ASSEMBLY_LINE_1, LEFT_TO_RIGHT, 72, 40), 57, 4);
        place(canvas, slots.progress(COMPONENT_ASSEMBLY_LINE_2, DOWN_TO_UP, 3, 12), 118, 10);
        var items = slots.slots(IO.IN, ItemRecipeInfo.INSTANCE);
        for (int i = 0; i < items.size(); i++) place(canvas, items.get(i), 3 + (i % 3) * UISizes.SLOT, 4 + (i / 3) * UISizes.SLOT);
        var fluids = slots.slots(IO.IN, FluidRecipeInfo.INSTANCE);
        for (int i = 0; i < fluids.size(); i++) place(canvas, fluids.get(i), 74 + (i % 3) * UISizes.SLOT, 22 + (i / 3) * UISizes.SLOT);
        var outputs = slots.slots(IO.OUT, ItemRecipeInfo.INSTANCE);
        for (int i = 0; i < outputs.size(); i++) place(canvas, outputs.get(i), 129, 4 + i * UISizes.SLOT);
        return canvas;
    };

    /** 维度聚焦激光蚀刻阵列：左列物品、流体输入，中间竖长的蚀刻底图，右列物品、流体输出。 */
    public static final RecipeSlotLayout DIMENSIONAL_FOCUS_ENGRAVING_ARRAY = slots -> {
        var canvas = canvas(62, 80);
        place(canvas, slots.progress(DIMENSIONAL_FOCUS_ENGRAVING, LEFT_TO_RIGHT, 18, 69), 22, 5);
        column(canvas, slots, IO.IN, 4);
        column(canvas, slots, IO.OUT, 40);
        return canvas;
    };

    /** 在 x 处从上往下排该方向的物品槽、再排流体槽。 */
    private static void column(UIElement canvas, RecipeSlots slots, IO io, int x) {
        int y = 4;
        for (var slot : slots.slots(io, ItemRecipeInfo.INSTANCE)) {
            place(canvas, slot, x, y);
            y += UISizes.SLOT;
        }
        for (var slot : slots.slots(io, FluidRecipeInfo.INSTANCE)) {
            place(canvas, slot, x, y);
            y += UISizes.SLOT;
        }
    }

    /** 流体热交换：左侧两个流体输入，中间热交换器示意图（静态），右侧三个流体输出。 */
    public static final RecipeSlotLayout HEAT_EXCHANGER_LAYOUT = slots -> {
        var canvas = canvas(158, 80);
        image(canvas, HEAT_EXCHANGER, 34, 4, 88, 69);
        var inputs = slots.slots(IO.IN, FluidRecipeInfo.INSTANCE);
        for (int i = 0; i < inputs.size(); i++) place(canvas, inputs.get(i), 9, 7 + i * 26);
        var outputs = slots.slots(IO.OUT, FluidRecipeInfo.INSTANCE);
        for (int i = 0; i < outputs.size(); i++) place(canvas, outputs.get(i), 130, 7 + i * 26);
        return canvas;
    };

    public static final RecipeSlotLayout SINGLE_ROW = slots -> {
        var inputs = UIElement.row(UISizes.SLOT);
        slots.slots(IO.IN, ItemRecipeInfo.INSTANCE).forEach(inputs::addChild);
        slots.slots(IO.IN, FluidRecipeInfo.INSTANCE).forEach(inputs::addChild);
        var outputs = UIElement.row(UISizes.SLOT);
        slots.slots(IO.OUT, ItemRecipeInfo.INSTANCE).forEach(outputs::addChild);
        slots.slots(IO.OUT, FluidRecipeInfo.INSTANCE).forEach(outputs::addChild);
        return new UIElement().layout(l -> l.row().paddingAll(PADDING).gapAll(PROGRESS_MARGIN).alignCenter())
                .addChildren(inputs, slots.progress(), outputs);
    };

    /** 精密组装：左侧上一行 4 个物品输入、下一行 4 个流体输入，箭头指向右侧的输出。 */
    public static final RecipeSlotLayout PRECISION_ASSEMBLER = slots -> {
        var inputs = new UIElement().layout(l -> l.column().gapAll(UISizes.SLOT));
        var items = slots.slots(IO.IN, ItemRecipeInfo.INSTANCE);
        if (!items.isEmpty()) inputs.addChild(grid(items, 4));
        var fluids = slots.slots(IO.IN, FluidRecipeInfo.INSTANCE);
        if (!fluids.isEmpty()) inputs.addChild(grid(fluids, 4));
        return new UIElement().layout(l -> l.row().paddingAll(PADDING).gapAll(PROGRESS_MARGIN).alignCenter())
                .addChildren(inputs, slots.progress(), grid(slots.slots(IO.OUT, ItemRecipeInfo.INSTANCE), 1));
    };

    /**
     * 大量产出（宇宙模拟、随机虚空采矿）：顶部居中的输入，下面向下落的锤子进度，再往下是每行 {@link #WIDE_COLUMNS} 格的产出网格
     * （物品在前、流体在后），放在最多 {@link #WIDE_VISIBLE_ROWS} 行高的滚动区里——产出多达上百格，整页铺开会超出屏幕。
     */
    public static final RecipeSlotLayout WIDE_OUTPUT = slots -> {
        var inputs = UIElement.row(UISizes.SLOT);
        for (var cap : slots.capabilities(IO.IN)) slots.slots(IO.IN, cap).forEach(inputs::addChild);
        var outputs = new UIElement().layout(l -> l.column());
        for (var cap : slots.capabilities(IO.OUT)) {
            var capSlots = slots.slots(IO.OUT, cap);
            if (!capSlots.isEmpty()) outputs.addChild(grid(capSlots, WIDE_COLUMNS));
        }
        // 宽度留出滚动条位置，否则最后一列被滚动条压住
        var scroller = new ScrollerView("gtocore_recipe_wide_output", WIDE_COLUMNS * UISizes.SLOT + ScrollerView.SCROLL_BAR_SPACE, UISizes.SLOT)
                .adaptiveHeight(WIDE_VISIBLE_ROWS * UISizes.SLOT);
        scroller.setResizable(false);
        scroller.addScrollViewChild(outputs);
        return new UIElement().layout(l -> l.column().paddingAll(PADDING).gapAll(UISizes.GAP).alignCenter())
                .addChildren(inputs, slots.progress(GuiTextures.PROGRESS_BAR_HAMMER, UP_TO_DOWN, RecipeSlots.PROGRESS_SIZE, RecipeSlots.PROGRESS_SIZE), scroller);
    };
}

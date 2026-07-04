package com.gtocore.common.machine.multiblock.part.voiding;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fluids.FluidStack;

import appeng.hooks.IUnique;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Predicate;

public final class FilterVoidOutputHatchPartMachine extends VoidOutputHatchPartMachine implements Predicate<FluidStack> {

    @Getter
    @SaveToDisk
    private final CustomFluidTank[] tanks = new CustomFluidTank[81];

    @Setter
    @Getter
    @SaveToDisk(defaultValue = "false")
    public boolean reverse;

    private final IntOpenHashSet ids = new IntOpenHashSet();

    public FilterVoidOutputHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MV);
        for (int i = 0; i < tanks.length; i++) {
            tanks[i] = new CustomFluidTank(1);
        }
    }

    @Override
    public boolean test(FluidStack stack) {
        var id = ((IUnique) stack.getFluid()).ae2$getUid();
        if (reverse) {
            return !ids.contains(id);
        } else {
            return ids.contains(id);
        }
    }

    private void onSlotChanged() {
        ids.clear();
        for (CustomFluidTank tank : tanks) {
            var stack = tank.getFluid();
            if (!stack.isEmpty()) {
                ids.add(((IUnique) stack.getFluid()).ae2$getUid());
            }
        }
        if (ids.isEmpty()) {
            handler.setFilter(GTUtil.FAVORABLE);
        } else {
            handler.setFilter(this);
        }
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onSlotChanged();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators((new IFancyConfiguratorButton.Toggle(GuiTextures.BUTTON_FILTER_DAMAGE.getSubTexture(0.0F, 0.5F, 1.0F, 0.5F), GuiTextures.BUTTON_FILTER_DAMAGE.getSubTexture(0.0F, 0.0F, 1.0F, 0.5F), this::isReverse, (clickData, pressed) -> this.setReverse(pressed))).setTooltipsSupplier((pressed) -> List.of(Component.translatable("gui.ae2wtlib.switch").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)).append(Component.translatable(pressed ? "gui.ae2wtlib.whitelist" : "gui.ae2wtlib.blacklist")))));
    }

    @Override
    public Widget createUIWidget() {
        int rowSize = 9;
        int colSize = 9;
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * colSize + 8);
        int index = 0;
        for (int y = 0; y < colSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int finalIndex = index++;
                container.addWidget(new PhantomFluidWidget(this.tanks[finalIndex], finalIndex, 4 + x * 18, 4 + y * 18, 18, 18, () -> this.tanks[finalIndex].getFluid(), (fluid -> tanks[finalIndex].setFluid(fluid.isEmpty() ? FluidStack.EMPTY : ICustomFluidStackHandler.copy(fluid, 1000)))).setChangeListener(this::onSlotChanged).setShowAmount(false).setBackground(GuiTextures.FLUID_SLOT));
            }
        }
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}

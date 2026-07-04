package com.gtocore.common.machine.multiblock.part.voiding;

import com.gtolib.api.machine.trait.VoidOutputFluidHandler;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IFilteredHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import com.gto.datasynclib.annotations.SaveToDisk;
import lombok.Getter;

public class VoidOutputHatchPartMachine extends WorkableTieredIOPartMachine {

    final VoidOutputFluidHandler handler = new VoidOutputFluidHandler(this);

    @Getter
    @SaveToDisk(defaultValue = "0")
    private int priority = 0;

    public VoidOutputHatchPartMachine(MetaMachineBlockEntity holder) {
        this(holder, GTValues.LV);
    }

    VoidOutputHatchPartMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.OUT);
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    private void setPriority(int priority) {
        this.priority = priority;
        handler.setPriority(priority);
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        handler.setPriority(priority);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(IFilteredHandler.createPriorityConfigurator(this::getPriority, this::setPriority));
    }
}

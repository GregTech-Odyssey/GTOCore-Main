package com.gtocore.common.machine.multiblock.part.voiding;

import com.gtolib.api.machine.trait.VoidOutputItemHandler;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IFilteredHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import com.gto.datasynclib.annotations.SaveToDisk;
import lombok.Getter;

public class VoidOutputBusPartMachine extends WorkableTieredIOPartMachine {

    final VoidOutputItemHandler handler = new VoidOutputItemHandler(this);

    @Getter
    @SaveToDisk(defaultValue = "0")
    private int priority = 0;

    public VoidOutputBusPartMachine(MetaMachineBlockEntity holder) {
        this(holder, GTValues.LV);
    }

    VoidOutputBusPartMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.OUT);
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    private void setPriority(int priority) {
        if (priority == Integer.MIN_VALUE) return;
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

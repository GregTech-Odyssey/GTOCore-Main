package com.gtocore.common.machine.multiblock.part.ae;

import com.gtolib.api.machine.trait.MEOutputFluidHandler;
import com.gtolib.api.machine.trait.MEOutputItemHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.recipe.handler.IFilteredHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDualOutputPartMachine extends StatusTrackedMEPartMachine {

    @SaveToDisk
    private final KeyStorage internalBuffer;
    @SaveToDisk
    private final KeyStorage internalTankBuffer;
    private final MEOutputItemHandler handler;
    private final MEOutputFluidHandler tank;

    @Getter
    @SaveToDisk(defaultValue = "10000")
    private int priority = 10000;

    public MEDualOutputPartMachine(MetaMachineBlockEntity holder) {
        super(holder, IO.OUT);
        internalBuffer = new KeyStorage();
        handler = new MEOutputItemHandler(this, internalBuffer);
        internalTankBuffer = new KeyStorage();
        tank = new MEOutputFluidHandler(this, internalTankBuffer);
    }

    private void setPriority(int priority) {
        this.priority = priority;
        handler.setPriority(priority);
        tank.setPriority(priority);
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        handler.setPriority(priority);
        tank.setPriority(priority);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(IFilteredHandler.createPriorityConfigurator(this::getPriority, this::setPriority));
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        handler.updateAutoOutputSubscription();
        tank.updateAutoOutputSubscription();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        handler.updateAutoOutputSubscription();
        tank.updateAutoOutputSubscription();
    }

    @Override
    public void onMachineRemoved() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            if (!internalBuffer.isEmpty()) {
                for (var entry : internalBuffer) {
                    grid.getStorageService().getInventory().insert(entry.getKey(), entry.getLongValue(),
                            Actionable.MODULATE, getActionSource());
                }
            }
            if (!internalTankBuffer.isEmpty()) {
                for (var entry : internalTankBuffer) {
                    grid.getStorageService().getInventory().insert(entry.getKey(), entry.getLongValue(),
                            Actionable.MODULATE, getActionSource());
                }
            }
        }
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.superAttachConfigurators(configuratorPanel);
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        group.addWidget(new LabelWidget(5, 0, () -> this.isOnline() ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        group.addWidget(new LabelWidget(5, 10, "gtceu.gui.waiting_list"));
        group.addWidget(new AEListGridWidget.Item(5, 20, 3, this.internalBuffer));
        group.addWidget(new AEListGridWidget.Fluid(5, 80, 3, this.internalTankBuffer));
        return group;
    }
}

package com.gtocore.common.machine.multiblock.part.ae;

import com.gtolib.api.machine.trait.MEOutputFluidHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.recipe.handler.IFilteredHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.uipro.UIElement;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MEOutputHatchPartMachine extends StatusTrackedMEPartMachine {

    @SaveToDisk
    private final KeyStorage internalBuffer;
    private final MEOutputFluidHandler tank;

    @Getter
    @SaveToDisk(defaultValue = "10000")
    private int priority = 10000;

    public MEOutputHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, IO.OUT);
        internalBuffer = new KeyStorage();
        tank = new MEOutputFluidHandler(this, internalBuffer);
    }

    private void setPriority(int priority) {
        if (priority == Integer.MIN_VALUE) return;
        this.priority = priority;
        tank.setPriority(priority);
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tank.setPriority(priority);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(IFilteredHandler.createOutputPriorityConfigurator(this::getPriority, this::setPriority));
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        tank.updateAutoOutputSubscription();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        tank.updateAutoOutputSubscription();
    }

    @Override
    public void onMachineRemoved() {
        var grid = getMainNode().getGrid();
        if (grid != null && !internalBuffer.isEmpty()) {
            for (var entry : internalBuffer) {
                grid.getStorageService().getInventory().insert(entry.getKey(), entry.getLongValue(),
                        Actionable.MODULATE, getActionSourceField());
            }
        }
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.superAttachConfigurators(configuratorPanel);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return MEPartUI.mainPage(this, widget, buildPage());
    }

    @Override
    public Widget createUIWidget() {
        return buildPage();
    }

    /** 页面："等待输出"网格（每行 9 格，只读）。 */
    private UIElement buildPage() {
        return MEPartUI.page().addChild(MEPartUI.waitingList("me.output_hatch.waiting", this.internalBuffer, true, null));
    }
}

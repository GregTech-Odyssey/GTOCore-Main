package com.gtocore.common.machine.multiblock.part.ae;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.trait.MEOutputFluidHandler;
import com.gtolib.api.machine.trait.MEOutputItemHandler;

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

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEDualOutputPartMachine extends StatusTrackedMEPartMachine {

    @RegisterLanguage(cn = "等待送入网络的物品", en = "Items waiting for the network")
    private static final String WAITING_ITEMS = "gtocore.machine.me_dual_output.waiting_items";
    @RegisterLanguage(cn = "等待送入网络的流体", en = "Fluids waiting for the network")
    private static final String WAITING_FLUIDS = "gtocore.machine.me_dual_output.waiting_fluids";

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
        if (priority == Integer.MIN_VALUE) return;
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
        sideTabs.attachSubTab(IFilteredHandler.createOutputPriorityConfigurator(this::getPriority, this::setPriority));
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
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return MEPartUI.mainPage(this::isOnline, getTitle(), widget, buildPage());
    }

    @Override
    public Widget createUIWidget() {
        return buildPage();
    }

    /** 页面：物品、流体两块"等待输出"网格（每行 9 格，只读）。 */
    private UIElement buildPage() {
        return MEPartUI.page().addChildren(
                MEPartUI.waitingList("me.dual_output.items", this.internalBuffer, false, WAITING_ITEMS),
                MEPartUI.waitingList("me.dual_output.fluids", this.internalTankBuffer, true, WAITING_FLUIDS));
    }
}

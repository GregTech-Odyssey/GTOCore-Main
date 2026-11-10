package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemSlot;
import com.gtocore.common.machine.multiblock.part.ae.widget.AEItemConfigWidget;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.annotation.SaveToDataStick;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.trait.CircuitHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.content.Circuits;
import com.gregtechceu.gtceu.api.recipe.handler.IFilteredHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;

import com.gto.ae.helpers.facility_management.WorkingStatus;
import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.suried.gtoutils.schedule.Task;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputBusPartMachine extends StatusTrackedMEPartMachine {

    private Task autoIOSubs;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor meInputMonitor = holder.monitorTick(GTOTickTimeMonitors.ME_INPUT, this::autoIO);
    @SaveToDataStick(getter = "getDataStickSettings", setter = "setDataStickSettings")
    @SaveToDisk
    final ExportOnlyAEItemList aeItemHandler;

    @SaveToDisk
    protected final NotifiableInventory<AEItemKey> circuitInventory;

    @Getter
    @SaveToDisk(defaultValue = "0")
    private int priority = 0;

    public MEInputBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder, IO.IN);
        aeItemHandler = createInventory();
        aeItemHandler.addChangedListener(() -> {
            getConfiguredSetting().clear();
            aeItemHandler.forEachKey(AEKeyTypes.ITEMS, (k, l) -> {
                getConfiguredSetting().set(k, l);
                return false;
            });
        });
        circuitInventory = CircuitHandler.create(this);
    }

    private void setPriority(int priority) {
        if (priority == Integer.MIN_VALUE) return;
        this.priority = priority;
        aeItemHandler.setPriority(priority);
        circuitInventory.setPriority(priority);
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        aeItemHandler.setPriority(priority);
        circuitInventory.setPriority(priority);
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(IFilteredHandler.createPriorityConfigurator(this::getPriority, this::setPriority));
    }

    /////////////////////////////////
    // ***** Machine LifeCycle ****//
    /////////////////////////////////

    @Override
    public void onMachineRemoved() {
        flushInventory();
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateInventorySubscription();
    }

    ExportOnlyAEItemList createInventory() {
        return new ExportOnlyAEItemList(this, CONFIG_SIZE);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.updateInventorySubscription();
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    private void autoIO() {
        setStatus(throughputCounter.map.isEmpty() ? WorkingStatus.IDLE : WorkingStatus.WORKING);
        throughputCounter.tickRefresh();
        if (this.updateMEStatus()) {
            this.syncME();
            this.updateInventorySubscription();
        }
    }

    void syncME() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            return;
        }
        MEStorage networkInv = grid.getStorageService().getInventory();
        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
            GenericStack exceedItem = aeSlot.exceedStack();
            if (exceedItem != null) {
                long total = exceedItem.amount();
                long inserted = networkInv.insert(exceedItem.what(), exceedItem.amount(), Actionable.MODULATE, this.getActionSourceField());
                throughputCounter.add(exceedItem.what(), inserted);
                if (inserted > 0) {
                    aeSlot.extract(inserted, false, true);
                    continue;
                } else {
                    aeSlot.extract(total, false, true);
                }
            }
            GenericStack reqItem = aeSlot.requestStack();
            if (reqItem != null) {
                long extracted = networkInv.extract(reqItem.what(), reqItem.amount(), Actionable.MODULATE, this.getActionSourceField());
                throughputCounter.remove(reqItem.what(), extracted);
                if (extracted != 0) {
                    aeSlot.addStack(new GenericStack(reqItem.what(), extracted));
                }
            }
        }
    }

    void updateInventorySubscription() {
        if (isWorkingEnabled() && getOnlineField()) {
            autoIOSubs = subscribeServerTick(autoIOSubs, meInputMonitor, 40);
        } else if (autoIOSubs != null) {
            setStatus(WorkingStatus.IDLE);
            autoIOSubs.cancel();
            autoIOSubs = null;
        }
    }

    void flushInventory() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            for (var aeSlot : aeItemHandler.getInventory()) {
                GenericStack stock = aeSlot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE, getActionSourceField());
                }
            }
        }
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return MEPartUI.mainPage(this, widget, Form.page().addChild(createUIWidget()));
    }

    /** 配置网格：每行 9 格（上：配置 / 下：库存）。 */
    @Override
    public Widget createUIWidget() {
        return new AEItemConfigWidget(0, 0, this.aeItemHandler, UISizes.SLOTS_PER_ROW);
    }

    private MEInputSettings getDataStickSettings() {
        return MEInputSettings.capture(isDistinct(), Circuits.get(circuitInventory.storage, 0),
                !aeItemHandler.isAutoPull(), aeItemHandler.getInventory());
    }

    private void setDataStickSettings(MEInputSettings settings) {
        setDistinct(settings.distinct());
        int circuit = settings.circuit();
        Circuits.set(circuitInventory.storage, 0, circuit >= 0 && circuit <= Circuits.MAX ? circuit : -1);
        settings.applyConfigs(aeItemHandler.getInventory());
        aeItemHandler.onContentsChanged();
        updateInventorySubscription();
    }
}

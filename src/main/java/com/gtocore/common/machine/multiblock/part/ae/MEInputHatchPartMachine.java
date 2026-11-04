package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidSlot;
import com.gtocore.common.machine.multiblock.part.ae.widget.AEFluidConfigWidget;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
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
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputHatchPartMachine extends StatusTrackedMEPartMachine {

    @Nullable
    private TickableSubscription autoIOSubs;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor meInputMonitor = holder.monitorTick(GTOTickTimeMonitors.ME_INPUT, this::autoIO);
    @SaveToDataStick(getter = "getDataStickSettings", setter = "setDataStickSettings")
    @SaveToDisk
    final ExportOnlyAEFluidList aeFluidHandler;

    @SaveToDisk
    protected final NotifiableInventory<AEItemKey> circuitInventory;

    @Getter
    @SaveToDisk(defaultValue = "0")
    private int priority = 0;

    public MEInputHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, IO.IN);
        aeFluidHandler = createTank();
        aeFluidHandler.addChangedListener(() -> {
            getConfiguredSetting().clear();
            aeFluidHandler.forEachKey(AEKeyTypes.FLUIDS, (k, l) -> {
                getConfiguredSetting().set(k, l);
                return false;
            });
        });
        circuitInventory = CircuitHandler.create(this);
    }

    private void setPriority(int priority) {
        // 防御 Integer.MIN_VALUE：客户端 IntInputWidget 初始化时
        // setMin(Integer.MIN_VALUE) 会触发空文本框被设为 min 值，
        // 然后通过 CPacketUIClientAction 发送到服务端覆盖正确值
        if (priority == Integer.MIN_VALUE) return;
        this.priority = priority;
        aeFluidHandler.setPriority(priority);
        circuitInventory.setPriority(priority);
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        aeFluidHandler.setPriority(priority);
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
        updateTankSubscription();
    }

    ExportOnlyAEFluidList createTank() {
        return new ExportOnlyAEFluidList(this, CONFIG_SIZE);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.updateTankSubscription();
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    private void autoIO() {
        setStatus(throughputCounter.map.isEmpty() ? WorkingStatus.IDLE : WorkingStatus.WORKING);
        throughputCounter.tickRefresh();
        if (this.updateMEStatus()) {
            this.syncME();
            this.updateTankSubscription();
        }
    }

    void syncME() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            return;
        }
        MEStorage networkInv = grid.getStorageService().getInventory();
        for (ExportOnlyAEFluidSlot aeTank : this.aeFluidHandler.getInventory()) {
            GenericStack exceedFluid = aeTank.exceedStack();
            if (exceedFluid != null) {
                long total = exceedFluid.amount();
                long inserted = networkInv.insert(exceedFluid.what(), exceedFluid.amount(), Actionable.MODULATE, this.getActionSourceField());
                throughputCounter.add(exceedFluid.what(), inserted);
                if (inserted > 0) {
                    aeTank.extract(inserted, false, true);
                    continue;
                } else {
                    aeTank.extract(total, false, true);
                }
            }
            GenericStack reqFluid = aeTank.requestStack();
            if (reqFluid != null) {
                long extracted = networkInv.extract(reqFluid.what(), reqFluid.amount(), Actionable.MODULATE, this.getActionSourceField());
                throughputCounter.remove(reqFluid.what(), extracted);
                if (extracted > 0) {
                    aeTank.addStack(new GenericStack(reqFluid.what(), extracted));
                }
            }
        }
    }

    void updateTankSubscription() {
        if (isWorkingEnabled() && getOnlineField()) {
            autoIOSubs = subscribeServerTick(autoIOSubs, meInputMonitor, 40);
        } else if (autoIOSubs != null) {
            setStatus(WorkingStatus.IDLE);
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    void flushInventory() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            for (var aeSlot : aeFluidHandler.getInventory()) {
                GenericStack stock = aeSlot.getStock();
                if (stock != null) {
                    grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            getActionSourceField());
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
        return new AEFluidConfigWidget(0, 0, this.aeFluidHandler, UISizes.SLOTS_PER_ROW);
    }

    private MEInputSettings getDataStickSettings() {
        return MEInputSettings.capture(isDistinct(), Circuits.get(circuitInventory.storage, 0),
                !aeFluidHandler.isAutoPull(), aeFluidHandler.getInventory());
    }

    private void setDataStickSettings(MEInputSettings settings) {
        setDistinct(settings.distinct());
        int circuit = settings.circuit();
        Circuits.set(circuitInventory.storage, 0, circuit >= 0 && circuit <= Circuits.MAX ? circuit : -1);
        settings.applyConfigs(aeFluidHandler.getInventory());
        aeFluidHandler.onContentsChanged();
        updateTankSubscription();
    }
}

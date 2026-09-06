package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.me.service.EnergyService;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class MEEnergyAccessPartMachine extends MEPartMachine implements IAEPowerStorage {

    /**
     * AE2 {@link EnergyService} 的 providerPowerSum 字段。它只在节点 addNode/removeNode 时更新，
     * 之后本仓容量变化不会自动刷新，这里通过反射直接读写该字段按差值修正缓存的容量。
     */
    private static final Field PROVIDER_POWER_SUM_FIELD;

    static {
        Field field = null;
        try {
            field = EnergyService.class.getDeclaredField("providerPowerSum");
            field.setAccessible(true);
        } catch (Exception ignored) {}
        PROVIDER_POWER_SUM_FIELD = field;
    }

    private TierCasingMultiblockMachine controller = null;

    private double ratio = 1;

    private boolean powerNotifyQueued = false;

    private TickableSubscription tickSubs = null;

    private double maxPower = 0;

    public MEEnergyAccessPartMachine(MetaMachineBlockEntity holder) {
        super(holder, IO.NONE);
        this.getMainNode().addService(IAEPowerStorage.class, this);
    }

    public long getEnergyCapacity() {
        if (isInValid() || controller == null) return 0;
        return controller.getEnergyContainer().getEnergyCapacity();
    }

    public long getEnergyStored() {
        if (isInValid() || controller == null) return 0;
        return controller.getEnergyContainer().getEnergyStored();
    }

    private void updateRatio() {
        ratio = ConfigHolder.INSTANCE.compat.energy.euToFeRatio * PowerUnits.FE.convertTo(PowerUnits.AE, 1);
        if (controller != null) {
            ratio *= 1 + 0.3 * controller.getCasingTier(GTORecipeDataKeys.GLASS_TIER);
            ratio *= controller.getSubFormedAmount() + 1;
        }
    }

    private void refreshAECapacity() {
        final double newMaxPower = getEnergyCapacity() * ratio;
        if (maxPower == newMaxPower) return;
        var grid = this.getMainNode().getGrid();
        if (grid != null) {
            try {
                final double delta = newMaxPower - maxPower;
                final IEnergyService energyService = grid.getEnergyService();
                PROVIDER_POWER_SUM_FIELD.setDouble(energyService, PROVIDER_POWER_SUM_FIELD.getDouble(energyService) + delta);
            } catch (Exception ignored) {}
        }
        maxPower = newMaxPower;
    }

    private void tick() {
        if (controller == null || !this.workingEnabled) return;
        if (controller.getEnergyContainer().getEnergyStored() <= 0) return;
        queuePowerNotify();
    }

    private void queuePowerNotify() {
        if (powerNotifyQueued) return;
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        powerNotifyQueued = true;
        serverLevel.getServer().tell(new TickTask(0, () -> {
            powerNotifyQueued = false;
            if (!isInValid() && controller != null && controller.getEnergyContainer().getEnergyStored() > 0) {
                postEnergyEvent();
            }
        }));
    }

    private void postEnergyEvent() {
        if (controller == null) return;
        if (this.getMainNode().getGrid() != null) {
            this.getMainNode().getGrid().postEvent(new GridPowerStorageStateChanged(this, GridPowerStorageStateChanged.PowerEventType.PROVIDE_POWER));
        }
    }

    @Override
    public void setOnline(boolean isOnline) {
        super.setOnline(isOnline);
        postEnergyEvent();
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        if (workingEnabled) postEnergyEvent();
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        this.controller = null;
        updateRatio();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::refreshAECapacity));
        }
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        this.controller = (TierCasingMultiblockMachine) controller;
        updateRatio();
        postEnergyEvent();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::refreshAECapacity));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubs = subscribeServerTick(tickSubs, this::tick, 20);
        }
        postEnergyEvent();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubs != null) {
            tickSubs.unsubscribe();
            tickSubs = null;
        }
    }

    @Override
    public double injectAEPower(double amt, Actionable mode) {
        return amt;
    }

    @Override
    public double getAEMaxPower() {
        return maxPower;
    }

    @Override
    public double getAECurrentPower() {
        if (!this.workingEnabled) return 0;
        return getEnergyStored() * ratio;
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ;
    }

    @Override
    public int getPriority() {
        return 1 << 30;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier) {
        return multiplier.divide(this.extractAEPower(multiplier.multiply(amt), mode));
    }

    private double extractAEPower(double amt, Actionable mode) {
        if (amt <= 0) return 0;
        if (controller == null || !this.workingEnabled) return 0;
        final double stored = getEnergyStored() * ratio;
        if (stored == 0) return 0;
        final double extracted = Math.min(stored, amt);
        if (mode.isSimulate()) return extracted;
        controller.getEnergyContainer().changeEnergy(-(long) Math.ceil(extracted / ratio));
        return extracted;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 170, 65);
        group.addWidget(new LabelWidget(5, 0, () -> this.getOnlineField() ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));
        return group;
    }
}

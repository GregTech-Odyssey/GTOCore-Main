package com.gtocore.common.machine.monitor;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.List;

public abstract class AbstractInfoProviderMonitor extends BasicMonitor implements IInformationProvider {

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private long priority = 0;

    protected TickableSubscription tickableSubscription;

    public AbstractInfoProviderMonitor(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        tickableSubscription = this.subscribe(() -> {
            try {
                this.syncInfoFromServer();
                this.requestSync();
            } catch (Throwable throwable) {
                GTOCore.LOGGER.error("Error syncing monitor info provider data", throwable);
            }
        });
    }

    protected TickableSubscription subscribe(Runnable runnable) {
        return this.subscribeAsyncTick(tickableSubscription, runnable, 10);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickableSubscription != null) {
            tickableSubscription.unsubscribe();
            tickableSubscription = null;
        }
    }

    @Override
    public long getPriority() {
        return priority;
    }

    @Override
    public void setPriority(long priority) {
        this.priority = priority;
        if (getLevel() != null && !getLevel().isClientSide()) {
            this.requestSync();
        }
    }

    @Override
    public List<ResourceLocation> getSortedRLs() {
        return getAvailableRLs();
    }

    protected UIElement createPriorityRow() {
        return Form.numberRow(Component.translatable("gtocore.machine.monitor.priority"),
                NumberField.ofLong(UISizes.CONTENT_WIDTH, this::getPriority, this::setPriority, (long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE));
    }

    @Override
    public Widget createUIWidget() {
        return super.createUIWidget();
    }

    public Widget createConfigurationPage() {
        return Form.page().addChild(createPriorityRow());
    }
}

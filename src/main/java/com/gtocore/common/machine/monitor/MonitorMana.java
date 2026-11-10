package com.gtocore.common.machine.monitor;

import com.gtolib.api.gui.MonitorMetricDisplay;
import com.gtolib.api.gui.MonitorResourceHistory;
import com.gtolib.api.wireless.WirelessManaContainer;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.uipro.elements.Form;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.suried.gtoutils.schedule.Task;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class MonitorMana extends AbstractInfoProviderMonitor implements ITeamInformationProvider {

    private final MonitorMetricDisplay storageDisplay = new MonitorMetricDisplay(DisplayRegistry.MANA_CURRENT, " MANA", false);
    private final MonitorMetricDisplay changeDisplay = new MonitorMetricDisplay(DisplayRegistry.MANA_CHANGE, " MANA/s", true);
    private final MonitorResourceHistory history = new MonitorResourceHistory();
    @Nullable
    private WirelessManaContainer sampledContainer;
    private int sampledWindow = -1;

    @SyncToClient
    private BigInteger manaBufferCache = BigInteger.ZERO;
    @SyncToClient
    private BigInteger manaChange = BigInteger.ZERO;
    @SyncToClient
    private BigInteger[] storageHistory = MonitorResourceHistory.EMPTY_VALUES;
    @SyncToClient
    private BigInteger[] throughputHistory = MonitorResourceHistory.EMPTY_VALUES;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int chartWindow;

    public MonitorMana(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    protected Task subscribe(Runnable runnable) {
        return subscribeServerTick(tickableSubscription, runnable, 10);
    }

    @Override
    public void syncInfoFromServer() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        if (getUUID() == null) {
            history.reset();
            sampledContainer = null;
            sampledWindow = -1;
            manaBufferCache = BigInteger.ZERO;
            manaChange = BigInteger.ZERO;
            storageHistory = MonitorResourceHistory.EMPTY_VALUES;
            throughputHistory = MonitorResourceHistory.EMPTY_VALUES;
            return;
        }
        var container = WirelessManaContainer.getOrCreateContainer(getUUID());
        if (sampledContainer != container) {
            history.reset();
            sampledContainer = container;
        }
        manaBufferCache = container.getStorage();
        boolean sampled = history.sample(level.getGameTime(), manaBufferCache);
        manaChange = history.changePerSecond();
        if (sampled || sampledWindow != chartWindow) {
            storageHistory = history.storage(chartWindow);
            throughputHistory = history.throughput(chartWindow);
            sampledWindow = chartWindow;
        }
    }

    @Override
    public DisplayComponentList provideInformation() {
        var informationList = ITeamInformationProvider.super.provideInformation();
        if (getUUID() != null) {
            informationList.addIfAbsent(DisplayRegistry.MANA_CURRENT.id(), storageDisplay.setValue(manaBufferCache));
            informationList.addIfAbsent(DisplayRegistry.MANA_CHANGE.id(), changeDisplay.setValue(manaChange));
            int interval = MonitorResourceHistory.sampleInterval(chartWindow);
            informationList.addIfAbsent(DisplayRegistry.MANA_STORAGE_CHART.id(),
                    DisplayComponent.lineChart(DisplayRegistry.MANA_STORAGE_CHART.id(), Arrays.asList(storageHistory), interval, 1, "MANA", false));
            informationList.addIfAbsent(DisplayRegistry.MANA_THROUGHPUT.id(),
                    DisplayComponent.lineChart(DisplayRegistry.MANA_THROUGHPUT.id(), Arrays.asList(throughputHistory), interval,
                            MonitorResourceHistory.RATE_SCALE, "MANA/s", false));
        }
        return informationList;
    }

    @Override
    public List<ResourceLocation> getAvailableRLs() {
        var rls = ITeamInformationProvider.super.getAvailableRLs();
        rls.addAll(List.of(DisplayRegistry.MANA_CURRENT.id(), DisplayRegistry.MANA_CHANGE.id(),
                DisplayRegistry.MANA_STORAGE_CHART.id(), DisplayRegistry.MANA_THROUGHPUT.id()));
        return rls;
    }

    @Override
    public Widget createConfigurationPage() {
        return Form.page().addChildren(
                Form.choiceRow(Component.translatable("gtocore.machine.monitor.chart_window"), 3,
                        index -> Component.translatable("gtocore.machine.monitor.chart_window." + index),
                        () -> chartWindow, value -> {
                            chartWindow = value;
                            onChanged();
                            requestSync();
                        }),
                createBackgroundRow(),
                createPriorityRow());
    }
}

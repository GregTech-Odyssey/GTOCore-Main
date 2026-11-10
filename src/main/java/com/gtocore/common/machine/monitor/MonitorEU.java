package com.gtocore.common.machine.monitor;

import com.gtocore.common.wireless.energy.GridReadouts;
import com.gtocore.common.wireless.energy.map.GridFormat;

import com.gtolib.api.gui.MonitorMetricDisplay;
import com.gtolib.api.gui.MonitorResourceHistory;
import com.gtolib.api.wireless.energy.EnergyStats;
import com.gtolib.api.wireless.energy.GridClock;
import com.gtolib.api.wireless.energy.GridSampler;
import com.gtolib.api.wireless.energy.WirelessGrid;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.uipro.elements.Form;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;

import com.gto.datasynclib.annotations.Access;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.google.common.collect.ImmutableBiMap;
import com.hepdd.gtmthings.utils.FormatUtil;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.suried.gtoutils.schedule.Task;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class MonitorEU extends AbstractInfoProviderMonitor implements ITeamInformationProvider {

    private final MonitorMetricDisplay localTierDisplay = new MonitorMetricDisplay(DisplayRegistry.EU_LOCAL_TIER, 0);
    private final MonitorMetricDisplay networkTierDisplay = new MonitorMetricDisplay(DisplayRegistry.EU_TIER, 0);
    private final MonitorResourceHistory storageSamples = new MonitorResourceHistory();
    private int sampledStorageWindow = -1;
    private List<FormattedCharSequence> textListCache;
    private static final ImmutableBiMap<Integer, DisplayRegistry> DISPLAY_REGISTRY = ImmutableBiMap.<Integer, DisplayRegistry>builder()
            .put(0, DisplayRegistry.TOTAL_ENERGY)
            .put(1, DisplayRegistry.ENERGY_TRANSFER_LIMIT)
            .put(2, DisplayRegistry.ENERGY_STAT_TITLE)
            .put(3, DisplayRegistry.ENERGY_STAT_MINUTE)
            .put(4, DisplayRegistry.ENERGY_STAT_HOUR)
            .put(5, DisplayRegistry.ENERGY_STAT_DAY)
            .put(6, DisplayRegistry.ENERGY_STAT_NOW)
            .put(7, DisplayRegistry.ENERGY_STAT_TITLE_INPUT)
            .put(8, DisplayRegistry.ENERGY_STAT_MINUTE_INPUT)
            .put(9, DisplayRegistry.ENERGY_STAT_HOUR_INPUT)
            .put(10, DisplayRegistry.ENERGY_STAT_DAY_INPUT)
            .put(11, DisplayRegistry.ENERGY_STAT_NOW_INPUT)
            .put(12, DisplayRegistry.ENERGY_STAT_TITLE_OUTPUT)
            .put(13, DisplayRegistry.ENERGY_STAT_MINUTE_OUTPUT)
            .put(14, DisplayRegistry.ENERGY_STAT_HOUR_OUTPUT)
            .put(15, DisplayRegistry.ENERGY_STAT_DAY_OUTPUT)
            .put(16, DisplayRegistry.ENERGY_STAT_NOW_OUTPUT)
            .put(17, DisplayRegistry.ENERGY_STAT_REMAINING_TIME)
            .put(18, DisplayRegistry.ENERGY_STAT_BOUND_INFO)
            .build();
    @SyncToClient
    private Component[] bufferCache = new Component[0];

    @SyncToClient
    private float energyFullness = 0.0f;
    @SyncToClient
    private String storageLabel = "";
    @SyncToClient
    private BigInteger[] storageHistory = MonitorResourceHistory.EMPTY_VALUES;

    @SyncToClient
    private int localTier;
    @SyncToClient
    private int networkTier;
    @SyncToClient
    private Component[] topologyLabels = new Component[0];
    @SyncToClient
    private int[] topologyEdges = new int[0];
    private int topologyRevision = -1;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private int chartWindow;
    @Nullable
    private GridSampler sampler;

    @SyncToClient
    @Access
    private ArrayList<String> EnergyInputHistoryDay = new ArrayList<>();

    @SyncToClient
    @Access
    private ArrayList<String> EnergyOutputHistoryDay = new ArrayList<>();

    @SyncToClient
    @Access
    private ArrayList<String> EnergyInputHistoryHour = new ArrayList<>();

    @SyncToClient
    @Access
    private ArrayList<String> EnergyOutputHistoryHour = new ArrayList<>();

    @SyncToClient
    @Access
    private ArrayList<String> EnergyInputHistoryMinute = new ArrayList<>();

    @SyncToClient
    @Access
    private ArrayList<String> EnergyOutputHistoryMinute = new ArrayList<>();

    public MonitorEU(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onUnload() {
        if (sampler != null) {
            sampler.release();
            sampler = null;
        }
        super.onUnload();
    }

    @Override
    protected Task subscribe(Runnable runnable) {
        // 吞吐读取会清理到期预约，必须与配方结算在服务端线程串行执行。
        return this.subscribeServerTick(tickableSubscription, runnable, 10);
    }

    @Override
    protected void clientTick() {
        super.clientTick();
        if (getOffsetTimer() % 10 == 0 && bufferCache != null) {
            textListCache = Stream.of(bufferCache)
                    .map(component -> Objects.requireNonNullElse(component, Component.empty()))
                    .map(Component::getVisualOrderText)
                    .toList();

        }
    }

    @Override
    public void syncInfoFromServer() {
        if (textListCache == null) {
            bufferCache = getComponentArray();
        }
    }

    public Component[] getComponentArray() {
        if (!(getLevel() instanceof ServerLevel)) return new Component[0];
        var account = WirelessGrid.accountIfPresent(getUUID());
        if (account.isNone()) {
            storageSamples.reset();
            storageHistory = MonitorResourceHistory.EMPTY_VALUES;
            sampledStorageWindow = -1;
            if (sampler != null) {
                sampler.release();
                sampler = null;
            }
            return new Component[0];
        }
        if (sampler == null || sampler.account() != account) {
            storageSamples.reset();
            if (sampler != null) sampler.release();
            sampler = GridSampler.acquire(account);
            topologyRevision = -1;
        }
        sampler.sample();
        var topology = sampler.topology();
        if (topology.revision() != topologyRevision) {
            topologyRevision = topology.revision();
            topologyLabels = new Component[topology.nodes().size()];
            for (int i = 0; i < topologyLabels.length; i++) topologyLabels[i] = GridFormat.bodyName(topology.nodes().get(i).dimension());
            topologyEdges = new int[topology.lines().size() * 2];
            for (int i = 0; i < topology.lines().size(); i++) {
                topologyEdges[i * 2] = topology.lines().get(i).a();
                topologyEdges[i * 2 + 1] = topology.lines().get(i).b();
            }
        }
        int second = GridClock.second();
        var stats = account.stats();
        var list = new ArrayList<Component>(DISPLAY_REGISTRY.size());
        var node = account.node(getLevel().dimension());
        int here = account.unitTier(getLevel().dimension());
        int all = account.unitTier(null);
        localTier = here;
        networkTier = all;
        list.add(GridReadouts.storage(account));
        list.add(GridReadouts.throughput(node, true, here));
        GridReadouts.flow(list, GridReadouts.Flow.NET, stats, second, all);
        GridReadouts.flow(list, GridReadouts.Flow.INPUT, stats, second, all);
        GridReadouts.flow(list, GridReadouts.Flow.OUTPUT, stats, second, all);
        list.add(GridReadouts.fillOrDrain(account, stats, second));
        list.add(GridReadouts.throughput(node, false, here));

        var lines = new Component[DISPLAY_REGISTRY.size()];
        for (int i = 0, n = Math.min(lines.length, list.size()); i < n; i++) lines[i] = list.get(i);
        double capacity = account.totalCapacityDouble();
        energyFullness = capacity <= 0 ? 0f : (float) Math.min(1, account.totalStorageDouble() / capacity);
        var stored = account.totalStorage();
        boolean sampled = storageSamples.sample(getLevel().getGameTime(), stored);
        if (sampled || sampledStorageWindow != chartWindow) {
            storageHistory = storageSamples.storage(chartWindow);
            sampledStorageWindow = chartWindow;
        }
        storageLabel = FormatUtil.formatBigIntegerNumberOrSic(stored) + " / " +
                FormatUtil.formatBigIntegerNumberOrSic(account.totalCapacity()) + " EU";
        EnergyInputHistoryDay = history(stats, EnergyStats.Window.DAY, true, second);
        EnergyOutputHistoryDay = history(stats, EnergyStats.Window.DAY, false, second);
        EnergyInputHistoryHour = history(stats, EnergyStats.Window.HOUR, true, second);
        EnergyOutputHistoryHour = history(stats, EnergyStats.Window.HOUR, false, second);
        EnergyInputHistoryMinute = history(stats, EnergyStats.Window.MINUTE, true, second);
        EnergyOutputHistoryMinute = history(stats, EnergyStats.Window.MINUTE, false, second);
        return lines;
    }

    private static ArrayList<String> history(@Nullable EnergyStats stats, EnergyStats.Window window, boolean input, int second) {
        var list = new ArrayList<String>();
        if (stats == null) return list;
        for (double value : stats.history(window, input, second)) list.add(BigDecimal.valueOf(value).toBigInteger().toString());
        return list;
    }

    @SuppressWarnings("all")
    public DisplayComponentList provideInformation() {
        var informationList = ITeamInformationProvider.super.provideInformation();
        if (bufferCache.length == DISPLAY_REGISTRY.size()) {
            informationList.addIfAbsent(
                    DisplayRegistry.EU_STATUS_BAR.id(),
                    DisplayComponent.progressBar(DisplayRegistry.EU_STATUS_BAR.id(), energyFullness, storageLabel));
            informationList.addIfAbsent(DisplayRegistry.EU_LOCAL_TIER.id(),
                    localTierDisplay.setValue(localTier));
            informationList.addIfAbsent(DisplayRegistry.EU_TIER.id(),
                    networkTierDisplay.setValue(networkTier));
            informationList.addIfAbsent(DisplayRegistry.EU_TOPOLOGY.id(),
                    DisplayComponent.topology(DisplayRegistry.EU_TOPOLOGY.id(), topologyLabels, topologyEdges));
            var chartInput = chartWindow == 2 ? EnergyInputHistoryDay : chartWindow == 1 ? EnergyInputHistoryHour : EnergyInputHistoryMinute;
            var chartOutput = chartWindow == 2 ? EnergyOutputHistoryDay : chartWindow == 1 ? EnergyOutputHistoryHour : EnergyOutputHistoryMinute;
            int chartSize = Math.min(chartInput.size(), chartOutput.size());
            var netHistory = new ArrayList<BigInteger>(chartSize);
            for (int i = 0; i < chartSize; i++) netHistory.add(new BigInteger(chartInput.get(i)).subtract(new BigInteger(chartOutput.get(i))));
            int sampleInterval = chartWindow == 2 ? 3600 : chartWindow == 1 ? 60 : 1;
            informationList.addIfAbsent(DisplayRegistry.EU_THROUGHPUT.id(),
                    DisplayComponent.lineChart(DisplayRegistry.EU_THROUGHPUT.id(), netHistory, sampleInterval, sampleInterval * 20.0, "EU/t", false));
            informationList.addIfAbsent(DisplayRegistry.EU_STORAGE_CHART.id(),
                    DisplayComponent.lineChart(DisplayRegistry.EU_STORAGE_CHART.id(), Arrays.asList(storageHistory), sampleInterval, 1, "EU", false));
        }
        return informationList;
    }

    @Override
    public List<ResourceLocation> getAvailableRLs() {
        return new ArrayList<>(List.of(DisplayRegistry.EU_STATUS_BAR.id(), DisplayRegistry.EU_LOCAL_TIER.id(), DisplayRegistry.EU_TIER.id(),
                DisplayRegistry.EU_TOPOLOGY.id(), DisplayRegistry.EU_THROUGHPUT.id(), DisplayRegistry.EU_STORAGE_CHART.id()));
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

package com.gtocore.common.machine.monitor;

import com.gtocore.api.wireless.energy.EnergyStats;
import com.gtocore.api.wireless.energy.GridClock;
import com.gtocore.api.wireless.energy.WirelessGrid;
import com.gtocore.common.wireless.energy.GridReadouts;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;

import com.google.common.collect.ImmutableBiMap;
import com.gto.datasynclib.annotations.Access;
import com.gto.datasynclib.annotations.SyncToClient;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class MonitorEU extends AbstractInfoProviderMonitor implements ITeamInformationProvider {

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

    public MonitorEU(Object o) {
        this((MetaMachineBlockEntity) o);
    }

    public MonitorEU(MetaMachineBlockEntity holder) {
        super(holder);
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
        if (account.isNone()) return new Component[0];
        int second = GridClock.second();
        var stats = account.stats();
        var list = new ArrayList<Component>(DISPLAY_REGISTRY.size());
        list.add(GridReadouts.storage(account));
        list.add(GridReadouts.rate(account));
        GridReadouts.flow(list, GridReadouts.Flow.NET, stats, second);
        GridReadouts.flow(list, GridReadouts.Flow.INPUT, stats, second);
        GridReadouts.flow(list, GridReadouts.Flow.OUTPUT, stats, second);
        list.add(GridReadouts.fillOrDrain(account, stats, second));
        var bind = GridReadouts.binding(account);
        list.add(bind != null ? bind : Component.translatable("gtocore.machine.monitor.eu.no_container").withStyle(ChatFormatting.RED));
        var lines = new Component[DISPLAY_REGISTRY.size()];
        for (int i = 0, n = Math.min(lines.length, list.size()); i < n; i++) lines[i] = list.get(i);
        double capacity = account.totalCapacityDouble();
        energyFullness = capacity <= 0 ? 0f : (float) Math.min(1, account.totalStorageDouble() / capacity);
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
            for (int i = 0; i < bufferCache.length; i++) {
                if (DISPLAY_REGISTRY.containsKey(i) && bufferCache[i] != null) {
                    informationList.addIfAbsent(
                            DISPLAY_REGISTRY.get(i).id(),
                            bufferCache[i].getVisualOrderText());
                }
            }
            informationList.addIfAbsent(
                    DisplayRegistry.EU_STATUS_BAR.id(),
                    DisplayComponent.progressBar(DisplayRegistry.EU_STATUS_BAR.id(), energyFullness, Component.translatable("gtocore.machine.monitor.eu.fullness", String.format("%.2f", energyFullness * 100)).getString()));

            informationList.addIfAbsent(
                    DisplayRegistry.EnergyInputHistoryDay.id(), // 假设你在 DisplayRegistry 中定义了这个ID
                    DisplayComponent.lineChart(DisplayRegistry.EnergyInputHistoryDay.id(), this.EnergyInputHistoryDay.stream().map(BigInteger::new).toList()));

            informationList.addIfAbsent(
                    DisplayRegistry.EnergyOutputHistoryDay.id(), // 假设你在 DisplayRegistry 中定义了这个ID
                    DisplayComponent.lineChart(DisplayRegistry.EnergyOutputHistoryDay.id(), this.EnergyOutputHistoryDay.stream().map(BigInteger::new).toList()));

            informationList.addIfAbsent(
                    DisplayRegistry.EnergyInputHistoryHour.id(), // 假设你在 DisplayRegistry 中定义了这个ID
                    DisplayComponent.lineChart(DisplayRegistry.EnergyInputHistoryHour.id(), this.EnergyInputHistoryHour.stream().map(BigInteger::new).toList()));

            informationList.addIfAbsent(
                    DisplayRegistry.EnergyOutputHistoryHour.id(), // 假设你在 DisplayRegistry 中定义了这个ID
                    DisplayComponent.lineChart(DisplayRegistry.EnergyOutputHistoryHour.id(), this.EnergyOutputHistoryHour.stream().map(BigInteger::new).toList()));

            informationList.addIfAbsent(
                    DisplayRegistry.EnergyInputHistoryMinute.id(), // 假设你在 DisplayRegistry 中定义了这个ID
                    DisplayComponent.lineChart(DisplayRegistry.EnergyInputHistoryMinute.id(), this.EnergyInputHistoryMinute.stream().map(BigInteger::new).toList()));

            informationList.addIfAbsent(
                    DisplayRegistry.EnergyOutputHistoryMinute.id(), // 假设你在 DisplayRegistry 中定义了这个ID
                    DisplayComponent.lineChart(DisplayRegistry.EnergyOutputHistoryMinute.id(), this.EnergyOutputHistoryMinute.stream().map(BigInteger::new).toList()));
        }
        return informationList;
    }

    @Override
    public List<ResourceLocation> getAvailableRLs() {
        var rls = ITeamInformationProvider.super.getAvailableRLs();
        rls.addAll(DISPLAY_REGISTRY.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .map(DisplayRegistry::id)
                .toList());
        rls.add(DisplayRegistry.EU_STATUS_BAR.id());
        rls.add(DisplayRegistry.EnergyInputHistoryDay.id());
        rls.add(DisplayRegistry.EnergyOutputHistoryDay.id());
        rls.add(DisplayRegistry.EnergyInputHistoryHour.id());
        rls.add(DisplayRegistry.EnergyOutputHistoryHour.id());
        rls.add(DisplayRegistry.EnergyInputHistoryMinute.id());
        rls.add(DisplayRegistry.EnergyOutputHistoryMinute.id());
        return rls;
    }
}

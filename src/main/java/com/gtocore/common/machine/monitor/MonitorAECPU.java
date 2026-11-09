package com.gtocore.common.machine.monitor;

import com.gtocore.api.ae2.crafting.ICraftingCpuSource;

import com.gtolib.api.machine.impl.MECPUMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.uipro.elements.Form;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.ArrayList;
import java.util.List;

public class MonitorAECPU extends AbstractAEInfoMonitor {

    @SyncToClient
    private int cpuCount;
    @SyncToClient
    private int busyCount;
    @SyncToClient
    private int superCount;
    @SyncToClient
    private int superBusy;

    @SaveToDisk(defaultValue = "1")
    @SyncToClient
    private int showSupercomputer = 1;

    public MonitorAECPU(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void syncInfoFromServer() {
        var time = (int) getLevel().getGameTime();
        if (time - lastUpdateTime < 20) return;
        lastUpdateTime = time;
        cpuCount = busyCount = superCount = superBusy = 0;
        var node = getGridNode();
        if (!isOnline || node == null || node.getGrid() == null) {
            state = State.NO_GRID;
            return;
        }
        for (var cpu : node.getGrid().getCraftingService().getCpus()) {
            cpuCount++;
            boolean busy = cpu.isBusy();
            if (busy) busyCount++;
            if (cpu instanceof ICraftingCpuSource source && source.gtocore$getSourceMachine() != null &&
                    source.gtocore$getSourceMachine().getController() instanceof MECPUMachine) {
                superCount++;
                if (busy) superBusy++;
            }
        }
        state = State.NORMAL;
    }

    @Override
    public DisplayComponentList provideInformation() {
        var info = super.provideInformation();
        String label = Component.translatable("gtocore.machine.monitor.ae.cpu.occupancy", busyCount, cpuCount).getString();
        if (showSupercomputer == 1 && superCount > 0) {
            label += " · " + Component.translatable("gtocore.machine.monitor.ae.cpu.super_occupancy",
                    superBusy, superCount == Integer.MAX_VALUE ? "∞" : Integer.toString(superCount)).getString();
        }
        info.addIfAbsent(DisplayRegistry.AE_CPU_OCCUPANCY.id(),
                DisplayComponent.progressBar(DisplayRegistry.AE_CPU_OCCUPANCY.id(),
                        cpuCount == 0 ? 0 : busyCount / (float) cpuCount, label));
        return info;
    }

    @Override
    public List<ResourceLocation> getAvailableRLs() {
        return new ArrayList<>(List.of(DisplayRegistry.AE_CPU_OCCUPANCY.id()));
    }

    @Override
    public Widget createConfigurationPage() {
        return Form.page().addChildren(
                Form.choiceRow(Component.translatable("gtocore.machine.monitor.ae.cpu.show_super"), 2,
                        index -> Component.translatable("gtocore.machine.monitor.ae.cpu.show_super." + index),
                        () -> showSupercomputer, value -> {
                            showSupercomputer = value;
                            onChanged();
                            requestSync();
                        }),
                createBackgroundRow(),
                createPriorityRow());
    }
}

package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.data.machines.GTOMachineProtocols;

import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.machine.feature.multiblock.ICustomHighlightMachine;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import earth.terrarium.adastra.api.systems.OxygenApi;
import earth.terrarium.adastra.api.systems.TemperatureApi;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractSpaceStation extends ElectricMultiblockMachine implements ISpacePredicateMachine, ICustomHighlightMachine {

    @Nullable
    private Collection<IWorkInSpaceMachine> spaceMachines;
    private final Set<BlockPos> lastDistributedBlocks = new ObjectOpenHashSet<>();
    @SyncToClient
    private BlockPos[] stationPorts = new BlockPos[0];

    @SaveToDisk(defaultValue = "0")
    protected int ready;
    @Nullable
    private TickableSubscription tickSubscription = null;
    boolean shouldShowReadyText = true;

    private boolean firstLoad;

    AbstractSpaceStation(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
    }

    @Override
    public void onStructureFormed() {
        var assembly = getAssembly();
        if (assembly != null) {
            var ports = new ArrayList<BlockPos>(assembly.ports(GTOMachineProtocols.STATION_DOCKING));
            ports.addAll(assembly.ports(GTOMachineProtocols.STATION_JUNCTION));
            stationPorts = ports.toArray(BlockPos[]::new);
        }
        super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        stationPorts = new BlockPos[0];
        super.onStructureInvalid();
    }

    public BlockPos[] getStationPorts() {
        return stationPorts;
    }

    @Override
    public boolean firstLoad() {
        if (firstLoad) {
            firstLoad = false;
            return true;
        }
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        firstLoad = true;
        if (spaceStationMonitor == null) spaceStationMonitor = holder.monitorTick(GTOTickTimeMonitors.SPACE_STATION, this::tickReady);
        tickSubscription = subscribeServerTick(tickSubscription, spaceStationMonitor);
    }

    /** 空间站 tick 的耗时监控。 */
    private TickTimeMonitor spaceStationMonitor;

    protected void tickReady() {
        var time = getOffsetTimer();
        if (time % 20 == 0) {
            if (getRecipeLogic().isWorking()) {
                int oldReady = ready;
                ready = Math.min(20, ready + 1);
                if (ready == 10 && oldReady < 10) updateSpaceMachines();
            } else ready = Math.max(0, ready - 1);
            if (ready == 0) {
                clearOxygenBlocks();
            }
        }
        if (time % 200 == 0) {
            updateSpaceMachines();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    @Override
    public @Nullable Collection<IWorkInSpaceMachine> getSpaceMachines() {
        return spaceMachines;
    }

    @Override
    public void setSpaceMachines(@Nullable Collection<IWorkInSpaceMachine> spaceMachines) {
        synchronized (this) {
            this.spaceMachines = spaceMachines;
        }
    }

    @Override
    public void resetLastDistributedBlocks(Set<BlockPos> positions) {
        lastDistributedBlocks.removeAll(positions);
        clearOxygenBlocks();
        lastDistributedBlocks.addAll(positions);
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        clearOxygenBlocks();
    }

    public void clearOxygenBlocks() {
        OxygenApi.API.removeOxygen(getLevel(), lastDistributedBlocks);
        TemperatureApi.API.removeTemperature(getLevel(), lastDistributedBlocks);
        lastDistributedBlocks.clear();
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        attachHighlightConfigurators(configuratorPanel);
    }

    public void updateSpaceMachines() {
        if (spaceMachines != null) {
            this.spaceMachines.forEach(m -> m.getRecipeLogic().updateTickSubscription());
        }
    }

    @Override
    public int checkPriority() {
        return 5;
    }

    @Override
    public boolean requiresServerCheck() {
        return true;
    }

    @Override
    public boolean isWorkspaceReady() {
        return ready >= 10;
    }

    @Override
    public Component getWorkspaceNotReadyReason() {
        String pos = getPos().toShortString();
        if (getRecipeLogic().isWorking()) return IdleReason.SPACE_STATION_PREPARING.reason(pos, Math.min(ready * 10, 100));
        if (!isWorkingEnabled()) return IdleReason.SPACE_STATION_PAUSED.reason(pos);
        return IdleReason.SPACE_STATION_NOT_RUNNING.reason(pos, getRecipeLogic().getIdleReason());
    }

    @Override
    public void regressRecipe(RecipeLogic recipeLogic) {
        super.regressRecipe(recipeLogic);
        ready = 0;
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        super.customText(list);
        if (shouldShowReadyText && !MultiblockPage.isScreenText()) list.add(Component.translatable("gtocore.machine.spacestation.ready", Math.min(ready * 10, 100)).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        if (shouldShowReadyText) page.addReading("gtocore.machine.spacestation.ready", MultiblockPage.numberText(() -> Math.min(ready * 10, 100), ""));
    }

    @Override
    public boolean isClean() {
        return isWorkspaceReady();
    }

    public int getReadyCount() {
        return ready;
    }

    public int getOxygenatedBlockCount() {
        return lastDistributedBlocks.size();
    }
}

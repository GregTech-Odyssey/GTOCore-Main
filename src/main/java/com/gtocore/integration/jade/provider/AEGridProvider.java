package com.gtocore.integration.jade.provider;

import com.gtocore.utils.Evaluate;

import com.gtolib.GTOCore;
import com.gtolib.api.ae2.IExpandedGrid;
import com.gtolib.api.ae2.StorageTopologyMonitor;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.blockentity.IObserved;

import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.IInWorldGridNodeHost;
import appeng.capabilities.Capabilities;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;

@DataGeneratorScanned
public final class AEGridProvider extends CapabilityBlockProvider<IExpandedGrid> {

    public static boolean OBSERVE = false;

    @RegisterLanguage(cn = "节点数量：%s", en = "Node Amount: %s")
    private static final String NODE_AMOUNT = "gtocore.jade.node_amount";
    @RegisterLanguage(cn = "网络耗时：%s", en = "Grid Tick Time: %s")
    public static final String TICK_TIME = "gtocore.jade.grid_tick_time";
    @RegisterLanguage(cn = "网络存储系统：%s(复杂度：%s)", en = "Grid Storage Evaluate: %s (complexity: %s)")
    public static final String STORAGE_EVALUATE = "gtocore.jade.grid_storage_evaluate";
    @RegisterLanguage(cn = "30 秒内网络拓扑变更 %s 次", en = "Network Topology Changed %s Times in 30s")
    public static final String TOPOLOGY_CHURN = "gtocore.jade.grid_topology_churn";
    @RegisterLanguage(cn = "复杂度：网络内各挂载存储的操作开销之和，越高越慢", en = "Complexity: sum of the operation cost of the mounted storages, higher is slower")
    public static final String STORAGE_EVALUATE_DETAIL = "gtocore.jade.grid_storage_evaluate.detail";
    @RegisterLanguage(cn = "拓扑变更：每次变更都会让所有网络的存储缓存失效，稳定后不该持续变更", en = "Topology Change: every change invalidates the storage cache of all grids, a settled grid should not keep changing")
    public static final String TOPOLOGY_CHURN_DETAIL = "gtocore.jade.grid_topology_churn.detail";

    private static final Component SHIFT_HINT = Component.translatable("gtceu.tooltip.hold_shift");

    public AEGridProvider() {
        super(GTOCore.id("ae_grid_provider"));
    }

    @Nullable
    @Override
    protected IExpandedGrid getCapability(Level level, BlockPos pos, BlockEntity blockEntity, @Nullable Direction side) {
        if (side != null || blockEntity == null) return null;
        if (blockEntity instanceof IObserved observed) {
            observed.onObserved();
        } else if (blockEntity instanceof SpecialFlowerBlockEntity flower) {
            flower.markObserved();
        }
        IInWorldGridNodeHost cap;
        if (blockEntity instanceof IInWorldGridNodeHost host) {
            cap = host;
        } else {
            cap = blockEntity.getCapability(Capabilities.IN_WORLD_GRID_NODE_HOST).orElse(null);
        }
        if (cap != null) {
            for (var s : Direction.values()) {
                var node = cap.getGridNode(s);
                if (node != null && node.getGrid() instanceof IExpandedGrid grid) {
                    return grid;
                }
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, IExpandedGrid capability) {
        if (capability != null) {
            data.putString("name", capability.toString());
            data.putInt("size", capability.size());
            data.putLong("tick_time", capability.getAverageTickTimeMicros());
            data.putInt("storage_opt", capability.getStorageService().getInventory().operationTime());
            data.putInt("topology_churn", StorageTopologyMonitor.recentChanges());
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        String name = capData.getString("name");
        if (name.isEmpty()) return;
        tooltip.add(Component.translatable(name));
        tooltip.add(Component.translatable(NODE_AMOUNT, capData.getInt("size")));
        long tickTime = capData.getLong("tick_time");
        if (tickTime != 0) {
            tooltip.add(Component.translatable(TICK_TIME, tickTime).append(" μs"));
        }
        int storage_opt = capData.getInt("storage_opt");
        if (storage_opt != 0) {
            tooltip.add(Component.translatable(STORAGE_EVALUATE, Evaluate.fromScore(100 - storage_opt).getComponent(), storage_opt));
        }
        int topologyChurn = capData.getInt("topology_churn");
        if (topologyChurn > 0) {
            tooltip.add(Component.translatable(TOPOLOGY_CHURN, topologyChurn).withStyle(ChatFormatting.RED));
        }
        if (storage_opt == 0 && topologyChurn == 0) return;
        if (!block.showDetails()) {
            tooltip.add(SHIFT_HINT);
            return;
        }
        if (storage_opt != 0) {
            tooltip.add(Component.translatable(STORAGE_EVALUATE_DETAIL).withStyle(ChatFormatting.GRAY));
        }
        if (topologyChurn != 0) {
            tooltip.add(Component.translatable(TOPOLOGY_CHURN_DETAIL).withStyle(ChatFormatting.GRAY));
        }
    }
}

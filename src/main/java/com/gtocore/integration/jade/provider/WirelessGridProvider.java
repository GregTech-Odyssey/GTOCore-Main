package com.gtocore.integration.jade.provider;

import com.gtocore.integration.ae.wireless.WirelessClientCache;
import com.gtocore.integration.ae.wireless.WirelessMachine;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade：已加入无线网络的机器显示"已连接：网络名"与连接状态（在线 / 离线 / 无权限）。
 * 服务端只下发网络 id 与状态；网络名在客户端从按玩家过滤的 {@link WirelessClientCache} 取，无权访问的网络不显示名称。
 */
public final class WirelessGridProvider extends CapabilityBlockProvider<WirelessMachine> {

    public WirelessGridProvider() {
        super(GTOCore.id("wireless_grid_provider"));
    }

    /** 与朝向无关：只按 null 面取一次，避免每个面重复写同样的数据。 */
    @Override
    protected @Nullable WirelessMachine getCapability(Level level, BlockPos pos, BlockEntity blockEntity, @Nullable Direction side) {
        return side == null && MetaMachine.getMachine(blockEntity) instanceof WirelessMachine machine ? machine : null;
    }

    @Override
    protected void write(CompoundTag data, WirelessMachine capability) {
        var id = capability.getWirelessNetworkId();
        if (id.isEmpty()) return;
        data.putString("network", id);
        data.putInt("state", capability.getWirelessLinkState().ordinal());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        var id = capData.getString("network");
        if (id.isEmpty()) return;
        var name = WirelessClientCache.name(id);
        tooltip.add(Component.translatable(WirelessMachine.KEY_CONNECTED,
                name == null ? Component.translatable(WirelessMachine.KEY_UNKNOWN_NETWORK) : Component.literal(name)));
        var states = WirelessMachine.LinkState.values();
        int state = capData.getInt("state");
        if (state >= 0 && state < states.length) tooltip.add(states[state].describe());
    }
}

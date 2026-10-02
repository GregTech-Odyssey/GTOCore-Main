package com.gtocore.integration.jade.provider

import com.gtocore.integration.ae.wireless.WirelessClientCache
import com.gtocore.integration.ae.wireless.WirelessMachine

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider
import com.gtolib.GTOCore
import snownee.jade.api.BlockAccessor
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

/** 服务端只下发网络 ID 和状态，名称从按玩家权限过滤的客户端缓存读取。 */
class WirelessGridProvider : CapabilityBlockProvider<WirelessMachine>(GTOCore.id("wireless_grid_provider")) {
    // 只处理 null 面，避免六个朝向重复发送相同数据。
    protected override fun getCapability(level: Level?, pos: BlockPos?, blockEntity: BlockEntity?, side: Direction?): WirelessMachine? = if (side == null) MetaMachine.getMachine(blockEntity) as? WirelessMachine else null

    protected override fun write(data: CompoundTag, capability: WirelessMachine) {
        val id = capability.getWirelessNetworkId()
        if (id.isEmpty()) return
        data.putString("network", id)
        data.putInt("state", capability.getWirelessLinkState().ordinal)
    }

    protected override fun addTooltip(capData: CompoundTag, tooltip: ITooltip, player: Player?, block: BlockAccessor?, blockEntity: BlockEntity?, config: IPluginConfig?) {
        val id = capData.getString("network")
        if (id.isEmpty()) return
        val name = WirelessClientCache.name(id)
        tooltip.add(
            Component.translatable(
                WirelessMachine.KEY_CONNECTED,
                if (name == null) Component.translatable(WirelessMachine.KEY_UNKNOWN_NETWORK) else Component.literal(name),
            ),
        )
        val states = WirelessMachine.LinkState.values()
        val state = capData.getInt("state")
        if (state >= 0 && state < states.size) tooltip.add(states[state].describe())
    }
}

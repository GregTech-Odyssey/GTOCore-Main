package com.gtocore.integration.jade.provider;

import com.gtocore.api.wireless.energy.EnergyPort;
import com.gtocore.api.wireless.energy.IEnergyPortHolder;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;

import net.minecraft.ChatFormatting;
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

@DataGeneratorScanned
public final class WirelessPortProvider extends CapabilityBlockProvider<EnergyPort> {

    @RegisterLanguage(cn = "所在维度未接入电网，%s 秒后重新搜索", en = "No grid in this dimension; searching again in %s s")
    private static final String RETRY = "gtocore.jade.wireless_port.retry";
    @RegisterLanguage(cn = "所在维度未接入电网，正在搜索", en = "No grid in this dimension; searching")
    private static final String SEARCHING = "gtocore.jade.wireless_port.searching";

    public WirelessPortProvider() {
        super(GTOCore.id("wireless_port_provider"));
    }

    @Override
    protected @Nullable EnergyPort getCapability(Level level, BlockPos pos, BlockEntity blockEntity, @Nullable Direction side) {
        return side == null && MetaMachine.getMachine(blockEntity) instanceof IEnergyPortHolder holder ? holder.getEnergyPort() : null;
    }

    @Override
    protected void write(CompoundTag data, EnergyPort port) {
        if (port.isSearching()) data.putInt("retry", port.retrySeconds());
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        if (!capData.contains("retry")) return;
        int seconds = capData.getInt("retry");
        tooltip.add((seconds > 0 ? Component.translatable(RETRY, seconds) : Component.translatable(SEARCHING)).withStyle(ChatFormatting.YELLOW));
    }
}

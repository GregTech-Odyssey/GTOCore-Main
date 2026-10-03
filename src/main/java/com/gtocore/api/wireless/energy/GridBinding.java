package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;

import com.hepdd.gtmthings.api.machine.IPowerSubstationMachine;
import com.hepdd.gtmthings.utils.TeamUtil;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;

public final class GridBinding {

    static final int RATE_REFRESH_TICKS = 200;

    private GridBinding() {}

    public static long bindingRate(BlockGetter level, BlockPos pos) {
        long rate = 0;
        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine instanceof BatteryBufferMachine batteryBufferMachine) {
            var inv = batteryBufferMachine.getBatteryInventory();
            for (int i = 0; i < inv.getSlots(); i++) {
                var electricItem = GTCapabilityHelper.getElectricItem(inv.getStackInSlot(i));
                if (electricItem != null) rate = U126.saturatedAdd(rate, GTValues.VEX[electricItem.getTier()]);
            }
        } else if (machine instanceof IPowerSubstationMachine substation && substation.isFormed()) {
            var capacity = substation.getEnergyInfo().capacity().divide(BigInteger.valueOf(4096));
            rate = capacity.bitLength() > 63 ? Long.MAX_VALUE : capacity.longValue();
        }
        return rate;
    }

    public static boolean bind(UUID player, GlobalPos pos, long rate) {
        var account = WirelessGrid.accountOf(player);
        if (account.isNone()) return false;
        account.rate = rate;
        account.bindPos = pos;
        return true;
    }

    public static boolean mayRebind(Player player, @Nullable UUID currentOwner) {
        if (currentOwner == null || currentOwner.equals(player.getUUID()) || player.hasPermissions(2)) return true;
        return TeamUtil.getTeamUUID(currentOwner).equals(TeamUtil.getTeamUUID(player.getUUID()));
    }

    public static boolean setOwner(MetaMachine machine, Player player, boolean bind) {
        if (!mayRebind(player, machine.getOwnerUUID())) {
            player.sendSystemMessage(Component.translatable(WirelessText.REBIND_DENIED));
            return false;
        }
        if (bind) {
            machine.setOwnerUUID(player.getUUID());
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.bind", TeamUtil.getName(player)));
        } else {
            machine.setOwnerUUID(null);
            player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.unbind"));
        }
        return true;
    }

    static void refreshRates(WirelessGrid grid, MinecraftServer server) {
        for (var account : grid.accounts.values()) {
            var pos = account.bindPos;
            if (pos == null) continue;
            var level = server.getLevel(pos.dimension());
            if (level == null || !level.isLoaded(pos.pos())) continue;
            account.rate = bindingRate(level, pos.pos());
        }
    }
}

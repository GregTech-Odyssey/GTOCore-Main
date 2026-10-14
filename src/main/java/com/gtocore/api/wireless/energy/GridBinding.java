package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.hepdd.gtmthings.utils.TeamUtil;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class GridBinding {

    private GridBinding() {}

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
}

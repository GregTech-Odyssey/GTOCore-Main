package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

final class PortHosts {

    private PortHosts() {}

    record MachineHost(MetaMachine machine) implements EnergyPort.Host {

        @Override
        public @Nullable UUID owner() {
            return machine.getOwnerUUID();
        }

        @Override
        public @Nullable Level level() {
            return machine.getLevel();
        }
    }

    record PlayerHost(Player player) implements EnergyPort.Host {

        @Override
        public UUID owner() {
            return player.getUUID();
        }

        @Override
        public Level level() {
            return player.level();
        }
    }

    record TeamHost(UUID owner, Level level) implements EnergyPort.Host {}
}

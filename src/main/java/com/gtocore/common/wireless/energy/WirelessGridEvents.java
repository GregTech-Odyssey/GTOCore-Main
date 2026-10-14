package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.TeamMerge;
import com.gtocore.api.wireless.energy.WirelessGrid;
import com.gtocore.integration.Mods;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;

public final class WirelessGridEvents {

    private WirelessGridEvents() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, WirelessGridEvents::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, WirelessGridEvents::onLevelLoad);
        MinecraftForge.EVENT_BUS.addListener(WirelessGridEvents::onServerStopped);
        if (Mods.FTBTEAMS.isLoaded()) Teams.register();
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) WirelessGrid.onServerTickStart(event.getServer());
        else WirelessGrid.onServerTickEnd(event.getServer());
    }

    private static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            WirelessGrid.load(level.getServer(), level.getDataStorage());
        }
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        WirelessGrid.unload();
    }

    private static final class Teams {

        static void register() {
            TeamEvent.PLAYER_JOINED_PARTY.register(Teams::onJoined);
            TeamEvent.PLAYER_LEFT_PARTY.register(Teams::onLeft);
            TeamEvent.PLAYER_CHANGED.register(Teams::onChanged);
        }

        private static void onJoined(PlayerJoinedPartyTeamEvent event) {
            TeamMerge.onTeamJoined(event.getPlayer().getUUID(), event.getPreviousTeam().getId(), event.getTeam().getId());
        }

        private static void onLeft(PlayerLeftPartyTeamEvent event) {
            TeamMerge.onTeamLeft(event.getPlayerId(), event.getTeam().getId(), event.getPlayerTeam().getId(), event.getTeamDeleted());
        }

        private static void onChanged(PlayerChangedTeamEvent event) {
            TeamMerge.onTeamChanged(event.getPreviousTeam().map(t -> t.getId()).orElse(null), event.getTeam().getId());
        }
    }
}

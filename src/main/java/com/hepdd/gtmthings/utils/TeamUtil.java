package com.hepdd.gtmthings.utils;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.UsernameCache;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;

import java.util.UUID;

import javax.annotation.Nullable;

public class TeamUtil {

    private static final boolean isFTBTeamsLoaded = GTCEu.isModLoaded("ftbteams");

    public static UUID getTeamUUID(UUID playerUUID) {
        if (isFTBTeamsLoaded && FTBTeamsAPI.api().isManagerLoaded()) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            return team.map(Team::getTeamId).orElse(playerUUID);
        } else if (isFTBTeamsLoaded && FTBTeamsAPI.api().isClientManagerLoaded()) {
            // Multiplayer client-side
            var team = FTBTeamsAPI.api().getClientManager().getTeams().stream().filter(
                    t -> t.getMembers().contains(playerUUID)).findFirst();
            if (team.isPresent() && team.get().isPartyTeam()) {
                return team.get().getTeamId();
            }
        }

        return playerUUID;
    }

    public static Component GetName(Player player) {
        Component name = findTeamOrPlayerName(player.level(), player.getUUID());
        return name == null ? player.getName() : name;
    }

    public static Component GetName(Level level, UUID playerUUID) {
        Component name = findTeamOrPlayerName(level, playerUUID);
        return name == null ? Component.literal(playerUUID.toString()) : name;
    }

    @Nullable
    public static Component findTeamOrPlayerName(Level level, UUID playerUUID) {
        Component teamName = findTeamName(playerUUID);
        if (teamName != null) {
            return teamName;
        }
        return findPlayerName(level, playerUUID);
    }

    @Nullable
    public static Component findPlayerOrTeamName(Level level, UUID playerUUID) {
        Component playerName = findPlayerName(level, playerUUID);
        if (playerName != null) {
            return playerName;
        }
        return findTeamName(playerUUID);
    }

    @Nullable
    public static Component findTeamName(UUID playerUUID) {
        if (isFTBTeamsLoaded && FTBTeamsAPI.api().isManagerLoaded()) {
            var team = FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID);
            if (team.isPresent()) {
                return team.get().getName();
            }
        } else if (isFTBTeamsLoaded && FTBTeamsAPI.api().isClientManagerLoaded()) {
            // Multiplayer client-side
            var team = FTBTeamsAPI.api().getClientManager().getTeams().stream().filter(
                    t -> t.getMembers().contains(playerUUID)).findFirst();
            if (team.isPresent() && team.get().isPartyTeam()) {
                return team.get().getName();
            }
        }

        return null;
    }

    @Nullable
    public static Component findPlayerName(Level level, UUID playerUUID) {
        Player player = level.getPlayerByUUID(playerUUID);
        if (player != null) return player.getName();

        if (isFTBTeamsLoaded && FTBTeamsAPI.api().isClientManagerLoaded()) {
            var knownPlayer = FTBTeamsAPI.api().getClientManager().getKnownPlayer(playerUUID);
            if (knownPlayer.isPresent()) {
                var name = normalize(knownPlayer.get().name());
                if (name != null) return Component.literal(name);
            }
        }

        if (isFTBTeamsLoaded && FTBTeamsAPI.api().isManagerLoaded()) {
            var team = FTBTeamsAPI.api().getManager().getKnownPlayerTeams().get(playerUUID);
            if (team != null && team.isPlayerTeam()) {
                var name = normalize(team.getName().getString());
                if (name != null) return Component.literal(name);
            }
        }

        var cachedName = normalize(UsernameCache.getLastKnownUsername(playerUUID));
        return cachedName == null ? null : Component.literal(cachedName);
    }

    public static boolean hasOwner(Level level, UUID playerUUID) {
        return findTeamOrPlayerName(level, playerUUID) != null;
    }

    @Nullable
    private static String normalize(@Nullable String name) {
        return name == null || name.isBlank() || name.equalsIgnoreCase("Unknown") ? null : name;
    }
}

package com.gtocore.utils

import net.minecraft.world.level.Level
import net.minecraftforge.common.UsernameCache
import net.minecraftforge.fml.ModList

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI

import java.util.*

object PlayerNameUtils {
    const val UNKNOWN = "Unknown"

    private val ftbLoaded = ModList.get().isLoaded("ftbteams")

    @JvmStatic
    fun getLastKnownName(level: Level?, uuid: UUID?): String = findLastKnownName(level, uuid) ?: UNKNOWN

    @JvmStatic
    fun findLastKnownName(level: Level?, uuid: UUID?): String? {
        if (uuid == null) return null
        level?.getPlayerByUUID(uuid)?.let { return normalize(it.name.string) }
        findFromFTBTeams(uuid)?.let { return it }
        return normalize(UsernameCache.getLastKnownUsername(uuid))
    }

    private fun findFromFTBTeams(uuid: UUID): String? {
        if (!ftbLoaded) return null

        val api = FTBTeamsAPI.api()
        if (api.isClientManagerLoaded) {
            val knownPlayer = api.clientManager.getKnownPlayer(uuid)
            if (knownPlayer.isPresent) {
                return normalize(knownPlayer.get().name())
            }
        }

        if (api.isManagerLoaded) {
            val team = api.manager.knownPlayerTeams[uuid]
            if (team != null && team.isPlayerTeam) {
                return normalize(team.name.string)
            }
        }

        return null
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun normalize(name: String?): String? {
        if (name == null) return null
        name as java.lang.String
        return if (name.isBlank || name.equalsIgnoreCase(UNKNOWN)) null else name
    }
}

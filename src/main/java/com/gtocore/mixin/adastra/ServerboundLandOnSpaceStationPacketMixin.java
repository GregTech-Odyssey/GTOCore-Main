package com.gtocore.mixin.adastra;

import com.gtolib.api.dimension.DimensionStations;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import earth.terrarium.adastra.common.network.messages.ServerboundLandOnSpaceStationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerboundLandOnSpaceStationPacket.class)
public class ServerboundLandOnSpaceStationPacketMixin {

    /**
     * @author GTOCore
     * @reason Ownership queries share the metadata-only check performed before loading.
     */
    @Overwrite(remap = false)
    private static boolean isAllowed(ServerPlayer player, ServerLevel level, ChunkPos targetPos) {
        return DimensionStations.mayLand(player, level.dimension(), targetPos);
    }
}

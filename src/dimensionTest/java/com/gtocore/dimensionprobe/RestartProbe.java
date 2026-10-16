package com.gtocore.dimensionprobe;

import com.gtolib.api.dimension.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.nio.file.*;
import java.util.*;

/**
 * 两次启动同一隔离存档的探针，验证原版、Forge、AE2 及可选 FTB 有效强加载恢复，并排除过期或无资格票据。
 */
public final class RestartProbe {

    RestartProbe() {
        ForgeChunkManager.setForcedChunkLoadingCallback("dimension_probe", (level, helper) -> {
            for (var position : List.copyOf(helper.getBlockTickets().keySet())) helper.removeAllTickets(position);
        });
        MinecraftForge.EVENT_BUS.addListener(this::started);
    }

    private void started(ServerStartedEvent event) {
        var server = event.getServer();
        var manager = DimensionManager.get(server);
        try {
            Path marker = Path.of("restart-stage-v2.txt");
            boolean coreOnly = Boolean.getBoolean("dimensionRestartCoreProbe");
            if (!Files.exists(marker)) {
                var owner = new OwnerRef(OwnerRef.Kind.PLAYER, UUID.fromString("be10b40e-9369-49f7-a089-b666cba74a4a"));
                var valid = manager.getOrCreatePrivate(owner, DimensionTemplates.VOID, "restart-valid", 928173L);
                var invalid = manager.getOrCreatePrivate(owner, DimensionTemplates.VOID, "restart-invalid", 182391L);
                var level = manager.loadNow(valid.dimension());
                level.setChunkForced(0, 0, true);
                var anchor = new BlockPos(1, 64, 1);
                level.setBlockAndUpdate(anchor, appeng.core.definitions.AEBlocks.SPATIAL_ANCHOR.block().defaultBlockState());
                require(ForgeChunkManager.forceChunk(level, "ae2", anchor, 0, 0, true, true), "AE2 force ticket was rejected");
                var invalidLevel = manager.loadNow(invalid.dimension());
                require(ForgeChunkManager.forceChunk(invalidLevel, "dimension_probe", BlockPos.ZERO, 0, 0, true, true), "Invalid callback candidate was not saved");
                if (coreOnly) {
                    Files.writeString(marker, valid.dimension().location() + "\n" + invalid.dimension().location());
                    Files.writeString(Path.of("restart-result.txt"), "RESTART_SAVED");
                    return;
                }
                var team = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager().createServerTeam(server.createCommandSourceStack(), "DimensionForceProbe_" + UUID.randomUUID(), "", dev.ftb.mods.ftblibrary.icon.Color4I.WHITE, UUID.randomUUID());
                var ftb = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.TEAM, team.getId()), DimensionTemplates.VOID, "restart-ftb", 83192L);
                require(server.getLevel(ftb.dimension()) == null, "FTB metadata creation woke terrain");
                var claims = dev.ftb.mods.ftbchunks.api.FTBChunksAPI.api().getManager().getOrCreateData(team);
                var position = new dev.ftb.mods.ftblibrary.math.ChunkDimPos(ftb.dimension(), 0, 0);
                require(claims.claim(server.createCommandSourceStack(), position, false).isSuccess(), "FTB claim failed");
                require(claims.forceLoad(server.createCommandSourceStack(), position, false).isSuccess(), "FTB force loading failed");
                require(server.getLevel(ftb.dimension()) != null && ForgeChunkManager.hasForcedChunks(server.getLevel(ftb.dimension())), "Eligible FTB force request did not load a dormant instance");
                var expired = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.TEAM, team.getId()), DimensionTemplates.VOID, "expired-ftb", 82193L);
                var expiredPosition = new dev.ftb.mods.ftblibrary.math.ChunkDimPos(expired.dimension(), 0, 0);
                require(claims.claim(server.createCommandSourceStack(), expiredPosition, false).isSuccess(), "Expired claim setup failed");
                var expiredClaim = dev.ftb.mods.ftbchunks.api.FTBChunksAPI.api().getManager().getChunk(expiredPosition);
                expiredClaim.setForceLoadExpiryTime(System.currentTimeMillis() - 1);
                require(claims.forceLoad(server.createCommandSourceStack(), expiredPosition, false).isSuccess(), "Expired claim metadata did not accept setup");
                require(server.getLevel(expired.dimension()) == null && !expiredClaim.isActuallyForceLoaded(), "Expired FTB claim woke a dormant instance");
                var denied = new net.minecraft.server.level.ServerPlayer(server, server.overworld(), new com.mojang.authlib.GameProfile(UUID.randomUUID(), "ForceDenied"));
                var forbidden = manager.getOrCreatePrivate(owner, DimensionTemplates.VOID, "forbidden-ftb", 28312L);
                var forbiddenPosition = new dev.ftb.mods.ftblibrary.math.ChunkDimPos(forbidden.dimension(), 0, 0);
                claims.claim(server.createCommandSourceStack(), forbiddenPosition, false);
                require(!claims.forceLoad(denied.createCommandSourceStack(), forbiddenPosition, false).isSuccess() && server.getLevel(forbidden.dimension()) == null, "Unauthorized FTB force request woke private terrain");
                dev.ftb.mods.ftbchunks.FTBChunksWorldConfig.FORCE_LOAD_MODE.set(dev.ftb.mods.ftbchunks.data.ForceLoadMode.NEVER);
                var offlineTeam = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager().createServerTeam(server.createCommandSourceStack(), "NoForceProbe_" + UUID.randomUUID(), "", dev.ftb.mods.ftblibrary.icon.Color4I.WHITE, UUID.randomUUID());
                var offline = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.TEAM, offlineTeam.getId()), DimensionTemplates.VOID, "offline-ftb", 992193L);
                var offlineClaims = dev.ftb.mods.ftbchunks.api.FTBChunksAPI.api().getManager().getOrCreateData(offlineTeam);
                var offlinePosition = new dev.ftb.mods.ftblibrary.math.ChunkDimPos(offline.dimension(), 0, 0);
                offlineClaims.claim(server.createCommandSourceStack(), offlinePosition, false);
                offlineClaims.forceLoad(server.createCommandSourceStack(), offlinePosition, false);
                require(server.getLevel(offline.dimension()) == null && !offlineClaims.canDoOfflineForceLoading(), "Disabled FTB force loading woke an instance");
                offlineClaims.unForceLoad(server.createCommandSourceStack(), offlinePosition, false);
                dev.ftb.mods.ftbchunks.FTBChunksWorldConfig.FORCE_LOAD_MODE.set(dev.ftb.mods.ftbchunks.data.ForceLoadMode.ALWAYS);
                Files.writeString(marker, valid.dimension().location() + "\n" + invalid.dimension().location() + "\n" + ftb.dimension().location());
                Files.writeString(Path.of("restart-result.txt"), "RESTART_SAVED");
            } else {
                var keys = Files.readAllLines(marker);
                var valid = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(keys.getFirst()));
                var invalid = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(keys.get(1)));
                var level = server.getLevel(valid);
                require(level != null && level.getSeed() == 928173L, "Forced instance did not restore its seed");
                require(level.getForcedChunks().contains(0L), "Vanilla forced chunk was not restored");
                var saved = level.getDataStorage().get(net.minecraft.world.level.ForcedChunksSavedData::load, "chunks");
                require(!saved.getBlockForcedChunks().isEmpty(), "AE2 valid anchor ticket was removed");
                require(server.getLevel(invalid) == null && manager.descriptor(invalid) != null, "Invalid Forge candidate remained loaded or lost its definition");
                require(manager.levels().size() == (coreOnly ? 4 : 5), "Startup loaded unrelated history: " + manager.levels().size());
                require(level.getBlockEntity(new BlockPos(1, 64, 1)) instanceof appeng.blockentity.spatial.SpatialAnchorBlockEntity, "Anchor block entity did not persist");
                level.setChunkForced(0, 0, false);
                ForgeChunkManager.forceChunk(level, "ae2", new BlockPos(1, 64, 1), 0, 0, false, true);
                if (!coreOnly) {
                    var ftb = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(keys.get(2)));
                    require(server.getLevel(ftb) != null && ForgeChunkManager.hasForcedChunks(server.getLevel(ftb)), "FTB valid forced dimension did not restore");
                    var claim = dev.ftb.mods.ftbchunks.api.FTBChunksAPI.api().getManager().getChunk(new dev.ftb.mods.ftblibrary.math.ChunkDimPos(ftb, 0, 0));
                    claim.unload(server.createCommandSourceStack());
                }
                Files.writeString(Path.of("restart-result.txt"), "RESTART_PROBE_PASSED");
            }
        } catch (Throwable failure) {
            failure.printStackTrace();
            try {
                Files.writeString(Path.of("restart-result.txt"), "FAILED: " + failure);
            } catch (Exception ignored) {}
        } finally {
            server.halt(false);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}

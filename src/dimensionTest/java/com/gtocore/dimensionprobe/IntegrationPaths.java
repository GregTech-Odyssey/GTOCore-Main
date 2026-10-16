package com.gtocore.dimensionprobe;

import com.gtolib.GTOCore;
import com.gtolib.api.adastra.PlanetTravel;
import com.gtolib.api.adastra.TravelSource;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.dimension.*;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import earth.terrarium.adastra.api.planets.PlanetApi;
import earth.terrarium.adastra.common.network.messages.ServerboundConstructSpaceStationPacket;
import earth.terrarium.adastra.common.network.messages.ServerboundLandOnSpaceStationPacket;
import earth.terrarium.adastra.common.network.messages.ServerboundLandPacket;

import java.util.List;
import java.util.UUID;

/**
 * 联机探针中的实际玩法入口验证，覆盖命令、虚空传送设备、空间交换、星球旅行、空间站、重生及队伍权限。
 */
final class IntegrationPaths {

    private final DimensionManager manager;
    private ResourceKey<Level> orbit;
    private ChunkPos station;
    private int phase, ticks;

    IntegrationPaths(DimensionManager manager) {
        this.manager = manager;
    }

    boolean tick(List<ServerPlayer> players) {
        if (++ticks > 2400) throw new AssertionError("Integration paths timed out at " + phase);
        var owner = players.getFirst();
        var guest = players.get(1);
        var server = owner.server;
        if (phase == 0) {
            String slot = "commands-" + UUID.randomUUID();
            command(owner, "gtocore dimensions templates", true);
            command(owner, "gtocore dimensions private create player " + owner.getUUID() + " gtocore:private_flat " + slot + " 94213", true);
            var instance = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.PLAYER, owner.getUUID()), DimensionTemplates.FLAT, slot, null);
            require(server.getLevel(instance.dimension()) == null && instance.seed() == 94213, "Private command woke world or lost explicit seed");
            command(owner, "gtocore dimensions info " + instance.dimension().location(), true);
            command(owner, "gtocore dimensions list 1", true);
            command(owner, "gtocore dimensions tp " + instance.dimension().location() + " " + guest.getGameProfile().getName(), false);
            require(server.getLevel(instance.dimension()) == null, "Denied target selector woke private world");
            command(guest, "execute in " + instance.dimension().location() + " run time query daytime", false);
            require(server.getLevel(instance.dimension()) == null, "Denied vanilla dimension argument woke private world");
            command(owner, "gtocore dimensions grant " + instance.dimension().location() + " " + guest.getUUID(), true);
            command(owner, "gtocore dimensions tp " + instance.dimension().location() + " " + guest.getGameProfile().getName(), true);
            require(guest.serverLevel().dimension() == instance.dimension(), "Authorized selector did not teleport");
            command(owner, "gtocore dimensions revoke " + instance.dimension().location() + " " + guest.getUUID(), true);
            require(guest.serverLevel().dimension() == Level.OVERWORLD, "Command revocation failed to eject guest");
            var loadOnly = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.PLAYER, owner.getUUID()), DimensionTemplates.VOID, slot + "-load", 936L);
            command(owner, "gtocore dimensions load " + loadOnly.dimension().location(), true);
            command(owner, "gtocore dimensions unload " + loadOnly.dimension().location(), true);
            var series = GTOCore.id("commands_" + UUID.randomUUID());
            command(owner, "gtocore dimensions series create " + series + " gtocore:private_void 273819", true);
            long before = manager.instances().count();
            command(owner, "gtocore dimensions series enter " + series + " -9223372036854775808 " + guest.getGameProfile().getName(), true);
            require(manager.instances().count() == before + 1, "Series command created neighbouring addresses");
            require(manager.teleport(guest, Level.OVERWORLD), "Series return failed");
            // Invoke the device's actual consumer using a placed machine block entity.
            var devicePos = new BlockPos(128, 150, 128);
            server.overworld().setBlockAndUpdate(devicePos, com.gtocore.common.data.machines.MultiBlockG.VOID_TRANSPORTER.defaultBlockState());
            var device = (com.gtocore.common.machine.multiblock.electric.voidseries.VoidTransporterMachine) com.gregtechceu.gtceu.api.machine.MetaMachine.getMachine(server.overworld(), devicePos);
            require(device != null, "Void transporter block entity was not constructed");
            var deviceTarget = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.PLAYER, owner.getUUID()), DimensionTemplates.VOID, slot + "-device", 946L);
            var consumer = com.gtocore.common.machine.multiblock.electric.voidseries.VoidTransporterMachine.teleportToDimension(deviceTarget.dimension(), new BlockPos(0, 70, 0));
            consumer.accept(device, guest);
            require(server.getLevel(deviceTarget.dimension()) == null, "Denied void device woke destination");
            consumer.accept(device, owner);
            require(owner.serverLevel().dimension() == deviceTarget.dimension(), "Void device did not load/enter destination");
            require(manager.teleport(owner, Level.OVERWORLD), "Void device return failed");
            // Actual AE2 block swap, including both scoped world leases.
            var spatial = appeng.spatial.SpatialStoragePlotManager.INSTANCE.getLevel();
            var mainPos = new BlockPos(64, 150, 64);
            var spatialPos = new BlockPos(64, 80, 64);
            server.overworld().setBlockAndUpdate(mainPos, Blocks.DIAMOND_BLOCK.defaultBlockState());
            spatial.setBlockAndUpdate(spatialPos, Blocks.GOLD_BLOCK.defaultBlockState());
            appeng.spatial.SpatialStorageHelper.getInstance().swapRegions(server.overworld(), 64, 150, 64, spatial, 64, 80, 64, 0, 0, 0);
            require(server.overworld().getBlockState(mainPos).is(Blocks.GOLD_BLOCK) && spatial.getBlockState(spatialPos).is(Blocks.DIAMOND_BLOCK), "AE2 did not exchange actual blocks");
            require(!manager.keepAliveReasons(spatial.dimension()).contains("SPATIAL_EXCHANGE"), "AE2 exchange retained a lease");
            // Space elevator opens the shared planet menu; the real land handler loads the planet.
            PlanetTravel.open(owner, new PlanetTravel(TravelSource.SPACE_ELEVATOR, 8));
            ServerboundLandPacket.TYPE.handle(new ServerboundLandPacket(GTODimensions.MOON, false)).accept(owner);
            require(owner.serverLevel().dimension() == GTODimensions.MOON, "Space elevator landing did not load/enter moon");
            require(owner.serverLevel().getSeed() == server.overworld().getSeed(), "Existing planet seed changed");
            returnHome(owner);
            // A rocket menu follows the same real handler with an actual rocket passenger.
            var rocket = earth.terrarium.adastra.common.registry.ModEntityTypes.TIER_1_ROCKET.get().create(server.overworld());
            rocket.setPos(owner.getX(), 1100, owner.getZ());
            server.overworld().addFreshEntity(rocket);
            owner.startRiding(rocket, true);
            PlanetTravel.open(owner, new PlanetTravel(TravelSource.ROCKET, rocket.tier()));
            ServerboundLandPacket.TYPE.handle(new ServerboundLandPacket(GTODimensions.MARS, false)).accept(owner);
            require(owner.serverLevel().dimension() == GTODimensions.MARS, "Rocket landing did not load/enter mars");
            returnHome(owner);
            rocket.discard();
            orbit = PlanetApi.API.getPlanet(Level.OVERWORLD).orbitIfPresent();
            require(server.getLevel(orbit) == null, "Orbit was eagerly loaded");
            owner.setGameMode(GameType.CREATIVE);
            PlanetTravel.open(owner, new PlanetTravel(TravelSource.SPACE_ELEVATOR, 8));
            ServerboundConstructSpaceStationPacket.TYPE.handle(new ServerboundConstructSpaceStationPacket(Level.OVERWORLD, Component.literal("Dimension probe station"))).accept(owner);
            require(owner.serverLevel().dimension() == orbit, "Station construction did not load/enter orbit");
            var stations = DimensionStations.query(server, orbit).get(owner.getUUID());
            require(stations != null && !stations.isEmpty(), "Station construction did not persist ownership");
            station = stations.iterator().next().position();
            returnHome(owner);
            owner.setGameMode(GameType.SURVIVAL);
            phase = 1;
            ticks = 0;
        } else if (phase == 1) {
            if (!manager.requestUnload(orbit)) return false;
            phase = 2;
        } else if (phase == 2 && manager.state(orbit) == DimensionLifecycle.State.DORMANT) {
            require(DimensionStations.mayLand(owner, orbit, station), "Sleeping station lost ownership");
            require(!DimensionStations.mayLand(guest, orbit, station), "Sleeping station granted stranger access");
            PlanetTravel.open(guest, new PlanetTravel(TravelSource.SPACE_ELEVATOR, 8));
            ServerboundLandOnSpaceStationPacket.TYPE.handle(new ServerboundLandOnSpaceStationPacket(orbit, station)).accept(guest);
            require(server.getLevel(orbit) == null && guest.serverLevel().dimension() == Level.OVERWORLD, "Denied station landing woke orbit");
            guest.closeContainer();
            PlanetTravel.open(owner, new PlanetTravel(TravelSource.SPACE_ELEVATOR, 8));
            ServerboundLandOnSpaceStationPacket.TYPE.handle(new ServerboundLandOnSpaceStationPacket(orbit, station)).accept(owner);
            require(owner.serverLevel().dimension() == orbit, "Authorized station landing did not reopen orbit");
            returnHome(owner);
            // Exercise Forge PlayerList respawn and the dynamic respawn packet path.
            var target = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.PLAYER, guest.getUUID()), DimensionTemplates.VOID, "respawn-" + UUID.randomUUID(), 298131L);
            require(manager.teleport(guest, target.dimension()), "Respawn target entry failed");
            var spawn = guest.serverLevel().getSharedSpawnPos();
            guest.setRespawnPosition(target.dimension(), spawn, 0, true, false);
            var replacement = server.getPlayerList().respawn(guest, false);
            // Vanilla's client-command handler performs this assignment after respawn.
            replacement.connection.player = replacement;
            require(replacement.serverLevel().dimension() == target.dimension(), "Dynamic forced respawn did not restore world");
            require(manager.teleport(replacement, Level.OVERWORLD), "Respawn return failed");
            System.out.println("DIMENSION_INTEGRATION_PATHS_PASSED");
            phase = 3;
        }
        return phase == 3;
    }

    private static void returnHome(ServerPlayer player) {
        var vehicle = player.getVehicle();
        player.stopRiding();
        if (vehicle != null) vehicle.discard();
        require(DimensionManager.get(player.server).teleport(player, Level.OVERWORLD), "Planet return failed");
        player.closeContainer();
    }

    private static void command(ServerPlayer source, String text, boolean success) {
        int result = source.server.getCommands().performPrefixedCommand(source.createCommandSourceStack().withPermission(2), text);
        require((result > 0) == success, "Unexpected command result " + result + ": " + text);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}

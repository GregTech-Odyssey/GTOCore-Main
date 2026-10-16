package com.gtocore.dimensionprobe;

import com.gtolib.api.dimension.*;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.DistExecutor;

import java.nio.file.*;
import java.util.*;

/**
 * 双客户端 Forge 联机探针，验证共享实例、权限撤销、休眠重开、同步顺序及断开重连后的连接缓存。
 */
public final class NetworkProbe {

    static void init() {
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT) DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Client::init);
        else new Server();
    }

    private static Path root() {
        return Path.of(System.getProperty("dimensionProbeRoot"));
    }

    private static void result(String name, String text) {
        try {
            Files.writeString(root().resolve(name + ".txt"), text);
        } catch (Exception error) {
            throw new RuntimeException(error);
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class Server {

        MinecraftServer server;
        DimensionManager manager;
        InstanceDescriptor instance;
        int phase, ticks;
        boolean reconnect;
        IntegrationPaths integration;

        Server() {
            MinecraftForge.EVENT_BUS.addListener(this::started);
            MinecraftForge.EVENT_BUS.addListener(this::tick);
        }

        void started(ServerStartedEvent event) {
            server = event.getServer();
            manager = DimensionManager.get(server);
            integration = new IntegrationPaths(manager);
            result("network-server", "READY");
        }

        void tick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || server == null) return;
            try {
                var players = server.getPlayerList().getPlayers();
                if (phase == 0) {
                    if (players.size() != 2) return;
                    if (!integration.tick(players)) return;
                    var owner = players.getFirst();
                    instance = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.PLAYER, owner.getUUID()), DimensionTemplates.VOID, "network-" + UUID.randomUUID(), 683821L);
                    var teams = dev.ftb.mods.ftbteams.api.FTBTeamsAPI.api().getManager();
                    var team = (dev.ftb.mods.ftbteams.data.PartyTeam) teams.createPartyTeam(owner, "DimensionProbe", "", dev.ftb.mods.ftblibrary.icon.Color4I.WHITE);
                    team.invite(owner, List.of(players.get(1).getGameProfile()));
                    team.join(players.get(1));
                    var teamInstance = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.TEAM, team.getId()), DimensionTemplates.VOID, "main", 993928L);
                    require(instance.owner().kind() == OwnerRef.Kind.PLAYER && instance.owner().id().equals(owner.getUUID()), "Joining a team transferred personal ownership");
                    for (var player : players) require(manager.teleport(player, teamInstance.dimension()), "Team member denied access");
                    team.leave(players.get(1).getUUID());
                    require(players.get(1).serverLevel().dimension() == Level.OVERWORLD && !manager.canAccess(players.get(1), teamInstance.dimension()), "Leaving a team did not immediately eject the member");
                    team.forceDisband(owner.createCommandSourceStack().withPermission(2));
                    require(owner.serverLevel().dimension() == Level.OVERWORLD && !manager.canAccess(owner, teamInstance.dimension()), "Deleting a team did not eject its owner");
                    require(manager.descriptor(teamInstance.dimension()) != null && Files.exists(manager.dimensionPath(teamInstance.dimension())), "Deleting a team removed its dimension");
                    manager.grant(instance.dimension(), players.get(1).getUUID(), true);
                    for (var player : players) require(manager.teleport(player, instance.dimension()), "Initial authorized teleport failed");
                    require(players.getFirst().serverLevel() == players.get(1).serverLevel(), "Multiplayer load duplicated instance");
                    require(!manager.requestUnload(instance.dimension()), "Occupied instance accepted unload");
                    phase = 1;
                    ticks = 0;
                } else if (phase == 1 && ++ticks == 80) {
                    manager.grant(instance.dimension(), players.get(1).getUUID(), false);
                    require(players.get(1).serverLevel().dimension() == Level.OVERWORLD, "Revoked visitor was not immediately returned");
                    manager.grant(instance.dimension(), players.get(1).getUUID(), true);
                    for (var player : players) require(manager.teleport(player, Level.OVERWORLD), "Return failed");
                    phase = 2;
                    ticks = 0;
                } else if (phase == 2 && ++ticks > 40) {
                    if (!manager.requestUnload(instance.dimension())) return;
                    phase = 3;
                } else if (phase == 3 && manager.state(instance.dimension()) == DimensionLifecycle.State.DORMANT) {
                    for (var player : players) require(manager.teleport(player, instance.dimension()), "Reopened teleport failed");
                    phase = 4;
                    ticks = 0;
                } else if (phase == 4 && ++ticks == 80) {
                    for (var player : java.util.List.copyOf(players)) player.connection.disconnect(net.minecraft.network.chat.Component.literal("Dimension probe reconnect"));
                    phase = 5;
                } else if (phase == 5 && players.size() == 2) {
                    for (var player : players) require(player.serverLevel().dimension() == instance.dimension(), "Login did not restore dormant/private instance");
                    phase = 6;
                    ticks = 0;
                } else if (phase == 6 && ++ticks == 80) {
                    result("network-server", "NETWORK_SERVER_PASSED");
                    for (var player : java.util.List.copyOf(players)) player.connection.disconnect(net.minecraft.network.chat.Component.literal("Dimension probe completed"));
                    server.halt(false);
                    phase = 7;
                }
            } catch (Throwable error) {
                error.printStackTrace();
                result("network-server", "FAILED: " + error);
                server.halt(false);
                phase = 7;
            }
        }
    }

    private static final class Client {

        static String name;
        static int ticks, attempts, logins, visits;
        static boolean connected, finished;
        static net.minecraft.resources.ResourceKey<Level> previous;

        static void init() {
            name = System.getProperty("dimensionClientName", "DimensionA");
            MinecraftForge.EVENT_BUS.addListener(Client::tick);
        }

        static void tick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || finished) return;
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            try {
                if (minecraft.level != null && minecraft.player != null) {
                    if (!connected) {
                        connected = true;
                        logins++;
                    }
                    var key = minecraft.level.dimension();
                    if (key.location().getNamespace().equals("gtocore") && key.location().getPath().startsWith("instance/")) {
                        var environment = DimensionSync.clientEnvironment(key);
                        require(environment != null, "Dynamic environment missing before client frame");
                        require(((com.gtolib.mc.ILevel) minecraft.level).gtolib$isVoid() == environment.voidWorld(), "Client environment did not reach level capability");
                        long known = minecraft.getConnection().levels().stream().filter(k -> k.location().getNamespace().equals("gtocore") && k.location().getPath().startsWith("instance/")).count();
                        require(known <= 2, "History was synchronized to the client");
                        if (previous != key) visits++;
                    }
                    previous = key;
                    if (++ticks % 20 == 0) result(name, "CONNECTED logins=" + logins + " visits=" + visits + " dimension=" + key.location());
                    return;
                }
                if (connected) {
                    connected = false;
                    if (previous != null) require(DimensionSync.clientEnvironment(previous) == null, "Connection cache survived disconnect");
                    previous = null;
                    if (logins >= 2 && visits >= 3) {
                        result(name, "NETWORK_CLIENT_PASSED logins=" + logins + " visits=" + visits);
                        finished = true;
                        minecraft.stop();
                        return;
                    }
                }
                if (++ticks % 60 != 0 || attempts >= 3) return;
                result(name, "WAITING screen=" + minecraft.screen + " attempts=" + attempts);
                if (minecraft.screen instanceof net.minecraftforge.client.gui.LoadingErrorScreen) {
                    var errors = minecraft.screen.getClass().getDeclaredField("modLoadErrors");
                    errors.setAccessible(true);
                    require(((List<?>) errors.get(minecraft.screen)).isEmpty(), "Forge client failed to load: " + errors.get(minecraft.screen));
                    minecraft.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
                }
                if (minecraft.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) minecraft.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
                if (!(minecraft.screen instanceof net.minecraft.client.gui.screens.TitleScreen) && !(minecraft.screen instanceof net.minecraft.client.gui.screens.DisconnectedScreen) && (minecraft.screen == null || !minecraft.screen.getClass().getName().equals("xaero.lib.client.gui.GuiUpdateAll"))) return;
                if (attempts == 0) {
                    var field = net.minecraft.client.Minecraft.class.getDeclaredField("user");
                    field.setAccessible(true);
                    var id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    field.set(minecraft, new net.minecraft.client.User(name, id.toString().replace("-", ""), "0", Optional.empty(), Optional.empty(), net.minecraft.client.User.Type.LEGACY));
                    minecraft.options.renderDistance().set(2);
                    minecraft.options.simulationDistance().set(2);
                }
                attempts++;
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(), minecraft,
                        net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25577"),
                        new net.minecraft.client.multiplayer.ServerData("Dimension probe", "127.0.0.1:25577", false), false);
            } catch (Throwable error) {
                error.printStackTrace();
                result(name, "FAILED: " + error);
                finished = true;
                minecraft.stop();
            }
        }
    }
}

package com.gtocore.dimensionprobe;

import com.gtolib.api.dimension.*;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import java.nio.file.*;
import java.util.*;

public final class CloseProbe {

    static ResourceKey<Level> target;
    private MinecraftServer server;

    CloseProbe() {
        MinecraftForge.EVENT_BUS.addListener(this::started);
        MinecraftForge.EVENT_BUS.addListener(this::stopped);
        MinecraftForge.EVENT_BUS.addListener(this::unloaded);
    }

    private void started(ServerStartedEvent event) {
        server = event.getServer();
        var manager = DimensionManager.get(server);
        var instance = manager.getOrCreatePrivate(new OwnerRef(OwnerRef.Kind.PLAYER, UUID.randomUUID()), DimensionTemplates.VOID, "close-failure", 427281L);
        target = instance.dimension();
        manager.loadNow(target);
        if (!manager.requestUnload(target)) throw new AssertionError("Close failure probe world was occupied");
    }

    private void unloaded(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == target) {
            throw new IllegalStateException("INJECTED_FATAL_CLOSE_FAILURE");
        }
    }

    private void stopped(ServerStoppedEvent event) {
        try {
            var manager = DimensionManager.get(server);
            if (manager.state(target) != DimensionLifecycle.State.POISONED) throw new AssertionError("Failed close was not poisoned");
            if (server.getLevel(target) != null) throw new AssertionError("Poisoned instance retained a queryable runtime");
            if (!manager.load(target).isCompletedExceptionally()) throw new AssertionError("Poisoned instance reopened");
            if (manager.descriptor(target) == null || !Files.exists(manager.dimensionPath(target))) throw new AssertionError("Close failure removed persisted data");
            Files.writeString(Path.of("close-result.txt"), "CLOSE_FAILURE_PROBE_PASSED");
        } catch (Throwable failure) {
            failure.printStackTrace();
            try {
                Files.writeString(Path.of("close-result.txt"), "FAILED: " + failure);
            } catch (Exception ignored) {}
        }
    }
}

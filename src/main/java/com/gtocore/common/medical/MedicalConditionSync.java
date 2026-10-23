package com.gtocore.common.medical;

import com.gtolib.api.network.NetworkPack;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import java.util.UUID;

public final class MedicalConditionSync {

    private static final Snapshot EMPTY = new Snapshot(null, 0);
    private static final O2OOpenCacheHashMap<UUID, Snapshot> LAST_SENT = new O2OOpenCacheHashMap<>();
    private static volatile Snapshot clientSnapshot = EMPTY;

    private static final NetworkPack SYNC = NetworkPack.registerS2C("gtocoreMedicalConditionS2C",
            (args, buf) -> ((Snapshot) args[1]).write(buf),
            (player, buf) -> {
                String name = buf.readUtf();
                clientSnapshot = name.isEmpty() ? EMPTY : new Snapshot(MedicalCondition.CONDITIONS.get(name), buf.readFloat());
            });

    private MedicalConditionSync() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(MedicalConditionSync::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(MedicalConditionSync::onPlayerLogout);
        MinecraftForge.EVENT_BUS.addListener(MedicalConditionSync::onServerStopped);
    }

    private static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        MedicalCondition highest = null;
        float progression = 0;
        float highestRatio = 0;
        var tracker = GTCapabilityHelper.getMedicalConditionTracker(player);
        if (tracker != null) {
            for (var it = tracker.getMedicalConditions().reference2FloatEntrySet().fastIterator(); it.hasNext();) {
                var entry = it.next();
                MedicalCondition condition = entry.getKey();
                float accumulated = entry.getFloatValue();
                if (condition.maxProgression <= 0 || accumulated <= 0) continue;
                float ratio = accumulated / condition.maxProgression;
                if (ratio > highestRatio || (ratio == highestRatio && highest != null && condition.name.compareTo(highest.name) < 0)) {
                    highest = condition;
                    progression = accumulated;
                    highestRatio = ratio;
                }
            }
        }
        Snapshot previous = LAST_SENT.get(player.getUUID());
        if (previous != null && previous.condition == highest && previous.progression == progression) return;
        Snapshot snapshot = highest == null ? EMPTY : new Snapshot(highest, progression);
        LAST_SENT.put(player.getUUID(), snapshot);
        SYNC.send(player, snapshot);
    }

    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) LAST_SENT.remove(player.getUUID());
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        LAST_SENT.clear();
    }

    public static Snapshot getClientSnapshot() {
        return clientSnapshot;
    }

    public static void clearClientSnapshot() {
        clientSnapshot = EMPTY;
    }

    public record Snapshot(MedicalCondition condition, float progression) {

        public float ratio() {
            return condition == null ? 0 : progression / condition.maxProgression;
        }

        private void write(FriendlyByteBuf buf) {
            buf.writeUtf(condition == null ? "" : condition.name);
            if (condition != null) buf.writeFloat(progression);
        }
    }
}

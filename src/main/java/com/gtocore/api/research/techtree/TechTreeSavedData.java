package com.gtocore.api.research.techtree;

import com.gtocore.api.research.TeamResearchContext;
import com.gtocore.client.Message;

import com.gtolib.GTOCore;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.utils.iostream.DataIOStream;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.DimensionDataStorage;

import com.gto.fastcollection.O2OOpenCacheHashMap;
import com.hepdd.gtmthings.utils.TeamUtil;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Getter
public class TechTreeSavedData extends FastSavedData {

    public static final String DATA_NAME = "tech_tree_data";
    public static final int DATA_VERSION = 1;
    public static TechTreeSavedData INSTANCE = new TechTreeSavedData();
    public static TechTreeSavedData CLIENT_INSTANCE = new TechTreeSavedData();

    private static boolean syncPending;

    private static final NetworkPack CLIENT_INSTANCE_SYNC = NetworkPack.registerS2C("techTreeSavedDataSyncS2C", (player, buffer) -> {
        try (var stream = DataIOStream.of(new ByteArrayInputStream(buffer.readByteArray()))) {
            TechTreeSavedData data = load(stream, DATA_VERSION);
            CLIENT_INSTANCE = data == null ? new TechTreeSavedData() : data;
        } catch (IOException | RuntimeException exception) {
            GTOCore.LOGGER.error("Failed to synchronize tech tree data", exception);
        }
    });

    private final Map<UUID, Map<String, TechTree>> teamTechTrees = new O2OOpenCacheHashMap<>();

    public static void init() {}

    public static TechTreeSavedData get() {
        return GTCEu.isClientThread() ? CLIENT_INSTANCE : INSTANCE;
    }

    public static TechTreeSavedData get(DimensionDataStorage dataStorage) {
        return FastSavedData.get(DATA_NAME, dataStorage, TechTreeSavedData::load, TechTreeSavedData::new, DATA_VERSION);
    }

    public static UUID getTeamUUID(Player player) {
        return TeamUtil.getTeamUUID(player.getUUID());
    }

    public static TechTree getOrCreateTree(Player player, TechTreeManager manager) {
        return getOrCreateTree(getTeamUUID(player), manager);
    }

    public static TechTree findTree(Player player, TechTreeManager manager) {
        return findTree(getTeamUUID(player), manager);
    }

    public static TechTree getOrCreateTree(UUID uuid, TechTreeManager manager) {
        UUID teamUUID = TeamUtil.getTeamUUID(uuid);
        Map<String, TechTree> teamTrees = get().teamTechTrees.computeIfAbsent(teamUUID, ignored -> new O2OOpenCacheHashMap<>());
        return teamTrees.computeIfAbsent(manager.getId(), ignored -> new TechTree(manager));
    }

    public static TechTree findTree(UUID uuid, TechTreeManager manager) {
        UUID teamUUID = TeamUtil.getTeamUUID(uuid);
        Map<String, TechTree> teamTrees = get().teamTechTrees.get(teamUUID);
        if (teamTrees == null) return null;
        return teamTrees.get(manager.getId());
    }

    public static boolean isUnlocked(ServerPlayer player, TechNode node) {
        return isUnlocked(getTeamUUID(player), node);
    }

    public static boolean isUnlocked(UUID uuid, TechNode node) {
        TechTree tree = findTree(uuid, node.getManager());
        return tree != null && tree.isUnlocked(node);
    }

    public static boolean unlock(Player player, TechNode node, TeamResearchContext context) {
        return unlock(getTeamUUID(player), node, context);
    }

    public static boolean unlock(UUID uuid, TechNode node, TeamResearchContext context) {
        TechTree tree = getOrCreateTree(uuid, node.getManager());
        boolean changed = !tree.isUnlocked(node) && tree.unlock(node, context, uuid).isSuccess();
        if (changed) {
            INSTANCE.setDirty();
            Message.sendResearchToast(uuid, node, true);
        }
        return changed;
    }

    public static boolean forceUnlock(UUID uuid, TechNode node) {
        TechTree tree = getOrCreateTree(uuid, node.getManager());
        if (tree.isUnlocked(node)) {
            return false;
        }
        tree.addUnlockedNode(node);
        INSTANCE.setDirty();
        return true;
    }

    public static boolean reset(Player player, TechTreeManager manager) {
        return reset(getTeamUUID(player), manager);
    }

    public static boolean reset(UUID uuid, TechTreeManager manager) {
        UUID teamUUID = TeamUtil.getTeamUUID(uuid);
        Map<String, TechTree> teamTrees = INSTANCE.teamTechTrees.get(teamUUID);
        if (teamTrees == null || teamTrees.remove(manager.getId()) == null) {
            return false;
        }
        if (teamTrees.isEmpty()) {
            INSTANCE.teamTechTrees.remove(teamUUID);
        }
        INSTANCE.setDirty();
        return true;
    }

    public static void sync(ServerPlayer player) {
        sendSnapshot(player);
    }

    public static void syncIfNeeded(MinecraftServer server) {
        if (!syncPending || server.getPlayerList().getPlayerCount() == 0) return;
        syncPending = false;
        sendSnapshot(server);
    }

    public static void clearClientInstance() {
        CLIENT_INSTANCE = new TechTreeSavedData();
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        if (dirty && this == INSTANCE) {
            syncPending = true;
        }
    }

    private static void sendSnapshot(Object recipient) {
        try {
            byte[] payload = encodeSnapshot();
            CLIENT_INSTANCE_SYNC.send(buffer -> buffer.writeByteArray(payload), recipient);
        } catch (IOException exception) {
            GTOCore.LOGGER.error("Failed to serialize tech tree data for synchronization", exception);
        }
    }

    private static byte[] encodeSnapshot() throws IOException {
        var output = new ByteArrayOutputStream();
        try (var stream = DataIOStream.of(output)) {
            INSTANCE.save(stream);
            stream.flush();
        }
        return output.toByteArray();
    }

    public static TechTreeSavedData load(DataIOStream stream, int dataVersion) {
        var data = new TechTreeSavedData();
        try {
            int teamCount = stream.readVarInt();
            for (int i = 0; i < teamCount; i++) {
                UUID teamId = stream.readUUID();
                int treeCount = stream.readVarInt();
                Map<String, TechTree> trees = new O2OOpenCacheHashMap<>();
                for (int j = 0; j < treeCount; j++) {
                    String treeId = stream.readUTF();
                    byte[] payload = stream.readByteArray();
                    var manager = TechTreeManager.getManager(treeId);
                    if (manager != null) {
                        try (var payloadStream = DataIOStream.of(new ByteArrayInputStream(payload))) {
                            trees.put(treeId, manager.decode(payloadStream, dataVersion));
                        }
                    }
                }
                if (!trees.isEmpty()) {
                    data.teamTechTrees.put(teamId, trees);
                }
            }
        } catch (Throwable ignored) {
            return null;
        }
        return data;
    }

    @Override
    public void save(DataIOStream stream) throws IOException {
        stream.writeVarInt(teamTechTrees.size());
        for (var teamEntry : teamTechTrees.entrySet()) {
            int treeCount = countNonEmptyTrees(teamEntry.getValue());
            stream.writeUUID(teamEntry.getKey());
            Map<String, TechTree> trees = teamEntry.getValue();
            stream.writeVarInt(treeCount);
            for (var treeEntry : trees.entrySet()) {
                if (treeEntry.getValue().isEmpty()) continue;
                stream.writeUTF(treeEntry.getKey());
                TechTreeManager manager = TechTreeManager.getManager(treeEntry.getKey());
                if (manager == null) {
                    manager = inferManager(treeEntry.getValue());
                }
                if (manager == null) {
                    throw new IOException("Missing TechTreeManager for tree id " + treeEntry.getKey());
                }
                stream.writeByteArray(encodeTree(manager, treeEntry.getValue()));
            }
        }
    }

    private static int countNonEmptyTrees(Map<String, TechTree> trees) {
        int count = 0;
        for (var tree : trees.values()) {
            if (!tree.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static byte[] encodeTree(TechTreeManager manager, TechTree tree) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (var treeStream = DataIOStream.of(baos)) {
            manager.encode(treeStream, tree);
            treeStream.flush();
        }
        return baos.toByteArray();
    }

    private static TechTreeManager inferManager(TechTree tree) {
        for (var node : tree.getEndNodes()) {
            return node.getManager();
        }
        return null;
    }
}

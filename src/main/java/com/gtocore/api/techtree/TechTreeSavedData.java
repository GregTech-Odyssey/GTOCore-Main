package com.gtocore.api.techtree;

import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.iostream.DataIOStream;

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

    private final Map<UUID, Map<String, TechTree<?>>> teamTechTrees = new O2OOpenCacheHashMap<>();

    public static TechTreeSavedData get(DimensionDataStorage dataStorage) {
        return FastSavedData.get(DATA_NAME, dataStorage, TechTreeSavedData::load, TechTreeSavedData::new, DATA_VERSION);
    }

    public static UUID getTeamUUID(Player player) {
        return TeamUtil.getTeamUUID(player.getUUID());
    }

    public static <T> TechTree<T> getOrCreateTree(Player player, TechTreeManager<T> manager) {
        return getOrCreateTree(getTeamUUID(player), manager);
    }

    public static <T> TechTree<T> findTree(Player player, TechTreeManager<T> manager) {
        return findTree(getTeamUUID(player), manager);
    }

    @SuppressWarnings("unchecked")
    public static <T> TechTree<T> getOrCreateTree(UUID uuid, TechTreeManager<T> manager) {
        UUID teamUUID = TeamUtil.getTeamUUID(uuid);
        Map<String, TechTree<?>> teamTrees = INSTANCE.teamTechTrees.computeIfAbsent(teamUUID, ignored -> new O2OOpenCacheHashMap<>());
        return (TechTree<T>) teamTrees.computeIfAbsent(manager.getId(), ignored -> new TechTree<>(manager));
    }

    @SuppressWarnings("unchecked")
    public static <T> TechTree<T> findTree(UUID uuid, TechTreeManager<T> manager) {
        UUID teamUUID = TeamUtil.getTeamUUID(uuid);
        Map<String, TechTree<?>> teamTrees = INSTANCE.teamTechTrees.get(teamUUID);
        if (teamTrees == null) return null;
        return (TechTree<T>) teamTrees.get(manager.getId());
    }

    public static <T> boolean isUnlocked(ServerPlayer player, TechNode<T> node) {
        return isUnlocked(getTeamUUID(player), node);
    }

    public static <T> boolean isUnlocked(UUID uuid, TechNode<T> node) {
        TechTree<T> tree = findTree(uuid, node.getManager());
        return tree != null && tree.isUnlocked(node);
    }

    public static <T> boolean unlock(Player player, TechNode<T> node, T args) {
        return unlock(getTeamUUID(player), node, args);
    }

    public static <T> boolean unlock(UUID uuid, TechNode<T> node, T args) {
        TechTree<T> tree = getOrCreateTree(uuid, node.getManager());
        boolean changed = !tree.isUnlocked(node) && tree.unlock(node, args, uuid).isSuccess();
        if (changed) {
            INSTANCE.setDirty();
        }
        return changed;
    }

    public static <T> boolean forceUnlock(UUID uuid, TechNode<T> node) {
        TechTree<T> tree = getOrCreateTree(uuid, node.getManager());
        if (tree.isUnlocked(node)) {
            return false;
        }
        tree.addUnlockedNode(node);
        INSTANCE.setDirty();
        return true;
    }

    public static boolean reset(Player player, TechTreeManager<?> manager) {
        return reset(getTeamUUID(player), manager);
    }

    public static boolean reset(UUID uuid, TechTreeManager<?> manager) {
        UUID teamUUID = TeamUtil.getTeamUUID(uuid);
        Map<String, TechTree<?>> teamTrees = INSTANCE.teamTechTrees.get(teamUUID);
        if (teamTrees == null || teamTrees.remove(manager.getId()) == null) {
            return false;
        }
        if (teamTrees.isEmpty()) {
            INSTANCE.teamTechTrees.remove(teamUUID);
        }
        INSTANCE.setDirty();
        return true;
    }

    public static TechTreeSavedData load(DataIOStream stream, int dataVersion) {
        var data = new TechTreeSavedData();
        try {
            int teamCount = stream.readVarInt();
            for (int i = 0; i < teamCount; i++) {
                UUID teamId = stream.readUUID();
                int treeCount = stream.readVarInt();
                Map<String, TechTree<?>> trees = new O2OOpenCacheHashMap<>();
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
            Map<String, TechTree<?>> trees = teamEntry.getValue();
            stream.writeVarInt(treeCount);
            for (var treeEntry : trees.entrySet()) {
                if (treeEntry.getValue().isEmpty()) continue;
                stream.writeUTF(treeEntry.getKey());
                TechTreeManager<?> manager = TechTreeManager.getManager(treeEntry.getKey());
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

    private static int countNonEmptyTrees(Map<String, TechTree<?>> trees) {
        int count = 0;
        for (var tree : trees.values()) {
            if (!tree.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static byte[] encodeTree(TechTreeManager manager, TechTree<?> tree) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (var treeStream = DataIOStream.of(baos)) {
            manager.encode(treeStream, tree);
            treeStream.flush();
        }
        return baos.toByteArray();
    }

    @SuppressWarnings("rawtypes")
    private static TechTreeManager<?> inferManager(TechTree<?> tree) {
        for (var node : tree.getEndNodes()) {
            return ((TechNode) node).getManager();
        }
        return null;
    }
}

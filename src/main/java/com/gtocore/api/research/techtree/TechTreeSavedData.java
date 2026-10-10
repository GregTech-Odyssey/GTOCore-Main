package com.gtocore.api.research.techtree;

import com.gtocore.api.research.TeamResearchSavedData;
import com.gtocore.client.Message;

import com.gtolib.GTOCore;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.api.network.NetworkPack;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.DimensionDataStorage;

import com.gto.datasynclib.util.VersionedFriendlyByteBuf;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.hepdd.gtmthings.utils.TeamUtil;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.DecoderException;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class TechTreeSavedData extends FastSavedData {

    public static final String DATA_NAME = "tech_tree_data";
    public static final int DATA_VERSION = 1;
    /**
     * 存档文件头写的版本号。负载形状至今没变过，所以文件头从 1 起：读到别的版本也按同一个形状解，
     * 将来负载变形了再在这里分支。
     */
    public static final int VERSION = 1;
    public static TechTreeSavedData INSTANCE = new TechTreeSavedData();
    public static TechTreeSavedData CLIENT_INSTANCE = new TechTreeSavedData();

    private static boolean syncPending;
    /**
     * 解锁数据的修改计数：服务端数据标脏、客户端收到新快照时各加一。界面按它判断"节点状态要不要重算"，
     * 不必每 tick 遍历整棵树（单人游戏两端共用这个计数，多出来的失效只是多算一次，不影响正确性）。
     */
    private static final AtomicInteger MOD_COUNT = new AtomicInteger();

    private static final NetworkPack CLIENT_INSTANCE_SYNC = NetworkPack.registerS2C("techTreeSavedDataSyncS2C",
            (objs, buf) -> INSTANCE.save(buf), (player, buffer) -> {
                try {
                    TechTreeSavedData data = read(buffer);
                    CLIENT_INSTANCE = data == null ? new TechTreeSavedData() : data;
                    MOD_COUNT.incrementAndGet();
                } catch (RuntimeException exception) {
                    GTOCore.LOGGER.error("Failed to synchronize tech tree data", exception);
                }
            });

    /**
     * 每支队伍 → (注册对象 TechTreeManager → 该队伍的 TechTree)。
     * 外层 key 是 UUID（值对象），用哈希缓存容器；内层 key 是注册对象 TechTreeManager，
     * 标识稳定、按引用比较，用引用容器 fastutil Reference2ObjectOpenHashMap。
     */
    private final O2OOpenCacheHashMap<UUID, Reference2ObjectOpenHashMap<TechTreeManager, TechTree>> teamTechTrees = new O2OOpenCacheHashMap<>();

    public static void init() {}

    public static TechTreeSavedData get() {
        return GTCEu.isClientThread() ? CLIENT_INSTANCE : INSTANCE;
    }

    public static TechTreeSavedData get(DimensionDataStorage dataStorage) {
        return FastSavedData.get(DATA_NAME, dataStorage, TechTreeSavedData::load, TechTreeSavedData::loadLegacy, TechTreeSavedData::new);
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
        Reference2ObjectOpenHashMap<TechTreeManager, TechTree> teamTrees = get().teamTechTrees.computeIfAbsent(teamUUID, ignored -> new Reference2ObjectOpenHashMap<>());
        return teamTrees.computeIfAbsent(manager, ignored -> new TechTree(manager));
    }

    public static TechTree findTree(UUID uuid, TechTreeManager manager) {
        UUID teamUUID = TeamUtil.getTeamUUID(uuid);
        Reference2ObjectOpenHashMap<TechTreeManager, TechTree> teamTrees = get().teamTechTrees.get(teamUUID);
        if (teamTrees == null) return null;
        return teamTrees.get(manager);
    }

    public static boolean isUnlocked(ServerPlayer player, TechNode node) {
        return isUnlocked(player.getUUID(), node);
    }

    public static boolean isUnlocked(@Nullable UUID uuid, TechNode node) {
        if (uuid == null) return false;
        TechTree tree = findTree(TeamUtil.getTeamUUID(uuid), node.getManager());
        return tree != null && tree.isUnlocked(node);
    }

    public static boolean isPrerequisitesUnlocked(@Nullable UUID uuid, TechNode node) {
        for (var prerequisite : node.prerequisites) {
            if (!isUnlocked(uuid, prerequisite)) return false;
        }
        return true;
    }

    public static boolean unlock(Player player, TechNode node) {
        return unlock(getTeamUUID(player), node);
    }

    public static boolean hasNodeMetCWURequirements(UUID uuid, TechNode node) {
        TechTree tree = findTree(TeamUtil.getTeamUUID(uuid), node.getManager());
        var context = TeamResearchSavedData.getOrCreateContext(uuid);
        return tree != null && tree.hasNodeMetCWURequirements(node, context);
    }

    public static boolean unlock(UUID uuid, TechNode node) {
        TechTree tree = getOrCreateTree(uuid, node.getManager());
        var context = TeamResearchSavedData.getOrCreateContext(uuid);
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
        Reference2ObjectOpenHashMap<TechTreeManager, TechTree> teamTrees = INSTANCE.teamTechTrees.get(teamUUID);
        if (teamTrees == null || teamTrees.remove(manager) == null) {
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
        MOD_COUNT.incrementAndGet();
    }

    /** 解锁数据的修改计数，见 {@link #MOD_COUNT}。 */
    public static int getModCount() {
        return MOD_COUNT.get();
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        if (dirty && this == INSTANCE) {
            syncPending = true;
            MOD_COUNT.incrementAndGet();
        }
    }

    private static void sendSnapshot(Object recipient) {
        CLIENT_INSTANCE_SYNC.send(recipient);
    }

    /**
     * 读取文件头带版本号的存档。
     * <p>
     * 旧存档的第一个字段是 VarInt 队伍数，头一个字节 1 会被当成版本 1——所以"版本对得上"不等于"这份文件带文件头"：
     * 只有整份载荷正好读完才算读对了。读不对就抛出去，交给 {@code loadVersionedFromFile} 回退到 {@link #loadLegacy}
     * 从文件开头按旧格式重读。负载形状没变过，所以真正的读取是下面那一个共用体。
     */
    public static TechTreeSavedData load(VersionedFriendlyByteBuf stream) {
        int fileVersion = stream.version();
        if (fileVersion != VERSION) {
            throw new DecoderException("tech_tree_data declares version " + fileVersion + ", this build reads " + VERSION);
        }
        var data = read(stream);
        if (data == null || stream.isReadable()) {
            throw new DecoderException("tech_tree_data is not a payload this build wrote");
        }
        return data;
    }

    /**
     * 读取早于文件头的存档；网络快照走的是同一份负载，也调下面这个共用体。
     */
    public static TechTreeSavedData loadLegacy(FriendlyByteBuf stream) {
        return read(stream);
    }

    private static TechTreeSavedData read(FriendlyByteBuf stream) {
        var data = new TechTreeSavedData();
        try {
            int teamCount = stream.readVarInt();
            for (int i = 0; i < teamCount; i++) {
                UUID teamId = stream.readUUID();
                int treeCount = stream.readVarInt();
                Reference2ObjectOpenHashMap<TechTreeManager, TechTree> trees = new Reference2ObjectOpenHashMap<>();
                for (int j = 0; j < treeCount; j++) {
                    String treeId = stream.readUtf();
                    int payloadLength = stream.readVarInt();
                    var manager = TechTreeManager.getManager(treeId);
                    // 单棵树的负载带上长度前缀：读侧按长度切一段共享内存的切片来解码，不复制字节
                    if (manager != null) {
                        trees.put(manager, manager.decode(new FriendlyByteBuf(stream.readSlice(payloadLength))));
                    } else {
                        stream.skipBytes(payloadLength);
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
    public int version() {
        return VERSION;
    }

    @Override
    public void save(FriendlyByteBuf stream) {
        stream.writeVarInt(teamTechTrees.size());
        for (var teamEntry : teamTechTrees.entrySet()) {
            int treeCount = countNonEmptyTrees(teamEntry.getValue());
            stream.writeUUID(teamEntry.getKey());
            Reference2ObjectOpenHashMap<TechTreeManager, TechTree> trees = teamEntry.getValue();
            stream.writeVarInt(treeCount);
            for (var treeEntry : trees.entrySet()) {
                if (treeEntry.getValue().isEmpty()) continue;
                TechTreeManager manager = treeEntry.getKey();
                stream.writeUtf(manager.getId());
                writeTree(stream, manager, treeEntry.getValue());
            }
        }
    }

    private static int countNonEmptyTrees(Reference2ObjectOpenHashMap<TechTreeManager, TechTree> trees) {
        int count = 0;
        for (var tree : trees.values()) {
            if (!tree.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 一棵树编码进一个长度前缀的独立区块：长度先于内容写出，读侧按长度切片解码，中间不落 byte[]。
     */
    private static void writeTree(FriendlyByteBuf stream, TechTreeManager manager, TechTree tree) {
        var buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            manager.encode(new FriendlyByteBuf(buf), tree);
            stream.writeVarInt(buf.readableBytes());
            stream.writeBytes(buf, buf.readerIndex(), buf.readableBytes());
        } finally {
            buf.release();
        }
    }
}

package com.gtocore.api.research;

import com.gtolib.api.misc.FastSavedData;
import com.gtolib.api.network.NetworkPack;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.gto.datasynclib.util.VersionedFriendlyByteBuf;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID;

public class TeamResearchSavedData extends FastSavedData {

    public static final String DATA_NAME = "team_research_data";
    public static final int DATA_VERSION = 4;
    /**
     * 存档文件头写的版本号。负载形状至今没变过，所以文件头从 1 起：读到别的版本也按同一个形状解，
     * 将来负载变形了再在这里分支。
     */
    public static final int VERSION = 1;
    public static TeamResearchSavedData INSTANCE = new TeamResearchSavedData();
    public static TeamResearchSavedData CLIENT_INSTANCE = new TeamResearchSavedData();

    private static boolean syncPending;

    private static final NetworkPack CLIENT_INSTANCE_SYNC = NetworkPack.registerS2C("teamResearchSavedDataSyncS2C",
            (objs, buf) -> INSTANCE.save(buf),
            (player, buf) -> CLIENT_INSTANCE = read(buf));

    private final O2OOpenCacheHashMap<UUID, TeamResearchContext> teamResearchContexts = new O2OOpenCacheHashMap<>();

    public static void init() {}

    public static TeamResearchSavedData get() {
        return GTCEu.isClientThread() ? CLIENT_INSTANCE : INSTANCE;
    }

    @Override
    public int version() {
        return VERSION;
    }

    @Override
    public void save(FriendlyByteBuf buf) {
        buf.writeInt(teamResearchContexts.size());
        for (var entry : Object2ObjectMaps.fastIterable(teamResearchContexts)) {
            buf.writeUUID(entry.getKey());
            TeamResearchContext.writeContext(buf, entry.getValue());
        }
    }

    /**
     * 读取文件头带版本号的存档。负载形状没变过，所以版本化读取就是下面那一个共用体；
     * 版本随流带进来，将来负载变形时在这里按 {@link VersionedFriendlyByteBuf#version()} 分支。
     */
    public static TeamResearchSavedData load(VersionedFriendlyByteBuf stream) {
        return read(stream);
    }

    /**
     * 读取早于文件头的存档；网络快照走的是同一份负载，也调这里。
     */
    public static TeamResearchSavedData loadLegacy(FriendlyByteBuf stream) {
        return read(stream);
    }

    private static TeamResearchSavedData read(FriendlyByteBuf buf) {
        TeamResearchSavedData savedData = new TeamResearchSavedData();
        int teamCount = buf.readInt();
        for (int i = 0; i < teamCount; i++) {
            UUID teamUUID = buf.readUUID();
            savedData.teamResearchContexts.put(teamUUID, TeamResearchContext.readContext(buf));
        }
        return savedData;
    }

    public static TeamResearchContext getOrCreateContext(Player player) {
        return getOrCreateContext(player.getUUID());
    }

    public static TeamResearchContext getOrCreateContext(UUID uuid) {
        return get().teamResearchContexts.computeIfAbsent(getTeamUUID(uuid), ignored -> new TeamResearchContext());
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
        CLIENT_INSTANCE = new TeamResearchSavedData();
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        if (dirty && this == INSTANCE) {
            syncPending = true;
        }
    }

    private static void sendSnapshot(Object recipient) {
        CLIENT_INSTANCE_SYNC.send(recipient);
    }
}

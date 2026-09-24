package com.gtocore.integration.ae.wireless;

import com.gtocore.config.GTOConfig;

import com.gtolib.GTOCore;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 无线网络的持久化数据（主世界 {@code data/gtocore_me_wireless.dat}），带版本号：
 *
 * <pre>
 * version  : Int = 1
 * networks : [{id: String, owner: IntArray[4], name: String, created: Long}]
 * favorites: [{player: IntArray[4], network: String}]
 * </pre>
 *
 * 读取时逐条解码，坏记录记日志后跳过，不会让整份数据加载失败。
 * <p>
 * 新文件存在却无法读取（文件损坏），或版本号高于本版本支持的版本时，本次运行的无线数据<strong>不可用</strong>
 * （{@link #isAvailable()} 为 false）：不迁移、不写回、所有操作返回 {@link WirelessStatus#UNAVAILABLE}，
 * 机器上的网络 id 一律不改，修好文件（或换回新版本）后重进即恢复。
 * <p>
 * 新文件不存在时从旧文件 {@code wireless_saved_data_<aeGridKey>} 迁移一次（见 {@link #migrate}）；旧文件只读不改不删，
 * 回退旧版本仍能读到旧数据。
 * <p>
 * 所有修改操作都以发起玩家校验权限，改完推送 {@link WirelessSync}。
 */
public final class WirelessNetworks extends SavedData {

    public static final String DATA_NAME = "gtocore_me_wireless";
    public static final int VERSION = 1;
    public static final int MAX_NAME_LENGTH = 32;
    /** 每名玩家最多创建的网络数。 */
    public static final int MAX_NETWORKS_PER_PLAYER = 64;
    private static final String LEGACY_PREFIX = "wireless_saved_data_";

    private record Favorite(UUID player, String network) {

        static final Codec<Favorite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("player").forGetter(Favorite::player),
                Codec.STRING.fieldOf("network").forGetter(Favorite::network))
                .apply(instance, Favorite::new));
    }

    /** 创建结果：成功时带新网络。 */
    public record Created(WirelessStatus status, @Nullable WirelessNetwork network) {}

    private final Map<String, WirelessNetwork> networks = new LinkedHashMap<>();
    private final Map<UUID, String> favorites = new HashMap<>();
    private final boolean available;
    /** 修改计数（不存盘）：网络增删改、收藏变化、队伍变动时加一，界面据此判断列表是否需要重算。 */
    private int revision;

    private WirelessNetworks(boolean available) {
        this.available = available;
    }

    public static WirelessNetworks get(MinecraftServer server) {
        var storage = server.overworld().getDataStorage();
        var data = storage.get(WirelessNetworks::load, DATA_NAME);
        if (data != null) return data;
        if (storage.getDataFile(DATA_NAME).exists()) {
            // 文件在但读不出来（原版已记录异常）：不能当作"没有数据"去迁移，否则会用旧文件覆盖掉之后新建的网络
            GTOCore.LOGGER.error("[ME无线] {} 存在但无法读取：本次运行无线网络不可用，不迁移、不写回，机器上的网络 id 保持不变", DATA_NAME);
            data = new WirelessNetworks(false);
        } else {
            data = migrate(storage);
        }
        storage.set(DATA_NAME, data);
        return data;
    }

    // ==================== 查询 ====================

    /** 数据是否可用；不可用时（见类说明）所有查询为空、所有操作返回 {@link WirelessStatus#UNAVAILABLE}。 */
    public boolean isAvailable() {
        return available;
    }

    public int revision() {
        return revision;
    }

    /** 队伍变动会改变"谁能用哪个网络"，列表需要重算。 */
    void markTeamsChanged() {
        revision++;
    }

    @Nullable
    public WirelessNetwork get(String id) {
        return networks.get(id);
    }

    /** 该玩家可使用的网络，按名称（不区分大小写）排序。 */
    public List<WirelessNetwork> listFor(UUID player) {
        var result = new ArrayList<WirelessNetwork>();
        for (var network : networks.values()) {
            if (network.canUse(player)) result.add(network);
        }
        result.sort(Comparator.comparing((WirelessNetwork n) -> n.name().toLowerCase(Locale.ROOT)).thenComparing(WirelessNetwork::id));
        return result;
    }

    @Nullable
    public String favorite(UUID player) {
        return favorites.get(player);
    }

    /** 所有者显示名：玩家名（在线、FTB 已知玩家或用户名缓存），查不到时用 UUID 前 8 位。 */
    public static String ownerName(MinecraftServer server, UUID owner) {
        var name = TeamUtil.findPlayerName(server.overworld(), owner);
        return name == null ? owner.toString().substring(0, 8) : name.getString();
    }

    // ==================== 修改（发起玩家校验） ====================

    public Created create(ServerPlayer player, String rawName) {
        if (!available) return new Created(WirelessStatus.UNAVAILABLE, null);
        var name = rawName.strip();
        if (!isValidName(name)) return new Created(WirelessStatus.NAME_INVALID, null);
        if (isNameTaken(player.getUUID(), name, null)) return new Created(WirelessStatus.NAME_TAKEN, null);
        if (ownedCount(player.getUUID()) >= MAX_NETWORKS_PER_PLAYER) return new Created(WirelessStatus.LIMIT_REACHED, null);
        var network = new WirelessNetwork(UUID.randomUUID().toString(), player.getUUID(), name, System.currentTimeMillis());
        networks.put(network.id(), network);
        changed();
        WirelessSync.pushNetwork(player.server, network);
        return new Created(WirelessStatus.OK, network);
    }

    public WirelessStatus rename(ServerPlayer player, String id, String rawName) {
        if (!available) return WirelessStatus.UNAVAILABLE;
        var network = networks.get(id);
        if (network == null) return WirelessStatus.NOT_FOUND;
        if (!network.canUse(player.getUUID())) return WirelessStatus.NO_PERMISSION_NETWORK;
        var name = rawName.strip();
        if (!isValidName(name)) return WirelessStatus.NAME_INVALID;
        if (name.equals(network.name())) return WirelessStatus.OK;
        if (isNameTaken(player.getUUID(), name, id)) return WirelessStatus.NAME_TAKEN;
        var renamed = network.withName(name);
        networks.put(id, renamed);
        changed();
        WirelessSync.pushNetwork(player.server, renamed);
        return WirelessStatus.OK;
    }

    /** 删除网络：已加载的成员立即清 id 并断开；未加载的成员下次加载时发现网络不存在，自行清 id。 */
    public WirelessStatus delete(ServerPlayer player, String id) {
        if (!available) return WirelessStatus.UNAVAILABLE;
        var network = networks.get(id);
        if (network == null) return WirelessStatus.NOT_FOUND;
        if (!network.canUse(player.getUUID())) return WirelessStatus.NO_PERMISSION_NETWORK;
        var affected = WirelessSync.usersOf(player.server, network);
        var hub = WirelessHub.get(id);
        if (hub != null) {
            for (var member : hub.members()) {
                member.setWirelessNetworkId("");
                member.markWirelessChanged();
            }
        }
        WirelessHub.destroy(id);
        networks.remove(id);
        favorites.values().removeIf(id::equals);
        changed();
        WirelessSync.pushTo(affected);
        return WirelessStatus.OK;
    }

    /** 收藏（潜行放置时自动加入）；已收藏同一网络时取消收藏。每名玩家至多一个收藏。 */
    public WirelessStatus toggleFavorite(ServerPlayer player, String id) {
        if (!available) return WirelessStatus.UNAVAILABLE;
        var network = networks.get(id);
        if (network == null) return WirelessStatus.NOT_FOUND;
        if (!network.canUse(player.getUUID())) return WirelessStatus.NO_PERMISSION_NETWORK;
        if (id.equals(favorites.get(player.getUUID()))) favorites.remove(player.getUUID());
        else favorites.put(player.getUUID(), id);
        changed();
        WirelessSync.pushTo(player);
        return WirelessStatus.OK;
    }

    private void changed() {
        revision++;
        setDirty();
    }

    private int ownedCount(UUID player) {
        int count = 0;
        for (var network : networks.values()) {
            if (network.owner().equals(player)) count++;
        }
        return count;
    }

    private static boolean isValidName(String name) {
        return !name.isEmpty() && name.length() <= MAX_NAME_LENGTH;
    }

    /** 同名判定只在该玩家可用的网络里做：全服唯一会泄露别的队伍的网络名。 */
    private boolean isNameTaken(UUID player, String name, @Nullable String exceptId) {
        for (var network : networks.values()) {
            if (network.name().equals(name) && !network.id().equals(exceptId) && network.canUse(player)) return true;
        }
        return false;
    }

    // ==================== 读写 ====================

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("version", VERSION);
        var networkList = new ListTag();
        for (var network : networks.values()) {
            networkList.add(WirelessNetwork.CODEC.encodeStart(NbtOps.INSTANCE, network).getOrThrow(false, GTOCore.LOGGER::error));
        }
        tag.put("networks", networkList);
        var favoriteList = new ListTag();
        for (var entry : favorites.entrySet()) {
            favoriteList.add(Favorite.CODEC.encodeStart(NbtOps.INSTANCE, new Favorite(entry.getKey(), entry.getValue())).getOrThrow(false, GTOCore.LOGGER::error));
        }
        tag.put("favorites", favoriteList);
        return tag;
    }

    private static WirelessNetworks load(CompoundTag tag) {
        int version = tag.getInt("version");
        if (version > VERSION) {
            // 更新版本写的数据：按旧格式硬读再写回会丢掉新版本的字段
            GTOCore.LOGGER.error("[ME无线] {} 的版本 {} 高于当前支持的 {}：本次运行无线网络不可用，不读取、不写回", DATA_NAME, version, VERSION);
            return new WirelessNetworks(false);
        }
        var data = new WirelessNetworks(true);
        for (var entry : tag.getList("networks", Tag.TAG_COMPOUND)) {
            WirelessNetwork.CODEC.parse(NbtOps.INSTANCE, entry)
                    .resultOrPartial(error -> GTOCore.LOGGER.error("[ME无线] 跳过损坏的网络记录 {}：{}", entry, error))
                    .ifPresent(network -> data.networks.putIfAbsent(network.id(), network));
        }
        for (var entry : tag.getList("favorites", Tag.TAG_COMPOUND)) {
            Favorite.CODEC.parse(NbtOps.INSTANCE, entry)
                    .resultOrPartial(error -> GTOCore.LOGGER.error("[ME无线] 跳过损坏的收藏记录 {}：{}", entry, error))
                    .filter(favorite -> data.networks.containsKey(favorite.network()))
                    .ifPresent(favorite -> data.favorites.put(favorite.player(), favorite.network()));
        }
        return data;
    }

    /**
     * 新文件不存在时执行一次：从旧文件 {@code wireless_saved_data_<aeGridKey>} 迁移（aeGridKey 取当前配置）。
     * <ul>
     * <li>S4b/S4c/S4d（含 {@code networks}）：{@code id→id, owner→owner, nickname→name（缺失用 id）}，
     * {@code max/maxOutputsPerInput/nodes} 丢弃；{@code defaultMap[{key,value}]→favorites}。</li>
     * <li>S1–S4a（只有
     * {@code WirelessSavedData}）：{@code name→id, owner→owner, nickname（缺失用 name）→name, isDefault→favorites}。</li>
     * </ul>
     * owner 解析失败的记录丢弃并记日志（不再随机生成 UUID）；指向不存在网络的收藏丢弃。
     * 旧文件经 {@link DimensionDataStorage#get} 读成只读对象（从不标记修改，存盘时不会写回），不改不删。
     * 迁移结果无论是否为空都标记修改，写出新文件，保证只迁移一次。
     */
    private static WirelessNetworks migrate(DimensionDataStorage storage) {
        var data = new WirelessNetworks(true);
        var legacyName = LEGACY_PREFIX + GTOConfig.INSTANCE.devMode.aeGridKey;
        var legacy = storage.get(LegacyData::new, legacyName);
        if (legacy != null) {
            var tag = legacy.tag;
            if (tag.contains("networks", Tag.TAG_LIST)) {
                for (var entry : tag.getList("networks", Tag.TAG_COMPOUND)) {
                    var compound = (CompoundTag) entry;
                    var id = compound.getString("id");
                    if (id.isEmpty()) {
                        GTOCore.LOGGER.error("[ME无线] 迁移：跳过缺少 id 的网络记录 {}", compound);
                        continue;
                    }
                    data.migrateNetwork(id, compound, "nickname");
                }
                for (var entry : tag.getList("defaultMap", Tag.TAG_COMPOUND)) {
                    var compound = (CompoundTag) entry;
                    var network = compound.getString("value");
                    if (compound.hasUUID("key") && data.networks.containsKey(network)) {
                        data.favorites.put(compound.getUUID("key"), network);
                    } else {
                        GTOCore.LOGGER.warn("[ME无线] 迁移：丢弃无效或指向不存在网络的收藏 {}", compound);
                    }
                }
            } else if (tag.contains("WirelessSavedData", Tag.TAG_LIST)) {
                for (var entry : tag.getList("WirelessSavedData", Tag.TAG_COMPOUND)) {
                    var compound = (CompoundTag) entry;
                    var id = compound.getString("name");
                    if (id.isEmpty()) {
                        GTOCore.LOGGER.error("[ME无线] 迁移：跳过缺少 name 的网络记录 {}", compound);
                        continue;
                    }
                    var network = data.migrateNetwork(id, compound, "nickname");
                    if (network != null && compound.getBoolean("isDefault")) data.favorites.put(network.owner(), id);
                }
            }
            GTOCore.LOGGER.info("[ME无线] 从 {} 迁移了 {} 个网络、{} 条收藏", legacyName, data.networks.size(), data.favorites.size());
        }
        data.setDirty();
        return data;
    }

    @Nullable
    private WirelessNetwork migrateNetwork(String id, CompoundTag compound, String nameKey) {
        var owner = legacyUuid(compound, "owner");
        if (owner.isEmpty()) {
            GTOCore.LOGGER.error("[ME无线] 迁移：网络 {} 的 owner 缺失或无法解析，丢弃该网络", id);
            return null;
        }
        var name = compound.getString(nameKey).strip();
        var network = new WirelessNetwork(id, owner.get(), name.isEmpty() ? id : name, 0L);
        return networks.putIfAbsent(id, network) == null ? network : null;
    }

    private static Optional<UUID> legacyUuid(CompoundTag compound, String key) {
        if (!compound.contains(key)) return Optional.empty();
        return UUIDUtil.CODEC.parse(NbtOps.INSTANCE, compound.get(key))
                .resultOrPartial(error -> GTOCore.LOGGER.error("[ME无线] 迁移：UUID 解析失败 {}：{}", compound.get(key), error));
    }

    /** 旧格式文件的只读持有者：从不标记修改，存盘时不会被写回。 */
    private static final class LegacyData extends SavedData {

        private final CompoundTag tag;

        private LegacyData(CompoundTag tag) {
            this.tag = tag;
        }

        @Override
        public CompoundTag save(CompoundTag compoundTag) {
            return tag;
        }
    }
}

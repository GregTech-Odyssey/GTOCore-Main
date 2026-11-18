package com.gtocore.integration.emi.jer;

import com.gtolib.api.network.NetworkPack;
import com.gtolib.api.scope.GTOScopes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;

import com.gto.datasynclib.util.ByteBufCodecExtends;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.suried.gtoutils.schedule.ScopeKey;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// 改写自 Just Enough Resources（way2muchnoise，"Don't Be a Jerk" 非商业许可）
// https://github.com/way2muchnoise/JustEnoughResources
public final class JerLootSync {

    private static final byte PLAYER_KILL = 1;
    private static final byte LOOTING = 2;

    private static final ScopeKey<Cache> KEY = ScopeKey.of("jer_loot");

    private static final NetworkPack PACK = NetworkPack.registerS2C("gtocoreJerLoot", (player, buf) -> dataset = decode(buf));

    private static final List<ResourceLocation> VANILLA = List.of(
            BuiltInLootTables.END_CITY_TREASURE, BuiltInLootTables.SIMPLE_DUNGEON, BuiltInLootTables.VILLAGE_WEAPONSMITH,
            BuiltInLootTables.VILLAGE_TOOLSMITH, BuiltInLootTables.VILLAGE_ARMORER, BuiltInLootTables.VILLAGE_CARTOGRAPHER,
            BuiltInLootTables.VILLAGE_MASON, BuiltInLootTables.VILLAGE_SHEPHERD, BuiltInLootTables.VILLAGE_BUTCHER,
            BuiltInLootTables.VILLAGE_FLETCHER, BuiltInLootTables.VILLAGE_FISHER, BuiltInLootTables.VILLAGE_TANNERY,
            BuiltInLootTables.VILLAGE_TEMPLE, BuiltInLootTables.VILLAGE_DESERT_HOUSE, BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
            BuiltInLootTables.VILLAGE_TAIGA_HOUSE, BuiltInLootTables.VILLAGE_SNOWY_HOUSE, BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
            BuiltInLootTables.ABANDONED_MINESHAFT, BuiltInLootTables.NETHER_BRIDGE, BuiltInLootTables.STRONGHOLD_LIBRARY,
            BuiltInLootTables.STRONGHOLD_CROSSING, BuiltInLootTables.STRONGHOLD_CORRIDOR, BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.JUNGLE_TEMPLE, BuiltInLootTables.IGLOO_CHEST, BuiltInLootTables.WOODLAND_MANSION,
            BuiltInLootTables.UNDERWATER_RUIN_SMALL, BuiltInLootTables.UNDERWATER_RUIN_BIG, BuiltInLootTables.BURIED_TREASURE,
            BuiltInLootTables.SHIPWRECK_MAP, BuiltInLootTables.SHIPWRECK_SUPPLY, BuiltInLootTables.SHIPWRECK_TREASURE,
            BuiltInLootTables.PILLAGER_OUTPOST, BuiltInLootTables.BASTION_TREASURE, BuiltInLootTables.BASTION_OTHER,
            BuiltInLootTables.BASTION_BRIDGE, BuiltInLootTables.BASTION_HOGLIN_STABLE, BuiltInLootTables.RUINED_PORTAL,
            BuiltInLootTables.ANCIENT_CITY, BuiltInLootTables.ANCIENT_CITY_ICE_BOX);

    private static volatile @Nullable Dataset dataset;

    private JerLootSync() {}

    public static void install() {
        GTOScopes.SERVER.install("jer_loot", (scope, server) -> scope.provide(KEY, new Cache()));
        MinecraftForge.EVENT_BUS.addListener(JerLootSync::onDatapackSync);
    }

    static @Nullable Dataset dataset() {
        return dataset;
    }

    static void clear() {
        dataset = null;
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        if (player == null) return;
        MinecraftServer server = event.getPlayerList().getServer();
        Cache cache = GTOScopes.SERVER.lookup(server, KEY);
        byte[] bytes = cache == null ? null : cache.bytes;
        if (bytes == null) {
            bytes = encode(server);
            if (cache != null) cache.bytes = bytes;
        }
        byte[] payload = bytes;
        PACK.send(out -> out.writeBytes(payload), player);
    }

    private static byte[] encode(MinecraftServer server) {
        JerLoot loot = new JerLoot(server);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            List<ResourceLocation> mobTables = mobTables(server.getWorldData().enabledFeatures());
            List<ResourceLocation> mobIds = new ArrayList<>(mobTables.size());
            List<List<JerLoot.Drop>> mobDrops = new ArrayList<>(mobTables.size());
            for (ResourceLocation table : mobTables) {
                List<JerLoot.Drop> drops = loot.read(table, true).drops();
                if (drops.isEmpty()) continue;
                mobIds.add(table);
                mobDrops.add(drops);
            }
            buf.writeVarInt(mobIds.size());
            for (int i = 0; i < mobIds.size(); i++) {
                ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.encode(buf, mobIds.get(i));
                writeDrops(buf, mobDrops.get(i));
            }
            List<ResourceLocation> chestTables = chestTables(loot);
            List<ResourceLocation> chestIds = new ArrayList<>(chestTables.size());
            List<JerLoot.Table> chests = new ArrayList<>(chestTables.size());
            for (ResourceLocation table : chestTables) {
                JerLoot.Table read = loot.read(table, false);
                if (read.drops().isEmpty()) continue;
                chestIds.add(table);
                chests.add(read);
            }
            buf.writeVarInt(chestIds.size());
            for (int i = 0; i < chestIds.size(); i++) {
                JerLoot.Table table = chests.get(i);
                ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.encode(buf, chestIds.get(i));
                buf.writeVarInt(table.minStacks());
                buf.writeVarInt(table.maxStacks());
                writeDrops(buf, table.drops());
            }
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return bytes;
        } finally {
            buf.release();
        }
    }

    private static List<ResourceLocation> mobTables(FeatureFlagSet features) {
        OpenCacheHashSet<ResourceLocation> seen = new OpenCacheHashSet<>();
        List<ResourceLocation> tables = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() == MobCategory.MISC || !type.isEnabled(features)) continue;
            if (seen.add(type.getDefaultLootTable())) tables.add(type.getDefaultLootTable());
        }
        for (ResourceLocation table : List.of(BuiltInLootTables.SHEEP_WHITE, BuiltInLootTables.SHEEP_ORANGE, BuiltInLootTables.SHEEP_MAGENTA,
                BuiltInLootTables.SHEEP_LIGHT_BLUE, BuiltInLootTables.SHEEP_YELLOW, BuiltInLootTables.SHEEP_LIME, BuiltInLootTables.SHEEP_PINK,
                BuiltInLootTables.SHEEP_GRAY, BuiltInLootTables.SHEEP_LIGHT_GRAY, BuiltInLootTables.SHEEP_CYAN, BuiltInLootTables.SHEEP_PURPLE,
                BuiltInLootTables.SHEEP_BLUE, BuiltInLootTables.SHEEP_BROWN, BuiltInLootTables.SHEEP_GREEN, BuiltInLootTables.SHEEP_RED,
                BuiltInLootTables.SHEEP_BLACK)) {
            if (seen.add(table)) tables.add(table);
        }
        return tables;
    }

    private static List<ResourceLocation> chestTables(JerLoot loot) {
        OpenCacheHashSet<ResourceLocation> vanilla = new OpenCacheHashSet<>(VANILLA);
        List<ResourceLocation> extra = new ArrayList<>();
        for (ResourceLocation id : loot.tableIds()) {
            if (id.getPath().startsWith("chests/") && !vanilla.contains(id)) extra.add(id);
        }
        extra.sort(Comparator.naturalOrder());
        List<ResourceLocation> tables = new ArrayList<>(VANILLA.size() + extra.size());
        tables.addAll(VANILLA);
        tables.addAll(extra);
        return tables;
    }

    private static void writeDrops(FriendlyByteBuf buf, List<JerLoot.Drop> drops) {
        buf.writeVarInt(drops.size());
        for (JerLoot.Drop drop : drops) {
            buf.writeItem(drop.stack().copyWithCount(1));
            buf.writeVarInt(drop.stack().getCount());
            buf.writeVarInt(drop.min());
            buf.writeVarInt(drop.max());
            buf.writeFloat(drop.chance());
            buf.writeByte((drop.playerKill() ? PLAYER_KILL : 0) | (drop.looting() ? LOOTING : 0));
        }
    }

    private static List<JerLoot.Drop> readDrops(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size == 0) return Collections.emptyList();
        List<JerLoot.Drop> drops = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack stack = buf.readItem();
            stack.setCount(buf.readVarInt());
            int min = buf.readVarInt();
            int max = buf.readVarInt();
            float chance = buf.readFloat();
            byte flags = buf.readByte();
            drops.add(new JerLoot.Drop(stack, min, max, chance, (flags & PLAYER_KILL) != 0, (flags & LOOTING) != 0));
        }
        return Collections.unmodifiableList(drops);
    }

    private static Dataset decode(FriendlyByteBuf buf) {
        int mobCount = buf.readVarInt();
        O2OOpenCacheHashMap<ResourceLocation, List<JerLoot.Drop>> mobs = new O2OOpenCacheHashMap<>(mobCount);
        for (int i = 0; i < mobCount; i++) {
            ResourceLocation id = ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.decode(buf);
            mobs.put(id, readDrops(buf));
        }
        int chestCount = buf.readVarInt();
        List<Chest> chests = new ArrayList<>(chestCount);
        for (int i = 0; i < chestCount; i++) {
            ResourceLocation id = ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.decode(buf);
            int minStacks = buf.readVarInt();
            int maxStacks = buf.readVarInt();
            chests.add(new Chest(id, new JerLoot.Table(readDrops(buf), minStacks, maxStacks)));
        }
        return new Dataset(mobs, Collections.unmodifiableList(chests));
    }

    private static final class Cache {

        private byte @Nullable [] bytes;
    }

    record Chest(ResourceLocation id, JerLoot.Table table) {}

    record Dataset(O2OOpenCacheHashMap<ResourceLocation, List<JerLoot.Drop>> mobs, List<Chest> chests) {

        List<JerLoot.Drop> mobDrops(ResourceLocation table) {
            List<JerLoot.Drop> drops = mobs.get(table);
            return drops == null ? Collections.emptyList() : drops;
        }
    }
}

package com.gtocore.common.machine.noenergy.game.milestone;

import com.gtocore.common.machine.noenergy.game.minesweeper.MinesweeperCampaign;

import com.gtolib.GTOCore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 扫雷独立里程碑数据：基础/战役用时与每玩家 60 位战役通关位图分开存放。
 * 记录键为「开启者 + 其他参与者集合」，同键保留最短用时；只有开启者完成战役关卡才推进进度。
 */
public final class MinesweeperMilestones extends SavedData {

    public static final String DATA_NAME = "gtocore_minesweeper_milestones";

    private static final int CATEGORY_BASE = 0;
    private static final int CATEGORY_CAMPAIGN = 1;
    private static final int BASE_MODE_COUNT = 5;
    private static final int CAMPAIGN_LEVEL_COUNT = 60;

    private static final String NBT_BASE = "minesweeper_base";
    private static final String NBT_CAMPAIGN = "minesweeper_campaign";
    private static final String NBT_PROGRESS = "minesweeper_progress";
    private static final String NBT_PLAYER = "player";
    private static final String NBT_PARTICIPANTS = "participants";
    private static final String NBT_CATEGORY = "category";
    private static final String NBT_BOARD = "board";
    private static final String NBT_TICKS = "ticks";
    private static final String NBT_COMPLETED = "completed";
    private static final String NBT_CLEARED = "cleared";

    /**
     * 一条记录的玩家集合。
     *
     * @param starter      开启这局扫雷的玩家
     * @param participants 其他参与者，构造时去重、排序并剔除 starter
     */
    public record MineKey(UUID starter, List<UUID> participants) {

        public MineKey {
            Objects.requireNonNull(starter, "starter");
            if (participants == null || participants.isEmpty()) {
                participants = Collections.emptyList();
            } else {
                var sorted = new ArrayList<UUID>(participants);
                sorted.removeIf(id -> id == null || id.equals(starter));
                if (sorted.isEmpty()) {
                    participants = Collections.emptyList();
                } else {
                    Collections.sort(sorted);
                    participants = List.copyOf(sorted);
                }
            }
        }
    }

    /** 一局完成的成绩；ticks 是消耗的游戏刻，completedGameTime 是完成时的时间点。 */
    public record MineStat(MineKey key, long ticks, long completedGameTime) {}

    /** 某个记录键在一个战役章节里的汇总。 */
    public record MineCampaignChapterStat(MineKey key, long totalTicks, int completedCount, int tierSize) {}

    private record BoardKey(int category, int board, MineKey key) {}

    private static final class ChapterAccumulator {

        private long totalTicks;
        private int completedCount;
    }

    private final O2OOpenCacheHashMap<BoardKey, MineStat> records = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, Long> campaignCleared = new O2OOpenCacheHashMap<>();
    /** 每名开启者每关的最快用时；0 表示还没有成绩。写入时顺带维护，避免界面逐关扫全表。 */
    private final O2OOpenCacheHashMap<UUID, long[]> campaignLevelBestByStarter = new O2OOpenCacheHashMap<>();

    /** 写一次加一：界面靠它判断里程碑文字要不要重建（不存盘）。 */
    private int modCount;

    private MinesweeperMilestones() {}

    // ==================== 读档/取实例 ====================

    /** 读档：基础/战役记录各一张 ListTag，进度一张 ListTag。 */
    public static MinesweeperMilestones fromNBT(CompoundTag tag) {
        var data = new MinesweeperMilestones();
        readRecords(tag.getList(NBT_BASE, Tag.TAG_COMPOUND), data, CATEGORY_BASE);
        readRecords(tag.getList(NBT_CAMPAIGN, Tag.TAG_COMPOUND), data, CATEGORY_CAMPAIGN);
        ListTag progress = tag.getList(NBT_PROGRESS, Tag.TAG_COMPOUND);
        for (int index = 0; index < progress.size(); index++) {
            var entry = progress.getCompound(index);
            var player = readPlayer(entry);
            if (player == null) continue;
            data.campaignCleared.put(player, entry.getLong(NBT_CLEARED) & MinesweeperCampaign.ALL_LEVELS_MASK);
        }
        return data;
    }

    /** 主世界的数据存储里拿这一份，保证全存档唯一。 */
    public static MinesweeperMilestones get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) throw new IllegalStateException("Unable to access the Overworld.");
        return overworld.getDataStorage()
                .computeIfAbsent(MinesweeperMilestones::fromNBT, MinesweeperMilestones::new, DATA_NAME);
    }

    public static MinesweeperMilestones get(MinecraftServer server) {
        return get(server.overworld());
    }

    // ==================== 写入 ====================

    /** 五个基础模式：只记录完成且用时不小于 1 tick 的一局。 */
    public void recordBase(int difficulty, @Nullable UUID starter, @Nullable Collection<UUID> participants,
                           long ticks, long completedGameTime) {
        if (starter == null || ticks <= 0L || difficulty < 0 || difficulty >= BASE_MODE_COUNT) return;
        if (putRecord(CATEGORY_BASE, difficulty, key(starter, participants), ticks, completedGameTime)) {
            touch();
        }
    }

    /**
     * 战役一关完成：开启者推进进度；成绩按最短记录更新。
     *
     * @return 该玩家是否首次完成这一关（用于发放首通奖励）
     */
    public boolean recordCampaign(int level, @Nullable UUID starter, @Nullable Collection<UUID> participants,
                                  long ticks, long completedGameTime) {
        if (starter == null || level < 0 || level >= CAMPAIGN_LEVEL_COUNT) return false;
        long bit = 1L << level;
        long cleared = campaignCleared.getOrDefault(starter, 0L) & MinesweeperCampaign.ALL_LEVELS_MASK;
        boolean first = (cleared & bit) == 0L;
        if (first) {
            campaignCleared.put(starter, cleared | bit);
            touch();
        }
        if (ticks > 0L && putRecord(CATEGORY_CAMPAIGN, level, key(starter, participants), ticks, completedGameTime)) {
            touch();
        }
        return first;
    }

    /** 当前玩家的战役通关位图。 */
    public long campaignCleared(@Nullable UUID player) {
        if (player == null) return 0L;
        Long cleared = campaignCleared.get(player);
        return cleared == null ? 0L : cleared & MinesweeperCampaign.ALL_LEVELS_MASK;
    }

    public boolean isCampaignUnlocked(@Nullable UUID player, int level) {
        return MinesweeperCampaign.isUnlocked(campaignCleared(player), level);
    }

    /** 当前数据版本：变了说明有记录/进度更新，界面该重建文字。 */
    public int modCount() {
        return modCount;
    }

    private void touch() {
        modCount++;
        setDirty();
    }

    private static MineKey key(UUID starter, @Nullable Collection<UUID> participants) {
        return new MineKey(starter, participants == null ? Collections.emptyList() : new ArrayList<>(participants));
    }

    private boolean putRecord(int category, int board, MineKey key, long ticks, long completedGameTime) {
        BoardKey boardKey = new BoardKey(category, board, key);
        MineStat old = records.get(boardKey);
        if (old != null && old.ticks() <= ticks) return false;
        records.put(boardKey, new MineStat(key, ticks, completedGameTime));
        if (category == CATEGORY_CAMPAIGN && board >= 0 && board < CAMPAIGN_LEVEL_COUNT) {
            long[] bestByLevel = campaignLevelBestByStarter.computeIfAbsent(key.starter(), k -> new long[CAMPAIGN_LEVEL_COUNT]);
            if (bestByLevel[board] <= 0L || ticks < bestByLevel[board]) bestByLevel[board] = ticks;
        }
        return true;
    }

    // ==================== 读取 ====================

    /** 某个开启者在某一关的最快用时；没有成绩时返回 0。 */
    public long campaignLevelBestTicks(int level, @Nullable UUID starter) {
        if (starter == null) return 0L;
        long[] bestByLevel = campaignLevelBestByStarter.get(starter);
        return bestByLevel == null ? 0L : bestByLevel[Math.clamp(level, 0, CAMPAIGN_LEVEL_COUNT - 1)];
    }

    /** 某个玩家是否已通关某一关。 */
    public boolean isCampaignCleared(@Nullable UUID player, int level) {
        if (player == null) return false;
        int index = Math.clamp(level, 0, CAMPAIGN_LEVEL_COUNT - 1);
        return (campaignCleared(player) & (1L << index)) != 0L;
    }

    /** 某个基础模式的成绩，用时升序；同用时按完成时间升序。 */
    public List<MineStat> baseBoard(int difficulty) {
        var board = new ObjectArrayList<MineStat>();
        for (var entry : records.entrySet()) {
            BoardKey key = entry.getKey();
            if (key.category() == CATEGORY_BASE && key.board() == difficulty) {
                board.add(entry.getValue());
            }
        }
        board.sort(Comparator.comparingLong(MineStat::ticks).thenComparingLong(MineStat::completedGameTime));
        return board;
    }

    /**
     * 某个战役章节（按全局关卡区间）的汇总：每个玩家集合一行，统计该章已完成关数与总用时。
     *
     * @param tierStart 章节起始全局关卡
     * @param tierSize  章节关卡数
     */
    public List<MineCampaignChapterStat> campaignChapterBoard(int tierStart, int tierSize) {
        int start = Math.clamp(tierStart, 0, CAMPAIGN_LEVEL_COUNT - 1);
        int end = Math.min(CAMPAIGN_LEVEL_COUNT, start + Math.max(0, tierSize));
        var accumulators = new O2OOpenCacheHashMap<MineKey, ChapterAccumulator>();
        for (var entry : records.entrySet()) {
            BoardKey key = entry.getKey();
            if (key.category() != CATEGORY_CAMPAIGN || key.board() < start || key.board() >= end) continue;
            var accumulator = accumulators.computeIfAbsent(key.key(), k -> new ChapterAccumulator());
            accumulator.totalTicks += entry.getValue().ticks();
            accumulator.completedCount++;
        }
        var board = new ObjectArrayList<MineCampaignChapterStat>(accumulators.size());
        for (var entry : accumulators.entrySet()) {
            var accumulator = entry.getValue();
            board.add(new MineCampaignChapterStat(entry.getKey(), accumulator.totalTicks,
                    accumulator.completedCount, Math.max(0, tierSize)));
        }
        board.sort(Comparator.comparingLong(MineCampaignChapterStat::totalTicks)
                .thenComparing(stat -> stat.key().starter().toString()));
        return board;
    }

    /** 榜单里的玩家名：档案缓存优先，取不到就用 UUID 顶着。 */
    public static String nameOf(MinecraftServer server, UUID player) {
        var cache = server.getProfileCache();
        Optional<GameProfile> profile = cache == null ? Optional.empty() : cache.get(player);
        return profile.map(GameProfile::getName).orElse(player.toString());
    }

    // ==================== 存盘 ====================

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag base = new ListTag();
        ListTag campaign = new ListTag();
        for (var entry : records.entrySet()) {
            BoardKey key = entry.getKey();
            if (key.category() == CATEGORY_BASE) {
                base.add(saveRecord(key, entry.getValue()));
            } else if (key.category() == CATEGORY_CAMPAIGN) {
                campaign.add(saveRecord(key, entry.getValue()));
            }
        }
        tag.put(NBT_BASE, base);
        tag.put(NBT_CAMPAIGN, campaign);

        ListTag progress = new ListTag();
        for (var entry : campaignCleared.entrySet()) {
            if (entry.getKey() == null) continue;
            var record = new CompoundTag();
            record.putUUID(NBT_PLAYER, entry.getKey());
            record.putLong(NBT_CLEARED, entry.getValue() & MinesweeperCampaign.ALL_LEVELS_MASK);
            progress.add(record);
        }
        tag.put(NBT_PROGRESS, progress);
        return tag;
    }

    private static CompoundTag saveRecord(BoardKey key, MineStat stat) {
        var record = new CompoundTag();
        record.putUUID(NBT_PLAYER, stat.key().starter());
        record.putInt(NBT_CATEGORY, key.category());
        record.putInt(NBT_BOARD, key.board());
        record.putLong(NBT_TICKS, stat.ticks());
        record.putLong(NBT_COMPLETED, stat.completedGameTime());
        ListTag participants = new ListTag();
        for (UUID participant : stat.key().participants()) {
            var entry = new CompoundTag();
            entry.putUUID(NBT_PLAYER, participant);
            participants.add(entry);
        }
        record.put(NBT_PARTICIPANTS, participants);
        return record;
    }

    private static void readRecords(ListTag list, MinesweeperMilestones data, int category) {
        for (int index = 0; index < list.size(); index++) {
            var entry = list.getCompound(index);
            var starter = readPlayer(entry);
            if (starter == null) continue;
            int board = entry.getInt(NBT_BOARD);
            if (category == CATEGORY_BASE && (board < 0 || board >= BASE_MODE_COUNT)) continue;
            if (category == CATEGORY_CAMPAIGN && (board < 0 || board >= CAMPAIGN_LEVEL_COUNT)) continue;
            long ticks = entry.getLong(NBT_TICKS);
            if (ticks <= 0L) continue;
            long completed = entry.getLong(NBT_COMPLETED);
            List<UUID> participants = readParticipants(entry.getList(NBT_PARTICIPANTS, Tag.TAG_COMPOUND));
            data.putRecord(category, board, new MineKey(starter, participants), ticks, completed);
        }
    }

    private static List<UUID> readParticipants(ListTag list) {
        if (list.isEmpty()) return Collections.emptyList();
        var participants = new ArrayList<UUID>(list.size());
        for (int index = 0; index < list.size(); index++) {
            var entry = list.getCompound(index);
            if (!entry.hasUUID(NBT_PLAYER)) continue;
            participants.add(entry.getUUID(NBT_PLAYER));
        }
        return participants;
    }

    private static @Nullable UUID readPlayer(CompoundTag entry) {
        if (!entry.hasUUID(NBT_PLAYER)) {
            GTOCore.LOGGER.warn("Dropped a minesweeper milestone entry without a player id");
            return null;
        }
        return entry.getUUID(NBT_PLAYER);
    }
}

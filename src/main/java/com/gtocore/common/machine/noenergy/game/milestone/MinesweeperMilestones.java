package com.gtocore.common.machine.noenergy.game.milestone;

import com.gtocore.common.machine.noenergy.game.minesweeper.MinesweeperCampaign;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;
import com.gto.datasynclib.datastream.codec.JavaValueOps;
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
 * 扫雷独立里程碑数据：基础/战役用时与每玩家的 60 位战役通关位图分开存放。
 * 记录键为「开启者 + 其他参与者集合」，同键保留最短用时；只有开启者完成战役关卡才推进进度。
 *
 * <p>
 * 存盘由 DataSyncLib 承担：{@link CompoundTag} 只是 {@link SavedData} 的外层容器，里面放一个字节数组，
 * 整份数据由 {@link #DATA_CODEC} 编成 carrier 再经 {@link JavaValueOps#toBytes(Object)} 落盘；
 * 复合键由 codec 按分量写。
 */
public final class MinesweeperMilestones extends SavedData {

    private static final String DATA_NAME = "gtocore_minesweeper_milestones";

    private static final int CATEGORY_BASE = 0;
    private static final int CATEGORY_CAMPAIGN = 1;
    private static final int BASE_MODE_COUNT = 5;
    private static final int CAMPAIGN_LEVEL_COUNT = 60;

    /** 载荷所在键：整份数据编码后的字节数组。 */
    private static final String NBT_DATA = "data";
    /** 载荷的格式版本，读档时交给 {@link JavaValueOps#create(int)}，codec 里用 {@code ops.dataVersion()} 取。 */
    private static final String NBT_VERSION = "data_version";
    private static final int DATA_VERSION = 1;

    /**
     * 一条记录的玩家集合。
     *
     * @param starter      开启这局扫雷的玩家
     * @param participants 其他参与者，构造时剔除 null 与 starter 并排序（不去重）
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

    /** 记录表的键：分区（基础/战役）+ 具体档位 + 玩家集合。 */
    private record BoardKey(int category, int board, MineKey key) {}

    private static final class ChapterAccumulator {

        private long totalTicks;
        private int completedCount;
    }

    // ==================== 编解码 ====================

    /** 参与者集合：两侧都是紧凑的 UUID 列表（通常 0~3 个）。 */
    private static final DataSyncCodec<List<UUID>> PARTICIPANTS_CODEC = CombinedCodec.list(DataSyncCodec.UUID_CODEC);

    private static final DataSyncCodec<MineKey> MINE_KEY_CODEC = CombinedCodec.composite(
            DataSyncCodec.UUID_CODEC, MineKey::starter,
            PARTICIPANTS_CODEC, MineKey::participants,
            MineKey::new);

    private static final DataSyncCodec<BoardKey> BOARD_KEY_CODEC = CombinedCodec.composite(
            DataSyncCodec.INT_CODEC, BoardKey::category,
            DataSyncCodec.INT_CODEC, BoardKey::board,
            MINE_KEY_CODEC, BoardKey::key,
            BoardKey::new);

    private static final DataSyncCodec<MineStat> MINE_STAT_CODEC = CombinedCodec.composite(
            MINE_KEY_CODEC, MineStat::key,
            DataSyncCodec.LONG_CODEC, MineStat::ticks,
            DataSyncCodec.LONG_CODEC, MineStat::completedGameTime,
            MineStat::new);

    private static final DataSyncCodec<O2OOpenCacheHashMap<BoardKey, MineStat>> RECORDS_CODEC = CombinedCodec.map(size -> new O2OOpenCacheHashMap<>(), BOARD_KEY_CODEC, MINE_STAT_CODEC);

    private static final DataSyncCodec<O2OOpenCacheHashMap<UUID, Long>> CLEARED_CODEC = CombinedCodec.map(size -> new O2OOpenCacheHashMap<>(), DataSyncCodec.UUID_CODEC, DataSyncCodec.LONG_CODEC);

    /** 整份数据：记录表 + 每位开启者的通关位图；派生的每关最快用时不入盘，解码后由 {@link #of} 重建。 */
    private static final DataSyncCodec<MinesweeperMilestones> DATA_CODEC = CombinedCodec.composite(
            RECORDS_CODEC, MinesweeperMilestones::records,
            CLEARED_CODEC, MinesweeperMilestones::campaignClearedField,
            MinesweeperMilestones::of);

    // ==================== 数据 ====================

    private final O2OOpenCacheHashMap<BoardKey, MineStat> records = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, Long> campaignCleared = new O2OOpenCacheHashMap<>();
    /** 每名开启者每关的最快用时；0 表示还没有成绩。写入时顺带维护，避免界面逐关扫全表（不存盘，解码后重建）。 */
    private final O2OOpenCacheHashMap<UUID, long[]> campaignLevelBestByStarter = new O2OOpenCacheHashMap<>();

    /** 写一次加一：界面靠它判断里程碑文字要不要重建（不存盘）。 */
    private int modCount;

    private MinesweeperMilestones() {}

    // ==================== codec 取数 ====================

    private O2OOpenCacheHashMap<BoardKey, MineStat> records() {
        return records;
    }

    private O2OOpenCacheHashMap<UUID, Long> campaignClearedField() {
        return campaignCleared;
    }

    /**
     * 解码构造：装回两张表，并按记录表重建「每开启者每关最快用时」读缓存。
     *
     * <p>
     * 空表在存盘格式里写成 null（空 Map 编成 null），解码回来就是 {@code null}，按空表处理。
     */
    private static MinesweeperMilestones of(@Nullable O2OOpenCacheHashMap<BoardKey, MineStat> records,
                                            @Nullable O2OOpenCacheHashMap<UUID, Long> campaignCleared) {
        var data = new MinesweeperMilestones();
        if (records != null) data.records.putAll(records);
        if (campaignCleared != null) {
            campaignCleared.forEach((player, cleared) -> data.campaignCleared.put(player, cleared & MinesweeperCampaign.ALL_LEVELS_MASK));
        }
        // 读缓存是推导值：直接按记录表重算，读档后 campaignLevelBestTicks 立刻正确
        for (var entry : data.records.entrySet()) {
            data.indexCampaignBest(entry.getKey(), entry.getValue());
        }
        return data;
    }

    // ==================== 读档/取实例 ====================

    /** 读档：{@code data} 是整份载荷，{@code data_version} 交回 codec。 */
    public static MinesweeperMilestones fromNBT(CompoundTag tag) {
        var ops = JavaValueOps.create(tag.getInt(NBT_VERSION));
        return DATA_CODEC.decode(ops, ops.fromBytes(tag.getByteArray(NBT_DATA)));
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
        var stat = new MineStat(key, ticks, completedGameTime);
        records.put(boardKey, stat);
        indexCampaignBest(boardKey, stat);
        return true;
    }

    /** 把一条记录折进「每开启者每关最快用时」读缓存；只对战役分区有意义。 */
    private void indexCampaignBest(BoardKey boardKey, MineStat stat) {
        if (boardKey.category() != CATEGORY_CAMPAIGN) return;
        int board = boardKey.board();
        if (board < 0 || board >= CAMPAIGN_LEVEL_COUNT) return;
        long[] bestByLevel = campaignLevelBestByStarter.computeIfAbsent(stat.key().starter(),
                k -> new long[CAMPAIGN_LEVEL_COUNT]);
        if (bestByLevel[board] <= 0L || stat.ticks() < bestByLevel[board]) bestByLevel[board] = stat.ticks();
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

    /** 存盘：整份数据编成一个 carrier，再写成 {@code data} 字节数组；版本号与载荷分开写。 */
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        var ops = JavaValueOps.create(DATA_VERSION);
        tag.putByteArray(NBT_DATA, ops.toBytes(DATA_CODEC.encode(ops, this)));
        tag.putInt(NBT_VERSION, DATA_VERSION);
        return tag;
    }
}

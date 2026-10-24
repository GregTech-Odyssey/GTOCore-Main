package com.gtocore.common.machine.noenergy.game.milestone;

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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 四台游戏机的玩家里程碑：每个存档一份（`world/data/gtocore_game_milestones.dat`），按游戏分成四个列表。
 *
 * <p>
 * 存盘格式与 {@code WalletSavedData} 一致：{@code save(CompoundTag)} 把每张表写成 {@link ListTag}（每条一个
 * compound，里面带玩家 UUID 与各项数字），读档走 {@link #fromNBT(CompoundTag)}，入口是
 * {@link #get(ServerLevel)}（绑定主世界，保证全局唯一）；写操作一律 {@link #setDirty()}，随原版自动保存落盘。
 *
 * <p>
 * 写入只发生在服务端、且只记单人局（见 {@code AbstractGameMachine#sessionCounts()}）；这里只存数据，名字与文案由
 * 调用方在拼文本时解析（{@link #nameOf}）。
 */
public final class GameMilestones extends SavedData {

    public static final String DATA_NAME = "gtocore_game_milestones";
    /** 最高分/最大合成榜的最低积分：不超过这个分数的局不进入里程碑。 */
    public static final int MIN_MILESTONE_SCORE = 1000;

    /** 游戏名（存盘分区与榜单标识）。 */
    public static final String SLOT_MACHINE = "slot_machine";
    public static final String JUST_GET_10 = "just_get_10";
    public static final String GAME_2048 = "game_2048";
    public static final String GAME_24 = "game_24";

    private static final String NBT_SLOT_MACHINE = "slot_machine";
    private static final String NBT_JUST_GET_10 = "just_get_10";
    private static final String NBT_GAME_2048 = "game_2048";
    private static final String NBT_GAME_24 = "game_24";
    private static final String NBT_PLAYER = "player";
    private static final String NBT_SPINS = "spins";
    private static final String NBT_STAKED = "staked";
    private static final String NBT_WON = "won";
    private static final String NBT_SCORE = "score";
    private static final String NBT_SCORE_TIME = "score_time";
    private static final String NBT_MERGED = "merged";
    private static final String NBT_MERGED_TIME = "merged_time";
    private static final String NBT_TEN = "ten";
    private static final String NBT_TEN_TIME = "ten_time";
    private static final String NBT_THIRTEEN = "thirteen";
    private static final String NBT_THIRTEEN_TIME = "thirteen_time";

    // ==================== 表项 ====================

    /** 老虎机：抽取次数、抽奖消耗的币总和、累计获得（后两个都是币值）。 */
    public record SlotStat(UUID player, long spins, long staked, long won) {}

    /** 合成十 / 2048：最高分与最大合成数字，各带达成时间。 */
    public record ScoreStat(UUID player, int bestScore, long bestScoreTime, int bestMerged, long bestMergedTime) {}

    /** 24 点：两个抽牌档各自的「10 题最快用时」（tick，0 表示没记录）与达成时间。 */
    public record PuzzleStat(UUID player, long tenTicks, long tenTime, long thirteenTicks, long thirteenTime) {}

    /** 老虎机榜单：回报比降序 → 获得降序 → 次数降序。 */
    private static final Comparator<SlotStat> SLOT_ORDER = Comparator
            .comparingDouble(GameMilestones::slotRatio).reversed()
            .thenComparing(Comparator.comparingLong(SlotStat::won).reversed())
            .thenComparing(Comparator.comparingLong(SlotStat::spins).reversed());
    /** 合成十 / 2048 榜单：最高分降序 → 最大合成降序 → 达成得早的在前。 */
    private static final Comparator<ScoreStat> SCORE_ORDER = Comparator
            .comparingInt(ScoreStat::bestScore).reversed()
            .thenComparing(Comparator.comparingInt(ScoreStat::bestMerged).reversed())
            .thenComparingLong(ScoreStat::bestScoreTime);

    // ==================== 数据 ====================

    private final O2OOpenCacheHashMap<UUID, SlotStat> slots = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, ScoreStat> justGet10 = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, ScoreStat> game2048 = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, PuzzleStat> puzzles = new O2OOpenCacheHashMap<>();

    /** 写一次加一：界面靠它判断里程碑文字要不要重建（不存盘）。 */
    private int modCount;

    private GameMilestones() {}

    /** 读档：四个分区各是一个 {@link ListTag}，每条里带玩家 UUID。 */
    public static GameMilestones fromNBT(CompoundTag tag) {
        var data = new GameMilestones();
        ListTag slots = tag.getList(NBT_SLOT_MACHINE, Tag.TAG_COMPOUND);
        for (int index = 0; index < slots.size(); index++) {
            var entry = slots.getCompound(index);
            var player = readPlayer(entry);
            if (player == null) continue;
            data.slots.put(player, new SlotStat(player, entry.getLong(NBT_SPINS), entry.getLong(NBT_STAKED),
                    entry.getLong(NBT_WON)));
        }
        readScores(tag.getList(NBT_JUST_GET_10, Tag.TAG_COMPOUND), data.justGet10);
        readScores(tag.getList(NBT_GAME_2048, Tag.TAG_COMPOUND), data.game2048);
        ListTag puzzles = tag.getList(NBT_GAME_24, Tag.TAG_COMPOUND);
        for (int index = 0; index < puzzles.size(); index++) {
            var entry = puzzles.getCompound(index);
            var player = readPlayer(entry);
            if (player == null) continue;
            data.puzzles.put(player, new PuzzleStat(player, entry.getLong(NBT_TEN), entry.getLong(NBT_TEN_TIME),
                    entry.getLong(NBT_THIRTEEN), entry.getLong(NBT_THIRTEEN_TIME)));
        }
        return data;
    }

    /** 主世界的数据存储里拿这一份（与钱包同一套取法，保证全局唯一）。 */
    public static GameMilestones get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) throw new IllegalStateException("Unable to access the Overworld.");
        return overworld.getDataStorage().computeIfAbsent(GameMilestones::fromNBT, GameMilestones::new, DATA_NAME);
    }

    /** 便捷入口：从服务器直接取（同样绑定主世界）。 */
    public static GameMilestones get(MinecraftServer server) {
        return get(server.overworld());
    }

    // ==================== 写入（服务端） ====================

    /** 老虎机：一次旋转结束 —— 次数 +1、抽奖消耗（这一把的下注）与赢得币值累加。 */
    public void addSpin(@Nullable UUID player, long bet, long reward) {
        if (player == null) return;
        slots.compute(player, (k, old) -> new SlotStat(player, (old == null ? 0L : old.spins()) + 1,
                addSaturated(old == null ? 0L : old.staked(), bet),
                addSaturated(old == null ? 0L : old.won(), reward)));
        touch();
    }

    /** 合成十 / 2048：一局结账 —— 分数与最大合成数字各取更优值，各自记住达成时间；不超过 {@link #MIN_MILESTONE_SCORE} 的局不记录。 */
    public void recordScore(String game, @Nullable UUID player, int score, int merged, long gameTime) {
        var stats = scoreStats(game);
        if (stats == null || player == null || score <= MIN_MILESTONE_SCORE) return;
        var old = stats.get(player);
        int bestScore = old == null ? 0 : old.bestScore();
        long bestScoreTime = old == null ? 0L : old.bestScoreTime();
        int bestMerged = old == null ? 0 : old.bestMerged();
        long bestMergedTime = old == null ? 0L : old.bestMergedTime();
        boolean changed = false;
        if (score > bestScore) {
            bestScore = score;
            bestScoreTime = gameTime;
            changed = true;
        }
        if (merged > bestMerged) {
            bestMerged = merged;
            bestMergedTime = gameTime;
            changed = true;
        }
        if (!changed) return;
        stats.put(player, new ScoreStat(player, bestScore, bestScoreTime, bestMerged, bestMergedTime));
        touch();
    }

    /** 24 点：一轮 10 题完成 —— 该抽牌档取更短的用时，并记住达成时间。 */
    public void recordPuzzle(@Nullable UUID player, int rangeOrdinal, long ticks, long gameTime) {
        if (player == null || ticks <= 0) return;
        var old = puzzles.get(player);
        long tenTicks = old == null ? 0L : old.tenTicks();
        long tenTime = old == null ? 0L : old.tenTime();
        long thirteenTicks = old == null ? 0L : old.thirteenTicks();
        long thirteenTime = old == null ? 0L : old.thirteenTime();
        if (rangeOrdinal == 0) {
            if (tenTicks > 0 && ticks >= tenTicks) return;
            tenTicks = ticks;
            tenTime = gameTime;
        } else {
            if (thirteenTicks > 0 && ticks >= thirteenTicks) return;
            thirteenTicks = ticks;
            thirteenTime = gameTime;
        }
        puzzles.put(player, new PuzzleStat(player, tenTicks, tenTime, thirteenTicks, thirteenTime));
        touch();
    }

    /** 当前数据版本：变了说明有记录更新，界面该重建文字。 */
    public int modCount() {
        return modCount;
    }

    private void touch() {
        modCount++;
        setDirty();
    }

    // ==================== 读取（已排序） ====================

    public List<SlotStat> slotBoard() {
        var board = new ObjectArrayList<SlotStat>(slots.size());
        board.addAll(slots.values());
        board.sort(SLOT_ORDER);
        return board;
    }

    public List<ScoreStat> justGet10Board() {
        return scoreBoard(justGet10);
    }

    public List<ScoreStat> game2048Board() {
        return scoreBoard(game2048);
    }

    /** 24 点某个抽牌档的榜单（0 = 1~10，1 = 1~13）：用时升序，只收有这一档记录的人。 */
    public List<PuzzleStat> puzzleBoard(int rangeOrdinal) {
        boolean ten = rangeOrdinal == 0;
        var board = new ObjectArrayList<PuzzleStat>(puzzles.size());
        for (var stat : puzzles.values()) {
            if ((ten ? stat.tenTicks() : stat.thirteenTicks()) <= 0L) continue;
            board.add(stat);
        }
        board.sort(ten ?
                Comparator.comparingLong(PuzzleStat::tenTicks).thenComparingLong(PuzzleStat::tenTime) :
                Comparator.comparingLong(PuzzleStat::thirteenTicks).thenComparingLong(PuzzleStat::thirteenTime));
        return board;
    }

    /** 老虎机的回报比：获得 / 抽奖消耗；没消耗过算 0。 */
    public static double slotRatio(SlotStat stat) {
        return stat.staked() <= 0 ? 0.0 : stat.won() / (double) stat.staked();
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
        ListTag slots = new ListTag();
        for (var stat : this.slots.values()) {
            var entry = new CompoundTag();
            entry.putUUID(NBT_PLAYER, stat.player());
            entry.putLong(NBT_SPINS, stat.spins());
            entry.putLong(NBT_STAKED, stat.staked());
            entry.putLong(NBT_WON, stat.won());
            slots.add(entry);
        }
        tag.put(NBT_SLOT_MACHINE, slots);
        tag.put(NBT_JUST_GET_10, saveScores(justGet10));
        tag.put(NBT_GAME_2048, saveScores(game2048));

        ListTag puzzles = new ListTag();
        for (var stat : this.puzzles.values()) {
            var entry = new CompoundTag();
            entry.putUUID(NBT_PLAYER, stat.player());
            entry.putLong(NBT_TEN, stat.tenTicks());
            entry.putLong(NBT_TEN_TIME, stat.tenTime());
            entry.putLong(NBT_THIRTEEN, stat.thirteenTicks());
            entry.putLong(NBT_THIRTEEN_TIME, stat.thirteenTime());
            puzzles.add(entry);
        }
        tag.put(NBT_GAME_24, puzzles);
        return tag;
    }

    private static ListTag saveScores(O2OOpenCacheHashMap<UUID, ScoreStat> stats) {
        var list = new ListTag();
        for (var stat : stats.values()) {
            var entry = new CompoundTag();
            entry.putUUID(NBT_PLAYER, stat.player());
            entry.putInt(NBT_SCORE, stat.bestScore());
            entry.putLong(NBT_SCORE_TIME, stat.bestScoreTime());
            entry.putInt(NBT_MERGED, stat.bestMerged());
            entry.putLong(NBT_MERGED_TIME, stat.bestMergedTime());
            list.add(entry);
        }
        return list;
    }

    private static void readScores(ListTag list, O2OOpenCacheHashMap<UUID, ScoreStat> target) {
        for (int index = 0; index < list.size(); index++) {
            var entry = list.getCompound(index);
            var player = readPlayer(entry);
            if (player == null) continue;
            target.put(player, new ScoreStat(player, entry.getInt(NBT_SCORE), entry.getLong(NBT_SCORE_TIME),
                    entry.getInt(NBT_MERGED), entry.getLong(NBT_MERGED_TIME)));
        }
    }

    private static @Nullable UUID readPlayer(CompoundTag entry) {
        if (!entry.hasUUID(NBT_PLAYER)) {
            GTOCore.LOGGER.warn("Dropped a milestone entry without a player id");
            return null;
        }
        return entry.getUUID(NBT_PLAYER);
    }

    private List<ScoreStat> scoreBoard(O2OOpenCacheHashMap<UUID, ScoreStat> stats) {
        var board = new ObjectArrayList<ScoreStat>(stats.size());
        board.addAll(stats.values());
        board.sort(SCORE_ORDER);
        return board;
    }

    @Nullable
    private O2OOpenCacheHashMap<UUID, ScoreStat> scoreStats(String game) {
        return JUST_GET_10.equals(game) ? justGet10 : GAME_2048.equals(game) ? game2048 : null;
    }

    private static long addSaturated(long base, long add) {
        if (add <= 0) return base;
        return base > Long.MAX_VALUE - add ? Long.MAX_VALUE : base + add;
    }
}

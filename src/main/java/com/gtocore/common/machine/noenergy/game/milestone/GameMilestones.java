package com.gtocore.common.machine.noenergy.game.milestone;

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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 游戏机的玩家里程碑：每个存档一份（`world/data/gtocore_game_milestones.dat`），按游戏分成多张表。
 *
 * <p>
 * 存盘由 DataSyncLib 承担：{@link CompoundTag} 只是 {@link SavedData} 的外层容器，里面放一个字节数组，
 * 整份数据由 {@link #DATA_CODEC} 编成 carrier 再经 {@link JavaValueOps#toBytes(Object)} 落盘。
 * 记录各分量的顺序与编码由 codec 决定，新增统计只改 record 与 codec。
 *
 * <p>
 * 只在服务端写入，且只记单人局（见 {@code AbstractGameMachine#sessionCounts()}）。
 */
public final class GameMilestones extends SavedData {

    private static final String DATA_NAME = "gtocore_game_milestones";
    /** 最高分/最大合成榜的最低积分：不超过这个分数的局不进入里程碑。 */
    private static final int MIN_MILESTONE_SCORE = 1000;

    /** 游戏名（榜单标识）。 */
    public static final String SLOT_MACHINE = "slot_machine";
    public static final String JUST_GET_10 = "just_get_10";
    public static final String GAME_2048 = "game_2048";
    public static final String GAME_24 = "game_24";
    public static final String NUMBER_PUZZLE = "number_puzzle";

    /** 载荷所在键：整份数据编码后的字节数组。 */
    private static final String NBT_DATA = "data";
    /** 载荷的格式版本，读档时交给 {@link JavaValueOps#create(int)}，codec 里用 {@code ops.dataVersion()} 取。 */
    private static final String NBT_VERSION = "data_version";
    private static final int DATA_VERSION = 1;

    /** 华容道尺寸档数。 */
    private static final int FIXED_SLOTS = 6;

    // ==================== 表项 ====================

    /** 老虎机：抽取次数、抽奖消耗的币总和、累计获得（后两个都是币值）。 */
    public record SlotStat(UUID player, long spins, long staked, long won) {}

    /** 合成十 / 2048：最高分与最大合成数字，各带达成时间。 */
    public record ScoreStat(UUID player, int bestScore, long bestScoreTime, int bestMerged, long bestMergedTime) {}

    /** 24 点：两个抽牌档各自的「10 题最快用时」（tick，0 表示没记录）与达成时间。 */
    public record PuzzleStat(UUID player, long tenTicks, long tenTime, long thirteenTicks, long thirteenTime) {}

    /** 数字华容道：六档尺寸各自的最快用时与达成时间；0 表示该尺寸没有记录。 */
    public record NumberPuzzleStat(UUID player, long[] ticks, long[] times) {}

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
    /** 数字华容道榜单：完成尺寸数降序，其次总用时升序。 */
    private static final Comparator<NumberPuzzleStat> NUMBER_PUZZLE_ORDER = Comparator
            .comparingInt(GameMilestones::numberPuzzleCompleted).reversed()
            .thenComparingLong(GameMilestones::numberPuzzleTotal)
            .thenComparing(stat -> stat.player().toString());

    // ==================== 编解码 ====================

    private static final DataSyncCodec<SlotStat> SLOT_CODEC = CombinedCodec.composite(
            DataSyncCodec.UUID_CODEC, SlotStat::player,
            DataSyncCodec.LONG_CODEC, SlotStat::spins,
            DataSyncCodec.LONG_CODEC, SlotStat::staked,
            DataSyncCodec.LONG_CODEC, SlotStat::won,
            SlotStat::new);

    private static final DataSyncCodec<ScoreStat> SCORE_CODEC = CombinedCodec.composite(
            DataSyncCodec.UUID_CODEC, ScoreStat::player,
            DataSyncCodec.INT_CODEC, ScoreStat::bestScore,
            DataSyncCodec.LONG_CODEC, ScoreStat::bestScoreTime,
            DataSyncCodec.INT_CODEC, ScoreStat::bestMerged,
            DataSyncCodec.LONG_CODEC, ScoreStat::bestMergedTime,
            ScoreStat::new);

    private static final DataSyncCodec<PuzzleStat> PUZZLE_CODEC = CombinedCodec.composite(
            DataSyncCodec.UUID_CODEC, PuzzleStat::player,
            DataSyncCodec.LONG_CODEC, PuzzleStat::tenTicks,
            DataSyncCodec.LONG_CODEC, PuzzleStat::tenTime,
            DataSyncCodec.LONG_CODEC, PuzzleStat::thirteenTicks,
            DataSyncCodec.LONG_CODEC, PuzzleStat::thirteenTime,
            PuzzleStat::new);

    private static final DataSyncCodec<NumberPuzzleStat> NUMBER_PUZZLE_CODEC = CombinedCodec.composite(
            DataSyncCodec.UUID_CODEC, NumberPuzzleStat::player,
            DataSyncCodec.LONGS_CODEC, NumberPuzzleStat::ticks,
            DataSyncCodec.LONGS_CODEC, NumberPuzzleStat::times,
            NumberPuzzleStat::new);

    /** 五张表：键都是玩家 UUID，值是整条记录。UUID 走两个 long，比存字符串短。 */
    private static final DataSyncCodec<O2OOpenCacheHashMap<UUID, SlotStat>> SLOTS_CODEC = CombinedCodec.map(size -> new O2OOpenCacheHashMap<>(), DataSyncCodec.UUID_CODEC, SLOT_CODEC);

    private static final DataSyncCodec<O2OOpenCacheHashMap<UUID, ScoreStat>> JUST_GET_10_CODEC = CombinedCodec.map(size -> new O2OOpenCacheHashMap<>(), DataSyncCodec.UUID_CODEC, SCORE_CODEC);

    private static final DataSyncCodec<O2OOpenCacheHashMap<UUID, ScoreStat>> GAME_2048_CODEC = CombinedCodec.map(size -> new O2OOpenCacheHashMap<>(), DataSyncCodec.UUID_CODEC, SCORE_CODEC);

    private static final DataSyncCodec<O2OOpenCacheHashMap<UUID, PuzzleStat>> PUZZLES_CODEC = CombinedCodec.map(size -> new O2OOpenCacheHashMap<>(), DataSyncCodec.UUID_CODEC, PUZZLE_CODEC);

    private static final DataSyncCodec<O2OOpenCacheHashMap<UUID, NumberPuzzleStat>> NUMBER_PUZZLES_CODEC = CombinedCodec.map(size -> new O2OOpenCacheHashMap<>(), DataSyncCodec.UUID_CODEC, NUMBER_PUZZLE_CODEC);

    /** 整份数据：五张表按声明顺序串成一个 carrier；解码侧 {@link #of} 装回去。 */
    private static final DataSyncCodec<GameMilestones> DATA_CODEC = CombinedCodec.composite(
            SLOTS_CODEC, GameMilestones::slots,
            JUST_GET_10_CODEC, GameMilestones::justGet10,
            GAME_2048_CODEC, GameMilestones::game2048,
            PUZZLES_CODEC, GameMilestones::puzzles,
            NUMBER_PUZZLES_CODEC, GameMilestones::numberPuzzles,
            GameMilestones::of);

    // ==================== 数据 ====================

    private final O2OOpenCacheHashMap<UUID, SlotStat> slots = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, ScoreStat> justGet10 = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, ScoreStat> game2048 = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, PuzzleStat> puzzles = new O2OOpenCacheHashMap<>();
    private final O2OOpenCacheHashMap<UUID, NumberPuzzleStat> numberPuzzles = new O2OOpenCacheHashMap<>();

    /** 写一次加一：界面靠它判断里程碑文字要不要重建（不存盘）。 */
    private int modCount;

    private GameMilestones() {}

    // ==================== codec 取数 ====================

    private O2OOpenCacheHashMap<UUID, SlotStat> slots() {
        return slots;
    }

    private O2OOpenCacheHashMap<UUID, ScoreStat> justGet10() {
        return justGet10;
    }

    private O2OOpenCacheHashMap<UUID, ScoreStat> game2048() {
        return game2048;
    }

    private O2OOpenCacheHashMap<UUID, PuzzleStat> puzzles() {
        return puzzles;
    }

    private O2OOpenCacheHashMap<UUID, NumberPuzzleStat> numberPuzzles() {
        return numberPuzzles;
    }

    /**
     * 解码构造：装回五张表，并把华容道的定长数组补齐到固定档数。
     *
     * <p>
     * 空表在存盘格式里写成 null（载荷为空集合的坑：{@code ValueCodec.map} 把空 Map 编成 null），
     * 解码回来就是 {@code null}，按空表处理；否则只要任何一张表为空，整份存档都读不出来。
     */
    private static GameMilestones of(@Nullable O2OOpenCacheHashMap<UUID, SlotStat> slots,
                                     @Nullable O2OOpenCacheHashMap<UUID, ScoreStat> justGet10,
                                     @Nullable O2OOpenCacheHashMap<UUID, ScoreStat> game2048,
                                     @Nullable O2OOpenCacheHashMap<UUID, PuzzleStat> puzzles,
                                     @Nullable O2OOpenCacheHashMap<UUID, NumberPuzzleStat> numberPuzzles) {
        var data = new GameMilestones();
        if (slots != null) data.slots.putAll(slots);
        if (justGet10 != null) data.justGet10.putAll(justGet10);
        if (game2048 != null) data.game2048.putAll(game2048);
        if (puzzles != null) data.puzzles.putAll(puzzles);
        if (numberPuzzles != null) {
            data.numberPuzzles.putAll(numberPuzzles);
            data.numberPuzzles.replaceAll((player, stat) -> normalizeNumberPuzzle(stat));
        }
        return data;
    }

    // ==================== 读档 / 取实例 ====================

    /** 读档：{@code data} 是整份载荷，{@code data_version} 交回 codec。 */
    public static GameMilestones fromNBT(CompoundTag tag) {
        var ops = JavaValueOps.create(tag.getInt(NBT_VERSION));
        return DATA_CODEC.decode(ops, ops.fromBytes(tag.getByteArray(NBT_DATA)));
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

    /** 数字华容道完成一局：该尺寸取更短用时，并记住达成时间。 */
    public void recordNumberPuzzle(@Nullable UUID player, int sizeOrdinal, long ticks, long gameTime) {
        if (player == null || sizeOrdinal < 0 || sizeOrdinal >= FIXED_SLOTS || ticks <= 0L) return;
        var old = numberPuzzles.get(player);
        long[] bestTicks = old == null ? new long[FIXED_SLOTS] : old.ticks().clone();
        long[] bestTimes = old == null ? new long[FIXED_SLOTS] : old.times().clone();
        if (bestTicks[sizeOrdinal] > 0L && ticks >= bestTicks[sizeOrdinal]) return;
        bestTicks[sizeOrdinal] = ticks;
        bestTimes[sizeOrdinal] = gameTime;
        numberPuzzles.put(player, new NumberPuzzleStat(player, bestTicks, bestTimes));
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

    public List<NumberPuzzleStat> numberPuzzleBoard() {
        var board = new ObjectArrayList<NumberPuzzleStat>(numberPuzzles.size());
        board.addAll(numberPuzzles.values());
        board.sort(NUMBER_PUZZLE_ORDER);
        return board;
    }

    /** 单个尺寸的排行榜：只收该尺寸有成绩的玩家，按用时升序。 */
    public List<NumberPuzzleStat> numberPuzzleBoard(int sizeOrdinal) {
        int index = Math.clamp(sizeOrdinal, 0, FIXED_SLOTS - 1);
        var board = new ObjectArrayList<NumberPuzzleStat>();
        for (var stat : numberPuzzles.values()) {
            if (stat.ticks()[index] > 0L) board.add(stat);
        }
        board.sort(Comparator.comparingLong((NumberPuzzleStat stat) -> stat.ticks()[index])
                .thenComparingLong(stat -> stat.times()[index])
                .thenComparing(stat -> stat.player().toString()));
        return board;
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

    /** 存盘：整份数据编成一个 carrier，再写成 {@code data} 字节数组；版本号与载荷分开写。 */
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        var ops = JavaValueOps.create(DATA_VERSION);
        tag.putByteArray(NBT_DATA, ops.toBytes(DATA_CODEC.encode(ops, this)));
        tag.putInt(NBT_VERSION, DATA_VERSION);
        return tag;
    }

    // ==================== 内部工具 ====================

    /** 解码后的华容道记录补齐到固定档数：载荷可能比当前版本短。 */
    private static NumberPuzzleStat normalizeNumberPuzzle(NumberPuzzleStat stat) {
        long[] ticks = normalizeTimes(stat.ticks(), FIXED_SLOTS);
        long[] times = normalizeTimes(stat.times(), FIXED_SLOTS);
        return ticks == stat.ticks() && times == stat.times() ? stat : new NumberPuzzleStat(stat.player(), ticks, times);
    }

    private static long[] normalizeTimes(long[] values, int size) {
        if (values.length == size) return values;
        long[] normalized = new long[size];
        System.arraycopy(values, 0, normalized, 0, Math.min(values.length, normalized.length));
        return normalized;
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

    private static int numberPuzzleCompleted(NumberPuzzleStat stat) {
        int count = 0;
        for (long ticks : stat.ticks()) {
            if (ticks > 0L) count++;
        }
        return count;
    }

    private static long numberPuzzleTotal(NumberPuzzleStat stat) {
        long total = 0L;
        for (long ticks : stat.ticks()) {
            if (ticks > 0L) total += ticks;
        }
        return total;
    }

    private static long addSaturated(long base, long add) {
        if (add <= 0) return base;
        return base > Long.MAX_VALUE - add ? Long.MAX_VALUE : base + add;
    }
}

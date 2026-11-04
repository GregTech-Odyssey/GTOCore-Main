package com.gtocore.common.machine.noenergy.game.milestone;

import com.gtocore.common.machine.noenergy.game.sudoku.SudokuRules;
import com.gtocore.common.machine.noenergy.game.sudoku.SudokuVariant;

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
 * 数独独立里程碑数据：每个存档一份（`world/data/gtocore_sudoku_milestones.dat`）。
 *
 * <p>
 * 记录键是「难度 × 变体」二元组，各组合互不干扰。存盘由 DataSyncLib 承担：{@link CompoundTag} 只是外层容器，
 * 内容是 {@link #DATA_CODEC} 编出的 carrier，经 {@link JavaValueOps#toBytes(Object)} 写成单字节数组。
 */
public final class SudokuMilestones extends SavedData {

    private static final String DATA_NAME = "gtocore_sudoku_milestones";

    /** 载荷所在键：整份数据编码后的字节数组。 */
    private static final String NBT_DATA = "data";
    /** 载荷的格式版本，读档时交给 {@link JavaValueOps#create(int)}，codec 里用 {@code ops.dataVersion()} 取。 */
    private static final String NBT_VERSION = "data_version";
    private static final int DATA_VERSION = 1;

    /** 难度档数（{@link SudokuRules.Difficulty.ALL}）；写盘时会校验越界。 */
    private static final int DIFFICULTY_COUNT = SudokuRules.Difficulty.ALL.length;
    /** 变体数（{@link SudokuVariant.ALL}）。 */
    private static final int VARIANT_COUNT = SudokuVariant.ALL.length;

    /**
     * 记录键：难度序数 × 变体序数，两维一起参与 {@code equals} 与 {@code hashCode}。
     */
    public record Key(int difficulty, int variant) {

        /**
         * 两个维度是否都在当前版本范围内。解码时不裁剪：越界键整条丢弃（见 {@link #of}），
         * 否则损坏的载荷会被夹到边界组合上，覆盖真实最高分。
         */
        boolean isValid() {
            return difficulty >= 0 && difficulty < DIFFICULTY_COUNT && variant >= 0 && variant < VARIANT_COUNT;
        }
    }

    /** 一条成绩：最快用时（tick，必然 > 0）与达成时刻。 */
    public record Record(UUID player, Key key, long ticks, long completedGameTime) {}

    /** 榜单排序：用时升序 → 达成早的在前 → 玩家 UUID（保证稳定）。 */
    private static final Comparator<Record> ORDER = Comparator
            .comparingLong(Record::ticks)
            .thenComparingLong(Record::completedGameTime)
            .thenComparing(record -> record.player().toString());

    // ==================== 编解码 ====================

    private static final DataSyncCodec<Key> KEY_CODEC = CombinedCodec.composite(
            DataSyncCodec.INT_CODEC, Key::difficulty,
            DataSyncCodec.INT_CODEC, Key::variant,
            Key::new);

    private static final DataSyncCodec<Record> RECORD_CODEC = CombinedCodec.composite(
            DataSyncCodec.UUID_CODEC, Record::player,
            KEY_CODEC, Record::key,
            DataSyncCodec.LONG_CODEC, Record::ticks,
            DataSyncCodec.LONG_CODEC, Record::completedGameTime,
            Record::new);

    private static final DataSyncCodec<O2OOpenCacheHashMap<Key, Record>> RECORDS_CODEC = CombinedCodec.map(size -> new O2OOpenCacheHashMap<>(), KEY_CODEC, RECORD_CODEC);

    /** 整份数据：一张「组合 → 记录」表；解码侧 {@link #of} 装回去。 */
    private static final DataSyncCodec<SudokuMilestones> DATA_CODEC = CombinedCodec.composite(
            RECORDS_CODEC, SudokuMilestones::records,
            SudokuMilestones::of);

    // ==================== 数据 ====================

    private final O2OOpenCacheHashMap<Key, Record> records = new O2OOpenCacheHashMap<>();

    /** 写一次加一：界面靠它判断里程碑文字要不要重建（不存盘）。 */
    private int modCount;

    private SudokuMilestones() {}

    private O2OOpenCacheHashMap<Key, Record> records() {
        return records;
    }

    /**
     * 解码构造：只装回有效记录，键任一维越界的整条丢弃（旧版本档位更少，或存档损坏）。
     *
     * <p>
     * 空表在存盘格式里写成 null（空 Map 编成 null），解码回来就是 {@code null}，按空表处理。
     */
    private static SudokuMilestones of(@Nullable O2OOpenCacheHashMap<Key, Record> records) {
        var data = new SudokuMilestones();
        if (records == null) return data;
        for (var entry : records.entrySet()) {
            if (!entry.getKey().isValid()) continue;
            data.records.put(entry.getKey(), entry.getValue());
        }
        return data;
    }

    // ==================== 读档 / 取实例 ====================

    /** 读档：{@code data} 是整份载荷，{@code data_version} 交回 codec。 */
    public static SudokuMilestones fromNBT(CompoundTag tag) {
        var ops = JavaValueOps.create(tag.getInt(NBT_VERSION));
        return DATA_CODEC.decode(ops, ops.fromBytes(tag.getByteArray(NBT_DATA)));
    }

    /** 主世界的数据存储里拿这一份，保证全存档唯一。 */
    public static SudokuMilestones get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) throw new IllegalStateException("Unable to access the Overworld.");
        return overworld.getDataStorage()
                .computeIfAbsent(SudokuMilestones::fromNBT, SudokuMilestones::new, DATA_NAME);
    }

    public static SudokuMilestones get(MinecraftServer server) {
        return get(server.overworld());
    }

    // ==================== 写入 ====================

    /**
     * 记录一次完成：同一「难度 × 变体」组合下，只保留更短的用时。
     *
     * @return 是否真的更新了记录（用时不够短、或参数非法时为 {@code false}）
     */
    public boolean record(@Nullable UUID player, int difficulty, int variant, long ticks, long completedGameTime) {
        if (player == null || ticks <= 0L) return false;
        if (difficulty < 0 || difficulty >= DIFFICULTY_COUNT) return false;
        if (variant < 0 || variant >= VARIANT_COUNT) return false;
        var key = new Key(difficulty, variant);
        var old = records.get(key);
        if (old != null && old.ticks() <= ticks) return false;
        records.put(key, new Record(player, key, ticks, completedGameTime));
        touch();
        return true;
    }

    /** 某组合的最快记录；没有则 {@code null}。 */
    public @Nullable Record record(int difficulty, int variant) {
        return records.get(new Key(difficulty, variant));
    }

    /** 当前数据版本：变了说明有记录更新，界面该重建文字。 */
    public int modCount() {
        return modCount;
    }

    private void touch() {
        modCount++;
        setDirty();
    }

    // ==================== 读取 ====================

    /** 某个「难度 × 变体」组合的最快记录（0 或 1 条）。 */
    public List<Record> board(int difficulty, int variant) {
        var key = new Key(difficulty, variant);
        var board = new ObjectArrayList<Record>();
        var record = records.get(key);
        if (record != null) board.add(record);
        board.sort(ORDER);
        return board;
    }

    /** 该难度下各变体的记录汇总（每个变体最多一条）。 */
    public List<Record> boardByDifficulty(int difficulty) {
        int clamped = Math.clamp(difficulty, 0, DIFFICULTY_COUNT - 1);
        var board = new ObjectArrayList<Record>();
        for (var record : records.values()) {
            if (record.key().difficulty() == clamped) board.add(record);
        }
        board.sort(ORDER);
        return board;
    }

    /** 该变体下各难度的记录汇总（每个难度最多一条）。 */
    public List<Record> boardByVariant(int variant) {
        int clamped = Math.clamp(variant, 0, VARIANT_COUNT - 1);
        var board = new ObjectArrayList<Record>();
        for (var record : records.values()) {
            if (record.key().variant() == clamped) board.add(record);
        }
        board.sort(ORDER);
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

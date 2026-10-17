package com.gtocore.common.machine.noenergy.minesweeper;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.CoinSeries;
import com.gtocore.data.transaction.data.TradeLang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import org.jetbrains.annotations.Nullable;

/**
 * 扫雷核心方块的方块实体：难度、雷盘、显示盘与「这一面盘结过账没有」都存这里，规则由 {@link MinesweeperRules} 算。
 *
 * <p>
 * 盘面在核心方块的 +X/+Z 方向铺开（第 0 行贴着方块），一格一个 {@link MinesweeperDisplayBlock}；显示方块只管渲染与
 * 转发点击，读盘时按方块状态校准显示盘，所以拆掉显示方块不会把逻辑搞乱。
 */
public class MinesweeperCoreBlockEntity extends BlockEntity {

    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_MINES = "mines";
    private static final String KEY_CELLS = "cells";
    private static final String KEY_SETTLED = "settled";

    /** 「还没开局」用空数组而不是 null。 */
    private static final boolean[] NO_MINES = new boolean[0];
    private static final int[] NO_CELLS = new int[0];

    /** 难度（{@link MinesweeperRules.Difficulty} 的序数）。 */
    private int difficulty = MinesweeperRules.Difficulty.EASY.ordinal();
    /** 雷盘，按行优先存 {@code width * height} 格；第一次翻开之前全是 false。 */
    private boolean[] mines = NO_MINES;
    /** 显示盘，取值见 {@link MinesweeperRules}。 */
    private int[] cells = NO_CELLS;
    /** 这一面盘是否已经结过账，一面盘只兑现一次。 */
    private boolean settled;

    /** 按当前难度建的规则实例；换难度时重建（临时容器跟着盘面尺寸分配）。 */
    private MinesweeperRules rules = MinesweeperRules.create(MinesweeperRules.Difficulty.EASY);

    public MinesweeperCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ==================== 局面 ====================

    public MinesweeperRules.Difficulty difficulty() {
        return rules.difficulty();
    }

    /** 盘面是否已经铺开（方块状态里也有一份，读档时以方块状态为准）。 */
    public boolean isUnfolded() {
        return getBlockState().hasProperty(MinesweeperCoreBlock.UNFOLDED) &&
                getBlockState().getValue(MinesweeperCoreBlock.UNFOLDED);
    }

    /** 这一面盘现在能兑现多少币。 */
    public long payout() {
        return MinesweeperRules.payout(rules.difficulty().maxReward(), rules.foundMines(mines, cells), rules.mineCount(),
                rules.isWon(mines, cells));
    }

    // ==================== 核心交互 ====================

    /** 铺开盘面：先把空地检查一遍，再一格一个显示方块铺出去。返回是否铺成功。 */
    public boolean unfold(ServerPlayer player) {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel) || isUnfolded()) return false;
        BlockPos core = getBlockPos();
        var preset = rules.difficulty();
        for (int x = 0; x <= preset.width() + 1; x++) {
            for (int z = 0; z <= preset.height() + 1; z++) {
                BlockPos pos = gridPos(x, z);
                if (pos.equals(core) || serverLevel.isEmptyBlock(pos)) continue;
                MinesweeperCoreBlock.send(player, net.minecraft.network.chat.Component.translatable(MinesweeperCoreBlock.MSG_OBSTRUCTED));
                return false;
            }
        }
        newBoard();
        for (int z = 0; z < preset.height(); z++) {
            for (int x = 0; x < preset.width(); x++) {
                BlockPos pos = cellPos(x, z);
                serverLevel.setBlock(pos, MinesweeperDisplayBlock.displayState(0), Block.UPDATE_ALL);
                if (serverLevel.getBlockEntity(pos) instanceof MinesweeperDisplayBlockEntity display) {
                    display.setCore(core);
                }
            }
        }
        // 外圈围一圈边框，右下角放控制方块（重制 / 最小化 / 关闭）
        BlockState frameState = GTOBlocks.MINESWEEPER_FRAME.get().defaultBlockState();
        for (int x = 0; x <= preset.width() + 1; x++) {
            for (int z = 0; z <= preset.height() + 1; z++) {
                if (x >= 1 && x <= preset.width() && z >= 1 && z <= preset.height()) continue;
                if (x == preset.width() + 1 && z == preset.height() + 1) continue;
                BlockPos framePos = gridPos(x, z);
                if (!framePos.equals(core)) serverLevel.setBlock(framePos, frameState, Block.UPDATE_ALL);
            }
        }
        BlockPos controlPos = gridPos(preset.width() + 1, preset.height() + 1);
        serverLevel.setBlock(controlPos, GTOBlocks.MINESWEEPER_CONTROL.get().defaultBlockState(), Block.UPDATE_ALL);
        if (serverLevel.getBlockEntity(controlPos) instanceof MinesweeperControlBlockEntity control) {
            control.setCore(core);
        }
        setUnfolded(true);
        MinesweeperCoreBlock.send(player, net.minecraft.network.chat.Component.translatable(MinesweeperCoreBlock.MSG_UNFOLDED));
        save();
        return true;
    }

    /** 重开一局：先按当前进度兑现，再把显示盘清成未翻开。 */
    public void restart(ServerPlayer player) {
        if (!isUnfolded() || !(getLevel() instanceof ServerLevel)) return;
        settle(player);
        newBoard();
        mirrorAll();
        save();
        MinesweeperCoreBlock.send(player, net.minecraft.network.chat.Component.translatable(MinesweeperCoreBlock.MSG_RESTARTED));
    }

    /** 收掉盘面（显示方块全拆掉），留着核心方块本身。 */
    public void cleanup() {
        if (!(getLevel() instanceof ServerLevel serverLevel) || mines.length == 0) return;
        BlockPos core = getBlockPos();
        int width = rules.difficulty().width();
        int height = rules.difficulty().height();
        for (int x = 0; x <= width + 1; x++) {
            for (int z = 0; z <= height + 1; z++) {
                BlockPos pos = gridPos(x, z);
                Block block = serverLevel.getBlockState(pos).getBlock();
                if (block instanceof MinesweeperDisplayBlock || block instanceof MinesweeperFrameBlock ||
                        block instanceof MinesweeperControlBlock) {
                    serverLevel.removeBlock(pos, false);
                }
            }
        }
        setUnfolded(false);
    }

    /** 只收盘面（参考的 minimize）：显示方块、边框、控制方块全收掉，核心方块留着。 */
    public void minimize(@Nullable ServerPlayer player) {
        cleanup();
        MinesweeperCoreBlock.send(player, Component.translatable(MinesweeperCoreBlock.MSG_MINIMIZED));
    }

    /** 关闭（参考的 close）：收掉盘面，再把核心方块本身掉出来。 */
    public void close(@Nullable ServerPlayer player) {
        Level level = getLevel();
        BlockPos core = getBlockPos();
        cleanup();
        if (level instanceof ServerLevel serverLevel) {
            Block.popResource(serverLevel, core, new ItemStack(GTOBlocks.MINESWEEPER_CORE.get()));
            serverLevel.removeBlock(core, false);
        }
        MinesweeperCoreBlock.send(player, Component.translatable(MinesweeperCoreBlock.MSG_CLOSED));
    }

    /** 换难度：只有在还没铺盘时能换（铺开了先收摊）。 */
    public void switchDifficulty(boolean forward, ServerPlayer player) {
        if (isUnfolded()) return;
        var preset = rules.difficulty();
        var all = MinesweeperRules.Difficulty.ALL;
        int next = forward ? (preset.ordinal() + 1) % all.length : (preset.ordinal() + all.length - 1) % all.length;
        setDifficulty(all[next]);
        MinesweeperCoreBlock.send(player, MinesweeperCoreBlock.difficultyMessage(all[next]));
    }

    public void setDifficulty(MinesweeperRules.Difficulty preset) {
        difficulty = preset.ordinal();
        applyDifficultyProperty();
        rules = MinesweeperRules.create(preset);
        mines = NO_MINES;
        cells = NO_CELLS;
        settled = false;
        save();
    }

    // ==================== 显示方块点进来的落子 ====================

    /** 显示方块被点：右击翻开、潜行右击插旗；数字格周围旗数对上了就连开一圈。 */
    public void click(BlockPos displayPos, ServerPlayer player, boolean flag) {
        if (!(getLevel() instanceof ServerLevel) || !isUnfolded() || settled) return;
        int cell = cellAt(displayPos);
        if (cell < 0) return;
        boolean changed;
        if (flag) {
            changed = rules.toggleFlag(cells, cell);
        } else if (cells[cell] >= MinesweeperRules.NUMBER_MIN) {
            changed = chord(cell);
        } else {
            changed = rules.open(mines, cells, cell, random());
        }
        if (!changed) return;
        mirrorAll();
        if (rules.isOver(mines, cells)) settle(player);
        save();
    }

    /** 数字格：周围旗数与数字相等时，把周围还扣着的格子一次翻开（可能踩雷，也可能直接通关）。 */
    private boolean chord(int cell) {
        int flagsAround = 0;
        for (int direction = 0; direction < MinesweeperRules.DIRECTIONS; direction++) {
            int next = rules.neighbour(cell, direction);
            if (next >= 0 && cells[next] == MinesweeperRules.FLAGGED) flagsAround++;
        }
        if (flagsAround != cells[cell]) return false;
        boolean changed = false;
        for (int direction = 0; direction < MinesweeperRules.DIRECTIONS; direction++) {
            int next = rules.neighbour(cell, direction);
            if (next < 0 || cells[next] != MinesweeperRules.UNOPENED) continue;
            changed |= rules.open(mines, cells, next, random());
            if (rules.isLost(cells)) break;
        }
        return changed;
    }

    /** 结算：按找到的雷把币直接给玩家（同一面盘只给一次），然后放个响。 */
    public void settle(@Nullable ServerPlayer player) {
        if (settled) return;
        settled = true;
        boolean won = rules.isWon(mines, cells);
        long amount = payout();
        if (amount > 0 && player != null) pay(player, amount);
        if (!won) explode();
        if (player != null) {
            MinesweeperCoreBlock.send(player, MinesweeperCoreBlock.resultMessage(won, amount));
        }
        save();
    }

    /** 踩雷的动静：不破坏方块，只是炸一下。 */
    private void explode() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        for (int cell = 0; cell < cells.length; cell++) {
            if (cells[cell] != MinesweeperRules.DEATH_BOMB) continue;
            BlockPos pos = displayPos(cell);
            serverLevel.explode(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 3.0F,
                    Level.ExplosionInteraction.NONE);
            return;
        }
    }

    /** 币直接进背包，塞不下的按面额掉在核心方块上。 */
    private void pay(ServerPlayer player, long amount) {
        var inventory = player.getInventory();
        var destination = new PlayerMainInvWrapper(inventory);
        CoinSeries coins = CoinExchange.gameCoins();
        long remaining = amount;
        for (int tier = coins.typeCount() - 1; tier >= 0 && remaining > 0; tier--) {
            long value = coins.value(tier);
            if (remaining < value) continue;
            ItemStack stack = coins.itemStack(tier);
            if (stack.isEmpty()) continue;
            int count = (int) Math.min(remaining / value, (long) inventory.items.size() * stack.getMaxStackSize());
            if (count == 0) continue;
            stack.setCount(count);
            remaining -= (count - ItemHandlerHelper.insertItemStacked(destination, stack, false).getCount()) * value;
        }
        if (remaining > 0 && getLevel() != null) {
            CoinExchange.dropToWorld(getLevel(), getBlockPos().above(), TradeLang.GAME_COINS, remaining);
        }
    }

    // ==================== 内部 ====================

    /** 铺一片新的显示盘与空雷盘。 */
    private void newBoard() {
        mines = rules.newMines();
        cells = rules.newCells();
        settled = false;
    }

    private RandomSource random() {
        Level level = getLevel();
        return level == null ? RandomSource.create() : level.getRandom();
    }

    /** 棋盘网格坐标（{@code gx} 向 +X、{@code gz} 向 -Z）对应的世界坐标；核心方块自己就是 (0, 0)。 */
    private BlockPos gridPos(int gx, int gz) {
        return getBlockPos().offset(gx, 0, -gz);
    }

    /** 第 {@code (x, z)} 格（0 起，棋盘在第 1 圈边框之内）的世界坐标。 */
    private BlockPos cellPos(int x, int z) {
        return gridPos(x + 1, z + 1);
    }

    private BlockPos displayPos(int cell) {
        int width = rules.difficulty().width();
        return cellPos(cell % width, cell / width);
    }

    /** 显示方块的位置 → 格子下标（{@code z} 向 -Z 数）；不在棋盘范围内返回 -1。 */
    public int cellAt(BlockPos displayPos) {
        var preset = rules.difficulty();
        int x = displayPos.getX() - getBlockPos().getX() - 1;
        int z = getBlockPos().getZ() - displayPos.getZ() - 1;
        if (x < 0 || x >= preset.width() || z < 0 || z >= preset.height()) return -1;
        return z * preset.width() + x;
    }

    /** 把难度写进方块状态，正面那张图跟着换（照参考渲染器按难度换正面）。 */
    private void applyDifficultyProperty() {
        Level level = getLevel();
        BlockState state = getBlockState();
        if (level != null && state.hasProperty(MinesweeperCoreBlock.DIFFICULTY)) {
            level.setBlock(getBlockPos(), state.setValue(MinesweeperCoreBlock.DIFFICULTY, difficulty), Block.UPDATE_ALL);
        }
    }

    private void setUnfolded(boolean unfolded) {
        Level level = getLevel();
        BlockState state = getBlockState();
        if (level != null && state.hasProperty(MinesweeperCoreBlock.UNFOLDED)) {
            level.setBlock(getBlockPos(), state.setValue(MinesweeperCoreBlock.UNFOLDED, unfolded), Block.UPDATE_ALL);
        }
    }

    /** 把显示盘同步到显示方块的方块状态上（只改不一样的那些）。 */
    private void mirrorAll() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        for (int cell = 0; cell < cells.length; cell++) {
            mirror(serverLevel, cell);
        }
    }

    private void mirror(ServerLevel level, int cell) {
        BlockPos pos = displayPos(cell);
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MinesweeperDisplayBlock)) return;
        int wanted = MinesweeperDisplayBlock.propertyFor(cells[cell]);
        if (state.getValue(MinesweeperDisplayBlock.STATE) != wanted) {
            level.setBlock(pos, state.setValue(MinesweeperDisplayBlock.STATE, wanted), Block.UPDATE_ALL);
        }
    }

    private void save() {
        setChanged();
        Level level = getLevel();
        if (level != null) level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    // ==================== 存取 ====================

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(KEY_DIFFICULTY, difficulty);
        tag.putBoolean(KEY_SETTLED, settled);
        if (mines.length > 0) tag.putByteArray(KEY_MINES, packMines());
        if (cells.length > 0) tag.putIntArray(KEY_CELLS, cells);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        var preset = MinesweeperRules.Difficulty.of(tag.getInt(KEY_DIFFICULTY));
        rules = MinesweeperRules.create(preset == null ? MinesweeperRules.Difficulty.EASY : preset);
        difficulty = rules.difficulty().ordinal();
        settled = tag.getBoolean(KEY_SETTLED);
        int cellCount = rules.cellCount();
        mines = tag.contains(KEY_MINES) ? unpackMines(tag.getByteArray(KEY_MINES), cellCount) : NO_MINES;
        cells = tag.contains(KEY_CELLS) && tag.getIntArray(KEY_CELLS).length == cellCount ? tag.getIntArray(KEY_CELLS) : NO_CELLS;
        applyDifficultyProperty();
        if (mines.length != cellCount || cells.length != cellCount) {
            // 存档对不上（换过难度、坏档）：这一局作废，等重新铺盘
            mines = NO_MINES;
            cells = NO_CELLS;
            settled = false;
        }
    }

    /** 雷盘按位存进 byte 数组。 */
    private byte[] packMines() {
        byte[] packed = new byte[(mines.length + 7) / 8];
        for (int cell = 0; cell < mines.length; cell++) {
            if (mines[cell]) packed[cell >> 3] |= (byte) (1 << (cell & 7));
        }
        return packed;
    }

    private static boolean[] unpackMines(byte[] packed, int cellCount) {
        boolean[] unpacked = new boolean[cellCount];
        for (int cell = 0; cell < cellCount && (cell >> 3) < packed.length; cell++) {
            unpacked[cell] = (packed[cell >> 3] & 1 << (cell & 7)) != 0;
        }
        return unpacked;
    }

    /** 拆掉核心方块前先把盘面收干净，免得留一地显示方块。 */
}

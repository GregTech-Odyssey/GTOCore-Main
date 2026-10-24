package com.gtocore.common.machine.noenergy.game.minesweeper;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 扫雷棋盘生成器：固定种子布雷 + L1~L3 无猜求解。
 *
 * <p>
 * 基础种子由世界种子、核心方块位置和本局随机盐混合，再派生候选种子。
 * 满足尺寸/密度限制时并行尝试 {@link #NO_GUESS_CANDIDATES} 个候选，取最快可解盘；否则单种子。
 * 所有路径都保证首点 3×3 无雷；任务只用本地数组、不访问 Level，可在任意线程调用。
 * 候选种子可复现，读档时据此重算。
 */
public final class MinesweeperBoardGenerator {

    /** 无猜尝试的候选数。 */
    private static final int NO_GUESS_CANDIDATES = 10;
    /** 超过这个格数不再尝试无猜。 */
    private static final int NO_GUESS_MAX_CELLS = 900;
    /** 超过这个雷密度不再尝试无猜。 */
    private static final double NO_GUESS_MAX_DENSITY = 0.22D;
    /** 一次生成最多等待的时间；超时用最快已完成候选/单种子兜底。 */
    private static final long NO_GUESS_BUDGET_MILLIS = 1000L;

    private static final int[] NEIGHBOR_COL = { -1, 0, 1, -1, 1, -1, 0, 1 };
    private static final int[] NEIGHBOR_ROW = { -1, -1, -1, 0, 0, 1, 1, 1 };

    /**
     * 生成结果。
     *
     * @param seed    选中的种子；写进机器后读档可重算
     * @param mines   雷盘，长度正好是 width * height
     * @param noGuess 是否通过了无猜校验
     */
    public record GeneratedBoard(long seed, boolean[] mines, boolean noGuess) {}

    private record Candidate(long seed, boolean[] mines, boolean noGuess, long elapsedNanos) {}

    private MinesweeperBoardGenerator() {}

    /**
     * 生成一张盘。基础种子由世界种子、核心方块位置和本局随机盐混合；条件满足时并行尝试
     * {@link #NO_GUESS_CANDIDATES} 个候选种子，否则单种子。
     *
     * @param random       提供本局随机盐；生成任务本身不接触 Level
     * @param worldSeed    当前存档的世界种子
     * @param corePosition 核心方块的 {@link net.minecraft.core.BlockPos#asLong()} 坐标
     * @param firstCell    首点（0..width*height-1）
     */
    public static GeneratedBoard generate(RandomSource random, long worldSeed, long corePosition,
                                          int width, int height, int mineCount, int firstCell) {
        int cellCount = width * height;
        if (width <= 0 || height <= 0 || cellCount <= 0) {
            throw new IllegalArgumentException("Invalid board size");
        }
        int safeCell = Math.clamp(firstCell, 0, cellCount - 1);
        int clampedMines = Math.clamp(mineCount, 0, Math.max(0, cellCount - 9));
        SplittableRandom boardRandom = new SplittableRandom(
                mixSeed(worldSeed, corePosition, random.nextLong()));
        boolean tryNoGuess = cellCount <= NO_GUESS_MAX_CELLS &&
                clampedMines / (double) cellCount <= NO_GUESS_MAX_DENSITY;
        if (!tryNoGuess) {
            long seed = boardRandom.nextLong();
            return new GeneratedBoard(seed, layout(width, height, clampedMines, seed, safeCell), false);
        }

        long[] seeds = new long[NO_GUESS_CANDIDATES];
        for (int index = 0; index < seeds.length; index++) {
            seeds[index] = boardRandom.nextLong();
        }
        List<CompletableFuture<Candidate>> futures = new ArrayList<>(seeds.length);
        for (long seed : seeds) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> candidate(width, height, clampedMines, seed, safeCell)));
        }

        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(NO_GUESS_BUDGET_MILLIS);
        Candidate bestNoGuess = null;
        Candidate fastest = null;
        for (CompletableFuture<Candidate> future : futures) {
            try {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0L) {
                    future.cancel(true);
                    continue;
                }
                Candidate candidate = future.get(remaining, TimeUnit.NANOSECONDS);
                if (candidate == null) continue;
                if (fastest == null || candidate.elapsedNanos < fastest.elapsedNanos) fastest = candidate;
                if (candidate.noGuess &&
                        (bestNoGuess == null || candidate.elapsedNanos < bestNoGuess.elapsedNanos)) {
                    bestNoGuess = candidate;
                }
            } catch (TimeoutException timeout) {
                future.cancel(true);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException failure) {
                // 单个候选失败不影响其它候选；全部失败时走单种子兜底。
            }
        }
        if (bestNoGuess != null) {
            return new GeneratedBoard(bestNoGuess.seed, bestNoGuess.mines, true);
        }
        if (fastest != null) {
            return new GeneratedBoard(fastest.seed, fastest.mines, false);
        }
        long seed = boardRandom.nextLong();
        return new GeneratedBoard(seed, layout(width, height, clampedMines, seed, safeCell), false);
    }

    /** 混合世界种子、核心位置和本局随机盐；使用 64 位 avalanche 终混，避免相近坐标产生相近种子。 */
    public static long mixSeed(long worldSeed, long corePosition, long salt) {
        long value = worldSeed ^ corePosition * 0x9E3779B97F4A7C15L ^ salt;
        value ^= value >>> 33;
        value *= 0xFF51AFD7ED558CCDL;
        value ^= value >>> 33;
        value *= 0xC4CEB9FE1A85EC53L;
        value ^= value >>> 33;
        return value;
    }

    /**
     * 固定算法布雷：排除首点 3×3，再对剩余格做部分洗牌取前 mineCount 个。
     * 这是所有候选、读档重算和单种子兜底共用的唯一布雷路径。
     */
    public static boolean[] layout(int width, int height, int mineCount, long seed, int firstCell) {
        int cellCount = width * height;
        boolean[] mines = new boolean[cellCount];
        if (cellCount <= 0 || mineCount <= 0) return mines;
        int safeCell = Math.clamp(firstCell, 0, cellCount - 1);
        int safeCol = safeCell % width;
        int safeRow = safeCell / width;
        int[] positions = new int[cellCount];
        int available = 0;
        for (int cell = 0; cell < cellCount; cell++) {
            int col = cell % width;
            int row = cell / width;
            if (Math.abs(col - safeCol) <= 1 && Math.abs(row - safeRow) <= 1) continue;
            positions[available++] = cell;
        }
        int count = Math.min(mineCount, available);
        SplittableRandom random = new SplittableRandom(seed);
        for (int index = 0; index < count; index++) {
            int pick = index + random.nextInt(available - index);
            int cell = positions[pick];
            positions[pick] = positions[index];
            positions[index] = cell;
            mines[cell] = true;
        }
        return mines;
    }

    /** 单候选：布雷、要求首点为 0、跑无猜求解器，记录耗时。 */
    private static Candidate candidate(int width, int height, int mineCount, long seed, int firstCell) {
        long start = System.nanoTime();
        boolean[] mines = layout(width, height, mineCount, seed, firstCell);
        int[] numbers = numbers(mines, width, height);
        boolean noGuess = numbers[firstCell] == 0 && solve(mines, numbers, width, height, mineCount, firstCell);
        return new Candidate(seed, mines, noGuess, System.nanoTime() - start);
    }

    /** 供测试/读档校验使用：判断该种子+首点是否是无猜盘。 */
    public static boolean isNoGuess(int width, int height, int mineCount, long seed, int firstCell) {
        boolean[] mines = layout(width, height, mineCount, seed, firstCell);
        int[] numbers = numbers(mines, width, height);
        return numbers[firstCell] == 0 && solve(mines, numbers, width, height, mineCount, firstCell);
    }

    /** 每格周围八向雷数；雷格为 -1。 */
    private static int[] numbers(boolean[] mines, int width, int height) {
        int[] numbers = new int[mines.length];
        for (int cell = 0; cell < mines.length; cell++) {
            if (mines[cell]) {
                numbers[cell] = -1;
                continue;
            }
            int count = 0;
            int col = cell % width;
            int row = cell / width;
            for (int direction = 0; direction < NEIGHBOR_COL.length; direction++) {
                int nextCol = col + NEIGHBOR_COL[direction];
                int nextRow = row + NEIGHBOR_ROW[direction];
                if (nextCol < 0 || nextCol >= width || nextRow < 0 || nextRow >= height) continue;
                if (mines[nextRow * width + nextCol]) count++;
            }
            numbers[cell] = count;
        }
        return numbers;
    }

    /** L1 基础规则 + L2 子集规则 + L3 全局雷数的确定性求解器；绝不猜测。 */
    private static boolean solve(boolean[] mines, int[] numbers, int width, int height, int mineCount, int firstCell) {
        Detective detective = new Detective(mines, numbers, width, height, mineCount);
        detective.open(firstCell);
        while (true) {
            boolean progress = detective.basic();
            if (detective.contradiction) return false;
            if (progress) continue;
            progress = detective.global();
            if (detective.contradiction) return false;
            if (progress) continue;
            progress = detective.subset();
            if (detective.contradiction) return false;
            if (progress) continue;
            break;
        }
        for (int cell = 0; cell < mines.length; cell++) {
            if (!mines[cell] && !detective.opened[cell]) return false;
        }
        return true;
    }

    private static final class Detective {

        private final boolean[] mines;
        private final int[] numbers;
        private final int width;
        private final int height;
        private final int mineCount;
        private final boolean[] opened;
        private final boolean[] knownMine;
        private final int[] queue;
        private boolean contradiction;

        private Detective(boolean[] mines, int[] numbers, int width, int height, int mineCount) {
            this.mines = mines;
            this.numbers = numbers;
            this.width = width;
            this.height = height;
            this.mineCount = mineCount;
            this.opened = new boolean[mines.length];
            this.knownMine = new boolean[mines.length];
            this.queue = new int[mines.length];
        }

        private void open(int start) {
            if (opened[start] || knownMine[start]) return;
            opened[start] = true;
            if (numbers[start] != 0) return;
            int top = 0;
            queue[top++] = start;
            while (top > 0) {
                int cell = queue[--top];
                int col = cell % width;
                int row = cell / width;
                for (int direction = 0; direction < NEIGHBOR_COL.length; direction++) {
                    int nextCol = col + NEIGHBOR_COL[direction];
                    int nextRow = row + NEIGHBOR_ROW[direction];
                    if (nextCol < 0 || nextCol >= width || nextRow < 0 || nextRow >= height) continue;
                    int next = nextRow * width + nextCol;
                    if (opened[next] || knownMine[next]) continue;
                    opened[next] = true;
                    if (numbers[next] == 0) queue[top++] = next;
                }
            }
        }

        private boolean basic() {
            boolean progress = false;
            for (int cell = 0; cell < mines.length; cell++) {
                if (!opened[cell] || numbers[cell] <= 0) continue;
                int flags = 0;
                int unknown = 0;
                int col = cell % width;
                int row = cell / width;
                for (int direction = 0; direction < NEIGHBOR_COL.length; direction++) {
                    int nextCol = col + NEIGHBOR_COL[direction];
                    int nextRow = row + NEIGHBOR_ROW[direction];
                    if (nextCol < 0 || nextCol >= width || nextRow < 0 || nextRow >= height) continue;
                    int next = nextRow * width + nextCol;
                    if (knownMine[next]) flags++;
                    else if (!opened[next]) unknown++;
                }
                int rest = numbers[cell] - flags;
                if (rest < 0 || rest > unknown) {
                    contradiction = true;
                    return false;
                }
                if (unknown == 0) continue;
                if (rest == 0) {
                    if (!openNeighbours(cell)) return false;
                    progress = true;
                } else if (rest == unknown) {
                    if (!markNeighbours(cell)) return false;
                    progress = true;
                }
            }
            return progress;
        }

        private boolean global() {
            int flags = 0;
            int unknown = 0;
            for (int cell = 0; cell < mines.length; cell++) {
                if (knownMine[cell]) flags++;
                else if (!opened[cell]) unknown++;
            }
            int remain = mineCount - flags;
            if (remain < 0 || remain > unknown) {
                contradiction = true;
                return false;
            }
            if (unknown == 0) return false;
            if (remain == 0) {
                for (int cell = 0; cell < mines.length; cell++) {
                    if (opened[cell] || knownMine[cell]) continue;
                    if (mines[cell]) {
                        contradiction = true;
                        return false;
                    }
                    open(cell);
                }
                return true;
            }
            if (remain == unknown) {
                for (int cell = 0; cell < mines.length; cell++) {
                    if (opened[cell] || knownMine[cell]) continue;
                    if (!mines[cell]) {
                        contradiction = true;
                        return false;
                    }
                    knownMine[cell] = true;
                }
                return true;
            }
            return false;
        }

        private boolean subset() {
            List<BitSet> constraints = new ArrayList<>();
            List<Integer> rests = new ArrayList<>();
            for (int cell = 0; cell < mines.length; cell++) {
                if (!opened[cell] || numbers[cell] <= 0) continue;
                BitSet unknown = new BitSet(mines.length);
                int flags = 0;
                int col = cell % width;
                int row = cell / width;
                for (int direction = 0; direction < NEIGHBOR_COL.length; direction++) {
                    int nextCol = col + NEIGHBOR_COL[direction];
                    int nextRow = row + NEIGHBOR_ROW[direction];
                    if (nextCol < 0 || nextCol >= width || nextRow < 0 || nextRow >= height) continue;
                    int next = nextRow * width + nextCol;
                    if (knownMine[next]) flags++;
                    else if (!opened[next]) unknown.set(next);
                }
                int rest = numbers[cell] - flags;
                if (unknown.cardinality() < 2) continue;
                constraints.add(unknown);
                rests.add(rest);
            }
            for (int a = 0; a < constraints.size(); a++) {
                BitSet first = constraints.get(a);
                int firstRest = rests.get(a);
                for (int b = 0; b < constraints.size(); b++) {
                    if (a == b) continue;
                    BitSet second = constraints.get(b);
                    if (!isSubset(first, second)) continue;
                    BitSet difference = (BitSet) second.clone();
                    difference.andNot(first);
                    if (difference.isEmpty()) continue;
                    int differenceRest = rests.get(b) - firstRest;
                    if (differenceRest < 0 || differenceRest > difference.cardinality()) {
                        contradiction = true;
                        return false;
                    }
                    if (differenceRest == 0) {
                        for (int cell = difference.nextSetBit(0); cell >= 0; cell = difference.nextSetBit(cell + 1)) {
                            if (mines[cell]) {
                                contradiction = true;
                                return false;
                            }
                            open(cell);
                        }
                        return true;
                    }
                    if (differenceRest == difference.cardinality()) {
                        for (int cell = difference.nextSetBit(0); cell >= 0; cell = difference.nextSetBit(cell + 1)) {
                            if (!mines[cell]) {
                                contradiction = true;
                                return false;
                            }
                            knownMine[cell] = true;
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean openNeighbours(int cell) {
            int col = cell % width;
            int row = cell / width;
            for (int direction = 0; direction < NEIGHBOR_COL.length; direction++) {
                int nextCol = col + NEIGHBOR_COL[direction];
                int nextRow = row + NEIGHBOR_ROW[direction];
                if (nextCol < 0 || nextCol >= width || nextRow < 0 || nextRow >= height) continue;
                int next = nextRow * width + nextCol;
                if (opened[next] || knownMine[next]) continue;
                if (mines[next]) {
                    contradiction = true;
                    return false;
                }
                open(next);
            }
            return true;
        }

        private boolean markNeighbours(int cell) {
            int col = cell % width;
            int row = cell / width;
            for (int direction = 0; direction < NEIGHBOR_COL.length; direction++) {
                int nextCol = col + NEIGHBOR_COL[direction];
                int nextRow = row + NEIGHBOR_ROW[direction];
                if (nextCol < 0 || nextCol >= width || nextRow < 0 || nextRow >= height) continue;
                int next = nextRow * width + nextCol;
                if (opened[next] || knownMine[next]) continue;
                if (!mines[next]) {
                    contradiction = true;
                    return false;
                }
                knownMine[next] = true;
            }
            return true;
        }

        private static boolean isSubset(BitSet first, BitSet second) {
            BitSet copy = (BitSet) first.clone();
            copy.andNot(second);
            return copy.isEmpty();
        }
    }
}

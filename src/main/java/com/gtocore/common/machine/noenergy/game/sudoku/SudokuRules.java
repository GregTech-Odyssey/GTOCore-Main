package com.gtocore.common.machine.noenergy.game.sudoku;

import net.minecraft.util.RandomSource;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 数独的出题入口与难度定义：9×9 盘面、每格 1~9，约束由 {@link SudokuConstraints} 决定（标准局为行、列、3×3 宫）。
 *
 * <p>
 * 锯齿先构造 9 区域再填完整解，其余变体直接填解；随后从解反推连续标记与杀手笼（保证解合法），最后在
 * {@link #MIN_CLUES}~{@link #MAX_CLUES} 内取目标提示数逐格挖空，每挖一格都要求全部约束下解仍唯一。
 * 难度由 {@link SudokuSolver#rate} 的逻辑评级决定，提示数只在范围内微调。
 *
 * <p>
 * 杀手例外：笼和就是提示，盘面不放数字，也不做唯一解与逻辑评级筛选（零提示盘面搜索量无法承受），档位只决定笼的最大尺寸。
 */
public final class SudokuRules {

    public static final int SIZE = 9;
    public static final int BOX = 3;
    public static final int CELLS = SIZE * SIZE;
    public static final int VALUE_MIN = 1;
    public static final int VALUE_MAX = 9;
    /** 1~9 全在的低九位。 */
    public static final int ALL_VALUES = (1 << VALUE_MAX) - 1;
    /** 杀手笼和的合法上限：1~9 全用一遍的和。 */
    public static final int MAX_CAGE_TOTAL = VALUE_MAX * (VALUE_MAX + 1) / 2;
    /** 每局提示数的随机范围（含两端），与难度档位无关。 */
    public static final int MIN_CLUES = 24;
    public static final int MAX_CLUES = 28;
    /** 隐性唯一数不超过此值时评级为 0。 */
    static final int SINGLES_EASY_LIMIT = 8;

    /**
     * 六档难度：除杀手外由 {@link SudokuSolver#rate} 的逻辑评级筛出，提示数只在 {@link #MIN_CLUES}~{@link #MAX_CLUES}
     * 内微调、不参与分档；杀手没有提示数字，只用 {@link #cageMaxSize()} 控制难度。奖励按档位固定。
     *
     * <p>
     * 构造参数依次为 reward、targetRate（0~5）、attempts、cageMaxSize（杀手笼最大格数，最小笼固定 2 格）；
     * ratingLevel 为目标评级 + 1（1~6 级）。
     */
    public enum Difficulty {

        BEGINNER(2, 0, 32, 5),
        EASY(5, 1, 16, 6),
        NORMAL(9, 2, 48, 7),
        HARD(16, 3, 64, 8),
        EXPERT(25, 4, 48, 9),
        MASTER(40, 5, 48, 9);

        public static final Difficulty[] ALL = values();

        private final long reward;
        private final int targetRate;
        private final int attempts;
        private final int cageMaxSize;

        Difficulty(long reward, int targetRate, int attempts, int cageMaxSize) {
            this.reward = reward;
            this.targetRate = targetRate;
            this.attempts = attempts;
            this.cageMaxSize = cageMaxSize;
        }

        /** 完成一局发放的游戏币数。 */
        public long reward() {
            return reward;
        }

        /** 这一档要求的逻辑评级（0~5）。 */
        public int targetRate() {
            return targetRate;
        }

        /** 目标评级的 1~6 级表示。 */
        public int ratingLevel() {
            return targetRate + 1;
        }

        /** 生成一局最多尝试几次，用来命中逻辑评级。 */
        public int attempts() {
            return attempts;
        }

        /** 杀手笼的最大格数；最小笼固定 2 格。 */
        public int cageMaxSize() {
            return cageMaxSize;
        }

        int rateDistance(int rate) {
            return Math.abs(rate - targetRate);
        }

        /** 按序数取档位，越界返回 {@code null}（存档与网络来的值一律校验）。 */
        public static @Nullable Difficulty of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    private SudokuRules() {}

    /**
     * 出一局新题：返回提示盘与变体附加数据，0 表示空格。目标提示数取自 {@link #MIN_CLUES}~{@link #MAX_CLUES}，
     * 按唯一解逐格挖空后用逻辑评级筛选；没命中就朝更难/更简单方向微调提示数重试，最多 {@link Difficulty#attempts()} 次，
     * 仍未命中返回最接近的一局。杀手不挖提示：盘面全空，只靠笼和。
     */
    public static SudokuPuzzle createPuzzle(RandomSource random, Difficulty difficulty, SudokuVariant variant) {
        return createPuzzle(random, difficulty, variant.mask());
    }

    /**
     * 出一局新题：{@code variantMask} 是 {@link SudokuVariant#mask()} 的位或，可叠加多条规则（界面目前只传单条）。
     * 叠加杀手时盘面没有提示数字，难度只由该档的笼尺寸决定，不走逻辑评级筛选。
     */
    public static SudokuPuzzle createPuzzle(RandomSource random, Difficulty difficulty, int variantMask) {
        int mask = SudokuRuleSet.normalizeMask(variantMask);
        // 杀手用笼和代替提示数字：零提示盘面无法用唯一解计数或逻辑评级筛选（解搜索量指数级放大），
        // 直接按完整解切笼并返回空提示盘，判题只要求满足行/列/宫、标记与笼和。
        if ((mask & SudokuVariant.KILLER.mask()) != 0) {
            SudokuSeed seed = createSeed(random, difficulty, mask);
            return new SudokuPuzzle(new int[CELLS], seed.jigsawMap(), seed.cageOf(), seed.cageTotals(),
                    seed.marks());
        }
        int targetRate = difficulty.targetRate();
        int clueTarget = MIN_CLUES + random.nextInt(MAX_CLUES - MIN_CLUES + 1);
        SudokuSeed seed = createSeed(random, difficulty, mask);
        SudokuPuzzle best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int attempt = 0; attempt < difficulty.attempts(); attempt++) {
            // 同一份解可反复挖题；每 SEED_REFRESH 次换解，省去每轮重新生成与求解。
            if (attempt > 0 && attempt % SEED_REFRESH == 0) {
                seed = createSeed(random, difficulty, mask);
            }
            SudokuPuzzle puzzle = carve(random, seed, clueTarget);
            int rate = SudokuSolver.rate(puzzle.givens(), seed.constraints());
            int distance = Math.abs(rate - targetRate);
            if (distance < bestDistance) {
                best = puzzle;
                bestDistance = distance;
            }
            if (distance == 0) return puzzle;
            // 偏简单就减少提示、偏难就增加提示，提示数始终留在随机范围内
            if (rate < targetRate && clueTarget > MIN_CLUES) {
                clueTarget--;
            } else if (rate > targetRate && clueTarget < MAX_CLUES) {
                clueTarget++;
            }
        }
        return best == null ? carve(random, seed, clueTarget) : best;
    }

    /** 同一份解挖几次后重新生成，兼顾难度命中率与生成耗时。 */
    private static final int SEED_REFRESH = 8;

    /**
     * 一局出题的母版：完整解、锯齿区域（非锯齿为空数组）以及由解反推的连续标记与杀手笼；挖提示只从母版复制，不重复求解。
     */
    private record SudokuSeed(int[] jigsawMap, int[] solution, int[] cageOf, int[] cageTotals, int[] marks,
                              SudokuConstraints constraints) {}

    /**
     * 生成母版：锯齿先构造 9 区域再填解，其余变体直接填解；随后反推标记与笼。区域无解时换一份，最多 {@link #LAYOUT_ATTEMPTS} 次。
     */
    private static SudokuSeed createSeed(RandomSource random, Difficulty difficulty, int variantMask) {
        int[] solution = new int[CELLS];
        int[] jigsawMap = new int[CELLS];
        SudokuConstraints geometry;
        if ((variantMask & SudokuVariant.JIGSAW.mask()) != 0) {
            boolean ready = false;
            geometry = null;
            for (int layoutAttempt = 0; layoutAttempt < LAYOUT_ATTEMPTS && !ready; layoutAttempt++) {
                var map = JigsawLayout.generate(random);
                if (map == null) continue;
                // 先把本局区域装进约束再填解。
                SudokuConstraints withMap = SudokuConstraints.of(variantMask, map, null, null, null);
                if (SudokuSolver.fillBoard(solution, withMap, random)) {
                    jigsawMap = map;
                    geometry = withMap;
                    ready = true;
                }
            }
            if (!ready) {
                jigsawMap = JigsawLayout.fallback();
                geometry = SudokuConstraints.of(variantMask, jigsawMap, null, null, null);
                fillBoard(solution, geometry, random);
            }
        } else {
            // 固定几何先组装，再填解。
            geometry = SudokuConstraints.of(variantMask, null, null, null, null);
            fillBoard(solution, geometry, random);
        }

        // 笼和与连续标记是解派生数据：先有满足几何约束的合法解，再落成数据，因此解天然满足它们。
        int[] cageOf = new int[CELLS];
        int[] cageTotals = new int[0];
        int[] marks = new int[CELLS];
        if ((variantMask & SudokuVariant.KILLER.mask()) != 0) {
            cageTotals = createCages(solution, cageOf, random, difficulty);
        }
        if ((variantMask & SudokuVariant.CONSECUTIVE.mask()) != 0) {
            createMarks(solution, marks);
        }
        SudokuConstraints constraints = new SudokuPuzzle(new int[CELLS], jigsawMap, cageOf, cageTotals, marks)
                .constraints(variantMask);
        return new SudokuSeed(jigsawMap, solution, cageOf, cageTotals, marks, constraints);
    }

    /** 锯齿区域无解时最多换几次。 */
    private static final int LAYOUT_ATTEMPTS = 8;
    /** 填解失败时换随机顺序重试的次数。 */
    private static final int FILL_ATTEMPTS = 8;

    /** 在固定约束下随机填一份完整解，失败时换随机顺序重试。 */
    private static boolean fillBoard(int[] solution, SudokuConstraints constraints, RandomSource random) {
        for (int attempt = 0; attempt < FILL_ATTEMPTS; attempt++) {
            if (SudokuSolver.fillBoard(solution, constraints, random)) return true;
        }
        return false;
    }

    /** 从母版的完整解挖到目标提示数，每挖一格都要求当前约束下仍唯一。 */
    private static SudokuPuzzle carve(RandomSource random, SudokuSeed seed, int clueTarget) {
        int[] puzzle = seed.solution().clone();
        int[] order = shuffledCells(random);
        int remaining = CELLS;
        for (int index = 0; index < order.length && remaining > clueTarget; index++) {
            int cell = order[index];
            int value = puzzle[cell];
            puzzle[cell] = 0;
            if (SudokuSolver.countSolutions(puzzle, seed.constraints(), 2) == 1) {
                remaining--;
            } else {
                puzzle[cell] = value;
            }
        }
        return new SudokuPuzzle(puzzle, seed.jigsawMap(), seed.cageOf(), seed.cageTotals(), seed.marks());
    }

    /**
     * 连续标记：标出完整解中所有「相邻且相差 1」的边；弱连续不约束未标记边，所以这份解天然满足。
     */
    private static void createMarks(int[] solution, int[] marks) {
        for (int cell = 0; cell < CELLS; cell++) {
            int row = cell / SIZE;
            int column = cell % SIZE;
            if (column < SIZE - 1 && Math.abs(solution[cell] - solution[cell + 1]) == 1) {
                marks[cell] |= SudokuConstraints.DIR_RIGHT;
                marks[cell + 1] |= SudokuConstraints.DIR_LEFT;
            }
            if (row < SIZE - 1 && Math.abs(solution[cell] - solution[cell + SIZE]) == 1) {
                marks[cell] |= SudokuConstraints.DIR_DOWN;
                marks[cell + SIZE] |= SudokuConstraints.DIR_UP;
            }
        }
    }

    /**
     * 杀手笼：随机贪心放 2~3 格直条，放不下的格子留成单格笼，再优先并进相邻笼；相邻两格值必不同，两个单格笼
     * 可直接合并，并入多格笼前仍检查解值不重叠。单格笼清完后随机合并相邻笼，上限由该档 cageMaxSize 决定；
     * 返回按 1 起的笼和，写进 {@code cageOf}。
     */
    private static int[] createCages(int[] solution, int[] cageOf, RandomSource random, Difficulty difficulty) {
        int maxSize = Math.max(2, difficulty.cageMaxSize());
        for (int attempt = 0; attempt < CAGE_LAYOUT_ATTEMPTS; attempt++) {
            var cages = new ObjectArrayList<int[]>(CELLS);
            int[] values = new int[CELLS];
            int[] owner = new int[CELLS];
            Arrays.fill(owner, -1);
            randomTiling(owner, cages, random);
            fillCageValues(solution, values, cages);
            boolean[] alive = new boolean[cages.size()];
            Arrays.fill(alive, true);
            eliminateSingletons(cages, alive, values, owner, maxSize, random);
            if (hasSingleton(cages, alive)) continue;
            mergeAdjacent(cages, alive, values, owner, maxSize, random);
            if (hasSingleton(cages, alive)) continue;
            return writeCages(solution, cageOf, cages, alive);
        }
        // 随机切分多次仍有单格笼时，退回每行 3+3+3 的直条切分。
        var cages = new ObjectArrayList<int[]>(CELLS);
        int[] values = new int[CELLS];
        int[] owner = new int[CELLS];
        Arrays.fill(owner, -1);
        fallbackTiling(owner, cages);
        fillCageValues(solution, values, cages);
        boolean[] alive = new boolean[cages.size()];
        Arrays.fill(alive, true);
        mergeAdjacent(cages, alive, values, owner, maxSize, random);
        return writeCages(solution, cageOf, cages, alive);
    }

    /** 随机切分的最多尝试次数。 */
    private static final int CAGE_LAYOUT_ATTEMPTS = 8;

    /**
     * 随机贪心切分：按随机格序以当前空格为中心向左右或上下扩 2~3 格直条；无位置时留单格笼，交给合并阶段。
     */
    private static void randomTiling(int[] owner, ObjectArrayList<int[]> cages, RandomSource random) {
        int[] order = shuffledCells(random);
        int[][] options = new int[12][];
        for (int cell : order) {
            if (owner[cell] != -1) continue;
            int row = cell / SIZE;
            int column = cell % SIZE;
            int count = 0;
            for (int length = 2; length <= 3; length++) {
                for (int start = column - length + 1; start <= column; start++) {
                    if (start < 0 || start + length > SIZE) continue;
                    int[] cells = new int[length];
                    boolean free = true;
                    for (int offset = 0; offset < length; offset++) {
                        cells[offset] = row * SIZE + start + offset;
                        if (owner[cells[offset]] != -1) free = false;
                    }
                    if (free) options[count++] = cells;
                }
                for (int start = row - length + 1; start <= row; start++) {
                    if (start < 0 || start + length > SIZE) continue;
                    int[] cells = new int[length];
                    boolean free = true;
                    for (int offset = 0; offset < length; offset++) {
                        cells[offset] = (start + offset) * SIZE + column;
                        if (owner[cells[offset]] != -1) free = false;
                    }
                    if (free) options[count++] = cells;
                }
            }
            int cageIndex = cages.size();
            int[] chosen = count == 0 ? new int[] { cell } : options[random.nextInt(count)];
            cages.add(chosen);
            for (int member : chosen) {
                owner[member] = cageIndex;
            }
        }
    }

    private static void fillCageValues(int[] solution, int[] values, ObjectArrayList<int[]> cages) {
        for (int index = 0; index < cages.size(); index++) {
            values[index] = cageValueMask(solution, cages.get(index));
        }
    }

    /** 反复把单格笼并进相邻笼；优先并另一个单格笼（相邻两格值必不同）。 */
    private static void eliminateSingletons(ObjectArrayList<int[]> cages, boolean[] alive, int[] values, int[] owner,
                                            int maxSize, RandomSource random) {
        boolean progress = true;
        while (progress) {
            progress = false;
            for (int index = 0; index < cages.size(); index++) {
                if (!alive[index] || cages.get(index).length != 1) continue;
                int candidate = findMergeCandidate(cages, alive, values, owner, index, maxSize, random);
                if (candidate < 0) continue;
                mergeCages(cages, alive, values, owner, index, candidate);
                progress = true;
            }
        }
    }

    /** 随机合并相邻且解值不重叠的笼，把 2~3 格直条长成更大的异形笼。 */
    private static void mergeAdjacent(ObjectArrayList<int[]> cages, boolean[] alive, int[] values, int[] owner,
                                      int maxSize, RandomSource random) {
        int failedAttempts = 0;
        while (failedAttempts < 300) {
            int index = random.nextInt(cages.size());
            if (!alive[index]) {
                failedAttempts++;
                continue;
            }
            int candidate = findMergeCandidate(cages, alive, values, owner, index, maxSize, random);
            if (candidate < 0) {
                failedAttempts++;
                continue;
            }
            mergeCages(cages, alive, values, owner, index, candidate);
            failedAttempts = 0;
        }
    }

    /** 在 {@code index} 的相邻笼里找可合并项；有单格笼候选时优先。 */
    private static int findMergeCandidate(ObjectArrayList<int[]> cages, boolean[] alive, int[] values, int[] owner,
                                          int index, int maxSize, RandomSource random) {
        int[] cage = cages.get(index);
        int single = -1;
        int singleCount = 0;
        int any = -1;
        int anyCount = 0;
        for (int cell : cage) {
            for (int direction : DIRECTION_ORDER) {
                int neighbor = SudokuConstraints.neighbor(cell, direction);
                if (neighbor < 0) continue;
                int other = owner[neighbor];
                if (other < 0 || other == index || !alive[other]) continue;
                if (cage.length + cages.get(other).length > maxSize) continue;
                if ((values[index] & values[other]) != 0) continue;
                anyCount++;
                if (random.nextInt(anyCount) == 0) any = other;
                if (cages.get(other).length == 1) {
                    singleCount++;
                    if (random.nextInt(singleCount) == 0) single = other;
                }
            }
        }
        return single >= 0 ? single : any;
    }

    private static void mergeCages(ObjectArrayList<int[]> cages, boolean[] alive, int[] values, int[] owner,
                                   int index, int other) {
        int[] cage = cages.get(index);
        int[] otherCage = cages.get(other);
        int[] merged = Arrays.copyOf(cage, cage.length + otherCage.length);
        System.arraycopy(otherCage, 0, merged, cage.length, otherCage.length);
        for (int cell : otherCage) {
            owner[cell] = index;
        }
        values[index] |= values[other];
        alive[other] = false;
        cages.set(index, merged);
    }

    private static boolean hasSingleton(ObjectArrayList<int[]> cages, boolean[] alive) {
        for (int index = 0; index < cages.size(); index++) {
            if (alive[index] && cages.get(index).length < 2) return true;
        }
        return false;
    }

    /** 把存活笼编号为 1..n，写回 {@code cageOf} 并汇总笼和。 */
    private static int[] writeCages(int[] solution, int[] cageOf, ObjectArrayList<int[]> cages, boolean[] alive) {
        int cageCount = 0;
        int[] totals = new int[cages.size()];
        for (int index = 0; index < cages.size(); index++) {
            if (!alive[index]) continue;
            cageCount++;
            int total = 0;
            for (int cell : cages.get(index)) {
                cageOf[cell] = cageCount;
                total += solution[cell];
            }
            totals[cageCount - 1] = total;
        }
        return Arrays.copyOf(totals, cageCount);
    }

    /** 备用切分：每行三个 3 格横条，必然覆盖全盘且无单格笼。 */
    private static void fallbackTiling(int[] owner, ObjectArrayList<int[]> cages) {
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column += 3) {
                int cageIndex = cages.size();
                int[] cells = { row * SIZE + column, row * SIZE + column + 1, row * SIZE + column + 2 };
                cages.add(cells);
                for (int member : cells) {
                    owner[member] = cageIndex;
                }
            }
        }
    }

    private static int cageValueMask(int[] solution, int[] cells) {
        int mask = 0;
        for (int cell : cells) {
            mask |= 1 << (solution[cell] - VALUE_MIN);
        }
        return mask;
    }

    /** 合并笼时按上右下左的顺序找相邻笼。 */
    private static final int[] DIRECTION_ORDER = { SudokuConstraints.DIR_UP, SudokuConstraints.DIR_RIGHT,
            SudokuConstraints.DIR_DOWN, SudokuConstraints.DIR_LEFT };

    private static int[] shuffledCells(RandomSource random) {
        int[] order = new int[CELLS];
        for (int cell = 0; cell < CELLS; cell++) {
            order[cell] = cell;
        }
        for (int index = CELLS - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            int value = order[index];
            order[index] = order[swap];
            order[swap] = value;
        }
        return order;
    }

    /** 格子最终显示的数字：提示优先，其次玩家填写，空为 0；数组越界按空处理。 */
    public static int displayedValue(int[] givens, int[] values, int cell) {
        if (cell < 0 || cell >= CELLS) return 0;
        int given = cell < givens.length ? givens[cell] : 0;
        if (given != 0) return given;
        return cell < values.length ? values[cell] : 0;
    }
}

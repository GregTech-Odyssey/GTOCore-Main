package com.gtocore.common.machine.noenergy.game.sudoku;

import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 锯齿变体的 9 区域生成器：每局生成区域，再由出题器在区域约束下填一份完整解。
 *
 * <p>
 * 成对生长：中心宫从 (4,4)（0 起算）随机向外扩，每次同时加入当前格与其 180° 对称格；外围四对固定核心
 * 同样成对生长，一对区域始终等大且互为镜像。生长中途淘汰「不足 9 格且不接触当前区域」的连通块，
 * 一对区域完成后要求剩余连通块都能按 9 格切分，否则换候选或整局重来。
 * 区域全部由相邻格生长，因此连通、每区 9 格、覆盖全盘且整体 180° 对称；结果还会拒绝退化成标准九宫。
 */
public final class JigsawLayout {

    private static final int ATTEMPTS = 512;
    private static final int SIZE = SudokuRules.SIZE;
    private static final int CELLS = SudokuRules.CELLS;
    private static final int REGION_SIZE = SudokuRules.SIZE;

    /** 四对固定核心：左上/右下、上中/下中、右上/左下、右中/左中；每行 {核心A, 核心B, 区域A, 区域B}。 */
    private static final int[][] PAIRS = {
            { 0, 80, 0, 1 },
            { 4, 76, 2, 3 },
            { 8, 72, 4, 5 },
            { 44, 36, 6, 7 } };

    private JigsawLayout() {}

    /** 生成一份锯齿区域；失败返回 {@code null}，由调用方重试。 */
    public static @Nullable int[] generate(RandomSource random) {
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int[] map = new int[CELLS];
            Arrays.fill(map, -1);
            if (growCenter(map, random) && growPairs(map, random) && complete(map) &&
                    !isStandardBoxes(map)) {
                return map;
            }
        }
        return null;
    }

    /** 校验存档里带回来的区域表：编号、数量与 180° 对称都合法。 */
    public static boolean isValid(int[] map) {
        if (map == null || map.length != CELLS) return false;
        int[] counts = new int[SIZE];
        for (int region : map) {
            if (region < 0 || region >= SIZE) return false;
            counts[region]++;
        }
        for (int count : counts) {
            if (count != SIZE) return false;
        }
        for (int cell = 0; cell < CELLS; cell++) {
            if (map[mirror(cell)] != mirrorRegion(map[cell])) return false;
        }
        return true;
    }

    /** 把每格区域编号收集成 9 个全异单元。 */
    public static int[][] regions(int[] map) {
        int[] counts = new int[SIZE];
        for (int region : map) {
            if (region >= 0 && region < SIZE) counts[region]++;
        }
        int[][] regions = new int[SIZE][];
        int[] filled = new int[SIZE];
        for (int region = 0; region < SIZE; region++) {
            regions[region] = new int[counts[region]];
        }
        for (int cell = 0; cell < map.length; cell++) {
            int region = map[cell];
            if (region < 0 || region >= SIZE) continue;
            regions[region][filled[region]++] = cell;
        }
        return regions;
    }

    /** 极端情况下退回按核心重标号的 9 个标准宫，仍满足对称与每区 9 格。 */
    public static int[] fallback() {
        int[] map = new int[CELLS];
        int[] labels = { 0, 2, 4, 7, 8, 6, 5, 3, 1 };
        for (int box = 0; box < SIZE; box++) {
            int startRow = box / SudokuRules.BOX * SudokuRules.BOX;
            int startColumn = box % SudokuRules.BOX * SudokuRules.BOX;
            for (int row = 0; row < SudokuRules.BOX; row++) {
                for (int column = 0; column < SudokuRules.BOX; column++) {
                    map[(startRow + row) * SIZE + startColumn + column] = labels[box];
                }
            }
        }
        return map;
    }

    /** 中心宫：从 (4,4) 起成对生长到 9 格。 */
    private static boolean growCenter(int[] map, RandomSource random) {
        int center = 4 * SIZE + 4;
        map[center] = 8;
        int[] cells = new int[REGION_SIZE];
        cells[0] = center;
        int size = 1;
        int[] candidates = new int[CELLS * 4];
        while (size < REGION_SIZE) {
            int count = collectPairCandidates(map, cells, size, candidates);
            if (count == 0) return false;
            boolean placed = false;
            shuffle(candidates, count, random);
            for (int index = 0; index < count; index++) {
                int cell = candidates[index * 2];
                int mirror = candidates[index * 2 + 1];
                map[cell] = 8;
                map[mirror] = 8;
                cells[size] = cell;
                cells[size + 1] = mirror;
                if (prune(map, false, cells, size + 2, false)) {
                    size += 2;
                    placed = true;
                    break;
                }
                map[cell] = -1;
                map[mirror] = -1;
            }
            if (!placed) return false;
        }
        return prune(map, true, null, 0, false);
    }

    /** 四对外围宫：每对同时生长，双方始终镜像等大。 */
    private static boolean growPairs(int[] map, RandomSource random) {
        int[] candidates = new int[CELLS * 4];
        for (int[] pair : PAIRS) {
            int coreA = pair[0];
            int coreB = pair[1];
            int regionA = pair[2];
            int regionB = pair[3];
            if (map[coreA] != -1 || map[coreB] != -1) return false;
            map[coreA] = regionA;
            map[coreB] = regionB;
            int[] cells = new int[REGION_SIZE];
            cells[0] = coreA;
            int size = 1;
            while (size < REGION_SIZE) {
                int count = collectPairCandidates(map, cells, size, candidates);
                if (count == 0) return false;
                boolean placed = false;
                shuffle(candidates, count, random);
                for (int index = 0; index < count; index++) {
                    int cell = candidates[index * 2];
                    int mirror = candidates[index * 2 + 1];
                    map[cell] = regionA;
                    map[mirror] = regionB;
                    cells[size] = cell;
                    if (prune(map, false, cells, size + 1, true)) {
                        size++;
                        placed = true;
                        break;
                    }
                    map[cell] = -1;
                    map[mirror] = -1;
                }
                if (!placed) return false;
            }
            if (!prune(map, true, null, 0, false)) return false;
        }
        return true;
    }

    /** 收集所有可成对加入的候选，按 [格, 对称格] 连续存放。 */
    private static int collectPairCandidates(int[] map, int[] cells, int size, int[] candidates) {
        int count = 0;
        for (int index = 0; index < size; index++) {
            for (int direction : SudokuConstraints.ALL_DIRECTIONS_BITS) {
                int neighbor = SudokuConstraints.neighbor(cells[index], direction);
                if (neighbor < 0 || map[neighbor] != -1) continue;
                int mirror = mirror(neighbor);
                if (mirror == neighbor || map[mirror] != -1) continue;
                candidates[count * 2] = neighbor;
                candidates[count * 2 + 1] = mirror;
                count++;
            }
        }
        return count;
    }

    /** 随机打乱候选对；成对数据整体交换，不能拆开。 */
    private static void shuffle(int[] candidates, int count, RandomSource random) {
        for (int index = count - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            int cell = candidates[index * 2];
            int mirror = candidates[index * 2 + 1];
            candidates[index * 2] = candidates[swap * 2];
            candidates[index * 2 + 1] = candidates[swap * 2 + 1];
            candidates[swap * 2] = cell;
            candidates[swap * 2 + 1] = mirror;
        }
    }

    /**
     * 连通块剪枝：生长中途只淘汰「小于 9 格且与当前生长区域（及其镜像）不接触」的连通块；
     * 一对区域完成后，剩余每个连通块必须是 9 的倍数。
     */
    private static boolean prune(int[] map, boolean pairDone, int[] growing, int size, boolean mirrorToo) {
        boolean[] visited = new boolean[CELLS];
        int[] stack = new int[CELLS];
        for (int start = 0; start < CELLS; start++) {
            if (map[start] != -1 || visited[start]) continue;
            int componentSize = 0;
            boolean touchesGrowing = false;
            int top = 0;
            stack[top++] = start;
            visited[start] = true;
            while (top > 0) {
                int cell = stack[--top];
                componentSize++;
                if (!touchesGrowing && touchesGrowing(cell, growing, size, mirrorToo)) {
                    touchesGrowing = true;
                }
                for (int direction : SudokuConstraints.ALL_DIRECTIONS_BITS) {
                    int neighbor = SudokuConstraints.neighbor(cell, direction);
                    if (neighbor < 0 || map[neighbor] != -1 || visited[neighbor]) continue;
                    visited[neighbor] = true;
                    stack[top++] = neighbor;
                }
            }
            if (pairDone && (componentSize < REGION_SIZE || componentSize % REGION_SIZE != 0)) {
                return false;
            }
            if (!pairDone && !touchesGrowing && componentSize < REGION_SIZE) {
                return false;
            }
        }
        return true;
    }

    /** 该格是否贴着当前正在生长的区域（或它的对称区域）。 */
    private static boolean touchesGrowing(int cell, int[] growing, int size, boolean mirrorToo) {
        for (int direction : SudokuConstraints.ALL_DIRECTIONS_BITS) {
            int neighbor = SudokuConstraints.neighbor(cell, direction);
            if (neighbor < 0) continue;
            for (int index = 0; index < size; index++) {
                if (neighbor == growing[index]) return true;
                if (mirrorToo && mirror(neighbor) == growing[index]) return true;
            }
        }
        return false;
    }

    /** 拒绝整份布局退化成标准九宫。 */
    private static boolean isStandardBoxes(int[] map) {
        for (int region = 0; region < SIZE; region++) {
            int box = -1;
            for (int cell = 0; cell < CELLS; cell++) {
                if (map[cell] != region) continue;
                int cellBox = cell / SIZE / SudokuRules.BOX * SudokuRules.BOX + cell % SIZE / SudokuRules.BOX;
                if (box < 0) box = cellBox;
                if (box != cellBox) return false;
            }
        }
        return true;
    }

    private static boolean complete(int[] map) {
        for (int cell = 0; cell < CELLS; cell++) {
            if (map[cell] < 0 || map[mirror(cell)] != mirrorRegion(map[cell])) return false;
        }
        return true;
    }

    private static int mirror(int cell) {
        int row = cell / SIZE;
        int column = cell % SIZE;
        return (SIZE - 1 - row) * SIZE + SIZE - 1 - column;
    }

    private static int mirrorRegion(int region) {
        return switch (region) {
            case 0 -> 1;
            case 1 -> 0;
            case 2 -> 3;
            case 3 -> 2;
            case 4 -> 5;
            case 5 -> 4;
            case 6 -> 7;
            case 7 -> 6;
            default -> 8;
        };
    }
}

package com.gtocore.common.machine.noenergy.game.numberPuzzle;

import net.minecraft.util.RandomSource;

/**
 * 数字华容道规则：n×n 盘面，数字 1..n²-1 加一个空格，目标按行排好、空格在右下。
 * 生成用 Fisher-Yates 随机排列，再按轮换奇偶性修正；判定与移动都是 O(n²) 以内的本地操作。
 */
public final class NumberPuzzleRules {

    public static final int MIN_SIZE = 3;
    public static final int MAX_SIZE = 8;
    public static final int SIZE_COUNT = MAX_SIZE - MIN_SIZE + 1;
    public static final int CELL_CAPACITY = MAX_SIZE * MAX_SIZE;

    /** 六档尺寸与通关奖励。 */
    public enum Difficulty {

        THREE(MIN_SIZE, 1),
        FOUR(4, 4),
        FIVE(5, 8),
        SIX(6, 15),
        SEVEN(7, 25),
        EIGHT(MAX_SIZE, 40);

        public static final Difficulty[] ALL = values();

        private final int size;
        private final long reward;

        Difficulty(int size, long reward) {
            this.size = size;
            this.reward = reward;
        }

        public int size() {
            return size;
        }

        public long reward() {
            return reward;
        }

        public static Difficulty of(int ordinal) {
            return ordinal >= 0 && ordinal < ALL.length ? ALL[ordinal] : null;
        }
    }

    private NumberPuzzleRules() {}

    public static int sizeOf(int ordinal) {
        return Math.clamp(ordinal, 0, SIZE_COUNT - 1) + MIN_SIZE;
    }

    /** 生成一局可解盘面；返回长度正好是 size²，0 为空格。 */
    public static int[] createBoard(int size, RandomSource random) {
        int cellCount = size * size;
        int[] tiles = new int[cellCount - 1];
        for (int index = 0; index < tiles.length; index++) {
            tiles[index] = index + 1;
        }
        for (int index = tiles.length - 1; index > 0; index--) {
            int other = random.nextInt(index + 1);
            int value = tiles[index];
            tiles[index] = tiles[other];
            tiles[other] = value;
        }

        int blank = random.nextInt(cellCount);
        int blankRow = blank / size;
        int parity = permutationParity(tiles);
        int neededParity = (size & 1) == 1 ? 0 : 1 - (blankRow & 1);
        if (parity != neededParity && tiles.length >= 2) {
            int value = tiles[0];
            tiles[0] = tiles[1];
            tiles[1] = value;
        }

        int[] board = new int[cellCount];
        int tile = 0;
        for (int index = 0; index < blank; index++) {
            board[index] = tiles[tile++];
        }
        board[blank] = 0;
        while (tile < tiles.length) {
            board[++blank] = tiles[tile++];
        }

        // 随机排列偶尔正好是完成态；做一次偶置换（3-循环），不破坏可解性。
        if (isSolved(board, size)) {
            int value = board[0];
            board[0] = board[1];
            board[1] = board[2];
            board[2] = value;
        }
        return board;
    }

    /** 轮换分解求排列奇偶性，O(N)。 */
    private static int permutationParity(int[] tiles) {
        boolean[] visited = new boolean[tiles.length];
        int cycles = 0;
        for (int index = 0; index < tiles.length; index++) {
            if (visited[index]) continue;
            cycles++;
            int next = index;
            while (!visited[next]) {
                visited[next] = true;
                next = tiles[next] - 1;
            }
        }
        return (tiles.length - cycles) & 1;
    }

    /** 传统奇偶判据，供生成测试与外部校验使用。 */
    public static boolean isSolvable(int[] board, int size) {
        int blank = findBlank(board, size);
        if (blank < 0) return false;
        int[] tiles = new int[size * size - 1];
        int tile = 0;
        for (int index = 0; index < size * size; index++) {
            if (index == blank) continue;
            tiles[tile++] = board[index];
        }
        int parity = permutationParity(tiles);
        int blankRow = blank / size;
        return (size & 1) == 1 ? parity == 0 : parity != (blankRow & 1);
    }

    /** 把所有方块按 1..n²-1、空格右下的顺序排好。 */
    public static boolean isSolved(int[] board, int size) {
        int cellCount = size * size;
        for (int index = 0; index < cellCount - 1; index++) {
            if (board[index] != index + 1) return false;
        }
        return board[cellCount - 1] == 0;
    }

    /** 找到空格；没有则返回 -1。 */
    public static int findBlank(int[] board, int size) {
        int cellCount = size * size;
        for (int index = 0; index < cellCount; index++) {
            if (board[index] == 0) return index;
        }
        return -1;
    }

    /** 把与空格上下左右相邻的格子滑进空格；返回新的空格下标，非法移动返回 -1。 */
    public static int move(int[] board, int size, int blank, int clicked) {
        int cellCount = size * size;
        if (blank < 0 || blank >= cellCount || clicked < 0 || clicked >= cellCount) return -1;
        int blankCol = blank % size;
        int blankRow = blank / size;
        int col = clicked % size;
        int row = clicked / size;
        if (Math.abs(blankCol - col) + Math.abs(blankRow - row) != 1) return -1;
        board[blank] = board[clicked];
        board[clicked] = 0;
        return clicked;
    }

    /** 盘面是否是合法的 0..n²-1 排列。 */
    public static boolean isValid(int[] board, int size) {
        int cellCount = size * size;
        if (board.length < cellCount) return false;
        boolean[] seen = new boolean[cellCount];
        for (int index = 0; index < cellCount; index++) {
            int value = board[index];
            if (value < 0 || value >= cellCount || seen[value]) return false;
            seen[value] = true;
        }
        return true;
    }
}

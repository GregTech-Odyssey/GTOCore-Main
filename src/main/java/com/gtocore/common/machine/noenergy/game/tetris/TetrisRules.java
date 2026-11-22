package com.gtocore.common.machine.noenergy.game.tetris;

import java.util.Arrays;

/**
 * 俄罗斯方块规则：10×20 盘面、7 种方块、4 个旋转态与 SRS 踢墙，消行与计分都用标准值。
 *
 * <p>
 * 盘面按行优先存 {@link #CELLS} 个 {@code int}，0 为空格、1~7 是已落定方块的类型 + 1。
 * 每种方块用 4×4 的 16 位掩码表示（位 = {@code y * 4 + x}），四个旋转态各一张。
 * 纯逻辑，不持有机器状态，只在服务端线程调用。
 */
public final class TetrisRules {

    /** 盘面宽（格）。 */
    public static final int COLS = 10;
    /** 盘面高（格）。 */
    public static final int ROWS = 20;
    public static final int CELLS = COLS * ROWS;

    /** 没有活动方块。 */
    public static final int NONE = -1;
    public static final int PIECE_COUNT = 7;
    public static final int ROTATION_COUNT = 4;

    public static final int PIECE_I = 0;
    public static final int PIECE_O = 1;
    public static final int PIECE_T = 2;
    public static final int PIECE_S = 3;
    public static final int PIECE_Z = 4;
    public static final int PIECE_J = 5;
    public static final int PIECE_L = 6;

    /** 一次消 1/2/3/4 行的基础分，倍数见 {@link #LINE_SCORES}。 */
    public static final int[] LINE_SCORES = { 0, 100, 300, 500, 800 };
    /** 兑现比例：超过兑现基线后每这么多分折 1 枚游戏币。 */
    public static final int POINTS_PER_COIN = 200;

    /** 每种 [方块][旋转态] 的 4×4 掩码；位下标是 {@code y * 4 + x}。 */
    private static final int[][] MASKS = {
            { 0x00F0, 0x4444, 0x0F00, 0x2222 },
            { 0x0066, 0x0066, 0x0066, 0x0066 },
            { 0x0072, 0x0262, 0x0270, 0x0232 },
            { 0x0036, 0x0462, 0x0360, 0x0231 },
            { 0x0063, 0x0264, 0x0630, 0x0132 },
            { 0x0071, 0x0226, 0x0470, 0x0322 },
            { 0x0074, 0x0622, 0x0170, 0x0223 } };

    /** 不旋转时的唯一偏移。 */
    private static final int[] NO_KICK = { 0, 0 };
    /** 从 {@code from} 转到 {@code to} 的踢墙偏移，下标 {@code from * 4 + to}；未填的沿用不旋转。 */
    private static final int[][] JLSTZ_KICKS = new int[16][];
    private static final int[][] I_KICKS = new int[16][];

    static {
        JLSTZ_KICKS[0 * 4 + 1] = new int[] { 0, 0, -1, 0, -1, -1, 0, 2, -1, 2 };
        JLSTZ_KICKS[1 * 4 + 0] = new int[] { 0, 0, 1, 0, 1, 1, 0, -2, 1, -2 };
        JLSTZ_KICKS[1 * 4 + 2] = new int[] { 0, 0, 1, 0, 1, 1, 0, -2, 1, -2 };
        JLSTZ_KICKS[2 * 4 + 1] = new int[] { 0, 0, -1, 0, -1, -1, 0, 2, -1, 2 };
        JLSTZ_KICKS[2 * 4 + 3] = new int[] { 0, 0, 1, 0, 1, -1, 0, 2, 1, 2 };
        JLSTZ_KICKS[3 * 4 + 2] = new int[] { 0, 0, -1, 0, -1, 1, 0, -2, -1, -2 };
        JLSTZ_KICKS[3 * 4 + 0] = new int[] { 0, 0, -1, 0, -1, 1, 0, -2, -1, -2 };
        JLSTZ_KICKS[0 * 4 + 3] = new int[] { 0, 0, 1, 0, 1, -1, 0, 2, 1, 2 };

        I_KICKS[0 * 4 + 1] = new int[] { 0, 0, -2, 0, 1, 0, -2, 1, 1, -2 };
        I_KICKS[1 * 4 + 0] = new int[] { 0, 0, 2, 0, -1, 0, 2, -1, -1, 2 };
        I_KICKS[1 * 4 + 2] = new int[] { 0, 0, -1, 0, 2, 0, -1, -2, 2, 1 };
        I_KICKS[2 * 4 + 1] = new int[] { 0, 0, 1, 0, -2, 0, 1, 2, -2, -1 };
        I_KICKS[2 * 4 + 3] = new int[] { 0, 0, 2, 0, -1, 0, 2, -1, -1, 2 };
        I_KICKS[3 * 4 + 2] = new int[] { 0, 0, -2, 0, 1, 0, -2, 1, 1, -2 };
        I_KICKS[3 * 4 + 0] = new int[] { 0, 0, 1, 0, -2, 0, 1, 2, -2, -1 };
        I_KICKS[0 * 4 + 3] = new int[] { 0, 0, -1, 0, 2, 0, -1, -2, 2, 1 };
    }

    private TetrisRules() {}

    /** 方块在某个旋转态下的掩码；越界序数按标准方块处理。 */
    public static int mask(int piece, int rotation) {
        return MASKS[Math.clamp(piece, 0, PIECE_COUNT - 1)][rotation & 3];
    }

    /** 方块放在 {@code (x, y)} 是否不越界且不重叠。 */
    public static boolean fits(int[] board, int piece, int rotation, int x, int y) {
        int mask = mask(piece, rotation);
        for (int bit = 0; bit < 16; bit++) {
            if ((mask & (1 << bit)) == 0) continue;
            int column = x + (bit & 3);
            int row = y + (bit >> 2);
            if (column < 0 || column >= COLS || row < 0 || row >= ROWS) return false;
            if (board[row * COLS + column] != 0) return false;
        }
        return true;
    }

    /** 把方块写进盘面，{@code value} 是落定后的格子值（方块序数 + 1）。 */
    public static void place(int[] board, int piece, int rotation, int x, int y, int value) {
        int mask = mask(piece, rotation);
        for (int bit = 0; bit < 16; bit++) {
            if ((mask & (1 << bit)) == 0) continue;
            int column = x + (bit & 3);
            int row = y + (bit >> 2);
            if (column < 0 || column >= COLS || row < 0 || row >= ROWS) continue;
            board[row * COLS + column] = value;
        }
    }

    /** 方块从当前位置能直落几格（硬降与残影预览共用）。 */
    public static int dropDistance(int[] board, int piece, int rotation, int x, int y) {
        int distance = 0;
        while (fits(board, piece, rotation, x, y + distance + 1)) {
            distance++;
        }
        return distance;
    }

    /**
     * 消行：从底部往上把满行删掉，上方整体下移；返回消掉的行数（0~4）。
     * 原地修改盘面，空出的顶部补 0。
     */
    public static int clearLines(int[] board) {
        int write = ROWS - 1;
        int cleared = 0;
        for (int row = ROWS - 1; row >= 0; row--) {
            int base = row * COLS;
            boolean full = true;
            for (int column = 0; column < COLS; column++) {
                if (board[base + column] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                cleared++;
                continue;
            }
            if (write != row) {
                System.arraycopy(board, base, board, write * COLS, COLS);
            }
            write--;
        }
        for (int row = write; row >= 0; row--) {
            Arrays.fill(board, row * COLS, row * COLS + COLS, 0);
        }
        return cleared;
    }

    /** 从 {@code from} 转到 {@code to} 的踢墙偏移（{@code x, y} 成对）；O 方块不需要踢墙。 */
    public static int[] kicks(int piece, int from, int to) {
        if (piece == PIECE_O) return NO_KICK;
        int[][] table = piece == PIECE_I ? I_KICKS : JLSTZ_KICKS;
        int[] kicks = table[(from & 3) * 4 + (to & 3)];
        return kicks == null ? NO_KICK : kicks;
    }
}

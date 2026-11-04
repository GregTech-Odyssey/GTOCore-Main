package com.gtocore.common.machine.noenergy.game.klotski;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 经典华容道的规则与关卡数据。
 *
 * <p>
 * 盘面固定 6×6，火车（{@link #TRAIN_TYPE}）占两格，左端停到 {@link #TARGET_X},{@link #TARGET_Y} 即通关。
 * 关卡表从 {@code data/gtocore/klotski_levels.txt} 读取：每行 {@code 最少步数|x,y,类型;...}；
 * 前 1000 行是分章节模式，后 1000 行是挑战模式。关卡定义是静态数据，服务端与客户端读同一份。
 */
public final class KlotskiRules {

    private KlotskiRules() {}

    /** 盘面边长（格）。 */
    public static final int BOARD_SIZE = 6;
    /** 每种模式的关卡数。 */
    public static final int LEVEL_COUNT = 1000;
    /** 分章节模式的章节数与每章关卡数。 */
    public static final int CHAPTER_COUNT = 20;
    public static final int CHAPTER_SIZE = 50;

    /** 火车（目标滑块）的类型编号。 */
    public static final int TRAIN_TYPE = 1;
    /** 通关时火车左端所在格。 */
    public static final int TARGET_X = 4;
    public static final int TARGET_Y = 2;

    /**
     * 分章节模式各章的解锁星数：累计星数达到该值才能进入该章。
     * 下标是章序号，第 0 章（第一章）始终开放。
     */
    public static final int[] CHAPTER_UNLOCK_STARS = {
            0, 30, 60, 90, 120, 180, 240, 300, 360, 420,
            480, 540, 600, 660, 720, 780, 840, 900, 960, 1020
    };

    /** 每累计这么多星发一次奖励。 */
    public static final int STARS_PER_REWARD = 50;
    /** 分章节模式每 50 星奖励的游戏币。 */
    public static final int CAMPAIGN_REWARD = 20;
    /** 挑战模式每 50 星奖励的游戏币。 */
    public static final int CHALLENGE_REWARD = 10;

    /** 0 = 分章节，1 = 挑战。 */
    public static final int MODE_CAMPAIGN = 0;
    public static final int MODE_CHALLENGE = 1;

    private static final String LEVEL_DATA = "/data/gtocore/game/klotski_levels.txt";

    private static final LevelData[] CAMPAIGN = new LevelData[LEVEL_COUNT];
    private static final LevelData[] CHALLENGE = new LevelData[LEVEL_COUNT];

    static {
        load();
    }

    /**
     * 一个滑块：左上角格子与类型。类型决定尺寸：
     * 1/2 → 2×1，3 → 3×1，4 → 1×2，5 → 1×3。表里没有 1×1 的滑块。
     */
    public record Block(int x, int y, int type) {

        public int width() {
            return switch (type) {
                case 1, 2 -> 2;
                case 3 -> 3;
                default -> 1;
            };
        }

        public int height() {
            return switch (type) {
                case 4 -> 2;
                case 5 -> 3;
                default -> 1;
            };
        }

        /** 只能沿横向长边滑动。 */
        public boolean horizontal() {
            return width() > height();
        }

        /** 只能沿纵向长边滑动。 */
        public boolean vertical() {
            return height() > width();
        }
    }

    /** 一关：最少步数与初始滑块布局。 */
    public record LevelData(int minMoves, Block[] blocks) {

        /** 初始位置，按 {@code x, y} 两个一组。 */
        public int[] initialPositions() {
            int[] positions = new int[blocks.length * 2];
            for (int index = 0; index < blocks.length; index++) {
                positions[index * 2] = blocks[index].x();
                positions[index * 2 + 1] = blocks[index].y();
            }
            return positions;
        }
    }

    /** 取某一关；模式与下标都会被夹到有效范围。 */
    public static LevelData level(int mode, int index) {
        LevelData[] levels = mode == MODE_CHALLENGE ? CHALLENGE : CAMPAIGN;
        return levels[Math.clamp(index, 0, LEVEL_COUNT - 1)];
    }

    /** 按步数评分：达到最少步数三星，多出不到五步二星，其余一星。 */
    public static int starsFor(int moves, int minMoves) {
        if (moves <= minMoves) return 3;
        return moves - minMoves < 5 ? 2 : 1;
    }

    /** 累计星数是否够解锁该章。 */
    public static boolean chapterUnlocked(int totalStars, int chapter) {
        int index = Math.clamp(chapter, 0, CHAPTER_COUNT - 1);
        return totalStars >= CHAPTER_UNLOCK_STARS[index];
    }

    /** 当前局面里火车是否已经停在出口。 */
    public static boolean isSolved(LevelData level, int[] positions) {
        for (int index = 0; index < level.blocks().length; index++) {
            if (level.blocks()[index].type() != TRAIN_TYPE) continue;
            return positions[index * 2] == TARGET_X && positions[index * 2 + 1] == TARGET_Y;
        }
        return false;
    }

    /**
     * 这一手是否合法：目标不能越过别的滑块，也不能出界；横滑块只能改 x，竖滑块只能改 y。
     * 起点与终点相同视为合法（不产生步数）。
     */
    public static boolean canSlide(LevelData level, int[] positions, int index, int targetX, int targetY) {
        if (index < 0 || index >= level.blocks().length || positions.length != level.blocks().length * 2) return false;
        Block block = level.blocks()[index];
        int x = positions[index * 2];
        int y = positions[index * 2 + 1];
        if (targetX == x && targetY == y) return true;
        if (block.horizontal() && targetY != y) return false;
        if (block.vertical() && targetX != x) return false;
        if (targetX < 0 || targetY < 0 ||
                targetX + block.width() > BOARD_SIZE || targetY + block.height() > BOARD_SIZE) {
            return false;
        }
        int stepX = Integer.signum(targetX - x);
        int stepY = Integer.signum(targetY - y);
        int steps = Math.max(Math.abs(targetX - x), Math.abs(targetY - y));
        for (int step = 1; step <= steps; step++) {
            int nextX = x + stepX * step;
            int nextY = y + stepY * step;
            if (!isFree(level, positions, index, nextX, nextY, block.width(), block.height())) return false;
        }
        return true;
    }

    /** 目标位置是否不与其他滑块重叠。 */
    private static boolean isFree(LevelData level, int[] positions, int moving, int x, int y, int width, int height) {
        for (int index = 0; index < level.blocks().length; index++) {
            if (index == moving) continue;
            Block other = level.blocks()[index];
            int otherX = positions[index * 2];
            int otherY = positions[index * 2 + 1];
            if (x < otherX + other.width() && x + width > otherX &&
                    y < otherY + other.height() && y + height > otherY) {
                return false;
            }
        }
        return true;
    }

    // ==================== 关卡表 ====================

    private static void load() {
        try (InputStream stream = KlotskiRules.class.getResourceAsStream(LEVEL_DATA)) {
            if (stream == null) throw new IllegalStateException("Missing klotski level data: " + LEVEL_DATA);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                int line = 0;
                String text;
                while ((text = reader.readLine()) != null) {
                    if (text.isBlank()) continue;
                    int separator = text.indexOf('|');
                    if (separator < 0) throw new IllegalStateException("Bad klotski level line " + (line + 1));
                    int minMoves = Integer.parseInt(text.substring(0, separator).trim());
                    List<Block> blocks = new ArrayList<>();
                    for (String block : text.substring(separator + 1).split(";")) {
                        if (block.isBlank()) continue;
                        String[] parts = block.split(",");
                        if (parts.length < 3) throw new IllegalStateException("Bad klotski block in line " + (line + 1));
                        String type = parts[2].trim();
                        if (type.startsWith("t") || type.startsWith("T")) type = type.substring(1);
                        blocks.add(new Block(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(type)));
                    }
                    LevelData data = new LevelData(minMoves, blocks.toArray(Block[]::new));
                    if (line < LEVEL_COUNT) {
                        CAMPAIGN[line] = data;
                    } else if (line < LEVEL_COUNT * 2) {
                        CHALLENGE[line - LEVEL_COUNT] = data;
                    } else {
                        break;
                    }
                    line++;
                }
                if (line < LEVEL_COUNT * 2) {
                    throw new IllegalStateException("Klotski level data has only " + line + " levels");
                }
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Unable to read klotski level data", e);
        }
    }
}

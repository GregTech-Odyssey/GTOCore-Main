package com.gtocore.common.machine.noenergy.game.sokoban;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 推箱子的规则与关卡数据。
 *
 * <p>
 * 关卡表从 {@code data/gtocore/game/sokoban_levels.txt} 读取，一行一关：
 * {@code 参考移动步数|参考推动步数|宽|高|第1行/第2行/...}，图块字符为 {@code #} 墙、空格地板、
 * {@code .} 目标、{@code $} 箱子、{@code *} 箱子在目标上、{@code @} 小人、{@code +} 小人在目标上。
 * 每行只写实际存在的格子，行尾之后没有写出的位置按墙处理。
 *
 * <p>
 * 关卡按两层章节组织：每 100 关为一章，8 章组成一个大章；累计星数每满
 * {@link #STARS_PER_CHAPTER} 星解锁下一章，章内完成前一关才解锁下一关。
 * 关卡定义是静态数据，服务端与客户端读同一份。
 */
public final class SokobanRules {

    private SokobanRules() {}

    /** 关卡总数。 */
    public static final int LEVEL_COUNT = 6395;
    /** 每个小章的关卡数。 */
    public static final int CHAPTER_SIZE = 50;
    /** 一个大章包含的小章数。 */
    public static final int CHAPTERS_PER_MAJOR = 16;
    /** 小章数量；最后一章可能不满 {@link #CHAPTER_SIZE} 关。 */
    public static final int CHAPTER_COUNT = (LEVEL_COUNT + CHAPTER_SIZE - 1) / CHAPTER_SIZE;
    /** 大章数量。 */
    public static final int MAJOR_COUNT = CHAPTER_COUNT / CHAPTERS_PER_MAJOR;
    /** 解锁下一章所需的累计星数；完成上一章最后一关也能解锁。 */
    public static final int STARS_PER_CHAPTER = 50;
    /** 每累计这么多星发一次奖励。 */
    public static final int STARS_PER_REWARD = 50;
    /** 每次星数奖励的游戏币。 */
    public static final int REWARD_COINS = 200;

    /** 方向：0 左、1 右、2 上、3 下。 */
    public static final int DIR_LEFT = 0;
    public static final int DIR_RIGHT = 1;
    public static final int DIR_UP = 2;
    public static final int DIR_DOWN = 3;

    /** 静态图块：墙。 */
    public static final char WALL = '#';
    /** 静态图块：目标点。 */
    public static final char GOAL = '.';

    private static final String LEVEL_DATA = "/data/gtocore/game/sokoban_levels.txt";

    private static final LevelData[] LEVELS = new LevelData[LEVEL_COUNT];

    static {
        load();
    }

    /**
     * 一关的静态数据：尺寸、参考移动/推动步数、静态图块（墙/地板/目标）、初始小人位置与箱子位置。
     * 可达区域在读取时用洪水填充标好，界面只画房间内的格子。
     */
    public record LevelData(int width, int height, int bestMoves, int bestPushes, char[] tiles, boolean[] inside,
                            int playerCell, int[] boxes, int goalCount) {}

    /** 取某一关；下标夹到有效范围。 */
    public static LevelData level(int index) {
        return LEVELS[Math.clamp(index, 0, LEVEL_COUNT - 1)];
    }

    /** 这一关所在的章。 */
    public static int chapterOf(int level) {
        return Math.clamp(level, 0, LEVEL_COUNT - 1) / CHAPTER_SIZE;
    }

    /** 这一关在章内的序号（0 起）。 */
    public static int levelInChapter(int level) {
        return Math.clamp(level, 0, LEVEL_COUNT - 1) % CHAPTER_SIZE;
    }

    /** 这一关所在的大章。 */
    public static int majorOf(int level) {
        return chapterOf(level) / CHAPTERS_PER_MAJOR;
    }

    /** 这一章第一关的关卡下标。 */
    public static int chapterStart(int chapter) {
        return Math.max(0, chapter) * CHAPTER_SIZE;
    }

    /** 某一章实际有多少关：最后一章可能不满。 */
    public static int chapterLevels(int chapter) {
        return Math.clamp(LEVEL_COUNT - chapterStart(chapter), 0, CHAPTER_SIZE);
    }

    /**
     * 这一章是否可进入：第一章始终开放；其余章节累计星数达到
     * {@link #STARS_PER_CHAPTER} × 章序号，或上一章最后一关已通关即可。
     */
    public static boolean chapterUnlocked(int totalStars, int chapter, int previousChapterLastStars) {
        if (chapter <= 0) return true;
        return totalStars >= chapter * STARS_PER_CHAPTER || previousChapterLastStars > 0;
    }

    /**
     * 这一关是否已解锁：所在章可进入，且章内第一关之外的关卡要完成前一关。
     *
     * @param previousLevelStars       上一关的星数（关卡 0 传 0）
     * @param previousChapterLastStars 上一章最后一关的星数（第一章传 0）
     */
    public static boolean levelUnlocked(int level, int totalStars, int previousLevelStars,
                                        int previousChapterLastStars) {
        int chapter = chapterOf(level);
        return chapterUnlocked(totalStars, chapter, previousChapterLastStars) &&
                (levelInChapter(level) == 0 || previousLevelStars > 0);
    }

    /** 按移动与推动步数评星：两项都不超过参考值得三星，都不超过参考值的 1.25 倍（向上取整）得二星，其余一星。 */
    public static int starsFor(int moves, int pushes, int bestMoves, int bestPushes) {
        if (moves <= bestMoves && pushes <= bestPushes) return 3;
        return moves <= allowed(bestMoves) && pushes <= allowed(bestPushes) ? 2 : 1;
    }

    /** 二星允许的上限：参考值的 1.25 倍向上取整。 */
    private static int allowed(int reference) {
        return (reference * 5 + 3) / 4;
    }

    /** 这一格是不是墙。 */
    public static boolean wall(LevelData level, int cell) {
        return level.tiles()[cell] == WALL;
    }

    /** 这一格上的箱子下标；没有箱子返回 -1。 */
    public static int boxAt(int[] boxes, int cell) {
        for (int index = 0; index < boxes.length; index++) {
            if (boxes[index] == cell) return index;
        }
        return -1;
    }

    /**
     * 走一步：小人在 {@code direction} 方向上移动一格；前方是箱子时把它往前推一格。
     * 撞墙、推出界或两个箱子挤在一起都不成立，返回 -1。箱子位置直接在 {@code boxes} 上修改。
     */
    public static int step(LevelData level, int playerCell, int[] boxes, int direction) {
        int width = level.width();
        int height = level.height();
        int x = playerCell % width;
        int y = playerCell / width;
        int dx = direction == DIR_LEFT ? -1 : direction == DIR_RIGHT ? 1 : 0;
        int dy = direction == DIR_UP ? -1 : direction == DIR_DOWN ? 1 : 0;
        int targetX = x + dx;
        int targetY = y + dy;
        if (targetX < 0 || targetY < 0 || targetX >= width || targetY >= height) return -1;
        int target = targetY * width + targetX;
        if (level.tiles()[target] == WALL) return -1;
        int box = boxAt(boxes, target);
        if (box >= 0) {
            int beyondX = targetX + dx;
            int beyondY = targetY + dy;
            if (beyondX < 0 || beyondY < 0 || beyondX >= width || beyondY >= height) return -1;
            int beyond = beyondY * width + beyondX;
            if (level.tiles()[beyond] == WALL || boxAt(boxes, beyond) >= 0) return -1;
            boxes[box] = beyond;
        }
        return target;
    }

    /**
     * 这一步是否会推动箱子：正前方是箱子即返回 {@code true}（不判断箱子后面是否被堵）。
     * 与 {@link #step} 配合使用：先判定，再走一步，成功时据此累计推动次数。
     */
    public static boolean pushesBox(LevelData level, int playerCell, int[] boxes, int direction) {
        int width = level.width();
        int x = playerCell % width;
        int y = playerCell / width;
        int dx = direction == DIR_LEFT ? -1 : direction == DIR_RIGHT ? 1 : 0;
        int dy = direction == DIR_UP ? -1 : direction == DIR_DOWN ? 1 : 0;
        int targetX = x + dx;
        int targetY = y + dy;
        if (targetX < 0 || targetY < 0 || targetX >= width || targetY >= level.height()) return false;
        int target = targetY * width + targetX;
        return level.tiles()[target] != WALL && boxAt(boxes, target) >= 0;
    }

    /** 所有箱子都落在目标点上。 */
    public static boolean isSolved(LevelData level, int[] boxes) {
        if (boxes.length != level.goalCount()) return false;
        char[] tiles = level.tiles();
        for (int cell : boxes) {
            if (tiles[cell] != GOAL) return false;
        }
        return true;
    }

    // ==================== 关卡表 ====================

    private static void load() {
        try (InputStream stream = SokobanRules.class.getResourceAsStream(LEVEL_DATA)) {
            if (stream == null) throw new IllegalStateException("Missing sokoban level data: " + LEVEL_DATA);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                int index = 0;
                String text;
                while ((text = reader.readLine()) != null) {
                    if (text.isEmpty()) continue;
                    if (index >= LEVEL_COUNT) throw new IllegalStateException("Sokoban level data has too many levels");
                    LEVELS[index] = parse(text, index + 1);
                    index++;
                }
                if (index < LEVEL_COUNT) {
                    throw new IllegalStateException("Sokoban level data has only " + index + " levels");
                }
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Unable to read sokoban level data", e);
        }
    }

    private static LevelData parse(String text, int number) {
        int first = text.indexOf('|');
        int second = text.indexOf('|', first + 1);
        int third = text.indexOf('|', second + 1);
        int fourth = text.indexOf('|', third + 1);
        if (first < 0 || second < 0 || third < 0 || fourth < 0) {
            throw new IllegalStateException("Bad sokoban level " + number);
        }
        int bestMoves = Integer.parseInt(text.substring(0, first));
        int bestPushes = Integer.parseInt(text.substring(first + 1, second));
        int width = Integer.parseInt(text.substring(second + 1, third));
        int height = Integer.parseInt(text.substring(third + 1, fourth));
        String[] rows = text.substring(fourth + 1).split("/", -1);
        if (width <= 0 || height <= 0 || rows.length != height) {
            throw new IllegalStateException("Bad sokoban level size " + number);
        }

        char[] tiles = new char[width * height];
        int player = -1;
        int goalCount = 0;
        int[] boxes = new int[width * height];
        int boxCount = 0;
        for (int y = 0; y < height; y++) {
            String row = rows[y];
            for (int x = 0; x < width; x++) {
                int cell = y * width + x;
                // 原始数据用换行断行，行内没有写出的位置是墙，不是地板
                if (x >= row.length()) {
                    tiles[cell] = WALL;
                    continue;
                }
                char symbol = row.charAt(x);
                switch (symbol) {
                    case '#' -> tiles[cell] = WALL;
                    case '.' -> {
                        tiles[cell] = GOAL;
                        goalCount++;
                    }
                    case '$' -> {
                        tiles[cell] = ' ';
                        boxes[boxCount++] = cell;
                    }
                    case '*' -> {
                        tiles[cell] = GOAL;
                        goalCount++;
                        boxes[boxCount++] = cell;
                    }
                    case '@' -> {
                        tiles[cell] = ' ';
                        player = cell;
                    }
                    case '+' -> {
                        tiles[cell] = GOAL;
                        goalCount++;
                        player = cell;
                    }
                    default -> tiles[cell] = ' ';
                }
            }
        }
        if (player < 0) throw new IllegalStateException("Sokoban level " + number + " has no player");

        boolean[] inside = floodInside(tiles, width, height, player, boxes, boxCount);
        return new LevelData(width, height, bestMoves, bestPushes, tiles, inside, player,
                java.util.Arrays.copyOf(boxes, boxCount), goalCount);
    }

    /**
     * 从初始小人位置向外洪水填充，标出房间内（含被箱子挡住的）可达格。
     * 与玩家隔开的封闭小格（有些关卡把箱子预先放在目标点上）也从箱子和目标点补填，界面才画得出来。
     */
    private static boolean[] floodInside(char[] tiles, int width, int height, int start, int[] boxes, int boxCount) {
        boolean[] inside = new boolean[width * height];
        int[] queue = new int[width * height];
        int tail = 0;
        tail = seed(tiles, inside, queue, tail, start);
        for (int index = 0; index < boxCount; index++) {
            tail = seed(tiles, inside, queue, tail, boxes[index]);
        }
        for (int cell = 0; cell < tiles.length; cell++) {
            if (tiles[cell] == GOAL) tail = seed(tiles, inside, queue, tail, cell);
        }
        int head = 0;
        while (head < tail) {
            int cell = queue[head++];
            int x = cell % width;
            int y = cell / width;
            if (x > 0) tail = visit(tiles, inside, queue, tail, cell - 1);
            if (x + 1 < width) tail = visit(tiles, inside, queue, tail, cell + 1);
            if (y > 0) tail = visit(tiles, inside, queue, tail, cell - width);
            if (y + 1 < height) tail = visit(tiles, inside, queue, tail, cell + width);
        }
        return inside;
    }

    private static int seed(char[] tiles, boolean[] inside, int[] queue, int tail, int cell) {
        if (cell < 0 || cell >= tiles.length || inside[cell] || tiles[cell] == WALL) return tail;
        inside[cell] = true;
        queue[tail] = cell;
        return tail + 1;
    }

    private static int visit(char[] tiles, boolean[] inside, int[] queue, int tail, int cell) {
        if (inside[cell] || tiles[cell] == WALL) return tail;
        inside[cell] = true;
        queue[tail] = cell;
        return tail + 1;
    }
}

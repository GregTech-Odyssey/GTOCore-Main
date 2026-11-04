package com.gtocore.common.machine.noenergy.game;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine.MilestoneCell;
import com.gtocore.common.machine.noenergy.game.AbstractGameMachine.MilestoneColumn;
import com.gtocore.common.machine.noenergy.game.game2048.Game2048Machine;
import com.gtocore.common.machine.noenergy.game.game24.Game24Machine;
import com.gtocore.common.machine.noenergy.game.justGet10.JustGet10Machine;
import com.gtocore.common.machine.noenergy.game.klotski.KlotskiMachine;
import com.gtocore.common.machine.noenergy.game.minesweeper.MinesweeperCoreMachine;
import com.gtocore.common.machine.noenergy.game.minesweeper.MinesweeperRules;
import com.gtocore.common.machine.noenergy.game.numberPuzzle.NumberPuzzleMachine;
import com.gtocore.common.machine.noenergy.game.slotMachine.SlotMachine;
import com.gtocore.common.machine.noenergy.game.sudoku.SudokuMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * 全服榜单目录：把分散在各游戏机里的榜统一成一条选择链（游戏 → 一级维度 → 可选的二级维度），
 * 供 {@link GameLeaderboardsPage} 逐级筛选。
 *
 * <p>
 * 每张榜都由对应游戏机自己的 {@code leaderboardBoard()} 给出（列定义、数据版本与行构建器），
 * 与那台机器里的排行榜页是同一份内容，不在这里另写一套。
 */
@DataGeneratorScanned
public final class GameBoardCatalog {

    // ==================== 文案 ====================

    @RegisterLanguage(cn = "游戏", en = "Game")
    private static final Component TITLE_GAME = Component.translatable("gtocore.machine.game.catalog.title.game");
    @RegisterLanguage(cn = "尺寸", en = "Size")
    private static final Component TITLE_SIZE = Component.translatable("gtocore.machine.game.catalog.title.size");
    @RegisterLanguage(cn = "抽牌范围", en = "Card range")
    private static final Component TITLE_RANGE = Component.translatable("gtocore.machine.game.catalog.title.range");
    @RegisterLanguage(cn = "章节", en = "Chapter")
    private static final Component TITLE_CHAPTER = Component.translatable("gtocore.machine.game.catalog.title.chapter");

    @RegisterLanguage(cn = "老虎机", en = "Slot machine")
    private static final Component GAME_SLOT = Component.translatable("gtocore.machine.game.catalog.game.slot");
    @RegisterLanguage(cn = "合成十", en = "Just get 10")
    private static final Component GAME_JUST_GET_10 = Component.translatable("gtocore.machine.game.catalog.game.just_get_10");
    @RegisterLanguage(cn = "电路 2048", en = "Circuit 2048")
    private static final Component GAME_2048 = Component.translatable("gtocore.machine.game.catalog.game.game_2048");
    @RegisterLanguage(cn = "24 点", en = "24 points")
    private static final Component GAME_24 = Component.translatable("gtocore.machine.game.catalog.game.game_24");
    @RegisterLanguage(cn = "数字华容道", en = "Number puzzle")
    private static final Component GAME_NUMBER_PUZZLE = Component.translatable("gtocore.machine.game.catalog.game.number_puzzle");
    @RegisterLanguage(cn = "扫雷 · 基础", en = "Minesweeper · Base")
    private static final Component GAME_MINESWEEPER_BASE = Component.translatable("gtocore.machine.game.catalog.game.minesweeper_base");
    @RegisterLanguage(cn = "扫雷 · 战役", en = "Minesweeper · Campaign")
    private static final Component GAME_MINESWEEPER_CAMPAIGN = Component.translatable("gtocore.machine.game.catalog.game.minesweeper_campaign");
    @RegisterLanguage(cn = "数独", en = "Sudoku")
    private static final Component GAME_SUDOKU = Component.translatable("gtocore.machine.game.catalog.game.sudoku");
    @RegisterLanguage(cn = "经典华容道", en = "Klotski")
    private static final Component GAME_KLOTSKI = Component.translatable("gtocore.machine.game.catalog.game.klotski");

    /** 游戏下拉栏：下标与 {@link #ALL} 的顺序一一对应。 */
    public static final Level GAMES = new Level(TITLE_GAME, new Component[] { GAME_SLOT, GAME_JUST_GET_10,
            GAME_2048, GAME_24, GAME_NUMBER_PUZZLE, GAME_MINESWEEPER_BASE, GAME_MINESWEEPER_CAMPAIGN, GAME_SUDOKU,
            GAME_KLOTSKI });

    // ==================== 目录 ====================

    /** 全部榜组；下标即游戏下拉栏里的序数。 */
    public static final Group[] ALL = {
            new Group(GAME_SLOT, null, null, SlotMachine.leaderboardBoard()),
            new Group(GAME_JUST_GET_10, null, null, JustGet10Machine.leaderboardBoard()),
            new Group(GAME_2048, null, null, Game2048Machine.leaderboardBoard()),
            new Group(GAME_24, new Level(TITLE_RANGE, Game24Machine.RANGE_NAMES), null,
                    Game24Machine.leaderboardBoard()),
            new Group(GAME_NUMBER_PUZZLE, new Level(TITLE_SIZE, NumberPuzzleMachine.SIZE_NAMES), null,
                    NumberPuzzleMachine.leaderboardBoard()),
            new Group(GAME_MINESWEEPER_BASE, new Level(Component.translatable(AbstractGameMachine.PAGE_DIFFICULTY), baseDifficultyNames()),
                    null, MinesweeperCoreMachine.baseLeaderboardBoard()),
            new Group(GAME_MINESWEEPER_CAMPAIGN, new Level(TITLE_CHAPTER, MinesweeperCoreMachine.TIER_NAMES), null,
                    MinesweeperCoreMachine.campaignLeaderboardBoard()),
            new Group(GAME_SUDOKU, new Level(Component.translatable(AbstractGameMachine.PAGE_DIFFICULTY), SudokuMachine.DIFFICULTY_NAMES),
                    new Level(SudokuMachine.PAGE_VARIANT, SudokuMachine.VARIANT_NAMES),
                    SudokuMachine.leaderboardBoard()),
            new Group(GAME_KLOTSKI, new Level(KlotskiMachine.COLUMN_MODE, klotskiModeNames()),
                    null, KlotskiMachine.leaderboardBoard()) };

    private GameBoardCatalog() {}

    /** 选择链的一级：标题 + 各档位的名字。 */
    public record Level(Component title, Component[] names) {

        public int count() {
            return names.length;
        }

        public Component name(int index) {
            return names[Math.clamp(index, 0, names.length - 1)];
        }
    }

    /** 一张榜：列定义、数据版本与行构建器（都在服务端执行）。 */
    public interface Board {

        MilestoneColumn[] columns();

        /** 存档的数据版本：变了说明界面的文字要重建。 */
        int version(MinecraftServer server);

        /** 当前选择对应的行；两个下标按该榜的维度解释，没有的维度传 0。 */
        List<MilestoneCell[]> rows(MinecraftServer server, int first, int second);
    }

    /** 行构建器：与机器排行榜页用的那一份相同，两个下标按该榜的维度解释。 */
    public interface BoardRows {

        List<MilestoneCell[]> build(MinecraftServer server, int first, int second);
    }

    /** 一个可在全部榜单页里选中的榜组：游戏名 + 一级维度 + 可选的二级维度 + 数据。 */
    public record Group(Component name, @Nullable Level first, @Nullable Level second, Board board) {}

    /** 由游戏机自带的列定义与行构建器组一张榜。 */
    public static Board board(MilestoneColumn[] columns, ToIntFunction<MinecraftServer> version, BoardRows rows) {
        return new BoardDef(columns, version, rows);
    }

    /** {@link Board} 的实现：列定义、存档取数与行构建器；版本号直接读对应存档的 {@code modCount}。 */
    private record BoardDef(MilestoneColumn[] columns, ToIntFunction<MinecraftServer> version, BoardRows rows)
            implements Board {

        private static final int NO_DATA = Integer.MIN_VALUE;

        @Override
        public int version(MinecraftServer server) {
            return server == null ? NO_DATA : version.applyAsInt(server);
        }

        @Override
        public List<MilestoneCell[]> rows(MinecraftServer server, int first, int second) {
            return server == null ? List.of() : rows.build(server, first, second);
        }
    }

    /** 扫雷基础只有前五档有榜（自定义档不计成绩）。 */
    private static Component[] baseDifficultyNames() {
        return Arrays.copyOf(MinesweeperCoreMachine.DIFFICULTY_NAMES, MinesweeperRules.Difficulty.CUSTOM.ordinal());
    }

    /** 经典华容道的两个榜：分章节与挑战。 */
    private static Component[] klotskiModeNames() {
        return KlotskiMachine.MODE_KEYS.clone();
    }
}

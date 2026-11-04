package com.gtocore.common.machine.noenergy.game.numberPuzzle;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
import com.gtocore.common.machine.noenergy.game.GameBoardCatalog;
import com.gtocore.common.machine.noenergy.game.LiveDurationLine;
import com.gtocore.common.machine.noenergy.game.milestone.GameMilestones;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.SwitchedContent;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 数字华容道游戏机：3×3 到 8×8 六档，点击与空格相邻的数字块滑动，把 1..n²-1 按序排好、空格归位即完成。
 * 完成一局按尺寸发放游戏币，并把该尺寸的最快完成时间记入里程碑。
 */
@DataGeneratorScanned
public class NumberPuzzleMachine extends AbstractGameMachine<NumberPuzzleMachine.BoardSnapshot> {

    private static final int CELL_SIZE = 20;

    @RegisterLanguage(cn = "3×3", en = "3×3")
    private static final Component SIZE_3 = Component.translatable("gtocore.machine.number_puzzle.size.3");
    @RegisterLanguage(cn = "4×4", en = "4×4")
    private static final Component SIZE_4 = Component.translatable("gtocore.machine.number_puzzle.size.4");
    @RegisterLanguage(cn = "5×5", en = "5×5")
    private static final Component SIZE_5 = Component.translatable("gtocore.machine.number_puzzle.size.5");
    @RegisterLanguage(cn = "6×6", en = "6×6")
    private static final Component SIZE_6 = Component.translatable("gtocore.machine.number_puzzle.size.6");
    @RegisterLanguage(cn = "7×7", en = "7×7")
    private static final Component SIZE_7 = Component.translatable("gtocore.machine.number_puzzle.size.7");
    @RegisterLanguage(cn = "8×8", en = "8×8")
    private static final Component SIZE_8 = Component.translatable("gtocore.machine.number_puzzle.size.8");
    public static final Component[] SIZE_NAMES = { SIZE_3, SIZE_4, SIZE_5, SIZE_6, SIZE_7, SIZE_8 };

    @RegisterLanguage(cn = "尺寸", en = "Size")
    private static final Component SECTION_SIZE = Component.translatable("gtocore.machine.number_puzzle.section.size");
    @RegisterLanguage(cn = "步数", en = "Moves")
    private static final Component LINE_MOVES = Component.translatable("gtocore.machine.number_puzzle.line.moves");
    @RegisterLanguage(cn = "用时", en = "Time")
    private static final Component LINE_TIME = Component.translatable("gtocore.machine.number_puzzle.line.time");
    @RegisterLanguage(cn = "进行中", en = "Playing")
    private static final String TEXT_PLAYING = "gtocore.machine.number_puzzle.text.playing";
    @RegisterLanguage(cn = "完成！用时 %s", en = "Solved! %s")
    private static final String TEXT_SOLVED = "gtocore.machine.number_puzzle.text.solved";
    @RegisterLanguage(cn = "完成奖励：%s 游戏币", en = "Clear reward: %s Game Coins")
    private static final String TEXT_SIZE_REWARD = "gtocore.machine.number_puzzle.text.size_reward";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final Component BUTTON_RESTART = Component.translatable("gtocore.machine.number_puzzle.button.restart");

    /** 单个尺寸的排行榜只有两列：玩家与最快用时。 */
    private static final MilestoneColumn[] LEADERBOARD_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 64),
            MilestoneColumn.center(COLUMN_DURATION, 54) };

    /** 「全部榜单」页里的华容道榜（一级维度是尺寸）：列与行和本机排行榜页完全一致。 */
    public static GameBoardCatalog.Board leaderboardBoard() {
        return GameBoardCatalog.board(LEADERBOARD_COLUMNS, server -> GameMilestones.get(server).modCount(),
                (server, size, unused) -> buildLeaderboardRows(GameMilestones.get(server), server, size));
    }

    private enum Status {
        IDLE,
        PLAYING,
        SOLVED
    }

    /**
     * 下发的界面快照：{@link #board} 只带当前尺寸的 {@code size²} 个有效格，不发整块
     * {@link NumberPuzzleRules#CELL_CAPACITY}（客户端只按 {@code sizeOrdinal} 读前 {@code size²} 格）；
     * {@code equals} 只看版本号，省掉逐元素比数组。
     */
    public record BoardSnapshot(int revision, int sizeOrdinal, int[] board, int moves, long elapsedTicks, int status) {

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, 0, new int[0],
                0, 0L, Status.IDLE.ordinal());
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT, BoardSnapshot::sizeOrdinal,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::board,
                ByteBufCodecs.VAR_INT, BoardSnapshot::moves,
                ByteBufCodecs.VAR_LONG, BoardSnapshot::elapsedTicks,
                ByteBufCodecs.VAR_INT, BoardSnapshot::status,
                BoardSnapshot::new);

        private boolean isSolved() {
            return status == Status.SOLVED.ordinal();
        }

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof BoardSnapshot snapshot && revision == snapshot.revision;
        }

        @Override
        public int hashCode() {
            return revision;
        }
    }

    /** 当前尺寸档；默认 3×3（序数 0）。 */
    @SaveToDisk(defaultValue = "0")
    private int sizeOrdinal = NumberPuzzleRules.Difficulty.THREE.ordinal();
    /** 固定长度盘面，前 size² 格有效；0 为空格。 */
    @SaveToDisk
    private int[] board = new int[NumberPuzzleRules.CELL_CAPACITY];
    @SaveToDisk(defaultValue = "0")
    private int blankCell;
    @SaveToDisk(defaultValue = "0")
    private int moves;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient(scheduleUpdate = true)
    private long roundStart;
    @SaveToDisk(defaultValue = "0")
    private long lastElapsed;
    @SaveToDisk(defaultValue = "0")
    private int status = Status.IDLE.ordinal();

    public NumberPuzzleMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    // ==================== 局面 ====================

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sizeOrdinal = Math.clamp(sizeOrdinal, 0, NumberPuzzleRules.SIZE_COUNT - 1);
        if (status < 0 || status >= Status.values().length) status = Status.IDLE.ordinal();
        int size = NumberPuzzleRules.sizeOf(sizeOrdinal);
        if (!NumberPuzzleRules.isValid(board, size)) {
            Arrays.fill(board, 0);
            blankCell = 0;
            moves = 0;
            lastElapsed = 0L;
            status = Status.IDLE.ordinal();
        } else {
            blankCell = NumberPuzzleRules.findBlank(board, size);
            moves = Math.max(0, moves);
            if (status == Status.PLAYING.ordinal() &&
                    (roundStart <= 0L || roundStart > gameTime())) {
                roundStart = gameTime();
            }
            if (status == Status.SOLVED.ordinal()) lastElapsed = Math.max(1L, lastElapsed);
        }
    }

    @Override
    protected BoardSnapshot createView() {
        return new BoardSnapshot(revision(), sizeOrdinal, activeBoard(), moves, elapsedTicks(), status);
    }

    /** 只取当前尺寸的有效格：客户端按 {@code sizeOrdinal} 读的正是这一段，后面的定长填充不必下发。 */
    private int[] activeBoard() {
        int size = NumberPuzzleRules.sizeOf(sizeOrdinal);
        int area = size * size;
        return area >= board.length ? board : Arrays.copyOf(board, area);
    }

    private boolean isPlaying() {
        return status == Status.PLAYING.ordinal();
    }

    private long elapsedTicks() {
        if (status == Status.SOLVED.ordinal()) return lastElapsed;
        if (status != Status.PLAYING.ordinal() || roundStart <= 0L) return 0L;
        return Math.max(0L, gameTime() - roundStart);
    }

    /** 计时行：进行中按世界刻现算，已完成用快照里的最终用时。 */
    private long displayElapsedTicks() {
        var snapshot = view();
        if (snapshot.isSolved()) return snapshot.elapsedTicks();
        if (snapshot.status() != Status.PLAYING.ordinal() || roundStart <= 0L) return 0L;
        return Math.max(0L, gameTime() - roundStart);
    }

    private void startRound(@Nullable ServerPlayer starter) {
        int size = NumberPuzzleRules.sizeOf(sizeOrdinal);
        int[] generated = NumberPuzzleRules.createBoard(size, random());
        Arrays.fill(board, 0);
        System.arraycopy(generated, 0, board, 0, generated.length);
        blankCell = NumberPuzzleRules.findBlank(board, size);
        moves = 0;
        lastElapsed = 0L;
        roundStart = gameTime();
        status = Status.PLAYING.ordinal();
        beginSession(starter);
        // 开局时刻单独同步一次，计时行才能在客户端本地读秒。
        requestSync();
        syncAndMarkChanged();
    }

    private void ensureStarted() {
        if (status == Status.IDLE.ordinal()) startRound(null);
    }

    /** 服务端：切到指定尺寸并立即开新局；旧局未完成不给币。 */
    private void selectSize(int ordinal, @Nullable ServerPlayer player) {
        if (isRemote() || player == null || !guard(player)) return;
        if (ordinal < 0 || ordinal >= NumberPuzzleRules.SIZE_COUNT || sizeOrdinal == ordinal) return;
        sizeOrdinal = ordinal;
        startRound(player);
        syncAndMarkChanged();
    }

    /** 服务端：重新开始当前尺寸；旧局完成过的话奖励已经发过，不会重复。 */
    private void restartRound(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        startRound(player);
        syncAndMarkChanged();
    }

    private void moveTile(int index, @Nullable ServerPlayer player) {
        if (isRemote() || !isPlaying() || !guard(player)) return;
        int nextBlank = NumberPuzzleRules.move(board, NumberPuzzleRules.sizeOf(sizeOrdinal), blankCell, index);
        if (nextBlank < 0) return;
        blankCell = nextBlank;
        moves++;
        if (NumberPuzzleRules.isSolved(board, NumberPuzzleRules.sizeOf(sizeOrdinal))) finishRound();
        syncAndMarkChanged();
    }

    private void finishRound() {
        status = Status.SOLVED.ordinal();
        lastElapsed = Math.max(1L, gameTime() - roundStart);
        addBalance(NumberPuzzleRules.Difficulty.ALL[sizeOrdinal].reward());
        if (sessionCounts()) {
            var store = milestones();
            if (store != null) {
                store.recordNumberPuzzle(sessionStarter(), sizeOrdinal, lastElapsed, gameTime());
            }
        }
    }

    // ==================== 界面 ====================

    @Override
    public Widget createUIWidget() {
        if (!isRemote()) ensureStarted();
        var page = Form.page();
        page.addChildren(createGameArea(), createStatusPanel());
        return page;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new SizePage());
        sideTabs.attachSubTab(new LeaderboardPage());
    }

    private StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance()));
        status.addLine(LINE_MOVES, () -> number(view().moves()));
        status.addChild(new LiveDurationLine(LINE_TIME, this::displayElapsedTicks));
        status.addChild(TextLine.of(LayoutStyle.AUTO, () -> view().isSolved() ?
                Component.translatable(TEXT_SOLVED, duration(view().elapsedTicks())) :
                Component.translatable(TEXT_PLAYING)).styled());
        var restart = Button.text(77, BUTTON_RESTART);
        restart.setOnServerClick(() -> restartRound(playerOf(restart)));
        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, createWithdrawButton(77)));
        return status;
    }

    /** 盘面区：外层固定，内部按快照尺寸重建网格。 */
    private UIElement createGameArea() {
        var area = panel();
        area.layout(LayoutStyle::alignCenter);
        registerView(area, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
        area.addChild(new SwitchedContent(() -> view().sizeOrdinal(),
                (sizeOrdinal, remote) -> createBoard(sizeOrdinal)));
        return area;
    }

    private UIElement createBoard(int sizeOrdinal) {
        int size = NumberPuzzleRules.sizeOf(sizeOrdinal);
        int width = CELL_SIZE * size;
        var rows = new UIElement().layout(style -> style.alignCenter().width(width));
        for (int row = 0; row < size; row++) {
            var cells = UIElement.row(CELL_SIZE).layout(style -> style.width(width));
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                var cell = Button.of(CELL_SIZE, CELL_SIZE);
                cell.bindClientText(() -> {
                    var snapshot = view();
                    // 快照只带有效格；未下发时是空数组，越界一律当空格。
                    var cellss = snapshot.board();
                    if (index < 0 || index >= cellss.length) return "";
                    int value = cellss[index];
                    return value == 0 ? "" : Integer.toString(value);
                });
                cell.setOnServerClick(() -> moveTile(index, playerOf(cell)));
                cells.addChild(cell);
            }
            rows.addChild(cells);
        }
        return rows;
    }

    /** 难度页：六个尺寸纵向排列，选中的那个两端带绿色 > <，切换后立即开新局。 */
    private final class SizePage extends GamePage {

        private SizePage() {
            super(WidgetIcons.SETTINGS, PAGE_DIFFICULTY);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(allowOthersRow());

            var section = Form.section(SECTION_SIZE);
            for (int ordinal = 0; ordinal < NumberPuzzleRules.SIZE_COUNT; ordinal++) {
                final int index = ordinal;
                var button = choiceButton(120, SIZE_NAMES[index], () -> sizeOrdinal == index);
                button.setHoverTooltips(Component.translatable(TEXT_SIZE_REWARD,
                        NumberPuzzleRules.Difficulty.ALL[index].reward()));
                button.setOnServerClick(() -> selectSize(index, playerOf(button)));
                section.addChild(button);
            }
            page.addChild(section);

            page.addChild(balancePanel());
            return page;
        }
    }

    /** 排行榜页：顶上一排尺寸选项，下面只显示选中的那张表。 */
    private final class LeaderboardPage extends LeaderboardTabPage {

        private LeaderboardPage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected int boardCount() {
            return NumberPuzzleRules.SIZE_COUNT;
        }

        @Override
        protected Component boardName(int index) {
            return SIZE_NAMES[index];
        }

        @Override
        protected Widget boardContent(int index) {
            var section = Form.section(SIZE_NAMES[index]);
            section.addChild(milestoneTable("game_milestones_number_puzzle_" + index, LEADERBOARD_COLUMNS,
                    (store, server) -> buildLeaderboardRows(store, server, index)));
            return section;
        }
    }

    /** 单个尺寸的榜单行：玩家 + 最快用时，悬停显示达成时刻。 */
    private static List<MilestoneCell[]> buildLeaderboardRows(GameMilestones store, MinecraftServer server,
                                                              int sizeOrdinal) {
        var stats = store.numberPuzzleBoard(sizeOrdinal);
        var rows = new ObjectArrayList<MilestoneCell[]>(stats.size());
        for (var stat : stats) {
            long ticks = stat.ticks()[sizeOrdinal];
            rows.add(new MilestoneCell[] {
                    MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.achieved(duration(ticks), stat.times()[sizeOrdinal]) });
        }
        return rows;
    }
}

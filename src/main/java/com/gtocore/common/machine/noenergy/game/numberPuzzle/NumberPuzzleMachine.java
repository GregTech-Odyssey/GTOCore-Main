package com.gtocore.common.machine.noenergy.game.numberPuzzle;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
import com.gtocore.common.machine.noenergy.game.milestone.GameMilestones;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.elements.SwitchedContent;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 数字华容道游戏机：3×3 到 8×8 六档，点击空格相邻的数字块滑动，按行排好即完成。
 * 完成一局按尺寸发放游戏币，并把最快完成时间记入该尺寸的里程碑。
 */
@DataGeneratorScanned
public class NumberPuzzleMachine extends AbstractGameMachine<NumberPuzzleMachine.BoardSnapshot> {

    private static final int CELL_SIZE = 20;

    @RegisterLanguage(cn = "3×3", en = "3×3")
    private static final String SIZE_3 = "gtocore.machine.number_puzzle.size.3";
    @RegisterLanguage(cn = "4×4", en = "4×4")
    private static final String SIZE_4 = "gtocore.machine.number_puzzle.size.4";
    @RegisterLanguage(cn = "5×5", en = "5×5")
    private static final String SIZE_5 = "gtocore.machine.number_puzzle.size.5";
    @RegisterLanguage(cn = "6×6", en = "6×6")
    private static final String SIZE_6 = "gtocore.machine.number_puzzle.size.6";
    @RegisterLanguage(cn = "7×7", en = "7×7")
    private static final String SIZE_7 = "gtocore.machine.number_puzzle.size.7";
    @RegisterLanguage(cn = "8×8", en = "8×8")
    private static final String SIZE_8 = "gtocore.machine.number_puzzle.size.8";
    private static final String[] SIZE_NAMES = { SIZE_3, SIZE_4, SIZE_5, SIZE_6, SIZE_7, SIZE_8 };

    @RegisterLanguage(cn = "尺寸", en = "Size")
    private static final String SECTION_SIZE = "gtocore.machine.number_puzzle.section.size";
    @RegisterLanguage(cn = "尺寸", en = "Sizes")
    private static final String PAGE_SIZE = "gtocore.machine.number_puzzle.page.sizes";
    @RegisterLanguage(cn = "数字华容道尺寸", en = "Number puzzle sizes")
    private static final String PAGE_SIZE_TOOLTIP = "gtocore.machine.number_puzzle.page.sizes.tooltip";
    @RegisterLanguage(cn = "排行榜", en = "Leaderboard")
    private static final String PAGE_LEADERBOARD = "gtocore.machine.number_puzzle.page.leaderboard";
    @RegisterLanguage(cn = "数字华容道排行榜", en = "Number puzzle leaderboard")
    private static final String PAGE_LEADERBOARD_TOOLTIP = "gtocore.machine.number_puzzle.page.leaderboard.tooltip";
    @RegisterLanguage(cn = "步数", en = "Moves")
    private static final String LINE_MOVES = "gtocore.machine.number_puzzle.line.moves";
    @RegisterLanguage(cn = "用时", en = "Time")
    private static final String LINE_TIME = "gtocore.machine.number_puzzle.line.time";
    @RegisterLanguage(cn = "进行中", en = "Playing")
    private static final String TEXT_PLAYING = "gtocore.machine.number_puzzle.text.playing";
    @RegisterLanguage(cn = "完成！用时 %s", en = "Solved! %s")
    private static final String TEXT_SOLVED = "gtocore.machine.number_puzzle.text.solved";
    @RegisterLanguage(cn = "完成奖励：%s 游戏币", en = "Clear reward: %s Game Coins")
    private static final String TEXT_SIZE_REWARD = "gtocore.machine.number_puzzle.text.size_reward";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.number_puzzle.button.restart";

    /** 单个尺寸的排行榜只有两列：玩家与最快用时。 */
    private static final MilestoneColumn[] LEADERBOARD_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 64),
            MilestoneColumn.center(COLUMN_DURATION, 54) };

    private enum Status {
        IDLE,
        PLAYING,
        SOLVED
    }

    public record BoardSnapshot(int revision, int sizeOrdinal, int[] board, int moves, long elapsedTicks, int status) {

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, 0, new int[NumberPuzzleRules.CELL_CAPACITY],
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

    /** 当前尺寸档；基础值为 3×3。 */
    @SaveToDisk
    private int sizeOrdinal = NumberPuzzleRules.Difficulty.THREE.ordinal();
    /** 固定长度盘面，前 size² 格有效；0 为空格。 */
    @SaveToDisk
    private int[] board = new int[NumberPuzzleRules.CELL_CAPACITY];
    @SaveToDisk
    private int blankCell;
    @SaveToDisk
    private int moves;
    @SaveToDisk
    private long roundStart;
    @SaveToDisk
    private long lastElapsed;
    @SaveToDisk
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
            if (status == Status.PLAYING.ordinal() && roundStart <= 0L) roundStart = gameTime();
            if (status == Status.SOLVED.ordinal()) lastElapsed = Math.max(1L, lastElapsed);
        }
    }

    @Override
    protected BoardSnapshot createView() {
        return new BoardSnapshot(revision(), sizeOrdinal, board, moves, elapsedTicks(), status);
    }

    private boolean isPlaying() {
        return status == Status.PLAYING.ordinal();
    }

    private long elapsedTicks() {
        if (status == Status.SOLVED.ordinal()) return lastElapsed;
        if (status != Status.PLAYING.ordinal() || roundStart <= 0L) return 0L;
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
        syncAndMarkChanged();
    }

    private void ensureStarted() {
        if (status == Status.IDLE.ordinal()) startRound(null);
    }

    /** 服务端：结掉旧局（未完成不给币）并换到指定尺寸。 */
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
        status.addLine(LINE_TIME, () -> duration(view().elapsedTicks()));
        status.addChild(TextLine.of(LayoutStyle.AUTO, () -> view().isSolved() ?
                Component.translatable(TEXT_SOLVED, duration(view().elapsedTicks())) :
                Component.translatable(TEXT_PLAYING)).styled());
        var restart = Button.translatable(77, BUTTON_RESTART);
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
                    int cellSize = NumberPuzzleRules.sizeOf(snapshot.sizeOrdinal());
                    if (index >= cellSize * cellSize) return "";
                    int value = snapshot.board()[index];
                    return value == 0 ? "" : Integer.toString(value);
                });
                cell.setOnServerClick(() -> moveTile(index, playerOf(cell)));
                cells.addChild(cell);
            }
            rows.addChild(cells);
        }
        return rows;
    }

    private static StatusPanel panel() {
        var panel = new StatusPanel();
        panel.getLayoutStyle().gapAll(2);
        return panel;
    }

    /** 尺寸页：六个难度纵向排列，切换后立即开新局。 */
    private final class SizePage implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(Form.controlRow(LINE_ALLOW_OTHERS,
                    Switch.of(NumberPuzzleMachine.this::allowOthers, NumberPuzzleMachine.this::setAllowOthers),
                    TOOLTIP_ALLOW_OTHERS));

            var section = Form.section(SECTION_SIZE);
            for (int ordinal = 0; ordinal < NumberPuzzleRules.SIZE_COUNT; ordinal++) {
                final int index = ordinal;
                var button = Button.translatable(120, SIZE_NAMES[ordinal]);
                button.setSelected(() -> view().sizeOrdinal() == index);
                button.setHoverTooltips(Component.translatable(TEXT_SIZE_REWARD,
                        NumberPuzzleRules.Difficulty.ALL[ordinal].reward()));
                button.setOnServerClick(() -> selectSize(index, playerOf(button)));
                section.addChild(button);
            }
            page.addChild(section);

            var balancePanel = panel();
            balancePanel.addLine(LINE_BALANCE, () -> number(balance()));
            page.addChild(balancePanel);
            return page;
        }

        @Override
        public IGuiTexture getTabIcon() {
            return WidgetIcons.SETTINGS;
        }

        @Override
        public Component getTitle() {
            return Component.translatable(PAGE_SIZE);
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable(PAGE_SIZE_TOOLTIP));
        }

        @Override
        public boolean hasPlayerInventory() {
            return false;
        }
    }

    /** 排行榜页：六档尺寸各一张两列表，按两列三排摆放。 */
    private final class LeaderboardPage implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
                var row = new UIElement().layout(style -> style.row().gapAll(UISizes.SECTION_GAP));
                for (int col = 0; col < 2; col++) {
                    int ordinal = rowIndex * 2 + col;
                    var section = Form.section(SIZE_NAMES[ordinal]);
                    section.addChild(milestoneTable("game_milestones_number_puzzle_" + ordinal,
                            LEADERBOARD_COLUMNS,
                            (store, server) -> buildLeaderboardRows(store, server, ordinal)));
                    row.addChild(section);
                }
                page.addChild(row);
            }

            var balancePanel = panel();
            balancePanel.addLine(LINE_BALANCE, () -> number(balance()));
            page.addChild(balancePanel);
            return page;
        }

        @Override
        public IGuiTexture getTabIcon() {
            return WidgetIcons.INFO;
        }

        @Override
        public Component getTitle() {
            return Component.translatable(PAGE_LEADERBOARD);
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable(PAGE_LEADERBOARD_TOOLTIP));
        }

        @Override
        public boolean hasPlayerInventory() {
            return false;
        }
    }

    /** 单个尺寸的排行榜：玩家 + 最快用时，用时悬停显示达成时刻。 */
    private List<MilestoneCell[]> buildLeaderboardRows(GameMilestones store, MinecraftServer server,
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

package com.gtocore.common.machine.noenergy.game.justGet10;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
import com.gtocore.common.machine.noenergy.game.GameBoardCatalog;
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
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 「合成十」游戏机：5×5 盘面的合并玩法。
 *
 * <p>
 * 一局攒积分，盘面无相邻同数字时结账、按兑现基线折成游戏币，「重新开始」提前结账重开；余额、取出与快照通道来自
 * {@link AbstractGameMachine}。每个数字格是 {@link Cell}（定尺寸外壳 + {@link CellButton}），盘面随界面通道下发。
 */
@DataGeneratorScanned
public class JustGet10Machine extends AbstractGameMachine<JustGet10Machine.BoardSnapshot> {

    @RegisterLanguage(cn = "本局积分", en = "Current score")
    private static final Component LINE_SCORE = Component.translatable("gtocore.machine.game.line.score");
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final Component BUTTON_RESTART = Component.translatable("gtocore.machine.game.button.restart");

    /** 里程碑表格的列（名次由表格自动加在最前）：玩家、最高分与最大合成，达成时间放在成绩格的悬停提示里。 */
    private static final MilestoneColumn[] MILESTONE_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 42),
            MilestoneColumn.right(COLUMN_BEST_SCORE, 54),
            MilestoneColumn.right(COLUMN_BEST_MERGED, 40) };

    /** 「全部榜单」页里的合成十榜：列与行和本机排行榜页完全一致。 */
    public static GameBoardCatalog.Board leaderboardBoard() {
        return GameBoardCatalog.board(MILESTONE_COLUMNS, server -> GameMilestones.get(server).modCount(),
                (server, first, second) -> buildMilestoneRows(GameMilestones.get(server), server));
    }

    /** 格子边长：数字材质是 32×32，这里缩成 24 画。 */
    private static final int CELL_SIZE = 24;

    /**
     * 额外分：一局里 {@link #SCORE_BONUS_MIN_VALUE}~{@link JustGet10Rules#VALUE_MAX} 的每个数字<b>首次</b>合出时
     * 各给一次，金额随数字递增；水位记在 {@link #maxValue} 上。
     */
    private static final int SCORE_BONUS_MIN_VALUE = 10;
    private static final int SCORE_BONUS_BASE = 1000;
    private static final int SCORE_BONUS_PER_VALUE = 500;
    /** 兑现门槛：一局积分超过它的部分才按比例折成游戏币，达不到一枚不给；公开给工具提示复用同一份数值。 */
    public static final int REDEMPTION_BASELINE = 1000;

    /** 提起位图：一个 {@code long} 装 {@link Long#SIZE} 格，25 格一个字够用。 */
    private static final int SELECTION_WORDS = (JustGet10Rules.CELL_COUNT + Long.SIZE - 1) / Long.SIZE;
    private static final long[] NO_SELECTION = new long[0];
    /** 「没有盘面」用空数组而不是 null；首次打开界面时会立刻换成随机盘面。 */
    private static final int[] NO_BOARD = new int[0];

    /** 数字材质：下标是数字，1~20 各有一张普通与一张「已提起」的图。 */
    private static final IGuiTexture[] NUMBER_TEXTURES = new IGuiTexture[JustGet10Rules.VALUE_MAX + 1];
    private static final IGuiTexture[] NUMBER_TEXTURES_PRESSED = new IGuiTexture[JustGet10Rules.VALUE_MAX + 1];

    static {
        String path = "gtocore:textures/gui/game/just_get_10/just_get_10_display_";
        for (int value = JustGet10Rules.VALUE_MIN; value <= JustGet10Rules.VALUE_MAX; value++) {
            NUMBER_TEXTURES[value] = new ResourceTexture(path + value + ".png");
            NUMBER_TEXTURES_PRESSED[value] = new ResourceTexture(path + value + "_highlighted.png");
        }
    }

    @SaveToDisk(defaultValue = "0")
    @SyncToClient(scheduleUpdate = true)
    private int score;
    /** 局面阶段；不进快照，界面按盘面自行判断能不能动。 */
    @SaveToDisk(defaultValue = "0")
    private int gameStatus = Status.IDLE.ordinal();
    /** 本局是否已结过账；不存盘，读档时按局面还原（已结束的那局当时就结过账）。 */
    private boolean settled;

    /** 局面阶段；GAME_OVER 的那一局已经兑现过。 */
    private enum Status {
        IDLE,
        PLAYING,
        GAME_OVER
    }

    /** 规则实例；临时容器跟着它复用。 */
    private final JustGet10Rules rules = JustGet10Rules.create();

    /** 服务端权威盘面，按行优先存 {@code size * size} 格，空格为 -1。 */
    @SaveToDisk
    private int[] board = NO_BOARD;
    /** 本局到过的最大数字，也是额外分的水位。 */
    @SaveToDisk(defaultValue = "1")
    @SyncToClient(scheduleUpdate = true)
    private int maxValue = JustGet10Rules.VALUE_MIN;
    /** 已提起的格子位图：字 = 格号 / 64，位 = 格号 % 64；一次提起的是点中那格所在的整个连通组。 */
    @SaveToDisk
    private long[] selection = NO_SELECTION;
    /** 本局合成出的最大数字：结账时拿去比里程碑。 */
    @SaveToDisk(defaultValue = "1")
    private int roundPeakValue = JustGet10Rules.VALUE_MIN;

    public JustGet10Machine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** 通道里下发的盘面；相等性只看版本号，省掉逐元素比格子。 */
    public record BoardSnapshot(int revision, int[] board, long[] selection) {

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, NO_BOARD, NO_SELECTION);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::board,
                ByteBufCodecs.VAR_LONG_ARRAY, BoardSnapshot::selection,
                BoardSnapshot::new);

        /** 第 {@code cell} 格是否已被提起。 */
        boolean isSelected(int cell) {
            return JustGet10Machine.isSelected(selection, cell);
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

    // ==================== 局面 ====================

    /** 读档后的收尾：盘面尺寸与存档对不上就作废重来，其余数值夹回合法范围。只在服务端调用。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    /**
     * 先夹回合法的局面阶段再整理盘面；盘面尺寸对不上（旧存档）本局作废、等下次打开界面重发，其余数值夹回合法范围。
     */
    private void sanitizeState() {
        if (gameStatus < 0 || gameStatus >= Status.values().length) setIdle();
        // 已结束的那一局在结束时就已经兑现过，读档后不能再结一次账
        settled = isGameOver();
        maxValue = Math.clamp(maxValue, JustGet10Rules.VALUE_MIN, JustGet10Rules.VALUE_MAX);
        if (selection.length != SELECTION_WORDS) selection = NO_SELECTION;
        if (board.length != JustGet10Rules.CELL_COUNT) {
            board = NO_BOARD;
            score = 0;
            maxValue = JustGet10Rules.VALUE_MIN;
            selection = NO_SELECTION;
            setIdle();
            return;
        }
        for (int cell = 0; cell < board.length; cell++) {
            board[cell] = Math.clamp(board[cell], JustGet10Rules.VALUE_MIN, JustGet10Rules.VALUE_MAX);
        }
    }

    @Override
    protected BoardSnapshot createView() {
        return new BoardSnapshot(revision(), board, selection);
    }

    private boolean isPlaying() {
        return gameStatus == Status.PLAYING.ordinal();
    }

    private boolean isGameOver() {
        return gameStatus == Status.GAME_OVER.ordinal();
    }

    private void setIdle() {
        gameStatus = Status.IDLE.ordinal();
    }

    /** 发一局新牌：按当前规则把盘面铺出来。 */
    private void dealBoard() {
        board = rules.createBoard(random());
        maxValue = JustGet10Rules.maxValue(board);
        selection = NO_SELECTION;
    }

    /** 服务端：清零本局成绩、进入进行中，并重新打开「可以结账」的开关。 */
    private void startRound() {
        dealBoard();
        score = 0;
        settled = false;
        roundPeakValue = JustGet10Rules.maxValue(board);
        gameStatus = Status.PLAYING.ordinal();
        beginSession(null);
    }

    /**
     * 把本局积分折成游戏币入余额；{@link #settled} 保证同一局只结一次账（结束、重新开始、拆机器都走这里）。
     * 没超过兑现基线时同样算结过账，只是给 0 枚。
     */
    @Override
    protected void cashOut() {
        if (settled) return;
        settled = true;
        long reward = (score - REDEMPTION_BASELINE) / JustGet10Rules.POINTS_PER_COIN;
        if (reward > 0) addBalance(reward);
        recordRound();
    }

    /** 结账时记一次里程碑：只有单人局、且分数超过里程碑门槛才写，并且只更新更好的分数/最大合成。 */
    private void recordRound() {
        if (!sessionCounts()) return;
        var server = server();
        if (server == null) return;
        GameMilestones.get(server).recordScore(GameMilestones.JUST_GET_10, sessionStarter(), score, roundPeakValue,
                gameTime());
    }

    /** 玩不动了：先结账再置为结束，{@link #settled} 兜住重复结账。 */
    private void endRound() {
        cashOut();
        gameStatus = Status.GAME_OVER.ordinal();
    }

    /** 服务端：结掉当前这一局——兑现本局积分并回到「没开局」，成绩清零。 */
    private void settleRound() {
        cashOut();
        score = 0;
        setIdle();
    }

    /** 服务端：「重新开始」：结掉当前这一局（没有在玩的一局就只结 0 分）再开一局。 */
    private void restartRoundOnServer(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        settleRound();
        startRound();
        beginSession(player);
        syncAndMarkChanged();
    }

    /** 首次打开界面时把没开的那一局开起来。 */
    private void ensureStarted() {
        if (gameStatus == Status.IDLE.ordinal()) startRound();
    }

    // ==================== 服务端动作 ====================

    /**
     * 落子（{@code index} 就是按钮编号）：未提起的格子先提起 {@code index} 所在的同数字连通组，不成组就清空提起；
     * 已提起则合并成「原数字 + 1」并整列下坠补格，再结算分数与额外分。合并会清掉所有提起（盘面换了位置，旧高亮
     * 会指错格子）；不可合并时状态不变。
     */
    private void clickCell(int index, @Nullable ServerPlayer player) {
        if (isRemote() || !isPlaying() || !guard(player)) return;
        int size = rules.size();
        if (index < 0 || index >= board.length) {
            selection = NO_SELECTION;
            syncAndMarkChanged();
            return;
        }
        if (!isSelected(selection, index)) {
            var group = rules.groupAt(board, index / size, index % size);
            selection = group == null ? NO_SELECTION : maskOf(group);
        } else {
            int row = index / size;
            int col = index % size;
            var group = rules.groupAt(board, row, col);
            if (group == null) return;
            var level = getLevel();
            if (level == null) return;
            var merge = rules.merge(board, group, row, col, level.getRandom(), score);
            if (merge == null) return;
            int mergedValue = merge.mergedValue();
            roundPeakValue = Math.max(roundPeakValue, mergedValue);
            int gained = mergedValue * group.size();
            board = merge.board();
            int reachedValue = Math.min(JustGet10Rules.VALUE_MAX, JustGet10Rules.maxValue(board));
            if (reachedValue > maxValue) {
                for (int value = Math.max(maxValue + 1, SCORE_BONUS_MIN_VALUE); value <= reachedValue; value++) {
                    gained += SCORE_BONUS_BASE + (value - SCORE_BONUS_MIN_VALUE) * SCORE_BONUS_PER_VALUE;
                }
                maxValue = reachedValue;
            }
            score = (int) Math.min(Integer.MAX_VALUE, (long) score + gained);
            selection = NO_SELECTION;
            if (rules.isGameOver(board)) endRound();
        }
        syncAndMarkChanged();
    }

    /** 提起位图：字 = 格号 / {@link Long#SIZE}，位 = 格号 % 64；越界或没这两个字时算未提起。 */
    private static boolean isSelected(long[] selection, int cell) {
        int word = cell >>> 6;
        return word < selection.length && (selection[word] & (1L << (cell & 63))) != 0;
    }

    /** 把一整组格号打成位图。 */
    private static long[] maskOf(IntArrayList group) {
        long[] mask = new long[SELECTION_WORDS];
        for (int index = 0; index < group.size(); index++) {
            int cell = group.getInt(index);
            mask[cell >>> 6] |= 1L << (cell & 63);
        }
        return mask;
    }

    // ==================== 界面 ====================

    /** 主页 = 盘面区 + 状态面板；盘面在左、状态在右的顺序两端必须一致，通道里的条目编号才对得上。 */
    @Override
    public Widget createUIWidget() {
        if (!isRemote()) ensureStarted();

        var page = Form.page();
        page.addChildren(createGameArea(), createStatusPanel());
        return page;
    }

    /** 主页之外挂「配置」与「排行榜」两个子页。 */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new ConfigPage());
        sideTabs.attachSubTab(new LeaderboardPage());
    }

    /** 状态面板：余额 + 本局积分 + 重新开始/取出全部。 */
    private StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance()));
        status.addLine(LINE_SCORE, () -> number(score));
        var restart = Button.text(77, BUTTON_RESTART);
        restart.setOnServerClick(() -> restartRoundOnServer(playerOf(restart)));
        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, createWithdrawButton(77)));
        return status;
    }

    /** 配置页：游玩权限开关 + 余额；排行榜单独一页。 */
    private final class ConfigPage extends GamePage {

        private ConfigPage() {
            super(WidgetIcons.SETTINGS, PAGE_SETTINGS);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(allowOthersRow());
            page.addChild(balancePanel());
            return page;
        }
    }

    /** 排行榜页：最高分榜（文字由服务端整表下发）。 */
    private final class LeaderboardPage extends SingleLeaderboardPage {

        private LeaderboardPage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected Widget boardContent() {
            var section = Form.section(SECTION_MILESTONES);
            section.addChild(milestoneTable("game_milestones_just_get_10",
                    MILESTONE_COLUMNS, JustGet10Machine::buildMilestoneRows));
            return section;
        }
    }

    /** 里程碑表格的行（服务端构建）：最高分榜，一人一行，名次由表格补，达成时间进悬停提示。 */
    private static List<MilestoneCell[]> buildMilestoneRows(GameMilestones store, MinecraftServer server) {
        var board = store.justGet10Board();
        var rows = new ObjectArrayList<MilestoneCell[]>(board.size());
        for (var stat : board) {
            rows.add(new MilestoneCell[] { MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.achieved(number(stat.bestScore()), stat.bestScoreTime()),
                    MilestoneCell.achieved(tileName(stat.bestMerged()), stat.bestMergedTime()) });
        }
        return rows;
    }

    /** 最大合成按电压等级名显示：1 → ULV、15 → MAX，更高的按 MAX+1、MAX+2 往后排（上限夹到 VALUE_MAX）。 */
    private static Component tileName(int value) {
        if (value < JustGet10Rules.VALUE_MIN) return Component.literal("-");
        return voltageName(Math.min(value, JustGet10Rules.VALUE_MAX) - 1);
    }

    /** 盘面区：外层面板当底并把盘面居中，盘面本体是 {@link #createBoard()}。 */
    private UIElement createGameArea() {
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChild(createBoard());
    }

    /** 按 {@link JustGet10Rules#SIZE} 铺格子，盘面快照挂在这一层上。 */
    private UIElement createBoard() {
        int width = CELL_SIZE * JustGet10Rules.SIZE;
        var boardRows = new UIElement().layout(style -> style.alignCenter().width(width));
        registerView(boardRows, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
        for (int row = 0; row < JustGet10Rules.SIZE; row++) {
            var cells = UIElement.row(CELL_SIZE).layout(style -> style.width(width));
            for (int col = 0; col < JustGet10Rules.SIZE; col++) {
                cells.addChild(new Cell(CELL_SIZE, row * JustGet10Rules.SIZE + col));
            }
            boardRows.addChild(cells);
        }
        return boardRows;
    }

    /** 数字格：定尺寸外壳，里面才是按钮；格号与尺寸定死，按钮不参与外层行的布局。 */
    private final class Cell extends UIElement {

        private Cell(int cellSize, int index) {
            layout(style -> style.size(cellSize, cellSize));
            addChild(new CellButton(cellSize, index));
        }
    }

    /**
     * 按钮本体：整格就是一张数字材质，提起时换成高亮的那套；不画底图、文字或禁用态。
     *
     * <p>
     * 一律不禁用：格值每帧现读同步快照，能不能动由服务端处理点击时判定，界面不会出现禁止操作的斜纹。
     */
    private final class CellButton extends Button {

        private final int cellSize;
        private final int index;

        private CellButton(int cellSize, int index) {
            super(cellSize, cellSize, null, null);
            this.cellSize = cellSize;
            this.index = index;
            setOnServerClick(() -> clickCell(index, playerOf(this)));
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var snapshot = view();
            int[] current = snapshot.board();
            int value = index < current.length ? current[index] : -1;
            if (value < JustGet10Rules.VALUE_MIN) return;
            int x = getPositionX();
            int y = getPositionY();
            var textures = snapshot.isSelected(index) ? NUMBER_TEXTURES_PRESSED : NUMBER_TEXTURES;
            textures[Math.min(value, JustGet10Rules.VALUE_MAX)].draw(graphics, mouseX, mouseY, x, y, cellSize, cellSize);
            if (isMouseOverElement(mouseX, mouseY)) UIDraw.hoverOverlay(graphics, x, y, cellSize, cellSize);
        }
    }
}

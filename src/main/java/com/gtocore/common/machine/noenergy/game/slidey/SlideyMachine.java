package com.gtocore.common.machine.noenergy.game.slidey;

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
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 土耳其方块游戏机：9×10 盘面，每一块是 1~4 格的横向条状方块，只能在本行左右滑动，整块一起下落，填满整行即消除。
 *
 * <p>
 * 每次横向移动算一个回合，全部在玩家松手的那一次操作里同步算完：移动 → 悬空方块整块落到底 →
 * 满行消除计分 → 底部补一行、盘面整体上移；没有服务端 tick，也不存在「半个回合」的中间状态。
 *
 * <p>
 * 最高分与累计消除行数记进 {@link GameMilestones}，并汇总到机器的排行榜页与 {@code /gtocore leaderboard} 聚合榜。
 */
@DataGeneratorScanned
public final class SlideyMachine extends AbstractGameMachine<SlideyMachine.BoardSnapshot> {

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PLAYING = 1;
    private static final int STATUS_GAME_OVER = 2;

    /** 每消一行的基础分；连击倍率见 {@link #COMBO_CAP}。 */
    public static final int LINE_SCORE = 100;
    /** 连击倍率上限。 */
    public static final int COMBO_CAP = 5;
    /** 兑现基线：一局超过它的部分才折成游戏币。 */
    public static final int REDEMPTION_BASELINE = 1000;
    public static final int POINTS_PER_COIN = 100;

    private static final int CELL_SIZE = 17;
    private static final int GRID = 1;
    private static final int CELL_PITCH = CELL_SIZE + GRID;
    private static final int BOARD_WIDTH = SlideyRules.COLS * CELL_PITCH + GRID;
    private static final int BOARD_HEIGHT = SlideyRules.ROWS * CELL_PITCH + GRID;
    private static final int VIEW_WIDTH = BOARD_WIDTH;
    private static final int VIEW_HEIGHT = BOARD_HEIGHT;

    private static final int COLOR_BOARD = 0xFFF3F6FA;
    private static final int COLOR_GRID = 0xFFC6CED9;
    private static final int COLOR_BORDER = 0xFF7D8798;
    private static final int COLOR_HIGHLIGHT = 0x40FFFFFF;
    private static final int COLOR_SHADOW = 0x24000000;
    private static final int COLOR_HOVER = 0xCC2E86DE;
    private static final int COLOR_OVERLAY = 0xB8FFFFFF;
    private static final int COLOR_OVERLAY_TEXT = 0xFF2A3140;
    /** 方块颜色，下标是颜色值 - 1。 */
    private static final int[] BLOCK_COLORS = {
            0xFF4FD8FF, 0xFFFFD54F, 0xFFB388FF, 0xFF69F0AE, 0xFFFF5252, 0xFF448AFF };

    @RegisterLanguage(cn = "本局得分", en = "Score")
    private static final Component LINE_SCORE_TEXT = Component.translatable("gtocore.machine.slidey.line.score");
    @RegisterLanguage(cn = "按住一块方块左右拖动，松手结算",
                      en = "Press a block and drag it sideways; release to finish the move")
    private static final Component LINE_CONTROLS = Component.translatable("gtocore.machine.slidey.line.controls");
    @RegisterLanguage(cn = "每次移动底部都会补一行；填满整行即可消除，连击越高每行得分越多",
                      en = "Every move adds a row at the bottom; fill a whole row to clear it, and combos raise the score")
    private static final Component LINE_RULES = Component.translatable("gtocore.machine.slidey.line.rules");

    @RegisterLanguage(cn = "游戏结束", en = "Game over")
    private static final Component TEXT_GAME_OVER = Component.translatable("gtocore.machine.slidey.text.game_over");

    private static final MilestoneColumn[] MILESTONE_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 42),
            MilestoneColumn.right(COLUMN_BEST_SCORE, 52),
            MilestoneColumn.right(COLUMN_LINES, 46) };

    /** 方块列表：每块 {@link SlideyRules#STRIDE} 个 int（行、列、长度、颜色）。 */
    @SaveToDisk
    private int[] blocks = new int[SlideyRules.MAX_BLOCKS * SlideyRules.STRIDE];
    @SaveToDisk(defaultValue = "0")
    private int blockCount;
    /** 已经涨过多少行；每涨 {@link SlideyRules#RAMP_EVERY} 行难度提升一档。 */
    @SaveToDisk(defaultValue = "0")
    private int tideCount;
    @SaveToDisk(defaultValue = "0")
    private int score;
    @SaveToDisk(defaultValue = "0")
    private int lines;
    @SaveToDisk(defaultValue = "1")
    private int streak = 1;
    @SaveToDisk(defaultValue = "0")
    private int status = STATUS_IDLE;

    /** 本回合是否消过行；用来结算连击，不存盘。 */
    private boolean turnCleared;
    /** 这一局是否已结过账；不存盘。 */
    private boolean settled;

    public SlideyMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** 下发的界面快照：方块列表、分数与阶段；相等性只看版本号。 */
    public record BoardSnapshot(int revision, int[] blocks, int blockCount, int score, int lines, int streak, int status,
                                int highScore, int bestLines) {

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, new int[0], 0, 0, 0, 1, STATUS_IDLE, 0, 0);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::blocks,
                ByteBufCodecs.VAR_INT, BoardSnapshot::blockCount,
                ByteBufCodecs.VAR_INT, BoardSnapshot::score,
                ByteBufCodecs.VAR_INT, BoardSnapshot::lines,
                ByteBufCodecs.VAR_INT, BoardSnapshot::streak,
                ByteBufCodecs.VAR_INT, BoardSnapshot::status,
                ByteBufCodecs.VAR_INT, BoardSnapshot::highScore,
                ByteBufCodecs.VAR_INT, BoardSnapshot::bestLines,
                BoardSnapshot::new);

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

    /** 读档后收尾：夹回合法值；旧档里若有悬空方块，直接落到底并清掉满行。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    private void sanitizeState() {
        if (blocks == null || blocks.length != SlideyRules.MAX_BLOCKS * SlideyRules.STRIDE) {
            blocks = new int[SlideyRules.MAX_BLOCKS * SlideyRules.STRIDE];
            resetRound();
            return;
        }
        blockCount = sanitizeBlocks();
        score = Math.max(0, score);
        lines = Math.max(0, lines);
        tideCount = Math.max(0, tideCount);
        streak = Math.clamp(streak, 1, COMBO_CAP);
        status = Math.clamp(status, STATUS_IDLE, STATUS_GAME_OVER);
        while (SlideyRules.stepGravity(blocks, blockCount)) {
            // 旧档可能停在下落途中：直接落到底
        }
        blockCount = SlideyRules.clearFullRows(blocks, blockCount)[0];
        settled = status == STATUS_GAME_OVER;
        turnCleared = false;
    }

    /** 丢掉越界或重叠的方块，把超过 {@link SlideyRules#MAX_BLOCK_LENGTH} 的旧方块截短；返回剩下的数量。 */
    private int sanitizeBlocks() {
        int write = 0;
        for (int index = 0; index < Math.min(blockCount, SlideyRules.MAX_BLOCKS); index++) {
            int base = index * SlideyRules.STRIDE;
            int row = blocks[base + SlideyRules.FIELD_ROW];
            int column = blocks[base + SlideyRules.FIELD_COL];
            int length = Math.min(blocks[base + SlideyRules.FIELD_LENGTH], SlideyRules.MAX_BLOCK_LENGTH);
            int color = blocks[base + SlideyRules.FIELD_COLOR];
            if (row < 0 || row >= SlideyRules.ROWS) continue;
            if (column < 0 || length <= 0 || column + length > SlideyRules.COLS) continue;
            if (color <= 0 || color > SlideyRules.COLOR_COUNT) continue;
            boolean overlaps = false;
            for (int other = 0; other < write && !overlaps; other++) {
                int otherBase = other * SlideyRules.STRIDE;
                if (blocks[otherBase + SlideyRules.FIELD_ROW] != row) continue;
                int otherStart = blocks[otherBase + SlideyRules.FIELD_COL];
                int otherEnd = otherStart + blocks[otherBase + SlideyRules.FIELD_LENGTH];
                overlaps = column < otherEnd && otherStart < column + length;
            }
            if (overlaps) continue;
            if (write != index) System.arraycopy(blocks, base, blocks, write * SlideyRules.STRIDE, SlideyRules.STRIDE);
            blocks[write * SlideyRules.STRIDE + SlideyRules.FIELD_LENGTH] = length;
            write++;
        }
        return write;
    }

    /** 清空一整局，回到「还没开局」；读档坏档与开局都走这里。 */
    private void resetRound() {
        Arrays.fill(blocks, 0);
        blockCount = 0;
        tideCount = 0;
        score = 0;
        lines = 0;
        streak = 1;
        turnCleared = false;
        status = STATUS_IDLE;
        settled = false;
    }

    @Override
    protected BoardSnapshot createView() {
        var server = server();
        var milestones = server == null ? null : GameMilestones.get(server);
        int highScore = milestones == null ? 0 : milestones.slideyBest(viewerId());
        int bestLines = milestones == null ? 0 : milestones.slideyBestLines(viewerId());
        return new BoardSnapshot(revision(), Arrays.copyOf(blocks, blockCount * SlideyRules.STRIDE), blockCount, score,
                lines, streak, status, highScore, bestLines);
    }

    @Override
    protected void onViewerChanged() {
        if (!isRemote() && getLevel() != null) syncAndMarkChanged();
    }

    // ==================== 界面打开 ====================

    /** 界面打开时：没开局或已结束就开新的一局。 */
    @Override
    public ModularUI createUI(Player player) {
        var ui = super.createUI(player);
        if (!isRemote() && player instanceof ServerPlayer) ensureStarted();
        return ui;
    }

    private void ensureStarted() {
        if (status == STATUS_IDLE || status == STATUS_GAME_OVER) startGame(null);
    }

    private void startGame(@Nullable ServerPlayer starter) {
        for (int attempt = 0; attempt < 32; attempt++) {
            blockCount = SlideyRules.fillStartRows(blocks, random());
            if (SlideyRules.hasAnyMove(blocks, blockCount)) break;
        }
        if (!SlideyRules.hasAnyMove(blocks, blockCount)) {
            // 极端情况下随机出来的行都动不了：清空后只补一行，保证一定能滑。
            Arrays.fill(blocks, 0);
            blockCount = SlideyRules.appendGeneratedRow(blocks, 0, SlideyRules.ROWS - 1, random(), 0);
        }
        tideCount = 0;
        score = 0;
        lines = 0;
        streak = 1;
        turnCleared = false;
        settled = false;
        beginSession(starter);
        status = STATUS_PLAYING;
        syncAndMarkChanged();
    }

    private void restartGame(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        cashOut();
        startGame(player);
    }

    // ==================== 回合 ====================

    /**
     * 一次出手的完整结算，顺序固定：
     * <ol>
     * <li>移动后先落再消（横移本身可能补满一行）；</li>
     * <li>场上不足 {@link SlideyRules#MIN_ROWS} 行时从底部自动补行；</li>
     * <li>按原版涨行规则生成新的一行顶到最底部，盘面整体上移；</li>
     * <li>被新行顶起的方块再次下落、再消一次；然后同样补足行数；</li>
     * <li>盘面稳定后判断死局，最后结算连击。</li>
     * </ol>
     * 全部在松手的那一次调用里同步完成，不依赖任何 tick。
     */
    private void takeTurn() {
        settleBoard();
        topUpRows();
        if (advanceRow()) {
            settleBoard();
            topUpRows();
            if (!SlideyRules.hasAnyMove(blocks, blockCount)) beginGameOver();
        }
        streak = turnCleared ? Math.min(COMBO_CAP, streak + 1) : 1;
        turnCleared = false;
        syncAndMarkChanged();
    }

    /**
     * 把盘面一直落到稳定：悬空方块整块落到底，消掉满行并计分；
     * 消行后若有上方方块悬空，继续落再消，直到没有任何满行。
     */
    private void settleBoard() {
        while (true) {
            while (SlideyRules.stepGravity(blocks, blockCount)) {
                // 整块逐格落到底
            }
            int[] result = SlideyRules.clearFullRows(blocks, blockCount);
            if (result[1] <= 0) return;
            blockCount = result[0];
            score += result[1] * LINE_SCORE * streak;
            lines += result[1];
            turnCleared = true;
        }
    }

    /**
     * 消完行后盘面太空就补行：不足 {@link SlideyRules#MIN_ROWS} 行有方块时，
     * 从底部补入新行，直到够为止；补行本身不增加涨行计数，也不结束对局。
     */
    private void topUpRows() {
        int guard = 0;
        while (SlideyRules.occupiedRows(blocks, blockCount) < SlideyRules.MIN_ROWS && guard++ < 6) {
            int next = SlideyRules.insertGeneratedRow(blocks, blockCount, random(), tideCount);
            if (next < 0) return;
            blockCount = next;
            settleBoard();
        }
    }

    /** 按原版规则生成并顶入新的一行；顶行放不下就结束对局并返回 {@code false}。 */
    private boolean advanceRow() {
        // 原版按「这一行是第几次涨行」取难度档位，所以传入自增后的计数
        int next = SlideyRules.insertGeneratedRow(blocks, blockCount, random(), tideCount + 1);
        if (next < 0) {
            beginGameOver();
            return false;
        }
        blockCount = next;
        tideCount++;
        return true;
    }

    private void beginGameOver() {
        status = STATUS_GAME_OVER;
        cashOut();
    }

    /**
     * 把本局分数折成余额；{@link #settled} 保证同一局只结一次账。
     * 最高分与消除行数只在单人局写入里程碑。
     */
    @Override
    protected void cashOut() {
        if (settled) return;
        settled = true;
        long reward = (score - REDEMPTION_BASELINE) / POINTS_PER_COIN;
        if (reward > 0) addBalance(reward);
        if (!sessionCounts()) return;
        var server = server();
        if (server == null) return;
        GameMilestones.get(server).recordScore(GameMilestones.SLIDEY, sessionStarter(), score, lines, gameTime());
    }

    // ==================== 服务端动作 ====================

    /**
     * 拖动松手：把 {@code (row, column)} 上的那一块横移 {@code delta} 格，并在同一次调用里
     * 把下落、消行、补行全部算完。
     */
    private void slideBlockAt(int row, int column, int delta, @Nullable ServerPlayer player) {
        if (isRemote() || status != STATUS_PLAYING || !guard(player)) return;
        if (row < 0 || row >= SlideyRules.ROWS || delta == 0 || Math.abs(delta) >= SlideyRules.COLS) return;
        int index = SlideyRules.blockAt(blocks, blockCount, row, column);
        if (index < 0 || !SlideyRules.canMove(blocks, blockCount, index, delta)) return;
        SlideyRules.move(blocks, index, delta);
        takeTurn();
    }

    // ==================== 界面 ====================

    @Override
    public Widget createUIWidget() {
        var page = Form.page();
        page.addChildren(createGameArea(), createStatusPanel());
        return page;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new ConfigPage());
        sideTabs.attachSubTab(new LeaderboardPage());
    }

    private UIElement createGameArea() {
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChild(new BoardView());
    }

    /** 状态面板：余额、本局得分、最高分、消除行数、连击与操作提示。 */
    private StatusPanel createStatusPanel() {
        var statusPanel = panel();
        statusPanel.addLine(LINE_BALANCE, () -> number(balance()));
        statusPanel.addLine(LINE_SCORE_TEXT, () -> number(view().score()));
        var restart = Button.text(77, BUTTON_RESTART);
        restart.setOnServerClick(() -> restartGame(playerOf(restart)));
        statusPanel.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, createWithdrawButton(77)));
        statusPanel.addChild(TextLine.constant(UISizes.CONTENT_WIDTH, LINE_CONTROLS)
                .bindClientColor(UITheme::textSecondary));
        statusPanel.addChild(TextLine.constant(UISizes.CONTENT_WIDTH, LINE_RULES)
                .bindClientColor(UITheme::textSecondary));
        return statusPanel;
    }

    /** 配置页：游玩权限开关 + 余额。 */
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

    /** 排行榜页：最高分榜（含消除行数）。 */
    private final class LeaderboardPage extends SingleLeaderboardPage {

        private LeaderboardPage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected Widget boardContent() {
            var section = Form.section(SECTION_MILESTONES);
            section.addChild(milestoneTable("game_milestones_slidey", MILESTONE_COLUMNS,
                    SlideyMachine::buildMilestoneRows));
            return section;
        }
    }

    /** 盘面控件：按住一块方块左右拖动（拖动只发生在客户端，松手才把结果发给服务端）。 */
    private final class BoardView extends UIElement {

        private final RPC<int[]> slide;

        /** 正在拖的方块下标；dragIndex < 0 表示没在拖。 */
        private int dragIndex = -1;
        private int dragRow;
        private int dragColumn;
        private double dragMouseX;
        /** 按下时的快照版本；拖动期间盘面变了就放弃这次拖动。 */
        private int dragRevision;
        /** 拖动时相对起点滑了几格（已按两侧空位限位）。 */
        private double dragOffset;
        /** 已发给服务端、还没被快照确认的横移，用来先按结果画一帧。 */
        private int pendingRow = -1;
        private int pendingColumn;
        private int pendingDelta;
        private boolean pendingMove;
        private int pendingFrames;
        /** 上一次对齐的版本号：快照一到就清掉提前落位。 */
        private int shownRevision = Integer.MIN_VALUE;

        private BoardView() {
            layout(style -> style.size(VIEW_WIDTH, VIEW_HEIGHT));
            registerView(this, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
            slide = addRPC(ByteBufCodecs.VAR_INT_ARRAY,
                    (player, payload) -> {
                        if (payload.length < 3) return;
                        slideBlockAt(payload[0], payload[1], payload[2],
                                player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                    });
        }

        /** 快照换版就清掉提前落位，换成服务端的真实结果。 */
        private void syncPending() {
            if (view().revision() == shownRevision) return;
            shownRevision = view().revision();
            pendingMove = false;
            pendingFrames = 0;
            pendingRow = -1;
            pendingDelta = 0;
        }

        /** 鼠标下的格子编码 {@code row * COLS + column}；落在格线槽或盘面外为 -1。 */
        private int cellAt(double mouseX, double mouseY) {
            if (!isMouseOverElement(mouseX, mouseY)) return -1;
            int localX = (int) (mouseX - getPositionX()) - GRID;
            int localY = (int) (mouseY - getPositionY()) - GRID;
            if (localX < 0 || localY < 0) return -1;
            int column = localX / CELL_PITCH;
            int row = localY / CELL_PITCH;
            if (column >= SlideyRules.COLS || row >= SlideyRules.ROWS) return -1;
            if (localX % CELL_PITCH >= CELL_SIZE || localY % CELL_PITCH >= CELL_SIZE) return -1;
            return row * SlideyRules.COLS + column;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            syncPending();
            var snapshot = view();
            if (button != 0 || snapshot.status() != STATUS_PLAYING || pendingMove) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            int cell = cellAt(mouseX, mouseY);
            if (cell < 0) return super.mouseClicked(mouseX, mouseY, button);
            int row = cell / SlideyRules.COLS;
            int column = cell % SlideyRules.COLS;
            int index = SlideyRules.blockAt(snapshot.blocks(), snapshot.blockCount(), row, column);
            if (index < 0) return super.mouseClicked(mouseX, mouseY, button);
            dragIndex = index;
            dragRow = row;
            dragColumn = SlideyRules.blockColumn(snapshot.blocks(), index);
            dragRevision = snapshot.revision();
            dragMouseX = mouseX;
            dragOffset = 0;
            capturePointer(button);
            playButtonClickSound();
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            if (dragIndex < 0) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            if (view().revision() != dragRevision) {
                // 拖动期间盘面被服务端改过，位置已经对不上，直接放弃这次拖动
                dragIndex = -1;
                dragOffset = 0;
                releasePointer();
                return true;
            }
            dragOffset = offsetFor(mouseX);
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (dragIndex < 0) return super.mouseReleased(mouseX, mouseY, button);
            if (view().revision() != dragRevision) {
                dragIndex = -1;
                dragOffset = 0;
                releasePointer();
                return true;
            }
            int steps = (int) Math.round(offsetFor(mouseX));
            if (steps != 0) {
                // 先按移动结果显示，服务端快照回来后再对齐；没有确认时会自动回落到原位
                pendingRow = dragRow;
                pendingColumn = dragColumn;
                pendingDelta = steps;
                pendingMove = true;
                pendingFrames = 0;
                slide.send(new int[] { dragRow, dragColumn, steps });
            }
            dragIndex = -1;
            dragOffset = 0;
            releasePointer();
            return true;
        }

        /** 拖动偏移：这一块左右还能滑几格就限到几格，不能越过别的方块，也不会滑出盘面。 */
        @OnlyIn(Dist.CLIENT)
        private double offsetFor(double mouseX) {
            var snapshot = view();
            int[] blocks = snapshot.blocks();
            if (dragIndex < 0 || dragIndex >= snapshot.blockCount()) return 0;
            int backward = SlideyRules.moveRoom(blocks, snapshot.blockCount(), dragIndex, -1);
            int forward = SlideyRules.moveRoom(blocks, snapshot.blockCount(), dragIndex, 1);
            double raw = (mouseX - dragMouseX) / CELL_PITCH;
            return Math.clamp(raw, -backward, forward);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            syncPending();
            if (pendingMove && ++pendingFrames > 40) {
                pendingMove = false;
                pendingRow = -1;
                pendingDelta = 0;
            }
            var snapshot = view();
            int left = getPositionX();
            int boardTop = getPositionY();
            graphics.fill(left, boardTop, left + BOARD_WIDTH, boardTop + BOARD_HEIGHT, COLOR_BOARD);
            for (int column = 0; column <= SlideyRules.COLS; column++) {
                int x = left + column * CELL_PITCH;
                graphics.fill(x, boardTop, x + GRID, boardTop + BOARD_HEIGHT, COLOR_GRID);
            }
            for (int row = 0; row <= SlideyRules.ROWS; row++) {
                int y = boardTop + row * CELL_PITCH;
                graphics.fill(left, y, left + BOARD_WIDTH, y + GRID, COLOR_GRID);
            }
            drawBorder(graphics, left, boardTop, BOARD_WIDTH, BOARD_HEIGHT);
            drawBlocks(graphics, snapshot, left, boardTop);
            if (dragIndex < 0 && !pendingMove) drawHover(graphics, snapshot, mouseX, mouseY, left, boardTop);
            drawOverlay(graphics, snapshot.status(), left, boardTop);
        }

        /** 画全部方块：正在拖的那一块按拖动像素偏移画，其余照常。 */
        private void drawBlocks(GuiGraphics graphics, BoardSnapshot snapshot, int left, int top) {
            int[] blocks = snapshot.blocks();
            int count = snapshot.blockCount();
            for (int index = 0; index < count; index++) {
                int offset = 0;
                if (index == dragIndex) {
                    offset = (int) Math.round(dragOffset * CELL_PITCH);
                } else if (pendingMove && pendingRow == SlideyRules.blockRow(blocks, index) &&
                        pendingColumn == SlideyRules.blockColumn(blocks, index)) {
                            offset = pendingDelta * CELL_PITCH;
                        }
                drawBlock(graphics, blocks, index, offset, left, top);
            }
        }

        /** 画一块：按长度连续画格子。 */
        private void drawBlock(GuiGraphics graphics, int[] blocks, int index, int offsetX, int left, int top) {
            int row = SlideyRules.blockRow(blocks, index);
            int start = SlideyRules.blockColumn(blocks, index);
            int length = SlideyRules.blockLength(blocks, index);
            int color = SlideyRules.blockColor(blocks, index);
            if (color <= 0 || color > SlideyRules.COLOR_COUNT) return;
            for (int cell = 0; cell < length; cell++) {
                drawCell(graphics, left + (start + cell) * CELL_PITCH + GRID + offsetX, top + row * CELL_PITCH + GRID,
                        CELL_SIZE, BLOCK_COLORS[color - 1]);
            }
        }

        /** 鼠标悬停在方块上时描出整块，提示可以拖动。 */
        @OnlyIn(Dist.CLIENT)
        private void drawHover(GuiGraphics graphics, BoardSnapshot snapshot, double mouseX, double mouseY, int left,
                               int top) {
            if (snapshot.status() != STATUS_PLAYING) return;
            int cell = cellAt(mouseX, mouseY);
            if (cell < 0) return;
            int row = cell / SlideyRules.COLS;
            int column = cell % SlideyRules.COLS;
            int index = SlideyRules.blockAt(snapshot.blocks(), snapshot.blockCount(), row, column);
            if (index < 0) return;
            int start = SlideyRules.blockColumn(snapshot.blocks(), index);
            int length = SlideyRules.blockLength(snapshot.blocks(), index);
            int x = left + start * CELL_PITCH;
            int y = top + row * CELL_PITCH;
            int width = length * CELL_PITCH + GRID;
            graphics.fill(x, y, x + width, y + 2, COLOR_HOVER);
            graphics.fill(x, y + CELL_PITCH - 2, x + width, y + CELL_PITCH, COLOR_HOVER);
            graphics.fill(x, y, x + 2, y + CELL_PITCH, COLOR_HOVER);
            graphics.fill(x + width - 2, y, x + width, y + CELL_PITCH, COLOR_HOVER);
        }

        private void drawCell(GuiGraphics graphics, int x, int y, int size, int color) {
            graphics.fill(x, y, x + size, y + size, color);
            graphics.fill(x, y, x + size, y + GRID, COLOR_HIGHLIGHT);
            graphics.fill(x, y, x + GRID, y + size, COLOR_HIGHLIGHT);
            graphics.fill(x + size - GRID, y, x + size, y + size, COLOR_SHADOW);
            graphics.fill(x, y + size - GRID, x + size, y + size, COLOR_SHADOW);
        }

        private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
            graphics.fill(x, y, x + width, y + GRID, COLOR_BORDER);
            graphics.fill(x, y + height - GRID, x + width, y + height, COLOR_BORDER);
            graphics.fill(x, y, x + GRID, y + height, COLOR_BORDER);
            graphics.fill(x + width - GRID, y, x + width, y + height, COLOR_BORDER);
        }

        private void drawOverlay(GuiGraphics graphics, int status, int left, int top) {
            if (status != STATUS_GAME_OVER) return;
            graphics.fill(left, top, left + BOARD_WIDTH, top + BOARD_HEIGHT, COLOR_OVERLAY);
            UIText.drawCentered(graphics, TEXT_GAME_OVER.getString(),
                    left + BOARD_WIDTH / 2, top + BOARD_HEIGHT / 2 - 4, BOARD_WIDTH, COLOR_OVERLAY_TEXT);
        }
    }

    // ==================== 排行榜 ====================

    /** 「全部榜单」页里的土耳其方块榜：列与行和本机排行榜页完全一致。 */
    public static GameBoardCatalog.Board leaderboardBoard() {
        return GameBoardCatalog.board(MILESTONE_COLUMNS, server -> GameMilestones.get(server).modCount(),
                (server, first, second) -> buildMilestoneRows(GameMilestones.get(server), server));
    }

    private static List<MilestoneCell[]> buildMilestoneRows(GameMilestones store, MinecraftServer server) {
        var board = store.slideyBoard();
        var rows = new ObjectArrayList<MilestoneCell[]>(board.size());
        for (var stat : board) {
            rows.add(new MilestoneCell[] { MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.achieved(number(stat.bestScore()), stat.bestScoreTime()),
                    MilestoneCell.achieved(number(stat.bestMerged()), stat.bestMergedTime()) });
        }
        return rows;
    }
}

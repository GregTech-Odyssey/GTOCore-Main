package com.gtocore.common.machine.noenergy.game.tetris;

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
import com.suried.gtoutils.schedule.Task;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

/**
 * 俄罗斯方块游戏机：10×20 盘面，7 种方块按 7-bag 抽取，SRS 旋转与踢墙，消行按等级计分。
 *
 * <p>
 * 重力由服务端 tick 驱动，键盘操作经 {@link RPC} 上行。新方块只在界面打开时生成；关闭界面后，
 * 已经在场上的方块仍会继续下落到底，落定后停在「等待」状态，等下一个玩家打开界面再生成下一块。
 * 最高分记进 {@link GameMilestones}，并汇总到机器的排行榜页与 {@code /gtocore leaderboard} 聚合榜。
 */
@DataGeneratorScanned
public final class TetrisMachine extends AbstractGameMachine<TetrisMachine.BoardSnapshot> {

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_PLAYING = 1;
    /** 方块已落定，但界面没开，暂不生成下一块。 */
    private static final int STATUS_WAITING = 2;
    private static final int STATUS_GAME_OVER = 3;

    /** 方块生成时的左上角列：3 宽的方块与 I 都居中，O 落在中间两列。 */
    private static final int SPAWN_X = 3;
    private static final int MAX_LEVEL = 99;
    private static final int BAG_SIZE = TetrisRules.PIECE_COUNT;

    /** 兑现基线：一局超过它的部分才折成游戏币。 */
    public static final int REDEMPTION_BASELINE = 1000;

    private static final int ACTION_LEFT = 0;
    private static final int ACTION_RIGHT = 1;
    private static final int ACTION_SOFT_DROP = 2;
    private static final int ACTION_ROTATE_CW = 3;
    private static final int ACTION_ROTATE_CCW = 4;
    private static final int ACTION_HARD_DROP = 5;

    private static final int CELL_SIZE = 9;
    private static final int GRID = 1;
    private static final int CELL_PITCH = CELL_SIZE + GRID;
    private static final int BOARD_WIDTH = TetrisRules.COLS * CELL_PITCH + GRID;
    private static final int BOARD_HEIGHT = TetrisRules.ROWS * CELL_PITCH + GRID;
    private static final int PREVIEW_CELL = 8;
    private static final int PREVIEW_PITCH = PREVIEW_CELL + GRID;
    private static final int PREVIEW_WIDTH = 4 * PREVIEW_PITCH + GRID;
    private static final int PREVIEW_GAP = 8;
    private static final int VIEW_WIDTH = BOARD_WIDTH + PREVIEW_GAP + PREVIEW_WIDTH;
    private static final int PREVIEW_TOP = 12;

    private static final int COLOR_BOARD = 0xFFF3F6FA;
    private static final int COLOR_GRID = 0xFFC6CED9;
    private static final int COLOR_BORDER = 0xFF7D8798;
    private static final int COLOR_HIGHLIGHT = 0x40FFFFFF;
    private static final int COLOR_SHADOW = 0x24000000;
    private static final int COLOR_GHOST = 0x66000000;
    private static final int COLOR_OVERLAY = 0xB8FFFFFF;
    private static final int COLOR_OVERLAY_TEXT = 0xFF2A3140;
    /** 方块颜色，下标是方块序数。 */
    private static final int[] PIECE_COLORS = {
            0xFF4FD8FF, 0xFFFFD54F, 0xFFB388FF, 0xFF69F0AE, 0xFFFF5252, 0xFF448AFF, 0xFFFFAB40 };

    private static final MilestoneColumn[] MILESTONE_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 70),
            MilestoneColumn.right(COLUMN_BEST_SCORE, 56) };

    @RegisterLanguage(cn = "本局得分", en = "Score")
    private static final Component LINE_SCORE = Component.translatable("gtocore.machine.tetris.line.score");
    @RegisterLanguage(cn = "下一个", en = "Next")
    private static final Component LINE_NEXT = Component.translatable("gtocore.machine.tetris.line.next");
    @RegisterLanguage(cn = "← → 移动 · ↓ 软降", en = "Left/Right move · Down soft drop")
    private static final Component LINE_CONTROLS_MOVE = Component.translatable("gtocore.machine.tetris.line.controls_move");
    @RegisterLanguage(cn = "↑/X 旋转 · Z 反向 · 空格硬降", en = "Up/X rotate · Z rotate back · Space hard drop")
    private static final Component LINE_CONTROLS_ACTION = Component.translatable("gtocore.machine.tetris.line.controls_action");

    @RegisterLanguage(cn = "游戏结束", en = "Game over")
    private static final String TEXT_GAME_OVER = "gtocore.machine.tetris.text.game_over";
    @RegisterLanguage(cn = "等待新方块", en = "Waiting")
    private static final String TEXT_WAITING = "gtocore.machine.tetris.text.waiting";
    @RegisterLanguage(cn = "等待新方块，打开界面继续", en = "Waiting for the next piece — reopen the UI to continue")
    private static final String TEXT_WAITING_DETAIL = "gtocore.machine.tetris.text.waiting_detail";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final Component BUTTON_RESTART = Component.translatable("gtocore.machine.tetris.button.restart");

    /** 盘面，行优先；0 空格，1~7 是已落定方块的类型 + 1。 */
    @SaveToDisk
    private int[] board = new int[TetrisRules.CELLS];
    @SaveToDisk(defaultValue = "0")
    private int activePiece = TetrisRules.NONE;
    @SaveToDisk(defaultValue = "0")
    private int activeRotation;
    @SaveToDisk(defaultValue = "0")
    private int activeX;
    @SaveToDisk(defaultValue = "0")
    private int activeY;
    @SaveToDisk(defaultValue = "0")
    private int nextPiece;
    @SaveToDisk(defaultValue = "0")
    private int score;
    @SaveToDisk(defaultValue = "0")
    private int lines;
    @SaveToDisk(defaultValue = "1")
    private int level = 1;
    @SaveToDisk(defaultValue = "0")
    private int status = STATUS_IDLE;
    /** 7-bag 的当前袋与取用位置；袋取完重新洗牌。 */
    @SaveToDisk
    private int[] bag = new int[BAG_SIZE];
    @SaveToDisk(defaultValue = "7")
    private int bagIndex = BAG_SIZE;

    /** 距下一次重力下落还差几 tick；不存盘。 */
    private int gravityTicks;
    /** 这一局是否已结过账；不存盘，读档时按局面还原。 */
    private boolean settled;
    /** 当前打开的界面数；关闭后不再生成新方块。 */
    private int openUIs;

    @Nullable
    private Task tickSubs;
    private final Runnable machineTick = this::tickMachine;

    public TetrisMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** 下发的界面快照：盘面、活动方块、下一块、分数与阶段；相等性只看版本号。 */
    public record BoardSnapshot(int revision, int[] board, int activePiece, int activeRotation, int activeX, int activeY,
                                int nextPiece, int score, int lines, int level, int status, int highScore) {

        private static final int[] EMPTY_BOARD = new int[TetrisRules.CELLS];
        public static final BoardSnapshot EMPTY = new BoardSnapshot(-1, EMPTY_BOARD, TetrisRules.NONE, 0, 0, 0, 0, 0, 0, 1,
                STATUS_IDLE, 0);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::board,
                ByteBufCodecs.VAR_INT, BoardSnapshot::activePiece,
                ByteBufCodecs.VAR_INT, BoardSnapshot::activeRotation,
                ByteBufCodecs.VAR_INT, BoardSnapshot::activeX,
                ByteBufCodecs.VAR_INT, BoardSnapshot::activeY,
                ByteBufCodecs.VAR_INT, BoardSnapshot::nextPiece,
                ByteBufCodecs.VAR_INT, BoardSnapshot::score,
                ByteBufCodecs.VAR_INT, BoardSnapshot::lines,
                ByteBufCodecs.VAR_INT, BoardSnapshot::level,
                ByteBufCodecs.VAR_INT, BoardSnapshot::status,
                ByteBufCodecs.VAR_INT, BoardSnapshot::highScore,
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

    /** 读档后收尾：夹回合法值，坏档退到等待状态；正在下落的方块由 tick 继续。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
        updateTickSubscription();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        cancelTickSubscription();
    }

    private void sanitizeState() {
        if (board == null || board.length != TetrisRules.CELLS) {
            board = new int[TetrisRules.CELLS];
            resetRound();
            return;
        }
        for (int cell = 0; cell < board.length; cell++) {
            if (board[cell] < 0 || board[cell] > TetrisRules.PIECE_COUNT) board[cell] = 0;
        }
        score = Math.max(0, score);
        lines = Math.max(0, lines);
        level = Math.clamp(level, 1, MAX_LEVEL);
        status = Math.clamp(status, STATUS_IDLE, STATUS_GAME_OVER);
        activeRotation &= 3;
        if (activePiece < TetrisRules.NONE || activePiece >= TetrisRules.PIECE_COUNT) activePiece = TetrisRules.NONE;
        if (nextPiece < 0 || nextPiece >= TetrisRules.PIECE_COUNT) nextPiece = 0;
        if (bag == null || bag.length != BAG_SIZE) {
            bag = new int[BAG_SIZE];
            bagIndex = BAG_SIZE;
        }
        if (bagIndex < 0 || bagIndex > BAG_SIZE) bagIndex = BAG_SIZE;
        if (status == STATUS_PLAYING && (activePiece == TetrisRules.NONE ||
                !TetrisRules.fits(board, activePiece, activeRotation, activeX, activeY))) {
            activePiece = TetrisRules.NONE;
            status = STATUS_WAITING;
        }
        if (status == STATUS_IDLE) activePiece = TetrisRules.NONE;
        settled = status == STATUS_GAME_OVER;
    }

    /** 清空一整局，回到「还没开局」；读档坏档与开局都走这里。 */
    private void resetRound() {
        Arrays.fill(board, 0);
        activePiece = TetrisRules.NONE;
        activeRotation = 0;
        activeX = SPAWN_X;
        activeY = 0;
        nextPiece = 0;
        score = 0;
        lines = 0;
        level = 1;
        if (bag == null || bag.length != BAG_SIZE) bag = new int[BAG_SIZE];
        Arrays.fill(bag, 0);
        bagIndex = BAG_SIZE;
        gravityTicks = 0;
        status = STATUS_IDLE;
        settled = false;
    }

    @Override
    protected BoardSnapshot createView() {
        var server = server();
        int highScore = server == null ? 0 : GameMilestones.get(server).tetrisBest(viewerId());
        return new BoardSnapshot(revision(), board.clone(), activePiece, activeRotation, activeX, activeY, nextPiece,
                score, lines, level, status, highScore);
    }

    @Override
    protected void onViewerChanged() {
        if (!isRemote() && getLevel() != null) syncAndMarkChanged();
    }

    @Override
    protected void onGameStateChanged() {
        if (!isRemote()) updateTickSubscription();
    }

    // ==================== 界面打开/关闭 ====================

    /**
     * 界面打开时记一次：之后落定的方块才会生成下一块。关闭时只减计数，
     * 已经在场上的方块继续由 tick 送到落点。
     */
    @Override
    public ModularUI createUI(Player player) {
        var ui = super.createUI(player);
        if (!isRemote() && player instanceof ServerPlayer) {
            openUIs++;
            ui.registerCloseListener(this::onUIClosed);
            ensureStarted();
            updateTickSubscription();
        }
        return ui;
    }

    private void onUIClosed() {
        if (isRemote()) return;
        if (openUIs > 0) openUIs--;
        updateTickSubscription();
    }

    /** 打开界面时：等待中补一块新的；空闲或已结束则开新的一局。 */
    private void ensureStarted() {
        if (status == STATUS_WAITING) {
            if (spawnPiece()) {
                status = STATUS_PLAYING;
            } else {
                beginGameOver();
            }
            syncAndMarkChanged();
            return;
        }
        if (status == STATUS_IDLE || status == STATUS_GAME_OVER) {
            startGame(null);
        }
    }

    private void startGame(@Nullable ServerPlayer starter) {
        Arrays.fill(board, 0);
        score = 0;
        lines = 0;
        level = 1;
        gravityTicks = 0;
        bagIndex = BAG_SIZE;
        nextPiece = takeFromBag();
        settled = false;
        beginSession(starter);
        status = STATUS_PLAYING;
        if (!spawnPiece()) beginGameOver();
        syncAndMarkChanged();
    }

    private void restartGame(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        cashOut();
        startGame(player);
    }

    // ==================== tick ====================

    private void tickMachine() {
        if (isRemote() || status != STATUS_PLAYING) {
            updateTickSubscription();
            return;
        }
        if (++gravityTicks < fallInterval()) return;
        gravityTicks = 0;
        if (TetrisRules.fits(board, activePiece, activeRotation, activeX, activeY + 1)) {
            activeY++;
            syncAndMarkChanged();
            return;
        }
        lockActive();
        syncAndMarkChanged();
    }

    /** 等级越高落得越快；最低 2 tick 一格。 */
    private int fallInterval() {
        return Math.max(2, 22 - (level - 1) * 2);
    }

    private void updateTickSubscription() {
        if (getLevel() == null || isRemote()) return;
        if (status == STATUS_PLAYING) {
            tickSubs = subscribeServerTick(tickSubs, machineTick);
        } else {
            cancelTickSubscription();
        }
    }

    private void cancelTickSubscription() {
        if (tickSubs != null) {
            tickSubs.cancel();
            tickSubs = null;
        }
    }

    // ==================== 落子 ====================

    /** 方块落定：写盘、消行、计分；界面开着就补下一块，否则停在等待状态。 */
    private void lockActive() {
        TetrisRules.place(board, activePiece, activeRotation, activeX, activeY, activePiece + 1);
        int cleared = TetrisRules.clearLines(board);
        if (cleared > 0) {
            score += TetrisRules.LINE_SCORES[Math.min(cleared, 4)] * level;
            lines += cleared;
            level = Math.min(MAX_LEVEL, 1 + lines / 10);
        }
        activePiece = TetrisRules.NONE;
        if (openUIs <= 0) {
            status = STATUS_WAITING;
            return;
        }
        if (!spawnPiece()) beginGameOver();
    }

    private boolean spawnPiece() {
        activePiece = nextPiece;
        nextPiece = takeFromBag();
        activeRotation = 0;
        activeX = SPAWN_X;
        activeY = 0;
        gravityTicks = 0;
        return TetrisRules.fits(board, activePiece, 0, activeX, activeY);
    }

    private int takeFromBag() {
        if (bagIndex >= BAG_SIZE) {
            for (int index = 0; index < BAG_SIZE; index++) {
                bag[index] = index;
            }
            for (int index = BAG_SIZE - 1; index > 0; index--) {
                int swap = random().nextInt(index + 1);
                int value = bag[index];
                bag[index] = bag[swap];
                bag[swap] = value;
            }
            bagIndex = 0;
        }
        return bag[bagIndex++];
    }

    private void beginGameOver() {
        status = STATUS_GAME_OVER;
        activePiece = TetrisRules.NONE;
        cashOut();
    }

    /**
     * 把本局分数折成余额；{@link #settled} 保证同一局只结一次账。
     * 最高分只在单人局写入里程碑，分数为 0 时不记。
     */
    @Override
    protected void cashOut() {
        if (settled) return;
        settled = true;
        long reward = (score - REDEMPTION_BASELINE) / TetrisRules.POINTS_PER_COIN;
        if (reward > 0) addBalance(reward);
        if (!sessionCounts()) return;
        var server = server();
        if (server == null) return;
        GameMilestones.get(server).recordScore(GameMilestones.TETRIS, sessionStarter(), score, 0, gameTime());
    }

    // ==================== 服务端动作 ====================

    private void handleAction(int action, @Nullable ServerPlayer player) {
        if (isRemote() || status != STATUS_PLAYING || !guard(player)) return;
        boolean changed = switch (action) {
            case ACTION_LEFT -> tryMove(-1, 0);
            case ACTION_RIGHT -> tryMove(1, 0);
            case ACTION_SOFT_DROP -> softDrop();
            case ACTION_ROTATE_CW -> rotate(1);
            case ACTION_ROTATE_CCW -> rotate(-1);
            case ACTION_HARD_DROP -> hardDrop();
            default -> false;
        };
        if (changed) syncAndMarkChanged();
    }

    private boolean tryMove(int deltaX, int deltaY) {
        if (!TetrisRules.fits(board, activePiece, activeRotation, activeX + deltaX, activeY + deltaY)) return false;
        activeX += deltaX;
        activeY += deltaY;
        return true;
    }

    private boolean softDrop() {
        if (TetrisRules.fits(board, activePiece, activeRotation, activeX, activeY + 1)) {
            activeY++;
            score++;
            gravityTicks = 0;
            return true;
        }
        lockActive();
        return true;
    }

    private boolean hardDrop() {
        int distance = TetrisRules.dropDistance(board, activePiece, activeRotation, activeX, activeY);
        if (distance > 0) {
            activeY += distance;
            score += distance * 2;
        }
        lockActive();
        return true;
    }

    private boolean rotate(int delta) {
        int to = (activeRotation + delta + TetrisRules.ROTATION_COUNT) & 3;
        if (to == activeRotation) return false;
        int[] kicks = TetrisRules.kicks(activePiece, activeRotation, to);
        for (int index = 0; index < kicks.length; index += 2) {
            int nextX = activeX + kicks[index];
            int nextY = activeY + kicks[index + 1];
            if (!TetrisRules.fits(board, activePiece, to, nextX, nextY)) continue;
            activeRotation = to;
            activeX = nextX;
            activeY = nextY;
            return true;
        }
        return false;
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

    /** 状态面板：余额、本局得分、最高分、消行、等级与操作提示。 */
    private StatusPanel createStatusPanel() {
        var statusPanel = panel();
        statusPanel.addLine(LINE_BALANCE, () -> number(balance()));
        statusPanel.addLine(LINE_SCORE, () -> number(view().score()));
        var restart = Button.text(77, BUTTON_RESTART);
        restart.setOnServerClick(() -> restartGame(playerOf(restart)));
        statusPanel.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, createWithdrawButton(77)));
        statusPanel.addChild(TextLine.constant(UISizes.CONTENT_WIDTH, LINE_CONTROLS_MOVE)
                .bindClientColor(UITheme::textSecondary));
        statusPanel.addChild(TextLine.constant(UISizes.CONTENT_WIDTH, LINE_CONTROLS_ACTION)
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

    /** 排行榜页：最高分榜。 */
    private final class LeaderboardPage extends SingleLeaderboardPage {

        private LeaderboardPage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected Widget boardContent() {
            var section = Form.section(SECTION_MILESTONES);
            section.addChild(milestoneTable("game_milestones_tetris", MILESTONE_COLUMNS,
                    TetrisMachine::buildMilestoneRows));
            return section;
        }
    }

    /** 盘面控件：自绘盘面、残影与下一块预览，同时接收键盘操作。 */
    private final class BoardView extends UIElement {

        private final RPC<Integer> action;

        private BoardView() {
            layout(style -> style.size(VIEW_WIDTH, BOARD_HEIGHT));
            registerView(this, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
            action = addRPC(ByteBufCodecs.VAR_INT,
                    (player, value) -> handleAction(value, player instanceof ServerPlayer serverPlayer ? serverPlayer : null));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int command = commandOf(keyCode);
            if (command < 0) return super.keyPressed(keyCode, scanCode, modifiers);
            action.send(command);
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            var snapshot = view();
            int left = getPositionX();
            int top = getPositionY();
            graphics.fill(left, top, left + BOARD_WIDTH, top + BOARD_HEIGHT, COLOR_BOARD);
            for (int column = 0; column <= TetrisRules.COLS; column++) {
                int x = left + column * CELL_PITCH;
                graphics.fill(x, top, x + GRID, top + BOARD_HEIGHT, COLOR_GRID);
            }
            for (int row = 0; row <= TetrisRules.ROWS; row++) {
                int y = top + row * CELL_PITCH;
                graphics.fill(left, y, left + BOARD_WIDTH, y + GRID, COLOR_GRID);
            }
            drawBorder(graphics, left, top, BOARD_WIDTH, BOARD_HEIGHT);
            drawSettled(graphics, snapshot.board(), left, top);
            drawActive(graphics, snapshot, left, top);
            drawPreview(graphics, snapshot.nextPiece(), left + BOARD_WIDTH + PREVIEW_GAP, top);
            drawOverlay(graphics, snapshot.status(), left, top);
        }

        private void drawSettled(GuiGraphics graphics, int[] cells, int left, int top) {
            if (cells.length != TetrisRules.CELLS) return;
            for (int row = 0; row < TetrisRules.ROWS; row++) {
                for (int column = 0; column < TetrisRules.COLS; column++) {
                    int value = cells[row * TetrisRules.COLS + column];
                    if (value <= 0 || value > TetrisRules.PIECE_COUNT) continue;
                    drawCell(graphics, left + column * CELL_PITCH + GRID, top + row * CELL_PITCH + GRID,
                            PIECE_COLORS[value - 1]);
                }
            }
        }

        private void drawActive(GuiGraphics graphics, BoardSnapshot snapshot, int left, int top) {
            int piece = snapshot.activePiece();
            if (piece < 0 || piece >= TetrisRules.PIECE_COUNT) return;
            if (snapshot.board().length != TetrisRules.CELLS) return;
            int rotation = snapshot.activeRotation() & 3;
            int x = snapshot.activeX();
            int y = snapshot.activeY();
            int ghost = y + TetrisRules.dropDistance(snapshot.board(), piece, rotation, x, y);
            if (ghost > y && ghost < TetrisRules.ROWS) {
                drawPiece(graphics, left, top, piece, rotation, x, ghost, COLOR_GHOST, false);
            }
            drawPiece(graphics, left, top, piece, rotation, x, y, PIECE_COLORS[piece], true);
        }

        private void drawPiece(GuiGraphics graphics, int left, int top, int piece, int rotation, int x, int y,
                               int color, boolean solid) {
            int mask = TetrisRules.mask(piece, rotation);
            for (int bit = 0; bit < 16; bit++) {
                if ((mask & (1 << bit)) == 0) continue;
                int column = x + (bit & 3);
                int row = y + (bit >> 2);
                if (column < 0 || column >= TetrisRules.COLS || row < 0 || row >= TetrisRules.ROWS) continue;
                int cellX = left + column * CELL_PITCH + GRID;
                int cellY = top + row * CELL_PITCH + GRID;
                if (solid) {
                    drawCell(graphics, cellX, cellY, color);
                } else {
                    graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + GRID, color);
                    graphics.fill(cellX, cellY + CELL_SIZE - GRID, cellX + CELL_SIZE, cellY + CELL_SIZE, color);
                    graphics.fill(cellX, cellY, cellX + GRID, cellY + CELL_SIZE, color);
                    graphics.fill(cellX + CELL_SIZE - GRID, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, color);
                }
            }
        }

        private void drawCell(GuiGraphics graphics, int x, int y, int color) {
            graphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, color);
            graphics.fill(x, y, x + CELL_SIZE, y + GRID, COLOR_HIGHLIGHT);
            graphics.fill(x, y, x + GRID, y + CELL_SIZE, COLOR_HIGHLIGHT);
            graphics.fill(x + CELL_SIZE - GRID, y, x + CELL_SIZE, y + CELL_SIZE, COLOR_SHADOW);
            graphics.fill(x, y + CELL_SIZE - GRID, x + CELL_SIZE, y + CELL_SIZE, COLOR_SHADOW);
        }

        private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
            graphics.fill(x, y, x + width, y + GRID, COLOR_BORDER);
            graphics.fill(x, y + height - GRID, x + width, y + height, COLOR_BORDER);
            graphics.fill(x, y, x + GRID, y + height, COLOR_BORDER);
            graphics.fill(x + width - GRID, y, x + width, y + height, COLOR_BORDER);
        }

        private void drawPreview(GuiGraphics graphics, int piece, int left, int top) {
            if (piece < 0 || piece >= TetrisRules.PIECE_COUNT) return;
            UIText.drawLeft(graphics, LINE_NEXT.getString(), left, top, UITheme.TEXT_SECONDARY);
            int gridTop = top + PREVIEW_TOP;
            graphics.fill(left, gridTop, left + PREVIEW_WIDTH, gridTop + PREVIEW_WIDTH, COLOR_BOARD);
            for (int index = 1; index < 4; index++) {
                int x = left + index * PREVIEW_PITCH;
                graphics.fill(x, gridTop, x + GRID, gridTop + PREVIEW_WIDTH, COLOR_GRID);
                int y = gridTop + index * PREVIEW_PITCH;
                graphics.fill(left, y, left + PREVIEW_WIDTH, y + GRID, COLOR_GRID);
            }
            drawBorder(graphics, left, gridTop, PREVIEW_WIDTH, PREVIEW_WIDTH);
            int mask = TetrisRules.mask(piece, 0);
            for (int bit = 0; bit < 16; bit++) {
                if ((mask & (1 << bit)) == 0) continue;
                int x = left + (bit & 3) * PREVIEW_PITCH + GRID;
                int y = gridTop + (bit >> 2) * PREVIEW_PITCH + GRID;
                graphics.fill(x, y, x + PREVIEW_CELL, y + PREVIEW_CELL, PIECE_COLORS[piece]);
                graphics.fill(x, y, x + PREVIEW_CELL, y + GRID, COLOR_HIGHLIGHT);
                graphics.fill(x + PREVIEW_CELL - GRID, y, x + PREVIEW_CELL, y + PREVIEW_CELL, COLOR_SHADOW);
            }
        }

        private void drawOverlay(GuiGraphics graphics, int status, int left, int top) {
            String text = switch (status) {
                case STATUS_GAME_OVER -> Component.translatable(TEXT_GAME_OVER).getString();
                case STATUS_WAITING -> Component.translatable(TEXT_WAITING).getString();
                default -> null;
            };
            if (text == null) return;
            graphics.fill(left, top, left + BOARD_WIDTH, top + BOARD_HEIGHT, COLOR_OVERLAY);
            UIText.drawCentered(graphics, text, left + BOARD_WIDTH / 2, top + BOARD_HEIGHT / 2 - 4,
                    BOARD_WIDTH, COLOR_OVERLAY_TEXT);
        }
    }

    /** 键盘到动作的映射：方向键、WASD、X/Z 与空格。 */
    private static int commandOf(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> ACTION_LEFT;
            case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> ACTION_RIGHT;
            case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> ACTION_SOFT_DROP;
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_X -> ACTION_ROTATE_CW;
            case GLFW.GLFW_KEY_Z -> ACTION_ROTATE_CCW;
            case GLFW.GLFW_KEY_SPACE -> ACTION_HARD_DROP;
            default -> -1;
        };
    }

    // ==================== 排行榜 ====================

    /** 「全部榜单」页里的俄罗斯方块榜：列与行和本机排行榜页完全一致。 */
    public static GameBoardCatalog.Board leaderboardBoard() {
        return GameBoardCatalog.board(MILESTONE_COLUMNS, server -> GameMilestones.get(server).modCount(),
                (server, first, second) -> buildMilestoneRows(GameMilestones.get(server), server));
    }

    private static List<MilestoneCell[]> buildMilestoneRows(GameMilestones store, MinecraftServer server) {
        var board = store.tetrisBoard();
        var rows = new ObjectArrayList<MilestoneCell[]>(board.size());
        for (var stat : board) {
            rows.add(new MilestoneCell[] { MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.achieved(number(stat.bestScore()), stat.bestScoreTime()) });
        }
        return rows;
    }
}

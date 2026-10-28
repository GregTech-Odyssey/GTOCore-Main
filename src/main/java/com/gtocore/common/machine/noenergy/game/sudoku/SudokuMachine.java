package com.gtocore.common.machine.noenergy.game.sudoku;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
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
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 数独游戏机：9×9 盘面，六档难度，完成按档位发放游戏币并把用时记进该难度的排行榜。
 *
 * <p>
 * 盘面是一整块自绘控件，按由下到上分五层：纸面底色、数字橙色高亮、选中格的天青色选择高亮、格线（细线在下、
 * 粗线在上）、数字层。提示数字是深蓝近黑，玩家填的数字是亮粉，候选数是灰色小字；被选中的同数字与同候选只加橙色
 * 底色高亮，数字本身始终保持原来的颜色。
 *
 * <p>
 * 盘面下是 5×2 的十个按钮：1~9 与「标记」。普通模式下点数字填写（再点同一个数字清除），标记模式下点数字只会
 * 添加或去掉该格的候选数。所有落子由服务端校验并下发快照。
 */
@DataGeneratorScanned
public class SudokuMachine extends AbstractGameMachine<SudokuMachine.BoardSnapshot> {

    @RegisterLanguage(cn = "入门", en = "Beginner")
    private static final String DIFFICULTY_BEGINNER = "gtocore.machine.sudoku.difficulty.beginner";
    @RegisterLanguage(cn = "简单", en = "Easy")
    private static final String DIFFICULTY_EASY = "gtocore.machine.sudoku.difficulty.easy";
    @RegisterLanguage(cn = "普通", en = "Normal")
    private static final String DIFFICULTY_NORMAL = "gtocore.machine.sudoku.difficulty.normal";
    @RegisterLanguage(cn = "困难", en = "Hard")
    private static final String DIFFICULTY_HARD = "gtocore.machine.sudoku.difficulty.hard";
    @RegisterLanguage(cn = "专家", en = "Expert")
    private static final String DIFFICULTY_EXPERT = "gtocore.machine.sudoku.difficulty.expert";
    @RegisterLanguage(cn = "大师", en = "Master")
    private static final String DIFFICULTY_MASTER = "gtocore.machine.sudoku.difficulty.master";
    /** 难度名字，下标与 {@link SudokuRules.Difficulty} 的序数一一对应。 */
    private static final String[] DIFFICULTY_NAMES = { DIFFICULTY_BEGINNER, DIFFICULTY_EASY, DIFFICULTY_NORMAL,
            DIFFICULTY_HARD, DIFFICULTY_EXPERT, DIFFICULTY_MASTER };

    @RegisterLanguage(cn = "难度", en = "Difficulty")
    private static final String SECTION_DIFFICULTY = "gtocore.machine.sudoku.section.difficulty";
    @RegisterLanguage(cn = "难度", en = "Difficulty")
    private static final String LINE_DIFFICULTY = "gtocore.machine.sudoku.line.difficulty";
    @RegisterLanguage(cn = "用时", en = "Time")
    private static final String LINE_TIME = "gtocore.machine.sudoku.line.time";
    @RegisterLanguage(cn = "%s · 完成奖励 %s 游戏币", en = "%s · Clear reward %s Game Coins")
    private static final String TEXT_DIFFICULTY = "gtocore.machine.sudoku.text.difficulty";
    @RegisterLanguage(cn = "标记", en = "Mark")
    private static final String BUTTON_MARK = "gtocore.machine.sudoku.button.mark";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.sudoku.button.restart";
    @RegisterLanguage(cn = "这一局已经完成了", en = "This puzzle is already solved")
    private static final String REASON_SOLVED = "gtocore.machine.sudoku.reason.solved";

    /** 排行榜列：玩家与最快完成用时。 */
    private static final MilestoneColumn[] LEADERBOARD_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 64),
            MilestoneColumn.center(COLUMN_DURATION, 54) };

    /** 单格边长：每个数字格 15×15 像素。 */
    private static final int CELL_SIZE = 15;
    /** 每条格线占据的线槽宽度；粗线填满整条线槽，细线只填中间 1 像素。 */
    private static final int THICK_GRID = 3;
    /** 一个格子加上它后面那条线槽的步进。 */
    private static final int CELL_PITCH = CELL_SIZE + THICK_GRID;
    /**
     * 棋盘边长：{@link SudokuRules#SIZE} 格 × 15 像素，加上 {@link SudokuRules#SIZE} + 1 条线槽 × 3 像素。
     */
    private static final int BOARD_PIXELS = CELL_SIZE * SudokuRules.SIZE + THICK_GRID * (SudokuRules.SIZE + 1);
    /** 数字键的宽度：一行五个加四个间隙略窄于内容区，整体居中。 */
    private static final int KEY_WIDTH = 30;
    private static final int NOTE_SLOT = CELL_SIZE / SudokuRules.BOX;
    private static final float NOTE_SCALE = 0.5F;

    private static final int COLOR_PAPER = 0xEFFBF7E8;
    private static final int COLOR_GRID = 0xFFB4AA92;
    private static final int COLOR_BOX = 0xFF4C4738;
    /** 提示数字：深蓝近黑。 */
    private static final int COLOR_GIVEN = 0xFF141C4A;
    /** 玩家填写：亮粉。 */
    private static final int COLOR_ENTERED = 0xFFFF4FC3;
    /** 候选数：灰色小字。 */
    private static final int COLOR_NOTE = 0xFF9A9A9A;
    /** 选中格子的天青色边框。 */
    private static final int COLOR_SELECTED = 0xFF4FD8FF;
    /** 与选中格同数字/同候选的橙色底色高亮。 */
    private static final int COLOR_MATCH_FILL = 0x55FF9A2E;
    private static final int COLOR_NOTE_MATCH_FILL = 0x66FF9A2E;

    /** 没有盘面时给客户端的全零占位；机器自己的数组不会被它共享。 */
    private static final int[] EMPTY_CELLS = new int[SudokuRules.CELLS];

    private enum Status {
        IDLE,
        PLAYING,
        SOLVED
    }

    private final SudokuRules rules = SudokuRules.create();

    /** 当前难度（{@link SudokuRules.Difficulty} 的序数）。 */
    @SaveToDisk
    private int difficulty = SudokuRules.Difficulty.BEGINNER.ordinal();
    /** 提示盘：0 为空格，非 0 的格子玩家不能修改。 */
    @SaveToDisk
    private int[] givens = new int[SudokuRules.CELLS];
    /** 玩家填写的数字，0 为未填。 */
    @SaveToDisk
    private int[] values = new int[SudokuRules.CELLS];
    /** 每格的候选数位掩码（第 n 位表示数字 n + 1）。 */
    @SaveToDisk
    private int[] notes = new int[SudokuRules.CELLS];
    /** 当前选中的格子，-1 表示没有选中。 */
    @SaveToDisk
    private int selected = -1;
    /** 标记模式：点数字只改候选数。 */
    @SaveToDisk
    private boolean notesMode;
    @SaveToDisk
    private long roundStart;
    @SaveToDisk
    private long lastElapsed;
    @SaveToDisk
    private int status = Status.IDLE.ordinal();

    public SudokuMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /**
     * 通道里下发的盘面快照：提示盘、玩家填写、候选数、选中格、模式标记、用时与局面阶段。
     * 相等性只看版本号，省掉逐元素比较三张盘。
     */
    public record BoardSnapshot(int revision, int difficulty, int[] givens, int[] values, int[] notes, int selected,
                                int flags, long elapsedTicks, int state) {

        private static final int FLAG_SOLVED = 1;
        private static final int FLAG_NOTES_MODE = 1 << 1;
        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, 0, EMPTY_CELLS, EMPTY_CELLS, EMPTY_CELLS,
                -1, 0, 0L, Status.IDLE.ordinal());
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT, BoardSnapshot::difficulty,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::givens,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::values,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::notes,
                ByteBufCodecs.VAR_INT, BoardSnapshot::selected,
                ByteBufCodecs.VAR_INT, BoardSnapshot::flags,
                ByteBufCodecs.VAR_LONG, BoardSnapshot::elapsedTicks,
                ByteBufCodecs.VAR_INT, BoardSnapshot::state,
                BoardSnapshot::new);

        boolean solved() {
            return (flags & FLAG_SOLVED) != 0;
        }

        boolean notesMode() {
            return (flags & FLAG_NOTES_MODE) != 0;
        }

        /** 第 {@code cell} 格最终显示的数字：提示优先，其次玩家填写。 */
        int valueAt(int cell) {
            return SudokuRules.displayedValue(givens, values, cell);
        }

        /**
         * 要一并高亮的数字集合（位掩码）：选中格有数字时就是该数字，空着但有候选数时就是那些候选数。
         * 盘面上同数字与别处的同候选都按这套位掩码判断。
         */
        int focusMask() {
            if (selected < 0 || selected >= SudokuRules.CELLS) return 0;
            int value = valueAt(selected);
            if (value != 0) return 1 << (value - SudokuRules.VALUE_MIN);
            return selected < notes.length ? notes[selected] : 0;
        }

        /** 当前难度的奖励；网络来的序数一律夹回合法范围。 */
        long reward() {
            var all = SudokuRules.Difficulty.ALL;
            return all[Math.clamp(difficulty, 0, all.length - 1)].reward();
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

    /** 读档后的收尾：难度、盘面与选中格夹回合法值；坏盘按没开局处理，等下次打开界面重发。只在服务端调用。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    private void sanitizeState() {
        difficulty = Math.clamp(difficulty, 0, SudokuRules.Difficulty.ALL.length - 1);
        if (givens.length != SudokuRules.CELLS || values.length != SudokuRules.CELLS ||
                notes.length != SudokuRules.CELLS) {
            resetBoard();
            return;
        }
        int clues = 0;
        for (int cell = 0; cell < SudokuRules.CELLS; cell++) {
            if (givens[cell] < SudokuRules.VALUE_MIN || givens[cell] > SudokuRules.VALUE_MAX) givens[cell] = 0;
            if (values[cell] < SudokuRules.VALUE_MIN || values[cell] > SudokuRules.VALUE_MAX) values[cell] = 0;
            if (givens[cell] != 0) {
                values[cell] = 0;
                notes[cell] = 0;
                clues++;
            } else if (values[cell] != 0) {
                notes[cell] = 0;
            }
            notes[cell] &= SudokuRules.ALL_VALUES;
        }
        selected = selected >= 0 && selected < SudokuRules.CELLS ? selected : -1;
        if (status < 0 || status >= Status.values().length) status = Status.IDLE.ordinal();
        if (status != Status.IDLE.ordinal() && clues == 0) {
            resetBoard();
            return;
        }
        if (status == Status.PLAYING.ordinal() && roundStart <= 0L) roundStart = gameTime();
        if (status == Status.SOLVED.ordinal()) lastElapsed = Math.max(1L, lastElapsed);
    }

    /** 清空成一局没开始的空盘。 */
    private void resetBoard() {
        Arrays.fill(givens, 0);
        Arrays.fill(values, 0);
        Arrays.fill(notes, 0);
        selected = -1;
        roundStart = 0L;
        lastElapsed = 0L;
        notesMode = false;
        status = Status.IDLE.ordinal();
    }

    @Override
    protected BoardSnapshot createView() {
        int flags = (status == Status.SOLVED.ordinal() ? BoardSnapshot.FLAG_SOLVED : 0) |
                (notesMode ? BoardSnapshot.FLAG_NOTES_MODE : 0);
        return new BoardSnapshot(revision(), difficulty, givens, values, notes, selected, flags, elapsedTicks(), status);
    }

    private SudokuRules.Difficulty currentDifficulty() {
        var difficulty = SudokuRules.Difficulty.of(this.difficulty);
        return difficulty == null ? SudokuRules.Difficulty.BEGINNER : difficulty;
    }

    private boolean isPlaying() {
        return status == Status.PLAYING.ordinal();
    }

    private long elapsedTicks() {
        if (status == Status.SOLVED.ordinal()) return lastElapsed;
        if (status != Status.PLAYING.ordinal() || roundStart <= 0L) return 0L;
        return Math.max(0L, gameTime() - roundStart);
    }

    /** 首次打开界面时把没开的一局开起来。 */
    private void ensureStarted() {
        if (status == Status.IDLE.ordinal()) startRound(null);
    }

    private void startRound(@Nullable ServerPlayer starter) {
        givens = rules.createPuzzle(random(), currentDifficulty());
        Arrays.fill(values, 0);
        Arrays.fill(notes, 0);
        selected = -1;
        notesMode = false;
        lastElapsed = 0L;
        roundStart = gameTime();
        status = Status.PLAYING.ordinal();
        beginSession(starter);
    }

    /** 完成一局：按难度发奖励，单人局再记一次该难度的最快用时。 */
    private void finishRound() {
        var difficulty = currentDifficulty();
        status = Status.SOLVED.ordinal();
        lastElapsed = Math.max(1L, gameTime() - roundStart);
        addBalance(difficulty.reward());
        if (!sessionCounts()) return;
        var store = milestones();
        if (store != null) {
            store.recordSudoku(sessionStarter(), difficulty.ordinal(), lastElapsed, gameTime());
        }
    }

    // ==================== 服务端动作 ====================

    private void selectCell(int cell, @Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        if (cell < 0 || cell >= SudokuRules.CELLS) return;
        selected = cell;
        syncAndMarkChanged();
    }

    /** 数字键：普通模式填写 / 清除，标记模式只切换候选数。 */
    private void enterDigit(int digit, @Nullable ServerPlayer player) {
        if (isRemote() || !isPlaying() || !guard(player)) return;
        if (digit < SudokuRules.VALUE_MIN || digit > SudokuRules.VALUE_MAX) return;
        if (selected < 0 || selected >= SudokuRules.CELLS || givens[selected] != 0) return;
        int bit = 1 << (digit - SudokuRules.VALUE_MIN);
        if (notesMode) {
            if (values[selected] != 0) return;
            notes[selected] ^= bit;
            syncAndMarkChanged();
            return;
        }
        values[selected] = values[selected] == digit ? 0 : digit;
        notes[selected] = 0;
        if (rules.isComplete(givens, values)) finishRound();
        syncAndMarkChanged();
    }

    /** 切换标记模式。 */
    private void toggleMark(@Nullable ServerPlayer player) {
        if (isRemote() || !isPlaying() || !guard(player)) return;
        notesMode = !notesMode;
        syncAndMarkChanged();
    }

    /** 服务端：重开一局。 */
    private void restartRound(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        startRound(player);
        syncAndMarkChanged();
    }

    /** 服务端：换难度并立即发一局新题。 */
    private void selectDifficulty(int ordinal, @Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        var difficulty = SudokuRules.Difficulty.of(ordinal);
        if (difficulty == null || difficulty.ordinal() == this.difficulty) return;
        this.difficulty = difficulty.ordinal();
        startRound(player);
        syncAndMarkChanged();
    }

    // ==================== 界面 ====================

    /** 主页 = 盘面 + 十个按键 + 状态面板；盘面控件先建，通道里的快照项才排在按键之前。 */
    @Override
    public Widget createUIWidget() {
        if (!isRemote()) ensureStarted();
        var page = Form.page();
        page.addChildren(createGameArea(), createKeypad(), createStatusPanel());
        return page;
    }

    /** 主页之外挂「难度」与「排行榜」两个子页。 */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new DifficultyPage());
        sideTabs.attachSubTab(new LeaderboardPage());
    }

    private UIElement createGameArea() {
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChild(new BoardView());
    }

    /** 9×9 盘面下 5×2 的按键：第一行 1~5，第二行 6~9 与标记。 */
    private UIElement createKeypad() {
        var keypad = panel();
        keypad.addChild(keyRow(SudokuRules.VALUE_MIN, SudokuRules.VALUE_MIN + 4));
        var second = keyRow(SudokuRules.VALUE_MIN + 5, SudokuRules.VALUE_MAX);
        second.addChild(createMarkButton());
        keypad.addChild(second);
        return keypad;
    }

    private UIElement keyRow(int from, int to) {
        var row = UIElement.row(Button.HEIGHT).layout(style -> style.justifyContent(AlignContent.CENTER).gapAll(2));
        for (int digit = from; digit <= to; digit++) {
            row.addChild(createDigitButton(digit));
        }
        return row;
    }

    private Button createDigitButton(int digit) {
        var button = Button.text(KEY_WIDTH, Component.literal(Integer.toString(digit)));
        button.clientDisabled(() -> view().solved(), REASON_SOLVED);
        button.setOnServerClick(() -> enterDigit(digit, playerOf(button)));
        return button;
    }

    private Button createMarkButton() {
        var button = Button.translatable(KEY_WIDTH, BUTTON_MARK);
        button.setSelected(() -> view().notesMode());
        button.clientDisabled(() -> view().solved(), REASON_SOLVED);
        button.setOnServerClick(() -> toggleMark(playerOf(button)));
        return button;
    }

    private StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance()));
        status.addLine(LINE_DIFFICULTY, ()-> difficultyName(view().difficulty()));
        status.addLine(LINE_TIME, () -> duration(view().elapsedTicks()));
        var restart = Button.translatable(77, BUTTON_RESTART);
        restart.setOnServerClick(() -> restartRound(playerOf(restart)));
        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, createWithdrawButton(77)));
        return status;
    }

    /** 难度名字的翻译键；越界按入门处理。 */
    private static Component difficultyName(int ordinal) {
        return Component.translatable(DIFFICULTY_NAMES[Math.clamp(ordinal, 0, DIFFICULTY_NAMES.length - 1)]);
    }

    /**
     * 盘面控件：整块自绘并接收格子点击。
     */
    private final class BoardView extends UIElement {

        private final RPC<Integer> click;

        private BoardView() {
            layout(style -> style.size(BOARD_PIXELS, BOARD_PIXELS));
            registerView(this, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
            click = addRPC(ByteBufCodecs.VAR_INT,
                    (player, cell) -> selectCell(cell,
                            player instanceof ServerPlayer serverPlayer ? serverPlayer : null));
        }

        /** 第 {@code column} 格的左边缘（跳过最左边的线槽）。 */
        private int cellX(int column) {
            return getPositionX() + THICK_GRID + column * CELL_PITCH;
        }

        /** 第 {@code row} 格的上边缘（跳过最上面的线槽）。 */
        private int cellY(int row) {
            return getPositionY() + THICK_GRID + row * CELL_PITCH;
        }

        /** 鼠标下的格子下标；落在格线槽里或盘面外为 -1。 */
        private int cellAt(double mouseX, double mouseY) {
            if (!isMouseOverElement(mouseX, mouseY)) return -1;
            int localX = (int) (mouseX - getPositionX()) - THICK_GRID;
            int localY = (int) (mouseY - getPositionY()) - THICK_GRID;
            if (localX < 0 || localY < 0) return -1;
            int column = localX / CELL_PITCH;
            int row = localY / CELL_PITCH;
            if (column >= SudokuRules.SIZE || row >= SudokuRules.SIZE) return -1;
            if (localX % CELL_PITCH >= CELL_SIZE || localY % CELL_PITCH >= CELL_SIZE) return -1;
            return row * SudokuRules.SIZE + column;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int cell = cellAt(mouseX, mouseY);
            if (cell < 0 || button != 0) return super.mouseClicked(mouseX, mouseY, button);
            click.send(cell);
            playButtonClickSound();
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            drawPaperLayer(graphics);
            drawHighlightLayer(graphics);
            drawSelectionLayer(graphics);
            drawGridLayer(graphics);
            drawDigitLayer(graphics);
            int hovered = cellAt(mouseX, mouseY);
            if (hovered >= 0 && hovered != view().selected()) {
                UIDraw.hoverOverlay(graphics, cellX(hovered % SudokuRules.SIZE), cellY(hovered / SudokuRules.SIZE),
                        CELL_SIZE, CELL_SIZE);
            }
        }

        /** 最底层：纸面底色。 */
        @OnlyIn(Dist.CLIENT)
        private void drawPaperLayer(GuiGraphics graphics) {
            int x0 = getPositionX();
            int y0 = getPositionY();
            UITheme.PANEL.draw(graphics, 0, 0, x0, y0, BOARD_PIXELS, BOARD_PIXELS);
            graphics.fill(x0, y0, x0 + BOARD_PIXELS, y0 + BOARD_PIXELS, COLOR_PAPER);
        }

        /** 数字橙色高亮层：同数字格整格加橙色底（向外扩 1 像素），同候选只给对应的小九宫格加橙色底；不改数字颜色。 */
        @OnlyIn(Dist.CLIENT)
        private void drawHighlightLayer(GuiGraphics graphics) {
            var snapshot = view();
            int focus = snapshot.focusMask();
            int[] notes = snapshot.notes();
            for (int row = 0; row < SudokuRules.SIZE; row++) {
                for (int column = 0; column < SudokuRules.SIZE; column++) {
                    int cell = row * SudokuRules.SIZE + column;
                    int x = cellX(column);
                    int y = cellY(row);
                    int value = snapshot.valueAt(cell);
                    if (focus != 0 && value != 0) {
                        int bit = 1 << (value - SudokuRules.VALUE_MIN);
                        if ((focus & bit) != 0) {
                            graphics.fill(x - 1, y - 1, x + CELL_SIZE + 1, y + CELL_SIZE + 1, COLOR_MATCH_FILL);
                        }
                        continue;
                    }
                    if (focus == 0 || cell >= notes.length) continue;
                    int matched = notes[cell] & focus;
                    if (matched == 0) continue;
                    for (int digit = SudokuRules.VALUE_MIN; digit <= SudokuRules.VALUE_MAX; digit++) {
                        int bit = 1 << (digit - SudokuRules.VALUE_MIN);
                        if ((matched & bit) == 0) continue;
                        int noteColumn = (digit - SudokuRules.VALUE_MIN) % SudokuRules.BOX;
                        int noteRow = (digit - SudokuRules.VALUE_MIN) / SudokuRules.BOX;
                        graphics.fill(x + noteColumn * NOTE_SLOT, y + noteRow * NOTE_SLOT,
                                x + (noteColumn + 1) * NOTE_SLOT, y + (noteRow + 1) * NOTE_SLOT,
                                COLOR_NOTE_MATCH_FILL);
                    }
                }
            }
        }

        /** 格线层：细格线先画、粗格线后画，交叉处由粗格线压住；每条线槽 3 像素，细线只填中间 1 像素。 */
        @OnlyIn(Dist.CLIENT)
        private void drawGridLayer(GuiGraphics graphics) {
            int x0 = getPositionX();
            int y0 = getPositionY();
            int middle = THICK_GRID / 2;
            for (int index = 0; index <= SudokuRules.SIZE; index++) {
                if (index % SudokuRules.BOX == 0) continue;
                int vertical = x0 + index * CELL_PITCH;
                int horizontal = y0 + index * CELL_PITCH;
                graphics.fill(vertical + middle, y0, vertical + middle + 1, y0 + BOARD_PIXELS, COLOR_GRID);
                graphics.fill(x0, horizontal + middle, x0 + BOARD_PIXELS, horizontal + middle + 1, COLOR_GRID);
            }
            for (int index = 0; index <= SudokuRules.SIZE; index++) {
                if (index % SudokuRules.BOX != 0) continue;
                int vertical = x0 + index * CELL_PITCH;
                int horizontal = y0 + index * CELL_PITCH;
                graphics.fill(vertical, y0, vertical + THICK_GRID, y0 + BOARD_PIXELS, COLOR_BOX);
                graphics.fill(x0, horizontal, x0 + BOARD_PIXELS, horizontal + THICK_GRID, COLOR_BOX);
            }
        }

        /**
         * 选中格的天青色选择高亮：贴着 15×15 的格子边界，不再向外扩，线宽固定 1 像素。
         * 这一层画在格线之前，格线会压住落在格线槽里的那部分边框。
         */
        @OnlyIn(Dist.CLIENT)
        private void drawSelectionLayer(GuiGraphics graphics) {
            int selected = view().selected();
            if (selected < 0 || selected >= SudokuRules.CELLS) return;
            int left = cellX(selected % SudokuRules.SIZE);
            int top = cellY(selected / SudokuRules.SIZE);
            int right = left + CELL_SIZE;
            int bottom = top + CELL_SIZE;
            graphics.fill(left, top, right, top + 1, COLOR_SELECTED);
            graphics.fill(left, bottom - 1, right, bottom, COLOR_SELECTED);
            graphics.fill(left, top + 1, left + 1, bottom - 1, COLOR_SELECTED);
            graphics.fill(right - 1, top + 1, right, bottom - 1, COLOR_SELECTED);
        }

        /** 顶层数字层：数字在格内居中，候选数按 3×3 小格显示；提示数字、玩家数字与候选数始终保持自己的颜色。 */
        @OnlyIn(Dist.CLIENT)
        private void drawDigitLayer(GuiGraphics graphics) {
            var snapshot = view();
            int[] givens = snapshot.givens();
            int[] values = snapshot.values();
            int[] notes = snapshot.notes();
            for (int row = 0; row < SudokuRules.SIZE; row++) {
                for (int column = 0; column < SudokuRules.SIZE; column++) {
                    int cell = row * SudokuRules.SIZE + column;
                    int x = cellX(column);
                    int y = cellY(row);
                    int given = cell < givens.length ? givens[cell] : 0;
                    if (given != 0) {
                        drawDigit(graphics, given, x, y, COLOR_GIVEN);
                        continue;
                    }
                    int value = cell < values.length ? values[cell] : 0;
                    if (value != 0) {
                        drawDigit(graphics, value, x, y, COLOR_ENTERED);
                        continue;
                    }
                    int note = cell < notes.length ? notes[cell] : 0;
                    if (note != 0) drawNotes(graphics, note, x, y);
                }
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawDigit(GuiGraphics graphics, int digit, int cellX, int cellY, int color) {
            var font = UIText.font();
            String text = Integer.toString(digit);
            graphics.drawString(font, text, cellX + (CELL_SIZE - font.width(text)) / 2,
                    UIText.centerY(cellY, CELL_SIZE), color, false);
        }

        /** 候选数按 3×3 摊在 15×15 的格内，字号缩到一半，一律用灰色。 */
        @OnlyIn(Dist.CLIENT)
        private void drawNotes(GuiGraphics graphics, int note, int cellX, int cellY) {
            var font = UIText.font();
            var pose = graphics.pose();
            for (int digit = SudokuRules.VALUE_MIN; digit <= SudokuRules.VALUE_MAX; digit++) {
                int bit = 1 << (digit - SudokuRules.VALUE_MIN);
                if ((note & bit) == 0) continue;
                String text = Integer.toString(digit);
                int column = (digit - SudokuRules.VALUE_MIN) % SudokuRules.BOX;
                int row = (digit - SudokuRules.VALUE_MIN) / SudokuRules.BOX;
                float offsetX = cellX + column * NOTE_SLOT + (NOTE_SLOT - font.width(text) * NOTE_SCALE) / 2;
                float offsetY = cellY + row * NOTE_SLOT + (NOTE_SLOT - UIText.GLYPH_HEIGHT * NOTE_SCALE) / 2;
                pose.pushPose();
                pose.translate(offsetX, offsetY, 0);
                pose.scale(NOTE_SCALE, NOTE_SCALE, 1.0F);
                graphics.drawString(font, text, 0, 0, COLOR_NOTE, false);
                pose.popPose();
            }
        }
    }

    /** 难度页：六个档位纵向排列，选中的那个两端带绿色 > <，切换后立即开新局。 */
    private final class DifficultyPage extends GamePage {

        private DifficultyPage() {
            super(WidgetIcons.SETTINGS, PAGE_DIFFICULTY, PAGE_DIFFICULTY_TOOLTIP);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(allowOthersRow());

            var section = Form.section(SECTION_DIFFICULTY);
            for (int ordinal = 0; ordinal < SudokuRules.Difficulty.ALL.length; ordinal++) {
                // 循环变量不是有效 final，先拷一份给 lambda 用
                int index = ordinal;
                var difficulty = SudokuRules.Difficulty.ALL[index];
                var button = choiceButton(LayoutStyle.AUTO, Component.translatable(DIFFICULTY_NAMES[index]),
                        () -> difficulty == SudokuRules.Difficulty.ALL[Math.clamp(SudokuMachine.this.difficulty, 0,
                                SudokuRules.Difficulty.ALL.length - 1)]);
                button.setOnServerClick(() -> selectDifficulty(index, playerOf(button)));
                section.addChild(button);
            }
            page.addChild(section);
            page.addChild(balancePanel());
            return page;
        }
    }

    /** 排行榜页：顶上一排难度选项，下面只显示选中的那张表。 */
    private final class LeaderboardPage extends LeaderboardTabPage {

        private LeaderboardPage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected int boardCount() {
            return SudokuRules.Difficulty.ALL.length;
        }

        @Override
        protected Component boardName(int index) {
            return Component.translatable(DIFFICULTY_NAMES[index]);
        }

        @Override
        protected Widget boardContent(int index) {
            var section = Form.section(DIFFICULTY_NAMES[index]);
            section.addChild(milestoneTable("game_milestones_sudoku_" + index, LEADERBOARD_COLUMNS,
                    (store, server) -> buildLeaderboardRows(store, server, index)));
            return section;
        }
    }

    /** 单个难度的排行榜：玩家 + 最快完成用时，用时悬停显示达成时刻。 */
    private List<MilestoneCell[]> buildLeaderboardRows(GameMilestones store, MinecraftServer server, int ordinal) {
        var stats = store.sudokuBoard(ordinal);
        var rows = new ObjectArrayList<MilestoneCell[]>(stats.size());
        for (var stat : stats) {
            rows.add(new MilestoneCell[] {
                    MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.achieved(duration(stat.ticks()[ordinal]), stat.times()[ordinal]) });
        }
        return rows;
    }
}

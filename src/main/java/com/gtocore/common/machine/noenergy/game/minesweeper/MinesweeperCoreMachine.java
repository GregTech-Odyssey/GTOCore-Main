package com.gtocore.common.machine.noenergy.game.minesweeper;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Adjuster;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 「扫雷」游戏机：{@link MinesweeperRules.Difficulty} 档位的翻格玩法，盘面以方块形式铺在机器旁边（+X / -Z 方向），
 * 一格一个 {@link MinesweeperDisplayBlock}。
 *
 * <p>
 * 机器本身只提供界面：右击打开（{@link #createUIWidget()}），里面换难度、重开一局、展开/收起盘面、取出游戏币；
 * 游戏里的落子仍然是右击显示方块翻开、潜行右击插旗（见 {@link #click}）。全部通关才把奖励记进余额，没收完的盘面
 * 重开、收起或拆机器都拿不到币；自定义档（自己定宽高与雷数，最大 64×64）不计奖励。
 *
 * <p>
 * 盘面按自定义档的最大尺寸分配定长数组（{@link #CELL_CAPACITY}）：DataSyncLib 的数组字段是按内容原地存取、长度不上网，
 * 定长才能换难度时不必换数组引用；多出来的尾巴一直是「未翻开 / 不是雷」，规则只看前 {@code rules.cellCount()} 格。
 * 大盘每次落子只刷新规则报告改动过的那几格（{@link MinesweeperRules#open} 的 {@code touched}），不整盘重刷。
 */
@DataGeneratorScanned
public class MinesweeperCoreMachine extends AbstractGameMachine<MinesweeperCoreMachine.BoardSnapshot> {

    @RegisterLanguage(cn = "初级", en = "Beginner")
    private static final String DIFFICULTY_EASY = "gtocore.machine.minesweeper.difficulty.easy";
    @RegisterLanguage(cn = "中级", en = "Intermediate")
    private static final String DIFFICULTY_NORMAL = "gtocore.machine.minesweeper.difficulty.normal";
    @RegisterLanguage(cn = "高级", en = "Advanced")
    private static final String DIFFICULTY_HARD = "gtocore.machine.minesweeper.difficulty.hard";
    @RegisterLanguage(cn = "专家", en = "Expert")
    private static final String DIFFICULTY_EXPERT = "gtocore.machine.minesweeper.difficulty.expert";
    @RegisterLanguage(cn = "地狱", en = "Hell")
    private static final String DIFFICULTY_HELL = "gtocore.machine.minesweeper.difficulty.hell";
    @RegisterLanguage(cn = "自定义", en = "Custom")
    private static final String DIFFICULTY_CUSTOM = "gtocore.machine.minesweeper.difficulty.custom";
    /** 难度名字，下标与 {@link MinesweeperRules.Difficulty} 的序数一一对应。 */
    private static final String[] DIFFICULTY_NAMES = { DIFFICULTY_EASY, DIFFICULTY_NORMAL, DIFFICULTY_HARD,
            DIFFICULTY_EXPERT, DIFFICULTY_HELL, DIFFICULTY_CUSTOM };

    @RegisterLanguage(cn = "难度", en = "Difficulty")
    private static final String SECTION_DIFFICULTY = "gtocore.machine.minesweeper.section.difficulty";
    @RegisterLanguage(cn = "自定义盘面", en = "Custom board")
    private static final String SECTION_CUSTOM = "gtocore.machine.minesweeper.section.custom";
    @RegisterLanguage(cn = "宽度", en = "Width")
    private static final String LINE_WIDTH = "gtocore.machine.minesweeper.line.width";
    @RegisterLanguage(cn = "高度", en = "Height")
    private static final String LINE_HEIGHT = "gtocore.machine.minesweeper.line.height";
    @RegisterLanguage(cn = "雷数", en = "Mines")
    private static final String LINE_MINES = "gtocore.machine.minesweeper.line.mines";
    @RegisterLanguage(cn = "%s×%s · %s 雷 · %s 币", en = "%s×%s · %s mines · %s coins")
    private static final String TEXT_DIFFICULTY_DETAIL = "gtocore.machine.minesweeper.text.difficulty_detail";
    @RegisterLanguage(cn = "不计奖励", en = "no payout")
    private static final String TEXT_CUSTOM_DETAIL = "gtocore.machine.minesweeper.text.custom_detail";
    @RegisterLanguage(cn = "%s %s×%s · %s雷 · %s币", en = "%s %s×%s · %s mines · %s coins")
    private static final String TEXT_DIFFICULTY = "gtocore.machine.minesweeper.text.difficulty";
    @RegisterLanguage(cn = "自定义 %s×%s · %s 雷 · 不计奖励", en = "Custom %s×%s · %s mines · no payout")
    private static final String TEXT_CUSTOM = "gtocore.machine.minesweeper.text.custom";
    @RegisterLanguage(cn = "当前自定义：%s×%s · %s 雷", en = "Current custom board: %s×%s · %s mines")
    private static final String TEXT_CUSTOM_SUMMARY = "gtocore.machine.minesweeper.text.custom_summary";
    @RegisterLanguage(cn = "自定义模式不计奖励；宽度与高度各 %s~%s，雷数按盘面大小夹紧",
                      en = "Custom mode pays nothing; width and height are %s to %s each, and the mine count is clamped to the board")
    private static final String TEXT_CUSTOM_HINT = "gtocore.machine.minesweeper.text.custom_hint";
    @RegisterLanguage(cn = "盘面还没铺开，点「展开盘面」开始", en = "The board is packed up — press Unfold board to start")
    private static final String TEXT_IDLE = "gtocore.machine.minesweeper.text.idle";
    @RegisterLanguage(cn = "已插旗 %s / %s 雷", en = "%s / %s mines flagged")
    private static final String TEXT_FLAGS = "gtocore.machine.minesweeper.text.flags";
    @RegisterLanguage(cn = "通关！已兑现 %s 游戏币", en = "Cleared! %s Game Coins")
    private static final String TEXT_WON = "gtocore.machine.minesweeper.text.won";
    @RegisterLanguage(cn = "踩到雷了，本局没有奖励", en = "Hit a mine — no reward this round")
    private static final String TEXT_LOST = "gtocore.machine.minesweeper.text.lost";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.minesweeper.button.restart";
    @RegisterLanguage(cn = "展开盘面", en = "Unfold board")
    private static final String BUTTON_UNFOLD = "gtocore.machine.minesweeper.button.unfold";
    @RegisterLanguage(cn = "收起盘面", en = "Pack up board")
    private static final String BUTTON_COLLAPSE = "gtocore.machine.minesweeper.button.collapse";
    @RegisterLanguage(cn = "盘面还没铺开", en = "The board is packed up")
    private static final String REASON_NOT_UNFOLDED = "gtocore.machine.minesweeper.reason.not_unfolded";
    @RegisterLanguage(cn = "盘面已经铺开了", en = "The board is already unfolded")
    private static final String REASON_UNFOLDED = "gtocore.machine.minesweeper.reason.unfolded";
    @RegisterLanguage(cn = "位置被挡住了，先把机器旁边的空地清出来", en = "The area is obstructed — clear the ground next to the machine first")
    private static final String MSG_OBSTRUCTED = "gtocore.machine.minesweeper.msg.obstructed";

    /** 踩雷时每颗雷的爆炸威力：踩中的那颗大一点，其余小一点。 */
    private static final float DEATH_EXPLOSION_POWER = 3.0F;
    private static final float MINE_EXPLOSION_POWER = 1.5F;
    /**
     * 一局最多炸几下：大盘的雷有几百颗，全炸会把服务器与客户端一起拖垮；踩中的那颗必炸，其余按固定间隔抽。
     */
    public static final int MAX_EXPLOSIONS = 24;

    /** 盘面容量：按自定义档的最大边长分配，字段长度固定（换难度只改读到哪里，不换数组引用）。 */
    private static final int CELL_CAPACITY = MinesweeperRules.CUSTOM_MAX_SIDE * MinesweeperRules.CUSTOM_MAX_SIDE;

    /** 盘面边长输入框的步长（默认 / Shift / Ctrl / Ctrl+Shift）。 */
    private static final long[] SIDE_STEPS = { 1, 4, 16, 32 };
    /** 雷数输入框的步长：盘面最大 64×64，雷数能到四千多。 */
    private static final long[] MINE_STEPS = { 1, 16, 128, 1024 };
    /** 配置页里输入框前面那一列名字的宽度：两个汉字足够。 */
    private static final int LABEL_WIDTH = 40;

    /** 难度（{@link MinesweeperRules.Difficulty} 的序数）。 */
    @SaveToDisk
    private int difficulty = MinesweeperRules.Difficulty.EASY.ordinal();
    /**
     * 自定义档的宽、高与雷数：只在自定义档生效，换到预设档时原样留着。默认值与地狱档一致（30×30、249 雷），
     * 下发到客户端是为了配置页能直接显示这组数（输入框与那行「当前自定义」都读它们）。
     */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int customWidth = MinesweeperRules.Difficulty.HELL.width();
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int customHeight = MinesweeperRules.Difficulty.HELL.height();
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int customMines = MinesweeperRules.Difficulty.HELL.mineCount();
    /** 盘面是否已经铺开（显示方块与边框还在世界里）。 */
    @SaveToDisk
    private boolean unfolded;
    /** 铺开时的盘面宽高：各档尺寸改过之后，读档能凭它认出「地上那片是旧尺寸」，收掉再重铺。 */
    @SaveToDisk
    private int boardWidth;
    @SaveToDisk
    private int boardHeight;
    /** 这一面盘是否已经结过账，一面盘只兑现一次。 */
    @SaveToDisk
    private boolean settled;
    /** 雷盘，按行优先存 {@code width * height} 格；第一次翻开之前全是 false。只在铺开盘面时存盘。 */
    @SaveToDisk(skipWhen = "boardPackedUp")
    private boolean[] mines = new boolean[CELL_CAPACITY];
    /** 显示盘，取值见 {@link MinesweeperRules}。只在铺开盘面时存盘。 */
    @SaveToDisk(skipWhen = "boardPackedUpCells")
    private int[] cells = new int[CELL_CAPACITY];

    /** 按当前难度建的规则实例；读档与换难度时重建（临时容器跟着盘面尺寸分配）。 */
    private MinesweeperRules rules = MinesweeperRules.create(MinesweeperRules.Difficulty.EASY);
    /** 一次落子改动过的格子，只拿来刷新这几个显示方块。 */
    private final IntArrayList touched = new IntArrayList();

    public MinesweeperCoreMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** 难度名字的 lang key，工具提示与配置页共用。 */
    public static String difficultyName(MinesweeperRules.Difficulty preset) {
        return DIFFICULTY_NAMES[preset.ordinal()];
    }

    /**
     * 通道里下发的界面快照：难度、盘面尺寸、雷数与奖励、已插旗数、局面阶段，相等性只看版本号。
     *
     * <p>
     * 尺寸与雷数写进快照，是为了自定义档改了宽高之后客户端不用自己再算一遍（服务端夹好的值才作数）。
     */
    public record BoardSnapshot(int revision, int difficulty, int width, int height, int mineCount, long maxReward,
                                int flags, int state) {

        /** 盘面收起（还没铺开）。 */
        private static final int STATE_COLLAPSED = 0;
        /** 铺开且还在进行中。 */
        private static final int STATE_PLAYING = 1;
        /** 全部非雷格翻开，已兑现。 */
        private static final int STATE_WON = 2;
        /** 踩到雷，已结算。 */
        private static final int STATE_LOST = 3;

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, 0, 0, 0, 0, 0, 0, STATE_COLLAPSED);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT, BoardSnapshot::difficulty,
                ByteBufCodecs.VAR_INT, BoardSnapshot::width,
                ByteBufCodecs.VAR_INT, BoardSnapshot::height,
                ByteBufCodecs.VAR_INT, BoardSnapshot::mineCount,
                ByteBufCodecs.VAR_LONG, BoardSnapshot::maxReward,
                ByteBufCodecs.VAR_INT, BoardSnapshot::flags,
                ByteBufCodecs.VAR_INT, BoardSnapshot::state,
                BoardSnapshot::new);

        /** 当前档位；网络来的序数一律夹回合法范围。 */
        MinesweeperRules.Difficulty preset() {
            var all = MinesweeperRules.Difficulty.ALL;
            return all[Math.clamp(difficulty, 0, all.length - 1)];
        }

        boolean collapsed() {
            return state == STATE_COLLAPSED;
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

    /** 读档后的收尾：难度与自定义尺寸夹回合法值并按它重建规则。只在服务端调用。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        rebuildRules();
        sanitizeBoard();
    }

    /**
     * 地上那片盘面还是不是当前档位的尺寸：档位尺寸改过（旧存档）或者有人改过盘面文件时，按记录下来的旧尺寸把方块收干净，
     * 当作没铺开——留着会让落子按下标错位。
     */
    private void sanitizeBoard() {
        if (!unfolded) return;
        if (boardWidth == rules.width() && boardHeight == rules.height()) return;
        removeLayout();
        unfolded = false;
        boardWidth = 0;
        boardHeight = 0;
        newBoard();
    }

    /**
     * 按当前档位重建规则：预设档直接用枚举里的尺寸与奖励；自定义档把宽高雷数夹进合法范围，并把夹好的值写回字段，
     * 界面上的输入框与状态行才不会跟规则各说一套。
     */
    private void rebuildRules() {
        var preset = MinesweeperRules.Difficulty.of(difficulty);
        if (preset == null) preset = MinesweeperRules.Difficulty.EASY;
        difficulty = preset.ordinal();
        if (preset.custom()) {
            rules = MinesweeperRules.createCustom(customWidth, customHeight, customMines);
            customWidth = rules.width();
            customHeight = rules.height();
            customMines = rules.mineCount();
        } else {
            rules = MinesweeperRules.create(preset);
        }
    }

    @Override
    protected BoardSnapshot createView() {
        return new BoardSnapshot(revision(), difficulty, rules.width(), rules.height(), rules.mineCount(),
                rules.maxReward(), rules.flags(cells), stateOrdinal());
    }

    /** 局面阶段：收起 / 进行中 / 通关 / 踩雷，全部从盘面现推。 */
    private int stateOrdinal() {
        if (!unfolded) return BoardSnapshot.STATE_COLLAPSED;
        if (rules.isLost(cells)) return BoardSnapshot.STATE_LOST;
        if (rules.isWon(mines, cells)) return BoardSnapshot.STATE_WON;
        return BoardSnapshot.STATE_PLAYING;
    }

    /** 这一面盘现在能兑现多少币（只有全部完成才非 0；自定义档恒为 0）。 */
    private long payout() {
        return rules.payout(rules.foundMines(mines, cells), rules.isWon(mines, cells));
    }

    /** 铺一片空盘：定长数组原地归零，尾巴一直是「未翻开 / 不是雷」。 */
    private void newBoard() {
        Arrays.fill(mines, false);
        Arrays.fill(cells, MinesweeperRules.UNOPENED);
        settled = false;
    }

    // ==================== 界面动作 ====================

    /** 铺开盘面：先把要占的空地检查一遍，再一格一个显示方块铺出去，外圈围一圈边框。位置被挡住时给点的人一句提示。 */
    private void unfoldOnServer(@Nullable ServerPlayer player) {
        if (isRemote() || unfolded) return;
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        int width = rules.width();
        int height = rules.height();
        BlockPos core = getPos();
        for (int x = 0; x <= width + 1; x++) {
            for (int z = 0; z <= height + 1; z++) {
                BlockPos pos = gridPos(x, z);
                if (pos.equals(core) || serverLevel.isEmptyBlock(pos)) continue;
                send(player, Component.translatable(MSG_OBSTRUCTED));
                return;
            }
        }
        newBoard();
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                BlockPos pos = cellPos(x, z);
                serverLevel.setBlock(pos, MinesweeperDisplayBlock.displayState(MinesweeperRules.UNOPENED),
                        Block.UPDATE_CLIENTS);
                if (serverLevel.getBlockEntity(pos) instanceof MinesweeperDisplayBlockEntity display) {
                    display.setCore(core);
                }
            }
        }
        BlockState frameState = GTOBlocks.MINESWEEPER_FRAME.get().defaultBlockState();
        for (int x = 0; x <= width + 1; x++) {
            for (int z = 0; z <= height + 1; z++) {
                if (x >= 1 && x <= width && z >= 1 && z <= height) continue;
                BlockPos framePos = gridPos(x, z);
                if (!framePos.equals(core)) serverLevel.setBlock(framePos, frameState, Block.UPDATE_CLIENTS);
            }
        }
        unfolded = true;
        boardWidth = width;
        boardHeight = height;
        syncAndMarkChanged();
    }

    /** 收起盘面：先把这一局按规矩结掉（没通关就是 0 币），再拆掉显示方块、边框并把盘面清空。 */
    private void collapseOnServer(@Nullable ServerPlayer player) {
        if (isRemote() || !unfolded) return;
        settle(player);
        removeLayout();
        unfolded = false;
        boardWidth = 0;
        boardHeight = 0;
        newBoard();
        syncAndMarkChanged();
    }

    /** 服务端：「重新开始」：结掉当前这一局，再按同一难度重铺一盘。只有铺开时才有意义。 */
    private void restartOnServer(@Nullable ServerPlayer player) {
        if (isRemote() || !unfolded) return;
        settle(player);
        newBoard();
        mirrorAll();
        syncAndMarkChanged();
    }

    /**
     * 服务端：换难度。当前这一局先结账并收摊（难度变了盘面尺寸也对不上），换了之后由玩家自己点「展开盘面」重新开局。
     *
     * @param ordinal 配置页按钮的序号，与 {@link MinesweeperRules.Difficulty} 的序数一一对应
     */
    private void switchDifficulty(int ordinal) {
        if (isRemote() || ordinal == difficulty) return;
        if (MinesweeperRules.Difficulty.of(ordinal) == null) return;
        collapseOnServer(null);
        difficulty = ordinal;
        rebuildRules();
        newBoard();
        syncAndMarkChanged();
    }

    /** 服务端：改自定义宽度（已由输入框夹过一次，这里再夹一次免得存档/网络来的值越界）。 */
    private void setCustomWidth(int width) {
        if (isRemote()) return;
        int clamped = Math.clamp(width, MinesweeperRules.CUSTOM_MIN_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE);
        if (clamped == customWidth) return;
        customWidth = clamped;
        applyCustomShape();
    }

    private void setCustomHeight(int height) {
        if (isRemote()) return;
        int clamped = Math.clamp(height, MinesweeperRules.CUSTOM_MIN_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE);
        if (clamped == customHeight) return;
        customHeight = clamped;
        applyCustomShape();
    }

    private void setCustomMines(int mines) {
        if (isRemote()) return;
        int clamped = MinesweeperRules.clampMines(customWidth, customHeight, mines);
        if (clamped == customMines) return;
        customMines = clamped;
        applyCustomShape();
    }

    /** 自定义盘面参数变了：雷数按新尺寸重新夹紧；正在玩自定义盘的话先结账收摊，再按新参数重铺。 */
    private void applyCustomShape() {
        customMines = MinesweeperRules.clampMines(customWidth, customHeight, customMines);
        if (rules.custom()) {
            collapseOnServer(null);
            rebuildRules();
            newBoard();
        }
        syncAndMarkChanged();
    }

    /** 自定义盘面这组数（服务端夹紧后的值，随字段下发），配置页那一行「当前自定义」用它。 */
    private Component customShapeText() {
        return Component.translatable(TEXT_CUSTOM_SUMMARY, customWidth, customHeight, customMines);
    }

    /** 自定义档的雷数上限（输入框用它限制可输入范围）。 */
    private int maxCustomMines() {
        return Math.max(1, customWidth * customHeight - 9);
    }

    // ==================== 落子 ====================

    /** 显示方块被点：右击翻开、潜行右击插旗；数字格周围旗数对上了就连开一圈。 */
    public void click(BlockPos displayPos, @Nullable ServerPlayer player, boolean flag) {
        if (isRemote() || !unfolded || settled) return;
        int cell = cellAt(displayPos);
        if (cell < 0) return;
        touched.clear();
        boolean changed;
        if (flag) {
            changed = rules.toggleFlag(cells, cell, touched);
        } else if (cells[cell] >= MinesweeperRules.NUMBER_MIN) {
            changed = chord(cell);
        } else {
            changed = rules.open(mines, cells, cell, random(), touched);
        }
        if (!changed) return;
        mirrorTouched();
        if (rules.isOver(mines, cells)) settle(player);
        syncAndMarkChanged();
    }

    /** 数字格：周围旗数与数字相等时，把周围还扣着的格子一次翻开（可能踩雷，也可能直接通关）。 */
    private boolean chord(int cell) {
        int flagsAround = 0;
        for (int direction = 0; direction < MinesweeperRules.DIRECTIONS; direction++) {
            int next = rules.neighbour(cell, direction);
            if (next >= 0 && cells[next] == MinesweeperRules.FLAGGED) flagsAround++;
        }
        if (flagsAround != cells[cell]) return false;
        boolean changed = false;
        for (int direction = 0; direction < MinesweeperRules.DIRECTIONS; direction++) {
            int next = rules.neighbour(cell, direction);
            if (next < 0 || cells[next] != MinesweeperRules.UNOPENED) continue;
            changed |= rules.open(mines, cells, next, random(), touched);
            if (rules.isLost(cells)) break;
        }
        return changed;
    }

    /**
     * 结算：全部通关（或雷全找到）才把奖励记进余额（同一面盘只给一次），踩雷时放个响并告诉点的人。
     * 中途重开、收起、换难度、拆机器也会调到这里，没通关就是 0 币。
     *
     * @param player 点出这一下的人，界面上没有（拆机器）时为 {@code null}
     */
    private void settle(@Nullable ServerPlayer player) {
        if (settled) return;
        settled = true;
        long amount = payout();
        if (amount > 0) addBalance(amount);
        if (rules.isLost(cells)) {
            explode();
            send(player, Component.translatable(TEXT_LOST));
        } else if (amount > 0) {
            send(player, Component.translatable(TEXT_WON, number(amount)));
        }
    }

    /** 拆除机器时先把盘面收干净，余额由基类按面额掉回世界。 */
    @Override
    public void onMachineRemoved() {
        removeLayout();
        super.onMachineRemoved();
    }

    /** 拆除机器、收起盘面等时机都会调：把还没结账的成绩折进余额。 */
    @Override
    protected void cashOut() {
        settle(null);
    }

    /**
     * 踩雷的动静：踩中的那颗按威力炸一次，其余雷按固定间隔抽到 {@link #MAX_EXPLOSIONS} 为止——每处都有爆炸声、
     * 对生物造成伤害与击退，但不破坏方块（{@link Level.ExplosionInteraction#NONE} 只影响实体、不动方块）。
     */
    private void explode() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        int cellCount = rules.cellCount();
        int deathCell = -1;
        int mineTotal = 0;
        for (int cell = 0; cell < cellCount; cell++) {
            if (!mines[cell]) continue;
            mineTotal++;
            if (cells[cell] == MinesweeperRules.DEATH_BOMB) deathCell = cell;
        }
        if (mineTotal == 0) return;
        int budget = MAX_EXPLOSIONS;
        if (deathCell >= 0) {
            explodeAt(serverLevel, deathCell, DEATH_EXPLOSION_POWER);
            budget--;
        }
        if (budget <= 0) return;
        int stride = Math.max(1, mineTotal / budget);
        int seen = 0;
        for (int cell = 0; cell < cellCount && budget > 0; cell++) {
            if (!mines[cell] || cell == deathCell) continue;
            if (seen++ % stride != 0) continue;
            explodeAt(serverLevel, cell, MINE_EXPLOSION_POWER);
            budget--;
        }
    }

    private void explodeAt(ServerLevel level, int cell, float power) {
        BlockPos pos = displayPos(cell);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, power,
                Level.ExplosionInteraction.NONE);
    }

    // ==================== 盘面方块 ====================

    /** 只把盘面方块拆掉：显示方块与边框；机器自己不动。按「实际铺开时的尺寸」收，尺寸改过也收得干净。 */
    private void removeLayout() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        int width = boardWidth > 0 ? boardWidth : rules.width();
        int height = boardHeight > 0 ? boardHeight : rules.height();
        for (int x = 0; x <= width + 1; x++) {
            for (int z = 0; z <= height + 1; z++) {
                BlockPos pos = gridPos(x, z);
                Block block = serverLevel.getBlockState(pos).getBlock();
                if (block instanceof MinesweeperDisplayBlock || block instanceof MinesweeperFrameBlock) {
                    serverLevel.removeBlock(pos, false);
                }
            }
        }
    }

    /** 把一次落子改动过的显示方块刷成新状态（{@link #touched} 里可能有重复，重复刷同一个格子无害）。 */
    private void mirrorTouched() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        for (int index = 0; index < touched.size(); index++) {
            mirror(serverLevel, touched.getInt(index));
        }
    }

    /** 把整盘刷一遍：重开一局时所有格子都回到「未翻开」，只能逐格刷。 */
    private void mirrorAll() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;
        for (int cell = 0; cell < rules.cellCount(); cell++) {
            mirror(serverLevel, cell);
        }
    }

    private void mirror(ServerLevel level, int cell) {
        if (cell < 0 || cell >= rules.cellCount()) return;
        BlockPos pos = displayPos(cell);
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof MinesweeperDisplayBlock)) return;
        int wanted = MinesweeperDisplayBlock.propertyFor(cells[cell]);
        if (state.getValue(MinesweeperDisplayBlock.STATE) != wanted) {
            level.setBlock(pos, state.setValue(MinesweeperDisplayBlock.STATE, wanted), Block.UPDATE_CLIENTS);
        }
    }

    /** 棋盘网格坐标（{@code gx} 向 +X、{@code gz} 向 -Z）对应的世界坐标；机器自己就是 (0, 0)。 */
    private BlockPos gridPos(int gx, int gz) {
        return getPos().offset(gx, 0, -gz);
    }

    /** 第 {@code (x, z)} 格（0 起，棋盘在第 1 圈边框之内）的世界坐标。 */
    private BlockPos cellPos(int x, int z) {
        return gridPos(x + 1, z + 1);
    }

    private BlockPos displayPos(int cell) {
        int width = rules.width();
        return cellPos(cell % width, cell / width);
    }

    /** 显示方块的位置 → 格子下标；不在棋盘范围内返回 -1。 */
    private int cellAt(BlockPos displayPos) {
        int width = rules.width();
        int height = rules.height();
        int x = displayPos.getX() - getPos().getX() - 1;
        int z = getPos().getZ() - displayPos.getZ() - 1;
        if (x < 0 || x >= width || z < 0 || z >= height) return -1;
        return z * width + x;
    }

    // ==================== 界面 ====================

    /** 主页 = 局面面板 + 状态面板；顺序两端必须一致，通道里的条目编号才对得上。 */
    @Override
    public Widget createUIWidget() {
        var page = Form.page();
        page.addChildren(createBoardArea(), createStatusPanel());
        return page;
    }

    /** 主页之外再挂一个「配置」子页，换难度与自定义盘面参数都在里面。 */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new BoardConfigPage());
    }

    /** 局面面板：难度、这一局的进度与操作说明，快照挂在这一层上。 */
    private UIElement createBoardArea() {
        var area = panel();
        registerView(area, BoardSnapshot.EMPTY, BoardSnapshot.CODEC);
        area.addChildren(
                TextLine.of(LayoutStyle.AUTO, this::difficultyText).styled().setTextAlign(Horizontal.CENTER),
                TextLine.of(LayoutStyle.AUTO, this::stateText).styled().setTextAlign(Horizontal.CENTER));
        return area;
    }

    /** 状态面板：余额 + 展开/收起 + 重新开始/取出全部。 */
    private StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance()));

        var unfold = Button.translatable(77, BUTTON_UNFOLD)
                .clientDisabled(() -> !view().collapsed(), REASON_UNFOLDED);
        unfold.setOnServerClick(() -> unfoldOnServer(playerOf(unfold)));
        var collapse = Button.translatable(77, BUTTON_COLLAPSE)
                .clientDisabled(() -> view().collapsed(), REASON_NOT_UNFOLDED);
        collapse.setOnServerClick(() -> collapseOnServer(playerOf(collapse)));
        var restart = Button.translatable(77, BUTTON_RESTART)
                .clientDisabled(() -> view().collapsed(), REASON_NOT_UNFOLDED);
        restart.setOnServerClick(() -> restartOnServer(playerOf(restart)));

        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(unfold, collapse));
        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, createWithdrawButton(77)));
        return status;
    }

    /** 带底图的面板，行距统一 2px；局面面板、状态面板与配置页都用它。 */
    private static StatusPanel panel() {
        var panel = new StatusPanel();
        panel.getLayoutStyle().gapAll(2);
        return panel;
    }

    /** 难度行：档位、盘面尺寸、雷数与全找到时的奖励；自定义档换成「不计奖励」。 */
    private Component difficultyText() {
        var snapshot = view();
        if (snapshot.preset().custom()) {
            return Component.translatable(TEXT_CUSTOM, snapshot.width(), snapshot.height(), snapshot.mineCount());
        }
        return Component.translatable(TEXT_DIFFICULTY, Component.translatable(difficultyName(snapshot.preset())),
                snapshot.width(), snapshot.height(), FormattingUtil.formatNumbers(snapshot.mineCount()),
                FormattingUtil.formatNumbers(snapshot.maxReward()));
    }

    /** 局面行：收起 / 已插旗 / 通关 / 踩雷，全部由服务端状态推出来。 */
    private Component stateText() {
        var snapshot = view();
        return switch (snapshot.state()) {
            case BoardSnapshot.STATE_WON -> Component.translatable(TEXT_WON, number(snapshot.maxReward()));
            case BoardSnapshot.STATE_LOST -> Component.translatable(TEXT_LOST);
            case BoardSnapshot.STATE_PLAYING -> Component.translatable(TEXT_FLAGS,
                    number(snapshot.flags()), number(snapshot.mineCount()));
            default -> Component.translatable(TEXT_IDLE);
        };
    }

    /** 按钮背后要的玩家：界面的持有者，服务端才拿得到。 */
    private static @Nullable ServerPlayer playerOf(Widget source) {
        var gui = source.getGui();
        return gui != null && gui.entityPlayer instanceof ServerPlayer player ? player : null;
    }

    private static void send(@Nullable ServerPlayer player, Component message) {
        if (player != null) player.displayClientMessage(message, true);
    }

    /** 配置页：六个难度（按钮上只写档位名，尺寸/雷数/奖励放悬浮提示）+ 自定义盘面的宽/高/雷数，最后是余额面板。 */
    private final class BoardConfigPage implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();

            var difficulties = Form.section(SECTION_DIFFICULTY);
            for (int ordinal = 0; ordinal < DIFFICULTY_NAMES.length; ordinal++) {
                // 循环变量不是有效 final，先拷一份给 lambda 用
                int index = ordinal;
                var preset = MinesweeperRules.Difficulty.ALL[index];
                var button = Button.translatable(LayoutStyle.AUTO, DIFFICULTY_NAMES[index]);
                if (preset.custom()) {
                    button.setHoverTooltips(Component.translatable(TEXT_CUSTOM_DETAIL),
                            Component.translatable(TEXT_CUSTOM_HINT, MinesweeperRules.CUSTOM_MIN_SIDE,
                                    MinesweeperRules.CUSTOM_MAX_SIDE));
                } else {
                    button.setHoverTooltips(Component.translatable(TEXT_DIFFICULTY_DETAIL, preset.width(),
                            preset.height(), FormattingUtil.formatNumbers(preset.mineCount()),
                            FormattingUtil.formatNumbers(preset.maxReward())));
                }
                button.setOnServerClick(() -> switchDifficulty(index));
                button.setSelected(() -> view().preset().ordinal() == index);
                difficulties.addChild(button);
            }
            page.addChild(difficulties);

            var custom = Form.section(SECTION_CUSTOM);
            custom.addChild(adjusterRow(LINE_WIDTH, NumberField.ofInt(LayoutStyle.AUTO,
                    () -> customWidth, MinesweeperCoreMachine.this::setCustomWidth,
                    MinesweeperRules.CUSTOM_MIN_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE).setSteps(SIDE_STEPS)));
            custom.addChild(adjusterRow(LINE_HEIGHT, NumberField.ofInt(LayoutStyle.AUTO,
                    () -> customHeight, MinesweeperCoreMachine.this::setCustomHeight,
                    MinesweeperRules.CUSTOM_MIN_SIDE, MinesweeperRules.CUSTOM_MAX_SIDE).setSteps(SIDE_STEPS)));
            custom.addChild(adjusterRow(LINE_MINES, NumberField.ofInt(LayoutStyle.AUTO,
                    () -> customMines, MinesweeperCoreMachine.this::setCustomMines,
                    () -> 1, MinesweeperCoreMachine.this::maxCustomMines).setSteps(MINE_STEPS)));
            custom.addChild(TextLine.of(LayoutStyle.AUTO, MinesweeperCoreMachine.this::customShapeText).styled());
            page.addChild(custom);

            var status = panel();
            status.addLine(LINE_BALANCE, () -> number(balance()));
            page.addChild(status);
            return page;
        }

        @Override
        public IGuiTexture getTabIcon() {
            return WidgetIcons.SETTINGS;
        }

        @Override
        public Component getTitle() {
            return Component.translatable(PAGE_SETTINGS);
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(Component.translatable(PAGE_SETTINGS_TOOLTIP));
        }

        @Override
        public boolean hasPlayerInventory() {
            return false;
        }
    }

    /** 一行「名字 + 输入框」：名字占固定一列，输入框吃掉剩下的横向空间，增减按钮按步长文字自适应。 */
    private static UIElement adjusterRow(String labelKey, Adjuster field) {
        var row = UIElement.row(UISizes.CONTROL_HEIGHT).layout(style -> style.gapAll(UISizes.GAP));
        row.addChildren(UIElement.column(LABEL_WIDTH).addChild(TextLine.translatable(LayoutStyle.AUTO, labelKey)), field);
        return row;
    }

    // ==================== 存盘跳过 ====================

    /** 盘面收起时不写雷盘（全 false 没有意义）；参数类型决定 DataSyncLib 认哪个重载。 */
    private boolean boardPackedUp(boolean[] mines) {
        return !unfolded;
    }

    /** 盘面收起时不写显示盘（全未翻开没有意义）。 */
    private boolean boardPackedUpCells(int[] cells) {
        return !unfolded;
    }
}

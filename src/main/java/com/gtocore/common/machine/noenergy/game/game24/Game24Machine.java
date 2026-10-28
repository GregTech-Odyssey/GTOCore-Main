package com.gtocore.common.machine.noenergy.game.game24;

import com.gtocore.common.machine.noenergy.game.AbstractGameMachine;
import com.gtocore.common.machine.noenergy.game.milestone.GameMilestones;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 「24点」游戏机：{@link Game24Rules#NUMBER_COUNT} 张牌（按 {@link Game24Rules.DrawRange} 抽 1~10 或 1~13），
 * 用 + − × ÷ 与括号把它们各用一次拼成 {@link Game24Rules#TARGET}。
 *
 * <p>
 * 界面只用按钮与文本：一张牌、一个运算符、一个括号都是一个按钮，点一下往表达式尾巴上追加一个 token；表达式、反馈与
 * 「哪张牌已经用掉」都由服务端算好下发（快照 + 文字各走自己的同步项）。答对一题按是否用过提示发游戏币进余额，
 * 换一题随时可点（放弃不扣钱）。余额、取出与界面骨架都在 {@link AbstractGameMachine} 里，这里只管题目与答题。
 */
@DataGeneratorScanned
public class Game24Machine extends AbstractGameMachine<Game24Machine.View> {

    @RegisterLanguage(cn = "撤回", en = "Undo")
    private static final String BUTTON_UNDO = "gtocore.machine.game_24.button.undo";
    @RegisterLanguage(cn = "清空", en = "Clear")
    private static final String BUTTON_CLEAR = "gtocore.machine.game_24.button.clear";
    @RegisterLanguage(cn = "提交", en = "Submit")
    private static final String BUTTON_SUBMIT = "gtocore.machine.game_24.button.submit";
    @RegisterLanguage(cn = "提示", en = "Hint")
    private static final String BUTTON_HINT = "gtocore.machine.game_24.button.hint";
    @RegisterLanguage(cn = "换一题", en = "New puzzle")
    private static final String BUTTON_NEXT = "gtocore.machine.game_24.button.next";
    @RegisterLanguage(cn = "这张牌已经用过了", en = "This card is already used")
    private static final String REASON_CARD_USED = "gtocore.machine.game_24.reason.card_used";
    @RegisterLanguage(cn = "这一题已经答对了，点「换一题」继续", en = "This puzzle is solved — hit New puzzle to continue")
    private static final String REASON_SOLVED = "gtocore.machine.game_24.reason.solved";
    @RegisterLanguage(cn = "表达式还是空的", en = "The expression is still empty")
    private static final String REASON_EMPTY = "gtocore.machine.game_24.reason.empty";
    @RegisterLanguage(cn = "本题已经用过提示了", en = "A hint was already used for this puzzle")
    private static final String REASON_HINTED = "gtocore.machine.game_24.reason.hinted";
    @RegisterLanguage(cn = "结果不是 24，再改改", en = "That is not 24 — try again")
    private static final String TEXT_WRONG = "gtocore.machine.game_24.text.wrong";
    @RegisterLanguage(cn = "答对了！", en = "Correct!")
    private static final String TEXT_SOLVED = "gtocore.machine.game_24.text.solved";
    @RegisterLanguage(cn = "完成 %s 题！+%s 游戏币", en = "%s puzzles done! +%s Game Coins")
    private static final String TEXT_REWARD = "gtocore.machine.game_24.text.reward";
    @RegisterLanguage(cn = "%s = 24", en = "%s = 24")
    private static final String TEXT_HINT = "gtocore.machine.game_24.text.hint";
    @RegisterLanguage(cn = "抽牌范围", en = "Card range")
    private static final String SECTION_DRAW_RANGE = "gtocore.machine.game_24.section.draw_range";
    @RegisterLanguage(cn = "1 ~ 10", en = "1 to 10")
    private static final String RANGE_TEN = "gtocore.machine.game_24.range.ten";
    @RegisterLanguage(cn = "1 ~ 13", en = "1 to 13")
    private static final String RANGE_THIRTEEN = "gtocore.machine.game_24.range.thirteen";
    @RegisterLanguage(cn = "%s 币 / %s 题", en = "%s coins per %s puzzles")
    private static final String TEXT_RANGE_REWARD = "gtocore.machine.game_24.text.range_reward";
    @RegisterLanguage(cn = "连解 %s / %s 题", en = "Run %s / %s puzzles")
    private static final String TEXT_RUN = "gtocore.machine.game_24.text.run";
    @RegisterLanguage(cn = "奖励 %s / %s 题", en = "Reward %s / %s puzzles")
    private static final String TEXT_REWARD_PROGRESS = "gtocore.machine.game_24.text.reward_progress";
    @RegisterLanguage(cn = "用过提示：这一题不计入奖励，连解榜单那一轮也作废",
                      en = "Hint used — this puzzle does not count towards the reward, and the streak no longer counts")
    private static final String MSG_RUN_VOID = "gtocore.machine.game_24.msg.run_void";
    @RegisterLanguage(cn = "1~10 榜", en = "1 to 10 board")
    private static final String SECTION_TEN_BOARD = "gtocore.machine.game_24.section.ten_board";
    @RegisterLanguage(cn = "1~13 榜", en = "1 to 13 board")
    private static final String SECTION_THIRTEEN_BOARD = "gtocore.machine.game_24.section.thirteen_board";

    /** 里程碑表格的列（名次由表格自动加在最前）：玩家 + 10 题用时，达成时间放在用时格的悬停提示里。 */
    private static final MilestoneColumn[] MILESTONE_COLUMNS = {
            MilestoneColumn.left(COLUMN_PLAYER, 70),
            MilestoneColumn.center(COLUMN_DURATION, 56) };

    /** 一轮里程碑的题数：连解这么多题才算一次用时，也才结一次奖励。 */
    public static final int RUN_PUZZLES = 10;
    /** 每张榜单的可见高度：四行。 */

    /** 范围按钮上的名字，下标与 {@link Game24Rules.DrawRange} 的序数一一对应。 */
    private static final String[] RANGE_NAMES = { RANGE_TEN, RANGE_THIRTEEN };

    private static final int CARD_WIDTH = 36;
    private static final int TOKEN_WIDTH = 23;
    private static final int ACTION_WIDTH = 48;
    private static final int WITHDRAW_WIDTH = 136;

    private static final int RESULT_NONE = 0;
    private static final int RESULT_SOLVED = 1;
    private static final int RESULT_WRONG = 2;
    /** 刚答对的那题正好凑满 10 题、结了奖励：反馈显示结算，而不是「答对了」。 */
    private static final int RESULT_REWARDED = 3;

    /** 「还没发牌」用空数组而不是 null；首次打开界面时会立刻发一局。 */
    private static final int[] NO_NUMBERS = new int[0];
    private static final int[] NO_TOKENS = new int[0];

    private final Game24Rules rules = Game24Rules.create();

    /** 服务端权威牌面，{@link Game24Rules#NUMBER_COUNT} 张。 */
    @SaveToDisk
    private int[] numbers = NO_NUMBERS;
    /** 抽牌范围（{@link Game24Rules.DrawRange} 的序数）；默认整副 1~13，和旧存档行为一致。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int drawRange = Game24Rules.DrawRange.ONE_TO_THIRTEEN.ordinal();
    /** 玩家拼到一半的表达式，按 token 顺序存。 */
    @SaveToDisk
    private int[] tokens = NO_TOKENS;
    /** 上一次提交的结果：只影响反馈文案与按钮禁用。 */
    @SaveToDisk
    private int result = RESULT_NONE;
    /** 本题是否用过提示（用过之后这一轮连解作废，这一题也不计入奖励进度）。 */
    @SaveToDisk
    private boolean hinted;
    /** 这一轮连解算不算数（只管榜单用时）：用提示、没答对就换题、换范围或多人的一轮作废。 */
    @SaveToDisk
    private boolean runValid;
    /** 本轮连解进度与起点（存档 gameTime，{@code 0} 表示这一轮已经收尾）。 */
    @SaveToDisk
    private int runSolved;
    @SaveToDisk
    private long runStart;
    /**
     * 奖励进度：单独累计「解出的题数」，不够十题也一直留着。用提示的那题不计，换题与多人同局都不影响，换抽牌范围会清零；
     * 每满 {@link #RUN_PUZZLES} 题结一次 {@link Game24Rules.DrawRange#runReward()}。
     */
    @SaveToDisk
    private int rewardProgress;

    public Game24Machine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /**
     * 通道里下发的界面快照：牌面、表达式、答对/答错/用过提示三个标记、连解进度与奖励进度。
     * 相等性只看版本号，省掉逐元素比数组。
     */
    public record View(int revision, int[] numbers, int[] tokens, int flags, int run, int reward) {

        private static final int FLAG_SOLVED = 1;
        private static final int FLAG_HINTED = 1 << 1;
        private static final View EMPTY = new View(-1, NO_NUMBERS, NO_TOKENS, 0, 0, 0);
        private static final StreamCodec<FriendlyByteBuf, View> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, View::revision,
                ByteBufCodecs.VAR_INT_ARRAY, View::numbers,
                ByteBufCodecs.VAR_INT_ARRAY, View::tokens,
                ByteBufCodecs.VAR_INT, View::flags,
                ByteBufCodecs.VAR_INT, View::run,
                ByteBufCodecs.VAR_INT, View::reward,
                View::new);

        boolean solved() {
            return (flags & FLAG_SOLVED) != 0;
        }

        boolean hinted() {
            return (flags & FLAG_HINTED) != 0;
        }

        /** 第 {@code index} 张牌是不是已经拼进表达式了。 */
        boolean isUsed(int index) {
            for (int token : tokens) {
                if (token == index) return true;
            }
            return false;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || other instanceof View view && revision == view.revision;
        }

        @Override
        public int hashCode() {
            return revision;
        }
    }

    // ==================== 局面 ====================

    /** 读档后的收尾，只在服务端调用。 */
    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    /**
     * 读档后的收尾：范围非法就回落整副 1~13；牌不对、或牌面超出当前范围（换过范围）就重发一局，token 里有非法项就把
     * 表达式丢掉，结果夹到合法取值。
     */
    private void sanitizeState() {
        var range = Game24Rules.DrawRange.of(drawRange);
        if (range == null) range = Game24Rules.DrawRange.ONE_TO_THIRTEEN;
        drawRange = range.ordinal();
        if (numbers.length != Game24Rules.NUMBER_COUNT) {
            startPuzzle();
        } else {
            for (int value : numbers) {
                if (value < range.min() || value > range.max()) {
                    startPuzzle();
                    break;
                }
            }
        }
        if (tokens.length > Game24Rules.MAX_TOKENS) {
            tokens = NO_TOKENS;
        } else {
            for (int token : tokens) {
                if (!Game24Rules.isToken(token)) {
                    tokens = NO_TOKENS;
                    break;
                }
            }
        }
        if (!isSolved() && result != RESULT_WRONG) result = RESULT_NONE;
        // 本轮进度与起点：进度夹回合法值，起点非法（0 或来自未来）就当这一轮没开始
        runSolved = Math.clamp(runSolved, 0, RUN_PUZZLES);
        if (runStart <= 0L || runStart > gameTime()) voidRun();
        // 奖励进度只在结账时减一档，正常永远 < RUN_PUZZLES；坏档夹回合法范围
        rewardProgress = Math.clamp(rewardProgress, 0, RUN_PUZZLES - 1);
    }

    @Override
    protected View createView() {
        int flags = (isSolved() ? View.FLAG_SOLVED : 0) | (hinted ? View.FLAG_HINTED : 0);
        return new View(revision(), numbers, tokens, flags, runSolved, rewardProgress);
    }

    /** 这一题答对了吗（不论有没有凑满十题）。 */
    private boolean isSolved() {
        return result == RESULT_SOLVED || result == RESULT_REWARDED;
    }

    /** 首次打开界面时把没发的那一题发出来，并保证有一轮在计时。 */
    private void ensureStarted() {
        if (numbers.length != Game24Rules.NUMBER_COUNT) startPuzzle();
        if (!runValid) startRun(null);
    }

    // ==================== 界面 ====================

    /** 主页 = 答题区 + 状态面板；答题区先建，通道里的快照项才排在状态面板之前。 */
    @Override
    public Widget createUIWidget() {
        if (!isRemote()) ensureStarted();

        var page = Form.page();
        page.addChildren(createGameArea(), createStatusPanel());
        return page;
    }

    /** 主页之外挂「难度」（抽牌范围）与「排行榜」两个子页。 */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new DifficultyPage());
        sideTabs.attachSubTab(new LeaderboardPage());
    }

    /** 答题区：表达式一行 + 数字牌一行 + 运算符一行 + 撤回/清空/提交一行。 */
    private UIElement createGameArea() {
        var board = panel();
        registerView(board, View.EMPTY, View.CODEC);
        board.addChildren(
                TextLine.of(LayoutStyle.AUTO, this::expressionText).styled().setTextAlign(Horizontal.CENTER),
                createCardRow(),
                createTokenRow(),
                createActionRow());
        return board;
    }

    /** 状态面板：余额 + 连解进度 + 奖励进度 + 反馈行 + 提示/换一题 + 取出全部。 */
    private StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance()));
        status.addChild(TextLine.of(LayoutStyle.AUTO, () -> Component.translatable(TEXT_RUN, view().run(), RUN_PUZZLES))
                .setTextAlign(Horizontal.CENTER));
        status.addChild(TextLine.of(LayoutStyle.AUTO, () -> Component.translatable(TEXT_REWARD_PROGRESS, view().reward(), RUN_PUZZLES))
                .setTextAlign(Horizontal.CENTER));
        status.addChild(TextLine.of(LayoutStyle.AUTO, this::feedbackText).setTextAlign(Horizontal.CENTER));
        status.addChild(createStatusActions());
        return status;
    }

    private UIElement createStatusActions() {
        var hint = Button.translatable(77, BUTTON_HINT)
                .clientDisabled(() -> view().hinted() || view().solved(), REASON_HINTED);
        hint.setOnServerClick(() -> useHint(playerOf(hint)));
        var next = Button.translatable(77, BUTTON_NEXT);
        next.setOnServerClick(() -> nextPuzzle(playerOf(next)));

        var actions = new UIElement();
        actions.layout(LayoutStyle::column);
        actions.getLayoutStyle().gapAll(2);
        actions.addChildren(
                centeredRow().addChildren(hint, next),
                centeredRow().addChildren(createWithdrawButton(WITHDRAW_WIDTH)));
        return actions;
    }

    // ==================== 局面状态 ====================

    /** 当前抽牌范围；存档/同步值异常时回落整副 1~13。 */
    private Game24Rules.DrawRange currentRange() {
        var range = Game24Rules.DrawRange.of(drawRange);
        return range == null ? Game24Rules.DrawRange.ONE_TO_THIRTEEN : range;
    }

    /** 发一局新牌：表达式、结果与提示标记全部归零。 */
    private void startPuzzle() {
        numbers = rules.deal(random(), currentRange());
        tokens = NO_TOKENS;
        result = RESULT_NONE;
        hinted = false;
    }

    /** 起一轮：起点、进度清零，并把「这一轮算不算」置位；发起者用开界面的人（没有就等第一个开界面的人）。 */
    private void startRun(@Nullable ServerPlayer player) {
        // 提示过的题不开启新一轮：否则关掉界面再打开时 ensureStarted 会把已经作废的一轮重新计时。
        if (hinted) return;
        runStart = gameTime();
        runSolved = 0;
        runValid = true;
        beginSession(player);
    }

    /** 本轮作废：计时、进度与「算不算」一起清零，下一次发牌重新起表。 */
    private void voidRun() {
        runStart = 0L;
        runSolved = 0;
        runValid = false;
    }

    /**
     * 答对一题：奖励与榜单各算各的。
     *
     * <ul>
     * <li>奖励：单独累计「解出的题数」（{@link #rewardProgress}），每完成 {@link #RUN_PUZZLES} 题结一次
     * {@link Game24Rules.DrawRange#runReward()}；用提示的那题不计，换题与多人同局都不影响，换抽牌范围清零；</li>
     * <li>榜单：同一轮里连解 {@link #RUN_PUZZLES} 题才记一次用时，用提示、没答对就换题、换范围或多人的一轮作废
     * （见 {@link #runValid}）。</li>
     * </ul>
     */
    private void onPuzzleSolved() {
        recordRunTime();
        // 用提示的那题不计数：既不加奖励进度，也不会把已经攒的进度清掉
        if (hinted) return;
        rewardProgress++;
        if (rewardProgress < RUN_PUZZLES) return;
        rewardProgress -= RUN_PUZZLES;
        addBalance(currentRange().runReward());
        result = RESULT_REWARDED;
    }

    /** 榜单那一半：连解满一轮就按本轮用时记一次（单人局且用时正常才算），然后重开一轮。 */
    private void recordRunTime() {
        // 用过提示的题不算连解；即使界面重开把 runValid 又置回来，这里也要把这一轮停掉。
        if (hinted) {
            voidRun();
            return;
        }
        if (!runValid) return;
        runSolved++;
        if (runSolved < RUN_PUZZLES) return;
        long elapsed = gameTime() - runStart;
        if (sessionCounts() && elapsed > 0) {
            var server = server();
            if (server != null) {
                GameMilestones.get(server).recordPuzzle(sessionStarter(), drawRange, elapsed, gameTime());
            }
        }
        voidRun();
    }

    /** 第 {@code index} 张牌是否已经拼进表达式。 */
    private boolean isCardUsed(int index) {
        for (int token : tokens) {
            if (token == index) return true;
        }
        return false;
    }

    // ==================== 服务端动作 ====================

    /** 点一张牌：没用过就接到表达式尾巴上；改表达式会把上一次「不是 24」的反馈抹掉。 */
    private void appendCard(int index, @Nullable ServerPlayer player) {
        if (isRemote() || isSolved() || !guard(player)) return;
        if (index < 0 || index >= numbers.length || isCardUsed(index)) return;
        if (tokens.length >= Game24Rules.MAX_TOKENS) return;
        tokens = append(tokens, index);
        result = RESULT_NONE;
        syncAndMarkChanged();
    }

    /** 点一个运算符/括号：同样只追加 token，语法留到提交时判。 */
    private void appendToken(int token, @Nullable ServerPlayer player) {
        if (isRemote() || isSolved() || !guard(player)) return;
        if (!Game24Rules.isToken(token) || Game24Rules.isNumberToken(token)) return;
        if (tokens.length >= Game24Rules.MAX_TOKENS) return;
        tokens = append(tokens, token);
        result = RESULT_NONE;
        syncAndMarkChanged();
    }

    /** 撤回最后一个 token。 */
    private void undo(@Nullable ServerPlayer player) {
        if (isRemote() || tokens.length == 0 || isSolved() || !guard(player)) return;
        tokens = Arrays.copyOf(tokens, tokens.length - 1);
        result = RESULT_NONE;
        syncAndMarkChanged();
    }

    /** 清空整个表达式。 */
    private void clearTokens(@Nullable ServerPlayer player) {
        if (isRemote() || tokens.length == 0 || isSolved() || !guard(player)) return;
        tokens = NO_TOKENS;
        result = RESULT_NONE;
        syncAndMarkChanged();
    }

    /** 提交：语法、用牌与数值全对才算解出；答对不直接给币，攒满一轮才结算。只是不对的话不扣钱，给一句反馈继续改。 */
    private void submit(@Nullable ServerPlayer player) {
        if (isRemote() || isSolved() || tokens.length == 0 || !guard(player)) return;
        if (!rules.isSolution(tokens, numbers)) {
            result = RESULT_WRONG;
            syncAndMarkChanged();
            return;
        }
        result = RESULT_SOLVED;
        onPuzzleSolved();
        syncAndMarkChanged();
    }

    /** 提示：显示一个解法；同一题只给一次，这一轮连解作废（进度清零，重新发牌后从 0 开始）。 */
    private void useHint(@Nullable ServerPlayer player) {
        if (isRemote() || hinted || isSolved() || !guard(player)) return;
        hinted = true;
        if (runValid) {
            voidRun();
            if (player != null) player.displayClientMessage(Component.translatable(MSG_RUN_VOID), true);
        }
        syncAndMarkChanged();
    }

    /** 换一题：放弃当前表达式重新发牌（没答对就换题的话本轮作废），答对后再点也是走这里。 */
    private void nextPuzzle(@Nullable ServerPlayer player) {
        if (isRemote() || !guard(player)) return;
        if (!isSolved() && runValid) voidRun();
        startPuzzle();
        if (!runValid) startRun(player);
        syncAndMarkChanged();
    }

    /** 服务端：换抽牌范围。当前这一题是按旧范围抽的，直接作废重发，本轮也重来。 */
    private void setDrawRange(int ordinal) {
        if (isRemote() || ordinal == drawRange || !guard(viewer())) return;
        var range = Game24Rules.DrawRange.of(ordinal);
        if (range == null) return;
        drawRange = range.ordinal();
        // 换档等于换一套牌：奖励进度清零，免得攒够九题再切档挑更高的奖励
        rewardProgress = 0;
        voidRun();
        startPuzzle();
        startRun(viewer());
        syncAndMarkChanged();
    }

    /** 追加一个 token；数组很短，直接重建一份（机器靠字段换引用确认状态变了）。 */
    private static int[] append(int[] tokens, int token) {
        int[] appended = Arrays.copyOf(tokens, tokens.length + 1);
        appended[tokens.length] = token;
        return appended;
    }

    // ==================== 界面 ====================

    /** 行内按钮横向居中：{@code centeredRow} 只对齐交叉轴，横向还要自己 justifyContent。 */
    private static UIElement centeredRow() {
        return UIElement.centeredRow(Button.HEIGHT).layout(style -> style.justifyContent(AlignContent.CENTER));
    }

    /** 4 张数字牌；牌面由同步快照现取（每局重发），出过的牌禁用。 */
    private UIElement createCardRow() {
        var row = centeredRow();
        for (int index = 0; index < Game24Rules.NUMBER_COUNT; index++) {
            row.addChild(createCard(index));
        }
        return row;
    }

    private Button createCard(int index) {
        var card = Button.of(CARD_WIDTH)
                .bindClientText(() -> {
                    int[] cards = view().numbers();
                    return index < cards.length ? Integer.toString(cards[index]) : "";
                })
                .clientDisabled(() -> view().isUsed(index) || view().solved(), REASON_CARD_USED);
        card.setOnServerClick(() -> appendCard(index, playerOf(card)));
        return card;
    }

    /** 运算符与括号按钮：字形就是 {@link Game24Rules#tokenText}，按钮上的字与表达式里显示的一致。 */
    private UIElement createTokenRow() {
        var row = centeredRow();
        for (int token = Game24Rules.ADD; token < Game24Rules.TOKEN_COUNT; token++) {
            // 循环变量不是有效 final，先拷一份给 lambda 用
            int code = token;
            var button = Button.translatable(TOKEN_WIDTH, Game24Rules.tokenText(code))
                    .clientDisabled(() -> view().solved(), REASON_SOLVED);
            button.setOnServerClick(() -> appendToken(code, playerOf(button)));
            row.addChild(button);
        }
        return row;
    }

    /** 撤回 / 清空 / 提交：空表达式时前两个没得可动，答对后三个都锁上。 */
    private UIElement createActionRow() {
        var undo = Button.translatable(ACTION_WIDTH, BUTTON_UNDO)
                .clientDisabled(() -> view().tokens().length == 0 || view().solved(), REASON_EMPTY);
        undo.setOnServerClick(() -> undo(playerOf(undo)));
        var clear = Button.translatable(ACTION_WIDTH, BUTTON_CLEAR)
                .clientDisabled(() -> view().tokens().length == 0 || view().solved(), REASON_EMPTY);
        clear.setOnServerClick(() -> clearTokens(playerOf(clear)));
        var submit = Button.translatable(ACTION_WIDTH, BUTTON_SUBMIT)
                .clientDisabled(() -> view().solved(), REASON_SOLVED);
        submit.setOnServerClick(() -> submit(playerOf(submit)));
        return centeredRow().addChildren(undo, clear, submit);
    }

    /** 表达式：数字牌显示牌面（青色），其余显示符号（白色）；空表达式用一条破折号占位。 */
    private Component expressionText() {
        if (tokens.length == 0) return Component.literal("—").withStyle(ChatFormatting.DARK_GRAY);
        MutableComponent text = Component.empty();
        for (int token : tokens) {
            if (Game24Rules.isNumberToken(token)) {
                text.append(Component.literal(Integer.toString(numbers[token])).withStyle(ChatFormatting.AQUA));
            } else {
                text.append(Component.literal(" " + Game24Rules.tokenText(token) + " ").withStyle(ChatFormatting.WHITE));
            }
        }
        return text;
    }

    /** 反馈行：这一轮结算 / 答对 / 提示 / 上一次不是 24 / 待提交，全部由服务端状态推出来。 */
    private Component feedbackText() {
        if (result == RESULT_REWARDED) {
            return Component.translatable(TEXT_REWARD, RUN_PUZZLES, number(currentRange().runReward()))
                    .withStyle(ChatFormatting.GREEN);
        }
        if (result == RESULT_SOLVED) return Component.translatable(TEXT_SOLVED).withStyle(ChatFormatting.GREEN);
        if (hinted) return Component.translatable(TEXT_HINT, rules.hint(numbers)).withStyle(ChatFormatting.YELLOW);
        if (result == RESULT_WRONG) return Component.translatable(TEXT_WRONG).withStyle(ChatFormatting.RED);
        return Component.literal("—").withStyle(ChatFormatting.GRAY);
    }

    /** 难度页：游玩权限开关 + 抽牌范围；排行榜单独一页。 */
    private final class DifficultyPage extends GamePage {

        private DifficultyPage() {
            super(WidgetIcons.SETTINGS, PAGE_DIFFICULTY, PAGE_DIFFICULTY_TOOLTIP);
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();
            page.addChild(allowOthersRow());

            var section = Form.section(SECTION_DRAW_RANGE);
            for (int ordinal = 0; ordinal < RANGE_NAMES.length; ordinal++) {
                // 循环变量不是有效 final，先拷一份给 lambda 用
                int index = ordinal;
                var button = choiceButton(LayoutStyle.AUTO, Component.translatable(RANGE_NAMES[index]),
                        () -> drawRange == index);
                button.setHoverTooltips(Component.translatable(TEXT_RANGE_REWARD,
                        number(Game24Rules.DrawRange.ALL[index].runReward()), RUN_PUZZLES));
                button.setOnServerClick(() -> setDrawRange(index));
                section.addChild(button);
            }
            page.addChild(section);
            page.addChild(balancePanel());
            return page;
        }
    }

    /** 排行榜页：顶上一排抽牌档选项，下面只显示选中的那张表。 */
    private final class LeaderboardPage extends LeaderboardTabPage {

        private LeaderboardPage() {
            super(PAGE_LEADERBOARD, PAGE_LEADERBOARD_TOOLTIP);
        }

        @Override
        protected int boardCount() {
            return RANGE_NAMES.length;
        }

        @Override
        protected Component boardName(int index) {
            return Component.translatable(RANGE_NAMES[index]);
        }

        @Override
        protected Widget boardContent(int index) {
            return index == 0 ? milestoneSection(SECTION_TEN_BOARD, "game_milestones_game_24_ten", 0) :
                    milestoneSection(SECTION_THIRTEEN_BOARD, "game_milestones_game_24_thirteen", 1);
        }

        private UIElement milestoneSection(String titleKey, String paneId, int rangeOrdinal) {
            var section = Form.section(titleKey);
            section.addChild(milestoneTable(paneId, MILESTONE_COLUMNS,
                    (store, server) -> buildMilestoneRows(store, server, rangeOrdinal)));
            return section;
        }
    }

    /** 里程碑表格的行（服务端构建）：某个抽牌档的用时榜，用时短的在前，名次由表格补，达成时间进悬停提示。 */
    private List<MilestoneCell[]> buildMilestoneRows(GameMilestones store, MinecraftServer server, int rangeOrdinal) {
        var board = store.puzzleBoard(rangeOrdinal);
        var rows = new ObjectArrayList<MilestoneCell[]>(board.size());
        for (var stat : board) {
            long ticks = rangeOrdinal == 0 ? stat.tenTicks() : stat.thirteenTicks();
            long time = rangeOrdinal == 0 ? stat.tenTime() : stat.thirteenTime();
            rows.add(new MilestoneCell[] { MilestoneCell.of(Component.literal(GameMilestones.nameOf(server, stat.player()))),
                    MilestoneCell.achieved(duration(ticks), time) });
        }
        return rows;
    }
}

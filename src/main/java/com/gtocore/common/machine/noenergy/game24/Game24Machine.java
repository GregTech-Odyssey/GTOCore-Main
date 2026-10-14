package com.gtocore.common.machine.noenergy.game24;

import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.CoinSeries;
import com.gtocore.data.transaction.data.TradeLang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 「24点」游戏机：{@link Game24Rules#NUMBER_COUNT} 张 {@link Game24Rules#VALUE_MIN}~{@link Game24Rules#VALUE_MAX} 的数字牌，
 * 用 + − × ÷ 与括号把它们各用一次拼成 {@link Game24Rules#TARGET}。
 *
 * <p>
 * 界面只用按钮与文本：一张牌、一个运算符、一个括号都是一个按钮，点一下往表达式尾巴上追加一个 token；表达式、反馈与
 * 「哪张牌已经用掉」都由服务端算好下发（快照 + 文字各走自己的同步项）。答对一题按是否用过提示发游戏币进余额，
 * 换一题随时可点（放弃不扣钱），余额照旧可以按面额取出。
 */
@DataGeneratorScanned
public class Game24Machine extends MetaMachine implements IFancyUIMachine, IMachineLife {

    private static final String CURRENCY_NAME = "gtocore.currency." + TradeLang.GAME_COINS;

    @RegisterLanguage(cn = "余额", en = "Credits")
    private static final String LINE_BALANCE = "gtocore.machine.game_24.line.balance";
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
    @RegisterLanguage(cn = "取出全部", en = "Withdraw all")
    private static final String BUTTON_WITHDRAW = "gtocore.machine.game_24.button.withdraw";
    @RegisterLanguage(cn = "没有余额可取出", en = "Nothing to withdraw")
    private static final String REASON_NOTHING_TO_WITHDRAW = "gtocore.machine.game_24.reason.nothing_to_withdraw";
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
    @RegisterLanguage(cn = "答对了！+%s 游戏币", en = "Correct! +%s Game Coins")
    private static final String TEXT_SOLVED = "gtocore.machine.game_24.text.solved";
    @RegisterLanguage(cn = "%s = 24", en = "%s = 24")
    private static final String TEXT_HINT = "gtocore.machine.game_24.text.hint";

    /** 一题答对的奖励；用过提示只给零头，提示才有代价。公开给工具提示复用，避免两处各写一个数。 */
    public static final long SOLVE_REWARD = 20;
    public static final long HINTED_SOLVE_REWARD = 0;
    private static final int CARD_WIDTH = 36;
    private static final int TOKEN_WIDTH = 23;
    private static final int ACTION_WIDTH = 48;

    private static final int RESULT_NONE = 0;
    private static final int RESULT_SOLVED = 1;
    private static final int RESULT_WRONG = 2;

    /** 「还没发牌」用空数组而不是 null；首次打开界面时会立刻发一局。 */
    private static final int[] NO_NUMBERS = new int[0];
    private static final int[] NO_TOKENS = new int[0];
    /** 放下机器时可能还没有世界可借随机源，退回这个静态的。 */
    private static final RandomSource PUZZLE_RANDOM = RandomSource.create();

    private final Game24Rules rules = Game24Rules.create();

    /** 服务端权威牌面，{@link Game24Rules#NUMBER_COUNT} 张。 */
    @SaveToDisk
    private int[] numbers = NO_NUMBERS;
    /** 玩家拼到一半的表达式，按 token 顺序存。 */
    @SaveToDisk
    private int[] tokens = NO_TOKENS;
    /** 上一次提交的结果，只影响反馈文案与按钮禁用。 */
    @SaveToDisk
    private int result = RESULT_NONE;
    /** 本题是否用过提示（用过只给 {@link #HINTED_SOLVE_REWARD}）。 */
    @SaveToDisk
    private boolean hinted;
    /** 未取出的游戏币余额。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private long balance;
    /** 界面快照版本号：只在服务端递增，靠它决定要不要重建 {@link #cachedView}。 */
    private int revision;

    /** 服务端当前快照；版本号没变就复用同一实例，数组只读，直接引用即可，不白复制。 */
    @Nullable
    private SyncValue<View> viewSync;
    @Nullable
    private View cachedView;
    private int cachedRevision = Integer.MIN_VALUE;

    /**
     * 通道里下发的界面快照：牌面、表达式、以及答对/答错/用过提示三个标记。
     * 相等性只看版本号，省掉逐元素比数组。
     */
    private record View(int revision, int[] numbers, int[] tokens, int flags) {

        private static final int FLAG_SOLVED = 1;
        private static final int FLAG_HINTED = 1 << 1;
        private static final View EMPTY = new View(-1, NO_NUMBERS, NO_TOKENS, 0);
        private static final StreamCodec<FriendlyByteBuf, View> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, View::revision,
                ByteBufCodecs.VAR_INT_ARRAY, View::numbers,
                ByteBufCodecs.VAR_INT_ARRAY, View::tokens,
                ByteBufCodecs.VAR_INT, View::flags,
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

    public Game24Machine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    /** 拆除时机器不带走余额：按面额换成实体游戏币掉回世界。 */
    @Override
    public void onMachineRemoved() {
        CoinExchange.dropToWorld(getLevel(), getPos(), TradeLang.GAME_COINS, balance);
    }

    /** 读档后的收尾：牌不对就重发一局，token 里有非法项就把表达式丢掉，结果与计数夹到合法范围。 */
    private void sanitizeState() {
        if (numbers.length != Game24Rules.NUMBER_COUNT) {
            startPuzzle();
            return;
        }
        for (int index = 0; index < numbers.length; index++) {
            numbers[index] = Math.clamp(numbers[index], Game24Rules.VALUE_MIN, Game24Rules.VALUE_MAX);
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
        if (result != RESULT_SOLVED && result != RESULT_WRONG) result = RESULT_NONE;
    }

    // ==================== 局面状态 ====================

    /** 发一局新牌：表达式、结果与提示标记全部归零。 */
    private void startPuzzle() {
        var level = getLevel();
        numbers = rules.deal(level == null ? PUZZLE_RANDOM : level.getRandom());
        tokens = NO_TOKENS;
        result = RESULT_NONE;
        hinted = false;
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
    private void appendCard(int index) {
        if (isRemote() || result == RESULT_SOLVED) return;
        if (index < 0 || index >= numbers.length || isCardUsed(index)) return;
        if (tokens.length >= Game24Rules.MAX_TOKENS) return;
        tokens = append(tokens, index);
        result = RESULT_NONE;
        syncAndMarkChanged();
    }

    /** 点一个运算符/括号：同样只追加 token，语法留到提交时判。 */
    private void appendToken(int token) {
        if (isRemote() || result == RESULT_SOLVED) return;
        if (!Game24Rules.isToken(token) || Game24Rules.isNumberToken(token)) return;
        if (tokens.length >= Game24Rules.MAX_TOKENS) return;
        tokens = append(tokens, token);
        result = RESULT_NONE;
        syncAndMarkChanged();
    }

    /** 撤回最后一个 token。 */
    private void undo() {
        if (isRemote() || tokens.length == 0 || result == RESULT_SOLVED) return;
        tokens = Arrays.copyOf(tokens, tokens.length - 1);
        result = RESULT_NONE;
        syncAndMarkChanged();
    }

    /** 清空整个表达式。 */
    private void clearTokens() {
        if (isRemote() || tokens.length == 0 || result == RESULT_SOLVED) return;
        tokens = NO_TOKENS;
        result = RESULT_NONE;
        syncAndMarkChanged();
    }

    /** 提交：语法、用牌与数值全对才算解出，按是否用过提示发币；只是不对的话不扣钱，给一句反馈继续改。 */
    private void submit() {
        if (isRemote() || result == RESULT_SOLVED || tokens.length == 0) return;
        if (!rules.isSolution(tokens, numbers)) {
            result = RESULT_WRONG;
            syncAndMarkChanged();
            return;
        }
        result = RESULT_SOLVED;
        long reward = hinted ? HINTED_SOLVE_REWARD : SOLVE_REWARD;
        balance += Math.min(Long.MAX_VALUE - balance, reward);
        syncAndMarkChanged();
    }

    /** 提示：显示一个解法，本题奖励降到 {@link #HINTED_SOLVE_REWARD}；同一题只给一次。 */
    private void useHint() {
        if (isRemote() || hinted || result == RESULT_SOLVED) return;
        hinted = true;
        syncAndMarkChanged();
    }

    /** 换一题：放弃当前表达式重新发牌，答对后再点也是走这里。 */
    private void nextPuzzle() {
        if (isRemote()) return;
        startPuzzle();
        syncAndMarkChanged();
    }

    /** 服务端：把余额按面额从大到小换成硬币塞进玩家背包，塞不下的留在余额里。 */
    private void withdrawAll(Widget source) {
        var gui = source.getGui();
        if (isRemote() || gui == null || !(gui.entityPlayer instanceof ServerPlayer player)) return;
        var inventory = player.getInventory();
        long remaining = balance;
        if (remaining == 0) return;
        var destination = new PlayerMainInvWrapper(inventory);
        CoinSeries coins = CoinExchange.gameCoins();
        for (int tier = coins.typeCount() - 1; tier >= 0 && remaining > 0; tier--) {
            long value = coins.value(tier);
            if (remaining < value) continue;
            ItemStack stack = new ItemStack(coins.item(tier));
            if (stack.isEmpty()) continue;
            int amount = (int) Math.min(remaining / value, (long) inventory.items.size() * stack.getMaxStackSize());
            if (amount == 0) continue;
            stack.setCount(amount);
            remaining -= (amount - ItemHandlerHelper.insertItemStacked(destination, stack, false).getCount()) * value;
        }
        long withdrawn = balance - remaining;
        if (withdrawn == 0) return;
        balance -= withdrawn;
        syncAndMarkChanged();
    }

    /** 追加一个 token；数组很短，直接重建一份（机器靠字段换引用确认状态变了）。 */
    private static int[] append(int[] tokens, int token) {
        int[] appended = Arrays.copyOf(tokens, tokens.length + 1);
        appended[tokens.length] = token;
        return appended;
    }

    // ==================== 界面同步 ====================

    /** 状态变了之后统一走这里：顶版本号让界面通道下一 tick 带上新快照，并标记机器已变。 */
    private void syncAndMarkChanged() {
        if (!isRemote()) revision++;
        onChanged();
    }

    // ==================== 界面 ====================

    @Override
    public Widget createUIWidget() {
        if (!isRemote() && numbers.length != Game24Rules.NUMBER_COUNT) startPuzzle();

        var board = new StatusPanel();
        viewSync = board.addSyncValue(SyncValue.of(this::viewSnapshot, View.CODEC, View.EMPTY));
        board.getLayoutStyle().gapAll(2);
        board.addChildren(
                TextLine.of(LayoutStyle.AUTO, this::expressionText).styled().setTextAlign(Horizontal.CENTER),
                createCardRow(),
                createTokenRow(),
                createActionRow());

        var status = new StatusPanel();
        status.getLayoutStyle().gapAll(2);
        status.addLine(LINE_BALANCE, () -> number(balance));
        status.addChild(TextLine.of(LayoutStyle.AUTO, this::feedbackText).setTextAlign(Horizontal.CENTER));

        var hint = Button.translatable(77, BUTTON_HINT)
                .setOnServerClick(this::useHint)
                .clientDisabled(() -> view().hinted() || view().solved(), REASON_HINTED);
        var next = Button.translatable(77, BUTTON_NEXT).setOnServerClick(this::nextPuzzle);
        status.addChild(centeredRow().addChildren(hint, next));

        var withdraw = Button.translatable(136, BUTTON_WITHDRAW);
        withdraw.setHoverTooltips(CURRENCY_NAME);
        withdraw.disabled(() -> balance == 0, REASON_NOTHING_TO_WITHDRAW);
        withdraw.setOnServerClick(() -> withdrawAll(withdraw));
        status.addChild(centeredRow().addChildren(withdraw));

        return Form.page().addChildren(board, status);
    }

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
        return Button.of(CARD_WIDTH)
                .bindClientText(() -> {
                    int[] cards = view().numbers();
                    return index < cards.length ? Integer.toString(cards[index]) : "";
                })
                .clientDisabled(() -> view().isUsed(index) || view().solved(), REASON_CARD_USED)
                .setOnServerClick(() -> appendCard(index));
    }

    /** 运算符与括号按钮：字形就是 {@link Game24Rules#tokenText}，按钮上的字与表达式里显示的一致。 */
    private UIElement createTokenRow() {
        var row = centeredRow();
        for (int token = Game24Rules.ADD; token < Game24Rules.TOKEN_COUNT; token++) {
            // 循环变量不是有效 final，先拷一份给 lambda 用
            int code = token;
            row.addChild(Button.translatable(TOKEN_WIDTH, Game24Rules.tokenText(code))
                    .clientDisabled(() -> view().solved(), REASON_SOLVED)
                    .setOnServerClick(() -> appendToken(code)));
        }
        return row;
    }

    /** 撤回 / 清空 / 提交：空表达式时前两个没得可动，答对后三个都锁上。 */
    private UIElement createActionRow() {
        var undo = Button.translatable(ACTION_WIDTH, BUTTON_UNDO)
                .setOnServerClick(this::undo)
                .clientDisabled(() -> view().tokens().length == 0 || view().solved(), REASON_EMPTY);
        var clear = Button.translatable(ACTION_WIDTH, BUTTON_CLEAR)
                .setOnServerClick(this::clearTokens)
                .clientDisabled(() -> view().tokens().length == 0 || view().solved(), REASON_EMPTY);
        var submit = Button.translatable(ACTION_WIDTH, BUTTON_SUBMIT)
                .setOnServerClick(this::submit)
                .clientDisabled(() -> view().solved(), REASON_SOLVED);
        return centeredRow().addChildren(undo, clear, submit);
    }

    /** 服务端当前快照；版本号没变就复用同一实例。 */
    private View viewSnapshot() {
        if (cachedView == null || cachedRevision != revision) {
            int flags = (result == RESULT_SOLVED ? View.FLAG_SOLVED : 0) | (hinted ? View.FLAG_HINTED : 0);
            cachedView = new View(revision, numbers, tokens, flags);
            cachedRevision = revision;
        }
        return cachedView;
    }

    /** 界面用的快照：客户端读通道下发的那份，服务端直接现造一份。 */
    private View view() {
        if (!isRemote()) return viewSnapshot();
        return viewSync == null ? View.EMPTY : viewSync.getValue();
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

    /** 反馈行：答对 / 提示 / 上一次不是 24 / 待提交，四种文案全部由服务端状态推出来。 */
    private Component feedbackText() {
        if (result == RESULT_SOLVED) {
            return Component.translatable(TEXT_SOLVED, number(hinted ? HINTED_SOLVE_REWARD : SOLVE_REWARD))
                    .withStyle(ChatFormatting.GREEN);
        }
        if (hinted) {
            return Component.translatable(TEXT_HINT, rules.hint(numbers), number(HINTED_SOLVE_REWARD))
                    .withStyle(ChatFormatting.YELLOW);
        }
        if (result == RESULT_WRONG) return Component.translatable(TEXT_WRONG).withStyle(ChatFormatting.RED);
        return Component.literal("—").withStyle(ChatFormatting.GRAY);
    }

    private static MutableComponent number(long value) {
        return Component.literal(FormattingUtil.formatNumbers(value)).withStyle(ChatFormatting.AQUA);
    }
}

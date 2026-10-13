package com.gtocore.common.machine.noenergy.justGet10;

import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.CoinSeries;
import com.gtocore.data.transaction.data.TradeLang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

/**
 * 「合成十」游戏机：5×5 合并玩法，按积分折算游戏币进余额（见 {@link JustGet10Rules} 与 {@link #POINTS_PER_COIN}）。
 *
 * <p>
 * 首次打开界面时立刻随机发牌，盘面无相邻同数字时这一局结束；按「重新开始」才兑现并随机开下一局，也可以随时提前兑现重开。
 * 25 个数字格各自是一个 {@link Cell}（外裹一层转发坐标，里面是 {@link CellButton}）：盘面走界面通道，只在盘面或提起
 * 位变了的那一 tick 下发；状态面板的数值由 {@code StatusPanel} 各自的 {@code SyncValue} 下发服务端算好的文字。
 */
@DataGeneratorScanned
public class JustGet10Machine extends MetaMachine implements IFancyUIMachine, IMachineLife {

    private static final String CURRENCY_NAME = "gtocore.currency." + TradeLang.GAME_COINS;

    @RegisterLanguage(cn = "余额", en = "Credits")
    private static final String LINE_BALANCE = "gtocore.machine.just_get_10.line.balance";
    @RegisterLanguage(cn = "本局积分", en = "Current score")
    private static final String LINE_SCORE = "gtocore.machine.just_get_10.line.score";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.just_get_10.button.restart";
    @RegisterLanguage(cn = "取出全部", en = "Withdraw all")
    private static final String BUTTON_WITHDRAW = "gtocore.machine.just_get_10.button.withdraw";
    @RegisterLanguage(cn = "没有余额可取出", en = "Nothing to withdraw")
    private static final String REASON_NOTHING_TO_WITHDRAW = "gtocore.machine.just_get_10.reason.nothing_to_withdraw";

    /** 格子边长与数字材质一致，紧排成一片盘面。 */
    private static final int CELL_SIZE = 24;
    private static final int BOARD_WIDTH = JustGet10Rules.SIZE * CELL_SIZE;

    /**
     * 额外分：一局里 {@link #SCORE_BONUS_MIN_VALUE}~{@link JustGet10Rules#VALUE_MAX} 的每个数字<b>首次</b>被合出时各给
     * 一次，金额随数字递增；水位记在 {@link #maxValue} 上。
     */
    private static final int SCORE_BONUS_MIN_VALUE = 10;
    private static final int SCORE_BONUS_BASE = 1000;
    private static final int SCORE_BONUS_PER_VALUE = 500;
    /** 兑现比例：超过 {@link #REDEMPTION_BASELINE} 的部分每 {@link #POINTS_PER_COIN} 分折 1 枚游戏币，向下取整。 */
    private static final int REDEMPTION_BASELINE = 1000;
    private static final int POINTS_PER_COIN = 20;

    private static final int NO_SELECTION = 0;
    /** 「没有盘面」用空数组而不是 null；首次打开界面时会立刻换成随机盘面。 */
    private static final int[] NO_BOARD = new int[0];
    /** 放下机器时可能还没有世界可借随机源，退回这个静态的。 */
    private static final RandomSource BOARD_RANDOM = RandomSource.create();

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

    private final JustGet10Rules rules = JustGet10Rules.create();

    @SaveToDisk(defaultValue = "true")
    @SyncToClient(scheduleUpdate = true)
    private boolean enabled = true;
    /** 服务端权威盘面，按行优先存 {@link JustGet10Rules#CELL_COUNT} 格，空格为 -1。 */
    @SaveToDisk
    private int[] board = NO_BOARD;
    /** 未取出的游戏币余额。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private long balance;
    /** 本局累计积分。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int score;
    /** 本局出现过的最大数字 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int maxValue = JustGet10Rules.VALUE_MIN;
    /** 已提起的格子，按盘面下标取位；提到的是「点中那格所在的整个连通组」，所以是位图。 */
    @SaveToDisk
    private int selectedMask = NO_SELECTION;
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private int gameStatus = Status.IDLE.ordinal();
    /** 盘面/状态版本号：只在服务端递增，靠它决定要不要重建 {@link #cachedSnapshot}。 */
    private int boardRevision;

    /** 服务端当前快照；版本号没变就复用同一实例，不白复制盘面。 */
    @Nullable
    private SyncValue<BoardSnapshot> boardSync;
    @Nullable
    private BoardSnapshot cachedSnapshot;
    private int cachedRevision = Integer.MIN_VALUE;

    /** 通道里下发的盘面；相等性只看版本号，省掉逐元素比 25 个数字。 */
    private record BoardSnapshot(int revision, int[] board, int selectedMask) {

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, NO_BOARD, NO_SELECTION);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::board,
                ByteBufCodecs.VAR_INT, BoardSnapshot::selectedMask,
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

    /** 局面阶段 */
    private enum Status {
        IDLE,
        PLAYING,
        GAME_OVER
    }

    public JustGet10Machine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    /** 拆除时先兑现本局积分，再把余额按面额换成游戏币掉回世界。机器本身不带走任何余额，也不再往掉落物里塞存档数据。 */
    @Override
    public void onMachineRemoved() {
        if (gameStatus != Status.GAME_OVER.ordinal()) convertReward();
        CoinExchange.dropToWorld(getLevel(), getPos(), TradeLang.GAME_COINS, balance);
    }

    /** 读档后的收尾：长度与取值范围都夹紧，阶段按盘面是否存在修正，提起位图裁到盘面范围内。 */
    private void sanitizeState() {
        board = JustGet10Rules.normalizeBoard(board);
        score = Math.clamp(score, 0, JustGet10Rules.SCORE_MAX);
        maxValue = Math.clamp(maxValue, JustGet10Rules.VALUE_MIN, JustGet10Rules.VALUE_MAX);
        if (board.length == 0 || gameStatus < 0 || gameStatus >= Status.values().length) {
            gameStatus = Status.IDLE.ordinal();
        }
        selectedMask &= board.length == 0 ? 0 : (1 << board.length) - 1;
    }

    // ==================== 局面状态 ====================

    /** 开新一局：整盘随机重发，积分、里程碑水位（{@link #maxValue}）与提起状态全部归零。 */
    private void startRound() {
        var level = getLevel();
        board = JustGet10Rules.createBoard(level == null ? BOARD_RANDOM : level.getRandom());
        score = 0;
        maxValue = JustGet10Rules.maxValue(board);
        selectedMask = NO_SELECTION;
        gameStatus = Status.PLAYING.ordinal();
    }

    /** 把本局积分折成游戏币入余额；同一局只兑现一次（分出胜负时或重新开始时）。 */
    private void convertReward() {
        long reward = (score - REDEMPTION_BASELINE) / POINTS_PER_COIN;
        if (reward > 0) balance += Math.min(Long.MAX_VALUE - balance, reward);
    }

    // ==================== 服务端动作 ====================

    /**
     * 落子（下标就是按钮编号）：
     *
     * <p>
     * 未提起的格子先提起整组——把 {@code index} 所在、数字相同且四向连通的一整组标进 {@link #selectedMask}；不成组
     * （单格、空格、越界）就清空提起。已提起的格子则合并：整组清空、被点中的那格写入「原数字 + 1」、逐列下坠补格，
     * 然后按新盘面结算分数与「首次合出」里程碑。合并会清掉<b>所有</b>提起（盘面换了位置，留着旧高亮会指错格子）；
     * 不可合并时直接返回、状态不变。
     */
    private void clickCell(int index) {
        if (isRemote() || !enabled || !(gameStatus == Status.PLAYING.ordinal())) return;
        if ((selectedMask & (1 << index)) == 0) {
            if (index >= 0 && index < board.length) {
                var group = rules.groupAt(board, index / JustGet10Rules.SIZE, index % JustGet10Rules.SIZE);
                if (group != null) {
                    int mask = NO_SELECTION;
                    for (int cell = 0; cell < group.size(); cell++) mask |= 1 << group.getInt(cell);
                    selectedMask = mask;
                } else {
                    selectedMask = NO_SELECTION;
                }
            } else {
                selectedMask = NO_SELECTION;
            }
        } else {
            int row = index / JustGet10Rules.SIZE;
            int col = index % JustGet10Rules.SIZE;
            var group = rules.groupAt(board, row, col);
            if (group == null) return;
            var level = getLevel();
            if (level == null) return;
            var merge = rules.merge(board, group, row, col, level.getRandom(), score);
            if (merge == null) return;
            int mergedValue = merge.mergedValue();
            int gained = mergedValue * group.size();
            board = merge.board();
            int reachedValue = Math.min(JustGet10Rules.VALUE_MAX, JustGet10Rules.maxValue(board));
            if (reachedValue > maxValue) {
                for (int value = Math.max(maxValue + 1, SCORE_BONUS_MIN_VALUE); value <= reachedValue; value++) {
                    gained += SCORE_BONUS_BASE + (value - SCORE_BONUS_MIN_VALUE) * SCORE_BONUS_PER_VALUE;
                }
                maxValue = reachedValue;
            }
            score = (int) Math.min(JustGet10Rules.SCORE_MAX, (long) score + gained);
            selectedMask = NO_SELECTION;
            if (JustGet10Rules.isGameOver(board)) {
                gameStatus = Status.GAME_OVER.ordinal();
                convertReward();
            }
        }
        syncAndMarkChanged();
    }

    /**
     * 服务端：重新开始一局（有进行中的一局就先兑现）。
     * 兑现并立刻开新一局：折币入余额、清零本局成绩后随机发新盘。没有进行中的一局时按下也无副作用，兑现 0 币照样重发
     */
    private void restartRoundOnServer() {
        if (isRemote() || !enabled) return;
        if (gameStatus != Status.GAME_OVER.ordinal()) convertReward();
        score = 0;
        maxValue = JustGet10Rules.VALUE_MIN;
        board = NO_BOARD;
        selectedMask = NO_SELECTION;
        gameStatus = Status.IDLE.ordinal();
        startRound();
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

    // ==================== 界面同步 ====================

    /** 状态变了之后统一走这里：顶版本号让界面通道下一 tick 带上新状态，并标记机器已变。 */
    private void syncAndMarkChanged() {
        if (!isRemote()) boardRevision++;
        onChanged();
    }

    // ==================== 界面 ====================

    @Override
    public Widget createUIWidget() {
        if (!isRemote() && gameStatus == Status.IDLE.ordinal()) startRound();

        var status = new StatusPanel();
        status.addLine(LINE_BALANCE, () -> number(balance));
        status.addLine(LINE_SCORE, () -> number(score));

        var restart = Button.translatable(77, BUTTON_RESTART).setOnServerClick(this::restartRoundOnServer);
        var withdraw = Button.translatable(77, BUTTON_WITHDRAW);
        withdraw.setHoverTooltips(CURRENCY_NAME);
        withdraw.disabled(() -> balance == 0, REASON_NOTHING_TO_WITHDRAW);
        withdraw.setOnServerClick(() -> withdrawAll(withdraw));
        var actions = UIElement.centeredRow(Button.HEIGHT).addChildren(restart, withdraw);
        status.addChild(actions);

        var page = Form.page();
        page.addChildren(createBoard(), status);
        return page;
    }

    /** 5×5 盘面：25 个数字格按钮，5 个一行紧排成一片；{@link #boardSync} 挂在这一层上。 */
    private UIElement createBoard() {
        var boardRows = new UIElement().layout(style -> style.alignCenter().width(BOARD_WIDTH));
        boardSync = boardRows.addSyncValue(SyncValue.of(this::boardSnapshot, BoardSnapshot.CODEC, BoardSnapshot.EMPTY));
        for (int row = 0; row < JustGet10Rules.SIZE; row++) {
            var cells = UIElement.row(CELL_SIZE).layout(style -> style.width(BOARD_WIDTH));
            for (int col = 0; col < JustGet10Rules.SIZE; col++) {
                cells.addChild(new Cell(row * JustGet10Rules.SIZE + col));
            }
            boardRows.addChild(cells);
        }
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChildren(boardRows);
    }

    /** 服务端当前快照；版本号没变就复用同一实例，盘面数组只读，直接引用即可，不白复制。 */
    private BoardSnapshot boardSnapshot() {
        if (cachedSnapshot == null || cachedRevision != boardRevision) {
            cachedSnapshot = new BoardSnapshot(boardRevision, board, selectedMask);
            cachedRevision = boardRevision;
        }
        return cachedSnapshot;
    }

    /** 界面显示用的快照：客户端读通道下发的那份，服务端直接现造一份。 */
    private BoardSnapshot display() {
        if (!isRemote()) return boardSnapshot();
        return boardSync == null ? BoardSnapshot.EMPTY : boardSync.getValue();
    }

    private static MutableComponent number(long value) {
        return Component.literal(FormattingUtil.formatNumbers(value)).withStyle(ChatFormatting.AQUA);
    }

    /**
     * 数字格：外裹定尺寸的 {@link UIElement}，里面才是按钮。这层不能省——LDLib 的
     * {@code WidgetGroup.mouseClicked} 会把父控件坐标原样转发给每个子控件，只有 {@link UIElement} 会在自己的坐标
     * 空间里再转发一次，少了它 25 个按钮会同时收到同一次点击。
     */
    private final class Cell extends UIElement {

        private Cell(int index) {
            layout(style -> style.size(CELL_SIZE, CELL_SIZE));
            addChild(new CellButton(index));
        }
    }

    /**
     * 按钮本体：整格就是一张数字材质，提起时换成高亮的那套；不画底图、文字或禁用态。
     *
     * <p>
     * 一律不禁用：格值每帧现读同步快照，有数字就画；能不能动由服务端在处理点击时判定，界面不会出现斜纹或
     * 「禁止操作」。
     */
    private final class CellButton extends Button {

        private final int index;

        private CellButton(int index) {
            super(CELL_SIZE, CELL_SIZE, null, null);
            this.index = index;
            setOnServerClick(() -> clickCell(index));
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            var snapshot = display();
            int[] current = snapshot.board();
            int value = index < current.length ? current[index] : -1;
            if (value < JustGet10Rules.VALUE_MIN) return;
            int x = getPositionX();
            int y = getPositionY();
            var textures = (snapshot.selectedMask() & (1 << index)) != 0 ? NUMBER_TEXTURES_PRESSED : NUMBER_TEXTURES;
            textures[Math.min(value, JustGet10Rules.VALUE_MAX)].draw(graphics, mouseX, mouseY, x, y, CELL_SIZE, CELL_SIZE);
            if (isMouseOverElement(mouseX, mouseY)) UIDraw.hoverOverlay(graphics, x, y, CELL_SIZE, CELL_SIZE);
        }
    }
}

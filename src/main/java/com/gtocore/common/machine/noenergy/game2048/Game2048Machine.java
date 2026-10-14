package com.gtocore.common.machine.noenergy.game2048;

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
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
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
import org.lwjgl.glfw.GLFW;

/**
 * 「2048」游戏机：4×4 推格合并玩法，按积分折算游戏币进余额（见 {@link Game2048Rules} 与 {@link #POINTS_PER_COIN}）。
 *
 * <p>
 * 首次打开界面时立刻发牌，盘面再也推不动时这一局结束；按「重新开始」才兑现并重新发牌，也可以随时提前兑现重开。
 * 盘面用键盘操作：W A S D 与四个方向键各对应一次移动（见 {@link BoardInput#keyPressed}），请求发到服务端由服务端改盘面，
 * 客户端只负责画下发来的快照。
 *
 * <p>
 * 状态面板的数值走 {@code StatusPanel} 各自的 {@code SyncValue}；盘面走 {@link BoardSnapshot}，只在盘面变了的那一 tick 下发。
 */
@DataGeneratorScanned
public class Game2048Machine extends MetaMachine implements IFancyUIMachine, IMachineLife {

    private static final String CURRENCY_NAME = "gtocore.currency." + TradeLang.GAME_COINS;

    @RegisterLanguage(cn = "余额", en = "Credits")
    private static final String LINE_BALANCE = "gtocore.machine.game_2048.line.balance";
    @RegisterLanguage(cn = "本局积分", en = "Current score")
    private static final String LINE_SCORE = "gtocore.machine.game_2048.line.score";
    @RegisterLanguage(cn = "重新开始", en = "Restart")
    private static final String BUTTON_RESTART = "gtocore.machine.game_2048.button.restart";
    @RegisterLanguage(cn = "取出全部", en = "Withdraw all")
    private static final String BUTTON_WITHDRAW = "gtocore.machine.game_2048.button.withdraw";
    @RegisterLanguage(cn = "没有余额可取出", en = "Nothing to withdraw")
    private static final String REASON_NOTHING_TO_WITHDRAW = "gtocore.machine.game_2048.reason.nothing_to_withdraw";

    /** 格子边长；4×4 盘面连同状态面板放进 {@code CONTENT_WIDTH} 宽的页面。 */
    private static final int CELL_SIZE = 28;
    private static final int BOARD_WIDTH = Game2048Rules.SIZE * CELL_SIZE;

    /**
     * 额外分：一局里 {@link #SCORE_BONUS_MIN_VALUE} 及以上的每个数字<b>首次</b>被合出时各给一次，金额随数字递增；
     * 水位就是当时盘面上的最大数字，合并只会把它顶得更高，所以直接和上一步的盘面比。
     */
    private static final int SCORE_BONUS_MIN_VALUE = 1024;
    private static final int SCORE_BONUS_BASE = 1000;
    /** 兑现比例：超过 {@link #REDEMPTION_BASELINE} 的部分每 {@link #POINTS_PER_COIN} 分折 1 枚游戏币，向下取整。 */
    private static final int REDEMPTION_BASELINE = 2000;
    private static final int POINTS_PER_COIN = 20;

    private static final int NO_DIRECTION = -1;
    /** 「没有盘面」用空数组而不是 null；首次打开界面时会立刻换成新发的一局。 */
    private static final int[] NO_BOARD = new int[0];
    /** 放下机器时可能还没有世界可借随机源，退回这个静态的。 */
    private static final RandomSource BOARD_RANDOM = RandomSource.create();

    /** 数字材质：下标是 2 的指数，{@code TILE_TEXTURES[exponent]} 画的是 {@code 1 << exponent}。 */
    private static final IGuiTexture[] TILE_TEXTURES = new IGuiTexture[17];

    static {
        String path = "gtocore:textures/gui/game/game_2048/game_2048_display_";
        for (int exponent = 1; exponent < TILE_TEXTURES.length; exponent++) {
            TILE_TEXTURES[exponent] = new ResourceTexture(path + (1 << exponent) + ".png");
        }
    }

    private final Game2048Rules rules = Game2048Rules.create();

    /** 服务端权威盘面，按行优先存 {@link Game2048Rules#CELL_COUNT} 格，空格为 0。 */
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
    /** 局面阶段；只有服务端看它，界面按盘面自己判断能不能动。 */
    @SaveToDisk
    private int gameStatus = Status.IDLE.ordinal();
    /** 盘面版本号：只在服务端递增，靠它决定要不要重建 {@link #cachedSnapshot}。 */
    private int boardRevision;

    /** 服务端当前快照；版本号没变就复用同一实例，不白复制盘面。 */
    @Nullable
    private SyncValue<BoardSnapshot> boardSync;
    @Nullable
    private BoardSnapshot cachedSnapshot;
    private int cachedRevision = Integer.MIN_VALUE;

    /** 通道里下发的盘面；相等性只看版本号，省掉逐元素比 16 个数字。 */
    private record BoardSnapshot(int revision, int[] board) {

        private static final BoardSnapshot EMPTY = new BoardSnapshot(-1, NO_BOARD);
        private static final StreamCodec<FriendlyByteBuf, BoardSnapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoardSnapshot::revision,
                ByteBufCodecs.VAR_INT_ARRAY, BoardSnapshot::board,
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

    public Game2048Machine(MetaMachineBlockEntity holder) {
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

    /** 读档后的收尾：盘面按尺寸与合法数字重整，阶段按盘面是否存在修正。 */
    private void sanitizeState() {
        board = Game2048Rules.normalizeBoard(board);
        score = Math.clamp(score, 0, Game2048Rules.SCORE_MAX);
        if (Game2048Rules.isEmpty(board) || gameStatus < 0 || gameStatus >= Status.values().length) {
            gameStatus = Status.IDLE.ordinal();
        }
    }

    // ==================== 局面状态 ====================

    /** 开新一局：重新发牌，本局积分归零。 */
    private void startRound() {
        var level = getLevel();
        board = Game2048Rules.createBoard(level == null ? BOARD_RANDOM : level.getRandom());
        score = 0;
        gameStatus = Status.PLAYING.ordinal();
    }

    /** 把本局积分折成游戏币入余额；同一局只兑现一次（分出胜负时或重新开始时）。 */
    private void convertReward() {
        long reward = (score - REDEMPTION_BASELINE) / POINTS_PER_COIN;
        if (reward > 0) balance += Math.min(Long.MAX_VALUE - balance, reward);
    }

    // ==================== 服务端动作 ====================

    /**
     * 服务端：朝 {@code directionOrdinal} 推一次（下标对应 {@link Game2048Rules.Direction}，网络来的值一律校验）。
     *
     * <p>
     * 推不动时 {@link Game2048Rules#move} 返回 {@code null}，状态一律不变。合并得分之外，本局首次合出的
     * {@link #SCORE_BONUS_MIN_VALUE} 及以上数字各再给一次 {@code (数字 / 1024) × SCORE_BONUS_BASE}。
     */
    private void moveOnServer(int directionOrdinal) {
        if (isRemote() || gameStatus != Status.PLAYING.ordinal()) return;
        var direction = Game2048Rules.Direction.of(directionOrdinal);
        if (direction == null) return;
        var level = getLevel();
        if (level == null) return;
        int previousMax = Game2048Rules.maxValue(board);
        var move = rules.move(board, direction, level.getRandom());
        if (move == null) return;
        board = move.board();
        int gained = move.gained();
        int reachedValue = Math.min(Game2048Rules.VALUE_MAX, Game2048Rules.maxValue(board));
        if (reachedValue > previousMax) {
            for (int value = Math.max(previousMax * 2, SCORE_BONUS_MIN_VALUE); value <= reachedValue; value *= 2) {
                gained += value / SCORE_BONUS_MIN_VALUE * SCORE_BONUS_BASE;
            }
        }
        score = (int) Math.min(Game2048Rules.SCORE_MAX, (long) score + gained);
        if (move.gameOver()) {
            gameStatus = Status.GAME_OVER.ordinal();
            convertReward();
        }
        syncAndMarkChanged();
    }

    /** 服务端：兑现本局积分并重新发一局。没有进行中的一局时按下也无副作用，兑现 0 币照样重发。 */
    private void restartRoundOnServer() {
        if (isRemote()) return;
        if (gameStatus != Status.GAME_OVER.ordinal()) convertReward();
        score = 0;
        board = NO_BOARD;
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

    /** 状态变了之后统一走这里：顶版本号让界面通道下一 tick 带上新盘面，并标记机器已变。 */
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
        status.addChild(UIElement.centeredRow(Button.HEIGHT).addChildren(restart, withdraw));

        var page = Form.page();
        page.addChildren(createBoard(), status);
        return page;
    }

    /** 4×4 盘面：外层面板当底并把盘面居中，里面是收键盘的 {@link BoardInput}。 */
    private UIElement createBoard() {
        return new StatusPanel().layout(LayoutStyle::alignCenter).addChild(new BoardInput());
    }

    /** 服务端当前快照；版本号没变就复用同一实例，盘面数组只读，直接引用即可，不白复制。 */
    private BoardSnapshot boardSnapshot() {
        if (cachedSnapshot == null || cachedRevision != boardRevision) {
            cachedSnapshot = new BoardSnapshot(boardRevision, board);
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

    /** 键盘到方向的映射：W A S D 与四个方向键都收，其余键返回 {@link #NO_DIRECTION}。 */
    private static int directionOf(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT -> Game2048Rules.Direction.LEFT.ordinal();
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP -> Game2048Rules.Direction.UP.ordinal();
            case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT -> Game2048Rules.Direction.RIGHT.ordinal();
            case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN -> Game2048Rules.Direction.DOWN.ordinal();
            default -> NO_DIRECTION;
        };
    }

    /**
     * 盘面本体：一次画完 16 格，同时是界面的键盘入口。
     *
     * <p>
     * 键盘事件由 LDLib 从界面根控件逐层向下派发（{@code WidgetGroup.keyPressed}），本元素在派发链上，所以不需要
     * 焦点；收到的移动请求经 {@link RPC} 上行，服务端照旧做一次校验再改盘面。盘面值只在绘制时读同步快照，
     * 两端的控件树完全一致，通道里的条目顺序（先盘面、后请求）也因此一致。
     */
    private final class BoardInput extends UIElement {

        private final RPC<Integer> move;

        private BoardInput() {
            layout(style -> style.width(BOARD_WIDTH).height(BOARD_WIDTH));
            boardSync = addSyncValue(SyncValue.of(Game2048Machine.this::boardSnapshot, BoardSnapshot.CODEC, BoardSnapshot.EMPTY));
            move = addRPC(ByteBufCodecs.VAR_INT, (player, direction) -> moveOnServer(direction));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int direction = directionOf(keyCode);
            if (direction == NO_DIRECTION) return super.keyPressed(keyCode, scanCode, modifiers);
            move.send(direction);
            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            int[] current = display().board();
            for (int row = 0; row < Game2048Rules.SIZE; row++) {
                for (int col = 0; col < Game2048Rules.SIZE; col++) {
                    int cell = row * Game2048Rules.SIZE + col;
                    int value = cell < current.length ? current[cell] : Game2048Rules.EMPTY;
                    if (value < Game2048Rules.VALUE_MIN || value > Game2048Rules.VALUE_MAX) continue;
                    TILE_TEXTURES[Integer.numberOfTrailingZeros(value)].draw(graphics, mouseX, mouseY,
                            getPositionX() + col * CELL_SIZE, getPositionY() + row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
}

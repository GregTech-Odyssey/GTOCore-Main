package com.gtocore.common.machine.noenergy.game;

import com.gtocore.common.machine.noenergy.game.milestone.GameAccess;
import com.gtocore.common.machine.noenergy.game.milestone.GameMilestones;
import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.CoinSeries;
import com.gtocore.data.transaction.data.TradeLang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.SwitchedContent;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 游戏机的公共底座：钱、下发通道、游玩权限与「这一局是谁在玩」，玩法、局面字段、界面排版与配置页都写在各自的机器里。
 *
 * <p>
 * 本类提供的部分：
 * <ul>
 * <li>余额：{@link #balance()} / {@link #addBalance(long)}，拆除时先 {@link #cashOut()} 兑现再按面额掉回世界；</li>
 * <li>取出：{@link #createWithdrawButton(int)} 那块「取出全部」按钮与它背后的取币逻辑；</li>
 * <li>界面快照：{@link #registerView} 在界面上注册一项同步值，子类给 {@link #createView()}，版本号没变就复用实例；</li>
 * <li>里程碑榜单：{@link #registerBoard} 把服务端构建好的全服榜单挂到界面上；</li>
 * <li>游玩权限：{@link #allowOthers()} 开关与 {@link #canPlay} 判定，锁上后非主人打不开也玩不了；</li>
 * <li>会话：{@link #beginSession} 记发起者、{@link #noteOpener} 记参与者，多人局用 {@link #sessionCounts()} 挡掉记录；</li>
 * <li>共用的文案与小工具：{@link #random()}、{@link #number(long)}、{@link #dayTime(long)}、{@link #duration(long)}。</li>
 * </ul>
 *
 * <p>
 * 余额等字段声明在本类：DataSyncLib 会沿父类链合并注解字段，存盘键就是字段名（{@code field.getName()}），所以字段
 * 位置不影响旧存档。{@link RegisterLanguage} 的扫描只看类自身的字段，所以本类同样要带 {@link DataGeneratorScanned}，
 * 共用的那几条文案才不会漏生成。
 *
 * @param <VIEW> 这类机器下发给客户端的界面快照类型
 */
@DataGeneratorScanned
public abstract class AbstractGameMachine<VIEW> extends MetaMachine implements IFancyUIMachine, IMachineLife {

    @RegisterLanguage(cn = "余额", en = "Credits")
    protected static final String LINE_BALANCE = "gtocore.machine.game.line.balance";
    @RegisterLanguage(cn = "取出全部", en = "Withdraw all")
    protected static final String BUTTON_WITHDRAW = "gtocore.machine.game.button.withdraw";
    @RegisterLanguage(cn = "没有余额可取出", en = "Nothing to withdraw")
    protected static final String REASON_NOTHING_TO_WITHDRAW = "gtocore.machine.game.reason.nothing_to_withdraw";
    /** 「配置」子页的标题与标签提示：三个游戏都挂同一个子页，文案也共用。 */
    @RegisterLanguage(cn = "配置", en = "Config")
    protected static final String PAGE_SETTINGS = "gtocore.machine.game.page.settings";
    @RegisterLanguage(cn = "游戏设置", en = "Game settings")
    protected static final String PAGE_SETTINGS_TOOLTIP = "gtocore.machine.game.page.settings.tooltip";
    @RegisterLanguage(cn = "允许其他玩家游玩", en = "Allow other players")
    protected static final String LINE_ALLOW_OTHERS = "gtocore.machine.game.allow_others";
    @RegisterLanguage(cn = "关闭后只有机器主人（含同队、好友与管理员）能打开并游玩这台机器",
                      en = "When off, only the machine owner (plus their team, friends and operators) can open and play it")
    protected static final String TOOLTIP_ALLOW_OTHERS = "gtocore.machine.game.allow_others.tooltip";
    @RegisterLanguage(cn = "只有机器主人能改这个开关", en = "Only the machine owner can change this")
    protected static final String REASON_ONLY_OWNER = "gtocore.machine.game.reason.only_owner";
    @RegisterLanguage(cn = "这台机器不允许你游玩", en = "You are not allowed to play this machine")
    protected static final String MSG_NO_PERMISSION = "gtocore.machine.game.msg.no_permission";
    @RegisterLanguage(cn = "里程碑", en = "Milestones")
    protected static final String SECTION_MILESTONES = "gtocore.machine.game.section.milestones";
    @RegisterLanguage(cn = "暂无记录", en = "No records yet")
    protected static final String TEXT_NO_RECORD = "gtocore.machine.game.text.no_record";
    @RegisterLanguage(cn = "%s天%s", en = "D%s %s")
    protected static final String TEXT_DAY_TIME = "gtocore.machine.game.text.day_time";
    /** 里程碑表格的列标题：几台游戏机共用的那几列。 */
    @RegisterLanguage(cn = "玩家", en = "Player")
    protected static final String COLUMN_PLAYER = "gtocore.machine.game.column.player";
    @RegisterLanguage(cn = "最高分", en = "Best score")
    protected static final String COLUMN_BEST_SCORE = "gtocore.machine.game.column.best_score";
    @RegisterLanguage(cn = "最大合成", en = "Best merged")
    protected static final String COLUMN_BEST_MERGED = "gtocore.machine.game.column.best_merged";
    @RegisterLanguage(cn = "用时 (分:秒)", en = "Time (m:ss)")
    protected static final String COLUMN_DURATION = "gtocore.machine.game.column.duration";
    @RegisterLanguage(cn = "达成：%s", en = "Achieved: %s")
    protected static final String TOOLTIP_ACHIEVED = "gtocore.machine.game.tooltip.achieved";

    /** 放下机器时可能还没有世界可借随机源，退回这个静态的。 */
    private static final RandomSource FALLBACK_RANDOM = RandomSource.create();

    /** 未取出的游戏币余额。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private long balance;
    /** 是否允许主人以外的玩家打开并游玩；默认允许。 */
    @SaveToDisk(defaultValue = "true")
    @SyncToClient(scheduleUpdate = true)
    private boolean allowOthers = true;
    /** 本局（本轮 / 本次旋转）的发起玩家：里程碑归属给他，也是「多人局」判定的基准。 */
    @SaveToDisk
    @Nullable
    private UUID sessionStarter;
    /** 本局是否出现过第二个玩家；置位后本局不写任何里程碑。 */
    @SaveToDisk
    private boolean sessionMultiplayer;
    /** 最近打开界面的玩家（不存盘）：配置页改开关时的操作者、以及本局发起者的来源。 */
    @Nullable
    private UUID viewerUUID;
    /** 界面刚打开、还没开局时先攒着，开局时拿出来当发起者（不存盘）。 */
    @Nullable
    private UUID pendingOpener;
    /** 界面快照版本号：只在服务端递增，靠它决定要不要重建 {@link #cachedView}。 */
    private int revision;

    /** 服务端当前快照；版本号没变就复用同一实例，不白复制。 */
    @Nullable
    private SyncValue<VIEW> viewSync;
    @Nullable
    private VIEW emptyView;
    @Nullable
    private VIEW cachedView;
    private int cachedRevision = Integer.MIN_VALUE;

    protected AbstractGameMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /** 拆除时先兑现还没结账的成绩，再把余额按面额换成游戏币掉回世界。机器不带走任何余额。 */
    @Override
    public void onMachineRemoved() {
        cashOut();
        CoinExchange.dropToWorld(getLevel(), getPos(), currencyId(), balance);
    }

    /**
     * 把还没结账的成绩折进余额。拆除机器、结束一局等时机都会调，所以子类必须保证同一份成绩只兑现一次；
     * 按题结算、下注即扣的游戏没有待兑现成绩，用默认空实现即可。
     */
    protected void cashOut() {}

    /** 这台机器收付哪种币：默认游戏币；用别的币的机器（老虎机收技术员币）覆盖它。 */
    protected String currencyId() {
        return TradeLang.GAME_COINS;
    }

    /** 与 {@link #currencyId()} 对应的面额表。 */
    protected CoinSeries currency() {
        return CoinExchange.gameCoins();
    }

    /** 币的面额名，取出按钮与硬币槽的工具提示用。 */
    protected final String currencyName() {
        return "gtocore.currency." + currencyId();
    }

    // ==================== 权限 ====================

    protected final boolean allowOthers() {
        return allowOthers;
    }

    /** 只有主人能改这个开关；改动会顶版本号，让两端的开关与按钮状态一起刷新。 */
    protected final void setAllowOthers(boolean allow) {
        if (isRemote() || allow == allowOthers) return;
        var player = viewer();
        if (player == null || !GameAccess.isOwner(this, player)) {
            send(player, REASON_ONLY_OWNER);
            return;
        }
        allowOthers = allow;
        syncAndMarkChanged();
    }

    /** 打开界面前先把「能不能玩」判掉：不允许就发一句提示并且不开界面。 */
    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (isRemote() || !(player instanceof ServerPlayer serverPlayer)) return true;
        if (GameAccess.canPlay(this, allowOthers, serverPlayer)) return true;
        serverPlayer.displayClientMessage(Component.translatable(MSG_NO_PERMISSION), true);
        return false;
    }

    /** 界面打开时记下查看者与本局参与者：同一局里出现第二个人就标成多人局。 */
    @Override
    public ModularUI createUI(Player player) {
        if (!isRemote() && player instanceof ServerPlayer serverPlayer) noteOpener(serverPlayer);
        return IFancyUIMachine.super.createUI(player);
    }

    /** 动作守卫：没权限就发提示并拒绝。拿不到玩家的路径（存档、拆机）不拦。 */
    protected final boolean guard(@Nullable ServerPlayer player) {
        if (player == null) return true;
        if (GameAccess.canPlay(this, allowOthers, player)) return true;
        player.displayClientMessage(Component.translatable(MSG_NO_PERMISSION), true);
        return false;
    }

    private void noteOpener(ServerPlayer player) {
        if (player instanceof FakePlayer) return;
        viewerUUID = player.getUUID();
        pendingOpener = player.getUUID();
        onViewerChanged();
        if (sessionStarter == null) {
            // 读档后直接开界面：这一局还没人认领，第一个进来的人就是发起者
            sessionStarter = player.getUUID();
            return;
        }
        if (sessionStarter.equals(player.getUUID())) return;
        sessionMultiplayer = true;
    }

    // ==================== 会话 ====================

    /** 服务端：开一局 / 开一轮 / 开一次旋转，把发起者定下来并清掉多人标记。 */
    protected final void beginSession(@Nullable ServerPlayer starter) {
        sessionStarter = starter != null && !(starter instanceof FakePlayer) ? starter.getUUID() : pendingOpener;
        sessionMultiplayer = false;
        pendingOpener = null;
    }

    /** 本局只有一名玩家时才写里程碑。 */
    protected final boolean sessionCounts() {
        return sessionStarter != null && !sessionMultiplayer;
    }

    protected final @Nullable UUID sessionStarter() {
        return sessionStarter;
    }

    /** 最近打开界面的玩家（服务端）；不在线时为 {@code null}。 */
    protected final @Nullable ServerPlayer viewer() {
        var server = server();
        return server == null || viewerUUID == null ? null : server.getPlayerList().getPlayer(viewerUUID);
    }

    /** 最近打开界面的玩家 UUID（可能离线）；按查看者构造快照的机器用它。 */
    protected final @Nullable UUID viewerId() {
        return viewerUUID;
    }

    /** 界面查看者变化时回调；需要按查看者重建快照的机器覆盖它。 */
    protected void onViewerChanged() {}

    protected final @Nullable MinecraftServer server() {
        return getLevel() instanceof ServerLevel level ? level.getServer() : null;
    }

    /** 当前存档的游戏时间：里程碑的达成时间与用时都以它为准。 */
    protected final long gameTime() {
        var level = getLevel();
        return level == null ? 0L : level.getGameTime();
    }

    /** 按钮背后要的玩家：界面的持有者，服务端才拿得到。 */
    protected static @Nullable ServerPlayer playerOf(Widget source) {
        var gui = source.getGui();
        return gui != null && gui.entityPlayer instanceof ServerPlayer player ? player : null;
    }

    private static void send(@Nullable ServerPlayer player, String langKey) {
        if (player != null) player.displayClientMessage(Component.translatable(langKey), true);
    }

    // ==================== 余额 ====================

    protected final long balance() {
        return balance;
    }

    /** 往余额里加币；超过 {@code long} 上限就饱和，不绕回负数。 */
    protected final void addBalance(long amount) {
        if (amount > 0) balance += Math.min(Long.MAX_VALUE - balance, amount);
    }

    /** 从余额里扣币（下注）；扣到 0 为止，返回实际扣掉的数量。 */
    protected final long removeBalance(long amount) {
        if (amount <= 0) return 0;
        long removed = Math.min(amount, balance);
        balance -= removed;
        return removed;
    }

    /** 状态变了之后统一走这里：顶版本号让界面通道下一 tick 带上新快照，并标记机器已变。 */
    protected final void syncAndMarkChanged() {
        if (!isRemote()) revision++;
        onChanged();
        onGameStateChanged();
    }

    /** {@link #syncAndMarkChanged()} 末尾的回调：要按状态刷新 tick 订阅等附带动作的机器（老虎机）在这里做。 */
    protected void onGameStateChanged() {}

    /** 当前快照版本号；子类把它写进自己的快照里，{@code equals} 只比它一个数。 */
    protected final int revision() {
        return revision;
    }

    /** 有世界就借它的随机源，没有（刚放下机器）就用静态兜底。 */
    protected final RandomSource random() {
        var level = getLevel();
        return level == null ? FALLBACK_RANDOM : level.getRandom();
    }

    /** 青色数字，状态面板与反馈文案共用。 */
    protected static MutableComponent number(long value) {
        return Component.literal(FormattingUtil.formatNumbers(value)).withStyle(ChatFormatting.AQUA);
    }

    /** 里程碑表格里的达成时间：{@code 第 N 天 HH:MM} 压成 {@code N天HH:MM}，列宽才放得下。 */
    protected static Component dayTime(long gameTime) {
        long day = gameTime / 24000L + 1;
        long timeOfDay = gameTime % 24000L;
        long hours = timeOfDay / 1000L;
        long minutes = timeOfDay % 1000L * 60L / 1000L;
        return Component.translatable(TEXT_DAY_TIME, day, twoDigits(hours) + ":" + twoDigits(minutes));
    }

    /** 里程碑表格里的用时：{@code 分:秒}，单位写在表头，格子只放数字才排得整齐。 */
    protected static Component duration(long ticks) {
        long seconds = ticks / 20L;
        return Component.literal(seconds / 60L + ":" + twoDigits(seconds % 60L));
    }

    /**
     * 电压等级的名字（带该档的颜色），盘面拿电压电路/电压字当格子的游戏用它显示成绩：
     * {@code 0} 是 ULV，{@code VN.length} 起按 {@code MAX+1}、{@code MAX+2} 往后排，和盘面上画的字一致。
     */
    protected static Component voltageName(int tier) {
        int index = Math.max(0, tier);
        int top = GTValues.VN.length - 1;
        if (index <= top) {
            return Component.literal(GTValues.VN[index]).withStyle(style -> style.withColor(TextColor.fromRgb(GTValues.VC[index])));
        }
        return Component.literal("MAX+" + (index - top)).withStyle(style -> style.withColor(TextColor.fromRgb(GTValues.VC[top])));
    }

    private static String twoDigits(long value) {
        return value < 10L ? "0" + value : Long.toString(value);
    }

    // ==================== 取出 ====================

    /** 「取出全部」按钮：余额为 0 时禁用；点一下把余额按面额从大到小塞进玩家背包，塞不下的留在余额里。 */
    protected final Button createWithdrawButton(int width) {
        var withdraw = Button.translatable(width, BUTTON_WITHDRAW);
        withdraw.setHoverTooltips(currencyName());
        withdraw.disabled(() -> balance == 0, REASON_NOTHING_TO_WITHDRAW);
        withdraw.setOnServerClick(() -> withdrawAll(withdraw));
        return withdraw;
    }

    /** 取出全部：按面额从大到小塞进玩家背包，塞不下的留在余额里。子类要自己加禁用条件时可以直接调它。 */
    protected final void withdrawAll(Widget source) {
        var gui = source.getGui();
        if (isRemote() || gui == null || !(gui.entityPlayer instanceof ServerPlayer player)) return;
        // allowOthers 关闭后只有主人能取出；拆机器时的 cashOut/掉币不走这里，不受影响。
        if (!guard(player)) return;
        var inventory = player.getInventory();
        long remaining = balance;
        if (remaining == 0) return;
        var destination = new PlayerMainInvWrapper(inventory);
        CoinSeries coins = currency();
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

    // ==================== 界面快照 ====================

    /**
     * 在界面上注册界面快照项：服务端与客户端各调一次，必须在两端走同一条路径（同一个控件、同样的顺序），
     * 通道里的条目编号才对得上。
     *
     * @param element 挂载点，通常就是游戏区那个控件
     * @param empty   客户端还没收到下发值时的占位快照
     * @param codec   网络专用编解码器（不与存盘格式共用）
     */
    protected final void registerView(UIElement element, VIEW empty, StreamCodec<? super FriendlyByteBuf, VIEW> codec) {
        emptyView = empty;
        viewSync = element.addSyncValue(SyncValue.of(this::viewSnapshot, codec, empty));
    }

    /** 界面显示用的快照：客户端读通道下发的那份，服务端直接现造一份。 */
    protected final VIEW view() {
        if (!isRemote()) return viewSnapshot();
        return viewSync == null ? emptyView : viewSync.getValue();
    }

    // ==================== 里程碑 ====================

    /** 表格默认高度：四行；最多撑到七行（与银行交易记录面板一致）。 */
    private static final int MILESTONE_HEIGHT = UISizes.SLOT_SIZE * 4;
    private static final int MILESTONE_MAX_HEIGHT = UISizes.SLOT_SIZE * 7;
    /** 名次列宽度：{@code #99} 放得下。 */
    private static final int MILESTONE_RANK_WIDTH = 16;
    /** 单元格之间的间隙。 */
    private static final int MILESTONE_GAP = 2;
    /** 隔行的淡色条纹：一眼看得出表格是一行一行的。 */
    private static final IGuiTexture MILESTONE_STRIPE = new ColorRectTexture(0x0A000000);

    /**
     * 里程碑表格的一列：表头文案、宽度与对齐。
     *
     * <p>
     * 列宽全部写死：服务端量不到字体，写死的尺寸两端才会搭出同一份布局（{@code LayoutStyle.AUTO} 配 {@code flex}
     * 在两端的文字测量结果不同，会错位）。宽了由 {@link TextLine} 截断加省略号，鼠标停上去看全文。
     */
    protected record MilestoneColumn(String headerKey, int width, Horizontal align) {

        /** 左对齐的列（玩家名这类文字）。 */
        public static MilestoneColumn left(String headerKey, int width) {
            return new MilestoneColumn(headerKey, width, Horizontal.LEFT);
        }

        /** 右对齐的列（数字）。 */
        public static MilestoneColumn right(String headerKey, int width) {
            return new MilestoneColumn(headerKey, width, Horizontal.RIGHT);
        }

        /** 居中的列（时间这类定长文字）。 */
        public static MilestoneColumn center(String headerKey, int width) {
            return new MilestoneColumn(headerKey, width, Horizontal.CENTER);
        }
    }

    /**
     * 表格的一个格子：显示的文字 + 悬停提示。提示用来放「达成时间」这种不占列的信息；提示为空就没有提示。
     */
    protected record MilestoneCell(Component text, Component hint) {

        private static final MilestoneCell EMPTY = new MilestoneCell(Component.empty(), Component.empty());

        /** 只有字的格子。 */
        public static MilestoneCell of(Component text) {
            return new MilestoneCell(text, Component.empty());
        }

        /** 带自定义悬停提示的格子。 */
        public static MilestoneCell of(Component text, Component hint) {
            return new MilestoneCell(text, hint);
        }

        /** 成绩格：字是成绩本身，达成时间放进悬停提示，表格就不用为时间单开一列。 */
        public static MilestoneCell achieved(Component value, long gameTime) {
            return new MilestoneCell(value, Component.translatable(TOOLTIP_ACHIEVED, dayTime(gameTime)));
        }
    }

    /** 服务端拿这份存档的里程碑存储；客户端或还没有世界时为 {@code null}。 */
    protected final @Nullable GameMilestones milestones() {
        return getLevel() instanceof ServerLevel level ? GameMilestones.get(level) : null;
    }

    /**
     * 里程碑表格：表头 + 每人一行，列宽固定、数值右对齐，整表可滚动。
     *
     * <p>
     * 表格由 {@link UIElement#row} 套 {@link TextLine} 搭出来（uipro 没有现成的表格控件，仓库里其它「表格」也是这么拼的）：
     * 表头用 {@link TextLine#constant} 两端同文、不加同步；数据格由服务端取值下发，带提示的格子再挂一项提示的同步值。
     *
     * <p>
     * 人数是 {@link SwitchedContent} 的键：人数变了才重建表格；同一行里数值变了由格子自己的同步值刷，不必重建。
     *
     * @param id      滚动区 id（拖动缩放的记忆键）
     * @param columns 列定义（不含名次列：名次由表格自动加在最前）
     * @param builder 服务端构建每一行的格子，顺序与 {@code columns} 一致
     */
    protected final ScrollerView milestoneTable(String id, MilestoneColumn[] columns,
                                                BiFunction<GameMilestones, MinecraftServer, List<MilestoneCell[]>> builder) {
        return milestoneTable(id, columns, this::gameMilestonesVersion, () -> gameMilestoneRows(builder));
    }

    /**
     * 里程碑表格的可复用版本：数据源不一定 {@link GameMilestones}（扫雷用自己的存档），
     * 但表格外观、排名、悬停与滚动都与其他游戏机器保持一致。
     */
    protected final ScrollerView milestoneTable(String id, MilestoneColumn[] columns, IntSupplier version,
                                                Supplier<List<MilestoneCell[]>> builder) {
        var table = new MilestoneTable(columns, version, builder);
        var scroller = new ScrollerView(id,
                table.width() + 2 * UISizes.PANEL_PADDING + ScrollerView.SCROLL_BAR_SPACE, MILESTONE_HEIGHT)
                .contentLayout(layout -> layout.paddingAll(UISizes.PANEL_PADDING));
        scroller.setBackground(UITheme.STATUS_PANEL);
        scroller.addScrollViewChild(new SwitchedContent(table::rowCount, (rows, remote) -> table.build(rows)));
        return scroller.setAdaptiveHeight(MILESTONE_MAX_HEIGHT);
    }

    private int gameMilestonesVersion() {
        var store = milestones();
        return store == null ? Integer.MIN_VALUE : store.modCount();
    }

    private List<MilestoneCell[]> gameMilestoneRows(
                                                    BiFunction<GameMilestones, MinecraftServer, List<MilestoneCell[]>> builder) {
        var store = milestones();
        var server = server();
        return store == null || server == null ? List.of() : builder.apply(store, server);
    }

    /** 一张表格的行缓存：数据版本没变就复用上次构建的行，不让每个格子各自重建一遍。 */
    private final class MilestoneTable {

        private final MilestoneColumn[] columns;
        private final IntSupplier version;
        private final Supplier<List<MilestoneCell[]>> builder;
        private final int width;
        private int mod = Integer.MIN_VALUE;
        private List<MilestoneCell[]> rows = List.of();

        private MilestoneTable(MilestoneColumn[] columns, IntSupplier version,
                               Supplier<List<MilestoneCell[]>> builder) {
            this.columns = columns;
            this.version = version;
            this.builder = builder;
            int sum = MILESTONE_RANK_WIDTH + MILESTONE_GAP * columns.length;
            for (var column : columns) {
                sum += column.width();
            }
            this.width = sum;
        }

        private int width() {
            return width;
        }

        /** 服务端：数据版本变了就重建行；返回人数，表格按它决定建几行。 */
        private int rowCount() {
            int current = version.getAsInt();
            if (mod != current) {
                mod = current;
                rows = builder.get();
            }
            return rows.size();
        }

        /** 第 {@code row} 行第 {@code column} 列的格子；越界给空格子（人数刚变、对面还没重建完时也不会崩）。 */
        private MilestoneCell cell(int row, int column) {
            if (row < 0 || row >= rows.size()) return MilestoneCell.EMPTY;
            var cells = rows.get(row);
            return column >= 0 && column < cells.length && cells[column] != null ? cells[column] : MilestoneCell.EMPTY;
        }

        /** 表头 + 每人一行；两端各自按同一个人数搭出同样的控件树。 */
        private Widget build(int rowCount) {
            var table = UIElement.column(LayoutStyle.AUTO).layout(layout -> layout.gapAll(MILESTONE_GAP));
            table.addChild(headerRow());
            if (rowCount <= 0) {
                table.addChild(TextLine.translatable(LayoutStyle.AUTO, TEXT_NO_RECORD).setTextAlign(Horizontal.CENTER));
                return table;
            }
            for (int row = 0; row < rowCount; row++) {
                table.addChild(dataRow(row));
            }
            return table;
        }

        private UIElement headerRow() {
            var row = UIElement.row(TextLine.HEIGHT).layout(layout -> layout.gapAll(MILESTONE_GAP));
            row.addChild(headerCell(MILESTONE_RANK_WIDTH, Component.literal("#"), Horizontal.RIGHT));
            for (var column : columns) {
                row.addChild(headerCell(column.width(), Component.translatable(column.headerKey()), column.align()));
            }
            return row;
        }

        private UIElement dataRow(int row) {
            var line = UIElement.row(TextLine.HEIGHT).layout(layout -> layout.gapAll(MILESTONE_GAP));
            line.addChild(TextLine.constant(MILESTONE_RANK_WIDTH, Component.literal("#" + (row + 1)))
                    .setTextAlign(Horizontal.RIGHT).bindClientColor(UITheme::textSecondary));
            for (int column = 0; column < columns.length; column++) {
                line.addChild(new CellLine(columns[column].width(), columns[column].align(), row, column));
            }
            if ((row & 1) == 1) line.setBackground(MILESTONE_STRIPE);
            return line;
        }

        private static TextLine headerCell(int width, Component text, Horizontal align) {
            return TextLine.constant(width, text).setTextAlign(align).bindClientColor(UITheme::textSecondary);
        }

        /**
         * 带悬停提示的格子：文字仍是服务端取值下发，提示单独走一项同步值，收到后写进 LDLib 的 tooltip 列表
         * ——提示只在悬停时画，但内容得先到客户端。每个格子都挂这项同步值（哪怕提示为空），两端控件树才和数据无关。
         */
        private final class CellLine extends TextLine {

            private CellLine(int width, Horizontal align, int row, int column) {
                super(width, () -> cell(row, column).text(), Component.empty());
                setTextAlign(align);
                addSyncValue(SyncValue.ofComponent(() -> cell(row, column).hint(), Component.empty()))
                        .onChanged(this::applyHint);
            }

            private void applyHint(Component hint) {
                if (hint == null || hint.getString().isEmpty()) {
                    tooltipTexts.clear();
                    return;
                }
                setHoverTooltips(hint);
            }
        }
    }

    /** 服务端当前快照；每次重建都要新建实例（相等性看版本号）。 */
    protected abstract VIEW createView();

    private VIEW viewSnapshot() {
        if (cachedView == null || cachedRevision != revision) {
            cachedView = createView();
            cachedRevision = revision;
        }
        return cachedView;
    }
}

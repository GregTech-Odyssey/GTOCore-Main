package com.gtocore.common.machine.noenergy.game;

import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.CoinSeries;
import com.gtocore.data.transaction.data.TradeLang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
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
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

/**
 * 游戏机的公共底座：只有「钱」和「下发通道」两件事，玩法、局面字段、界面排版与配置页都写在各自的机器里。
 *
 * <p>
 * 本类提供的部分：
 * <ul>
 * <li>余额：{@link #balance()} / {@link #addBalance(long)}，拆除时先 {@link #cashOut()} 兑现再按面额掉回世界；</li>
 * <li>取出：{@link #createWithdrawButton(int)} 那块「取出全部」按钮与它背后的取币逻辑；</li>
 * <li>界面快照：{@link #registerView} 在界面上注册一项同步值，子类给 {@link #createView()}，版本号没变就复用实例；</li>
 * <li>共用的文案与两个小工具：{@link #random()}、{@link #number(long)}。</li>
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

    /** 游戏币的面额名，取出按钮的工具提示用。 */
    protected static final String CURRENCY_NAME = "gtocore.currency." + TradeLang.GAME_COINS;

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

    /** 放下机器时可能还没有世界可借随机源，退回这个静态的。 */
    private static final RandomSource FALLBACK_RANDOM = RandomSource.create();

    /** 未取出的游戏币余额。 */
    @SaveToDisk
    @SyncToClient(scheduleUpdate = true)
    private long balance;
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
        CoinExchange.dropToWorld(getLevel(), getPos(), TradeLang.GAME_COINS, balance);
    }

    /**
     * 把还没结账的成绩折进余额。拆除机器、结束一局等时机都会调，所以子类必须保证同一份成绩只兑现一次；
     * 按题结算、下注即扣的游戏没有待兑现成绩，用默认空实现即可。
     */
    protected void cashOut() {}

    // ==================== 余额 ====================

    protected final long balance() {
        return balance;
    }

    /** 往余额里加币；超过 {@code long} 上限就饱和，不绕回负数。 */
    protected final void addBalance(long amount) {
        if (amount > 0) balance += Math.min(Long.MAX_VALUE - balance, amount);
    }

    /** 状态变了之后统一走这里：顶版本号让界面通道下一 tick 带上新快照，并标记机器已变。 */
    protected final void syncAndMarkChanged() {
        if (!isRemote()) revision++;
        onChanged();
    }

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

    // ==================== 取出 ====================

    /** 「取出全部」按钮：余额为 0 时禁用；点一下把余额按面额从大到小塞进玩家背包，塞不下的留在余额里。 */
    protected final Button createWithdrawButton(int width) {
        var withdraw = Button.translatable(width, BUTTON_WITHDRAW);
        withdraw.setHoverTooltips(CURRENCY_NAME);
        withdraw.disabled(() -> balance == 0, REASON_NOTHING_TO_WITHDRAW);
        withdraw.setOnServerClick(() -> withdrawAll(withdraw));
        return withdraw;
    }

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

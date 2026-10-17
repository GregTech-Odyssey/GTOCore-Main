package com.gtocore.common.machine.noenergy.game;

import com.gtocore.data.transaction.data.CoinExchange;
import com.gtocore.data.transaction.data.CoinSeries;
import com.gtocore.data.transaction.data.TradeLang;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
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
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 游戏机的公共壳子：余额与取出、拆除兑现、界面快照的下发、状态面板与配置页的骨架，都收在这里，三个游戏机器只留
 * 自己的规则、局面字段与游戏区绘制。
 *
 * <p>
 * 各机器共用的部分：
 * <ul>
 * <li>余额：{@link #balance()} / {@link #addBalance(long)}，以及「取出全部」按钮与 {@link #onMachineRemoved()} 的掉落；</li>
 * <li>界面快照：{@link #registerView} 在游戏区注册一项同步值，子类给 {@link #createView()}，版本号没变就复用实例；</li>
 * <li>页面骨架：{@link #createUIWidget()}（游戏区 + 状态面板）与 {@link #attachSideTabs}（多挂一个 {@link SettingsPage} 配置页）；</li>
 * <li>通用文案：余额、取出全部、配置页标题与图标等。</li>
 * </ul>
 *
 * <p>
 * 余额等字段声明在父类：DataSyncLib 会沿父类链合并注解字段，存盘键就是字段名（{@code field.getName()}），所以这些
 * 字段换到父类不影响旧存档。{@link RegisterLanguage} 的扫描只看类自身的字段，所以本类同样要带
 * {@link DataGeneratorScanned}，共用的那几条文案才不会漏生成。
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

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        sanitizeState();
    }

    /** 拆除时先兑现还没结账的成绩，再把余额按面额换成游戏币掉回世界。机器不带走任何余额。 */
    @Override
    public void onMachineRemoved() {
        cashOut();
        CoinExchange.dropToWorld(getLevel(), getPos(), TradeLang.GAME_COINS, balance);
    }

    // ==================== 子类接口 ====================

    /**
     * 把还没结账的成绩折进余额。拆除机器、结束一局等时机都会调，所以子类必须保证同一份成绩只兑现一次；
     * 按题结算、下注即扣的游戏没有待兑现成绩，直接用默认空实现即可。
     */
    protected void cashOut() {}

    /** 读档后的收尾：取值范围夹紧、尺寸/范围与存档对不上就作废重来等。只在服务端调用。 */
    protected abstract void sanitizeState();

    /** 游戏区（盘面 / 牌面），不含状态面板。首次打开界面时由 {@link #createUIWidget()} 调用。 */
    protected abstract UIElement createGameArea();

    /** 首次打开界面时把没开的那一局开起来；已经在局中或没有「开局」概念时什么都不做。 */
    protected abstract void ensureStarted();

    /** 服务端当前快照；每次重建都要新建实例（相等性看版本号）。 */
    protected abstract VIEW createView();

    /** 配置页；不需要配置项的机器返回一个没有选项的页即可。 */
    protected abstract IFancyUIProvider createSettingsPage();

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

    // ==================== 界面快照 ====================

    /**
     * 在游戏区注册界面快照项：服务端与客户端各调一次，必须在两端走同一条路径（同一个控件、同样的顺序），
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

    private VIEW viewSnapshot() {
        if (cachedView == null || cachedRevision != revision) {
            cachedView = createView();
            cachedRevision = revision;
        }
        return cachedView;
    }

    // ==================== 界面 ====================

    /**
     * 主页之外再挂一个「配置」子页：GTM 的 {@code TabsWidget} 只当数据容器，uipro 的 {@code MachineWindow} 据此画
     * 顶部标签栏，并把每个子页当独立界面处理（各自 {@code createMainPage}、各自是否显示玩家背包）。
     */
    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        IFancyUIMachine.super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(createSettingsPage());
    }

    @Override
    public Widget createUIWidget() {
        if (!isRemote()) ensureStarted();

        var page = Form.page();
        page.addChildren(createGameArea(), createStatusPanel());
        return page;
    }

    /** 状态面板：余额 + {@link #addStatusLines} 补的行 + {@link #createStatusActions()} 那一排按钮。 */
    protected StatusPanel createStatusPanel() {
        var status = panel();
        status.addLine(LINE_BALANCE, () -> number(balance));
        addStatusLines(status);
        status.addChild(createStatusActions());
        return status;
    }

    /** 带底图的面板，行距统一 2px；状态面板、配置页面板与游戏区都用它，免得各处各写一遍。 */
    protected static StatusPanel panel() {
        var panel = new StatusPanel();
        panel.getLayoutStyle().gapAll(2);
        return panel;
    }

    /** 状态面板里除余额以外的行。 */
    protected void addStatusLines(StatusPanel status) {}

    /** 配置页余额面板里除余额以外的行。 */
    protected void addSettingsLines(StatusPanel status) {}

    /** 状态面板底部那一排操作按钮（重新开始 / 取出全部 / 提示…）。 */
    protected abstract UIElement createStatusActions();

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

    /**
     * 配置页骨架：一个区块（说明文字 + 若干选项按钮）+ 余额面板。选项由子类给出名字、可选的比例文案、当前选中项与
     * 点击后的服务端动作，页面的搭法与标签栏对接都在这里，三个游戏只写自己的那几个钩子。
     */
    protected abstract class SettingsPage implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var page = Form.page();

            var section = Form.section(settingsTitle());
            var hint = settingsHint();
            if (hint != null) section.addChild(TextLine.translatable(LayoutStyle.AUTO, hint));
            var names = optionNames();
            for (int ordinal = 0; ordinal < names.length; ordinal++) {
                section.addChild(createOptionButton(ordinal, names[ordinal]));
            }
            page.addChild(section);
            page.addChild(createSettingsStatus());
            return page;
        }

        /** 一个选项一个按钮：文字是「名字 · 附加说明（可选）」，当前选项用选中框标出；点击才发服务端请求。 */
        private Button createOptionButton(int ordinal, String name) {
            var label = Component.translatable(name);
            var detail = optionDetail(ordinal);
            if (detail != null) label.append(" · ").append(detail);
            var button = Button.text(LayoutStyle.AUTO, label);
            button.setOnServerClick(() -> applyOption(ordinal));
            button.setSelected(() -> currentOption() == ordinal);
            return button;
        }

        /** 配置页的余额面板；还需要显示别的数值就重写 {@link AbstractGameMachine#addSettingsLines}。 */
        protected StatusPanel createSettingsStatus() {
            var status = panel();
            status.addLine(LINE_BALANCE, () -> number(balance));
            addSettingsLines(status);
            return status;
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

        /** 区块标题的 lang key。 */
        protected abstract String settingsTitle();

        /** 区块里那句说明的 lang key，没有说明就返回 {@code null}（整行不画）。 */
        protected @Nullable String settingsHint() {
            return null;
        }

        /** 选项名字的 lang key 表，下标就是选项序号。 */
        protected abstract String[] optionNames();

        /** 选项按钮上「 · 」后面的附加说明（如兑现比例），没有就返回 {@code null}。 */
        protected @Nullable Component optionDetail(int ordinal) {
            return null;
        }

        /** 当前选中的选项序号（读同步下来的配置字段）。 */
        protected abstract int currentOption();

        /** 服务端：切到这个选项。 */
        protected abstract void applyOption(int ordinal);
    }
}

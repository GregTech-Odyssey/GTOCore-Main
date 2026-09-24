package com.gtocore.integration.ae.wireless;

import com.gtocore.api.gui.GTOGuiTextures;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Indicator;
import com.gregtechceu.gtceu.uipro.elements.ItemView;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uipro.window.Popup;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.networking.pathing.ControllerState;
import appeng.core.definitions.AEItems;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 无线网络界面（新式 UI 框架）：ME 无线连接机主页、ME 部件的"无线网络"侧标签页，以及与配置器共用的片段。
 *
 * <pre>
 * ┌ 标题栏：● 网络名 ──────────────────────────┐   ┌ 网络详情（弹出面板）──────┐
 * │ ┌ 状态面板：状态 / (网络) / 所有者 / 成员   │   │ [输入框（占位=当前名）][重命名] │
 * │ └ [网络详情（整行）]                        │   │ 成员（n）滚动列表（≤4 行）   │
 * │ ┌ [网络名称输入框 …………] [新建]            │   │ 控制器冲突提示 [删除网络]   │
 * │ │ [★] 名称 …………………………… [加入]/[断开]   │   │   → 确认删除？[确认][取消] │
 * └ └（滚动列表，打开时 2~4 行）               ┘   └─────────────────────────┘
 * 状态面板的"状态"行在操作后 3 秒内显示操作结果，不单独占一行。
 * </pre>
 *
 * 放在 {@link MachineWindow} 里时网络名进标题栏、详情用弹出面板；放在 GTM 外壳里（ME 部件自身界面、多方块主机里的部件页）时，
 * 状态面板多一行"网络"，详情与主页用 {@link WirelessSwitch} 原位切换。只读状态一律放进状态面板（{@link StatusPanel}），不用散落的文字行。
 * <p>
 * 尺寸：由 Taffy 布局决定（flexbox）。页面至少标准内容宽，区块、按钮、列表行都被拉伸到同宽，名称类用 {@code flex(1)} 吃满剩余宽度；
 * 列表高度跟随内容，最多 {@link #LIST_MAX_VISIBLE_ROWS} 行且窗口不超过屏幕 2/3（见 {@link #maxRows}），超出滚动；
 * 列表右下角可拖拽缩放，窗口随内容一起变。尺寸只影响客户端外观，两端控件树始终一致。
 * <p>
 * 数据同步：机器与网络数据由各控件的 {@link SyncValue} 从服务端下发；列表行由 {@link WirelessRows} 服务端驱动增删；
 * 操作只走 {@link Button#setOnServerClick} 与 {@link TextField}，以打开界面的玩家校验权限。界面状态在 {@link WirelessUIContext}。
 * <p>
 * 查看权限：机器所在网络的名称、所有者、成员数、成员坐标只下发给能使用该网络或能管理这台机器的玩家
 * （{@link #canView}），其他人看到"无权查看的网络"，详情打不开。
 */
@DataGeneratorScanned
public final class WirelessMachineUI {

    @RegisterLanguage(cn = "无线网络", en = "Wireless Network")
    static final String TAB = "gtocore.wireless.tab";
    @RegisterLanguage(cn = "独立运行", en = "Standalone")
    static final String STANDALONE = "gtocore.wireless.standalone";
    @RegisterLanguage(cn = "状态", en = "Status")
    static final String LINE_STATE = "gtocore.wireless.line.state";
    @RegisterLanguage(cn = "网络", en = "Network")
    static final String LINE_NETWORK = "gtocore.wireless.line.network";
    @RegisterLanguage(cn = "所有者", en = "Owner")
    static final String LINE_OWNER = "gtocore.wireless.line.owner";
    @RegisterLanguage(cn = "成员", en = "Members")
    static final String LINE_MEMBERS = "gtocore.wireless.line.members";
    // 状态面板"状态"行的短文字（完整说明句留给 Jade 等地方）：行内只剩约 12 个汉字的宽度
    @RegisterLanguage(cn = "未加入网络", en = "Not joined")
    static final String STATE_STANDALONE = "gtocore.wireless.line.state.standalone";
    @RegisterLanguage(cn = "在线", en = "Online")
    static final String STATE_ONLINE = "gtocore.wireless.line.state.online";
    @RegisterLanguage(cn = "ME 网络离线", en = "ME network offline")
    static final String STATE_OFFLINE = "gtocore.wireless.line.state.offline";
    @RegisterLanguage(cn = "所有者无权使用此网络", en = "Owner not permitted")
    static final String STATE_NO_PERMISSION = "gtocore.wireless.line.state.no_permission";
    @RegisterLanguage(cn = "网络数据不可用", en = "Data unavailable")
    static final String STATE_UNAVAILABLE = "gtocore.wireless.line.state.unavailable";
    @RegisterLanguage(cn = "未绑定所有者", en = "No machine owner")
    static final String STATE_NO_OWNER = "gtocore.wireless.line.state.no_owner";
    @RegisterLanguage(cn = "控制器冲突", en = "Controller conflict")
    static final String STATE_CONFLICT = "gtocore.wireless.line.state.conflict";
    /** 状态行悬停说明：未绑定所有者的完整原因。 */
    @RegisterLanguage(cn = "机器未绑定所有者，按操作玩家校验权限", en = "No machine owner; the acting player's permissions apply")
    static final String NO_OWNER = "gtocore.wireless.no_owner";
    /** 状态面板里没有值（未加入 / 无权查看）时显示的占位。 */
    static final String NO_VALUE = "—";
    @RegisterLanguage(cn = "暂无可用网络", en = "No networks available")
    static final String EMPTY = "gtocore.wireless.empty";
    @RegisterLanguage(cn = "网络名称", en = "Network name")
    static final String NAME_PLACEHOLDER = "gtocore.wireless.name_placeholder";
    @RegisterLanguage(cn = "新建", en = "Create")
    static final String CREATE = "gtocore.wireless.create";
    @RegisterLanguage(cn = "加入", en = "Join")
    static final String JOIN = "gtocore.wireless.join";
    @RegisterLanguage(cn = "断开", en = "Leave")
    static final String LEAVE = "gtocore.wireless.leave";
    @RegisterLanguage(cn = "网络详情", en = "Network Details")
    static final String DETAIL = "gtocore.wireless.detail";
    @RegisterLanguage(cn = "未加入网络，或无权查看此网络", en = "Not joined, or not permitted to view this network")
    static final String DETAIL_UNAVAILABLE = "gtocore.wireless.detail_unavailable";
    @RegisterLanguage(cn = "收藏：潜行放置无线机器时自动加入此网络", en = "Favorite: wireless machines placed while sneaking join this network")
    static final String FAVORITE = "gtocore.wireless.favorite";
    @RegisterLanguage(cn = "此机器不能连接无线网络", en = "This machine cannot connect to wireless networks")
    static final String BANNED = "gtocore.wireless.banned";
    @RegisterLanguage(cn = "网络详情：%s", en = "Network: %s")
    static final String DETAIL_TITLE = "gtocore.wireless.detail_title";
    @RegisterLanguage(cn = "返回", en = "Back")
    static final String BACK = "gtocore.wireless.back";
    @RegisterLanguage(cn = "重命名", en = "Rename")
    static final String RENAME = "gtocore.wireless.rename";
    @RegisterLanguage(cn = "成员（%s）", en = "Members (%s)")
    static final String MEMBERS = "gtocore.wireless.members";
    @RegisterLanguage(cn = "%1$s  %2$s  [%3$s]", en = "%1$s  %2$s  [%3$s]")
    static final String MEMBER_ROW = "gtocore.wireless.member_row";
    @RegisterLanguage(cn = "点击：在聊天栏生成传送命令", en = "Click: put a teleport command in chat")
    static final String MEMBER_HINT = "gtocore.wireless.member_hint";
    @RegisterLanguage(cn = "[传送到 %s]", en = "[Teleport to %s]")
    static final String MEMBER_TP = "gtocore.wireless.member_tp";
    @RegisterLanguage(cn = "控制器冲突：网络内有多个互不相连的 ME 控制器", en = "Controller conflict: several unconnected ME controllers")
    static final String CONFLICT = "gtocore.wireless.conflict";
    @RegisterLanguage(cn = "删除网络", en = "Delete")
    static final String DELETE = "gtocore.wireless.delete";
    @RegisterLanguage(cn = "确认删除？", en = "Delete?")
    static final String DELETE_CONFIRM = "gtocore.wireless.delete_confirm";
    @RegisterLanguage(cn = "删除后所有成员立即断开，且无法恢复", en = "All members disconnect at once; this cannot be undone")
    static final String DELETE_WARNING = "gtocore.wireless.delete_warning";
    @RegisterLanguage(cn = "确认", en = "Confirm")
    static final String CONFIRM = "gtocore.wireless.confirm";
    @RegisterLanguage(cn = "取消", en = "Cancel")
    static final String CANCEL = "gtocore.wireless.cancel";

    /** 网络详情弹出面板的键（参数不使用）。 */
    public static final String DETAIL_POPUP = "wireless_network_detail";

    /** 列表最多显示行数的下限（屏幕很矮时也至少能看到这么多行）。 */
    static final int LIST_MIN_ROWS = 2;
    /** 列表最多显示的行数，再多滚动查看（与"窗口不超过屏幕 2/3"两者先到为准）。 */
    static final int LIST_MAX_VISIBLE_ROWS = 10;
    /** 详情弹出面板里成员列表最多显示的行数，超出滚动。 */
    static final int LIST_MAX_ROWS = 4;
    /** 列表的首选宽度：标准内容宽减去区块左右内边距（被拉伸或拖宽时更宽）。 */
    static final int LIST_WIDTH = UISizes.CONTENT_WIDTH - 2 * UITheme.PANEL_PADDING;
    /** 成员行高：16 的机器图标，右侧两行小字。 */
    static final int MEMBER_ROW_HEIGHT = UISizes.SLOT;
    /** 窗口外框与标题行占的高度（上下内边距 + 标题行 + 与页面的间距），估算整个窗口高度用。 */
    static final int WINDOW_CHROME = UISizes.WINDOW_PADDING_TOP + UISizes.CONTROL_HEIGHT + UISizes.SECTION_GAP + UISizes.WINDOW_PADDING_BOTTOM;
    /** 网络区块除列表视口外的高度：上下内边距（ME 无线连接机另有新建行，见 {@link #CREATE_ROW_HEIGHT}）。 */
    static final int NETWORK_SECTION_FIXED = UITheme.PANEL_PADDING + UITheme.PANEL_PADDING_BOTTOM;
    /** 新建行连同与列表的间距。 */
    static final int CREATE_ROW_HEIGHT = UISizes.CONTROL_HEIGHT + UISizes.GAP;

    static final SyncValue.Codec<String> STRING_CODEC = new SyncValue.Codec<>() {

        @Override
        public void write(FriendlyByteBuf buf, String value) {
            buf.writeUtf(value);
        }

        @Override
        public String read(FriendlyByteBuf buf) {
            return buf.readUtf();
        }
    };

    private WirelessMachineUI() {}

    // ==================== 入口 ====================

    /** ME 部件侧边的"无线网络"标签页。 */
    public static IFancyUIProvider tab(WirelessMachine machine) {
        return new IFancyUIProvider() {

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                return createPage(machine, widget);
            }

            @Override
            public IGuiTexture getTabIcon() {
                return new ItemStackTexture(AEItems.WIRELESS_RECEIVER.stack());
            }

            @Override
            public Component getTitle() {
                return Component.translatable(TAB);
            }

            @Override
            public boolean hasPlayerInventory() {
                return false;
            }
        };
    }

    /**
     * 机器页（连接机主页、ME 部件侧标签页）。宽度至少 {@link UISizes#CONTENT_WIDTH}，其余全部由布局决定：
     * 各区块、按钮被拉伸到同宽，列表被拖宽时整页一起变宽；高度随内容，窗口跟着变。
     */
    public static Widget createPage(WirelessMachine machine, FancyMachineUIWidget host) {
        var root = page();
        if (!machine.allowWirelessConnection()) {
            return root.addChild(TextLine.translatable(LayoutStyle.AUTO, BANNED).setColor(UITheme.STATUS_OFFLINE));
        }
        var ctx = new WirelessUIContext(host.getGui().entityPlayer, machine.self()::getOffsetTimer);
        if (!ctx.remote) WirelessSync.pushTo(ctx.serverPlayer());

        var main = page();
        if (host instanceof MachineWindow window) {
            window.setTitleContent(width -> header(machine, width, UITheme.TEXT, () -> networkName(ctx, machine)));
            // 客户端只在服务端决定打开后才构建面板，所以查看权限只需服务端判定
            window.registerPopup(DETAIL_POPUP, argument -> ctx.remote || canView(ctx, machine) ? detailPopup(machine, ctx) : null);
            buildMain(main, machine, ctx, true, maxRows(ctx, mainOtherHeight(3, machine), UISizes.CONTROL_HEIGHT),
                    detail -> detail.setOnClientClick(() -> window.togglePopup(DETAIL_POPUP, 0)));
            root.addChild(main);
        } else {
            // 主页与原位详情页叠放，只显示当前页，窗口高度跟随当前页
            var detailPage = page();
            var pages = new WirelessSwitch(main, detailPage);
            var back = Button.icon(UITheme.ARROW_LEFT);
            back.setHoverTooltips(BACK);
            var title = TextLine.of(0, () -> detailTitle(ctx, machine));
            title.layout(l -> l.flex(1));
            detailPage.addChild(UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChildren(back, title));
            // 点"确认"删除后两端都回到主页（按钮的 setOnClick 两端各执行一次）
            buildDetail(detailPage, machine, ctx, "wireless.part.members", maxRows(ctx, detailOtherHeight(), MEMBER_ROW_HEIGHT), () -> pages.select(0), button -> {});
            buildMain(main, machine, ctx, false, maxRows(ctx, mainOtherHeight(4, machine), UISizes.CONTROL_HEIGHT), detail -> detail.setOnClick(click -> pages.select(1)));
            back.setOnClick(click -> pages.select(0));
            pages.select(0);
            root.addChild(pages);
        }
        return root;
    }

    /** 页面级纵向容器：至少标准内容宽，宽度随内容（被拖宽的列表）变化，子元素拉伸到同宽。 */
    private static UIElement page() {
        return new UIElement().layout(l -> l.column().minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));
    }

    // ==================== 标题行 ====================

    /** 指示灯 + 一行文字（服务端取值），高 {@link UISizes#CONTROL_HEIGHT}。 */
    static UIElement header(WirelessMachine machine, int width, int color, Supplier<Component> text) {
        var indicator = Indicator.of(() -> machine.getWirelessLinkState().ordinal(),
                Indicator.State.of(UITheme.TEXT_SECONDARY, WirelessMachine.KEY_STATE_STANDALONE),
                Indicator.State.of(UITheme.STATUS_ONLINE, WirelessMachine.KEY_STATE_ONLINE),
                Indicator.State.of(UITheme.STATUS_OFFLINE, WirelessMachine.KEY_STATE_OFFLINE),
                Indicator.State.of(UITheme.STATUS_WARNING, WirelessMachine.KEY_STATE_NO_PERMISSION),
                Indicator.State.of(UITheme.STATUS_WARNING, WirelessMachine.KEY_STATE_UNAVAILABLE));
        var line = TextLine.of(0, text).setColor(color);
        line.layout(l -> l.flex(1));
        return UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.width(width).gapAll(UISizes.SECTION_GAP).alignCenter())
                .addChildren(indicator, line);
    }

    // ==================== 主页 ====================

    /**
     * @param inWindow 在 {@link MachineWindow} 里（网络名已在标题栏）：区块第一行只放说明；否则第一行是 ● 网络名 + 说明
     * @param maxRows  网络列表最多显示的行数（高度跟随内容，超出滚动）
     */
    private static void buildMain(UIElement main, WirelessMachine machine, WirelessUIContext ctx, boolean inWindow, int maxRows,
                                  Consumer<Button> detailAction) {
        // 当前网络：状态面板 + 紧贴其下、与面板两边对齐的整行 [网络详情]（都被拉伸到页面宽）
        var current = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP));
        var detail = Button.translatable(LayoutStyle.AUTO, DETAIL)
                .disabled(() -> machine.getWirelessNetwork() == null || !canView(ctx, machine), DETAIL_UNAVAILABLE);
        detailAction.accept(detail);
        current.addChildren(statusPanel(machine, ctx, !inWindow), detail);
        main.addChild(current);

        // 可用网络：当前网络那一行的按钮是 [断开]（红色），其余行是 [加入]。
        // 新建行只有连接机有，新建前先按加入的规则预检，不会建出加不进去的网络
        boolean create = canCreate(machine);
        main.addChild(networkSection(ctx, create ? "wireless.networks" : "wireless.part.networks", maxRows, JOIN, LEAVE,
                id -> id.equals(machine.getWirelessNetworkId()),
                id -> machine.joinWireless(ctx.serverPlayer(), id),
                from -> {
                    var result = machine.leaveWireless(ctx.serverPlayer());
                    if (result.ok()) closeDetail(from);
                    return result;
                },
                create ? () -> machine.checkCreateAndJoin(ctx.serverPlayer()) : null,
                create ? network -> machine.joinWireless(ctx.serverPlayer(), network.id()) : null));
    }

    /**
     * 列表最多显示的行数（高度跟随内容，放不下才滚动）：不超过 {@link #LIST_MAX_VISIBLE_ROWS} 行，
     * 也不让整个窗口高过屏幕（GUI 缩放后）的 {@link UISizes#MAX_WINDOW_SCREEN_RATIO}——两者哪个先到按哪个，至少 {@link #LIST_MIN_ROWS} 行。
     * {@code otherHeight} 是页面里除列表视口外的高度，{@code rowHeight} 是一行的高度。
     * <p>
     * 屏幕尺寸只在客户端知道，服务端按行数上限算；两端尺寸不同不影响同步（控件树一致）。
     * 玩家还可以拖列表右下角的缩放角改变高度。
     */
    static int maxRows(WirelessUIContext ctx, int otherHeight, int rowHeight) {
        if (!ctx.remote) return LIST_MAX_VISIBLE_ROWS;
        return Math.clamp(rowsFitting(otherHeight, rowHeight), LIST_MIN_ROWS, LIST_MAX_VISIBLE_ROWS);
    }

    /** 客户端：窗口不超过屏幕高度上限时，列表视口最多能放几行。 */
    @OnlyIn(Dist.CLIENT)
    private static int rowsFitting(int otherHeight, int rowHeight) {
        int budget = (int) (Minecraft.getInstance().getWindow().getGuiScaledHeight() * UISizes.MAX_WINDOW_SCREEN_RATIO) - WINDOW_CHROME - otherHeight;
        return (budget + UISizes.GAP) / (rowHeight + UISizes.GAP);
    }

    /** 主页除网络列表视口外的高度：状态面板（{@code statusLines} 行）+ [网络详情] + 网络区块的固定部分（连接机含新建行）。 */
    private static int mainOtherHeight(int statusLines, WirelessMachine machine) {
        return StatusPanel.heightFor(statusLines) + UISizes.GAP + UISizes.CONTROL_HEIGHT + UISizes.SECTION_GAP + NETWORK_SECTION_FIXED +
                (canCreate(machine) ? CREATE_ROW_HEIGHT : 0);
    }

    /** 只有 ME 无线连接机能在界面里新建网络；ME 部件、配置器只能从已有网络里选。 */
    private static boolean canCreate(WirelessMachine machine) {
        return machine instanceof MeWirelessConnectMachine;
    }

    /** 原位详情页除成员列表视口外的高度：标题行、改名区块、成员区块的标题与内边距、删除区块。 */
    private static int detailOtherHeight() {
        int section = UITheme.PANEL_PADDING + UISizes.CONTROL_HEIGHT + UITheme.PANEL_PADDING_BOTTOM;
        return UISizes.CONTROL_HEIGHT + UISizes.SECTION_GAP + section + UISizes.SECTION_GAP +
                (section + TextLine.HEIGHT + UISizes.GAP) + UISizes.SECTION_GAP + section;
    }

    /**
     * 网络区块：可用网络的滚动列表（高度跟随内容，最多 {@code maxRows} 行，超出滚动），每行 [★] 名称 [操作]。
     * 当前网络那一行的按钮换成 {@code currentKey}（红色，如"断开"），执行 {@code currentAction}。与部件标签页、配置器共用。
     * 只有 ME 无线连接机在列表上方有新建行（输入框 + [新建]），其他地方 {@code precheck} 传 null、不显示新建行。
     *
     * @param scrollerId    列表滚动区的固定 id（锁定的高度按它保存）
     * @param isCurrent     服务端：该网络是否就是当前网络
     * @param action        服务端：点击其他行的操作按钮
     * @param currentAction 服务端：点击当前网络那一行的按钮，参数是被点的按钮
     * @param precheck      服务端：新建前的检查；null 时没有新建行
     * @param afterCreate   服务端：新建成功后做的事（加入 / 选中）
     */
    static UIElement networkSection(WirelessUIContext ctx, String scrollerId, int maxRows, String actionKey, String currentKey,
                                    Predicate<String> isCurrent, Function<String, WirelessStatus> action,
                                    Function<Widget, WirelessStatus> currentAction, @Nullable Supplier<WirelessStatus> precheck,
                                    @Nullable Function<WirelessNetwork, WirelessStatus> afterCreate) {
        var section = UIElement.section();
        var scroller = new ScrollerView(scrollerId, LIST_WIDTH, listHeight(1)).adaptiveHeight(listHeight(maxRows));
        if (precheck != null && afterCreate != null) {
            // 新建行与列表行同宽：[新建] 与各行的 [加入] 右对齐成一列；滚动条出现时新建行右侧让出滚动条的宽度
            var create = createRow(ctx, precheck, afterCreate);
            scroller.setOnContentWidthChanged(contentWidth -> create.layout(l -> l.marginRight(scroller.isVerticalScrollBarShown() ? ScrollerView.SCROLL_BAR_SPACE : 0)));
            section.addChild(create);
        }
        scroller.addScrollViewChild(new WirelessRows<>(ctx.remote, STRING_CODEC,
                () -> ctx.networks().listFor(ctx.uuid()).stream().map(WirelessNetwork::id).toList(),
                () -> ctx.networks().revision(),
                id -> networkRow(id, ctx, actionKey, currentKey, isCurrent, action, currentAction),
                Component.translatable(EMPTY)));
        return section.addChild(scroller);
    }

    /** {@code rows} 行（行高 14、行距 2）的列表视口高度。 */
    static int listHeight(int rows) {
        return rows * UISizes.CONTROL_HEIGHT + (rows - 1) * UISizes.GAP;
    }

    private static UIElement networkRow(String id, WirelessUIContext ctx, String actionKey, String currentKey,
                                        Predicate<String> isCurrent, Function<String, WirelessStatus> action,
                                        Function<Widget, WirelessStatus> currentAction) {
        // 宽度由列表拉伸，名称 flex(1) 吃掉剩余宽度
        var row = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
        var favorite = row.addSyncValue(SyncValue.of(() -> id.equals(ctx.networks().favorite(ctx.uuid())), SyncValue.BOOLEAN, false));
        var current = row.addSyncValue(SyncValue.of(() -> isCurrent.test(id), SyncValue.BOOLEAN, false));
        var star = Button.icon(UITheme.switching(favorite::getValue, GTOGuiTextures.FAVORITE_OFF, GTOGuiTextures.FAVORITE_ON))
                .setOnServerClick(() -> ctx.report(ctx.networks().toggleFavorite(ctx.serverPlayer(), id)));
        star.setHoverTooltips(FAVORITE);
        var name = TextLine.of(0, () -> {
            var network = ctx.networks().get(id);
            return network == null ? Component.empty() : Component.literal(network.name());
        }).setColor(UITheme.PANEL_TEXT);
        name.layout(l -> l.flex(1));
        // 当前网络：红色 [断开]；其他网络：[加入]。点击时以服务端的当前网络为准，不信客户端显示
        var button = Button.text(UISizes.BUTTON_WIDTH, () -> Component.translatable(current.getValue() ? currentKey : actionKey).getString())
                .setVariant(() -> current.getValue() ? UITheme.ButtonVariant.DANGER : UITheme.ButtonVariant.DEFAULT);
        button.setOnServerClick(() -> ctx.report(isCurrent.test(id) ? currentAction.apply(button) : action.apply(id)));
        return row.addChildren(star, name, button);
    }

    /**
     * 新建行：输入框 + [新建]；{@code precheck} 通过才创建，新建成功后清空输入并执行 {@code afterCreate}（加入 / 选中）。
     */
    private static UIElement createRow(WirelessUIContext ctx, Supplier<WirelessStatus> precheck,
                                       Function<WirelessNetwork, WirelessStatus> afterCreate) {
        var field = new TextField(0, () -> ctx.pendingName, text -> ctx.pendingName = text);
        field.layout(l -> l.flex(1));
        field.setPlaceholder(() -> Component.translatable(NAME_PLACEHOLDER));
        field.getInput().setMaxStringLength(WirelessNetworks.MAX_NAME_LENGTH);
        var create = Button.translatable(UISizes.BUTTON_WIDTH, CREATE).setVariant(UITheme.ButtonVariant.CONFIRM)
                .setOnServerClick(() -> {
                    var allowed = precheck.get();
                    if (!allowed.ok()) {
                        ctx.report(allowed);
                        return;
                    }
                    var created = ctx.networks().create(ctx.serverPlayer(), ctx.pendingName);
                    if (created.network() == null) {
                        ctx.report(created.status());
                        return;
                    }
                    ctx.pendingName = "";
                    ctx.report(afterCreate.apply(created.network()));
                });
        return UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter())
                .addChildren(field, create);
    }

    // ==================== 详情 ====================

    private static Popup detailPopup(WirelessMachine machine, WirelessUIContext ctx) {
        return Popup.of(() -> ctx.remote ? Component.empty() : detailTitle(ctx, machine),
                column -> buildDetail(column, machine, ctx, "wireless.members", LIST_MAX_ROWS, () -> {}, WirelessMachineUI::closeDetail));
    }

    /**
     * 网络详情：改名、成员列表（点击成员在聊天栏生成传送命令）、控制器冲突提示、删除（二次确认）。
     * 各区块被拉伸到所在容器的宽度，成员列表被拖宽时一起变宽。
     *
     * @param membersId   成员列表滚动区的固定 id
     * @param memberRows  成员列表最多显示的行数（高度跟随成员数，超出滚动）
     * @param onConfirm   两端：点"确认"后执行（原位切换模式下回到主页）
     * @param afterDelete 服务端：删除成功后对"确认"按钮做的事（弹出面板模式下关闭面板）
     */
    private static void buildDetail(UIElement column, WirelessMachine machine, WirelessUIContext ctx, String membersId, int memberRows,
                                    Runnable onConfirm, Consumer<Button> afterDelete) {
        // 改名
        var rename = UIElement.section();
        var currentName = rename.addSyncValue(SyncValue.of(() -> networkName(ctx, machine), SyncValue.COMPONENT, Component.empty()));
        var field = new TextField(0, () -> ctx.renameBuffer, text -> ctx.renameBuffer = text);
        field.layout(l -> l.flex(1));
        field.setPlaceholder(currentName::getValue);
        field.getInput().setMaxStringLength(WirelessNetworks.MAX_NAME_LENGTH);
        var apply = Button.translatable(UISizes.BUTTON_WIDTH, RENAME).setOnServerClick(() -> {
            var result = ctx.networks().rename(ctx.serverPlayer(), machine.getWirelessNetworkId(), ctx.renameBuffer);
            if (result.ok()) ctx.renameBuffer = "";
            ctx.report(result);
        });
        // 改名：输入框占位文字就是当前名称，按钮写明"重命名"，不再另加标题行
        // 未加入网络时整行只读（输入框和按钮一起），悬停说明原因
        rename.addChild(UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter())
                .disabled(() -> machine.getWirelessNetwork() == null, STATE_STANDALONE).addChildren(field, apply));
        column.addChild(rename);

        // 成员：每行 [机器图标] 两行小字（机器名 / 坐标与维度）
        var members = UIElement.section();
        var scroller = new ScrollerView(membersId, LIST_WIDTH, MEMBER_ROW_HEIGHT).adaptiveHeight(memberListHeight(memberRows));
        scroller.addScrollViewChild(new WirelessRows<>(ctx.remote, MemberKey.CODEC,
                () -> memberKeys(ctx, machine), () -> memberVersion(ctx, machine), MemberRow::new, null));
        members.addChildren(TextLine.of(LayoutStyle.AUTO, () -> {
            var hub = WirelessHub.get(machine.getWirelessNetworkId());
            return Component.translatable(MEMBERS, hub == null || !canView(ctx, machine) ? 0 : hub.memberCount());
        }).setColor(UITheme.PANEL_TEXT), scroller);
        column.addChild(members);

        // 删除：二次确认，确认状态在控件里（WirelessSwitch），不在机器上。
        // 控制器冲突提示放在 [删除网络] 左侧的空位里，没有冲突时不占一整行
        var delete = UIElement.section();
        var ask = Button.translatable(UISizes.BUTTON_WIDTH, DELETE).setVariant(UITheme.ButtonVariant.DANGER)
                .disabled(() -> machine.getWirelessNetwork() == null, STATE_STANDALONE);
        var confirm = Button.translatable(UISizes.BUTTON_WIDTH, CONFIRM).setVariant(UITheme.ButtonVariant.DANGER);
        confirm.setHoverTooltips(DELETE_WARNING);
        var cancel = Button.translatable(UISizes.BUTTON_WIDTH, CANCEL);
        var conflict = TextLine.of(0, () -> canView(ctx, machine) && isConflict(machine) ? Component.translatable(CONFLICT) : Component.empty())
                .setColor(UITheme.STATUS_OFFLINE);
        conflict.layout(l -> l.flex(1));
        var askRow = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChildren(conflict, ask);
        var question = TextLine.translatable(0, DELETE_CONFIRM).setColor(UITheme.PANEL_TEXT);
        question.layout(l -> l.flex(1));
        var confirmRow = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter()).addChildren(question, confirm, cancel);
        var steps = new WirelessSwitch(askRow, confirmRow);
        ask.setOnClick(click -> steps.select(1));
        cancel.setOnClick(click -> steps.select(0));
        confirm.setOnClick(click -> {
            steps.select(0);
            onConfirm.run();
            if (click.isRemote) return;
            var result = ctx.networks().delete(ctx.serverPlayer(), machine.getWirelessNetworkId());
            ctx.report(result);
            if (result.ok()) afterDelete.accept(confirm);
        });
        column.addChild(delete.addChild(steps));
    }

    /** {@code rows} 行成员（行高 {@link #MEMBER_ROW_HEIGHT}、行距 2）的列表视口高度。 */
    private static int memberListHeight(int rows) {
        return rows * MEMBER_ROW_HEIGHT + (rows - 1) * UISizes.GAP;
    }

    /** 服务端：关闭所在窗口的详情弹出面板（不在 MachineWindow 里时什么都不做）。 */
    private static void closeDetail(Widget from) {
        var window = MachineWindow.of(from);
        if (window != null) window.closePopup(DETAIL_POPUP);
    }

    private static boolean isConflict(WirelessMachine machine) {
        var hub = WirelessHub.get(machine.getWirelessNetworkId());
        return hub != null && hub.controllerState() == ControllerState.CONTROLLER_CONFLICT;
    }

    // ==================== 成员行 ====================

    /** 成员：所在维度、坐标、机器定义 id（客户端据此取机器名与图标）。 */
    record MemberKey(String dimension, BlockPos pos, String machineId) {

        static final SyncValue.Codec<MemberKey> CODEC = new SyncValue.Codec<>() {

            @Override
            public void write(FriendlyByteBuf buf, MemberKey value) {
                buf.writeUtf(value.dimension());
                buf.writeBlockPos(value.pos());
                buf.writeUtf(value.machineId());
            }

            @Override
            public MemberKey read(FriendlyByteBuf buf) {
                return new MemberKey(buf.readUtf(), buf.readBlockPos(), buf.readUtf());
            }
        };

        String posText() {
            return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        }

        Component text() {
            return Component.translatable(MEMBER_ROW, name(), posText(), dimension);
        }

        @Nullable
        private MachineDefinition definition() {
            var id = ResourceLocation.tryParse(machineId);
            return id == null ? null : GTRegistries.MACHINES.get(id);
        }

        /** 机器名；定义已不存在时显示 id。 */
        Component name() {
            var definition = definition();
            return definition == null ? Component.literal(machineId) : Component.translatable(definition.getDescriptionId());
        }

        ItemStack icon() {
            var definition = definition();
            return definition == null ? ItemStack.EMPTY : definition.asStack();
        }

        /** 第二行：坐标与维度。 */
        Component location() {
            return Component.literal(posText() + "  " + dimension);
        }
    }

    private static List<MemberKey> memberKeys(WirelessUIContext ctx, WirelessMachine machine) {
        var hub = WirelessHub.get(machine.getWirelessNetworkId());
        if (hub == null || !canView(ctx, machine)) return List.of();
        return hub.members().stream()
                .map(member -> {
                    var self = member.self();
                    var level = self.getLevel();
                    var dimension = level == null ? "" : level.dimension().location().toString();
                    return new MemberKey(dimension, self.getPos(), self.getDefinition().getId().toString());
                })
                .sorted(Comparator.comparing(MemberKey::dimension).thenComparing(MemberKey::pos))
                .toList();
    }

    /** 成员列表的版本：成员构成或查看权限变化时变化，列表只在这时重建。 */
    private static int memberVersion(WirelessUIContext ctx, WirelessMachine machine) {
        var hub = WirelessHub.get(machine.getWirelessNetworkId());
        if (hub == null) return 0;
        return 31 * hub.membershipStamp() + (canView(ctx, machine) ? 1 : 0);
    }

    /**
     * 成员行：左侧 16 的机器图标，右侧两行小字（机器名 / 坐标与维度），宽度由列表拉伸、文字截断时悬停看全文。
     * 只读，客户端点击在聊天栏输出一条可点的传送命令（不发包）。
     */
    private static final class MemberRow extends UIElement {

        private static final int ICON = 16;

        private final MemberKey key;

        private MemberRow(MemberKey key) {
            this.key = key;
            layout(l -> l.row().height(MEMBER_ROW_HEIGHT).gapAll(UISizes.GAP).alignCenter());
            var tooltips = new Component[] { key.name(), key.location(), Component.translatable(MEMBER_HINT) };
            var icon = ItemView.of(key.icon(), ICON);
            icon.setHoverTooltips(tooltips);
            var name = TextLine.constant(LayoutStyle.AUTO, key.name()).setSmall().setColor(UITheme.PANEL_TEXT);
            name.setHoverTooltips(tooltips);
            var location = TextLine.constant(LayoutStyle.AUTO, key.location()).setSmall().setColor(UITheme.TEXT_SECONDARY);
            location.setHoverTooltips(tooltips);
            var lines = new UIElement().layout(l -> l.column().flex(1).gapAll(2)).addChildren(name, location);
            addChildren(icon, lines);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !isMouseOverElement(mouseX, mouseY)) return super.mouseClicked(mouseX, mouseY, button);
            var player = Minecraft.getInstance().player;
            if (player == null) return false;
            var command = "/execute in " + key.dimension() + " run tp @s " + key.pos().getX() + " " + key.pos().getY() + " " + key.pos().getZ();
            player.displayClientMessage(Component.translatable(MEMBER_TP, key.posText()).withStyle(style -> style
                    .withColor(ChatFormatting.AQUA).withUnderlined(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))), false);
            playButtonClickSound();
            return true;
        }
    }

    // ==================== 服务端文字 ====================

    /**
     * 服务端：打开界面的玩家能否查看本机所在网络的信息（名称、所有者、成员）：能使用该网络，或能管理这台机器。
     * 没有网络（未加入 / 已删除）时没有可泄露的信息，视为可查看。
     */
    private static boolean canView(WirelessUIContext ctx, WirelessMachine machine) {
        var network = machine.getWirelessNetwork();
        return network == null || network.canUse(ctx.uuid()) || WirelessPermissions.canManage(ctx.serverPlayer(), machine.self());
    }

    /** 网络名；未加入时"独立运行"，数据不可用、网络已不存在、无权查看时显示对应说明。 */
    static Component networkName(WirelessUIContext ctx, WirelessMachine machine) {
        var id = machine.getWirelessNetworkId();
        if (id.isEmpty()) return Component.translatable(STANDALONE);
        if (!machine.isWirelessAvailable()) return Component.translatable(WirelessMachine.KEY_STATE_UNAVAILABLE);
        var network = machine.getWirelessNetwork();
        if (network == null) return WirelessStatus.NOT_FOUND.message();
        return canView(ctx, machine) ? Component.literal(network.name()) : Component.translatable(WirelessMachine.KEY_UNKNOWN_NETWORK);
    }

    private static Component detailTitle(WirelessUIContext ctx, WirelessMachine machine) {
        return Component.translatable(DETAIL_TITLE, networkName(ctx, machine));
    }

    // ==================== 状态面板 ====================

    /**
     * 当前网络的状态面板：状态（最近操作结果 &gt; 机器提示 &gt; 连接状态）、网络名（{@code withNetwork}，窗口模式网络名已在标题栏）、所有者、成员数。
     * 所有者与成员只下发给能查看该网络的玩家（{@link #canView}），否则显示"—"。
     */
    private static StatusPanel statusPanel(WirelessMachine machine, WirelessUIContext ctx, boolean withNetwork) {
        var panel = new StatusPanel();
        // 最近 3 秒内的操作结果优先显示在状态行（成功绿灯、失败红灯），之后回到连接状态；悬停看完整原因
        panel.addLine(LINE_STATE, () -> ctx.stateText(() -> stateText(ctx, machine)))
                .level(() -> ctx.stateLevel(() -> stateLevel(ctx, machine)))
                .detail(() -> stateDetail(ctx, machine));
        if (withNetwork) panel.addLine(LINE_NETWORK, () -> networkName(ctx, machine));
        panel.addLine(LINE_OWNER, () -> ownerValue(ctx, viewableNetwork(ctx, machine)));
        panel.addLine(LINE_MEMBERS, () -> memberValue(viewableNetwork(ctx, machine)));
        return panel;
    }

    /** 服务端：本机所在、且打开界面的玩家可查看的网络；没有时为 null。 */
    @Nullable
    private static WirelessNetwork viewableNetwork(WirelessUIContext ctx, WirelessMachine machine) {
        if (!machine.isWirelessAvailable()) return null;
        var network = machine.getWirelessNetwork();
        return network != null && canView(ctx, machine) ? network : null;
    }

    /** 状态面板"所有者"的值；{@code network} 为 null 时为"—"。 */
    static Component ownerValue(WirelessUIContext ctx, @Nullable WirelessNetwork network) {
        return network == null ? Component.literal(NO_VALUE) : Component.literal(ctx.ownerName(network.owner()));
    }

    /** 状态面板"成员"的值（已连上的成员数）；{@code network} 为 null 时为"—"。 */
    static Component memberValue(@Nullable WirelessNetwork network) {
        if (network == null) return Component.literal(NO_VALUE);
        var hub = WirelessHub.get(network.id());
        return Component.literal(String.valueOf(hub == null ? 0 : hub.memberCount()));
    }

    /** 状态行文字（短句）：机器提示优先，否则连接状态。 */
    private static Component stateText(WirelessUIContext ctx, WirelessMachine machine) {
        var hint = machineHint(ctx, machine);
        if (hint != null) return hint;
        return Component.translatable(switch (machine.getWirelessLinkState()) {
            case STANDALONE -> STATE_STANDALONE;
            case ONLINE -> STATE_ONLINE;
            case OFFLINE -> STATE_OFFLINE;
            case NO_PERMISSION -> STATE_NO_PERMISSION;
            case UNAVAILABLE -> STATE_UNAVAILABLE;
        });
    }

    /** 状态行等级：控制器冲突为错误，未绑定所有者为注意；否则按连接状态（在线正常、离线错误、无权 / 不可用注意、未加入为普通）。 */
    private static StatusLine.Level stateLevel(WirelessUIContext ctx, WirelessMachine machine) {
        if (machine.self().getOwnerUUID() == null) return StatusLine.Level.WARNING;
        if (canView(ctx, machine) && isConflict(machine)) return StatusLine.Level.ERROR;
        return switch (machine.getWirelessLinkState()) {
            case STANDALONE -> StatusLine.Level.NORMAL;
            case ONLINE -> StatusLine.Level.GOOD;
            case OFFLINE -> StatusLine.Level.ERROR;
            case NO_PERMISSION, UNAVAILABLE -> StatusLine.Level.WARNING;
        };
    }

    /** 状态行悬停说明（完整句子）：未绑定所有者 / 控制器冲突的完整原因，否则连接状态的完整描述。 */
    private static Component stateDetail(WirelessUIContext ctx, WirelessMachine machine) {
        if (machine.self().getOwnerUUID() == null) return Component.translatable(NO_OWNER);
        if (canView(ctx, machine) && isConflict(machine)) return Component.translatable(CONFLICT);
        return machine.getWirelessLinkState().describe();
    }

    /** 机器级提示（短句）：无主人 / 控制器冲突（无权查看网络时不给网络相关提示）；没有时为 null。主人无权使用网络由连接状态给出。 */
    @Nullable
    private static Component machineHint(WirelessUIContext ctx, WirelessMachine machine) {
        if (machine.self().getOwnerUUID() == null) return Component.translatable(STATE_NO_OWNER);
        if (canView(ctx, machine) && isConflict(machine)) return Component.translatable(STATE_CONFLICT);
        return null;
    }
}

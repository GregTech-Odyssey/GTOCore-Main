package com.gtocore.integration.ae.wireless;

import com.gtocore.common.item.MEWirelessMachineConfigurator;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

/**
 * ME 无线机器配置器的手持界面（放在 {@link com.gregtechceu.gtceu.uipro.window.MachineWindow} 里，单页、无背包）：
 * 目标网络、可用网络列表（[选择] 写入手中物品）、新建网络、状态行。
 * 列表、新建行与机器页共用 {@link WirelessMachineUI} 的片段。
 */
@DataGeneratorScanned
public final class WirelessConfiguratorUI {

    @RegisterLanguage(cn = "目标网络", en = "Target Network")
    static final String TARGET = "gtocore.wireless.configurator.target";
    @RegisterLanguage(cn = "未选择", en = "None selected")
    static final String NONE = "gtocore.wireless.configurator.none";
    @RegisterLanguage(cn = "选择", en = "Select")
    static final String SELECT = "gtocore.wireless.configurator.select";
    @RegisterLanguage(cn = "取消选择", en = "Clear")
    static final String CLEAR = "gtocore.wireless.configurator.clear";
    @RegisterLanguage(cn = "右键无线机器：加入目标网络", en = "Right-click a machine: join target")
    static final String HINT = "gtocore.wireless.configurator.hint";
    @RegisterLanguage(cn = "潜行右键：读取机器的网络", en = "Sneak-right-click: copy its network")
    static final String HINT_SNEAK = "gtocore.wireless.configurator.hint_sneak";
    @RegisterLanguage(cn = "配置器还没有选择目标网络：手持右键空气打开界面选择", en = "No target network selected: right-click the air to choose one")
    public static final String NO_TARGET = "gtocore.wireless.configurator.no_target";

    private WirelessConfiguratorUI() {}

    public static IFancyUIProvider provider(Player player, InteractionHand hand) {
        return new IFancyUIProvider() {

            @Override
            public Widget createMainPage(FancyMachineUIWidget widget) {
                // 只有一页：隐藏左侧标签列（主标签仍要设置，否则标签列绘制时取不到图标）
                widget.getSideTabsWidget().setVisible(false);
                widget.getSideTabsWidget().setActive(false);
                return createPage(player, hand);
            }

            @Override
            public void attachSideTabs(TabsWidget sideTabs) {
                sideTabs.setMainTab(this);
            }

            @Override
            public IGuiTexture getTabIcon() {
                return new ItemStackTexture(player.getItemInHand(hand).copyWithCount(1));
            }

            @Override
            public Component getTitle() {
                return player.getItemInHand(hand).getHoverName();
            }

            @Override
            public boolean hasPlayerInventory() {
                return false;
            }
        };
    }

    private static Widget createPage(Player player, InteractionHand hand) {
        var ctx = new WirelessUIContext(player, () -> player.tickCount);
        // 至少标准内容宽，其余由布局决定：各区块拉伸到同宽，列表被拖宽时整页一起变宽
        var root = new UIElement().layout(l -> l.column().minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));

        // 目标网络：状态面板（目标网络 / 所有者 / 成员）
        var target = new StatusPanel();
        // 最近 3 秒内的操作结果优先显示在第一行
        target.addLine(TARGET, () -> ctx.stateText(() -> selectedLine(ctx, hand))).level(() -> ctx.stateLevel(() -> selectedLevel(ctx, hand)));
        target.addLine(WirelessMachineUI.LINE_OWNER, () -> WirelessMachineUI.ownerValue(ctx, selected(ctx, hand)));
        target.addLine(WirelessMachineUI.LINE_MEMBERS, () -> WirelessMachineUI.memberValue(selected(ctx, hand)));
        root.addChild(target);

        // 列表以外的高度：状态面板 3 行 + 网络区块固定部分 + 底部两行说明，各隔一个区块间距
        int otherHeight = StatusPanel.heightFor(3) + UISizes.SECTION_GAP + WirelessMachineUI.NETWORK_SECTION_FIXED +
                UISizes.SECTION_GAP + 2 * TextLine.HEIGHT + UISizes.GAP;
        // 配置器只从已有网络里选，不能新建（新建只在 ME 无线连接机里）
        root.addChild(WirelessMachineUI.networkSection(ctx, "wireless.configurator.networks", WirelessMachineUI.maxRows(ctx, otherHeight, UISizes.CONTROL_HEIGHT), SELECT, CLEAR,
                id -> id.equals(selectedId(ctx, hand)),
                id -> select(ctx, hand, id),
                from -> clear(ctx, hand),
                null, null));
        root.addChild(UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                TextLine.translatable(LayoutStyle.AUTO, HINT).setColor(UITheme.TEXT_SECONDARY),
                TextLine.translatable(LayoutStyle.AUTO, HINT_SNEAK).setColor(UITheme.TEXT_SECONDARY)));
        return root;
    }

    private static String selectedId(WirelessUIContext ctx, InteractionHand hand) {
        return MEWirelessMachineConfigurator.getNetworkId(ctx.player.getItemInHand(hand));
    }

    /** 服务端：选中的、且本玩家可用的网络。 */
    @Nullable
    private static WirelessNetwork selected(WirelessUIContext ctx, InteractionHand hand) {
        var network = ctx.networks().get(selectedId(ctx, hand));
        return network != null && network.canUse(ctx.uuid()) ? network : null;
    }

    /** 目标网络一行的值：未选择 / 网络不存在 / 网络名。 */
    private static Component selectedLine(WirelessUIContext ctx, InteractionHand hand) {
        if (selectedId(ctx, hand).isEmpty()) return Component.translatable(NONE);
        var network = selected(ctx, hand);
        return network == null ? WirelessStatus.NOT_FOUND.message() : Component.literal(network.name());
    }

    /** 目标网络一行的图标：未选择为说明，网络不存在为错误，已选中为正常。 */
    private static StatusLine.Level selectedLevel(WirelessUIContext ctx, InteractionHand hand) {
        if (selectedId(ctx, hand).isEmpty()) return StatusLine.Level.NORMAL;
        return selected(ctx, hand) == null ? StatusLine.Level.ERROR : StatusLine.Level.GOOD;
    }

    /** 服务端：把网络写入手中的配置器。 */
    private static WirelessStatus select(WirelessUIContext ctx, InteractionHand hand, String id) {
        if (!ctx.networks().isAvailable()) return WirelessStatus.UNAVAILABLE;
        var network = ctx.networks().get(id);
        if (network == null) return WirelessStatus.NOT_FOUND;
        if (!network.canUse(ctx.uuid())) return WirelessStatus.NO_PERMISSION_NETWORK;
        var stack = ctx.player.getItemInHand(hand);
        if (!MEWirelessMachineConfigurator.isConfigurator(stack)) return WirelessStatus.NOT_ALLOWED;
        MEWirelessMachineConfigurator.setNetworkId(stack, id);
        return WirelessStatus.OK;
    }

    /** 服务端：清除手中配置器的目标网络。 */
    private static WirelessStatus clear(WirelessUIContext ctx, InteractionHand hand) {
        var stack = ctx.player.getItemInHand(hand);
        if (!MEWirelessMachineConfigurator.isConfigurator(stack)) return WirelessStatus.NOT_ALLOWED;
        MEWirelessMachineConfigurator.setNetworkId(stack, "");
        return WirelessStatus.OK;
    }
}

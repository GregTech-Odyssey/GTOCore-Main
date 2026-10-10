package com.gtocore.common.wireless.energy.map;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.ServerList;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.Popup;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;

/**
 * 星球卡片内容（两端都建）：状态、储能、流量、线路与设备列表，数值均为服务端求值的同步值；不储能、无设备时整块收成一行。
 */
public final class GridNodeCard {

    private static final Component TOP_PORTS = Component.translatable(GridMapLang.TOP_PORTS);
    private static final Component NO_PORTS = Component.translatable(GridMapLang.NO_PORTS);
    private static final int NOTE_WIDTH = UISizes.POPUP_CONTENT_WIDTH - 4 * UISizes.PANEL_PADDING;

    private GridNodeCard() {}

    static Popup popup(GridCardData data) {
        return Popup.of(data::title, column -> build(column, data));
    }

    static void build(UIElement column, GridCardData data) {
        column.addChildren(header(data), storage(data), flow(data), lines(data), ports(data));
    }

    private static UIElement header(GridCardData data) {
        var panel = new StatusPanel(LayoutStyle.AUTO);
        panel.addSentence(data::state).bindLevel(data::stateLevel);
        panel.addLine(GridMapLang.LOCATION, data::location);
        return panel;
    }

    private static UIElement storage(GridCardData data) {
        var bar = ProgressBar.of(LayoutStyle.AUTO, Component.translatable(GridMapLang.STORED), 0, data::progress).percent();
        bar.bindClientColor(() -> GridSummaryPanel.barColor(bar));
        bar.bindDetail(data::storedDetail);
        var status = new StatusPanel(LayoutStyle.AUTO);
        status.addLine(GridMapLang.TIER, data::tier);
        status.addLine(GridMapLang.TOWERS, data::towers);
        status.addLine(GridMapLang.NODE_LOSS, data::loss);
        status.addLine(GridMapLang.PORTS, data::ports);
        status.addLine(GridMapLang.DELTA, data::delta);
        status.addLine(GridMapLang.ETA, data::eta);
        var block = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(bar, status);
        var note = new StatusPanel(LayoutStyle.AUTO);
        note.addChild(Label.of(NOTE_WIDTH, data::storageNote).bindClientColor(UITheme::panelText));
        var section = UIElement.section().addChildren(block, note);
        section.addSyncValue(SyncValue.ofBool(data::hasStorage, false).onChanged(has -> {
            block.setDisplay(has);
            note.setDisplay(!has);
        }));
        block.setDisplay(false);
        return section;
    }

    private static UIElement flow(GridCardData data) {
        var title = TextLine.translatable(0, GridMapLang.FLOW).bindClientColor(UITheme::panelText);
        var titleRow = UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(title.layout(l -> l.flex(1)), InfoIcon.info(GridMapLang.FLOW_INFO));
        var ctx = data.ctx;
        var status = new StatusPanel(LayoutStyle.AUTO);
        status.addLine(GridMapLang.NODE_INPUT, data::input);
        status.addLine(GridMapLang.NODE_OUTPUT, data::output);
        status.addLine(GridMapLang.LOSS, data::lossRate);
        return UIElement.section().addChildren(titleRow, GridSummaryPanel.windowGroup(ctx::getWindow, ctx::setWindow), status);
    }

    private static UIElement lines(GridCardData data) {
        var title = TextLine.of(LayoutStyle.AUTO, data::lineTitle).bindClientColor(UITheme::panelText);
        var relay = new StatusPanel(LayoutStyle.AUTO);
        relay.addLine(GridMapLang.RELAY_IN, data::relayIn);
        relay.addLine(GridMapLang.RELAY_OUT, data::relayOut);
        var list = ServerList.of(ByteBufCodecs.VAR_INT, data::lineKeys, line -> new GridLineRow(data, line))
                .version(data::lineVersion).rowHeight(GridLineRow.HEIGHT).maxRows(GridCardLines.MAX_ROWS).emptyText(GridMapLang.LINES_EMPTY);
        return UIElement.section().addChildren(title, relay, list);
    }

    private static UIElement ports(GridCardData data) {
        var title = TextLine.of(LayoutStyle.AUTO, () -> data.hasPorts() ? TOP_PORTS : NO_PORTS).bindClientColor(UITheme::panelText);
        var list = ServerList.of(ByteBufCodecs.VAR_INT, () -> data.detail().portKeys(), key -> new GridPortRow(data, key))
                .version(() -> data.detail().revision());
        var section = UIElement.section().addChildren(title, list);
        section.addSyncValue(SyncValue.ofBool(data::hasPorts, false).onChanged(list::setDisplay));
        list.setDisplay(false);
        return section;
    }
}

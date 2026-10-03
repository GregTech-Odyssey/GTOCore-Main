package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.NodeDetail;
import com.gtocore.client.forge.ForgeClientEvent;

import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

final class GridPortRow extends UIElement {

    private static final int RATE_WIDTH = 60;
    private static final int HIGHLIGHT_SECONDS = 10;

    private final GridCardData data;
    private final int key;
    private final SyncValue<Long> pos;
    @Nullable
    private NodeDetail shown;
    private Component name = GridFormat.NO_VALUE, rate = GridFormat.NO_VALUE;
    private Level rateLevel = Level.NORMAL;
    private Long position = 0L;

    GridPortRow(GridCardData data, int key) {
        this.data = data;
        this.key = key;
        layout(l -> l.row().height(UISizes.CONTROL_HEIGHT).gapAll(UISizes.GAP).alignCenter());
        pos = addSyncValue(SyncValue.ofLong(this::position, 0L));
        var nameText = TextLine.of(0, this::portName).bindClientColor(UITheme::panelText);
        var rateText = TextLine.of(RATE_WIDTH, this::rate).setTextAlign(Horizontal.RIGHT).bindLevel(this::rateLevel);
        var highlight = Button.icon(WidgetIcons.HIGHLIGHT).setOnClientClick(() -> {
            if (isRemote()) highlight(pos.getValue());
        });
        highlight.tooltips(GridMapLang.PORT_HIGHLIGHT);
        addChildren(nameText.layout(l -> l.flex(1)), rateText, highlight);
        disabled(() -> !data.isPortHere(key), GridMapLang.NOT_HERE);
    }

    private void refresh() {
        var detail = data.detail();
        if (detail == shown) return;
        shown = detail;
        var port = detail.port(key);
        if (port == null) {
            name = rate = GridFormat.NO_VALUE;
            rateLevel = Level.NORMAL;
            position = 0L;
            return;
        }
        name = port.name();
        rate = GridFormat.signedRate(port.rate());
        rateLevel = port.rate() > 0 ? Level.GOOD : port.rate() < 0 ? Level.WARNING : Level.NORMAL;
        position = port.pos().asLong();
    }

    private Component portName() {
        refresh();
        return name;
    }

    private Component rate() {
        refresh();
        return rate;
    }

    private Level rateLevel() {
        refresh();
        return rateLevel;
    }

    private Long position() {
        refresh();
        return position;
    }

    @OnlyIn(Dist.CLIENT)
    private static void highlight(long position) {
        ForgeClientEvent.highlightBlock(BlockPos.of(position), HIGHLIGHT_SECONDS);
        var player = Minecraft.getInstance().player;
        if (player != null) player.closeContainer();
    }
}

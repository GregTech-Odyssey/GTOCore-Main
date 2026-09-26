package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.RichText;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.item.HeldItemPage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.hepdd.gtmthings.common.block.machine.electric.WirelessEnergyMonitor.DISPLAY_TEXT_WIDTH;

public class WirelessEnergyTerminalBehavior implements IItemUIFactory {

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        var monitor = new WirelessMonitor(entityPlayer.getUUID(), entityPlayer.level());
        return new HeldItemPage(holder, window -> {
            var text = new RichText().justify(".");
            text.textSupplier(monitor.isRemote() ? null : monitor::addDisplayText);
            text.clickHandler(monitor::handleClick);
            var scroller = new ScrollerView("wireless_energy_terminal.display", DISPLAY_TEXT_WIDTH + 2 * UITheme.PANEL_PADDING + ScrollerView.SCROLL_BAR_SPACE, UISizes.MACHINE_PAGE_HEIGHT);
            scroller.setBackground(UITheme.STATUS_PANEL);
            scroller.layoutContent(l -> l.paddingAll(UITheme.PANEL_PADDING));
            scroller.addScrollViewChild(text);
            scroller.adaptiveHeight(window.isRemote() ? MachineWindow.clientPageHeightLimit(false) : Integer.MAX_VALUE / 4);
            return UIElement.column(LayoutStyle.AUTO).addChild(scroller);
        }).noInventory().noScroll().createUI(entityPlayer);
    }

    private static class WirelessMonitor implements IWirelessMonitor {

        private WirelessMonitor(UUID uuid, Level level) {
            this.uuid = uuid;
            this.level = level;
        }

        private boolean isRemote() {
            return level == null ? GTCEu.isClientThread() : level.isClientSide;
        }

        private final UUID uuid;
        private final Level level;
        private boolean all;
        private int powerDisplayMode;

        private List<Component> displayTextCache;

        @Getter
        @Setter
        private WirelessEnergyContainer WirelessEnergyContainerCache;

        private void addDisplayText(List<Component> textList) {
            if (displayTextCache == null || level.getServer().getTickCount() % 10 == 0) {
                displayTextCache = getDisplayText(all, powerDisplayMode, DISPLAY_TEXT_WIDTH);
            }
            textList.addAll(displayTextCache);
        }

        private void handleClick(String data, ClickData clickData) {
            if (clickData.isRemote) return;
            if (data.equals("all")) all = !all;
            else if (data.equals("power_mode")) powerDisplayMode = (powerDisplayMode + 1) % 3;
            else return;
            displayTextCache = null;
        }

        @Override
        public @Nullable UUID getUUID() {
            return uuid;
        }

        @Override
        public boolean display() {
            return false;
        }

        @Override
        public Level getLevel() {
            return level;
        }
    }
}

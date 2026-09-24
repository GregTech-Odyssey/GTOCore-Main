package com.gtocore.common.item;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.elements.Label;
import com.gtocore.api.gui.ui.elements.ScrollerView;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.translation.OrganTranslation;
import com.gtocore.common.item.misc.OrganItemBase;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.utils.OrganUtilsKt;

import com.gtolib.api.player.IEnhancedPlayer;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 器官修改器：按器官类型分组显示玩家装配的器官槽，放入/取出后立即写回玩家数据并刷新器官等级缓存。
 */
public class OrganModifierBehaviour implements IItemUIFactory {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        return new ModularUI(WIDTH, HEIGHT, holder, player).widget(new FancyMachineUIWidget(new Page(player), WIDTH, HEIGHT));
    }

    /** 每次打开界面单独一份，绑定打开界面的玩家（行为对象是全物品共享的单例）。 */
    private record Page(Player player) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var handlers = new HandlerContainer(player);
            var scroller = new ScrollerView(WIDTH, HEIGHT, 8);
            scroller.addScrollViewChild(UIElement.spacer(0, 0));
            int columnWidth = scroller.getContentWidth() - 8;
            handlers.handlers.forEach((organType, handler) -> {
                var slots = UIElement.row(18);
                for (int index = 0; index < handler.getSlots(); index++) {
                    var slot = new SlotWidget(handler, index, 0, 0, true, true);
                    slot.setChangeListener(() -> {
                        handlers.save();
                        handlers.read();
                        OrganUtilsKt.ktFreshOrganState(IEnhancedPlayer.of(player).getPlayerData());
                    });
                    slots.addChild(slot);
                }
                var column = UIElement.column(columnWidth).addChildren(
                        Label.translatable(organType.getTranslationKey(), columnWidth), slots);
                scroller.addScrollViewChild(UIElement.row(50).addChildren(UIElement.spacer(8, 0), column));
            });
            return new UIElement().layout(l -> l.size(WIDTH, HEIGHT)).addChild(scroller);
        }

        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(this);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(GTOOrganItems.INSTANCE.getORGAN_MODIFIER().get());
        }

        @Override
        public Component getTitle() {
            return OrganTranslation.INSTANCE.getOrganModifierName().get();
        }
    }

    /** 每种器官一个物品槽容器，与玩家数据里的器官列表互相转换。 */
    private static final class HandlerContainer {

        private final Player player;
        private final List<ItemStack> organItemStacks;
        private final Map<OrganType, CustomItemStackHandler> handlers = new EnumMap<>(OrganType.class);

        private HandlerContainer(Player player) {
            this.player = player;
            this.organItemStacks = IEnhancedPlayer.of(player).getPlayerData().organItemStacks;
            for (var organType : OrganType.values()) {
                var handler = new CustomItemStackHandler(organType.getSlotCount());
                handler.setFilter(stack -> stack.getItem() instanceof OrganItemBase organ && organ.getOrganType() == organType);
                handlers.put(organType, handler);
            }
            read();
        }

        private void read() {
            var organStacks = OrganUtilsKt.ktGetOrganStack(IEnhancedPlayer.of(player).getPlayerData());
            handlers.forEach((organType, handler) -> {
                handler.clear();
                var stacks = organStacks.get(organType);
                if (stacks == null) return;
                for (int i = 0; i < Math.min(stacks.size(), organType.getSlotCount()); i++) {
                    handler.setStackInSlot(i, stacks.get(i));
                }
            });
        }

        private void save() {
            organItemStacks.clear();
            for (var handler : handlers.values()) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    var stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) organItemStacks.add(stack);
                }
            }
        }
    }
}

package com.gtocore.common.item;

import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.translation.OrganTranslation;
import com.gtocore.common.item.misc.OrganItemBase;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.utils.OrganUtilsKt;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.player.IEnhancedPlayer;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 器官修改器：玩家装配的器官槽，放入/取出后立即写回玩家数据并刷新器官等级缓存。
 * 
 * <pre>
 * ┌ 翅膀 ─────────────────────┐   多格的器官：标题 + 每行 9 个槽
 * │ [][][][][][][][]           │
 * └────────────────────────────┘
 * ┌ 身体器官 ─────────────────┐   单格的器官合成一块：每行"名称 …… [槽]"
 * │ 眼睛 ……………………………… []  │
 * │ 脊椎 ……………………………… []  │
 * └────────────────────────────┘
 * </pre>
 * 
 * 列表放在滚动区里，高度跟随内容，整个窗口最高到屏幕的 {@link UISizes#MAX_WINDOW_SCREEN_RATIO}，再多才滚动。
 */
@DataGeneratorScanned
public class OrganModifierBehaviour implements IItemUIFactory {

    @RegisterLanguage(cn = "身体器官", en = "Body organs")
    private static final String BODY_ORGANS = "gtocore.organ_modifier.body_organs";

    /// ModularUI 的初始尺寸，打开后外壳按页面重算
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    /// 窗口除列表外的高度：外框、标题行、玩家背包（估算列表在屏幕上能占多高用）
    private static final int WINDOW_CHROME = UISizes.WINDOW_PADDING_TOP + UISizes.CONTROL_HEIGHT + UISizes.SECTION_GAP +
            UISizes.WINDOW_PADDING_BOTTOM + UISizes.PLAYER_INVENTORY_HEIGHT;
    /// 服务端不知道屏幕尺寸：用一个足够大的值（两端尺寸可以不同，控件树一致即可）
    private static final int SERVER_LIST_HEIGHT = 1000;

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        return new ModularUI(WIDTH, HEIGHT, holder, player).widget(new MachineWindow(new Page(player)));
    }

    /** 每次打开界面单独一份，绑定打开界面的玩家（行为对象是全物品共享的单例）。 */
    private record Page(Player player) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var handlers = new HandlerContainer(player);
            Runnable onChanged = () -> {
                handlers.save();
                handlers.read();
                OrganUtilsKt.ktFreshOrganState(IEnhancedPlayer.of(player).getPlayerData());
            };
            var scroller = new ScrollerView("organ.modifier", UISizes.CONTENT_WIDTH, UISizes.SLOT, UISizes.SECTION_GAP)
                    .adaptiveHeight(player.level().isClientSide ? clientListHeight() : SERVER_LIST_HEIGHT);
            var body = UIElement.section();
            body.addChild(TextLine.translatable(LayoutStyle.AUTO, BODY_ORGANS).setColor(UITheme.PANEL_TEXT));
            boolean hasSingle = false;
            for (var entry : handlers.handlers.entrySet()) {
                var organType = entry.getKey();
                if (organType.getSlotCount() > 1) {
                    scroller.addScrollViewChild(multiSlotSection(organType, entry.getValue(), onChanged));
                } else {
                    body.addChild(singleSlotRow(organType, entry.getValue(), onChanged));
                    hasSingle = true;
                }
            }
            if (hasSingle) scroller.addScrollViewChild(body);
            // 页面至少标准内容宽，尺寸跟随滚动区（拖拽右下角缩放时窗口一起变）
            return UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH)).addChild(scroller);
        }

        /** 多格的器官（如翅膀）：标题 + 每行 9 个槽。 */
        private static UIElement multiSlotSection(OrganType organType, CustomItemStackHandler handler, Runnable onChanged) {
            var section = UIElement.section();
            section.addChild(TextLine.translatable(LayoutStyle.AUTO, organType.getTranslationKey()).setColor(UITheme.PANEL_TEXT));
            for (int rowStart = 0; rowStart < handler.getSlots(); rowStart += UISizes.SLOTS_PER_ROW) {
                var row = UIElement.row(UISizes.SLOT);
                for (int index = rowStart; index < Math.min(handler.getSlots(), rowStart + UISizes.SLOTS_PER_ROW); index++) {
                    row.addChild(organSlot(organType, handler, index, onChanged));
                }
                section.addChild(row);
            }
            return section;
        }

        /** 单格的器官：一行"名称 …… [槽]"。 */
        private static UIElement singleSlotRow(OrganType organType, CustomItemStackHandler handler, Runnable onChanged) {
            var name = TextLine.translatable(0, organType.getTranslationKey()).setColor(UITheme.PANEL_TEXT);
            name.layout(l -> l.flex(1));
            return UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.GAP).alignCenter())
                    .addChildren(name, organSlot(organType, handler, 0, onChanged));
        }

        /** 器官槽：只收这种器官（容器的过滤），空槽悬停显示器官种类。 */
        private static ItemSlot organSlot(OrganType organType, CustomItemStackHandler handler, int index, Runnable onChanged) {
            var slot = ItemSlot.of(handler, index);
            slot.setChangeListener(onChanged);
            slot.setHoverTooltips(Component.translatable(organType.getTranslationKey()));
            return slot;
        }

        /** 客户端：窗口不超过屏幕高度上限时，列表视口最多多高。 */
        @OnlyIn(Dist.CLIENT)
        private static int clientListHeight() {
            int screen = Minecraft.getInstance().getWindow().getGuiScaledHeight();
            return Math.max(UISizes.SLOT, (int) (screen * UISizes.MAX_WINDOW_SCREEN_RATIO) - WINDOW_CHROME);
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

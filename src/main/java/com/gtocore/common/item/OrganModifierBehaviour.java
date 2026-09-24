package com.gtocore.common.item;

import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.item.misc.OrganTooltips;
import com.gtocore.common.item.misc.OrganType;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.OrganInventory;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 器官修改器：直接编辑玩家身上的器官库存（{@link OrganInventory}，槽位固定），放入 / 取出立即生效。
 *
 * <pre>
 * ┌ 状态面板 ─────────────────────┐
 * │ 套装等级 ……………… 标准级（1） ●│
 * │ 生命上限 ……………………… 完整 ●│
 * │ 可停留星球 ………… 2 级及以下  │
 * └───────────────────────────────┘
 * [眼][肺][心][肝][脊][左臂][右臂][左腿][右腿]   身体器官一行 9 格，与背包同一条左边缘
 *  眼睛 肺 心脏 …                                 每格下方小字标出部位
 * 翅膀 …………………………………… [ ][ ][ ][ ]           翅膀 4 格靠右
 * </pre>
 */
@DataGeneratorScanned
public class OrganModifierBehaviour implements IItemUIFactory, IAddInformation {

    @RegisterLanguage(cn = "套装等级", en = "Set tier")
    private static final String LABEL_SET_TIER = "gtocore.organ_modifier.set_tier";
    @RegisterLanguage(cn = "生命上限", en = "Max health")
    private static final String LABEL_HEALTH = "gtocore.organ_modifier.health";
    @RegisterLanguage(cn = "可停留星球", en = "Habitable planets")
    private static final String LABEL_PLANET = "gtocore.organ_modifier.planet";
    @RegisterLanguage(cn = "套装等级取九种身体器官中最低的等级，缺任何一件都不算整套", en = "The set tier is the lowest tier among the nine body organs; any missing organ breaks the set")
    private static final String TIP_SET_TIER = "gtocore.organ_modifier.set_tier.tip";
    @RegisterLanguage(cn = "每缺一件身体器官，生命上限按比例降低", en = "Each missing body organ lowers your max health")
    private static final String TIP_HEALTH = "gtocore.organ_modifier.health.tip";

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        // 初始尺寸随便填，外壳按页面重算
        return new ModularUI(UISizes.WINDOW_WIDTH, 166, holder, player).widget(new MachineWindow(new Page(player)));
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        OrganTooltips.addModifierTooltip(tooltipComponents);
    }

    /** 每次打开界面单独一份，绑定打开界面的玩家（行为对象是全物品共享的单例）。 */
    private record Page(Player player) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            OrganInventory organs = IEnhancedPlayer.of(player).getPlayerData().organs;

            var status = new StatusPanel(LayoutStyle.AUTO);
            status.addLine(LABEL_SET_TIER, () -> OrganTooltips.tierValue(organs.getSetTier()))
                    .level(() -> organs.getSetTier() >= 1 ? StatusLine.Level.GOOD : organs.getSetTier() == 0 ? StatusLine.Level.WARNING : StatusLine.Level.ERROR)
                    .tooltip(TIP_SET_TIER);
            status.addLine(LABEL_HEALTH, () -> OrganTooltips.healthValue(organs.getMissingBodyCount()))
                    .level(() -> organs.getMissingBodyCount() == 0 ? StatusLine.Level.GOOD : organs.getMissingBodyCount() < 3 ? StatusLine.Level.WARNING : StatusLine.Level.ERROR)
                    .tooltip(TIP_HEALTH);
            status.addLine(LABEL_PLANET, () -> OrganTooltips.planetValue(organs.getSetTier()));

            // 身体器官一行 9 格（正好内容宽），下方小字部位名逐格对齐
            var bodySlots = UIElement.row(UISizes.SLOT);
            var bodyLabels = UIElement.row(UISizes.SMALL_TEXT_HEIGHT);
            for (var type : OrganType.BODY) {
                bodySlots.addChild(organSlot(organs, type, 0));
                // 与槽同宽；放不下时截断，悬停看全名
                bodyLabels.addChild(TextLine.translatable(UISizes.SLOT, type.translationKey).setSmall());
            }
            var body = UIElement.column(LayoutStyle.AUTO).addChildren(bodySlots, bodyLabels);

            // 翅膀：名称 …… 4 格
            var wingName = TextLine.translatable(0, OrganType.WING.translationKey);
            wingName.layout(l -> l.flex(1));
            var wings = UIElement.row(UISizes.SLOT).layout(l -> l.alignCenter()).addChild(wingName);
            for (int i = 0; i < OrganType.WING.slotCount; i++) {
                wings.addChild(organSlot(organs, OrganType.WING, i));
            }

            return UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP))
                    .addChildren(status, body, wings);
        }

        /** 器官槽：直接绑定玩家器官库存的固定槽位，库存只收对应部位（{@link OrganInventory#isItemValid}）；空槽悬停显示部位。 */
        private static ItemSlot organSlot(OrganInventory organs, OrganType type, int index) {
            var slot = ItemSlot.of(organs, type.firstSlot() + index);
            slot.setHoverTooltips(Component.translatable(type.translationKey));
            return slot;
        }

        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(this);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(GTOOrganItems.ORGAN_MODIFIER.get());
        }

        @Override
        public Component getTitle() {
            return GTOOrganItems.ORGAN_MODIFIER.get().getDescription();
        }
    }
}

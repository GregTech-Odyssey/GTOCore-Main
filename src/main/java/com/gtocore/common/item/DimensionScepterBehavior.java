package com.gtocore.common.item;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.translation.GTOItemTooltips;
import com.gtocore.config.GTOConfig;
import com.gtocore.eio_travel.logic.TravelHandler;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.LV;

@DataGeneratorScanned
public class DimensionScepterBehavior extends ElectricStats implements IItemUIFactory {

    private static final long EU_COST = 128L;

    @RegisterLanguage(cn = "无法传送到该旅行锚", en = "Cannot teleport to this travel anchor")
    private static final String BOOKMARK_FAILED = "gtocore.dimension_scepter.bookmark.failed";
    @RegisterLanguage(cn = "电量不足", en = "Not enough energy")
    private static final String NO_ENERGY = "gtocore.dimension_scepter.no_energy";
    @RegisterLanguage(cn = "没有选中的收藏锚", en = "No saved anchor selected")
    private static final String NO_SELECTION = "gtocore.dimension_scepter.no_selection";
    @RegisterLanguage(cn = "选中旅行锚：%s", en = "Selected anchor: %s")
    private static final String SELECTED_BOOKMARK = "gtocore.dimension_scepter.selected_bookmark";

    private static final Component BOOKMARK_FAILED_C = Component.translatable(BOOKMARK_FAILED);
    private static final Component NO_ENERGY_C = Component.translatable(NO_ENERGY);
    private static final Component NO_SELECTION_C = Component.translatable(NO_SELECTION);

    protected DimensionScepterBehavior(long maxCharge, int tier, boolean chargeable, boolean dischargeable) {
        super(maxCharge, tier, chargeable, dischargeable);
    }

    public static DimensionScepterBehavior create() {
        var behavior = new DimensionScepterBehavior(
                0L,
                LV,
                true,
                false);
        behavior.tierFromBattery();
        return behavior;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item self, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (tryPerformAction(level, player, stack)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && tryPerformAction(context.getLevel(), context.getPlayer(), context.getItemInHand())) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        return InteractionResult.FAIL;
    }

    private boolean tryPerformAction(Level level, Player player, ItemStack stack) {
        if (player.isSpectator() || player.getCooldowns().isOnCooldown(stack.getItem())) {
            return false;
        }
        DimensionScepterBookmarks.Bookmark bookmark = DimensionScepterBookmarks.selected(stack);
        if (bookmark == null) {
            if (!level.isClientSide()) player.displayClientMessage(NO_SELECTION_C, true);
            return false;
        }
        boolean isCreative = player.isCreative();
        if (!isCreative && !hasResources(stack)) {
            if (!level.isClientSide()) player.displayClientMessage(NO_ENERGY_C, true);
            return false;
        }
        if (level.isClientSide()) {
            // 客户端本地先记一次冷却，右键不必等服务端下发就一直有效；服务端成功后会用同样的时长覆盖。
            addCooldown(player, stack);
            return true;
        }
        if (!TravelHandler.bookmarkTeleport(player, bookmark.dimension(), bookmark.pos())) {
            player.displayClientMessage(BOOKMARK_FAILED_C, true);
            return false;
        }
        if (!isCreative) consumeResources(stack);
        addCooldown(player, stack);
        return true;
    }

    /**
     * 从权杖界面直接传送到指定收藏锚，并扣除电量与设置冷却。
     */
    public static boolean teleportToBookmark(Player player, ItemStack stack, int index) {
        if (player.level().isClientSide() || player.isSpectator() || player.getCooldowns().isOnCooldown(stack.getItem())) {
            return false;
        }
        DimensionScepterBookmarks.Bookmark bookmark = DimensionScepterBookmarks.get(stack, index);
        if (bookmark == null) return false;
        if (!player.isCreative() && !hasResources(stack)) {
            player.displayClientMessage(NO_ENERGY_C, true);
            return false;
        }
        if (!TravelHandler.bookmarkTeleport(player, bookmark.dimension(), bookmark.pos())) {
            player.displayClientMessage(BOOKMARK_FAILED_C, true);
            return false;
        }
        if (!player.isCreative()) consumeResources(stack);
        addCooldown(player, stack);
        return true;
    }

    private static void addCooldown(Player player, ItemStack stack) {
        player.getCooldowns().addCooldown(stack.getItem(), GTOConfig.INSTANCE.travelConfig.travelStaffCooldown);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return MachineWindow.createUI(new Page(holder), holder, entityPlayer);
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                               TooltipFlag isAdvanced) {
        tooltipComponents.addAll(GTOItemTooltips.DimensionScepterTooltips.get());
        DimensionScepterBookmarks.Bookmark bookmark = DimensionScepterBookmarks.selected(stack);
        if (bookmark != null) {
            tooltipComponents.add(Tips.stat(Component.translatable(SELECTED_BOOKMARK, DimensionScepterUI.bookmarkName(level, bookmark))));
        }
        super.appendTooltips(stack, level, tooltipComponents, isAdvanced);
    }

    private static boolean hasResources(ItemStack stack) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;
        return electricItem.getCharge() >= EU_COST;
    }

    private static void consumeResources(ItemStack stack) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return;
        electricItem.discharge(
                EU_COST,
                electricItem.getTier(),
                true,
                false,
                false);
    }

    private record Page(HeldItemUIFactory.HeldItemHolder holder) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var view = new DimensionScepterUI.BookmarkView();
            var scroller = ScrollerView.page("dimension_scepter.page", UISizes.CONTENT_WIDTH).adaptiveWidth();
            scroller.addScrollViewChild(Form.page()
                    .addChild(DimensionScepterUI.bookmarksSection(holder, view))
                    .addChild(DimensionScepterUI.nearbySection(holder, view)));
            return UIElement.column(LayoutStyle.AUTO).addChild(scroller);
        }

        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(this);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(GTOItems.DIMENSION_SCEPTER.asItem());
        }

        @Override
        public Component getTitle() {
            return GTOItems.DIMENSION_SCEPTER.asItem().getDescription();
        }
    }
}

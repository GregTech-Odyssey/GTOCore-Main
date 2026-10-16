package com.gtocore.common.item;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.translation.GTOItemTooltips;
import com.gtocore.config.GTOConfig;
import com.gtocore.eio_travel.api.TravelTargetType;
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
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.LV;

@DataGeneratorScanned
public class TravelStaffBehavior extends ElectricStats implements IItemUIFactory {

    private static final long EU_COST = 128L;

    @RegisterLanguage(cn = "可传送目标", en = "Teleport Targets")
    private static final String TARGETS = "gtocore.travel_staff.targets";
    @RegisterLanguage(cn = "可传送目标：%s", en = "Teleport targets: %s")
    private static final String TARGETS_TOOLTIP = "gtocore.travel_staff.targets.tooltip";
    @RegisterLanguage(cn = "无", en = "None")
    private static final String TARGETS_NONE = "gtocore.travel_staff.targets.none";
    @RegisterLanguage(cn = "、", en = ", ")
    private static final String TARGETS_SEPARATOR = "gtocore.travel_staff.targets.separator";
    @RegisterLanguage(cn = "旅行锚", en = "Travel Anchors")
    private static final String ANCHOR = "gtocore.travel_staff.target.anchor";
    @RegisterLanguage(cn = "范围内的旅行锚", en = "Travel anchors in range")
    private static final String ANCHOR_TIP = "gtocore.travel_staff.target.anchor.tip";
    @RegisterLanguage(cn = "队友", en = "Teammates")
    private static final String TEAMMATE = "gtocore.travel_staff.target.teammate";
    @RegisterLanguage(cn = "同队玩家头顶显示头像，对准头像即可传送到其身边", en = "Players in your team show a head icon; aim at it to teleport next to them")
    private static final String TEAMMATE_TIP = "gtocore.travel_staff.target.teammate.tip";
    @RegisterLanguage(cn = "样板节点", en = "Pattern Nodes")
    private static final String PATTERN_NODE = "gtocore.travel_staff.target.pattern_node";
    @RegisterLanguage(cn = "AE 样板供应器与 ME 样板总成，数量多时会占满视野", en = "AE pattern providers and ME pattern buffers; many of them can crowd the view")
    private static final String PATTERN_NODE_TIP = "gtocore.travel_staff.target.pattern_node.tip";
    @RegisterLanguage(cn = "激活隐藏节点", en = "Include Hidden Nodes")
    private static final String SHOW_HIDDEN = "gtocore.travel_staff.show_hidden";
    @RegisterLanguage(cn = "方块设置里未开启旅行网络显示的旅行锚与样板节点也作为传送目标", en = "Also target travel anchors and pattern nodes that are hidden from the travel network in their own settings")
    private static final String SHOW_HIDDEN_TIP = "gtocore.travel_staff.show_hidden.tip";
    @RegisterLanguage(cn = "服务器配置已关闭样板节点传送", en = "Disabled in the server config")
    private static final String PATTERN_NODE_DISABLED = "gtocore.travel_staff.target.pattern_node.disabled";

    protected TravelStaffBehavior(long maxCharge, int tier, boolean chargeable, boolean dischargeable) {
        super(maxCharge, tier, chargeable, dischargeable);
    }

    public static TravelStaffBehavior create() {
        var behavior = new TravelStaffBehavior(
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
        if (getActivationStatus(stack).isAir()) {
            if (tryPerformAction(self, level, player, stack)) {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
            return InteractionResultHolder.fail(stack);
        }
        return super.use(self, level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (getActivationStatus(context.getItemInHand()).isBlock()) {
            if (context.getPlayer() != null && tryPerformAction(context.getItemInHand().getItem(), context.getLevel(), context.getPlayer(), context.getItemInHand())) {
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
            }

            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }

    private boolean tryPerformAction(Item self, Level level, Player player, ItemStack stack) {
        boolean isCreative = player.isCreative();
        if (hasResources(stack) || isCreative) {
            if (performAction(self, level, player, stack)) {
                if (!level.isClientSide() && !isCreative) {
                    consumeResources(stack);
                }

                return true;
            }

            return false;
        }

        return false;
    }

    /**
     * Perform your action
     * 
     * @return true if it was a success and you want to consume the resources
     */
    public boolean performAction(Item self, Level level, Player player, ItemStack stack) {
        if (player.isShiftKeyDown()) {
            if (TravelHandler.shortTeleport(level, player)) {
                player.getCooldowns().addCooldown(self, GTOConfig.INSTANCE.travelConfig.travelStaffCooldown);
                return true;
            }
        } else {
            if (TravelHandler.staffTeleport(level, player)) {
                player.getCooldowns().addCooldown(self, GTOConfig.INSTANCE.travelConfig.travelStaffCooldown);
                return true;
            }
        }
        return false;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return MachineWindow.createUI(new Page(holder), holder, entityPlayer);
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                               TooltipFlag isAdvanced) {
        tooltipComponents.addAll(GTOItemTooltips.TravelStaffTooltips.get());
        tooltipComponents.add(Tips.stat(Component.translatable(TARGETS_TOOLTIP, enabledTargets(TravelTargetType.getMask(stack)))));
        super.appendTooltips(stack, level, tooltipComponents, isAdvanced);
    }

    public boolean hasResources(ItemStack stack) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;
        return electricItem.getCharge() >= EU_COST;
    }

    public void consumeResources(ItemStack stack) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return;
        electricItem.discharge(
                EU_COST,
                LV,
                true,
                true,
                false);
    }

    private static Component enabledTargets(int mask) {
        MutableComponent names = null;
        for (var type : TravelTargetType.VALUES) {
            if (!type.isEnabled(mask)) continue;
            if (names == null) {
                names = Component.translatable(labelKey(type));
            } else {
                names.append(Component.translatable(TARGETS_SEPARATOR)).append(Component.translatable(labelKey(type)));
            }
        }
        return names == null ? Component.translatable(TARGETS_NONE) : names;
    }

    private static String labelKey(TravelTargetType type) {
        return switch (type) {
            case ANCHOR -> ANCHOR;
            case TEAMMATE -> TEAMMATE;
            case PATTERN_NODE -> PATTERN_NODE;
        };
    }

    private static String tipKey(TravelTargetType type) {
        return switch (type) {
            case ANCHOR -> ANCHOR_TIP;
            case TEAMMATE -> TEAMMATE_TIP;
            case PATTERN_NODE -> PATTERN_NODE_TIP;
        };
    }

    private record Page(HeldItemUIFactory.HeldItemHolder holder) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            var targets = Form.section(TARGETS);
            for (var type : TravelTargetType.VALUES) {
                var row = Form.controlRow(labelKey(type), Switch.of(() -> type.isEnabled(holder.held), on -> setEnabled(type, on)), tipKey(type));
                if (type == TravelTargetType.PATTERN_NODE) {
                    row.disabled(() -> !GTOConfig.INSTANCE.travelConfig.staffOfTravellingPatternNodes, PATTERN_NODE_DISABLED);
                }
                targets.addChild(row);
            }
            targets.addChild(Form.controlRow(SHOW_HIDDEN, Switch.of(() -> TravelTargetType.showsHidden(holder.held), this::setShowHidden), SHOW_HIDDEN_TIP));
            var scroller = ScrollerView.page("travel_staff.page", UISizes.CONTENT_WIDTH).adaptiveWidth();
            scroller.addScrollViewChild(Form.page().addChild(targets));
            return UIElement.column(LayoutStyle.AUTO).addChild(scroller);
        }

        private void setEnabled(TravelTargetType type, boolean enabled) {
            if (!holder.isInvalid() && holder.held.is(GTOItems.TRAVEL_STAFF.asItem())) {
                type.setEnabled(holder.held, enabled);
            }
        }

        private void setShowHidden(boolean show) {
            if (!holder.isInvalid() && holder.held.is(GTOItems.TRAVEL_STAFF.asItem())) {
                TravelTargetType.setShowHidden(holder.held, show);
            }
        }

        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(this);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(GTOItems.TRAVEL_STAFF.asItem());
        }

        @Override
        public Component getTitle() {
            return GTOItems.TRAVEL_STAFF.asItem().getDescription();
        }
    }

    protected ActivationStatus getActivationStatus(ItemStack stack) {
        return ActivationStatus.ALL;
    }

    @Getter
    protected enum ActivationStatus {

        BLOCK(true, false),
        AIR(false, true),
        ALL(true, true);

        private final boolean isBlock;
        private final boolean isAir;

        ActivationStatus(boolean isBlock, boolean isAir) {
            this.isBlock = isBlock;
            this.isAir = isAir;
        }
    }
}

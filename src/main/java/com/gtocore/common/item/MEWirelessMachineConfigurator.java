package com.gtocore.common.item;

import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.window.MachineWindow;
import com.gtocore.common.data.translation.GTOItemTooltips;
import com.gtocore.integration.ae.wireless.WirelessConfiguratorUI;
import com.gtocore.integration.ae.wireless.WirelessMachine;
import com.gtocore.integration.ae.wireless.WirelessNetworks;
import com.gtocore.integration.ae.wireless.WirelessPermissions;
import com.gtocore.integration.ae.wireless.WirelessStatus;
import com.gtocore.integration.ae.wireless.WirelessSync;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * ME 无线机器配置器：手持右键空气打开界面选择目标网络（存在物品 NBT {@code configuringNetworkId}）；
 * 右键无线机器让它加入目标网络，潜行右键把机器当前的网络读进配置器。所有操作以使用者校验权限，结果在 actionbar 提示。
 */
public enum MEWirelessMachineConfigurator implements IItemUIFactory, IAddInformation {

    INSTANCE;

    private static final String NETWORK_KEY = "configuringNetworkId";

    public static boolean isConfigurator(ItemStack stack) {
        return stack.getItem() instanceof ComponentItem item && item.getComponents().contains(INSTANCE);
    }

    /** 物品里记录的网络 id（不校验网络是否存在），没有时为空串。 */
    public static String getNetworkId(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null ? "" : tag.getString(NETWORK_KEY);
    }

    public static void setNetworkId(ItemStack stack, String networkId) {
        stack.getOrCreateTag().putString(NETWORK_KEY, networkId);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        if (entityPlayer instanceof ServerPlayer player) WirelessSync.pushTo(player);
        return new ModularUI(UISizes.WINDOW_WIDTH, UISizes.WINDOW_WIDTH, holder, entityPlayer)
                .widget(new MachineWindow(WirelessConfiguratorUI.provider(entityPlayer, holder.hand)));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        var level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity blockEntity) ||
                !(blockEntity.getMetaMachine() instanceof WirelessMachine machine)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            var target = getNetworkId(itemStack);
            Component message;
            if (serverPlayer.isShiftKeyDown()) message = copyFrom(serverPlayer, machine, itemStack).message();
            else if (target.isEmpty()) message = Component.translatable(WirelessConfiguratorUI.NO_TARGET);
            else message = machine.joinWireless(serverPlayer, target).message();
            serverPlayer.displayClientMessage(message, true);
        }
        return InteractionResult.SUCCESS;
    }

    /** 把机器当前的网络读进配置器：要能管理这台机器，也要能使用那个网络。 */
    private static WirelessStatus copyFrom(ServerPlayer player, WirelessMachine machine, ItemStack stack) {
        var networks = WirelessNetworks.get(player.server);
        if (!networks.isAvailable()) return WirelessStatus.UNAVAILABLE;
        if (!WirelessPermissions.canManage(player, machine.self())) return WirelessStatus.NO_PERMISSION_MACHINE;
        var network = networks.get(machine.getWirelessNetworkId());
        if (network == null) return WirelessStatus.NOT_FOUND;
        if (!network.canUse(player.getUUID())) return WirelessStatus.NO_PERMISSION_NETWORK;
        setNetworkId(stack, network.id());
        return WirelessStatus.OK;
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.addAll(GTOItemTooltips.MEWirelessMachineConfigurator.get());
    }
}

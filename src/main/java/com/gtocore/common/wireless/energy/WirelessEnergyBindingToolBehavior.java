package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.GridBinding;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class WirelessEnergyBindingToolBehavior implements IInteractionItem {

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        var player = context.getPlayer();
        if (context.getLevel().isClientSide() || player == null) return InteractionResult.PASS;
        var pos = context.getClickedPos();
        long rate = GridBinding.bindingRate(context.getLevel(), pos);
        if (rate <= 0) return InteractionResult.PASS;
        if (GridBinding.bind(player.getUUID(), GlobalPos.of(context.getLevel().dimension(), pos), rate)) {
            player.sendSystemMessage(Component.translatable("item.gtmthings.wireless_transfer.tooltip.bind.1", Component.translatable(context.getLevel().getBlockState(pos).getBlock().getDescriptionId()), pos.toShortString()));
        }
        return InteractionResult.CONSUME;
    }
}

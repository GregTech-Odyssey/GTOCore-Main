package com.gtocore.common.item.misc;

import com.gtolib.utils.StringUtils;

import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MysteriousBoostPotionBehaviour implements IInteractionItem {

    public MysteriousBoostPotionBehaviour() {}

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide()) {
            livingEntity.sendSystemMessage(Component.literal(StringUtils.full_color(Component.translatable("gtocore.player_exp_status.mysterious_boost_potion.success").getString())));
        }

        return stack;
    }
}

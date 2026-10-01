package com.gtocore.common.item.misc;

import com.gtolib.api.player.OrganTier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 分级的身体器官（0 萌芽级 ~ 4 原型级 (IV)）。
 */
public final class TierOrganItem extends OrganItemBase {

    private final OrganTier tier;

    public TierOrganItem(OrganTier tier, Properties properties, OrganType organType) {
        super(properties, organType);
        this.tier = tier;
    }

    public OrganTier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        OrganTooltips.addTierOrganTooltip(stack, this, tooltipComponents);
    }
}

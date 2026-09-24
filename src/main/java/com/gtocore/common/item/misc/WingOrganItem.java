package com.gtocore.common.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 翅膀：装上即可飞行。耐久型飞行时每秒消耗 1 点耐久（耐久即可飞行的秒数）；电动型飞行时每秒耗电。
 */
public final class WingOrganItem extends OrganItemBase {

    private final float maxFlySpeed;
    private final boolean electric;

    public WingOrganItem(Properties properties, float maxFlySpeed, boolean electric) {
        super(properties, OrganType.WING);
        this.maxFlySpeed = maxFlySpeed;
        this.electric = electric;
    }

    public float getMaxFlySpeed() {
        return maxFlySpeed;
    }

    public boolean isElectric() {
        return electric;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        OrganTooltips.addWingTooltip(stack, this, tooltipComponents);
    }
}

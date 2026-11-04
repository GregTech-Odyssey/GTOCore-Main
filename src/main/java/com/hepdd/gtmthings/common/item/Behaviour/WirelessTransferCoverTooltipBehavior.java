package com.hepdd.gtmthings.common.item.Behaviour;

import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class WirelessTransferCoverTooltipBehavior implements IAddInformation {

    private final Consumer<List<Component>> tooltips;

    /**
     * @param tooltips a consumer adding translated tooltips to the tooltip list
     */
    public WirelessTransferCoverTooltipBehavior(@NotNull Consumer<List<Component>> tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                               TooltipFlag isAdvanced) {
        var tag = stack.getTag();
        GlobalPos globalPos = WirelessTransferCoverPlaceBehavior.getStoredGlobalPos(tag);
        if (globalPos != null) {
            tooltipComponents.add(Tips.stat(Component.translatable("item.gtmthings.wireless_transfer.tooltip.1", Component.translatable(tag.getString(WirelessTransferCoverPlaceBehavior.TAG_BLOCK_ID)), globalPos.pos().toShortString())));
            tooltipComponents.add(Tips.stat(Component.translatable("recipe.condition.dimension.tooltip", Component.translatable(GTODimensions.getTranslationKey(globalPos.dimension())))));
        }
        tooltips.accept(tooltipComponents);
    }
}

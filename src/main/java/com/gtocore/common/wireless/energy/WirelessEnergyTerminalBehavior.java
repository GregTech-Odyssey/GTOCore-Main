package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.GridBody;
import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.common.wireless.energy.map.GridMapUIFactory;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

@DataGeneratorScanned
public final class WirelessEnergyTerminalBehavior implements IInteractionItem, IAddInformation {

    @RegisterLanguage(cn = "对空右键：打开电网星图，查看队伍电网的节点、线路与流量", en = "Right-click in the air: open the Grid Map to view the team grid's nodes, lines and flow")
    private static final String TOOLTIP_USE = "gtocore.wireless_energy.terminal.use";

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer) GridMapUIFactory.open(serverPlayer, GridView.dimRef(GridBody.of(level.dimension())));
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable(TOOLTIP_USE).withStyle(ChatFormatting.GRAY));
    }
}

package com.gtocore.common.machine.multiblock.part;

import com.gtolib.api.machine.trait.WirelessComputationContainerTrait;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableMultiblockPartMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.hepdd.gtmthings.api.capability.IBindable;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.GetName;

public final class WirelessNetworkComputationHatchMachine extends WorkableMultiblockPartMachine implements IInteractedMachine, IBindable, IWailaDisplayProvider {

    private final WirelessComputationContainerTrait trait;

    public WirelessNetworkComputationHatchMachine(MetaMachineBlockEntity holder, boolean transmitter) {
        super(holder);
        trait = new WirelessComputationContainerTrait(this, transmitter);
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            setOwnerUUID(player.getUUID());
            if (isRemote()) {
                player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.bind", GetName(player)));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) {
            setOwnerUUID(null);
            if (isRemote()) {
                player.sendSystemMessage(Component.translatable("gtmthings.machine.wireless_energy_hatch.tooltip.unbind"));
            }
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return trait.getUUID();
    }

    @Override
    public void onUnload() {
        if (trait.isTransmitter) {
            var c = trait.getWirelessComputationContainer();
            if (c == null) return;
            for (var controller : getControllers()) {
                if (controller instanceof IOpticalComputationProvider provider) c.removeProvider(provider);
            }
        }
        super.onUnload();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        if (trait.isTransmitter) {
            var c = trait.getWirelessComputationContainer();
            if (c == null) return;
            if (controller instanceof IOpticalComputationProvider provider) c.removeProvider(provider);
        }
        super.removedFromController(controller);
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (trait.isTransmitter) {
            var c = trait.getWirelessComputationContainer();
            if (c == null) return;
            if (controller instanceof IOpticalComputationProvider provider) c.addProvider(provider);
        }
    }

    @Override
    public void appendWailaTooltip(CompoundTag data, ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        iTooltip.add(Component.translatable("gtceu.multiblock.computation.usable", Component.literal(FormattingUtil.formatNumbers(data.getLong("cwu"))).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
        var c = trait.getWirelessComputationContainer();
        if (c == null) return;
        data.putLong("cwu", trait.requestCWU(Long.MAX_VALUE, true));
    }
}

package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.EnergyPort;
import com.gtocore.api.wireless.energy.EnergyPortTrait;
import com.gtocore.api.wireless.energy.GridBinding;
import com.gtocore.api.wireless.energy.PortKind;
import com.gtocore.api.wireless.energy.WirelessText;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.hepdd.gtmthings.api.capability.IBindable;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class WirelessEnergyInterfaceMachine extends TieredIOPartMachine implements IInteractedMachine, IMachineLife, IBindable {

    @SaveToDisk
    private final EnergyPortTrait portTrait;
    private final EnergyPort port;
    public final NotifiableEnergyContainer energyContainer;

    public WirelessEnergyInterfaceMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        this.portTrait = new EnergyPortTrait(this, PortKind.INTERFACE, GTValues.MAX);
        this.port = portTrait.port();
        this.energyContainer = new Interface(this);
    }

    @Override
    public @Nullable <T> Object getGTCapability(Class<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.ENERGY_CONTAINER) {
            if (side == null || side == getFrontFacing()) return energyContainer;
            return GTCapability.EMPTY;
        }
        return super.getGTCapability(cap, side);
    }

    @Override
    public Widget createUIWidget() {
        return MachineDisplay.page(this, this::addDisplayText, controls -> PortPriorityUI.addTo(controls, portTrait::getPriority, portTrait::setPriority));
    }

    private void addDisplayText(List<Component> textList) {
        textList.add(WirelessText.node(port.node()).copy().withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) return InteractionResult.PASS;
        if (isRemote()) return InteractionResult.SUCCESS;
        return GridBinding.setOwner(this, player, true) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    public boolean onLeftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        if (!player.getItemInHand(hand).is(GTItems.TOOL_DATA_STICK.asItem())) return false;
        if (isRemote()) return true;
        GridBinding.setOwner(this, player, false);
        return true;
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        if (player != null) setOwnerUUID(player.getUUID());
    }

    @Override
    public @Nullable UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public boolean preferTeamName() {
        return true;
    }

    @Override
    public int tintColor(int index) {
        if (index == 2) return GTValues.VC[getTier()];
        return super.tintColor(index);
    }

    private static final class Interface extends NotifiableEnergyContainer {

        private final WirelessEnergyInterfaceMachine owner;

        private Interface(WirelessEnergyInterfaceMachine machine) {
            super(machine, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, 0, 0);
            this.owner = machine;
        }

        @Override
        public long getEnergyCapacity() {
            return (long) owner.port.node().capacityDouble();
        }

        @Override
        public long getEnergyStored() {
            return (long) owner.port.node().storageDouble();
        }

        @Override
        public long acceptEnergyFromNetwork(Object o, @Nullable Direction side, long voltage, long energyAdded) {
            if (o instanceof NotifiableEnergyContainer && (side == null || inputsEnergy(side))) {
                return owner.port.deposit(energyAdded, GTUtil.getTierByVoltage(voltage));
            }
            return 0;
        }

        @Override
        public void checkOutputSubscription() {}

        @Override
        public void updateTick() {
            if (updateSubs != null) {
                updateSubs.unsubscribe();
                updateSubs = null;
            }
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return machine.getFrontFacing() == side;
        }

        @Override
        public boolean outputsEnergy(Direction side) {
            return false;
        }

        @Override
        public long changeEnergy(long energyToAdd) {
            return 0;
        }
    }
}

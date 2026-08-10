package com.gtocore.common.machine.multiblock.part;

import com.gtolib.api.wireless.ReceiverTransmitterHandler;
import com.gtolib.api.wireless.ReceiverTransmitterHandler.ConnectionType;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalDataHatchMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.hepdd.gtmthings.api.capability.IGTMTJadeIF;
import com.hepdd.gtmthings.api.misc.CleanableReferenceSupplier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class WirelessOpticalDataHatchMachine extends OpticalDataHatchMachine implements IDataStickInteractable, IGTMTJadeIF, IMachineLife {

    @SaveToDisk
    private BlockPos transmitterPos;
    @SaveToDisk
    private BlockPos receiverPos;

    public static final String KEY_TRANSMITTER = "wireless_data_transmitter";
    public static final String KEY_RECEIVER = "wireless_data_receiver";

    private final CleanableReferenceSupplier<MetaMachine> transmitterMachine = new CleanableReferenceSupplier<>(() -> MetaMachine.getMachine(getLevel(), transmitterPos), MetaMachine::isInValid);

    public WirelessOpticalDataHatchMachine(MetaMachineBlockEntity holder, boolean transmitter) {
        super(holder, transmitter);
    }

    @Override
    protected @Nullable IDataAccessHatch getAccessHatch() {
        Level level = getLevel();
        if (level == null || transmitterPos == null) return null;
        if (transmitterMachine.get() instanceof WirelessOpticalDataHatchMachine machine) {
            return machine;
        }
        return null;
    }

    private static CompoundTag createPos(BlockPos pos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        return posTag;
    }

    private static BlockPos getPos(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    private void setTransmitterPos(@Nullable BlockPos pos) {
        Level level = getLevel();
        if (transmitterPos != null && level != null && MetaMachine.getMachine(level, transmitterPos) instanceof WirelessOpticalDataHatchMachine machine) {
            machine.receiverPos = null;
            machine.onChanged();
        }
        transmitterPos = pos == null ? null : pos.immutable();
        transmitterMachine.clean();
        onChanged();
    }

    private void setReceiverPos(@Nullable BlockPos pos) {
        Level level = getLevel();
        if (receiverPos != null && level != null && MetaMachine.getMachine(level, receiverPos) instanceof WirelessOpticalDataHatchMachine machine) {
            machine.transmitterPos = null;
            machine.transmitterMachine.clean();
            machine.onChanged();
        }
        receiverPos = pos == null ? null : pos.immutable();
        onChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        refreshClientHandle();
    }

    @Override
    public void onUnload() {
        ReceiverTransmitterHandler.unregister(getLevel(), ConnectionType.DATA, getPos());
        super.onUnload();
    }

    @Override
    public void onMachineRemoved() {
        if (isTransmitter()) setReceiverPos(null);
        else setTransmitterPos(null);
        ReceiverTransmitterHandler.unregister(getLevel(), ConnectionType.DATA, getPos());
    }

    @Override
    public boolean isbinded() {
        return transmitterPos != null || receiverPos != null;
    }

    @Override
    public String getBindPos() {
        if (isTransmitter() && receiverPos != null) {
            return receiverPos.toShortString();
        } else if (!isTransmitter() && transmitterPos != null) {
            return transmitterPos.toShortString();
        }
        return "";
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack dataStick) {
        if (isRemote()) return InteractionResult.SUCCESS;

        CompoundTag tag = dataStick.getOrCreateTag();
        BlockPos currentPos = getPos();
        if (isTransmitter()) {
            tag.put(KEY_TRANSMITTER, createPos(currentPos));
            player.sendSystemMessage(Component.translatable("gtocore.machine.wireless_data_transmitter_hatch.to_bind"));
        } else {
            tag.put(KEY_RECEIVER, createPos(currentPos));
            player.sendSystemMessage(Component.translatable("gtocore.machine.wireless_data_receiver_hatch.to_bind"));
        }
        dataStick.setHoverName(Component.translatable("gtceu.machine.me.import_part.data_stick.name", Component.translatable(this.getDefinition().getDescriptionId())));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        if (isRemote()) return InteractionResult.sidedSuccess(true);

        CompoundTag tag = dataStick.getTag();
        if (tag == null) return InteractionResult.PASS;

        if (isTransmitter() && tag.contains(KEY_RECEIVER, 10)) {
            BlockPos otherPos = getPos(tag.getCompound(KEY_RECEIVER));
            if (bindWith(otherPos, player)) {
                return InteractionResult.SUCCESS;
            }
        } else if (!isTransmitter() && tag.contains(KEY_TRANSMITTER, 10)) {
            BlockPos otherPos = getPos(tag.getCompound(KEY_TRANSMITTER));
            if (bindWith(otherPos, player)) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private boolean bindWith(BlockPos otherPos, Player player) {
        Level level = getLevel();
        if (level == null) return false;

        MetaMachine otherMachine = MetaMachine.getMachine(level, otherPos);
        if (otherMachine instanceof WirelessOpticalDataHatchMachine otherWodh) {
            if (this.isTransmitter() == otherWodh.isTransmitter()) {
                return false;
            }
            if (isTransmitter()) {
                setReceiverPos(otherPos);
                otherWodh.setTransmitterPos(getPos());
            } else {
                setTransmitterPos(otherPos);
                otherWodh.setReceiverPos(getPos());
            }
            refreshClientHandle();

            player.sendSystemMessage(Component.translatable("gtocore.machine.wireless_data_hatch.bind"));
            return true;
        }
        return false;
    }

    private void refreshClientHandle() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;
        BlockPos otherPos = isTransmitter() ? receiverPos : transmitterPos;
        if (otherPos == null) return;
        BlockPos transmitter = isTransmitter() ? getPos() : otherPos;
        BlockPos receiver = isTransmitter() ? otherPos : getPos();
        ReceiverTransmitterHandler.register(level, ConnectionType.DATA, transmitter, receiver);
    }
}

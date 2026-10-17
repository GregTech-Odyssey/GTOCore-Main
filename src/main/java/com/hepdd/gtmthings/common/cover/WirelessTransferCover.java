package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyTransfer;
import com.gregtechceu.gtceu.api.transfer.key.RemoteKeyTarget;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageAccess;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.resources.ResourceLocation.tryParse;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WirelessTransferCover extends CoverBehavior {

    public static final int TRANSFER_ITEM = 1;
    public static final int TRANSFER_FLUID = 2;

    protected final int transferType;
    private TickableSubscription subscription;
    @SaveToDisk
    private String dimensionId;
    @SaveToDisk
    protected BlockPos targetPos;
    @SaveToDisk
    protected Direction facing;

    private final RemoteKeyTarget target = new RemoteKeyTarget();

    public WirelessTransferCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int transferType) {
        super(definition, coverHolder, attachedSide);
        this.transferType = transferType;
    }

    @Override
    public boolean canAttach() {
        if (super.canAttach()) {
            var targetMachine = MetaMachine.getMachine(coverHolder.holder());
            return targetMachine != null && (targetMachine.getItemHandlerCap(attachedSide, false) != null || targetMachine.getFluidHandlerCap(attachedSide, false) != null);
        }
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (coverHolder.isRemote()) return;
        getTargetLevel();
        subscription = coverHolder.subscribeServerTick(subscription, this::update, 20);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            this.dimensionId = tag.getString("dimensionid");
            var intX = tag.getInt("x");
            var intY = tag.getInt("y");
            var intZ = tag.getInt("z");
            this.targetPos = new BlockPos(intX, intY, intZ);
            this.facing = Direction.byName(tag.getString("facing"));
            getTargetLevel();
        }
        var targetMachine = MetaMachine.getMachine(coverHolder.holder());
        if (targetMachine instanceof SimpleTieredMachine simpleTieredMachine) {
            if (this.transferType == TRANSFER_ITEM) simpleTieredMachine.setAutoOutputItems(false);
            if (this.transferType == TRANSFER_FLUID) simpleTieredMachine.setAutoOutputFluids(false);
        } else if (targetMachine instanceof ItemBusPartMachine itemBusPartMachine && this.transferType == TRANSFER_ITEM) {
            itemBusPartMachine.setWorkingEnabled(false);
        } else if (targetMachine instanceof FluidHatchPartMachine fluidHatchPartMachine && this.transferType == TRANSFER_FLUID) {
            fluidHatchPartMachine.setWorkingEnabled(false);
        }
        super.onAttached(itemStack, player);
    }

    private void update() {
        if (transferType == TRANSFER_ITEM) {
            var targetItemTransfer = getTargetItemTransfer();
            var ownItemTransfer = getOwnItemTransfer();
            if (ownItemTransfer != null && targetItemTransfer != null) {
                KeyTransfer.transfer(ownItemTransfer, targetItemTransfer, Integer.MAX_VALUE);
            }
        } else if (transferType == TRANSFER_FLUID) {
            var targetFluidTransfer = getTargetFluidTransfer();
            var ownFluidTransfer = getOwnFluidTransfer();
            if (ownFluidTransfer != null && targetFluidTransfer != null) {
                KeyTransfer.transfer(ownFluidTransfer, targetFluidTransfer, Integer.MAX_VALUE);
            }
        }
    }

    private void getTargetLevel() {
        if (this.dimensionId == null) return;
        ResourceLocation resLoc = tryParse(this.dimensionId);
        ResourceKey<Level> resKey = ResourceKey.create(Registries.DIMENSION, resLoc);
        target.bind(Objects.requireNonNull(coverHolder.getLevel().getServer()).getLevel(resKey), targetPos);
    }

    protected @Nullable IKeyHandler<AEItemKey> getOwnItemTransfer() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    protected @Nullable IKeyHandler<AEItemKey> getTargetItemTransfer() {
        return target.items(facing, StorageAccess.INSERT);
    }

    protected @Nullable IKeyHandler<AEFluidKey> getOwnFluidTransfer() {
        return coverHolder.getFluidHandlerCap(attachedSide, false);
    }

    protected @Nullable IKeyHandler<AEFluidKey> getTargetFluidTransfer() {
        return target.fluids(facing, StorageAccess.INSERT);
    }
}

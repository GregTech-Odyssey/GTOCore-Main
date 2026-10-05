package com.gtocore.common.cover;

import com.gtolib.api.capability.IWirelessChargerInteraction;
import com.gtolib.api.machine.impl.WirelessChargerMachine;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyHandlerList;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class WirelessChargerCover extends CoverBehavior implements IWirelessChargerInteraction {

    private WirelessChargerMachine wirelessChargerMachine;

    private MetaMachine machine;

    private TickableSubscription subscription;

    private IKeyHandler<AEItemKey> handlerModifiable;

    public WirelessChargerCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        if (super.canAttach()) {
            machine = MetaMachine.getMachine(coverHolder.holder());
            if (machine != null) {
                for (var direction : Direction.values()) {
                    if (machine.getCoverContainer().getCoverAtSide(direction) instanceof WirelessChargerCover) return false;
                }
                handlerModifiable = machine.getItemHandlerCap(attachedSide, false);
                return handlerModifiable != null;
            }
        }
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (coverHolder.getLevel() instanceof ServerLevel) {
            subscription = coverHolder.subscribeServerTick(subscription, this::update, 20);
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        unsubscribe();
    }

    private void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private void update() {
        if (handlerModifiable == null) {
            if (machine == null) {
                machine = MetaMachine.getMachine(coverHolder.holder());
            }
            if (machine == null) {
                unsubscribe();
                return;
            } else {
                handlerModifiable = machine.getItemHandlerCap(attachedSide, false);
                if (handlerModifiable == null) {
                    return;
                }
            }
        }
        chargeHandler(handlerModifiable);
    }

    private void chargeHandler(IKeyHandler<AEItemKey> handler) {
        var base = handler.unrestricted();
        if (base instanceof KeyHandlerList<AEItemKey> list) {
            for (var h : list.handlers()) chargeHandler(h);
        } else if (base instanceof StackInventory stacks) {
            var slots = stacks.size();
            for (int i = 0; i < slots; i++) {
                var stack = stacks.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    IWirelessChargerInteraction.charge(getNetMachine(), stack);
                }
            }
        } else if (base instanceof KeyInventory<?> inventory) {
            var slots = inventory.size();
            for (int i = 0; i < slots; i++) {
                long amount = inventory.amountAt(i);
                if (amount <= 0 || !(inventory.keyAt(i) instanceof AEItemKey key) || !needsCharge(key.getReadOnlyStack())) continue;
                var stack = Keys.toStack(key, amount);
                IWirelessChargerInteraction.charge(getNetMachine(), stack);
                var charged = AEItemKey.of(stack);
                if (charged != null && charged != key) inventory.set(i, charged, amount);
            }
        }
    }

    private static boolean needsCharge(ItemStack stack) {
        var electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem != null) return electricItem.chargeable() && electricItem.getCharge() < electricItem.getMaxCharge();
        var energyItem = GTCapabilityHelper.getForgeEnergyItem(stack);
        return energyItem != null && energyItem.canReceive() && energyItem.getEnergyStored() < energyItem.getMaxEnergyStored();
    }

    @Override
    public BlockPos getPos() {
        return coverHolder.getPos();
    }

    @Override
    public @Nullable Level getLevel() {
        return coverHolder.getLevel();
    }

    @Override
    public UUID getOwnerUUID() {
        return machine.getOwnerUUID();
    }

    @Override
    public WirelessChargerMachine getNetMachineCache() {
        return wirelessChargerMachine;
    }

    @Override
    public void setNetMachineCache(WirelessChargerMachine cache) {
        wirelessChargerMachine = cache;
    }

    @Override
    public @Nullable UUID getUUID() {
        return machine == null ? null : machine.getOwnerUUID();
    }
}

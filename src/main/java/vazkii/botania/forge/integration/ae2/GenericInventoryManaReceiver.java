package vazkii.botania.forge.integration.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.capabilities.Capabilities;

import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;

import java.util.Optional;

record GenericInventoryManaReceiver(GenericInternalInventory inventory, BlockEntity owner) implements ManaPool {

    @Nullable
    static ManaReceiver find(BlockEntity be, @Nullable Direction side) {
        var inventory = be.getCapability(Capabilities.GENERIC_INTERNAL_INV, side).orElse(null);
        return inventory == null || !inventory.canInsert() || !inventory.isAllowed(ManaKey.KEY) ? null : new GenericInventoryManaReceiver(inventory, be);
    }

    private long insert(long amount, Actionable mode) {
        long remaining = amount;
        for (int slot = 0, size = inventory.size(); slot < size && remaining > 0; slot++) {
            var key = inventory.getKey(slot);
            if (key == null || key == ManaKey.KEY) {
                remaining -= inventory.insert(slot, ManaKey.KEY, remaining, mode);
            }
        }
        return amount - remaining;
    }

    private long extract(long amount, Actionable mode) {
        if (!inventory.canExtract()) {
            return 0;
        }
        long remaining = amount;
        for (int slot = 0, size = inventory.size(); slot < size && remaining > 0; slot++) {
            if (inventory.getKey(slot) == ManaKey.KEY) {
                remaining -= inventory.extract(slot, ManaKey.KEY, remaining, mode);
            }
        }
        return amount - remaining;
    }

    @Override
    public Level getManaReceiverLevel() {
        return owner.getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return owner.getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return (int) Math.min(Integer.MAX_VALUE, getCurrentManaLong());
    }

    @Override
    public long getCurrentManaLong() {
        long total = 0;
        for (int slot = 0, size = inventory.size(); slot < size; slot++) {
            if (inventory.getKey(slot) == ManaKey.KEY) {
                total += inventory.getAmount(slot);
            }
        }
        return total;
    }

    @Override
    public boolean isFull() {
        return insert(1, Actionable.SIMULATE) <= 0;
    }

    @Override
    public void receiveMana(int mana) {
        receiveManaLong(mana);
    }

    @Override
    public void receiveManaLong(long mana) {
        if (mana > 0) {
            insert(mana, Actionable.MODULATE);
        } else if (mana < 0) {
            extract(-mana, Actionable.MODULATE);
        }
    }

    @Override
    public long addMana(long amount, boolean simulate) {
        return amount <= 0 ? 0 : insert(amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
    }

    @Override
    public long removeMana(long amount, boolean simulate) {
        return amount <= 0 ? 0 : extract(amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE);
    }

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @Override
    public int getMaxMana() {
        return (int) Math.min(Integer.MAX_VALUE, getMaxManaLong());
    }

    @Override
    public long getMaxManaLong() {
        long total = 0;
        long perSlot = inventory.getMaxAmount(ManaKey.KEY);
        for (int slot = 0, size = inventory.size(); slot < size; slot++) {
            var key = inventory.getKey(slot);
            if (key == null || key == ManaKey.KEY) {
                total += perSlot;
                if (total < 0) {
                    return Long.MAX_VALUE;
                }
            }
        }
        return total;
    }

    @Override
    public Optional<DyeColor> getColor() {
        return Optional.empty();
    }

    @Override
    public void setColor(Optional<DyeColor> color) {}

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }
}

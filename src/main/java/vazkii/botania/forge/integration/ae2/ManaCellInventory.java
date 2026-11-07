package vazkii.botania.forge.integration.ae2;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;

import org.jetbrains.annotations.Nullable;

final class ManaCellInventory implements StorageCell {

    static final String TAG_AMOUNT = "amount";

    private final ItemStack stack;
    private final ManaCellItem cell;
    @Nullable
    private final ISaveProvider host;
    private long stored;
    private boolean persisted = true;
    private boolean versionStale;

    ManaCellInventory(ItemStack stack, ManaCellItem cell, @Nullable ISaveProvider host) {
        this.stack = stack;
        this.cell = cell;
        this.host = host;
        this.stored = Math.min(storedIn(stack), cell.getCapacity());
    }

    static long storedIn(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, tag.getLong(TAG_AMOUNT));
    }

    @Override
    public boolean supportsKeyType(appeng.api.stacks.AEKeyType<?> type) {
        return type == ManaKeyType.TYPE;
    }

    @Override
    public int operationTime() {
        return 1;
    }

    @Override
    public CellState getStatus() {
        if (stored <= 0) {
            return CellState.EMPTY;
        }
        return stored >= cell.getCapacity() ? CellState.FULL : CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return cell.getIdleDrain();
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what != ManaKey.KEY || amount <= 0) {
            return 0;
        }
        long accepted = Math.min(amount, cell.getCapacity() - stored);
        if (accepted > 0 && mode == Actionable.MODULATE) {
            stored += accepted;
            changed();
        }
        return Math.max(0, accepted);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what != ManaKey.KEY || amount <= 0) {
            return 0;
        }
        long taken = Math.min(amount, stored);
        if (taken > 0 && mode == Actionable.MODULATE) {
            stored -= taken;
            changed();
        }
        return Math.max(0, taken);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (stored > 0) {
            out.add(ManaKey.KEY, stored);
        }
    }

    private void changed() {
        persisted = false;
        if (host != null) {
            host.saveChanges();
        } else {
            persist();
        }
    }

    @Override
    public void persist() {
        if (persisted) {
            return;
        }
        if (stored > 0) {
            stack.getOrCreateTag().putLong(TAG_AMOUNT, stored);
        } else if (stack.getTag() != null) {
            stack.getTag().remove(TAG_AMOUNT);
            if (stack.getTag().isEmpty()) {
                stack.setTag(null);
            }
        }
        persisted = true;
    }

    @Override
    public Component getDescription() {
        return stack.getHoverName();
    }
}

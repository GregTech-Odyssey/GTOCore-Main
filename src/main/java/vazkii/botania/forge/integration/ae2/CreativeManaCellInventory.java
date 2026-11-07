package vazkii.botania.forge.integration.ae2;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;

final class CreativeManaCellInventory implements StorageCell {

    private final ItemStack stack;

    CreativeManaCellInventory(ItemStack stack) {
        this.stack = stack;
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
        return CellState.TYPES_FULL;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return what == ManaKey.KEY ? Math.max(0, amount) : 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return what == ManaKey.KEY ? Math.max(0, amount) : 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        out.add(ManaKey.KEY, Integer.MAX_VALUE);
    }

    @Override
    public void persist() {}

    @Override
    public Component getDescription() {
        return stack.getHoverName();
    }
}

package com.gtocore.common.machine.multiblock.part.ae.slots;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableContentHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.PlanScratch;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.recipesearch.IntLongMap;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ExportOnlyAEFluidList extends NotifiableContentHandler implements IRecipeHandler, IKeyHandler<AEFluidKey>, IConfigurableSlotList {

    @Getter
    @SaveToDisk
    final ExportOnlyAEFluidSlot[] inventory;
    private final long[] taken;
    private final AEFluidKey[] takenKeys;

    public ExportOnlyAEFluidList(MetaMachine machine, int slots) {
        this(machine, slots, ExportOnlyAEFluidSlot::new);
    }

    ExportOnlyAEFluidList(MetaMachine machine, int slots, Supplier<ExportOnlyAEFluidSlot> slotFactory) {
        super(machine, IO.IN);
        this.inventory = new ExportOnlyAEFluidSlot[slots];
        this.taken = new long[slots];
        this.takenKeys = new AEFluidKey[slots];
        for (int i = 0; i < slots; i++) {
            this.inventory[i] = slotFactory.get();
            this.inventory[i].setHandler(this);
        }
    }

    @Override
    public boolean updateEmpty() {
        for (var i : inventory) {
            if (i.config == null) continue;
            var stock = i.stock;
            if (stock == null || stock.amount() == 0) continue;
            return false;
        }
        return true;
    }

    boolean prepare() {
        return true;
    }

    boolean accepts(boolean consume) {
        return true;
    }

    @Override
    public boolean handlesFluids() {
        return true;
    }

    @Override
    public long available(AEKeyType type, KeyIngredient ingredient) {
        if (!prepare()) return 0;
        long total = 0;
        for (var i : inventory) {
            if (i.config == null) continue;
            var key = i.key();
            if (key != null && KeyIngredient.accepts(ingredient, key.uid, key)) {
                long a = i.stock.amount();
                total = total + a < 0 ? Long.MAX_VALUE : total + a;
            }
        }
        return total;
    }

    @Override
    public long reserveInput(PlanScratch plan, int member, AEKeyType type, int entry, KeyIngredient ingredient, long need, boolean consume) {
        if (!accepts(consume) || !prepare()) return 0;
        long got = 0;
        var inv = inventory;
        for (int s = 0; s < inv.length && got < need; s++) {
            var slot = inv[s];
            var key = slot.key();
            if (key == null || !KeyIngredient.accepts(ingredient, key.uid, key)) continue;
            long free = slot.stock.amount() - reserved(plan, member, s, false);
            if (free <= 0) continue;
            long t = Math.min(free, need - got);
            plan.logCustom(member, s, entry, t, type, consume, false);
            got += t;
        }
        return got;
    }

    @Override
    public boolean commitInput(PlanScratch plan, int member, AEKeyType type) {
        int n = inventory.length;
        for (int s = 0; s < n; s++) {
            taken[s] = 0;
            takenKeys[s] = null;
        }
        for (int i = 0, logSize = plan.logSize(); i < logSize; i++) {
            if (plan.logMember(i) == member && plan.logIsFluid(i) && plan.logConsumes(i)) taken[plan.logToken(i)] += plan.logAmount(i);
        }
        boolean changed = false;
        for (int s = 0; s < n; s++) {
            long want = taken[s];
            if (want <= 0) continue;
            var slot = inventory[s];
            var key = slot.key();
            long got = key == null ? 0 : slot.extract(want, false, false);
            takenKeys[s] = key;
            taken[s] = got;
            if (got < want) {
                restore(s);
                return false;
            }
            changed = true;
        }
        if (changed) onContentsChanged();
        return true;
    }

    @Override
    public void rollbackInput(PlanScratch plan, int member, AEKeyType type) {
        if (restore(inventory.length - 1)) onContentsChanged();
    }

    private boolean restore(int last) {
        boolean changed = false;
        for (int s = 0; s <= last; s++) {
            var key = takenKeys[s];
            long t = taken[s];
            taken[s] = 0;
            takenKeys[s] = null;
            if (key != null && t > 0) {
                inventory[s].restore(key, t);
                changed = true;
            }
        }
        return changed;
    }

    private static long reserved(PlanScratch plan, int member, int slot, boolean consumeOnly) {
        long r = 0;
        for (int i = 0, logSize = plan.logSize(); i < logSize; i++) {
            if (plan.logMember(i) == member && plan.logToken(i) == slot && plan.logIsFluid(i) && (!consumeOnly || plan.logConsumes(i))) r += plan.logAmount(i);
        }
        return r;
    }

    @Override
    public boolean forEachKey(AEKeyType type, KeyVisitor visitor) {
        if (!prepare()) return false;
        for (var i : inventory) {
            if (i.config == null) continue;
            var key = i.key();
            if (key != null && visitor.visit(key, i.stock.amount())) return true;
        }
        return false;
    }

    @Override
    public void fillSearchMap(@NotNull GTRecipeType type, @NotNull IntLongMap map) {
        if (!prepare()) return;
        for (var i : inventory) {
            if (i.config == null) continue;
            var key = i.key();
            if (key != null) type.convertKey(key, i.stock.amount(), map);
        }
    }

    @Override
    public AEKeyType keyType() {
        return AEKeyTypes.FLUIDS;
    }

    @Override
    public int size() {
        return inventory.length;
    }

    @Override
    public @Nullable AEFluidKey keyAt(int slot) {
        return inventory[slot].key();
    }

    @Override
    public long amountAt(int slot) {
        var s = inventory[slot];
        return s.key() == null ? 0 : s.stock.amount();
    }

    @Override
    public long slotLimit(int slot) {
        return Long.MAX_VALUE;
    }

    @Override
    public long insert(int slot, AEFluidKey key, long amount, boolean simulate) {
        return 0;
    }

    @Override
    public long extract(int slot, AEFluidKey key, long amount, boolean simulate) {
        return 0;
    }

    @Override
    public IConfigurableSlot getConfigurableSlot(int index) {
        return inventory[index];
    }

    @Override
    public int getConfigurableSlots() {
        return inventory.length;
    }

    public boolean isAutoPull() {
        return false;
    }

    @Override
    public boolean isLossyRollback() {
        return isStocking();
    }

    public boolean isStocking() {
        return false;
    }
}

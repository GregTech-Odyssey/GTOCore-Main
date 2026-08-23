package com.hepdd.gtmthings.api.misc;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.datasynclib.GTDataFixer;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import com.gto.datasynclib.datastream.data.NullData;
import com.gto.datasynclib.util.DataCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class UnlimitedItemStackTransfer extends CustomItemStackHandler {

    public UnlimitedItemStackTransfer(int size) {
        super(size);
    }

    public UnlimitedItemStackTransfer(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public UnlimitedItemStackTransfer(ItemStack stack) {
        super(stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Data writeData() {
        ListData list = new ListData();
        if (this.isInputLimited) list.addNull();
        ItemStack[] stacks = this.stacks;
        for (int i = 0; i < this.size; ++i) {
            ItemStack stack = stacks[i];
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.putInt("realCount", stack.getCount());
                stack.copyWithCount(1).save(itemTag);
                list.add(DataCodecs.COMPOUND_TAG_CODEC.encode(itemTag));
            }
        }
        return list.isEmpty() ? NullData.INSTANCE : list;
    }

    @Override
    public void readData(@NotNull Data data, int dataVersion) {
        isInputLimited = false;
        if (dataVersion < 1) {
            GTDataFixer.decodeCustomItemStackHandler(this, data, dataVersion);
        } else {
            ItemStack[] stacks = this.stacks;
            Arrays.fill(stacks, ItemStack.EMPTY);
            if (data == NullData.INSTANCE) return;
            ListData list = data.asListData();
            int size = list.size();
            if (size == 0) return;
            int i = 0;
            if (list.get(0) == NullData.INSTANCE) {
                isInputLimited = true;
                ++i;
            }
            for (; i < size; ++i) {
                var item = DataCodecs.COMPOUND_TAG_CODEC.decode(list.get(i), dataVersion);
                int slot = item.getInt("Slot");
                int count = item.getInt("realCount");
                if (slot >= 0 && slot < this.size && count > 0) {
                    var stack = ItemStack.of(item);
                    stack.setCount(count);
                    stacks[slot] = stack;
                }
            }
        }
    }
}

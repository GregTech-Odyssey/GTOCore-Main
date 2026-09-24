package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;

import com.gto.datasynclib.LogicalSide;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import com.gto.datasynclib.datastream.data.NullData;
import com.gto.datasynclib.util.DataCodecs;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.ParametersAreNonnullByDefault;

@Setter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftPatternPartMachine extends MEPatternPartMachine<MECraftPatternPartMachine.InternalSlot> {

    private Runnable onContentsChanged = () -> {};

    public MECraftPatternPartMachine(MetaMachineBlockEntity holder) {
        super(holder, 72);
    }

    @Override
    public InternalSlot[] createInternalSlotArray() {
        return new InternalSlot[72];
    }

    @Override
    public boolean patternFilter(ItemStack stack) {
        return stack.getItem() instanceof EncodedPatternItem &&
                !(stack.getItem() instanceof ProcessingPatternItem) &&
                checkDuplicatedPattern(this, stack);
    }

    @Override
    public InternalSlot createInternalSlot(int i) {
        return new InternalSlot(this);
    }

    @Override
    public boolean defaultShowInTravel() {
        return false;
    }

    public static final class InternalSlot extends AbstractInternalSlot {

        @Getter
        private ItemStack output;
        @Setter
        @Getter
        private long amount;
        private final MECraftPatternPartMachine machine;

        private InternalSlot(MECraftPatternPartMachine machine) {
            this.machine = machine;
        }

        @Override
        public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
            if (patternDetails instanceof IMolecularAssemblerSupportedPattern pattern && pattern.getOutputs().length == 1 && pattern.getOutputs()[0].what() instanceof AEItemKey itemKey) {
                if (output == null) output = itemKey.toStack();
                amount += pattern.getOutputs()[0].amount();
                machine.onContentsChanged.run();
                return true;
            }
            return false;
        }

        @Override
        public void onPatternChange() {
            output = null;
            amount = 0;
        }

        @Override
        public void writeBuffer(LogicalSide logicalSide, FriendlyByteBuf friendlyByteBuf) {
            // 无同步，不实现
        }

        @Override
        public void readBuffer(LogicalSide logicalSide, FriendlyByteBuf friendlyByteBuf) {
            // 无同步，不实现
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            amount = nbt.getLong("amount");
            if (nbt.contains("output")) {
                output = ItemStack.of(nbt.getCompound("output"));
            }
        }

        /// 旧版这里构造了列表却没有返回，待取出的合成产物从未存盘；旧存档里该槽是空数据，读取时跳过
        @Override
        public Data writeData() {
            if (output == null) return NullData.INSTANCE;
            var list = new ListData(2);
            list.addLong(amount);
            list.add(DataCodecs.ITEM_STACK_CODEC.encode(output));
            return list;
        }

        @Override
        public void readData(Data data, int dataVersion) {
            if (data.isNull()) return;
            if (dataVersion < 2) {
                var nbt = DataCodecs.TAG_CODEC.decode(data, dataVersion);
                if (nbt instanceof CompoundTag compoundTag) {
                    deserializeNBT(compoundTag);
                    return;
                }
            }
            var list = data.asListData();
            amount = list.getLong(0);
            output = DataCodecs.ITEM_STACK_CODEC.decode(list.get(1), dataVersion);
        }
    }

    @Override
    public boolean gto$isCraftingContainer() {
        return true;
    }
}

package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.data.GTOCodecs;
import com.gtocore.integration.jade.AEKeyTooltip;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyMap;
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
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import javax.annotation.ParametersAreNonnullByDefault;

@Setter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftPatternPartMachine extends MEPatternPartMachine<MECraftPatternPartMachine.InternalSlot> implements IWailaDisplayProvider {

    private Runnable onContentsChanged = () -> {};

    public MECraftPatternPartMachine(MetaMachineBlockEntity holder) {
        this(holder, 72);
    }

    public MECraftPatternPartMachine(MetaMachineBlockEntity holder, int maxPatternCount) {
        super(holder, maxPatternCount);
    }

    @Override
    public InternalSlot[] createInternalSlotArray() {
        return new InternalSlot[getMaxPatternCount()];
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
        @Nullable
        private AEItemKey output;
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
                return addOutput(itemKey, pattern.getOutputs()[0].amount());
            }
            return false;
        }

        boolean addOutput(AEItemKey itemKey, long addAmount) {
            if (addAmount < 1) return false;
            if (output == null) output = itemKey;
            amount += addAmount;
            machine.onContentsChanged.run();
            return true;
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
                output = AEItemKey.of(ItemStack.of(nbt.getCompound("output")));
            }
        }

        @Override
        public Data writeData() {
            if (output == null || amount == 0) return NullData.INSTANCE;
            var list = new ListData(2);
            list.addLong(amount);
            list.add(GTOCodecs.AE_ITEM_KEY_DATA_CODEC, output);
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
            output = GTOCodecs.AE_ITEM_KEY_DATA_CODEC.decode(list.get(1), dataVersion);
        }
    }

    @Override
    public boolean gto$isCraftingContainer() {
        return true;
    }

    @Override
    public void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
        var pending = new AEKeyMap<AEItemKey>();
        for (var slot : getInternalInventory()) {
            var output = slot.getOutput();
            var amount = slot.getAmount();
            if (output == null || amount < 1) continue;
            pending.insert(output, amount);
        }
        AEKeyTooltip.write(data, AEKeyTooltip.PENDING, pending);
    }

    @Override
    public void appendWailaTooltip(CompoundTag data, ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config) {
        AEKeyTooltip.read(tooltip, data, AEKeyTooltip.PENDING);
    }
}

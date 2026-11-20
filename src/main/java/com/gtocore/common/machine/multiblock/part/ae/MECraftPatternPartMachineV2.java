package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.data.GTOCodecs;
import com.gtocore.config.GTORules;
import com.gtocore.integration.jade.AEKeyTooltip;

import com.gtolib.api.machine.trait.MEStorageCapacity;
import com.gtolib.utils.LegacyIO;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyLongMap;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;

import com.gto.datasynclib.LogicalSide;
import com.gto.datasynclib.annotations.AdditionalHolder;
import com.gto.datasynclib.datastream.codec.ValueOps;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;

import javax.annotation.ParametersAreNonnullByDefault;

@Setter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftPatternPartMachineV2 extends MEPatternPartMachine<MECraftPatternPartMachineV2.InternalSlot> implements IWailaDisplayProvider {

    @AdditionalHolder
    protected final MEStorageCapacity capacity = new MEStorageCapacity(this, this::isCapacityLimited);

    protected boolean isCapacityLimited() {
        return GTORules.ME_CAPACITY_LIMIT.get();
    }

    private Runnable onContentsChanged = () -> {};

    public MECraftPatternPartMachineV2(MetaMachineBlockEntity holder) {
        this(holder, 72);
    }

    public MECraftPatternPartMachineV2(MetaMachineBlockEntity holder, int maxPatternCount) {
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

    public static final class InternalSlot extends AbstractInternalSlot {

        @Getter
        @Nullable
        private AEItemKey output;
        @Setter
        @Getter
        private long amount;
        private final MECraftPatternPartMachineV2 machine;

        private InternalSlot(MECraftPatternPartMachineV2 machine) {
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
            if (addAmount < 1 || (output != null && output != itemKey && amount > 0)) return false;
            if (addAmount > Math.max(0, machine.capacity.items() - amount)) return false;
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
        public @NotNull Object writeValue(@NotNull ValueOps ops) {
            if (output == null || amount == 0) return ops.createNull();
            var list = new ArrayList<Object>(2);
            ops.addLong(list, amount);
            list.add(GTOCodecs.AE_ITEM_KEY_DATA_CODEC.encode(ops, output));
            return ops.createList(list);
        }

        @Override
        public void readValue(@NotNull Object data, @NotNull ValueOps ops) {
            if (ops.isNull(data)) return;
            if (ops.dataVersion() < 2) {
                var nbt = LegacyIO.legacyCompound(ops, data);
                if (nbt != null) {
                    deserializeNBT(nbt);
                    return;
                }
            }
            var list = ops.getList(data);
            amount = ops.getLong(list, 0);
            output = GTOCodecs.AE_ITEM_KEY_DATA_CODEC.decode(ops, list.get(1));
        }
    }

    @Override
    public boolean gto$isCraftingContainer() {
        return true;
    }

    @Override
    public void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
        var pending = new AEKeyLongMap<AEItemKey>();
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

    @Override
    public boolean savePickClone() {
        return false;
    }
}

package com.gtocore.mixin.jade;

import com.gtocore.common.blockentity.TesseractBlockEntity;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyHandlerList;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MufflerPartMachine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.addon.universal.ItemIterator;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.util.JadeForgeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(JadeForgeUtils.class)
public abstract class JadeForgeUtilsMixin {

    @Shadow(remap = false)
    public static ItemIterator<? extends IItemHandler> fromItemHandler(IItemHandler storage, int fromIndex, Function<Object, @Nullable IItemHandler> containerFinder) {
        return null;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    @SuppressWarnings("unchecked")
    public static ItemIterator<? extends IItemHandler> fromItemHandler(IItemHandler storage, int fromIndex) {
        return fromItemHandler(storage, fromIndex, target -> {
            if (target instanceof CapabilityProvider<?> capProvider) {
                if (capProvider instanceof MetaMachineBlockEntity blockEntity && !(blockEntity instanceof TesseractBlockEntity)) {
                    if (blockEntity.metaMachine instanceof MEPatternPartMachine<?>) return null;
                    if (blockEntity.metaMachine instanceof MufflerPartMachine mufflerPartMachine) {
                        return new ForgeItemAdapter(mufflerPartMachine.getInventory());
                    }
                    var ts = blockEntity.metaMachine.getTraits();
                    List<IKeyHandler<AEItemKey>> filteredTraits = new ArrayList<>(ts.size());
                    for (var t : ts) {
                        if (t instanceof IKeyHandler<?> handler && handler.keyType() == AEKeyType.items()) {
                            filteredTraits.add((IKeyHandler<AEItemKey>) handler);
                        }
                    }
                    if (!filteredTraits.isEmpty()) {
                        return new ForgeItemAdapter(new KeyHandlerList<>(AEKeyType.items(), filteredTraits));
                    }
                }
                return capProvider.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            }
            return null;
        });
    }

    @Inject(method = "fromFluidHandler", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gto$fromKeyFluidHandler(IFluidHandler fluidHandler, CallbackInfoReturnable<List<ViewGroup<CompoundTag>>> cir) {
        if (!(fluidHandler instanceof ForgeFluidAdapter adapter)) return;
        var handler = adapter.getHandler();
        int size = handler.size();
        List<CompoundTag> list = new ArrayList<>(size);
        long emptyCapacity = 0;
        for (int i = 0; i < size; i++) {
            long capacity = handler.slotLimit(i);
            if (capacity <= 0) continue;
            var key = handler.keyAt(i);
            long amount = handler.amountAt(i);
            if (key == null || amount <= 0) {
                emptyCapacity = emptyCapacity + capacity < 0 ? Long.MAX_VALUE : emptyCapacity + capacity;
            } else {
                list.add(gto$fluidView(key.getFluid(), amount, capacity, key.copyTag()));
            }
        }
        if (list.isEmpty() && emptyCapacity > 0) {
            list.add(gto$fluidView(Fluids.EMPTY, 0, emptyCapacity, null));
        }
        cir.setReturnValue(list.isEmpty() ? List.of() : List.of(new ViewGroup<>(list)));
    }

    @Unique
    private static CompoundTag gto$fluidView(Fluid fluid, long amount, long capacity, @Nullable CompoundTag tag) {
        var nbt = new CompoundTag();
        nbt.putString("fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
        nbt.putLong("amount", amount);
        nbt.putLong("capacity", capacity);
        if (tag != null) nbt.put("tag", tag);
        return nbt;
    }
}

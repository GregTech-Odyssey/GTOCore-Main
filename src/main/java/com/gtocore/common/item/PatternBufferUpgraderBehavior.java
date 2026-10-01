package com.gtocore.common.item;

import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.PatternBufferType;

import com.gtolib.api.item.IMachineUpgraderBehavior;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

import static com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachine.INTERNAL_INVENTORY;
import static com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachine.PATTERN_INVENTORY;

public enum PatternBufferUpgraderBehavior implements IMachineUpgraderBehavior {

    PatternBuffer(() -> GTAEMachines.ME_PATTERN_BUFFER),
    ExPatternBuffer(() -> GTAEMachines.ME_EXTEND_PATTERN_BUFFER),
    UltraPatternBuffer(() -> GTAEMachines.ME_EXTEND_PATTERN_BUFFER_ULTRA),;

    private final Supplier<MachineDefinition> upgradeTo;

    PatternBufferUpgraderBehavior(Supplier<MachineDefinition> upgradeTo) {
        this.upgradeTo = upgradeTo;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var pos = context.getClickedPos();
        var world = context.getLevel();
        var tile = world.getBlockEntity(pos);
        if (tile instanceof MetaMachineBlockEntity mbe &&
                mbe.getMetaMachine() instanceof MEPatternBufferPartMachine machine) {
            var target = PatternBufferType.of(upgradeTo.get());
            if (target == null || !machine.getType().canUpgradeTo(target)) return InteractionResult.PASS;

            var originState = world.getBlockState(pos);
            var state = copyBlockStateProperties(originState, upgradeTo.get().get().defaultBlockState());

            BlockEntity upgradedTile = upgradeTo.get().get().newBlockEntity(pos, state);
            if (upgradedTile instanceof MetaMachineBlockEntity upgradedMbe &&
                    upgradedMbe.getMetaMachine() instanceof MEPatternBufferPartMachine upgradedMachine) {

                replaceBlockEntityWithNBTHook(world, pos, tile, upgradedTile, state, contents -> growContents(contents, upgradedTile.serializeNBT(), machine.getMaxPatternCount()));
                for (int i = 0; i < machine.getMaxPatternCount(); i++) {
                    upgradedMachine.getPatternInventory().setStackInSlot(i, machine.getPatternInventory().getStackInSlot(i).copy());
                }
                state.getBlock().setPlacedBy(context.getLevel(), pos, state, context.getPlayer(), context.getItemInHand());

                ItemStack replaced = machine.getDefinition().asStack();
                if (context.getPlayer() instanceof ServerPlayer player) {
                    player.playNotifySound(upgradedMachine.getBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 2.0F);
                    context.getItemInHand().shrink(1);
                    if (!context.getPlayer().getInventory().add(replaced)) context.getPlayer().drop(replaced, false);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    private static void growContents(CompoundTag contents, CompoundTag empty, int oldSize) {
        contents.put(PATTERN_INVENTORY, empty.getCompound(PATTERN_INVENTORY).copy());
        var slots = contents.getList(INTERNAL_INVENTORY, Tag.TAG_COMPOUND);
        var emptySlots = empty.getList(INTERNAL_INVENTORY, Tag.TAG_COMPOUND);
        for (int i = oldSize; i < emptySlots.size(); i++) slots.add(emptySlots.get(i).copy());
        contents.put(INTERNAL_INVENTORY, slots);
    }
}

package com.gtocore.common.machine.magic.rune;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import org.jetbrains.annotations.Nullable;

/**
 * 符文支托：单格上限为 1，存放单个符文或仪式材料；右键放入或取回，支持管道/漏斗自动输入输出。
 *
 * <p>
 * 仪式进行时大师符文支托会设置漂浮目标，符文沿抛物线漂向中心。
 * </p>
 */
public class RuneHolderMachine extends MetaMachine implements IInteractedMachine, IMachineLife {

    @SaveToDisk
    public final NotifiableStackInventory inventory;

    /** 客户端渲染用的符文副本。 */
    @SyncToClient
    private ItemStack syncedRune = ItemStack.EMPTY;

    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean hasFloatTarget;

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private long floatTarget;

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private double floatProgress;

    public RuneHolderMachine(MetaMachineBlockEntity holder) {
        super(holder);
        var storage = new StackInventory(1) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        inventory = new NotifiableStackInventory(this, storage, IO.NONE, IO.BOTH);
        inventory.storage.setFilter(stack -> !stack.isEmpty());
        inventory.addChangedListener(() -> {
            syncedRune = inventory.storage.getStackInSlot(0).copy();
            requestSync();
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            ItemStack current = inventory.storage.getStackInSlot(0);
            if (!ItemStack.matches(syncedRune, current)) {
                syncedRune = current.copy();
                requestSync();
            }
        }
    }

    public ItemStack getRune() {
        return isRemote() ? syncedRune : inventory.storage.getStackInSlot(0);
    }

    public void setRune(ItemStack stack) {
        inventory.storage.setStackInSlot(0, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
    }

    public boolean hasRune() {
        return !getRune().isEmpty();
    }

    @Nullable
    public BlockPos getFloatTarget() {
        return hasFloatTarget ? BlockPos.of(floatTarget) : null;
    }

    public double getFloatProgress() {
        return floatProgress;
    }

    public void setFloatTarget(@Nullable BlockPos target, double progress) {
        boolean hasTarget = target != null;
        long position = hasTarget ? target.asLong() : 0L;
        if (hasFloatTarget == hasTarget && floatTarget == position && Double.compare(floatProgress, progress) == 0) {
            return;
        }
        hasFloatTarget = hasTarget;
        floatTarget = position;
        floatProgress = progress;
        requestSync();
    }

    @Override
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack stored = getRune();
        if (!stored.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack give = stored.copy();
                if (held.isEmpty()) {
                    player.setItemInHand(hand, give);
                } else {
                    player.getInventory().placeItemBackInInventory(give);
                }
                setRune(ItemStack.EMPTY);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!held.isEmpty()) {
            if (!level.isClientSide) {
                setRune(held.split(1));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }
}

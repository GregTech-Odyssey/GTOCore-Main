package com.gtocore.common.machine.magic.rune;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;

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
 * 符文支托：只存放一个物品（符文或仪式材料），右键放入或取回，漏斗可以从上方输入、下方抽出。
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

    @SaveToDisk(defaultValue = "-1")
    @SyncToClient
    private long floatTarget = -1;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private double floatProgress = 0;

    public RuneHolderMachine(MetaMachineBlockEntity holder) {
        super(holder);
        inventory = new NotifiableStackInventory(this, 1, IO.NONE, IO.BOTH);
        inventory.storage.setFilter(stack -> !stack.isEmpty());
        inventory.addChangedListener(() -> {
            syncedRune = inventory.storage.getStackInSlot(0).copy();
            requestSync();
        });
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
        return floatTarget < 0 ? null : BlockPos.of(floatTarget);
    }

    public double getFloatProgress() {
        return floatProgress;
    }

    public void setFloatTarget(@Nullable BlockPos target, double progress) {
        long position = target == null ? -1 : target.asLong();
        if (floatTarget == position && Double.compare(floatProgress, progress) == 0) {
            return;
        }
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
                if (held.isEmpty()) {
                    player.setItemInHand(hand, stored.copy());
                } else if (ItemStack.isSameItemSameTags(held, stored) && held.getCount() < held.getMaxStackSize()) {
                    held.grow(1);
                } else {
                    player.getInventory().add(stored.copy());
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

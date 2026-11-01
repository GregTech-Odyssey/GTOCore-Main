package com.hepdd.gtmthings.common.item.Behaviour;

import com.gtocore.common.item.ICoordinateHolder;

import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.data.GTMTCovers;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import org.jetbrains.annotations.Nullable;

public record WirelessTransferCoverPlaceBehavior(CoverDefinition coverDefinition) implements IInteractionItem, ICoordinateHolder {

    /** 旧版维度键，仅用于兼容读取 */
    private static final String TAG_DIMENSION_ID = "dimensionid";
    public static final String TAG_BLOCK_ID = "blockid";
    public static final String TAG_FACING = "facing";

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            Level level = context.getLevel();
            BlockPos blockPos = context.getClickedPos();
            Direction face = context.getClickedFace();
            if (canBind(level, blockPos, face)) {
                String blockId = level.getBlockState(blockPos).getBlock().getDescriptionId();
                CompoundTag tag = itemStack.getOrCreateTag();
                ICoordinateHolder.write(tag, GlobalPos.of(level.dimension(), blockPos));
                tag.remove(TAG_DIMENSION_ID);
                tag.putString(TAG_BLOCK_ID, blockId);
                tag.putString(TAG_FACING, face.getName());
                if (level.isClientSide()) player.sendSystemMessage(Component.translatable("item.gtmthings.wireless_transfer.tooltip.bind.1", Component.translatable(blockId), blockPos.toShortString()));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean canBind(Level level, BlockPos pos, Direction face) {
        if (coverDefinition == GTMTCovers.WIRELESS_ITEM_TRANSFER || coverDefinition == GTMTCovers.ADVANCED_WIRELESS_ITEM_TRANSFER) {
            var itemTransfer = ItemTransferHelper.getItemTransfer(level, pos, face);
            return itemTransfer != null && itemTransfer.getSlots() > 0;
        }
        if (coverDefinition == GTMTCovers.WIRELESS_FLUID_TRANSFER || coverDefinition == GTMTCovers.ADVANCED_WIRELESS_FLUID_TRANSFER) {
            var fluidTransfer = FluidTransferHelper.getFluidTransfer(level, pos, face);
            return fluidTransfer != null && fluidTransfer.getTanks() > 0;
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack is = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown() && getStoredGlobalPos(is.getTag()) != null) {
            ICoordinateHolder.clear(is, TAG_DIMENSION_ID, TAG_BLOCK_ID, TAG_FACING);
            if (level.isClientSide()) player.sendSystemMessage(Component.translatable("item.gtmthings.wireless_transfer.tooltip.bind.2"));
            return InteractionResultHolder.sidedSuccess(is, level.isClientSide());
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public @Nullable GlobalPos getStoredGlobalPos(ItemStack stack) {
        return getStoredGlobalPos(stack.getTag());
    }

    @Nullable
    public static GlobalPos getStoredGlobalPos(@Nullable CompoundTag tag) {
        return ICoordinateHolder.read(tag, TAG_DIMENSION_ID, null);
    }
}

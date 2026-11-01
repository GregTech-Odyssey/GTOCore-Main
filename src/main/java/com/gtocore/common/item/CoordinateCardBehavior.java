package com.gtocore.common.item;

import com.gtolib.api.data.GTODimensions;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoordinateCardBehavior implements IInteractionItem, IAddInformation, ICoordinateHolder {

    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_MACHINE = "machine";

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (context.getPlayer() == null) return InteractionResult.PASS;
        BlockPos blockPos = context.getClickedPos();
        ItemStack card = context.getItemInHand();
        setStoredCoordinates(card, GlobalPos.of(level.dimension(), blockPos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
        ItemStack card = player.getItemInHand(usedHand);
        if (getStoredCoordinates(card.getTag()) == null) return IInteractionItem.super.use(item, level, player, usedHand);
        if (!level.isClientSide) ICoordinateHolder.clear(card, TAG_MACHINE);
        return InteractionResultHolder.sidedSuccess(card, level.isClientSide);
    }

    @Override
    public void appendTooltips(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(Tips.rightClick(Component.translatable("gtocore.tooltip.item.machine_coordinate_card.tooltip.1")));
        CompoundTag tag = itemStack.getTag();
        GlobalPos globalPos = getStoredGlobalPos(tag, level == null ? null : level.dimension());
        BlockPos pos = globalPos != null ? globalPos.pos() : getStoredCoordinates(tag);
        if (pos == null) return;
        list.add(Tips.stat(Component.translatable("gtocore.tooltip.item.machine_coordinate_card.tooltip.2", Component.translatable("config.jade.plugin_jade.coordinates"), "§5" + pos.getX(), "§d" + pos.getY(), "§e" + pos.getZ())));
        if (globalPos != null) {
            list.add(Tips.stat(Component.translatable("recipe.condition.dimension.tooltip", Component.translatable(GTODimensions.getTranslationKey(globalPos.dimension())))));
        }
    }

    @Override
    public @Nullable GlobalPos getStoredGlobalPos(ItemStack stack) {
        return getStoredGlobalPos(stack.getTag(), null);
    }

    /** 写入坐标并清除机器标记 */
    public static void setStoredCoordinates(ItemStack stack, GlobalPos pos) {
        var tag = stack.getOrCreateTag();
        ICoordinateHolder.write(tag, pos);
        tag.remove(TAG_MACHINE);
    }

    @Nullable
    public static GlobalPos getStoredGlobalPos(ItemStack stack, @Nullable ResourceKey<Level> fallback) {
        return getStoredGlobalPos(stack.getTag(), fallback);
    }

    @Nullable
    public static GlobalPos getStoredGlobalPos(@Nullable CompoundTag tag, @Nullable ResourceKey<Level> fallback) {
        return ICoordinateHolder.read(tag, TAG_DIMENSION, fallback);
    }

    @Nullable
    public static BlockPos getStoredCoordinates(@Nullable CompoundTag tag) {
        return ICoordinateHolder.readPos(tag);
    }
}

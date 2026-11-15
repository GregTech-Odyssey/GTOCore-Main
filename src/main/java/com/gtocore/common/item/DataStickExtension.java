package com.gtocore.common.item;

import com.gtolib.api.item.MirrorHatchDataStick;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IDataItemExtension;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DataStickExtension implements IDataItemExtension, IAddInformation {

    public static final DataStickExtension INSTANCE = new DataStickExtension();

    private DataStickExtension() {}

    @Override
    public InteractionResult afterDataItemUse(MetaMachine machine, ItemStack stack, UseOnContext context, InteractionResult result) {
        return MirrorHatchDataStick.afterDataItemUse(machine, stack, context, result);
    }

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        MirrorHatchDataStick.appendTooltips(stack, lines);
    }

    public static boolean isItem(ItemStack stack) {
        if (!(stack.getItem() instanceof ComponentItem item)) return false;
        var components = item.getComponents();
        for (IItemComponent component : components) {
            if (component instanceof DataStickExtension) return true;
        }
        return false;
    }
}

package com.gtocore.common.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;

import net.minecraft.world.item.ItemStack;

public final class DataStickExtension implements IItemComponent {

    public static final DataStickExtension INSTANCE = new DataStickExtension();

    private DataStickExtension() {}

    public static boolean isItem(ItemStack stack) {
        if (!(stack.getItem() instanceof ComponentItem item)) return false;
        var components = item.getComponents();
        for (IItemComponent component : components) {
            if (component instanceof DataStickExtension) return true;
        }
        return false;
    }
}

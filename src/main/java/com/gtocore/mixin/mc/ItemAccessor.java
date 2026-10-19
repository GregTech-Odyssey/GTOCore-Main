package com.gtocore.mixin.mc;

import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemAccessor {

    @Accessor("descriptionId")
    void gtocore$setDescriptionId(String descriptionId);
}

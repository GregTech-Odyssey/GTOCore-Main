package com.gtocore.mixin.ae2.storage;

import com.gregtechceu.gtceu.api.tooltip.StatKind;
import com.gregtechceu.gtceu.api.tooltip.Tips;

import net.minecraft.network.chat.Component;

import appeng.me.cells.BasicCellHandler;
import appeng.me.cells.BasicCellInventory;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(BasicCellHandler.class)
public class BasicCellHandlerMixin {

    @Redirect(method = "addCellInformationToTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), remap = false)
    private boolean gtocore$bytesUsed(List<Component> instance, Object e, @Local(name = "handler") BasicCellInventory handler) {
        return instance.add(Tips.usage(StatKind.STORAGE_BYTES, handler.getUsedBytes(), handler.getTotalBytes()));
    }

    @Redirect(method = "addCellInformationToTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1), remap = false)
    private boolean gtocore$typesUsed(List<Component> instance, Object e, @Local(name = "handler") BasicCellInventory handler) {
        return instance.add(Tips.stat(StatKind.STORAGE_TYPES, handler.getStoredItemTypes()));
    }
}

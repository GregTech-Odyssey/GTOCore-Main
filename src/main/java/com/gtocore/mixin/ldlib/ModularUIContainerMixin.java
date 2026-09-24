package com.gtocore.mixin.ldlib;

import com.gtocore.api.gui.ui.IShiftClickPriority;

import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shift+点击选目标槽时，先按 {@link IShiftClickPriority} 从高到低，同优先级再按 LDLib 原有的槽位顺序
 * （放进容器时注册顺序在前的先放，放回玩家背包时反过来）。
 */
@Mixin(value = ModularUIContainer.class, remap = false)
public abstract class ModularUIContainerMixin {

    @Shadow
    @Final
    private ModularUI modularUI;

    @Inject(method = "getShiftClickSlots", at = @At("HEAD"), cancellable = true)
    private void gtocore$sortByPriority(ItemStack itemStack, boolean fromContainer, CallbackInfoReturnable<List<SlotWidget>> cir) {
        var slots = new ArrayList<SlotWidget>();
        for (var slot : modularUI.getSlotMap().values()) {
            if (slot.isPlayerContainer == fromContainer && slot.canMergeSlot(itemStack)) slots.add(slot);
        }
        int direction = fromContainer ? -1 : 1;
        slots.sort((a, b) -> {
            int byPriority = Integer.compare(IShiftClickPriority.of(b), IShiftClickPriority.of(a));
            if (byPriority != 0) return byPriority;
            return Integer.compare(direction * a.getHandler().index, direction * b.getHandler().index);
        });
        cir.setReturnValue(slots);
    }
}

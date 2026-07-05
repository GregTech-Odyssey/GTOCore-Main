package com.gtocore.mixin.ftbxmodcompat;

import net.minecraft.world.item.ItemStack;

import dev.ftb.mods.ftbquests.integration.item_filtering.ItemMatchingSystem;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbxmodcompat.ftbquests.recipemod_common.WrappedQuest;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WrappedQuest.class)
public class WrappedQuestMixin {

    @Shadow(remap = false)
    @Final
    public List<List<ItemStack>> input;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void gtocore$expandItemTaskFilters(Quest quest, List<Reward> rewards, CallbackInfo ci) {
        int inputIndex = quest.getTasks().size() == 1 ? 4 : 0;

        for (var task : quest.getTasks()) {
            if (task instanceof ItemTask itemTask && ItemMatchingSystem.INSTANCE.isItemFilter(itemTask.getItemStack())) {
                input.set(inputIndex, List.copyOf(itemTask.getValidDisplayItems()));
            }
            inputIndex++;
        }
    }
}

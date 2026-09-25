package com.gtocore.common.machine.multiblock.part.voiding;

import com.gtocore.api.machine.ITagFilterMachine;
import com.gtocore.utils.Caches;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.util.prioritylist.IPartitionList;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;

import java.util.function.Predicate;

public final class TagFilterVoidOutputBusPartMachine extends VoidOutputBusPartMachine implements ITagFilterMachine, Predicate<ItemStack> {

    @Getter
    @SaveToDisk(defaultValue = "")
    private String tagWhite = "";
    @Getter
    @SaveToDisk(defaultValue = "")
    private String tagBlack = "";

    private IPartitionList filter;

    public TagFilterVoidOutputBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.HV);
    }

    @Override
    public boolean test(ItemStack stack) {
        if (filter == null) filter = Caches.getTagPriorityList(tagWhite, tagBlack);
        return filter.isListed(AEItemKey.of(stack.getItem()));
    }

    private void onSlotChanged() {
        filter = null;
        if (tagWhite.isBlank() && tagBlack.isBlank()) {
            handler.setFilter(GTUtil.FAVORABLE);
        } else {
            handler.setFilter(this);
        }
        RecipeHandlerUnit.notify(this);
    }

    @Override
    public void setTagWhite(final String tagWhite) {
        this.tagWhite = tagWhite;
        onSlotChanged();
        onChanged();
    }

    @Override
    public void setTagBlack(final String tagBlack) {
        this.tagBlack = tagBlack;
        onSlotChanged();
        onChanged();
    }

    @Override
    public boolean isItemFilter() {
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onSlotChanged();
    }

    @Override
    public Widget createUIWidget() {
        return TagFilterUI.create(this);
    }
}

package com.gtocore.common.machine.multiblock.part.voiding;

import com.gtocore.api.machine.ITagFilterMachine;
import com.gtocore.utils.Caches;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.AEKeyFilter;
import appeng.util.prioritylist.IPartitionList;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;

public final class TagFilterVoidOutputBusPartMachine extends VoidOutputBusPartMachine implements ITagFilterMachine, AEKeyFilter {

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
    public boolean matches(AEKey key) {
        if (!(key instanceof AEItemKey itemKey)) return false;
        if (filter == null) filter = Caches.getTagPriorityList(tagWhite, tagBlack);
        return filter.isListed(itemKey);
    }

    private void onSlotChanged() {
        filter = null;
        if (tagWhite.isBlank() && tagBlack.isBlank()) {
            handler.setFilter(null);
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

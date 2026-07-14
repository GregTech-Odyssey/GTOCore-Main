package com.gtocore.common.machine.multiblock.part.voiding;

import com.gtocore.api.machine.ITagFilterMachine;
import com.gtocore.utils.Caches;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.util.prioritylist.IPartitionList;

import com.gto.datasynclib.annotations.SaveToDisk;
import lombok.Getter;

import java.util.function.Predicate;

public final class TagFilterVoidOutputBusPartMachine extends VoidOutputBusPartMachine implements ITagFilterMachine, Predicate<ItemStack> {

    @Getter
    @SaveToDisk
    private String tagWhite = "";
    @Getter
    @SaveToDisk
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
    }

    @Override
    public void setTagBlack(final String tagBlack) {
        this.tagBlack = tagBlack;
        onSlotChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        onSlotChanged();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new FilterIFancyConfigurator(this));
    }
}

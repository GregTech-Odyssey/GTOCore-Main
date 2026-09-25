package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.machine.ITagFilterMachine;
import com.gtocore.utils.Caches;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;

import appeng.api.stacks.AEKey;
import appeng.util.prioritylist.IPartitionList;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class METagFilterStockBusPartMachine extends MEStockingBusPartMachine implements ITagFilterMachine {

    @SaveToDisk(defaultValue = "")
    private String tagWhite = "";
    @SaveToDisk(defaultValue = "")
    private String tagBlack = "";

    private IPartitionList filter;

    public METagFilterStockBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return MEPartUI.mainPage(this, widget, MEPartUI.page().addChildren(TagFilterUI.create(this), createUIWidget()));
    }

    @Override
    boolean test(AEKey what) {
        if (filter == null) filter = Caches.getTagPriorityList(tagWhite, tagBlack);
        return filter.isListed(what);
    }

    @Override
    protected CompoundTag writeConfigToTag() {
        CompoundTag tag = super.writeConfigToTag();
        tag.putString("TagWhite", tagWhite);
        tag.putString("TagBlack", tagBlack);
        return tag;
    }

    @Override
    protected void readConfigFromTag(CompoundTag tag) {
        super.readConfigFromTag(tag);
        if (tag.contains("TagWhite")) {
            setTagWhite(tag.getString("TagWhite"));
        }
        if (tag.contains("TagBlack")) {
            setTagBlack(tag.getString("TagBlack"));
        }
    }

    @Override
    public void setTagWhite(final String tagWhite) {
        this.tagWhite = tagWhite;
        filter = Caches.getTagPriorityList(tagWhite, tagBlack);
        onChanged();
    }

    @Override
    public void setTagBlack(final String tagBlack) {
        this.tagBlack = tagBlack;
        filter = Caches.getTagPriorityList(tagWhite, tagBlack);
        onChanged();
    }

    @Override
    public boolean isItemFilter() {
        return true;
    }

    @Override
    public String getTagWhite() {
        return this.tagWhite;
    }

    @Override
    public String getTagBlack() {
        return this.tagBlack;
    }
}

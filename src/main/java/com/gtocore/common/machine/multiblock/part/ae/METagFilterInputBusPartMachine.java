package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.machine.ITagFilterMachine;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemList;
import com.gtocore.utils.Caches;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.uipro.elements.Form;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;

import appeng.api.stacks.AEItemKey;

import com.glodblock.github.extendedae.common.me.taglist.TagPriorityList;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class METagFilterInputBusPartMachine extends MEInputBusPartMachine implements ITagFilterMachine {

    private static final long MAX_KEEP_AMOUNT = Integer.MAX_VALUE;

    @SaveToDisk(defaultValue = "")
    private String tagWhite = "";
    @SaveToDisk(defaultValue = "")
    private String tagBlack = "";
    @SaveToDisk(defaultValue = "64")
    private long keepAmount = 64;

    private TagPriorityList filter;

    public METagFilterInputBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    ExportOnlyAEItemList createInventory() {
        return new ExportOnlyAEItemList(this, CONFIG_SIZE) {

            @Override
            public boolean isAutoPull() {
                return true;
            }
        };
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return MEPartUI.mainPage(this, widget, Form.page().addChildren(TagFilterUI.create(this),
                METagFilterInput.keepAmountSection(this::getKeepAmount, this::setKeepAmount, MAX_KEEP_AMOUNT, false), createUIWidget()));
    }

    @Override
    void syncME() {
        var grid = getMainNode().getGrid();
        if (grid == null) return;
        if (filter == null) filter = Caches.getTagPriorityList(tagWhite, tagBlack);
        METagFilterInput.refreshConfigs(aeItemHandler.getInventory(), AEItemKey.class, filter, keepAmount, grid.getStorageService(), getActionSourceField());
        super.syncME();
    }

    @Override
    CompoundTag writeConfigToTag() {
        CompoundTag tag = super.writeConfigToTag();
        tag.remove("ConfigStacks");
        tag.putString("TagWhite", tagWhite);
        tag.putString("TagBlack", tagBlack);
        tag.putLong("KeepAmount", keepAmount);
        return tag;
    }

    @Override
    void readConfigFromTag(CompoundTag tag) {
        super.readConfigFromTag(tag);
        if (tag.contains("TagWhite")) {
            setTagWhite(tag.getString("TagWhite"));
        }
        if (tag.contains("TagBlack")) {
            setTagBlack(tag.getString("TagBlack"));
        }
        if (tag.contains("KeepAmount")) {
            setKeepAmount(tag.getLong("KeepAmount"));
        }
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        ITagFilterMachine.super.saveToItem(tag);
        tag.putLong("KeepAmount", keepAmount);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        ITagFilterMachine.super.loadFromItem(tag);
        if (tag.contains("KeepAmount")) {
            setKeepAmount(tag.getLong("KeepAmount"));
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

    private long getKeepAmount() {
        return keepAmount;
    }

    private void setKeepAmount(long keepAmount) {
        this.keepAmount = Math.clamp(keepAmount, 1, MAX_KEEP_AMOUNT);
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

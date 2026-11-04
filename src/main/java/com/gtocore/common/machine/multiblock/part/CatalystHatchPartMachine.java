package com.gtocore.common.machine.multiblock.part;

import com.gtolib.api.recipe.extension.CatalystRecipeExtension;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeStackAdapter;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CatalystHatchPartMachine extends WorkableTieredIOPartMachine {

    @SaveToDisk
    private final NotifiableStackInventory inventory;

    public CatalystHatchPartMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.IN);
        this.inventory = CatalystRecipeExtension.createInventory(this, tier == 2 ? 4 : 36, true);
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    @Override
    public Widget createUIWidget() {
        int count = tier == 2 ? 4 : 36;
        var handler = new ForgeStackAdapter(inventory.storage);
        return HatchViews.page(SlotGrid.square(count, index -> new SlotWidget(handler, index, 0, 0, true, io.support(IO.IN))
                .setBackgroundTexture(GuiTextures.SLOT)
                .setIngredientIO(this.io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT)));
    }

    @Override
    public boolean canShared() {
        return false;
    }
}

package com.gtocore.common.machine.multiblock.part;

import com.gtolib.api.machine.trait.NotifiableConnectingRodHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.ZPM;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ConnectingRodHatch extends WorkableTieredIOPartMachine {

    @SaveToDisk
    private final NotifiableConnectingRodHandler inventory;

    public ConnectingRodHatch(MetaMachineBlockEntity holder) {
        super(holder, ZPM, IO.IN);
        this.inventory = new NotifiableConnectingRodHandler(this);
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    @Override
    public Widget createUIWidget() {
        return HatchViews.page(SlotGrid.square(1, index -> new SlotWidget(inventory.storage, 0, 0, 0, true, io.support(IO.IN))
                .setBackgroundTexture(GuiTextures.SLOT).setIngredientIO(this.io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT)));
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Nullable
    public Material getRodMaterial() {
        return inventory.getRodMaterial();
    }
}

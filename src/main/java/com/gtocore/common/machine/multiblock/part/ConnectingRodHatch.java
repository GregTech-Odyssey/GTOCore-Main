package com.gtocore.common.machine.multiblock.part;

import com.gtolib.api.machine.trait.NotifiableConnectingRodHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.recipe.handler.IO;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
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
        int rowSize = 1;
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * rowSize + 16);
        var container = new WidgetGroup(4, 4, 18 * rowSize + 8, 18 * rowSize + 8);
        container.addWidget(new SlotWidget(inventory.storage, 0, 4, 4, true, io.support(IO.IN))
                .setBackgroundTexture(GuiTextures.SLOT).setIngredientIO(this.io == IO.IN ? IngredientIO.INPUT : IngredientIO.OUTPUT));
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
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

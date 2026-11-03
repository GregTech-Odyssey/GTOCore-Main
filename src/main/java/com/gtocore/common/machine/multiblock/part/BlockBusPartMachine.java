package com.gtocore.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.MenuItemAdapter;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class BlockBusPartMachine extends MultiblockPartMachine implements IMachineLife {

    @SaveToDisk
    private final NotifiableInventory<AEItemKey> inventory;

    public BlockBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
        inventory = NotifiableInventory.items(this, 100, IO.NONE, IO.BOTH);
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }

    @Override
    public Widget createUIWidget() {
        var handler = new MenuItemAdapter(inventory.storage);
        return HatchViews.page(SlotGrid.square(100, index -> new SlotWidget(handler, index, 0, 0, true, true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setIngredientIO(IngredientIO.INPUT)));
    }

    @Override
    public boolean canShared() {
        return false;
    }
}

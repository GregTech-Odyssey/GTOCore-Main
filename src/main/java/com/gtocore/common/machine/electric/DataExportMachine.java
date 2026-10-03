package com.gtocore.common.machine.electric;

import com.gtocore.api.research.ui.RecipeExportTab;

import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class DataExportMachine extends WorkableTieredMachine implements ICustomRecipeLogicHolder, RecipeExportTab.DataItemHolder {

    private ItemStack in;
    private ItemStack out;

    public DataExportMachine(MetaMachineBlockEntity holder) {
        super(holder, IV, t -> 8000);
    }

    @Override
    protected NotifiableInventory<AEItemKey> createImportItemHandler(Object... args) {
        return NotifiableInventory.items(this, getRecipeType().getMaxInputs(ItemRecipeInfo.INSTANCE), IO.IN, IO.BOTH);
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (in == null || out == null) {
            return null;
        }
        var r = RecipeBuilder.ofRaw().duration(600).EUt(VA[IV]).inputItems(in).outputItems(out)
                .build();
        in = null;
        out = null;
        return r;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return MachineWindow.createUI(new RecipeExportTab(this), this, entityPlayer);
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public KeyInventory<AEItemKey> getDataItemStorage() {
        return importItems.storage;
    }

    @Override
    public KeyInventory<AEItemKey> getDataOutputStorage() {
        return exportItems.storage;
    }

    @Override
    public void exportSelectedRecipe(ItemStack dataStack, GTRecipeDefinition recipe) {
        in = dataStack.copy();
        ResearchManager.writeResearchToNBT(dataStack.getOrCreateTag(), recipe.id.toString(), recipe.recipeType);
        out = dataStack;
        getRecipeLogic().updateTickSubscription();
    }
}

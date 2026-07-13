package com.gtocore.common.machine.electric;

import com.gtocore.api.research.ui.RecipeExportTab;

import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class DataExportMachine extends WorkableTieredMachine implements ICustomRecipeLogicHolder, RecipeExportTab.DataItemHolder {

    private ItemStack in;
    private ItemStack out;

    public DataExportMachine(MetaMachineBlockEntity holder) {
        super(holder, IV, t -> 8000);
    }

    @Override
    protected NotifiableItemStackHandler createImportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, getRecipeType().getMaxInputs(ItemRecipeInfo.INSTANCE), IO.IN, IO.BOTH);
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (in == null || out == null) {
            return null;
        }
        var r = RecipeBuilder.ofRaw().duration(100).EUt(VA[EV]).inputItems(in).outputItems(out)
                .build();
        in = null;
        out = null;
        return r;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer).widget(new FancyMachineUIWidget(new RecipeExportTab(this), 176, 166));
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public ICustomItemStackHandler getDataItemStorage() {
        return importItems;
    }

    @Override
    public ICustomItemStackHandler getDataOutputStorage() {
        return exportItems;
    }

    @Override
    public void exportSelectedRecipe(ItemStack dataStack, GTRecipeDefinition recipe) {
        in = dataStack.copy();
        ResearchManager.writeResearchToNBT(dataStack.getOrCreateTag(), recipe.id.toString(), recipe.recipeType);
        out = dataStack;
        getRecipeLogic().updateTickSubscription();
    }
}

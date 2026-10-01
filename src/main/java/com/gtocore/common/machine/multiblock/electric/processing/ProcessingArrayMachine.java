package com.gtocore.common.machine.multiblock.electric.processing;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.item.MachineItemStackHandler;
import com.gtolib.api.machine.feature.multiblock.IArrayMachine;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ProcessingArrayMachine extends TierCasingMultiblockMachine implements IParallelMachine, IArrayMachine {

    @RegisterLanguage(cn = "未放入机器", en = "No machine inserted")
    static final String NO_MACHINE = "gtocore.machine.processing_array.no_machine";

    private MachineDefinition machineDefinitionCache;
    @Getter
    @SyncToClient
    @SaveToDisk
    private final NotifiableItemStackHandler inventory;
    private final int arrayTier;

    public ProcessingArrayMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, GTORecipeDataKeys.GLASS_TIER);
        this.arrayTier = tier;
        inventory = createMachineStorage();
    }

    private NotifiableItemStackHandler createMachineStorage() {
        NotifiableItemStackHandler storage = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH, slots -> new MachineItemStackHandler(() -> getMachineLimit(arrayTier)));
        storage.setFilter(i -> storageFilter(i, getCasingTier(GTORecipeDataKeys.GLASS_TIER)));
        storage.addChangedListener(this::onStorageChanged);
        return storage;
    }

    static boolean storageFilter(ItemStack itemStack, int tier) {
        if (itemStack.getItem() instanceof MetaMachineItem metaMachineItem) {
            MachineDefinition definition = metaMachineItem.getDefinition();
            if (definition instanceof MultiblockMachineDefinition) return false;
            if (definition.getTier() > tier) return false;
            GTRecipeType[] recipeTypes = definition.getRecipeTypes();
            if (recipeTypes != null && recipeTypes.length == 1) {
                return GTRecipeTypes.ELECTRIC.equals(recipeTypes[0].group);
            }
        }
        return false;
    }

    @Override
    public int getTier() {
        MachineDefinition definition = getMachineDefinition();
        return Math.min(definition == null ? 0 : definition.getTier(), tier);
    }

    @Override
    @Nullable
    public GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (!inventory.getStackInSlot(0).isEmpty()) {
            recipe = ParallelLogic.accurateParallel(this, unit, recipe, getMaxParallel());
            if (recipe == null) return null;
            return super.getRealRecipe(unit, recipe);
        }
        IdleReason.MACHINE_STORAGE_EMPTY.report(this, IssueStage.MODIFIER, recipe.definition);
        return null;
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (!MultiblockPage.isScreenText()) MachineUtils.addRecipeTypeText(textList, this);
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading("gtceu.gui.machinemode", MultiblockPage.recipeTypeText(this, NO_MACHINE));
    }

    public static Block getCasingState(int tier) {
        return tier == GTValues.IV ? GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get() : GTBlocks.CASING_HSSE_STURDY.get();
    }

    public static int getMachineLimit(Integer tier) {
        return tier == GTValues.IV ? 16 : 64;
    }

    @Override
    public long getMaxParallel() {
        return Math.clamp(inventory.getStackInSlot(0).getCount(), 1, getMachineLimit(arrayTier));
    }

    @Override
    public long getMinParallel() {
        return IParallelMachine.MIN_PARALLEL;
    }

    @Override
    public Item getStorageItem() {
        return inventory.getStackInSlot(0).getItem();
    }

    @Override
    public void setMachineDefinitionCache(final MachineDefinition machineDefinitionCache) {
        this.machineDefinitionCache = machineDefinitionCache;
    }

    @Override
    public MachineDefinition getMachineDefinitionCache() {
        return this.machineDefinitionCache;
    }
}

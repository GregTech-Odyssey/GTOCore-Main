package com.gtocore.common.machine.multiblock.electric.voidseries;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOOres;
import com.gtocore.common.item.DimensionDataItem;
import com.gtocore.data.IdleReason;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.issue.GTIssues;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.EURecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTMaterials.DrillingFluid;
import static net.minecraft.network.chat.Component.translatable;

public final class VoidMinerMachine extends StorageMultiblockMachine implements ICustomRecipeLogicHolder, IIssueProvider {

    private ResourceKey<Level> dim;

    public VoidMinerMachine(MetaMachineBlockEntity holder) {
        super(holder, 1, i -> i.is(GTOItems.DIMENSION_DATA.get()) && i.hasTag());
    }

    @Override
    public void onMachineChanged() {
        dim = null;
        if (isEmpty()) return;
        dim = GTODimensions.getDimensionKey(DimensionDataItem.getDimension(getStorageStack()));
        if (GTOOres.ALL_ORES.containsKey(dim)) {
            getRecipeLogic().updateTickSubscription();
            return;
        }
        dim = null;
    }

    private ItemStack[] getItems() {
        ItemStack[] stacks = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            stacks[i] = new ItemStack(ChemicalHelper.getItem(TagPrefix.rawOre, GTOOres.selectMaterial(dim)), (int) Math.pow(getTier() - 3, GTValues.RNG.nextDouble() + 1));
        }
        return stacks;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        onMachineChanged();
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (dim != null && isFormed() && !getStorageStack().isEmpty()) {
            textList.add(translatable("gtceu.multiblock.ore_rig.drilled_ores_list"));
            GTOOres.ALL_ORES.get(dim).forEach((mat, i) -> textList.add(mat.getLocalizedName().append("x" + i)));
        }
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (dim == null || isEmpty()) {
            if (machineStorage.updateEmpty()) IdleReason.DIMENSION_DATA_MISSING.report(this);
            else IdleReason.NO_ORES.report(this);
            return null;
        }
        if (getTier() <= 3) {
            reportIssue(GTIssues.LOW_VOLTAGE, null, IO.IN, EURecipeInfo.INSTANCE, -1, GTValues.EV, getTier(), null);
            return null;
        }
        if (unit.matchFluid(DrillingFluid.getFluid(), 1000)) {
            var builder = getRecipeBuilder();
            builder.EUt(GTValues.VA[getTier()]);
            builder.inputFluids(DrillingFluid.getFluid(), 1000);
            builder.outputItems(getItems());
            return builder.build();
        }
        reportIssue(GTIssues.INPUT_SHORT, null, IO.IN, FluidRecipeInfo.INSTANCE, -1, 1000, -1, null);
        return null;
    }

    @Override
    public void collectIssues(IssueSink sink) {
        if (!isFormed() || dim != null) return;
        if (machineStorage.updateEmpty()) IdleReason.DIMENSION_DATA_MISSING.collect(sink);
        else IdleReason.NO_ORES.collect(sink);
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public String getStorageSlotLabel() {
        return IStorageMultiblock.SLOT_DIMENSION_DATA;
    }

    @Override
    public ItemStack[] getStorageSlotGhosts() {
        return new ItemStack[] { GTOItems.DIMENSION_DATA.asStack() };
    }
}

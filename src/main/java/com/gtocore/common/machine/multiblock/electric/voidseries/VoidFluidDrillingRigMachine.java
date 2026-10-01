package com.gtocore.common.machine.multiblock.electric.voidseries;

import com.gtocore.common.data.GTOBedrockFluids;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.DimensionDataItem;
import com.gtocore.data.IdleReason;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.api.machine.impl.DrillingControlCenterMachine;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;
import com.gtolib.api.machine.trait.IFluidDrillLogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.issue.GTIssues;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.EURecipeInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.common.data.GTItems.PROGRAMMED_CIRCUIT;
import static net.minecraft.network.chat.Component.translatable;

public final class VoidFluidDrillingRigMachine extends StorageMultiblockMachine implements IFluidDrillLogic, ICustomRecipeLogicHolder, IIssueProvider {

    private List<FluidStack> fluidStacks;
    private DrillingControlCenterMachine cache;

    public VoidFluidDrillingRigMachine(MetaMachineBlockEntity holder) {
        super(holder, 1, i -> i.is(GTOItems.DIMENSION_DATA.get()) && i.hasTag());
    }

    @Override
    public void onMachineChanged() {
        fluidStacks = GTOBedrockFluids.ALL_BEDROCK_FLUID.get(GTODimensions.getDimensionKey(DimensionDataItem.getDimension(getStorageStack())));
        if (fluidStacks == null) return;
        getRecipeLogic().updateTickSubscription();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        onMachineChanged();
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (fluidStacks != null && isFormed() && !getStorageStack().isEmpty()) {
            textList.add(translatable("gui.ae2.Fluids").append(":"));
            fluidStacks.forEach(f -> textList.add(f.getFluid().getFluidType().getDescription().copy().append("x" + f.getAmount())));
        }
    }

    @Override
    public DrillingControlCenterMachine getNetMachineCache() {
        return cache;
    }

    @Override
    public void setNetMachineCache(DrillingControlCenterMachine cache) {
        this.cache = cache;
    }

    @Override
    public MetaMachine getMachine() {
        return this;
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (fluidStacks == null) {
            if (machineStorage.updateEmpty()) IdleReason.DIMENSION_DATA_MISSING.report(this);
            else IdleReason.NO_BEDROCK_FLUIDS.report(this);
            return null;
        }
        if (getOverclockVoltage() > VA[GTValues.LuV] && !isEmpty()) {
            if (unit.matchItem(PROGRAMMED_CIRCUIT.asItem(), 1)) {
                var builder = getRecipeBuilder();
                builder.EUt(VA[getTier()]);
                FluidStack fluidStack = fluidStacks.get(Math.min(fluidStacks.size() - 1, unit.getCircuit(false))).copy();
                int amount = fluidStack.getAmount() * (1 << getTier() - 2);
                var machine = getNetMachine();
                if (machine != null) amount = (int) (amount * machine.getMultiplier());
                fluidStack.setAmount(amount);
                builder.outputFluids(fluidStack);
                return builder.build();
            }
            IdleReason.SET_CIRCUIT.report(this);
        } else if (!machineStorage.updateEmpty()) {
            reportIssue(GTIssues.LOW_VOLTAGE, null, IO.IN, EURecipeInfo.INSTANCE, -1, GTValues.ZPM, getTier(), null);
        }
        return null;
    }

    @Override
    public void collectIssues(IssueSink sink) {
        if (!isFormed() || fluidStacks != null) return;
        if (machineStorage.updateEmpty()) IdleReason.DIMENSION_DATA_MISSING.collect(sink);
        else IdleReason.NO_BEDROCK_FLUIDS.collect(sink);
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

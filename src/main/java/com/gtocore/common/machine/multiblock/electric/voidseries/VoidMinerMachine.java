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
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.GTMaterials.DrillingFluid;
import static net.minecraft.network.chat.Component.translatable;

public final class VoidMinerMachine extends StorageMultiblockMachine implements ICustomRecipeLogicHolder {

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
        if (dim == null) {
            (isEmpty() ? IdleReason.DIMENSION_DATA_MISSING : IdleReason.NO_ORES).setReason(this);
            return null;
        }
        if (!isEmpty() && getTier() > 3) {
            if (unit.matchFluid(DrillingFluid.getFluid(), 1000)) {
                var builder = getRecipeBuilder();
                builder.EUt(GTValues.VA[getTier()]);
                builder.inputFluids(DrillingFluid.getFluid(), 1000);
                builder.outputItems(getItems());
                return builder.build();
            }
            setIdleReason(ActionResult.failInsufficientIn(FluidRecipeInfo.INSTANCE.getName()));
        } else if (isEmpty()) {
            IdleReason.DIMENSION_DATA_MISSING.setReason(this);
        } else {
            IdleReason.VOLTAGE_TIER_NOT_SATISFIES.setReason(this, GTValues.VN[GTValues.EV], GTValues.VN[getTier()]);
        }
        return null;
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

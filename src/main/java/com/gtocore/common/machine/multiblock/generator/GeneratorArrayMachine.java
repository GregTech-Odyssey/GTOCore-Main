package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.config.GTORules;

import com.gtolib.api.capability.IExtendWirelessEnergyContainerHolder;
import com.gtolib.api.machine.feature.multiblock.IArrayMachine;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;
import com.gtolib.utils.GTOUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.RecipeInfo;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.hepdd.gtmthings.api.misc.WirelessEnergyContainer;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class GeneratorArrayMachine extends StorageMultiblockMachine implements IArrayMachine, IExtendWirelessEnergyContainerHolder {

    private WirelessEnergyContainer WirelessEnergyContainerCache;
    private MachineDefinition machineDefinitionCache;
    @SaveToDisk(defaultValue = "false")
    private boolean isw;
    @SaveToDisk(defaultValue = "0")
    private long eut;

    private static boolean isEligibleRecipeType(GTRecipeType type) {
        return Wrapper.ELIGIBLE_RECIPE_TYPES.contains(type);
    }

    public GeneratorArrayMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORules.GENERATOR_ARRAY_LIMIT.get(), GeneratorArrayMachine::filter);
    }

    private static boolean filter(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaMachineItem metaMachineItem) {
            MachineDefinition definition = metaMachineItem.getDefinition();
            if (definition instanceof MultiblockMachineDefinition) {
                return false;
            }
            var recipeTypes = definition.getRecipeTypes();
            if (recipeTypes == null) {
                return false;
            }
            for (GTRecipeType type : recipeTypes) {
                if (isEligibleRecipeType(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onMachineChanged() {
        onStorageChanged();
    }

    @Override
    public int getTier() {
        MachineDefinition definition = getMachineDefinition();
        int definitionTier = definition == null ? 0 : definition.getTier();
        if (isw) {
            return definitionTier;
        }
        return Math.min(definitionTier, tier);
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (isw) {
            if (eut > 0) {
                var container = getWirelessEnergyContainer();
                if (container != null) {
                    int loss = container.getLoss();
                    container.setLoss(loss + GTORules.GENERATOR_ARRAY_LOSS.get() * 10);
                    container.addEnergy(eut, this);
                    container.setLoss(loss);
                }
            } else {
                return false;
            }
        } else {
            return super.handleTickRecipe(recipe);
        }
        return true;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeInfo capability) {
        return true;
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (isEmpty()) return null;
        int a = machineStorage.storage.getStackInSlot(0).getCount();
        if (a > 0) {
            long EUt = recipe.getOutputEUt();
            if (EUt > 0) {
                recipe.itemOutputs = Collections.emptyList();
                recipe.fluidOutputs = Collections.emptyList();
                recipe = ParallelLogic.accurateContentParallel(this, unit, recipe, (long) (GTORules.GENERATOR_ARRAY_MULTIPLY.get() * GTValues.V[getOverclockTier()] * a * GTOUtils.getGeneratorAmperage(getTier()) / EUt));
                if (recipe == null) return null;
                recipe.duration = recipe.duration * GTOUtils.getGeneratorEfficiency(recipe.definition.recipeType, getTier()) / 100;
                if (isw) {
                    recipe.setEUt(0);
                    eut = EUt * recipe.parallels;
                }
                return recipe;
            }
        }
        return null;
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable("gtocore.machine.generator_array.wireless").append(ComponentPanelWidget.withButton(Component.literal("[").append(isw ? Component.translatable("gtocore.machine.on") : Component.translatable("gtocore.machine.off")).append(Component.literal("]")), "wireless_switch")));
        if (isActive() && isw) {
            GTRecipe r = getRecipeLogic().getLastRecipe();
            if (r != null) {
                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick", FormattingUtil.formatNumbers(eut), Component.literal(GTValues.VNF[GTUtil.getFloorTierByVoltage(eut)])));
            }
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if ("wireless_switch".equals(componentData)) {
                isw = !isw;
                eut = 0;
                requestCheck();
            } else super.handleDisplayClick(componentData, clickData);
        }
    }

    @Override
    public Item getStorageItem() {
        return getStorageStack().getItem();
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public boolean matchRecipeOutput(GTRecipe recipe) {
        return true;
    }

    @Override
    public boolean matchTickRecipe(GTRecipe recipe) {
        return isw || super.matchTickRecipe(recipe);
    }

    private static class Wrapper {

        private static final Set<GTRecipeType> ELIGIBLE_RECIPE_TYPES = Set.of(GTRecipeTypes.STEAM_TURBINE_FUELS, GTRecipeTypes.GAS_TURBINE_FUELS, GTRecipeTypes.COMBUSTION_GENERATOR_FUELS, GTORecipeTypes.SEMI_FLUID_GENERATOR_FUELS, GTORecipeTypes.ROCKET_ENGINE_FUELS, GTORecipeTypes.NAQUADAH_REACTOR);
    }

    public static double getMultiply() {
        return GTORules.GENERATOR_ARRAY_MULTIPLY.get();
    }

    @Override
    public void setWirelessEnergyContainerCache(final WirelessEnergyContainer WirelessEnergyContainerCache) {
        this.WirelessEnergyContainerCache = WirelessEnergyContainerCache;
    }

    @Override
    public WirelessEnergyContainer getWirelessEnergyContainerCache() {
        return this.WirelessEnergyContainerCache;
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

package com.gtocore.common.machine.electric;

import com.gtocore.api.research.ExResearchManager;
import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.recipe.ScanningRecipeExtion;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.common.item.DataCrystalItem;

import com.gtolib.GTOCore;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.misc.PlanetManagement;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.util.holder.ObjHolder;
import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ScannerMachine extends SimpleTieredMachine implements ICustomRecipeLogicHolder {

    public ScannerMachine(MetaMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public @Nullable GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit u) {
        var team = TeamUtil.getTeamUUID(getOwnerUUID());
        RecipeData data = new RecipeData();
        ObjHolder<GTRecipeDefinition> recipeObjectHolder = new ObjHolder<>();
        data.dataCrystal = ItemStack.EMPTY;
        data.item = ItemStack.EMPTY;
        data.team = team;
        u.forEachItems(false, (stack, amount) -> {
            var item = stack.getItem();
            var isMold = item instanceof DataCrystalItem;
            if (isMold && data.dataCrystal.isEmpty()) {
                data.dataCrystal = stack;
            } else if (!isMold && data.item.isEmpty()) {
                data.item = stack;
            }
            if (data.found()) {
                var recipe = data.buildRecipe();
                if (recipe != null) {
                    recipeObjectHolder.value = recipe;
                    return true;
                }
            }
            return false;
        });
        if (data.fluidStack.isEmpty()) {
            u.forEachFluids(false, (stack, amount) -> {
                if (data.fluidStack.isEmpty() && amount >= 1000) {
                    data.fluidStack = stack;
                }
                if (data.found()) {
                    var recipe = data.buildRecipe();
                    if (recipe != null) {
                        recipeObjectHolder.value = recipe;
                        return true;
                    }
                }
                return false;
            });
        }
        return recipeObjectHolder.value;
    }

    @Override
    public void afterWorking() {
        forEachItems(true, (stack, amount) -> {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                String planet = tag.getString("planet");
                if (!planet.isEmpty()) {
                    UUID uuid = tag.getUUID("uuid");
                    var dim = GTODimensions.getDimensionKey(RLUtils.parse(planet));
                    if (PlanetManagement.isUnlocked(uuid, dim)) return false;
                    PlanetManagement.unlock(uuid, dim);
                    ExResearchManager.triggerPlanetaryResearch(uuid, dim);
                    stack.setCount(0);
                    return true;
                }
            }
            return false;
        });
        super.afterWorking();
    }

    @Override
    public boolean searchRecipe() {
        return true;
    }

    private static final class RecipeData {

        private final RecipeBuilder recipeBuilder = RecipeBuilder.ofRaw();
        private UUID team;

        private ItemStack dataCrystal = ItemStack.EMPTY;
        private ItemStack item = ItemStack.EMPTY;
        private FluidStack fluidStack = FluidStack.EMPTY;

        private boolean found() {
            return !dataCrystal.isEmpty() && (!item.isEmpty() || !fluidStack.isEmpty()) && team != null;
        }

        private GTRecipeDefinition buildRecipe() {
            var input = dataCrystal.copyWithCount(1);
            var output = dataCrystal.copyWithCount(1);
            boolean istem = !item.isEmpty();
            if (istem) {
                var c = DataScanningManager.scanData(item.getItem(), team, true);
                if (c.isEmpty() && ResearchRequirements.getEurekaRequirements(AEItemKey.of(item.getItem())).isEmpty()) {
                    return null;
                }
                DataCrystalItem.setTeamUUID(output, team);
                var bytesScanned = c.countBytes();
                return recipeBuilder.inputItems(input)
                        .inputItems(item.copyWithCount(1))
                        .duration(200 * GTOCore.difficulty).EUt(eut(bytesScanned))
                        .addExtension(ScanningRecipeExtion.INSTANCE)
                        .addData(ScanningRecipeExtion.INSTANCE, ScanningRecipeExtion.create(AEItemKey.of(item.getItem()), output, team))
                        .build();
            } else {
                var c = DataScanningManager.scanData(fluidStack.getFluid(), team, true);
                if (c.isEmpty() && ResearchRequirements.getEurekaRequirements(AEFluidKey.of(fluidStack.getFluid())).isEmpty()) {
                    return null;
                }
                DataCrystalItem.setTeamUUID(output, team);
                var bytesScanned = c.countBytes();
                return recipeBuilder.inputItems(input)
                        .inputFluids(fluidStack.getFluid(), 1000)
                        .duration(200 * GTOCore.difficulty).EUt(eut(bytesScanned))
                        .addExtension(ScanningRecipeExtion.INSTANCE)
                        .addData(ScanningRecipeExtion.INSTANCE, ScanningRecipeExtion.create(AEFluidKey.of(fluidStack.getFluid()), output, team))
                        .build();
            }
        }
    }

    public static long eut(long bytesScanned) {
        return 8 * bytesScanned + 8;
    }
}

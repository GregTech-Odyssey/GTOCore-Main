package com.gtocore.common.machine.electric;

import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.common.item.DataCrystalItem;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import com.gto.datasynclib.util.holder.ObjHolder;
import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@DataGeneratorScanned
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
            var output = dataCrystal.copyWithCount(1);
            boolean istem = !item.isEmpty();
            if (istem) {
                var c = DataScanningManager.scanData(item.getItem(), team, true);
                if (c.isEmpty() && ResearchRequirements.getEurekaRequirements(AEItemKey.of(item.getItem())).isEmpty()) {
                    return null;
                }
                if (!DataCrystalItem.setDataCrystalData(output, team, c)) {
                    return null;
                }
                var bytesScanned = c.countBytes();
                DataScanningManager.scanData(item.getItem(), team, false);
                return recipeBuilder.inputItems(dataCrystal.getItem())
                        .inputItems(item.copyWithCount(1))
                        .outputItems(output)
                        .duration(200 * GTOCore.difficulty).EUt(eut(bytesScanned))
                        .build();
            } else {
                var c = DataScanningManager.scanData(fluidStack.getFluid(), team, true);
                if (c.isEmpty() && ResearchRequirements.getEurekaRequirements(AEFluidKey.of(fluidStack.getFluid())).isEmpty()) {
                    return null;
                }
                if (!DataCrystalItem.setDataCrystalData(output, team, c)) {
                    return null;
                }
                var bytesScanned = c.countBytes();
                DataScanningManager.scanData(fluidStack.getFluid(), team, false);
                return recipeBuilder.inputItems(dataCrystal.getItem())
                        .inputFluids(fluidStack.getFluid(), 1000)
                        .outputItems(output)
                        .duration(200 * GTOCore.difficulty).EUt(eut(bytesScanned))
                        .build();
            }
        }
    }

    public static long eut(long bytesScanned) {
        return 480 * ((long) (Math.cbrt(bytesScanned) + 1)) + 8;
    }

    @RegisterLanguage(cn = "待扫描物品", en = "Item to Scan")
    public static final String ITEM_TO_SCAN = "gtocore.tooltip.recipe.item_to_scan";
}

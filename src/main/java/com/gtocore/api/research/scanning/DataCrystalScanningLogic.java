package com.gtocore.api.research.scanning;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.DataCrystalItem;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;

import com.gto.datasynclib.util.holder.ObjHolder;
import com.hepdd.gtmthings.utils.TeamUtil;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@DataGeneratorScanned
public class DataCrystalScanningLogic implements GTRecipeType.ICustomRecipeLogic {

    @RegisterLanguage(cn = "待扫描物品", en = "Item to Scan")
    public static final String ITEM_TO_SCAN = "gtocore.tooltip.recipe.item_to_scan";

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
                if (c.isEmpty() || !DataCrystalItem.setDataCrystalData(output, team, c)) {
                    return null;
                }
                var bytesScanned = c.countBytes();
                var EUt = 120 * ((long) (Math.sqrt(bytesScanned) + 1));
                DataScanningManager.scanData(item.getItem(), team, false);
                return recipeBuilder.inputItems(dataCrystal.getItem())
                        .inputItems(item.copyWithCount(1))
                        .outputItems(output)
                        .duration(200 * GTOCore.difficulty).EUt(EUt)
                        .build();
            } else {
                var c = DataScanningManager.scanData(fluidStack.getFluid(), team, true);
                if (c.isEmpty() || !DataCrystalItem.setDataCrystalData(output, team, c)) {
                    return null;
                }
                var bytesScanned = c.countBytes();
                var EUt = 120 * ((long) (Math.sqrt(bytesScanned) + 1));
                DataScanningManager.scanData(fluidStack.getFluid(), team, false);
                return recipeBuilder.inputItems(dataCrystal.getItem())
                        .inputFluids(fluidStack.getFluid(), 1000)
                        .outputItems(output)
                        .duration(200 * GTOCore.difficulty).EUt(EUt)
                        .build();
            }
        }
    }

    @Override
    public @Nullable GTRecipeDefinition createCustomRecipe(IRecipeHandlerHolder h, RecipeHandlerUnit u) {
        var team = TeamUtil.getTeamUUID(h.self().getOwnerUUID());
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
    public void buildRepresentativeRecipes() {
        ItemStack press = GTOItems.DATA_CRYSTAL_COMPONENT_MK1.asStack();
        ItemStack toName = new ItemStack(Items.DIRT);
        toName.setHoverName(Component.translatable(ITEM_TO_SCAN));
        ItemStack named = press.copy();
        DataCrystalItem.addResearchData(named, ResearchTag.MATERIAL, 1);
        GTRecipeDefinition recipe = GTRecipeTypes.SCANNER_RECIPES.recipeBuilder("name_item")
                .inputItems(press)
                .inputItems(toName)
                .outputItems(named)
                .duration(200 * GTOCore.difficulty)
                .EUt(120 * ((long) (Math.sqrt(ResearchTag.MATERIAL.getBytePerPoint()) + 1)))
                .build();
        GTRecipeTypes.SCANNER_RECIPES.addToMainCategory(recipe);
    }
}

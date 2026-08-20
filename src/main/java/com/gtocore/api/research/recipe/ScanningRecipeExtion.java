package com.gtocore.api.research.recipe;

import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.common.item.DataCrystalItem;

import com.gtolib.api.annotation.DataGeneratorScanned;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;

import com.fast.recipesearch.IntLongMap;
import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@DataGeneratorScanned
public class ScanningRecipeExtion extends RecipeExtension<ScanningRecipeExtion.AEKeyDataCrystal> {

    /** AEKeyDataCrystal 的网络+持久化编解码器（composite 组合，注册在 GTOCodecs.init()）。 */
    public static final DataSyncCodec<AEKeyDataCrystal> AEKEYDATACRYSTAL_CODEC = CombinedCodec.composite(
            GTOCodecs.KEY_COUNTER_SYNC_CODEC, AEKeyDataCrystal::aeKeys,
            DataSyncCodec.ITEM_STACK_CODEC, AEKeyDataCrystal::dataCystal,
            DataSyncCodec.UUID_CODEC, AEKeyDataCrystal::team,
            AEKeyDataCrystal::new);

    public static final ScanningRecipeExtion INSTANCE = new ScanningRecipeExtion("scanning_recipe");

    public ScanningRecipeExtion(String name) {
        super(name, AEKEYDATACRYSTAL_CODEC, false);
    }

    @Override
    public boolean handleOutput(@NotNull IRecipeHandlerHolder holder, @NotNull GTRecipe recipe, boolean simulate) {
        var aeKeyDataCrystal = recipe.data.getData(INSTANCE);
        if (aeKeyDataCrystal == null) {
            return false;
        }
        ItemStack dataCrystal = aeKeyDataCrystal.dataCystal().copy();
        var aeKeys = aeKeyDataCrystal.aeKeys();
        UUID team = aeKeyDataCrystal.team();
        for (var entry : aeKeys) {
            DataCrystalItem.addResearchData(dataCrystal, DataScanningManager.scanData(entry.getKey(), team, entry.getLongValue(), simulate));
        }
        if (simulate) {
            return holder.simulateOutputItem(dataCrystal);
        } else {
            holder.outputItem(dataCrystal);
        }
        return true;
    }

    @Override
    public void extractInput(GTRecipeDefinition recipe, IntLongMap map) {}

    @Override
    public long getParallel(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipe recipe, long parallel) {
        return 0;
    }

    @Override
    public void setParallel(GTRecipe recipe, long parallel) {}

    @Override
    public void addInfo(GTRecipeDefinition recipe, WidgetGroup group, int xOffset, MutableInt yOffset) {}

    @Override
    public int getInfoHeight(GTRecipeDefinition recipe) {
        return 0;
    }

    public record AEKeyDataCrystal(KeyCounter aeKeys, ItemStack dataCystal, UUID team) {}

    public static AEKeyDataCrystal create(KeyCounter aeKeys, ItemStack dataCystal, UUID team) {
        return new AEKeyDataCrystal(aeKeys, dataCystal, team);
    }

    public static AEKeyDataCrystal create(AEKey aeKey, ItemStack dataCystal, UUID team) {
        var keyCounter = new KeyCounter();
        keyCounter.add(aeKey, 1);
        return new AEKeyDataCrystal(keyCounter, dataCystal, team);
    }
}

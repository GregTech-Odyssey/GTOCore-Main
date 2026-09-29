package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.machine.multiblock.part.SpoolHatchPartMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.CoilMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@DataGeneratorScanned
public final class DrawingTowerMachine extends CoilMultiblockMachine {

    @RegisterLanguage(cn = "拉丝段层数", en = "Drawing Layers")
    public static final String LAYERS_NAME = "gtocore.multiblock.drawing_tower.layers";
    @RegisterLanguage(cn = "塔基与塔顶之间的拉丝段层数", en = "Number of drawing layers between the base and the top")
    public static final String LAYERS_DESC = "gtocore.multiblock.drawing_tower.layers.desc";
    public static final ParamKey LAYERS = ParamKey.of(LAYERS_NAME, LAYERS_DESC);

    private SpoolHatchPartMachine spoolHatchPartMachine;

    private int height;

    private double reduction = 1;

    private int parallels = 1;

    public DrawingTowerMachine(MetaMachineBlockEntity holder) {
        super(holder, false, false);
    }

    @Override
    public void onPartScan(IMultiPart part) {
        super.onPartScan(part);
        if (spoolHatchPartMachine == null && part instanceof SpoolHatchPartMachine spoolHatchPart) {
            spoolHatchPartMachine = spoolHatchPart;
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        height = structureParam(LAYERS);
        reduction = Math.max(0.00001, 2 / Math.pow(1.2, ((height / 8D) * ((getTemperature() - 5000D) / 900D))));
        parallels = (getTemperature() <= 10000) ? 1 : (int) Math.round(Math.log(getTemperature() - 9600) / Math.log(1.08) - 84);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        height = 0;
        spoolHatchPartMachine = null;
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (spoolHatchPartMachine == null) return null;
        CustomItemStackHandler storage = spoolHatchPartMachine.getInventory().storage;
        ItemStack item = storage.getStackInSlot(0);
        int tier = getItemTier(item);

        // Check if the item is a valid spool and matches the required tier in the recipe
        if (tier == recipe.data.getInt(GTORecipeDataKeys.SPOOL)) {
            // Decrease the item count instead of increasing damage
            if (item.getCount() > 1) {
                item.shrink(1); // Reduce the stack size by one
                storage.setStackInSlot(0, item);
            } else {
                storage.setStackInSlot(0, ItemStack.EMPTY); // Remove the item if only one left
            }
            recipe.duration = (int) (recipe.duration * reduction);
            return ParallelLogic.accurateParallel(this, unit, recipe, parallels);
        }
        return null;
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable("gtocore.machine.height", height));
        textList.add(Component.translatable("gtocore.machine.duration_multiplier.tooltip", reduction));
        textList.add(Component.translatable("gtocore.machine.parallel", parallels));
    }

    private static int getItemTier(ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }
        return SpoolHatchPartMachine.SPOOL.getOrDefault(item.getItem(), 0);
    }
}

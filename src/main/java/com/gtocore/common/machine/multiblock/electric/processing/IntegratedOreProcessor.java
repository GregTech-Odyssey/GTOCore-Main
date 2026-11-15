package com.gtocore.common.machine.multiblock.electric.processing;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;

@DataGeneratorScanned
public final class IntegratedOreProcessor extends CrossRecipeMultiblockMachine {

    @RegisterLanguage(cn = "扩展处理线", en = "Extended Processing Line")
    private static final String EXT_NAME = "gtocore.multiblock.integrated_ore_processor.extension";
    @RegisterLanguage(cn = "搭建后线程数由 1 提升至 8，并可切换重复配方", en = "When built, raises threads from 1 to 8 and enables the repeated recipes toggle")
    private static final String EXT_DESC = "gtocore.multiblock.integrated_ore_processor.extension.desc";
    public static final ParamKey EXTENSION = ParamKey.of(EXT_NAME, EXT_DESC);
    @RegisterLanguage(cn = "并行重复配方", en = "Parallel Repeated Recipes")
    private static final Component REPEATED_RECIPES = Component.translatable("gtocore.multiblock.integrated_ore_processor.repeated_recipes");
    @RegisterLanguage(cn = "需要搭建扩展处理线", en = "Requires the Extended Processing Line")
    private static final Component REPEATED_RECIPES_UNAVAILABLE = Component.translatable("gtocore.multiblock.integrated_ore_processor.repeated_recipes.unavailable");

    @SaveToDisk(defaultValue = "true")
    private boolean repeatedRecipes = true;

    public IntegratedOreProcessor(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
    }

    @Override
    public void addControls(ControlPanel controls) {
        super.addControls(controls);
        controls.addToggle(REPEATED_RECIPES, () -> repeatedRecipes, value -> repeatedRecipes = value)
                .disabled(() -> !getAssembly().has(EXTENSION), REPEATED_RECIPES_UNAVAILABLE);
    }

    @Override
    public int getThread() {
        return getAssembly().has(EXTENSION) ? 8 : 1;
    }

    @Override
    public boolean isRepeatedRecipes() {
        return repeatedRecipes;
    }
}

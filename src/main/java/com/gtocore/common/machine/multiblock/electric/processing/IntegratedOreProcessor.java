package com.gtocore.common.machine.multiblock.electric.processing;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@DataGeneratorScanned
public final class IntegratedOreProcessor extends CrossRecipeMultiblockMachine {

    @RegisterLanguage(cn = "扩展处理线", en = "Extended Processing Line")
    private static final String EXT_NAME = "gtocore.multiblock.integrated_ore_processor.extension";
    @RegisterLanguage(cn = "搭建后线程数由 1 提升至 8，并可切换重复配方", en = "When built, raises threads from 1 to 8 and enables the repeated recipes toggle")
    private static final String EXT_DESC = "gtocore.multiblock.integrated_ore_processor.extension.desc";
    public static final ParamKey EXTENSION = ParamKey.of(EXT_NAME, EXT_DESC);

    @SaveToDisk(defaultValue = "true")
    private boolean repeatedRecipes = true;

    public IntegratedOreProcessor(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        super.customText(list);
        if (hasStructurePart(EXTENSION)) {
            list.add(Component.translatable("gtocore.machine.repeated_recipes", ComponentPanelWidget.withButton(repeatedRecipes ? Component.translatable("gtocore.machine.on") : Component.translatable("gtocore.machine.off"), "toggle")));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("toggle")) {
                repeatedRecipes = !repeatedRecipes;
            }
        } else {
            super.handleDisplayClick(componentData, clickData);
        }
    }

    @Override
    public int getThread() {
        return hasStructurePart(EXTENSION) ? 8 : 1;
    }

    @Override
    public boolean isRepeatedRecipes() {
        return repeatedRecipes;
    }
}

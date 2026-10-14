package com.gtocore.common.machine.multiblock.part;

import com.gtocore.api.machine.part.IRadiationHatch;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.world.entity.player.Player;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import lombok.Getter;

public class CreativeRadiationHatch extends MultiblockPartMachine implements IRadiationHatch {

    @Getter
    @SaveToDisk(defaultValue = "0")
    private int radioactivity;

    public CreativeRadiationHatch(MetaMachineBlockEntity holder) {
        super(holder);
    }

    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(140, 95, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(7, 7, "gtocore.recipe.radioactivity"))
                .widget(new TextFieldWidget(9, 20, 122, 16, () -> String.valueOf(radioactivity), value -> {
                    radioactivity = Integer.parseInt(value);
                    getControllers().forEach(IMultiController::requestCheck);
                })
                        .setNumbersOnly(0, Long.MAX_VALUE))
                .widget(new LabelWidget(7, 42, "gtceu.creative.computation.average"));
    }
}

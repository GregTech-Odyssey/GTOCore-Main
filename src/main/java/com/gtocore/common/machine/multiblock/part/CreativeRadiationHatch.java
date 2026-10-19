package com.gtocore.common.machine.multiblock.part;

import com.gtocore.api.machine.part.IRadiationHatch;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;

public class CreativeRadiationHatch extends MultiblockPartMachine implements IRadiationHatch {

    @Getter
    @SaveToDisk(defaultValue = "0")
    private int radioactivity;

    public CreativeRadiationHatch(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public Widget createUIWidget() {
        return Form.page().addChild(UIElement.section().addChildren(
                TextLine.constant(LayoutStyle.AUTO, Component.translatable("gtocore.machine.radioactivity_trait.dose").append(" (Sv)"))
                        .bindClientColor(UITheme::panelText),
                NumberField.ofInt(LayoutStyle.AUTO, () -> radioactivity, value -> {
                    radioactivity = value;
                    markAsDirty();
                    getControllers().forEach(IMultiController::requestCheck);
                }, 0, Integer.MAX_VALUE)));
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }
}

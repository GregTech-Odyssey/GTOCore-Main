package com.gtocore.common.machine.monitor;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MonitorCustomInfo extends AbstractInfoProviderMonitor implements IFancyUIMachine {

    private static final char FORMATTING_CODE = '\u00A7';
    private static final int MAX_LENGTH = 100;

    @SyncToClient
    @SaveToDisk(defaultValue = "")
    private String content = "";

    public MonitorCustomInfo(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void syncInfoFromServer() {}

    @Override
    public Widget createUIWidget() {
        var textField = TextField.of(UISizes.CONTENT_WIDTH - UISizes.CONTROL_HEIGHT - UISizes.GAP, this::getContent, this::setContent);
        textField.getInput().setMaxStringLength(MAX_LENGTH);
        textField.setHoverTooltips(Component.translatable("gtocore.machine.monitor.custom_info.tooltip"));

        var formattingCodeInputButton = Button.glyph(String.valueOf(FORMATTING_CODE))
                .setOnClientClick(() -> Minecraft.getInstance().keyboardHandler.setClipboard(String.valueOf(FORMATTING_CODE)));
        formattingCodeInputButton.setHoverTooltips(
                Component.translatable("gtocore.machine.monitor.custom_info.code_input_tooltip.1"),
                Component.translatable("gtocore.machine.monitor.custom_info.code_input_tooltip.2"),
                Component.translatable("gtocore.machine.monitor.custom_info.code_input_tooltip.3"));

        var textRow = UIElement.row(UISizes.CONTROL_HEIGHT)
                .layout(l -> l.gapAll(UISizes.GAP).alignCenter())
                .addChildren(textField, formattingCodeInputButton);

        return Form.page().addChildren(
                textRow,
                Form.numberRow(Component.translatable("gtocore.machine.monitor.priority"),
                        NumberField.ofLong(UISizes.CONTENT_WIDTH, this::getPriority, this::setPriority, (long) Integer.MIN_VALUE, (long) Integer.MAX_VALUE)));
    }

    @Override
    public DisplayComponentList provideInformation() {
        var infoList = super.provideInformation();
        infoList.addIfAbsent(
                DisplayRegistry.CUSTOM_DISPLAY.id(),
                Component.literal(content).getVisualOrderText());
        return infoList;
    }

    @Override
    public List<ResourceLocation> getAvailableRLs() {
        var rls = super.getAvailableRLs();
        rls.add(DisplayRegistry.CUSTOM_DISPLAY.id());
        return rls;
    }
}

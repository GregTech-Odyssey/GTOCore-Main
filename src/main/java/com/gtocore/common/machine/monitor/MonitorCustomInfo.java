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

import java.util.List;

@Getter
public class MonitorCustomInfo extends AbstractInfoProviderMonitor implements IFancyUIMachine {

    private static final char FORMATTING_CODE = '\u00A7';
    private static final int MAX_LENGTH = 100;

    @SyncToClient
    @SaveToDisk(defaultValue = "")
    private String content = "";

    @SyncToClient
    @SaveToDisk(defaultValue = "9")
    private int fontSize = 9;

    @SyncToClient
    @SaveToDisk(defaultValue = "0")
    private int horizontalAlignment;

    @SyncToClient
    @SaveToDisk(defaultValue = "0")
    private int verticalAlignment;

    public MonitorCustomInfo(MetaMachineBlockEntity holder) {
        super(holder);
    }

    public void setContent(String content) {
        this.content = content;
        onChanged();
        requestSync();
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        onChanged();
        requestSync();
    }

    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        onChanged();
        requestSync();
    }

    public void setVerticalAlignment(int verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        onChanged();
        requestSync();
    }

    @Override
    public void syncInfoFromServer() {}

    @Override
    public Widget createConfigurationPage() {
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
                Form.numberRow(Component.translatable("gtocore.machine.monitor.custom_info.font_size"),
                        NumberField.ofInt(UISizes.CONTENT_WIDTH, this::getFontSize, this::setFontSize, 4, 48)),
                Form.choiceRow(Component.translatable("gtocore.machine.monitor.custom_info.horizontal_alignment"), 3,
                        index -> Component.translatable("gtocore.machine.monitor.custom_info.horizontal_alignment." + index),
                        this::getHorizontalAlignment, this::setHorizontalAlignment),
                Form.choiceRow(Component.translatable("gtocore.machine.monitor.custom_info.vertical_alignment"), 3,
                        index -> Component.translatable("gtocore.machine.monitor.custom_info.vertical_alignment." + index),
                        this::getVerticalAlignment, this::setVerticalAlignment),
                createPriorityRow());
    }

    @Override
    public DisplayComponentList provideInformation() {
        var infoList = super.provideInformation();
        infoList.addIfAbsent(
                DisplayRegistry.CUSTOM_DISPLAY.id(),
                DisplayComponent.paragraph(DisplayRegistry.CUSTOM_DISPLAY.id(), Component.literal(content), fontSize, horizontalAlignment, verticalAlignment));
        return infoList;
    }

    @Override
    public List<ResourceLocation> getAvailableRLs() {
        var rls = super.getAvailableRLs();
        rls.add(DisplayRegistry.CUSTOM_DISPLAY.id());
        return rls;
    }
}

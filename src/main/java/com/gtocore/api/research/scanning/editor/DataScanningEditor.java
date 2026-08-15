package com.gtocore.api.research.scanning.editor;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class DataScanningEditor implements IFancyUIProvider {

    public static final DataScanningEditor INSTANCE = new DataScanningEditor();

    private DataScanningEditor() {}

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return new DataScanningEditorWidget();
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(Items.SPYGLASS);
    }

    @Override
    public Component getTitle() {
        return Component.literal("Data Scanning Editor");
    }
}

package com.gtocore.api.research.techtree.editor;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

public final class TechNodeEditor implements IFancyUIProvider {

    public static final TechNodeEditor INSTANCE = new TechNodeEditor();

    private TechNodeEditor() {}

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return new TechNodeEditorWidget();
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(Items.WRITABLE_BOOK);
    }

    @Override
    public Component getTitle() {
        return Component.literal("Tech Node Editor");
    }
}

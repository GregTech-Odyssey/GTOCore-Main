package com.hepdd.gtmthings.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.inventory.SlotGridView;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Accessors(chain = true)
public class InventoryFancyConfigurator implements IFancyConfigurator {

    private final CustomItemStackHandler inventory;

    @Getter
    private final Component title;

    @Getter
    @Setter
    private List<Component> tooltips = Collections.emptyList();

    public InventoryFancyConfigurator(CustomItemStackHandler inventory, Component title) {
        this.inventory = inventory;
        this.title = title;
    }

    @Override
    public IGuiTexture getIcon() {
        return WidgetIcons.ITEMS;
    }

    /** 展开后的内容：标准物品槽紧排成的小网格。 */
    @Override
    public Widget createConfigurator() {
        return SlotGridView.items(inventory);
    }
}

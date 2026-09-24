package com.gtocore.common.machine.multiblock.part.ae.widget;

import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemSlot;
import com.gtocore.common.machine.multiblock.part.ae.widget.slot.AEItemConfigSlotWidget;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import appeng.api.stacks.GenericStack;

public class AEItemConfigWidget extends ConfigWidget {

    private final ExportOnlyAEItemList itemList;

    public AEItemConfigWidget(int x, int y, ExportOnlyAEItemList list) {
        this(x, y, list, DEFAULT_COLUMNS);
    }

    /** 每行 {@code columns} 格。 */
    public AEItemConfigWidget(int x, int y, ExportOnlyAEItemList list, int columns) {
        super(x, y, list.getInventory(), list.isStocking(), columns);
        this.itemList = list;
    }

    @Override
    void init() {
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new ExportOnlyAEItemSlot();
            this.cached[index] = new ExportOnlyAEItemSlot();
            var position = cellPosition(index);
            this.addWidget(new AEItemConfigSlotWidget(position.x, position.y, this, index));
        }
    }

    @Override
    public boolean hasStackInConfig(GenericStack stack) {
        return itemList.hasStackInConfig(stack, true);
    }

    @Override
    boolean listAutoPull() {
        return itemList.isAutoPull();
    }
}

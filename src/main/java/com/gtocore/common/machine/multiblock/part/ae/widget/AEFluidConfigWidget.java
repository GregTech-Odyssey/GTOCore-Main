package com.gtocore.common.machine.multiblock.part.ae.widget;

import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidSlot;
import com.gtocore.common.machine.multiblock.part.ae.widget.slot.AEFluidConfigSlotWidget;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import appeng.api.stacks.GenericStack;

public class AEFluidConfigWidget extends ConfigWidget {

    private final ExportOnlyAEFluidList fluidList;

    public AEFluidConfigWidget(int x, int y, ExportOnlyAEFluidList list) {
        this(x, y, list, DEFAULT_COLUMNS);
    }

    /** 每行 {@code columns} 格。 */
    public AEFluidConfigWidget(int x, int y, ExportOnlyAEFluidList list, int columns) {
        super(x, y, list.getInventory(), list.isStocking(), columns);
        this.fluidList = list;
    }

    @Override
    void init() {
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new ExportOnlyAEFluidSlot();
            this.cached[index] = new ExportOnlyAEFluidSlot();
            var position = cellPosition(index);
            this.addWidget(new AEFluidConfigSlotWidget(position.x, position.y, this, index));
        }
    }

    @Override
    public boolean hasStackInConfig(GenericStack stack) {
        return fluidList.hasStackInConfig(stack, true);
    }

    @Override
    boolean listAutoPull() {
        return fluidList.isAutoPull();
    }
}

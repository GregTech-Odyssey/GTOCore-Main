package com.gtocore.common.machine.multiblock.part.ae.widget;

import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidSlot;
import com.gtocore.common.machine.multiblock.part.ae.widget.slot.AELimitFluidConfigSlotWidget;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.Nullable;

/**
 * 限制配置用的紧凑流体格：每格只有一个 18×18 的配置槽（没有 ME 输入仓那种"库存"下半格），行距也是 18。
 *
 * @see AELimitItemConfigWidget
 */
public class AELimitFluidConfigWidget extends AEFluidConfigWidget {

    /// 一格的边长：只有配置槽
    static final int CELL = 18;

    @Nullable
    private final Runnable onChanged;

    public AELimitFluidConfigWidget(int x, int y, ExportOnlyAEFluidList list, int columns, @Nullable Runnable onChanged) {
        super(x, y, list, columns);
        this.onChanged = onChanged;
        int rows = Math.max(1, (config.length + columns() - 1) / columns());
        setSize(new Size(getSizeWidth(), rows * CELL));
    }

    @Override
    void init() {
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new ExportOnlyAEFluidSlot();
            this.cached[index] = new ExportOnlyAEFluidSlot();
            var position = cellPosition(index);
            this.addWidget(new AELimitFluidConfigSlotWidget(position.x, position.y, this, index));
        }
    }

    /// 每行几格：父类把宽度定成 {@code min(columns, 格数) * 18}，这里按宽度反推（构造期本类字段还没赋值）
    private int columns() {
        return Math.max(1, getSizeWidth() / CELL);
    }

    @Override
    Position cellPosition(int index) {
        int columns = columns();
        return new Position(index % columns * CELL, index / columns * CELL);
    }

    /// 数量下限 0：0 就是"禁止"（查表时按 -1 记）
    @Override
    public long minAmount() {
        return 0;
    }

    @Override
    public void notifyConfigChanged() {
        if (onChanged != null) onChanged.run();
    }
}

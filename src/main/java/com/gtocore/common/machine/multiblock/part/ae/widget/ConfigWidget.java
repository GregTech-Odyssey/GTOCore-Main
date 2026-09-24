package com.gtocore.common.machine.multiblock.part.ae.widget;

import com.gtocore.common.machine.multiblock.part.ae.widget.slot.AEConfigSlotWidget;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;

public abstract class ConfigWidget extends WidgetGroup {

    final IConfigurableSlot[] config;
    IConfigurableSlot[] cached;
    private final Int2ObjectMap<IConfigurableSlot> changeMap = new Int2ObjectOpenHashMap<>();
    IConfigurableSlot[] displayList;
    private final AmountSetWidget amountSetWidget;
    private static final int UPDATE_ID = 1000;
    /// 自动拉取状态的下发（不能用 1、2：WidgetGroup 用它们转发子控件的更新）
    private static final int AUTO_PULL_ID = 1001;
    /// 客户端：服务端下发的自动拉取状态；服务端：上次下发的值
    private boolean autoPull;
    @Getter
    private final boolean isStocking;
    @Setter
    private boolean showAmount;

    /// 默认每行格数（ME 输入总线/仓的 16 格排两行）
    static final int DEFAULT_COLUMNS = 8;
    /// 每格是"配置 + 库存"上下两个槽，宽 18、高 36，行距 2
    private static final int CELL_WIDTH = 18;
    private static final int ROW_HEIGHT = 38;

    /// {@link #mapAmount} 的返回值：输入框里的数量不接受
    static final long REJECT_AMOUNT = Long.MIN_VALUE;

    /// 每行格数
    private final int columns;

    ConfigWidget(int x, int y, IConfigurableSlot[] config, boolean isStocking, boolean showAmount, int columns) {
        super(new Position(x, y), new Size(Math.min(columns, config.length) * CELL_WIDTH, gridHeight(config.length, columns)));
        this.columns = columns;
        this.isStocking = isStocking;
        this.showAmount = showAmount;
        this.config = config;
        this.init();
        // 设置数量的小面板浮在网格中央（原先在网格上方 60 像素，放进窗口后会飘到窗口外），面板自己抬高一层盖住下面的物品
        this.amountSetWidget = new AmountSetWidget((getSizeWidth() - AmountSetWidget.WIDTH) / 2, (getSizeHeight() - AmountSetWidget.HEIGHT) / 2, this);
        this.addWidget(this.amountSetWidget);
        this.addWidget(this.amountSetWidget.getAmountText());
        this.amountSetWidget.setVisible(false);
        this.amountSetWidget.getAmountText().setVisible(false);
    }

    ConfigWidget(int x, int y, IConfigurableSlot[] config, boolean isStocking, int columns) {
        this(x, y, config, isStocking, !isStocking, columns);
    }

    private static int gridHeight(int count, int columns) {
        return Math.max(1, (count + columns - 1) / columns) * ROW_HEIGHT - 2;
    }

    /** 第 {@code index} 格的位置：每行 {@link #columns} 格。 */
    Position cellPosition(int index) {
        return new Position(index % columns * CELL_WIDTH, index / columns * ROW_HEIGHT);
    }

    @OnlyIn(Dist.CLIENT)
    public void enableAmountClient(int slotIndex) {
        this.amountSetWidget.setSlotIndexClient(slotIndex);
        this.amountSetWidget.setVisible(true);
        this.amountSetWidget.getAmountText().setVisible(true);
    }

    @OnlyIn(Dist.CLIENT)
    public void disableAmountClient() {
        this.amountSetWidget.setSlotIndexClient(-1);
        this.amountSetWidget.setVisible(false);
        this.amountSetWidget.getAmountText().setVisible(false);
    }

    public void enableAmount(int slotIndex) {
        this.amountSetWidget.setSlotIndex(slotIndex);
        this.amountSetWidget.setVisible(true);
        this.amountSetWidget.getAmountText().setVisible(true);
    }

    public void disableAmount() {
        this.amountSetWidget.setSlotIndex(-1);
        this.amountSetWidget.setVisible(false);
        this.amountSetWidget.getAmountText().setVisible(false);
    }

    /** 数量面板打开且鼠标在面板上：下面的格子不响应悬停和点击。 */
    public boolean isAmountPanelOver(double mouseX, double mouseY) {
        return this.amountSetWidget.isVisible() && this.amountSetWidget.isMouseOverElement(mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.amountSetWidget.isVisible()) {
            if (this.amountSetWidget.getAmountText().mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (!this.amountSetWidget.isMouseOverElement(mouseX, mouseY)) {
                this.disableAmountClient();
            }
        }
        if (isAmountPanelOver(mouseX, mouseY)) return true;
        for (Widget w : this.widgets) {
            if (w instanceof AEConfigSlotWidget slot) {
                slot.setSelect(false);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    abstract void init();

    /**
     * 输入框里的数量映射成真正写进格子的数量；返回 {@link #REJECT_AMOUNT} 表示不接受。
     * 默认只接受正数；可配置存储访问仓覆写成「0 记成 -1（禁止）」。
     */
    long mapAmount(long newAmount) {
        return newAmount > 0 ? newAmount : REJECT_AMOUNT;
    }

    /** 服务端配置格内容变了（键或数量）：默认什么都不做，配了查表的机器覆写它重建查表。 */
    public void notifyConfigChanged() {}

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        autoPull = listAutoPull();
        buffer.writeBoolean(autoPull);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        autoPull = buffer.readBoolean();
    }

    public abstract boolean hasStackInConfig(GenericStack stack);

    /** 配置列表此刻是否自动拉取（服务端数据，经 {@link #isAutoPull} 下发）。 */
    abstract boolean listAutoPull();

    /**
     * 是否自动拉取：服务端直接读列表；客户端读服务端下发的值——库存总线/仓的自动拉取开关只存在服务端，
     * 直接在客户端读永远是关闭，配置格的禁用显示就不准。
     */
    public final boolean isAutoPull() {
        return isRemote() ? autoPull : listAutoPull();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        boolean currentAutoPull = listAutoPull();
        if (currentAutoPull != autoPull) {
            autoPull = currentAutoPull;
            writeUpdateInfo(AUTO_PULL_ID, buf -> buf.writeBoolean(currentAutoPull));
        }
        this.changeMap.clear();
        for (int index = 0; index < this.config.length; index++) {
            IConfigurableSlot newSlot = this.config[index];
            IConfigurableSlot oldSlot = this.cached[index];
            GenericStack nConfig = newSlot.getConfig();
            GenericStack nStock = newSlot.getStock();
            GenericStack oConfig = oldSlot.getConfig();
            GenericStack oStock = oldSlot.getStock();
            if (!areAEStackCountsEqual(nConfig, oConfig) || !areAEStackCountsEqual(nStock, oStock)) {
                this.changeMap.put(index, newSlot.copy());
                this.cached[index] = this.config[index].copy();
                this.gui.holder.markAsDirty();
            }
        }
        if (!this.changeMap.isEmpty()) {
            this.writeUpdateInfo(UPDATE_ID, buf -> {
                buf.writeVarInt(this.changeMap.size());
                for (int index : this.changeMap.keySet()) {
                    GenericStack sConfig = this.changeMap.get(index).getConfig();
                    GenericStack sStock = this.changeMap.get(index).getStock();
                    buf.writeVarInt(index);
                    if (sConfig != null) {
                        buf.writeBoolean(true);
                        GenericStack.writeBuffer(sConfig, buf);
                    } else {
                        buf.writeBoolean(false);
                    }
                    if (sStock != null) {
                        buf.writeBoolean(true);
                        GenericStack.writeBuffer(sStock, buf);
                    } else {
                        buf.writeBoolean(false);
                    }
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == AUTO_PULL_ID) autoPull = buffer.readBoolean();
        if (id == UPDATE_ID) {
            int size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                int index = buffer.readVarInt();
                IConfigurableSlot slot = this.displayList[index];
                if (buffer.readBoolean()) {
                    slot.setConfig(GenericStack.readBuffer(buffer));
                } else {
                    slot.setConfig(null);
                }
                if (buffer.readBoolean()) {
                    slot.setStock(GenericStack.readBuffer(buffer));
                } else {
                    slot.setStock(null);
                }
            }
        }
    }

    public final IConfigurableSlot getConfig(int index) {
        return this.config[index];
    }

    public final IConfigurableSlot getDisplay(int index) {
        return this.displayList[index];
    }

    private static boolean areAEStackCountsEqual(GenericStack s1, GenericStack s2) {
        if (s2 == s1) {
            return true;
        }
        if (s1 != null && s2 != null) {
            return s1.amount() == s2.amount() && s1.what().matches(s2);
        }
        return false;
    }

    public boolean showAmount() {
        return showAmount;
    }
}

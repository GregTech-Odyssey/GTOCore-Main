package com.gtocore.common.machine.multiblock.part.ae.widget;

import com.gtocore.common.machine.multiblock.part.ae.widget.slot.AEConfigSlotWidget;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

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

    /// 每行格数
    private final int columns;

    ConfigWidget(int x, int y, IConfigurableSlot[] config, boolean isStocking, boolean showAmount, int columns) {
        super(new Position(x, y), new Size(Math.min(columns, config.length) * CELL_WIDTH, gridHeight(config.length, columns)));
        this.columns = columns;
        this.isStocking = isStocking;
        this.showAmount = showAmount;
        this.config = config;
        this.init();
        // 设置数量的小面板：打开时摆到对应配置格的正下方（见 placeAmountPanel），最后加入、自己抬高一层盖住下面的格子
        this.amountSetWidget = new AmountSetWidget(this);
        this.addWidget(this.amountSetWidget);
        this.amountSetWidget.setVisible(false);
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

    /**
     * 把数量面板摆到第 {@code slotIndex} 格的配置格（上格）正下方：面板顶边离配置格下沿一个尖角高，尖角对准该格中心；
     * 水平以该格为中心，超出网格左右边界时收回网格内（网格比面板窄时收回窗口内）。两端都摆，控件树一致。
     * 坐标以主控件组为基准：客户端的绝对坐标含界面在屏幕上的偏移，服务端没有，直接拿去和界面尺寸比会两端不一致。
     */
    private void placeAmountPanel(int slotIndex) {
        if (!isValidIndex(slotIndex)) return;
        Position cell = cellPosition(slotIndex);
        int width = AmountSetWidget.WIDTH;
        int centerX = cell.x + CELL_WIDTH / 2;
        int x = centerX - width / 2;
        int y = cell.y + UISizes.SLOT + UITheme.POPUP_NOTCH;
        var gui = getGui();
        if (width <= getSizeWidth()) {
            x = Math.max(0, Math.min(x, getSizeWidth() - width));
        } else if (gui != null) {
            // 网格比面板窄（如只有一两格）：收回窗口内，留出窗口内边距；网格不在界面矩形内（挂在界面外的弹出面板里）时不收
            int gx = getPositionX() - gui.mainGroup.getPositionX();
            int gy = getPositionY() - gui.mainGroup.getPositionY();
            boolean inWindow = gx >= 0 && gy >= 0 && gx + getSizeWidth() <= gui.getWidth() && gy + getSizeHeight() <= gui.getHeight();
            if (inWindow) {
                int minX = UISizes.WINDOW_PADDING_X - gx;
                int maxX = gui.getWidth() - UISizes.WINDOW_PADDING_X - width - gx;
                x = Math.max(minX, Math.min(x, maxX));
            }
        }
        // 尖角对准格子中心，但不伸进面板的圆角
        int margin = UITheme.POPUP_NOTCH + 2;
        this.amountSetWidget.setNotchX(Math.max(margin, Math.min(width - margin, centerX - x)));
        this.amountSetWidget.setSelfPosition(new Position(x, y));
    }

    @OnlyIn(Dist.CLIENT)
    public void enableAmountClient(int slotIndex) {
        placeAmountPanel(slotIndex);
        this.amountSetWidget.setSlotIndexClient(slotIndex);
        this.amountSetWidget.setVisible(true);
    }

    /** 关闭数量面板，并取消格子的选中框。 */
    @OnlyIn(Dist.CLIENT)
    public void disableAmountClient() {
        this.amountSetWidget.setSlotIndexClient(-1);
        this.amountSetWidget.setVisible(false);
        for (Widget w : this.widgets) {
            if (w instanceof AEConfigSlotWidget slot) {
                slot.setSelect(false);
            }
        }
    }

    /** 数量面板打开且鼠标在面板上：下面的格子不响应悬停和点击。 */
    public boolean isAmountPanelOver(double mouseX, double mouseY) {
        return this.amountSetWidget.isVisible() && this.amountSetWidget.isMouseOverElement(mouseX, mouseY);
    }

    boolean isValidIndex(int index) {
        return index >= 0 && index < this.config.length;
    }

    /**
     * 服务端：第 {@code index} 格此刻能否改数量——格号合法、格子有配置、不是库存模式（数量无意义，面板也不会打开）、
     * 没有自动拉取（配置由机器管理）。数量写入前必须判它，客户端的请求可以伪造。
     */
    public boolean canSetAmount(int index) {
        return isValidIndex(index) && !isStocking && !isAutoPull() && this.config[index].getConfig() != null;
    }

    /**
     * 面板打开时：点在面板上交给面板（面板吃掉整块区域的点击）；点在别处时面板已在点击分发前关闭
     * （{@link AmountSetWidget#onOutsideClick}，窗口内外都算，先提交输入框草稿、后关闭），这里只是不在机器窗口里时的后备。
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.amountSetWidget.isVisible()) {
            if (this.amountSetWidget.mouseClicked(mouseX, mouseY, button) || isAmountPanelOver(mouseX, mouseY)) {
                return true;
            }
            this.disableAmountClient();
        }
        for (Widget w : this.widgets) {
            if (w instanceof AEConfigSlotWidget slot) {
                slot.setSelect(false);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** 面板上的滚轮交给调节器，不漏到下面的格子。 */
    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (isAmountPanelOver(mouseX, mouseY)) {
            this.amountSetWidget.mouseWheelMove(mouseX, mouseY, wheelDelta);
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    abstract void init();

    /**
     * 配置数量的下限：数量面板调节器的最小值，服务端写入数量时也按它校验。
     * 默认 1；可配置存储访问仓的限制格为 0（0 表示禁止，查表时按 -1 记）。
     */
    public long minAmount() {
        return 1;
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

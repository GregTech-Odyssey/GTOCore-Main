package com.gtocore.common.machine.multiblock.part.ae.widget;

import com.gtocore.common.machine.multiblock.part.ae.widget.slot.AEConfigSlotWidget;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.PageOverlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;

/**
 * 配置格"设置数量"的小弹出面板（页内浮层 {@link PageOverlay}：画在整个窗口最上层、盖住处的鼠标先交给它）：
 * 标准窗口外观（{@link UITheme#WINDOW}），顶边伸出一个小尖角指向所属的配置格
 * （{@link UITheme#drawPopupNotch}），里面只有一个标准整数调节器 {@link NumberField}（确认后提交、服务端夹取、滚轮与修饰键步长，
 * 支持简写与算式）。
 * <p>
 * 面板对应哪一格（{@link #index}）是这个打开的界面的状态：只由客户端打开 / 关闭时上报，服务端校验后记下（放入、清除配置不会改它）；
 * 数量只在服务端写入，写之前由 {@link ConfigWidget#canSetAmount} 再判一次（格号、是否有配置、自动拉取 / 库存模式）。
 * 位置由 {@link ConfigWidget} 摆放（配置格正下方，尖角对准该格）。
 */
class AmountSetWidget extends PageOverlay {

    /// 调节器宽：按钮放得下 "±512"（Ctrl+Shift），输入框放得下约 10 位数字
    private static final int FIELD_WIDTH = 134;
    /// 窗口外框的内边距：左右、上 4，下 6（外框底部是厚边）
    private static final int PADDING = 4;
    private static final int PADDING_BOTTOM = 6;
    static final int WIDTH = FIELD_WIDTH + 2 * PADDING;
    static final int HEIGHT = NumberField.HEIGHT + PADDING + PADDING_BOTTOM;
    /// 客户端上报面板对应的格号（不能用 1、2：WidgetGroup 用它们转发子控件的请求）
    private static final int SLOT_INDEX_ID = 1000;

    private final ConfigWidget parentWidget;
    /// 面板对应的格号，-1 为未打开；两端各自记，服务端的值经校验
    private int index = -1;
    /// 尖角中线相对面板左边的位置（对准所属格子的中心）
    private int notchX = WIDTH / 2;

    AmountSetWidget(ConfigWidget widget) {
        this.parentWidget = widget;
        layout(l -> l.row().size(WIDTH, HEIGHT).paddingAll(PADDING).paddingBottom(PADDING_BOTTOM));
        var field = new NumberField(FIELD_WIDTH, this::getAmount, this::setAmount, parentWidget::minAmount, () -> Long.MAX_VALUE);
        // 两层禁用（原因不同）：自动拉取时整个面板禁用；格子没有配置时调节器禁用（打开空格子时，配置随后才到）
        disabled(parentWidget::isAutoPull, AEConfigSlotWidget.CONFIG_MANAGED);
        field.disabled(() -> parentWidget.isValidIndex(index) && parentWidget.getConfig(index).getConfig() == null, AEConfigSlotWidget.NO_CONFIG);
        addChild(field);
    }

    int getIndex() {
        return index;
    }

    /** 尖角对准的位置（相对面板左边）。 */
    void setNotchX(int notchX) {
        this.notchX = notchX;
    }

    /** 客户端：打开（格号 ≥ 0）或关闭（-1），同时告诉服务端。 */
    @OnlyIn(Dist.CLIENT)
    void setSlotIndexClient(int slotIndex) {
        this.index = slotIndex;
        writeClientAction(SLOT_INDEX_ID, buf -> buf.writeVarInt(slotIndex));
    }

    /** 服务端：客户端上报的格号，越界（伪造）按关闭处理。 */
    private void setSlotIndex(int slotIndex) {
        this.index = parentWidget.isValidIndex(slotIndex) ? slotIndex : -1;
    }

    /** 服务端取值：面板对应格的配置数量，未打开或没有配置时为 0。 */
    private long getAmount() {
        if (!parentWidget.isValidIndex(index)) return 0;
        GenericStack config = parentWidget.getConfig(index).getConfig();
        return config == null ? 0 : config.amount();
    }

    /** 服务端写值：调节器已夹到 [下限, Long.MAX]，这里再校验格子此刻能不能改（客户端可以伪造请求）。 */
    private void setAmount(long amount) {
        if (amount < parentWidget.minAmount() || !parentWidget.canSetAmount(index)) return;
        IConfigurableSlot slot = parentWidget.getConfig(index);
        slot.setConfig(new GenericStack(slot.getConfig().what(), amount));
        parentWidget.notifyConfigChanged();
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == SLOT_INDEX_ID) {
            setSlotIndex(buffer.readVarInt());
            return;
        }
        super.handleClientAction(id, buffer);
    }

    /** 点在面板外面（窗口内外任意处）：关闭；输入框里没确认的草稿此前已提交，关闭请求排在提交之后。 */
    @Override
    protected void onOutsideClick() {
        parentWidget.disableAmountClient();
    }

    /** 标准窗口外框 + 顶边指向所属格子的尖角（由窗口在最上层画，见 {@link PageOverlay}）。 */
    @OnlyIn(Dist.CLIENT)
    @Override
    protected void drawOverlayBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPositionX(), y = getPositionY();
        UITheme.WINDOW.draw(graphics, mouseX, mouseY, x, y, getSizeWidth(), getSizeHeight());
        UITheme.drawPopupNotch(graphics, x + notchX, y);
    }
}

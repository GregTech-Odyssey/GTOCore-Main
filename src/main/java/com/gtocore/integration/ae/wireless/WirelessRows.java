package com.gtocore.integration.ae.wireless;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.data.SyncValueHost;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 由服务端决定内容的纵向列表，两端对称增删。
 * <p>
 * <b>行下标在一次界面生命周期内稳定</b>：只追加、不删除、不重排。某个 key 从数据源里消失时，它的行原地隐藏
 * （{@link UIElement#setDisplay} 隐藏，{@code display: none} 不占位置）；再次出现时原地恢复。LDLib1 的客户端操作按子控件下标路由，
 * 下标不变就保证了客户端对"旧列表"里某一行的点击，在服务端一定落到同一个 key 的行上，不会点中别的条目。
 * 每次打开界面都新建本控件，所以隐藏行只存在于当次界面，重开后是一份干净的列表。
 * <p>
 * 协议：
 * <ul>
 * <li>初始：服务端在 {@link #initWidget()} 里按数据源建行；{@link #writeInitialData} 先写 {@code n × (key, 可见)}，
 * 再写子控件数据；客户端 {@link #readInitialData} 先读出 key、按同样顺序建行，再读子控件数据。</li>
 * <li>更新：服务端每次 {@link #detectAndSendChanges} 先看数据版本号，版本变了才重算数据源并比较，有变化时先发 {@link #ROWS_ID}
 * {@code [n × 可见][m × 新 key]}，再在本端应用（新行经 {@code addWidget} 的 id 2 通道下发初始数据，客户端此时已建好同下标的行）。</li>
 * </ul>
 * 行内容的变化（名称、收藏状态等）由行自己的 {@link SyncValue} 负责，不重建行。本控件没有客户端到服务端的消息。
 *
 * @param <K> 行的 key（需正确实现 equals/hashCode），两端用 {@code codec} 传输
 */
final class WirelessRows<K> extends UIElement {

    /** 行结构更新包：避开 WidgetGroup 自用的 1、2 与本控件 SyncValueHost 的 {@code ID_BASE + i}。 */
    private static final int ROWS_ID = SyncValueHost.ID_BASE - 1;

    private final boolean remote;
    private final SyncValue.Codec<K> codec;
    private final Supplier<List<K>> source;
    private final IntSupplier version;
    private final Function<K, UIElement> rowFactory;
    @Nullable
    private final Component emptyText;
    private final List<K> keys = new ArrayList<>();
    private boolean built;
    private int builtVersion;

    /**
     * @param remote     是否客户端
     * @param source     数据源，只在服务端调用
     * @param version    数据版本号，只在服务端调用；数据源的结果变化时它必须变化，版本不变时不重算数据源
     * @param rowFactory 按 key 建一行，两端都会调用，同一 key 必须产生同样结构的控件
     * @param emptyText  没有可见行时显示的提示（客户端绘制），可为 null
     */
    WirelessRows(boolean remote, SyncValue.Codec<K> codec, Supplier<List<K>> source, IntSupplier version,
                 Function<K, UIElement> rowFactory, @Nullable Component emptyText) {
        this.remote = remote;
        this.codec = codec;
        this.source = source;
        this.version = version;
        this.rowFactory = rowFactory;
        this.emptyText = emptyText;
        // 纵向排行，行宽被拉伸到列表宽；隐藏的行 display: none 不占位
        layout(l -> l.column().gapAll(UISizes.GAP));
        updateEmptyHeight();
    }

    @Override
    public void initWidget() {
        if (!remote && !built) {
            built = true;
            builtVersion = version.getAsInt();
            for (var key : source.get()) addRow(key);
        }
        super.initWidget();
    }

    private void addRow(K key) {
        keys.add(key);
        addWidget(rowFactory.apply(key));
    }

    private void setRowVisible(int index, boolean visible) {
        ((UIElement) widgets.get(index)).setDisplay(visible);
    }

    /// 没有可见行时留出一行高度画空提示
    private void updateEmptyHeight() {
        if (emptyText != null) layout(l -> l.minHeight(hasVisibleRow() ? 0 : TextLine.HEIGHT));
    }

    private boolean hasVisibleRow() {
        for (var widget : widgets) {
            if (widget instanceof UIElement row && row.isDisplayed()) return true;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (emptyText != null && !hasVisibleRow()) {
            var font = Minecraft.getInstance().font;
            graphics.drawString(font, UITheme.clip(font, emptyText.getString(), getSizeWidth()), getPositionX(), getPositionY(), UITheme.TEXT_SECONDARY, false);
        }
    }

    // ==================== 同步 ====================

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            codec.write(buffer, keys.get(i));
            buffer.writeBoolean(((UIElement) widgets.get(i)).isDisplayed());
        }
        super.writeInitialData(buffer);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            addRow(codec.read(buffer));
            setRowVisible(i, buffer.readBoolean());
        }
        updateEmptyHeight();
        super.readInitialData(buffer);
    }

    @Override
    public void detectAndSendChanges() {
        if (!remote && built) syncRows();
        super.detectAndSendChanges();
    }

    private void syncRows() {
        int current = version.getAsInt();
        if (current == builtVersion) return;
        builtVersion = current;
        var latest = new LinkedHashSet<>(source.get());
        var visible = new boolean[keys.size()];
        boolean changed = false;
        for (int i = 0; i < keys.size(); i++) {
            visible[i] = latest.remove(keys.get(i));
            if (visible[i] != ((UIElement) widgets.get(i)).isDisplayed()) changed = true;
        }
        var added = new ArrayList<>(latest);
        if (!changed && added.isEmpty()) return;
        writeUpdateInfo(ROWS_ID, buffer -> {
            buffer.writeVarInt(visible.length);
            for (var value : visible) buffer.writeBoolean(value);
            buffer.writeVarInt(added.size());
            for (var key : added) codec.write(buffer, key);
        });
        apply(visible, added);
    }

    private void apply(boolean[] visible, List<K> added) {
        for (int i = 0; i < visible.length; i++) setRowVisible(i, visible[i]);
        for (var key : added) addRow(key);
        updateEmptyHeight();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id != ROWS_ID) {
            super.readUpdateInfo(id, buffer);
            return;
        }
        var visible = new boolean[buffer.readVarInt()];
        for (int i = 0; i < visible.length; i++) visible[i] = buffer.readBoolean();
        int count = buffer.readVarInt();
        var added = new ArrayList<K>(count);
        for (int i = 0; i < count; i++) added.add(codec.read(buffer));
        apply(visible, added);
    }
}

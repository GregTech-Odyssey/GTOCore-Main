package com.gtocore.api.gui.ui.data;

import net.minecraft.network.FriendlyByteBuf;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 一个控件上所有 {@link SyncValue} 的编号与收发，概念上对应 LDLib2 的 {@code UISyncManager}，
 * 但作用域限定在单个控件内：LDLib1 的消息按控件树下标路由，没有全局 ID 表可用。
 * <p>
 * 更新包 ID 从 {@link #ID_BASE} 起按注册顺序分配，避开 {@code WidgetGroup} 自用的 1、2。
 */
public final class SyncValueHost {

    public static final int ID_BASE = 0x5A00;

    private final Widget owner;
    private final List<SyncValue<?>> values = new ArrayList<>(2);

    public SyncValueHost(Widget owner) {
        this.owner = owner;
    }

    public <T> SyncValue<T> add(SyncValue<T> value) {
        values.add(value);
        return value;
    }

    public void writeInitialData(FriendlyByteBuf buf) {
        if (owner.isClientSideWidget()) {
            buf.writeBoolean(false);
            return;
        }
        buf.writeBoolean(true);
        for (var value : values) value.writeInitial(buf);
    }

    public void readInitialData(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) return;
        for (var value : values) value.readInitial(buf);
    }

    /** 服务端：把变化的值逐个交给 {@code sender} 发出去（sender 即 {@code widget.writeUpdateInfo}）。 */
    public void detectAndSendChanges(UpdateSender sender) {
        if (owner.isClientSideWidget()) return;
        for (int i = 0; i < values.size(); i++) {
            var value = values.get(i);
            if (value.detectChange()) sender.send(ID_BASE + i, value::write);
        }
    }

    /** @return 该 ID 是否属于本 host */
    public boolean readUpdateInfo(int id, FriendlyByteBuf buf) {
        int index = id - ID_BASE;
        if (index < 0 || index >= values.size()) return false;
        values.get(index).read(buf);
        return true;
    }

    public void pollClient() {
        for (var value : values) value.pollClient();
    }

    @FunctionalInterface
    public interface UpdateSender {

        void send(int id, Consumer<FriendlyByteBuf> writer);
    }
}

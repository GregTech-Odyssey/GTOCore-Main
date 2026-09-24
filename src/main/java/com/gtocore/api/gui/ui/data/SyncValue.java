package com.gtocore.api.gui.ui.data;

import com.gtocore.api.gui.ui.UIElement;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 服务端到客户端（S2C）的单值绑定，概念上对应 LDLib2 的 {@code SyncValue} + {@code DataBindingBuilder.xxxS2C}。
 * <p>
 * 服务端：打开界面时随初始数据下发一次；之后每次 {@code detectAndSendChanges} 比较 getter，变了才下发。
 * 客户端：收到新值（含初始值）后回调 {@link #onChanged}。
 * 开启 {@link #pollOnClient()} 后，客户端每帧还会用本端 getter 取一次值（不回调），
 * 适合 getter 只依赖已同步到客户端的机器字段、希望本地即时跟随的场景。
 * <p>
 * 由 {@link SyncValueHost} 统一编号和收发，挂到 {@link UIElement} 上使用。
 */
public final class SyncValue<T> {

    public interface Codec<T> {

        void write(FriendlyByteBuf buf, T value);

        T read(FriendlyByteBuf buf);
    }

    public static final Codec<Integer> INT = new Codec<>() {

        @Override
        public void write(FriendlyByteBuf buf, Integer value) {
            buf.writeVarInt(value);
        }

        @Override
        public Integer read(FriendlyByteBuf buf) {
            return buf.readVarInt();
        }
    };

    public static final Codec<Boolean> BOOLEAN = new Codec<>() {

        @Override
        public void write(FriendlyByteBuf buf, Boolean value) {
            buf.writeBoolean(value);
        }

        @Override
        public Boolean read(FriendlyByteBuf buf) {
            return buf.readBoolean();
        }
    };

    public static final Codec<Component> COMPONENT = new Codec<>() {

        @Override
        public void write(FriendlyByteBuf buf, Component value) {
            buf.writeComponent(value);
        }

        @Override
        public Component read(FriendlyByteBuf buf) {
            return buf.readComponent();
        }
    };

    private final Supplier<T> getter;
    private final Codec<T> codec;
    private T value;
    @Nullable
    private Consumer<T> onChanged;
    private boolean pollOnClient;

    private SyncValue(Supplier<T> getter, Codec<T> codec, T initialValue) {
        this.getter = getter;
        this.codec = codec;
        this.value = initialValue;
    }

    public static <T> SyncValue<T> of(Supplier<T> getter, Codec<T> codec, T initialValue) {
        return new SyncValue<>(getter, codec, initialValue);
    }

    public static SyncValue<Integer> ofInt(Supplier<Integer> getter, int initialValue) {
        return of(getter, INT, initialValue);
    }

    public static SyncValue<Component> ofComponent(Supplier<Component> getter) {
        return of(getter, COMPONENT, getter.get());
    }

    /** 客户端收到不同的新值时回调（服务端比较出变化时也会回调）。 */
    public SyncValue<T> onChanged(Consumer<T> listener) {
        this.onChanged = listener;
        return this;
    }

    /** 客户端每帧也用本端 getter 刷新值；getter 依赖服务端独有状态时不要开。 */
    public SyncValue<T> pollOnClient() {
        this.pollOnClient = true;
        return this;
    }

    /** 最近一次同步到的值；客户端渲染一律读它。 */
    public T getValue() {
        return value;
    }

    void writeInitial(FriendlyByteBuf buf) {
        value = getter.get();
        codec.write(buf, value);
    }

    void readInitial(FriendlyByteBuf buf) {
        accept(codec.read(buf));
    }

    boolean detectChange() {
        var latest = getter.get();
        if (Objects.equals(latest, value)) return false;
        accept(latest);
        return true;
    }

    void write(FriendlyByteBuf buf) {
        codec.write(buf, value);
    }

    void read(FriendlyByteBuf buf) {
        accept(codec.read(buf));
    }

    void pollClient() {
        if (!pollOnClient) return;
        var latest = getter.get();
        if (!Objects.equals(latest, value)) value = latest;
    }

    private void accept(T newValue) {
        var old = value;
        value = newValue;
        if (onChanged != null && !Objects.equals(old, newValue)) onChanged.accept(newValue);
    }
}

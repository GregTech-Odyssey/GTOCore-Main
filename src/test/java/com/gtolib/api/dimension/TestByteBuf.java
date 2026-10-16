package com.gtolib.api.dimension;

import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Owns the buffer used by disk-codec tests and flushes encoded bytes to their fixture builder. */
public final class TestByteBuf extends FriendlyByteBuf implements AutoCloseable {

    private final ByteArrayOutputStream output;

    private TestByteBuf(ByteBuf buffer, ByteArrayOutputStream output) {
        super(buffer);
        this.output = output;
    }

    public static TestByteBuf of(byte[] bytes) {
        return new TestByteBuf(Unpooled.wrappedBuffer(bytes), null);
    }

    public static TestByteBuf of(ByteArrayOutputStream output) {
        return new TestByteBuf(Unpooled.buffer(), output);
    }

    @Override
    public void close() throws IOException {
        try {
            if (output != null) getBytes(readerIndex(), output, readableBytes());
        } finally {
            release();
        }
    }
}

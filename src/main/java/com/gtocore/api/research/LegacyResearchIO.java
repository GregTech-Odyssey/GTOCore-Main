package com.gtocore.api.research;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.DecoderException;
import org.jetbrains.annotations.ApiStatus;

import java.io.DataInputStream;
import java.io.IOException;

/** dev12 之前的研究与科技树存档用 DataOutputStream 写字符串（2 字节长度 + modified UTF-8），按原格式读回。 */
@Deprecated(since = "0.6.0", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
public final class LegacyResearchIO {

    private LegacyResearchIO() {}

    public static String readUtf(ByteBuf buf) {
        try {
            return DataInputStream.readUTF(new ByteBufInputStream(buf));
        } catch (IOException e) {
            throw new DecoderException(e);
        }
    }
}

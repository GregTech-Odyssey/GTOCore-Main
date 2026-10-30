package com.gtocore.mixin.corpse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.Unpooled;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Corpse 的死亡历史会把每条记录的装备 NBT 全部塞进一个包，记录一多就超过
 * 网络载荷的 1 MiB 上限或客户端 readNbt 的 2 MiB 配额，导致发送或解码失败。
 * 这里同时按实际载荷字节数和 NbtAccounter 记账量裁剪：历史由 DeathManager 按时间
 * 倒序提供，优先保留最新记录的装备，超限后只保留历史条目的元数据；反射失败时
 * 退回原编码。
 */
@Pseudo
@Mixin(targets = "de.maxhenkel.corpse.net.MessageOpenHistory", remap = false)
public abstract class MessageOpenHistoryMixin {

    @Unique
    private static final long gtocore$MAX_NBT_USAGE = 2_000_000L;
    @Unique
    private static final int gtocore$MAX_PAYLOAD_BYTES = 1_048_576;
    // 独立 compound 的类型和空名称不出现在 ListTag 元素中
    @Unique
    private static final int gtocore$NBT_ROOT_HEADER_BYTES = 3;
    // ListTag 读取时按元素追加 4 字节记账
    @Unique
    private static final long gtocore$LIST_ELEMENT_USAGE = 4L;
    @Unique
    private static volatile Method gtocore$toNbtMethod;

    @Shadow
    private List<?> deaths;

    @Inject(method = "toBytes", at = @At("HEAD"), cancellable = true)
    private void gtocore$writeBoundedHistory(FriendlyByteBuf buf, CallbackInfo ci) {
        List<?> history = deaths;
        if (history.isEmpty()) {
            return;
        }
        Method method = gtocore$resolveToNbt(history.get(0));
        if (method == null) {
            return;
        }

        ListTag list = new ListTag();
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("Deaths", list);
        FriendlyByteBuf probe = new FriendlyByteBuf(Unpooled.buffer());
        try {
            long usage = gtocore$accountedUsage(wrapper, probe);
            // SimpleChannel 在调用编码器前已写入消息编号，也需要计入载荷预算
            long payloadBytes = (long) buf.writerIndex() + probe.writerIndex();
            boolean withoutEquipment = false;
            for (Object death : history) {
                CompoundTag entry = (CompoundTag) method.invoke(death, false);
                if (!withoutEquipment) {
                    long entryUsage = gtocore$accountedUsage(entry, probe) + gtocore$LIST_ELEMENT_USAGE;
                    int entryBytes = probe.writerIndex() - gtocore$NBT_ROOT_HEADER_BYTES;
                    if (usage + entryUsage <= gtocore$MAX_NBT_USAGE && payloadBytes + entryBytes <= gtocore$MAX_PAYLOAD_BYTES) {
                        list.add(entry);
                        usage += entryUsage;
                        payloadBytes += entryBytes;
                        continue;
                    }
                    withoutEquipment = true;
                }
                entry.remove("Equipment");
                long entryUsage = gtocore$accountedUsage(entry, probe) + gtocore$LIST_ELEMENT_USAGE;
                int entryBytes = probe.writerIndex() - gtocore$NBT_ROOT_HEADER_BYTES;
                if (usage + entryUsage > gtocore$MAX_NBT_USAGE || payloadBytes + entryBytes > gtocore$MAX_PAYLOAD_BYTES) {
                    break;
                }
                list.add(entry);
                usage += entryUsage;
                payloadBytes += entryBytes;
            }
        } catch (ReflectiveOperationException e) {
            return;
        } finally {
            probe.release();
        }

        buf.writeNbt(wrapper);
        ci.cancel();
    }

    @Unique
    private static Method gtocore$resolveToNbt(Object death) {
        Method method = gtocore$toNbtMethod;
        if (method != null) {
            return method;
        }
        try {
            method = death.getClass().getMethod("toNBT", boolean.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
        gtocore$toNbtMethod = method;
        return method;
    }

    @Unique
    private static long gtocore$accountedUsage(CompoundTag tag, FriendlyByteBuf probe) {
        probe.clear();
        probe.writeNbt(tag);
        NbtAccounter accounter = new NbtAccounter(Long.MAX_VALUE);
        probe.readNbt(accounter);
        return accounter.getUsage();
    }
}

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
 * 客户端 readNbt 的 2 MiB 配额，导致解码失败并踢出玩家。
 * 这里按客户端 NbtAccounter 的实际记账量重新裁剪：历史由 DeathManager 按时间
 * 倒序提供，优先保留最新记录的装备，超限后只保留历史条目的元数据；反射失败时
 * 退回原编码。
 */
@Pseudo
@Mixin(targets = "de.maxhenkel.corpse.net.MessageOpenHistory", remap = false)
public abstract class MessageOpenHistoryMixin {

    @Unique
    private static final long gtocore$MAX_NBT_USAGE = 2_000_000L;
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
        long usage = gtocore$accountedUsage(wrapper);
        boolean withoutEquipment = false;
        try {
            for (Object death : history) {
                CompoundTag entry = (CompoundTag) method.invoke(death, false);
                if (!withoutEquipment) {
                    long entryUsage = gtocore$accountedUsage(entry);
                    if (usage + entryUsage <= gtocore$MAX_NBT_USAGE) {
                        list.add(entry);
                        usage += entryUsage + gtocore$LIST_ELEMENT_USAGE;
                        continue;
                    }
                    withoutEquipment = true;
                }
                entry.remove("Equipment");
                long entryUsage = gtocore$accountedUsage(entry);
                if (usage + entryUsage > gtocore$MAX_NBT_USAGE) {
                    break;
                }
                list.add(entry);
                usage += entryUsage + gtocore$LIST_ELEMENT_USAGE;
            }
        } catch (ReflectiveOperationException e) {
            return;
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
    private static long gtocore$accountedUsage(CompoundTag tag) {
        FriendlyByteBuf probe = new FriendlyByteBuf(Unpooled.buffer());
        probe.writeNbt(tag);
        NbtAccounter accounter = new NbtAccounter(Long.MAX_VALUE);
        probe.readNbt(accounter);
        return accounter.getUsage();
    }
}

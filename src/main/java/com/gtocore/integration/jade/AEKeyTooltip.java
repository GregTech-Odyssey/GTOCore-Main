package com.gtocore.integration.jade;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyMap;

import snownee.jade.api.ITooltip;

/**
 * Jade 里展示一批「AE 物品键 → 数量」：服务端把键自己的 tag 与数量写进机器数据，客户端按本地语言渲染
 * 小图标 + 名称，所以物品名不会固定成服务端语言。
 * <p>
 * NBT 形状与 {@code MEPatternBufferPartMachine} 的 {@code writeBufferTag}/{@code readBufferTag} 一致。
 */
public final class AEKeyTooltip {

    private static final String AMOUNT = "real";

    public static final String PENDING = "gtocore_pending_outputs";

    private AEKeyTooltip() {}

    public static void write(CompoundTag data, String name, AEKeyMap<AEItemKey> keys) {
        if (keys.isEmpty()) return;
        var list = new ListTag();
        for (var entry : keys) {
            var key = entry.getKey();
            var amount = entry.getLongValue();
            if (key == null || amount < 1) continue;
            var tag = key.toTag();
            tag.putLong(AMOUNT, amount);
            list.add(tag);
        }
        if (!list.isEmpty()) data.put(name, list);
    }

    public static void read(ITooltip tooltip, CompoundTag data, String name) {
        var list = data.getList(name, Tag.TAG_COMPOUND);
        if (list.isEmpty()) return;
        var helper = tooltip.getElementHelper();
        for (Tag t : list) {
            if (!(t instanceof CompoundTag tag)) continue;
            var key = AEItemKey.fromTag(tag);
            if (key == null) continue;
            var amount = tag.getLong(AMOUNT);
            if (amount < 1) continue;
            tooltip.add(helper.smallItem(key.getReadOnlyStack()));
            tooltip.append(Component.literal(" ")
                    .append(Component.literal(FormattingUtil.formatNumbers(amount)).withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal("× ").withStyle(ChatFormatting.WHITE))
                    .append(key.getDisplayName().copy().withStyle(ChatFormatting.GOLD)));
        }
    }
}

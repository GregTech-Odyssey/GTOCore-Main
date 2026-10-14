package com.gtocore.integration.ae.wireless;

import net.minecraft.core.UUIDUtil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

/**
 * 一个无线网络的持久化数据（不可变）。运行时连接由 {@link WirelessHub} 维护，这里只有元数据。
 *
 * @param id      网络 id（UUID 字符串；由旧格式迁移来的网络沿用旧 id），机器与配置器物品里存的就是它
 * @param owner   创建者
 * @param name    显示名称
 * @param created 创建时间（epoch 毫秒，迁移来的网络为 0）
 */
public record WirelessNetwork(String id, UUID owner, String name, long created) {

    public static final Codec<WirelessNetwork> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(WirelessNetwork::id),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(WirelessNetwork::owner),
            Codec.STRING.fieldOf("name").forGetter(WirelessNetwork::name),
            Codec.LONG.fieldOf("created").forGetter(WirelessNetwork::created))
            .apply(instance, WirelessNetwork::new));

    /** 该玩家能否使用（加入、改名、删除、收藏）这个网络：创建者本人或与其同队。 */
    public boolean canUse(UUID player) {
        return WirelessPermissions.sameTeam(owner, player);
    }

    public WirelessNetwork withName(String newName) {
        return new WirelessNetwork(id, owner, newName, created);
    }
}

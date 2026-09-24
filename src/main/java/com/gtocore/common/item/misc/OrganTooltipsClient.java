package com.gtocore.common.item.misc;

import com.gtolib.api.player.IEnhancedPlayer;
import com.gtolib.api.player.OrganInventory;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * {@link OrganTooltips} 里只在客户端可用的部分，单独成类避免服务端加载客户端类。
 */
@OnlyIn(Dist.CLIENT)
final class OrganTooltipsClient {

    private OrganTooltipsClient() {}

    /** 本地玩家身上的器官（由服务端同步）；还没进入世界时为 null。 */
    @Nullable
    static OrganInventory localOrgans() {
        var player = Minecraft.getInstance().player;
        return player instanceof IEnhancedPlayer enhanced ? enhanced.getPlayerData().organs : null;
    }
}

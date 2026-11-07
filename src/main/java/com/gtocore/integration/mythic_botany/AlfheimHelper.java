package com.gtocore.integration.mythic_botany;

import com.gtolib.api.player.IEnhancedPlayer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import vazkii.botania.common.alfheim.AlfheimAccess;

import java.util.Set;

/**
 * 亚尔夫海姆准入的对外入口：服务端判定在 {@link AlfheimAccess}，这里提供客户端同步状态给 EMI 页面用。
 */
public class AlfheimHelper {

    /** 是否满足进入亚尔夫海姆的完整条件（含创造与旁观放行）。 */
    public static boolean gtocore$canPlayerUsePortal(ServerPlayer player) {
        return AlfheimAccess.canEnter(player);
    }

    /** 客户端同步状态：诗之知识与十二遗物都已满足。 */
    public static boolean clientCanPlayerUsePortal(Player player) {
        var data = IEnhancedPlayer.of(player).getPlayerData();
        return data.mythicBotKnowledgeState && data.mythicBotRelicState;
    }

    public static boolean clientPlayerHasKnowledge(Player player) {
        return IEnhancedPlayer.of(player).getPlayerData().mythicBotKnowledgeState;
    }

    public static Set<Item> itemObtained(Player player) {
        return AlfheimAccess.itemObtained(player);
    }
}

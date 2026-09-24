package com.gtocore.integration.ae.wireless;

import com.gtocore.integration.Mods;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import dev.ftb.mods.ftbteams.api.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;

/**
 * 无线网络的服务端事件：主世界卸载时丢弃 hub 引用；FTB 队伍变动（加入、退出、被踢、解散）后，
 * 断开主人已无权使用所在网络的成员，并向在线玩家重推各自可访问的网络列表。
 */
public final class WirelessEvents {

    private WirelessEvents() {}

    /** mod 构造期调用一次。 */
    public static void init() {
        WirelessSync.init();
        MinecraftForge.EVENT_BUS.addListener(WirelessEvents::onLevelUnload);
        if (Mods.FTBTEAMS.isLoaded()) Teams.register();
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) WirelessHub.clearAll();
    }

    /** 与 FTB Teams 相关的类只在它加载时才触碰。 */
    private static final class Teams {

        static void register() {
            TeamEvent.PLAYER_CHANGED.register(Teams::onPlayerChanged);
        }

        private static void onPlayerChanged(PlayerChangedTeamEvent event) {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            var networks = WirelessNetworks.get(server);
            if (!networks.isAvailable()) return;
            networks.markTeamsChanged();
            WirelessHub.detachUnauthorized(networks);
            WirelessSync.pushTo(server.getPlayerList().getPlayers());
        }
    }
}

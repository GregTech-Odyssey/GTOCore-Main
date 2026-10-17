package com.gtocore.integration.ae.wireless;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.server.level.ServerPlayer;

import com.hepdd.gtmthings.utils.TeamUtil;

import java.util.UUID;

/**
 * 无线网络的两层权限：能否管理这台机器（{@link #canManage}），能否使用目标网络（{@link WirelessNetwork#canUse}）。
 * 所有服务端操作都以<strong>发起操作的玩家</strong>判定，不再以机器主人身份代行。
 */
public final class WirelessPermissions {

    private WirelessPermissions() {}

    /**
     * 能否改动这台机器的无线设置：机器主人、与主人同队、或 OP（与 GTM 打开主人机器界面的 OP 等级一致）。
     * 没有主人的机器（极少见：GTM 放置和首次右键都会补上主人）任何玩家都可管理，网络权限则按该玩家判定。
     */
    public static boolean canManage(ServerPlayer player, MetaMachine machine) {
        var owner = machine.getOwnerUUID();
        if (owner == null || sameTeam(owner, player.getUUID())) return true;
        return player.hasPermissions(ConfigHolder.INSTANCE.machines.ownerOPBypass);
    }

    /** 同一玩家，或同属一个 FTB 队伍（没有队伍时队伍 id 就是玩家自身 UUID）。 */
    public static boolean sameTeam(UUID a, UUID b) {
        return a.equals(b) || TeamUtil.getTeamUUID(a).equals(TeamUtil.getTeamUUID(b));
    }
}

package com.gtocore.common.machine.noenergy.game.milestone;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;

/**
 * 游戏机的游玩权限：主人始终能玩，其余玩家看机器上的「允许其他玩家游玩」开关。
 *
 * <p>
 * 「主人」沿 GTM 的 {@code MachineOwner} 语义：放置者本身、同队/好友、以及有权限（等级 2）的管理员；没有主人的机器
 * （命令放置、从没被右键过）谁都能玩。
 */
public final class GameAccess {

    private GameAccess() {}

    public static boolean isOwner(MetaMachine machine, Player player) {
        var ownerUUID = machine.getOwnerUUID();
        if (ownerUUID == null || ownerUUID.equals(player.getUUID())) return true;
        if (player.hasPermissions(2)) return true;
        var owner = machine.getOwner();
        return owner != null && (owner.isPlayerInTeam(player) || owner.isPlayerFriendly(player));
    }

    /** 假玩家（自动化/炮台之类）一律不能玩。 */
    public static boolean canPlay(MetaMachine machine, boolean allowOthers, Player player) {
        if (player instanceof FakePlayer) return false;
        return allowOthers || isOwner(machine, player);
    }
}

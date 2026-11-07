package vazkii.botania.common.alfheim;

import net.minecraft.server.level.ServerPlayer;

/** 未满足准入条件却出现在亚尔夫海姆的玩家由空间法则击杀。 */
public final class AlfheimTrespass {

    private AlfheimTrespass() {}

    public static void tick(ServerPlayer player) {
        if (!Alfheim.isAlfheim(player.level()) || !player.isAlive()) {
            return;
        }
        if (AlfheimAccess.canEnter(player)) {
            return;
        }
        player.kill();
    }
}

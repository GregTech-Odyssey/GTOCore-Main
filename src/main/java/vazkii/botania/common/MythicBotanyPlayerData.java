package vazkii.botania.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

/**
 * 神话植物学的玩家进度。键名与存放位置沿用原模组，已写入的存档换回原模组后仍然有效。
 */
public final class MythicBotanyPlayerData {

    public static final String TAG = "MythicBotanyPlayerInfo";
    public static final String KVASIR_KNOWLEDGE = "KvasirKnowledge";
    public static final String MIMIR_KNOWLEDGE = "MimirKnowledge";
    public static final String ENTER_ALFHEIM = "enterAlfheim";

    private MythicBotanyPlayerData() {}

    public static boolean hasKvasirKnowledge(Player player) {
        return info(player).getBoolean(KVASIR_KNOWLEDGE);
    }

    /** @return 本次是否新获得诗之知识 */
    public static boolean grantKvasirKnowledge(Player player) {
        CompoundTag info = info(player);
        if (info.getBoolean(KVASIR_KNOWLEDGE)) {
            return false;
        }
        info.putBoolean(KVASIR_KNOWLEDGE, true);
        return true;
    }

    public static boolean hasMimirKnowledge(Player player) {
        return info(player).getBoolean(MIMIR_KNOWLEDGE);
    }

    /** @return 本次是否新获得密米尔的知识 */
    public static boolean grantMimirKnowledge(Player player) {
        CompoundTag info = info(player);
        if (info.getBoolean(MIMIR_KNOWLEDGE)) {
            return false;
        }
        info.putBoolean(MIMIR_KNOWLEDGE, true);
        return true;
    }

    public static boolean hasEnteredAlfheim(Player player) {
        return info(player).getBoolean(ENTER_ALFHEIM);
    }

    public static void markEnteredAlfheim(Player player) {
        info(player).putBoolean(ENTER_ALFHEIM, true);
    }

    private static CompoundTag info(Player player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag info = root.getCompound(TAG);
        if (!root.contains(TAG, Tag.TAG_COMPOUND)) {
            root.put(TAG, info);
        }
        return info;
    }
}

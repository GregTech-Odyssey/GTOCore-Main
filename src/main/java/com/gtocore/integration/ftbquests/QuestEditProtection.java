package com.gtocore.integration.ftbquests;

import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

public final class QuestEditProtection {

    private static Boolean prohibit;

    private QuestEditProtection() {}

    public static boolean canEdit() {
        if (prohibit == null && ServerQuestFile.INSTANCE != null) {
            prohibit = false;
            for (var c : ServerQuestFile.INSTANCE.getAllChapters()) {
                if (c.getRawTitle().contains("gto")) {
                    prohibit = true;
                    break;
                }
            }
        }
        return prohibit != null && !prohibit;
    }
}

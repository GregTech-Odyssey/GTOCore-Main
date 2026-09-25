package com.gtocore.integration.ftbquests;

import com.gtocore.data.transaction.manager.TradeData;

import com.hepdd.gtmthings.utils.TeamUtil;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

/** FTB Quests integration used only when the optional mod is present at runtime. */
public final class QuestTradeIntegration {

    private QuestTradeIntegration() {}

    public static boolean hasCompletedQuest(TradeData data, long questId) {
        if (data.uuid() == null || ServerQuestFile.INSTANCE == null) return false;
        var questFile = ServerQuestFile.INSTANCE;
        var quest = questFile.getQuest(questId);
        var teamData = questFile.getNullableTeamData(TeamUtil.getTeamUUID(data.uuid()));
        return quest != null && teamData != null && teamData.isCompleted(quest);
    }
}

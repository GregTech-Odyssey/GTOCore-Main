package com.gtocore.integration.ftbquests;

import com.gtocore.data.transaction.manager.TradeData;

import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.utils.TeamUtil;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

import java.util.Locale;

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

    public static Component questRequirement(long questId) {
        String id = String.format(Locale.ROOT, "%016X", questId);
        if (ServerQuestFile.INSTANCE == null) {
            return Component.translatable("gtocore.trade.quest_requirement_id", id);
        }
        var quest = ServerQuestFile.INSTANCE.getQuest(questId);
        if (quest == null) {
            return Component.translatable("gtocore.trade.quest_requirement_id", id);
        }
        Component title = quest.getTitle();
        return title.getString().isBlank() ? Component.translatable("gtocore.trade.quest_requirement_id", id) : Component.translatable("gtocore.trade.quest_requirement", title);
    }
}

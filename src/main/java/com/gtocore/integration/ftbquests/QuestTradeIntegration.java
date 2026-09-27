package com.gtocore.integration.ftbquests;

import com.gtocore.data.transaction.manager.TradeData;

import net.minecraft.network.chat.Component;

import com.hepdd.gtmthings.utils.TeamUtil;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;

/** FTB Quests integration used only when the optional mod is present at runtime. */
public final class QuestTradeIntegration {

    private QuestTradeIntegration() {}

    /**
     * 检查当前交易玩家所在队伍是否已完成指定任务。
     *
     * @param data    当前交易数据
     * @param questId FTB Quests 任务 ID
     * @return 任务、队伍数据均存在且任务已完成时返回 {@code true}
     */
    public static boolean hasCompletedQuest(TradeData data, long questId) {
        if (data.uuid() == null || ServerQuestFile.INSTANCE == null) return false;
        var questFile = ServerQuestFile.INSTANCE;
        var quest = questFile.getQuest(questId);
        var teamData = questFile.getNullableTeamData(TeamUtil.getTeamUUID(data.uuid()));
        return quest != null && teamData != null && teamData.isCompleted(quest);
    }

    /**
     * 获取任务标题；任务书尚未加载、任务不存在或标题为空时回退为任务 ID。
     *
     * @param questId FTB Quests 任务 ID
     * @param id      已格式化、用于回退显示的任务 ID
     * @return 任务标题或任务 ID 组件
     */
    public static Component questName(long questId, String id) {
        if (ServerQuestFile.INSTANCE == null) {
            return Component.translatable("gtocore.trade.quest_id", id);
        }
        var quest = ServerQuestFile.INSTANCE.getQuest(questId);
        if (quest == null) {
            return Component.translatable("gtocore.trade.quest_id", id);
        }
        Component title = quest.getTitle();
        return title.getString().isBlank() ? Component.translatable("gtocore.trade.quest_id", id) : title;
    }
}

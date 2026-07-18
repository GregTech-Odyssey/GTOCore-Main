package com.gtocore.integration.emi.research;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.data.recipe.research.AnalyzeData;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@DataGeneratorScanned
public class ResearchEmiNameHelper {

    @RegisterLanguage(cn = "数据扫描", en = "Data Scanning")
    static final String CATEGORY_NAME = "gtocore.research.data_scanning";
    @RegisterLanguage(cn = "获得此项研究的尤里卡", en = "Eureka for this research")
    static final String EUREKA_NAME = "gtocore.research.eureka";
    @RegisterLanguage(cn = "团队总量：%s", en = "Team Total: %s")
    static final String TEAM_TOTAL_NAME = "gtocore.research.team_total";
    @RegisterLanguage(cn = "科技节点[%s]", en = "Tech Node [%s]")
    static final String TECH_NODE_NAME = "gtocore.research.tech_node";
    @RegisterLanguage(cn = "[%s]领域数据", en = "[%s] Domain Data")
    static final String DOMAIN_DATA_NAME = "gtocore.research.domain_data";

    public static Component getResearchTagTeamTotal(ResearchTag tag) {
        var plr = Minecraft.getInstance().player;
        if (plr != null) {
            var ctx = TeamResearchSavedDtat.getOrCreateContext(plr);
            return Component.translatable(TEAM_TOTAL_NAME, ctx.getResearchPoints().getLong(tag)).withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable(TEAM_TOTAL_NAME, 0L).withStyle(ChatFormatting.GRAY);
    }

    public static Component getTechNodeState(TechNode tag) {
        var plr = Minecraft.getInstance().player;
        if (plr != null) {
            var ctx = TechTreeSavedData.findTree(plr, AnalyzeData.TechTree).getUnlockedNodes().contains(tag);
            return Component.translatable("gtocore.techtree.widget.status." + (ctx ? "unlocked" : "locked"))
                    .withStyle(ctx ? ChatFormatting.GREEN : ChatFormatting.GOLD);
        }
        return Component.empty();
    }
}

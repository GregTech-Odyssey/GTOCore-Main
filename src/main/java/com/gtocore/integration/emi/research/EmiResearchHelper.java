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

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;

import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@DataGeneratorScanned
public class EmiResearchHelper {

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
    @RegisterLanguage(cn = "领域数据可由扫描站扫描存储了此领域数据的晶片获得", en = "Domain Data can be obtained by scanning the chip that stores this domain data with a scanning station")
    static final String DOMAIN_DATA_DESC = "gtocore.research.domain_data.desc";
    @RegisterLanguage(cn = "可累计到团队总量，并用于解锁科技节点", en = "Can be accumulated to the team total and used to unlock tech nodes")
    static final String TEAM_TOTAL_DESC = "gtocore.research.team_total.desc";

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

    public static @Nullable EmiStack toEmiStack(AEKey key) {
        return EmiStackHelper.toEmiStack(new GenericStack(key, key.getAmountPerOperation()));
    }

    public static List<EmiStack> toEmiStacks(Collection<AEKey> keys) {
        return keys.stream()
                .map(EmiResearchHelper::toEmiStack)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing((EmiStack stack) -> stack.getId().toString())
                        .thenComparing(stack -> stack.getKey().getClass().getName()))
                .toList();
    }
}

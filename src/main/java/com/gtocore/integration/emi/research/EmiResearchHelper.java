package com.gtocore.integration.emi.research;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchSavedData;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.api.research.techtree.ui.TechTreeView;
import com.gtocore.common.machine.electric.ScannerMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;

import com.hepdd.gtmthings.utils.TeamUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@DataGeneratorScanned
public final class EmiResearchHelper {

    @RegisterLanguage(cn = "数据扫描", en = "Data Scanning")
    static final String CATEGORY_NAME = "gtocore.research.data_scanning";
    @RegisterLanguage(cn = "获得此项研究的尤里卡", en = "Eureka for this research")
    static final String EUREKA_NAME = "gtocore.research.eureka";
    @RegisterLanguage(cn = "团队总量：%s", en = "Team Total: %s")
    static final String TEAM_TOTAL_NAME = "gtocore.research.team_total";
    @RegisterLanguage(cn = "科技节点[%s]", en = "Tech Node [%s]")
    static final String TECH_NODE_NAME = "gtocore.research.tech_node";
    @RegisterLanguage(cn = "[%s]研究点数", en = "[%s] Research Points")
    static final String DOMAIN_DATA_NAME = "gtocore.research.domain_data";
    @RegisterLanguage(cn = "研究点数可由扫描站扫描存储了此领域数据的晶片获得", en = "Research Points can be obtained by scanning the chip that stores this domain data with a scanning station")
    static final String DOMAIN_DATA_DESC = "gtocore.research.domain_data.desc";
    @RegisterLanguage(cn = "可累计到团队总量，并用于解锁科技节点", en = "Can be accumulated to the team total and used to unlock tech nodes")
    static final String TEAM_TOTAL_DESC = "gtocore.research.team_total.desc";
    @RegisterLanguage(cn = "每一份[%s]需要占用%s晶片存储空间", en = "Each [%s] requires %s chip storage space")
    static final String DOMAIN_DATA_STORAGE = "gtocore.research.domain_data.storage";
    @RegisterLanguage(cn = "(共%s份，占用%s)", en = "(Total %s pieces, occupying %s)")
    static final String DOMAIN_DATA_STORAGE_TOTAL = "gtocore.research.domain_data.storage.total";
    @RegisterLanguage(cn = "这件物品已经被扫描，重复扫描仅能获得%s%%的研究点数", en = "This item has already been scanned, and repeated scanning can only obtain %s%% of the research points")
    static final String DOMAIN_DATA_STORAGE_REPEAT = "gtocore.research.domain_data.storage.repeat";
    @RegisterLanguage(cn = "这件物品还没有被扫描", en = "This item has not been scanned yet")
    static final String DOMAIN_DATA_STORAGE_NOT_SCANNED = "gtocore.research.domain_data.storage.not_scanned";
    @RegisterLanguage(cn = "打开科技树", en = "Open Tech Tree")
    static final String OPEN_TECH_TREE = "gtocore.research.open_tech_tree";
    @RegisterLanguage(cn = "扫描耗能", en = "Scan Power")
    static final String SCAN_POWER = "gtocore.research.scan_power";
    @RegisterLanguage(cn = "扫描状态", en = "Scan Status")
    static final String SCAN_STATE = "gtocore.research.scan_state";
    @RegisterLanguage(cn = "未扫描", en = "Not scanned")
    static final String SCAN_STATE_NEW = "gtocore.research.scan_state.new";
    @RegisterLanguage(cn = "已扫描，收益 %s%%", en = "Scanned, %s%% yield")
    static final String SCAN_STATE_REPEAT = "gtocore.research.scan_state.repeat";
    @RegisterLanguage(cn = "尤里卡：%s", en = "Eureka: %s")
    static final String EUREKA_UNLOCK = "gtocore.research.eureka_unlock";

    public static Component getResearchTagTeamTotal(ResearchTag tag) {
        var plr = Minecraft.getInstance().player;
        if (plr != null) {
            var ctx = TeamResearchSavedData.getOrCreateContext(plr);
            return Component.translatable(TEAM_TOTAL_NAME, ctx.researchPoints().getLong(tag)).withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable(TEAM_TOTAL_NAME, 0L).withStyle(ChatFormatting.GRAY);
    }

    public static Component getTechNodeState(TechNode node) {
        var plr = Minecraft.getInstance().player;
        if (plr != null) {
            var plrTree = TechTreeSavedData.findTree(plr, node.getManager());
            if (plrTree != null) {
                var ctx = plrTree.getUnlockedNodes().contains(node);
                return Component.translatable(ctx ? TechTreeView.STATUS_UNLOCKED : TechTreeView.STATUS_LOCKED)
                        .withStyle(ctx ? ChatFormatting.GREEN : ChatFormatting.GOLD);
            }
        }
        return Component.empty();
    }

    public static @Nullable EmiStack toEmiStack(AEKey key) {
        return EmiStackHelper.toEmiStack(new GenericStack(key, key.getAmountPerOperation()));
    }

    public static void openTechNode(TechNode node) {
        var recipe = EmiApi.getRecipeManager().getRecipe(TechTreeEmiRecipe.recipeId(node));
        if (recipe != null) EmiApi.displayRecipe(recipe);
        else EmiApi.displayUses(new TechNodeEmiStack(node));
    }

    public static long getScannerEUt(AEKey key) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        var bytes = DataScanningManager.scanData(key, TeamUtil.getTeamUUID(player.getUUID()), true).countBytes();
        return ScannerMachine.eut(bytes);
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

package com.gtocore.mixin.ftbxmodcompat;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbxmodcompat.ftbquests.recipemod_common.WrappedQuest;
import dev.ftb.mods.ftbxmodcompat.ftbquests.recipemod_common.WrappedQuestCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WrappedQuestCache.class)
public class WrappedQuestCacheMixin {

    @Shadow(remap = false)
    @Final
    private List<WrappedQuest> wrappedQuestsCache;

    @Inject(method = "lambda$rebuildWrappedQuestCache$1", at = @At("HEAD"), remap = false, cancellable = true)
    private void gtocore$includeVisibleQuests(Quest quest, CallbackInfo ci) {
        TeamData teamData = ClientQuestFile.INSTANCE.selfTeamData;
        if (quest.isVisible(teamData) && quest.showInRecipeMod()) {
            wrappedQuestsCache.add(new WrappedQuest(quest, gtocore$getVisibleRewards(quest)));
        }

        ci.cancel();
    }

    @Unique
    private static List<Reward> gtocore$getVisibleRewards(Quest quest) {
        return quest.getRewards().stream()
                .filter(reward -> reward.getAutoClaimType() != RewardAutoClaim.INVISIBLE && reward.getIcon().getIngredient() != null)
                .toList();
    }
}

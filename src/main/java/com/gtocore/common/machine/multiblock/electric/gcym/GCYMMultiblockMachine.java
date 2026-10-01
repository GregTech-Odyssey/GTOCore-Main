package com.gtocore.common.machine.multiblock.electric.gcym;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;

public class GCYMMultiblockMachine extends TierCasingMultiblockMachine implements IIssueProvider {

    private int energyTier;

    public GCYMMultiblockMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        energyTier = tier;
        tier = Math.min(getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER), tier);
    }

    @Override
    public void collectIssues(IssueSink sink) {
        if (isFormed() && tier < energyTier) IdleReason.FRAMEWORK_TIER_LIMIT.collect(sink, tier, energyTier);
    }

    @Override
    public boolean gtolib$canUpgraded() {
        return true;
    }
}

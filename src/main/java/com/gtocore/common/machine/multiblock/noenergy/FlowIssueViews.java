package com.gtocore.common.machine.multiblock.noenergy;

import com.gregtechceu.gtceu.api.machine.feature.IVoidable;
import com.gregtechceu.gtceu.api.machine.issue.DiagnosisEntry;
import com.gregtechceu.gtceu.api.machine.issue.DiagnosisResult;
import com.gregtechceu.gtceu.api.machine.issue.GTIssues;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.uiwidgets.flow.IssueView;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeDiagnoser;
import com.gregtechceu.gtceu.uiwidgets.flow.RecipeIssue;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

/**
 * 鸿蒙之眼、中子活化器流程页共用的节点视图与说明句：状态取自统一诊断结果的条目与问题。
 */
final class FlowIssueViews {

    private FlowIssueViews() {}

    @Nullable
    static IssueView inputProblem(DiagnosisResult result) {
        for (var entry : result.entries()) {
            if (entry.subject().io() != IO.IN || !isMaterial(entry) || !entry.isProblem()) continue;
            return IssueView.of(entry, result.recipe());
        }
        return null;
    }

    @Nullable
    static IssueView output(DiagnosisResult result, IVoidable machine, boolean working, int units) {
        DiagnosisEntry blocked = null, missing = null;
        int total = 0, voided = 0;
        boolean items = false, fluids = false;
        for (var entry : result.entries()) {
            if (entry.subject().io() != IO.OUT || !isMaterial(entry)) continue;
            if (entry.subject().capability() == ItemRecipeInfo.INSTANCE) items = true;
            else fluids = true;
            switch (entry.state()) {
                case OK -> total++;
                case VOIDED -> {
                    total++;
                    voided++;
                }
                case MISSING_HANDLER -> {
                    if (missing == null) missing = entry;
                }
                case SKIPPED -> {}
                default -> {
                    if (blocked == null) blocked = entry;
                }
            }
        }
        if (!working) {
            var full = result.issue(GTIssues.OUTPUT_FULL);
            if (full != null) return IssueView.of(full);
        }
        if (blocked != null) return IssueView.of(blocked, result.recipe());
        if (missing != null) return RecipeIssue.NO_OUTPUT_HATCH.view();
        if (voided > 0) return RecipeIssue.OUTPUT_VOIDED.view();
        boolean voidItems = machine.canVoidRecipeOutputs(ItemRecipeInfo.INSTANCE);
        boolean voidFluids = machine.canVoidRecipeOutputs(FluidRecipeInfo.INSTANCE);
        boolean voiding = items || fluids ? (!items || voidItems) && (!fluids || voidFluids) : voidItems && voidFluids;
        if (total == 0 && units == 0) return voiding ? RecipeIssue.OUTPUT_VOIDED.view() : RecipeIssue.NO_OUTPUT_HATCH.view();
        if (voiding) return working ? RecipeIssue.OUTPUT_VOID_OVERFLOW_ACTIVE.view() : RecipeIssue.OUTPUT_VOID_OVERFLOW.view();
        if (working) return RecipeIssue.OUTPUT_ACTIVE.view();
        return null;
    }

    static Component sentence(IssueView view, @Nullable String description) {
        var text = description != null ? Component.translatable(description) : view.description().copy();
        return text.withStyle(RecipeDiagnoser.style(view.issue()));
    }

    private static boolean isMaterial(DiagnosisEntry entry) {
        var capability = entry.subject().capability();
        return capability == ItemRecipeInfo.INSTANCE || capability == FluidRecipeInfo.INSTANCE;
    }
}

package com.gtocore.common.machine.multiblock.electric.nano;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiModule;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage.NO_VALUE;

public final class NanitesModuleMachine extends ElectricMultiblockMachine implements IMultiModule<NanitesIntegratedMachine>, IIssueProvider {

    @Getter
    private NanitesIntegratedMachine controller;

    private final int type;

    public NanitesModuleMachine(MetaMachineBlockEntity holder, int type) {
        super(holder);
        this.type = type;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (controller != null) {
            controller.module.add(type);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (controller != null) {
            controller.module.remove(type);
        }
    }

    @Override
    public GTRecipe fullModifyRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipeDefinition definition) {
        if (controller == null) {
            IdleReason.NANITES_NOT_CONNECTED.report(this, IssueStage.MODIFIER, definition);
            return null;
        }
        var recipe = super.fullModifyRecipe(unit, definition);
        if (recipe != null) {
            NanitesIntegratedMachine.trimRecipe(recipe, controller.chance);
            return recipe;
        }
        return null;
    }

    @Override
    public boolean checkConditions(RecipeHandlerUnit unit, @NotNull GTRecipeDefinition recipe) {
        var controller = this.controller;
        if (controller == null) {
            IdleReason.NANITES_NOT_CONNECTED.report(this, IssueStage.CONDITION, recipe);
            return false;
        }
        int temperature = recipe.data.getInt(GTRecipeDataKeys.EBF_TEMP);
        if (temperature > controller.getTemperature()) {
            IdleReason.INSUFFICIENT_TEMPERATURE.report(this, IssueStage.CONDITION, temperature, controller.getTemperature(), recipe);
            return false;
        }
        if (recipe.data.getInt(GTORecipeDataKeys.MODULE) != type) {
            IdleReason.NOT_APPLICABLE.report(this, IssueStage.CONDITION, recipe);
            return false;
        }
        return super.checkConditions(unit, recipe);
    }

    @Override
    public void collectIssues(IssueSink sink) {
        if (controller == null) IdleReason.NANITES_NOT_CONNECTED.collect(sink);
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (controller != null) {
            textList.add(Component.translatable("gtocore.machine.nanites_module.connected"));
            if (MultiblockPage.isScreenText()) return;
            textList.add(Component.translatable("tooltip.emi.chance.consume", 100 - controller.chance));
            textList.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature", Component.literal(FormattingUtil.formatNumbers(controller.getTemperature()) + "K").setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
        } else {
            textList.add(Component.translatable("gtocore.machine.nanites_module.not_connected"));
        }
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addLine(NanitesIntegratedMachine.CONSUME_CHANCE, MultiblockPage.cached(this::controllerConsumeChance, value -> value < 0 ? NO_VALUE : Component.literal(value + "%")));
        page.addReading("gtceu.multiblock.blast_furnace.max_temperature", MultiblockPage.cached(this::controllerTemperature, value -> value < 0 ? NO_VALUE : Component.literal(FormattingUtil.formatNumbers(value) + " K")));
    }

    private long controllerConsumeChance() {
        var c = controller;
        return c == null ? -1 : 100 - c.chance;
    }

    private long controllerTemperature() {
        var c = controller;
        return c == null ? -1 : c.getTemperature();
    }

    @Override
    public void setController(NanitesIntegratedMachine controller) {
        this.controller = controller;
        if (isFormed && controller != null) controller.module.add(type);
    }
}

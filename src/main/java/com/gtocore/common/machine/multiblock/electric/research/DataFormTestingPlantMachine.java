package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@DataGeneratorScanned
public class DataFormTestingPlantMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder, IResearchPointsOperation {

    @SaveToDisk
    private final Set<AEKey> containedKeys = new ReferenceOpenHashSet<>();
    @SaveToDisk(defaultValue = "0")
    private long nextTestBytes = 0;
    @SaveToDisk(defaultValue = "0")
    private int testLevel = 0;
    @SaveToDisk
    private AEKey currentKey = null;
    @SaveToDisk(defaultValue = "0")
    private long eut = 0;

    @SaveToDisk(defaultValue = "IDLE")
    private Mode mode = Mode.IDLE;

    public DataFormTestingPlantMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        mode = Mode.IDLE;
    }

    @Override
    public boolean searchRecipe() {
        return true;
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (mode == Mode.ANALYZING) {
            return RecipeBuilder.ofRaw()
                    .circuitMeta(1)
                    .EUt(eut)
                    .duration(900)
                    .researchPoints(ResearchTag.DATA_STORAGE, testLevel * testLevel).build();
        }
        return null;
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, GTRecipe recipe) {
        var lvl = recipe.data.getOrDefaultData(GTORecipeDataKeys.DATA_TESTING_LEVEL, 0);
        if (lvl > 0) {
            testLevel = lvl;
            nextTestBytes = getNextTestBytes();
            mode = Mode.TESTING;
            eut = recipe.eut;
        }
        super.beforeWorking(unit, recipe);
    }

    @Override
    public void regressRecipe(RecipeLogic recipeLogic) {
        if (mode == Mode.TESTING) {
            setWorkingEnabled(false);
            recipeLogic.resetRecipeLogic();
            mode = Mode.IDLE;
            nextTestBytes = 0;
            testLevel = 0;
            currentKey = null;
            eut = 0;
        } else super.regressRecipe(recipeLogic);
    }

    private long getNextTestBytes() {
        if (testLevel <= 0) return 0;
        return 1L << (11 + testLevel);
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public void afterWorking() {
        if (mode == Mode.TESTING) {
            containedKeys.clear();
            nextTestBytes = 0;
            mode = Mode.ANALYZING;
        } else if (mode == Mode.ANALYZING) {
            testLevel = 0;
            mode = Mode.IDLE;
            eut = 0;
        }
        super.afterWorking();
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (mode == Mode.TESTING) {
            textList.add(Component.translatable(LANG_TESTING));
            textList.add(Component.translatable(LANG_TESTING_LEVEL, testLevel));
            textList.add(Component.translatable(LANG_TESTING_PROGRESS, currentKey == null ? Component.translatable("gtocore.data.empty") : currentKey.getDisplayName(),
                    FormattingUtil.formatNumberReadable(getNextTestBytes() - nextTestBytes),
                    FormattingUtil.formatNumberReadable(getNextTestBytes())));
            textList.add(Component.translatable(LANG_TESTING_USED_KEYS, ComponentUtils.formatList(
                    containedKeys.stream().map(AEKey::getDisplayName).toList(), Component.literal(", "))));
        } else if (mode == Mode.ANALYZING) {
            textList.add(Component.translatable(LANG_ANALYZING));
            textList.add(Component.translatable(LANG_EXPECTED_TOTAL_DATA, FormattingUtil.formatNumberReadable((long) testLevel * testLevel)));
        }
    }

    public long insert(AEKey what, long amount, Actionable act, IActionSource source) {
        if (!getRecipeLogic().isWorking() || mode != Mode.TESTING) return 0;
        if (currentKey == null) {
            if (containedKeys.contains(what)) return 0;
            if (act == Actionable.MODULATE) currentKey = what;
        } else if (currentKey != what) {
            return 0;
        }
        var toInsert = Math.min(amount, nextTestBytes * what.getAmountPerByte());
        if (act == Actionable.MODULATE) {
            nextTestBytes -= toInsert / what.getAmountPerByte();
            if (nextTestBytes <= 0) {
                containedKeys.add(currentKey);
                currentKey = null;
                testLevel++;
                nextTestBytes = getNextTestBytes();
            }
            if (recipeLogic.getProgress() > 1) recipeLogic.setProgress(1);
        }
        return toInsert;
    }

    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (!getRecipeLogic().isWorking() || mode != Mode.TESTING) return false;
        if (currentKey == null) {
            return !containedKeys.contains(what);
        } else {
            return currentKey == what;
        }
    }

    enum Mode {
        IDLE,
        ANALYZING,
        TESTING
    }

    @RegisterLanguage(cn = "压缩测试中...", en = "Compressing Test...")
    public static final String LANG_TESTING = "gtceu.machine.data_form_testing_plant.testing";
    @RegisterLanguage(cn = "当前测试等级: %s", en = "Current Testing Level: %s")
    public static final String LANG_TESTING_LEVEL = "gtceu.machine.data_form_testing_plant.testing_level";
    @RegisterLanguage(cn = "当前输入项<%s>已完成: %s/%s", en = "Current Input Item<%s> Completed: %s/%s")
    public static final String LANG_TESTING_PROGRESS = "gtceu.machine.data_form_testing_plant.testing_progress";
    @RegisterLanguage(cn = "已使用的输入项: %s", en = "Used Input Items: %s")
    public static final String LANG_TESTING_USED_KEYS = "gtceu.machine.data_form_testing_plant.testing_used_keys";
    @RegisterLanguage(cn = "分析中...", en = "Analyzing...")
    public static final String LANG_ANALYZING = "gtceu.machine.data_form_testing_plant.analyzing";
    @RegisterLanguage(cn = "预期数据总量: %s", en = "Expected Total Data: %s")
    public static final String LANG_EXPECTED_TOTAL_DATA = "gtceu.machine.data_form_testing_plant.expected_total_data";
}

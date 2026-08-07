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

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@DataGeneratorScanned
public class DataFormTestingPlantMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder, IResearchPointsOperation {

    @SaveToDisk
    private final ReferenceOpenHashSet<AEKey> containedKeys = new ReferenceOpenHashSet<>();
    @SaveToDisk
    private long remainingBytes = 0;
    @SaveToDisk
    private long initialRemainingBytes = 0;
    @SaveToDisk
    private int fragmentation = 0;
    @SaveToDisk
    private AEKey currentKey = null;
    @SaveToDisk
    private long eut = 0;
    @SaveToDisk
    private int points = 0;

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
            var dataAmount = containedKeys.size();
            var basePoints = Math.log(initialRemainingBytes) / Math.log(2);
            basePoints = basePoints * basePoints * dataAmount * 0.1;
            if (remainingBytes > 0) {
                var ratio = (double) (initialRemainingBytes - remainingBytes) / initialRemainingBytes;
                basePoints *= ratio * ratio;
            }
            points = (int) basePoints;
            var r = RecipeBuilder.ofRaw()
                    .EUt(eut)
                    .duration(400 + 20 * fragmentation)
                    .researchPoints(ResearchTag.DATA_STORAGE, (int) basePoints).build();
            containedKeys.clear();
            remainingBytes = initialRemainingBytes = 0;
            return r;
        }
        return null;
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, GTRecipe recipe) {
        var capacity = recipe.data.getOrDefaultData(GTORecipeDataKeys.DATA_TESTING_CAPACITY, 0);
        if (capacity > 0) {
            fragmentation = 0;
            remainingBytes = initialRemainingBytes = capacity;
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
            remainingBytes = initialRemainingBytes = 0;
            fragmentation = 0;
            currentKey = null;
            eut = 0;
        } else super.regressRecipe(recipeLogic);
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public void afterWorking() {
        if (mode == Mode.TESTING) {
            mode = Mode.ANALYZING;
        } else if (mode == Mode.ANALYZING) {
            fragmentation = 0;
            mode = Mode.IDLE;
            eut = 0;
            points = 0;
        }
        super.afterWorking();
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (mode == Mode.TESTING) {
            textList.add(Component.translatable(LANG_TESTING));
            textList.add(Component.translatable(LANG_TESTING_FRAGMENTATION, fragmentation));
            textList.add(Component.translatable(LANG_TESTING_PROGRESS, currentKey == null ? Component.translatable("gtocore.data.empty") : currentKey.getDisplayName()));
        } else if (mode == Mode.ANALYZING) {
            textList.add(Component.translatable(LANG_ANALYZING));
            textList.add(Component.translatable(LANG_EXPECTED_TOTAL_DATA, FormattingUtil.formatNumberReadable(points)));
        }
    }

    public long insert(AEKey what, long amount, Actionable act) {
        if (!getRecipeLogic().isWorking() || mode != Mode.TESTING) return 0;
        var toInsert = Math.min(amount, remainingBytes * what.getAmountPerByte());
        if (remainingBytes <= 0) {
            return 0;
        }
        if (act == Actionable.MODULATE) {
            int fragmentation = 0;
            if (currentKey == null) {
                fragmentation = 2;
            } else if (currentKey != what) {
                fragmentation = containedKeys.contains(what) ? 15 : 2;
            }
            var usedBytes = toInsert / what.getAmountPerByte();
            remainingBytes -= usedBytes;
            if (usedBytes > 0) {
                fragmentation += Math.min((int) Math.ceil((double) initialRemainingBytes / usedBytes) - 1, 60);
            } else {
                fragmentation += 60;
            }
            this.fragmentation += fragmentation;
            containedKeys.add(currentKey);
            currentKey = what;
            if (recipeLogic.getProgress() > 1) recipeLogic.setProgress(1);
        }
        return toInsert;
    }

    public boolean isPreferredStorageFor() {
        return !getRecipeLogic().isWorking() || mode != Mode.TESTING;
    }

    enum Mode {
        IDLE,
        ANALYZING,
        TESTING
    }

    @RegisterLanguage(cn = "压缩测试中...", en = "Compressing Test...")
    public static final String LANG_TESTING = "gtceu.machine.data_form_testing_plant.testing";
    @RegisterLanguage(cn = "当前碎片化等级: %s", en = "Current Fragmentation Level: %s")
    public static final String LANG_TESTING_FRAGMENTATION = "gtceu.machine.data_form_testing_plant.testing_fragmentation";
    @RegisterLanguage(cn = "当前输入项<%s>", en = "Current Input Item<%s>")
    public static final String LANG_TESTING_PROGRESS = "gtceu.machine.data_form_testing_plant.testing_progress";
    @RegisterLanguage(cn = "分析中...", en = "Analyzing...")
    public static final String LANG_ANALYZING = "gtceu.machine.data_form_testing_plant.analyzing";
    @RegisterLanguage(cn = "预期数据总量: %s", en = "Expected Total Data: %s")
    public static final String LANG_EXPECTED_TOTAL_DATA = "gtceu.machine.data_form_testing_plant.expected_total_data";
}

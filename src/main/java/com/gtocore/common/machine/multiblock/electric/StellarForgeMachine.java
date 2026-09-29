package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.mana.feature.IManaMultiblock;
import com.gtolib.api.machine.mana.trait.ManaTrait;
import com.gtolib.api.machine.multiblock.TierCasingMultiblockMachine;
import com.gtolib.api.misc.ManaContainerList;
import com.gtolib.api.recipe.extension.MANATRecipeExtension;
import com.gtolib.utils.explosion.SphereExplosion;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@DataGeneratorScanned
public final class StellarForgeMachine extends TierCasingMultiblockMachine implements IExplosionMachine, IManaMultiblock {

    @RegisterLanguage(cn = "内部压力：", en = "Internal Pressure: ")
    private static final String PRESSURE = "gtocore.machine.stellar_forge.pressure";

    @RegisterLanguage(cn = "魔法模块", en = "Magic Module")
    private static final String MAGIC_MODULE_NAME = "gtocore.multiblock.stellar_forge.magic_module";
    @RegisterLanguage(cn = "搭建后可运行产出魔力的配方，输出魔力仓只能安装在此结构上", en = "When built, mana-producing recipes can be run; output mana hatches can only be installed on this structure")
    private static final String MAGIC_MODULE_DESC = "gtocore.multiblock.stellar_forge.magic_module.desc";
    public static final ParamKey MAGIC_MODULE = ParamKey.of(MAGIC_MODULE_NAME, MAGIC_MODULE_DESC);

    @SaveToDisk(defaultValue = "0")
    private int pressure;

    private final ManaTrait manaTrait;

    private int consecutiveRecipes;

    public StellarForgeMachine(MetaMachineBlockEntity holder) {
        super(holder, GTORecipeDataKeys.STELLAR_CONTAINMENT_TIER);
        this.manaTrait = new ManaTrait(this) {

            @Override
            public void customText(@NotNull List<Component> textList) {
                if (hasStructurePart(MAGIC_MODULE)) {
                    super.customText(textList);
                }
            }
        };
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        tier = switch (getCasingTier(GTORecipeDataKeys.STELLAR_CONTAINMENT_TIER)) {
            case 3 -> GTValues.MAX;
            case 2 -> GTValues.OpV;
            default -> GTValues.UXV;
        };
    }

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        consecutiveRecipes++;
        var manat = MANATRecipeExtension.getMANAt(recipe);
        if (manat < 0) {
            if (!hasStructurePart(MAGIC_MODULE)) {
                consecutiveRecipes = 0;
                return null;
            }
            if (consecutiveRecipes > 1) {
                MANATRecipeExtension.setMANAt(recipe, Math.max((long) (manat * Math.log(consecutiveRecipes + Math.E - 1)), Long.MIN_VALUE));
            }
            return recipe;
        }
        recipe = RecipeModifier.laserLossOverclocking(this, unit, recipe);
        if (recipe != null && consecutiveRecipes > 1) {
            recipe.duration = Math.max(recipe.duration / 2, 1);
        }
        return recipe;
    }

    @Override
    public void regressRecipe(RecipeLogic recipeLogic) {
        setWorkingEnabled(false);
        recipeLogic.resetRecipeLogic();
        doExplosion(1);
    }

    @Override
    public void doExplosion(BlockPos pos, float explosionPower) {
        var machine = self();
        var level = machine.getLevel();
        if (level != null) {
            level.removeBlock(pos, false);
            SphereExplosion.explosion(pos, level, 100, true, true);
        }
    }

    @Override
    public @NotNull ManaContainerList getManaContainer() {
        return manaTrait.getManaContainers();
    }

    @Override
    public boolean isGeneratorMana() {
        return true;
    }

    private static final class Wrapper {

        private static final Map<Item, Integer> BOMB = Map.of(GTOBlocks.NUKE_BOMB.asItem(), 1,
                GTOBlocks.NAQUADRIA_CHARGE.asItem(), 4,
                GTOBlocks.LEPTONIC_CHARGE.asItem(), 16,
                GTOBlocks.QUANTUM_CHROMODYNAMIC_CHARGE.asItem(), 64);
    }
}

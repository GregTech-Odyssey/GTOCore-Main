package com.gtocore.common.machine.multiblock.electric;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class GreenhouseMachine extends ElectricMultiblockMachine {

    public GreenhouseMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    private int SkyLight = 15;

    private void getGreenhouseLight() {
        Level level = getLevel();
        if (level == null) return;
        SkyLight = 15;
        BlockPos pos = MachineUtils.getOffsetPos(1, 4, getFrontFacing(), getPos());
        BlockPos[] coordinates = { pos,
                pos.offset(1, 0, 0),
                pos.offset(1, 0, 1),
                pos.offset(1, 0, -1),
                pos.offset(0, 0, 1),
                pos.offset(0, 0, -1),
                pos.offset(-1, 0, 0),
                pos.offset(-1, 0, 1),
                pos.offset(-1, 0, -1) };
        for (BlockPos i : coordinates) {
            int l = level.getBrightness(LightLayer.SKY, i) - (level.dimension() == Level.OVERWORLD ? level.getSkyDarken() : 0);
            if (l < SkyLight) {
                SkyLight = l;
            }
        }
    }

    @Override
    public boolean checkConditions(RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        getGreenhouseLight();
        if (SkyLight == 0) {
            IdleReason.NO_SUNLIGHT.setReason(this);
            return false;
        }
        return super.checkConditions(unit, recipe);
    }

    @Override
    public void onWorking() {
        super.onWorking();
        if (getOffsetTimer() % 20 == 0) {
            getGreenhouseLight();
            if (SkyLight == 0) {
                getRecipeLogic().setProgress(0);
            }
            if (SkyLight < 13) {
                getRecipeLogic().setProgress(getRecipeLogic().getProgress() - 10);
            }
        }
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (getOffsetTimer() % 10 == 0) {
            getGreenhouseLight();
        }
        if (!MultiblockPage.isScreenText()) textList.add(Component.translatable("gtocore.machine.greenhouse.SkyLight", SkyLight));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading("gtocore.machine.greenhouse.SkyLight", MultiblockPage.numberText(() -> SkyLight, ""));
    }
}

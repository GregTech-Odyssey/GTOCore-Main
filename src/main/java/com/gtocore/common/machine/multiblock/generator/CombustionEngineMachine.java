package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.machine.multiblock.part.InfiniteIntakeHatchPartMachine;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CombustionEngineMachine extends ElectricMultiblockMachine {

    public static final ParamKey EXTENSION = ParamKey.of(Lang.EXTENSION_NAME, Lang.EXTENSION_DESC);
    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor generatorIntakeMonitor = holder.monitorTick(GTOTickTimeMonitors.GENERATOR_INTAKE, this::intake);

    private static final FluidStack OXYGEN_STACK = GTMaterials.Oxygen.getFluid(20);
    private static final FluidStack LIQUID_OXYGEN_STACK = GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 80);
    private static final FluidStack LUBRICANT_STACK = GTMaterials.Lubricant.getFluid(1);
    private final int tier;
    // runtime
    private boolean isOxygenBoosted;
    @SaveToDisk
    private final NotifiableFluidTank tank;
    private final ConditionalSubscriptionHandler tankSubs;

    public CombustionEngineMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
        this.tank = new NotifiableFluidTank(this, 1, 128000, IO.IN, IO.NONE);
        tankSubs = new ConditionalSubscriptionHandler(this, generatorIntakeMonitor, 20, () -> isFormed && !isIntakesObstructed());
    }

    @Override
    @Nullable
    public ICustomItemStackHandler getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    @Override
    @Nullable
    public ICustomFluidStackHandler getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return null;
    }

    private void intake() {
        var fluid = InfiniteIntakeHatchPartMachine.AIR_MAP.get(getLevel().dimension());
        if (fluid == null) {
            tankSubs.unsubscribe();
            return;
        }
        tank.fillInternal(new FluidStack(fluid, hasStructurePart(EXTENSION) ? 24000 : 8000), IFluidHandler.FluidAction.EXECUTE);
        tankSubs.updateSubscription();
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        tankSubs.updateSubscription();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        tankSubs.initialize(getLevel());
    }

    private boolean isIntakesObstructed() {
        if (getLevel() == null) return false;
        Direction facing = getFrontFacing();
        boolean permuteXZ = facing.getAxis() == Direction.Axis.Z;
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if (x == 0 && y == 0) continue;
                if (!getLevel().getBlockState(getPos().relative(facing).offset(permuteXZ ? x : 0, y, permuteXZ ? 0 : x)).isAir()) return true;
            }
        }
        return false;
    }

    private boolean isExtreme() {
        return tier > GTValues.EV;
    }

    private boolean isBoostAllowed() {
        return getMaxVoltage() >= GTValues.V[tier + 1];
    }

    //////////////////////////////////////
    // ****** Recipe Logic *******//
    //////////////////////////////////////
    @Override
    public long getOverclockVoltage() {
        int shift = hasStructurePart(EXTENSION) ? 3 : 1;
        return GTValues.V[tier] << (isOxygenBoosted ? shift + 1 : shift);
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        long EUt = recipe.getOutputEUt();
        if (EUt > 0 && unit.matchFluid(LUBRICANT_STACK) && !isIntakesObstructed()) {
            recipe = ParallelLogic.accurateContentParallel(this, unit, recipe, getOverclockVoltage() / EUt);
            if (recipe == null) return null;
            if (isOxygenBoosted) {
                recipe.setEUt(-((long) (recipe.getOutputEUt() * (isExtreme() ? 2 : 1.5))));
            }
            return recipe;
        }
        (EUt <= 0 ? IdleReason.NOT_APPLICABLE : !unit.matchFluid(LUBRICANT_STACK) ? IdleReason.NO_LUBRICANT : IdleReason.INTAKE_OBSTRUCTED).setReason(this);
        return null;
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (!super.handleTickRecipe(recipe)) return false;
        long totalContinuousRunningTime = recipeLogic.getTotalContinuousRunningTime();
        if ((totalContinuousRunningTime == 1 || totalContinuousRunningTime % 72 == 0)) {
            if (!inputFluid(LUBRICANT_STACK)) {
                IdleReason.NO_LUBRICANT.setReason(this);
                return false;
            }
        }
        if ((totalContinuousRunningTime == 1 || totalContinuousRunningTime % 20 == 0) && isBoostAllowed()) {
            isOxygenBoosted = inputFluid(isExtreme() ? LIQUID_OXYGEN_STACK : OXYGEN_STACK);
        }
        return true;
    }

    //////////////////////////////////////
    // ******* GUI ********//
    //////////////////////////////////////
    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (isBoostAllowed()) {
            if (!isExtreme()) {
                if (isOxygenBoosted) {
                    textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.oxygen_boosted"));
                } else {
                    textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.supply_oxygen_to_boost"));
                }
            } else {
                if (isOxygenBoosted) {
                    textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.liquid_oxygen_boosted"));
                } else {
                    textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.supply_liquid_oxygen_to_boost"));
                }
            }
        } else {
            textList.add(Component.translatable("gtceu.multiblock.large_combustion_engine.boost_disallowed"));
        }
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        super.attachTooltips(tooltipsPanel);
        tooltipsPanel.attachTooltips(IFancyTooltip.covering("gtceu.issue.intake_obstructed", new Basic(() -> WidgetIcons.STATUS_OBSTRUCTED, () -> List.of(Component.translatable("gtceu.multiblock.large_combustion_engine.obstructed").setStyle(Style.EMPTY.withColor(ChatFormatting.RED))), this::isIntakesObstructed, () -> null)));
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @DataGeneratorScanned
    public static final class Lang {

        @RegisterLanguage(cn = "扩展燃烧室", en = "Auxiliary Combustion Chamber")
        public static final String EXTENSION_NAME = "gtocore.multiblock.combustion_engine.extension";
        @RegisterLanguage(cn = "搭建后进气量变为 3 倍，最大输出功率变为 4 倍，并可在其中额外安装最多 3 个动力仓", en = "When built, air intake is tripled, maximum power output is quadrupled, and up to 3 additional dynamo hatches can be installed in it")
        public static final String EXTENSION_DESC = "gtocore.multiblock.combustion_engine.extension.desc";

        private Lang() {}
    }
}

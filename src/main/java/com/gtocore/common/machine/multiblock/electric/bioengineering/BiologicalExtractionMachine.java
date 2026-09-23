package com.gtocore.common.machine.multiblock.electric.bioengineering;

import com.gtocore.common.data.GTOFluids;

import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BiologicalExtractionMachine extends CrossRecipeMultiblockMachine {

    private static final FluidStack CLOUD_SEED_CONCENTRATED = new FluidStack(GTOFluids.CLOUD_SEED_CONCENTRATED.getSource(), 1000);
    private static final FluidStack FIRE_WATER = new FluidStack(GTOFluids.FIRE_WATER.getSource(), 1000);
    private static final FluidStack VAPOR_OF_LEVITY = new FluidStack(GTOFluids.VAPOR_OF_LEVITY.getSource(), 1000);

    private static final Set<Fluid> FLUIDS = Set.of(CLOUD_SEED_CONCENTRATED.getFluid(), FIRE_WATER.getFluid(), VAPOR_OF_LEVITY.getFluid());

    /**
     * Experimental ME startup delay: after the structure forms, the recipe logic does not tick while any ME part is
     * still offline, for at most this many ticks. Nothing is consumed or advanced meanwhile, so the continuous
     * running time survives the AE grid booting after a world load.
     */
    private static final int ME_HOLD_TICKS = 100;

    private int redstoneSignalOutput;

    @Nullable
    private TickableSubscription meHoldSubs;
    private long meHoldUntil;

    public BiologicalExtractionMachine(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() != null) {
            meHoldUntil = getLevel().getGameTime() + ME_HOLD_TICKS;
            meHoldSubs = subscribeServerTick(meHoldSubs, this::checkMeHold, 5);
        }
    }

    @Override
    public void onStructureInvalid() {
        stopMeHold();
        super.onStructureInvalid();
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        return super.isRecipeLogicAvailable() && isNotMeHolding();
    }

    private boolean isNotMeHolding() {
        if (meHoldUntil == 0) return true;
        if (getLevel() != null && getLevel().getGameTime() < meHoldUntil) {
            for (var part : getParts()) {
                if (part instanceof IGridConnectedMachine me && !me.isOnline()) return false;
            }
        }
        // released once per formation, so a later ME hiccup falls back to the normal rules
        meHoldUntil = 0;
        return true;
    }

    private void checkMeHold() {
        if (isNotMeHolding()) {
            stopMeHold();
            getRecipeLogic().updateTickSubscription();
        }
    }

    private void stopMeHold() {
        meHoldUntil = 0;
        if (meHoldSubs != null) {
            meHoldSubs.unsubscribe();
            meHoldSubs = null;
        }
    }

    @Override
    public boolean isIndependentThread() {
        return false;
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (getRecipeLogic().getTotalContinuousRunningTime() < 400) {
            recipe.itemOutputs = Collections.emptyList();
            recipe.fluidOutputs = Collections.emptyList();
            return recipe;
        } else {
            return super.getRealRecipe(unit, recipe);
        }
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (super.handleTickRecipe(recipe)) {
            if (redstoneSignalOutput > 9) {
                redstoneSignalOutput--;
                if (redstoneSignalOutput == 9) {
                    redstoneSignalOutput = 0;
                    updateSignal();
                }
            }
            if (getRecipeLogic().getProgress() % 20 == 0) {
                if (inputFluid(GTOFluids.NUTRIENT_DISTILLATION.getSource(), 1000)) {
                    redstoneSignalOutput = 15;
                    updateSignal();
                } else {
                    return false;
                }
            }
            return switch ((int) getRecipeLogic().getTotalContinuousRunningTime()) {
                case 100 -> input(CLOUD_SEED_CONCENTRATED);
                case 300 -> input(FIRE_WATER);
                case 400 -> input(VAPOR_OF_LEVITY);
                default -> true;
            };
        }
        return false;
    }

    private boolean input(FluidStack stack) {
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);
        forEachFluids(true, (fluidStack, amount) -> {
            var fluid = fluidStack.getFluid();
            if (FLUIDS.contains(fluid)) {
                if (fluid == stack.getFluid()) {
                    if (amount >= stack.getAmount()) {
                        inputFluid(stack);
                        success.set(true);
                    }
                } else {
                    failed.set(true);
                    return true;
                }
            }
            return false;
        });
        return success.get() && !failed.get();
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable("gtocore.machine.total_time", getRecipeLogic().getTotalContinuousRunningTime()));
        if (!isActive()) return;
        if (getRecipeLogic().getTotalContinuousRunningTime() < 100) {
            textList.add(Component.translatable("gtocore.machine.need", CLOUD_SEED_CONCENTRATED.getDisplayName()));
        } else if (getRecipeLogic().getTotalContinuousRunningTime() < 300) {
            textList.add(Component.translatable("gtocore.machine.need", FIRE_WATER.getDisplayName()));
        } else if (getRecipeLogic().getTotalContinuousRunningTime() < 400) {
            textList.add(Component.translatable("gtocore.machine.need", VAPOR_OF_LEVITY.getDisplayName()));
        }
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        if (side == getFrontFacing().getOpposite()) {
            return redstoneSignalOutput;
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        return side == getFrontFacing();
    }
}

package com.gtocore.common.machine.multiblock.steam;

import com.gtocore.common.machine.multiblock.part.LargeSteamHatchPartMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.utils.MachineUtils;
import com.gtolib.utils.MathUtil;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CleanroomMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BaseSteamMultiblockMachine extends SteamParallelMultiblockMachine {

    @RegisterLanguage(cn = "超频次数", en = "Overclocks")
    private static final String OC_AMOUNT = "gtocore.machine.steam_parallel_machine.oc_amount";
    @RegisterLanguage(cn = "需要支持超频的大型蒸汽仓", en = "Requires a large steam hatch that supports overclocking")
    private static final String OC_UNAVAILABLE = "gtocore.machine.steam_parallel_machine.oc_unavailable";

    protected int maxOCamount;
    private int euMultiplier;
    private double conversionRate;

    @SaveToDisk(defaultValue = "0")
    private int amountOC;

    private final long eut;
    private final double durationMultiplier;

    public BaseSteamMultiblockMachine(MetaMachineBlockEntity holder, int maxParallels, int eut, double durationMultiplier) {
        super(holder, maxParallels);
        this.eut = eut;
        this.durationMultiplier = durationMultiplier;
    }

    BaseSteamMultiblockMachine(MetaMachineBlockEntity holder, int maxParallels, double durationMultiplier) {
        this(holder, maxParallels, 32, durationMultiplier);
    }

    boolean oc() {
        return false;
    }

    @Override
    public int getRecipeTier() {
        return GTUtil.getTierByVoltage(this.eut << euMultiplier);
    }

    @Override
    public int getTier() {
        return GTUtil.getTierByVoltage(this.eut << euMultiplier);
    }

    @Override
    protected void addSteamEnergy() {
        maxOCamount = 0;
        euMultiplier = 0;
        conversionRate = 2D;
        for (var part : getParts()) {
            if (part instanceof SteamHatchPartMachine machine) {
                var fluid = GTMaterials.Steam.getFluid(1);
                if (machine instanceof LargeSteamHatchPartMachine partMachine) {
                    conversionRate = partMachine.c;
                    fluid = partMachine.f;
                    euMultiplier = partMachine.o;
                    if (oc()) maxOCamount = partMachine.o;
                }
                energyContainer = new EnergyContainer(fluid, conversionRate, machine.tank);
                return;
            }
        }
    }

    @Override
    public double getConversionRate() {
        return conversionRate;
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        long eut = recipe.getInputEUt();
        if (eut <= (this.eut << euMultiplier)) {
            recipe = ParallelLogic.accurateParallel(this, unit, recipe, maxParallels);
            if (recipe == null) return null;
            recipe.duration = (int) (recipe.duration * durationMultiplier);
            if (maxOCamount > 0) {
                eut *= recipe.parallels;
                recipe.eut = eut << (amountOC << 1);
                recipe.duration = Math.max(1, recipe.duration / (1 << amountOC));
            }
            return recipe;
        }
        IdleReason.LOW_POWER.setReason(this, eut, this.eut << euMultiplier);
        return null;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed() && maxOCamount > 0) {
            textList.add(Component.translatable("gtocore.machine.oc_amount", amountOC)
                    .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.translatable("gtocore.machine.steam_parallel_machine.oc")))));
        }
    }

    @Override
    public void addControls(ControlPanel controls) {
        if (!oc()) return;
        controls.addInt(OC_AMOUNT, () -> amountOC, value -> amountOC = value, () -> 0, () -> maxOCamount, "gtocore.machine.steam_parallel_machine.oc")
                .disabled(() -> maxOCamount <= 0, OC_UNAVAILABLE);
    }

    @Override
    protected void attachExtraConfigurators(ConfiguratorPanel configuratorPanel) {
        MachineUtils.attachStructureCheckConfigurators(configuratorPanel, this);
    }

    @Override
    public void setCleanroom(@Nullable ICleanroomProvider provider) {
        if (provider instanceof CleanroomMachine) return;
        super.setCleanroom(provider);
    }

    private static class EnergyContainer extends SteamEnergyContainer {

        private final FluidStack steam;

        private final double conversionRate;
        private final NotifiableFluidTank steamTank;

        private EnergyContainer(FluidStack steam, double conversionRate, NotifiableFluidTank steamTank) {
            super(conversionRate, steamTank);
            this.steam = steam;
            this.conversionRate = conversionRate;
            this.steamTank = steamTank;
        }

        @Override
        public long changeEnergy(long differenceAmount) {
            differenceAmount = -differenceAmount;
            int totalSteam = Math.max(1, MathUtil.saturatedCast((long) (differenceAmount * conversionRate)));
            var steam = this.steam.copy();
            steam.setAmount(totalSteam);
            var leftSteam = steamTank.drainInternal(steam, IFluidHandler.FluidAction.EXECUTE).getAmount();
            if (leftSteam == totalSteam) return -differenceAmount;
            differenceAmount = (long) (leftSteam / conversionRate);
            return -differenceAmount;
        }
    }
}

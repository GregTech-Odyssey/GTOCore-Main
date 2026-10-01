package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.api.machine.part.IHeatContainerPart;
import com.gtocore.api.pattern.GTOPredicates;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.NoEnergyMultiblockMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.feature.IDummyEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDistillationTower;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.styletemplate.MachineEra;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.VoidFluidHandler;

import com.lowdragmc.lowdraglib.gui.widget.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PrimitiveDistillationTowerMachine extends NoEnergyMultiblockMachine implements IExplosionMachine, IDummyEnergyMachine, IDistillationTower {

    private static final DummyContainer CONTAINER = new DummyContainer(120);

    @NotNull
    @Getter
    private final List<IFluidHandler> fluidOutputs = new ArrayList<>();

    private IHeatContainerPart heatMachineA;
    private IHeatContainerPart heatMachineB;

    public PrimitiveDistillationTowerMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    private boolean shouldTick() {
        return isFormed && heatMachineA != null && heatMachineB != null;
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (heatMachineA != null && heatMachineB != null) {
            var a = heatMachineA.getHeatContainer().getTemperature();
            var b = heatMachineB.getHeatContainer().getTemperature();
            if (b < 400) {
                IdleReason.INSUFFICIENT_TEMPERATURE.setReason(this, 400, (long) b);
                return null;
            }
            recipe.duration = (int) (recipe.duration * getDurationMultiplier(a, b));
            return recipe;
        }
        setIdleReason(IdleReason.INSUFFICIENT_TEMPERATURE, 400, -1);
        return null;
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (heatMachineA == null || heatMachineB == null) {
            IdleReason.INSUFFICIENT_TEMPERATURE.setReason(this, 400, -1);
            return false;
        }
        var a = heatMachineA.getHeatContainer().getTemperature();
        var b = heatMachineB.getHeatContainer().getTemperature();
        if (heatMachineB.getHeatContainer().removeHeatUnrestricted(1, false) == 1) {
            if (getOffsetTimer() % 2 == 0 && a < b - 100) heatMachineA.getHeatContainer().addHeatUnrestricted(1, false);
            return true;
        } else {
            IdleReason.HEAT_SHORT.setReason(this, 1, heatMachineB.getHeatContainer().getCurrentHeat());
            return false;
        }
    }

    private double getDurationMultiplier(double temperatureA, double temperatureB) {
        return temperatureB > 400 ? Math.sqrt(Math.max(1, temperatureA - 350D)) * 800 / (temperatureB * Math.clamp(getRecipeLogic().getTotalContinuousRunningTime() / 1000, 1, 2)) : 1;
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (heatMachineA == null || heatMachineB == null) return;
        var a = heatMachineA.getHeatContainer().getTemperature();
        var b = heatMachineB.getHeatContainer().getTemperature();
        textList.add(Component.translatable("gtocore.machine.current_temperature", a + " | " + b));
        textList.add(Component.translatable("gtocore.machine.total_time", getRecipeLogic().getTotalContinuousRunningTime()));
        textList.add(Component.translatable("gtocore.machine.duration_multiplier.tooltip", FormattingUtil.formatNumbers(getDurationMultiplier(a, b))));
    }

    @RegisterLanguage(cn = "塔顶温度", en = "Top Temperature")
    private static final String TOP_TEMPERATURE = "gtocore.machine.primitive_distillation_tower.top_temperature";
    @RegisterLanguage(cn = "塔底温度", en = "Bottom Temperature")
    private static final String BOTTOM_TEMPERATURE = "gtocore.machine.primitive_distillation_tower.bottom_temperature";
    @RegisterLanguage(cn = "连续运行", en = "Continuous Run")
    private static final String CONTINUOUS = "gtocore.machine.primitive_distillation_tower.continuous";
    @RegisterLanguage(cn = "耗时倍率", en = "Duration Multiplier")
    private static final String DURATION = "gtocore.machine.primitive_distillation_tower.duration";
    @RegisterLanguage(cn = "塔底需高于 400 K 才能运行", en = "The bottom must exceed 400 K to run")
    private static final String BOTTOM_HINT = "gtocore.machine.primitive_distillation_tower.bottom_hint";
    @RegisterLanguage(cn = "此层没有输出仓，产物将被丢弃", en = "No output hatch on this layer; the product is discarded")
    private static final String LAYER_MISSING = "gtocore.machine.primitive_distillation_tower.layer_missing";
    @RegisterLanguage(cn = "无输出仓", en = "No Hatch")
    private static final String LAYER_MISSING_SHORT = "gtocore.machine.primitive_distillation_tower.layer_missing_short";
    @RegisterLanguage(cn = "部分产物丢失", en = "Products Lost")
    private static final String PRODUCTS_LOST = "gtocore.machine.primitive_distillation_tower.products_lost";

    @Override
    public UIElement createUIWidget() {
        var page = MultiblockPage.of(this).setScreen(MachineEra.STEEL.getScreen());
        page.getRecipeList().setFluidOutputBlocked(this::layerMissing, LAYER_MISSING_SHORT, LAYER_MISSING);
        page.bindAlert(this::productsLost, PRODUCTS_LOST);
        temperatureBar(page, BOTTOM_TEMPERATURE, () -> heatMachineB).addMarker(400, 0xFF2A2F33).bindDetail(() -> BOTTOM_DETAIL);
        temperatureBar(page, TOP_TEMPERATURE, () -> heatMachineA);
        page.addLine(CONTINUOUS, MultiblockPage.cached(() -> getRecipeLogic().getTotalContinuousRunningTime() / 20,
                seconds -> Component.literal(FormattingUtil.formatNumbers(seconds) + " s")));
        page.addLine(DURATION, MultiblockPage.cached(() -> Math.round(currentMultiplier() * 100),
                percent -> Component.literal(FormattingUtil.formatNumbers(percent / 100.0) + "x")));
        return page.build();
    }

    private static final Component BOTTOM_DETAIL = Component.translatable(BOTTOM_HINT);

    private static ProgressBar temperatureBar(MultiblockPage page, String key, Supplier<IHeatContainerPart> part) {
        return page.addBar(key, UITheme::barHeat, () -> {
            var hatch = part.get();
            if (hatch == null) return ProgressBar.Progress.EMPTY;
            var heat = hatch.getHeatContainer();
            return new ProgressBar.Progress((long) heat.getTemperature(), heat.getMaxTemperature(), 0);
        }).setUnit("K");
    }

    private double currentMultiplier() {
        if (heatMachineA == null || heatMachineB == null) return 1;
        return getDurationMultiplier(heatMachineA.getHeatContainer().getTemperature(), heatMachineB.getHeatContainer().getTemperature());
    }

    private boolean productsLost() {
        var recipe = getRecipeLogic().getLastRecipe();
        if (recipe == null) return false;
        for (int i = 0; i < recipe.fluidOutputs.size(); i++) {
            if (layerMissing(i)) return true;
        }
        return false;
    }

    private boolean layerMissing(int index) {
        return index >= fluidOutputs.size() || fluidOutputs.get(index) instanceof VoidFluidHandler;
    }

    @Override
    public void onPartScan(IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof IHeatContainerPart heatContainerPart) {
            if (getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.A, Collections.emptySet()).contains(heatContainerPart.self().getPos())) {
                this.heatMachineA = heatContainerPart;
            } else {
                this.heatMachineB = heatContainerPart;
            }
        }
    }

    @Override
    public Comparator<IMultiPart> getPartSorter() {
        return Comparator.comparingInt(p -> p.self().getPos().getY());
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        addOutputs();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        heatMachineA = null;
        heatMachineB = null;
        fluidOutputs.clear();
    }

    @Override
    public IEnergyContainer getEnergyContainer() {
        return CONTAINER;
    }

    @Override
    public boolean jade() {
        return false;
    }

    @Override
    public int getYOffset() {
        return 1;
    }
}

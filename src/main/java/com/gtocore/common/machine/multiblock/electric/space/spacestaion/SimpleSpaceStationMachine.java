package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.api.gui.overview.OverviewWidget;
import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.client.forge.ForgeClientEvent;

import com.gtolib.api.recipe.IdleReason;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineSubWindows;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.uipro.window.WindowAnchor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import earth.terrarium.adastra.api.planets.PlanetApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.common.data.GTMaterials.DistilledWater;
import static com.gtocore.common.data.GTOMaterials.FlocculationWasteSolution;

public class SimpleSpaceStationMachine extends AbstractSpaceStation implements ICustomRecipeLogicHolder, IMachineSubWindows {

    private static final String WINDOW_OVERVIEW = "overview";

    @Nullable
    private Set<BlockPos> outputDistilledWaterHatches;
    @Nullable
    private List<RecipeHandlerUnit> outputDistilledWaterHatchesList;
    /// 空间站附赠超净间
    private int cleanroomTier;

    static final int MAX_WATER_PER_HATCH = 1000;

    @SaveToDisk(defaultValue = "8")
    private int waterAmountPerHatch = 8;

    public SimpleSpaceStationMachine(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
    }

    @Override
    public void addHandlerList(RecipeHandlerUnit handler) {
        if (outputDistilledWaterHatches != null && outputDistilledWaterHatches.contains(handler.part.self().getPos()) && handler.handlerIO == IO.OUT) {
            if (outputDistilledWaterHatchesList == null) {
                outputDistilledWaterHatchesList = new ArrayList<>();
            }
            outputDistilledWaterHatchesList.add(handler);
            return;
        }
        super.addHandlerList(handler);
    }

    /// 超净间太空版
    /// @see com.gregtechceu.gtceu.common.machine.multiblock.electric.CleanroomMachine

    /// @see com.gregtechceu.gtceu.common.machine.multiblock.electric.CleanroomMachine#onStructureFormed()
    @Override
    public void onStructureFormed() {
        this.outputDistilledWaterHatches = getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.SPACE_MACHINE_PHOTOVOLTAIC_SUPP, Collections.emptySet());
        super.onStructureFormed();
        this.cleanroomTier = getMultiblockState().getMatchContext().getOrDefault(Predicates.DataKey.FILTER_TYPE, 1);
        onFormed();
    }

    /// @see com.gregtechceu.gtceu.common.machine.multiblock.electric.CleanroomMachine#onStructureInvalid()
    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.outputDistilledWaterHatches = null;
        this.outputDistilledWaterHatchesList = null;
        onInvalid();
    }

    @Override
    public List<ForgeClientEvent.HighlightNeed> getCustomHighlights() {
        BlockPos corner0 = getPos()
                .relative(getFrontFacing(), 0)
                .above(3)
                .relative(getFrontFacing().getClockWise(), 3);
        BlockPos corner1 = getPos()
                .relative(getFrontFacing(), 29)
                .below(3)
                .relative(getFrontFacing().getCounterClockWise(), 3);
        return List.of(new ForgeClientEvent.HighlightNeed(corner0, corner1, ChatFormatting.GRAY.getColor()));
    }

    public List<Component> getHighlightText() {
        return List.of(Component.translatable("tooltip.ad_astra.oxygen_distribution_area"));
    }

    private static RecipeBuilder inputFluids(RecipeBuilder builder) {
        builder.inputFluids(DistilledWater, 15);
        builder.inputFluids(GTMaterials.RocketFuel, 10);
        builder.inputFluids(GTMaterials.Air, 100);
        return builder;
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        super.customText(list);
        list.add(Component.translatable("gtocore.machine.simple_spacestation.distilled_water", waterAmountPerHatch));
    }

    @Override
    public void onWorking() {
        var time = getOffsetTimer();
        if (time % 20 == 0) {

            if (firstLoad() || time % 400 == 0) provideOxygen();

            /// Distilled Water distribution
            if (waterAmountPerHatch > 0 && outputDistilledWaterHatchesList != null && !outputDistilledWaterHatchesList.isEmpty()) {
                for (var handler : outputDistilledWaterHatchesList) {
                    if (handler.simulateOutputFluid(DistilledWater.getFluid(), waterAmountPerHatch) && inputFluid(DistilledWater.getFluid(), waterAmountPerHatch)) {
                        handler.outputFluid(DistilledWater.getFluid(), waterAmountPerHatch);
                    }
                }
            }
        }
        super.onWorking();
    }

    @Override
    public int getCleanroomTier() {
        return this.cleanroomTier;
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (!isInSpace()) {
            IdleReason.SPACE_STATION_NOT_IN_SPACE.setReason(this);
            return null;
        }
        return roundRecipe();
    }

    GTRecipeDefinition roundRecipe() {
        return inputFluids(getRecipeBuilder().duration(200).EUt(VA[EV]))
                .outputFluids(FlocculationWasteSolution.getFluid(30))
                .build();
    }

    boolean isInSpace() {
        return PlanetApi.API.isSpace(getLevel());
    }

    int getWaterAmountPerHatch() {
        return waterAmountPerHatch;
    }

    void setWaterAmountPerHatch(int amount) {
        int clamped = Math.max(0, Math.min(MAX_WATER_PER_HATCH, amount));
        if (clamped == waterAmountPerHatch) return;
        waterAmountPerHatch = clamped;
        onChanged();
    }

    int getInnerVolume() {
        if (!isFormed()) return 0;
        return getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.SPACE, Collections.emptySet()).size();
    }

    @Nullable
    List<RecipeHandlerUnit> getSupplyHatches() {
        return outputDistilledWaterHatchesList;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return SpaceStationFlowPage.create(this, widget);
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(OverviewWidget.button(this, WINDOW_OVERVIEW, getDefinition(), ExplorerOverviewAdapter.INSTANCE));
    }

    @Override
    public @Nullable ModularUI createSubWindow(String key, Player player) {
        if (!WINDOW_OVERVIEW.equals(key)) return null;
        return new ModularUI(this, player).widget(new OverviewWidget(this, ExplorerOverviewAdapter.INSTANCE));
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public boolean windowHasPlayerInventory() {
        return true;
    }

    @Override
    public WindowAnchor windowAnchor() {
        return WindowAnchor.CENTER;
    }
}

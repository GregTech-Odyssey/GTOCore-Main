package com.gtocore.common.machine.multiblock.electric.space.spacestaion;

import com.gtocore.api.gui.overview.OverviewWidget;
import com.gtocore.api.machine.ILargeSpaceStationMachine;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.TierDataKey;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineSubWindows;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.uipro.window.WindowAnchor;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.hepdd.gtmthings.api.capability.IBindable;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import earth.terrarium.adastra.api.planets.PlanetApi;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.GTValues.IV;
import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.common.data.GTMaterials.DistilledWater;
import static com.gtocore.common.data.GTOMaterials.FlocculationWasteSolution;
import static com.gtocore.data.techtree.MachinesNode.LaserSpaceEngineering;

@DataGeneratorScanned
public class Core extends AbstractSpaceStation implements ILargeSpaceStationMachine, IBindable, ITierCasingMachine, IMachineSubWindows {

    private static final String WINDOW_OVERVIEW = "station_overview";
    @RegisterLanguage(cn = "核心舱", en = "Core Module")
    public static final String CORE_MODULE = "gtocore.machine.spacestation.core_module";

    @Getter
    private final Map<Class<? extends ISpaceServiceMachine>, ISpaceServiceMachine> serviceMachineMap = new Reference2ObjectOpenHashMap<>();

    private final ReferenceOpenHashSet<ILargeSpaceStationMachine> subMachinesFlat;
    private final TierCasingTrait tierCasingTrait;

    @Getter
    private boolean dirty = false;

    @Override
    public void markDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Core(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
        this.subMachinesFlat = new ReferenceOpenHashSet<>();
        tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
    }

    @Override
    public Core getRoot() {
        return this;
    }

    @Override
    public void setRoot(Core root) {}

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        onFormed();
        IIWirelessInteractor.addToNet(this);
        markDirty(true);
    }

    @Override
    public void onUnload() {
        IIWirelessInteractor.removeFromNet(this);
        super.onUnload();
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean ignored) {}

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        IIWirelessInteractor.removeFromNet(this);
        onInvalid();
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        removeAllSubMachines();
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        super.customText(list);
        if (MultiblockPage.isScreenText()) return;
        list.add(Component.translatable("gui.ae2.PowerUsageRate", "%s EU/t".formatted(FormattingUtil.formatNumbers(getEUt()))).withStyle(ChatFormatting.YELLOW));
        list.add(Component.translatable("gtocore.machine.spacestation.energy_consumption.total", FormattingUtil.formatNumbers(getTotalEUt())).withStyle(ChatFormatting.GOLD));
        list.add(Component.translatable("gtocore.machine.spacestation.module_count", subMachinesFlat.size()));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading("gui.ae2.PowerUsageRate", MultiblockPage.numberText(this::getEUt, "EU/t"));
        page.addReading("gtocore.machine.spacestation.energy_consumption.total", MultiblockPage.numberText(this::getTotalEUt, ""));
        page.addReading("gtocore.machine.spacestation.module_count", MultiblockPage.numberText(this::getModuleCount, ""));
    }

    public long getTotalEUt() {
        GTRecipe recipe = getRecipeLogic().getLastRecipe();
        return recipe == null ? 0 : recipe.getInputEUt();
    }

    public int getModuleCount() {
        return subMachinesFlat.size();
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(OverviewWidget.button(this, WINDOW_OVERVIEW, getDefinition(), StationOverviewAdapter.INSTANCE));
    }

    @Override
    public @Nullable ModularUI createSubWindow(String key, Player player) {
        if (!WINDOW_OVERVIEW.equals(key)) return null;
        return new ModularUI(this, player).widget(new OverviewWidget(this, StationOverviewAdapter.INSTANCE));
    }

    Set<ILargeSpaceStationMachine> getModules() {
        return subMachinesFlat;
    }

    boolean isInSpace() {
        return PlanetApi.API.isSpace(getLevel());
    }

    GTRecipeDefinition buildCycleRecipe() {
        long EUt = getEUt();
        for (ILargeSpaceStationMachine machine : subMachinesFlat) {
            if (machine.isFormed()) EUt += machine.getEUt();
        }
        int shares = subMachinesFlat.size() + 1;
        return inputFluids(getRecipeBuilder().duration(20).EUt(EUt), shares).tier(1).outputFluids(FlocculationWasteSolution.getFluid(30 * shares)).build();
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return CoreFlowPage.create(this, widget);
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

    private void removeAllSubMachines() {
        for (ILargeSpaceStationMachine m : subMachinesFlat) {
            if (m != this && m.getRoot() == this) {
                m.setRoot(null);
            }
        }
        subMachinesFlat.clear();
    }

    /// 很吃性能的操作，使用dirty标记需要更新
    private void refreshModules() {
        removeAllSubMachines();
        // provider = null;
        // laserProvider = null;
        serviceMachineMap.clear();
        Set<ILargeSpaceStationMachine> its = new ReferenceOpenHashSet<>(getConnectedModules());
        while (!its.isEmpty()) {
            var it = its.iterator();
            ILargeSpaceStationMachine m = it.next();
            it.remove();
            if (m.getRoot() != null) continue;
            m.setRoot(this);
            if (m instanceof ISpaceServiceMachine serviceMachine) {
                serviceMachineMap.putIfAbsent(serviceMachine.getClass(), serviceMachine);
            }
            if (subMachinesFlat.add(m)) {
                its.addAll(m.getConnectedModules());
            }
        }
    }

    @Override
    public ConnectType getConnectType() {
        return ConnectType.CORE;
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (!PlanetApi.API.isSpace(getLevel())) {
            IdleReason.SPACE_STATION_NOT_IN_SPACE.setReason(this);
            return null;
        }
        if (dirty) {
            refreshModules();
            dirty = false;
        }
        for (ILargeSpaceStationMachine machine : subMachinesFlat) {
            if (machine instanceof IRecipeLogicMachine r) r.getRecipeLogic().updateTickSubscription();
        }
        return buildCycleRecipe();
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    private static RecipeBuilder inputFluids(RecipeBuilder builder, int mul) {
        builder.inputFluids(DistilledWater, 15 * mul);
        builder.inputFluids(GTMaterials.RocketFuel, 10 * mul);
        builder.inputFluids(GTMaterials.Air, 100 * mul);
        return builder;
    }

    @Override
    public void onWorking() {
        if (firstLoad() || getOffsetTimer() % 400 == 0) provideOxygen();
        super.onWorking();
    }

    @Override
    public long getEUt() {
        return VA[IV];
    }

    @Override
    public Reference2IntMap<TierDataKey> getCasingTiers() {
        return tierCasingTrait.getCasingTiers();
    }

    @Override
    @Nullable
    public UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public Set<CleanroomType> getTypes() {
        CleanroomProvider provider = (CleanroomProvider) serviceMachineMap.get(CleanroomProvider.class);
        if (provider == null) {
            return Collections.emptySet();
        }
        return provider.getTypes();
    }

    public boolean hasLaserBoost() {
        return TechTreeSavedData.isUnlocked(getOwnerUUID(), LaserSpaceEngineering);
    }

    public double getDurationMultiplier() {
        SpaceElevatorConnectorModule provider = (SpaceElevatorConnectorModule) serviceMachineMap.get(SpaceElevatorConnectorModule.class);
        if (provider == null) {
            return 1.0;
        }
        return provider.getDurationMultiplier();
    }
}

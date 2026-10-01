package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.common.data.GTOTickTimeMonitors;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.api.machine.trait.EnergyContainerTrait;
import com.gtolib.api.recipe.IdleReason;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTRecipeDataKeys;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.api.GTValues.LuV;
import static com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine.calculateEnergyStorageFactor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@DataGeneratorScanned
public final class AdvancedFusionReactorMachine extends CrossRecipeMultiblockMachine {

    public static final PortKey HIGH_ENERGY_OUT = PortKey.of("high_energy_out");
    public static final PortKey OVERCLOCK_OUT = PortKey.of("overclock_out");

    @RegisterLanguage(cn = "一级高能模块", en = "High-Energy Module I")
    private static final String HIGH_ENERGY_1_NAME = "gtocore.multiblock.kuangbiao_one.high_energy_1";
    @RegisterLanguage(cn = "搭建后反应堆等级提升一级、热容量翻倍；二级高能模块搭建在此模块上", en = "Raises the reactor tier by one and doubles its heat capacity; the High-Energy Module II is built onto this module")
    private static final String HIGH_ENERGY_1_DESC = "gtocore.multiblock.kuangbiao_one.high_energy_1.desc";
    @RegisterLanguage(cn = "二级高能模块", en = "High-Energy Module II")
    private static final String HIGH_ENERGY_2_NAME = "gtocore.multiblock.kuangbiao_one.high_energy_2";
    @RegisterLanguage(cn = "搭建后反应堆等级再提升一级、热容量翻倍；三级高能模块搭建在此模块上", en = "Raises the reactor tier by one more and doubles its heat capacity; the High-Energy Module III is built onto this module")
    private static final String HIGH_ENERGY_2_DESC = "gtocore.multiblock.kuangbiao_one.high_energy_2.desc";
    @RegisterLanguage(cn = "三级高能模块", en = "High-Energy Module III")
    private static final String HIGH_ENERGY_3_NAME = "gtocore.multiblock.kuangbiao_one.high_energy_3";
    @RegisterLanguage(cn = "搭建后反应堆等级再提升一级、热容量翻倍；四级高能模块搭建在此模块上", en = "Raises the reactor tier by one more and doubles its heat capacity; the High-Energy Module IV is built onto this module")
    private static final String HIGH_ENERGY_3_DESC = "gtocore.multiblock.kuangbiao_one.high_energy_3.desc";
    @RegisterLanguage(cn = "四级高能模块", en = "High-Energy Module IV")
    private static final String HIGH_ENERGY_4_NAME = "gtocore.multiblock.kuangbiao_one.high_energy_4";
    @RegisterLanguage(cn = "搭建后反应堆等级再提升一级、热容量翻倍，并显示额外光环", en = "Raises the reactor tier by one more, doubles its heat capacity and shows additional light rings")
    private static final String HIGH_ENERGY_4_DESC = "gtocore.multiblock.kuangbiao_one.high_energy_4.desc";
    @RegisterLanguage(cn = "超频模块", en = "Overclock Module")
    private static final String OVERCLOCK_NAME = "gtocore.multiblock.kuangbiao_one.overclock";
    @RegisterLanguage(cn = "搭建后可在此模块上安装超频仓与线程仓", en = "Allows Overclock Hatches and Thread Hatches to be installed on this module")
    private static final String OVERCLOCK_DESC = "gtocore.multiblock.kuangbiao_one.overclock.desc";
    @RegisterLanguage(cn = "反应堆储能", en = "Reactor Energy Buffer")
    private static final String ENERGY_BUFFER = "gtocore.machine.kuangbiao_one.energy_buffer";
    @RegisterLanguage(cn = "热量", en = "Heat")
    private static final String HEAT = "gtocore.machine.kuangbiao_one.heat";

    public static final ParamKey HIGH_ENERGY_1 = ParamKey.of(HIGH_ENERGY_1_NAME, HIGH_ENERGY_1_DESC);
    public static final ParamKey HIGH_ENERGY_2 = ParamKey.of(HIGH_ENERGY_2_NAME, HIGH_ENERGY_2_DESC);
    public static final ParamKey HIGH_ENERGY_3 = ParamKey.of(HIGH_ENERGY_3_NAME, HIGH_ENERGY_3_DESC);
    public static final ParamKey HIGH_ENERGY_4 = ParamKey.of(HIGH_ENERGY_4_NAME, HIGH_ENERGY_4_DESC);
    public static final ParamKey OVERCLOCK_MODULE = ParamKey.of(OVERCLOCK_NAME, OVERCLOCK_DESC);
    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor reactorHeatMonitor = holder.monitorTick(GTOTickTimeMonitors.REACTOR_HEAT, this::updateHeat);

    @Getter
    @SyncToClient
    private int color = -1;
    @Getter
    @SyncToClient
    private int highEnergyModules;
    @Nullable
    private GTRecipe colorRecipe;
    private static final int tier = LuV;
    @SaveToDisk(defaultValue = "0")
    private long heat = 0;
    @SaveToDisk
    private final EnergyContainerTrait energyContainer;
    private final ConditionalSubscriptionHandler preHeatSubs;

    public AdvancedFusionReactorMachine(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
        this.energyContainer = createEnergyContainer();
        preHeatSubs = new ConditionalSubscriptionHandler(this, reactorHeatMonitor, 0, () -> isFormed || heat > 0);
    }

    private EnergyContainerTrait createEnergyContainer() {
        return new EnergyContainerTrait(this, 0);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        int size = 0;
        for (var handler : getCapabilitiesFlat(IO.IN, IEnergyContainer.class)) {
            size++;
        }
        var assembly = getAssembly();
        highEnergyModules = assembly == null ? 0 : assembly.get(HIGH_ENERGY_1) + assembly.get(HIGH_ENERGY_2) + assembly.get(HIGH_ENERGY_3) + assembly.get(HIGH_ENERGY_4);
        energyContainer.resetBasicInfo(calculateEnergyStorageFactor(tier + highEnergyModules, size));
        preHeatSubs.initialize(getLevel());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        highEnergyModules = 0;
        heat = 0;
        energyContainer.resetBasicInfo(0);
        energyContainer.setEnergyStored(0);
    }

    @Override
    @Nullable
    public GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        long eu_to_start = recipe.data.getLong(GTRecipeDataKeys.EU_TO_START);
        if (eu_to_start > energyContainer.getEnergyCapacity()) {
            setIdleReason(IdleReason.START_ENERGY_CAPACITY, eu_to_start, energyContainer.getEnergyCapacity());
            return null;
        }
        long heatDiff = eu_to_start - heat;
        if (heatDiff > 0) {
            if (energyContainer.getEnergyStored() < heatDiff) {
                setIdleReason(IdleReason.START_ENERGY_SHORT, heatDiff, energyContainer.getEnergyStored());
                return null;
            }
            energyContainer.removeEnergy(heatDiff);
            heat += heatDiff;
        }
        return super.getRealRecipe(unit, recipe);
    }

    private void updateHeat() {
        if (heat > 0 && (getRecipeLogic().isIdle() || !isWorkingEnabled() || (getRecipeLogic().isWaiting() && getRecipeLogic().getProgress() == 0))) {
            heat = heat <= 10000 ? 0 : (heat - 10000);
        }
        if (isFormed() && getEnergyContainer().getEnergyStored() > 0) {
            var leftStorage = energyContainer.getEnergyCapacity() - energyContainer.getEnergyStored();
            if (leftStorage > 0) {
                energyContainer.addEnergy(getEnergyContainer().removeEnergy(leftStorage));
            }
        }
        preHeatSubs.updateSubscription();
    }

    @Override
    public void onWorking() {
        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipe != null && recipe != colorRecipe) {
            colorRecipe = recipe;
            if (!recipe.fluidOutputs.isEmpty()) {
                var fluid = recipe.fluidOutputs.getFirst().inner.getFluid();
                if (fluid != null) {
                    int newColor = -16777216 | GTUtil.getFluidColor(fluid);
                    if (color != newColor) {
                        color = newColor;
                    }
                }
            }
        }
        super.onWorking();
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (MultiblockPage.isScreenText()) return;
        textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.energy", this.energyContainer.getEnergyStored(), this.energyContainer.getEnergyCapacity()));
        textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.heat", heat));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addLine(ENERGY_BUFFER, MultiblockPage.fractionText(() -> energyContainer.getEnergyStored(), () -> energyContainer.getEnergyCapacity(), "EU"));
        page.addNumber(HEAT, () -> heat, "");
    }

    @Override
    public int getTier() {
        return tier + highEnergyModules;
    }
}

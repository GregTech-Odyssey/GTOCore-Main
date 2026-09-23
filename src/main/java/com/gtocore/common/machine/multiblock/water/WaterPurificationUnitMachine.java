package com.gtocore.common.machine.multiblock.water;

import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.multiblock.NoEnergyCustomParallelMultiblockMachine;
import com.gtolib.utils.GTOUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
abstract class WaterPurificationUnitMachine extends NoEnergyCustomParallelMultiblockMachine implements IIWirelessInteractor<WaterPurificationPlantMachine>, IDataInfoProvider {

    abstract long prepareRecipe(RecipeHandlerUnit unit);

    /// 本轮产出下一级净化水的成功率（0-100），仅在运行时有意义
    abstract double getSuccessChance();

    private WaterPurificationPlantMachine netMachineCache;
    GTRecipe recipe;
    RecipeHandlerUnit unit;
    @SaveToDisk
    long eut;
    public final long multiple;
    private final ConditionalSubscriptionHandler tickSubs;

    WaterPurificationUnitMachine(MetaMachineBlockEntity holder, long multiple) {
        super(holder, m -> IParallelMachine.MAX_PARALLEL, m -> 1000L);
        this.multiple = multiple;
        tickSubs = new ConditionalSubscriptionHandler(this, this::tickUpdate, 80, this::isFormed);
        customParallelTrait.setDefaultMax(false);
    }

    private void tickUpdate() {
        WaterPurificationPlantMachine machine = getNetMachine();
        if (machine == null) getRecipeLogic().resetRecipeLogic();
        tickSubs.updateSubscription();
    }

    void calculateVoltage(long input) {
        eut = input * multiple / 2;
    }

    long parallel() {
        WaterPurificationPlantMachine machine = getNetMachine();
        if (machine == null) {
            return 0;
        }
        return Math.min(super.getParallel(), (machine.availableEu << 1) / multiple);
    }

    void setWorking(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    public void onContentChanges(RecipeHandlerUnit handlerList) {
        if (getRecipeLogic().isIdle()) {
            WaterPurificationPlantMachine machine = getNetMachine();
            if (machine != null && machine.getRecipeLogic().isIdle()) {
                machine.getRecipeLogic().updateTickSubscription();
            }
        }
    }

    @Override
    public Class<WaterPurificationPlantMachine> getProviderClass() {
        return WaterPurificationPlantMachine.class;
    }

    @Override
    public boolean firstTestMachine(WaterPurificationPlantMachine machine) {
        Level level = machine.getLevel();
        if (level != null && isFormed() && machine.isFormed() && GTOUtils.calculateDistance(machine.getPos(), getPos()) < 32) {
            machine.waterPurificationUnitMachineMap.put(this, getRecipeLogic().isWorking());
            return true;
        }
        return false;
    }

    @Override
    public boolean testMachine(WaterPurificationPlantMachine machine) {
        return isFormed() && machine.isFormed();
    }

    @Override
    public void removeNetMachineCache() {
        if (netMachineCache != null) {
            netMachineCache.waterPurificationUnitMachineMap.removeBoolean(this);
            netMachineCache = null;
        }
    }

    @Override
    public void onStructureFormed() {
        unit = null;
        super.onStructureFormed();
        if (!isRemote()) {
            getNetMachine();
            tickSubs.initialize(getLevel());
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        removeNetMachineCache();
        unit = null;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        tickSubs.unsubscribe();
        removeNetMachineCache();
    }

    /// 运行时信息，机器 GUI 与便携式扫描仪共用
    void addWorkingText(List<Component> textList) {
        textList.add(successChanceText(getSuccessChance()));
    }

    static Component successChanceText(double chance) {
        return Component.translatable("gtocore.machine.water_purification_unit.success_chance", FormattingUtil.formatNumber2Places(Math.min(chance, 100))).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (getRecipeLogic().isWorking()) addWorkingText(textList);
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if ((mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) && getRecipeLogic().isWorking()) {
            List<Component> list = new ArrayList<>(3);
            addWorkingText(list);
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    public SoundEntry getSound() {
        return GTSoundEntries.COOLING;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {}

    @Override
    public RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new CustomLogic(this);
    }

    private static final class CustomLogic extends RecipeLogic {

        private CustomLogic(WaterPurificationUnitMachine machine) {
            super(machine);
        }

        @Override
        public boolean findAndHandleRecipe() {
            return false;
        }

        @Override
        public void updateTickSubscription() {}

        @Override
        public void serverTick() {}

        @Override
        public boolean onRecipeFinish() {
            machine.afterWorking();
            if (lastRecipe != null) {
                machine.handleRecipeOutput(lastRecipe);
                lastRecipe = null;
            }
            if (suspendAfterFinish) {
                setStatus(SUSPEND);
                suspendAfterFinish = false;
            } else {
                setStatus(IDLE);
            }
            progress = 0;
            duration = 0;
            isActive = false;
            return false;
        }
    }

    @Override
    public void setNetMachineCache(final WaterPurificationPlantMachine netMachineCache) {
        this.netMachineCache = netMachineCache;
    }

    @Override
    public WaterPurificationPlantMachine getNetMachineCache() {
        return this.netMachineCache;
    }
}

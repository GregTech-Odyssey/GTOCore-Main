package com.gtocore.common.machine.trait;

import com.gtocore.common.machine.multiblock.electric.voidseries.AdvancedInfiniteDrillMachine;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.impl.DrillingControlCenterMachine;
import com.gtolib.api.machine.trait.IFluidDrillLogic;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.data.worldgen.bedrock.BedrockVeinKind;
import com.gregtechceu.gtceu.api.data.worldgen.bedrock.BedrockVeinSavedData;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.VeinDrillLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AdvancedInfiniteDrillLogic extends VeinDrillLogic implements IFluidDrillLogic {

    private static final int MAX_PROGRESS = 20;
    private final Reference2IntOpenHashMap<Fluid> veinFluids = new Reference2IntOpenHashMap<>();
    private final Reference2LongOpenHashMap<Fluid> perSecond = new Reference2LongOpenHashMap<>();
    @Setter
    @Getter
    private int range;
    private DrillingControlCenterMachine cache;

    public AdvancedInfiniteDrillLogic(IRecipeLogicMachine machine, int range) {
        super(machine);
        this.range = range;
    }

    @Override
    public AdvancedInfiniteDrillMachine getMachine() {
        return (AdvancedInfiniteDrillMachine) super.getMachine();
    }

    @Override
    protected boolean resolveVein(ServerLevel serverLevel) {
        if (veinFluids.isEmpty()) {
            getGridFluid(BedrockVeinKind.FLUID.data(serverLevel));
        }
        return !veinFluids.isEmpty();
    }

    @Nullable
    @Override
    protected GTRecipe buildDrillRecipe() {
        if (getMachine().isEmpty() || !getMachine().canRunnable()) {
            if (getMachine().isEmpty()) IdleReason.DRILL_HEAD_MISSING.setReason(getMachine());
            else IdleReason.INSUFFICIENT_TEMPERATURE.setReason(getMachine(), AdvancedInfiniteDrillMachine.RUNNING_HEAT, getMachine().getCurrentHeat());
            return null;
        }
        if (!veinFluids.isEmpty()) {
            var builder = RecipeBuilder.ofRaw().duration(MAX_PROGRESS).EUt(20000);
            veinFluids.reference2IntEntrySet().fastForEach(e -> builder.outputFluids(e.getKey(), e.getIntValue()));
            var recipe = builder.buildRawRecipe();
            recipe.modifier(outputMultiplier(), true);
            recipe = RecipeModifier.overclocking(getMachine(), RecipeHandlerUnit.NO_DATA, recipe);
            recordPerSecond(recipe);
            return recipe;
        }
        return null;
    }

    private long outputMultiplier() {
        return getParallel() * efficiency(getMachine().getRate() * 500);
    }

    private void recordPerSecond(@Nullable GTRecipe recipe) {
        perSecond.clear();
        if (recipe == null || recipe.duration <= 0) return;
        var outputs = recipe.fluidOutputs;
        for (int i = 0; i < outputs.size(); i++) {
            if (outputs.ingredient(i).displayKey() instanceof AEFluidKey key) {
                perSecond.addTo(key.getFluid(), outputs.effective(i, recipe.scale) * 20 / recipe.duration);
            }
        }
    }

    public long getOutputPerSecond(Fluid fluid) {
        if (isWorking() && perSecond.containsKey(fluid)) return perSecond.getLong(fluid);
        return (long) veinFluids.getInt(fluid) * outputMultiplier() * 20 / MAX_PROGRESS;
    }

    private long getParallel() {
        AdvancedInfiniteDrillMachine drill = getMachine();
        var currentHeat = drill.getCurrentHeat();
        var heat = drill.getRate();
        var efficiency = efficiency(currentHeat);
        var produced = (long) efficiency * heat;
        var machine = getNetMachine();
        if (machine != null) produced = (long) (produced * machine.getMultiplier());
        return produced;
    }

    /**
     * 温度乘数计算
     *
     * @param heat 当前温度
     * @return 乘数
     */
    private static int efficiency(int heat) {
        if (heat < 6000) {
            return 2;
        } else if (heat < 8000) {
            return 4;
        } else {
            return 8;
        }
    }

    private void getGridFluid(BedrockVeinSavedData<BedrockFluidDefinition> data) {
        int x = getChunkX();
        int z = getChunkZ();
        int mid = range / 2;
        for (int i = -mid; i <= mid; i++) {
            for (int j = -mid; j <= mid; j++) {
                var entry = data.entry(x + i, z + j);
                var definition = entry.definition();
                if (definition != null) {
                    int produced = entry.yieldPerOperation();
                    if (produced > 0) veinFluids.addTo(definition.fluid(), produced * 10);
                }
            }
        }
    }

    @NotNull
    public Reference2IntOpenHashMap<Fluid> getVeinFluids() {
        return veinFluids;
    }

    @Override
    public DrillingControlCenterMachine getNetMachineCache() {
        return cache;
    }

    @Override
    public void setNetMachineCache(DrillingControlCenterMachine cache) {
        this.cache = cache;
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        this.cache = null;
    }
}

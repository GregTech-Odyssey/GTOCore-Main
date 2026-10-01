package com.gtocore.common.machine.multiblock.electric.processing;

import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.multiblock.electric.space.spacestaion.AbstractSpaceStation;
import com.gtocore.config.GTORules;
import com.gtocore.data.IdleReason;

import com.gtolib.api.gui.ParallelConfigurator;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.api.machine.feature.multiblock.IStorageMultiblock;
import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;
import com.gtolib.api.machine.trait.CustomParallelTrait;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.GTORecipeModifiers;
import com.gtolib.api.recipe.TierDataKey;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiPart;
import com.gregtechceu.gtceu.api.machine.issue.IIssueProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueSink;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CleanroomMachine;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ProcessingPlantMachine extends StorageMultiblockMachine implements IParallelMachine, ITierCasingMachine, IIssueProvider {

    private static final Set<GTRecipeType> RECIPE_TYPES = Set.of(
            GTRecipeTypes.BENDER_RECIPES,
            GTRecipeTypes.COMPRESSOR_RECIPES,
            GTRecipeTypes.FORGE_HAMMER_RECIPES,
            GTRecipeTypes.CUTTER_RECIPES,
            GTRecipeTypes.LASER_ENGRAVER_RECIPES,
            GTRecipeTypes.EXTRUDER_RECIPES,
            GTRecipeTypes.LATHE_RECIPES,
            GTRecipeTypes.WIREMILL_RECIPES,
            GTRecipeTypes.FORMING_PRESS_RECIPES,
            GTRecipeTypes.DISTILLERY_RECIPES,
            GTRecipeTypes.POLARIZER_RECIPES,
            GTORecipeTypes.CLUSTER_RECIPES,
            GTORecipeTypes.ROLLING_RECIPES,
            GTRecipeTypes.PACKER_RECIPES,
            GTORecipeTypes.UNPACKER_RECIPES,
            GTRecipeTypes.ASSEMBLER_RECIPES,
            GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
            GTRecipeTypes.CENTRIFUGE_RECIPES,
            GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES,
            GTRecipeTypes.ELECTROLYZER_RECIPES,
            GTRecipeTypes.SIFTER_RECIPES,
            GTRecipeTypes.MACERATOR_RECIPES,
            GTRecipeTypes.EXTRACTOR_RECIPES,
            GTORecipeTypes.DEHYDRATOR_RECIPES,
            GTRecipeTypes.MIXER_RECIPES,
            GTRecipeTypes.CHEMICAL_BATH_RECIPES,
            GTRecipeTypes.ORE_WASHER_RECIPES,
            GTRecipeTypes.CHEMICAL_RECIPES,
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES,
            GTRecipeTypes.AUTOCLAVE_RECIPES,
            GTRecipeTypes.ALLOY_SMELTER_RECIPES,
            GTRecipeTypes.ARC_FURNACE_RECIPES,
            GTRecipeTypes.CANNER_RECIPES,
            GTRecipeTypes.BREWING_RECIPES,
            GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES,
            GTORecipeTypes.ARC_GENERATOR_RECIPES,
            GTORecipeTypes.LOOM_RECIPES,
            GTORecipeTypes.LAMINATOR_RECIPES,
            GTORecipeTypes.LASER_WELDER_RECIPES);

    public static Component getComponent() {
        var c = Component.empty();
        boolean first = true;
        for (var r : RECIPE_TYPES) {
            if (!first) c.append(", ");
            first = false;
            c.append(Component.translatable("gtceu." + r.registryName.getPath()));
        }
        return c;
    }

    private boolean mismatched;

    @SaveToDisk
    private final CustomParallelTrait customParallelTrait;

    private final TierCasingTrait tierCasingTrait;

    public ProcessingPlantMachine(MetaMachineBlockEntity holder) {
        super(holder, 1, ProcessingPlantMachine::filter);
        customParallelTrait = new CustomParallelTrait(this, machine -> {
            ProcessingPlantMachine processingPlantMachine = (ProcessingPlantMachine) machine;
            if (processingPlantMachine.getTier() <= 0) return 0;
            return (long) processingPlantMachine.getTier() * getParallelPerTier(processingPlantMachine.hasStructurePart(StructureModuleKeys.PROCESSING_PLANT_EXTENSION));
        });
        tierCasingTrait = new TierCasingTrait(this, GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER);
    }

    public static int getParallelPerTier(boolean hasModule) {
        int parallel = GTORules.PROCESSING_PLANT_PARALLEL.get();
        return hasModule ? parallel * 2 : parallel;
    }

    private static boolean filter(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaMachineItem metaMachineItem) {
            MachineDefinition definition = metaMachineItem.getDefinition();
            if (definition instanceof MultiblockMachineDefinition) {
                return false;
            }
            var types = definition.getRecipeTypes();
            if (types == null || types.length == 0) return false;
            GTRecipeType recipeType = types[0];
            return RECIPE_TYPES.contains(recipeType);
        }
        return false;
    }

    @Override
    public boolean findRecipe(GTRecipeType type, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle, @Nullable GTRecipeDefinition lockedRecipe) {
        if (machineStorage.updateEmpty()) IdleReason.MACHINE_STORAGE_EMPTY.report(this, IssueStage.SEARCH, null);
        return super.findRecipe(type, canHandle, lockedRecipe);
    }

    @Override
    public boolean checkConditions(RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (isEmpty()) {
            IdleReason.MACHINE_STORAGE_EMPTY.report(this, IssueStage.CONDITION, recipe);
            return false;
        }
        if (mismatched) {
            IdleReason.PROCESSING_TIER_MISMATCH.report(this, IssueStage.CONDITION, storedMachineTier(), tier, recipe);
            return false;
        }
        return super.checkConditions(unit, recipe);
    }

    private int storedMachineTier() {
        return machineStorage.storage.getStackInSlot(0).getItem() instanceof MetaMachineItem item ? item.getDefinition().getTier() : -1;
    }

    @Override
    public void collectIssues(IssueSink sink) {
        if (!isFormed()) return;
        if (machineStorage.updateEmpty()) IdleReason.MACHINE_STORAGE_EMPTY.collect(sink);
        else if (mismatched) IdleReason.PROCESSING_TIER_MISMATCH.collect(sink, storedMachineTier(), tier);
    }

    @Nullable
    @Override
    protected GTRecipe getRealRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (!mismatched && !isEmpty()) {
            recipe = GTORecipeModifiers.parallel(this, unit, recipe);
            if (recipe == null) return null;
            return RecipeModifier.overclocking(this, unit, recipe, false, 0.9, 0.8, 0.5);
        }
        if (machineStorage.updateEmpty()) IdleReason.MACHINE_STORAGE_EMPTY.report(this, IssueStage.MODIFIER, recipe.definition);
        else IdleReason.PROCESSING_TIER_MISMATCH.report(this, IssueStage.MODIFIER, storedMachineTier(), tier, recipe.definition);
        return null;
    }

    @Override
    public GTRecipeType[] getAvailableRecipeTypes() {
        var cache = availableRecipeTypesCache;
        if (cache == null) {
            mismatched = false;
            cache = new GTRecipeType[] { GTRecipeTypes.DUMMY_RECIPES };
            if (machineStorage.storage.getStackInSlot(0).getItem() instanceof MetaMachineItem metaMachineItem) {
                MachineDefinition definition = metaMachineItem.getDefinition();
                if (tier != definition.getTier()) {
                    mismatched = true;
                }
                cache = definition.getRecipeTypes();
                availableRecipeTypesCache = cache;
                for (var p : getParts()) {
                    if (p instanceof IWorkableMultiPart part) {
                        part.setAvailableRecipeTypes(cache);
                    }
                }
            }
        }
        return cache;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        customParallelTrait.onStructureInvalid();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new ParallelConfigurator(this));
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (!MultiblockPage.isScreenText()) MachineUtils.addRecipeTypeText(textList, this);
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading("gtceu.gui.machinemode", MultiblockPage.recipeTypeText(this, ProcessingArrayMachine.NO_MACHINE));
    }

    @Override
    public void onMachineChanged() {
        customParallelTrait.onStructureInvalid();
        if (isFormed) {
            if (getRecipeLogic().getLastRecipe() != null) {
                getRecipeLogic().markLastRecipeDirty();
            }
            getRecipeLogic().updateTickSubscription();
            customParallelTrait.onStructureFormed();
            availableRecipeTypesCache = null;
        }
    }

    @Override
    public long getMaxParallel() {
        return customParallelTrait.getMaxParallel();
    }

    @Override
    public long getMinParallel() {
        return customParallelTrait.getMinParallel();
    }

    @Override
    public long getParallel() {
        return customParallelTrait.getParallel();
    }

    @Override
    public void setParallel(long number) {
        customParallelTrait.setParallel(number);
    }

    @Override
    public void setCleanroom(@Nullable ICleanroomProvider provider) {
        if (provider instanceof CleanroomMachine || provider instanceof AbstractSpaceStation) super.setCleanroom(provider);
    }

    @Override
    public int getTier() {
        if (!isFormed) return 0;
        return Math.min(getCasingTier(GTORecipeDataKeys.INTEGRAL_FRAMEWORK_TIER), tier);
    }

    @Override
    public Reference2IntMap<TierDataKey> getCasingTiers() {
        return tierCasingTrait.getCasingTiers();
    }

    @Override
    public String getStorageSlotLabel() {
        return IStorageMultiblock.SLOT_MACHINE;
    }
}

package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.api.electrochemistry.HalfReaction;
import com.gtocore.api.electrochemistry.HalfReactionRecipeExtension;
import com.gtocore.api.electrochemistry.IonCombinations;
import com.gtocore.api.electrochemistry.IonDefinition;
import com.gtocore.api.electrochemistry.IonMembrane;
import com.gtocore.api.electrochemistry.MembraneType;
import com.gtocore.api.electrochemistry.MolarUnits;
import com.gtocore.api.electrochemistry.PairedReactionRecipeExtension;
import com.gtocore.api.electrochemistry.ReactionBalance;
import com.gtocore.api.machine.RecipeTypeOverrideUnit;
import com.gtocore.common.machine.multiblock.part.SemipermeableMembraneHatch;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ContentList;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.PlanScratch;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

/** Pairs half reactions with either separate side pools or one shared input unit. */
public class ElectrochemicalMachine extends ElectricMultiblockMachine {

    protected SemipermeableMembraneHatch membraneHatch;
    private RecipeHandlerUnit left = RecipeHandlerUnit.NO_DATA;
    private RecipeHandlerUnit right = RecipeHandlerUnit.NO_DATA;
    private RecipeHandlerUnit paired = RecipeHandlerUnit.NO_DATA;
    protected List<RecipeHandlerUnit> ordinaryInputUnits = Collections.emptyList();
    private final ArrayList<GTRecipeDefinition> firstCandidates = new ArrayList<>(16);
    private final ArrayList<GTRecipeDefinition> secondCandidates = new ArrayList<>(16);
    private final Reference2LongOpenHashMap<IonDefinition> transferred = new Reference2LongOpenHashMap<>(8);
    private static final MembraneType[] MEMBRANE_TYPES = MembraneType.values();
    private int failurePriority;
    private IdleReason searchFailure;
    private Object[] failureArguments;
    private Component recipeFailure;
    private boolean leftCandidateFound;
    // Reservations from saves written before membrane durability was charged at recipe start.
    @SaveToDisk(defaultValue = "0")
    private long pendingMembraneDamage;
    @SaveToDisk(defaultValue = "false")
    private boolean membraneInUse;

    public ElectrochemicalMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    public RecipeHandlerUnit leftUnit() {
        return left;
    }

    public RecipeHandlerUnit rightUnit() {
        return right;
    }

    protected boolean sharedReactionInputs() {
        return false;
    }

    public long getPairedInputParallel(GTRecipe recipe, long parallel) {
        parallel = Math.min(parallel, membraneHatch.parallelLimit(membraneUsesRemaining()));
        // ParallelLogic already bounds the combined inputs against this unit.
        if (sharedReactionInputs()) return parallel;
        var inputs = recipe.data.getData(PairedReactionRecipeExtension.INSTANCE);
        return Math.min(left.inputParallel(inputs.left(), parallel), right.inputParallel(inputs.right(), parallel));
    }

    public ItemStack membraneStack() {
        return membraneHatch == null ? ItemStack.EMPTY : membraneHatch.membrane();
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof SemipermeableMembraneHatch hatch) {
            membraneHatch = hatch;
            if (pendingMembraneDamage > 0) {
                // Older saves may only contain the controller's total reservation.
                if (!hatch.hasReservedUses() && !hatch.membrane().isEmpty()) hatch.reserveUses(pendingMembraneDamage);
                hatch.consumeReservedUses();
                pendingMembraneDamage = 0;
                membraneInUse = true;
                onChanged();
            }
            hatch.setLocked(membraneInUse);
        }
    }

    @Override
    public void arrangeHandlerList() {
        super.arrangeHandlerList();
        ordinaryInputUnits = getInputUnits();
        if (sharedReactionInputs()) return;
        var inputs = getCapabilitiesProxy(IO.IN);
        if (inputs.isEmpty()) return;
        var leftHandlers = new ReferenceOpenHashSet<IRecipeHandler>(inputs.size());
        var rightHandlers = new ReferenceOpenHashSet<IRecipeHandler>(inputs.size());
        var direction = RelativeDirection.LEFT.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped());
        var origin = getPos();
        for (var unit : inputs) {
            if (unit.part == null) continue;
            var pos = unit.part.self().getPos();
            int side = (pos.getX() - origin.getX()) * direction.getStepX() +
                    (pos.getY() - origin.getY()) * direction.getStepY() +
                    (pos.getZ() - origin.getZ()) * direction.getStepZ();
            if (side == 0) continue;
            var handlers = side > 0 ? leftHandlers : rightHandlers;
            handlers.addAll(Arrays.asList(unit.allHandlers));
        }
        left = RecipeHandlerUnit.of(IO.IN, leftHandlers);
        right = RecipeHandlerUnit.of(IO.IN, rightHandlers);
        left.setDistinct(true);
        right.setDistinct(true);
        setInputUnits(List.of(left, right));
        leftHandlers.addAll(rightHandlers);
        paired = RecipeHandlerUnit.of(IO.IN, leftHandlers);
    }

    @Override
    public void onStructureInvalid() {
        if (membraneHatch != null) membraneHatch.setLocked(false);
        pendingMembraneDamage = 0;
        membraneInUse = false;
        super.onStructureInvalid();
        membraneHatch = null;
        left = RecipeHandlerUnit.NO_DATA;
        right = RecipeHandlerUnit.NO_DATA;
        paired = RecipeHandlerUnit.NO_DATA;
        ordinaryInputUnits = Collections.emptyList();
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public boolean findRecipe(GTRecipeType type, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle, GTRecipeDefinition lockedRecipe) {
        failurePriority = 0;
        searchFailure = null;
        failureArguments = null;
        recipeFailure = null;
        leftCandidateFound = false;
        if (membraneStack().isEmpty()) {
            setIdleReason(IdleReason.ELECTROCHEMICAL_MEMBRANE_MISSING);
            return false;
        }
        if (!membraneHatch.hasAvailableUses()) {
            setIdleReason(IdleReason.ELECTROCHEMICAL_MEMBRANE_EXHAUSTED);
            return false;
        }
        boolean found = false;
        if (sharedReactionInputs()) {
            for (var unit : getInputUnits()) {
                if (RecipeTypeOverrideUnit.of(unit, type) != type) continue;
                if (findPairedRecipe(type, canHandle, unit, unit, unit)) {
                    found = true;
                    break;
                }
            }
        } else {
            found = findPairedRecipe(type, canHandle, left, right, paired);
        }
        if (!found) {
            if (recipeFailure != null) setIdleReason(recipeFailure);
            else if (searchFailure != null) {
                if (failureArguments == null) setIdleReason(searchFailure);
                else setIdleReason(searchFailure, failureArguments);
            } else setIdleReason(IdleReason.ELECTROCHEMICAL_HALF_MISSING, sideName(leftCandidateFound));
        }
        searchFailure = null;
        failureArguments = null;
        recipeFailure = null;
        return found;
    }

    private boolean findPairedRecipe(GTRecipeType type, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle,
                                     RecipeHandlerUnit firstUnit, RecipeHandlerUnit secondUnit, RecipeHandlerUnit pairedUnit) {
        firstCandidates.clear();
        secondCandidates.clear();
        // RecipeDB reuses its search state, so finish searching before pairing candidates.
        collectHalfRecipes(type, firstUnit, firstCandidates, true);
        if (firstUnit != secondUnit) collectHalfRecipes(type, secondUnit, secondCandidates, false);
        var seconds = firstUnit == secondUnit ? firstCandidates : secondCandidates;
        boolean found = false;
        for (int i = 0; i < firstCandidates.size(); i++) {
            var first = firstCandidates.get(i);
            var firstHalf = first.data.getData(HalfReactionRecipeExtension.INSTANCE);
            for (int j = 0; j < seconds.size(); j++) {
                var second = seconds.get(j);
                var secondHalf = second.data.getData(HalfReactionRecipeExtension.INSTANCE);
                if (firstHalf.electrolyteType() != secondHalf.electrolyteType()) {
                    recordFailure(IdleReason.ELECTROCHEMICAL_ELECTROLYTE_MISMATCH, 2);
                    continue;
                }
                if (!selectMembrane(firstHalf, secondHalf)) continue;
                var combined = combine(type, first, second, firstHalf, secondHalf);
                if (combined == null) continue;
                if (canHandle.test(pairedUnit, combined)) {
                    found = true;
                    break;
                }
                // Preserve input, voltage, condition and output failures from a fully paired recipe.
                recordRecipeFailure(8);
            }
            if (found) break;
        }
        firstCandidates.clear();
        secondCandidates.clear();
        return found;
    }

    private void collectHalfRecipes(GTRecipeType type, RecipeHandlerUnit unit, ArrayList<GTRecipeDefinition> candidates, boolean firstSide) {
        unit.findRecipe(type, (u, recipe) -> {
            if (!acceptHalf(recipe.data.getData(HalfReactionRecipeExtension.INSTANCE))) return false;
            if (firstSide) leftCandidateFound = true;
            if (!checkTier(recipe) || !checkConditions(u, recipe)) {
                recordRecipeFailure(1);
                return false;
            }
            candidates.add(recipe);
            return false;
        });
    }

    /** Keep the failure from the candidate that reached the furthest matching stage. */
    protected boolean recordFailure(IdleReason reason, int priority) {
        if (priority <= failurePriority) return false;
        failurePriority = priority;
        searchFailure = reason;
        failureArguments = null;
        recipeFailure = null;
        return true;
    }

    private void recordRecipeFailure(int priority) {
        if (priority <= failurePriority) return;
        failurePriority = priority;
        searchFailure = null;
        failureArguments = null;
        recipeFailure = getRecipeLogic().getIdleReason();
    }

    private static Component sideName(boolean rightSide) {
        return Component.translatable(rightSide ? "gtocore.electrochemical.right" : "gtocore.electrochemical.left");
    }

    protected boolean acceptHalf(HalfReaction half) {
        return half != null;
    }

    protected GTRecipeDefinition combine(GTRecipeType type, GTRecipeDefinition first, GTRecipeDefinition second, HalfReaction firstHalf, HalfReaction secondHalf) {
        var balance = ReactionBalance.cross(firstHalf.charge(), secondHalf.charge());
        if (balance == null) {
            recordFailure(IdleReason.ELECTROCHEMICAL_CHARGE_MISMATCH, 3);
            return null;
        }
        return combine(type, first, second, balance.firstMultiplier(), balance.secondMultiplier());
    }

    protected GTRecipeDefinition combine(GTRecipeType type, GTRecipeDefinition first, GTRecipeDefinition second, long firstMultiplier, long secondMultiplier) {
        if (!prepareIons(first.data.getData(HalfReactionRecipeExtension.INSTANCE), firstMultiplier,
                second.data.getData(HalfReactionRecipeExtension.INSTANCE), secondMultiplier))
            return null;
        var firstRuntime = first.toRuntime();
        var secondRuntime = second.toRuntime();
        firstRuntime.modifier(firstMultiplier, false);
        secondRuntime.modifier(secondMultiplier, false);
        firstRuntime.bake();
        secondRuntime.bake();
        if (!preparePair(firstRuntime, secondRuntime)) return null;
        var membrane = IonMembrane.of(membraneHatch.membrane());
        var totalEU = first.eut * first.duration * firstMultiplier + second.eut * second.duration * secondMultiplier;
        var totalDuration = totalEU / Math.max(1, (firstRuntime.eut + secondRuntime.eut) / 2);
        var builder = RecipeBuilder.ofRaw().recipeType(type)
                .EUt(Math.max(1, (long) ((double) (first.eut + second.eut) / 2 * membrane.euMultiplier())))
                .duration(Math.max(1, (int) (totalDuration * membrane.durationMultiplier())));
        addCombinations(builder);
        // Allocate recipe-local data before build; empty recipe data is shared.
        builder.addData(PairedReactionRecipeExtension.INSTANCE, new PairedReactionRecipeExtension.Inputs(firstRuntime, secondRuntime));
        var products = builder.build();
        return new GTRecipeDefinition(false, type, first.recipeCategory, products.id,
                ContentList.concat(firstRuntime.itemInputs, secondRuntime.itemInputs),
                ContentList.concat(ContentList.concat(firstRuntime.itemOutputs, secondRuntime.itemOutputs), products.itemOutputs),
                ContentList.concat(firstRuntime.fluidInputs, secondRuntime.fluidInputs),
                ContentList.concat(ContentList.concat(firstRuntime.fluidOutputs, secondRuntime.fluidOutputs), products.fluidOutputs),
                Collections.emptyList(), Collections.emptyList(), Collections.singletonList(PairedReactionRecipeExtension.INSTANCE), Collections.emptyList(), products.data, first.chanceFunction,
                products.eut, Math.max(first.tier, second.tier), products.duration, Math.max(first.priority, second.priority));
    }

    protected boolean preparePair(GTRecipe first, GTRecipe second) {
        return true;
    }

    private boolean prepareIons(HalfReaction first, long firstMultiplier, HalfReaction second, long secondMultiplier) {
        var missing = second.missingIon(first, secondMultiplier, firstMultiplier);
        boolean rightSide = false;
        if (missing == null) {
            missing = first.missingIon(second, firstMultiplier, secondMultiplier);
            rightSide = true;
        }
        if (missing != null) {
            if (recordFailure(IdleReason.ELECTROCHEMICAL_ION_MISSING, 4)) {
                failureArguments = new Object[] { sideName(rightSide), Component.translatable(missing.translationKey()) };
            }
            return false;
        }
        transferred.clear();
        for (var ion : first.ions()) transferred.addTo(ion.ion(), Math.multiplyExact(ion.amount(), firstMultiplier));
        for (var ion : second.ions()) transferred.addTo(ion.ion(), Math.multiplyExact(ion.amount(), secondMultiplier));
        for (var ion : first.requiredIons()) transferred.addTo(ion.ion(), -Math.multiplyExact(ion.amount(), firstMultiplier));
        for (var ion : second.requiredIons()) transferred.addTo(ion.ion(), -Math.multiplyExact(ion.amount(), secondMultiplier));
        return true;
    }

    private boolean selectMembrane(HalfReaction first, HalfReaction second) {
        int types = first.compatibleMembranes(second);
        if (types == 0) {
            recordFailure(IdleReason.ELECTROCHEMICAL_NO_COMMON_MEMBRANE, 5);
            return false;
        }
        boolean exhausted = false;
        for (var type : MEMBRANE_TYPES) {
            if ((types & (1 << type.ordinal())) == 0) continue;
            if (membraneHatch.selectMembrane(type)) return true;
            if (!membraneHatch.membrane(type).isEmpty()) exhausted = true;
        }
        var detected = IonMembrane.typeOf(membraneStack());
        if (exhausted) {
            recordFailure(IdleReason.ELECTROCHEMICAL_MEMBRANE_EXHAUSTED, 5);
        } else {
            if (recordFailure(IdleReason.ELECTROCHEMICAL_MEMBRANE_MISMATCH, 5)) {
                var allowed = Component.empty();
                for (var type : MEMBRANE_TYPES) {
                    if ((types & (1 << type.ordinal())) == 0) continue;
                    if (!allowed.getSiblings().isEmpty()) allowed.append(" / ");
                    allowed.append(Component.translatable(type.translationKey()));
                }
                failureArguments = new Object[] { allowed, detected == null ? membraneStack().getHoverName() : Component.translatable(detected.translationKey()) };
            }
        }
        return false;
    }

    private void addCombinations(RecipeBuilder builder) {
        for (var combination : IonCombinations.REGISTRY) {
            var positive = combination.positive();
            var negative = combination.negative();
            if (!membraneHatch.permits(positive.ion()) && !membraneHatch.permits(negative.ion())) continue;
            long amount = Math.min(transferred.getLong(positive.ion()) / positive.amount(), transferred.getLong(negative.ion()) / negative.amount());
            if (amount == 0) continue;
            MolarUnits.output(builder, combination.product(), Math.multiplyExact(amount, combination.productAmount()));
            transferred.addTo(positive.ion(), -amount * positive.amount());
            transferred.addTo(negative.ion(), -amount * negative.amount());
        }
    }

    @Override
    public boolean matchRecipeInput(RecipeHandlerUnit unit, GTRecipe recipe) {
        var inputs = recipe.data.getData(PairedReactionRecipeExtension.INSTANCE);
        if (inputs == null) return super.matchRecipeInput(unit, recipe);
        if (membraneHatch.durabilityCost(recipe.parallels, false) > membraneUsesRemaining()) {
            setIdleReason(IdleReason.ELECTROCHEMICAL_MEMBRANE_EXHAUSTED);
            return false;
        }
        if (sharedReactionInputs()) return super.matchRecipeInput(unit, recipe);
        if (!left.matchInputs(inputs.left())) {
            setIdleReason(IdleReason.ELECTROCHEMICAL_INPUT_SHORT, sideName(false));
            return false;
        }
        if (!right.matchInputs(inputs.right())) {
            setIdleReason(IdleReason.ELECTROCHEMICAL_INPUT_SHORT, sideName(true));
            return false;
        }
        return true;
    }

    @Override
    public boolean handleRecipeInput(RecipeHandlerUnit unit, GTRecipe recipe) {
        var inputs = recipe.data.getData(PairedReactionRecipeExtension.INSTANCE);
        if (inputs == null || sharedReactionInputs()) return super.handleRecipeInput(unit, recipe);
        var leftPlan = PlanScratch.acquire();
        var rightPlan = PlanScratch.acquire();
        try {
            left.rollInputs(inputs.left(), leftPlan);
            right.rollInputs(inputs.right(), rightPlan);
            if (!left.planInputs(inputs.left(), leftPlan, inputs.left().scale, true) || !right.planInputs(inputs.right(), rightPlan, inputs.right().scale, true)) return false;
            if (!left.commitFallible(leftPlan)) return false;
            if (!right.commitFallible(rightPlan)) {
                left.rollbackFallible(leftPlan);
                return false;
            }
            left.commitArrays(leftPlan);
            right.commitArrays(rightPlan);
            left.onCommitted(inputs.left());
            right.onCommitted(inputs.right());
            return true;
        } finally {
            PlanScratch.release();
            PlanScratch.release();
        }
    }

    @Override
    public void beforeWorking(RecipeHandlerUnit unit, GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        if (recipe.data.getData(PairedReactionRecipeExtension.INSTANCE) != null) {
            consumeMembraneUses(membraneHatch.durabilityCost(recipe.parallels, false));
        }
    }

    protected void consumeMembraneUses(long amount) {
        membraneInUse = true;
        membraneHatch.setLocked(true);
        membraneHatch.consumeUses(amount);
        onChanged();
    }

    public long membraneUsesRemaining() {
        return membraneHatch == null ? 0 : membraneHatch.remainingUses();
    }

    @Override
    public void afterWorking() {
        if (membraneInUse) {
            membraneInUse = false;
            onChanged();
            if (membraneHatch != null) membraneHatch.setLocked(false);
        }
        super.afterWorking();
    }
}

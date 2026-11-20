package com.gtocore.api.ae2.crafting;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineV2;
import com.gtocore.integration.ae.CraftingCpuHelperExtended;
import com.gtocore.integration.ae.hooks.ITemporaryCraftableService;

import com.gtolib.GTOCore;
import com.gtolib.ae2.pattern.ParallelAEProcessingPattern;
import com.gtolib.api.ae2.IPatternProviderLogic;
import com.gtolib.api.ae2.PushBatch;
import com.gtolib.api.ae2.pattern.IDetails;
import com.gtolib.api.ae2.pattern.IParallelPatternDetails;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.*;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.*;
import appeng.core.AEConfig;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.CraftingJobStatusPacket;
import appeng.crafting.CraftingLink;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.CraftingSubmitResult;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.hooks.ticking.TickHandler;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.CraftingJobSource;
import appeng.me.service.CraftingService;

import com.gto.datasynclib.util.holder.LongHolder;
import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class OptimizedCraftingCpuLogic extends CraftingCpuLogic {

    private ExecutingCraftingJob job = null;

    private Consumer<AEKey> listener = null;

    private final TaskBatch batch = new TaskBatch();

    private final SetMultimap<AEKey, GlobalPos> pendingRequests = Multimaps.newSetMultimap(new AEKeyObjectMap<>(), OpenCacheHashSet::new);
    @Getter
    private final SetMultimap<AEKey, IPatternProviderLogic.PushResult> craftingResults = Multimaps.newSetMultimap(new AEKeyObjectMap<>(), ReferenceOpenHashSet::new);

    public OptimizedCraftingCpuLogic(CraftingCPUCluster cluster) {
        super(cluster);
    }

    @Override
    protected void postChange(AEKey what) {
        lastModifiedOnTick = TickHandler.instance().getCurrentTick();
        if (listener != null) {
            listener.accept(what);
        }
    }

    @Override
    public ICraftingSubmitResult trySubmitJob(IGrid grid, ICraftingPlan plan, IActionSource src, ICraftingRequester requester) {
        if (this.job != null) return CraftingSubmitResult.CPU_BUSY;
        if (!cluster.isActive()) return CraftingSubmitResult.CPU_OFFLINE;
        if (cluster.getAvailableStorage() < plan.bytes()) return CraftingSubmitResult.CPU_TOO_SMALL;

        if (!inventory.list.isEmpty()) GTOCore.LOGGER.error("Crafting CPU inventory is not empty yet a job was submitted.");

        KeyCounter missingIng;
        if (AEConfig.instance().isAllowMissingCraftingJobs() && src.player().isPresent()) {
            missingIng = CraftingCpuHelperExtended.tryExtractInitialItemsIgnoreMissing(plan, grid, inventory, src);
        } else {
            var missingIngredient = CraftingCpuHelper.tryExtractInitialItems(plan, grid, inventory, src);
            if (missingIngredient != null) return CraftingSubmitResult.missingIngredient(missingIngredient);
            missingIng = new KeyCounter();
        }

        var playerId = src.player()
                .map(p -> p instanceof ServerPlayer serverPlayer ? IPlayerRegistry.getPlayerId(serverPlayer) : null)
                .orElse(null);
        var craftId = UUID.randomUUID();
        var linkCpu = new CraftingLink(CraftingCpuHelper.generateLinkData(craftId, requester == null, false), cluster);
        this.job = new ExecutingCraftingJob(plan, this::postChange, linkCpu, playerId, missingIng);
        this.job.followJob = src instanceof CraftingJobSource craftingSource && craftingSource.isFollowing();
        cluster.updateOutput(plan.finalOutput());
        cluster.markDirty();
        notifyJobOwner(job, CraftingJobStatusPacket.Status.STARTED);
        ((ITemporaryCraftableService) grid.getCraftingService()).gto$setTempPatternDetails(null);
        if (requester != null) {
            var linkReq = new CraftingLink(CraftingCpuHelper.generateLinkData(craftId, false, true), requester);

            var craftingService = (CraftingService) grid.getCraftingService();
            craftingService.addLink(linkCpu);
            craftingService.addLink(linkReq);

            return CraftingSubmitResult.successful(linkReq);
        } else {
            return CraftingSubmitResult.successful(null);
        }
    }

    @Override
    public void tickCraftingLogic(IEnergyService eg, CraftingService cc) {
        if (!cluster.isActive()) return;
        cantStoreItems = false;
        if (this.job == null) {
            this.storeItems();
            if (!this.inventory.list.isEmpty()) {
                cantStoreItems = true;
            }
            return;
        }
        if (job.link.isCanceled()) {
            cancel();
            return;
        }

        if (executeCrafting(cluster.getCoProcessors(), cc, eg, cluster.getLevel()) == 0) {
            GenericStack stack = getFinalJobOutput();
            if (job != null && job.isOrder && stack != null) {
                // the job is crafting an order and is waiting for an order, which means its dependencies have been
                // crafted
                job.waitingFor.list.removeZeros();
                final var waitingFor = getWaitingFor(stack.what());
                if (waitingFor > 0) {
                    final var remainingAmount = job.remainingAmount - waitingFor;
                    // Simulate inserting final result with the same logic as CraftingCpuLogic.insert
                    if (remainingAmount <= 0) {
                        finishJob(true);
                        cluster.updateOutput(null);
                    } else {
                        cluster.updateOutput(new GenericStack(stack.what(), remainingAmount));
                    }
                } else if (job.waitingFor.list.isEmpty() && job.tasks.size() == 1 && stack.what() instanceof AEItemKey item && item.getItem() == GTOItems.TEMP_ORDER.asItem()) {
                    // Temp order item has no real pattern details, so if it's the only task left, and we're not waiting
                    // for anything, we can assume the job is done
                    finishJob(true);
                    cluster.updateOutput(null);
                }
            }
        }
    }

    private static void purgePatternEverywhere(AEKeyObjectMap<AEKey, Object2LongOpenHashMap<IPatternDetails>> allocations, Object patternDefinition) {
        if (allocations == null || allocations.isEmpty() || patternDefinition == null) return;
        for (var outIt = allocations.reference2ObjectEntrySet().fastIterator(); outIt.hasNext();) {
            var out = outIt.next();
            var inner = out.getValue();
            if (inner == null || inner.isEmpty()) {
                outIt.remove();
                continue;
            }
            for (var inIt = inner.object2LongEntrySet().fastIterator(); inIt.hasNext();) {
                var pe = inIt.next();
                if (pe.getKey().getDefinition().equals(patternDefinition)) {
                    inIt.remove();
                }
            }
            if (inner.isEmpty()) {
                outIt.remove();
            }
        }
    }

    private int executeCrafting(int maxPatterns, CraftingService craftingService, IEnergyService energyService, Level level) {
        var job = this.job;
        if (job == null || job.paused) return 0;

        var batch = this.batch;
        batch.start(job, energyService, maxPatterns);

        var it = job.tasks.object2ObjectEntrySet().fastIterator();

        taskLoop:
        while (it.hasNext()) {
            var task = it.next();
            var progress = task.getValue();
            if (progress.value <= 0) {
                it.remove();
                continue;
            }

            // 寻找样板和对应的可用优先物品名额
            if (!job.allocations.isEmpty()) {
                job.defsToPurge.clear();
                for (var outerIt = job.allocations.reference2ObjectEntrySet().fastIterator(); outerIt.hasNext();) {
                    var outer = outerIt.next();
                    var inner = outer.getValue();
                    if (inner == null || inner.isEmpty()) continue;
                    for (var peIt = inner.object2LongEntrySet().fastIterator(); peIt.hasNext();) {
                        var pe = peIt.next();
                        if (pe.getLongValue() <= 0) {
                            job.defsToPurge.add(pe.getKey().getDefinition());
                        }
                    }
                }
                for (var def : job.defsToPurge) {
                    purgePatternEverywhere(job.allocations, def);
                }
            }

            var pattern = task.getKey();
            long maxParallel = pattern instanceof ParallelAEProcessingPattern processing ? batch.limitProcessingParallel(processing, progress.value) :
                    pattern instanceof IParallelPatternDetails && progress.value > 1 ? getMaxParallel(progress.value, pattern, inventory.list.getMap()) : 1;
            if (maxParallel == 0) continue;
            var targetOutputKey = pattern.getPrimaryOutput().what();
            batch.begin(it, progress);
            long preparedParallel = 0;
            for (ICraftingProvider iCraftingProvider : craftingService.getProviders(pattern)) {
                if (iCraftingProvider.isBusy()) continue;

                long parallel = maxParallel;
                if (iCraftingProvider instanceof MEPatternBufferPartMachineV2 buffer && pattern instanceof ParallelAEProcessingPattern processing) {
                    parallel = buffer.limitInputParallel(processing, parallel);
                    if (parallel == 0) continue;
                }
                if (parallel != preparedParallel) {
                    batch.returnInputs();
                    preparedParallel = parallel;
                    batch.prepare(pattern, parallel);
                }
                var inputs = batch.inputs();
                if (inputs == null) continue;
                var details = batch.details();

                job.currentConsumed.clear();
                for (var kc : inputs) {
                    if (kc == null) continue;
                    for (var entry : kc) {
                        job.currentConsumed.insert(entry.getKey(), entry.getLongValue());
                    }
                }
                if (!batch.powered(CraftingCpuHelper.calculatePatternPower(inputs))) break;

                if (iCraftingProvider instanceof IPatternProviderLogic logic) {
                    var result = logic.gtolib$pushPattern(batch);
                    if (result != IPatternProviderLogic.PushResult.PATTERN_DOES_NOT_EXIST) {
                        this.pendingRequests.put(targetOutputKey, logic.gto$getPos());
                    }
                    if (!result.success()) {
                        this.craftingResults.put(targetOutputKey, result);
                        continue;
                    }
                    this.craftingResults.removeAll(targetOutputKey);
                    this.craftingResults.put(targetOutputKey, result);
                    cluster.markDirty();
                } else if (iCraftingProvider.pushPattern(details, inputs)) {
                    batch.next();
                    this.craftingResults.removeAll(targetOutputKey);
                    this.craftingResults.put(targetOutputKey, IPatternProviderLogic.PushResult.SUCCESS);
                    cluster.markDirty();
                    if (iCraftingProvider instanceof BlockEntity be) {
                        this.pendingRequests.put(targetOutputKey, GlobalPos.of(be.getLevel().dimension(), be.getBlockPos()));
                    } else if (iCraftingProvider instanceof MetaMachine mm) {
                        this.pendingRequests.put(targetOutputKey, GlobalPos.of(mm.getLevel().dimension(), mm.getPos()));
                    }
                } else {
                    continue;
                }
                if (batch.stop == TaskBatch.STOP_ALL) break taskLoop;
                if (batch.stop == TaskBatch.STOP_TASK) continue taskLoop;
                if (batch.inputs() == null) break;
            }

            batch.returnInputs();
        }

        return batch.finish();
    }

    private final class TaskBatch extends PushBatch {

        private static final byte GO = 0;
        private static final byte STOP_TASK = 1;
        private static final byte STOP_ALL = 2;

        private ExecutingCraftingJob job;
        private IEnergyService energy;
        private Iterator<?> tasks;
        private LongHolder progress;
        private long parallel;
        private boolean oneShot;
        private double power;
        private int maxPatterns;
        private int pushed;
        private byte stop;

        private void start(ExecutingCraftingJob job, IEnergyService energy, int maxPatterns) {
            this.job = job;
            this.energy = energy;
            this.maxPatterns = maxPatterns;
            this.pushed = 0;
        }

        private void begin(Iterator<?> tasks, LongHolder progress) {
            this.tasks = tasks;
            this.progress = progress;
            this.stop = GO;
        }

        private long limitProcessingParallel(ParallelAEProcessingPattern pattern, long parallel) {
            var stock = inventory.list.getMap();
            var definition = pattern.getDefinition();
            boolean allocate = !job.allocations.isEmpty();
            long allocated = parallel;
            for (var input : pattern.getInputs()) {
                var key = input.getPossibleInputs()[0].what();
                long perCraft = input.getMultiplier();
                if (perCraft <= 0) return 0;
                parallel = Math.min(parallel, stock.getAmount(key) / perCraft);
                if (parallel == 0) return 0;
                if (allocate) {
                    allocated = Math.min(allocated, getAllocationQuota(key, definition) / perCraft);
                }
            }
            if (allocated == 0) {
                craftingResults.put(pattern.getPrimaryOutput().what(), IPatternProviderLogic.PushResult.INSUFFICIENT_PRIORITY);
            }
            return Math.min(parallel, allocated);
        }

        private void prepare(IPatternDetails pattern, long requestedParallel) {
            var details = pattern;
            if (pattern instanceof IParallelPatternDetails parallelPattern &&
                    (requestedParallel > 1 || pattern instanceof ParallelAEProcessingPattern && parallelPattern.getParallel() != 1)) {
                var scaled = parallelPattern.getCopy();
                scaled.parallel(requestedParallel);
                details = scaled;
            }
            job.expectedOutputs.clear();
            var inputs = extractPatternInputs(details, inventory, job.expectedOutputs);
            if (inputs == null) return;

            long allocatedParallel = pattern instanceof ParallelAEProcessingPattern ? requestedParallel :
                    limitByAllocation(details, inputs, requestedParallel);
            if (allocatedParallel < requestedParallel) {
                CraftingCpuHelper.reinjectPatternInputs(inventory, inputs);
                if (allocatedParallel <= 0 || !(details instanceof IParallelPatternDetails scaled) || scaled.getParallel() <= 1) {
                    craftingResults.put(pattern.getPrimaryOutput().what(), IPatternProviderLogic.PushResult.INSUFFICIENT_PRIORITY);
                    return;
                }
                scaled.parallel(allocatedParallel);
                job.expectedOutputs.reset();
                inputs = extractPatternInputs(details, inventory, job.expectedOutputs);
                if (inputs == null) return;
            }
            this.details = details;
            this.inputs = inputs;
            this.parallel = allocatedParallel;
            this.oneShot = pattern instanceof IParallelPatternDetails;
        }

        private long limitByAllocation(IPatternDetails pattern, KeyCounter[] inputs, long parallel) {
            if (job.allocations.isEmpty()) return parallel;
            job.totalConsumed.clear();
            for (var input : inputs) {
                for (var entry : input) {
                    job.totalConsumed.insert(entry.getKey(), entry.getLongValue());
                }
            }
            long allowed = parallel;
            var definition = pattern.getDefinition();
            for (var consumed : job.totalConsumed) {
                long quota = getAllocationQuota(consumed.getKey(), definition);
                long perCraft = Math.max(1, consumed.getLongValue() / parallel);
                allowed = Math.min(allowed, quota / perCraft);
                if (allowed <= 0) return 0;
            }
            return allowed;
        }

        private long getAllocationQuota(AEKey key, AEItemKey definition) {
            var allocations = job.allocations.get(key);
            if (allocations == null || allocations.isEmpty()) return Long.MAX_VALUE;
            for (var it = allocations.object2LongEntrySet().fastIterator(); it.hasNext();) {
                var allocation = it.next();
                if (allocation.getKey().getDefinition().equals(definition)) return allocation.getLongValue();
            }
            return 0;
        }

        private void returnInputs() {
            if (inputs == null) return;
            CraftingCpuHelper.reinjectPatternInputs(inventory, inputs);
            inputs = null;
        }

        private boolean powered(double power) {
            this.power = power;
            return power <= 0 || energy.extractAEPower(power, Actionable.SIMULATE, PowerMultiplier.CONFIG) >= power - 0.01;
        }

        private int finish() {
            job = null;
            energy = null;
            tasks = null;
            progress = null;
            details = null;
            inputs = null;
            return pushed;
        }

        private boolean halt(byte reason) {
            stop = reason;
            inputs = null;
            return false;
        }

        @Override
        public boolean next() {
            var job = this.job;
            if (power > 0) {
                energy.extractAEPower(power, Actionable.MODULATE, PowerMultiplier.CONFIG);
            }
            pushed++;

            for (var expectedOutput : job.expectedOutputs) {
                job.waitingFor.insert(expectedOutput.getKey(), expectedOutput.getLongValue(), Actionable.MODULATE);
            }

            job.purgeDefsLocal.clear();
            var detailsDef = details.getDefinition();
            for (var ce : job.currentConsumed) {
                var key = ce.getKey();
                var map = job.allocations.get(key);
                if (map == null || map.isEmpty()) continue;
                IPatternDetails matchedKey = null;
                for (var aeIt = map.object2LongEntrySet().fastIterator(); aeIt.hasNext();) {
                    var ae = aeIt.next();
                    if (ae.getKey().getDefinition().equals(detailsDef)) {
                        matchedKey = ae.getKey();
                        break;
                    }
                }
                if (matchedKey != null) {
                    long q = map.getLong(matchedKey);
                    long newQ = q - ce.getLongValue();
                    if (newQ <= 0) {
                        job.purgeDefsLocal.add(detailsDef);
                    } else {
                        map.put(matchedKey, newQ);
                    }
                }
            }
            for (var def : job.purgeDefsLocal) {
                purgePatternEverywhere(job.allocations, def);
            }

            progress.value -= parallel;
            boolean done = progress.value <= 0;
            if (done) tasks.remove();
            if (pushed > maxPatterns) return halt(STOP_ALL);
            if (done || oneShot) return halt(STOP_TASK);
            if (!powered(power)) return halt(STOP_TASK);

            job.expectedOutputs.reset();
            inputs = extractPatternInputs(details, inventory, job.expectedOutputs);
            return inputs != null;
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable type) {
        if (what == null || job == null)
            return 0;

        var waitingFor = job.waitingFor.extract(what, amount, Actionable.SIMULATE);
        if (waitingFor <= 0) {
            return 0;
        }

        if (amount > waitingFor) {
            amount = waitingFor;
        }

        if (type == Actionable.MODULATE) {
            job.waitingFor.extract(what, amount, Actionable.MODULATE);
            if (amount == waitingFor) {
                this.pendingRequests.removeAll(what);
            }
            job.tt.gtolib$decrementItems(amount, what.getType());
            cluster.markDirty();
        }

        long inserted = amount;
        if (what.matches(job.finalOutput)) {
            inserted = job.link.insert(what, amount, type);

            if (type == Actionable.MODULATE) {
                postChange(what);
                job.remainingAmount = Math.max(0, job.remainingAmount - amount);

                if (job.remainingAmount <= 0) {
                    finishJob(true);
                    cluster.updateOutput(null);
                } else {
                    cluster.updateOutput(new GenericStack(job.finalOutput.what(), job.remainingAmount));
                }
            }
        } else {
            if (type == Actionable.MODULATE) {
                inventory.insert(what, amount, Actionable.MODULATE);
            }
        }

        return inserted;
    }

    @Override
    public void cancel() {
        if (job == null) return;
        cluster.updateOutput(null);
        finishJob(false);
    }

    public void setPaused(boolean paused) {
        if (this.job != null) {
            this.job.paused = paused;
            cluster.markDirty();
        }
    }

    private void storeItems() {
        if (this.inventory.list.isEmpty()) return;

        var g = cluster.getGrid();
        if (g == null) return;

        var storage = g.getStorageService().getInventory();

        for (var entry : this.inventory.list) {
            postChange(entry.getKey());
            var inserted = storage.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, cluster.getSrc());

            entry.setValue(entry.getLongValue() - inserted);
        }
        this.inventory.list.removeZeros();

        cluster.markDirty();
    }

    @Override
    public boolean hasJob() {
        return this.job != null;
    }

    public long getJobStartedAt() {
        return job == null ? 0 : job.startedAt;
    }

    public long getJobUsedBytes() {
        return job == null ? -1 : job.usedBytes;
    }

    @Override
    public GenericStack getFinalJobOutput() {
        return this.job != null ? this.job.finalOutput : null;
    }

    @Override
    public ElapsedTimeTracker getElapsedTimeTracker() {
        if (this.job != null) {
            return this.job.timeTracker;
        } else {
            return new ElapsedTimeTracker();
        }
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        this.inventory.readFromNBT(data.getList("inventory", 10));
        if (data.contains("job")) {
            this.job = new ExecutingCraftingJob(data.getCompound("job"), this::postChange, this);
            if (job.finalOutput == null) {
                this.job = null;
            } else {
                cluster.updateOutput(new GenericStack(job.finalOutput.what(), job.remainingAmount));
                return;
            }
        }
        cluster.updateOutput(null);
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        data.put("inventory", this.inventory.writeToNBT());
        if (this.job != null) {
            data.put("job", this.job.writeToNBT());
        }
    }

    @Override
    public ICraftingLink getLastLink() {
        if (this.job != null) {
            return this.job.link;
        }
        return null;
    }

    @Override
    public void addListener(Consumer<AEKey> listener) {
        this.listener = listener;
    }

    @Override
    public void removeListener(Consumer<AEKey> listener) {
        this.listener = null;
    }

    @Override
    public long getWaitingFor(AEKey template) {
        if (this.job != null) {
            return this.job.waitingFor.extract(template, Long.MAX_VALUE, Actionable.SIMULATE);
        }
        return 0;
    }

    @Override
    public void getAllWaitingFor(Set<AEKey> waitingFor) {
        if (this.job != null) {
            for (var entry : this.job.waitingFor.list) {
                waitingFor.add(entry.getKey());
            }
        }
    }

    @Override
    public long getPendingOutputs(AEKey template) {
        long count = 0;
        if (this.job != null) {
            for (ObjectIterator<Object2ObjectMap.Entry<IPatternDetails, LongHolder>> it = job.tasks.object2ObjectEntrySet().fastIterator(); it.hasNext();) {
                var entry = it.next();
                for (var output : entry.getKey().getOutputs()) {
                    if (template.matches(output)) {
                        count += output.amount() * entry.getValue().value;
                    }
                }
            }
        }
        return count;
    }

    public Set<GlobalPos> getPendingRequests(AEKey template) {
        return this.pendingRequests.get(template);
    }

    @Override
    public void getAllItems(KeyCounter out) {
        out.addAll(this.inventory.list);
        if (this.job != null) {
            out.addAll(job.waitingFor.list);
            for (ObjectIterator<Object2ObjectMap.Entry<IPatternDetails, LongHolder>> it = job.tasks.object2ObjectEntrySet().fastIterator(); it.hasNext();) {
                var entry = it.next();
                for (var output : entry.getKey().getOutputs()) {
                    out.add(output.what(), output.amount() * entry.getValue().value);
                }
            }
        }
    }

    private void finishJob(boolean success) {
        if (success) {
            job.link.markDone();
        } else {
            job.link.cancel();
        }

        job.waitingFor.clear();
        for (ObjectIterator<Object2ObjectMap.Entry<IPatternDetails, LongHolder>> it = job.tasks.object2ObjectEntrySet().fastIterator(); it.hasNext();) {
            for (var output : it.next().getKey().getOutputs()) {
                postChange(output.what());
            }
        }

        notifyJobOwner(job, success ? CraftingJobStatusPacket.Status.FINISHED : CraftingJobStatusPacket.Status.CANCELLED);

        this.job = null;

        this.pendingRequests.clear();
        this.craftingResults.clear();

        this.storeItems();
    }

    private void notifyJobOwner(ExecutingCraftingJob job, CraftingJobStatusPacket.Status status) {
        this.lastModifiedOnTick = TickHandler.instance().getCurrentTick();

        var playerId = job.playerId;
        if (playerId == null) {
            return;
        }

        var server = cluster.getLevel().getServer();
        var connectedPlayer = IPlayerRegistry.getConnected(server, playerId);
        if (connectedPlayer != null) {
            var jobId = job.link.getCraftingID();
            NetworkHandler.instance().sendTo(
                    new CraftingJobStatusPacket(
                            jobId,
                            job.finalOutput.what(),
                            job.finalOutput.amount(),
                            job.remainingAmount,
                            status,
                            job.timeTracker.getElapsedTime(),
                            job.followJob),
                    connectedPlayer);
        }
    }

    private static KeyCounter[] extractPatternInputs(IPatternDetails details, ListCraftingInventory sourceInv, KeyCounter expectedOutputs) {
        var inputs = details.getInputs();
        KeyCounter[] inputHolder = getInputHolder((IDetails) details);
        boolean found = true;

        var counter = sourceInv.list;
        for (int x = 0; x < inputs.length; x++) {
            var list = inputHolder[x];
            var input = inputs[x];
            var possibleInputs = input.getPossibleInputs();
            boolean combineSubstitutes = possibleInputs.length > 1 && hasSameTemplateAmount(possibleInputs);
            long remaining = input.getMultiplier();
            if (combineSubstitutes) {
                remaining *= possibleInputs[0].amount();
            }
            for (var stack : possibleInputs) {
                if (!counter.contains(stack.what())) continue;
                long amount = combineSubstitutes ? 1 : stack.amount();
                var extracted = sourceInv.extract(stack.what(), amount * remaining, Actionable.MODULATE);
                if (extracted == 0) continue;
                list.add(stack.what(), extracted);
                remaining -= (extracted / amount);
                if (remaining == 0) break;
            }

            if (remaining > 0) {
                found = false;
                break;
            }
        }

        if (!found) {
            CraftingCpuHelper.reinjectPatternInputs(sourceInv, inputHolder);
            return null;
        }

        for (var output : details.getOutputs()) {
            expectedOutputs.add(output.what(), output.amount());
        }

        return inputHolder;
    }

    private static long getMaxParallel(long maxParallel, IPatternDetails details, AEKeyLongMap<AEKey> sourceInv) {
        if (sourceInv.isEmpty()) return 0;
        var seen = new AEKeySet<AEKey>();
        for (IPatternDetails.IInput input : details.getInputs()) {
            long extracted = 0;
            seen.clear();
            for (var stack : input.getPossibleInputs()) {
                if (seen.add(stack.what())) {
                    extracted += sourceInv.getAmount(stack.what()) / stack.amount();
                }
            }
            maxParallel = Math.min(maxParallel, extracted / input.getMultiplier());
            if (maxParallel < 1) {
                return 0;
            }
        }
        return maxParallel;
    }

    private static boolean hasSameTemplateAmount(GenericStack[] inputs) {
        long amount = inputs[0].amount();
        for (int i = 1; i < inputs.length; i++) {
            if (inputs[i].amount() != amount) return false;
        }
        return true;
    }

    private static KeyCounter[] getInputHolder(IDetails details) {
        int length = details.getInputs().length;
        var inputHolder = new KeyCounter[length];
        var ih = details.gtolib$getInputHolder();
        for (int x = 0; x < length; x++) {
            var kc = ih[x];
            kc.clear();
            inputHolder[x] = kc;
        }
        return inputHolder;
    }

    public boolean isPaused() {
        if (this.job != null) {
            return this.job.paused;
        }
        return false;
    }
}

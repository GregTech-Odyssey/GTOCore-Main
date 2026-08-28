package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidSlot;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemSlot;
import com.gtocore.common.machine.multiblock.part.ae.widget.MEInputBufferPartMachineUIKt;
import com.gtocore.common.machine.multiblock.part.ae.widget.slot.AEPatternViewSlotWidgetKt;
import com.gtocore.common.machine.trait.InternalSlotRecipeHandler;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.gui.ktflexible.VBoxBuilder;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.MultiCraftingTracker;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.annotations.SyncToServer;
import com.gto.datasynclib.listener.IntNotifiableHolder;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.IntSupplier;

@DataGeneratorScanned
public class MEInputBufferPartMachine extends MEPatternPartMachineKt<MEInputBufferPartMachine.InternalSlot> {

    private IStackWatcher craftingWatcher;

    private final List<RecipeHandlerUnit> recipeHandlers;

    @SyncToClient
    final boolean[] disconnectStates = new boolean[getMaxPatternCount()];

    @Getter
    @SyncToServer
    public IntNotifiableHolder configuratorField = IntNotifiableHolder.create(-1)
            .setReceiverListener((side, o, n) -> {
                if (side.isServer()) TaskHandler.enqueueTask(Objects.requireNonNull(getLevel()), () -> freshWidgetGroup.serverFresh());
            });

    @Override
    public void onMouseClicked(int index) {
        if (!isRemote()) return;
        if (configuratorField.get() == index) {
            configuratorField.set(-1);
        } else {
            configuratorField.set(index);
        }
        configuratorField.markAsChanged();
        syncToServer();
    }

    private final Multimap<AEKey, InternalSlot> watcher2SlotMap = Multimaps.newSetMultimap(new Reference2ObjectOpenHashMap<>(), ReferenceOpenHashSet::new);
    private final Reference2ReferenceMap<InternalSlot, AEKey> slot2WatcherMap = new Reference2ReferenceOpenHashMap<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final ICraftingWatcherNode craftingWatcherNode = new ICraftingWatcherNode() {

        @Override
        public void updateWatcher(IStackWatcher newWatcher) {
            craftingWatcher = newWatcher;
            configureWatchers();
        }

        @Override
        public void onRequestChange(AEKey what) {
            updateState();
        }

        @Override
        public void onCraftableChange(AEKey what) {}
    };

    @Nullable
    private TickableSubscription autoIOSubs;

    public MEInputBufferPartMachine(MetaMachineBlockEntity holder) {
        super(holder, 9);
        getMainNode().addService(ICraftingWatcherNode.class, craftingWatcherNode);
        this.recipeHandlers = Arrays.stream(getInternalInventory())
                .map(s -> (RecipeHandlerUnit) new SlotRHL(s, this)).toList();
    }

    void autoIO() {
        if (this.updateMEStatus()) {
            IGrid grid = getMainNode().getGrid();
            if (grid == null) {
                return;
            }
            for (InternalSlot slot : getInternalInventory()) {
                slot.syncME(grid);
            }
            this.updateSubscription();
            configureWatchers();
        }
    }

    private void updateSubscription() {
        if (isWorkingEnabled() && getOnlineField()) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO, 40);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.@NotNull State reason) {
        super.onMainNodeStateChanged(reason);
        this.updateSubscription();
    }

    @Override
    public void onDetailsPostInit() {
        for (InternalSlot slot : getInternalInventory()) {
            slot.reloadConfig();
        }
        configureWatchers();
    }

    @Override
    public @NotNull InternalSlot createInternalSlot(int i) {
        return new InternalSlot(this, i);
    }

    @Override
    public InternalSlot @NotNull [] createInternalSlotArray() {
        return new InternalSlot[getMaxPatternCount()];
    }

    @Override
    public @NotNull List<RecipeHandlerUnit> getRecipeHandlers() {
        return recipeHandlers;
    }

    @Override
    public @NotNull List<IPatternDetails> getAvailablePatterns() {
        return Collections.emptyList();
    }

    @Override
    public boolean pushPattern(@NotNull IPatternDetails patternDetails, KeyCounter @NotNull [] inputHolder) {
        return false;
    }

    @Override
    public @NotNull Widget createUIWidget() {
        return MEInputBufferPartMachineUIKt.createUIWidgetFor(this);
    }

    @Override
    public void buildToolBoxContent(@NotNull VBoxBuilder $this$buildToolBoxContent) {
        MEInputBufferPartMachineUIKt.buildToolBoxContentFor($this$buildToolBoxContent, this);
    }

    @Override
    public boolean isBusy() {
        return true;
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        for (InternalSlot slot : getInternalInventory()) {
            slot.refund();
            for (var job : slot.craftingTracker.getRequestedJobs()) {
                job.cancel();
            }
        }
    }

    @Override
    public @Nullable IPatternDetails decodePattern(ItemStack stack, int index) {
        var pattern = super.decodePattern(stack, index);
        if (pattern == null) return null;
        MEPatternVirtualInputHelper.readRecipeTag(stack, getInternalInventory()[index]::setRecipe);
        return pattern;
    }

    @Override
    public @NotNull IPatternDetails convertPattern(@NotNull IPatternDetails pattern, int index) {
        var slot = getInternalInventory()[index];
        return MEPatternVirtualInputHelper.convertPattern(pattern, this::getGrid, this::getActionSource,
                slot.circuitInventory, slot.shareInventory.storage, () -> true);
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        return slot2WatcherMap.entrySet().stream()
                .filter(e -> e.getKey().isEmitterMode)
                .filter(e -> e.getValue() != null)
                .map(Map.Entry::getValue)
                .collect(ObjectOpenHashSet::new, Set::add, Set::addAll);
    }

    @Override
    public void onPatternChange(int index) {
        super.onPatternChange(index);
    }

    private void configureWatchers() {
        if (this.craftingWatcher != null) {
            this.craftingWatcher.reset();
        }

        ICraftingProvider.requestUpdate(getMainNode());

        collectWatcherValues();

        updateState();
        onChanged();
    }

    private void updateState() {
        if (getController() instanceof WorkableMultiblockMachine w) {
            w.recipeLogic.updateTickSubscription();
        }
    }

    private void collectWatcherValues() {
        var slots = getInternalInventory();
        slot2WatcherMap.clear();
        watcher2SlotMap.clear();
        for (InternalSlot slot : slots) {
            if (slot == null || slot.reportingKey == null) continue;
            if (slot.isEmitterMode && craftingWatcher != null) {
                craftingWatcher.add(slot.reportingKey);
            }
            slot2WatcherMap.put(slot, slot.reportingKey);
            watcher2SlotMap.put(slot.reportingKey, slot);
        }
    }

    @Override
    public boolean patternFilter(@NotNull ItemStack stack) {
        return stack.getItem() instanceof ProcessingPatternItem &&
                MEPatternPartMachineKtKt.checkDuplicatedPattern(this, stack);
    }

    @Override
    public @NotNull IntSupplier getApplyIndex() {
        return configuratorField::get;
    }

    @Override
    public void runOnUpdate() {
        if (isRemote()) {
            configuratorField.set(-1);
            configuratorField.markAsChanged();
            syncToServer();
        }
    }

    @Override
    public @NotNull AEPatternViewSlotWidgetKt createPatternSlotWidget(int index) {
        return new AEPatternViewSlotWidgetKt(
                0,
                0,
                index,
                getApplyIndex(),
                getPatternInventory(),
                () -> onMouseClicked(-1),
                () -> onMouseClicked(index)) {

            @Override
            public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

                if (getInner().getItem().isEmpty()) {
                    return;
                }
                var state = disconnectStates[index];
                var text = state ? Component.translatable(STOPPED) : Component.translatable(RESTOCKING);
                StackSizeRenderer.renderSizeLabel(
                        graphics, Minecraft.getInstance().font,
                        getPositionX() + 1,
                        getPositionY() + 17 - Minecraft.getInstance().font.lineHeight * 0.5f,
                        text, 0.5f, true, true

                );
            }
        };
    }

    public static final class InternalSlot extends MEPatternBufferPartMachine.InternalSlot implements ICraftingRequester {

        public final MEInputBufferPartMachine inputBuffer;
        public final AEKeyMap<AEItemKey> itemRequest = new AEKeyMap<>();
        public final AEKeyMap<AEFluidKey> fluidRequest = new AEKeyMap<>();

        public AEKey reportingKey = null;
        @Getter
        @Setter
        public long minThreshold = -1;
        @Setter
        public long multiplier = 1;
        private boolean isEmitterMode = false;
        public boolean useRequest = false;
        /// used to prevent frequent disconnect and reconnect when the pattern is being crafted and the output
        /// fluctuates around the threshold
        private boolean disconnected = false;

        private MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, 0);
        private int craftingSlotCount;
        private long requestVersion;

        private InternalSlot(MEInputBufferPartMachine machine, int index) {
            super(machine, index);
            this.inputBuffer = machine;
        }

        @Override
        public void onPatternChange() {
            refund();
            setRecipe(null);
            for (var job : craftingTracker.getRequestedJobs()) {
                job.cancel();
            }
            craftingSlotCount = -1;
            reloadConfig();
        }

        @Override
        public void setRecipe(@Nullable GTRecipeDefinition recipe) {
            this.recipe = recipe;
        }

        public boolean isEmitterMode() {
            if (reportingKey == null) return false;
            return isEmitterMode;
        }

        public void setEmitterMode(boolean emitterMode) {
            isEmitterMode = emitterMode;
            ICraftingProvider.requestUpdate(inputBuffer.getMainNode());
        }

        public void reloadConfig() {
            final var oldWatcher = reportingKey;
            if (oldWatcher != null) {
                inputBuffer.watcher2SlotMap.remove(oldWatcher, this);
                inputBuffer.slot2WatcherMap.remove(this);
            }
            clearConfig();
            var newPattern = inputBuffer.getInternalPatternInventory().getStackInSlot(index);
            var details = inputBuffer.decodePattern(newPattern, index);
            if (details == null) {
                reportingKey = null;
                resizeCraftingTracker();
                return;
            }
            if (details instanceof AEProcessingPattern aeProcessingPattern) {
                reportingKey = aeProcessingPattern.getPrimaryOutput().what();
                inputBuffer.watcher2SlotMap.put(reportingKey, this);
                inputBuffer.slot2WatcherMap.put(this, reportingKey);

                if (newPattern.getOrCreateTag().tags.get("recipe") instanceof StringTag stringTag) {
                    var recipe = RecipeBuilder.get(RLUtils.parse(stringTag.getAsString()));
                    setRecipe(recipe);
                }

                var inputs = aeProcessingPattern.getSparseInputs();
                int itemInputCount = 0;
                int fluidInputCount = 0;
                for (var ingredient : inputs) {
                    if (ingredient.what() instanceof AEItemKey) {
                        itemInputCount++;
                    } else if (ingredient.what() instanceof AEFluidKey) {
                        fluidInputCount++;
                    }
                }
                itemRequest.ensureCapacity(itemInputCount);
                fluidRequest.ensureCapacity(fluidInputCount);
                for (var ingredient : inputs) {
                    var key = ingredient.what();
                    var amount = ingredient.amount() * multiplier;
                    if (key instanceof AEItemKey itemKey) {
                        itemRequest.insert(itemKey, amount);
                    } else if (key instanceof AEFluidKey fluidKey) {
                        fluidRequest.insert(fluidKey, amount);
                    }
                }
                itemInventory.ensureCapacity(itemRequest.size());
                fluidInventory.ensureCapacity(fluidRequest.size());
                resizeCraftingTracker();
            }
        }

        private void clearConfig() {
            itemRequest.clear();
            fluidRequest.clear();
            requestVersion++;
        }

        private void resizeCraftingTracker() {
            int size = itemRequest.size() + fluidRequest.size();
            if (size != craftingSlotCount) {
                craftingTracker = new MultiCraftingTracker(this, size);
                craftingSlotCount = size;
            }
        }

        public ExportOnlyAEItemList createItemRequestView() {
            int[] index = { 0 };
            return new ExportOnlyAEItemList(inputBuffer, 16, () -> new ItemRequestViewSlot(this, index[0]++)) {

                @Override
                public boolean isStocking() {
                    return true;
                }

                @Override
                public boolean isAutoPull() {
                    return true;
                }
            };
        }

        public ExportOnlyAEFluidList createFluidRequestView() {
            int[] index = { 0 };
            return new ExportOnlyAEFluidList(inputBuffer, 16, () -> new FluidRequestViewSlot(this, index[0]++)) {

                @Override
                public boolean isStocking() {
                    return true;
                }

                @Override
                public boolean isAutoPull() {
                    return true;
                }
            };
        }

        private static final class ItemRequestViewSlot extends ExportOnlyAEItemSlot {

            private final InternalSlot owner;
            private final int index;
            private GenericStack cachedConfig;
            private GenericStack cachedStock;
            private long cachedRequestVersion = Long.MIN_VALUE;
            private AEItemKey cachedKey;

            private ItemRequestViewSlot(InternalSlot owner, int index) {
                this.owner = owner;
                this.index = index;
            }

            @Override
            public @Nullable GenericStack getConfig() {
                AEItemKey key = getKey();
                long amount = key == null ? 0 : owner.itemRequest.getLong(key);
                if (key == null || amount <= 0) return cachedConfig = null;
                if (cachedConfig == null || cachedConfig.what() != key || cachedConfig.amount() != amount) {
                    cachedConfig = new GenericStack(key, amount);
                }
                return cachedConfig;
            }

            @Override
            public @Nullable GenericStack getStock() {
                AEItemKey key = getKey();
                long amount = key == null ? 0 : owner.itemInventory.getLong(key);
                if (key == null || amount <= 0) return cachedStock = null;
                if (cachedStock == null || cachedStock.what() != key || cachedStock.amount() != amount) {
                    cachedStock = new GenericStack(key, amount);
                }
                return cachedStock;
            }

            @Override
            public ExportOnlyAEItemSlot copy() {
                var copy = new ExportOnlyAEItemSlot();
                copy.setConfig(getConfig());
                copy.setStock(getStock());
                return copy;
            }

            private @Nullable AEItemKey getKey() {
                if (cachedRequestVersion == owner.requestVersion) return cachedKey;
                cachedRequestVersion = owner.requestVersion;
                int current = 0;
                for (var entry : owner.itemRequest) {
                    if (current++ == index) return cachedKey = entry.getKey();
                }
                return cachedKey = null;
            }
        }

        private static final class FluidRequestViewSlot extends ExportOnlyAEFluidSlot {

            private final InternalSlot owner;
            private final int index;
            private GenericStack cachedConfig;
            private GenericStack cachedStock;
            private long cachedRequestVersion = Long.MIN_VALUE;
            private AEFluidKey cachedKey;

            private FluidRequestViewSlot(InternalSlot owner, int index) {
                this.owner = owner;
                this.index = index;
            }

            @Override
            public @Nullable GenericStack getConfig() {
                AEFluidKey key = getKey();
                long amount = key == null ? 0 : owner.fluidRequest.getLong(key);
                if (key == null || amount <= 0) return cachedConfig = null;
                if (cachedConfig == null || cachedConfig.what() != key || cachedConfig.amount() != amount) {
                    cachedConfig = new GenericStack(key, amount);
                }
                return cachedConfig;
            }

            @Override
            public @Nullable GenericStack getStock() {
                AEFluidKey key = getKey();
                long amount = key == null ? 0 : owner.fluidInventory.getLong(key);
                if (key == null || amount <= 0) return cachedStock = null;
                if (cachedStock == null || cachedStock.what() != key || cachedStock.amount() != amount) {
                    cachedStock = new GenericStack(key, amount);
                }
                return cachedStock;
            }

            @Override
            public ExportOnlyAEFluidSlot copy() {
                var copy = new ExportOnlyAEFluidSlot();
                copy.setConfig(getConfig());
                copy.setStock(getStock());
                return copy;
            }

            private @Nullable AEFluidKey getKey() {
                if (cachedRequestVersion == owner.requestVersion) return cachedKey;
                cachedRequestVersion = owner.requestVersion;
                int current = 0;
                for (var entry : owner.fluidRequest) {
                    if (current++ == index) return cachedKey = entry.getKey();
                }
                return cachedKey = null;
            }
        }

        private boolean shouldSync(IGrid grid) {
            if (reportingKey == null) {
                return false;
            }
            if (isEmitterMode) {
                return grid.getCraftingService().isRequesting(reportingKey);
            }
            if (minThreshold < 0) {
                return true;
            }
            var last = grid.getStorageService().getCachedInventory().get(reportingKey);
            return last < minThreshold;
        }

        private void syncME(@NotNull IGrid grid) {
            if (!shouldSync(grid)) {
                if (disconnected) {
                    return;
                }
                disconnected = true;
                inputBuffer.disconnectStates[index] = true;
                clearConfig();
            } else {
                if (disconnected) {
                    reloadConfig();
                }
                disconnected = false;
                inputBuffer.disconnectStates[index] = false;
            }
            MEStorage networkInv = grid.getStorageService().getInventory();
            ICraftingService craftingService = grid.getCraftingService();
            int craftingSlot = syncInventory(itemRequest, itemInventory, networkInv, craftingService, 0);
            syncInventory(fluidRequest, fluidInventory, networkInv, craftingService, craftingSlot);
        }

        private <K extends AEKey> int syncInventory(AEKeyMap<K> requests, AEKeyMap<K> inventory,
                                                    MEStorage networkInv, ICraftingService craftingService,
                                                    int craftingSlot) {
            boolean changed = false;
            for (var it = inventory.iterator(); it.hasNext();) {
                var entry = it.next();
                long stored = entry.getLongValue();
                long requested = requests.getLong(entry.getKey());
                long excess = stored - requested;
                if (excess <= 0) continue;
                long inserted = networkInv.insert(entry.getKey(), excess, Actionable.MODULATE,
                        inputBuffer.getActionSourceField());
                long remaining = stored - (inserted > 0 ? inserted : excess);
                if (remaining > 0) {
                    entry.setValue(remaining);
                } else {
                    it.remove();
                }
                changed = true;
            }
            for (var it = requests.iterator(); it.hasNext(); craftingSlot++) {
                var entry = it.next();
                long missing = entry.getLongValue() - inventory.getLong(entry.getKey());
                if (missing <= 0) continue;
                long extracted = networkInv.extract(entry.getKey(), missing, Actionable.MODULATE,
                        inputBuffer.getActionSourceField());
                if (useRequest && extracted < missing) {
                    craftingTracker.handleCrafting(craftingSlot, entry.getKey(), missing - extracted,
                            inputBuffer.getLevel(), craftingService, inputBuffer.getActionSourceField());
                }
                if (extracted > 0) {
                    inventory.insert(entry.getKey(), extracted);
                    changed = true;
                }
            }
            if (changed) {
                markContentsChanged();
            }
            return craftingSlot;
        }

        @Override
        public @NotNull CompoundTag serializeNBT() {
            CompoundTag tag = super.serializeNBT();
            tag.putBoolean("emitterMode", isEmitterMode);
            tag.putBoolean("useRequest", useRequest);
            tag.putLong("minThreshold", minThreshold);
            tag.putLong("multiplier", multiplier);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            super.deserializeNBT(tag);
            migrateLegacyInventory(tag.getList("exI", Tag.TAG_COMPOUND), itemInventory, AEItemKey.class);
            migrateLegacyInventory(tag.getList("exF", Tag.TAG_COMPOUND), fluidInventory, AEFluidKey.class);
            if (tag.tags.get("emitterMode") instanceof ByteTag emitterMode) {
                isEmitterMode = emitterMode.getAsByte() != 0;
            }
            if (tag.tags.get("useRequest") instanceof ByteTag useReq) {
                this.useRequest = useReq.getAsByte() != 0;
            }
            if (tag.tags.get("minThreshold") instanceof LongTag minThres) {
                this.minThreshold = minThres.getAsLong();
            }
            if (tag.tags.get("multiplier") instanceof LongTag mul) {
                this.multiplier = mul.getAsLong();
            }
        }

        private static <K extends AEKey> void migrateLegacyInventory(ListTag legacySlots, AEKeyMap<K> inventory,
                                                                     Class<K> keyType) {
            if (!inventory.isEmpty()) return;
            for (Tag legacySlot : legacySlots) {
                if (!(legacySlot instanceof CompoundTag slotTag)) continue;
                GenericStack stock = GenericStack.readTag(slotTag.getCompound("stock"));
                if (stock == null || !keyType.isInstance(stock.what()) || stock.amount() <= 0) continue;
                inventory.insert(keyType.cast(stock.what()), stock.amount());
            }
        }

        @Override
        public boolean pushPattern(@NotNull IPatternDetails patternDetails, @NotNull KeyCounter @NotNull [] inputHolder) {
            return false;
        }

        @Override
        public ImmutableSet<ICraftingLink> getRequestedJobs() {
            return craftingTracker.getRequestedJobs();
        }

        @Override
        public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
            if (inputBuffer.getMainNode().getGrid() == null) return 0;
            if (what instanceof AEItemKey itemKey) {
                return insertCrafted(itemKey, amount, mode, itemRequest, itemInventory);
            }
            if (what instanceof AEFluidKey fluidKey) {
                return insertCrafted(fluidKey, amount, mode, fluidRequest, fluidInventory);
            }
            return 0;
        }

        private <K extends AEKey> long insertCrafted(K what, long amount, Actionable mode,
                                                     AEKeyMap<K> requests, AEKeyMap<K> inventory) {
            long remainingRequest = requests.getLong(what) - inventory.getLong(what);
            long inserted = Math.min(amount, remainingRequest);
            if (inserted <= 0) return 0;
            if (mode == Actionable.MODULATE) {
                inventory.insert(what, inserted);
                markContentsChanged();
            }
            return inserted;
        }

        @Override
        public void jobStateChange(ICraftingLink link) {
            craftingTracker.jobStateChange(link);
            inputBuffer.updateSubscription();
        }

        @Override
        public @Nullable IGridNode getActionableNode() {
            return inputBuffer.getActionableNode();
        }
    }

    private static final class SlotRHL extends InternalSlotRecipeHandler.AbstractRHL<InternalSlot> {

        SlotRHL(InternalSlot slot, MEInputBufferPartMachine part) {
            super(slot, part, slot.shareInventory, slot.shareTank, slot.circuitInventory,
                    new InternalSlotRecipeHandler.SlotRecipeHandler(part, slot));
        }

        private SlotRHL(InternalSlot slot, IRecipeHandler... handlers) {
            super(slot, null, handlers);
        }

        @Override
        protected @Nullable GTRecipeDefinition getCachedRecipe() {
            return slot.recipe;
        }

        @Override
        protected void clearCachedRecipe() {
            slot.setRecipe(null);
        }

        @Override
        protected @Nullable GTRecipeType getEffectiveRecipeType(GTRecipeType recipeType) {
            final var r = slot.recipe;
            if (r != null && r.recipeType != null && r.recipeType != recipeType) {
                return r.recipeType;
            }
            return recipeType;
        }

        @Override
        protected void onRecipeHandled(GTRecipe recipe) {
            slot.setRecipe(recipe.definition);
        }

        @Override
        public RecipeHandlerUnit wrapper(Collection<IRecipeHandler> handlers) {
            return new SlotRHL(slot, handlers.toArray(new IRecipeHandler[0]));
        }

        @Override
        public boolean findRecipe(GTRecipeType recipeType, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle) {
            if (slot.isEmpty()) return false;
            var cachedRecipe = getCachedRecipe();
            if (cachedRecipe != null) {
                if (canHandle.test(this, cachedRecipe)) {
                    return true;
                }
            }
            recipeType = getEffectiveRecipeType(recipeType);
            if (recipeType == null) return false;
            var map = this.getSearchMap(recipeType);
            if (map.isEmpty()) return false;
            return recipeType.search(this, map, canHandle);
        }
    }

    @RegisterLanguage(cn = "补货中", en = "Restocking")
    public static final String RESTOCKING = "gtocore.machine.me_input_buffer.restocking";
    @RegisterLanguage(cn = "已停止", en = "Stopped")
    public static final String STOPPED = "gtocore.machine.me_input_buffer.stopped";
}

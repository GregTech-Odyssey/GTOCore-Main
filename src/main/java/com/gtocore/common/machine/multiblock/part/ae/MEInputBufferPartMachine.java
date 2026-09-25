package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidList;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEFluidSlot;
import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAEItemList;
import com.gtocore.common.machine.multiblock.part.ae.widget.MEInputBufferPartMachineUI;
import com.gtocore.common.machine.multiblock.part.ae.widget.slot.MEPatternViewSlotWidget;
import com.gtocore.common.machine.trait.InternalSlotRecipeHandler;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.trait.NotifiableNotConsumableFluidHandler;
import com.gtolib.api.machine.trait.NotifiableNotConsumableItemHandler;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.CircuitHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.transfer.fluid.LockableIFluidHandler;
import com.gregtechceu.gtceu.api.transfer.item.LockableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.uipro.UIElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
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
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.MultiCraftingTracker;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.gto.datasynclib.FieldDataManager;
import com.gto.datasynclib.IFieldDataHolder;
import com.gto.datasynclib.LazyFieldDataManager;
import com.gto.datasynclib.LogicalSide;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.util.DataCodecs;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;

@DataGeneratorScanned
public class MEInputBufferPartMachine extends MEPatternPartMachine<MEInputBufferPartMachine.InternalSlot> {

    private IStackWatcher craftingWatcher;

    private final List<RecipeHandlerUnit> recipeHandlers;

    @SyncToClient
    final boolean[] disconnectStates = new boolean[getMaxPatternCount()];

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
    protected boolean supportsSlotConfig() {
        return true;
    }

    @Override
    protected void buildSlotConfig(@NotNull UIElement column, int index) {
        MEInputBufferPartMachineUI.buildSlotConfig(column, this, index);
    }

    @Override
    public boolean isBusy() {
        return true;
    }

    @Override
    public void onMachineRemoved() {
        super.onMachineRemoved();
        for (InternalSlot slot : getInternalInventory()) {
            slot.clearVirtualInputs();
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
                slot.circuitInventory, slot.notConsumableItem.storage, slot.notConsumableFluid.getStorages(), slot.virtualInputState,
                () -> true);
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
        if (!isRemote()) {
            getInternalInventory()[index].clearVirtualInputs();
        }
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
                checkDuplicatedPattern(this, stack);
    }

    @Override
    public @NotNull MEPatternViewSlotWidget createPatternSlotWidget(int index) {
        return new MEPatternViewSlotWidget(index, getPatternInventory(), SLOT_CONFIG_POPUP) {

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

    public static final class InternalSlot extends AbstractRecipeInternalSlot implements ICraftingRequester, IFieldDataHolder {

        /// 每个样板的物品、流体配置各几格（界面每行 9 格排两行）。
        /// 旧存档是 16 格：存档按下标读入、多出的格子忽略/留空（DataSyncLib 数组与旧 NBT 路径都取两者较短的长度），
        /// 所以旧的 16 格库存原样落在前 16 格，无需额外迁移；配置每次由样板重新生成
        public static final int CONFIG_SLOTS = 18;

        public final MEInputBufferPartMachine machine;
        public final int index;

        @SaveToDisk
        public final NotifiableNotConsumableItemHandler notConsumableItem;
        @SaveToDisk
        public final NotifiableNotConsumableFluidHandler notConsumableFluid;
        @SaveToDisk
        public final ExportOnlyAEItemList exportOnlyItemList;
        @SaveToDisk
        public final ExportOnlyAEFluidList exportOnlyFluidList;
        @SaveToDisk
        public final NotifiableItemStackHandler circuitInventory;

        @Getter
        public final LockableItemStackHandler[] itemUiHandlers;

        public AEKey reportingKey = null;
        @Getter
        @SaveToDisk(defaultValue = "-1")
        public long minThreshold = -1;
        @SaveToDisk(defaultValue = "1")
        public long multiplier = 1;
        @SaveToDisk(defaultValue = "false")
        private boolean isEmitterMode = false;
        @SaveToDisk(defaultValue = "false")
        public boolean useRequest = false;
        @Setter
        @SaveToDisk
        public GTRecipeDefinition recipe;

        /// used to prevent frequent disconnect and reconnect when the pattern is being crafted and the output
        /// fluctuates around the threshold
        private boolean disconnected = false;

        MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, CONFIG_SLOTS * 2);

        private final MEVirtualInputState virtualInputState;
        private final LazyFieldDataManager fieldDataManager = new LazyFieldDataManager(this);

        private InternalSlot(MEInputBufferPartMachine machine, int index) {
            this.machine = machine;
            this.index = index;
            this.notConsumableItem = createShareInventory();
            this.notConsumableFluid = new NotifiableNotConsumableFluidHandler(machine, 9, 64000);

            this.exportOnlyItemList = new ExportOnlyAEItemList(machine, CONFIG_SLOTS) {

                @Override
                public boolean isStocking() {
                    return true;
                }

                @Override
                public boolean isAutoPull() {
                    return true;
                }
            };
            this.exportOnlyFluidList = new ExportOnlyAEFluidList(machine, CONFIG_SLOTS) {

                @Override
                public boolean isStocking() {
                    return true;
                }

                @Override
                public boolean isAutoPull() {
                    return true;
                }
            };

            this.circuitInventory = CircuitHandler.create(machine);
            this.virtualInputState = new MEVirtualInputState(notConsumableItem.storage, notConsumableFluid.getStorages(), circuitInventory.storage);
            this.itemUiHandlers = virtualInputState.getItemUiHandlers();
        }

        private NotifiableNotConsumableItemHandler createShareInventory() {
            var h = new NotifiableNotConsumableItemHandler(machine, 9, IO.NONE);
            h.setFilter(stack -> !(stack.getItem() instanceof EncodedPatternItem));
            return h;
        }

        void clearVirtualInputs() {
            virtualInputState.clearVirtualInputs();
        }

        public LockableIFluidHandler[] getFluidUiHandlers() {
            return virtualInputState.getFluidUiHandlers();
        }

        public void setCircuitConfiguration(ItemStack circuit) {
            if (!virtualInputState.isVirtualCircuit()) {
                virtualInputState.setManualCircuit(circuit);
            }
        }

        public boolean isEmpty() {
            return exportOnlyItemList.isEmpty() && exportOnlyFluidList.isEmpty();
        }

        private void refund() {
            var network = machine.getMainNode().getGrid();
            if (network != null) {
                MEStorage networkInv = network.getStorageService().getInventory();
                for (var aeSlot : exportOnlyItemList.getInventory()) {
                    GenericStack stock = aeSlot.getStock();
                    if (stock != null) {
                        long inserted = networkInv.insert(stock.what(), stock.amount(), Actionable.MODULATE,
                                machine.getActionSourceField());
                        if (inserted > 0) {
                            aeSlot.extract(inserted, false, true);
                        }
                    }
                }
                for (var aeTank : exportOnlyFluidList.getInventory()) {
                    GenericStack stock = aeTank.getStock();
                    if (stock != null) {
                        long inserted = networkInv.insert(stock.what(), stock.amount(), Actionable.MODULATE,
                                machine.getActionSourceField());
                        if (inserted > 0) {
                            aeTank.extract(inserted, false, true);
                        }
                    }
                }
                markContentsChanged();
            }
        }

        @Override
        public void onPatternChange() {
            refund();
            setRecipe(null);
            for (var job : craftingTracker.getRequestedJobs()) {
                job.cancel();
            }
            reloadConfig();
        }

        public boolean isEmitterMode() {
            if (reportingKey == null) return false;
            return isEmitterMode;
        }

        // 以下设置项只有 @SaveToDisk（没有 @SyncToClient，不会被自动标脏），改动后必须 machine.onChanged() 才会存盘

        /// 发信合成监听的是样板主产物的下单请求：槽里没有处理样板时无从监听，不能开启
        public boolean canUseEmitterMode() {
            return reportingKey != null;
        }

        public void setEmitterMode(boolean emitterMode) {
            // 没有样板时拒绝开启（否则开关看起来没反应，却把隐藏的状态存成开启，放入样板后突然生效）
            if (emitterMode && !canUseEmitterMode()) return;
            if (isEmitterMode == emitterMode) return;
            isEmitterMode = emitterMode;
            machine.onChanged();
            ICraftingProvider.requestUpdate(machine.getMainNode());
        }

        public void setMinThreshold(long minThreshold) {
            if (this.minThreshold == minThreshold) return;
            this.minThreshold = minThreshold;
            machine.onChanged();
        }

        public void setMultiplier(long multiplier) {
            if (this.multiplier == multiplier) return;
            this.multiplier = multiplier;
            machine.onChanged();
            reloadConfig();
        }

        public void setUseRequest(boolean useRequest) {
            if (this.useRequest == useRequest) return;
            this.useRequest = useRequest;
            machine.onChanged();
        }

        public void reloadConfig() {
            final var oldWatcher = reportingKey;
            if (oldWatcher != null) {
                machine.watcher2SlotMap.remove(oldWatcher, this);
                machine.slot2WatcherMap.remove(this);
            }
            var newPattern = machine.getInternalPatternInventory().getStackInSlot(index);
            var details = machine.decodePattern(newPattern, index);
            if (details == null) {
                reportingKey = null;
                for (var slot : exportOnlyItemList.getInventory()) {
                    slot.setConfig(null);
                }
                for (var slot : exportOnlyFluidList.getInventory()) {
                    slot.setConfig(null);
                }
                return;
            }
            if (details instanceof AEProcessingPattern aeProcessingPattern) {
                reportingKey = aeProcessingPattern.getPrimaryOutput().what();
                machine.watcher2SlotMap.put(reportingKey, this);
                machine.slot2WatcherMap.put(this, reportingKey);

                if (newPattern.getOrCreateTag().tags.get("recipe") instanceof StringTag stringTag) {
                    var recipe = RecipeBuilder.get(RLUtils.parse(stringTag.getAsString()));
                    setRecipe(recipe);
                }

                int itemIdx = 0, fluidIdx = 0;
                for (var ingredient : aeProcessingPattern.getSparseInputs()) {
                    var key = ingredient.what();
                    if (key instanceof AEItemKey itemKey && MEPatternVirtualInputHelper.isVirtualProvider(itemKey)) continue;
                    var amount = ingredient.amount();
                    var configStack = new GenericStack(key, amount * multiplier);
                    if (key instanceof AEItemKey) {
                        if (itemIdx >= exportOnlyItemList.getInventory().length) continue;
                        exportOnlyItemList.getInventory()[itemIdx++].setConfig(configStack);
                    } else if (key instanceof AEFluidKey) {
                        if (fluidIdx >= exportOnlyFluidList.getInventory().length) continue;
                        exportOnlyFluidList.getInventory()[fluidIdx++].setConfig(configStack);
                    }
                }
            }
        }

        private void clearConfig() {
            for (var slot : exportOnlyItemList.getInventory()) {
                slot.setConfig(null);
            }
            for (var slot : exportOnlyFluidList.getInventory()) {
                slot.setConfig(null);
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
                machine.disconnectStates[index] = true;
                clearConfig();
            } else {
                if (disconnected) {
                    reloadConfig();
                }
                disconnected = false;
                machine.disconnectStates[index] = false;
            }
            var cg = grid.getCraftingService();
            MEStorage networkInv = grid.getStorageService().getInventory();
            for (int i = 0; i < exportOnlyItemList.getConfigurableSlots(); i++) {
                var aeSlot = exportOnlyItemList.getInventory()[i];
                GenericStack exceedItem = aeSlot.exceedStack();
                if (exceedItem != null) {
                    long total = exceedItem.amount();
                    long inserted = networkInv.insert(exceedItem.what(), exceedItem.amount(), Actionable.MODULATE, machine.getActionSourceField());
                    if (inserted > 0) {
                        aeSlot.extract(inserted, false, true);
                        continue;
                    } else {
                        aeSlot.extract(total, false, true);
                    }
                }
                GenericStack reqItem = aeSlot.requestStack();
                if (reqItem != null) {
                    long extracted = networkInv.extract(reqItem.what(), reqItem.amount(), Actionable.MODULATE, machine.getActionSourceField());
                    if (useRequest && extracted < reqItem.amount()) {
                        craftingTracker.handleCrafting(i, reqItem.what(), reqItem.amount() - extracted,
                                machine.getLevel(), cg, machine.getActionSourceField());
                    }
                    if (extracted != 0) {
                        aeSlot.addStack(new GenericStack(reqItem.what(), extracted));
                    }
                }
            }
            for (int i = 0; i < exportOnlyFluidList.getTanks(); i++) {
                ExportOnlyAEFluidSlot aeTank = exportOnlyFluidList.getInventory()[i];
                GenericStack exceedFluid = aeTank.exceedStack();
                if (exceedFluid != null) {
                    long total = exceedFluid.amount();
                    long inserted = networkInv.insert(exceedFluid.what(), exceedFluid.amount(), Actionable.MODULATE, machine.getActionSourceField());
                    if (inserted > 0) {
                        aeTank.extract(inserted, false, true);
                        continue;
                    } else {
                        aeTank.extract(total, false, true);
                    }
                }
                GenericStack reqFluid = aeTank.requestStack();
                if (reqFluid != null) {
                    long extracted = networkInv.extract(reqFluid.what(), reqFluid.amount(), Actionable.MODULATE, machine.getActionSourceField());
                    if (useRequest && extracted < reqFluid.amount()) {
                        craftingTracker.handleCrafting(i + exportOnlyItemList.getConfigurableSlots(), reqFluid.what(), reqFluid.amount() - extracted,
                                machine.getLevel(), cg, machine.getActionSourceField());
                    }
                    if (extracted > 0) {
                        aeTank.addStack(new GenericStack(reqFluid.what(), extracted));
                    }
                }
            }
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            virtualInputState.resetForDeserialize();
            if (tag.get("recipe") instanceof ByteArrayTag byteArrayTag) setRecipe(GTRecipeDefinition.DATA_CODEC.decode(Data.readData(byteArrayTag.getAsByteArray())));
            notConsumableItem.storage.deserializeNBT(tag.tags.get("inv"));
            if (tag.tags.get("tank") instanceof ListTag tanks) {
                for (int i = 0; i < tanks.size(); i++) {
                    var t = tanks.getCompound(i);
                    if (t.isEmpty()) continue;
                    var tank = notConsumableFluid.getStorages()[i];
                    tank.deserializeNBT(t);
                }
            }
            if (tag.tags.get("exI") instanceof ListTag exportItems) {
                var slots = exportOnlyItemList.getInventory();
                for (int i = 0; i < exportItems.size() && i < slots.length; i++) {
                    var t = exportItems.getCompound(i);
                    slots[i].deserializeNBT(t);
                }
            }
            if (tag.tags.get("exF") instanceof ListTag exportFluids) {
                var slots = exportOnlyFluidList.getInventory();
                for (int i = 0; i < exportFluids.size() && i < slots.length; i++) {
                    var t = exportFluids.getCompound(i);
                    slots[i].deserializeNBT(t);
                }
            }
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
            var c = tag.getInt("c");
            if (c > 0) circuitInventory.storage.setStackInSlot(0, IntCircuitBehaviour.stack(c));
        }

        @Override
        public void writeBuffer(LogicalSide logicalSide, FriendlyByteBuf friendlyByteBuf) {
            // 无同步，不实现
        }

        @Override
        public void readBuffer(LogicalSide logicalSide, FriendlyByteBuf friendlyByteBuf) {
            // 无同步，不实现
        }

        @Override
        public Data writeData() {
            return fieldDataManager.get().writeToData();
        }

        @Override
        public void readData(Data data, int dataVersion) {
            if (data.isNull()) return;
            if (dataVersion < 2) {
                var nbt = DataCodecs.TAG_CODEC.decode(data, dataVersion);
                if (nbt instanceof CompoundTag compoundTag) {
                    deserializeNBT(compoundTag);
                    return;
                }
            }
            fieldDataManager.get().readFromData(data, dataVersion);
        }

        @Override
        public FieldDataManager getFieldDataManager() {
            return fieldDataManager.get();
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
            return 0;
        }

        @Override
        public void jobStateChange(ICraftingLink link) {
            craftingTracker.jobStateChange(link);
            machine.updateSubscription();
        }

        @Override
        public @Nullable IGridNode getActionableNode() {
            return machine.getActionableNode();
        }
    }

    private static final class SlotRHL extends InternalSlotRecipeHandler.AbstractRHL<InternalSlot> {

        SlotRHL(InternalSlot slot, MEInputBufferPartMachine part) {
            super(slot, part, slot.notConsumableItem, slot.notConsumableFluid, slot.circuitInventory, slot.exportOnlyItemList, slot.exportOnlyFluidList);
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

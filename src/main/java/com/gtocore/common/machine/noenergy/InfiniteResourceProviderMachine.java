package com.gtocore.common.machine.noenergy;

import com.gtolib.api.ae2.infinite.InfiniteResourceRegistry;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.MEStorageProvider;
import com.gregtechceu.gtceu.api.transfer.key.MEStorageProviderIndex;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.FluidSlot;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.elements.ae.AEKeySlot;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeySet;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.blockentity.SaveToItem;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.function.IntFunction;

/**
 * 无限资源供应器：把已解锁的资源挂到 ME 网络当存储供应，取多少给多少、永不耗尽。
 * <p>
 * 默认供应的资源开箱即用；标签注册的整组资源把其中任意一份放进样机槽解锁，只解锁放进去的那一项，
 * 拿走样机就收起供应。已解锁的资源可以在界面里逐个关掉。解锁状态就是样机槽本身，不另记一份。
 */
@DataGeneratorScanned
public final class InfiniteResourceProviderMachine extends MetaMachine implements IGridConnectedMachine, MEStorage, IStorageProvider, MEStorageProvider, IFancyUIMachine {

    private static final long SUPPLY_AMOUNT = IParallelMachine.MAX_PARALLEL;
    private static final int SAMPLE_SLOTS = UISizes.SLOTS_PER_ROW << 1;
    private static final int SAMPLE_CAPACITY = 1000;
    private static final String PAGE_SCROLLER = "infinite_resource_provider.page";

    private static final int PRIORITY_MIN = Integer.MIN_VALUE + 1;
    private static final int PRIORITY_MAX = Integer.MAX_VALUE - 1;

    private static final Component PRIORITY = Component.translatable("gui.ae2.Priority");
    private static final Component PRIORITY_EXTRACTION_HINT = Component.translatable("gui.ae2.PriorityExtractionHint");
    private static final Component PRIORITY_INSERTION_HINT = Component.translatable("gui.ae2.PriorityInsertionHint");

    @RegisterLanguage(cn = "解锁资源", en = "Unlock Resources")
    private static final Component UNLOCK = Component.translatable("gtocore.machine.infinite_resource_provider.unlock");
    @RegisterLanguage(cn = "放入物品样机解锁对应资源，拿走样机即收起", en = "Insert an item sample to unlock that resource; remove it to stop")
    private static final Component ITEM_SAMPLE = Component.translatable("gtocore.machine.infinite_resource_provider.item_sample");
    @RegisterLanguage(cn = "放入流体样机解锁对应流体，抽走样机即收起", en = "Insert a fluid sample to unlock that fluid; drain it to stop")
    private static final Component FLUID_SAMPLE = Component.translatable("gtocore.machine.infinite_resource_provider.fluid_sample");
    @RegisterLanguage(cn = "无限资源", en = "Infinite Resources")
    private static final Component RESOURCES = Component.translatable("gtocore.machine.infinite_resource_provider.resources");
    @RegisterLanguage(cn = "放入对应样机后可以开关", en = "Insert the matching sample before toggling")
    private static final Component LOCKED = Component.translatable("gtocore.machine.infinite_resource_provider.locked");
    @RegisterLanguage(cn = "没有已注册的无限资源", en = "No infinite resource registered")
    private static final Component NO_RESOURCES = Component.translatable("gtocore.machine.infinite_resource_provider.empty");

    @SaveToDisk
    @SaveToItem
    private final AEKeySet<AEKey> disabled = new AEKeySet<>();

    @SaveToDisk(defaultValue = "2147483646")
    @SaveToItem
    private int priority = PRIORITY_MAX;
    @SaveToDisk
    @SaveToItem
    private final NotifiableInventory<AEItemKey> itemSample;
    @SaveToDisk
    @SaveToItem
    private final NotifiableInventory<AEFluidKey> fluidSample;
    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    @SyncToClient
    private boolean isOnline;
    private final MEStorage.AvailableStacksCache cache;
    private final AEKeySet<AEKey> samples = new AEKeySet<>();
    private final AEKeySet<AEKey> unlockedKeys = new AEKeySet<>();
    private final AEKeySet<AEKey> suppliedKeys = new AEKeySet<>();
    private final ObjectArrayList<AEKey> suppliedOrder = new ObjectArrayList<>();

    public InfiniteResourceProviderMachine(MetaMachineBlockEntity holder) {
        super(holder);
        this.cache = new MEStorage.AvailableStacksCache(this::addSupplied);
        this.cache.setTickUpdate(false);
        this.nodeHolder = new GridNodeHolder(this);
        this.itemSample = NotifiableInventory.items(this, SAMPLE_SLOTS, IO.NONE, IO.BOTH);
        this.fluidSample = NotifiableInventory.fluids(this, SAMPLE_SLOTS, SAMPLE_CAPACITY, IO.NONE, IO.BOTH);
        this.itemSample.addChangedListener(this::onSuppliedChanged);
        this.fluidSample.addChangedListener(this::onSuppliedChanged);
        getMainNode().addService(IStorageProvider.class, this);
    }

    @Override
    public MEStorage getStorageCap(net.minecraft.core.Direction side) {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        rebuildSupplied();
        MEStorageProviderIndex.add(getLevel(), this);
    }

    @Override
    public void onUnload() {
        MEStorageProviderIndex.remove(getLevel(), this);
        super.onUnload();
    }

    private void onSuppliedChanged() {
        if (isRemote()) return;
        rebuildSupplied();
        var grid = getMainNode().getGrid();
        if (grid != null) {
            IStorageService storage = grid.getStorageService();
            if (storage != null) storage.invalidateCache();
        }
        onChanged();
    }

    private void rebuildSupplied() {
        samples.clear();
        collect(itemSample.storage, samples);
        collect(fluidSample.storage, samples);
        unlockedKeys.clear();
        suppliedKeys.clear();
        suppliedOrder.clear();
        var keys = InfiniteResourceRegistry.keys();
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            if (!InfiniteResourceRegistry.isDefault(key) && !samples.containKey(key)) continue;
            unlockedKeys.add(key);
            if (disabled.containKey(key)) continue;
            suppliedKeys.add(key);
            suppliedOrder.add(key);
        }
        cache.markAsDirty();
    }

    private static void collect(KeyInventory<? extends AEKey> storage, AEKeySet<AEKey> out) {
        for (int i = 0; i < storage.size(); i++) {
            var key = storage.keyAt(i);
            if (key != null && storage.amountAt(i) > 0) out.add(key);
        }
    }

    private void setSupplied(AEKey key, boolean supplied) {
        if (isRemote() || !unlockedKeys.containKey(key)) return;
        if (supplied ? !disabled.remove(key) : !disabled.add(key)) return;
        onSuppliedChanged();
    }

    private int getPriority() {
        return priority;
    }

    /// 换优先级要重新挂一次存储
    private void setPriority(int value) {
        if (value == priority) return;
        priority = value;
        if (!isRemote()) IStorageProvider.requestUpdate(getMainNode());
        onChanged();
    }

    private void addSupplied(KeyCounter out) {
        for (int i = 0; i < suppliedOrder.size(); i++) {
            out.add(suppliedOrder.get(i), SUPPLY_AMOUNT);
        }
    }

    // ==================== ME 网络 ====================

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(this, priority);
    }

    @Override
    public Component getDescription() {
        return getDefinition().asItem().getDescription();
    }

    @Override
    public boolean supportsKeyType(AEKeyType<?> aeKeyType) {
        return aeKeyType == AEKeyTypes.ITEMS || aeKeyType == AEKeyTypes.FLUIDS;
    }

    @Override
    public int operationTime() {
        return 1;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return amount > 0 && suppliedKeys.containKey(what) ? amount : 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return amount > 0 && suppliedKeys.containKey(what) ? amount : 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        out.addAll(cache.getAvailableStacksCache());
    }

    @Override
    public KeyCounter getAvailableStacks() {
        return cache.getAvailableStacksCache();
    }

    @Override
    public Widget createUIWidget() {
        var page = Form.page();
        page.addChild(Form.numberRow(PRIORITY, NumberField.ofInt(LayoutStyle.AUTO, this::getPriority, this::setPriority, PRIORITY_MIN, PRIORITY_MAX),
                PRIORITY_EXTRACTION_HINT, PRIORITY_INSERTION_HINT));
        var unlock = Form.section(UNLOCK);
        unlock.addChild(Form.label(ITEM_SAMPLE));
        addSlots(unlock, SAMPLE_SLOTS, i -> ItemSlot.of(itemSample.storage, i));
        unlock.addChild(Form.label(FLUID_SAMPLE));
        var tanks = new ForgeFluidAdapter(fluidSample.storage);
        addSlots(unlock, SAMPLE_SLOTS, i -> FluidSlot.of(tanks, i, true, true));
        var resources = Form.section(RESOURCES);
        var keys = InfiniteResourceRegistry.keys();
        if (keys.isEmpty()) resources.addChild(Form.label(NO_RESOURCES));
        for (int i = 0; i < keys.size(); i++) {
            resources.addChild(resourceRow(keys.get(i)));
        }
        page.addChildren(unlock, resources);
        var scroller = ScrollerView.page(PAGE_SCROLLER, UISizes.CONTENT_WIDTH).adaptiveWidth();
        scroller.addScrollViewChild(page);
        return scroller;
    }

    private static void addSlots(UIElement section, int count, IntFunction<Widget> factory) {
        for (int start = 0; start < count; start += UISizes.SLOTS_PER_ROW) {
            var row = UIElement.row(UISizes.SLOT_SIZE);
            for (int i = start; i < Math.min(count, start + UISizes.SLOTS_PER_ROW); i++) {
                row.addChild(factory.apply(i));
            }
            section.addChild(row);
        }
    }

    private UIElement resourceRow(AEKey key) {
        var toggle = Switch.of(() -> suppliedKeys.containKey(key), supplied -> setSupplied(key, supplied));
        toggle.disabled(() -> !unlockedKeys.containKey(key), LOCKED);
        return UIElement.centeredRow(UISizes.SLOT_SIZE).addChildren(
                AEKeySlot.display(() -> key),
                Form.label(key::getDisplayName),
                toggle);
    }
}

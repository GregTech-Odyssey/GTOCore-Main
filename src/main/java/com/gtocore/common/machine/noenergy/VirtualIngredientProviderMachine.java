package com.gtocore.common.machine.noenergy;

import com.gtolib.api.ae2.storage.CellDataStorage;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.ButtonConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.FluidSlot;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.hepdd.gtmthings.common.item.VirtualFluidProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualProviderData;
import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.List;
import java.util.function.Supplier;

public final class VirtualIngredientProviderMachine extends MetaMachine implements IFancyUIMachine, IDropSaveMachine, MEStorage, IGridConnectedMachine, IStorageProvider {

    private static final String INVENTORY_TAG = "inventory";
    private static final String FLUID_INVENTORY_TAG = "fluid_inventory";
    private static final String SLOT_TAG = "slot";
    private static final String FLUID_TAG = "fluid";
    private static final int SLOT_COUNT = 288;
    private static final int FLUID_CAPACITY = 64000;
    private static final String CONFIGURED = "gtocore.machine.virtual_ingredient_provider.configured";
    private static final Item VIRTUAL_ITEM_PROVIDER = CustomItems.VIRTUAL_ITEM_PROVIDER.asItem();
    private static final Item VIRTUAL_FLUID_PROVIDER = CustomItems.VIRTUAL_FLUID_PROVIDER.asItem();
    private static final AEItemKey EMPTY_ITEM_PROVIDER = createEmptyProvider(VIRTUAL_ITEM_PROVIDER);
    private static final AEItemKey EMPTY_FLUID_PROVIDER = createEmptyProvider(VIRTUAL_FLUID_PROVIDER);

    private static AEItemKey createEmptyProvider(Item provider) {
        ItemStack stack = new ItemStack(provider);
        VirtualProviderData.setLocked(stack, true);
        return AEItemKey.of(stack);
    }

    private final CellDataStorage storage = new CellDataStorage();
    @SaveToDisk
    private final NotifiableItemStackHandler inventory;
    @SaveToDisk
    private final NotifiableFluidTank fluidInventory;
    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    @SyncToClient
    private boolean isOnline;
    private int configuredSlotCount;

    public VirtualIngredientProviderMachine(MetaMachineBlockEntity holder) {
        super(holder);
        this.inventory = new NotifiableItemStackHandler(this, SLOT_COUNT, IO.NONE, IO.BOTH);
        this.fluidInventory = new NotifiableFluidTank(this, SLOT_COUNT, FLUID_CAPACITY, IO.NONE, IO.BOTH);
        this.nodeHolder = new GridNodeHolder(this);
        getMainNode().addService(IStorageProvider.class, this);
        storage.setStoredMap(new AEKeyMap<>());
        inventory.addChangedListener(this::rebuildStorage);
        fluidInventory.addChangedListener(this::rebuildStorage);
    }

    private void rebuildStorage() {
        storage.cache.markAsDirty();
        storage.getStoredMap().clear();
        storage.getStoredMap().insert(EMPTY_ITEM_PROVIDER, IParallelMachine.MAX_PARALLEL << 6);
        storage.getStoredMap().insert(EMPTY_FLUID_PROVIDER, IParallelMachine.MAX_PARALLEL << 6);
        int configured = 0;
        for (int i = 0; i < inventory.storage.size; i++) {
            ItemStack stack = inventory.storage.stacks[i];
            if (stack.isEmpty()) continue;
            configured++;
            Item item = stack.getItem();
            if (item == VIRTUAL_ITEM_PROVIDER || item == VIRTUAL_FLUID_PROVIDER) {
                if (!hasValidContent(stack)) continue;
                int count = stack.getCount();
                stack = stack.copyWithCount(1);
                VirtualProviderData.setLocked(stack, true);
                storage.getStoredMap().insert(AEItemKey.of(stack), scaleSupply(count));
            } else {
                int count = stack.getCount();
                stack = VirtualItemProviderBehavior.setVirtualItem(new ItemStack(VIRTUAL_ITEM_PROVIDER), stack);
                stack = stack.copyWithCount(1);
                VirtualProviderData.setLocked(stack, true);
                storage.getStoredMap().insert(AEItemKey.of(stack), scaleSupply(count));
            }
        }
        for (var tank : fluidInventory.getStorages()) {
            FluidStack fluid = tank.getFluid();
            if (fluid.isEmpty()) continue;
            configured++;
            ItemStack provider = VirtualFluidProviderBehavior.setVirtualFluid(
                    new ItemStack(VIRTUAL_FLUID_PROVIDER), fluid);
            VirtualProviderData.setLocked(provider, true);
            storage.getStoredMap().insert(AEItemKey.of(provider), scaleSupply(fluid.getAmount()));
        }
        configuredSlotCount = configured;
    }

    private static long scaleSupply(int amount) {
        return amount > Long.MAX_VALUE / IParallelMachine.MAX_PARALLEL ? Long.MAX_VALUE :
                IParallelMachine.MAX_PARALLEL * amount;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        rebuildStorage();
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        if (widget instanceof MachineWindow window) window.setInventoryGutter(ScrollerView.SCROLL_BAR_SPACE);
        return createUIWidget();
    }

    @Override
    public Widget createUIWidget() {
        var tanks = fluidInventory.getStorages();
        var grid = UIElement.column(UISizes.SLOTS_PER_ROW * UISizes.SLOT);
        for (int start = 0; start < SLOT_COUNT; start += UISizes.SLOTS_PER_ROW) {
            var items = UIElement.row(UISizes.SLOT);
            var fluids = UIElement.row(UISizes.SLOT);
            for (int i = start; i < Math.min(SLOT_COUNT, start + UISizes.SLOTS_PER_ROW); i++) {
                items.addChild(ItemSlot.of(inventory.storage, i));
                fluids.addChild(new FluidSlot(tanks[i], 0, true, true));
            }
            grid.addChild(items);
            grid.addChild(fluids);
        }
        int height = HatchViews.MAX_GRID_ROWS * UISizes.SLOT;
        var scroller = new ScrollerView("virtual_ingredient_provider.slots", UISizes.CONTENT_WIDTH, height).adaptiveWidth().adaptiveHeight(height);
        scroller.addScrollViewChild(grid);
        var status = new StatusPanel();
        status.addLine(CONFIGURED, new ConfiguredText());
        return HatchViews.page(scroller, status);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new ButtonConfigurator(WidgetIcons.SORT, clickData -> {
            if (!clickData.isRemote) GTTransferUtils.sortInventory(inventory.storage);
        }).setTooltips(List.of(Component.translatable("gtceu.gui.inventory.sort"))));
    }

    void addMigratedProviders() {
        for (var provider : new ItemStack[] { new ItemStack(VIRTUAL_ITEM_PROVIDER), new ItemStack(VIRTUAL_FLUID_PROVIDER) }) {
            var remainder = ItemHandlerHelper.insertItem(inventory.storage, provider, false);
            if (!remainder.isEmpty()) Block.popResource(getLevel(), getPos(), remainder);
        }
    }

    private final class ConfiguredText implements Supplier<Component> {

        private int count = -1;
        private Component text = Component.empty();

        @Override
        public Component get() {
            if (configuredSlotCount != count) {
                count = configuredSlotCount;
                text = Component.literal(count + " / " + (SLOT_COUNT << 1));
            }
            return text;
        }
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (tag.get(INVENTORY_TAG) instanceof CompoundTag inventoryTag) {
            inventory.storage.deserializeNBT(inventoryTag);
        }
        ListTag fluids = tag.getList(FLUID_INVENTORY_TAG, Tag.TAG_COMPOUND);
        var tanks = fluidInventory.getStorages();
        for (int i = 0; i < fluids.size(); i++) {
            CompoundTag entry = fluids.getCompound(i);
            int slot = entry.getInt(SLOT_TAG);
            if (slot >= 0 && slot < tanks.length && entry.get(FLUID_TAG) instanceof CompoundTag fluidTag) {
                tanks[slot].deserializeNBT(fluidTag);
            }
        }
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        if (!inventory.isEmpty()) tag.put(INVENTORY_TAG, inventory.storage.serializeNBT());
        if (!fluidInventory.isEmpty()) {
            ListTag fluids = new ListTag();
            var tanks = fluidInventory.getStorages();
            for (int i = 0; i < tanks.length; i++) {
                if (tanks[i].isEmpty()) continue;
                CompoundTag entry = new CompoundTag();
                entry.putInt(SLOT_TAG, i);
                entry.put(FLUID_TAG, tanks[i].serializeNBT());
                fluids.add(entry);
            }
            tag.put(FLUID_INVENTORY_TAG, fluids);
        }
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(this, Integer.MAX_VALUE - 1);
    }

    @Override
    public Component getDescription() {
        return getDefinition().asItem().getDescription();
    }

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
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return what instanceof AEItemKey itemKey && isVirtualProvider(itemKey);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount > 0 && what instanceof AEItemKey itemKey && isVirtualProvider(itemKey)) {
            ItemStack stack = itemKey.getReadOnlyStack();
            if (!hasValidContent(stack)) return 0;
            if (VirtualProviderData.isLocked(stack)) return amount;
            int offeredAmount = (int) Math.min(amount, Integer.MAX_VALUE);
            ItemStack offered = stack.copyWithCount(offeredAmount);
            ItemStack remainder = ItemHandlerHelper.insertItem(inventory.storage, offered, mode.isSimulate());
            return offeredAmount - remainder.getCount();
        }
        return 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount > 0 && what instanceof AEItemKey itemKey && isVirtualProvider(itemKey) &&
                storage.getStoredMap().contains(itemKey)) {
            return amount;
        }
        return 0;
    }

    private static boolean isVirtualProvider(AEItemKey key) {
        Item item = key.getItem();
        return item == VIRTUAL_ITEM_PROVIDER || item == VIRTUAL_FLUID_PROVIDER;
    }

    private static boolean hasValidContent(ItemStack stack) {
        Item item = stack.getItem();
        if (item == VIRTUAL_ITEM_PROVIDER) {
            return !VirtualItemProviderBehavior.getVirtualItem(stack).isEmpty();
        } else if (item == VIRTUAL_FLUID_PROVIDER) {
            return !VirtualFluidProviderBehavior.getVirtualFluid(stack).isEmpty();
        }
        return false;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        out.addAll(storage.cache.getAvailableStacksCache());
    }

    @Override
    public KeyCounter getAvailableStacks() {
        return storage.cache.getAvailableStacksCache();
    }
}

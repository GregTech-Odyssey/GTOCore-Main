package com.gtocore.common.machine.noenergy;

import com.gtocore.common.data.machines.GTAEMachines;

import com.gtolib.GTOCore;
import com.gtolib.api.ae2.storage.CellDataStorage;
import com.gtolib.api.machine.feature.multiblock.IParallelMachine;
import com.gtolib.utils.SortUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
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
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.stream.Stream;

@Deprecated
public final class VirtualItemProviderMachine extends MetaMachine implements IUIMachine, IDropSaveMachine, MEStorage, IGridConnectedMachine, IStorageProvider {

    private static final Item VIRTUAL_ITEM_PROVIDER = CustomItems.VIRTUAL_ITEM_PROVIDER.asItem();
    static private final AEKey EMPTY_STACK;

    static {
        var es = VirtualItemProviderBehavior.setVirtualItem(new ItemStack(VIRTUAL_ITEM_PROVIDER.asItem()), ItemStack.EMPTY);
        es = es.copyWithCount(1);
        es.getOrCreateTag().putBoolean("marked", true);
        EMPTY_STACK = AEItemKey.of(es);
    }

    private final CellDataStorage storage = new CellDataStorage();
    @SaveToDisk
    private final NotifiableItemStackHandler inventory;
    @SaveToDisk
    private final GridNodeHolder nodeHolder;
    @SyncToClient
    private boolean isOnline;

    public VirtualItemProviderMachine(MetaMachineBlockEntity holder) {
        super(holder);
        this.inventory = new NotifiableItemStackHandler(this, 288, IO.NONE, IO.BOTH);
        this.nodeHolder = new GridNodeHolder(this);
        getMainNode().addService(IStorageProvider.class, this);
        storage.setStoredMap(new AEKeyMap<>());
        inventory.addChangedListener(() -> {
            storage.cache.markAsDirty();
            storage.getStoredMap().clear();
            storage.getStoredMap().insert(EMPTY_STACK, IParallelMachine.MAX_PARALLEL << 6);
            for (var i = 0; i < inventory.storage.size; i++) {
                var stack = inventory.storage.stacks[i];
                if (stack.isEmpty()) continue;
                if (stack.getItem() == VIRTUAL_ITEM_PROVIDER.asItem() && stack.hasTag()) {
                    stack = stack.copyWithCount(1);
                    stack.getOrCreateTag().putBoolean("marked", true);
                    storage.getStoredMap().insert(AEItemKey.of(stack), IParallelMachine.MAX_PARALLEL);
                } else {
                    int count = stack.getCount();
                    stack = VirtualItemProviderBehavior.setVirtualItem(new ItemStack(VIRTUAL_ITEM_PROVIDER.asItem()), stack);
                    stack = stack.copyWithCount(1);
                    stack.getOrCreateTag().putBoolean("marked", true);
                    storage.getStoredMap().insert(AEItemKey.of(stack), IParallelMachine.MAX_PARALLEL * count);
                }

            }
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        inventory.notifyListeners();
        if (getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.enqueueTask(serverLevel, this::migrateToIngredientProvider, 1);
        }
    }

    private void migrateToIngredientProvider() {
        if (isRemoved() || !(getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = getPos();
        if (!level.isLoaded(pos) || level.getBlockEntity(pos) != holder) return;
        BlockState oldState = getBlockState();
        BlockState newState = GTAEMachines.VIRTUAL_INGREDIENT_PROVIDER.defaultBlockState();
        for (Property<?> property : oldState.getProperties()) {
            if (newState.hasProperty(property)) newState = copyProperty(oldState, newState, property);
        }
        CompoundTag tag = holder.saveWithoutMetadata();
        if (!(newState.getBlock() instanceof EntityBlock entityBlock) || !(entityBlock.newBlockEntity(pos, newState) instanceof MetaMachineBlockEntity newHolder)) return;
        newHolder.load(tag);
        coverContainer.onUnload();
        for (Direction side : GTUtil.DIRECTIONS) {
            coverContainer.setCoverAtSideinternal(null, side);
        }
        level.setBlock(pos, newState, Block.UPDATE_ALL);
        level.setBlockEntity(newHolder);
        newHolder.setChanged();
        if (newHolder.metaMachine instanceof VirtualIngredientProviderMachine provider) provider.addMigratedProviders();
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        int xOffset = 162;
        int yOverflow = 9;
        var modularUI = new ModularUI(xOffset + 19, 244, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(5, 5, () -> Component.translatable(getBlockState().getBlock().getDescriptionId()).getString() +
                        "(" + Stream.of(inventory.storage.stacks).filter(i -> !i.isEmpty()).count() + "/" + 288 + ")"))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 162, true));

        var innerContainer = new DraggableScrollableWidgetGroup(4, 4, xOffset + 6, 130)
                .setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON).setYScrollBarWidth(4);

        modularUI.widget(new ButtonWidget(176 - 15, 3, 14, 14,
                new ResourceTexture(GTOCore.id("textures/gui/sort.png")),
                (press) -> SortUtils.sort()));
        int x = 0;
        int y = 0;
        for (int slot = 0; slot < 288; slot++) {
            innerContainer.addWidget(new SlotWidget(inventory.storage, slot, x * 18, y * 18) {

                @Override
                public boolean isEnabled() {
                    return true;
                }
            }.setBackgroundTexture(GuiTextures.SLOT));
            x++;
            if (x == yOverflow) {
                x = 0;
                y++;
            }
        }
        var container = new WidgetGroup(3, 17, xOffset + 20, 140).addWidget(innerContainer);
        return modularUI.widget(container);
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        inventory.storage.deserializeNBT(tag.get("inventory"));
        inventory.notifyListeners();
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.put("inventory", inventory.storage.serializeNBT());
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
        return what instanceof AEItemKey itemKey && itemKey.getItem() == VIRTUAL_ITEM_PROVIDER && itemKey.hasTag();
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount > 0 && what instanceof AEItemKey itemKey && itemKey.getItem() == VIRTUAL_ITEM_PROVIDER) {
            var stack = itemKey.getReadOnlyStack();
            var tag = stack.getTag();
            if (tag != null && tag.tags.containsKey("n")) {
                if (tag.getBoolean("marked")) return amount;
                if (ItemHandlerHelper.insertItem(inventory.storage, stack, mode.isSimulate()).getCount() < amount) {
                    return amount;
                }
            }
        }
        return 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount > 0 && what instanceof AEItemKey itemKey && itemKey.getItem() == VIRTUAL_ITEM_PROVIDER && storage.getStoredMap().contains(itemKey)) {
            return amount;
        }
        return 0;
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

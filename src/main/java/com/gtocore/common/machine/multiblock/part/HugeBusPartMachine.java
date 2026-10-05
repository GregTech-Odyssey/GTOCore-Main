package com.gtocore.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.KeyTransfer;
import com.gregtechceu.gtceu.common.data.GTTickTimeMonitors;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageAccess;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.data.StringMapData;
import com.gto.datasynclib.util.DataCodecs;
import com.gto.datasynclib.util.NbtUtil;
import com.hepdd.gtmthings.api.machine.fancyconfigurator.ButtonConfigurator;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class HugeBusPartMachine extends WorkableTieredIOPartMachine implements IDropSaveMachine {

    @SaveToDisk
    private final HugeInventory inventory;
    @Nullable
    private TickableSubscription autoIOSubs;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor autoIOMonitor = holder.monitorTick(GTTickTimeMonitors.AUTO_IO, this::autoIO);
    @Nullable
    private ISubscription inventorySubs;

    public HugeBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.IV, IO.IN);
        this.inventory = new HugeInventory(this);
        workingEnabled = false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            TaskHandler.enqueueTask(serverLevel, this::updateInventorySubscription);
        }
        inventorySubs = inventory.addChangedListener(this::updateInventorySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
    }

    private void refundAll(ClickData clickData) {
        if (clickData.isRemote) return;
        setWorkingEnabled(false);
        exportToNearby(inventory, getFrontFacing());
    }

    @Override
    public void onPaintingColorChanged(int color) {
        getHandlerUnit().setColor(color, true);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateInventorySubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateInventorySubscription();
    }

    @Override
    public boolean saveBreak() {
        return !inventory.storage.isEmpty();
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    @Override
    public void saveToItem(CompoundTag tag) {
        var key = inventory.storage.keyAt(0);
        tag.put("stored", (key == null ? ItemStack.EMPTY : key.toStack(1)).serializeNBT());
        tag.putLong("storedAmount", inventory.storage.amountAt(0));
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (!tag.contains("storedAmount")) return;
        inventory.storage.set(0, AEItemKey.of(ItemStack.of(tag.getCompound("stored"))), tag.getLong("storedAmount"));
    }

    private void updateInventorySubscription() {
        var level = getLevel();
        if (level != null && isWorkingEnabled() && holder.blockEntityDirectionCache.hasAdjacentTarget(getLevel(), getPos(), getFrontFacing(), AEKeyTypes.ITEMS, StorageAccess.EXTRACT)) {
            autoIOSubs = subscribeServerTick(autoIOSubs, autoIOMonitor, 40);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    private void autoIO() {
        if (isWorkingEnabled()) {
            inventory.importFromNearby(getFrontFacing());
        }
        updateInventorySubscription();
    }

    @SuppressWarnings("unchecked")
    private void exportToNearby(HugeInventory handler, Direction facing) {
        if (handler.storage.isEmpty()) return;
        var level = getLevel();
        if (level != null && holder.blockEntityDirectionCache.getAdjacentKeyHandler(level, getPos(), facing, AEKeyTypes.ITEMS, StorageAccess.INSERT) instanceof IKeyHandler<?> target) {
            KeyTransfer.transfer(handler.storage, (IKeyHandler<AEItemKey>) target, Integer.MAX_VALUE);
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateInventorySubscription();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new ButtonConfigurator(WidgetIcons.REFUND, this::refundAll).setTooltips(List.of(Component.translatable("gtmthings.machine.huge_item_bus.tooltip.1"))));
    }

    @Override
    public Widget createUIWidget() {
        // 与其他总线、仓一样分两区：上面放入槽、存储物品（只取）、取出一组按钮，下面状态面板（物品、存储数量）
        var importSlot = ItemSlot.of(createImportItems(), 0, false, true);
        importSlot.setBackgroundTexture(new GuiTextureGroup(UITheme.ITEM_SLOT, GuiTextures.IN_SLOT_OVERLAY));
        var storedSlot = ItemSlot.of(inventory.storage, 0, false, false);
        storedSlot.setItemHook(s -> s.copyWithCount((int) Math.min(inventory.storage.amountAt(0), s.getMaxStackSize())));
        var extract = Button.icon(UISizes.SLOT_SIZE, UITheme.ARROW_DOWN);
        extract.setOnServerClick(() -> extractStack(extract.getGui() == null ? null : extract.getGui().entityPlayer));
        extract.setHoverTooltips(HatchViews.EXTRACT_STACK);
        var status = new StatusPanel();
        status.addLine(HatchViews.ITEM, new StoredName());
        status.addLine(HatchViews.STORED, new StoredText());
        return HatchViews.page(HatchViews.operations(importSlot, HatchViews.group(storedSlot, extract)), status);
    }

    /** 取出一组给玩家，背包放不下的部分像原版一样丢在玩家面前。 */
    private void extractStack(@Nullable Player player) {
        if (player == null) return;
        var key = inventory.storage.keyAt(0);
        if (key == null) return;
        long n = inventory.storage.extract(0, key, Math.min(inventory.storage.amountAt(0), key.getMaxStackSize()), false);
        if (n <= 0) return;
        var extracted = key.toStack((int) n);
        // addItem 只放进一部分时也返回 true，剩下的留在 extracted 里
        player.getInventory().add(extracted);
        if (!extracted.isEmpty()) player.drop(extracted, false);
    }

    /** 存储物品的名称：物品不变时复用上次的文字。 */
    private final class StoredName implements Supplier<Component> {

        @Nullable
        private AEItemKey last;
        private Component text = Component.translatable(HatchViews.EMPTY);

        @Override
        public Component get() {
            var current = inventory.storage.keyAt(0);
            if (current != last) {
                last = current;
                text = current == null ? Component.translatable(HatchViews.EMPTY) : current.getReadOnlyStack().getHoverName();
            }
            return text;
        }
    }

    /** 存储数量的文字：数量不变时复用上次的文字。 */
    private final class StoredText implements Supplier<Component> {

        private long count = -1;
        private Component text = Component.empty();

        @Override
        public Component get() {
            long current = inventory.storage.amountAt(0);
            if (current != count) {
                count = current;
                text = Component.literal(FormattingUtil.formatNumbers(current));
            }
            return text;
        }
    }

    private KeyInventory<AEItemKey> createImportItems() {
        var importItems = KeyInventory.items(1);
        importItems.setFilter(key -> key instanceof AEItemKey itemKey && inventory.canCapInput() && inventory.storage.insert(0, itemKey, 1, true) > 0);
        importItems.setOnChanged(() -> {
            var key = importItems.keyAt(0);
            if (key == null) return;
            long amount = importItems.amountAt(0);
            importItems.set(0, null, 0);
            inventory.storage.insert(0, key, amount, false);
        });
        return importItems;
    }

    private static final class HugeInventory extends NotifiableInventory<AEItemKey> {

        private HugeInventory(MetaMachine machine) {
            super(machine, KeyInventory.items(1, Long.MAX_VALUE, false), IO.IN, IO.BOTH);
        }

        @Override
        public void readCustomSaveData(StringMapData data, int dataVersion) {
            super.readCustomSaveData(data, dataVersion);
            readLegacyStorage(data, dataVersion);
        }

        @Deprecated(since = "0.6.0", forRemoval = true)
        @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
        private void readLegacyStorage(StringMapData data, int dataVersion) {
            var legacy = data.get("storage");
            if (!(legacy instanceof StringMapData) && (legacy == null || legacy.toCustomData(NbtUtil.COMPOUND_TAG_TYPE) == null)) return;
            data.remove("storage");
            var nbt = DataCodecs.COMPOUND_TAG_CODEC.decode(legacy, dataVersion);
            var stackTag = nbt.getCompound("stack").copy();
            stackTag.putByte("Count", (byte) 1);
            storage.set(0, AEItemKey.of(ItemStack.of(stackTag)), nbt.getLong("count"));
        }
    }

    public NotifiableInventory<AEItemKey> getInventory() {
        return this.inventory;
    }
}

package com.gtocore.common.machine.multiblock.part;

import com.gtolib.utils.MathUtil;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;
import com.gregtechceu.gtceu.uiwidgets.inventory.HatchViews;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.TaskHandler;
import com.gregtechceu.gtceu.utils.function.ObjLongPredicate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.gto.datasynclib.LogicalSide;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.util.DataCodecs;
import com.gto.recipesearch.IntLongMap;
import com.hepdd.gtmthings.api.machine.fancyconfigurator.ButtonConfigurator;
import com.hepdd.gtmthings.api.transfer.UnlimitItemTransferHelper;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class HugeBusPartMachine extends WorkableTieredIOPartMachine implements IMachineLife {

    @SaveToDisk
    private final HugeNotifiableItemStackHandler inventory;
    @Nullable
    private TickableSubscription autoIOSubs;
    @Nullable
    private ISubscription inventorySubs;

    public HugeBusPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.IV, IO.IN);
        this.inventory = new HugeNotifiableItemStackHandler(this);
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
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }

    private void updateInventorySubscription() {
        var level = getLevel();
        if (level != null && isWorkingEnabled() && holder.blockEntityDirectionCache.hasAdjacentItemHandler(getLevel(), getPos(), getFrontFacing())) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO, 40);
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

    private void exportToNearby(HugeNotifiableItemStackHandler handler, Direction facing) {
        if (handler.getCount() < 1) return;
        var level = getLevel();
        var pos = getPos();
        if (level != null) {
            UnlimitItemTransferHelper.exportToTarget(handler.storage, Integer.MAX_VALUE, f -> true, level, pos.relative(facing), facing.getOpposite());
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
        var importSlot = new ItemSlot(createImportItems(), 0, false, true);
        importSlot.setBackgroundTexture(new GuiTextureGroup(UITheme.ITEM_SLOT, GuiTextures.IN_SLOT_OVERLAY));
        var storedSlot = new ItemSlot(inventory, 0, false, false);
        storedSlot.setItemHook(s -> s.copyWithCount((int) Math.min(inventory.getCount(), s.getMaxStackSize())));
        var extract = Button.icon(UITheme.ARROW_DOWN, UISizes.SLOT);
        extract.setOnServerClick(() -> extractStack(extract.getGui() == null ? null : extract.getGui().entityPlayer));
        extract.setHoverTooltips(HatchViews.EXTRACT_STACK);
        var status = new StatusPanel();
        status.addLine(HatchViews.ITEM, new StoredName());
        status.addLine(HatchViews.STORED, new StoredText());
        return HatchViews.page(HatchViews.operations(importSlot, HatchViews.group(storedSlot, extract)), status);
    }

    /** 取出一组给玩家，背包放不下的部分像原版一样丢在玩家面前。 */
    private void extractStack(@Nullable Player player) {
        if (player == null || inventory.isEmpty()) return;
        var extracted = inventory.extractItemInternal(0, (int) Math.min(inventory.getCount(), inventory.getStackInSlot(0).getMaxStackSize()), false);
        // addItem 只放进一部分时也返回 true，剩下的留在 extracted 里
        player.getInventory().add(extracted);
        if (!extracted.isEmpty()) player.drop(extracted, false);
    }

    /** 存储物品的名称：物品不变时复用上次的文字。 */
    private final class StoredName implements Supplier<Component> {

        private ItemStack last = ItemStack.EMPTY;
        private Component text = Component.translatable(HatchViews.EMPTY);

        @Override
        public Component get() {
            var current = inventory.getStackInSlot(0);
            if (current.isEmpty() != last.isEmpty() || !ItemStack.isSameItemSameTags(current, last)) {
                last = current.copyWithCount(1);
                text = current.isEmpty() ? Component.translatable(HatchViews.EMPTY) : current.getHoverName();
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
            long current = inventory.getCount();
            if (current != count) {
                count = current;
                text = Component.literal(FormattingUtil.formatNumbers(current));
            }
            return text;
        }
    }

    private CustomItemStackHandler createImportItems() {
        var importItems = new CustomItemStackHandler();
        importItems.setFilter(itemStack -> inventory.canCapInput() && (inventory.insertItem(0, itemStack, true).getCount() != itemStack.getCount()));
        importItems.setOnContentsChanged(() -> {
            var item = importItems.getStackInSlot(0).copy();
            if (!item.isEmpty()) {
                importItems.setStackInSlot(0, ItemStack.EMPTY);
                importItems.onContentsChanged(0);
                inventory.insertItem(0, item.copy(), false);
            }
        });
        return importItems;
    }

    private static final class HugeNotifiableItemStackHandler extends NotifiableItemStackHandler {

        private HugeNotifiableItemStackHandler(MetaMachine machine) {
            super(machine, 1, IO.IN, IO.BOTH, i -> new HugeCustomItemStackHandler());
        }

        private long getCount() {
            return ((HugeCustomItemStackHandler) storage).count;
        }

        @Override
        public boolean forEachItems(ObjLongPredicate<ItemStack> function) {
            var amount = ((HugeCustomItemStackHandler) storage).count;
            if (amount > 0) {
                return function.test(getStackInSlot(0), amount);
            }
            return false;
        }

        @Override
        public void fastForEachItems(ObjLongConsumer<ItemStack> function) {
            var amount = ((HugeCustomItemStackHandler) storage).count;
            if (amount > 0) {
                function.accept(getStackInSlot(0), amount);
            }
        }

        @Override
        public boolean updateEmpty() {
            return storage.stacks[0].isEmpty();
        }

        @Override
        public void fillSearchMap(GTRecipeType type, IntLongMap map) {
            var amount = ((HugeCustomItemStackHandler) storage).count;
            if (amount > 0) {
                type.convertItem(getStackInSlot(0), amount, map);
            }
        }

        @Override
        public boolean canCapOutput() {
            return true;
        }

        @Override
        public boolean handleRecipeItem(IO io, GTRecipe recipe, List<Content<ItemIngredient>> items, boolean simulate) {
            if (io != IO.IN || getCount() < 1) return items.isEmpty();
            for (var it = items.iterator(); it.hasNext();) {
                var ingredient = it.next();
                if (ingredient.isEmpty()) {
                    it.remove();
                    continue;
                }
                if (ingredient.inner.test(getStackInSlot(0))) {
                    var extracted = Math.min(ingredient.amount, getCount());
                    if (!simulate) {
                        ((HugeCustomItemStackHandler) storage).count -= extracted;
                        getStackInSlot(0).setCount(MathUtil.saturatedCast(((HugeCustomItemStackHandler) storage).count));
                        storage.onContentsChanged(0);
                    }
                    ingredient.shrink(extracted);
                    if (ingredient.amount <= 0) {
                        it.remove();
                        break;
                    }
                }
            }
            return items.isEmpty();
        }
    }

    private static final class HugeCustomItemStackHandler extends CustomItemStackHandler {

        private long count;

        private HugeCustomItemStackHandler() {
            super(1);
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public void onContentsChanged(int index) {
            count = stacks[0].getCount();
            super.onContentsChanged(index);
        }

        @Override
        public int extract(int slot, ItemStack s, int amount, boolean simulate) {
            var count = MathUtil.saturatedCast(this.count);
            if (amount == 0 || count < 1 || this.stacks[0].isEmpty()) return 0;
            if (amount >= count) {
                if (!simulate) {
                    this.count = 0;
                    this.stacks[0] = ItemStack.EMPTY;
                    super.onContentsChanged(0);
                }
                return count;
            } else {
                if (!simulate) {
                    this.count -= amount;
                    stacks[0].setCount(MathUtil.saturatedCast(count));
                    super.onContentsChanged(0);
                }
                return amount;
            }
        }

        @Override
        public int insert(int slot, ItemStack stack, int amount, boolean simulate) {
            if (amount == 0 || stack.isEmpty()) return 0;
            if (count < 1 || this.stacks[0].isEmpty()) {
                if (!simulate) {
                    this.stacks[0] = stack.copy();
                    this.count = amount;
                    super.onContentsChanged(0);
                }
                return amount;
            } else if (this.stacks[0].getItem() == stack.getItem()) {
                var tag = this.stacks[0].getTag();
                if (tag == null) {
                    if (stack.getTag() == null) {
                        if (!simulate) {
                            this.count += amount;
                            this.stacks[0].setCount(MathUtil.saturatedCast(this.count));
                            super.onContentsChanged(0);
                        }
                        return amount;
                    }
                } else if (tag.equals(stack.getTag())) {
                    if (!simulate) {
                        this.count += amount;
                        this.stacks[0].setCount(MathUtil.saturatedCast(this.count));
                        super.onContentsChanged(0);
                    }
                    return amount;
                }
            }
            return 0;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public void writeBuffer(LogicalSide side, FriendlyByteBuf data) {
            // 无同步，不实现
        }

        @Override
        public void readBuffer(LogicalSide side, FriendlyByteBuf data) {
            // 无同步，不实现
        }

        @Override
        public Data writeData() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("stack", stacks[0].serializeNBT());
            nbt.putLong("count", count);
            return DataCodecs.COMPOUND_TAG_CODEC.encode(nbt);
        }

        @Override
        public void readData(Data data, int dataVersion) {
            var nbt = DataCodecs.COMPOUND_TAG_CODEC.decode(data);
            var stack = nbt.get("stack");
            if (stack instanceof CompoundTag tag) {
                this.stacks[0] = ItemStack.of(tag);
            }
            count = nbt.getLong("count");
            this.stacks[0].setCount(MathUtil.saturatedCast(count));
        }
    }

    public NotifiableItemStackHandler getInventory() {
        return this.inventory;
    }
}

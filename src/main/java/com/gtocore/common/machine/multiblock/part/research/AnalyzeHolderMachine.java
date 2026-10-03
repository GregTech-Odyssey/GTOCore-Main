package com.gtocore.common.machine.multiblock.part.research;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.common.item.DataCrystalItem;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeStackAdapter;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class AnalyzeHolderMachine extends MultiblockPartMachine implements IMachineLife {

    public static final int CATALYST_SLOT = 0;
    public static final int EMPTY_SLOT = 1;
    public static final int DATA_SLOT = 2;

    protected final IO io;

    @SaveToDisk
    private final AnalyzeHolder heldItems;
    @Setter
    @Getter
    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean isLocked;

    public AnalyzeHolderMachine(MetaMachineBlockEntity holder) {
        super(holder);
        this.io = IO.IN;
        heldItems = new AnalyzeHolder(this);
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(this.heldItems.storage);
    }

    public @NotNull NotifiableStackInventory getAsHandler() {
        return heldItems;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        var handler = new LockedSlots();
        group.addWidget(new ImageWidget(0, 15, 84, 60, GuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE))
                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT, 33, 36, true, io.support(IO.IN))
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new BlockableSlotWidget(handler, CATALYST_SLOT, 99, 15, true, io.support(IO.IN))
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.MOLECULAR_OVERLAY_1))
                .addWidget(new BlockableSlotWidget(handler, EMPTY_SLOT, 99, 57, true, io.support(IO.IN))
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY));
        return group;
    }

    @Override
    public void setFrontFacing(@NotNull Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        var controllers = getControllers();
        for (var controller : controllers) {
            if (controller != null && controller.isFormed()) controller.checkPatternWithLock();
        }
    }

    private final class LockedSlots implements IItemHandlerModifiable {

        private final ForgeStackAdapter delegate = new ForgeStackAdapter(heldItems.storage);

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return delegate.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return isLocked ? ItemStack.EMPTY : delegate.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegate.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return delegate.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            delegate.setStackInSlot(slot, stack);
        }
    }

    private static class AnalyzeHolder extends NotifiableStackInventory {

        private final AnalyzeHolderMachine machine;

        private AnalyzeHolder(AnalyzeHolderMachine machine) {
            super(machine, new Slots(), IO.IN, IO.BOTH);
            this.machine = machine;
        }

        // 防止在锁定状态下提取物品
        @Override
        public boolean canCapOutput() {
            return !machine.isLocked() && super.canCapOutput();
        }

        private static final class Slots extends StackInventory {

            private Slots() {
                super(3);
            }

            // 各槽位容量限制
            @Override
            public int getSlotLimit(int slot) {
                return switch (slot) {
                    case DATA_SLOT, CATALYST_SLOT -> 1;
                    case EMPTY_SLOT -> 64;
                    default -> super.getSlotLimit(slot);
                };
            }

            // 槽位物品验证
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (stack.isEmpty()) return true;

                // 检查是否为数据物品
                boolean isDataItem = false;
                boolean hasNBT = false;
                boolean emptyNBT = false;
                if (stack.getItem() instanceof DataCrystalItem) {
                    isDataItem = true;
                    hasNBT = stack.hasTag();
                    if (stack.getTag() != null && stack.hasTag() && stack.getTag().contains("empty_crystal", CompoundTag.TAG_COMPOUND))
                        emptyNBT = true;
                }

                return switch (slot) {
                    case DATA_SLOT -> hasNBT && !emptyNBT;
                    case EMPTY_SLOT -> emptyNBT;
                    case CATALYST_SLOT -> !isDataItem;
                    default -> super.isItemValid(slot, stack);
                };
            }
        }
    }
}

package com.gtocore.common.machine.multiblock.part.research;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.common.item.DataCrystalItem;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableStackInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeStackAdapter;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class DataGenerateHolderMachine extends MultiblockPartMachine implements IMachineLife {

    public static final int CATALYST_SLOT_1 = 0;
    public static final int CATALYST_SLOT_2 = 1;
    public static final int EMPTY_SLOT = 2;
    public static final int[] DATA_SLOT = { 3, 4, 5, 6, 7, 8, 9, 10 };

    @SaveToDisk
    private final DataGenerateHolder heldItems;
    @Setter
    @Getter
    @SaveToDisk(defaultValue = "false")
    @SyncToClient
    private boolean isLocked;

    public DataGenerateHolderMachine(MetaMachineBlockEntity holder) {
        super(holder);
        heldItems = new DataGenerateHolder(this);
    }

    public @NotNull ItemStack getDataItem(boolean remove) {
        return getHeldItem(EMPTY_SLOT, remove);
    }

    public void setDataItem(@NotNull ItemStack dataItem) {
        heldItems.storage.setStackInSlot(EMPTY_SLOT, dataItem);
    }

    public @NotNull NotifiableStackInventory getAsHandler() {
        return heldItems;
    }

    @NotNull
    private ItemStack getHeldItem(int slot, boolean remove) {
        ItemStack stackInSlot = heldItems.storage.getStackInSlot(slot);
        if (remove && !stackInSlot.isEmpty()) {
            heldItems.storage.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return stackInSlot;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(this.heldItems.storage);
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        int centerX = 65;
        int centerY = 48;
        var handler = new ForgeStackAdapter(heldItems.storage, () -> true, () -> !isLocked());
        group.addWidget(new ImageWidget(centerX - 33, centerY - 21, 84, 60, GTOGuiTextures.PROGRESS_BAR_DATA_GENERATE_BASE))

                .addWidget(new BlockableSlotWidget(handler, CATALYST_SLOT_1, 0, centerY - 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.MOLECULAR_OVERLAY_1))
                .addWidget(new BlockableSlotWidget(handler, CATALYST_SLOT_2, 0, centerY + 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.MOLECULAR_OVERLAY_1))
                .addWidget(new BlockableSlotWidget(handler, EMPTY_SLOT, centerX, centerY)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY))

                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT[0], centerX - 33, centerY - 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT[1], centerX - 11, centerY - 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT[2], centerX + 11, centerY - 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT[3], centerX + 33, centerY - 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT[4], centerX + 33, centerY + 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT[5], centerX + 11, centerY + 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT[6], centerX - 11, centerY + 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new BlockableSlotWidget(handler, DATA_SLOT[7], centerX - 33, centerY + 39)
                        .setIsBlocked(this::isLocked)
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

    private static class DataGenerateHolder extends NotifiableStackInventory {

        private final DataGenerateHolderMachine machine;

        private DataGenerateHolder(DataGenerateHolderMachine machine) {
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
                super(11);
            }

            // 各槽位容量限制
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            // 槽位物品验证
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (stack.isEmpty()) return true;

                // 检查是否为数据物品
                boolean isExDataItem = false;
                boolean isDataItem = false;
                boolean hasExNBT = false;
                boolean hasNBT = false;
                boolean emptyNBT = false;
                if (stack.getItem() instanceof DataCrystalItem) {
                    isExDataItem = true;
                    hasExNBT = stack.hasTag();
                    if (stack.getTag() != null && stack.hasTag() && stack.getTag().contains("empty_crystal", CompoundTag.TAG_COMPOUND))
                        emptyNBT = true;
                } else if (stack.getItem() instanceof IDataItem) {
                    isDataItem = true;
                    hasNBT = !stack.hasTag();
                }

                if (slot == EMPTY_SLOT) return hasNBT;
                else if (slot >= 3 && slot <= 10) return hasExNBT && !emptyNBT;
                else if (slot == CATALYST_SLOT_1 || slot == CATALYST_SLOT_2) return !isExDataItem && !isDataItem;
                else return super.isItemValid(slot, stack);
            }
        }
    }
}

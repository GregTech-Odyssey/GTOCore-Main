package com.gtocore.common.machine.multiblock.part;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.common.item.DataCrystalItem;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.core.Direction;
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

public class ResearchHolderMachine extends MultiblockPartMachine implements IMachineLife {

    public static final int EMPTY_SLOT = 0;

    protected final IO io;

    @SaveToDisk
    private final ResearchHolder heldItems;
    @Setter
    @Getter
    @SaveToDisk
    @SyncToClient
    private boolean isLocked;

    public ResearchHolderMachine(MetaMachineBlockEntity holder) {
        super(holder);
        this.io = IO.IN;
        heldItems = new ResearchHolder(this);
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(this.heldItems.storage);
    }

    public @NotNull NotifiableItemStackHandler getAsHandler() {
        return heldItems;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        int centerX = 60 - 18;
        int centerY = 55;
        group.addWidget(new ImageWidget(centerX - 40, centerY - 28 - 16, 98, 74 + 32, GTOGuiTextures.PROGRESS_BAR_RESEARCH_BASE))

                .addWidget(new BlockableSlotWidget(heldItems, EMPTY_SLOT, centerX, centerY, true, io.support(IO.IN))
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY));

        return group;
    }

    @Override
    public void setFrontFacing(@NotNull Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        var controllers = getControllers();
        for (var controller : controllers) {
            if (controller != null && controller.isFormed()) {
                controller.checkPatternWithLock();
            }
        }
    }

    @Override
    public void onChanged() {
        super.onChanged();
        getControllers().forEach(m -> {
            if (m.self() instanceof IRecipeLogicMachine rm) rm.getRecipeLogic().updateTickSubscription();
        });
    }

    private static class ResearchHolder extends NotifiableItemStackHandler {

        private final ResearchHolderMachine machine;

        private ResearchHolder(ResearchHolderMachine machine) {
            super(machine, 1, IO.IN, IO.BOTH, MyCustomItemStackHandler::new);
            this.machine = machine;
        }

        // 各槽位容量限制
        @Override
        public int getSlotLimit(int slot) {
            if (slot == EMPTY_SLOT) return 1;
            else return super.getSlotLimit(slot);
        }

        // 防止在锁定状态下提取物品
        @Override
        public boolean canCapOutput() {
            return !machine.isLocked() && super.canCapOutput();
        }

        // 槽位物品验证
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) return true;
            if (slot == EMPTY_SLOT) return stack.getItem() instanceof DataCrystalItem;
            else return super.isItemValid(slot, stack);
        }

        private static final class MyCustomItemStackHandler extends CustomItemStackHandler {

            private MyCustomItemStackHandler(int size) {
                super(size);
            }

            @Override
            public int getSlotLimit(int slot) {
                if (slot == EMPTY_SLOT) return 1;
                else return super.getSlotLimit(slot);
            }
        }
    }
}

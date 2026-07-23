package com.gtocore.common.machine.multiblock.part.research;

import com.gtocore.api.gui.GTOGuiTextures;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.item.DataCrystalItem;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SimpleResearchTagPartMachine extends MultiblockPartMachine implements IMachineLife {

    @SaveToDisk
    private final ScanningHolder heldItems;
    @Setter
    @Getter
    @SaveToDisk
    @SyncToClient(notifyUpdate = true)
    private double dataCache;

    SimpleResearchTagPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
        heldItems = new ScanningHolder(this);
    }

    public static Function<MetaMachineBlockEntity, MetaMachine> create(ResearchTag researchTag, long dataCapacity) {
        return (holder) -> new SimpleResearchTagPartMachine(holder) {
            ;

            @Override
            public long getDataCapacity() {
                return dataCapacity;
            }

            @Override
            public ResearchTag getResearchTag() {
                return researchTag;
            }
        };
    }

    public NotifiableItemStackHandler getAsHandler() {
        return heldItems;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(this.heldItems.storage);
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        group.addWidget(new ImageWidget(46, 15, 84, 60, GuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE))
                .addWidget(new SlotWidget(heldItems, 0, 79, 36)
                        .setBackground(GuiTextures.SLOT, GTOGuiTextures.DATA_CRYSTAL_OVERLAY))
                .addWidget(new ScanningWidget(15, 15, 18, 60, this)
                        .setBackground(GuiTextures.SLOT));
        return group;
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        var controllers = getControllers();
        for (var controller : controllers) {
            if (controller != null && controller.isFormed()) controller.checkPatternWithLock();
        }
    }

    public void addData(double amount) {
        amount += dataCache;
        var dataCrystalStack = heldItems.getStackInSlot(0);
        if (dataCrystalStack.getItem() instanceof DataCrystalItem) {
            var remainingCapacity = DataCrystalItem.getRemainingCapacity(dataCrystalStack) / getResearchTag().getBytePerPoint();
            long dataToAdd = (long) Math.floor(Math.min(amount, remainingCapacity));
            DataCrystalItem.addResearchData(dataCrystalStack, getResearchTag(), dataToAdd);
            amount = amount - dataToAdd;
            if (getOwnerUUID() != null) {
                DataCrystalItem.setTeamUUID(dataCrystalStack, TeamUtil.getTeamUUID(getOwnerUUID()));
            }
        }
        dataCache = Math.min(amount, getDataCapacity());
    }

    public abstract long getDataCapacity();

    public abstract ResearchTag getResearchTag();

    @Override
    public boolean canShared() {
        return false;
    }

    private static final class ScanningHolder extends NotifiableItemStackHandler {

        private ScanningHolder(SimpleResearchTagPartMachine machine) {
            super(machine, 3, IO.NONE, IO.BOTH, ScanningHolderStackHandler::new);
        }

        // 各槽位容量限制
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        // 槽位物品验证
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) return true;

            return stack.getItem() instanceof DataCrystalItem;
        }

        private static final class ScanningHolderStackHandler extends CustomItemStackHandler {

            private ScanningHolderStackHandler(int size) {
                super(size);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        }
    }

    public static final class ScanningWidget extends Widget implements IIngredientSlot {

        private final SimpleResearchTagPartMachine machine;

        public ScanningWidget(int x, int y, int width, int height, SimpleResearchTagPartMachine machine) {
            super(x, y, width, height);
            this.machine = machine;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            var dataCapacity = machine.getDataCapacity();
            var fillHeight = (int) (machine.dataCache / dataCapacity * (this.getSizeHeight() - 2));
            var fillBottom = this.getPosition().getY() + this.getSizeHeight() - 1;
            var fillTop = fillBottom - fillHeight;
            graphics.fill(getPositionX() + 1, fillTop,
                    getPositionX() + 1 + getSizeWidth() - 2, fillBottom, machine.getResearchTag().getColor() | 0xaa000000);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public @Nullable Object getXEIIngredientOverMouse(double v, double v1) {
            if (machine.dataCache > 0 && this.isMouseOverElement((int) v, (int) v1) && this.getHoverElement((int) v, (int) v1) == this) {
                return new ResearchTagEmiStack(machine.getResearchTag()).setAmount((long) machine.dataCache);
            }
            return null;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        protected void drawTooltipTexts(int mouseX, int mouseY) {
            var ingredient = (ResearchTagEmiStack) getXEIIngredientOverMouse(mouseX, mouseY);
            if (ingredient != null && this.isMouseOverElement(mouseX, mouseY) && this.getHoverElement(mouseX, mouseY) == this && this.gui != null && this.gui.getModularUIGui() != null) {
                this.gui.getModularUIGui().setHoverTooltip(ingredient.getTooltipText(), ItemStack.EMPTY, null, null);
            }
        }
    }
}

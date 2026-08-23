package com.hepdd.gtmthings.common.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

public final class VirtualFluidProviderBehavior implements IAddInformation, IItemUIFactory {

    public static final VirtualFluidProviderBehavior INSTANCE = new VirtualFluidProviderBehavior();

    public static ItemStack setVirtualFluid(ItemStack stack, FluidStack virtualFluid) {
        if (virtualFluid.isEmpty()) {
            VirtualProviderData.clearContent(stack);
            return stack;
        }
        FluidStack storedFluid = ICustomFluidStackHandler.copy(virtualFluid, 1000);
        VirtualProviderData.setContent(stack, storedFluid.writeToNBT(new CompoundTag()));
        return stack;
    }

    public static FluidStack getVirtualFluid(final ItemStack stack) {
        CompoundTag fluidTag = VirtualProviderData.getContent(stack);
        if (fluidTag == null) return FluidStack.EMPTY;
        return FluidStack.loadFluidStackFromNBT(fluidTag);
    }

    @Override
    public void appendTooltips(@NotNull ItemStack itemstack, @Nullable Level world, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        FluidStack fluid = getVirtualFluid(itemstack);
        if (fluid.isEmpty()) return;
        list.add(Component.translatable("gui.ae2.Fluids").append(": "));
        list.add(fluid.getDisplayName());
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return new ModularUI(176, 166, holder, entityPlayer)
                .widget(new FancyMachineUIWidget(new ProviderUI(holder.getHand()), 176, 166));
    }

    private record ProviderUI(InteractionHand hand) implements IFancyUIProvider {

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            WidgetGroup group = new WidgetGroup(0, 0, 18 + 16, 18 + 16);
            WidgetGroup container = new WidgetGroup(4, 4, 18 + 8, 18 + 8);
            container.addWidget(new TankWidget(new FluidHandler(widget.getGui().entityPlayer, hand), 0, 4, 4, true, true)
                    .setBackground(GuiTextures.FLUID_SLOT));
            group.addWidget(container);
            return group;
        }

        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(this);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return new ItemStackTexture(CustomItems.VIRTUAL_FLUID_PROVIDER.get());
        }

        @Override
        public Component getTitle() {
            return CustomItems.VIRTUAL_FLUID_PROVIDER.get().getDescription();
        }
    }

    private static class FluidHandler implements ICustomFluidStackHandler {

        private ItemStack getItem() {
            return entityPlayer.getItemInHand(hand);
        }

        private FluidStack virtualFluid;
        private final Player entityPlayer;
        private final InteractionHand hand;

        private FluidHandler(Player entityPlayer, InteractionHand hand) {
            this.entityPlayer = entityPlayer;
            this.hand = hand;
        }

        @Override
        public void setFluidInTank(int i, FluidStack fluidStack) {
            if (entityPlayer.isLocalPlayer()) return;
            virtualFluid = ICustomFluidStackHandler.copy(fluidStack, 1000);
            entityPlayer.setItemInHand(hand, setVirtualFluid(getItem(), virtualFluid));
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int i) {
            if (virtualFluid == null) virtualFluid = getVirtualFluid(getItem());
            return virtualFluid;
        }

        @Override
        public int getTankCapacity(int i) {
            return 1000;
        }

        @Override
        public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
            return !entityPlayer.isLocalPlayer() && !fluidStack.isEmpty();
        }

        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction) {
            if (entityPlayer.isLocalPlayer() || fluidStack.isEmpty() || fluidStack.getAmount() < 1000) return 0;
            if (fluidAction.execute()) {
                virtualFluid = ICustomFluidStackHandler.copy(fluidStack, 1000);
                entityPlayer.setItemInHand(hand, setVirtualFluid(getItem(), virtualFluid));
            }
            return 1000;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
            if (fluidStack.getAmount() < 1000) return FluidStack.EMPTY;
            FluidStack stored = getFluidInTank(0);
            if (!stored.isFluidEqual(fluidStack)) return FluidStack.EMPTY;
            return drainStored(stored, fluidAction);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction fluidAction) {
            if (maxDrain < 1000) return FluidStack.EMPTY;
            return drainStored(getFluidInTank(0), fluidAction);
        }

        private FluidStack drainStored(FluidStack stored, FluidAction fluidAction) {
            if (stored.isEmpty() || entityPlayer.isLocalPlayer() || VirtualProviderData.isLocked(getItem())) return FluidStack.EMPTY;
            FluidStack drained = ICustomFluidStackHandler.copy(stored, 1000);
            if (fluidAction.execute()) {
                entityPlayer.setItemInHand(hand, setVirtualFluid(getItem(), FluidStack.EMPTY));
                virtualFluid = FluidStack.EMPTY;
            }
            return drained;
        }
    }
}

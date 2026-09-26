package com.hepdd.gtmthings.common.item;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.FluidSlot;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.item.HeldItemPage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

@DataGeneratorScanned
public final class VirtualFluidProviderBehavior implements IAddInformation, IItemUIFactory {

    public static final VirtualFluidProviderBehavior INSTANCE = new VirtualFluidProviderBehavior();

    @RegisterLanguage(cn = "未设置虚拟流体", en = "No virtual fluid set")
    private static final String EMPTY = "gtocore.virtual_fluid_provider.empty";

    public static ItemStack setVirtualFluid(ItemStack stack, FluidStack virtualFluid) {
        return VirtualProviderData.setVirtualFluid(stack, virtualFluid);
    }

    public static FluidStack getVirtualFluid(final ItemStack stack) {
        return VirtualProviderData.getVirtualFluid(stack);
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
        return HeldItemPage.create(holder, entityPlayer, window -> {
            var handler = new FluidHandler(entityPlayer, holder.getHand());
            var name = TextLine.of(0, () -> {
                var fluid = handler.getFluidInTank(0);
                return fluid.isEmpty() ? Component.translatable(EMPTY) : fluid.getDisplayName();
            }).setColor(UITheme.PANEL_TEXT);
            name.layout(l -> l.flex(1));
            var row = UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.SECTION_GAP).alignCenter()).addChildren(FluidSlot.of(handler), name);
            return UIElement.section(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH)).addChild(row);
        });
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

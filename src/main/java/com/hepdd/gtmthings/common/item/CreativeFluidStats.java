package com.hepdd.gtmthings.common.item;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.PhantomFluidSlot;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uiwidgets.cover.CoverUIs;
import com.gregtechceu.gtceu.uiwidgets.item.HeldItemPage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import com.hepdd.gtmthings.api.misc.CreativeFluidHandlerItemStack;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@DataGeneratorScanned
public class CreativeFluidStats implements IItemComponent, IComponentCapability, IAddInformation, IItemUIFactory {

    @RegisterLanguage(cn = "未设置流体", en = "No fluid set")
    private static final String EMPTY = "gtocore.creative_fluid_cell.empty";
    @RegisterLanguage(cn = "精确输出", en = "Accurate output")
    private static final String ACCURATE = "gtocore.creative_fluid_cell.accurate";
    @RegisterLanguage(cn = "开启后，单次抽取最多为下方设定的流体量；关闭时单次抽取不设上限", en = "When on, each drain is capped at the amount set below; when off, drains are not capped")
    private static final String ACCURATE_TIP = "gtocore.creative_fluid_cell.accurate.tip";
    @RegisterLanguage(cn = "单次抽取上限（mB）", en = "Drain cap (mB)")
    private static final String CAPACITY = "gtocore.creative_fluid_cell.capacity";
    @RegisterLanguage(cn = "未开启精确输出", en = "Accurate output is off")
    private static final String ACCURATE_OFF = "gtocore.creative_fluid_cell.accurate_off";

    @Override
    public void appendTooltips(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("gtmthings.creative_tooltip"));
        if (stack.hasTag() && stack.getTag().contains("Fluid")) {
            FluidUtil.getFluidContained(stack).ifPresent(tank -> {
                tooltipComponents
                        .add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip1", tank.getDisplayName()));
            });
            if (getAccurate(stack)) {
                tooltipComponents
                        .add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip3", getCapacity(stack)));
            }
        } else {
            tooltipComponents.add(Component.translatable("item.gtmthings.creative_fluid_cell.tooltip2"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            FluidStack fluidStack = getStored(itemStack);
            int capacity = getAccurate(itemStack) ? getCapacity(itemStack) : Integer.MAX_VALUE;
            if (!fluidStack.isEmpty()) {
                return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap, LazyOptional.of(() -> new CreativeFluidHandlerItemStack(itemStack, capacity, fluidStack)));
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return HeldItemPage.create(holder, entityPlayer, window -> {
            var tank = new CustomFluidTank(1000);
            tank.setFluid(getStored(holder.getHeld()));
            var slot = new PhantomFluidSlot(tank, 0, () -> getStored(holder.getHeld()), fluid -> {
                tank.setFluid(fluid.isEmpty() ? FluidStack.EMPTY : new FluidStack(fluid, 1000));
                if (!holder.isRemote()) setStored(holder.getHeld(), fluid);
            }).xeiPhantom();
            var name = TextLine.of(0, () -> {
                var fluid = getStored(holder.getHeld());
                return fluid.isEmpty() ? Component.translatable(EMPTY) : fluid.getDisplayName();
            }).setColor(UITheme.PANEL_TEXT);
            name.layout(l -> l.flex(1));
            var capacity = new NumberField(LayoutStyle.AUTO, () -> getCapacity(holder.getHeld()),
                    value -> setCapacity(holder.getHeld(), (int) Math.clamp(value, 1L, Integer.MAX_VALUE)), () -> 1L, () -> Integer.MAX_VALUE);
            capacity.disabled(() -> !getAccurate(holder.getHeld()), ACCURATE_OFF);
            return UIElement.section(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH)).addChildren(
                    UIElement.row(UISizes.SLOT).layout(l -> l.gapAll(UISizes.SECTION_GAP).alignCenter()).addChildren(slot, name),
                    CoverUIs.controlRow(ACCURATE, Switch.of(() -> getAccurate(holder.getHeld()), value -> setAccurate(holder.getHeld(), value)), ACCURATE_TIP),
                    CoverUIs.inlineNumberRow(CAPACITY, capacity));
        });
    }

    private static boolean getAccurate(ItemStack fluidCell) {
        CompoundTag tagCompound = fluidCell.getTag();
        return tagCompound != null && tagCompound.contains("Accurate") && tagCompound.getBoolean("Accurate");
    }

    private static void setAccurate(ItemStack fluidCell, boolean isEnable) {
        fluidCell.getOrCreateTag().putBoolean("Accurate", isEnable);
    }

    private static FluidStack getStored(ItemStack fluidCell) {
        CompoundTag tagCompound = fluidCell.getTag();
        return tagCompound != null && tagCompound.contains("Fluid") ? FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("Fluid")) : FluidStack.EMPTY;
    }

    private static void setStored(ItemStack fluidCell, FluidStack fluid) {
        if (fluid.isEmpty()) {
            fluidCell.getOrCreateTag().remove("Fluid");
            setAccurate(fluidCell, false);
        } else {
            FluidStack stored = fluid.copy();
            stored.setAmount(1000);
            CompoundTag fluidTag = new CompoundTag();
            stored.writeToNBT(fluidTag);
            fluidCell.getOrCreateTag().put("Fluid", fluidTag);
        }
    }

    private static int getCapacity(ItemStack fluidCell) {
        CompoundTag tagCompound = fluidCell.getTag();
        return tagCompound != null && tagCompound.contains("Capacity") ? tagCompound.getInt("Capacity") : 1000;
    }

    private static void setCapacity(ItemStack fluidCell, int capacity) {
        fluidCell.getOrCreateTag().putInt("Capacity", capacity);
    }
}

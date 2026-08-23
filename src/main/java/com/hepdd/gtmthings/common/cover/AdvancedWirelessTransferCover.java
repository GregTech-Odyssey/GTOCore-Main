package com.hepdd.gtmthings.common.cover;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.core.ILevel;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.hepdd.gtmthings.api.misc.BlockEntityCache;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.resources.ResourceLocation.tryParse;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedWirelessTransferCover extends CoverBehavior implements IUICover {

    public static final int TRANSFER_ITEM = 1;
    public static final int TRANSFER_FLUID = 2;

    protected final int transferType;
    private TickableSubscription subscription;
    protected ServerLevel targetLever;
    @SaveToDisk
    private String dimensionId;
    @SaveToDisk
    protected BlockPos targetPos;
    @SaveToDisk
    protected Direction facing;

    @SaveToDisk
    @SyncToClient
    @Getter
    protected final FilterHandler<FluidStack, FluidFilter> filterHandlerFluid;
    @SaveToDisk
    @SyncToClient
    @Getter
    protected final FilterHandler<ItemStack, ItemFilter> filterHandlerItem;

    private final BlockEntityCache target = new BlockEntityCache(() -> ILevel.getCachedBlockEntity(targetLever, targetPos));

    public AdvancedWirelessTransferCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int transferType) {
        super(definition, coverHolder, attachedSide);
        this.transferType = transferType;
        filterHandlerFluid = FilterHandlers.fluid(this);
        filterHandlerItem = FilterHandlers.item(this);
    }

    @Override
    public boolean canAttach() {
        if (super.canAttach()) {
            var targetMachine = MetaMachine.getMachine(coverHolder.holder());
            return targetMachine != null && (targetMachine.getItemHandlerCap(attachedSide, false) != null || targetMachine.getFluidHandlerCap(attachedSide, false) != null);
        }
        return false;
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            this.dimensionId = tag.getString("dimensionid");
            var intX = tag.getInt("x");
            var intY = tag.getInt("y");
            var intZ = tag.getInt("z");
            this.targetPos = new BlockPos(intX, intY, intZ);
            this.facing = readFacingOrFallback(tag.getString("facing"), attachedSide);
            getTargetLevel();
        }
        var targetMachine = MetaMachine.getMachine(coverHolder.holder());
        if (targetMachine instanceof SimpleTieredMachine simpleTieredMachine) {
            if (this.transferType == TRANSFER_ITEM) simpleTieredMachine.setAutoOutputItems(false);
            if (this.transferType == TRANSFER_FLUID) simpleTieredMachine.setAutoOutputFluids(false);
        } else if (targetMachine instanceof ItemBusPartMachine itemBusPartMachine && this.transferType == TRANSFER_ITEM) {
            itemBusPartMachine.setWorkingEnabled(false);
        } else if (targetMachine instanceof FluidHatchPartMachine fluidHatchPartMachine && this.transferType == TRANSFER_FLUID) {
            fluidHatchPartMachine.setWorkingEnabled(false);
        }
        super.onAttached(itemStack, player);
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandlerFluid.getFilterItem().isEmpty()) {
            list.add(filterHandlerFluid.getFilterItem());
        }
        if (!filterHandlerItem.getFilterItem().isEmpty()) {
            list.add(filterHandlerItem.getFilterItem());
        }
        return list;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getSafeFacing();
        getTargetLevel();
        subscription = coverHolder.subscribeServerTick(subscription, this::update, 20);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void update() {
        if (transferType == TRANSFER_ITEM) {
            var targetItemTransfer = getTargetItemTransfer();
            var ownItemTransfer = getOwnItemTransfer();
            if (ownItemTransfer != null && targetItemTransfer != null) {
                GTTransferUtils.transferItemsFiltered(ownItemTransfer, targetItemTransfer, filterHandlerItem.getFilter(), Integer.MAX_VALUE);
            }
        } else if (transferType == TRANSFER_FLUID) {
            var targetFluidTransfer = getTargetFluidTransfer();
            var ownFluidTransfer = getOwnFluidTransfer();
            if (ownFluidTransfer != null && targetFluidTransfer != null) {
                GTTransferUtils.transferFluidsFiltered(ownFluidTransfer, targetFluidTransfer, filterHandlerFluid.getFilter(), Integer.MAX_VALUE);
            }
        }
    }

    private void getTargetLevel() {
        if (this.dimensionId == null) return;
        ResourceLocation resLoc = tryParse(this.dimensionId);
        ResourceKey<Level> resKey = ResourceKey.create(Registries.DIMENSION, resLoc);
        this.targetLever = Objects.requireNonNull(coverHolder.getLevel().getServer()).getLevel(resKey);
    }

    protected @Nullable IItemHandler getOwnItemTransfer() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    protected @Nullable IItemHandler getTargetItemTransfer() {
        if (targetLever == null || targetPos == null) return null;
        return GTCapabilityHelper.getItemHandler(target.get(), getSafeFacing().getOpposite());
    }

    protected @Nullable IFluidHandler getOwnFluidTransfer() {
        return coverHolder.getFluidHandlerCap(attachedSide, false);
    }

    protected @Nullable IFluidHandler getTargetFluidTransfer() {
        if (targetLever == null || targetPos == null) return null;
        return GTCapabilityHelper.getFluidHandler(target.get(), getSafeFacing().getOpposite());
    }

    private Direction getSafeFacing() {
        this.facing = sanitizeFacing(this.facing, attachedSide);
        return this.facing;
    }

    static Direction readFacingOrFallback(@Nullable String facingName, @Nullable Direction fallback) {
        return sanitizeFacing(facingName == null ? null : Direction.byName(facingName), fallback);
    }

    static Direction sanitizeFacing(@Nullable Direction direction, @Nullable Direction fallback) {
        if (direction != null) return direction;
        return fallback != null ? fallback : Direction.NORTH;
    }

    @Override
    public Widget createUIWidget() {
        if (transferType == TRANSFER_ITEM) {
            return createItemUIWidget();
        } else {
            return createFluidUIWidget();
        }
    }

    public Widget createItemUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 107);
        var titleLabel = new LabelWidget(10, 5, Component.translatable("item.gtmthings.advanced_wireless_item_transfer_cover"));
        titleLabel.setText(Component.translatable("item.gtmthings.advanced_wireless_item_transfer_cover").getString());
        group.addWidget(titleLabel);
        group.addWidget(filterHandlerItem.createFilterSlotUI(10, 20));
        group.addWidget(filterHandlerItem.createFilterConfigUI(10, 42, 156, 60));

        return group;
    }

    public Widget createFluidUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 107);
        var titleLabel = new LabelWidget(10, 5, Component.translatable("item.gtmthings.advanced_wireless_fluid_transfer_cover"));
        titleLabel.setText(Component.translatable("item.gtmthings.advanced_wireless_fluid_transfer_cover").getString());
        group.addWidget(titleLabel);
        group.addWidget(filterHandlerFluid.createFilterSlotUI(10, 20));
        group.addWidget(filterHandlerFluid.createFilterConfigUI(10, 42, 156, 60));

        return group;
    }
}

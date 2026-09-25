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
import com.gregtechceu.gtceu.uipro.elements.StatusLine;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uiwidgets.cover.CoverUIs;
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
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

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
        var status = new StatusPanel();
        status.addLine("gtocore.cover.advanced_wireless_transfer.status", this::connectionText).level(this::connectionLevel);
        status.addLine("gtocore.cover.advanced_wireless_transfer.target",
                new Memo<>(() -> isBound() ? target.get() : null, be -> be.getBlockState().getBlock().getName()));
        status.addLine("gtocore.cover.advanced_wireless_transfer.position",
                new Memo<>(() -> targetPos, pos -> Component.literal(pos.toShortString())));
        status.addLine("gtocore.cover.advanced_wireless_transfer.dimension",
                new Memo<>(() -> dimensionId, Component::literal));
        return CoverUIs.page().addChildren(status,
                CoverUIs.filterSection(transferType == TRANSFER_ITEM ? filterHandlerItem : filterHandlerFluid));
    }

    private boolean isBound() {
        return targetLever != null && targetPos != null;
    }

    private boolean isConnected() {
        return transferType == TRANSFER_ITEM ? getTargetItemTransfer() != null : getTargetFluidTransfer() != null;
    }

    private Component connectionText() {
        if (!isBound()) return UNBOUND;
        return isConnected() ? CONNECTED : UNAVAILABLE;
    }

    private StatusLine.Level connectionLevel() {
        if (!isBound()) return StatusLine.Level.WARNING;
        return isConnected() ? StatusLine.Level.GOOD : StatusLine.Level.ERROR;
    }

    private static final Component NONE = Component.literal("—");
    private static final Component UNBOUND = Component.translatable("gtocore.cover.advanced_wireless_transfer.unbound");
    private static final Component CONNECTED = Component.translatable("gtocore.cover.advanced_wireless_transfer.connected");
    private static final Component UNAVAILABLE = Component.translatable("gtocore.cover.advanced_wireless_transfer.unavailable");

    private static final class Memo<K> implements Supplier<Component> {

        private final Supplier<K> key;
        private final Function<K, Component> text;
        private @Nullable K last;
        private Component value = NONE;

        private Memo(Supplier<K> key, Function<K, Component> text) {
            this.key = key;
            this.text = text;
        }

        @Override
        public Component get() {
            K current = key.get();
            if (current != last) {
                last = current;
                value = current == null ? NONE : text.apply(current);
            }
            return value;
        }
    }
}

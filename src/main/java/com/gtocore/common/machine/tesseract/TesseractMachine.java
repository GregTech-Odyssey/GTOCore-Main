package com.gtocore.common.machine.tesseract;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.CoordinateCardBehavior;

import com.gtolib.api.player.IEnhancedPlayer;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.core.ILevel;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;

import com.google.common.collect.ImmutableList;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TesseractMachine extends MetaMachine implements IFancyUIMachine, IMachineLife, ITesseractMarkerInteractable {

    private static final Set<Capability<?>> CAPABILITIES = Set.of(ForgeCapabilities.ITEM_HANDLER, ForgeCapabilities.FLUID_HANDLER);

    @Override
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }

    private WeakReference<BlockEntity> blockEntityReference;

    @SaveToDisk
    public BlockPos pos;

    @SaveToDisk
    protected NotifiableItemStackHandler inventory;

    private boolean call;

    public TesseractMachine(MetaMachineBlockEntity holder) {
        super(holder);
        inventory = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.NONE).setFilter(stack -> stack.is(GTOItems.COORDINATE_CARD.asItem()));
        inventory.storage.setOnContentsChanged(() -> {
            onChanged();
            call = false;
            pos = null;
            blockEntityReference = null;
            ItemStack card = inventory.storage.getStackInSlot(0);
            if (card.isEmpty()) return;
            CompoundTag posTags = card.getTag();
            if (posTags == null || !posTags.contains("x") || !posTags.contains("y") || !posTags.contains("z")) return;
            var pos = new BlockPos(posTags.getInt("x"), posTags.getInt("y"), posTags.getInt("z"));
            if (pos.equals(getPos())) return;
            this.pos = pos;
        });
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                WidgetIcons.HIGHLIGHT, WidgetIcons.HIGHLIGHT, () -> false,
                (clickData, pressed) -> {
                    if (clickData.isRemote && getLevel() != null && pos != null) {
                        AdvancedTesseractMachine.HIGHLIGHTS.add(ImmutableList.of(pos.asLong()), 200);
                    }
                })
                .setTooltipsSupplier(pressed -> Collections.singletonList(Component.translatable(HIGHLIGHT_TEXT))));
    }

    @Override
    public Widget createUIWidget() {
        var targets = new TesseractUI.Targets(this::getLevel, getPos(), this::cardTargets, () -> inventory.storage.getStackInSlot(0).hashCode(),
                Component.translatable(TesseractUI.ROW_EMPTY));
        var row = TesseractUI.row(ItemSlot.of(inventory.storage, 0), targets, 0, TesseractUI.face(targets, 0));
        var bound = Component.translatable(TesseractUI.VALUE_BOUND, 1, 1);
        var unbound = Component.translatable(TesseractUI.VALUE_BOUND, 0, 1);
        return TesseractUI.page(
                TesseractUI.status(() -> pos == null ? unbound : bound, false, false),
                TesseractUI.listSection(TesseractUI.SECTION_TARGETS, TesseractUI.column(List.of(row)),
                        TesseractUI.TARGETS_TOOLTIP, TesseractUI.TARGETS_MARKER_TOOLTIP),
                TesseractUI.basicPushSection());
    }

    private List<TesseractUI.Target> cardTargets() {
        var level = getLevel();
        var card = inventory.storage.getStackInSlot(0);
        var cardPos = CoordinateCardBehavior.getStoredCoordinates(card);
        if (level == null || cardPos == null) return Collections.singletonList(null);
        return Collections.singletonList(new TesseractUI.Target(GlobalPos.of(level.dimension(), cardPos), null));
    }

    @Override
    public @Nullable ICustomItemStackHandler getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        var cap = getCapability(ForgeCapabilities.ITEM_HANDLER, side);
        return cap != null ? cap.orElse(null) instanceof ICustomItemStackHandler m ? m : null : null;
    }

    @Override
    public @Nullable ICustomFluidStackHandler getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        var cap = getCapability(ForgeCapabilities.FLUID_HANDLER, side);
        return cap != null ? cap.orElse(null) instanceof ICustomFluidStackHandler m ? m : null : null;
    }

    @Override
    @Nullable
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (call) return null;
        if (pos != null && CAPABILITIES.contains(cap)) {
            LazyOptional<T> result = null;
            call = true;
            if (blockEntityReference == null) {
                var be = ILevel.getCachedBlockEntity(getLevel(), pos);
                if (be != null) {
                    blockEntityReference = new WeakReference<>(be);
                    result = be.getCapability(cap, side);
                }
            } else {
                var blockEntity = blockEntityReference.get();
                if (blockEntity == null || blockEntity.isRemoved()) {
                    blockEntity = ILevel.getCachedBlockEntity(getLevel(), pos);
                    if (blockEntity != null) {
                        blockEntityReference = new WeakReference<>(blockEntity);
                        result = blockEntity.getCapability(cap, side);
                    }
                } else {
                    result = blockEntity.getCapability(cap, side);
                }
            }
            call = false;
            if (side != null && result != null) {
                var handler = result.orElse(null);
                if (handler instanceof ICustomItemStackHandler modifiable) {
                    CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
                    return cover != null ? ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> cover.getItemHandlerCap(modifiable))) : result;
                } else if (handler instanceof ICustomFluidStackHandler modifiable) {
                    CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
                    return cover != null ? ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, LazyOptional.of(() -> cover.getFluidHandlerCap(modifiable))) : result;
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public boolean onMarkerInteract(Player player, List<TesseractDirectedTarget> targets) {
        if (getLevel() == null || getLevel().isClientSide()) return true;
        TesseractDirectedTarget first = null;
        for (var target : targets) {
            if (target.pos().dimension() == getLevel().dimension() && !target.pos().pos().equals(getPos())) {
                first = target;
                break;
            }
        }
        if (first == null) {
            player.displayClientMessage(Component.translatable(targets.isEmpty() ? WRITE_EMPTY_TEXT : WRITE_NO_TARGET_TEXT), true);
            return true;
        }
        ItemStack card = GTOItems.COORDINATE_CARD.asItem().getDefaultInstance();
        if (inventory.storage.getStackInSlot(0).isEmpty()) {
            card = ItemStack.EMPTY;
            if (card.isEmpty()) {
                var idx = player.getInventory().findSlotMatchingItem(GTOItems.COORDINATE_CARD.asItem().getDefaultInstance());
                if (idx >= 0) {
                    card = player.getInventory().removeItem(idx, 1);
                }
            }
            ae:
            if (card.isEmpty()) {
                var meStorage = IEnhancedPlayer.getMEStorageService((ServerPlayer) player);
                if (meStorage == null) {
                    break ae;
                }
                var cardNum = meStorage.getInventory().extract(AEItemKey.of(GTOItems.COORDINATE_CARD.asItem()), 1, Actionable.MODULATE, IActionSource.ofPlayer(player));
                if (cardNum <= 0) {
                    break ae;
                }
                card = GTOItems.COORDINATE_CARD.asItem().getDefaultInstance();
            }
            if (card.isEmpty()) {
                player.displayClientMessage(Component.translatable(WRITE_FAIL_TEXT), true);
                return true;
            }
        }
        var pos = first.pos().pos();
        CompoundTag posTags = card.getOrCreateTag();
        posTags.putInt("x", pos.getX());
        posTags.putInt("y", pos.getY());
        posTags.putInt("z", pos.getZ());
        inventory.storage.setStackInSlot(0, card);
        player.displayClientMessage(Component.translatable(WRITE_SUCCESS_TEXT), true);
        return true;
    }

    @Override
    public List<TesseractDirectedTarget> getMarkerTargets() {
        if (pos == null) return Collections.emptyList();
        var exposedInAirFace = Direction.NORTH;
        for (var face : Direction.values()) {
            var offsetPos = pos.relative(face);
            if (getLevel().getBlockState(offsetPos).isAir()) {
                exposedInAirFace = face;
                break;
            }
        }
        return List.of(new TesseractDirectedTarget(GlobalPos.of(getLevel().dimension(), pos), exposedInAirFace, 0));
    }
}

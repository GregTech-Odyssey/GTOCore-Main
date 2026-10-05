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
import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
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

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import com.google.common.collect.ImmutableList;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class TesseractMachine extends MetaMachine implements IFancyUIMachine, IMachineLife, ITesseractMarkerInteractable, TesseractCapCache.Holder {

    @Override
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }

    @SaveToDisk
    public BlockPos pos;

    @SaveToDisk
    protected NotifiableInventory<AEItemKey> inventory;

    private boolean call;

    private final TesseractTargets remotes = new TesseractTargets(this);
    private final TesseractCapCache<AEItemKey> itemCaps = TesseractCapCache.items(this);
    private final TesseractCapCache<AEFluidKey> fluidCaps = TesseractCapCache.fluids(this);

    public TesseractMachine(MetaMachineBlockEntity holder) {
        super(holder);
        inventory = NotifiableInventory.items(this, 1, IO.NONE, IO.NONE).setFilter(k -> k instanceof AEItemKey item && item.getItem() == GTOItems.COORDINATE_CARD.asItem());
        inventory.storage.setOnChanged(() -> {
            onChanged();
            call = false;
            pos = readCardPos();
            bindTargets();
            notifyExposureChanged();
        });
    }

    private void bindTargets() {
        var target = pos;
        remotes.resize(target == null ? 0 : 1);
        if (target != null) remotes.bind(0, getLevel(), target);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        bindTargets();
        remotes.subscribe(!isRemote());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        remotes.subscribe(false);
    }

    @Nullable
    private BlockPos readCardPos() {
        var card = inventory.storage.keyAt(0);
        if (card == null) return null;
        CompoundTag posTags = card.getTag();
        if (posTags == null || !posTags.contains("x") || !posTags.contains("y") || !posTags.contains("z")) return null;
        var pos = new BlockPos(posTags.getInt("x"), posTags.getInt("y"), posTags.getInt("z"));
        return pos.equals(getPos()) ? null : pos;
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
        var targets = new TesseractUI.Targets(this::getLevel, getPos(), this::cardTargets, inventory.storage::version,
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
        var card = inventory.storage.keyAt(0);
        var cardPos = card == null ? null : CoordinateCardBehavior.getStoredCoordinates(card.getReadOnlyStack());
        if (level == null || cardPos == null) return Collections.singletonList(null);
        return Collections.singletonList(new TesseractUI.Target(GlobalPos.of(level.dimension(), cardPos), null));
    }

    @Override
    public @Nullable IKeyHandler<AEItemKey> getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return itemCaps.collect(side);
    }

    @Override
    public @Nullable IKeyHandler<AEFluidKey> getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return fluidCaps.collect(side);
    }

    @Override
    public boolean isCalled() {
        return call;
    }

    @Override
    public void setCalled(boolean call) {
        this.call = call;
    }

    @Override
    public TesseractTargets getRemoteTargets() {
        return remotes;
    }

    @Override
    public TesseractCapCache<AEItemKey> getItemCaps() {
        return itemCaps;
    }

    @Override
    public TesseractCapCache<AEFluidKey> getFluidCaps() {
        return fluidCaps;
    }

    @Override
    public void clearDirectionCache() {
        if (itemCaps != null) {
            itemCaps.invalidate();
            fluidCaps.invalidate();
        }
        super.clearDirectionCache();
    }

    @Override
    public void onCoverUpdate(@Nullable CoverBehavior coverBehavior, Direction side) {
        itemCaps.invalidate(side);
        fluidCaps.invalidate(side);
        super.onCoverUpdate(coverBehavior, side);
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
        if (inventory.storage.amountAt(0) == 0) {
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
        inventory.storage.set(0, AEItemKey.of(card), card.getCount());
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

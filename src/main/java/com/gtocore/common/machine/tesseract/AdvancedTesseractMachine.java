package com.gtocore.common.machine.tesseract;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.item.CoordinateCardBehavior;

import com.gtolib.api.ae2.BlockingPatternTarget;
import com.gtolib.api.ae2.IPatternProviderLogic;
import com.gtolib.api.ae2.machine.ICustomCraftingMachine;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.patternprovider.PatternProviderTarget;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.util.holder.BooleanHolder;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class AdvancedTesseractMachine extends MetaMachine implements IFancyUIMachine, IMachineLife, ICustomCraftingMachine, ITesseractMarkerInteractable, TesseractCapCache.Holder {

    public static final Multiset<ImmutableList<Long>> HIGHLIGHTS = HashMultiset.create();
    public static final int MAX_TARGETS = 20;

    @SaveToDisk
    @SyncToClient(autoDetect = false)
    public final List<BlockPos> poss = new ArrayList<>(MAX_TARGETS);

    @SaveToDisk
    protected NotifiableInventory<AEItemKey> inventory;

    @SaveToDisk(defaultValue = "false")
    private boolean roundRobin;

    private final TesseractTargets remotes = new TesseractTargets(this);
    @Getter
    private final TesseractCapCache<AEItemKey> itemCaps = TesseractCapCache.items(this);
    @Getter
    private final TesseractCapCache<AEFluidKey> fluidCaps = TesseractCapCache.fluids(this);

    @Getter
    @Setter
    private boolean called;

    public AdvancedTesseractMachine(MetaMachineBlockEntity holder) {
        super(holder);
        inventory = NotifiableInventory.items(this, MAX_TARGETS, IO.NONE, IO.NONE).setFilter(k -> k instanceof AEItemKey item && item.getItem() == GTOItems.COORDINATE_CARD.asItem());
        inventory.storage.setOnChanged(() -> {
            onChanged();
            called = false;
            poss.clear();
            for (int i = 0; i < MAX_TARGETS; i++) {
                var card = inventory.storage.keyAt(i);
                if (card == null) continue;
                CompoundTag posTags = card.getTag();
                if (posTags == null || !posTags.contains("x") || !posTags.contains("y") || !posTags.contains("z"))
                    continue;
                var pos = new BlockPos(posTags.getInt("x"), posTags.getInt("y"), posTags.getInt("z"));
                if (pos.equals(getPos())) continue;
                if (!poss.contains(pos)) {
                    poss.add(pos);
                }
            }
            bindTargets();
            notifyExposureChanged();
            markFieldsForSync("poss");
        });
    }

    private void bindTargets() {
        int size = poss.size();
        remotes.resize(size);
        var level = getLevel();
        for (int i = 0; i < size; i++) {
            remotes.bind(i, level, poss.get(i));
        }
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

    @Override
    protected @NotNull InteractionResult onScrewdriverClick(@NotNull Player playerIn, @NotNull InteractionHand hand, @NotNull Direction gridSide, @NotNull BlockHitResult hitResult) {
        if (!super.onScrewdriverClick(playerIn, hand, gridSide, hitResult).shouldSwing()) {
            setRoundRobin(!roundRobin);
            playerIn.displayClientMessage(Component.translatable(roundRobin ? "tooltip.ad_astra.distribution_mode.round_robin" : "tooltip.ad_astra.distribution_mode.sequential"), true);
            return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                WidgetIcons.HIGHLIGHT, WidgetIcons.HIGHLIGHT, () -> false,
                (clickData, pressed) -> {
                    if (clickData.isRemote && getLevel() != null) {
                        HIGHLIGHTS.add(poss.stream().map(BlockPos::asLong).collect(ImmutableList.toImmutableList()), 200);
                    }
                })
                .setTooltipsSupplier(pressed -> Collections.singletonList(Component.translatable(HIGHLIGHT_TEXT))));
    }

    private void setRoundRobin(boolean roundRobin) {
        if (this.roundRobin == roundRobin) return;
        this.roundRobin = roundRobin;
        onChanged();
    }

    @Override
    public Widget createUIWidget() {
        var targets = new TesseractUI.Targets(this::getLevel, getPos(), this::cardTargets, poss::hashCode, Component.translatable(TesseractUI.ROW_EMPTY));
        var rows = new ArrayList<Widget>(MAX_TARGETS);
        for (int i = 0; i < MAX_TARGETS; i++) {
            rows.add(TesseractUI.row(ItemSlot.of(inventory.storage, i), targets, i, TesseractUI.face(targets, i)));
        }
        return TesseractUI.page(
                TesseractUI.status(() -> Component.translatable(TesseractUI.VALUE_BOUND, poss.size(), MAX_TARGETS), false, false),
                TesseractUI.listSection(TesseractUI.SECTION_TARGETS, TesseractUI.column(rows), TesseractUI.TARGETS_TOOLTIP, TesseractUI.TARGETS_MARKER_TOOLTIP),
                TesseractUI.roundRobinSection(() -> roundRobin, this::setRoundRobin));
    }

    private List<TesseractUI.Target> cardTargets() {
        var level = getLevel();
        var result = new ArrayList<TesseractUI.Target>(MAX_TARGETS);
        for (int i = 0; i < MAX_TARGETS; i++) {
            var card = inventory.storage.keyAt(i);
            var cardPos = level == null || card == null ? null : CoordinateCardBehavior.getStoredCoordinates(card.getReadOnlyStack());
            result.add(cardPos == null ? null : new TesseractUI.Target(GlobalPos.of(level.dimension(), cardPos), null));
        }
        return result;
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
    public TesseractTargets getRemoteTargets() {
        return remotes;
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
    public void onMachineRemoved() {
        clearInventory(inventory.storage);
    }

    @Override
    public boolean customPush() {
        return roundRobin;
    }

    @Override
    public IPatternProviderLogic.PushResult pushPattern(IPatternProviderLogic logic, IActionSource actionSource, BooleanHolder success, Operate operate, Set<AEKey> patternInputs, IPatternDetails patternDetails, ObjHolder<KeyCounter[]> inputHolder, Supplier<IPatternProviderLogic.PushResult> pushPatternSuccess, BooleanSupplier canPush, Direction direction, Direction adjBeSide) {
        var size = remotes.size();
        List<PatternProviderTarget> targets = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            var be = remotes.get(i).blockEntity();
            if (be == null) continue;
            var target = BlockingPatternTarget.find(be, logic, adjBeSide, actionSource, be.getBlockPos().asLong());
            if (target == null) continue;
            targets.add(target);
        }
        int count = 1000;
        while (count > 0) {
            count--;
            boolean done = true;
            for (var target : targets) {
                if (target.containsPatternInput(patternInputs)) continue;
                var result = operate.pushTarget(patternDetails, inputHolder, pushPatternSuccess, canPush, direction, target, false);
                if (result.success()) success.value = true;
                if (result.needBreak()) return result;
                if (result == IPatternProviderLogic.PushResult.SUCCESS) done = false;
            }
            if (done) break;
        }
        return IPatternProviderLogic.PushResult.NOWHERE_TO_PUSH;
    }

    @Override
    public boolean onMarkerInteract(Player player, List<TesseractDirectedTarget> targets) {
        if (getLevel() == null || getLevel().isClientSide()) {
            return true;
        }
        if (targets.isEmpty()) {
            player.displayClientMessage(Component.translatable(WRITE_EMPTY_TEXT), true);
            return true;
        }
        int availableCards = 0;
        for (int slot = 0; slot < MAX_TARGETS; slot++) {
            if (inventory.storage.amountAt(slot) > 0) availableCards++;
        }
        inventory.storage.clear();
        var iterator = targets.iterator();
        int i = 0;
        int skipped = 0;
        while (iterator.hasNext()) {
            var target = iterator.next();
            if (i >= MAX_TARGETS) break;
            if (target.pos().dimension() != getLevel().dimension()) {
                skipped++;
                continue;
            }
            var pos = target.pos().pos();
            if (pos.equals(getPos())) {
                continue;
            }
            ItemStack card = ItemStack.EMPTY;
            if (availableCards > 0) {
                availableCards--;
                card = GTOItems.COORDINATE_CARD.asItem().getDefaultInstance();
            }
            if (card.isEmpty()) {
                var idx = player.getInventory().findSlotMatchingItem(GTOItems.COORDINATE_CARD.asItem().getDefaultInstance());
                if (idx < 0) {
                    card = ItemStack.EMPTY;
                } else {
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
            CompoundTag posTags = card.getOrCreateTag();
            posTags.putInt("x", pos.getX());
            posTags.putInt("y", pos.getY());
            posTags.putInt("z", pos.getZ());
            inventory.storage.set(i, AEItemKey.of(card), card.getCount());
            i++;
        }
        if (availableCards > 0) {
            for (; availableCards > 0; availableCards--) {
                Block.popResource(getLevel(), getPos(), GTOItems.COORDINATE_CARD.asItem().getDefaultInstance());
            }
        }
        player.displayClientMessage(skipped > 0 ? Component.translatable(WRITE_SKIPPED_TEXT, skipped) : Component.translatable(WRITE_SUCCESS_TEXT), true);
        return true;
    }

    @Override
    public List<TesseractDirectedTarget> getMarkerTargets() {
        ImmutableList.Builder<TesseractDirectedTarget> builder = ImmutableList.builder();
        var levelKey = getLevel().dimension();
        for (int i = 0; i < poss.size(); i++) {
            var pos = poss.get(i);
            if (pos == null) continue;
            var exposedInAirFace = Direction.NORTH;
            for (var face : Direction.values()) {
                var offsetPos = pos.relative(face);
                if (getLevel().getBlockState(offsetPos).isAir()) {
                    exposedInAirFace = face;
                    break;
                }
            }
            builder.add(new TesseractDirectedTarget(GlobalPos.of(levelKey, pos), exposedInAirFace, i));
        }
        return builder.build();
    }
}

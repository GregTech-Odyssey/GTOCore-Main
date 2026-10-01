package com.gtocore.common.machine.tesseract;

import com.gtolib.api.ae2.AEKeyTypeMap;
import com.gtolib.api.ae2.IPatternProviderLogic;
import com.gtolib.api.ae2.PatternProviderTargetCache;
import com.gtolib.api.ae2.machine.ICustomCraftingMachine;
import com.gtolib.utils.ServerUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.core.ILevel;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ItemCell;
import com.gregtechceu.gtceu.uipro.elements.ServerList;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;
import com.gregtechceu.gtceu.uiwidgets.icon.WidgetIcons;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.crafting.pattern.AEProcessingPattern;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.me.storage.CompositeStorage;
import appeng.me.storage.ExternalStorageFacade;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.gto.datasynclib.util.holder.BooleanHolder;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class DirectedTesseractMachine extends MetaMachine implements
                                      IFancyUIMachine,
                                      IMachineLife,
                                      ICustomCraftingMachine,
                                      IMultiTesseract {

    public static final Multiset<ImmutableList<TesseractDirectedTarget>> HIGHLIGHTS = HashMultiset.create();

    @Getter
    private final List<ICustomItemStackHandler> itemHandlers = new ArrayList<>(20);
    @Getter
    private final List<ICustomFluidStackHandler> fluidHandlers = new ArrayList<>(20);

    @Getter
    @Setter
    private boolean called;

    @SaveToDisk
    @SyncToClient
    @Getter
    public final List<TesseractDirectedTarget> targets;

    @SaveToDisk
    private final List<TesseractDirectedTarget> unfinishedPushes = new ArrayList<>();
    @SaveToDisk
    private final List<GenericStack> unfinishedStacks = new ArrayList<>();

    private WeakReference<BlockEntity>[] blockEntityReference;
    private final ConditionalSubscriptionHandler task;

    @SuppressWarnings("unchecked")
    private static WeakReference<BlockEntity>[] createBlockEntityReferences(int size) {
        return (WeakReference<BlockEntity>[]) new WeakReference<?>[size];
    }

    public DirectedTesseractMachine(MetaMachineBlockEntity holder) {
        super(holder);
        targets = new ArrayList<>();
        task = new ConditionalSubscriptionHandler(this, this::push, 20, this::hasWorkToDo);
    }

    @Override
    public boolean customPush() {
        return true;
    }

    public void setTargets(Collection<TesseractDirectedTarget> newTargets) {
        targets.clear();
        targets.addAll(newTargets);
        targets.sort(TesseractDirectedTarget.SORTER);
        blockEntityReference = createBlockEntityReferences(targets.size());
        onChanged();
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return widget instanceof MachineWindow window ? createPage(window) : createUIWidget();
    }

    @Override
    public Widget createUIWidget() {
        return createPage(null);
    }

    private Widget createPage(@Nullable MachineWindow window) {
        boolean remote = isRemote();
        var view = new TesseractUI.Targets(this::getLevel, getPos(), this::uiTargets, targets::hashCode, Component.empty());
        if (window != null) {
            window.registerPopup(TesseractUI.FACE_POPUP, index -> remote || index < targets.size() ?
                    TesseractUI.facePopup(view, index, () -> index < targets.size() ? targets.get(index).face() : null, face -> setTargetFace(index, face)) : null);
        }
        var rows = ServerList.of(ByteStreamCodec.INT_CODEC, () -> indices(targets.size()), index -> targetRow(view, index, window))
                .version(targets::size).emptyText(TesseractUI.DIRECTED_EMPTY);
        rows.layout(l -> l.paddingTop(1).paddingBottom(1));
        var pending = ServerList.of(ByteStreamCodec.INT_CODEC, () -> indices(unfinishedStacks.size()), index -> pendingRow(view, index))
                .version(unfinishedStacks::size);
        var status = TesseractUI.status(() -> Component.translatable(TesseractUI.VALUE_UNLIMITED, targets.size()), true, true);
        status.addLine(TesseractUI.LINE_PENDING, () -> hasWorkToDo() ? Component.translatable(TesseractUI.VALUE_PENDING, unfinishedStacks.size()) : Component.translatable(TesseractUI.VALUE_PENDING_NONE))
                .bindLevel(() -> hasWorkToDo() ? Level.WARNING : Level.NORMAL)
                .tooltips(TesseractUI.PENDING_DETAIL);
        return TesseractUI.page(status,
                TesseractUI.listSection(TesseractUI.SECTION_DIRECTED_TARGETS, rows, TesseractUI.DIRECTED_TARGETS_TOOLTIP, TesseractUI.DIRECTED_READ_TOOLTIP),
                TesseractUI.directedPushSection(targets::size, pending));
    }

    private UIElement targetRow(TesseractUI.Targets view, int index, @Nullable MachineWindow window) {
        var number = Component.literal(String.valueOf(index + 1));
        var badge = TesseractUI.index(() -> number, Component.translatable(TesseractUI.DIRECTED_BADGE, index + 1));
        var face = TesseractUI.face(view, index);
        if (window != null) TesseractUI.bindFacePopup(face, window, index);
        return TesseractUI.row(badge, view, index, face);
    }

    private void setTargetFace(int index, Direction face) {
        if (index < 0 || index >= targets.size()) return;
        var target = targets.get(index);
        if (target.face() == face) return;
        targets.set(index, new TesseractDirectedTarget(target.pos(), face, target.order()));
        onChanged();
    }

    private UIElement pendingRow(TesseractUI.Targets view, int index) {
        var text = TextLine.of(0, () -> pendingText(view, index)).bindClientColor(UITheme::panelText);
        text.layout(l -> l.flex(1));
        return UIElement.centeredRow(UISizes.SLOT_SIZE)
                .addChildren(ItemCell.of(() -> pendingStack(index)), text);
    }

    private ItemStack pendingStack(int index) {
        if (index >= unfinishedStacks.size()) return ItemStack.EMPTY;
        var stack = unfinishedStacks.get(index);
        return stack.what() instanceof AEItemKey item ? item.toStack() : GenericStack.wrapInItemStack(stack.what(), stack.amount());
    }

    private Component pendingText(TesseractUI.Targets view, int index) {
        if (index >= unfinishedStacks.size() || index >= unfinishedPushes.size()) return Component.empty();
        var stack = unfinishedStacks.get(index);
        int target = targets.indexOf(unfinishedPushes.get(index));
        var name = target < 0 ? Component.literal("—") : view.get(target).name();
        return Component.translatable(TesseractUI.PENDING_ROW, stack.what().formatAmount(stack.amount(), AmountFormat.FULL), target + 1, name);
    }

    private List<TesseractUI.Target> uiTargets() {
        var result = new ArrayList<TesseractUI.Target>(targets.size());
        for (var target : targets) result.add(new TesseractUI.Target(target.pos(), target.face()));
        return result;
    }

    private static List<Integer> indices(int count) {
        var result = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) result.add(i);
        return result;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        task.unsubscribe();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        task.initialize(getLevel());
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                WidgetIcons.HIGHLIGHT, WidgetIcons.HIGHLIGHT, () -> false,
                (clickData, pressed) -> {
                    if (clickData.isRemote && getLevel() != null) {
                        HIGHLIGHTS.add(ImmutableList.copyOf(targets), 200);
                    }
                })
                .setTooltipsSupplier(pressed -> Collections.singletonList(Component.translatable(HIGHLIGHT_TEXT))));
    }

    public BlockEntity getBlockEntity(int index) {
        if (blockEntityReference == null) {
            if (targets.isEmpty()) {
                return null;
            }
            blockEntityReference = createBlockEntityReferences(targets.size());
        }
        if (blockEntityReference[index] != null) {
            var be = blockEntityReference[index].get();
            if (be != null) {
                return be.isRemoved() ? null : be;
            }
        }
        var target = targets.get(index);
        var dim = ServerUtils.getServer().getLevel(target.pos().dimension());
        if (dim == null) {
            return null;
        }
        var be = ILevel.getCachedBlockEntity(dim, target.pos().pos());
        blockEntityReference[index] = new WeakReference<>(be);
        if (be != null) {
            return be.isRemoved() ? null : be;
        }
        return null;
    }

    @Override
    public IPatternProviderLogic.PushResult pushPattern(IPatternProviderLogic logic, IActionSource actionSource, BooleanHolder success, Operate operate, Set<AEKey> patternInputs, IPatternDetails patternDetails, ObjHolder<KeyCounter[]> inputHolder, Supplier<IPatternProviderLogic.PushResult> pushPatternSuccess, BooleanSupplier canPush, Direction direction, Direction adjBeSide) {
        if (!(patternDetails instanceof AEProcessingPattern processingPattern))
            return IPatternProviderLogic.PushResult.REJECTED;
        var sparseInputs = processingPattern.getSparseInputs();

        if (hasWorkToDo() ||
                targets.isEmpty() ||
                sparseInputs.length > targets.size()) {
            return IPatternProviderLogic.PushResult.NOWHERE_TO_PUSH;
        }

        Map<TesseractDirectedTarget, GenericStack> remainingStacks = new O2OOpenCacheHashMap<>(sparseInputs.length);
        Map<PatternProviderTarget, GenericStack> readyToPushStacks = new O2OOpenCacheHashMap<>(sparseInputs.length);
        for (var i = 0; i < sparseInputs.length; i++) {
            var targetAt = targets.get(i);
            var be = getBlockEntity(i);
            var subPushStack = sparseInputs[i];
            if (subPushStack == null) continue;
            if (be == null) {
                return IPatternProviderLogic.PushResult.NOWHERE_TO_PUSH;
            }
            var toPush = PatternProviderTargetCache.find(be, logic, targetAt.face(), actionSource, targetAt.pos().pos().asLong());
            if (toPush == null) {
                return IPatternProviderLogic.PushResult.NOWHERE_TO_PUSH;
            }
            var blocked = toPush.containsPatternInput(patternInputs);
            if (blocked) {
                return IPatternProviderLogic.PushResult.NOWHERE_TO_PUSH;
            }
            var haveEnoughSpace = toPush.insert(subPushStack.what(), subPushStack.amount(), Actionable.SIMULATE) == subPushStack.amount();
            if (!haveEnoughSpace) {
                remainingStacks.put(targetAt, subPushStack);
                continue;
            }
            readyToPushStacks.put(toPush, subPushStack);
        }

        remainingStacks.forEach(this::addTask);
        readyToPushStacks.forEach((toPush, stack) -> toPush.insert(stack.what(), stack.amount(), Actionable.MODULATE));
        this.push();
        return pushPatternSuccess.get();
    }

    @Override
    public int getTotalBlockEntities() {
        return targets.size();
    }

    @Override
    public Direction getSideForBlockEntity(int i, @Nullable Direction side) {
        return targets.get(i).face();
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
        setTargets(targets);
        player.displayClientMessage(Component.translatable(WRITE_SUCCESS_TEXT), true);
        return true;
    }

    @Override
    public List<TesseractDirectedTarget> getMarkerTargets() {
        return targets;
    }

    @Override
    public void onMachineRemoved() {
        this.unfinishedStacks.forEach(stack -> {
            if (getLevel() != null && stack.what() instanceof AEItemKey item) {
                Block.popResource(getLevel(), getHolder().getBlockPos(), item.toStack(Math.toIntExact(stack.amount())));
            }
        });
    }

    private static MEStorage getMEStorage(TesseractDirectedTarget target, MinecraftServer levelGetter) {
        var dim = levelGetter.getLevel(target.pos().dimension());
        if (dim == null) {
            return null;
        }
        var be = ILevel.getCachedBlockEntity(dim, target.pos().pos());
        if (be == null) {
            return null;
        }
        var item = be.getCapability(ForgeCapabilities.ITEM_HANDLER, target.face()).map(ExternalStorageFacade::of).orElse(null);
        var fluid = be.getCapability(ForgeCapabilities.FLUID_HANDLER, target.face()).map(ExternalStorageFacade::of).orElse(null);
        if (item != null && fluid != null) return new CompositeStorage(new AEKeyTypeMap<>(item, fluid));
        if (item != null) return item;
        return fluid;
    }

    void push() {
        if (getLevel() instanceof ServerLevel level) {
            task.updateSubscription();
            var server = level.getServer();
            for (int i = 0; i < unfinishedPushes.size(); i++) {
                var target = unfinishedPushes.get(i);
                var stack = unfinishedStacks.get(i);
                var meStorage = getMEStorage(target, server);
                if (meStorage != null) {
                    var inserted = meStorage.insert(stack.what(), stack.amount(), Actionable.MODULATE, IActionSource.empty());
                    if (inserted == stack.amount()) {
                        unfinishedPushes.remove(i);
                        unfinishedStacks.remove(i);
                        i--;
                    } else {
                        unfinishedStacks.set(i, new GenericStack(stack.what(), stack.amount() - inserted));
                    }
                }
            }
        }
    }

    boolean hasWorkToDo() {
        return !unfinishedPushes.isEmpty();
    }

    void addTask(TesseractDirectedTarget target, GenericStack stack) {
        unfinishedPushes.add(target);
        unfinishedStacks.add(stack);
        onChanged();
        task.updateSubscription();
    }
}

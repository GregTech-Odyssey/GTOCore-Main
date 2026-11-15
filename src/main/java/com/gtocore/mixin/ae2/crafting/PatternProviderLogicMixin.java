package com.gtocore.mixin.ae2.crafting;

import com.gtocore.integration.ae.PatternContainerGroupHelper;

import com.gtolib.api.ae2.*;
import com.gtolib.api.ae2.machine.PatternTargetSource;
import com.gtolib.api.blockentity.IDirectionCacheBlockEntity;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Nameable;

import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.localization.GuiText;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.helpers.patternprovider.PatternProviderTarget;
import appeng.helpers.patternprovider.PatternProviderTargetCache;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = PatternProviderLogic.class, remap = false)
public abstract class PatternProviderLogicMixin implements IPatternProviderLogic {

    @Final
    @Shadow
    private PatternProviderLogicHost host;
    @Final
    @Shadow
    private IManagedGridNode mainNode;
    @Final
    @Shadow
    private IActionSource actionSource;
    @Final
    @Shadow
    private ConfigManager configManager;
    @Final
    @Shadow
    private List<IPatternDetails> patterns;
    @Final
    @Shadow
    private Set<AEKey> patternInputs;
    @Shadow
    @Final
    private AppEngInternalInventory patternInventory;
    @Shadow
    @Final
    private List<GenericStack> sendList;
    @Shadow
    private Direction sendDirection;
    @Unique
    private PatternTarget[] gtolib$targets;
    @Unique
    private PatternTarget[] gtolib$single;
    @Unique
    private int[] gtolib$counts;
    @Unique
    private int gtolib$cursor;
    @Unique
    private PatternTarget.Overflow gtolib$overflow;
    @Unique
    private GlobalPos gto$pos;

    @Shadow
    public abstract LockCraftingMode getCraftingLockedReason();

    @Shadow
    protected abstract Set<Direction> getActiveSides();

    @Shadow
    public abstract PatternContainerGroup getTerminalGroup();

    @Shadow
    protected abstract void onPushPatternSuccess(IPatternDetails pattern);

    @Shadow
    protected abstract boolean adapterAcceptsAll(PatternProviderTarget target, KeyCounter[] inputHolder);

    @Shadow
    protected abstract void addToSendList(AEKey what, long amount);

    @Shadow
    public abstract PatternProviderTargetCache getTargetCache(Direction side);

    @Inject(method = "<init>(Lappeng/api/networking/IManagedGridNode;Lappeng/helpers/patternprovider/PatternProviderLogicHost;I)V", at = @At("TAIL"), remap = false)
    private void init(IManagedGridNode mainNode, PatternProviderLogicHost host, int patternInventorySize, CallbackInfo ci) {
        configManager.registerSetting(GTOSettings.BLOCKING_TYPE, BlockingType.NONE);
    }

    @Inject(method = "getTerminalGroup", at = @At("HEAD"), cancellable = true, remap = false)
    private void gto$getTerminalGroup(CallbackInfoReturnable<PatternContainerGroup> cir) {
        if (!(host instanceof Nameable nameable) || !nameable.hasCustomName()) {
            return;
        }

        String customName = nameable.getCustomName().getString();
        if (!customName.startsWith("+") || customName.length() == 1) {
            return;
        }

        String extraSuffix = customName.substring(1).strip();
        var suffix = extraSuffix.isEmpty() ? Component.empty() : Component.literal(" ").append(extraSuffix);
        var blockEntity = host.getBlockEntity();
        var level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        var groups = new ObjectLinkedOpenHashSet<PatternContainerGroup>();
        for (Direction side : getActiveSides()) {
            var adjacentPos = blockEntity.getBlockPos().relative(side);
            var group = PatternContainerGroupHelper.fromMachine(level, adjacentPos, extraSuffix);
            if (group == null) {
                var fallbackGroup = PatternContainerGroup.fromMachine(level, adjacentPos, side.getOpposite());
                if (fallbackGroup != null) {
                    group = new PatternContainerGroup(fallbackGroup.icon(), fallbackGroup.name().copy().append(suffix), fallbackGroup.tooltip());
                }
            }
            if (group != null) {
                groups.add(group);
            }
        }

        if (groups.size() == 1) {
            cir.setReturnValue(groups.getFirst());
            return;
        }

        List<Component> tooltip = Collections.emptyList();
        if (groups.size() > 1) {
            tooltip = new ArrayList<>();
            tooltip.add(GuiText.AdjacentToDifferentMachines.text().withStyle(net.minecraft.ChatFormatting.BOLD));
            for (var group : groups) {
                tooltip.add(group.name());
                for (var line : group.tooltip()) {
                    tooltip.add(Component.literal("  ").append(line));
                }
            }
        }

        var hostIcon = host.getTerminalIcon();
        cir.setReturnValue(new PatternContainerGroup(hostIcon, hostIcon.getDisplayName().copy().append(suffix), tooltip));
    }

    @Override
    public PushResult gtolib$pushPattern(PushBatch batch) {
        if (!this.mainNode.isActive()) return PushResult.GRID_NODE_MISSING;
        var details = batch.details();
        if (!this.patterns.contains(details)) return PushResult.PATTERN_DOES_NOT_EXIST;
        var be = host.getBlockEntity();
        var cache = IDirectionCacheBlockEntity.getBlockEntityDirectionCache(be);
        if (cache == null) return PushResult.REJECTED;
        if (!gtolib$canPush()) return PushResult.PATTERN_PROVIDER_LOCKED;
        var level = be.getLevel();
        var pos = be.getBlockPos();
        var mode = configManager.getSetting(GTOSettings.BLOCKING_TYPE);
        boolean molecular = !details.supportsPushInputsToExternalInventory();
        boolean pushed = false;
        for (var direction : getActiveSides()) {
            if (batch.inputs() == null || !gtolib$canPush()) break;
            var adjBe = cache.getAdjacentBlockEntity(level, pos, direction);
            if (adjBe == null) continue;
            var adjBeSide = direction.getOpposite();
            if (molecular) {
                var craftingMachine = ICraftingMachine.of(level, pos.relative(direction), adjBeSide, adjBe);
                if (craftingMachine != null && gtolib$pushCraftingMachine(craftingMachine, batch, adjBeSide)) pushed = true;
            } else if (adjBe instanceof MetaMachineBlockEntity machineBlockEntity && machineBlockEntity.metaMachine instanceof PatternTargetSource source) {
                var targets = source.patternTargets(adjBeSide, actionSource);
                if (targets == null) targets = gtolib$single(gtolib$findTarget(direction));
                if (gtolib$pushTargets(targets, batch, mode, direction)) pushed = true;
            } else {
                var target = gtolib$findTarget(direction);
                if (target != null && gtolib$pushTargets(gtolib$single(target), batch, mode, direction)) pushed = true;
            }
        }
        return pushed ? PushResult.SUCCESS : PushResult.NOWHERE_TO_PUSH;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return gtolib$pushPattern(new PushBatch.Single(patternDetails, inputHolder)).success();
    }

    @Unique
    private boolean gtolib$canPush() {
        return getCraftingLockedReason() == LockCraftingMode.NONE && sendList.isEmpty();
    }

    @Unique
    private boolean gtolib$pushCraftingMachine(ICraftingMachine craftingMachine, PushBatch batch, Direction adjBeSide) {
        var details = batch.details();
        boolean pushed = false;
        KeyCounter[] inputs;
        while ((inputs = batch.inputs()) != null && gtolib$canPush() && craftingMachine.acceptsPlans() && craftingMachine.pushPattern(details, inputs, adjBeSide)) {
            onPushPatternSuccess(details);
            pushed = true;
            if (!batch.next()) break;
        }
        return pushed;
    }

    @Unique
    private boolean gtolib$pushTargets(PatternTarget[] targets, PushBatch batch, BlockingType mode, Direction direction) {
        var details = batch.details();
        int n = targets.length;
        var counts = gtolib$counts;
        if (counts == null || counts.length < n) gtolib$counts = counts = new int[Math.max(n, 6)];
        int open = 0;
        for (int i = 0; i < n; i++) {
            var target = targets[i];
            if (target == null || mode.rule.blocks(target, details, patternInputs)) {
                counts[i] = -1;
            } else {
                counts[i] = 0;
                open++;
            }
        }
        var overflow = gtolib$overflow;
        if (overflow == null) gtolib$overflow = overflow = this::addToSendList;
        boolean pushed = false;
        int start = n > 1 ? Math.floorMod(gtolib$cursor, n) : 0;
        while (open > 0) {
            for (int k = 0; k < n; k++) {
                int i = start + k;
                if (i >= n) i -= n;
                int count = counts[i];
                if (count < 0) continue;
                var inputs = batch.inputs();
                if (inputs == null || !gtolib$canPush()) return pushed;
                var target = targets[i];
                if (!adapterAcceptsAll(target.storage(), inputs)) {
                    counts[i] = -1;
                    open--;
                    continue;
                }
                target.push(details, inputs, overflow);
                onPushPatternSuccess(details);
                this.sendDirection = direction;
                if (n > 1) gtolib$cursor = i + 1;
                pushed = true;
                if (++count >= mode.limit) {
                    counts[i] = -1;
                    open--;
                } else {
                    counts[i] = count;
                }
                if (!batch.next()) return true;
            }
        }
        return pushed;
    }

    @Unique
    private PatternTarget[] gtolib$single(@Nullable PatternTarget target) {
        var single = gtolib$single;
        if (single == null) gtolib$single = single = new PatternTarget[1];
        single[0] = target;
        return single;
    }

    @Unique
    @Nullable
    private PatternTarget gtolib$findTarget(Direction side) {
        var cache = getTargetCache(side);
        var storage = cache.find();
        if (storage == null) return null;
        var blockEntity = cache.targetBlockEntity();
        if (blockEntity == null) return null;
        var targets = gtolib$targets;
        if (targets == null) gtolib$targets = targets = new PatternTarget[6];
        int index = side.get3DDataValue();
        var cached = targets[index];
        if (cached != null && cached.is(storage, blockEntity)) return cached;
        return targets[index] = new PatternTarget(storage, blockEntity);
    }

    @Override
    public BlockingType gtolib$getBlocking() {
        return configManager.getSetting(GTOSettings.BLOCKING_TYPE);
    }

    /**
     * @author xinxinsuried
     * @reason 更细时根据网络中的me整理机替换物品
     */
    @Overwrite
    public void updatePatterns() {
        patterns.clear();
        patternInputs.clear();

        for (var stack : this.patternInventory) {
            var details = MyPatternDetailsHelper.decodePattern(stack, this.host.getBlockEntity(), mainNode.getGrid());

            if (details != null) {
                patterns.add(details);

                for (var iinput : details.getInputs()) {
                    for (var inputCandidate : iinput.getPossibleInputs()) {
                        patternInputs.add(inputCandidate.what().dropSecondary());
                    }
                }
            }
        }

        ICraftingProvider.requestUpdate(mainNode);
    }

    @Override
    public GlobalPos gto$getPos() {
        if (gto$pos != null) return gto$pos;
        return gto$pos = GlobalPos.of(host.getBlockEntity().getLevel().dimension(), host.getBlockEntity().getBlockPos());
    }
}

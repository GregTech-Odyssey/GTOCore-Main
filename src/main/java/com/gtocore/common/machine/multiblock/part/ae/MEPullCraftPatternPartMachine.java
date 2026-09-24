package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.machine.multiblock.electric.AbstractMEPatternAssemblerMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 拉取样板仓（分子装配工厂专用）：只做被动补货，不向 AE 公布样板，合成任务不会发配到这里。
 * <p>
 * 每个样板槽可单独开启拉取并设置产物阈值（目标库存）：当「网络存量 + 本槽待交付 + 工厂待输出」低于阈值时，
 * 按差额（不超过网络原料能支撑的次数）从网络抽出原料，直接记为已合成产物，产物照常由工厂取走并送回网络。
 * <p>
 * 拉取只挂一个每 2 秒一次的服务端 tick，并且只在被多方块接入后（{@link #addedToController}）才挂：
 * 没进结构、没有槽开启拉取时不占任何 tick，既不占用 AE 的发配通道，也不改动发配流程。
 */
@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPullCraftPatternPartMachine extends MECraftPatternPartMachine {

    @RegisterLanguage(cn = "拉取", en = "Pull")
    private static final String PULL_TITLE = "gtocore.pull_pattern_hatch.title";
    @RegisterLanguage(cn = "拉取模式", en = "Pull Mode")
    private static final String PULL_MODE = "gtocore.pull_pattern_hatch.pull_mode";
    @RegisterLanguage(cn = "开启后，该样板的产物在网络里不足阈值时自动从网络拉取原料补足", en = "When enabled, ingredients are pulled from the network to top this pattern's product up to the threshold")
    private static final String PULL_MODE_TOOLTIP = "gtocore.pull_pattern_hatch.pull_mode_tooltip";
    @RegisterLanguage(cn = "产物阈值", en = "Product Threshold")
    private static final String PULL_THRESHOLD = "gtocore.pull_pattern_hatch.threshold";
    @RegisterLanguage(cn = "网络中该产物的目标库存量，不足时按差额拉取原料；0 表示不补货", en = "Target amount of this product in the network; the deficit is pulled when lower. 0 disables restocking")
    private static final String PULL_THRESHOLD_TOOLTIP = "gtocore.pull_pattern_hatch.threshold_tooltip";
    @RegisterLanguage(cn = "拉取模式：目标库存 %s", en = "Pull mode: target stock %s")
    private static final String PULL_SLOT_HINT = "gtocore.pull_pattern_hatch.slot_hint";

    @SaveToDisk(key = "pullModes", skipWhen = "noPullModes")
    @SyncToClient
    private final boolean[] pullModes = new boolean[getMaxPatternCount()];

    @SaveToDisk(key = "pullThresholds", skipWhen = "noPullThresholds")
    @SyncToClient
    private final long[] pullThresholds = new long[getMaxPatternCount()];

    @Nullable
    private TickableSubscription pullSubs;

    private final ReferenceOpenHashSet<AEKey> countedKeys = new ReferenceOpenHashSet<>();

    private final AEKeyMap<AEKey> extractedKeys = new AEKeyMap<>();

    public MEPullCraftPatternPartMachine(MetaMachineBlockEntity holder, int maxPatternCount) {
        super(holder, maxPatternCount);
    }

    public void setPullMode(int index, boolean enabled) {
        if (pullModes[index] == enabled) return;
        pullModes[index] = enabled;
        onChanged();
        refreshPullSubs();
    }

    public void setPullThreshold(int index, long threshold) {
        if (pullThresholds[index] == threshold) return;
        pullThresholds[index] = threshold;
        onChanged();
        refreshPullSubs();
    }

    /** 值和默认值一样时不必写盘。 */
    private boolean noPullModes(boolean[] modes) {
        for (boolean mode : modes) {
            if (mode) return false;
        }
        return true;
    }

    /** 值和默认值一样时不必写盘。 */
    private boolean noPullThresholds(long[] thresholds) {
        for (long threshold : thresholds) {
            if (threshold != 0) return false;
        }
        return true;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        refreshPullSubs();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        cancelPullSubs();
        super.removedFromController(controller);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        cancelPullSubs();
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable Component appendHoverTooltips(int index) {
        if (index < 0 || index >= pullModes.length || !pullModes[index]) return null;
        return Component.translatable(PULL_SLOT_HINT, pullThresholds[index]);
    }

    private void refreshPullSubs() {
        if (isRemote()) return;
        if (hasPullSlot() && isFormed()) {
            pullSubs = subscribeServerTick(pullSubs, this::pullTick, 40);
        } else {
            cancelPullSubs();
        }
    }

    private void cancelPullSubs() {
        if (pullSubs != null) {
            pullSubs.unsubscribe();
            pullSubs = null;
        }
    }

    private boolean hasPullSlot() {
        for (boolean mode : pullModes) {
            if (mode) return true;
        }
        return false;
    }

    private void pullTick() {
        if (!hasPullSlot() || !isFormed()) {
            cancelPullSubs();
            return;
        }
        var grid = getMainNode().getGrid();
        if (grid == null || !getMainNode().isActive()) return;
        if (!(getController() instanceof AbstractMEPatternAssemblerMachine assembler) || !assembler.isFormed()) return;
        var storage = grid.getStorageService().getInventory();
        var patternMap = getDetailsSlotMap().inverse();
        var slots = getInternalInventory();
        for (int i = 0; i < slots.length; i++) {
            if (!pullModes[i]) continue;
            long threshold = pullThresholds[i];
            if (threshold < 1) continue;
            var pattern = patternMap.get(slots[i]);
            if (pattern == null) continue;
            pullSlot(assembler, slots[i], pattern, threshold, storage);
        }
    }

    private void pullSlot(AbstractMEPatternAssemblerMachine assembler, InternalSlot slot, IPatternDetails pattern,
                          long threshold, MEStorage storage) {
        if (!(pattern instanceof IMolecularAssemblerSupportedPattern supported)) return;
        var outputs = supported.getOutputs();
        if (outputs.length != 1 || !(outputs[0].what() instanceof AEItemKey key)) return;
        long perCraft = outputs[0].amount();
        if (perCraft < 1) return;

        long stock = storage.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, getActionSourceField()) +
                slot.getAmount() + assembler.getPendingAmount(key);
        if (stock >= threshold) return;

        long deficit = threshold - stock;
        long crafts = deficit / perCraft + (deficit % perCraft == 0 ? 0 : 1);
        crafts = Math.min(crafts, maxCrafts(supported, storage));
        if (crafts < 1) return;
        if (!extractInputs(supported, crafts, storage)) return;
        slot.addOutput(key, perCraft * crafts);
    }

    private long maxCrafts(IPatternDetails pattern, MEStorage storage) {
        long max = Long.MAX_VALUE;
        for (var input : pattern.getInputs()) {
            long multiplier = input.getMultiplier();
            if (multiplier < 1) continue;
            countedKeys.clear();
            long available = 0;
            for (var candidate : input.getPossibleInputs()) {
                long templateAmount = candidate.amount();
                if (templateAmount < 1 || !countedKeys.add(candidate.what())) continue;
                available += storage.extract(candidate.what(), Long.MAX_VALUE, Actionable.SIMULATE, getActionSourceField()) / templateAmount;
            }
            max = Math.min(max, available / multiplier);
            if (max < 1) return 0;
        }
        return max;
    }

    private boolean extractInputs(IPatternDetails pattern, long crafts, MEStorage storage) {
        var source = getActionSourceField();
        extractedKeys.clear();
        for (var input : pattern.getInputs()) {
            long multiplier = input.getMultiplier();
            if (multiplier < 1) continue;
            var possibleInputs = input.getPossibleInputs();
            if (possibleInputs.length == 0) continue;
            boolean combine = possibleInputs.length > 1 && hasSameTemplateAmount(possibleInputs);
            long remaining = (combine ? multiplier * possibleInputs[0].amount() : multiplier) * crafts;
            for (var candidate : possibleInputs) {
                long perUnit = combine ? 1 : candidate.amount();
                if (perUnit < 1) continue;
                long extracted = storage.extract(candidate.what(), perUnit * remaining, Actionable.MODULATE, source);
                if (extracted <= 0) continue;
                extractedKeys.insert(candidate.what(), extracted);
                remaining -= extracted / perUnit;
                if (remaining <= 0) break;
            }
            if (remaining > 0) {
                for (var entry : extractedKeys) {
                    storage.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE, source);
                }
                return false;
            }
        }
        return true;
    }

    private static boolean hasSameTemplateAmount(GenericStack[] inputs) {
        long amount = inputs[0].amount();
        for (int i = 1; i < inputs.length; i++) {
            if (inputs[i].amount() != amount) return false;
        }
        return true;
    }

    @Override
    protected boolean supportsSlotConfig() {
        return true;
    }

    @Override
    protected void buildSlotConfig(UIElement column, int index) {
        var section = MEPatternPartUI.section(column, PULL_TITLE);
        int width = section.getContentWidth();
        var threshold = MEPatternPartUI.longField(UISizes.BUTTON_WIDTH, () -> pullThresholds[index], value -> setPullThreshold(index, value), 0);
        section.addChildren(
                MEPatternPartUI.labeledRow(width, PULL_MODE, PULL_MODE_TOOLTIP,
                        Switch.of(() -> pullModes[index], enabled -> setPullMode(index, enabled))),
                MEPatternPartUI.labeledRow(width, PULL_THRESHOLD, PULL_THRESHOLD_TOOLTIP, threshold));
    }
}

package com.gtocore.integration.ae;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.patternprovider.PatternProviderTarget;

import java.util.Set;

/**
 * Pattern-provider target used by GTO's NON_CONTAIN mode when an Advanced Blocking Card
 * is installed in the target ME Interface.
 *
 * The active-pattern state lives on InterfaceLogic. This class only routes insertion
 * directly into the interface subnet storage and bridges the blocking check.
 */
public final class AdvancedBlockingTarget implements PatternProviderTarget {

    private final MEStorage storage;
    private final IActionSource actionSource;
    private final InterfaceLogic interfaceLogic;
    private final AEItemKey patternDefinition;

    public AdvancedBlockingTarget(
                                  MEStorage storage,
                                  IActionSource actionSource,
                                  InterfaceLogic interfaceLogic,
                                  AEItemKey patternDefinition) {
        this.storage = storage;
        this.actionSource = actionSource;
        this.interfaceLogic = interfaceLogic;
        this.patternDefinition = patternDefinition;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode) {
        long inserted = storage.insert(what, amount, mode, actionSource);

        if (mode == Actionable.MODULATE && inserted > 0) {
            interfaceLogic.setAdvancedBlockingPattern(patternDefinition);
        }

        return inserted;
    }

    @Override
    public boolean containsPatternInput(Set<AEKey> patternInputs) {
        return !interfaceLogic.canStartAdvancedBlockingPattern(patternDefinition);
    }
}

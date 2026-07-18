package com.gtocore.integration.emi.research;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;

import dev.emi.emi.api.stack.EmiStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ResearchEmiStacks {

    private ResearchEmiStacks() {}

    public static @Nullable EmiStack toEmiStack(AEKey key) {
        return EmiStackHelper.toEmiStack(new GenericStack(key, key.getAmountPerOperation()));
    }

    public static List<EmiStack> toEmiStacks(Collection<AEKey> keys) {
        return keys.stream()
                .map(ResearchEmiStacks::toEmiStack)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing((EmiStack stack) -> stack.getId().toString())
                        .thenComparing(stack -> stack.getKey().getClass().getName()))
                .toList();
    }
}

package com.gtocore.mixin.ae2.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGridNode;
import appeng.me.service.helpers.NetworkCraftingProviders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(value = NetworkCraftingProviders.class, remap = false)
public interface NetworkCraftingProvidersAccessor {

    @Accessor("craftingProviders")
    Map<IGridNode, ?> gto$getProviderStates();

    @Mixin(targets = "appeng.me.service.helpers.NetworkCraftingProviders$ProviderState", remap = false)
    interface ProviderStateAccessor {

        @Accessor("patterns")
        List<IPatternDetails> gto$getMountedPatterns();
    }
}

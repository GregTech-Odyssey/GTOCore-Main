package com.gtocore.mixin.ae2.pattern;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.GenericStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "appeng.crafting.pattern.AEPatternHelper")
public class AEPatternHelperMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static GenericStack[] condenseStacks(GenericStack[] sparseInput) {
        var map = new AEKeyMap<AEKey>();
        for (var input : sparseInput) {
            if (input == null || input.amount() == 0) continue;
            map.addTo(input.what(), input.amount());
        }
        if (map.isEmpty()) {
            throw new IllegalStateException("No pattern here!");
        }
        GenericStack[] out = new GenericStack[map.size()];
        int i = 0;
        for (var it = map.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            out[i++] = new GenericStack(entry.getKey(), entry.getLongValue());
        }
        return out;
    }
}

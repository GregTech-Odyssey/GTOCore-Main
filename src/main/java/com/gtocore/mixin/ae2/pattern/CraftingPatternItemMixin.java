package com.gtocore.mixin.ae2.pattern;

import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.AECraftingPattern;
import appeng.crafting.pattern.CraftingPatternItem;

import com.gto.fastcollection.cache.WeakValueIdentityHashCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CraftingPatternItem.class)
public class CraftingPatternItemMixin {

    @Unique
    private static final WeakValueIdentityHashCache<AEItemKey, AECraftingPattern> gtolib$CACHE = new WeakValueIdentityHashCache<>();

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public AECraftingPattern decode(AEItemKey what, Level level) {
        if (what == null || !what.hasTag()) {
            return null;
        }

        try {
            return gtolib$CACHE.getCache(what, k -> new AECraftingPattern(k, level));
        } catch (Exception e) {
            return null;
        }
    }
}

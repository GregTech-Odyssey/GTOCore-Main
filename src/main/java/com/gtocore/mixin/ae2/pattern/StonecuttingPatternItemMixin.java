package com.gtocore.mixin.ae2.pattern;

import net.minecraft.world.level.Level;

import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.AEStonecuttingPattern;
import appeng.crafting.pattern.StonecuttingPatternItem;

import com.gto.fastcollection.cache.WeakValueIdentityHashCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StonecuttingPatternItem.class)
public class StonecuttingPatternItemMixin {

    @Unique
    private static final WeakValueIdentityHashCache<AEItemKey, AEStonecuttingPattern> gtolib$CACHE = new WeakValueIdentityHashCache<>();

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public AEStonecuttingPattern decode(AEItemKey what, Level level) {
        if (what == null || !what.hasTag()) {
            return null;
        }

        try {
            return gtolib$CACHE.getCache(what, k -> new AEStonecuttingPattern(k, level));
        } catch (Exception e) {
            return null;
        }
    }
}

package com.gtocore.mixin.arsnouveau;

import com.hollingsworth.arsnouveau.common.block.tile.VolcanicSourcelinkTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(VolcanicSourcelinkTile.class)
public class VolcanicSourcelinkTileMixin {

    @ModifyConstant(method = "tick", remap = false, constant = @Constant(longValue = 20))
    private long modifyTick(long constant) {
        return 80;
    }
}
